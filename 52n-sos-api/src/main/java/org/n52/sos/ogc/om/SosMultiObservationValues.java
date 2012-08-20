package org.n52.sos.ogc.om;

import java.util.HashMap;
import java.util.Map;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;

public class SosMultiObservationValues implements IObservationValue {

    private IValue values;
    
    private ITime phenomenonTime;

    @Override
    public ITime getPhenomenonTime() {
        if (phenomenonTime == null && values instanceof SweDataArrayValue) {
            // TODO: get phenomenon time from value map
        }
        return phenomenonTime;
    }

    @Override
    public IValue getValue() {
        return values;
    }

    @Override
    public void setValue(IValue value) {
        this.values = value;
    }

    @Override
    public void setPhenomenonTime(ITime phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }

}
