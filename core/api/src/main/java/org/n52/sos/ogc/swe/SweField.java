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

import org.n52.sos.ogc.swe.SWEConstants.SweDataComponentType;



/**
 * SOS internal representation of SWE field
 * 
 */
public class SweField extends SweAbstractDataComponent{

    /**
     * field name
     */
    private String name;

    /**
     * field element
     */
    private SweAbstractDataComponent element;

    /**
     * constructor
     * 
     * @param name
     *            Field name
     * @param element
     *            Field element
     */
    public SweField(final String name, final SweAbstractDataComponent element) {
        super();
        this.name = name;
        this.element = element;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name
     *            the name to set
     * @return 
     */
    public SweField setName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * @return the element
     */
    public SweAbstractDataComponent getElement() {
        return element;
    }

    /**
     * @param element
     *            the element to set
     * @return 
     */
    public SweField setElement(final SweAbstractDataComponent element) {
        this.element = element;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 67;
        int hash = 3;
        hash = prime * hash + super.hashCode();
        hash = prime * hash + (getName() != null ? getName().hashCode() : 0);
        hash = prime * hash + (getElement() != null ? getElement().hashCode() : 0);
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
        final SweField other = (SweField) obj;
        if ((getName() == null) ? (other.getName() != null) : !getName().equals(other.getName())) {
            return false;
        }
        if (getElement() != other.getElement() && (getElement() == null || !getElement().equals(other.getElement()))) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.format("SosSweField[name=%s, element=%s]", getName(), getElement());
    }
    
    @Override
    public SweDataComponentType getDataComponentType(){
    	return SweDataComponentType.Field;
    }
}
