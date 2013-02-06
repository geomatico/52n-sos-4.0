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

import java.util.concurrent.locks.ReentrantLock;
import org.hibernate.Session;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.service.ConfigurationException;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class HibernateSettingsManager extends SettingsManager {
    public static final SQLiteSettingFactory SQLITE_SETTING_FACTORY = new SQLiteSettingFactory();
    private IConnectionProvider connectionProvider;
    private final ReentrantLock lock = new ReentrantLock();

    public HibernateSettingsManager() throws ConfigurationException {
        super();
    }

    @Override
    public ISettingValueFactory getSettingFactory() {
        return SQLITE_SETTING_FACTORY;
    }

    private IConnectionProvider getConnectionProvider() {
        if (this.connectionProvider == null) {
            lock.lock();
            try {
                if (this.connectionProvider == null) {
                    this.connectionProvider = new HibernateSettingsSessionFactory() {
                    };
                }
            } finally {
                lock.unlock();
            }
        }
        return connectionProvider;
    }
    
    public void setConnectionProvider(IConnectionProvider connectionProvider) {
        lock.lock();
        try {
            this.connectionProvider = connectionProvider;
        } finally {
            lock.unlock();
        }
    }
    
    protected Session getSession() {
        return (Session) getConnectionProvider().getConnection();
    }
    
    protected void returnSession(Session session) {
        getConnectionProvider().returnConnection(session);
    }
}
