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

package org.n52.sos.ds.hibernate.util;

import org.hibernate.criterion.Criterion;
import org.n52.sos.exception.ows.concrete.UnsupportedOperatorException;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.ows.OwsExceptionReport;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SpatialRestrictions {
    /**
     * Get spatial filter restrictions.
     *
     * @param propertyName column name
     * @param operator     Spatial filter
     * @param geometry     the geometry
     *
     * @return filter restriction
     *
     * @throws OwsExceptionReport If the spatial filter is not supported
     */
    public static Criterion filter(String propertyName, SpatialOperator operator, Geometry geometry) throws
            OwsExceptionReport {
        switch (operator) {
            case BBOX:
                return within(propertyName, geometry);
            // TODO why are these commented?
            case Contains:
//                return contains(propertyName, geometry);
            case Crosses:
//                return crosses(propertyName, geometry);
            case Disjoint:
//                return disjoint(propertyName, geometry);
            case DWithin:
//                return distanceWithin(propertyName, geometry, 10);
            case Equals:
//                return eq(propertyName, geometry);
            case Intersects:
//                return intersects(propertyName, geometry);
            case Overlaps:
//                return overlaps(propertyName, geometry);
            case Touches:
//                return touches(propertyName, geometry);
            case Within:
//                return within(propertyName, geometry);
            case Beyond:
            default:
                throw new UnsupportedOperatorException(operator);
        }
    }

    public static Criterion eq(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.eq(propertyName, value);
    }

    public static Criterion within(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.within(propertyName, value);
    }

    public static Criterion contains(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.contains(propertyName, value);
    }

    public static Criterion crosses(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.crosses(propertyName, value);
    }

    public static Criterion disjoint(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.disjoint(propertyName, value);
    }

    public static Criterion intersects(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.intersects(propertyName, value);
    }

    public static Criterion overlaps(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.overlaps(propertyName, value);
    }

    public static Criterion touches(String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.touches(propertyName, value);
    }

    public static Criterion distanceWithin(String propertyName, Geometry geometry, double distance) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.distanceWithin(propertyName, geometry, distance);
    }

    public static Criterion havingSRID(String propertyName, int srid) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.havingSRID(propertyName, srid);
    }

    public static Criterion isEmpty(String propertyName) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.isEmpty(propertyName);
    }

    public static Criterion isNotEmpty(String propertyName) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.isNotEmpty(propertyName);
    }

    public static Criterion spatialRestriction(int relation, String propertyName, Geometry value) {
        return org.hibernate.spatial.criterion.SpatialRestrictions.spatialRestriction(relation, propertyName, value);
    }

    private SpatialRestrictions() {
    }
}
