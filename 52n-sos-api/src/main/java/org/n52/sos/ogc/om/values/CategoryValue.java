package org.n52.sos.ogc.om.values;

public class CategoryValue implements IValue<String> {

    private String value;
    
    private String unit;

    public CategoryValue(String value) {
       this.value = value;
    }

    @Override
    public void setValue(String value) {
       this.value = value;
    }

    @Override
    public String getValue() {
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
