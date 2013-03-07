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
import org.n52.sos.config.AdministratorUser;
import org.n52.sos.config.SettingValue;
import org.n52.sos.config.SettingValueFactory;
import org.n52.sos.config.sqlite.entities.AbstractSettingValue;
import org.n52.sos.config.sqlite.entities.AdminUser;
import org.n52.sos.config.sqlite.entities.BooleanSettingValue;
import org.n52.sos.config.sqlite.entities.FileSettingValue;
import org.n52.sos.config.sqlite.entities.IntegerSettingValue;
import org.n52.sos.config.sqlite.entities.NumericSettingValue;
import org.n52.sos.config.sqlite.entities.Operation;
import org.n52.sos.config.sqlite.entities.OperationKey;
import org.n52.sos.config.sqlite.entities.StringSettingValue;
import org.n52.sos.config.sqlite.entities.UriSettingValue;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.config.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteSettingsManager extends AbstractSettingsManager {
    private static final Logger log = LoggerFactory.getLogger(SQLiteSettingsManager.class);
    private static final Pattern SETTINGS_TYPE_CHANGED = Pattern.compile(
            ".*Abort due to constraint violation \\(column .* is not unique\\)");
    public static final SettingValueFactory SQLITE_SETTING_FACTORY = new SqliteSettingFactory();
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

    protected <T> T execute(HibernateAction<T> action) throws ConnectionProviderException {
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
        } catch (ConnectionProviderException cpe) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw cpe;
        } finally {
            getConnectionProvider().returnConnection(session);
        }
    }

    @Override
    public SettingValueFactory getSettingFactory() {
        return SQLITE_SETTING_FACTORY;
    }

    @Override
    public SettingValue<?> getSettingValue(final String key) throws HibernateException, ConnectionProviderException {
        return execute(new GetSettingValueAction(key));
    }

    @Override
    public void saveSettingValue(final SettingValue<?> setting) throws HibernateException, ConnectionProviderException {
        log.debug("Saving Setting {}", setting);
        try {
            execute(new SaveSettingValueAction(setting));
        } catch (HibernateException e) {
            if (isSettingsTypeChangeException(e)) {
                log.warn("Type of setting {} changed!", setting.getKey());
                execute(new DeleteAndSaveValueAction(setting));
            } else {
                throw e;
            }
        }
    }

    @Override
    public Set<SettingValue<?>> getSettingValues() throws HibernateException, ConnectionProviderException {
        return execute(new GetSettingValuesAction());
    }

    @Override
    public AdministratorUser getAdminUser(String username) throws HibernateException, ConnectionProviderException {
        return execute(new GetAdminUserAction(username));
    }

    @Override
    protected void deleteSettingValue(String setting) throws HibernateException, ConnectionProviderException {
        execute(new DeleteSettingValueAction(setting));
    }

    protected boolean isSettingsTypeChangeException(HibernateException e) throws HibernateException {
        return e.getMessage() != null && SETTINGS_TYPE_CHANGED.matcher(e.getMessage()).matches();
    }

    @Override
    public AdministratorUser createAdminUser(String username, String password) throws HibernateException,                                                                                       ConnectionProviderException {
        return execute(new CreateAdminUserAction(username, password));
    }

    @Override
    public void saveAdminUser(AdministratorUser user) throws HibernateException, ConnectionProviderException {
        execute(new SaveAdminUserAction(user));
    }

    @Override
    public void deleteAdminUser(String username) throws HibernateException, ConnectionProviderException {
        execute(new DeleteAdminUserAction(username));
    }

    @Override
    public void deleteAll() throws ConnectionProviderException {
        execute(new DeleteAllAction());
    }

    @Override
    public Set<AdministratorUser> getAdminUsers() throws ConnectionProviderException {
        return execute(new GetAdminUsersAction());
    }

    @Override
    public void cleanup() {
        getConnectionProvider().cleanup();
    }

    @Override
    public boolean isActive(final RequestOperatorKeyType requestOperatorKeyType) throws ConnectionProviderException {
        return execute(new IsActiveAction(requestOperatorKeyType)).booleanValue();
    }

    @Override
    public void setActive(final RequestOperatorKeyType requestOperatorKeyType, final boolean active) throws
            ConnectionProviderException {
        execute(new SetActiveAction(requestOperatorKeyType, active));
    }

    private static class SqliteSettingFactory extends AbstractSettingValueFactory {
        @Override
        public BooleanSettingValue newBooleanSettingValue() {
            return new BooleanSettingValue();
        }

        @Override
        public IntegerSettingValue newIntegerSettingValue() {
            return new IntegerSettingValue();
        }

        @Override
        public StringSettingValue newStringSettingValue() {
            return new StringSettingValue();
        }

        @Override
        public FileSettingValue newFileSettingValue() {
            return new FileSettingValue();
        }

        @Override
        public UriSettingValue newUriSettingValue() {
            return new UriSettingValue();
        }

        @Override
        protected SettingValue<Double> newNumericSettingValue() {
            return new NumericSettingValue();
        }
    }

    protected abstract class HibernateAction<T> {
        protected abstract T call(Session session);
    }

    protected abstract class VoidHibernateAction extends HibernateAction<Void> {
        @Override
        protected Void call(Session session) {
            run(session);
            return null;
        }

        protected abstract void run(Session session);
    }

    private class SetActiveAction extends VoidHibernateAction {
        private final RequestOperatorKeyType requestOperatorKeyType;
        private final boolean active;

        SetActiveAction(RequestOperatorKeyType requestOperatorKeyType, boolean active) {
            this.requestOperatorKeyType = requestOperatorKeyType;
            this.active = active;
        }

        @Override
        protected void run(Session session) {
            Operation o = (Operation) session.get(Operation.class, new OperationKey(requestOperatorKeyType));
            if (o != null) {
                if (active != o.isActive()) {
                    session.update(o.setActive(active));
                }
            } else {
                session.save(new Operation(requestOperatorKeyType).setActive(active));
            }
        }
    }

    private class IsActiveAction extends HibernateAction<Boolean> {
        private final RequestOperatorKeyType requestOperatorKeyType;

        IsActiveAction(RequestOperatorKeyType requestOperatorKeyType) {
            this.requestOperatorKeyType = requestOperatorKeyType;
        }

        @Override
        protected Boolean call(Session session) {
            Operation o = (Operation) session.get(Operation.class, new OperationKey(requestOperatorKeyType));
            return (o == null) ? true : o.isActive();
        }
    }

    private class GetAdminUsersAction extends HibernateAction<Set<AdministratorUser>> {
        @Override
        @SuppressWarnings("unchecked")
        protected Set<AdministratorUser> call(Session session) {
            return new HashSet<AdministratorUser>(
                    session.createCriteria(AdministratorUser.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                    .list());
        }
    }

    private class DeleteAllAction extends VoidHibernateAction {
        @Override
        @SuppressWarnings("unchecked")
        protected void run(Session session) {
            List<AdministratorUser> users = session.createCriteria(AdministratorUser.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
            for (AdministratorUser u : users) {
                session.delete(u);
            }
            List<AbstractSettingValue<?>> settings = session.createCriteria(AbstractSettingValue.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
            for (SettingValue<?> v : settings) {
                session.delete(v);
            }
        }
    }

    private class DeleteAdminUserAction extends VoidHibernateAction {
        private final String username;

        DeleteAdminUserAction(String username) {
            this.username = username;
        }

        @Override
        protected void run(Session session) {
            AdministratorUser au = (AdministratorUser) session.createCriteria(AdministratorUser.class)
                    .add(Restrictions.eq(AdminUser.USERNAME_PROPERTY, username)).uniqueResult();
            if (au != null) {
                session.delete(au);
            }
        }
    }

    private class SaveAdminUserAction extends VoidHibernateAction {
        private final AdministratorUser user;

        SaveAdminUserAction(AdministratorUser user) {
            this.user = user;
        }

        @Override
        protected void run(Session session) {
            log.debug("Updating AdministratorUser {}", user);
            session.update(user);
        }
    }

    private class CreateAdminUserAction extends HibernateAction<AdminUser> {
        private final String username;
        private final String password;

        CreateAdminUserAction(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected AdminUser call(Session session) {
            AdminUser user = new AdminUser().setUsername(username).setPassword(password);
            log.debug("Creating AdministratorUser {}", user);
            session.save(user);
            return user;
        }
    }

    private class DeleteSettingValueAction extends VoidHibernateAction {
        private final String setting;

        DeleteSettingValueAction(String setting) {
            this.setting = setting;
        }

        @Override
        protected void run(Session session) {
            AbstractSettingValue<?> hSetting = (AbstractSettingValue<?>) session.get(
                    AbstractSettingValue.class, setting);
            if (hSetting != null) {
                log.debug("Deleting Setting {}", hSetting);
                session.delete(hSetting);
            }
        }
    }

    private class GetAdminUserAction extends HibernateAction<AdminUser> {
        private final String username;

        GetAdminUserAction(String username) {
            this.username = username;
        }

        @Override
        protected AdminUser call(Session session) {
            return (AdminUser) session.createCriteria(AdminUser.class)
                    .add(Restrictions.eq(AdminUser.USERNAME_PROPERTY, username)).uniqueResult();
        }
    }

    private class GetSettingValueAction extends HibernateAction<SettingValue<?>> {
        private final String key;

        GetSettingValueAction(String key) {
            this.key = key;
        }

        @Override
        protected SettingValue<?> call(Session session) {
            return (SettingValue<?>) session.get(AbstractSettingValue.class, key);
        }
    }

    private class SaveSettingValueAction extends VoidHibernateAction {
        private final SettingValue<?> setting;

        SaveSettingValueAction(SettingValue<?> setting) {
            this.setting = setting;
        }

        @Override
        protected void run(Session session) {
            session.saveOrUpdate(setting);
        }
    }

    private class DeleteAndSaveValueAction extends VoidHibernateAction {
        private final SettingValue<?> setting;

        DeleteAndSaveValueAction(SettingValue<?> setting) {
            this.setting = setting;
        }

        @Override
        protected void run(Session session) {
            AbstractSettingValue<?> hSetting = (AbstractSettingValue<?>) session.get(AbstractSettingValue.class, setting
                    .getKey());
            if (hSetting != null) {
                log.debug("Deleting Setting {}", hSetting);
                session.delete(hSetting);
            }
            log.debug("Saving Setting {}", setting);
            session.save(setting);
        }
    }

    private class GetSettingValuesAction extends HibernateAction<Set<SettingValue<?>>> {
        @Override
        @SuppressWarnings("unchecked")
        protected Set<SettingValue<?>> call(Session session) {
            return new HashSet<SettingValue<?>>(session
                    .createCriteria(AbstractSettingValue.class)
                    .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list());
        }
    }
}