package org.n52.sos.ds.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;
import org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.ds.Datasource;

public class OracleDatasource extends AbstractHibernateFullDBDatasource {
	private static final String DIALECT_NAME = "Oracle Spatial";

	private static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.OracleDriver";

	private static final Pattern JDBC_URL_PATTERN = Pattern
			.compile("^jdbc:oracle:thin://([^:]+):([0-9]+)/(.*)$");

	public static final String USERNAME_DESCRIPTION = "Your database server user name. "
			+ "The default value for Oracle Spatial is \"oracle\".";
	public static final String USERNAME_DEFAULT_VALUE = "oracle";
	public static final String PASSWORD_DESCRIPTION = "Your database server password. "
			+ "The default value is \"oracle\".";
	public static final String PASSWORD_DEFAULT_VALUE = "oracle";
	public static final String HOST_DESCRIPTION = "Set this to the IP/net location of "
			+ "Oracle Spatial database server. The default value for Oracle is "
			+ "\"localhost\".";
	public static final String PORT_DESCRIPTION = "Set this to the port number of your "
			+ "Oracle Spatial server. The default value for Oracle is 1521.";
	public static final int PORT_DEFAULT_VALUE = 1521;
	public static final String SCHEMA_DEFAULT_VALUE = "oracle";

	public OracleDatasource() {
		super(USERNAME_DEFAULT_VALUE, USERNAME_DESCRIPTION,
				PASSWORD_DEFAULT_VALUE, PASSWORD_DESCRIPTION,
				DATABASE_DEFAULT_VALUE, null, HOST_DEFAULT_VALUE,
				HOST_DESCRIPTION, PORT_DEFAULT_VALUE, PORT_DESCRIPTION,
				SCHEMA_DEFAULT_VALUE, SCHEMA_DESCRIPTION);
	}

	@Override
	public String getDialectName() {
		return DIALECT_NAME;
	}

	@Override
	public boolean checkSchemaCreation(Map<String, Object> settings) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = openConnection(settings);
			stmt = conn.createStatement();
			doCheckSchemaCreation((String) settings.get(SCHEMA_KEY), stmt);
			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			close(stmt);
			close(conn);
		}
	}

	@Override
	protected String[] getPreSchemaScript() {
		return new String[] { "ALTER SESSION SET deferred_segment_creation=false" };
	}

	/**
	 * A statement provided version of
	 * {@link Datasource#checkSchemaCreation(Map)} for testing
	 */
	void doCheckSchemaCreation(String schema, Statement stmt)
			throws SQLException {
		schema = schema == null ? "" : schema + ".";
		final String command = String
				.format("BEGIN\n"
						+ "  BEGIN\n"
						+ "    EXECUTE IMMEDIATE 'DROP TABLE \"%1$ssos_test\"';\n"
						+ "  EXCEPTION\n"
						+ "    WHEN OTHERS THEN\n"
						+ "      IF SQLCODE != -942 THEN\n"
						+ "        RAISE;\n"
						+ "      END IF;\n"
						+ "  END;\n"
						+ "  EXECUTE IMMEDIATE 'CREATE TABLE \"%1$ssos_test\" (id integer NOT NULL)';\n"
						+ "  EXECUTE IMMEDIATE 'DROP TABLE \"%1$ssos_test\"';\n"
						+ "END;\n", schema);
		stmt.execute(command);
	}

	@Override
	public void clear(Properties properties) {
		Map<String, Object> settings = parseDatasourceProperties(properties);
		CustomConfiguration config = getConfig(settings);

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = openConnection(settings);
			stmt = conn.createStatement();

			Iterator<Table> tables = config.getTableMappings();
			List<String> names = new ArrayList<String>();
			while (tables.hasNext()) {
				Table table = tables.next();
				if (table.isPhysicalTable()) {
					names.add(table.getName());
				}
			}

			while (names.size() > 0) {
				int clearedThisPass = 0;
				for (int i = names.size() - 1; i >= 0; i--) {
					try {
						stmt.execute("DELETE FROM " + names.get(i));
						names.remove(i);
						clearedThisPass++;
					} catch (SQLException ex) {
						// ignore
					}
				}

				if (clearedThisPass == 0) {
					throw new RuntimeException("Cannot clear!");
				}
			}

			conn.commit();
		} catch (SQLException e) {
			throw new RuntimeException("Cannot clear!", e);
		} finally {
			close(stmt);
			close(conn);
		}
	}

	@Override
	public boolean supportsClear() {
		return true;
	}

	@Override
	protected void validatePrerequisites(Connection con,
			DatabaseMetadata metadata, Map<String, Object> settings) {
	}

	@Override
	protected Dialect createDialect() {
		return new OracleSpatial10gDialect();
	}

	@Override
	protected String toURL(Map<String, Object> settings) {
		String url = String.format("jdbc:oracle:thin://%s:%d/%s",
				settings.get(HOST_KEY), settings.get(PORT_KEY),
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
	protected String getDriverClass() {
		return ORACLE_DRIVER_CLASS;
	}
}
