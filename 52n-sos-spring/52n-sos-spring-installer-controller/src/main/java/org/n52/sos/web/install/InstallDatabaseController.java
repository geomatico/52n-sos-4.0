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

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.n52.sos.util.SQLHelper;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.JdbcUrl;
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
        checkSchema(parameters, settings);
        String jdbc = checkJdbcUrl(parameters, settings);
        boolean overwriteTables = checkOverwrite(parameters, settings);
        boolean createTables = checkCreate(parameters, overwriteTables, settings);
        boolean createTestData = checkCreateTestData(parameters, settings);
        Connection con = null;
        Statement st = null;
        try {
            con = DriverManager.getConnection(jdbc);
            st = createStatement(con, settings);
            checkIfSchemaExists(st, settings);
            if (createTables || createTestData) {
                checkTableCreation(st, settings);
            }
            boolean alreadyExistent = checkAlreadyExistent(st, settings);
            checkTableConfiguration(createTables, overwriteTables, alreadyExistent, settings, st);
            checkPostGIS(st, settings);
        } catch (SQLException ex) {
            throw new InstallationSettingsError(settings, String
                    .format(ErrorMessages.COULD_NOT_CONNECT_TO_DATABASE_SERVER, ex.getMessage()), ex);
        } finally {
            SQLHelper.close(st);
            SQLHelper.close(con);
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
        } catch (ClassNotFoundException ex) {
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
        } catch (ClassNotFoundException ex) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.COULD_NOT_LOAD_CONNECTION_POOL, ex
                    .getMessage()), ex);
        }
    }

    protected void checkDialect(Map<String, String> parameters,
                                InstallationConfiguration settings) throws InstallationSettingsError {
        String dialect = parameters.get(InstallConstants.DIALECT_PARAMETER);
        if (dialect == null) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_DIALECT_SPECIFIED);
        }
        settings.setDatabaseSetting(InstallConstants.DIALECT_PARAMETER, dialect);
        try {
            Class.forName(dialect);
        } catch (ClassNotFoundException ex) {
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

    protected void checkTableCreation(Statement st,
                                      InstallationConfiguration settings) throws InstallationSettingsError {
        try {
            String schema = getSchema(settings);
            schema = schema == null ? "" : "." + schema;
            final String command = String.format(
                    "BEGIN; "
                    + "DROP TABLE IF EXISTS \"%1$ssos_installer_test_table\"; "
                    + "CREATE TABLE \"%1$ssos_installer_test_table\" (id integer NOT NULL); "
                    + "DROP TABLE \"%1$ssos_installer_test_table\"; "
                    + "END;", schema);
            st.execute(command);
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, String.format(ErrorMessages.COULD_NOT_CREATE_TABLES, e
                    .getMessage()), e);
        }
    }

    protected boolean checkAlreadyExistent(Statement st, InstallationConfiguration settings) throws
            InstallationSettingsError {
        return checkTable(getSchema(settings), "observation", st, settings);
    }

//    protected void checkVersion(Statement st) throws SQLException {
//        /* check version, but for now do not fail on this one... */
//        String version = null;
//        String currentVersion = null;
//        ResultSet rs = null;
//        try {
//            rs = st.executeQuery(InstallConstants.GET_VERSION_OF_DATABASE_INSTALLATION);
//            if (rs.next()) {
//                version = rs.getString(1);
//            }
//        } catch (SQLException e) {
//            log.error("Could not determine version of installed database schema.", e);
//        } finally {
//            SQLHelper.close(rs);
//        }
//
//        try {
//            currentVersion = getMetaDataHandler().get(MetaDataHandler.Metadata.VERSION);
//        } catch (ConfigurationException e) {
//            log.error("Can not load version metadata property", e);
//        }
//
//        if (currentVersion != null && !currentVersion.equals(version)) {
//            /* TODO do some conversion? */
//            log.warn("Installed database schema ({}) is not the current one ({}).", version, currentVersion);
//        }
//    }

    protected void checkPostGIS(Statement st,
                                InstallationConfiguration settings) throws InstallationSettingsError {
        checkPostGISInstallation(st, settings);
        checkSpatialRefSysTable(st, settings);
    }

    protected void checkPostGISInstallation(Statement st,
                                            InstallationConfiguration settings) throws InstallationSettingsError {
        try {
            st.execute(InstallConstants.IS_POSTGIS_INSTALLED);
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, ErrorMessages.POST_GIS_IS_NOT_INSTALLED_IN_THE_DATABASE, e);
        }
    }

    protected void checkSpatialRefSysTable(Statement st,
                                           InstallationConfiguration settings) throws InstallationSettingsError {

        if (!checkTable(null, "spatial_ref_sys", st, settings)) {
            throw new InstallationSettingsError(settings, ErrorMessages.COULD_NOT_READ_SPATIAL_REF_SYS_TABLE);
        }
    }

    protected void checkTableConfiguration(boolean createTables, boolean overwriteTables, boolean alreadyExistent,
                                           InstallationConfiguration settings, Statement st) throws
            InstallationSettingsError, SQLException {
        if (createTables) {
            if (alreadyExistent && !overwriteTables) {
                throw new InstallationSettingsError(settings, ErrorMessages.TABLES_ALREADY_CREATED_BUT_SHOULD_NOT_OVERWRITE);
            }
        } else if (!alreadyExistent) {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_TABLES_AND_SHOULD_NOT_CREATE);
//        } else {
//            checkVersion(st);
        }
    }

    protected void checkSchema(Map<String, String> parameters, InstallationConfiguration settings) throws
            InstallationSettingsError {
        final String schema = parameters.get(InstallConstants.SCHEMA_PARAMETER);
        if (schema != null && !schema.trim().isEmpty()) {
            settings.setDatabaseSetting(InstallConstants.SCHEMA_PARAMETER, schema);
        } else {
            throw new InstallationSettingsError(settings, ErrorMessages.NO_SCHEMA_SPECIFIED);
        }
    }

    protected boolean checkTable(String schema, String table, Statement st, InstallationConfiguration settings) throws
            InstallationSettingsError {
        ResultSet rs = null;
        try {
            String command;
            if (schema != null) {
                command = String
                        .format("SELECT true FROM pg_tables WHERE tablename = '%s' AND schemaname = '%s';", table, schema);
            } else {
                command = String.format("SELECT true FROM pg_tables WHERE tablename = '%s';", table);
            }

            log.debug("Testing table existence: {}", command);
            rs = st.executeQuery(command);
            return rs.next();
        } catch (SQLException e) {
            throw new InstallationSettingsError(settings, String
                    .format(ErrorMessages.COULD_NOT_CHECK_IF_TABLE_EXISTS, table, e
                    .getMessage()), e);
        } finally {
            SQLHelper.close(rs);
        }
    }

    protected void checkIfSchemaExists(Statement st, InstallationConfiguration settings) throws
            InstallationSettingsError {
        String schema = getSchema(settings);
        if (schema != null) {
            ResultSet rs = null;
            try {
                String command = String
                        .format("SELECT true FROM information_schema.schemata WHERE schema_name = '%s';", schema);
                rs = st.executeQuery(command);
                if (!rs.next()) {
                    throw new InstallationSettingsError(settings, String
                            .format(ErrorMessages.SCHEMA_DOES_NOT_EXIST, schema));
                }
            } catch (SQLException e) {
                throw new InstallationSettingsError(settings, String
                        .format(ErrorMessages.COULD_NOT_CHECK_IF_SCHEMA_EXISTS, schema, e
                        .getMessage()), e);
            } finally {
                SQLHelper.close(rs);
            }
        }
    }
}
