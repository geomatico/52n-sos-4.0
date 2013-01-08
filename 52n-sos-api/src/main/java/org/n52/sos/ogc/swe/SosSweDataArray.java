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
package org.n52.sos.ogc.swe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;

/**
 * SOS internal representation of SWE dataArray
 * TODO document that this implementation supports only simple types in swe:elementType.swe:DataRecord in TWiki
 */
public class SosSweDataArray extends SosSweAbstractDataComponent{

    /**
     * swe:values<br />
     * Each list entry represents one block, a list of tokens.<br />
     * Atm, this implementation using java.lang.String to represent each token.
     */
    private List<List<String>> values;

    /**
     * swe:elementType
     */
    private SosSweAbstractDataComponent elementType;

    /**
     * 
     */
    private SosSweAbstractEncoding encoding;

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
     */
    public void setValues(List<List<String>> values) {
        this.values = values;
    }

    /**
     * @return the elementType
     */
    public SosSweAbstractDataComponent getElementType() {
        return elementType;
    }

    /**
     * @param elementType
     *            the elementType to set
     */
    public void setElementType(SosSweAbstractDataComponent elementType) {
        this.elementType = elementType;
    }

    public SosSweCount getElementCount() {
            SosSweCount elementCount = new SosSweCount();
            if (isSetValues())
            {
            	elementCount.setValue(values.size());
            }
            else
            {
            	elementCount.setValue(0);
            }
            return elementCount; 
    }

    public SosSweAbstractEncoding getEncoding()
    {
        return encoding;
    }

    public void setEncoding(SosSweAbstractEncoding encoding)
    {
        this.encoding = encoding;
    }
    
    /**
     * @return <tt>true</tt>, if the values field is set properly
     */
    public boolean isSetValues()
    {
    	return values != null && values.size() >= 0;
    }

    /**
     * Adds the given block - a {@link List}<{@link String}> - add the end of the current list of blocks
     * @param blockOfTokensToAddAtTheEnd
     * @return <tt>true</tt> (as specified by {@link Collection#add}) <br />
     *          <tt>false</tt> if block could not be added
     */
    public boolean add(List<String> blockOfTokensToAddAtTheEnd)
    {
        if (values == null)
        {
            values = new ArrayList<List<String>>();
        }
        return values.add(blockOfTokensToAddAtTheEnd);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + super.hashCode();
        hash = 23 * hash + (this.getValues() != null ? this.getValues().hashCode() : 0);
        hash = 23 * hash + (this.getElementType() != null ? this.getElementType().hashCode() : 0);
        hash = 23 * hash + (this.getEncoding() != null ? this.getEncoding().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosSweDataArray other = (SosSweDataArray) obj;
        if (this.getValues() != other.getValues() && (this.getValues() == null || !this.getValues().equals(other.getValues()))) {
            return false;
        }
        if (this.getElementType() != other.getElementType() && (this.getElementType() == null || !this.getElementType().equals(other.getElementType()))) {
            return false;
        }
        if (this.getEncoding() != other.getEncoding() && (this.getEncoding() == null || !this.getEncoding().equals(other.getEncoding()))) {
            return false;
        }
        return super.equals(obj);
    }

    public boolean isSetElementTyp() {
        return elementType != null;
    }
}
