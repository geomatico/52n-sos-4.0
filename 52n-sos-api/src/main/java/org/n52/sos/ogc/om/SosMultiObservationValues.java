/**
 * Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
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
