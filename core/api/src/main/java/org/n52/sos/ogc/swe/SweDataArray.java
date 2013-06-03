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
package org.n52.sos.ogc.swe;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.n52.sos.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.sos.ogc.swe.simpleType.SweCount;

/**
 * SOS internal representation of SWE dataArray
 * TODO document that this implementation supports only simple types in swe:elementType.swe:DataRecord in TWiki
 */
public class SweDataArray extends SweAbstractDataComponent{

    /**
     * swe:values<br />
     * Each list entry represents one block, a list of tokens.<br />
     * Atm, this implementation using java.lang.String to represent each token.
     */
    private List<List<String>> values;

    /**
     * swe:elementType
     */
    private SweAbstractDataComponent elementType;

    /**
     * 
     */
    private SweAbstractEncoding encoding;

    private SweCount elementCount;

    /**
     * @return the values
     */
    public List<List<String>> getValues() {
        return values;
    }

    /**
     *
     * @param values
     *            the values to set
     * @return 
     */
    public SweDataArray setValues(final List<List<String>> values) {
        this.values = values;
        return this;
    }

    /**
     * @return the elementType
     */
    public SweAbstractDataComponent getElementType() {
        return elementType;
    }

    /**
     * @param elementType
     *            the elementType to set
     * @return 
     */
    public SweDataArray setElementType(final SweAbstractDataComponent elementType) {
        this.elementType = elementType;
        return this;
    }

    public SweCount getElementCount() {
            SweCount elementCount = new SweCount();
            if (isSetValues())
            {
            	elementCount.setValue(values.size());
            }
            else if (isSetElementCount())
            {
            	elementCount = this.elementCount;
            } else {
                elementCount.setValue(0);
            }
            return elementCount; 
    }

    public SweAbstractEncoding getEncoding()
    {
        return encoding;
    }

    public SweDataArray setEncoding(final SweAbstractEncoding encoding)
    {
        this.encoding = encoding;
        return this;
    }
    
    /**
     * @return <tt>true</tt>, if the values field is set properly
     */
    public boolean isSetValues()
    {
        if (values != null && !values.isEmpty()) {
            if (values.size() == 1) {
                final List<String> list = values.get(0);
                return list != null && !list.isEmpty();
            }
            return true;
        }
    	return false;
    }

    /**
     * Adds the given block - a {@link List}<{@link String}> - add the end of the current list of blocks
     * @param blockOfTokensToAddAtTheEnd
     * @return <tt>true</tt> (as specified by {@link Collection#add}) <br />
     *          <tt>false</tt> if block could not be added
     */
    public boolean add(final List<String> blockOfTokensToAddAtTheEnd)
    {
        if (values == null)
        {
            values = new LinkedList<List<String>>();
        }
        return values.add(blockOfTokensToAddAtTheEnd);
    }

    @Override
    public int hashCode() {
        final int prime = 23;
        int hash = 7;
        hash = prime * hash + super.hashCode();
        hash = prime * hash + (getValues() != null ? getValues().hashCode() : 0);
        hash = prime * hash + (getElementType() != null ? getElementType().hashCode() : 0);
        hash = prime * hash + (getEncoding() != null ? getEncoding().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SweDataArray other = (SweDataArray) obj;
        if (getValues() != other.getValues() && (getValues() == null || !getValues().equals(other.getValues()))) {
            return false;
        }
        if (getElementType() != other.getElementType() && (getElementType() == null || !getElementType().equals(other.getElementType()))) {
            return false;
        }
        if (getEncoding() != other.getEncoding() && (getEncoding() == null || !getEncoding().equals(other.getEncoding()))) {
            return false;
        }
        return super.equals(obj);
    }

    public boolean isSetElementTyp() {
        return elementType != null;
    }
    
    public boolean isSetEncoding() {
        return encoding != null;
    }

    public SweDataArray setElementCount(final SweCount elementCount) {
        this.elementCount = elementCount;
        return this;
    }
    
    public boolean isSetElementCount() {
        return elementCount != null || isSetValues();
    }

    public boolean isEmpty() {
        return isSetElementTyp() && isSetEncoding() && isSetValues();
    }
    
}
