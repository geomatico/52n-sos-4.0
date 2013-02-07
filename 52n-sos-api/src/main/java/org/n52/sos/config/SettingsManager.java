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
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import org.n52.sos.service.AdminUser;
import org.n52.sos.service.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class SettingsManager {

    private static final Logger log = LoggerFactory.getLogger(SettingsManager.class);
    private static ReentrantLock creationLock = new ReentrantLock();
    private static SettingsManager instance;

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
    private final SettingDefinitionProviderRepository settingWanterRepository;

    protected SettingsManager() throws ConfigurationException {
        settingWanterRepository = new SettingDefinitionProviderRepository();
    }

    public SettingDefinitionProviderRepository getSettingWanterRepository() {
        return settingWanterRepository;
    }

    public Set<ISettingDefinition<?>> getSettings() {
        return getSettingWanterRepository().getWantedSettings();
    }

    public Set<String> getKeys() {
        Set<ISettingDefinition<?>> settings = getSettings();
        HashSet<String> keys = new HashSet<String>(settings.size());
        for (ISettingDefinition<?> setting  : settings) {
            keys.add(setting.getKey());
        }
        return keys;
    }
    
    public abstract <T> ISettingValue<T> getValue(ISettingDefinition<T> key);
    public abstract Set<ISettingValue<?>> getValues();
    public abstract void saveValue(ISettingValue<?> setting);
    public abstract AdminUser getAdminUser();
    public abstract void saveAdminUser(AdminUser adminUser);
    public abstract void setAdminUserName(String name);
    public abstract void setAdminPassword(String password);
    public abstract ISettingValueFactory getSettingFactory();
}
