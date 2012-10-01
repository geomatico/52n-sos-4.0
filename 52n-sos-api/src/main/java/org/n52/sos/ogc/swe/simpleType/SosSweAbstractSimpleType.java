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

import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SWEConstants.SweSimpleType;

/**
 * Interface for the SOS internal representation of SWE simpleTypes
 * 
 * @author Carsten Hollmann
 * @version 1.0.0
 */
public abstract class SosSweAbstractSimpleType extends SosSweAbstractDataComponent {

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
    public abstract SosSweQuality getQuality();

    /**
     * Set quality information
     * 
     * @param quality
     *            quality information to set
     */
    public abstract void setQuality(SosSweQuality quality);

    /**
     * Get value
     * 
     * @return value
     */
    public abstract String getValue();

    /**
     * Set value
     * 
     * @param value
     *            value to set
     */
    public abstract void setValue(String value);

}
