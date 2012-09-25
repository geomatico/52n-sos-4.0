package org.n52.sos.ogc.om.values;

import java.io.Serializable;

public interface IValue<T> extends Serializable{
    
    public void setValue(T value);
    
    public T getValue();
    
    public void setUnit(String unit);
    
    public String getUnit();

}
