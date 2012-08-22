package org.n52.sos.ogc.ows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class OWSOperationsMetadata {

    private Collection<OWSOperation> operations;

    private Map<String, OWSParameterValue> commonValues;

    public Collection<OWSOperation> getOperations() {
        return operations;
    }

    public void setOperations(Collection<OWSOperation> operations) {
        this.operations = operations;
    }

    public Map<String, OWSParameterValue> getCommonValues() {
        return commonValues;
    }

    public void setCommonValues(Map<String, OWSParameterValue> commonValues) {
        this.commonValues = commonValues;
    }

    public void addOperation(OWSOperation operation) {
        if (operations == null) {
            operations = new ArrayList<OWSOperation>();
        }
        operations.add(operation);
    }

    public void addCommonValue(String name, OWSParameterValue value) {
        if (commonValues == null) {
            commonValues = new HashMap<String, OWSParameterValue>();
        }
        commonValues.put(name, value);
    }
}
