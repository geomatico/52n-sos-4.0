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
package org.n52.sos.web.admin;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.ds.hibernate.util.DefaultHibernateConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.JdbcUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.ADMIN_DATABASE_SETTINGS)
public class AdminDatabaseSettingsController extends AbstractController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminDatabaseSettingsController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView view() {
        try {
            Properties p = getDatabaseSettingsHandler().getAll();
            return new ModelAndView(ControllerConstants.Views.ADMIN_DATABASE_SETTINGS,
                                    ControllerConstants.JDBC_PARAMETER, new JdbcUrl(p));
        } catch (Exception ex) {
            LOG.error("Error reading database settings", ex);
            return new ModelAndView(ControllerConstants.Views.ADMIN_DATABASE_SETTINGS,
                                    ControllerConstants.ERROR_MODEL_ATTRIBUTE, ex.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView save(@RequestParam(ControllerConstants.JDBC_PARAMETER) String jdbc) {
        LOG.debug("JDBCURI: {}", jdbc);
        try {
            JdbcUrl jdbcUrl = new JdbcUrl(jdbc);
            String error = jdbcUrl.isValid();
            if (error != null) {
                return onSaveError(jdbc, error, null);
            }
            Properties p = jdbcUrl.toProperties();
            Properties merged = getDatabaseSettingsHandler().getAll();
            for (String s : p.stringPropertyNames()) {
                merged.put(s, p.getProperty(s));
            }
            /* test connection once */
            Connection con = null;
            try {
                Class.forName(merged.getProperty(DefaultHibernateConstants.DRIVER_PROPERTY));
                con = DriverManager.getConnection(jdbc.toString());
            } catch (Throwable t) {
                return onSaveError(jdbc, null, t);
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (SQLException e) {
                        LOG.warn("Can not close connection.", e);
                    }

                }
            }

            /* save settings */
            getDatabaseSettingsHandler().saveAll(p);
            /* destroy configurator */
            if (Configurator.getInstance() != null) {
                Configurator.getInstance().cleanup();
            }
            /* reinitialize */
            Configurator.createInstance(getDatabaseSettingsHandler().getAll(), getBasePath());
        } catch (ConfigurationException ex) {
            return onSaveError(jdbc, null, ex);
        } catch (URISyntaxException ex) {
            return onSaveError(jdbc, null, ex);
        }
        return new ModelAndView(new RedirectView(ControllerConstants.Paths.ADMIN_DATABASE_SETTINGS, true));
    }

    private ModelAndView onSaveError(String uri, String message, Throwable e) {
        Map<String, String> model = new HashMap<String, String>(2);
        model.put(ControllerConstants.ERROR_MODEL_ATTRIBUTE, (message != null) ? message : (e != null) ? e.getMessage()
                                                                                           : "Could not save settings");
        if (uri != null) {
            model.put(ControllerConstants.JDBC_PARAMETER, uri);
        }
        LOG.error("Error saving database settings: " + message, e);
        return new ModelAndView(ControllerConstants.Views.ADMIN_DATABASE_SETTINGS, model);
    }
}