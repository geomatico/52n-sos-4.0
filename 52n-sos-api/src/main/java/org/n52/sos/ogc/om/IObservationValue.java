package org.n52.sos.ogc.om;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.values.IValue;

public interface IObservationValue {
    /** phenomenon or sampling time of the observation */
    public ITime getPhenomenonTime();
    
    public void setPhenomenonTime(ITime phenomenonTime);

    public IValue getValue();

    public void setValue(IValue value);

}