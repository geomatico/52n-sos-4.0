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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.dialect.Dialect;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.util.CollectionHelper;

/**
 * TODO JavaDoc
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class H2InMemoryDatasource extends AbstractHibernateDatasource {
    private static final String DIALECT = "H2/GeoDB (in memory)";
    private static final String H2_DRIVER_CLASS = "org.h2.Driver";
    private static final String H2_DIALECT_CLASS = GeoDBDialect.class.getName();
    private static final String DEFAULT_USERNAME = "sa";
    private static final String DEFAULT_PASSWORD = "";
    private static final String JDBC_URL =
            "jdbc:h2:mem:sos;DB_CLOSE_DELAY=-1;INIT=create domain if not exists geometry as blob";

    @Override
    public String getDialectName() {
        return DIALECT;
    }

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return CollectionHelper
                .<SettingDefinition<?, ?>>set(getTransactionalDefiniton());
    }

    @Override
    public Set<SettingDefinition<?, ?>> getChangableSettingDefinitions() {
        return Collections.emptySet();
    }

    @Override
    public boolean checkSchemaCreation(Map<String, Object> settings) {
        return true;
    }

    @Override
    public boolean supportsTestData() {
        return false;
    }

    @Override
    public void insertTestData(Map<String, Object> settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertTestData(Properties settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTestDataPresent(Properties settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeTestData(Properties settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsClear() {
        return false;
    }

    @Override
    public void clear(Properties settings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Properties getDatasourceProperties(Map<String, Object> settings) {
        Properties p = new Properties();
        p.put(HibernateConstants.CONNECTION_URL, JDBC_URL);
        p.put(HibernateConstants.DRIVER_CLASS, H2_DRIVER_CLASS);
        p.put(HibernateConstants.DIALECT, H2_DIALECT_CLASS);
        p.put(HibernateConstants.CONNECTION_USERNAME, DEFAULT_USERNAME);
        p.put(HibernateConstants.CONNECTION_PASSWORD, DEFAULT_PASSWORD);
        p.put(HibernateConstants.HBM2DDL_AUTO,
              HibernateConstants.HBM2DDL_CREATE);
        addMappingFileDirectories(settings, p);
        return p;
    }

    @Override
    public boolean needsSchema() {
        return false;
    }

    @Override
    protected Map<String, Object> parseDatasourceProperties(Properties current) {
        Map<String, Object> settings = new HashMap<String, Object>(2);
        settings.put(getTransactionalDefiniton().getKey(),
                     isTransactional(current));
        return settings;
    }

    @Override
    protected void validatePrerequisites(Connection con,
                                         DatabaseMetadata metadata) {
    }

    @Override
    protected Dialect createDialect() {
        return new GeoDBDialect();
    }

    @Override
    protected Connection openConnection(Map<String, Object> settings)
            throws SQLException {
        try {
            Class.forName(H2_DRIVER_CLASS);
            return DriverManager.getConnection(JDBC_URL,
                                               DEFAULT_USERNAME,
                                               DEFAULT_PASSWORD);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }
}
