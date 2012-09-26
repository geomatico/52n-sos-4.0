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
package org.n52.sos.ogc.filter;

import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Spatial filter classs
 * 
 */
public class SpatialFilter {

    /**
     * Spatial filter operator
     */
    private SpatialOperator operator;

    /**
     * Filter geometry
     */
    private Geometry geometry;

    /**
     * Filter valueReference
     */
    private String valueReference;

    /**
     * constructor
     * 
     * @param operatorp
     *            Spatial operator
     * @param geomWKTp
     *            Filter geometry
     * @param valueReferencep
     *            Filter valueReference
     */
    public SpatialFilter(SpatialOperator operatorp, Geometry geomWKTp, String valueReferencep) {
        this.operator = operatorp;
        this.geometry = geomWKTp;
        this.valueReference = valueReferencep;
    }

    /**
     * default constructor
     */
    public SpatialFilter() {
    }

    /**
     * Get spatial filter operator
     * 
     * @return spatial filter operator
     */
    public SpatialOperator getOperator() {
        return operator;
    }

    /**
     * Set spatial filter operator
     * 
     * @param operator
     *            spatial filter operator
     */
    public void setOperator(SpatialOperator operator) {
        this.operator = operator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Spatial filter: " + operator + " " + geometry;
    }

    /**
     * Get SRID
     * 
     * @return SRID
     */
    public int getSrid() {
        return geometry.getSRID();
    }

    /**
     * Get filter geometry
     * 
     * @return filter geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Set filter geometry
     * 
     * @param geometry
     *            filter geometry
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Get valueReference
     * 
     * @return valueReference
     */
    public String getValueReference() {
        return valueReference;
    }

    /**
     * Set valueReference
     * 
     * @param valueReference
     *            valueReference
     */
    public void setValueReference(String valueReference) {
        this.valueReference = valueReference;
    }
}
