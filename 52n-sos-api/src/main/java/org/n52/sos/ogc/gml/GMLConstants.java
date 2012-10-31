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
package org.n52.sos.ogc.gml;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.om.OMConstants;

public class GMLConstants {

    /* namespaces and schema locations */
    public static final String NS_GML = "http://www.opengis.net/gml";

    public static final String NS_GML_32 = "http://www.opengis.net/gml/3.2";

    public static final String NS_GML_PREFIX = "gml";

    public static final String SCHEMA_LOCATION_GML = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    public static final String SCHEMA_LOCATION_GML_32 = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";

    public static final String GML_ID_ATT = "id";

    /* element names used in GML */

    public static final String EN_TIME_INSTANT = "TimeInstant";

    public static final String EN_TIME_PERIOD = "TimePeriod";

    public static final String EN_TIME_BEGIN = "beginTime";

    public static final String EN_TIME_END = "endTime";

    public static final String EN_TIME_POSITION = "timePosition";

    public static final String EN_BEGIN_POSITION = "beginPosition";

	public static final String EN_END_POSITION = "endPosition";

    public static final String GML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    // nil values from GML 3.2 Section 8.2.3.1
    public static final String NIL_INAPPLICABLE = "urn:ogc:def:nil:OGC:inapplicable";

    public static final String NIL_MISSING = "urn:ogc:def:nil:OGC:missing";

    public static final String NIL_TEMPLATE = "urn:ogc:def:nil:OGC:template";

    public static final String NIL_UNKNOWN = "urn:ogc:def:nil:OGC:unknown";

    public static final String NIL_WITHHELD = "urn:ogc:def:nil:OGC:withheld";

    public static final String EN_ENVELOPE = "Envelope";

    public static final String EN_ABSTRACT_TIME_OBJECT = "_TimeObject";

    public static final String EN_ABSTRACT_ENCODING = "_Encoding";

    public static final String EN_ABSTRACT_OBSERVATION = "AbstractObservation";

    public static final String EN_ABSTRACT_FEATURE = "_Feature";

    public static final String EN_ABSTRACT_FEATURE_COLLECTION = "_FeatureCollection";

    public static final String EN_ABSTRACT_GEOMETRY = "_Geometry";

    public static final String EN_ABSTRACT_SURFACE = "_Surface";

    public static final String EN_ABSTRACT_TIME_GEOM_PRIM = "_TimeGeometricPrimitive";

    public static final String EN_ABSTRACT_RING = "_Ring";

    public static final String EN_LINEAR_RING = "LinearRing";

    public static final String EN_LINE_STRING = "LineString";

    public static final String EN_POINT = "Point";

    public static final String EN_POLYGON = "Polygon";

    public static final String EN_LOWER_CORNER = "lowerCorner";

    public static final String EN_UPPER_CORNER = "upperCorner";

    /* attribute names in GML */

    public static final String AN_ID = "id";
	
    /* QNames for elements */

    public static final QName QN_ENVELOPE = new QName(NS_GML, EN_ENVELOPE, NS_GML_PREFIX);

    public static final QName QN_POINT = new QName(NS_GML, EN_POINT, NS_GML_PREFIX);

    public static final QName QN_LINESTRING = new QName(NS_GML, EN_LINE_STRING, NS_GML_PREFIX);

    public static final QName QN_POLYGON = new QName(NS_GML, EN_POLYGON, NS_GML_PREFIX);

    public static final QName QN_ENVELOPE_32 = new QName(NS_GML_32, EN_ENVELOPE, NS_GML_PREFIX);

    public static final QName QN_POINT_32 = new QName(NS_GML_32, EN_POINT, NS_GML_PREFIX);

    public static final QName QN_LINESTRING_32 = new QName(NS_GML_32, EN_LINE_STRING, NS_GML_PREFIX);

    public static final QName QN_POLYGON_32 = new QName(NS_GML_32, EN_POLYGON, NS_GML_PREFIX);

    public static final QName QN_TIME_INSTANT = new QName(NS_GML, EN_TIME_INSTANT, NS_GML_PREFIX);

    public static final QName QN_TIME_PERIOD = new QName(NS_GML, EN_TIME_PERIOD, NS_GML_PREFIX);

    public static final QName QN_TIME_INSTANT_32 = new QName(NS_GML_32, EN_TIME_INSTANT, NS_GML_PREFIX);

    public static final QName QN_TIME_PERIOD_32 = new QName(NS_GML_32, EN_TIME_PERIOD, NS_GML_PREFIX);

    public static final QName QN_ABSTRACT_FEATURE_COLLECTION = new QName(NS_GML, EN_ABSTRACT_FEATURE_COLLECTION,
            NS_GML_PREFIX);

    public static final QName QN_FEATURE_COLLECTION = new QName(GMLConstants.NS_GML,
            OMConstants.EN_FEATURE_COLLECTION, NS_GML_PREFIX);

    public static final QName QN_ABSTRACT_RING = new QName(NS_GML, EN_ABSTRACT_RING, NS_GML_PREFIX);

    public static final QName QN_LINEAR_RING = new QName(NS_GML, EN_LINEAR_RING, NS_GML_PREFIX);

    public static final QName QN_ABSTRACT_RING_32 = new QName(NS_GML_32, EN_ABSTRACT_RING, NS_GML_PREFIX);

    public static final QName QN_LINEAR_RING_32 = new QName(NS_GML_32, EN_LINEAR_RING, NS_GML_PREFIX);

    public static final QName QN_ABSTRACT_TIME_OBJECT = new QName(NS_GML, EN_ABSTRACT_TIME_OBJECT, NS_GML_PREFIX);

    public static final QName QN_ABSTRACT_TIME_GEOM_PRIM =
            new QName(NS_GML, EN_ABSTRACT_TIME_GEOM_PRIM, NS_GML_PREFIX);

    /** string constant for ascending sorting order */
    public static final String SORT_ORDER_ASC = SortingOrder.ASC.name();

    /** Constant for result model of common observations */
    public static final String SORT_ORDER_DESC = SortingOrder.DESC.name();

    /**
     * Hide utility constructor
     */
    private GMLConstants() {
        super();
    }

    /**
     * Enumeration of the possible values for indeterminate Time attribute of
     * eventtime in GetObservation request
     * 
     */
    public static enum IndetTimeValues {
        after, before, now, unknown
    }

    /**
     * enumeration of the possible sorting orders
     * 
     */
    public static enum SortingOrder {
        ASC, DESC
    }

}
