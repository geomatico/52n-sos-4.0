package org.n52.sos.ogc.om.values;

public interface IValue<T> {
    
    public void setValue(T value);
    
    public T getValue();
    
    public void setUnit(String unit);
    
    public String getUnit();

}
