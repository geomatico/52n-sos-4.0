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

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.ogc.swe.SosMetadata;

public class InsertSensorRequest extends AbstractServiceRequest {

    private final String operationName = Sos2Constants.Operations.InsertSensor.name();

    private String procedureDescriptionFormat;

    /** observableProperty parameter */
    private List<String> observableProperty;
    
    private List<SosFeatureRelationship> relatedFeatures;

    /** SOS SensorML description */
    private SosProcedureDescription procedureDescription;
    
    private String assignedProcedureIdentifier;
    
    private List<SosOffering> assignedOfferings = new ArrayList<SosOffering>();

    /** metadata parameter */
    private SosMetadata metadata;

    /**
     * default constructor
     */
    public InsertSensorRequest() {

    }

    public String getProcedureDescriptionFormat() {
        return procedureDescriptionFormat;
    }

    public void setProcedureDescriptionFormat(String procedureDescriptionFormat) {
        this.procedureDescriptionFormat = procedureDescriptionFormat;
    }

    /**
     * Get the observableProperty contained in request.
     * 
     * @return the observableProperty
     */
    public List<String> getObservableProperty() {
        return observableProperty;
    }

    /**
     * Set the observableProperty contained in request.
     * 
     * @param observableProperty
     *            the observableProperty to set
     */
    public void setObservableProperty(List<String> observableProperty) {
        this.observableProperty = observableProperty;
    }

    /**
     * Get the sensor description contained in request.
     * 
     * @return the sosSensorML
     */
    public SosProcedureDescription getProcedureDescription() {
        return procedureDescription;
    }

    /**
     * Set the sensor description contained in request.
     * 
     * @param sosSensorML
     *            the sosSensorML to set
     */
    public void setProcedureDescription(SosProcedureDescription procedureDescription) {
        this.procedureDescription = procedureDescription;
    }

    /**
     * Get the metadata contained in request.
     * 
     * @return the metadata
     */
    public SosMetadata getMetadata() {
        return metadata;
    }

    /**
     * Set the metadata contained in request.
     * 
     * @param metadata
     *            the metadata to set
     */
    public void setMetadata(SosMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    public void setRelatedFeature(List<SosFeatureRelationship> relatedFeatures) {
        this.relatedFeatures = relatedFeatures;
    }
    
    public List<SosFeatureRelationship> getRelatedFeatures() {
        return relatedFeatures;
    }

    public String getAssignedProcedureIdentifier() {
        return assignedProcedureIdentifier;
    }

    public List<SosOffering> getAssignedOfferings() {
        return assignedOfferings;
    }
    
    public SosOffering getFirstAssignedOffering() {
        if (isSetAssignedOfferings()) {
            return assignedOfferings.get(0);
        }
        return null;
    }

    private boolean isSetAssignedOfferings() {
        return assignedOfferings != null && !assignedOfferings.isEmpty();
    }

    public void setAssignedProcedureIdentifier(String assignedProcedureID) {
        this.assignedProcedureIdentifier = assignedProcedureID;
    }

    public void setAssignedOfferings(List<SosOffering> assignedOfferings) {
        this.assignedOfferings.addAll(assignedOfferings);
    }
}
