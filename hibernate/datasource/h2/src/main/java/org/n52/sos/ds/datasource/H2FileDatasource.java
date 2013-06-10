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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.CollectionHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class H2FileDatasource extends AbstractH2Datasource {
    private static final String DIALECT = "H2/GeoDB (file based)";
    private static final Pattern JDBC_URL_PATTERN =
            Pattern.compile("^jdbc:h2:(.+); INIT=create domain if not exists geometry as blob$");
    private static final String JDBC_URL_FORMAT =
            "jdbc:h2:%s; INIT=create domain if not exists geometry as blob";
    private final StringSettingDefinition h2Database =
            createDatabaseDefinition()
            .setDescription("Set this to the name/path of the database you want to use for SOS.")
            .setDefaultValue(System.getProperty("user.home") +
                             File.separator + "sos");

    @Override
    protected Connection openConnection(Map<String, Object> settings) throws
            SQLException {
        try {
            String jdbc = toURL(settings);
            Class.forName(H2_DRIVER_CLASS);
            return DriverManager
                    .getConnection(jdbc, DEFAULT_USERNAME, DEFAULT_PASSWORD);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getDialectName() {
        return DIALECT;
    }

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return CollectionHelper
                .<SettingDefinition<?, ?>>set(h2Database, getTransactionalDefiniton());
    }

    @Override
    public Properties getDatasourceProperties(Map<String, Object> settings) {
        Properties p = new Properties();
        p.put(HibernateConstants.CONNECTION_URL, toURL(settings));
        p.put(HibernateConstants.DRIVER_CLASS, H2_DRIVER_CLASS);
        p.put(HibernateConstants.DIALECT, H2_DIALECT_CLASS);
        p.put(HibernateConstants.CONNECTION_USERNAME, DEFAULT_USERNAME);
        p.put(HibernateConstants.CONNECTION_PASSWORD, DEFAULT_PASSWORD);
        p.put(HibernateConstants.CONNECTION_POOL_SIZE, "1");
        p.put(HibernateConstants.CONNECTION_RELEASE_MODE, HibernateConstants.CONNECTION_RELEASE_MODE_AFTER_TRANSACTION);
        p.put(HibernateConstants.CURRENT_SESSION_CONTEXT, HibernateConstants.THREAD_LOCAL_SESSION_CONTEXT);
        addMappingFileDirectories(settings, p);
        return p;
    }

    @Override
    protected Map<String, Object> parseDatasourceProperties(Properties current) {
        Map<String, Object> settings = new HashMap<String, Object>(2);
        Matcher matcher = JDBC_URL_PATTERN.matcher(current
                .getProperty(HibernateConstants.CONNECTION_URL));
        matcher.find();
        settings.put(h2Database.getKey(), matcher.group(1));
        settings.put(getTransactionalDefiniton().getKey(),
                     isTransactional(current));
        return settings;
    }

    private String toURL(Map<String, Object> settings) {
        return String.format(JDBC_URL_FORMAT, settings.get(h2Database.getKey()));
    }

    @Override
    public boolean checkSchemaCreation(Map<String, Object> settings) {
        String path = (String) settings.get(h2Database.getKey());
        File f = new File(path + ".h2.db");
        if (f.exists()) {
            return false;
        } else {
            File parent = f.getParentFile();
            if (parent != null) {
                if (!parent.exists()) {
                    boolean mkdirs = parent.mkdirs();
                    if (!mkdirs) {
                        return false;
                    }
                }
            }
            try {
                boolean created = f.createNewFile();
                if (created) {
                    f.delete();
                }
                return created;
            } catch (IOException ex) {
                throw new ConfigurationException(ex);
            }
        }
    }

    @Override
    protected void validatePrerequisites(Connection con,
                                         DatabaseMetadata metadata) {
    }

    @Override
    public boolean supportsTestData() {
        return false;
    }

    @Override
    public void insertTestData(Map<String, Object> settings) {
        /* TODO implement org.n52.sos.ds.datasource.H2FileDatasource.insertTestData() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.H2FileDatasource.insertTestData() not yet implemented");
    }

    @Override
    public void insertTestData(Properties settings) {
        /* TODO implement org.n52.sos.ds.datasource.H2FileDatasource.insertTestData() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.H2FileDatasource.insertTestData() not yet implemented");
    }

    @Override
    public boolean isTestDataPresent(Properties settings) {
        /* TODO implement org.n52.sos.ds.datasource.H2FileDatasource.isTestDataPresent() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.H2FileDatasource.isTestDataPresent() not yet implemented");
    }

    @Override
    public void removeTestData(Properties settings) {
        /* TODO implement org.n52.sos.ds.datasource.H2FileDatasource.removeTestData() */
        throw new UnsupportedOperationException("org.n52.sos.ds.datasource.H2FileDatasource.removeTestData() not yet implemented");
    }
}
