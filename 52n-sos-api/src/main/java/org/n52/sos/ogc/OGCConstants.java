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
package org.n52.sos.ogc;

/**
 * Constants for OGC
 * 
 */
public class OGCConstants {

    public static final String NS_OGC = "http://www.opengis.net/ogc";

    public static final String NS_OGC_PREFIX = "ogc";

    public static final String SCHEMA_LOCATION_OGC = "http://schemas.opengis.net/sos/1.0.0/ogc4sos.xsd";

    public static final String UNKNOWN = "http://www.opengis.net/def/nil/OGC/0/unknown";

	/** Constant for prefixes of FOIs */
	public static final String URN_FOI_PREFIX = "urn:ogc:def:object:feature:";

	public static final String URN_IDENTIFIER_IDENTIFICATION = "urn:ogc:def:identifier:OGC::identification";

	public static final String URN_OFFERING_ID = "urn:ogc:def:identifier:OGC:offeringID";

	/** Constant for prefixes of procedures */
	public static final String URN_PHENOMENON_PREFIX = "urn:ogc:def:phenomenon:OGC:1.0.30:";

	/** Constant for prefixes of procedures */
	public static final String URN_PROCEDURE_PREFIX = "urn:ogc:object:feature:Sensor:IFGI:";

	public static final String URN_PROPERTY_NAME_LOCATION = "urn:ogc:data:location";

	public static final String URN_PROPERTY_NAME_SAMPLING_GEOMETRY = "urn:ogc:data:samplingGeometry";

	public static final String URN_PROPERTY_NAME_SPATIAL_VALUE = "urn:ogc:data:spatialValue";

	public static final String URN_UNIQUE_IDENTIFIER = "urn:ogc:def:identifier:OGC:uniqueID";

	public static final String URN_UNIQUE_IDENTIFIER_END = "uniqueID";

	public static final String URN_UNIQUE_IDENTIFIER_START = "urn:ogc:def:identifier:OGC:";
}
