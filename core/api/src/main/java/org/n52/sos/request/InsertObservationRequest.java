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
package org.n52.sos.request;

import java.util.LinkedList;
import java.util.List;

import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.sos.SosConstants;

/**
 * SOS InsertObservation request
 * 
 */
public class InsertObservationRequest extends AbstractServiceRequest {

    /**
     * InsertObservation operation name
     */
    private final String operationName = SosConstants.Operations.InsertObservation.name();

    /**
     * Assigned sensor id
     */
    private String assignedSensorId;
    
    private List<String> offerings;

    /**
     * SOS observation collection with observations to insert
     */
    private List<SosObservation> observations;

    public InsertObservationRequest() {

    }

    /**
     * Get assigned sensor id
     * 
     * @return assigned sensor id
     */
    public String getAssignedSensorId() {
        return assignedSensorId;
    }

    /**
     * Set assigned sensor id
     * 
     * @param assignedSensorId
     *            assigned sensor id
     */
    public void setAssignedSensorId(String assignedSensorId) {
        this.assignedSensorId = assignedSensorId;
    }

    /**
     * Get observations to insert
     * 
     * @return observations to insert
     */
    public List<SosObservation> getObservations() {
        return observations;
    }

    /**
     * Set observations to insert
     * 
     * @param observation
     *            observations to insert
     */
    public void setObservation(List<SosObservation> observation) {
        this.observations = observation;
    }

    public void addObservation(SosObservation observation) {
       if (observations == null) {
           observations = new LinkedList<SosObservation>();
       }
        observations.add(observation);
    }

    public void setOfferings(List<String> offerings) {
        this.offerings = offerings;
    }

    public List<String> getOfferings() {
        return offerings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.request.AbstractSosRequest#getOperationName()
     */
    @Override
    public String getOperationName() {
        return operationName;
    }
}
