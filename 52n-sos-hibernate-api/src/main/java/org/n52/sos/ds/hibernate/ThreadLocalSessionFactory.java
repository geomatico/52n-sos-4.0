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

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hibernate.Session;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ThreadLocalSessionFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadLocalSessionFactory.class);
    private final IConnectionProvider connectionProvider;
    private final Lock lock = new ReentrantLock();
    private final Set<Session> createdSessions = CollectionHelper.synchronizedSet();
    private boolean closed = false;
    
    private ThreadLocal<Session> threadLocal = new ThreadLocal<Session>() {
        @Override
        protected Session initialValue() {
            try {
                return (Session) getConnectionProvider().getConnection();
            } catch (ConnectionProviderException cpe) {
                LOGGER.error("Error while getting initialValue for ThreadLocalSessionFactory!", cpe);
            }
            return null;
        }
    };

    public ThreadLocalSessionFactory(IConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Session getSession() {
        lock.lock();
        try {
            if (isClosed()) {
                throw new IllegalStateException("factory already closed");
            }
            Session s = this.threadLocal.get();
            getCreatedSessions().add(s);
            return s;
        } finally {
            lock.unlock();
        }
    }

    public void close() throws ConnectionProviderException {
        setClosed();
        returnSessions();
    }

    public IConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    protected Set<Session> getCreatedSessions() {
        return createdSessions;
    }

    protected void setClosed() {
        lock.lock();
        try {
            closed = true;
        } finally {
            lock.unlock();
        }
    }
    
    protected boolean isClosed() {
        lock.lock();
        try {
            return closed;
        } finally {
            lock.unlock();
        }
    }

    protected void returnSessions() throws ConnectionProviderException {
        for (Session s : getCreatedSessions()) {
            getConnectionProvider().returnConnection(s);
        }
    }
}
