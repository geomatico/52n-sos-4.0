package org.n52.sos.ogc.om;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;

public class SosMultiObservationValues implements IObservationValue {

    private IValue values;

    private ITime phenomenonTime;

    @Override
    public ITime getPhenomenonTime() {
        if (phenomenonTime == null && values instanceof SweDataArrayValue) {
            SweDataArrayValue dataArrayValue = (SweDataArrayValue) values;
            dataArrayValue.getValue().keySet();
            DateTime start = null;
            DateTime end = null;
            for (ITime time : dataArrayValue.getValue().keySet()) {
                if (time instanceof TimeInstant) {
                    TimeInstant ti = (TimeInstant) time;
                    if (start == null || ti.getValue().isBefore(start)) {
                        start = ti.getValue();
                    }
                    if (end == null || ti.getValue().isAfter(end)) {
                        end = ti.getValue();
                    }
                } else if (time instanceof TimePeriod) {
                    TimePeriod tp = (TimePeriod) time;
                    if (start == null || tp.getStart().isBefore(start)) {
                        start = tp.getStart();
                    }
                    if (end == null || tp.getEnd().isAfter(end)) {
                        end = tp.getEnd();
                    }
                }
            }
            if (start.isEqual(end)) {
               return new TimeInstant(start, null);
            } else {
               return new TimePeriod(start, end);
            }
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
