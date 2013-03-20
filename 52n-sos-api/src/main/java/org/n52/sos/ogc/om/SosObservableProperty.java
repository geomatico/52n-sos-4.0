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
package org.n52.sos.ogc.om;


/**
 * class represents a phenomenon of an observation
 * 
 */
public class SosObservableProperty extends AbstractSosPhenomenon {
    private static final long serialVersionUID = -1820718860701876580L;

    /** unit of the values of the phenomenons observations */
    private String unit;

    /** valueType in the database of the phenomenons observation values */
    private String valueType;

    /**
     * constructor
     * 
     * @param identifier
     *            observableProperty identifier
     */
    public SosObservableProperty(String identifier) {
        super(identifier);
    }

    /**
     * constructor
     * 
     * @param identifier
     *            id of the observableProperty
     * @param description
     *            description of the observableProperty
     * @param unit
     *            unit of the observation values according to this
     *            observableProperty
     * @param valueType
     *            database valType of the observation values according to this
     *            observableProperty
     */
    public SosObservableProperty(String identifier, String description, String unit,
            String valueType) {
        super(identifier, description);
        this.unit = unit;
        this.valueType = valueType;
    }

    /**
     * Get unit of measurement
     * 
     * @return Returns the unit.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Set unit of measurement
     * 
     * @param unit
     *            The unit to set.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Get value type
     * 
     * @return Returns the valueType.
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * Set value type
     * 
     * @param valueType
     *            The valueType to set.
     */
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
}
