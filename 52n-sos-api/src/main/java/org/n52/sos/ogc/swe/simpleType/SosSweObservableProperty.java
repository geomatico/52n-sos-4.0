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
 * SOS internal representation of SWE simpleType observableProperty
 * 
 */
public class SosSweObservableProperty implements ISosSweSimpleType {

    /**
     * SWE simple type type
     */
    private SweSimpleType simpleType = SweSimpleType.ObservableProperty;

    /**
     * definition
     */
    private String definition;

    /**
     * description
     */
    private String description;

    /**
     * quality information
     */
    private SosSweQuality quality;

    /**
     * value
     */
    private String value;

    /**
     * constructor
     */
    public SosSweObservableProperty() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.ogc.swe.simpleType.ISosSweSimpleType#getSimpleType()
     */
    @Override
    public SweSimpleType getSimpleType() {
        return simpleType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.ogc.swe.simpleType.ISosSweSimpleType#getQuality()
     */
    @Override
    public SosSweQuality getQuality() {
        return quality;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.ogc.swe.simpleType.ISosSweSimpleType#setQuality(org.n52.ogc.swe
     * .simpleType.SosSweQuality)
     */
    @Override
    public void setQuality(SosSweQuality quality) {
        this.quality = quality;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.ogc.swe.simpleType.ISosSweSimpleType#getDefinition()
     */
    @Override
    public String getDefinition() {
        return definition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.ogc.swe.simpleType.ISosSweSimpleType#setDefinition(java.lang.
     * String)
     */
    @Override
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.ogc.swe.simpleType.ISosSweSimpleType#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.ogc.swe.simpleType.ISosSweSimpleType#setDescription(java.lang
     * .String)
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.ogc.swe.simpleType.ISosSweSimpleType#getValue()
     */
    @Override
    public String getValue() {
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.ogc.swe.simpleType.ISosSweSimpleType#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        this.value = value;
    }

}
