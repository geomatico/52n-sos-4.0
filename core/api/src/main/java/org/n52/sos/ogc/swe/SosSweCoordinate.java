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

import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractUomType;

/**
 * SOS internal representation of SWE coordinates
 * 
 * @param <T> 
 */
public class SosSweCoordinate<T> {

    /**
     * Coordinate name
     */
    private SweCoordinateName name;

    /**
     * Coordinate value
     * TODO is this assignment to generic? maybe, we switch to {@link SosSweAbstractUomType}?
     */
    private SosSweAbstractSimpleType<T> value;

    /**
     * constructor
     * 
     * @param name
     *            Coordinate name
     * @param value
     *            Coordinate value
     */
    public SosSweCoordinate(final SweCoordinateName name, final SosSweAbstractSimpleType<T> value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name
     */
    public SweCoordinateName getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(final SweCoordinateName name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public SosSweAbstractSimpleType<T> getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(final SosSweAbstractSimpleType<T> value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("SosSweCoordinate[name=%s, value=%s]", getName(), getValue());
    }
}
