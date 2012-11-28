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
package org.n52.sos.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosContextListener implements ServletContextListener {
	
	public static final String INIT_PARAM_DATA_SOURCE_CONFIG_LOCATION = "datasourceConfigLocation";
	private static final Logger log = LoggerFactory.getLogger(SosContextListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (Configurator.getInstance() == null) {
			ServletContext context = sce.getServletContext();
			String initParam = context.getInitParameter(INIT_PARAM_DATA_SOURCE_CONFIG_LOCATION);
			if (initParam == null) {
				throw new RuntimeException("Missing InitParameter '" + INIT_PARAM_DATA_SOURCE_CONFIG_LOCATION + "'");
			}
			File config = new File(context.getRealPath(initParam));
			if (config.exists()) {
				try {
					log.debug("Initialising Configurator ({},{})", 
                            config.getAbsolutePath(), context.getRealPath("/"));
                    FileInputStream in = null;
                    Properties p = new Properties();
                    try {
                        in = new FileInputStream(config);
                        p.load(in);
                    } catch(IOException e) {
                        log.error("Can not load configuration file");
                        throw new RuntimeException(e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch(IOException e) {
                                log.error("Can not close file", e);
                            }
                        }
                    }
					Configurator.getInstance(p, context.getRealPath("/"));
				} catch (ConfigurationException ce) {
					String message = "Configurator initialization failed!";
					log.error(message, ce);
					throw new RuntimeException(message, ce);
				}
			} else {
				log.warn("Can not initialize Configurator; config file is not present: {}", 
                        config.getAbsolutePath());
			}
		} else {
			log.warn("Configurator already instantiated.");
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (Configurator.getInstance() != null) {
			Configurator.getInstance().cleanup();
		}
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				log.info("Deregistering jdbc driver: {}", driver);
			} catch (Exception e) {
				log.error("Error deregistering driver " + driver, e);
			}

		}
	}
}
