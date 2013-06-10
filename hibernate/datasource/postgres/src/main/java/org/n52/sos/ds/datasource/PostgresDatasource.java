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
package org.n52.sos.ds.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.Dialect;
import org.hibernate.spatial.dialect.postgis.PostgisDialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.ds.datasource.AbstractHibernateDatasource;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.CollectionHelper;


/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class PostgresDatasource extends AbstractHibernateDatasource {
    private static final String TRUE = "true";
    private static final String DIALECT_NAME = "PostgreSQL/PostGIS";
    private static final String C3P0_CONNCETION_PROVIDER_CLASS =
            "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider";
    private static final String POSTGIS_DIALECT_CLASS =
            PostgisDialect.class.getName();
    private static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";
    private static final Pattern JDBC_URL_PATTERN =
            Pattern.compile("^jdbc:postgresql://([^:]+):([0-9]+)/(.*)$");
    protected final StringSettingDefinition pgUsername =
            createUsernameDefinition()
            .setDescription("Your database server user name. The default value for PostgreSQL is \"postgres\".")
            .setDefaultValue("postgres");
    protected final StringSettingDefinition pgPassword =
            createPasswordDefinition()
            .setDescription("Your database server password. The default value is \"postgres\".")
            .setDefaultValue("postgres");
    protected final StringSettingDefinition pgDatabase =
            createDatabaseDefinition();
    protected final StringSettingDefinition pgHost = createHostDefinition()
            .setDescription("Set this to the IP/net location of PostgreSQL database server. The default value for PostgreSQL is \"localhost\".");
    protected final IntegerSettingDefinition pgPort = createPortDefinition()
            .setTitle("Database Port")
            .setDescription("Set this to the port number of your PostgreSQL server. The default value for PostgreSQL is 5432.")
            .setDefaultValue(5432);
    protected final StringSettingDefinition pgCatalog =
            createCatalogDefinition()
            .setDefaultValue("public");

    @Override
    public String getDialectName() {
        return DIALECT_NAME;
    }

    @Override
    protected Dialect createDialect() {
        return new PostgisDialect();
    }

    @Override
    protected Connection openConnection(Map<String, Object> settings) throws
            SQLException {
        try {
            String jdbc = toURL(settings);
            Class.forName(POSTGRES_DRIVER_CLASS);
            String pass = (String) settings
                    .get(HibernateConstants.CONNECTION_PASSWORD);
            String user = (String) settings
                    .get(HibernateConstants.CONNECTION_USERNAME);
            return DriverManager.getConnection(jdbc, user, pass);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return CollectionHelper.<SettingDefinition<?, ?>>set(pgUsername,
                                                             pgPassword,
                                                             pgDatabase,
                                                             pgHost,
                                                             pgPort,
                                                             pgCatalog,
                                                             getTransactionalDefiniton());
    }

    @Override
    public Set<SettingDefinition<?, ?>> getChangableSettingDefinitions() {
        return CollectionHelper.<SettingDefinition<?, ?>>set(pgUsername,
                                                             pgPassword,
                                                             pgDatabase,
                                                             pgHost,
                                                             pgPort,
                                                             pgCatalog);
    }


    @Override
    public boolean supportsTestData() {
        //FIXME
        return false;
    }

    @Override
    public boolean checkSchemaCreation(Map<String, Object> settings) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = openConnection(settings);
            stmt = conn.createStatement();
            String schema = (String) settings.get(pgCatalog.getKey());
            schema = schema == null ? "" : "." + schema;
            final String command = String.format(
                    "BEGIN; " +
                    "DROP TABLE IF EXISTS \"%1$ssos_installer_test_table\"; " +
                    "CREATE TABLE \"%1$ssos_installer_test_table\" (id integer NOT NULL); " +
                    "DROP TABLE \"%1$ssos_installer_test_table\"; " +
                    "END;", schema);
            stmt.execute(command);
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            close(stmt);
            close(conn);
        }
    }

    @Override
    protected void validatePrerequisites(Connection con,
                                         DatabaseMetadata metadata) {
        checkPostgis(con);
        checkSpatialRefSys(con, metadata);
    }

    protected void checkPostgis(Connection con) {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute("SELECT postgis_version()");
            // TODO check PostGIS version
        } catch (SQLException ex) {
            throw new ConfigurationException("PostGIS does not seem to be installed.", ex);
        } finally {
            close(stmt);
        }
    }

    protected void checkSpatialRefSys(Connection con, DatabaseMetadata metadata) {
        Statement stmt = null;
        try {
            if (!metadata.isTable("spatial_ref_sys")) {
                throw new ConfigurationException("Missing 'spatial_ref_sys' table.");
            }
            stmt = con.createStatement();
            stmt.execute("SELECT count(*) from spatial_ref_sys");
        } catch (SQLException ex) {
            throw new ConfigurationException("Can not read from table 'spatial_ref_sys'", ex);
        } finally {
            close(stmt);
        }
    }

    @Override
    public Properties getDatasourceProperties(
            Map<String, Object> settings) {
        Properties p = new Properties();
        p.put(HibernateConstants.DEFAULT_CATALOG, settings.get(pgCatalog.getKey()));
        p.put(HibernateConstants.CONNECTION_USERNAME, settings.get(pgUsername.getKey()));
        p.put(HibernateConstants.CONNECTION_PASSWORD, settings.get(pgPassword.getKey()));
        p.put(HibernateConstants.CONNECTION_URL, toURL(settings));
        p.put(HibernateConstants.CONNECTION_PROVIDER_CLASS, C3P0_CONNCETION_PROVIDER_CLASS);
        p.put(HibernateConstants.DIALECT, POSTGIS_DIALECT_CLASS);
        p.put(HibernateConstants.DRIVER_CLASS, POSTGRES_DRIVER_CLASS);
        p.put(HibernateConstants.C3P0_MIN_SIZE, "10");
        p.put(HibernateConstants.C3P0_MAX_SIZE, "30");
        p.put(HibernateConstants.C3P0_IDLE_TEST_PERIOD, "1");
        p.put(HibernateConstants.C3P0_ACQUIRE_INCREMENT, "1");
        p.put(HibernateConstants.C3P0_TIMEOUT, "0");
        p.put(HibernateConstants.C3P0_MAX_STATEMENTS, "0");
        p.put(HibernateConstants.CONNECTION_AUTO_RECONNECT, TRUE);
        p.put(HibernateConstants.CONNECTION_AUTO_RECONNECT_FOR_POOLS, TRUE);
        p.put(HibernateConstants.CONNECTION_TEST_ON_BORROW, TRUE);
        addMappingFileDirectories(settings, p);
        return p;
    }

    protected String toURL(
            Map<String, Object> settings) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                                   settings.get(pgHost.getKey()),
                                   settings.get(pgPort.getKey()),
                                   settings.get(pgDatabase.getKey()));
        return url;
    }

    @Override
    protected Map<String, Object> parseDatasourceProperties(Properties current) {
        Map<String, Object> settings = new HashMap<String, Object>(current
                .size());
        settings.put(pgCatalog.getKey(),
                     current.getProperty(HibernateConstants.DEFAULT_CATALOG));
        settings.put(pgUsername.getKey(),
                     current.getProperty(HibernateConstants.CONNECTION_USERNAME));
        settings.put(pgPassword.getKey(),
                     current.getProperty(HibernateConstants.CONNECTION_PASSWORD));
        settings.put(getTransactionalDefiniton().getKey(),
                     isTransactional(current));
        String url = current.getProperty(HibernateConstants.CONNECTION_URL);
        Matcher matcher = JDBC_URL_PATTERN.matcher(url);
        matcher.find();
        String host = matcher.group(1);
        String port = matcher.group(2);
        String db = matcher.group(3);
        settings.put(pgHost.getKey(), host);
        settings.put(pgPort.getKey(),
                     port == null ? null : Integer.valueOf(port));
        settings.put(pgDatabase.getKey(), db);
        return settings;
    }

    @Override
    public void clear(Properties settings) {
        //FIXME datasource clear
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.PostgresDatasource.clearDatabase() not yet implemented");
    }

    @Override
    public void insertTestData(Map<String, Object> settings) {
        /* TODO implement org.n52.sos.ds.datasource.PostgresDatasource.insertTestData() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.PostgresDatasource.insertTestData() not yet implemented");
    }

    @Override
    public void insertTestData(Properties settings) {
        /* TODO implement org.n52.sos.ds.datasource.PostgresDatasource.insertTestData() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.PostgresDatasource.insertTestData() not yet implemented");
    }

    @Override
    public boolean isTestDataPresent(Properties settings) {
        /* TODO implement org.n52.sos.ds.datasource.PostgresDatasource.isTestDataPresent() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.PostgresDatasource.isTestDataPresent() not yet implemented");
    }

    @Override
    public void removeTestData(Properties settings) {
        /* TODO implement org.n52.sos.ds.datasource.PostgresDatasource.removeTestData() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.PostgresDatasource.removeTestData() not yet implemented");
    }
}
