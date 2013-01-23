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

import org.n52.sos.ogc.swe.AbstractDataRecord;

/**
 * SOS internal representation of SensorML characteristics
 * 
 */
public class SosSMLCharacteristics {

    private String typeDefinition;

    private AbstractDataRecord abstractDataRecord;

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
     * @param abstractDataRecord
     *            abstractDataRecord
     */
    public SosSMLCharacteristics( AbstractDataRecord abstractDataRecord) {
        super();
        this.abstractDataRecord = abstractDataRecord;
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
     * @return the abstractDataRecord
     */
    public AbstractDataRecord getDataRecord() {
        return abstractDataRecord;
    }

    /**
     * @param abstractDataRecord
     *            the abstractDataRecord to set
     */
    public void setDataRecord(AbstractDataRecord abstractDataRecord) {
        this.abstractDataRecord = abstractDataRecord;
    }

    public boolean isSetAbstractDataRecord() {
        return abstractDataRecord != null;
    }
    
    public boolean isSetTypeDefinition() {
        return typeDefinition != null && !typeDefinition.isEmpty();
    }

}
