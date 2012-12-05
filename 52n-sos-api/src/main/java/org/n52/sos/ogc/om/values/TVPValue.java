package org.n52.sos.ogc.om.values;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.om.TimeValuePair;

public class TVPValue implements IValue<List<TimeValuePair>> {

    private List<TimeValuePair> values = new ArrayList<TimeValuePair>(0);
    
    private String unit;

    @Override
    public void setValue(List<TimeValuePair> value) {
        this.values = value;
    }

    @Override
    public List<TimeValuePair> getValue() {
        return values;
    }
    
    public void addValue(TimeValuePair value) {
        this.values.add(value);
    }
    
    public void addValues(List<TimeValuePair> values) {
        this.values.addAll(values);
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String getUnit() {
        return this.unit;
    }



}
