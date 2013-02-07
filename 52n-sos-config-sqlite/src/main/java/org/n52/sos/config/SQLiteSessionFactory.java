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
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.userType.FileType;
import org.hibernate.userType.UriType;
import org.n52.sos.config.entities.AdministratorUser;
import org.n52.sos.config.entities.BooleanSettingValue;
import org.n52.sos.config.entities.FileSettingValue;
import org.n52.sos.config.entities.IntegerSettingValue;
import org.n52.sos.config.entities.NumericSettingValue;
import org.n52.sos.config.entities.StringSettingValue;
import org.n52.sos.config.entities.UriSettingValue;
import org.n52.sos.ds.IConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SQLiteSessionFactory implements IConnectionProvider {

    private static final Logger log = LoggerFactory.getLogger(SQLiteSessionFactory.class);
    
    public static final String HIBERNATE_DIALECT = "hibernate.dialect";
    public static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";
    public static final String HIBERNATE_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
    public static final String HIBERNATE_UPDATE_SCHEMA = "hibernate.hbm2ddl.auto";
    public static final String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";
    public static final String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";
    
    private static final String CONNECTION_URL_DEFAULT = "jdbc:sqlite:configuration.db";
    private static final String SQLITE_HIBERNATE_DIALECT = "org.hibernate.dialect.SQLiteDialect";
    private static final String UPDATE_SCHEMA_VALUE = "update";
    private static final String SQLITE_JDBC_DRIVER = "org.sqlite.JDBC";
    public static final String EMPTY = "";
    
    private static final Properties DEFAULT_PROPERTIES = new Properties() {
        private static final long serialVersionUID = 1L;
        {
            put(HIBERNATE_CONNECTION_URL, CONNECTION_URL_DEFAULT);
            put(HIBERNATE_UPDATE_SCHEMA, UPDATE_SCHEMA_VALUE);
            put(HIBERNATE_DIALECT, SQLITE_HIBERNATE_DIALECT);
            put(HIBERNATE_CONNECTION_DRIVER_CLASS, SQLITE_JDBC_DRIVER);
            put(HIBERNATE_CONNECTION_USERNAME, EMPTY);
            put(HIBERNATE_CONNECTION_PASSWORD, EMPTY);
        }
    };

    private final ReentrantLock lock = new ReentrantLock();
    private SessionFactory sessionFactory;

    protected SessionFactory getSessionFactory() {
        if (this.sessionFactory == null) {
            lock.lock();
            try {
                if (this.sessionFactory == null) {
                    this.sessionFactory = createSessionFactory(null);
                }
            } finally {
                lock.unlock();
            }

        }
        return this.sessionFactory;
    }

    private SessionFactory createSessionFactory(Properties properties) {
        Configuration cfg = new Configuration()
                .addAnnotatedClass(BooleanSettingValue.class)
                .addAnnotatedClass(FileSettingValue.class)
                .addAnnotatedClass(IntegerSettingValue.class)
                .addAnnotatedClass(NumericSettingValue.class)
                .addAnnotatedClass(StringSettingValue.class)
                .addAnnotatedClass(UriSettingValue.class)
                .addAnnotatedClass(AdministratorUser.class);
        
        cfg.registerTypeOverride(new FileType(), new String[]{"file", File.class.getName()});
        cfg.registerTypeOverride(new UriType(), new String[]{"uri", URI.class.getName()});
        
        if (properties != null) {
            cfg.mergeProperties(properties);
        }
        cfg.mergeProperties(DEFAULT_PROPERTIES);
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
                .applySettings(cfg.getProperties()).buildServiceRegistry();
        return cfg.buildSessionFactory(serviceRegistry);
    }

    @Override
    public Session getConnection() {
        return getSessionFactory().openSession();
    }

    @Override
    public void returnConnection(Object connection) {
        if (connection != null && connection instanceof Session) {
            Session session = (Session) connection;
            if (session.isOpen()) {
                session.close();
            } else {
                log.warn("Returned session already closed");
            }
        }
    }

    @Override
    public void cleanup() {
        if (getSessionFactory() != null) {
            try {
                getSessionFactory().close();
                log.info("Connection provider closed successfully!");
            } catch (HibernateException he) {
                log.error("Error while closing connection provider!", he);
            }
        }
    }

    @Override
    public void initialize(Properties properties) {
        lock.lock();
        try {
            this.sessionFactory = createSessionFactory(properties);
        } finally {
            lock.unlock();
        }
    }
}
