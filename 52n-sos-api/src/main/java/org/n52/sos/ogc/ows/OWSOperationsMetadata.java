package org.n52.sos.ogc.ows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OWSOperationsMetadata {

    private Collection<OWSOperation> operations;

    private Map<String, Set<String>> commonValues;

    public Collection<OWSOperation> getOperations() {
        return operations;
    }

    public void setOperations(Collection<OWSOperation> operations) {
        this.operations = operations;
    }

    public Map<String, Set<String>> getCommonValues() {
        return commonValues;
    }

    public void setCommonValues(Map<String, Set<String>> commonValues) {
        this.commonValues = commonValues;
    }

    public void addOperation(OWSOperation operation) {
        if (operations == null) {
            operations = new ArrayList<OWSOperation>();
        }
        operations.add(operation);
    }

    public void addCommonValues(String name, Set<String> values) {
        if (commonValues == null) {
            commonValues = new HashMap<String, Set<String>>();
        }
        if (commonValues.containsKey(name)) {
            commonValues.get(name).addAll(values);
        } else {
            commonValues.put(name, values);
        }

    }

    public void addCommonValue(String name, String value) {
        if (commonValues == null) {
            commonValues = new HashMap<String, Set<String>>();
        }
        if (commonValues.containsKey(name)) {
            commonValues.get(name).add(value);
        } else {
            Set<String> values = new HashSet<String>();
            values.add(value);
            commonValues.put(name, values);
        }

    }

}
