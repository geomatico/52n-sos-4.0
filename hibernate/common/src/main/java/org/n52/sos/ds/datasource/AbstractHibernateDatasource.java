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

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.ds.Datasource;
import org.n52.sos.ds.hibernate.SessionFactoryProvider;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.SQLConstants;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractHibernateDatasource implements Datasource, SQLConstants {
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractHibernateDatasource.class);
    public static final String CORE_MAPPINGS_PATH = "/mapping/core";
    public static final String TRANSACTIONAL_MAPPINGS_PATH =
            "/mapping/transactional";
    public static final String USERNAME_TITLE = "User Name";
    public static final String PASSWORD_TITLE = "Password";
    public static final String DATABASE_KEY = "jdbc.database";
    public static final String DATABASE_TITLE = "Database";
    public static final String DATABASE_DESCRIPTION =
            "Set this to the name of the database you want to use for SOS.";
    public static final String DATABASE_DEFAULT_VALUE = "sos";
    public static final String HOST_KEY = "jdbc.host";
    public static final String HOST_TITLE = "Host";
    public static final String HOST_DESCRIPTION =
            "Set this to the IP/net location of the database server. The default value for is \"localhost\".";
    public static final String HOST_DEFAULT_VALUE = "localhost";
    public static final String PORT_KEY = "jdbc.port";
    public static final String PORT_TITLE = "Database Port";
//    public static final String CATALOG_KEY = HibernateConstants.DEFAULT_CATALOG;
//    public static final String CATALOG_TITLE = "Catalog";
//    public static final String CATALOG_DESCRIPTION =
//            "Qualifies unqualified table names with the given catalog in generated SQL.";
//    public static final String CATALOG_DEFAULT_VALUE = "public";
    public static final String SCHEMA_KEY = HibernateConstants.DEFAULT_SCHEMA;
    public static final String SCHEMA_TITLE = "Schema";
    public static final String SCHEMA_DESCRIPTION = "Qualifies unqualified table names with the given schema in generated SQL.";
    public static final String SCHMEA_DEFAULT_VALUE = "public";
    public static final String TRANSACTIONAL_TITLE = "Transactional Profile";
    public static final String TRANSACTIONAL_DESCRIPTION =
            "Should the database support the transactional profile?";
    public static final String TRANSACTIONAL_KEY = "sos.transactional";
    public static final boolean TRANSACTIONAL_DEFAULT_VALUE = true;
    public static final String USERNAME_KEY =
            HibernateConstants.CONNECTION_USERNAME;
    public static final String PASSWORD_KEY =
            HibernateConstants.CONNECTION_PASSWORD;
    private Dialect dialect;
    private final BooleanSettingDefinition transactionalDefiniton =
            createTransactionalDefinition();

    protected StringSettingDefinition createUsernameDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(1)
                .setKey(USERNAME_KEY)
                .setTitle(USERNAME_TITLE);
    }

    protected StringSettingDefinition createPasswordDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(2)
                .setKey(PASSWORD_KEY)
                .setTitle(PASSWORD_TITLE);
    }

    protected StringSettingDefinition createDatabaseDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(3)
                .setKey(DATABASE_KEY)
                .setTitle(DATABASE_TITLE)
                .setDescription(DATABASE_DESCRIPTION)
                .setDefaultValue(DATABASE_DEFAULT_VALUE);
    }

    protected StringSettingDefinition createHostDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(4)
                .setKey(HOST_KEY)
                .setTitle(HOST_TITLE)
                .setDescription(HOST_DESCRIPTION)
                .setDefaultValue(HOST_DEFAULT_VALUE);
    }

    protected IntegerSettingDefinition createPortDefinition() {
        return new IntegerSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(5)
                .setKey(PORT_KEY)
                .setTitle(PORT_TITLE);
    }
