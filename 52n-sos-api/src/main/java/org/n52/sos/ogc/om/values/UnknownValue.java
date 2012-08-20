package org.n52.sos.ogc.om.values;

public class UnknownValue implements IValue<Object> {
    
    private Object value;
    
    private String unit;

    public UnknownValue(Object value) {
        this.value = value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String getUnit() {
        return unit;
    }

}
