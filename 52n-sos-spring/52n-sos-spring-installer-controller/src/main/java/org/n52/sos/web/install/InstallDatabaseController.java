/**
 * Copyright (C) 2013 by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk 52 North Initiative for Geospatial Open Source Software GmbH Martin-Luther-King-Weg 24 48155
 * Muenster, Germany info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under the terms of the GNU General Public
 * License version 2 as published by the Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied WARRANTY OF MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program (see gnu-gpl v2.txt). If
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit
 * the Free Software Foundation web page, http://www.fsf.org.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.sos.web.install;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.n52.sos.config.ConfigurationException;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.JdbcUrl;
import org.n52.sos.web.MetaDataHandler;
import org.n52.sos.web.SqlUtils;
import org.n52.sos.web.install.InstallConstants.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_DATABASE)
public class InstallDatabaseController extends AbstractProcessingInstallationController {
    private static final Logger log = LoggerFactory.getLogger(InstallDatabaseController.class);

    @Override
    protected Step getStep() {
        return Step.DATABASE;
    }

    // TODO move user message text strings out of Java into properties file.
    @Override
    protected void process(Map<String, String> parameters, InstallationConfiguration settings) throws
            InstallationSettingsError {
        checkDriver(parameters, settings);
        checkConnectionPool(parameters, settings);
        checkDialect(parameters, settings);
        String jdbc = checkJdbcUrl(parameters, settings);
        boolean overwriteTables = checkOverwrite(parameters, settings);
        boolean createTables = checkCreate(parameters, overwriteTables, settings);
        boolean createTestData = checkCreateTestData(parameters, settings);
        Connection con = null;
        Statement st = null;
        try {
            con = DriverManager.getConnection(jdbc);
            st = createStatement(con, settings);
            if (createTables || createTestData) {
                checkTableCreation(st, settings);
            }
            boolean alreadyExistent = checkAlreadyExistent(st);
            checkTableConfiguration(createTables, overwriteTables, alreadyExistent, settings, st);
            checkPostGis(st, settings);
        } catch (SQLException ex) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.COULD_NOT_CONNECT_TO_DB_SERVER, ex
                    .getMessage()), ex);
        } finally {
            SqlUtils.close(st);
            SqlUtils.close(con);
        }
    }

    protected void checkDriver(Map<String, String> parameters,
                               InstallationConfiguration settings) throws InstallationSettingsError {
        String driver = parameters.get(InstallConstants.DRIVER_PARAMETER);
        if (driver == null) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_DRIVER_SPECIFIED);
        }
        settings.setDatabaseSetting(InstallConstants.DRIVER_PARAMETER, driver);
        try {
            Class.forName(driver);
        } catch (Throwable ex) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.COULD_NOT_LOAD_DRIVER, ex
                    .getMessage()), ex);
        }
    }

    protected void checkConnectionPool(Map<String, String> parameters,
                                       InstallationConfiguration settings) throws InstallationSettingsError {
        String connectionPool = parameters.get(InstallConstants.CONNECTION_POOL_PARAMETER);
        if (connectionPool == null) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_CONNECTION_POOL_SPECIFIED);
        }
        settings.setDatabaseSetting(InstallConstants.CONNECTION_POOL_PARAMETER, connectionPool);
        try {
            Class.forName(connectionPool);
        } catch (Throwable ex) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.COULD_NOT_LOAD_CONNECTION_POOL, ex
                    .getMessage()), ex);
        }
    }

    protected void checkDialect(Map<String, String> parameters,
                                InstallationConfiguration settings) throws InstallationSettingsError {
        String dialect = parameters.get(InstallConstants.JDBC_DIALECT_PARAMETER);
        if (dialect == null) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_DIALECT_SPECIFIED);
        }
        settings.setDatabaseSetting(InstallConstants.JDBC_DIALECT_PARAMETER, dialect);
        try {
            Class.forName(dialect);
        } catch (Throwable ex) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.COULD_NOT_LOAD_DIALECT, ex
                    .getMessage()), ex);
        }
    }

    protected String checkJdbcUrl(Map<String, String> parameters,
                                  InstallationConfiguration settings) throws InstallationSettingsError {
        String jdbc = parameters.get(ControllerConstants.JDBC_PARAMETER);
        if (jdbc == null) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_JDBC_URL_SPECIFIED);
        }
        JdbcUrl url;
        try {
            url = new JdbcUrl(jdbc);
        } catch (URISyntaxException ex) {
            throw new InstallationSettingsError(settings, ErrorMessages.INVALID_JDBC_URL, ex);
        }
        String error = url.isValid();
        if (error != null) {
            url.correct();
            settings.setDatabaseSetting(ControllerConstants.JDBC_PARAMETER, url.toString());
            throw new InstallationSettingsError(settings, String
                    .format(ErrorMessages.INVALID_JDBC_URL_WITH_ERROR_MESSAGE, error));
        }
        settings.setDatabaseSetting(ControllerConstants.JDBC_PARAMETER, jdbc);
        return jdbc;
    }

    protected boolean checkOverwrite(Map<String, String> parameters,
                                     InstallationConfiguration settings) {
        boolean overwriteTables = false;
        Boolean overwriteTablesParameter = parseBoolean(parameters, InstallConstants.OVERWRITE_TABLES_PARAMETER);
        if (overwriteTablesParameter != null) {
            overwriteTables = overwriteTablesParameter.booleanValue();
        }
        settings.setDatabaseSetting(InstallConstants.OVERWRITE_TABLES_PARAMETER, overwriteTables);
        return overwriteTables;
    }

    protected boolean checkCreate(Map<String, String> parameters, boolean overwriteTables,
                                  InstallationConfiguration settings) {
        boolean createTables = true;
        Boolean createTablesParameter = parseBoolean(parameters, InstallConstants.CREATE_TABLES_PARAMETER);
        if (createTablesParameter != null) {
            createTables = (overwriteTables) ? overwriteTables : createTablesParameter.booleanValue();
        }
        settings.setDatabaseSetting(InstallConstants.CREATE_TABLES_PARAMETER, createTables);
        return createTables;
    }

    protected boolean checkCreateTestData(Map<String, String> parameters,
                                          InstallationConfiguration settings) {
        boolean createTestData = false;
        Boolean createTestDataParameter = parseBoolean(parameters, InstallConstants.CREATE_TEST_DATA_PARAMETER);
        if (createTestDataParameter != null) {
            createTestData = createTestDataParameter.booleanValue();
        }
        settings.setDatabaseSetting(InstallConstants.CREATE_TEST_DATA_PARAMETER, createTestData);
        return createTestData;
    }

    protected Statement createStatement(Connection con,
                                        InstallationConfiguration settings) throws InstallationSettingsError {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.CANNOT_CREATE_STATEMENT, e
                    .getMessage()), e);
        }
    }

    protected void checkTableCreation(Statement st,
                                      InstallationConfiguration settings) throws InstallationSettingsError {
        try {
            st.execute(InstallConstants.CAN_CREATE_TABLES);
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.CANNOT_CREATE_TABLES, e
                    .getMessage()), e);
        }
    }

    protected boolean checkAlreadyExistent(Statement st) {
        boolean alreadyExistent = true;
        try {
            st.execute(InstallConstants.TABLES_ALREADY_EXISTENT);
        } catch (SQLException e) {
            alreadyExistent = false;
        }
        return alreadyExistent;
    }

    protected void checkVersion(Statement st) throws SQLException {
        /* check version, but for now do not fail on this one... */
        String version = null;
        String currentVersion = null;
        ResultSet rs = null;
        try {
            rs = st.executeQuery(InstallConstants.GET_VERSION_OF_DATABASE_INSTALLATION);
            if (rs.next()) {
                version = rs.getString(1);
            }
        } catch (SQLException e) {
            log.error("Could not determine version of installed database schema.", e);
        } finally {
            SqlUtils.close(rs);
        }

        try {
            currentVersion = getMetaDataHandler().get(MetaDataHandler.Metadata.VERSION);
        } catch (ConfigurationException e) {
            log.error("Can not load version metadata property", e);
        }

        if (currentVersion != null && !currentVersion.equals(version)) {
            /* TODO do some conversion? */
            log.warn("Installed database schema ({}) is not the current one ({}).", version, currentVersion);
        }
    }

    protected void checkPostGis(Statement st,
                                InstallationConfiguration settings) throws InstallationSettingsError {
        checkPostGisInstallation(st, settings);
        checkSpatialRefSys(st, settings);
    }

    protected void checkPostGisInstallation(Statement st,
                                            InstallationConfiguration settings) throws InstallationSettingsError {
        try {
            st.execute(InstallConstants.IS_POSTGIS_INSTALLED);
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, ErrorMessages.POST_GIS_IS_NOT_INSTALLED_IN_THE_DATABASE, e);
        }
    }

    protected void checkSpatialRefSys(Statement st,
                                      InstallationConfiguration settings) throws InstallationSettingsError {
        try {
            st.execute(InstallConstants.CAN_READ_SPATIAL_REF_SYS);
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, ErrorMessages.CANNOT_READ_SPATIAL_REF_SYS_TABLE, e);
        }
    }

    protected void checkTableConfiguration(boolean createTables, boolean overwriteTables, boolean alreadyExistent,
                                           InstallationConfiguration settings, Statement st) throws
            InstallationSettingsError, SQLException {
        if (createTables) {
            if (!overwriteTables && alreadyExistent) {
                throw new InstallationSettingsError(settings, ErrorMessages.TABLES_ALREADY_CREATED_BUT_SHOULD_NOT_OVERWRITE);
            }
        } else if (!alreadyExistent) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_TABLES_AND_SHOULD_NOT_CREATE);
        } else {
            checkVersion(st);
        }
    }
}
