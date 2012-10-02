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
package org.n52.sos.ds.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Implementation of the SessionFactory.
 * 
 */
public class SessionFactoryProvider implements IConnectionProvider {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactoryProvider.class);

    /**
     * SessionFactory instance
     */
    private SessionFactory sessionFactory = null;

    /**
     * constructor. Opens a new Hibernate SessionFactory
     * 
     * @throws OwsExceptionReport
     *             If an error occurs during instantiation.
     */
    public SessionFactoryProvider() throws OwsExceptionReport {
        try {
//            Configuration configuration = new Configuration().addResource("/sos-hibernate.cfg.xml").configure();
            Configuration configuration = new Configuration().configure("/sos-hibernate.cfg.xml");
            // Configuration configuration = new Configuration();
            // configuration.configure(new
            // File(Configurator.getInstance().getBasePath() +
            // "WEB-INF\\conf\\hibernate\\hibernate.cfg.xml"));
            ServiceRegistry serviceRegistry =
                    new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
            this.sessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (HibernateException he) {
            String exceptionText = "An error occurs during instantiation of the database connection pool!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.IConnectionProvider#getConnection()
     */
    @Override
    public Session getConnection() {
        return sessionFactory.openSession();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.IConnectionProvider#returnConnection(java.lang.Object)
     */
    @Override
    public void returnConnection(Object connection) {
        if (connection != null) {
            if (connection instanceof Session) {
                Session session = (Session) connection;
                if (session.isOpen()) {
                    session.clear();
                    session.close();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.IConnectionProvider#cleanup()
     */
    @Override
    public void cleanup() {
        try {
            if (sessionFactory instanceof SessionFactoryImpl) {
                SessionFactoryImpl sf = (SessionFactoryImpl) sessionFactory;
                ConnectionProvider conn = sf.getConnectionProvider();
                if (conn instanceof C3P0ConnectionProvider) {
                    ((C3P0ConnectionProvider) conn).close();
                }
            }
            this.sessionFactory.close();
            LOGGER.info("Connection provider closed successfully!");
        } catch (HibernateException he) {
            LOGGER.error("Error while closing connection provider!", he);
        }
    }

}
