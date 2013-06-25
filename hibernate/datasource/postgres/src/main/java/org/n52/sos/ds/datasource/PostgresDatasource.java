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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;
import org.hibernate.spatial.dialect.postgis.PostgisDialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.StringHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class PostgresDatasource extends AbstractHibernateFullDBDatasource {
    private static final String DIALECT_NAME = "PostgreSQL/PostGIS";

    private static final String POSTGRES_DRIVER_CLASS = "org.postgresql.Driver";

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("^jdbc:postgresql://([^:]+):([0-9]+)/(.*)$");

    public static final String USERNAME_DESCRIPTION =
            "Your database server user name. The default value for PostgreSQL is \"postgres\".";

    public static final String USERNAME_DEFAULT_VALUE = "postgres";

    public static final String PASSWORD_DESCRIPTION =
            "Your database server password. The default value is \"postgres\".";

    public static final String PASSWORD_DEFAULT_VALUE = "postgres";

    public static final String HOST_DESCRIPTION =
            "Set this to the IP/net location of PostgreSQL database server. The default value for PostgreSQL is \"localhost\".";

    public static final String PORT_DESCRIPTION =
            "Set this to the port number of your PostgreSQL server. The default value for PostgreSQL is 5432.";

    public static final int PORT_DEFAULT_VALUE = 5432;

//    public static final String CATALOG_DEFAULT_VALUE = "public";

    public static final String SCHEMA_DEFAULT_VALUE = "public";

    public static final String FUNC_POSTGIS_VERSION = "postgis_version()";

    public static final String TAB_SPATIAL_REF_SYS = "spatial_ref_sys";

    @Override
    public String getDialectName() {
        return DIALECT_NAME;
    }

    @Override
    protected Dialect createDialect() {
        return new PostgisDialect();
    }

    @Override
    protected String getDriverClass() {
        return POSTGRES_DRIVER_CLASS;
    }

    @Override
    public boolean checkSchemaCreation(Map<String, Object> settings) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = openConnection(settings);
            stmt = conn.createStatement();
            String schema = (String) settings.get(createSchemaDefinition().getKey());
            schema = schema == null ? "" : "." + schema;
            final String command =
                    String.format("BEGIN; " + "DROP TABLE IF EXISTS \"%1$ssos_installer_test_table\"; "
                            + "CREATE TABLE \"%1$ssos_installer_test_table\" (id integer NOT NULL); "
                            + "DROP TABLE \"%1$ssos_installer_test_table\"; " + "END;", schema);
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
    protected void validatePrerequisites(Connection con, DatabaseMetadata metadata, Map<String, Object> settings) {
        checkPostgis(con, settings);
        checkSpatialRefSys(con, metadata, settings);
    }

    protected void checkPostgis(Connection con, Map<String, Object> settings) {
        Statement stmt = null;
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(SELECT);
            builder.append(SPACE);
            builder.append(FUNC_POSTGIS_VERSION);
            builder.append(SEMICOLON);
            stmt = con.createStatement();
            stmt.execute(builder.toString());
            // TODO check PostGIS version
        } catch (SQLException ex) {
            throw new ConfigurationException("PostGIS does not seem to be installed.", ex);
        } finally {
            close(stmt);
        }
    }

    protected void checkSpatialRefSys(Connection con, DatabaseMetadata metadata, Map<String, Object> settings) {
        Statement stmt = null;
        try {
            if (!metadata.isTable("spatial_ref_sys")) {
                throw new ConfigurationException("Missing 'spatial_ref_sys' table.");
            }
            StringBuilder builder = new StringBuilder();
            builder.append(SELECT);
            builder.append(SPACE);
            builder.append(DEFAULT_COUNT);
            builder.append(SPACE);
            builder.append(FROM);
            builder.append(SPACE);
            builder.append(TAB_SPATIAL_REF_SYS);
            builder.append(SEMICOLON);
            stmt = con.createStatement();
            stmt.execute(builder.toString());
        } catch (SQLException ex) {
            throw new ConfigurationException("Can not read from table 'spatial_ref_sys'", ex);
        } finally {
            close(stmt);
        }
    }

    @Override
    protected String toURL(
            Map<String, Object> settings) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                                   settings.get(HOST_KEY),
                                   settings.get(PORT_KEY),
                                   settings.get(DATABASE_KEY));
        return url;
    }

    @Override
    protected String[] parseURL(String url) {
        Matcher matcher = JDBC_URL_PATTERN.matcher(url);
        matcher.find();
        return new String[] { matcher.group(1), matcher.group(2),
        	    matcher.group(3) };
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public void clear(Properties properties) {
        Map<String, Object> settings = parseDatasourceProperties(properties);
        CustomConfiguration config = getConfig(settings);
        Iterator<Table> tables = config.getTableMappings();
        List<String> names = new LinkedList<String>();
        while (tables.hasNext()) {
            Table table = tables.next();
            if (table.isPhysicalTable()) {
                names.add(table.getName());
            }
        }
        if (!names.isEmpty()) {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = openConnection(settings);
                stmt = conn.createStatement();
                stmt.execute(String.format("truncate %s restart identity cascade", StringHelper.join(", ", names)));
            } catch (SQLException ex) {
                throw new ConfigurationException(ex);
            } finally {
                close(stmt);
                close(conn);
            }
        }
    }
}
