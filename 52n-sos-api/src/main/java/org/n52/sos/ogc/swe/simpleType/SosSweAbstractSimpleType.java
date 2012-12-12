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
package org.n52.sos.ogc.swe.simpleType;

import org.n52.sos.ogc.swe.SWEConstants.SweSimpleType;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;

/**
 * Interface for the SOS internal representation of SWE simpleTypes
 * 
 * @author Carsten Hollmann
 * @version 1.0.0
 */
public abstract class SosSweAbstractSimpleType<T> extends SosSweAbstractDataComponent {

    private SosSweQuality quality;

	/**
     * Get type of simpleType
     * 
     * @return Type of simpleType
     */
    public abstract SweSimpleType getSimpleType();

    /**
     * Get quality information
     * 
     * @return Quality information
     */
    public SosSweQuality getQuality() {
        return quality;
    }

    /**
     * Set quality information
     * 
     * @param quality
     *            quality information to set
     */
    public void setQuality(SosSweQuality quality) {
    	this.quality = quality;        
    }
    
    /**
     * @return <tt>true</tt>, if the quality field is not <tt>null</tt>,<br>
     * 			<tt>false</tt> else.
     */
    public boolean isSetQuality()
    {
    	return quality != null;
    }

    /**
     * Get value
     * 
     * @return value
     */
    public abstract T getValue();
    
    public abstract String getStringValue();
    
    public abstract boolean isSetValue();
    
    /**
     * Set value
     * 
     * @param value
     *            value to set
     */
    public abstract void setValue(T value);
    
//    /**
//     * @return <tt>true</tt>, if the value field is set and not returning an empty string.
//     */
//    public boolean isSetValue()
//    {
//    	return getValue() != null && !getValue().isEmpty();
//    }
    

	@Override
	public String toString()
	{
		return String.format("%s [value=%s; quality=%s; simpleType=%s]",
				this.getClass().getSimpleName(),
				getValue(),
				getQuality(),
				getSimpleType());
	}
    
    

}
