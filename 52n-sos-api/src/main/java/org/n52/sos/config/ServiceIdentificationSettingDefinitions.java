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

import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ServiceIdentificationSettingDefinitions {

    public static final SettingDefinitionGroup GROUP = new SettingDefinitionGroup()
            .setTitle("Service identification");
    
    public static final ISettingDefinition<String> SERVICE_TYPE = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_SERVICE_TYPE")
            .setTitle("Service Type")
            .setDescription("SOS Service Type.")
            .setDefaultValue("OGC:SOS");
    public static final ISettingDefinition<String> ACCESS_CONSTRAINTS = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS")
            .setTitle("Access Constraints")
            .setDescription("Service access constraints.")
            .setDefaultValue("NONE");
    public static final ISettingDefinition<File> FILE = new FileSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_FILE")
            .setTitle("Access Constraints")
            .setOptional(true)
            .setDescription("The path to a file containing an ows:ServiceIdentification"
                            + " overriding the above settings. It can be either an absolute path "
                            + "(like <code>/home/user/sosconfig/identification.xml</code>) or a path "
                            + "relative to the web application directory "
                            + "(e.g. <code>WEB-INF/identification.xml</code>).");
    public static final ISettingDefinition<String> TITLE = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_TITLE")
            .setTitle("52N SOS")
            .setDescription("SOS Service Title.")
            .setDefaultValue("52N SOS");
    public static final ISettingDefinition<String> KEYWORDS = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_KEYWORDS")
            .setTitle("Keywords")
            .setDescription("Comma separated SOS service keywords.")
            .setOptional(true);
    public static final ISettingDefinition<String> ABSTRACT = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_ABSTRACT")
            .setTitle("SOS Abstract")
            .setDescription("SOS service abstract.")
            .setDefaultValue("52North Sensor Observation Service - Data Access for the Sensor Web");
    public static final ISettingDefinition<String> FEES = new StringSettingDefinition()
            .setGroup(GROUP)
            .setKey("SERVICE_IDENTIFICATION_FEES")
            .setTitle("Fees")
            .setDescription("SOS Service Fees.")
            .setDefaultValue("NONE");

    private ServiceIdentificationSettingDefinitions() {
    }
}