//    protected StringSettingDefinition createCatalogDefinition() {
//        return new StringSettingDefinition()
//                .setGroup(ADVANCED_GROUP)
//                .setOrder(1)
//                .setKey(CATALOG_KEY)
//                .setTitle(CATALOG_TITLE)
//                .setDescription(CATALOG_DESCRIPTION)
//                .setDefaultValue(CATALOG_DEFAULT_VALUE);
//    }
    protected StringSettingDefinition createSchemaDefinition() {
        return new StringSettingDefinition()
                .setGroup(ADVANCED_GROUP)
                .setOrder(1)
                .setKey(SCHEMA_KEY)
                .setTitle(SCHEMA_TITLE)
                .setDescription(SCHEMA_DESCRIPTION)
                .setDefaultValue(SCHMEA_DEFAULT_VALUE);
    }

    protected BooleanSettingDefinition createTransactionalDefinition() {
        return new BooleanSettingDefinition()
                .setDefaultValue(TRANSACTIONAL_DEFAULT_VALUE)
                .setTitle(TRANSACTIONAL_TITLE)
                .setDescription(TRANSACTIONAL_DESCRIPTION)
                .setGroup(ADVANCED_GROUP)
                .setOrder(2)
                .setKey(TRANSACTIONAL_KEY);
    }

    public CustomConfiguration getConfig(Map<String, Object> settings) {
        CustomConfiguration config = new CustomConfiguration();
        config.configure("/sos-hibernate.cfg.xml");
        config.addDirectory(resource("/mapping/core"));
        Boolean transactional = (Boolean) settings.get(
                this.transactionalDefiniton.getKey());
        if (transactional != null && transactional.booleanValue()) {
            config.addDirectory(resource("/mapping/transactional"));
        }
        if (isSetSchema(settings)) {
            Properties properties = new Properties();
            properties.put(HibernateConstants.DEFAULT_SCHEMA, settings.get(HibernateConstants.DEFAULT_SCHEMA));
            config.addProperties(properties);
        }
        config.buildMappings();
        return config;
    }

    protected File resource(String resource) {
        try {
            return new File(getClass().getResource(resource).toURI());
        } catch (URISyntaxException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public String[] createSchema(Map<String, Object> settings) {
        String[] script = getConfig(settings)
                .generateSchemaCreationScript(getDialectInternal());
        String[] pre = getPreSchemaScript();
        String[] post = getPostSchemaScript();

        script = (pre == null)
                 ? (post == null) ? script : concat(script, post)
                 : (post == null) ? concat(pre, script)
                   : concat(pre, script, post);
        return script;
    }

    @Override
    public String[] dropSchema(Map<String, Object> settings) {
        Connection conn = null;
        try {
            conn = openConnection(settings);
            DatabaseMetadata metadata =
                    new DatabaseMetadata(conn, getDialectInternal(), true);
            String[] dropScript = getConfig(settings)
                    .generateDropSchemaScript(getDialectInternal(), metadata);
            return dropScript;
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(conn);
        }
    }

    @Override
    public void validateSchema(Map<String, Object> settings) {
        Connection conn = null;
        try {
            conn = openConnection(settings);
            DatabaseMetadata metadata =
                    new DatabaseMetadata(conn, getDialectInternal(), true);
            getConfig(settings).validateSchema(getDialectInternal(), metadata);
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } catch (HibernateException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(conn);
        }
    }

    @Override
    public boolean checkIfSchemaExists(Map<String, Object> settings) {
        Connection conn = null;
        try {
            /* check if any of the needed tables is exisiting */
            conn = openConnection(settings);
            DatabaseMetadata metadata =
                    new DatabaseMetadata(conn, getDialectInternal(), true);
            Iterator<Table> iter = getConfig(settings).getTableMappings();
            while (iter.hasNext()) {
                Table table = iter.next();
                if (table.isPhysicalTable() &&
                    metadata.isTable(table.getName())) {
                    return true;
                }
            }
            return false;
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(conn);
        }
    }

    protected Dialect getDialectInternal() {
        if (dialect == null) {
            dialect = createDialect();
        }
        return dialect;
    }

    public void execute(String[] sql,
                           Map<String, Object> settings)
            throws HibernateException {
        Connection conn = null;
        try {
            execute(sql, conn = openConnection(settings));
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(conn);
        }
    }

    protected void execute(String[] sql,
                           Connection conn)
            throws HibernateException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            for (String cmd : sql) {
                stmt.execute(cmd);
            }

        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(stmt);
        }
    }

    protected void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.error("Error closing connection", e);
            }
        }
    }

    protected void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                LOG.error("Error closing statement", e);
            }
        }
    }

    @Override
    public void validateConnection(Map<String, Object> settings) {
        Connection conn = null;
        try {
            conn = openConnection(settings);
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(conn);
        }
    }


    @Override
    public boolean needsSchema() {
        return true;
    }

    @Override
    public void validatePrerequisites(Map<String, Object> settings) {
        Connection conn = null;
        try {
            conn = openConnection(settings);
            DatabaseMetadata metadata =
                    new DatabaseMetadata(conn, getDialectInternal(), true);
            validatePrerequisites(conn, metadata, settings);
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(conn);
        }
    }

    @Override
    public void validateConnection(Properties current,
                                   Map<String, Object> changed) {
        validateConnection(mergeProperties(current, changed));
    }

    @Override
    public void validatePrerequisites(Properties current,
                                      Map<String, Object> changed) {
        validatePrerequisites(mergeProperties(current, changed));
    }

    @Override
    public void validateSchema(Properties current,
                               Map<String, Object> changed) {
        validateSchema(mergeProperties(current, changed));
    }

    @Override
    public boolean checkIfSchemaExists(Properties current,
                                       Map<String, Object> changed) {
        return checkIfSchemaExists(mergeProperties(current, changed));
    }

    @Override
    public Properties getDatasourceProperties(Properties current,
                                              Map<String, Object> changed) {
        return getDatasourceProperties(mergeProperties(current, changed));
    }

    protected Map<String, Object> mergeProperties(Properties current,
                                                  Map<String, Object> changed) {
        Map<String, Object> settings = parseDatasourceProperties(current);
        settings.putAll(changed);
        return settings;
    }

    protected void addMappingFileDirectories(Map<String, Object> settings,
                                             Properties p) {
        String dirList = resource(CORE_MAPPINGS_PATH).getAbsolutePath();
        Boolean t = (Boolean) settings.get(transactionalDefiniton.getKey());
        if (t.booleanValue()) {
            dirList += SessionFactoryProvider.PATH_SEPERATOR +
                       resource(TRANSACTIONAL_MAPPINGS_PATH).getAbsolutePath();
        }
        p.put(SessionFactoryProvider.HIBERNATE_DIRECTORY, dirList);
    }

    protected boolean isTransactional(Properties properties) {
        String p = properties
                .getProperty(SessionFactoryProvider.HIBERNATE_DIRECTORY);
        return p == null || p.contains(TRANSACTIONAL_MAPPINGS_PATH);
    }

    protected BooleanSettingDefinition getTransactionalDefiniton() {
        return transactionalDefiniton;
    }

    private <T> T[] concat(T[] first, T[]... rest) {
        int length = first.length;
        for (int i = 0; i < rest.length; ++i) {
            length += rest[i].length;
        }
        T[] result = Arrays.copyOf(first, length);
        int offset = first.length;
        for (int i = 0; i < rest.length; ++i) {
            System.arraycopy(rest[i], 0, result, offset, rest[i].length);
            offset += rest[i].length;
        }
        return result;
    }

    /**
     * @return script to run before the schema creation
     */
    protected String[] getPreSchemaScript() {
        return null;
    }

    /**
     * @return script to run after the schema creation
     */
    protected String[] getPostSchemaScript() {
        return null;
    }
    
    protected boolean isSetSchema(Map<String, Object> settings) {
        if (settings.containsKey(HibernateConstants.DEFAULT_SCHEMA)) {
            return StringHelper.isNotEmpty((String)settings.get(HibernateConstants.DEFAULT_SCHEMA));
        }
        return false;
    }

    protected String getSchema(Map<String, Object> settings) {
        if (isSetSchema(settings)) {
            return (String)settings.get(HibernateConstants.DEFAULT_SCHEMA) + ".";
        }
        return "";
    }

    protected abstract Map<String, Object> parseDatasourceProperties(
            Properties current);

    protected abstract void validatePrerequisites(Connection con,
                                                  DatabaseMetadata metadata, Map<String, Object> settings);

    protected abstract Dialect createDialect();

    protected abstract Connection openConnection(Map<String, Object> settings)
            throws SQLException;
}
