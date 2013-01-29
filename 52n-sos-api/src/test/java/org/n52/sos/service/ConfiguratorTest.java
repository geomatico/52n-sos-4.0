package org.n52.sos.service;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ConfiguratorTest {

    @Test(expected=ConfigurationException.class)
    public void createConfiguratorTest() throws ConfigurationException {
        assertNotNull(Configurator.createInstance(null, null));
        
    }

    @Test(expected=ConfigurationException.class)
    public void createConfiguratorWithEmptyPropertiesTest() throws ConfigurationException {
        assertNotNull(Configurator.createInstance(new Properties(), null));
    }

    @Test(expected=ConfigurationException.class)
    public void createConfiguratorWithEmptyPropertieAndEmptyBasepathTest() throws ConfigurationException {
        assertNotNull(Configurator.createInstance(new Properties(), ""));
    }

    @Test(expected=ConfigurationException.class)
    public void createConfiguratorWithNullPropertieAndEmptyBasepathTest() throws ConfigurationException {
        assertNotNull(Configurator.createInstance(null, ""));
    }
    
    @Ignore("Make Configurator initialization more test friendly.")
    @Test public void 
    createInstanceShouldReturnInstance()
            throws Exception {
        Properties config = new Properties();
        config.load(getClass().getResourceAsStream("/test-config.properties"));
        assertNotNull(Configurator.createInstance(config, ""));
    }
    
}
