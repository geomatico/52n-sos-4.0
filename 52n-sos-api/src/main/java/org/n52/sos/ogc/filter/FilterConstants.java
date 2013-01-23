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
package org.n52.sos.ogc.filter;

/**
 * Constants class for filter
 * 
 */
public class FilterConstants {

    public static final String NS_FES_2 = "http://www.opengis.net/fes/2.0";

    public static final String NS_FES_2_PREFIX = "fes";

    /**
     * element name TEquals
     */
    public static final String EN_TEQUALS = "TEquals";

    /**
     * element name ValueReference
     */
    public static final String EN_VALUE_REFERENCE = "ValueReference";

    /**
     * Enumeration for conformance class constraint names
     * 
     */
    public enum ConformanceClassConstraintNames {
        ImplementsQuery, ImplementsAdHocQuery, ImplementsFunctions, ImplementsResourceld, ImplementsMinStandardFilter, ImplementsStandardFilter, ImplementsMinSpatialFilter, ImplementsSpatialFilter, ImplementsMinTemporalFilter, ImplementsTemporalFilter, ImplementsVersionNav, ImplementsSorting, ImplementsExtendedOperators, ImplementsMinimumXPath, ImplementsSchemaElementFunc
    }

    /**
     * Enumeration for temporal operators
     * 
     */
    public enum TimeOperator {
        TM_Before, TM_After, TM_Begins, TM_Ends, TM_EndedBy, TM_BegunBy, TM_During, TM_Equals, TM_Contains, TM_Overlaps, TM_Meets, TM_MetBy, TM_OverlappedBy
    }

    /**
     * Enumeration for FES 2.0 temporal operators
     * 
     */
    public enum TimeOperator2 {
        Before, After, Begins, Ends, EndedBy, BegunBy, During, TEquals, Contains, Overlaps, Meets, MetBy, OverlappedBy
    }

    /**
     * Enumeration for spatial operators
     * 
     */
    public enum SpatialOperator {
        Equals, Disjoint, Touches, Within, Overlaps, Crosses, Intersects, Contains, DWithin, Beyond, BBOX
    }

    /**
     * Enumeration for comparison operators
     * 
     */
    public enum ComparisonOperator {
        PropertyIsEqualTo, PropertyIsNotEqualTo, PropertyIsLessThan, PropertyIsGreaterThan, PropertyIsLessThanOrEqualTo, PropertyIsGreaterThanOrEqualTo, PropertyIsLike, PropertyIsNil, PropertyIsNull, PropertyIsBetween
    }

}
