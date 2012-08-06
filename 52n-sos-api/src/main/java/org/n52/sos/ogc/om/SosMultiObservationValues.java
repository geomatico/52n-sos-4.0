package org.n52.sos.ogc.om;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.quality.SosQuality;

public class SosMultiObservationValues implements IObservationValue<Map<ITime, Map<String, String>>> {
    
    private Map<ITime, Map<String, String>> values;

    @Override
    public ITime getPhenomenonTime() {
        return null;
    }
    
    @Override
    public Map<ITime, Map<String, String>> getValue() {
        return values;
    }

    @Override
    public void setValues(Map<ITime, Map<String, String>> value) {
        this.values = value;
    }
    
    public void addValue(ITime time, String observedProperty, String value) {
        if (values.containsKey(time)) {
            Map<String, String> obsPropValue = values.get(time);
            obsPropValue.put(observedProperty, value);
        } else {
            Map<String, String> obsPropValue = new HashMap<String, String>();
            obsPropValue.put(observedProperty, value);
            values.put(time, obsPropValue);
        }
    }

}
