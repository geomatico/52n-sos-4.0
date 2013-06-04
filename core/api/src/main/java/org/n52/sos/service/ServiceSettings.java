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

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionGroup;
import org.n52.sos.config.SettingDefinitionProvider;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;
import org.n52.sos.util.CollectionHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ServiceSettings implements SettingDefinitionProvider {

    public static final String SERVICE_URL = "service.sosUrl";
    public static final String MINIMUM_GZIP_SIZE = "service.minimumGzipSize";
    public static final String SUPPORTS_QUALITY = "service.supportsQuality";
    public static final String SENSOR_DIRECTORY = "service.sensorDirectory";
    public static final String USE_DEFAULT_PREFIXES = "service.useDefaultPrefixes";
    public static final String ENCODE_FULL_CHILDREN_IN_DESCRIBE_SENSOR = "service.encodeFullChildrenInDescribeSensor";
    public static final String GENERATE_OFFERING_WHEN_NOT_SPECIFIED = "service.generateOfferingWhenNotSpecified";
    public static final String MAX_GET_OBSERVATION_RESULTS = "service.maxGetObservationResults";
    
    public static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup()
            .setTitle("Service").setOrder(2);
    
    public static final UriSettingDefinition SERVICE_URL_DEFINITION = new UriSettingDefinition()
            .setGroup(GROUP)
            .setOrder(0)
            .setKey(SERVICE_URL)
            .setTitle("SOS URL")
            .setDescription("The endpoint URL of this sos which will be shown in the GetCapabilities response "
                            + "(e.g. <code>http://localhost:8080/52nSOS/sos</code>). The path to a specific "
                            + "binding (like <code>/soap</code>) will appended to this URL.");
    
    public static final IntegerSettingDefinition MINIMUM_GZIP_SIZE_DEFINITION = new IntegerSettingDefinition()
            .setMinimum(0)
            .setOrder(3)
            .setGroup(GROUP)
            .setKey(MINIMUM_GZIP_SIZE)
            .setDefaultValue(1048576)
            .setTitle("GZIP Threshold")
            .setDescription("The size (in byte) the SOS starts to gzip responses (if the client supports it).");
    public static final IntegerSettingDefinition MAX_GET_OBSERVATION_RESULTS_DEFINITION = new IntegerSettingDefinition()
            .setGroup(GROUP)
            .setOrder(4)
            .setKey(MAX_GET_OBSERVATION_RESULTS)
            .setDefaultValue(0)
            .setTitle("Maximum number of observations")
            .setDescription("Maximum number of observation in GetObservation responses. "
                            + "Set to <code>0</code> (zero) for unlimited number of observations.");
    public static final BooleanSettingDefinition SUPPORTS_QUALITY_DEFINITION = new BooleanSettingDefinition()
            .setGroup(GROUP)
            .setOrder(5)
            .setKey(SUPPORTS_QUALITY)
            .setDefaultValue(true)
            .setTitle("Supports quality")
            .setDescription("Support quality information in observations.");
    
    public static final StringSettingDefinition SENSOR_DIRECTORY_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(7)
            .setKey(SENSOR_DIRECTORY)
            .setDefaultValue("/sensors")
            .setOptional(true)
            .setTitle("Sensor Directory")
            .setDescription("The path to a directory with the sensor descriptions in SensorML format. "
                            + "It can be either an absolute path (like <code>/home/user/sosconfig/sensors</code>) "
                            + "or a path relative to the web application directory (e.g. <code>WEB-INF/sensors</code>).");

    public static final BooleanSettingDefinition USE_DEFAULT_PREFIXES_DEFINITION = new BooleanSettingDefinition()
            .setGroup(GROUP)
            .setOrder(11)
            .setKey(USE_DEFAULT_PREFIXES)
            .setDefaultValue(false)
            .setOptional(true)
            .setTitle("Use default prefixes for offering, procedure, features")
            .setDescription("Use default prefixes for offering, procedure, features.");

    public static final BooleanSettingDefinition ENCODE_FULL_CHILDREN_IN_DESCRIBE_SENSOR_DEFINITION = new BooleanSettingDefinition()
    		.setGroup(GROUP)
    		.setOrder(12)
    		.setKey(ENCODE_FULL_CHILDREN_IN_DESCRIBE_SENSOR)
    		.setDefaultValue(true)
    		.setTitle("Whether to encode full SensorML for child procedures in DescribeSensor responses")
    		.setDescription("Whether to encode full SensorML for child procedures in DescribeSensor responses.");    

    public static final BooleanSettingDefinition GENERATE_OFFERING_WHEN_NOT_SPECIFIED_DEFINITION = new BooleanSettingDefinition()
			.setGroup(GROUP)
			.setOrder(13)
			.setKey(GENERATE_OFFERING_WHEN_NOT_SPECIFIED)
			.setDefaultValue(true)
			.setTitle("Whether to generate an offering during InsertSensor if none are specified.")
			.setDescription("Whether to generate an offering during InsertSensor if none are specified.");    
    
    private static final Set<SettingDefinition<?, ?>> DEFINITIONS = CollectionHelper.<SettingDefinition<?,?>>set(
            SERVICE_URL_DEFINITION,
            MINIMUM_GZIP_SIZE_DEFINITION,
            MAX_GET_OBSERVATION_RESULTS_DEFINITION,
            SUPPORTS_QUALITY_DEFINITION,
            SENSOR_DIRECTORY_DEFINITION,
            USE_DEFAULT_PREFIXES_DEFINITION,
            ENCODE_FULL_CHILDREN_IN_DESCRIBE_SENSOR_DEFINITION,
            GENERATE_OFFERING_WHEN_NOT_SPECIFIED_DEFINITION);

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return Collections.unmodifiableSet(DEFINITIONS);
    }
}
