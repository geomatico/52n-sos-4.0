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
package org.n52.sos.ds.hibernate;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;
import org.n52.sos.cache.ctrl.ScheduledContentCacheControllerSettings;
import org.n52.sos.config.sqlite.SQLiteSessionFactory;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.SosContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(H2Configuration.class);
    private static final String HIBERNATE_CONNECTION_URL = SQLiteSessionFactory.HIBERNATE_CONNECTION_URL;
    private static final String HIBERNATE_CONNECTION_DRIVER_CLASS =
                                SQLiteSessionFactory.HIBERNATE_CONNECTION_DRIVER_CLASS;
    private static final String HIBERNATE_DIALECT = SQLiteSessionFactory.HIBERNATE_DIALECT;
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String H2_CONNECTION_URL = "jdbc:h2:mem:sos;DB_CLOSE_DELAY=-1";
    private static final Properties PROPERTIES = new Properties() {
        private static final long serialVersionUID = 3109256773218160485L;
        {
            put(HIBERNATE_CONNECTION_URL, H2_CONNECTION_URL);
            put(HIBERNATE_CONNECTION_DRIVER_CLASS, H2_DRIVER);
            put(HIBERNATE_DIALECT, GeoDBDialect.class.getName());
        }
    };
    private static final Object lock = new Object();
    private static H2Configuration instance;

    public static void assertInitialized() {
        synchronized (lock) {
            if (instance == null) {
                try {
                    instance = new H2Configuration();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (OwsExceptionReport ex) {
                    throw new RuntimeException(ex);
                } catch (ConnectionProviderException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    protected static Session getSession() {
        H2Configuration.assertInitialized();
        try {
            return (Session) Configurator.getInstance().getDataConnectionProvider().getConnection();
        } catch (ConnectionProviderException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected static void returnSession(Session session) {
        if (session != null) {
            Configurator.getInstance().getDataConnectionProvider().returnConnection(session);
        }
    }
    private File tempDir;


    private H2Configuration() throws IOException, OwsExceptionReport, ConnectionProviderException {
        init();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                cleanup();
            }
        }));
    }

    private void cleanup() throws RuntimeException {
        try {
            final Configurator configurator = Configurator.getInstance();
            if (configurator != null) {
                configurator.cleanup();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        try {
            final File directory = getTempDir();
            if (directory != null && directory.exists()) {
                FileUtils.deleteDirectory(directory);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void setDefaultSettings() {
        ScheduledContentCacheControllerSettings.CACHE_UPDATE_INTERVAL_DEFINITION.setDefaultValue(0);
    }

    private File getTempDir() {
        return tempDir;
    }

    private void setTempDir(File aTempDir) {
        tempDir = aTempDir;
    }

    private void createTempDir() throws IOException {
        setTempDir(File.createTempFile("hibernate-test-case", ""));
        getTempDir().delete();
        FileUtils.forceMkdir(getTempDir());
        SosContextListener.setPath(getTempDir().getAbsolutePath());
    }
    private void createConfigurator() throws ConfigurationException {
        Configurator.createInstance(PROPERTIES, getTempDir().getAbsolutePath());
    }

    private void prepareDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(H2_DRIVER);
            conn = DriverManager.getConnection(H2_CONNECTION_URL);
            stmt = conn.createStatement();
            String cmd = "create domain geometry as blob";
            LOG.debug("Executing {}", cmd);
            stmt.execute(cmd);
            Configuration configuration = new Configuration().configure("/sos-hibernate.cfg.xml");
            String[] ddl = configuration.generateSchemaCreationScript(new GeoDBDialect());
            for (String s : ddl) {
                LOG.debug("Executing {}", s);
                stmt.execute(s);
            }
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    private void init() throws ConfigurationException, IOException {
        setDefaultSettings();
        createTempDir();
        prepareDatabase();
        createConfigurator();
    }
}
