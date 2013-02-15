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
package org.n52.sos.service;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingDefinitionProvider;
import org.n52.sos.util.CollectionHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ConfiguratorSettingDefinitionProvider implements ISettingDefinitionProvider {

    private static final Set<ISettingDefinition<?, ?>> DEFINITIONS = CollectionHelper.<ISettingDefinition<?,?>>set(
            MiscSettingDefinitions.TOKEN_SEPERATOR_DEFINITION,
            MiscSettingDefinitions.TUPLE_SEPERATOR_DEFINITION,
            MiscSettingDefinitions.DECIMAL_SEPARATOR_DEFINITION,
            MiscSettingDefinitions.GML_DATE_FORMAT_DEFINITION,
            MiscSettingDefinitions.SRS_NAME_PREFIX_SOS_V1_DEFINITION,
            MiscSettingDefinitions.SRS_NAME_PREFIX_SOS_V2_DEFINITION,
            MiscSettingDefinitions.DEFAULT_OFFERING_PREFIX_DEFINITION,
            MiscSettingDefinitions.DEFAULT_PROCEDURE_PREFIX_DEFINITION,
            MiscSettingDefinitions.CHARACTER_ENCODING_DEFINITION,
            MiscSettingDefinitions.EPSG_CODES_WITH_REVERSED_AXIS_ORDER_DEFINITION,
            ServiceSettingDefinitions.SERVICE_URL_DEFINITION,
            ServiceSettingDefinitions.DEFAULT_EPSG_DEFINITION,
            ServiceSettingDefinitions.LEASE_DEFINITION,
            ServiceSettingDefinitions.MINIMUM_GZIP_SIZE_DEFINITION,
            ServiceSettingDefinitions.MAX_GET_OBSERVATION_RESULTS_DEFINITION,
            ServiceSettingDefinitions.SUPPORTS_QUALITY_DEFINITION,
            ServiceSettingDefinitions.CAPABILITIES_CACHE_UPDATE_INTERVAL_DEFINITION,
            ServiceSettingDefinitions.SENSOR_DIRECTORY_DEFINITION,
            ServiceSettingDefinitions.CACHE_THREAD_COUNT_DEFINITION,
            ServiceSettingDefinitions.SKIP_DUPLICATE_OBSERVATIONS_DEFINITION,
            ServiceSettingDefinitions.CONFIGURATION_FILES_DEFINITION);

    @Override
    public Set<ISettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(DEFINITIONS);
    }
}
