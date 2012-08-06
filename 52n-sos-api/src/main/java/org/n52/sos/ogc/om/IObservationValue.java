package org.n52.sos.ogc.om;

import org.n52.sos.ogc.gml.time.ITime;

public interface IObservationValue<T> {
    
    /** phenomenon or sampling time of the observation */
    public ITime getPhenomenonTime();
    
    public T getValue();
    
    public void setValues(T value);
    
}