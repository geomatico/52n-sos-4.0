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


/**
 * SOS internal representation of SWE field
 * 
 */
public class SosSweField extends SosSweAbstractDataComponent{

    /**
     * field name
     */
    private String name;

    /**
     * field element
     */
    private SosSweAbstractDataComponent element;

    /**
     * constructor
     * 
     * @param name
     *            Field name
     * @param element
     *            Field element
     */
    public SosSweField(String name, SosSweAbstractDataComponent element) {
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
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the element
     */
    public SosSweAbstractDataComponent getElement() {
        return element;
    }

    /**
     * @param element
     *            the element to set
     */
    public void setElement(SosSweAbstractDataComponent element) {
        this.element = element;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + super.hashCode();
        hash = 67 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 67 * hash + (this.getElement() != null ? this.getElement().hashCode() : 0);
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
        final SosSweField other = (SosSweField) obj;
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if (this.getElement() != other.getElement() && (this.getElement() == null || !this.getElement().equals(other.getElement()))) {
            return false;
        }
        return super.equals(obj);
    }

}
