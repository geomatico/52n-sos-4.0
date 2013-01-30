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
