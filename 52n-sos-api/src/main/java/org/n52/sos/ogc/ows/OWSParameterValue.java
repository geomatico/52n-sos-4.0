package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.HashSet;

public class OWSParameterValue implements IOWSParameterValue {
    
    private Collection<String> values;

    public OWSParameterValue(Collection<String> values) {
        super();
        this.values = values;
    }

    public OWSParameterValue(String valueString) {
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
