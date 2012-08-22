/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.gml.time;

import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

/**
 * Class represents a GML conform timePeriod element.
 * 
 */
public class TimePeriod implements ITime {
    
    private String id;

    /** start Date of timePeriod */
    private DateTime start;

    /** indeterminate position of startPosition */
    private String startIndet;

    /** end Date of timePeriod */
    private DateTime end;

    /** indeterminate position of endPosition */
    private String endIndet;

    /** duration value */
    private Period duration = null; // ISO8601 format

    /** intervall value */
    private String intervall = null; // ISO8601 format

    /** indeterminate position of timeInstant */
    private String indeterminateValue;

    /**
     * default constructor
     * 
     */
    public TimePeriod() {
    }

    /**
     * constructor with start and end date as parameters
     * 
     * @param start
     *            start date of the time period
     * @param end
     *            end date of the timeperiod
     */
    public TimePeriod(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
    }

    public TimePeriod(DateTime start, DateTime end, String id) {
        this.start = start;
        this.end = end;
        this.id = id;
    }

    /**
     * standard constructor
     * 
     * @param start
     *            timeString of startposition in ISO8601 format
     * @param startIndet
     *            indeterminate time position of start
     * @param end
     *            timeString of endposition in ISO8601 format
     * @param endIndet
     *            indeterminate time value of end position
     * @param duration
     *            duration in ISO8601 format
     * @throws ParseException
     *             if parsing the time strings of start or end into
     *             java.util.Date failed
     */
    public TimePeriod(DateTime start, String startIndet, DateTime end, String endIndet, String duration, String id)
            throws ParseException {
        this.start = start;
        this.startIndet = startIndet;
        this.end = end;
        this.endIndet = endIndet;
        this.duration = ISOPeriodFormat.standard().parsePeriod(duration);
        this.id = id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }
    /**
     * Get duration
     * 
     * @return Returns the duration.
     */
    public Period getDuration() {
        return duration;
    }

    /**
     * Set duration
     * 
     * @param duration
     *            The duration to set.
     */
    public void setDuration(Period duration) {
        this.duration = duration;
    }

    /**
     * Get start time
     * 
     * @return Returns the start.
     */
    public DateTime getStart() {
        return start;
    }

    /**
     * Set start time
     * 
     * @param start
     *            The start to set.
     */
    public void setStart(DateTime start) {
        this.start = start;
    }

    /**
     * Get end time
     * 
     * @return Returns the end.
     */
    public DateTime getEnd() {
        return end;
    }

    /**
     * Set end time
     * 
     * @param end
     *            The end to set.
     */
    public void setEnd(DateTime end) {
        this.end = end;
    }

    /**
     * Get start indet time
     * 
     * @return Returns the startIndet.
     */
    public String getStartIndet() {
        return startIndet;
    }

    /**
     * Set start indet time
     * 
     * @param startIndet
     *            The startIndet to set.
     */
    public void setStartIndet(String startIndet) {
        this.startIndet = startIndet;
    }

    /**
     * Get end indet time
     * 
     * @return Returns the endIndet.
     */
    public String getEndIndet() {
        return endIndet;
    }

    /**
     * Set end indet time
     * 
     * @param endIndet
     *            The endIndet to set.
     */
    public void setEndIndet(String endIndet) {
        this.endIndet = endIndet;
    }

    /**
     * Get intervall
     * 
     * @return
     */
    public String getIntervall() {
        return this.intervall;
    }

    /**
     * Set intervall
     * 
     * @param intervall
     */
    public void setIntervall(String intervall) {
        this.intervall = intervall;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.ogc.gml.time.ISosTime#getIndeterminateValue()
     */
    @Override
    public String getIndeterminateValue() {
        return indeterminateValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.ogc.gml.time.ISosTime#setIndeterminateValue(java.lang.String)
     */
    @Override
    public void setIndeterminateValue(String indeterminateValue) {
        this.indeterminateValue = indeterminateValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String result = "Time period: ";
        if (start != null) {
            result += start.toString() + ", ";
        }
        result += startIndet + ", ";
        if (end != null) {
            result += end.toString() + ", ";
        }
        result += endIndet;
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ITime o) {
        if (o instanceof TimeInstant) {
            TimeInstant ti = (TimeInstant) o;
            if (end.isBefore(ti.getValue())) {
                return -1;
            } else if (start.isAfter(ti.getValue())) {
                return 1;
            }
        } else if (o instanceof TimePeriod) {
            // TODO: CHECK if TimerPeriod for PhenTime is supported
            TimePeriod tp = (TimePeriod) o;
            if (start.isBefore(tp.getStart())) {
                return -1;
            } else if (end.isAfter(tp.getEnd())) {
                return 1;
            }
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object paramObject) {
        if (start != null && end != null && paramObject instanceof TimePeriod) {
            TimePeriod tp = (TimePeriod) paramObject;
            return (start.isEqual(tp.start) && end.isEqual(tp.end));
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + start.hashCode();
        hash = 31 * hash + end.hashCode();
        return hash;
    }
}
