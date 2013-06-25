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
package org.n52.sos.ogc.gml;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.util.SchemaLocation;

public interface GMLConstants {

    /* namespaces and schema locations */
    String NS_GML = "http://www.opengis.net/gml";

    String NS_GML_32 = "http://www.opengis.net/gml/3.2";

    String NS_GML_PREFIX = "gml";

    String SCHEMA_LOCATION_URL_GML_311 = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    String SCHEMA_LOCATION_URL_GML_32 = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
    
    SchemaLocation GML_311_SCHEMAL_LOCATION = new SchemaLocation(NS_GML, SCHEMA_LOCATION_URL_GML_311);
    
    SchemaLocation GML_32_SCHEMAL_LOCATION = new SchemaLocation(NS_GML_32, SCHEMA_LOCATION_URL_GML_32);
            
    String GML_ID_ATT = "id";

    /* element names used in GML */

    String EN_DESCRIPTION = "description";
    
    String EN_TIME_INSTANT = "TimeInstant";

    String EN_TIME_PERIOD = "TimePeriod";

    String EN_TIME_BEGIN = "beginTime";

    String EN_TIME_END = "endTime";

    String EN_TIME_POSITION = "timePosition";

    String EN_BEGIN_POSITION = "beginPosition";

	String EN_END_POSITION = "endPosition";

    String GML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    // nil values from GML 3.2 Section 8.2.3.1
    String NIL_INAPPLICABLE = "urn:ogc:def:nil:OGC:inapplicable";

    String NIL_MISSING = "urn:ogc:def:nil:OGC:missing";

    String NIL_TEMPLATE = "urn:ogc:def:nil:OGC:template";

    String NIL_UNKNOWN = "urn:ogc:def:nil:OGC:unknown";

    String NIL_WITHHELD = "urn:ogc:def:nil:OGC:withheld";

    String EN_ENVELOPE = "Envelope";

    String EN_ABSTRACT_TIME_OBJECT = "_TimeObject";
	
	String EN_ABSTRACT_TIME_OBJECT_32 = "AbstractTimeObject";

    String EN_ABSTRACT_ENCODING = "_Encoding";

    String EN_ABSTRACT_OBSERVATION = "AbstractObservation";

    String EN_ABSTRACT_FEATURE = "_Feature";
	
	String EN_ABSTRACT_FEATURE_32 = "AbstractFeature";

    String EN_ABSTRACT_FEATURE_COLLECTION = "_FeatureCollection";
    
    String EN_FEATURE_COLLECTION = "FeatureCollection";

    String EN_ABSTRACT_GEOMETRY = "_Geometry";

    String EN_ABSTRACT_SURFACE = "_Surface";

    String EN_ABSTRACT_TIME_GEOM_PRIM = "_TimeGeometricPrimitive";

    String EN_ABSTRACT_RING_311 = "_Ring";
    
    String EN_ABSTRACT_RING_32 = "AbstractRing";

    String EN_LINEAR_RING = "LinearRing";

    String EN_LINE_STRING = "LineString";

    String EN_POINT = "Point";

    String EN_MULTIPOINT = "MultiPoint";
    
    String EN_POLYGON = "Polygon";

    String EN_LOWER_CORNER = "lowerCorner";

    String EN_UPPER_CORNER = "upperCorner";

    /* attribute names in GML */

    String AN_ID = "id";
	
    /* QNames for elements */

    QName QN_DESCRIPTION = new QName(NS_GML, EN_DESCRIPTION, NS_GML_PREFIX);
    
    QName QN_ENVELOPE = new QName(NS_GML, EN_ENVELOPE, NS_GML_PREFIX);

    QName QN_POINT = new QName(NS_GML, EN_POINT, NS_GML_PREFIX);

    QName QN_MULTIPOINT = new QName(NS_GML, EN_MULTIPOINT, NS_GML_PREFIX);
    
    QName QN_LINESTRING = new QName(NS_GML, EN_LINE_STRING, NS_GML_PREFIX);

    QName QN_POLYGON = new QName(NS_GML, EN_POLYGON, NS_GML_PREFIX);

    QName QN_ENVELOPE_32 = new QName(NS_GML_32, EN_ENVELOPE, NS_GML_PREFIX);

    QName QN_POINT_32 = new QName(NS_GML_32, EN_POINT, NS_GML_PREFIX);

    QName QN_LINESTRING_32 = new QName(NS_GML_32, EN_LINE_STRING, NS_GML_PREFIX);

    QName QN_POLYGON_32 = new QName(NS_GML_32, EN_POLYGON, NS_GML_PREFIX);

    QName QN_TIME_INSTANT = new QName(NS_GML, EN_TIME_INSTANT, NS_GML_PREFIX);

    QName QN_TIME_PERIOD = new QName(NS_GML, EN_TIME_PERIOD, NS_GML_PREFIX);

    QName QN_TIME_INSTANT_32 = new QName(NS_GML_32, EN_TIME_INSTANT, NS_GML_PREFIX);

    QName QN_TIME_PERIOD_32 = new QName(NS_GML_32, EN_TIME_PERIOD, NS_GML_PREFIX);

    QName QN_ABSTRACT_FEATURE_COLLECTION = new QName(NS_GML, EN_ABSTRACT_FEATURE_COLLECTION,
            NS_GML_PREFIX);

    QName QN_FEATURE_COLLECTION = new QName(NS_GML,
            OMConstants.EN_FEATURE_COLLECTION, NS_GML_PREFIX);

    QName QN_ABSTRACT_RING = new QName(NS_GML, EN_ABSTRACT_RING_311, NS_GML_PREFIX);

    QName QN_LINEAR_RING = new QName(NS_GML, EN_LINEAR_RING, NS_GML_PREFIX);

    QName QN_ABSTRACT_RING_32 = new QName(NS_GML_32, EN_ABSTRACT_RING_32, NS_GML_PREFIX);

    QName QN_LINEAR_RING_32 = new QName(NS_GML_32, EN_LINEAR_RING, NS_GML_PREFIX);

    QName QN_ABSTRACT_TIME_OBJECT = new QName(NS_GML, EN_ABSTRACT_TIME_OBJECT, NS_GML_PREFIX);

    QName QN_ABSTRACT_TIME_GEOM_PRIM =
            new QName(NS_GML, EN_ABSTRACT_TIME_GEOM_PRIM, NS_GML_PREFIX);
	
	QName QN_ABSTRACT_FEATURE_GML = new QName(NS_GML, EN_ABSTRACT_FEATURE, NS_GML_PREFIX);
	
	QName QN_ABSTRACT_FEATURE_GML_32 = new QName(NS_GML_32, EN_ABSTRACT_FEATURE_32, NS_GML_PREFIX);
	
    QName QN_ABSTRACT_TIME_32 = new QName(NS_GML_32, EN_ABSTRACT_TIME_OBJECT_32, NS_GML_PREFIX);
    

    /** string constant for ascending sorting order */
    String SORT_ORDER_ASC = SortingOrder.ASC.name();

    /** Constant for result model of common observations */
    String SORT_ORDER_DESC = SortingOrder.DESC.name();

    /**
     * Enumeration of the possible values for indeterminate Time attribute of
     * eventtime in GetObservation request
     * 
     */
    enum IndetTimeValues {
        after, before, now, unknown
    }

    /**
     * enumeration of the possible sorting orders
     * 
     */
    enum SortingOrder {
        ASC, DESC
    }

}
