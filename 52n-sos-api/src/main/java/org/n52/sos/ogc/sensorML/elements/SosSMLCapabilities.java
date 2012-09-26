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
package org.n52.sos.ogc.sensorML.elements;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.SosGmlMetaDataProperty;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SosSweField;

/**
 * SOS internal representation of SensorML capabilities
 * 
 */
public class SosSMLCapabilities {

    private String name;

    private SweAggregateType capabilitiesType;

    private List<SosSweField> fields;

    private List<SosGmlMetaDataProperty> metaDataProperties;

    /**
     * default constructor
     */
    public SosSMLCapabilities() {
        super();
    }

    /**
     * constructor
     * 
     * @param characteristicsType
     *            Type
     * @param fields
     *            Fields
     */
    public SosSMLCapabilities(String name, SweAggregateType capabilitiesType, List<SosSweField> fields) {
        super();
        this.setName(name);
        this.capabilitiesType = capabilitiesType;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the capabilitiesType
     */
    public SweAggregateType getCapabilitiesType() {
        return capabilitiesType;
    }

    /**
     * @param capabilitiesType
     *            the capabilitiesType to set
     */
    public void setCapabilitiesType(SweAggregateType capabilitiesType) {
        this.capabilitiesType = capabilitiesType;
    }

    /**
     * @return the fields
     */
    public List<SosSweField> getFields() {
        return fields;
    }

    /**
     * @param fields
     *            the fields to set
     */
    public void setFields(List<SosSweField> fields) {
        this.fields = fields;
    }

    public List<SosGmlMetaDataProperty> getMetaDataProperties() {
        return metaDataProperties;
    }

    public void setMetaDataProperties(List<SosGmlMetaDataProperty> metaDataProperties) {
        this.metaDataProperties = metaDataProperties;
    }

    public void addMetaDataProperties(SosGmlMetaDataProperty metaDataProperty) {
        if (metaDataProperties == null) {
            metaDataProperties = new ArrayList<SosGmlMetaDataProperty>();
        }
        metaDataProperties.add(metaDataProperty);
    }

}
