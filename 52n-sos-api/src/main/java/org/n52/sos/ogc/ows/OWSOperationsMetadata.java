package org.n52.sos.ogc.ows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OWSOperationsMetadata {

    private Collection<OWSOperation> operations;

    private Map<String, List<IOWSParameterValue>> commonValues;

    public Collection<OWSOperation> getOperations() {
        return operations;
    }

    public void setOperations(Collection<OWSOperation> operations) {
        this.operations = operations;
    }

    public Map<String, List<IOWSParameterValue>> getCommonValues() {
        return commonValues;
    }

    public void setCommonValues(Map<String, List<IOWSParameterValue>> commonValues) {
        this.commonValues = commonValues;
    }

    public void addOperation(OWSOperation operation) {
        if (operations == null) {
            operations = new ArrayList<OWSOperation>();
        }
        operations.add(operation);
    }

    public void addCommonValue(String parameterName, IOWSParameterValue value) {
        if (commonValues == null) {
            commonValues = new HashMap<String, List<IOWSParameterValue>>();
        }
        if (!commonValues.containsKey(parameterName)) {
            List<IOWSParameterValue> list = new ArrayList<IOWSParameterValue>(1);
            list.add(value);
            commonValues.put(parameterName, list);
        } else {
            commonValues.get(parameterName).add(value);
        }
    }
}
