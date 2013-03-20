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
package org.n52.sos.ogc.sensorML;

import javax.xml.namespace.QName;

/**
 * Constants class for SensorML
 * 
 */
public interface SensorMLConstants {

    // namespaces and schema locations
    String NS_SML = "http://www.opengis.net/sensorML/1.0.1";

    String NS_SML_PREFIX = "sml";

    String SCHEMA_LOCATION_SML = "http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd";

    String SENSORML_OUTPUT_FORMAT_MIME_TYPE = "text/xml;subtype=\"sensorML/1.0.1\"";

    String SENSORML_OUTPUT_FORMAT_URL = NS_SML;

    String SENSORML_CONTENT_TYPE = "text/xml;subtype=\"sensorML/1.0.1\"";

    String EN_SYSTEM = "System";

    String EN_PROCESS_MODEL = "ProcessModel";
    
    String EN_COMPONENT = "Component";

    String EN_ABSTRACT_PROCESS = "AbstractProcess";

    QName SYSTEM_QNAME = new QName(NS_SML, EN_SYSTEM, NS_SML_PREFIX);

    QName PROCESS_MODEL_QNAME = new QName(NS_SML, EN_PROCESS_MODEL, NS_SML_PREFIX);
    
    QName COMPONENT_QNAME = new QName(NS_SML, EN_COMPONENT, NS_SML_PREFIX);

    QName ABSTRACT_PROCESS_QNAME = new QName(NS_SML, EN_ABSTRACT_PROCESS, NS_SML_PREFIX);

    String VERSION_V101 = "1.0.1";

	/**
	 * Name of a SensorML element describing the offering, a procedure/sensor is related to, or should be inserted into
	 */
    String ELEMENT_NAME_OFFERING = "offering";

}
