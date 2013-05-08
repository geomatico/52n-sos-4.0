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

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.GmlMetaDataProperty;
import org.n52.sos.ogc.swe.DataRecord;

/**
 * SOS internal representation of SensorML capabilities
 * 
 */
public class SosSMLCapabilities {

    private String name;

    private DataRecord dataRecord;

    private List<GmlMetaDataProperty> gmlMetaDataProperties;

    /**
     * default constructor
     */
    public SosSMLCapabilities() {
        super();
    }

    /**
     * constructor
     * 
     * @param name               Type
     * @param dataRecord
     *            DataRecord
     */
    public SosSMLCapabilities(String name, DataRecord dataRecord) {
        super();
        this.setName(name);
        this.dataRecord = dataRecord;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<GmlMetaDataProperty> getMetaDataProperties() {
        return gmlMetaDataProperties;
    }

    public void setMetaDataProperties(List<GmlMetaDataProperty> gmlMetaDataProperties) {
        this.gmlMetaDataProperties = gmlMetaDataProperties;
    }

    public void addMetaDataProperties(GmlMetaDataProperty gmlMetaDataProperty) {
        if (gmlMetaDataProperties == null) {
            gmlMetaDataProperties = new ArrayList<GmlMetaDataProperty>();
        }
        gmlMetaDataProperties.add(gmlMetaDataProperty);
    }
    
    public boolean isSetAbstractDataRecord() {
        return dataRecord != null;
    }

    public boolean isSetName() {
        return name != null && !name.isEmpty();
    }

}
