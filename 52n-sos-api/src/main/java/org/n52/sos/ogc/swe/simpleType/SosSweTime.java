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

/**
 * SOS internal representation of SWE simpleType time
 * 
 */
public class SosSweTime extends SosSweAbstractSimpleType {

    /**
     * SWE simple type type
     */
    private SweSimpleType simpleType = SweSimpleType.Time;

    /**
     * quality data
     */
    private SosSweQuality quality;

    /**
     * value
     */
    private String value;

    /**
     * unit of measurement
     */
    private String uom;

    /**
     * constructor
     */
    public SosSweTime() {
        super();
    }

    @Override
    public SweSimpleType getSimpleType() {
        return simpleType;
    }

    @Override
    public SosSweQuality getQuality() {
        return quality;
    }

    @Override
    public void setQuality(SosSweQuality quality) {
        this.quality = quality;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get unit of measurement
     * 
     * @return the uom
     */
    public String getUom() {
        return uom;
    }

    /**
     * Set unit of measurement
     * 
     * @param uom
     *            the uom to set
     */
    public void setUom(String uom) {
        this.uom = uom;
    }

}
