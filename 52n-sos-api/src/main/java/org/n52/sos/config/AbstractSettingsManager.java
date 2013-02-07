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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.SettingsChangeEvent;
import org.n52.sos.service.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractSettingsManager extends SettingsManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractSettingsManager.class);
    private final SettingDefinitionProviderRepository settingDefinitionRepository;

    protected AbstractSettingsManager() throws ConfigurationException {
        settingDefinitionRepository = new SettingDefinitionProviderRepository();
    }

    protected SettingDefinitionProviderRepository getSettingDefinitionRepository() {
        return settingDefinitionRepository;
    }

    @Override
    public Set<ISettingDefinition<?>> getSettingDefinitions() {
        return getSettingDefinitionRepository().getSettingDefinitions();
    }

    public Set<String> getKeys() {
        Set<ISettingDefinition<?>> settings = getSettingDefinitions();
        HashSet<String> keys = new HashSet<String>(settings.size());
        for (ISettingDefinition<?> setting : settings) {
            keys.add(setting.getKey());
        }
        return keys;
    }

    @Override
    public void changeSetting(ISettingDefinition<?> setting,
                              ISettingValue<?> newValue) throws ConfigurationException {
        if (newValue == null) {
            deleteSetting(setting);
            return;
        }
        ISettingValue<?> oldValue = getSettingValue(setting.getKey());
        if (oldValue == null || !oldValue.equals(newValue)) {
            applySetting(setting, oldValue, newValue);
            saveSettingValue(newValue);
        }
        
        SosEventBus.fire(new SettingsChangeEvent(setting, oldValue, newValue));
    }

    @Override
    public void deleteSetting(ISettingDefinition<?> setting) throws ConfigurationException {
        ISettingValue<?> oldValue = getSettingValue(setting.getKey());
        if (oldValue != null) {
            applySetting(setting, oldValue, null);
            deleteSettingValue(setting.getKey());
        }
        
        SosEventBus.fire(new SettingsChangeEvent(setting, oldValue, null));
    }

    private void applySetting(ISettingDefinition<?> setting, ISettingValue<?> oldValue,
                              ISettingValue<?> newValue) throws ConfigurationException {

        Set<ISettingDefinitionProvider> providers = getSettingDefinitionRepository().getProviders(setting);
        LinkedList<ISettingDefinitionProvider> changed = new LinkedList<ISettingDefinitionProvider>();
        ConfigurationException e = null;

        for (ISettingDefinitionProvider provider : providers) {
            try {
                provider.changeSettingValue(setting, oldValue, newValue);
            } catch (ConfigurationException ce) {
                e = ce;
                break;
            } finally {
                changed.add(provider);
            }
        }

        if (e != null) {
            log.debug("Reverting setting...");
            for (ISettingDefinitionProvider provider : changed) {
                try {
                    provider.changeSettingValue(setting, newValue, oldValue);
                } catch (ConfigurationException ce) {
                    /* there is nothing we can do... */
                    log.error("Error reverting setting!", ce);
                }
            }
            throw e;
        }
    }

    @Override
    public <T> ISettingValue<T> getSetting(ISettingDefinition<T> key) {
        return (ISettingValue<T>) getSettingValue(key.getKey());
    }
    
    @Override
    public Map<ISettingDefinition<?>, ISettingValue<?>> getSettings() {
        Set<ISettingValue<?>> values = getSettingValues();
        Map<ISettingDefinition<?>, ISettingValue<?>> settingsByDefinition 
                = new HashMap<ISettingDefinition<?>, ISettingValue<?>>(values.size());
        for (ISettingValue<?> value : values) {
            settingsByDefinition.put(getSettingDefinitionRepository().getDefinition(value.getKey()), value);
        }
        return settingsByDefinition;
    }
    
    @Override
    public void deleteAdminUser(IAdministratorUser user) {
        deleteAdminUser(user.getUsername());
    }
    
    protected abstract Set<ISettingValue<?>> getSettingValues();

    protected abstract ISettingValue<?> getSettingValue(String key);

    protected abstract void deleteSettingValue(String key);

    protected abstract void saveSettingValue(ISettingValue<?> setting);
}
