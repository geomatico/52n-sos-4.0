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


import org.n52.sos.config.settings.StringSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class MiscellaneousSettingDefinitions {
   
    public static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup()
            .setTitle("Miscellaneous settings");

    public static final ISettingDefinition<String> TOKEN_SEPERATOR = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("TOKEN_SEPERATOR")
            .setDefaultValue(",")
            .setTitle("Token separator")
            .setDescription("Token separator in result element (a character)");
    public static final ISettingDefinition<String> TUPLE_SEPERATOR = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("TUPLE_SEPERATOR")
            .setDefaultValue(";")
            .setTitle("Tuple separator")
            .setDescription("Tuple separator in result element (a character)");
    public static final ISettingDefinition<String> DECIMAL_SEPARATOR = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("DECIMAL_SEPARATOR")
            .setDefaultValue(".")
            .setTitle("Decimal separator")
            .setDescription("Decimal separator in result element (a character)");
    public static final ISettingDefinition<String> GML_DATE_FORMAT = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("GML_DATE_FORMAT")
            .setOptional(true)
            .setTitle("Date format of GML")
            .setDescription("Date format of Geography Markup Language");
    public static final ISettingDefinition<String> NO_DATA_VALUE = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("NO_DATA_VALUE")
            .setDefaultValue("noData")
            .setTitle("No data value")
            .setDescription("No data value");
    public static final ISettingDefinition<String> SRS_NAME_PREFIX_SOS_V1 = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SRS_NAME_PREFIX_SOS_V1")
            .setDefaultValue("urn:ogc:def:crs:EPSG::")
            .setTitle("SOSv1 SRS Prefix")
            .setDescription("Prefix for the SRS name in SOS v1.0.0.");
    public static final ISettingDefinition<String> SRS_NAME_PREFIX_SOS_V2 = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SRS_NAME_PREFIX_SOS_V2")
            .setDefaultValue("http://www.opengis.net/def/crs/EPSG/0/")
            .setTitle("SOSv2 SRS Prefix")
            .setDescription("Prefix for the SRS name in SOS v2.0.0.");
    public static final ISettingDefinition<String> DEFAULT_OFFERING_PREFIX = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("DEFAULT_OFFERING_PREFIX")
            .setDefaultValue("OFFERING_")
            .setTitle("Default Offering Prefix")
            .setDescription("The default prefix for generated offerings (if not defined in RegisterSensor requests).");
    public static final ISettingDefinition<String> DEFAULT_PROCEDURE_PREFIX = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("DEFAULT_PROCEDURE_PREFIX")
            .setDefaultValue("urn:ogc:object:feature:Sensor:")
            .setTitle("Default Procedure Prefix")
            .setDescription("The default prefix for generated procedures (if not defined in RegisterSensor requests).");
    public static final ISettingDefinition<String> CHARACTER_ENCODING = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("CHARACTER_ENCODING")
            .setDefaultValue("UTF-8")
            .setTitle("Character Encoding")
            .setDescription("The character encoding used for responses.");
    public static final ISettingDefinition<String> SWITCH_COORDINATES_FOR_EPSG_CODES = new StringSettingDefinition()
            .setGroup(GROUP)
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

    private MiscellaneousSettingDefinitions() {
    }
}
