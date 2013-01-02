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
package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.n52.sos.ogc.sos.SosConstants;

/**
 * Class represents a OperationMetadata. Used in SosCapabilities.
 * 
 * 
 */
public class OWSOperation {

    /**
     * Name of the operation which metadata are represented.
     */
    private String operationName;

    /**
     * Supported DCPs
     */
    private Map<String, List<String>> dcp;

    /**
     * Map with names and allowed values for the parameter.
     */
    private Map<String, List<IOWSParameterValue>> parameterValues;

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
    public Map<String, List<String>> getDcp() {
        return dcp;
    }

    /**
     * Set DCP for operation
     * 
     * @param dcp
     *            DCP map
     */
    public void setDcp(Map<String, List<String>> dcp) {
        this.dcp = dcp;
    }

    /**
     * Add DCP for operation
     * 
     * @param operation
     *            Operation name
     * @param values
     *            DCP values
     */
    public void addDcp(String operation, List<String> values) {
        if (dcp == null) {
            dcp = new HashMap<String, List<String>>(1);
        }
        dcp.put(operation, values);
    }

    /**
     * Get parameter and value map
     * 
     * @return Parameter value map
     */
    public Map<String, List<IOWSParameterValue>> getParameterValues() {
        return parameterValues;
    }

    /**
     * Set parameter and value map
     * 
     * @param parameterValuesrValues
     *            Parameter value map
     */
    public void setParameterValues(Map<String, List<IOWSParameterValue>> parameterValues) {
        this.parameterValues = parameterValues;
    }

    /**
     * Add values for parameter
     * 
     * @param parameterName
     *            parameter name
     * @param allowedValues
     *            values to add
     */
    public void addParameterValue(String parameterName, IOWSParameterValue value) {
        if (parameterValues == null) {
            parameterValues = new HashMap<String, List<IOWSParameterValue>>();
        }
        List<IOWSParameterValue> list = parameterValues.get(parameterName);
        if (list == null) {
            parameterValues.put(parameterName, list = new LinkedList<IOWSParameterValue>());
        }
        list.add(value);
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
        addParameterValue(parameterName, new OWSParameterValuePossibleValues(values));
    }

    public void addPossibleValuesParameter(String parameterName, String value) {
        addParameterValue(parameterName, new OWSParameterValuePossibleValues(value));
    }
    
    public void addAnyParameterValue(String paramterName) {
        addPossibleValuesParameter(paramterName, Collections.<String>emptyList());
    }
    
    public  <E extends Enum<E>> void addAnyParameterValue(E parameterName) {
        addAnyParameterValue(parameterName.name());
    }
    
    public void addAnyParameterListValue(String paramterName) {
        addPossibleValuesParameter(paramterName, Collections.singletonList(SosConstants.PARAMETER_ANY));
    }
    
    public <E extends Enum<E>> void addAnyParameterListValue(E parameterName) {
        addAnyParameterListValue(parameterName.name());
    }

    public void addRangeParameterValue(String parameterName, String min, String max) {
        addParameterValue(parameterName, new OWSParameterValueRange(min, max));
    }

    public <E extends Enum<E>> void addRangeParameterValue(E parameterName, String min, String max) {
        addRangeParameterValue(parameterName.name(), min, max);
    }
    
    public void addRangeParameterValue(String parameterName, Map<OWSConstants.MinMax,String> minMax) {
        addParameterValue(parameterName, new OWSParameterValueRange(minMax));
    }

    public <E extends Enum<E>> void addRangeParameterValue(E parameterName, Map<OWSConstants.MinMax,String> minMax) {
        addRangeParameterValue(parameterName.name(), minMax);
    }
    
    public void addDataTypeParameter(String parameterName, String value) {
        addParameterValue(parameterName, new OWSParameterDataType(value));
    }

    public <E extends Enum<E>> void addDataTypeParameter(E parameterName, String value) {
        addDataTypeParameter(parameterName.name(), value);
    }
}
