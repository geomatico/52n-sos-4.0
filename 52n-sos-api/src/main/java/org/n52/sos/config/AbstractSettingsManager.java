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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.SettingsChangeEvent;
import org.n52.sos.service.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@code SettingsManaeger} implementation that handles the loading of {@link ISettingDefinition}s and the
 * configuration of objects.
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public abstract class AbstractSettingsManager extends SettingsManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractSettingsManager.class);
    private final SettingDefinitionProviderRepository settingDefinitionRepository;
    private final Map<String, Set<ConfigurableObject>> configurableObjects = new HashMap<String, Set<ConfigurableObject>>();
    private final ReadWriteLock configurableObjectsLock = new ReentrantReadWriteLock();

    /**
     * Constructs a new instance.
     * <p/>
     * @throws ConfigurationException if loading of {@link ISettingDefinitionProvider} fails
     */
    protected AbstractSettingsManager() throws ConfigurationException {
        settingDefinitionRepository = new SettingDefinitionProviderRepository();
    }

    /**
     * @return the repository holding the setting definitions
     */
    protected SettingDefinitionProviderRepository getSettingDefinitionRepository() {
        return settingDefinitionRepository;
    }

    @Override
    public Set<ISettingDefinition<?, ?>> getSettingDefinitions() {
        return getSettingDefinitionRepository().getSettingDefinitions();
    }

    /**
     * @return the keys for all definiions
     */
    public Set<String> getKeys() {
        Set<ISettingDefinition<?, ?>> settings = getSettingDefinitions();
        HashSet<String> keys = new HashSet<String>(settings.size());
        for (ISettingDefinition<?, ?> setting : settings) {
            keys.add(setting.getKey());
        }
        return keys;
    }

    @Override
    public void changeSetting(ISettingValue<?> newValue) throws ConfigurationException, ConnectionProviderException {
        if (newValue == null) {
            throw new NullPointerException("newValue can not be null");
        }
        if (newValue.getKey() == null) {
            throw new NullPointerException("key can not be null");
        }
        ISettingDefinition<?, ?> def = getDefinitionByKey(newValue.getKey());

        if (def.getType() != newValue.getType()) {
            throw new IllegalArgumentException(String.format("Invalid type for definition (%s vs. %s)", def.getType(),
                                                             newValue.getType()));
        }

        ISettingValue<?> oldValue = getSettingValue(newValue.getKey());
        if (oldValue == null || !oldValue.equals(newValue)) {
            applySetting(def, oldValue, newValue);
            saveSettingValue(newValue);
        }

        SosEventBus.fire(new SettingsChangeEvent(def, oldValue, newValue));
    }

    @Override
    public void deleteSetting(ISettingDefinition<?, ?> setting) throws ConfigurationException, ConnectionProviderException {
        ISettingValue<?> oldValue = getSettingValue(setting.getKey());
        if (oldValue != null) {
            applySetting(setting, oldValue, null);
            deleteSettingValue(setting.getKey());
        }

        SosEventBus.fire(new SettingsChangeEvent(setting, oldValue, null));
    }

    /**
     * Applies the a new setting to all {@code ConfiguredObject}s. If an error occurs the the old value is reapplied.
     * <p/>
     * @param setting the definition
     * @param oldValue the old value (or {@code null} if there is none)
     * @param newValue the new value (or {@code null} if there is none)
     * <p/>
     * @throws ConfigurationException if there is a error configuring the objects
     */
    private void applySetting(ISettingDefinition<?, ?> setting,
                              ISettingValue<?> oldValue,
                              ISettingValue<?> newValue) throws ConfigurationException {
        LinkedList<ConfigurableObject> changed = new LinkedList<ConfigurableObject>();
        ConfigurationException e = null;
        configurableObjectsLock.readLock().lock();
        try {
            Set<ConfigurableObject> cos = configurableObjects.get(setting.getKey());
            if (cos != null) {
                for (ConfigurableObject co : cos) {
                    try {
                        co.configure(newValue.getValue());
                    } catch (ConfigurationException ce) {
                        e = ce;
                        break;
                    } finally {
                        changed.add(co);
                    }
                    co.configure(newValue.getValue());
                }
                if (e != null) {
                    log.debug("Reverting setting...");
                    for (ConfigurableObject co : changed) {
                        try {
                            co.configure(oldValue.getValue());
                        } catch (ConfigurationException ce) {
                            /* there is nothing we can do... */
                            log.error("Error reverting setting!", ce);
                        }
                    }
                    throw e;
                }
            }
        } finally {
            configurableObjectsLock.readLock().unlock();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ISettingValue<T> getSetting(ISettingDefinition<?, T> key) throws ConnectionProviderException {
        return (ISettingValue<T>) getSettingValue(key.getKey());
    }

    @Override
    public Map<ISettingDefinition<?, ?>, ISettingValue<?>> getSettings() throws ConnectionProviderException {
        Set<ISettingValue<?>> values = getSettingValues();
        Map<ISettingDefinition<?, ?>, ISettingValue<?>> settingsByDefinition = new HashMap<ISettingDefinition<?, ?>, ISettingValue<?>>(values
                .size());
        for (ISettingValue<?> value : values) {
            final ISettingDefinition<?, ?> definition = getSettingDefinitionRepository().getDefinition(value.getKey());
            if (definition == null) {
                log.warn("No definition for '{}' found.", value.getKey());
            } else {
                settingsByDefinition.put(definition, value);
            }
        }
        HashSet<ISettingDefinition<?, ?>> nullValues = new HashSet<ISettingDefinition<?, ?>>(getSettingDefinitions());
        nullValues.removeAll(settingsByDefinition.keySet());
        for (ISettingDefinition<?, ?> s : nullValues) {
            settingsByDefinition.put(s, null);
        }
        return settingsByDefinition;
    }

    @Override
    public void deleteAdminUser(IAdministratorUser user) throws ConnectionProviderException {
        deleteAdminUser(user.getUsername());
    }

    @Override
    public void configure(Object object) throws ConfigurationException {
        Class<?> clazz = object.getClass();
        Configurable configurable = clazz.getAnnotation(Configurable.class);
        if (configurable == null) {
            return;
        }

        for (Method method : clazz.getMethods()) {
            Setting s = method.getAnnotation(Setting.class);

            if (s != null) {
                String key = s.value();
                if (key == null || key.isEmpty()) {
                    throw new ConfigurationException(String.format("Invalid value for @Setting: '%s'", key));
                }
                if (getSettingDefinitionRepository().getDefinition(key) == null) {
                    throw new ConfigurationException(String.format("No SettingDefinition found for key %s", key));
                }
                if (method.getParameterTypes().length != 1) {
                    throw new ConfigurationException(String.format(
                            "Method %s annotated with @Setting in %s has a invalid method signature", method, clazz));
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new ConfigurationException(String.format("Non-public method %s annotated with @Setting in %s",
                                                                   method, clazz));
                }
                configure(new ConfigurableObject(method, object, key));
            }
        }
    }

    private void configure(ConfigurableObject co) throws ConfigurationException {
        configurableObjectsLock.writeLock().lock();
        try {
            Set<ConfigurableObject> cos = configurableObjects.get(co.getKey());
            if (cos == null) {
                configurableObjects.put(co.getKey(), cos = new HashSet<ConfigurableObject>());
            }
            cos.add(co);
        } finally {
            configurableObjectsLock.writeLock().unlock();
        }
        try {
            co.configure(getNotNullSettingValue(co));
        } catch (ConnectionProviderException cpe) {
            throw new ConfigurationException(cpe);
        }
    }

    @Override
    public ISettingDefinition<?, ?> getDefinitionByKey(String key) {
        return getSettingDefinitionRepository().getDefinition(key);
    }

    @SuppressWarnings("unchecked")
    private ISettingValue<Object> getNotNullSettingValue(ConfigurableObject co) throws ConnectionProviderException, ConfigurationException {
        ISettingValue<Object> val = (ISettingValue<Object>) getSettingValue(co.getKey());
        if (val == null) {
            ISettingDefinition<?,?> def = getDefinitionByKey(co.getKey());
            if (def == null) {
                throw new ConfigurationException(String.format("No SettingDefinition found for key %s", co.getKey()));
            }
            val =(ISettingValue<Object>) getSettingFactory().newSettingValue(def, null);
            if (def.isOptional()) {
                log.debug("No value found for optional setting {}", co.getKey());
                saveSettingValue(val);
            } else if (def.hasDefaultValue()) {
                log.debug("Using default value '{}' for required setting {}", def.getDefaultValue(), co.getKey());
                saveSettingValue(val.setValue(def.getDefaultValue()));
            } else {
                throw new ConfigurationException(String.format("No value found for required Setting '%s' with no default value.", co.getKey()));
            }
        }
        return val;
    }

    /**
     * @return all saved setting values
     *
     * @throws ConnectionProviderException
     * @throws HibernateException
     */
    protected abstract Set<ISettingValue<?>> getSettingValues() throws ConnectionProviderException;

    /**
     * Returns the value of the specified setting or {@code null} if it does not exist.
     * <p/>
     * @param key the key
     * <p/>
     * @return the value
     *
     * @throws ConnectionProviderException
     */
    protected abstract ISettingValue<?> getSettingValue(String key) throws ConnectionProviderException;

    /**
     * Deletes the setting with the specified key.
     * <p/>
     * @param key the key
     *
     * @throws ConnectionProviderException
     */
    protected abstract void deleteSettingValue(String key) throws ConnectionProviderException;

    /**
     * Saves the setting value.
     * <p/>
     * @param setting the value
     *
     * @throws ConnectionProviderException
     */
    protected abstract void saveSettingValue(ISettingValue<?> setting) throws ConnectionProviderException;

    /*
     * TODO handle the references as WeakReferences
     */
    private class ConfigurableObject {

        private final Method method;
        private final WeakReference<Object> target;
        private final String key;

        /**
         * Constructs a new {@code ConfigurableObject}
         * <p/>
         * @param method the method of the target
         * @param target the target object
         * @param key the settings key
         */
        ConfigurableObject(Method method, Object target, String key) {
            this.method = method;
            this.target = new WeakReference<Object>(target);
            this.key = key;
        }

        /**
         * @return the method
         */
        public Method getMethod() {
            return method;
        }

        /**
         * @return the target object
         */
        public WeakReference<Object> getTarget() {
            return target;
        }

        /**
         * @return the settings key
         */
        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + (getMethod() != null ? getMethod().hashCode() : 0);
            hash = 41 * hash + (getTarget() != null ? getTarget().hashCode() : 0);
            hash = 41 * hash + (getKey() != null ? getKey().hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ConfigurableObject other = (ConfigurableObject) obj;
            if (getMethod() != other.getMethod() && (getMethod() == null || !getMethod().equals(other.getMethod()))) {
                return false;
            }
            if (getTarget() != other.getTarget() && (getTarget() == null || !getTarget().equals(other.getTarget()))) {
                return false;
            }
            if ((getKey() == null) ? (other.getKey() != null) : !getKey().equals(other.getKey())) {
                return false;
            }
            return true;
        }

        /**
         * Configures this object with the specified value.
         * <p/>
         * @param val the value
         * <p/>
         * @throws ConfigurationException if an error occurs
         */
        public void configure(ISettingValue<?> val) throws ConfigurationException {
            configure(val.getValue());
        }

        /**
         * Configures this object with the specified value. Exceptions are wrapped in a {@code ConfigurationException}.
         * <p/>
         * @param val the value
         * <p/>
         * @throws ConfigurationException if an error occurs
         */
        public void configure(Object val) throws ConfigurationException {
            try {
                if (getTarget().get() != null) {
                    getMethod().invoke(getTarget().get(), val);
                }
            } catch (IllegalAccessException ex) {
                logAndThrowError(val, ex);
            } catch (IllegalArgumentException ex) {
                logAndThrowError(val, ex);
            } catch (InvocationTargetException ex) {
                logAndThrowError(val, ex.getTargetException());
            }
        }

        private void logAndThrowError(Object val, Throwable t) throws ConfigurationException {
            String message = String.format("Error while setting value '%s' (%s) for property '%s' with method '%s'",
                                           val, val.getClass(), getKey(), getMethod());
            log.error(message);
            throw new ConfigurationException(message, t);
        }
    }
}
