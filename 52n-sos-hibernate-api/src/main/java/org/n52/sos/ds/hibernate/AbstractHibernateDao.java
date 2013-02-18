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

import org.hibernate.Session;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHibernateDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHibernateDao.class);
    
    private final IConnectionProvider connectionProvider;

    public AbstractHibernateDao() {
        this.connectionProvider = Configurator.getInstance().getDataConnectionProvider();
    }

    protected Session getSession() throws OwsExceptionReport {
        try {
            return getSession(connectionProvider.getConnection());
        } catch (ConnectionProviderException cpe) {
           String exceptionText = "Error while getting new Session!";
           throw Util4Exceptions.createNoApplicableCodeException(cpe, exceptionText);
        }
    }

    protected void returnSession(Session session) {
        this.connectionProvider.returnConnection(session);
    }

    protected Session getSession(Object connection) throws OwsExceptionReport {
        if (!(connection instanceof Session)) {
            String exceptionText = "The parameter connection is not an Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        return (Session) connection;
    }
}
