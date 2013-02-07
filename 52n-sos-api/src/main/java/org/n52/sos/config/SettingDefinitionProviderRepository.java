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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.util.AbstractServiceLoaderRepository;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
class SettingDefinitionProviderRepository extends AbstractServiceLoaderRepository<ISettingDefinitionProvider> {
    private Set<ISettingDefinition<?>> settingDefinition = Collections.emptySet();
    private Map<ISettingDefinition<?>, Set<ISettingDefinitionProvider>> providersByDefinition = Collections.emptyMap();

    SettingDefinitionProviderRepository() throws ConfigurationException {
        super(ISettingDefinitionProvider.class, false);
    }

    public Set<ISettingDefinition<?>> getWantedSettings() {
        return Collections.unmodifiableSet(this.settingDefinition);
    }

    public Set<ISettingDefinitionProvider> getWantersForSetting(ISettingDefinition<?> setting) {
        Set<ISettingDefinitionProvider> set = this.providersByDefinition.get(setting);
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    @Override
    protected void processImplementations(Set<ISettingDefinitionProvider> implementations) throws ConfigurationException {
        this.settingDefinition = new HashSet<ISettingDefinition<?>>();
        this.providersByDefinition = new HashMap<ISettingDefinition<?>, Set<ISettingDefinitionProvider>>();
        for (ISettingDefinitionProvider provider : implementations) {
            Set<ISettingDefinition<?>> requiredSettings = provider.getSettingDefinitions();
            this.settingDefinition.addAll(requiredSettings);
            for (ISettingDefinition<?> setting : requiredSettings) {
                Set<ISettingDefinitionProvider> wanters = this.providersByDefinition.get(setting);
                if (wanters == null) {
                    this.providersByDefinition.put(setting, wanters = new HashSet<ISettingDefinitionProvider>());
                }
                wanters.add(provider);
            }
        }
    }
    
}
