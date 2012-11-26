/**
 * Copyright (C) 2012
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import org.n52.sos.ds.ISettingsDao;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.service.AdminUser;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.Setting;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.SqlUtils;
import org.n52.sos.web.admin.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_FINISH)
public class InstallFinishController extends AbstractInstallController {

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_INDEX, true));
        }
        else if (session.getAttribute(InstallConstants.DBCONFIG_COMPLETE) == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_DATABASE_CONFIGURATION, true));
        }
        else if (session.getAttribute(InstallConstants.OPTIONAL_COMPLETE) == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_SETTINGS, true));
        }
        return new ModelAndView(ControllerConstants.Views.INSTALL_FINISH, getSettings(session));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_INDEX, true));
        }
        else if (session.getAttribute(InstallConstants.DBCONFIG_COMPLETE) == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_DATABASE_CONFIGURATION, true));
        }
        else if (session.getAttribute(InstallConstants.OPTIONAL_COMPLETE) == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_SETTINGS, true));
        }
        Map<String, Object> settings = process(getParameters(req), getSettings(session));
        setSettings(session, settings);
        if (wasSuccessfull(settings)) {
            /* TODO redirect to index */
            session.invalidate();
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INDEX + "?install=finished", true));
        }
        else {
            return new ModelAndView(ControllerConstants.Views.INSTALL_FINISH, settings);
        }
    }

    private Map<String, Object> process(Map<String, String> param, Map<String, Object> settings) {

        String username = param.get(HibernateConstants.ADMIN_USERNAME_KEY);
        if (username == null || username.trim().isEmpty()) {
            return error(settings, "Username is invalid.");
        }
        settings.put(HibernateConstants.ADMIN_USERNAME_KEY, username);

        String password = param.get(HibernateConstants.ADMIN_PASSWORD_KEY);
        if (password == null || password.trim().isEmpty()) {
            return error(settings, "Password is invalid.");
        }
        settings.put(HibernateConstants.ADMIN_PASSWORD_KEY, password);

        /* get the database properties */
        String driver = (String) settings.get(InstallConstants.DRIVER_PARAMETER);
        String connectionString = (String) settings.get(InstallConstants.JDBC_PARAMETER);

        JdbcUrl jdbc;
        try {
            jdbc = new JdbcUrl(connectionString);
        }
        catch (URISyntaxException ex) {
            return error(settings, ex.getMessage());
        }
        String error = jdbc.isValid();
        if (error != null) {
            return error(settings, error);
        }

        Properties properties = new Properties();
        properties.put(HibernateConstants.CONNECTION_STRING_PROPERTY, jdbc.getConnectionString());
        properties.put(HibernateConstants.DRIVER_PROPERTY, driver);
        properties.put(HibernateConstants.USER_PROPERTY, jdbc.getUser());
        properties.put(HibernateConstants.PASS_PROPERTY, jdbc.getPassword());

        Connection con = null;

        try {
            Class.forName(driver);
            con = DriverManager.getConnection(jdbc.toString());

            /* create tables */
            Boolean createTables = (Boolean) settings.get(InstallConstants.CREATE_TABLES_PARAMETER);
            if (createTables.booleanValue()) {
                try {
                    SqlUtils.executeSQLFile(con,
                                            getContext().getRealPath(ControllerConstants.CREATE_DATAMODEL_SQL_FILE));
                }
                catch (Exception e) {
                    return error(settings, "Could not create sos tables: " + e);
                }
            }

            /* insert test data */
            Boolean createTestData = (Boolean) settings.get(InstallConstants.CREATE_TEST_DATA_PARAMETER);
            if (createTestData.booleanValue()) {
                try {
                    SqlUtils.executeSQLFile(con,
                                            getContext().getRealPath(ControllerConstants.INSERT_TEST_DATA_SQL_FILE));
                }
                catch (Exception e) {
                    return error(settings, "Could insert test data:" + e);
                }
            }

            /* insert properties into the database */
            try {
                ISettingsDao dao = ServiceLoader.load(ISettingsDao.class).iterator().next();
                Map<String, String> s = new HashMap<String, String>(settings.size());
                for (Setting sosSetting : Setting.values()) {
                    s.put(sosSetting.name(), (String) settings.get(sosSetting.name()));
                }
                dao.save(s, con);
            }
            catch (SQLException e) {
                return error(settings, "Could not insert settings into the database: " + e.getMessage(), e);
            }

            /* save admin credentials */
            try {
                userService.saveAdmin(new AdminUser(username, password), properties);
            }
            catch (Throwable e) {
                return error(settings, "Could not save admin credentials into the database: " + e.getMessage());
            }
        }
        catch (ClassNotFoundException e) {
            return error(settings, "Could not connect to the database: " + e.getMessage());
        }
        catch (SQLException e) {
            return error(settings, "Could not connect to the database: " + e.getMessage());
        }
        finally {
            if (con != null) {
                try {
                    con.close();
                }
                catch (Exception ex) {
                }
            }
        }

        /* write dssos.config */
        try {
            writeDsSosConfig(driver, properties);
        }
        catch (IOException e) {
            return error(settings, "Could not write dssos.config: " + e.getMessage());
        }

        /* instantiate sos configurator */
        if (Configurator.getInstance() == null) {
            log.info("Instantiation Configurator...");
            try {
                Configurator.getInstance(new File(getDsSosConfigPath()), getBasePath());
            }
            catch (ConfigurationException ex) {
                String message = "Cannot instantiate Configurator:" + ex.getMessage();
                File f = new File(getDsSosConfigPath());
                /* configuration error: delete the datasource file */
                if (f.exists()) {
                    f.delete();
                }
                return error(settings, message, ex);
            }
        }
        else {
            log.warn("Configurator seems to be already instantiated...");
        }
        return success(settings);
    }

    private void writeDsSosConfig(String driver, Properties p) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(getDsSosConfigPath());
            p.store(out, "####################################################################\n"
                    + "### configuration of the SOS's datasource\n"
                    + "####################################################################\n");
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (Exception e) {
                }
            }
        }
    }

}
