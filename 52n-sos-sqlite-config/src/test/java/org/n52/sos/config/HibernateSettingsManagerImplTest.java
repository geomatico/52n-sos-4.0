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
package org.n52.sos.config;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.n52.sos.config.entities.BooleanSettingValue;
import org.n52.sos.config.entities.FileSettingValue;
import org.n52.sos.config.entities.IntegerSettingValue;
import org.n52.sos.config.entities.NumericSettingValue;
import org.n52.sos.config.entities.StringSettingValue;
import org.n52.sos.config.entities.UriSettingValue;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.NumericSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.service.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class HibernateSettingsManagerImplTest {

    private static final String URI_SETTING = "uri_setting";
    private static final String DOUBLE_SETTING = "double_setting";
    private static final String INTEGER_SETTING = "integer_setting";
    private static final String FILE_SETTING = "file_setting";
    private static final String STRING_SETTING = "string_setting";
    private static final String BOOLEAN_SETTING = "boolean_setting";
    
    private static final Logger log = LoggerFactory.getLogger(HibernateSettingsManagerImplTest.class);
    private static IConnectionProvider connectionProvider;
    private static File databaseFile;
    private HibernateSettingsManagerImpl dao;
    
    @BeforeClass
    public static void setUpClass() throws ConfigurationException, IOException {
        databaseFile = File.createTempFile("configuration-test", ".db");
        Properties properties = new Properties();
        properties.put(HibernateSettingsSessionFactory.HIBERNATE_CONNECTION_URL, 
                String.format("jdbc:sqlite:%s", databaseFile.getAbsolutePath()));
        connectionProvider = new HibernateSettingsSessionFactory();
        connectionProvider.initialize(properties);
        log.info("using database file: {}", databaseFile.getAbsolutePath());
    }

    @AfterClass
    public static void tearDownClass() {
        if (connectionProvider != null) {
            connectionProvider.cleanup();
        }
        if (databaseFile !=  null && databaseFile.exists()) {
            databaseFile.delete();
        }
    }

    @Before
    public void setUp() throws ConfigurationException {
        this.dao = new HibernateSettingsManagerImpl();
        this.dao.setConnectionProvider(connectionProvider);
    }

    @Test
    public void testBooleanSettings() {
        Boolean value = Boolean.FALSE;
        dao.saveValue(new BooleanSettingValue().setKey(BOOLEAN_SETTING).setValue(value));
        assertEquals(value, dao.getValue(new BooleanSettingDefinition().setKey(BOOLEAN_SETTING)).getValue());
    }

    @Test
    public void testStringSettings() {
        String value = "string";
        dao.saveValue(new StringSettingValue().setKey(STRING_SETTING).setValue(value));
        assertEquals(value, dao.getValue(new StringSettingDefinition().setKey(STRING_SETTING)).getValue());
    }

    @Test
    public void testFileSettings() {
        File value = new File("/home/auti/sos");
        dao.saveValue(new FileSettingValue().setKey(FILE_SETTING).setValue(value));
        assertEquals(value, dao.getValue(new FileSettingDefinition().setKey(FILE_SETTING)).getValue());
    }

    @Test
    public void testIntegerSettings() {
        Integer value = Integer.valueOf(12312);
        dao.saveValue(new IntegerSettingValue().setKey(INTEGER_SETTING).setValue(value));
        assertEquals(value, dao.getValue(new IntegerSettingDefinition().setKey(INTEGER_SETTING)).getValue());
    }

    @Test
    public void testNumericSettings() {
        double value = 212.1213;
        dao.saveValue(new NumericSettingValue().setKey(DOUBLE_SETTING).setValue(Double.valueOf(value)));
        assertEquals(value, dao.getValue(new NumericSettingDefinition().setKey(DOUBLE_SETTING)).getValue(), 0.0000001);

    }

    @Test
    public void testUriSettings() {
        URI value = URI.create("http://localhost:8080/a");
        dao.saveValue(new UriSettingValue().setKey(URI_SETTING).setValue(value));
        assertEquals(value, dao.getValue(new UriSettingDefinition().setKey(URI_SETTING)).getValue());
    }
}
