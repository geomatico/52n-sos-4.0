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

import java.io.Serializable;
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
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.config.SettingValue;
import org.n52.sos.config.SettingValueFactory;
import org.n52.sos.config.sqlite.entities.AbstractSettingValue;
import org.n52.sos.config.sqlite.entities.Activatable;
import org.n52.sos.config.sqlite.entities.AdminUser;
import org.n52.sos.config.sqlite.entities.BooleanSettingValue;
import org.n52.sos.config.sqlite.entities.FileSettingValue;
import org.n52.sos.config.sqlite.entities.IntegerSettingValue;
import org.n52.sos.config.sqlite.entities.NumericSettingValue;
import org.n52.sos.config.sqlite.entities.Operation;
import org.n52.sos.config.sqlite.entities.OperationKey;
import org.n52.sos.config.sqlite.entities.ProcedureEncoding;
import org.n52.sos.config.sqlite.entities.ObservationEncodingKey;
import org.n52.sos.config.sqlite.entities.ObservationEncoding;
import org.n52.sos.config.sqlite.entities.StringSettingValue;
import org.n52.sos.config.sqlite.entities.UriSettingValue;
import org.n52.sos.ds.ConnectionProvider;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.encode.ResponseFormatKeyType;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLiteSettingsManager extends AbstractSettingsManager {
    private static final Logger log = LoggerFactory.getLogger(SQLiteSettingsManager.class);
    private static final Pattern SETTINGS_TYPE_CHANGED = Pattern.compile(
            ".*Abort due to constraint violation \\(column .* is not unique\\)");
    public static final SettingValueFactory SQLITE_SETTING_FACTORY = new SqliteSettingFactory();
    private ConnectionProvider connectionProvider;
    private final ReentrantLock lock = new ReentrantLock();

    public SQLiteSettingsManager() throws ConfigurationException {
        super();
    }

    protected ConnectionProvider getConnectionProvider() {
        if (this.connectionProvider == null) {
            createDefaultConnectionProvider();
        }
        return connectionProvider;
    }

    public void setConnectionProvider(ConnectionProvider connectionProvider) {
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
    public boolean isActive(RequestOperatorKeyType requestOperatorKeyType) throws ConnectionProviderException {
        return isActive(Operation.class, new OperationKey(requestOperatorKeyType));
    }

    @Override
    protected void setOperationStatus(RequestOperatorKeyType key, final boolean active) throws
            ConnectionProviderException {
        setActive(Operation.class, new Operation(key), active);
    }

    @Override
    protected void setResponseFormatStatus(ResponseFormatKeyType rfkt, boolean active) throws
            ConnectionProviderException {
        setActive(ObservationEncoding.class, new ObservationEncoding(rfkt), active);
    }

    protected <K extends Serializable, T extends Activatable<K, T>> void setActive(Class<T> type, T activatable,
                                                                                   boolean active) throws
            ConnectionProviderException {
        execute(new SetActiveAction<K, T>(type, activatable, active));
    }

    protected <K extends Serializable, T extends Activatable<K, T>> boolean isActive(Class<T> c, K key) throws
            ConnectionProviderException {
        return execute(new IsActiveAction<K, T>(c, key)).booleanValue();
    }

    @Override
    protected void setProcedureDescriptionFormatStatus(String pdf, boolean active) throws ConnectionProviderException {
        setActive(ProcedureEncoding.class, new ProcedureEncoding(pdf), active);
    }

    @Override
    public boolean isActive(ResponseFormatKeyType rfkt) throws ConnectionProviderException {
        return isActive(ObservationEncoding.class, new ObservationEncodingKey(rfkt));
    }

    @Override
    public boolean isActive(String pdf) throws ConnectionProviderException {
        return isActive(ProcedureEncoding.class, pdf);
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

    private class SetActiveAction<K extends Serializable, T extends Activatable<K, T>> extends VoidHibernateAction {
        private final Activatable<K, T> activatable;
        private final Class<T> type;
        private final boolean active;

        SetActiveAction(Class<T> type, T activatable, boolean active) {
            this.activatable = activatable;
            this.type = type;
            this.active = active;
        }

        @Override
        protected void run(Session session) {
            @SuppressWarnings("unchecked")
            T o = (T) session.get(type, activatable.getKey());
            if (o != null) {
                if (active != o.isActive()) {
                    session.update(o.setActive(active));
                }
            } else {
                session.save(activatable.setActive(active));
            }
        }
    }

    private class IsActiveAction<K extends Serializable, T extends Activatable<K, T>> extends HibernateAction<Boolean> {
        private final K key;
        private Class<T> type;

        IsActiveAction(Class<T> type, K key) {
            this.type = type;
            this.key = key;
        }

        @Override
        protected Boolean call(Session session) {
            @SuppressWarnings("unchecked")
            T o = (T) session.get(type, key);
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