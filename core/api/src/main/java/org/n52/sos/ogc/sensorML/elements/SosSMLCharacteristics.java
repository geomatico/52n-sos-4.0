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
package org.n52.sos.ogc.sensorML.elements;

import org.n52.sos.ogc.swe.DataRecord;

/**
 * SOS internal representation of SensorML characteristics
 * 
 */
public class SosSMLCharacteristics {

    private String typeDefinition;

    private DataRecord dataRecord;

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
     * @param dataRecord
     *            dataRecord
     */
    public SosSMLCharacteristics( DataRecord dataRecord) {
        super();
        this.dataRecord = dataRecord;
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
     * @return the dataRecord
     */
    public DataRecord getDataRecord() {
        return dataRecord;
    }

    /**
     * @param dataRecord
     *            the dataRecord to set
     */
    public void setDataRecord(DataRecord dataRecord) {
        this.dataRecord = dataRecord;
    }

    public boolean isSetAbstractDataRecord() {
        return dataRecord != null;
    }
    
    public boolean isSetTypeDefinition() {
        return typeDefinition != null && !typeDefinition.isEmpty();
    }

}
