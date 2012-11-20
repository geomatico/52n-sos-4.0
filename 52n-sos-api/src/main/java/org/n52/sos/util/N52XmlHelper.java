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
package org.n52.sos.util;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SWEConstants;

/**
 * Utility class for 52N
 * 
 */
public class N52XmlHelper {

    /**
     * Sets the schema location to a XmlObject
     * 
     * @param document
     *            XML document
     * @param schemaLocations
     *            schema location
     */
    public static void setSchemaLocationToDocument(XmlObject document, String schemaLocations) {
        XmlCursor cursor = document.newCursor();
        if (cursor.toFirstChild()) {
            cursor.setAttributeText(getSchemaLocationQName(), schemaLocations);
        }
        cursor.dispose();
    }

    /**
     * Sets the schema locations to a XmlObject
     * 
     * @param document
     *            XML document
     * @param schemaLocations
     *            schema locations
     */
    public static void setSchemaLocationsToDocument(XmlObject document, List<String> schemaLocations) {
        StringBuilder schemaLocation = new StringBuilder();
        for (String sl : schemaLocations) {
            schemaLocation.append(sl);
            schemaLocation.append(" ");
        }
        schemaLocation = schemaLocation.deleteCharAt(schemaLocation.lastIndexOf(" "));
        setSchemaLocationToDocument(document, schemaLocation.toString());
    }

    /**
     * W3C XSI schema location
     * 
     * @return QName of schema location
     */
    public static QName getSchemaLocationQName() {
        return new QName(W3CConstants.NS_XSI, W3CConstants.AN_SCHEMA_LOCATION);
    }
    
    /**
     * W3C XSI schema location with prefix
     * 
     * @return QName of schema location
     */
    public static QName getSchemaLocationQNameWithPrefix() {
        return new QName(W3CConstants.NS_XSI, W3CConstants.AN_SCHEMA_LOCATION, W3CConstants.NS_XSI_PREFIX);
    }

    /**
     * SOS 1.0.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSOS100() {
        return Sos1Constants.NS_SOS + " " + Sos1Constants.SCHEMA_LOCATION_SOS;
    }

    /**
     * SOS 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSOS200() {
        return Sos2Constants.NS_SOS_20 + " " + Sos2Constants.SCHEMA_LOCATION_SOS;
    }

    /**
     * OM 1.0.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForOM100() {
        return OMConstants.NS_OM + " " + OMConstants.SCHEMA_LOCATION_OM_CONSTRAINT;
    }

    /**
     * OM 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForOM200() {
        return OMConstants.NS_OM_2 + " " + OMConstants.SCHEMA_LOCATION_OM_2;
    }

    /**
     * GML 3.1.1 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForGML311() {
        return GMLConstants.NS_GML + " " + GMLConstants.SCHEMA_LOCATION_GML;
    }

    /**
     * GML 3.2.1 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForGML321() {
        return GMLConstants.NS_GML_32 + " " + GMLConstants.SCHEMA_LOCATION_GML_32;
    }

    /**
     * SOS OGC schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForOGC() {
        return OGCConstants.NS_OGC + " " + OGCConstants.SCHEMA_LOCATION_OGC;
    }

    /**
     * OWS 1.1.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForOWS110() {
        return OWSConstants.NS_OWS + " " + OWSConstants.SCHEMA_LOCATION_OWS;
    }
    
    /**
     * OWS 1.1.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForOWS110Exception() {
        return OWSConstants.NS_OWS + " " + OWSConstants.SCHEMA_LOCATION_OWS_EXCEPTIONREPORT;
    }

    /**
     * Sampling 1.0.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSA100() {
        return SFConstants.NS_SA + " " + SFConstants.SCHEMA_LOCATION_SA;
    }

    /**
     * Sampling 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSF200() {
        return SFConstants.NS_SF + " " + SFConstants.SCHEMA_LOCATION_SF;
    }

    /**
     * SamplingSpatial 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSAMS200() {
        return SFConstants.NS_SAMS + " " + SFConstants.SCHEMA_LOCATION_SAMS;
    }

    /**
     * SensorML 1.0.1 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSML101() {
        return SensorMLConstants.NS_SML + " " + SensorMLConstants.SCHEMA_LOCATION_SML;
    }

    /**
     * SWECommon 1.0.1 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSWE101() {
        return SWEConstants.NS_SWE + " " + SWEConstants.SCHEMA_LOCATION_SWE;
    }

    /**
     * SWECommon 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSWE200() {
        return SWEConstants.NS_SWE_20 + " " + SWEConstants.SCHEMA_LOCATION_SWE_200;
    }
    
    /**
     * SWECommon 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForSWES200() {
        return SWEConstants.NS_SWES_20 + " " + SWEConstants.SCHEMA_LOCATION_SWES_200;
    }

    /**
     * W3C XLINK schema location
     * 
     * @return QName of schema location
     */
    public static String getSchemaLocationForXLINK() {
        return W3CConstants.NS_XLINK + " " + W3CConstants.SCHEMA_LOCATION_XLINK;
    }

    public static String getSchemaLocationForSOAP12() {
        return SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + " " + SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE;
    }

}
