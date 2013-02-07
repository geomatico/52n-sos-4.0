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

import java.io.File;
import java.net.URI;

import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SettingDefinitions {

    public static final SettingDefinitionGroup SERVICE_IDENTIFICATION_GROUP = new SettingDefinitionGroup()
            .setTitle("Service identification");
    public static final SettingDefinitionGroup SERVICE_PROVIDER_GROUP = new SettingDefinitionGroup()
            .setTitle("Service Provider");
    public static final SettingDefinitionGroup MISCELLANEOUS_GROUP = new SettingDefinitionGroup()
            .setTitle("Miscellaneous settings");
    public static final SettingDefinitionGroup SERVICE_SETTINGS_GROUP = new SettingDefinitionGroup()
            .setTitle("Service settings");
    public static final ISettingDefinition<String> SERVICE_IDENTIFICATION_SERVICE_TYPE = new StringSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_SERVICE_TYPE")
            .setTitle("Service Type")
            .setDescription("SOS Service Type.")
            .setDefaultValue("OGC:SOS");
    public static final ISettingDefinition<File> SERVICE_IDENTIFICATION_FILE = new FileSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_FILE")
            .setTitle("Access Constraints")
            .setOptional(true)
            .setDescription("The path to a file containing an ows:ServiceIdentification"
                            + " overriding the above settings. It can be either an absolute path "
                            + "(like <code>/home/user/sosconfig/identification.xml</code>) or a path "
                            + "relative to the web application directory "
                            + "(e.g. <code>WEB-INF/identification.xml</code>).");
    public static final ISettingDefinition<String> SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS = new StringSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS")
            .setTitle("Access Constraints")
            .setDescription("Service access constraints.")
            .setDefaultValue("NONE");
    public static final ISettingDefinition<String> SERVICE_IDENTIFICATION_ABSTRACT = new StringSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_ABSTRACT")
            .setTitle("SOS Abstract")
            .setDescription("SOS service abstract.")
            .setDefaultValue("52North Sensor Observation Service - Data Access for the Sensor Web");
    public static final ISettingDefinition<String> SERVICE_IDENTIFICATION_TITLE = new StringSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_TITLE")
            .setTitle("52N SOS")
            .setDescription("SOS Service Title.")
            .setDefaultValue("52N SOS");
    public static final ISettingDefinition<String> SERVICE_IDENTIFICATION_FEES = new StringSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_FEES")
            .setTitle("Fees")
            .setDescription("SOS Service Fees.")
            .setDefaultValue("NONE");
    public static final ISettingDefinition<String> SERVICE_IDENTIFICATION_KEYWORDS = new StringSettingDefinition()
            .setGroup(SERVICE_IDENTIFICATION_GROUP)
            .setKey("SERVICE_IDENTIFICATION_KEYWORDS")
            .setTitle("Keywords")
            .setDescription("Comma separated SOS service keywords.")
            .setOptional(true);
    public static final ISettingDefinition<String> SERVICE_PROVIDER_DELIVERY_POINT = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_ADDRESS")
            .setTitle("Address")
            .setDescription("The street address of the responsible person.")
            .setDefaultValue("Martin-Luther-King-Weg 24");
    public static final ISettingDefinition<File> SERVICE_PROVIDER_FILE = new FileSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_FILE")
            .setTitle("Service Provider File")
            .setDescription("The path to a file containing an ows:ServiceProvider "
                            + "overriding the above settings. It can be either an "
                            + "absolute path (like <code>/home/user/sosconfig/provider.xml</code>) "
                            + "or a path relative to the web application directory (e.g. "
                            + "<code>WEB-INF/provider.xml</code>).")
            .setOptional(true);
    public static final ISettingDefinition<String> SERVICE_PROVIDER_COUNTRY = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_COUNTRY")
            .setTitle("Country")
            .setDescription("The country of the responsible person.")
            .setDefaultValue("Germany");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_PHONE = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_PHONE")
            .setTitle("Phone")
            .setDescription("The phone number of the responsible person.")
            .setDefaultValue("+49(0)251/396 371-0");
    public static final ISettingDefinition<URI> SERVICE_PROVIDER_SITE = new UriSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_SITE")
            .setTitle("Website")
            .setDescription("Your website.")
            .setDefaultValue(URI.create("http://52north.org/swe"));
    public static final ISettingDefinition<String> SERVICE_PROVIDER_ADMINISTRATIVE_AREA = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_STATE")
            .setTitle("State")
            .setDescription("The state of the responsible person.")
            .setDefaultValue("North Rhine-Westphalia");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_POSITION_NAME = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_POSITION_NAME")
            .setTitle("Position")
            .setDescription("The position of the responsible person.")
            .setDefaultValue("TBA");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_NAME = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_NAME")
            .setTitle("Name")
            .setDescription("Your or your company's name.")
            .setDefaultValue("52North");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_CITY = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_CITY")
            .setTitle("City")
            .setDescription("The city of the responsible person.")
            .setDefaultValue("M\u00fcnster");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_INDIVIDUAL_NAME = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_INDIVIDUAL_NAME")
            .setTitle("Responsible Person")
            .setDescription("The name of the responsible person of this service.")
            .setDefaultValue("TBA");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_MAIL_ADDRESS = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_EMAIL")
            .setTitle("Mail-Address")
            .setDescription("The electronic mail address of the responsible person.")
            .setDefaultValue("info@52north.org");
    public static final ISettingDefinition<String> SERVICE_PROVIDER_POSTAL_CODE = new StringSettingDefinition()
            .setGroup(SERVICE_PROVIDER_GROUP)
            .setKey("SERVICE_PROVIDER_ZIP")
            .setTitle("Postal Code")
            .setDescription("The postal code of the responsible person.")
            .setDefaultValue("48155");
    public static final ISettingDefinition<URI> SOS_URL = new UriSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("SOS_URL")
            .setTitle("SOS URL")
            .setDescription("The endpoint URL of this sos which will be shown in the GetCapabilities response "
                            + "(e.g. <code>http://localhost:8080/52nSOS/sos</code>). The path to a specific "
                            + "binding (like <code>/soap</code>) will appended to this URL.");
    public static final ISettingDefinition<Boolean> SUPPORTS_QUALITY = new BooleanSettingDefinition()
            .setGroup(
            SERVICE_SETTINGS_GROUP)
            .setKey("SUPPORTS_QUALITY")
            .setDefaultValue(true)
            .setTitle("Supports quality")
            .setDescription("Support quality information in observations.");
    public static final ISettingDefinition<Boolean> SKIP_DUPLICATE_OBSERVATIONS = new BooleanSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("SKIP_DUPLICATE_OBSERVATIONS")
            .setDefaultValue(true)
            .setTitle("Skip duplicate observations")
            .setDescription("Skip duplicate observations silently when inserted by batch.");
    public static final ISettingDefinition<Boolean> FOI_ENCODED_IN_OBSERVATION = new BooleanSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("FOI_ENCODED_IN_OBSERVATION")
            .setDefaultValue(true)
            .setTitle("FOI encoded in observation")
            .setDescription(
            "Encode the complete FOI instance within an Observation instance instead of just the FOI id.");
    public static final ISettingDefinition<Boolean> FOI_LISTED_IN_OFFERINGS = new BooleanSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("FOI_LISTED_IN_OFFERINGS")
            .setDefaultValue(true)
            .setTitle("FOI in offerings")
            .setDescription("Include list of FOI IDs in offerings.");
    public static final ISettingDefinition<Boolean> SHOW_FULL_OPERATIONS_METADATA = new BooleanSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("SHOW_FULL_OPERATIONS_METADATA")
            .setDefaultValue(true)
            .setTitle("Show full operations metadata")
            .setDescription("Include allowed values in OperationMetadata of GetCapabilities response.");
    public static final ISettingDefinition<Boolean> SHOW_FULL_OPERATIONS_METADATA_FOR_OBSERVATIONS = new BooleanSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("SHOW_FULL_OPERATIONS_METADATA_FOR_OBSERVATIONS")
            .setDefaultValue(true)
            .setTitle("Show full operations metadata for observations")
            .setDescription("Include allowed values for featureOfInterest in OperationMetadata of GetObservation.");
    public static final ISettingDefinition<Boolean> CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBE_SENSOR = new BooleanSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBE_SENSOR")
            .setDefaultValue(false)
            .setTitle("Encode child procedures.")
            .setDescription(
            "Encode the complete sensor description of a child procedure in the parent procedure description.");
    public static final ISettingDefinition<Integer> LEASE = new IntegerSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("LEASE")
            .setDefaultValue(600)
            .setTitle("Lease")
            .setDescription("Time of lease for result template in GetResult operation in minutes.");
    public static final ISettingDefinition<Integer> MAX_GET_OBSERVATION_RESULTS = new IntegerSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("MAX_GET_OBSERVATION_RESULTS")
            .setDefaultValue(0)
            .setTitle("Maximum number of observations")
            .setDescription("Maximum number of observation in GetObservation responses. "
                            + "Set to <code>0</code> (zero) for unlimited number of observations.");
    public static final ISettingDefinition<Integer> CAPABILITIES_CACHE_UPDATE_INTERVAL = new IntegerSettingDefinition()
            .setMinimum(0)
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("CAPABILITIES_CACHE_UPDATE_INTERVAL")
            .setDefaultValue(5)
            .setTitle("Capabilities cache update interval")
            .setDescription("The update interval of the capabilities cache in minutes.");
    public static final ISettingDefinition<Integer> MINIMUM_GZIP_SIZE = new IntegerSettingDefinition()
            .setMinimum(0)
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("MINIMUM_GZIP_SIZE")
            .setDefaultValue(1048576)
            .setTitle("GZIP Threshold")
            .setDescription("The size (in byte) the SOS starts to gzip responses (if the client supports it).");
    public static final ISettingDefinition<Integer> DEFAULT_EPSG = new IntegerSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("DEFAULT_EPSG")
            .setDefaultValue(4326)
            .setTitle("Default EPSG Code")
            .setDescription("The EPSG code in which the geometries are stored.");
    public static final ISettingDefinition<String> SENSOR_DIRECTORY = new StringSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("SENSOR_DIRECTORY")
            .setOptional(true)
            .setTitle("Sensor Directory")
            .setDescription("The path to a directory with the sensor descriptions in SensorML format. "
                            + "It can be either an absolute path (like <code>/home/user/sosconfig/sensors</code>) "
                            + "or a path relative to the web application directory (e.g. <code>WEB-INF/sensors</code>).");
    public static final ISettingDefinition<String> CONFIGURATION_FILES = new StringSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("CONFIGURATION_FILES")
            .setOptional(true)
            .setTitle("Configuration Files")
            .setDescription(
            "Configuration files and their file identifier (List: IDENTIFIER FILENAME;IDENTIFIER2 FILENAME2; ...).");
    public static final ISettingDefinition<Integer> CACHE_THREAD_COUNT = new IntegerSettingDefinition()
            .setGroup(SERVICE_SETTINGS_GROUP)
            .setKey("CACHE_THREAD_COUNT")
            .setDefaultValue(5)
            .setTitle("Cache Feeder Threads")
            .setDescription("The number of threads used to fill the capabilities cache.");
    public static final ISettingDefinition<String> TOKEN_SEPERATOR = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("TOKEN_SEPERATOR")
            .setDefaultValue(",")
            .setTitle("Token separator")
            .setDescription("Token separator in result element (a character)");
    public static final ISettingDefinition<String> TUPLE_SEPERATOR = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("TUPLE_SEPERATOR")
            .setDefaultValue(";")
            .setTitle("Tuple separator")
            .setDescription("Tuple separator in result element (a character)");
    public static final ISettingDefinition<String> DECIMAL_SEPARATOR = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("DECIMAL_SEPARATOR")
            .setDefaultValue(".")
            .setTitle("Decimal separator")
            .setDescription("Decimal separator in result element (a character)");
    public static final ISettingDefinition<String> GML_DATE_FORMAT = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("GML_DATE_FORMAT")
            .setOptional(true)
            .setTitle("Date format of GML")
            .setDescription("Date format of Geography Markup Language");
    public static final ISettingDefinition<String> NO_DATA_VALUE = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("NO_DATA_VALUE")
            .setDefaultValue("noData")
            .setTitle("No data value")
            .setDescription("No data value");
    public static final ISettingDefinition<String> SRS_NAME_PREFIX_SOS_V1 = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("SRS_NAME_PREFIX_SOS_V1")
            .setDefaultValue("urn:ogc:def:crs:EPSG::")
            .setTitle("SOSv1 SRS Prefix")
            .setDescription("Prefix for the SRS name in SOS v1.0.0.");
    public static final ISettingDefinition<String> SRS_NAME_PREFIX_SOS_V2 = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("SRS_NAME_PREFIX_SOS_V2")
            .setDefaultValue("http://www.opengis.net/def/crs/EPSG/0/")
            .setTitle("SOSv2 SRS Prefix")
            .setDescription("Prefix for the SRS name in SOS v2.0.0.");
    public static final ISettingDefinition<String> DEFAULT_OFFERING_PREFIX = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("DEFAULT_OFFERING_PREFIX")
            .setDefaultValue("OFFERING_")
            .setTitle("Default Offering Prefix")
            .setDescription("The default prefix for generated offerings (if not defined in RegisterSensor requests).");
    public static final ISettingDefinition<String> DEFAULT_PROCEDURE_PREFIX = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("DEFAULT_PROCEDURE_PREFIX")
            .setDefaultValue("urn:ogc:object:feature:Sensor:")
            .setTitle("Default Procedure Prefix")
            .setDescription("The default prefix for generated procedures (if not defined in RegisterSensor requests).");
    public static final ISettingDefinition<String> CHARACTER_ENCODING = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("CHARACTER_ENCODING")
            .setDefaultValue("UTF-8")
            .setTitle("Character Encoding")
            .setDescription("The character encoding used for responses.");
    public static final ISettingDefinition<String> SWITCH_COORDINATES_FOR_EPSG_CODES = new StringSettingDefinition()
            .setGroup(MISCELLANEOUS_GROUP)
            .setKey("SWITCH_COORDINATES_FOR_EPSG_CODES")
            .setOptional(true)
            .setDefaultValue("2044-2045;2081-2083;2085-2086;2093;2096-2098;2105-2132;2169-2170;2176-2180;"
                             + "2193;2200;2206-2212;2319;2320-2462;2523-2549;2551-2735;2738-2758;2935-2941;"
                             + "2953;3006-3030;3034-3035;3058-3059;3068;3114-3118;3126-3138;3300-3301;3328-3335;"
                             + "3346;3350-3352;3366;3416;4001-4999;20004-20032;20064-20092;21413-21423;21473-21483;"
                             + "21896-21899;22171;22181-22187;22191-22197;25884;27205-27232;27391-27398;27492;"
                             + "28402-28432;28462-28492;30161-30179;30800;31251-31259;31275-31279;31281-31290;31466-31700")
            .setTitle("EPSG Codes with Switched Coordinates")
            .setDescription("A list of all EPSG codes for which the SOS has to switch coordinate order, "
                            + "for example from lat/lon to lon/lat, or from x/y to y/x.");

    private SettingDefinitions() {
    }
}
