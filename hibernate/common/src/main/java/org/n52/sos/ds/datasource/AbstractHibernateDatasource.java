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

import static org.n52.sos.ds.Datasource.ADVANCED_GROUP;
import static org.n52.sos.ds.Datasource.BASE_GROUP;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractHibernateDatasource implements Datasource {
    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractHibernateDatasource.class);
    public static final String CORE_MAPPINGS_PATH =
            "/mapping/core";
    public static final String TRANSACTIONAL_MAPPINGS_PATH =
            "/mapping/transactional";
    private Dialect dialect;
    private final BooleanSettingDefinition transactionalDefiniton =
            createTransactionalDefinition();

    protected StringSettingDefinition createUsernameDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(1)
                .setKey(HibernateConstants.CONNECTION_USERNAME)
                .setTitle("User Name");
    }

    protected StringSettingDefinition createPasswordDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(2)
                .setKey(HibernateConstants.CONNECTION_PASSWORD)
                .setTitle("Password");
    }

    protected StringSettingDefinition createDatabaseDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(3)
                .setKey("jdbc.database")
                .setTitle("Database")
                .setDescription("Set this to the name of the database you want to use for SOS.")
                .setDefaultValue("sos");
    }

    protected StringSettingDefinition createHostDefinition() {
        return new StringSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(4)
                .setKey("jdbc.host")
                .setTitle("Host")
                .setDescription("Set this to the IP/net location of the database server. The default value for is \"localhost\".")
                .setDefaultValue("localhost");
    }

    protected IntegerSettingDefinition createPortDefinition() {
        return new IntegerSettingDefinition()
                .setGroup(BASE_GROUP)
                .setOrder(5)
                .setKey("jdbc.port")
                .setTitle("Port");
    }
    protected StringSettingDefinition createCatalogDefinition() {
        return new StringSettingDefinition()
                .setGroup(ADVANCED_GROUP)
                .setOrder(1)
                .setKey(HibernateConstants.DEFAULT_CATALOG)
                .setTitle("Schema")
                .setDescription("Qualifies unqualified table names with the given schema in generated SQL.")
                .setDefaultValue("public");
    }

    protected BooleanSettingDefinition createTransactionalDefinition() {
        return new BooleanSettingDefinition()
                .setDefaultValue(true)
                .setTitle("Transactional Profile")
                .setDescription("Should the database support the transactional profile?")
                .setGroup(ADVANCED_GROUP)
                .setOrder(2)
                .setOptional(false)
                .setKey("sos.transactional");
    }

    public CustomConfiguration getConfig(Map<String, Object> settings) {
        CustomConfiguration config = new CustomConfiguration();
        config.configure("/sos-hibernate.cfg.xml");
        config.addDirectory(resource("/mapping/core"));
        Boolean transactional = (Boolean) settings.get(
                this.transactionalDefiniton.getKey());
        if (transactional.booleanValue()) {
            config.addDirectory(resource("/mapping/transactional"));
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
    public void createSchema(Map<String, Object> settings) {
        String[] script = getConfig(settings)
                .generateSchemaCreationScript(getDialectInternal());
        String[] pre = getPreSchemaScript();
        String[] post = getPostSchemaScript();

        script = (pre == null)
                 ? (post == null) ? script : concat(script, post)
                 : (post == null) ? concat(pre, script)
                   : concat(pre, script, post);

        execute(script, settings);
    }

    @Override
    public void dropSchema(Map<String, Object> settings) {
        Connection conn = null;
        try {
            conn = openConnection(settings);
            DatabaseMetadata metadata =
                    new DatabaseMetadata(conn, getDialectInternal(), true);
            String[] dropScript = getConfig(settings)
                    .generateDropSchemaScript(getDialectInternal(), metadata);
            execute(dropScript, settings);
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

    protected void execute(String[] sql,
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
            validatePrerequisites(conn, metadata);
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

    protected abstract Map<String, Object> parseDatasourceProperties(
            Properties current);

    protected abstract void validatePrerequisites(Connection con,
                                                  DatabaseMetadata metadata);

    protected abstract Dialect createDialect();

    protected abstract Connection openConnection(Map<String, Object> settings)
            throws SQLException;
}
