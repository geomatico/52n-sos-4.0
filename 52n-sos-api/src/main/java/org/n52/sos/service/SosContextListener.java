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

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.n52.sos.config.SettingsManager;
import org.n52.sos.util.Cleanupable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(SosContextListener.class);
    private static String path = null;

    public static String getPath() {
        return SosContextListener.path;
    }

    public static boolean hasPath() {
        return SosContextListener.path != null;
    }

    public static void setPath(String path) {
        SosContextListener.path = path;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        setPath(sce.getServletContext().getRealPath("/"));
        if (Configurator.getInstance() == null) {
            instantiateConfigurator(sce.getServletContext());
        } else {
            log.error("Configurator already instantiated.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        cleanupConfigurator();
        cleanupSettingsManager();
        cleanupDrivers();
    }

    protected void cleanupDrivers() {
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

    protected void cleanupSettingsManager() {
        try {
            if (SettingsManager.getInstance() != null) {
                SettingsManager.getInstance().cleanup();
            }
        } catch (Throwable ex) {
            log.error("Error while SettingsManager clean up", ex);
        }
    }

    protected void cleanupConfigurator() {
        try {
            if (Configurator.getInstance() != null) {
                Configurator.getInstance().cleanup();
            }
        } catch (Throwable ex) {
            log.error("Error while Configurator clean up", ex);
        }
    }

    protected void instantiateConfigurator(ServletContext context) throws RuntimeException {
        DatabaseSettingsHandler dbsh = DatabaseSettingsHandler.getInstance(context);
        if (dbsh.exists()) {
            log.debug("Initialising Configurator ({},{})", dbsh.getPath(), getPath());
            try {
                instantiateConfigurator(dbsh.getAll());
            } catch (ConfigurationException ex) {
                log.error("Error reading database properties", ex);
            }
        } else {
            log.warn("Can not initialize Configurator; config file is not present: {}", dbsh.getPath());
        }
    }

    protected void instantiateConfigurator(Properties p) {
        try {
            Configurator.createInstance(p, getPath());
        } catch (ConfigurationException ce) {
            String message = "Configurator initialization failed!";
            log.error(message, ce);
            throw new RuntimeException(message, ce);
        }
    }
}
