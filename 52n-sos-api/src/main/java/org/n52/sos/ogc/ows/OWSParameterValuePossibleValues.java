package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.HashSet;

public class OWSParameterValuePossibleValues implements IOWSParameterValue {
    
    private Collection<String> values;

    public OWSParameterValuePossibleValues(Collection<String> values) {
        super();
        this.values = values;
    }

    public OWSParameterValuePossibleValues(String valueString) {
        if (values == null) {
            values = new HashSet<String>();
        }
        values.add(valueString);
    }

    public Collection<String> getValues() {
        return values;
    }

    public void setValues(Collection<String> values) {
        this.values = values;
    }

}
