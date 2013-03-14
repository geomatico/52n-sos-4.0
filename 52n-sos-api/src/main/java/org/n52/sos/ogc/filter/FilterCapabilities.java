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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.util.QNameComparator;

/**
 * SOS filter capabilities
 *
 */
public class FilterCapabilities {
    /**
     * Spatial operands list
     */
    private SortedSet<QName> spatialOperands = new TreeSet<QName>(QNameComparator.INSTANCE);
    /**
     * Spatial operators map
     */
    private SortedMap<SpatialOperator, SortedSet<QName>> spatialOperators =
                                                         new TreeMap<SpatialOperator, SortedSet<QName>>();
    /**
     * Temporal operands list
     */
    private SortedSet<QName> temporalOperands = new TreeSet<QName>(QNameComparator.INSTANCE);
    /**
     * Temporal operators map
     */
    private SortedMap<TimeOperator, SortedSet<QName>> temporalOperators = new TreeMap<TimeOperator, SortedSet<QName>>();
    /**
     * Comparison operators list
     */
    private SortedSet<ComparisonOperator> comparisonOperators = new TreeSet<ComparisonOperator>();

    /**
     * Get spatial operands
     *
     * @return spatial operands
     */
    public SortedSet<QName> getSpatialOperands() {
        return Collections.unmodifiableSortedSet(spatialOperands);
    }

    /**
     * Set spatial operands
     *
     * @param spatialOperands spatial operands
     */
    public void setSpatialOperands(Collection<QName> spatialOperands) {
        this.spatialOperands.clear();
        if (spatialOperands != null) {
            this.spatialOperands.addAll(spatialOperands);
        }
    }

    /**
     * Get spatial operators
     *
     * @return spatial operators
     */
    public SortedMap<SpatialOperator, SortedSet<QName>> getSpatialOperators() {
        return Collections.unmodifiableSortedMap(spatialOperators);
    }

    /**
     * Set spatial operators
     *
     * @param spatialOperators spatial operators
     */
    public void setSpatialOperators(Map<SpatialOperator, ? extends Collection<QName>> spatialOperators) {
        this.spatialOperators.clear();
        if (spatialOperators != null) {
            for (SpatialOperator spatialOperator : spatialOperators.keySet()) {
                final TreeSet<QName> set = new TreeSet<QName>(QNameComparator.INSTANCE);
                if (spatialOperators.get(spatialOperator) != null) {
                    set.addAll(spatialOperators.get(spatialOperator));
                }
                this.spatialOperators.put(spatialOperator, set);
            }
        }
    }

    /**
     * Get temporal operands
     *
     * @return temporal operands
     */
    public SortedSet<QName> getTemporalOperands() {
        return Collections.unmodifiableSortedSet(temporalOperands);
    }

    /**
     * Set temporal operands
     *
     * @param temporalOperands temporal operands
     */
    public void setTemporalOperands(Collection<QName> temporalOperands) {
        this.temporalOperands.clear();
        if (temporalOperands != null) {
            this.temporalOperands.addAll(temporalOperands);
        }
    }

    /**
     * Get temporal operators
     *
     * @return temporal operators
     */
    public SortedMap<TimeOperator, SortedSet<QName>> getTempporalOperators() {
        return Collections.unmodifiableSortedMap(temporalOperators);
    }

    /**
     * Set temporal operators
     *
     * @param temporalOperators temporal operators
     */
    public void setTempporalOperators(Map<TimeOperator, ? extends Collection<QName>> temporalOperators) {
        this.temporalOperators.clear();
        if (temporalOperators != null) {
            for (TimeOperator timeOperator : temporalOperators.keySet()) {
                final TreeSet<QName> set = new TreeSet<QName>(QNameComparator.INSTANCE);
                if (temporalOperators.get(timeOperator) != null) {
                    set.addAll(temporalOperators.get(timeOperator));
                }
                this.temporalOperators.put(timeOperator, set);
            }
        }
    }

    /**
     * Get comparison operators
     *
     * @return comparison operators
     */
    public SortedSet<ComparisonOperator> getComparisonOperators() {
        return Collections.unmodifiableSortedSet(comparisonOperators);
    }

    /**
     * Set comparison operators
     *
     * @param comparisonOperators comparison operators
     */
    public void setComparisonOperators(Collection<ComparisonOperator> comparisonOperators) {
        this.comparisonOperators.clear();
        if (comparisonOperators != null) {
            this.comparisonOperators.addAll(comparisonOperators);
        }
    }
}
