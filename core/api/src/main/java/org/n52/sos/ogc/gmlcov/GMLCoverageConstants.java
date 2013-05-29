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
package org.n52.sos.ogc.gmlcov;

import org.n52.sos.util.SchemaLocation;

public interface GMLCoverageConstants {
    
    String NS_GML_COV = "http://www.opengis.net/gmlcov/1.0";

    String NS_GML_COV_PREFIX = "gmlcov";
    
    String SCHEMA_LOCATION_URL_GML_COVERAGE_10 = "http://schemas.opengis.net/gmlcov/1.0/gmlcovAll.xsd";
    
    SchemaLocation GML_COVERAGE_10_SCHEMA_LOCATION = new SchemaLocation(NS_GML_COV, SCHEMA_LOCATION_URL_GML_COVERAGE_10);

}
