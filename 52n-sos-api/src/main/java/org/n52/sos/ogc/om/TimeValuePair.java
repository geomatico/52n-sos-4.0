package org.n52.sos.ogc.om;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.values.IValue;

public class TimeValuePair {
    
    private ITime time;
    
    private IValue value;

    public TimeValuePair(ITime time, IValue value) {
        this.time = time;
        this.value = value;
    }

    public ITime getTime() {
        return time;
    }

    public IValue getValue() {
        return value;
    }

    public void setTime(ITime time) {
        this.time = time;
    }

    public void setValue(IValue value) {
        this.value = value;
    }

}
