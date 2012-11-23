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

import java.util.List;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosMultiObservationValues<T> implements IObservationValue<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosMultiObservationValues.class);

    private static final long serialVersionUID = 1L;

    private IValue<T> values;

    private ITime phenomenonTime;

    @Override
    public ITime getPhenomenonTime()
    {
        if (phenomenonTime == null && values != null && values instanceof SweDataArrayValue) {
            SweDataArrayValue dataArrayValue = (SweDataArrayValue) values;
            SosSweDataArray dataArray = dataArrayValue.getValue();
            DateTime start = null;
            DateTime end = null;
            int dateTokenIndex = -1;
            // TODO Eike: implement
            if (dataArray != null && dataArray.getElementType() != null && dataArray.getEncoding() != null) {
                // get index of time token from elementtype
                if (dataArray.getElementType() instanceof SosSweDataRecord) {
                    SosSweDataRecord elementType = (SosSweDataRecord) dataArray.getElementType();
                    List<SosSweField> fields = elementType.getFields();
                    for (int i = 0; i < fields.size(); i++) {
                        SosSweField sweField = fields.get(i);
                        if (sweField.getElement() instanceof SosSweTime) {
                            dateTokenIndex = i;
                            break;
                        }
                    }

                }
            } else {
                String errorMsg = String.format("Value of type \"%s\" not set correct.", SweDataArrayValue.class.getName());
                LOGGER.error(errorMsg);
            }
            if (dateTokenIndex > -1) {
                for (List<String> block : dataArray.getValues()) {
                    // check for "/" to identify time periods (Is conform with
                    // ISO
                    // 8601 (see WP))
                    // datetimehelper to DateTime from joda time
                    String token = block.get(dateTokenIndex);
                    ITime time = null;
                    try {
                        if (token.contains("/")) {
                            String[] subTokens = token.split("/");
                            time = new TimePeriod(DateTimeHelper.parseIsoString2DateTime(subTokens[0]), DateTimeHelper.parseIsoString2DateTime(subTokens[1]));
                        } else {
                            time = new TimeInstant(DateTimeHelper.parseIsoString2DateTime(token));
                        }
                    } catch (DateTimeException dte) {
                        String exceptionMsg = String.format("Could not parse ISO8601 string \"%s\". Exception thrown: \"%s\"; Message: \"%s\"", token, dte.getClass().getName(), dte.getMessage());
                        LOGGER.error(exceptionMsg);
                        // TODO throw exception here?
                        continue; // try next block;
                    }
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
            } else {
                String errorMsg = "PhenomenonTime field could not be found in ElementType";
                LOGGER.error(errorMsg);
            }
            if (start.isEqual(end)) {
                return new TimeInstant(start);
            } else {
                return new TimePeriod(start, end);
            }
        }
        return phenomenonTime;
    }

    @Override
    public IValue<T> getValue()
    {
        return values;
    }

    @Override
    public void setValue(IValue<T> value)
    {
        this.values = value;
    }

    @Override
    public void setPhenomenonTime(ITime phenomenonTime)
    {
        this.phenomenonTime = phenomenonTime;
    }

}
