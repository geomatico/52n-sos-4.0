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
package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.n52.sos.util.MinMax;

/**
 * Class represents a OperationMetadata. Used in SosCapabilities.
 * 
 * 
 */
public class OwsOperation implements Comparable<OwsOperation> {

    /**
     * Name of the operation which metadata are represented.
     */
    private String operationName;

    /**
     * Supported DCPs
     */
    private SortedMap<String, Set<DCP>> dcp = new TreeMap<String, Set<DCP>>();

    /**
     * Map with names and allowed values for the parameter.
     */
    private SortedMap<String, List<IOWSParameterValue>> parameterValues =
                                                        new TreeMap<String, List<IOWSParameterValue>>();

    /**
     * Get operation name
     * 
     * @return operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set operation name
     * 
     * @param operationName
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get DCP for operation
     * 
     * @return DCP map
     */
    public SortedMap<String, Set<DCP>> getDcp() {
        return Collections.unmodifiableSortedMap(this.dcp);
    }

    /**
     * Set DCP for operation
     * 
     * @param dcp
     *            DCP map
     */
    public void setDcp(Map<String, ? extends Collection<DCP>> dcp) {
        this.dcp.clear();
        for (Entry<String, ? extends Collection<DCP>> e : dcp.entrySet()) {
            addDcp(e.getKey(), e.getValue());
        }
    }

    /**
     * Add DCP for operation
     * 
     * @param operation
     *            Operation name
     * @param values
     *            DCP values
     */
    public void addDcp(String operation, Collection<DCP> values) {
        this.dcp.put(operation, new HashSet<DCP>(values));
    }

    /**
     * Get parameter and value map
     * 
     * @return Parameter value map
     */
    public SortedMap<String, List<IOWSParameterValue>> getParameterValues() {
        return Collections.unmodifiableSortedMap(this.parameterValues);
    }

    /**
     * Set parameter and value map
     * 
     * @param parameterValues
     *            Parameter value map
     */
    public void setParameterValues(Map<String, List<IOWSParameterValue>> parameterValues) {
        this.parameterValues.clear();
        for (String parameterName : parameterValues.keySet()) {
            for (IOWSParameterValue value : parameterValues.get(parameterName)) {
                addParameterValue(parameterName, value);
            }
        }
    }

    /**
     * Add values for parameter
     * 
     * @param parameterName
     *            parameter name
     * @param value
     *            values to add
     */
    public void addParameterValue(String parameterName, IOWSParameterValue value) {
        List<IOWSParameterValue> values = parameterValues.get(parameterName);
        if (values == null) {
            parameterValues.put(parameterName, values = new LinkedList<IOWSParameterValue>());
        }
        values.add(value);
    }

    public <E extends Enum<E>> void addParameterValue(E parameterName, IOWSParameterValue value) {
        addParameterValue(parameterName.name(), value);
    }

    public <E extends Enum<E>> void addPossibleValuesParameter(E parameterName, Collection<String> values) {
        addPossibleValuesParameter(parameterName.name(), values);
    }

    public <E extends Enum<E>> void addPossibleValuesParameter(E parameterName, String value) {
        addPossibleValuesParameter(parameterName.name(), value);
    }

    public void addPossibleValuesParameter(String parameterName, Collection<String> values) {
        addParameterValue(parameterName, new OwsParameterValuePossibleValues(values));
    }

    public void addPossibleValuesParameter(String parameterName, String value) {
        addParameterValue(parameterName, new OwsParameterValuePossibleValues(value));
    }

    public void addAnyParameterValue(String paramterName) {
        addPossibleValuesParameter(paramterName, Collections.<String> emptyList());
    }

    public <E extends Enum<E>> void addAnyParameterValue(E parameterName) {
        addAnyParameterValue(parameterName.name());
    }

    public void addRangeParameterValue(String parameterName, String min, String max) {
        addParameterValue(parameterName, new OwsParameterValueRange(min, max));
    }

    public <E extends Enum<E>> void addRangeParameterValue(E parameterName, String min, String max) {
        addRangeParameterValue(parameterName.name(), min, max);
    }

    public void addRangeParameterValue(String parameterName, MinMax<String> minMax) {
        addRangeParameterValue(parameterName, minMax.getMinimum(), minMax.getMaximum());
    }

    public <E extends Enum<E>> void addRangeParameterValue(E parameterName, MinMax<String> minMax) {
        addRangeParameterValue(parameterName.name(), minMax);
    }

    public void addDataTypeParameter(String parameterName, String value) {
        addParameterValue(parameterName, new OwsParameterDataType(value));
    }

    public <E extends Enum<E>> void addDataTypeParameter(E parameterName, String value) {
        addDataTypeParameter(parameterName.name(), value);
    }

    public <E extends Enum<E>> void addRangeParameterValue(E parameterName, OwsParameterValueRange value) {
        addParameterValue(parameterName.name(), value);
    }

    @Override
    public int compareTo(OwsOperation o) {
        return getOperationName().compareTo(o.getOperationName());
    }
}
