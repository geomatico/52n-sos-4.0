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
 * Constants for SOAP messages
 */
public interface SosSoapConstants {

    // SOS core
    String REQ_ACTION_GET_CAPABILITIES = "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetCapabilities";
    String RESP_ACTION_GET_CAPABILITIES = "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetCapabilitiesResponse";
    
    String REQ_ACTION_DESCRIBE_SENSOR = "http://www.opengis.net/swes/2.0/DescribeSensor";
    String RESP_ACTION_DESCRIBE_SENSOR = "http://www.opengis.net/swes/2.0/DescribeSensorResponse";
    
    String REQ_ACTION_GET_OBSERVATION = "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetObservation";
    String RESP_ACTION_GET_OBSERVATION = "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetObservationResponse";

    // SOS transactional
    String REQ_ACTION_INSERT_OBSERVATION = "http://www.opengis.net/def/serviceOperation/sos/obsInsertion/2.0/InsertObservation";
    String RESP_ACTION_INSERT_OBSERVATION = "http://www.opengis.net/def/serviceOperation/sos/obsInsertion/2.0/InsertObservationResponse";
    
     String REQ_ACTION_UPDATE_SENSOR_DESCRIPTION = "http://www.opengis.net/swes/2.0/UpdateSensorDescription";
    String RESP_ACTION_UPDATE_SENSOR_DESCRIPTION = "http://www.opengis.net/swes/2.0/UpdateSensorDescriptionResponse";
    
    String REQ_ACTION_INSERT_SENSOR = "http://www.opengis.net/swes/2.0/InsertSensor";
    String RESP_ACTION_INSERT_SENSOR = "http://www.opengis.net/swes/2.0/InsertSensorResponse";
    
    String REQ_ACTION_DELETE_SENSOR = "http://www.opengis.net/swes/2.0/DeleteSensor";
    String RESP_ACTION_DELETE_SENSOR = "http://www.opengis.net/swes/2.0/DeleteSensorResponse";

    // SOS enhanced
    String REQ_ACTION_GET_FEATURE_OF_INTEREST = "http://www.opengis.net/def/serviceOperation/sos/foiRetrieval/2.0/GetFeatureOfInterest";
    String RESP_ACTION_GET_FEATURE_OF_INTEREST = "http://www.opengis.net/def/serviceOperation/sos/foiRetrieval/2.0/GetFeatureOfInterestResponse";
    
    String REQ_ACTION_GET_OBSERVATION_BY_ID = "http://www.opengis.net/def/serviceOperation/sos/obsByIdRetrieval/2.0/GetObservationById";
    String RESP_ACTION_GET_OBSERVATION_BY_ID = "http://www.opengis.net/def/serviceOperation/sos/obsByIdRetrieval/2.0/GetObservationByIdResponse";
    
    // SOS result handling
    String REQ_ACTION_GET_RESULT_TEMPLATE = "http://www.opengis.net/def/serviceOperation/sos/resultRetrieval/2.0/GetResultTemplate";
    String RESP_ACTION_GET_RESULT_TEMPLATE = "http://www.opengis.net/def/serviceOperation/sos/resultRetrieval/2.0/GetResultTemplateResponse";
    
    String REQ_ACTION_INSERT_RESULT_TEMPLATE = "http://www.opengis.net/def/serviceOperation/sos/resultInsertion/2.0/InsertResultTemplate";
    String RESP_ACTION_INSERT_RESULT_TEMPLATE = "http://www.opengis.net/def/serviceOperation/sos/resultInsertion/2.0/InsertResultTemplateResponse";
    
    String REQ_ACTION_GET_RESULT = "http://www.opengis.net/def/serviceOperation/sos/resultRetrieval/2.0/GetResult";
    String RESP_ACTION_GET_RESULT = "http://www.opengis.net/def/serviceOperation/sos/resultRetrieval/2.0/GetResultResponse";
     
    String REQ_ACTION_INSERT_RESULT = "http://www.opengis.net/def/serviceOperation/sos/resultInsertion/2.0/InsertResultTemplate";
    String RESP_ACTION_INSERT_RESULT = "http://www.opengis.net/def/serviceOperation/sos/resultInsertion/2.0/InsertResultTemplateResponse";
    
    // Exceptions

    /**
     * SWES exception response action URI
     */
    String RESP_ACTION_SWES = "http://www.opengis.net/swes/2.0/Exception";

    /**
     * SOS exception response action URI
     */
    String RESP_ACTION_SOS = "http://www.opengis.net/def/serviceOperation/sos/core/2.0/Exception";

    /**
     * OWS exception response action URI
     */
    String RESP_ACTION_OWS = "http://www.opengis.net/ows/1.1/Exception";

    /**
     * SOAP exception response action URI
     */
    String RESP_ACTION_SOAP = "http://www.w3.org/2005/08/addressing/soap/fault";

    /**
     * WSA exception response action URI
     */
    String RESP_ACTION_WSA = "http://www.w3.org/2005/08/addressing/fault";

    /**
     * WSN exception response action URI
     */
    String RESP_ACTION_WSN = "http://docs.oasis-open.org/wsn/fault";

}
