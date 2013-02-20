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

import org.n52.sos.config.SettingDefinitionGroup;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ServiceSettingDefinitions {

    public static final String SERVICE_URL = "service.sosUrl";
    public static final String LEASE = "service.lease";
    public static final String MINIMUM_GZIP_SIZE = "service.minimumGzipSize";
    public static final String MAX_GET_OBSERVATION_RESULTS = "service.maxGetObservationResults";
    public static final String SUPPORTS_QUALITY = "service.supportsQuality";
    public static final String CAPABILITIES_CACHE_UPDATE_INTERVAL = "service.capabilitiesCacheUpdateInterval";
    public static final String SENSOR_DIRECTORY = "service.sensorDirectory";
    public static final String SKIP_DUPLICATE_OBSERVATIONS = "service.skipDuplicateObservations";
    public static final String CONFIGURATION_FILES = "service.configurationFiles";
    public static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup()
            .setTitle("Service Settings").setOrder(2);
    
    public static final UriSettingDefinition SERVICE_URL_DEFINITION = new UriSettingDefinition()
            .setGroup(GROUP)
            .setOrder(0)
            .setKey(SERVICE_URL)
            .setTitle("SOS URL")
            .setDescription("The endpoint URL of this sos which will be shown in the GetCapabilities response "
                            + "(e.g. <code>http://localhost:8080/52nSOS/sos</code>). The path to a specific "
                            + "binding (like <code>/soap</code>) will appended to this URL.");
    public static final IntegerSettingDefinition LEASE_DEFINITION = new IntegerSettingDefinition()
            .setGroup(GROUP)
            .setOrder(2)
            .setKey(LEASE)
            .setDefaultValue(600)
            .setTitle("Lease")
            .setDescription("Time of lease for result template in GetResult operation in minutes.");
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
    public static final IntegerSettingDefinition CAPABILITIES_CACHE_UPDATE_INTERVAL_DEFINITION = new IntegerSettingDefinition()
            .setGroup(GROUP)
            .setOrder(6)
            .setKey(CAPABILITIES_CACHE_UPDATE_INTERVAL)
            .setDefaultValue(5)
            .setMinimum(0)
            .setTitle("Capabilities cache update interval")
            .setDescription("The update interval of the capabilities cache in minutes.");
    public static final StringSettingDefinition SENSOR_DIRECTORY_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(7)
            .setKey(SENSOR_DIRECTORY)
            .setOptional(true)
            .setTitle("Sensor Directory")
            .setDescription("The path to a directory with the sensor descriptions in SensorML format. "
                            + "It can be either an absolute path (like <code>/home/user/sosconfig/sensors</code>) "
                            + "or a path relative to the web application directory (e.g. <code>WEB-INF/sensors</code>).");
    public static final BooleanSettingDefinition SKIP_DUPLICATE_OBSERVATIONS_DEFINITION = new BooleanSettingDefinition()
            .setGroup(GROUP)
            .setOrder(9)
            .setKey(SKIP_DUPLICATE_OBSERVATIONS)
            .setDefaultValue(true)
            .setTitle("Skip duplicate observations")
            .setDescription("Skip duplicate observations silently when inserted by batch.");
    public static final StringSettingDefinition CONFIGURATION_FILES_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(10)
            .setKey(CONFIGURATION_FILES)
            .setOptional(true)
            .setTitle("Configuration Files")
            .setDescription(
            "Configuration files and their file identifier (List: IDENTIFIER FILENAME;IDENTIFIER2 FILENAME2; ...).");

    private ServiceSettingDefinitions() {
    }
}
