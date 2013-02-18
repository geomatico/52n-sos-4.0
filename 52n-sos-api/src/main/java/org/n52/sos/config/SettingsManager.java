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

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.AbstractConfiguringServiceLoaderRepository;
import org.n52.sos.util.ConfiguringSingletonServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle the settings and configuration of the SOS. Allows other classes to change, delete, and declare
 * settings and to create, modify and delete administrator users. {@code ISettingDefinitions} are loaded from
 * {@link ISettingDefinitionProvider} by the Java {@link ServiceLoader} interface. Classes can subscribe to specific
 * settings using the {@code Configurable} and {@code Setting} annotations. To be recognized by the SettingsManager
 * {@link #configure(java.lang.Object)} has to be called for every object that wants to recieve settings. This is
 * automatically done for all classes loaded by the {@link Configurator}. All other classes have to call
 * {@code configure(java.lang.Object)} manually.
 * <p/>
 * @see IAdministratorUser
 * @see ISettingDefinition
 * @see ISettingDefinitionProvider
 * @see ISettingValue
 * @see Configurable
 * @see ConfiguringSingletonServiceLoader
 * @see AbstractConfiguringServiceLoaderRepository
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public abstract class SettingsManager {

    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
    private static ReentrantLock creationLock = new ReentrantLock();
    private static SettingsManager instance;

    /**
     * Gets the singleton instance of the SettingsManager.
     * <p/>
     * @return the settings manager
     * <p/>
     * @throws ConfigurationException if no implementation can be found
     */
    public static SettingsManager getInstance() throws ConfigurationException {
        if (instance == null) {
            creationLock.lock();
            try {
                if (instance == null) {
                    instance = createInstance();
                }
            } finally {
                creationLock.unlock();
            }
        }
        return instance;
    }

    /**
     * Creates a new {@code SettingsManager} with the {@link ServiceLoader} interface.
     * <p/>
     * @return the implementation
     * <p/>
     * @throws ConfigurationException if no implementation can be found
     */
    private static SettingsManager createInstance() throws ConfigurationException {
        ServiceLoader<SettingsManager> serviceLoader = ServiceLoader.load(SettingsManager.class);
        Iterator<SettingsManager> i = serviceLoader.iterator();
        while (i.hasNext()) {
            try {
                return i.next();
            } catch (ServiceConfigurationError e) {
                log.error("Could not instantiate SettingsManager", e);
            }
        }
        throw new ConfigurationException("No SettingsManager implementation loaded");
    }

    /**
     * Configure {@code o} with the required settings. All changes to a setting required by the object will be applied.
     * <p/>
     * @param o the object to configure
     * <p/>
     * @throws ConfigurationException if there is a problem configuring the object
     * @see Configurable
     * @see Setting
     */
    public abstract void configure(Object o) throws ConfigurationException;

    /**
     * Get the definition that is defined with the specified key.
     * <p/>
     * @param key the key
     * <p/>
     * @return the definition or {@code null} if there is no definition for the key
     */
    public abstract ISettingDefinition<?, ?> getDefinitionByKey(String key);

    /**
     * Gets all {@code ISettingDefinition}s known by this class.
     * <p/>
     * @return the defnitions
     */
    public abstract Set<ISettingDefinition<?, ?>> getSettingDefinitions();

    /**
     * Gets the value of the setting defined by {@code key}.
     * <p/>
     * @param <T> the type of the setting and value
     * @param key the definition of the setting
     * <p/>
     * @return the value of the setting
     */
    public abstract <T> ISettingValue<T> getSetting(ISettingDefinition<?, T> key);

    /**
     * Gets all values for all definitions. If there is no value for a definition {@code null} is added to the map.
     * <p/>
     * @return all values by definition
     */
    public abstract Map<ISettingDefinition<?, ?>, ISettingValue<?>> getSettings();

    /**
     * Deletes the setting defined by {@code setting}.
     * <p/>
     * @param setting the definition
     * <p/>
     * @throws ConfigurationException if there is a problem deleting the setting
     */
    public abstract void deleteSetting(ISettingDefinition<?, ?> setting) throws ConfigurationException;

    /**
     * Changes a setting. The change is propagated to all Objects that are configured. If the change fails for one of
     * these objects, the setting is reverted to the old value of the setting for all objects.
     * <p/>
     * @param value the new value of the setting
     * <p/>
     * @throws ConfigurationException if there is a problem changing the setting.
     */
    public abstract void changeSetting(ISettingValue<?> value) throws ConfigurationException;

    /* TODO JavaDoc */
    public abstract ISettingValueFactory getSettingFactory();

    /* TODO JavaDoc */
    public abstract Set<IAdministratorUser> getAdminUsers();

    /* TODO JavaDoc */
    public abstract IAdministratorUser getAdminUser(String username);

    /**
     * Creates a new {@code IAdministratorUser}. This method will fail if the username is already used by another user.
     * <p/>
     * @param username the proposed username
     * @param password the proposed (hashed) password
     * <p/>
     * @return the created user
     */
    public abstract IAdministratorUser createAdminUser(String username, String password);

    /**
     * Saves a user previously returned by {@link #getAdminUser(java.lang.String)} or {@link #getAdminUsers()}.
     * <p/>
     * @param user the user to change
     */
    public abstract void saveAdminUser(IAdministratorUser user);

    /**
     * Deletes the user with the specified username.
     * <p/>
     * @param username the username
     */
    public abstract void deleteAdminUser(String username);

    /**
     * Deletes the user previously returned by {@link #getAdminUser(java.lang.String)} or {@link #getAdminUsers()}.
     * <p/>
     * @param user
     */
    public abstract void deleteAdminUser(IAdministratorUser user);

    /**
     * Deletes all settings and users.
     */
    public abstract void deleteAll();

    /**
     * Clean up this SettingsManager. All subsequent calls to this class are undefined.
     */
    public abstract void cleanup();
}
