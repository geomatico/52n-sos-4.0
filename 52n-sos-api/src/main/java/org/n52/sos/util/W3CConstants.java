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

import javax.xml.namespace.QName;

/**
 * Constants class for W3C
 * 
 */
public class W3CConstants {

    // attribute names
	
    public static final String AN_HREF = "href";
	
    public static final String AN_TITLE = "title";
	
    // namespaces and schema locations

    public static final String NS_XLINK = "http://www.w3.org/1999/xlink";

    public static final String NS_XLINK_PREFIX = "xlink";

    public static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    public static final String NS_XSI_PREFIX = "xsi";

    public static final String SCHEMA_LOCATION_XLINK = "http://www.w3.org/1999/xlink.xsd";

    public static final String AN_SCHEMA_LOCATION = "schemaLocation";

    public static final String NS_XS = "http://www.w3.org/2001/XMLSchema";

    public static final String NS_XS_PREFIX = "xs";

    public static final QName QN_SCHEMA_LOCATION = new QName(NS_XSI, AN_SCHEMA_LOCATION);

    public static final QName QN_SCHEMA_LOCATION_PREFIXED = new QName(NS_XSI, AN_SCHEMA_LOCATION, NS_XSI_PREFIX);

    private W3CConstants() {
    }
}
