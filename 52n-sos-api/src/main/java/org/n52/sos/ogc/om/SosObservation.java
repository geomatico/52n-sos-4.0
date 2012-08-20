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

package org.n52.sos.ogc.om;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;

/**
 * Class represents a SOS observation
 * 
 */
public class SosObservation implements Cloneable {
    
    /**
     * ID of this observation; in the standard 52n SOS PostgreSQL database, this
     * is implemented through a sequence type.
     */
    private String observationID;

    /**
     * identifier of this observation gml:identifier
     */
    private String identifier;

    /** result time of the observation */
    private TimeInstant resultTime;

    /** valid time of the observation */
    private TimePeriod validTime;

    /**
     * constellation of procedure, obervedProperty, offering and observationType
     */
    private SosObservationConstellation observationConstellation;

    /** type of the value or the result the value points to */
    private String resultType;

    /**
     * Map with observation values for each obsservableProeprty
     */
    private IObservationValue value;

    /**
     * token separator for the value tuples contained in the result element of
     * the generic observation
     */
    private String tokenSeparator;

    /** no data value for the values contained in the result element */
    private String noDataValue;

    /** separator of value tuples, which are contained in the resulte element */
    private String tupleSeparator;

    /**
     * constructor
     */
    public SosObservation() {
        super();
    }

    /**
     * Get the observation constellation
     * 
     * @return the observationConstellation
     */
    public SosObservationConstellation getObservationConstellation() {
        return observationConstellation;
    }

    /**
     * Set the observation constellation
     * 
     * @param observationConstellation
     *            the observationConstellation to set
     */
    public void setObservationConstellation(SosObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }

    /**
     * Get observation ID
     * 
     * @return the observationID
     */
    public String getObservationID() {
        return observationID;
    }

    /**
     * Set observation ID
     * 
     * @param observationID
     *            the observationID to set
     */
    public void setObservationID(String observationID) {
        this.observationID = observationID;
    }

    /**
     * Get observation identifier
     * 
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set observation identifier
     * 
     * @param identifier
     *            the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Get phenomenon time
     * 
     * @return the phenomenonTime
     */
    public ITime getPhenomenonTime() {
        return value.getPhenomenonTime();
    }

    /**
     * Get result time
     * 
     * @return the resultTime
     */
    public TimeInstant getResultTime() {
        return resultTime;
    }

    /**
     * Set result time
     * 
     * @param resultTime
     *            the resultTime to set
     */
    public void setResultTime(TimeInstant resultTime) {
        this.resultTime = resultTime;
    }

    /**
     * Get valid time
     * 
     * @return the validTime
     */
    public TimePeriod getValidTime() {
        return validTime;
    }

    /**
     * Set valid time
     * 
     * @param validTime
     *            the validTime to set
     */
    public void setValidTime(TimePeriod validTime) {
        this.validTime = validTime;
    }

    /**
     * Get result type
     * 
     * @return the resultType
     */
    public String getResultType() {
        return resultType;
    }

    /**
     * Set result type
     * 
     * @param resultType
     *            the resultType to set
     */
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    /**
     * Get token separator
     * 
     * @return the tokenSeparator
     */
    public String getTokenSeparator() {
        return tokenSeparator;
    }

    /**
     * Set token separator
     * 
     * @param tokenSeparator
     *            the tokenSeparator to set
     */
    public void setTokenSeparator(String tokenSeparator) {
        this.tokenSeparator = tokenSeparator;
    }

    /**
     * Get noData value
     * 
     * @return the noDataValue
     */
    public String getNoDataValue() {
        return noDataValue;
    }

    /**
     * Set noData value
     * 
     * @param noDataValue
     *            the noDataValue to set
     */
    public void setNoDataValue(String noDataValue) {
        this.noDataValue = noDataValue;
    }

    /**
     * Get tuple separator
     * 
     * @return the tupleSeparator
     */
    public String getTupleSeparator() {
        return tupleSeparator;
    }

    /**
     * Set tuple separator
     * 
     * @param tupleSeparator
     *            the tupleSeparator to set
     */
    public void setTupleSeparator(String tupleSeparator) {
        this.tupleSeparator = tupleSeparator;
    }

    /**
     * Get observation values
     * 
     * @return the values
     */
    public IObservationValue getValue() {
        return value;
    }

    /**
     * Set observation values
     * 
     * @param values
     *            the values to set
     */
    public void setValue(IObservationValue value) {
        this.value = value;
    }

    public SosObservation clone() throws CloneNotSupportedException{
            return (SosObservation)super.clone();
    }
}
