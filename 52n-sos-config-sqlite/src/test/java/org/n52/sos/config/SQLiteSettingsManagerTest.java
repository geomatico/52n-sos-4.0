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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
public class SQLiteSettingsManagerTest {

    private static final String URI_SETTING = "uri_setting";
    private static final String DOUBLE_SETTING = "double_setting";
    private static final String INTEGER_SETTING = "integer_setting";
    private static final String FILE_SETTING = "file_setting";
    private static final String STRING_SETTING = "string_setting";
    private static final String BOOLEAN_SETTING = "boolean_setting";
    private static final Logger log = LoggerFactory.getLogger(SQLiteSettingsManagerTest.class);
    private static IConnectionProvider connectionProvider;
    private static File databaseFile;
    private SettingsManager settingsManager;

    @BeforeClass
    public static void setUpClass() throws ConfigurationException, IOException {
        databaseFile = File.createTempFile("configuration-test", ".db");
        Properties properties = new Properties();
        properties.put(SQLiteSessionFactory.HIBERNATE_CONNECTION_URL,
                       String.format("jdbc:sqlite:%s", databaseFile.getAbsolutePath()));
        connectionProvider = new SQLiteSessionFactory();
        connectionProvider.initialize(properties);
        log.info("using database file: {}", databaseFile.getAbsolutePath());
    }

    @AfterClass
    public static void tearDownClass() {
        if (connectionProvider != null) {
            connectionProvider.cleanup();
        }
        if (databaseFile != null && databaseFile.exists()) {
            databaseFile.delete();
        }
    }

    @Before
    public void setUp() throws ConfigurationException {
        SQLiteSettingsManager manager = new SQLiteSettingsManager();
        manager.setConnectionProvider(connectionProvider);
        this.settingsManager = manager;
    }

    @Test
    public void testBooleanSettings() throws ConfigurationException {
        final ISettingDefinition<Boolean> settingDefinition = new BooleanSettingDefinition().setKey(BOOLEAN_SETTING);
        final ISettingValue<Boolean> settingValue = new BooleanSettingValue().setKey(BOOLEAN_SETTING).setValue(
                Boolean.TRUE);
        final ISettingValue<Boolean> newSettingValue = new BooleanSettingValue().setKey(BOOLEAN_SETTING)
                .setValue(Boolean.FALSE);
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testStringSettings() throws ConfigurationException {
        final ISettingDefinition<String> settingDefinition = new StringSettingDefinition().setKey(STRING_SETTING);
        final ISettingValue<String> settingValue = new StringSettingValue().setKey(STRING_SETTING).setValue("string1");
        final ISettingValue<String> newSettingValue = new StringSettingValue().setKey(STRING_SETTING)
                .setValue("string2");
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testFileSettings() throws ConfigurationException {
        final ISettingDefinition<File> settingDefinition = new FileSettingDefinition().setKey(FILE_SETTING);
        final ISettingValue<File> settingValue = new FileSettingValue().setKey(FILE_SETTING).setValue(new File(
                "/home/auti/sos1"));
        final ISettingValue<File> newSettingValue = new FileSettingValue().setKey(FILE_SETTING).setValue(new File(
                "/home/auti/sos2"));
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testIntegerSettings() throws ConfigurationException {
        final ISettingDefinition<Integer> settingDefinition = new IntegerSettingDefinition().setKey(INTEGER_SETTING);
        final ISettingValue<Integer> settingValue = new IntegerSettingValue().setKey(INTEGER_SETTING).setValue(12312);
        final ISettingValue<Integer> newSettingValue = new IntegerSettingValue().setKey(INTEGER_SETTING).setValue(12311);
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testNumericSettings() throws ConfigurationException {
        final ISettingDefinition<Double> settingDefinition = new NumericSettingDefinition().setKey(DOUBLE_SETTING);
        final ISettingValue<Double> settingValue = new NumericSettingValue().setKey(DOUBLE_SETTING).setValue(212.1213);
        final ISettingValue<Double> newSettingValue = new NumericSettingValue().setKey(DOUBLE_SETTING)
                .setValue(212.1211);
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testUriSettings() throws ConfigurationException {
        final ISettingDefinition<URI> settingDefinition = new UriSettingDefinition().setKey(URI_SETTING);
        final ISettingValue<URI> settingValue = new UriSettingValue().setKey(URI_SETTING).setValue(URI.create(
                "http://localhost:8080/a"));
        final ISettingValue<URI> newSettingValue = new UriSettingValue().setKey(URI_SETTING).setValue(URI.create(
                "http://localhost:8080/b"));
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testChangedSettingsTypeForKey() throws ConfigurationException {
        final ISettingDefinition<Boolean> booleanDefinition = new BooleanSettingDefinition().setKey(BOOLEAN_SETTING);
        final ISettingDefinition<Double> doubleDefinition = new NumericSettingDefinition().setKey(BOOLEAN_SETTING);
        final ISettingValue<Boolean> booleanValue = new BooleanSettingValue().setKey(BOOLEAN_SETTING).setValue(Boolean.TRUE);
        final ISettingValue<Double> doubleValue = new NumericSettingValue().setKey(BOOLEAN_SETTING).setValue(212.1213);
        
        settingsManager.changeSetting(doubleDefinition, doubleValue);
        assertEquals(doubleValue, settingsManager.getSetting(doubleDefinition));

        settingsManager.changeSetting(booleanDefinition, booleanValue);
        assertEquals(booleanValue, settingsManager.getSetting(booleanDefinition));
    }

    public <T> void testSaveGetAndDelete(
            final ISettingDefinition<T> settingDefinition,
            final ISettingValue<T> settingValue,
            final ISettingValue<T> newSettingValue) throws ConfigurationException {

        assertNotEquals(settingValue, newSettingValue);
        settingsManager.changeSetting(settingDefinition, settingValue);
        assertEquals(settingValue, settingsManager.getSetting(settingDefinition));

        settingsManager.changeSetting(settingDefinition, newSettingValue);
        final ISettingValue<T> value = settingsManager.getSetting(settingDefinition);
        assertEquals(newSettingValue, value);
        assertNotEquals(settingValue, value);

        settingsManager.deleteSetting(settingDefinition);
        assertNull(settingsManager.getSetting(settingDefinition));
    }
}
