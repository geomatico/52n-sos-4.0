package org.n52.sos.ogc.om.values;

public class BooleanValue implements IValue<Boolean> {

    private Boolean value;
    
    private String unit;

    public BooleanValue(Boolean value) {
        this.value = value;
    }

    @Override
    public void setValue(Boolean value) {
       this.value = value;
    }

    @Override
    public Boolean getValue() {
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
