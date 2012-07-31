/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.ows.OWSConstants.MinMax;

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
    private Map<String, Collection<String>> parameterValues;
    
    /**
     * Map for min/max values, e. g. observation ids
     */
    private Map<String, Map<MinMax, String>> parameterminMaxMap;

    /**
     * Get operation name
     * @return operation name
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set operation name
     * @param operationName
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get DCP for operation
     * @return DCP map
     */
    public Map<String, List<String>> getDcp() {
        return dcp;
    }

    /**
     * Set DCP for operation
     * @param dcp DCP map
     */
    public void setDcp(Map<String, List<String>> dcp) {
        if (this.dcp == null) {
            this.dcp = dcp;
        } else {
            // ...
        }
    }

    /**
     * Add DCP for operation
     * @param operation Operation name
     * @param value DCP values
     */
    public void addDcp(String operation, List<String> values) {
        if (dcp == null) {
            dcp = new HashMap<String, List<String>>();
        }
        dcp.put(operation, values);
    }

    /**
     * Get parameter and value map
     * @return Parameter value map
     */
    public Map<String, Collection<String>> getParameterValues() {
        return parameterValues;
    }

    /**
     * Set parameter and value map
     * @param parameterValues Parameter value map
     */
    public void setParameterValues(Map<String, Collection<String>> parameterValues) {
        if (this.parameterValues == null) {
            this.parameterValues = parameterValues;
        } else {
            // TODO: ...
        }

    }

    /**
     * Add values for parameter
     * @param parameterName parameter name
     * @param allowedValues values to add
     */
    public void addParameterValue(String parameterName, Collection<String> allowedValues) {
        if (parameterValues == null) {
            parameterValues = new HashMap<String, Collection<String>>();
        }
        parameterValues.put(parameterName, allowedValues);
    }
    
    /**
     * Get min/max values map
     * @return min/max value map
     */
    public Map<String, Map<MinMax, String>> getParameterMinMaxMap() {
        return parameterminMaxMap;
    }

    /**
     * Set min/max value map
     * @param minMaxMap min/max value map
     */
    public void addParameterMinMaxMapValue(String parameter, Map<MinMax, String> minMaxMap) {
        if (parameterminMaxMap == null) {
            parameterminMaxMap = new HashMap<String, Map<MinMax,String>>();
        }
        if (parameterminMaxMap.containsKey(parameter)) {
            parameterminMaxMap.get(parameter).putAll(minMaxMap);
        } else {
            parameterminMaxMap.put(parameter, minMaxMap);
        }
    }

}
