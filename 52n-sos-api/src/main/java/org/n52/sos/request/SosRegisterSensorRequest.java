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

import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.sos.Sos1Constants;

/**
 * SOS RegisterSensor request
 * 
 */
public class SosRegisterSensorRequest extends AbstractServiceRequest {

    /**
     * RegisterSensor operation name
     */
    private final String operationName = Sos1Constants.Operations.RegisterSensor.name();

    /**
     * SOS Sensor system
     */
    // private SensorSystem system;

    /**
     * observableProperties collection
     */
    private Collection<AbstractSosPhenomenon> observableProperties;

    /**
     * featureOfInterest collection
     */
    private Collection<SosAbstractFeature> featuresOfInterest;

    /**
     * Sensor description
     */
    private String sensorDescription;

    /**
     * constructor
     * 
     * @param system
     *            SOS sensor system
     * @param sosComponents
     *            observableProperties
     * @param sensorDescription
     *            Sensor description
     * @param featuresOfInterest
     *            featuresOfInterst
     */
    public SosRegisterSensorRequest(Collection<AbstractSosPhenomenon> observableProperties, String sensorDescription,
            Collection<SosAbstractFeature> featuresOfInterest) {
        // this.system = system;
        this.observableProperties = observableProperties;
        this.sensorDescription = sensorDescription;
        this.featuresOfInterest = featuresOfInterest;
    }

    /**
     * Get observableProperties
     * 
     * @return observableProperties
     */
    public Collection<AbstractSosPhenomenon> getObservableProperties() {
        return observableProperties;
    }

    /**
     * Set observableProperties
     * 
     * @param observableProperties
     *            observableProperties
     */
    public void setObservableProperties(Collection<AbstractSosPhenomenon> observableProperties) {
        this.observableProperties = observableProperties;
    }

    /**
     * Get sensor description
     * 
     * @return sensor description
     */
    public String getSensorDescription() {
        return sensorDescription;
    }

    /**
     * Set sensor description
     * 
     * @param sensorDescription
     *            sensor description
     */
    public void setSensorDescription(String sensorDescription) {
        this.sensorDescription = sensorDescription;
    }

    // /**
    // * Get SOS sensor system
    // *
    // * @return SOS sensor system
    // */
    // public SensorSystem getSystem() {
    // return system;
    // }
    //
    // /**
    // * Set SOS sensor system
    // *
    // * @param system
    // * SOS sensor system
    // */
    // public void setSystem(SensorSystem system) {
    // this.system = system;
    // }

    /**
     * Get featuresOfInterst
     * 
     * @return featuresOfInterst
     */
    public Collection<SosAbstractFeature> getFeaturesOfInterest() {
        return featuresOfInterest;
    }

    /**
     * Set featuresOfInterst
     * 
     * @param featuresOfInterest
     *            featuresOfInterst
     */
    public void setFeaturesOfInterest(Collection<SosAbstractFeature> featuresOfInterest) {
        this.featuresOfInterest = featuresOfInterest;
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
