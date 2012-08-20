package org.n52.sos.ogc.om.values;

public class QuantityValue implements IValue<Double> {

    private Double value;
    
    private String unit;

    public QuantityValue(Double value) {
       this.value = value;
    }

    @Override
    public void setValue(Double value) {
       this.value = value;
    }

    @Override
    public Double getValue() {
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
