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

package org.n52.sos.request;

import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SosMetadata;

public class InsertSensorRequest extends AbstractServiceRequest {
    
    private final String operationName = Sos2Constants.Operations.InsertSensor
            .name();

    /**
     * phenomena, for which the sensor, which should be registered, offers
     * values for
     */
    private Collection<AbstractSosPhenomenon> phenomena;

    /**
     * features of interest whose properties are measured by this sensor
     */
    private Collection<SosAbstractFeature> featuresOfInterest;

    /** String representing xml sensor description */
    private String sensorDescription;

    /** mobileEnabled parameter */
    private boolean mobileEnabled;

    /** observableProperty parameter */
    private List<String> observableProperty;

    /** SOS SensorML description */
    private SensorML sensorML;

    /** metadata parameter */
    private SosMetadata metadata;

    /**
     * default constructor
     */
    public InsertSensorRequest() {

    }

    /**
     * constructor
     * 
     * @param systemp
     *            sensor system, which should be registered
     * @param sosComponents
     *            offering ID, for which the sensor, which should be registered,
     *            offers values for
     * @param sensorDescriptionp
     *            offering ID, for which the sensor, which should be registered,
     *            offers values for
     */
    public InsertSensorRequest(Collection<AbstractSosPhenomenon> sosComponents,
            String sensorDescriptionp, Collection<SosAbstractFeature> featuresOfInterest,
            boolean mobileEnabled) {
        this.phenomena = sosComponents;
        this.sensorDescription = sensorDescriptionp;
        this.featuresOfInterest = featuresOfInterest;
        this.mobileEnabled = mobileEnabled;
    }

    /**
     * @return the phenomena
     */
    public Collection<AbstractSosPhenomenon> getPhenomena() {
        return phenomena;
    }

    /**
     * @param phenomena
     *            the phenomena to set
     */
    public void setPhenomena(Collection<AbstractSosPhenomenon> phenomena) {
        this.phenomena = phenomena;
    }

    /**
     * @return the sensorDescription
     */
    public String getSensorDescription() {
        return sensorDescription;
    }

    /**
     * @param sensorDescription
     *            the sensorDescription to set
     */
    public void setSensorDescription(String sensorDescription) {
        this.sensorDescription = sensorDescription;
    }

    /**
     * 
     * @return mobileEnabled
     */
    public boolean isMobileEnabled() {
        return mobileEnabled;
    }

    /**
     * 
     * @param mobileEnabled
     */
    public void setMobileEnabled(boolean mobileEnabled) {
        this.mobileEnabled = mobileEnabled;
    }

    /**
     * Get the features of interest contained in request.
     * 
     * @return Collection with features of interest
     */
    public Collection<SosAbstractFeature> getFeaturesOfInterest() {
        return featuresOfInterest;
    }

    /**
     * Set the features of interest contained in request.
     * 
     * @param featuresOfInterest
     */
    public void setFeaturesOfInterest(Collection<SosAbstractFeature> featuresOfInterest) {
        this.featuresOfInterest = featuresOfInterest;
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
    public SensorML getSosSensorML() {
        return sensorML;
    }

    /**
     * Set the sensor description contained in request.
     * 
     * @param sosSensorML
     *            the sosSensorML to set
     */
    public void setSosSensorML(SensorML sosSensorML) {
        this.sensorML = sosSensorML;
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

}
