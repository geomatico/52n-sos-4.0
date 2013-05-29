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
package org.n52.sos.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor.TokenType;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.service.CodingRepository;

/**
 * Utility class for 52N
 * 
 */
public final class N52XmlHelper {
    
    private static final String SPACE = " ";

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
     *            List of schema locations
     */
    public static void setSchemaLocationsToDocument(XmlObject document, Collection<SchemaLocation> schemaLocations) {
        StringBuilder schemaLocation = new StringBuilder();
        for (SchemaLocation sl : schemaLocations) {
            if (sl != null) {
                schemaLocation.append(sl.getSchemaLocationString());
                schemaLocation.append(SPACE);
            }
        }
        schemaLocation = schemaLocation.deleteCharAt(schemaLocation.lastIndexOf(SPACE));
        setSchemaLocationToDocument(document, schemaLocation.toString());
    }

    /**
     * W3C XSI schema location
     * 
     * @return QName of schema location
     */
    public static QName getSchemaLocationQName() {
        return W3CConstants.QN_SCHEMA_LOCATION;
    }
    
    /**
     * W3C XSI schema location with prefix
     * 
     * @return QName of schema location
     */
    public static QName getSchemaLocationQNameWithPrefix() {
        return W3CConstants.QN_SCHEMA_LOCATION_PREFIXED;
    }

    /**
     * SOS 1.0.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSOS100() {
        return new SchemaLocation(Sos1Constants.NS_SOS, Sos1Constants.SCHEMA_LOCATION_SOS);
    }

    /**
     * SOS 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSOS200() {
        return new SchemaLocation(Sos2Constants.NS_SOS_20, Sos2Constants.SCHEMA_LOCATION_URL_SOS);
    }

    /**
     * OM 1.0.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForOM100() {
        return new SchemaLocation(OMConstants.NS_OM, OMConstants.SCHEMA_LOCATION_URL_OM_CONSTRAINT);
    }

    /**
     * OM 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForOM200() {
        return new SchemaLocation(OMConstants.NS_OM_2, OMConstants.SCHEMA_LOCATION_URL_OM_20);
    }

    /**
     * GML 3.1.1 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForGML311() {
        return new SchemaLocation(GMLConstants.NS_GML, GMLConstants.SCHEMA_LOCATION_URL_GML_311);
    }

    /**
     * GML 3.2.1 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForGML321() {
        return new SchemaLocation(GMLConstants.NS_GML_32, GMLConstants.SCHEMA_LOCATION_URL_GML_32);
    }

    /**
     * SOS OGC schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForOGC() {
        return new SchemaLocation(OGCConstants.NS_OGC, OGCConstants.SCHEMA_LOCATION_OGC);
    }

    /**
     * OWS 1.1.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForOWS110() {
        return new SchemaLocation(OWSConstants.NS_OWS, OWSConstants.SCHEMA_LOCATION_URL_OWS);
    }
    
    /**
     * OWS 1.1.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForOWS110Exception() {
        return new SchemaLocation(OWSConstants.NS_OWS, OWSConstants.SCHEMA_LOCATION_URL_OWS_EXCEPTIONREPORT);
    }

    /**
     * Sampling 1.0.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSA100() {
        return new SchemaLocation(SFConstants.NS_SA, SFConstants.SCHEMA_LOCATION_URL_SA);
    }

    /**
     * Sampling 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSF200() {
        return new SchemaLocation(SFConstants.NS_SF, SFConstants.SCHEMA_LOCATION_URL_SF);
    }

    /**
     * SamplingSpatial 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSAMS200() {
        return new SchemaLocation(SFConstants.NS_SAMS, SFConstants.SCHEMA_LOCATION_URL_SAMS);
    }

    /**
     * SensorML 1.0.1 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSML101() {
        return new SchemaLocation(SensorMLConstants.NS_SML, SensorMLConstants.SCHEMA_LOCATION_URL_SML_101);
    }

    /**
     * SWECommon 1.0.1 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSWE101() {
        return new SchemaLocation(SWEConstants.NS_SWE_101, SWEConstants.SCHEMA_LOCATION_URL_SWE_101);
    }

    /**
     * SWECommon 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSWE200() {
        return new SchemaLocation(SWEConstants.NS_SWE_20, SWEConstants.SCHEMA_LOCATION_URL_SWE_20);
    }
    
    /**
     * SWECommon 2.0 schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForSWES200() {
        return new SchemaLocation(SWEConstants.NS_SWES_20, SWEConstants.SCHEMA_LOCATION_URL_SWES_20);
    }

    /**
     * W3C XLINK schema location
     * 
     * @return QName of schema location
     */
    public static SchemaLocation getSchemaLocationForXLINK() {
        return new SchemaLocation(W3CConstants.NS_XLINK, W3CConstants.SCHEMA_LOCATION_XLINK);
    }

    public static SchemaLocation getSchemaLocationForSOAP12() {
        return new SchemaLocation(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
    }

    private N52XmlHelper() {
    }

    public static void addSchemaLocationsForTo(XmlObject xmlObject, Set<SchemaLocation> schemaLocations) {
        Set<String> namespaces = new HashSet<String>();
        XmlCursor newCursor = xmlObject.newCursor();
        while (newCursor.hasNextToken()) {
            TokenType evt = newCursor.toNextToken();
            if (evt == TokenType.START) {
                QName qName = newCursor.getName();
                if (qName != null) {
                    namespaces.add(qName.getNamespaceURI());
                }
            }
        }
        addSchemaLocationsForTo(namespaces, schemaLocations);
    }

    private static void addSchemaLocationsForTo(Set<String> namespaces, Set<SchemaLocation> schemaLocations) {
       for (String namespace : namespaces) {
           schemaLocations.addAll(CodingRepository.getInstance().getSchemaLocation(namespace));
       }
    }
}
