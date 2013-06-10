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

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.sos.config.SettingValue;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.Datasource;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.MetaDataHandler;
import org.n52.sos.web.auth.UserService;
import org.n52.sos.web.install.InstallConstants.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_FINISH)
public class InstallFinishController extends AbstractProcessingInstallationController {
    private static final Logger LOG = LoggerFactory.getLogger(InstallFinishController.class);
    @Autowired
    private UserService userService;

    @Override
    protected Step getStep() {
        return Step.FINISH;
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(HttpServletRequest req, HttpServletResponse resp) throws InstallationRedirectError,
                                                                                      InstallationSettingsError {
        HttpSession session = checkPrevious(req);
        process(getParameters(req), getSettings(session));
        session.invalidate();
        return redirect(ControllerConstants.Paths.GET_INVOLVED + "?install=finished");
    }

    @Override
    protected void process(Map<String, String> param, InstallationConfiguration c) throws InstallationSettingsError {
        checkUsername(param, c);
        checkPassword(param, c);
        clearSettings(c);
        Datasource datasource = c.getDatasource();

        try {
            if (c.isDropSchema()) {
                datasource.dropSchema(c.getDatabaseSettings());
            }
            if (c.isCreateSchema()) {
                datasource.createSchema(c.getDatabaseSettings());
            }
            if (c.isCreateTestData()) {
                datasource.insertTestData(c.getDatabaseSettings());
            }
        } catch (Throwable e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_CONNECT_TO_THE_DATABASE,
                                                                 e.getMessage()), e);
        }
        saveServiceSettings(c);
        createAdministratorUser(c);
        Properties properties =
                datasource.getDatasourceProperties(c.getDatabaseSettings());
        // save the used datasource class
        properties.put(Datasource.class.getCanonicalName(),
                       datasource.getClass().getCanonicalName());
        instantiateConfigurator(properties, c);
        saveDatabaseProperties(properties, c);
        saveInstallationDate();
    }

    protected void checkUsername(Map<String, String> param, InstallationConfiguration c) throws
            InstallationSettingsError {
        String username = param.get(ControllerConstants.ADMIN_USERNAME_REQUEST_PARAMETER);
        if (username == null || username.trim().isEmpty()) {
            throw new InstallationSettingsError(c, ErrorMessages.USERNAME_IS_INVALID);
        }
        c.setUsername(username);
    }

    protected void checkPassword(Map<String, String> param, InstallationConfiguration c) throws
            InstallationSettingsError {
        String password = param.get(ControllerConstants.ADMIN_PASSWORD_REQUEST_PARAMETER);
        if (password == null || password.trim().isEmpty()) {
            throw new InstallationSettingsError(c, ErrorMessages.PASSWORD_IS_INVALID);
        }
        c.setPassword(password);
    }

    protected void instantiateConfigurator(Properties properties, InstallationConfiguration c) throws
            InstallationSettingsError {
        if (Configurator.getInstance() == null) {
            LOG.info("Instantiating Configurator...");
            try {
                Configurator.createInstance(properties, getBasePath());
            } catch (ConfigurationException ex) {
                throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_INSTANTIATE_CONFIGURATOR, ex
                        .getMessage()), ex);
            }
        } else {
            LOG.error("Configurator seems to be already instantiated...");
        }
    }

    protected void saveDatabaseProperties(Properties properties, InstallationConfiguration c) throws
            InstallationSettingsError {
        try {
            getDatabaseSettingsHandler().saveAll(properties);
        } catch (ConfigurationException e) {
            /* TODO desctruct configurator? */
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_WRITE_DATASOURCE_CONFIG, e
                    .getMessage()), e);
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
            LOG.error("Error saveing installation date", ex);
        }
    }

    protected void saveServiceSettings(InstallationConfiguration c) throws InstallationSettingsError {
        try {
            for (SettingValue<?> e : c.getSettings().values()) {
                getSettingsManager().changeSetting(e);
            }
        } catch (ConfigurationException e) {
            throw new InstallationSettingsError(c, String
                    .format(ErrorMessages.COULD_NOT_INSERT_SETTINGS, e.getMessage()), e);
        } catch (ConnectionProviderException e) {
            throw new InstallationSettingsError(c, String
                    .format(ErrorMessages.COULD_NOT_INSERT_SETTINGS, e.getMessage()), e);
        }
    }

    protected void createAdministratorUser(InstallationConfiguration c) throws InstallationSettingsError {
        try {
            userService.createAdmin(c.getUsername(), c.getPassword());
        } catch (Throwable e) {
            throw new InstallationSettingsError(c, String.format(ErrorMessages.COULD_NOT_SAVE_ADMIN_CREDENTIALS,
                                                                 e.getMessage()), e);
        }
    }

    protected void clearSettings(InstallationConfiguration c) throws
            InstallationSettingsError {
        try {
            getSettingsManager().deleteAll();
        } catch (Throwable e) {
            throw new InstallationSettingsError(c, String
                    .format(ErrorMessages.COULD_NOT_DELETE_PREVIOUS_SETTINGS, e
                    .getMessage()));
        }
    }
}
