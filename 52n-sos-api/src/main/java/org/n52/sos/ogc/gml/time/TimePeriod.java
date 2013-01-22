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
package org.n52.sos.ogc.gml.time;

import java.text.ParseException;
import java.util.Collection;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class represents a GML conform timePeriod element.
 * 
 */
public class TimePeriod extends ITime {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TimePeriod.class);
    
    private static final long serialVersionUID = 1L;

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
        super(id);
        this.start = start;
        this.end = end;
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
        super(id);
        this.start = start;
        this.startIndet = startIndet;
        this.end = end;
        this.endIndet = endIndet;
        this.duration = ISOPeriodFormat.standard().parsePeriod(duration);
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

    /**
     * Extend TimePeriod to contain Collection<ISosTime>
     * 
     * @param sosTime
     */
    public void extendToContain( Collection<ITime> times ){
        for( ITime time : times ){
            extendToContain( time );
        }
    }
    
    /**
     * Extend TimePeriod to contain ISosTime
     * 
     * @param sosTime
     */
    public void extendToContain( ITime time ){
        if( time instanceof TimeInstant ){
            extendToContain( (TimeInstant) time );
        } else if( time instanceof TimePeriod ){
            extendToContain( (TimePeriod) time );
        } else {
        	String errorMsg = String.format("Received ITime type \"%s\" unknown.", time!=null?time.getClass().getName():time);
        	LOGGER.error(errorMsg);
        	throw new IllegalArgumentException(errorMsg);
        }
    }
    
    /**
     * Extend TimePeriod to contain another TimePeriod
     * 
     * @param period
     */
    public void extendToContain( TimePeriod period ){
        extendToContain( period.getStart() );
        extendToContain( period.getEnd() );
    }    

    /**
     * Extend TimePeriod to contain TimeInstant
     * @param instant
     */
    public void extendToContain( TimeInstant instant ){
        if( instant != null ){
            extendToContain( instant.getValue() );
        }
    }
    
    /**
     * Extend TimePeriod to contain DateTime. Used by other extendToContain methods.
     * 
     * @param time
     */
    public void extendToContain( DateTime time ){
        if( time != null ){
            if( isSetStart() || time.isBefore( start ) ){
                start = time;
            }
            if( isSetEnd() || time.isAfter( end ) ){
                end = time;
            }
        }
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

    @Override
    public boolean equals(Object paramObject) {
        if (start != null && end != null && paramObject instanceof TimePeriod) {
            TimePeriod tp = (TimePeriod) paramObject;
            return (start.isEqual(tp.start) && end.isEqual(tp.end));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + start.hashCode();
        hash = 31 * hash + end.hashCode();
        return hash;
    }
    
    /**
     * @return <tt>true</tt>, if start and end are NOT set
     * @see #isSetStart()
     * @see #isSetEnd()
     */
    public boolean isEmpty()
    {
    	return !isSetEnd() && !isSetStart();
    }

    /**
     * @return <tt>true</tt>, if start is set
     * @see #isEmpty()
     * @see #isSetEnd()
     */
    public boolean isSetStart()
	{
		return start != null;
	}

    /**
     * @return <tt>true</tt>, if end is set
     * @see #isSetStart()
     * @see #isEmpty()
     */
    public boolean isSetEnd()
	{
		return end != null;
	}
    
}
