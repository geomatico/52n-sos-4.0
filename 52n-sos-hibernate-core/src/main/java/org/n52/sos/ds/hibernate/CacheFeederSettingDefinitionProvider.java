/*
 * Copyright (C) 2013 52north.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.n52.sos.ds.hibernate;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingDefinitionProvider;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.service.ServiceSettingDefinitions;
import org.n52.sos.util.CollectionHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CacheFeederSettingDefinitionProvider implements ISettingDefinitionProvider {

    public static final String CACHE_THREAD_COUNT = "service.cacheThreadCount";
    
    public static final IntegerSettingDefinition CACHE_THREAD_COUNT_DEFINITION = new IntegerSettingDefinition()
            .setGroup(ServiceSettingDefinitions.GROUP)
            .setOrder(8)
            .setKey(CACHE_THREAD_COUNT)
            .setDefaultValue(5)
            .setTitle("Cache Feeder Threads")
            .setDescription("The number of threads used to fill the capabilities cache.");
    private static final Set<ISettingDefinition<?, ?>> DEFINITIONS = CollectionHelper.<ISettingDefinition<?, ?>>set(
            CACHE_THREAD_COUNT_DEFINITION);

    @Override
    public Set<ISettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(DEFINITIONS);
    }
}
