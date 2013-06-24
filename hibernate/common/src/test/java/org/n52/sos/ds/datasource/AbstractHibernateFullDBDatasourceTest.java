package org.n52.sos.ds.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.ds.hibernate.util.HibernateConstants;

public class AbstractHibernateFullDBDatasourceTest extends TestCase {
	private AbstractHibernateFullDBDatasource ds;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ds = new MockDatasource();
	}

	public void testGetSettingDefinitions() throws Exception {
		Set<SettingDefinition<?, ?>> settings = ds.getSettingDefinitions();
		checkSettingDefinitionsTransactional(settings);
	}

	public void testGetChangableSettingDefinitions() throws Exception {
		Set<SettingDefinition<?, ?>> settings = ds
				.getChangableSettingDefinitions(new Properties());
		checkSettingDefinitionsNonTransactional(settings);
	}

	public void testParseDatasourceProperties() throws Exception {
		Properties current = new Properties();
		current.put(HibernateConstants.DEFAULT_CATALOG, "public");
		current.put(HibernateConstants.CONNECTION_USERNAME, "postgres");
		current.put(HibernateConstants.CONNECTION_PASSWORD, "postgres");
		current.put(HibernateConstants.CONNECTION_URL,
				"jdbc:postgresql://localhost:5432/test");

		Map<String, Object> settings = ds.parseDatasourceProperties(current);
		checkSettingKeysTransactional(settings.keySet());
	}

	private void checkSettingDefinitionsTransactional(
			Set<SettingDefinition<?, ?>> settings) {
		checkSettingDefinitions(settings, true);
	}

	private void checkSettingDefinitionsNonTransactional(
			Set<SettingDefinition<?, ?>> settings) {
		checkSettingDefinitions(settings, false);
	}

	private void checkSettingDefinitions(Set<SettingDefinition<?, ?>> settings,
			boolean transactional) {
		List<String> keys = new ArrayList<String>();
		Iterator<SettingDefinition<?, ?>> iterator = settings.iterator();
		while (iterator.hasNext()) {
			keys.add(iterator.next().getKey());
		}
		checkSettingKeys(keys, transactional);
	}

	private void checkSettingKeysTransactional(Collection<String> keys) {
		checkSettingKeys(keys, true);
	}

	private void checkSettingKeys(Collection<String> keys, boolean transactional) {
		assertEquals(transactional ? 7 : 6, keys.size());
		assertTrue(keys.contains(AbstractHibernateDatasource.HOST_KEY));
		assertTrue(keys.contains(AbstractHibernateDatasource.PORT_KEY));
		assertTrue(keys.contains(AbstractHibernateDatasource.DATABASE_KEY));
		assertTrue(keys.contains(AbstractHibernateDatasource.USERNAME_KEY));
		assertTrue(keys.contains(AbstractHibernateDatasource.PASSWORD_KEY));
		assertTrue(keys.contains(AbstractHibernateDatasource.SCHEMA_KEY));
	}

	private class MockDatasource extends AbstractHibernateFullDBDatasource {
		@Override
		protected Dialect createDialect() {
			return null;
		}

		@Override
		public String getDialectName() {
			return null;
		}

		@Override
		public boolean checkSchemaCreation(Map<String, Object> settings) {
			return false;
		}

		@Override
		protected String toURL(Map<String, Object> settings) {
			return null;
		}

		@Override
		protected String[] parseURL(String url) {
			return new String[] { "localhost", "5432", "db" };
		}

		@Override
		protected String getDriverClass() {
			return null;
		}

		@Override
		public boolean supportsTestData() {
			return false;
		}

		@Override
		public void insertTestData(Map<String, Object> settings) {
		}

		@Override
		public void insertTestData(Properties settings) {
		}

		@Override
		public boolean isTestDataPresent(Properties settings) {
			return false;
		}

		@Override
		public void removeTestData(Properties settings) {
		}

		@Override
		public void clear(Properties settings) {
		}

		@Override
		public boolean supportsClear() {
			return false;
		}

		@Override
		protected void validatePrerequisites(Connection con,
				DatabaseMetadata metadata, Map<String, Object> settings) {
		}
	}
}
