package org.n52.sos.ogc.om.values;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryValue implements IValue<Geometry> {
    
    private Geometry value;
    
    private String unit;
    
    public GeometryValue(Geometry value) {
        this.value = value;
    }

    @Override
    public void setValue(Geometry value) {
        this.value = value;
    }

    @Override
    public Geometry getValue() {
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
