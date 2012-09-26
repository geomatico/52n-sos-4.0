/**
 * Copyright (C) 2012
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
 * 
 */
public class SosSoapConstants {

    // SOS core
    public static final String REQ_ACTION_GETCAPABILITIES =
            "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetCapabilities";

    public static final String RESP_ACTION_GETCAPABILITIES =
            "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetCapabilitiesResponse";

    public static final String REQ_ACTION_GETOBSERVATION =
            "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetObservation";

    public static final String RESP_ACTION_GETOBSERVATION =
            "http://www.opengis.net/def/serviceOperation/sos/core/2.0/GetObservationResponse";

    public static final String REQ_ACTION_DESCRIBESENSOR = "http://www.opengis.net/swes/2.0/DescribeSensor";

    public static final String RESP_ACTION_DESCRIBESENSOR = "http://www.opengis.net/swes/2.0/DescribeSensorResponse";

    // SOS transactional
    public static final String REQ_ACTION_INSERTSENSOR = "http://www.opengis.net/swes/2.0/InsertSensor";

    public static final String RESP_ACTION_INSERTSENSOR = "http://www.opengis.net/swes/2.0/InsertSensorResponse";

    public static final String REQ_ACTION_INSERTOBSERVATION =
            "http://www.opengis.net/def/serviceOperation/sos/obsInsertion/2.0/InsertObservation";

    public static final String RESP_ACTION_INSERTOBSERVATION =
            "http://www.opengis.net/def/serviceOperation/sos/obsInsertion/2.0/InsertObservationResponse";

    public static final String REQ_ACTION_UPDATESENSOR = "http://www.opengis.net/swes/2.0/UpdateSensorDescription";

    public static final String RESP_ACTION_UPDATESENSOR =
            "http://www.opengis.net/swes/2.0/UpdateSensorDescriptionResponse";

    public static final String REQ_ACTION_DELETESENSOR = "http://www.opengis.net/swes/2.0/DeleteSensor";

    public static final String RESP_ACTION_DELETESENSOR = "http://www.opengis.net/swes/2.0/DeleteSensorResponse";

    // Exceptions

    /**
     * SWES exception response action URI
     */
    public static final String RESP_ACTION_SWES = "http://www.opengis.net/swes/2.0/Exception";

    /**
     * SOS exception response action URI
     */
    public static final String RESP_ACTION_SOS = "http://www.opengis.net/def/serviceOperation/sos/core/2.0/Exception";

    /**
     * OWS exception response action URI
     */
    public static final String RESP_ACTION_OWS = "http://www.opengis.net/ows/1.1/Exception";

    /**
     * SOAP exception response action URI
     */
    public static final String RESP_ACTION_SOAP = "http://www.w3.org/2005/08/addressing/soap/fault";

    /**
     * WSA exception response action URI
     */
    public static final String RESP_ACTION_WSA = "http://www.w3.org/2005/08/addressing/fault";

    /**
     * WSN exception response action URI
     */
    public static final String RESP_ACTION_WSN = "http://docs.oasis-open.org/wsn/fault";

}
