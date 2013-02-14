/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.web.install;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.sos.ds.ISettingsDao;
import org.n52.sos.ds.hibernate.util.DefaultHibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.service.AdminUser;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.JdbcUrl;
import org.n52.sos.web.MetaDataHandler;
import org.n52.sos.web.SqlUtils;
import org.n52.sos.web.admin.auth.UserService;
import org.n52.sos.web.install.InstallConstants.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_FINISH)
public class InstallFinishController extends AbstractProcessingInstallationController {
    
    @Autowired
    private UserService userService;

    @Override
    protected Step getStep() {
        return Step.FINISH;
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(HttpServletRequest req, HttpServletResponse resp) throws InstallationRedirectError, InstallationSettingsError {
        HttpSession session = checkPrevious(req);
        process(getParameters(req), getSettings(session));
        session.invalidate();
        return redirect(ControllerConstants.Paths.INDEX + "?install=finished");
    }
    
    @Override
    protected void process(Map<String, String> param, InstallationConfiguration c) throws InstallationSettingsError {
        checkUsername(param, c);
        checkPassword(param, c);

        Properties properties = createHibernateProperties(c);

        loadDriver(properties, c);
        Connection con = null;
        try {
            con = createConnection(properties);
            createTables(c, con);
            insertTestData(c, con);
            insertSettings(c, con);
            saveAdmin(c, properties);
        } catch (SQLException e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_CONNECT_TO_THE_DATABASE, e.getMessage()));
        } finally {
            SqlUtils.close(con);
        }
        instantiateConfigurator(properties, c);
        saveDatabaseProperties(properties, c);
        saveInstallationDate();
    }
    
    protected Properties createHibernateProperties(InstallationConfiguration c) throws InstallationSettingsError {
        Properties p = checkJdbcUrl(c).toProperties();
        p.put(DefaultHibernateConstants.DRIVER_PROPERTY, (String) c.getDatabaseSetting(InstallConstants.DRIVER_PARAMETER));
        p.put(DefaultHibernateConstants.CONNECTION_POOL_PROPERTY, (String) c.getDatabaseSetting(InstallConstants.CONNECTION_POOL_PARAMETER));
        p.put(DefaultHibernateConstants.DIALECT_PROPERTY, (String) c.getDatabaseSetting(InstallConstants.JDBC_DIALECT_PARAMETER));
        return p;
    }
    
    protected void checkUsername(Map<String, String> param, InstallationConfiguration c) throws InstallationSettingsError {
        String username = param.get(HibernateConstants.ADMIN_USERNAME_KEY);
        if (username == null || username.trim().isEmpty()) {
            throw new InstallationSettingsError(c, ErrorMessages.USERNAME_IS_INVALID);
        }
        c.setUsername(username);
    }

    protected void checkPassword(Map<String, String> param, InstallationConfiguration c) throws InstallationSettingsError {
        String password = param.get(HibernateConstants.ADMIN_PASSWORD_KEY);
        if (password == null || password.trim().isEmpty()) {
            throw new InstallationSettingsError(c, ErrorMessages.PASSWORD_IS_INVALID);
        }
        c.setPassword(password);
    }

    protected void instantiateConfigurator(Properties properties, InstallationConfiguration c) throws InstallationSettingsError {
        /* instantiate sos configurator */
        if (Configurator.getInstance() == null) {
            log.info("Instantiating Configurator...");
            try {
                Configurator.createInstance(properties, getBasePath());
            } catch (ConfigurationException ex) {
                throw new InstallationSettingsError(c, String.format(ErrorMessages.CANNOT_INSTANTIATE_CONFIGURATOR, ex
                        .getMessage()), ex);
            }
        } else {
            log.error("Configurator seems to be already instantiated...");
        }
    }

    protected void saveDatabaseProperties(Properties properties, InstallationConfiguration c) throws InstallationSettingsError {
        try {
            getDatabaseSettingsHandler().saveAll(properties);
        } catch (ConfigurationException e) {
            /* TODO desctruct configurator? */
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_WRITE_DATASOURCE_CONFIG, e.getMessage()));
        }
    }

    protected void saveInstallationDate() {
        try {
            /* save the installation date (same format as maven svn buildnumber plugin produces) */
            DateTimeFormatter f = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            getMetaDataHandler().save(MetaDataHandler.Metadata.INSTALL_DATE,
                                      f.print(new DateTime()));
        } catch (ConfigurationException ex) {
            /* don't fail on this one */
            log.error("Error saveing installation date", ex);
        }
    }

    protected void createTables(InstallationConfiguration c, Connection con) throws InstallationSettingsError {
        /* create tables */
        Boolean createTables = (Boolean) c.getDatabaseSetting(InstallConstants.CREATE_TABLES_PARAMETER);
        if (createTables.booleanValue()) {
            try {
                SqlUtils.executeSQLFile(con,
                                        new File(getContext().getRealPath(
                        ControllerConstants.CREATE_DATAMODEL_SQL_FILE)));
            } catch (Exception e) {
                throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_CREATE_SOS_TABLES, e.getMessage()));
            }
        }
    }

    protected void insertTestData(InstallationConfiguration c, Connection con) throws InstallationSettingsError {
        /* insert test data */
        Boolean createTestData = (Boolean) c.getDatabaseSetting(InstallConstants.CREATE_TEST_DATA_PARAMETER);
        if (createTestData.booleanValue()) {
            try {
                SqlUtils.executeSQLFile(con,
                                        new File(getContext().getRealPath(
                        ControllerConstants.INSERT_TEST_DATA_SQL_FILE)));
            } catch (Exception e) {
                throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_INSERT_TEST_DATA, e.getMessage()));
            }
        }
    }

    protected void insertSettings(InstallationConfiguration c, Connection con) throws InstallationSettingsError {
        /* insert properties into the database */
        try {
            ISettingsDao dao = ServiceLoader.load(ISettingsDao.class).iterator().next();
            Map<String, String> s = new HashMap<String, String>(c.getSettings());
            try {
                s.put(MetaDataHandler.Metadata.VERSION.name(),
                      getMetaDataHandler().get(MetaDataHandler.Metadata.VERSION));
            } catch (ConfigurationException ex) {
                /* don't fail on this one... */
                log.error("Could not load SOS version", ex);
            }

            dao.save(s, con);
        } catch (SQLException e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_INSERT_SETTINGS, e.getMessage()), e);
        }
    }

    protected void saveAdmin(InstallationConfiguration c, Properties properties) throws InstallationSettingsError {
        /* save admin credentials */
        try {
            userService.saveAdmin(new AdminUser(c.getUsername(), c.getPassword()), properties);
        } catch (Throwable e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_SAVE_ADMIN_CREDENTIALS, e.getMessage()));
        }
    }

    protected JdbcUrl checkJdbcUrl(InstallationConfiguration c) throws InstallationSettingsError {
        String connectionString = (String) c.getDatabaseSetting(ControllerConstants.JDBC_PARAMETER);
        JdbcUrl jdbc;
        try {
            jdbc = new JdbcUrl(connectionString);
        } catch (URISyntaxException ex) {
            throw new InstallationSettingsError(c, ex.getMessage());
        }
        String error = jdbc.isValid();
        if (error != null) {
            throw new InstallationSettingsError(c, error);
        }
        return jdbc;
    }

    protected void loadDriver(Properties properties, InstallationConfiguration c) throws InstallationSettingsError {
        try {
            Class.forName(properties.getProperty(DefaultHibernateConstants.DRIVER_PROPERTY));
        } catch (ClassNotFoundException e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_CONNECT_TO_THE_DATABASE, e.getMessage()));
        }
    }

    private Connection createConnection(Properties properties) throws SQLException {
        return DriverManager.getConnection(
             properties.getProperty(DefaultHibernateConstants.CONNECTION_STRING_PROPERTY),
             properties.getProperty(DefaultHibernateConstants.USER_PROPERTY),
             properties.getProperty(DefaultHibernateConstants.PASS_PROPERTY));
    }
}
