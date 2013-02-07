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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.config.entities.AbstractSettingValue;
import org.n52.sos.config.entities.AdministratorUser;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.service.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteSettingsManager extends AbstractSettingsManager {

    private static final Logger log = LoggerFactory.getLogger(SQLiteSettingsManager.class);
    private static final Pattern SETTINGS_TYPE_CHANGED = Pattern.compile(
            ".*Abort due to constraint violation \\(column .* is not unique\\)");
    public static final SQLiteSettingFactory SQLITE_SETTING_FACTORY = new SQLiteSettingFactory();
    private IConnectionProvider connectionProvider;
    private final ReentrantLock lock = new ReentrantLock();

    public SQLiteSettingsManager() throws ConfigurationException {
        super();
    }

    private IConnectionProvider getConnectionProvider() {
        if (this.connectionProvider == null) {
            createDefaultConnectionProvider();
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

    protected void createDefaultConnectionProvider() {
        lock.lock();
        try {
            if (this.connectionProvider == null) {
                this.connectionProvider = new SQLiteSessionFactory();
            }
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

    @Override
    public ISettingValueFactory getSettingFactory() {
        return SQLITE_SETTING_FACTORY;
    }

    @Override
    public ISettingValue<?> getSettingValue(final String key) throws HibernateException {
        Session session = null;
        try {
            return getSettingValue(key, session = getSession());
        } finally {
            returnSession(session);
        }
    }

    @Override
    public void saveSettingValue(final ISettingValue<?> setting) throws HibernateException {
        Session session = null;
        try {
            saveSettingValue(setting, session = getSession());
        } finally {
            returnSession(session);
        }
    }

    @Override
    public Set<ISettingValue<?>> getSettingValues() throws HibernateException {
        Session session = null;
        try {
            return getSettingValues(session = getSession());

        } finally {
            returnSession(session);
        }
    }

    @Override
    public IAdministratorUser getAdminUser(String username) throws HibernateException {
        Session session = null;
        try {
            return getAdminUser(username, session = getSession());
        } finally {
            returnSession(session);
        }
    }

    protected void deleteAndSave(final ISettingValue<?> setting) throws HibernateException {
        Session session = null;
        try {
            deleteAndSave(setting, session = getSession());
        } finally {
            returnSession(session);
        }
    }

    protected ISettingValue<?> getSettingValue(String key, Session session) throws HibernateException {
        log.debug("Getting Setting {}", key);
        return (ISettingValue<?>) session.get(AbstractSettingValue.class, key);
    }

    protected Set<ISettingValue<?>> getSettingValues(Session session) throws HibernateException {
        log.debug("Getting Settings");
        List<AbstractSettingValue<?>> list = (List<AbstractSettingValue<?>>) session
                .createCriteria(AbstractSettingValue.class).list();
        return new HashSet<ISettingValue<?>>(list);
    }

    protected void saveSettingValue(final ISettingValue<?> setting, Session session) throws HibernateException {
        log.debug("Saving Setting {}", setting);
        Transaction transaction = null;
        boolean typeChange = false;
        try {
            transaction = session.beginTransaction();
            session.saveOrUpdate(setting);
            session.flush();
            transaction.commit();
        } catch (HibernateException he1) {
            if (transaction != null) {
                transaction.rollback();
            }
            typeChange = isSettingsTypeChangeException(he1);
            if (!typeChange) {
                throw he1;
            }
        }
        if (typeChange) {
            log.warn("Type of setting {} changed!", setting.getKey());
            /* we have to get a new session!! */
            deleteAndSave(setting);
        }
    }

    protected void deleteAndSave(final ISettingValue<?> setting, Session session) throws HibernateException {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            deleteSettingValue(setting.getKey(), session);
            log.debug("Saving Setting {}", setting);
            session.save(setting);
            session.flush();
            transaction.commit();
        } catch (HibernateException he2) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he2;
        }
    }

    @Override
    protected void deleteSettingValue(String setting) throws HibernateException {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            deleteSettingValue(setting, session);
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            returnSession(session);
        }
    }

    protected void deleteSettingValue(String setting, Session session) throws HibernateException {
        log.debug("Deleting Setting {}", setting);
        AbstractSettingValue<?> hSetting = (AbstractSettingValue<?>) session.get(AbstractSettingValue.class, setting);
        if (hSetting != null) {
            log.debug("Deleting Setting {}", hSetting);
            session.delete(hSetting);
            session.flush();
        }
    }

    protected boolean isSettingsTypeChangeException(HibernateException e) throws HibernateException {
        return e.getMessage() != null && SETTINGS_TYPE_CHANGED.matcher(e.getMessage()).matches();
    }

    protected IAdministratorUser getAdminUser(String username, Session session) throws HibernateException {
        log.debug("Getting AdministratorUsers");
        return (IAdministratorUser) session.createCriteria(AdministratorUser.class)
                .add(Restrictions.eq(AdministratorUser.USERNAME_PROPERTY, username)).uniqueResult();
    }

    @Override
    public IAdministratorUser createAdminUser(String username, String password) throws HibernateException {
        Session session = null;
        try {
            session = getSession();
            return createAdminUser(username, password, session);
        } finally {
            returnSession(session);
        }
    }

    protected IAdministratorUser createAdminUser(String username, String password, Session session) throws
            HibernateException {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            IAdministratorUser user = new AdministratorUser().setUsername(username).setPassword(password);
            log.debug("Creating AdministratorUser {}", user);
            session.save(user);
            session.flush();
            transaction.commit();
            return user;
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }

    @Override
    public void saveAdminUser(IAdministratorUser user) throws HibernateException {
        Session session = null;
        try {
            saveAdminUser(user, session = getSession());
        } finally {
            returnSession(session);
        }
    }

    public void saveAdminUser(IAdministratorUser user, Session session) throws HibernateException {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            log.debug("Updating AdministratorUser {}", user);
            session.update(user);
            transaction.commit();
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
    }
    
    @Override
    public void deleteAdminUser(String username) throws HibernateException {
        Session session = null;
        try {
            deleteAdminUser(username, session = getSession());
        } finally {
            returnSession(session);
        }
    }
    
    protected void deleteAdminUser(String username, Session session) throws HibernateException {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.delete(getAdminUser(username, session));
            transaction.commit();
        } catch(HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        }
        
    } 
}
