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
package org.n52.sos.ogc.sos;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface ConformanceClasses {
    
    String SOS_V2_CORE_PROFILE = "http://www.opengis.net/spec/SOS/2.0/conf/core";
    
    String SOS_V2_SOAP_BINDING = "http://www.opengis.net/spec/SOS/2.0/conf/soap";
    String SOS_V2_KVP_CORE_BINDING = "http://www.opengis.net/spec/SOS/2.0/conf/kvp-core";
    String SOS_V2_POX_BINDING = "http://www.opengis.net/spec/SOS/2.0/conf/pox";
    
    String SOS_V2_FEATURE_OF_INTEREST_RETRIEVAL = "http://www.opengis.net/spec/SOS/2.0/conf/foiRetrieval";
    String SOS_V2_OBSERVATION_BY_ID_RETRIEVAL = "http://www.opengis.net/spec/SOS/2.0/conf/obsByIdRetrieval";
    
    String SOS_V2_SENSOR_INSERTION = "http://www.opengis.net/spec/SOS/2.0/conf/sensorInsertion";
    String SOS_V2_SENSOR_DELETION = "http://www.opengis.net/spec/SOS/2.0/conf/sensorDeletion";
    String SOS_V2_UPDATE_SENSOR_DESCRIPTION = "http://www.opengis.net/spec/SOS/2.0/conf/updateSensorDescription";
    String SOS_V2_INSERTION_CAPABILITIES = "http://www.opengis.net/spec/SOS/2.0/conf/insertionCap";
    
    String SOS_V2_OBSERVATION_INSERTION = "http://www.opengis.net/spec/SOS/2.0/conf/obsInsertion";
    
    String SOS_V2_RESULT_RETRIEVAL = "http://www.opengis.net/spec/SOS/2.0/conf/resultRetrieval";
    String SOS_V2_RESULT_INSERTION = "http://www.opengis.net/spec/SOS/2.0/conf/resultInsertion";
    
    String OM_V2_TEXT_OBSERVATION = "http://www.opengis.net/spec/OMXML/2.0/conf/textObservation";
    String OM_V2_TRUTH_OBSERVATION = "http://www.opengis.net/spec/OMXML/2.0/conf/truthObservation";
    String OM_V2_MEASUREMENT = "http://www.opengis.net/spec/OMXML/2.0/conf/measurement";
    String OM_V2_GEOMETRY_OBSERVATION = "http://www.opengis.net/spec/OMXML/2.0/conf/geometryObservation";
    String OM_V2_COUNT_OBSERVATION = "http://www.opengis.net/spec/OMXML/2.0/conf/countObservation";
    String OM_V2_CATEGORY_OBSERVATION = "http://www.opengis.net/spec/OMXML/2.0/conf/categoryObservation";
    String OM_V2_SAMPLING_POINT = "http://www.opengis.net/spec/OMXML/2.0/conf/samplingPoint";
    String OM_V2_SAMPLING_CURVE = "http://www.opengis.net/spec/OMXML/2.0/conf/samplingCurve";
    String OM_V2_SAMPLING_SURFACE = "http://www.opengis.net/spec/OMXML/2.0/conf/samplingSurface";
    String OM_V2_SPATIAL_SAMPLING = "http://www.opengis.net/spec/OMXML/2.0/conf/spatialSampling";
    
    
    String SWE_V2_CORE = "http://www.opengis.net/spec/SWE/2.0/conf/core";
    String SWE_V2_UML_SIMPLE_COMPONENTS = "http://www.opengis.net/spec/SWE/2.0/conf/uml-simple-components";
    String SWE_V2_UML_RECORD_COMPONENTS = "http://www.opengis.net/spec/SWE/2.0/conf/uml-record-components";
    String SWE_V2_UML_BLOCK_ENCODINGS = "http://www.opengis.net/spec/SWE/2.0/conf/uml-block-components";
    String SWE_V2_UML_SIMPLE_ENCODINGS = "http://www.opengis.net/spec/SWE/2.0/conf/uml-simple-encodings";
    String SWE_V2_XSD_SIMPLE_COMPONENTS = "http://www.opengis.net/spec/SWE/2.0/conf/xsd-simple-components";
    String SWE_V2_XSD_RECORD_COMPONENTS = "http://www.opengis.net/spec/SWE/2.0/conf/xsd-record-components";
    String SWE_V2_XSD_BLOCK_COMPONENTS = "http://www.opengis.net/spec/SWE/2.0/conf/xsd-block-components";
    String SWE_V2_XSD_SIMPLE_ENCODINGS = "http://www.opengis.net/spec/SWE/2.0/conf/xsd-simple-encodings";
    String SWE_V2_GENERAL_ENCODING_RULES = "http://www.opengis.net/spec/SWE/2.0/conf/general-encoding-rules";
    String SWE_V2_TEXT_ENCODING_RULES = "http://www.opengis.net/spec/SWE/2.0/conf/text-encoding-rules";
    
}
