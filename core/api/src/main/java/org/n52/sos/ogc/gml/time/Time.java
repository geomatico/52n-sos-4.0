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
package org.n52.sos.ogc.gml.time;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.sos.ogc.sos.SosConstants.IndeterminateTime;

/**
 * Interface for time objects
 * 
 */
public abstract class Time implements Comparable<Time>, Serializable {
    
    private static final long serialVersionUID = 1366100818431254519L;

    private String gmlId;
    
    private String indeterminateValue;
    
    private TimeFormat timeFormat = TimeFormat.NOT_SET;

    public Time() {
        this(null, null);
    }

    public Time(String gmlId) {
        this(gmlId, null);
    }

    public Time(String gmlId, String indeterminateValue) {
        this.gmlId = gmlId;
        this.indeterminateValue = indeterminateValue;
    }

    public void setGmlId(String gmlId) {
        this.gmlId = gmlId;
    }

    public String getGmlId() {
        if (this.gmlId != null) {
            return this.gmlId.replaceFirst("#", "");
        }
        return this.gmlId;
    }

    /**
     * Get indeterminate value
     * 
     * @return Returns the indeterminateValue.
     */
    public String getIndeterminateValue() {
        return indeterminateValue;
    }

    /**
     * Set indeterminate value
     * 
     * @param indeterminateValue
     *            The indeterminateValue to set.
     */
    public void setIndeterminateValue(String indeterminateValue) {
        this.indeterminateValue = indeterminateValue;
    }
    
    public boolean isSetGmlId() {
        return getGmlId() != null && !getGmlId().isEmpty();
    }
    
    public boolean isSetIndeterminateValue(){
        return getIndeterminateValue() != null && !getIndeterminateValue().isEmpty();
    }
    
    public boolean isReferenced() {
        return isSetGmlId() && this.gmlId.startsWith("#");
    }
    
    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public enum TimeFormat {
        ISO8601, YMD, YM, Y, NOT_SET
    }
    
    protected DateTime resolveDateTime(DateTime dateTime, String indeterminateValue) {
        if (dateTime != null) {
            return dateTime;
        }
        if (indeterminateValue != null && IndeterminateTime.valueOf(indeterminateValue)
                .equals(IndeterminateTime.now)) {
            return new DateTime(DateTimeZone.UTC);
        }
        return null;
    }
}
