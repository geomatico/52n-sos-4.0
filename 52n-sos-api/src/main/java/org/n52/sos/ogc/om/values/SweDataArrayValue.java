package org.n52.sos.ogc.om.values;

import java.util.HashMap;
import java.util.Map;

import org.n52.sos.ogc.gml.time.ITime;

public class SweDataArrayValue implements IValue<Map<ITime, Map<String, IValue>>> {
    
    private Map<ITime, Map<String, IValue>> values;
    
    private String unit;

    @Override
    public void setValue(Map<ITime, Map<String, IValue>> value) {
        this.values = value;
    }

    @Override
    public Map<ITime, Map<String, IValue>> getValue() {
        return values;
    }

    @Override
    public void setUnit(String unit) {
        // do nothing
    }

    @Override
    public String getUnit() {
        return unit;
    }
    
    public void addValue(ITime time, String observedProperty, IValue value) {
        if (values == null) {
            values = new HashMap<ITime, Map<String,IValue>>(0);
        }
        if (values.containsKey(time)) {
            Map<String, IValue> obsPropValue = values.get(time);
            obsPropValue.put(observedProperty, value);
        } else {
            Map<String, IValue> obsPropValue = new HashMap<String, IValue>();
            obsPropValue.put(observedProperty, value);
            values.put(time, obsPropValue);
        }
    }

}
