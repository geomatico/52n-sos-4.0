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
import org.n52.sos.config.settings.StringSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class MiscSettingDefinitions {

    public static final String TOKEN_SEPERATOR = "misc.tokenSeperator";
    public static final String TUPLE_SEPERATOR = "misc.tupleSeperator";
    public static final String DECIMAL_SEPARATOR = "misc.decimalSeperator";
    public static final String GML_DATE_FORMAT = "misc.gmlDateFormat";
    public static final String SRS_NAME_PREFIX_SOS_V1 = "misc.srsNamePrefixSosV1";
    public static final String SRS_NAME_PREFIX_SOS_V2 = "misc.srsNamePrefixSosV2";
    public static final String DEFAULT_OFFERING_PREFIX = "misc.defaultOfferingPrefix";
    public static final String DEFAULT_PROCEDURE_PREFIX = "misc.defaultProcedurePrefix";
    public static final String CHARACTER_ENCODING = "misc.characterEncoding";
    
    public static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup()
            .setTitle("Miscellaneous Settings").setOrder(3);
    public static final StringSettingDefinition TOKEN_SEPERATOR_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(0)
            .setKey(TOKEN_SEPERATOR)
            .setDefaultValue(",")
            .setTitle("Token separator")
            .setDescription("Token separator in result element (a character)");
    public static final StringSettingDefinition TUPLE_SEPERATOR_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(0)
            .setKey(TUPLE_SEPERATOR)
            .setDefaultValue(";")
            .setTitle("Tuple separator")
            .setDescription("Tuple separator in result element (a character)");
    public static final StringSettingDefinition DECIMAL_SEPARATOR_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(0)
            .setKey(DECIMAL_SEPARATOR)
            .setDefaultValue(".")
            .setTitle("Decimal separator")
            .setDescription("Decimal separator in result element (a character)");
    public static final StringSettingDefinition GML_DATE_FORMAT_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(1)
            .setKey(GML_DATE_FORMAT)
            .setOptional(true)
            .setTitle("Date format of GML")
            .setDescription("Date format of Geography Markup Language");
    public static final StringSettingDefinition SRS_NAME_PREFIX_SOS_V1_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(2)
            .setKey(SRS_NAME_PREFIX_SOS_V1)
            .setDefaultValue("urn:ogc:def:crs:EPSG::")
            .setTitle("SOSv1 SRS Prefix")
            .setDescription("Prefix for the SRS name in SOS v1.0.0.");
    public static final StringSettingDefinition SRS_NAME_PREFIX_SOS_V2_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(3)
            .setKey(SRS_NAME_PREFIX_SOS_V2)
            .setDefaultValue("http://www.opengis.net/def/crs/EPSG/0/")
            .setTitle("SOSv2 SRS Prefix")
            .setDescription("Prefix for the SRS name in SOS v2.0.0.");
    public static final StringSettingDefinition DEFAULT_OFFERING_PREFIX_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(4)
            .setKey(DEFAULT_OFFERING_PREFIX)
            .setDefaultValue("OFFERING_")
            .setTitle("Default Offering Prefix")
            .setDescription("The default prefix for generated offerings (if not defined in RegisterSensor requests).");
    public static final StringSettingDefinition DEFAULT_PROCEDURE_PREFIX_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(4)
            .setKey(DEFAULT_PROCEDURE_PREFIX)
            .setDefaultValue("urn:ogc:object:feature:Sensor:")
            .setTitle("Default Procedure Prefix")
            .setDescription("The default prefix for generated procedures (if not defined in RegisterSensor requests).");
    public static final StringSettingDefinition CHARACTER_ENCODING_DEFINITION = new StringSettingDefinition()
            .setGroup(GROUP)
            .setOrder(5)
            .setKey(CHARACTER_ENCODING)
            .setDefaultValue("UTF-8")
            .setTitle("Character Encoding")
            .setDescription("The character encoding used for responses.");

    private MiscSettingDefinitions() {
    }
}
