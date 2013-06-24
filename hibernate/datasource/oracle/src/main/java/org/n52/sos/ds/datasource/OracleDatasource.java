package org.n52.sos.ds.datasource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.dialect.Dialect;
import org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.ds.Datasource;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleDatasource extends AbstractHibernateFullDBDatasource {
	private static final Logger LOG = LoggerFactory
			.getLogger(AbstractHibernateDatasource.class);

	private static final String SQL_FILE_INSERT_TEST_DATA = "insert_test_data.sql";

	private static final String TEST_ID_PREFIX = "http://www.52north.org/test/";

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
	public boolean supportsTestData() {
		return true;
	}

	@Override
	public void insertTestData(Map<String, Object> settings) {
		try {
			String[] script = readFile(SQL_FILE_INSERT_TEST_DATA);
			execute(script, settings);
		} catch (IOException e) {
			LOG.error("Cannot read SQL file for removing test data", e);
		}
	}

	@Override
	public void insertTestData(Properties settings) {
		insertTestData(parseDatasourceProperties(settings));
	}

	@Override
	public boolean isTestDataPresent(Properties settings) {
		Set<String> offerings = Configurator.getInstance().getCache()
				.getFeaturesOfInterest();
		for (String offering : offerings) {
			if (offering.startsWith(TEST_ID_PREFIX)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void removeTestData(Properties settings) {
		throw new UnsupportedOperationException(getClass().getCanonicalName()
				+ ".removeTestData() not yet implemented");
	}

	@Override
	public void clear(Properties properties) {
		throw new UnsupportedOperationException(getClass().getCanonicalName()
				+ ".clear() not yet implemented");
	}

	@Override
	public boolean supportsClear() {
		return false;
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

	private String[] readFile(String file) throws IOException {
		InputStream resource = getClass().getResourceAsStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				resource));
		String line;
		List<String> lines = new ArrayList<String>();
		String current = "";
		while ((line = reader.readLine()) != null) {
			if (line.endsWith(";")) {
				current += line.substring(0, line.length() - 1);
				lines.add(current);
				current = "";
			} else {
				current += line + " ";
			}
		}

		return lines.toArray(new String[lines.size()]);
	}
}
