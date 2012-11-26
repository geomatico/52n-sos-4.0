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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.n52.sos.web.install;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.n52.sos.web.ControllerConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_DATABASE_CONFIGURATION)
public class InstallDatabaseController extends AbstractInstallController {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_INDEX, true));
        }
        return new ModelAndView(ControllerConstants.Views.INSTALL_DATABASE, getSettings(session));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_INDEX, true));
        }
        Map<String, Object> settings = process(getParameters(req), getSettings(session));
        setSettings(session, settings);
        if (wasSuccessfull(settings)) {
            session.setAttribute(InstallConstants.DBCONFIG_COMPLETE, true);
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_SETTINGS, true));
        }
        else {
            return new ModelAndView(ControllerConstants.Views.INSTALL_DATABASE, settings);
        }
    }

    private Map<String, Object> process(Map<String, String> parameters, Map<String, Object> settings) {

        String driver = parameters.get(InstallConstants.DRIVER_PARAMETER);
        if (driver == null) {
            return error(settings, "no driver specified");
        }
        settings.put(InstallConstants.DRIVER_PARAMETER, driver);
        try {
            Class.forName(driver);
        }
        catch (Throwable ex) {
            return error(settings, "Could not load Driver:" + ex.getMessage(), ex);
        }

        String jdbc = parameters.get(InstallConstants.JDBC_PARAMETER);
        if (jdbc == null) {
            return error(settings, "No JDBC URL specified.");
        }
        JdbcUrl url;
        try {
            url = new JdbcUrl(jdbc);
        }
        catch (URISyntaxException ex) {
            return error(settings, "Invalid JDBC URL.");
        }
        String error = url.isValid();
        if (error != null) {
            url.correct();
            settings.put(InstallConstants.JDBC_PARAMETER, url.toString());
            return error(settings, "Invalid JDBC URL: " + error);
        }
        settings.put(InstallConstants.JDBC_PARAMETER, jdbc);

        boolean createTables = true;
        Boolean createTablesParameter = parseBoolean(parameters, InstallConstants.CREATE_TABLES_PARAMETER);
        if (createTablesParameter != null) {
            createTables = createTablesParameter.booleanValue();
        }
        settings.put(InstallConstants.CREATE_TABLES_PARAMETER, createTables);

        boolean overwriteTables = false;
        Boolean overwriteTablesParameter = parseBoolean(parameters, InstallConstants.OVERWRITE_TABLES_PARAMETER);
        if (overwriteTablesParameter != null) {
            overwriteTables = overwriteTablesParameter.booleanValue();
        }
        settings.put(InstallConstants.OVERWRITE_TABLES_PARAMETER, overwriteTables);

        boolean createTestData = false;
        Boolean createTestDataParameter = parseBoolean(parameters, InstallConstants.CREATE_TEST_DATA_PARAMETER);
        if (createTestDataParameter != null) {
            createTestData = createTestDataParameter.booleanValue();
        }
        settings.put(InstallConstants.CREATE_TEST_DATA_PARAMETER, createTestData);

        Connection con = null;
        Statement st = null;
        try {
            con = DriverManager.getConnection(jdbc);
            try {
                st = con.createStatement();
            }
            catch (SQLException e) {
                return error(settings, "Cannot create Statement: " + e.getMessage(), e);
            }
            if (createTables || createTestData) {
                try {
                    st.execute(InstallConstants.CAN_CREATE_TABLES);
                }
                catch (SQLException e) {
                    return error(settings, "Cannot create tables: " + e.getMessage(), e);
                }
            }
            boolean alreadyExistent = true;
            try {
                st.execute(InstallConstants.TABLES_ALREADY_EXISTENT);
            }
            catch (SQLException e) {
                alreadyExistent = false;
            }

            if (createTables) {
                if ( !overwriteTables && alreadyExistent) {
                    return error(settings, "Tables already created, but should not overwrite.");
                }
            }
            else if ( !alreadyExistent) {
                return error(settings, "No tables are present in the database and no tables should be created.");
            }

            try {
                st.execute(InstallConstants.IS_POSTGIS_INSTALLED);
            }
            catch (SQLException e) {
                return error(settings, "PostGIS is not installed in the database.", e);
            }

            try {
                st.execute(InstallConstants.CAN_READ_SPATIAL_REF_SYS);
            }
            catch (SQLException e) {
                return error(settings, "Cannot read 'spatial_ref_sys'. Please revise your database configuration.", e);
            }

        }
        catch (SQLException ex) {
            return error(settings, "Could not connect to DB server: " + ex.getMessage(), ex);
        }
        finally {
            if (st != null) {
                try {
                    st.close();
                }
                catch (SQLException ex) {
                }
            }
            if (con != null) {
                try {
                    con.close();
                }
                catch (SQLException ex) {
                }
            }
        }

        return success(settings);
    }
}
