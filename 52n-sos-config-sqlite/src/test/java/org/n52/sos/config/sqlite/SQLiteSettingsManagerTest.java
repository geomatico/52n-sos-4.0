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
package org.n52.sos.config.sqlite;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.n52.sos.config.sqlite.TestSettingDefinitionProvider.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import static org.hamcrest.core.Is.*;
import org.hibernate.HibernateException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.sos.config.IAdministratorUser;
import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingValue;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.config.sqlite.entities.BooleanSettingValue;
import org.n52.sos.config.sqlite.entities.FileSettingValue;
import org.n52.sos.config.sqlite.entities.IntegerSettingValue;
import org.n52.sos.config.sqlite.entities.NumericSettingValue;
import org.n52.sos.config.sqlite.entities.StringSettingValue;
import org.n52.sos.config.sqlite.entities.UriSettingValue;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.NumericSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SQLiteSettingsManagerTest {

    private static final Logger log = LoggerFactory.getLogger(SQLiteSettingsManagerTest.class);
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";
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
    public void testBooleanSettings() throws ConfigurationException, ConnectionProviderException {
        final BooleanSettingDefinition settingDefinition = new BooleanSettingDefinition().setKey(BOOLEAN_SETTING);
        final ISettingValue<Boolean> settingValue = new BooleanSettingValue().setKey(BOOLEAN_SETTING).setValue(
                Boolean.TRUE);
        final ISettingValue<Boolean> newSettingValue = new BooleanSettingValue().setKey(BOOLEAN_SETTING)
                .setValue(Boolean.FALSE);
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testStringSettings() throws ConfigurationException, ConnectionProviderException {
        final StringSettingDefinition settingDefinition = new StringSettingDefinition().setKey(STRING_SETTING);
        final ISettingValue<String> settingValue = new StringSettingValue().setKey(STRING_SETTING).setValue("string1");
        final ISettingValue<String> newSettingValue = new StringSettingValue().setKey(STRING_SETTING)
                .setValue("string2");
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testFileSettings() throws ConfigurationException, ConnectionProviderException {
        final FileSettingDefinition settingDefinition = new FileSettingDefinition().setKey(FILE_SETTING);
        final ISettingValue<File> settingValue = new FileSettingValue().setKey(FILE_SETTING).setValue(new File(
                "/home/auti/sos1"));
        final ISettingValue<File> newSettingValue = new FileSettingValue().setKey(FILE_SETTING).setValue(new File(
                "/home/auti/sos2"));
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testIntegerSettings() throws ConfigurationException, ConnectionProviderException {
        final IntegerSettingDefinition settingDefinition = new IntegerSettingDefinition().setKey(INTEGER_SETTING);
        final ISettingValue<Integer> settingValue = new IntegerSettingValue().setKey(INTEGER_SETTING).setValue(12312);
        final ISettingValue<Integer> newSettingValue = new IntegerSettingValue().setKey(INTEGER_SETTING).setValue(12311);
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testNumericSettings() throws ConfigurationException, ConnectionProviderException {
        final NumericSettingDefinition settingDefinition = new NumericSettingDefinition().setKey(DOUBLE_SETTING);
        final ISettingValue<Double> settingValue = new NumericSettingValue().setKey(DOUBLE_SETTING).setValue(212.1213);
        final ISettingValue<Double> newSettingValue = new NumericSettingValue().setKey(DOUBLE_SETTING)
                .setValue(212.1211);
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test
    public void testUriSettings() throws ConfigurationException, ConnectionProviderException {
        final UriSettingDefinition settingDefinition = new UriSettingDefinition().setKey(URI_SETTING);
        final ISettingValue<URI> settingValue = new UriSettingValue().setKey(URI_SETTING).setValue(URI.create(
                "http://localhost:8080/a"));
        final ISettingValue<URI> newSettingValue = new UriSettingValue().setKey(URI_SETTING).setValue(URI.create(
                "http://localhost:8080/b"));
        testSaveGetAndDelete(settingDefinition, settingValue, newSettingValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangedSettingsTypeForKey() throws ConfigurationException, ConnectionProviderException {
        final ISettingValue<Double> doubleValue = new NumericSettingValue().setKey(BOOLEAN_SETTING).setValue(212.1213);
        settingsManager.changeSetting(doubleValue);
    }

    public <T> void testSaveGetAndDelete(
            final ISettingDefinition<? extends ISettingDefinition<?, T>, T> settingDefinition,
            final ISettingValue<T> settingValue,
            final ISettingValue<T> newSettingValue) throws ConfigurationException, ConnectionProviderException {

        assertNotEquals(settingValue, newSettingValue);
        settingsManager.changeSetting(settingValue);
        assertEquals(settingValue, settingsManager.getSetting(settingDefinition));

        settingsManager.changeSetting(newSettingValue);
        final ISettingValue<T> value = settingsManager.getSetting(settingDefinition);
        assertEquals(newSettingValue, value);
        assertNotEquals(settingValue, value);

        settingsManager.deleteSetting(settingDefinition);
        assertNull(settingsManager.getSetting(settingDefinition));
    }

    @Test
    public void createAdminUserTest() throws ConnectionProviderException {
        IAdministratorUser au = settingsManager.createAdminUser(USERNAME, PASSWORD);
        assertNotNull(au);
        assertEquals(USERNAME, au.getUsername());
        assertEquals(PASSWORD, au.getPassword());

        IAdministratorUser au2 = settingsManager.getAdminUser(USERNAME);
        assertNotNull(au2);
        assertEquals(au, au2);
    }

    @Test(expected = HibernateException.class)
    public void createDuplicateAdminUser() throws ConnectionProviderException {
        settingsManager.createAdminUser(USERNAME, PASSWORD);
        settingsManager.createAdminUser(USERNAME, PASSWORD);
    }

    @Test
    public void deleteAdminUserTest() throws ConnectionProviderException {
        IAdministratorUser au = settingsManager.getAdminUser(USERNAME);
        if (au == null) {
            au = settingsManager.createAdminUser(USERNAME, PASSWORD);

        }
        assertNotNull(au);
        settingsManager.deleteAdminUser(au);
        assertNull(settingsManager.getAdminUser(USERNAME));

        settingsManager.createAdminUser(USERNAME, PASSWORD);
        assertNotNull(settingsManager.getAdminUser(USERNAME));
        settingsManager.deleteAdminUser(USERNAME);
        assertNull(settingsManager.getAdminUser(USERNAME));
    }
    
    @Test
    public void testActiveOperations() throws ConnectionProviderException {
        RequestOperatorKeyType key = new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS,
                                                                                           Sos2Constants.SERVICEVERSION),
                                                                SosConstants.Operations.GetCapabilities.name());
        
        
        assertThat(settingsManager.isActive(key), is(true));
        settingsManager.setActive(key, true);
        assertThat(settingsManager.isActive(key), is(true));
        settingsManager.setActive(key, false);
        assertThat(settingsManager.isActive(key), is(false));
        
    }
}
