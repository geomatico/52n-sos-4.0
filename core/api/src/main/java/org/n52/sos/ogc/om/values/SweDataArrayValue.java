/**
 * Copyright (C) 2013
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
package org.n52.sos.ogc.om.values;

import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.simpleType.SweTime;
import org.n52.sos.exception.ows.concrete.DateTimeParseException;
import org.n52.sos.util.DateTimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweDataArrayValue implements IMultiValue<SweDataArray> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SweDataArrayValue.class);
    private static final long serialVersionUID = 3022136042762771037L;
    
    private SweDataArray value;
    
    @Override
    public void setValue(SweDataArray value) {
        this.value = value;
    }

    @Override
    public SweDataArray getValue() {
        return value;
    }

    @Override
    public void setUnit(String unit) {
        // do nothing
    }

    @Override
    public String getUnit() {
        return null;
    }

    /**
     * Adds the given block - a {@link List}<{@link String}> - add the end of
     * the current list of blocks
     * 
     * @param blockOfTokensToAddAtTheEnd
     * @return <tt>true</tt> (as specified by {@link Collection#add}) <br />
     *         <tt>false</tt> if block could not be added
     */
    public boolean addBlock(List<String> blockOfTokensToAddAtTheEnd) {
        if (value != null) {
            return value.add(blockOfTokensToAddAtTheEnd);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("SweDataArrayValue [value=%s, unit=null]", value);
    }

    @Override
    public Time getPhenomenonTime() {
        TimePeriod timePeriod = new TimePeriod();
        int dateTokenIndex = -1;
        if (value != null && value.getElementType() != null && value.getEncoding() != null) {
            // get index of time token from elementtype
            if (value.getElementType() instanceof SweDataRecord) {
                SweDataRecord elementType = (SweDataRecord) value.getElementType();
                List<SweField> fields = elementType.getFields();
                for (int i = 0; i < fields.size(); i++) {
                    SweField sweField = fields.get(i);
                    if (sweField.getElement() instanceof SweTime) {
                        dateTokenIndex = i;
                        break;
                    }
                }

            }
            if (dateTokenIndex > -1) {
                for (List<String> block : value.getValues()) {
                    // check for "/" to identify time periods (Is
                    // conform with ISO8601 (see WP))
                    // datetimehelper to DateTime from joda time
                    String token = block.get(dateTokenIndex);
                    Time time = null;
                    try {
                        if (token.contains("/")) {
                            String[] subTokens = token.split("/");
                            time =
                                    new TimePeriod(DateTimeHelper.parseIsoString2DateTime(subTokens[0]),
                                            DateTimeHelper.parseIsoString2DateTime(subTokens[1]));
                        } else {
                            time = new TimeInstant(DateTimeHelper.parseIsoString2DateTime(token));
                        }
                    } catch (DateTimeParseException dte) {
                        LOGGER.error(String.format("Could not parse ISO8601 string \"%s\"", token), dte);
                        // TODO throw exception here?
                        continue; // try next block;
                    }
                    timePeriod.extendToContain(time);
                }
            } else {
                String errorMsg = "PhenomenonTime field could not be found in ElementType";
                LOGGER.error(errorMsg);
            }
        } else {
            String errorMsg =
                    String.format("Value of type \"%s\" not set correct.", SweDataArrayValue.class.getName());
            LOGGER.error(errorMsg);
        }
        return timePeriod;
    }

    @Override
    public boolean isSetValue() {
        return value != null && value.isEmpty();
    }

    @Override
    public boolean isSetUnit() {
        return false;
    }

}
