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

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;

/**
 * SOS filter capabilities
 * 
 */
public class FilterCapabilities {

    /**
     * Spatial operands list
     */
    private List<QName> spatialOperands;

    /**
     * Spatial operators map
     */
    private Map<SpatialOperator, List<QName>> spatialOperators;

    /**
     * Temporal operands list
     */
    private List<QName> temporalOperands;

    /**
     * Temporal operators map
     */
    private Map<TimeOperator, List<QName>> tempporalOperators;

    /**
     * Comparison operators list
     */
    private List<ComparisonOperator> comparisonOperators;

    /**
     * Get spatial operands
     * 
     * @return spatial operands
     */
    public List<QName> getSpatialOperands() {
        return spatialOperands;
    }

    /**
     * Set spatial operands
     * 
     * @param spatialOperands
     *            spatial operands
     */
    public void setSpatialOperands(List<QName> spatialOperands) {
        this.spatialOperands = spatialOperands;
    }

    /**
     * Get spatial operators
     * 
     * @return spatial operators
     */
    public Map<SpatialOperator, List<QName>> getSpatialOperators() {
        return spatialOperators;
    }

    /**
     * Set spatial operators
     * 
     * @param spatialOperators
     *            spatial operators
     */
    public void setSpatialOperators(Map<SpatialOperator, List<QName>> spatialOperators) {
        this.spatialOperators = spatialOperators;
    }

    /**
     * Get temporal operands
     * 
     * @return temporal operands
     */
    public List<QName> getTemporalOperands() {
        return temporalOperands;
    }

    /**
     * Set temporal operands
     * 
     * @param temporalOperands
     *            temporal operands
     */
    public void setTemporalOperands(List<QName> temporalOperands) {
        this.temporalOperands = temporalOperands;
    }

    /**
     * Get temporal operators
     * 
     * @return temporal operators
     */
    public Map<TimeOperator, List<QName>> getTempporalOperators() {
        return tempporalOperators;
    }

    /**
     * Set temporal operators
     * 
     * @param tempporalOperators
     *            temporal operators
     */
    public void setTempporalOperators(Map<TimeOperator, List<QName>> tempporalOperators) {
        this.tempporalOperators = tempporalOperators;
    }

    /**
     * Get comparison operators
     * 
     * @return comparison operators
     */
    public List<ComparisonOperator> getComparisonOperators() {
        return comparisonOperators;
    }

    /**
     * Set comparison operators
     * 
     * @param comparisonOperators
     *            comparison operators
     */
    public void setComparisonOperators(List<ComparisonOperator> comparisonOperators) {
        this.comparisonOperators = comparisonOperators;
    }

}
