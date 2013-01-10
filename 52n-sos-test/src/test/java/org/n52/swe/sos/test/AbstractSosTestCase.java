/**
 * Copyright (C) 2012
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
package org.n52.swe.sos.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSosTestCase extends Assert {

    protected static final Logger log = LoggerFactory.getLogger(AbstractSosTestCase.class);
    private static final String DATASOURCE_PROPERTIES = "/datasource.properties";

    @BeforeClass
    public static void beforeClass() throws ConfigurationException, IOException {
        if (Configurator.getInstance() == null) {
            Properties p = new Properties();
            InputStream in = null;
            try {
                in = AbstractSosTestCase.class.getResourceAsStream(DATASOURCE_PROPERTIES);
                if (in == null) {
                    throw new ConfigurationException("No \"" + DATASOURCE_PROPERTIES + "\" found in src/test/resources.");
                }
                p.load(AbstractSosTestCase.class.getResourceAsStream(DATASOURCE_PROPERTIES));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("Error closing file", e);
                    }
                }
            }
            Configurator.getInstance(p, "/");
        }
    }

    public AbstractSosTestCase() {
    }
}
