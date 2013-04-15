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

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.AbstractServiceLoaderRepository;
import org.n52.sos.util.MultiMaps;
import org.n52.sos.util.SetMultiMap;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository for {@code SettingDefinitionProvider} implementations.
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class SettingDefinitionProviderRepository extends AbstractServiceLoaderRepository<SettingDefinitionProvider> {
    private static final Logger LOG = LoggerFactory.getLogger(SettingDefinitionProviderRepository.class);
    private Map<String, SettingDefinition<?, ?>> definitionsByKey = Collections.emptyMap();
    private Set<SettingDefinition<?, ?>> settingDefinitions = Collections.emptySet();
    private SetMultiMap<SettingDefinition<?, ?>, SettingDefinitionProvider> providersByDefinition = MultiMaps
            .newSetMultiMap();

    /**
     * Constructs a new repository.
     * <p/>
     * @throws ConfigurationException if there is a problem while loading implementations
     */
    public SettingDefinitionProviderRepository() throws ConfigurationException {
        super(SettingDefinitionProvider.class, false);
        super.load(false);
    }

    /**
     * @return all setting definitions
     */
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(this.settingDefinitions);
    }

    /**
     * Returns all providers that declared a specific setting.
     * <p/>
     * @param setting the setting
     * <p/>
     * @return the providers
     */
    public Set<SettingDefinitionProvider> getProviders(SettingDefinition<?, ?> setting) {
        Set<SettingDefinitionProvider> set = this.providersByDefinition.get(setting);
        if (set == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableSet(set);
        }
    }

    /**
     * Gets the definition for the specified key.
     * <p/>
     * @param key the key
     * <p/>
     * @return the definition or {@code null} if none is known
     */
    public SettingDefinition<?, ?> getDefinition(String key) {
        return this.definitionsByKey.get(key);
    }

    @Override
    protected void processImplementations(Set<SettingDefinitionProvider> implementations) throws ConfigurationException {
        this.settingDefinitions = new HashSet<SettingDefinition<?, ?>>();
        this.providersByDefinition = MultiMaps.newSetMultiMap();
        this.definitionsByKey = new HashMap<String, SettingDefinition<?, ?>>();

        for (SettingDefinitionProvider provider : implementations) {
            LOG.debug("Processing IDefinitionProvider {}", provider);
            Set<SettingDefinition<?, ?>> requiredSettings = provider.getSettingDefinitions();
            for (SettingDefinition<?, ?> definition : requiredSettings) {
                SettingDefinition<?, ?> prev = definitionsByKey.put(definition.getKey(), definition);
                if (prev != null && !prev.equals(definition)) {
                    LOG.warn("{} overwrites {} requested by [{}]", definition, prev,
                             StringHelper.join(", ", this.providersByDefinition.get(prev)));
                    this.providersByDefinition.remove(prev);
                }
                LOG.debug("Found Setting definition for key '{}'", definition.getKey());
                if (!definition.isOptional() && !definition.hasDefaultValue()) {
                    LOG.warn("No default value for optional setting {}", definition.getKey());
                }
            }
            this.settingDefinitions.addAll(requiredSettings);
            for (SettingDefinition<?, ?> setting : requiredSettings) {
                this.providersByDefinition.add(setting, provider);
            }
        }
    }
}
