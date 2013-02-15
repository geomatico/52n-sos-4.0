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
package org.n52.sos.config.sqlite;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.config.AbstractSettingValueFactory;
import org.n52.sos.config.AbstractSettingsManager;
import org.n52.sos.config.IAdministratorUser;
import org.n52.sos.config.ISettingValue;
import org.n52.sos.config.ISettingValueFactory;
import org.n52.sos.config.sqlite.entities.AbstractSettingValue;
import org.n52.sos.config.sqlite.entities.AdministratorUser;
import org.n52.sos.config.sqlite.entities.BooleanSettingValue;
import org.n52.sos.config.sqlite.entities.FileSettingValue;
import org.n52.sos.config.sqlite.entities.IntegerSettingValue;
import org.n52.sos.config.sqlite.entities.NumericSettingValue;
import org.n52.sos.config.sqlite.entities.StringSettingValue;
import org.n52.sos.config.sqlite.entities.UriSettingValue;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.service.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteSettingsManager extends AbstractSettingsManager {

    private static final Logger log = LoggerFactory.getLogger(SQLiteSettingsManager.class);
    private static final Pattern SETTINGS_TYPE_CHANGED = Pattern.compile(
            ".*Abort due to constraint violation \\(column .* is not unique\\)");
    public static final ISettingValueFactory SQLITE_SETTING_FACTORY = new SqliteSettingFactory();
    private IConnectionProvider connectionProvider;
    private final ReentrantLock lock = new ReentrantLock();
    
    public SQLiteSettingsManager() throws ConfigurationException {
        super();
    }

     protected IConnectionProvider getConnectionProvider() {
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
    
     protected <T> T execute(HibernateAction<T> action) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = (Session) getConnectionProvider().getConnection();
            transaction = session.beginTransaction();
            T result = action.call(session);
            session.flush();
            transaction.commit();
            return result;
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } finally {
            getConnectionProvider().returnConnection(session);
        }
    }
    
    @Override
    public ISettingValueFactory getSettingFactory() {
        return SQLITE_SETTING_FACTORY;
    }

    @Override
    public ISettingValue<?> getSettingValue(final String key) throws HibernateException {
        return execute(new HibernateAction<ISettingValue<?>>() {
            @Override
            protected ISettingValue<?> call(Session session) {
                return (ISettingValue<?>) session.get(AbstractSettingValue.class, key);
            }
        });
    }

    @Override
    public void saveSettingValue(final ISettingValue<?> setting) throws HibernateException {
        log.debug("Saving Setting {}", setting);
        try {
            execute(new VoidHibernateAction() {
                @Override protected void run(Session session) {
                    session.saveOrUpdate(setting);
                }
            });
        } catch (HibernateException e) {
            if (isSettingsTypeChangeException(e)) {
                log.warn("Type of setting {} changed!", setting.getKey());
                execute(new VoidHibernateAction() {
                    @Override protected void run(Session session) {
                        AbstractSettingValue<?> hSetting = (AbstractSettingValue<?>) session.get(
                                AbstractSettingValue.class, setting.getKey());
                        if (hSetting != null) {
                            log.debug("Deleting Setting {}", hSetting);
                            session.delete(hSetting);
                        }
                        log.debug("Saving Setting {}", setting);
                        session.save(setting);
                    }
                });
            } else {
                throw e;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<ISettingValue<?>> getSettingValues() throws HibernateException {
        return execute(new HibernateAction<Set<ISettingValue<?>>>() {
            @Override protected Set<ISettingValue<?>> call(Session session) {
                return new HashSet<ISettingValue<?>>(session
                        .createCriteria(AbstractSettingValue.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list());
            }
        });
    }

    @Override
    public IAdministratorUser getAdminUser(final String username) throws HibernateException {
        return execute(new HibernateAction<IAdministratorUser>() {
            @Override protected IAdministratorUser call(Session session) {
                return (IAdministratorUser) session.createCriteria(AdministratorUser.class)
                        .add(Restrictions.eq(AdministratorUser.USERNAME_PROPERTY, username)).uniqueResult();
            }
        });
    }

    @Override
    protected void deleteSettingValue(final String setting) throws HibernateException {
        execute(new VoidHibernateAction() {
            @Override protected void run(Session session) {
                AbstractSettingValue<?> hSetting = (AbstractSettingValue<?>) session.get(
                        AbstractSettingValue.class, setting);
                if (hSetting != null) {
                    log.debug("Deleting Setting {}", hSetting);
                    session.delete(hSetting);
                }
            }
        });
    }

    protected boolean isSettingsTypeChangeException(HibernateException e) throws HibernateException {
        return e.getMessage() != null && SETTINGS_TYPE_CHANGED.matcher(e.getMessage()).matches();
    }

    @Override
    public IAdministratorUser createAdminUser(final String username, final String password) throws HibernateException {
        return execute(new HibernateAction<IAdministratorUser>() {
            @Override protected IAdministratorUser call(Session session) {
                IAdministratorUser user = new AdministratorUser().setUsername(username).setPassword(password);
                log.debug("Creating AdministratorUser {}", user);
                session.save(user);
                return user;
            }
        });
    }

    @Override
    public void saveAdminUser(final IAdministratorUser user) throws HibernateException {
        execute(new VoidHibernateAction() {
            @Override protected void run(Session session) {
                log.debug("Updating AdministratorUser {}", user);
                session.update(user);
            }
        });
    }

    @Override
    public void deleteAdminUser(final String username) throws HibernateException {
        execute(new VoidHibernateAction() {
            @Override protected void run(Session session) {
                IAdministratorUser au = (IAdministratorUser) session.createCriteria(AdministratorUser.class)
                        .add(Restrictions.eq(AdministratorUser.USERNAME_PROPERTY, username)).uniqueResult();
                if (au != null) {
                    session.delete(au);
                }
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void deleteAll() {
        execute(new VoidHibernateAction() {
            @Override protected void run(Session session) {
                List<IAdministratorUser> users = session.createCriteria(AdministratorUser.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
                for (IAdministratorUser u : users) {
                    session.delete(u);
                }
                List<AbstractSettingValue<?>> settings = session.createCriteria(AbstractSettingValue.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
                for (ISettingValue<?> v : settings) {
                    session.delete(v);
                }
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<IAdministratorUser> getAdminUsers() {
        return execute(new HibernateAction<Set<IAdministratorUser>>() {
            @Override
            protected Set<IAdministratorUser> call(Session session) {
                return new HashSet<IAdministratorUser>(
                        session.createCriteria(AdministratorUser.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .list());
            }
        });
    }

    @Override
    public void cleanup() {
        getConnectionProvider().cleanup();
    }
    
    private static class SqliteSettingFactory extends AbstractSettingValueFactory {
        @Override public BooleanSettingValue newBooleanSettingValue() { return new BooleanSettingValue(); }
        @Override public IntegerSettingValue newIntegerSettingValue() { return new IntegerSettingValue(); }
        @Override public StringSettingValue newStringSettingValue() { return new StringSettingValue(); }
        @Override public FileSettingValue newFileSettingValue() { return new FileSettingValue(); }
        @Override public UriSettingValue newUriSettingValue() { return new UriSettingValue(); }
        @Override protected ISettingValue<Double> newNumericSettingValue() { return new NumericSettingValue(); }
    }
    
    protected abstract class HibernateAction<T>  {
        protected abstract T call(Session session);
    }
    
    protected abstract class VoidHibernateAction extends HibernateAction<Void> {
        @Override protected Void call(Session session) {
            run(session); return null;
        }
        protected abstract void run(Session session);
    }
}