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

import org.joda.time.DateTime;

/**
 * Class represents a GML conform timeInstant element
 * 
 */
public class TimeInstant implements ITime {

	/** date for this timeInstant */
	private DateTime value;

	/** indeterminate position of timeInstant */
	private String indeterminateValue;

	/** length of timeInstant date */
	private int requestedTimeLength;

	/**
	 * constructor with date and indeterminateValue
	 * 
	 * @param value
	 *            date of the timeInstante
	 * @param indeterminateValue
	 */
	public TimeInstant(DateTime dateValue, String indeterminateValue) {
		this.value = dateValue;
		this.indeterminateValue = indeterminateValue;
	}

	/**
	 * default constructor
	 * 
	 */
	public TimeInstant() {
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

	/**
	 * Get time value
	 * 
	 * @return Returns the value.
	 */
	public DateTime getValue() {
		return value;
	}

	/**
	 * Set time value
	 * 
	 * @param value
	 *            The value to set.
	 */
	public void setValue(DateTime value) {
		this.value = value;
	}

	/**
	 * Get requested time length
	 * 
	 * @param requestedTimeLength
	 *            the requestedTimeLength to set
	 */
	public void setRequestedTimeLength(int requestedTimeLength) {
		this.requestedTimeLength = requestedTimeLength;
	}

	/**
	 * Set requested time length
	 * 
	 * @return the requestedTimeLength
	 */
	public int getRequestedTimeLength() {
		return requestedTimeLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (value != null) {
			String dateString;
			dateString = value.toString();
			return "Time instant: " + dateString + "," + indeterminateValue;
		} else {
			return "Time instant: " + indeterminateValue;
		}
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
			if (value.isBefore(ti.getValue())) {
				return -1;
			} else if (value.isAfter(ti.getValue())) {
				return 1;
			}
		} else if (o instanceof TimePeriod) {
			TimePeriod tp = (TimePeriod) o;
			if (value.isBefore(tp.getStart())) {
				return -1;
			} else if (value.isAfter(tp.getEnd())) {
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
		if (value != null && paramObject instanceof TimeInstant) {
			return value.isEqual(((TimeInstant) paramObject).getValue());
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
		hash = 31 * hash + value.hashCode();
		return hash;
	}

}
