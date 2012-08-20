package org.n52.sos.ogc.om.values;

public class CountValue implements IValue<Integer> {

    private Integer value;
    
    private String unit;

    public CountValue(Integer value) {
        this.value = value;
    }

    @Override
    public void setValue(Integer value) {
       this.value = value;
    }

    @Override
    public Integer getValue() {
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
