/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ogc.sensorML.elements;

import java.util.List;

import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SosSweField;

/**
 * SOS internal representation of SensorML characteristics
 * 
 */
public class SosSMLCharacteristics {

    private SweAggregateType characteristicsType;

    private String typeDefinition;

    private List<SosSweField> fields;

    /**
     * default constructor
     */
    public SosSMLCharacteristics() {
        super();
    }

    /**
     * constructor
     * 
     * @param characteristicsType
     *            type
     * @param fields
     *            fields
     */
    public SosSMLCharacteristics(SweAggregateType characteristicsType, List<SosSweField> fields) {
        super();
        this.characteristicsType = characteristicsType;
        this.fields = fields;
    }

    /**
     * @return the characteristicsType
     */
    public SweAggregateType getCharacteristicsType() {
        return characteristicsType;
    }

    /**
     * @param characteristicsType
     *            the characteristicsType to set
     */
    public void setCharacteristicsType(SweAggregateType characteristicsType) {
        this.characteristicsType = characteristicsType;
    }

    /**
     * @return the typeDefinition
     */
    public String getTypeDefinition() {
        return typeDefinition;
    }

    /**
     * @param typeDefinition
     *            the typeDefinition to set
     */
    public void setTypeDefinition(String typeDefinition) {
        this.typeDefinition = typeDefinition;
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

}
