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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;

/**
 * class represents a updateSensor request
 * 
 */
public class UpdateSensorRequest extends AbstractServiceRequest {
    
    private final String operationName = Sos2Constants.Operations.UpdateSensor
            .name();

    private String procedureID;

    /** SensorML system, which should be updated */
    private SensorML system;

    /** String representing xml sensor description */
    private String sensorDescription;

    /** SOS SensorML description */
    private List<SensorML> sosSensorML;

    /**
     * default constructor
     */
    public UpdateSensorRequest() {

    }

    /**
     * constructor
     * 
     * @param systemp
     *            sensor system, which should be updated
     * @param sosComponents
     *            offering ID, for which the sensor, which should be updated,
     *            offers values for
     * @param sensorDescriptionp
     *            offering ID, for which the sensor, which should be updated,
     *            offers values for
     */
    public UpdateSensorRequest(String procedureID, Collection<AbstractSosPhenomenon> sosComponents,
            String sensorDescriptionp) {
        this.procedureID = procedureID;
        this.sensorDescription = sensorDescriptionp;
    }

    /**
     * @return the procedureID
     */
    public String getProcedureID() {
        return procedureID;
    }

    /**
     * @param procedureID
     *            the procedureID to set
     */
    public void setProcedureID(String procedureID) {
        this.procedureID = procedureID;
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

//    /**
//     * @return the system
//     */
//    public SensorSystem getSystem() {
//        return system;
//    }
//
//    /**
//     * @param system
//     *            the system to set
//     */
//    public void setSystem(SensorSystem system) {
//        this.system = system;
//    }

    /**
     * Get the sensor description contained in request.
     * 
     * @return the sosSensorML
     */
    public List<SensorML> getSosSensorML() {
        return sosSensorML;
    }

    /**
     * Set the sensor description contained in request.
     * 
     * @param sosSensorML
     *            the sosSensorML to set
     */
    public void setSosSensorML(List<SensorML> sosSensorML) {
        this.sosSensorML.addAll(sosSensorML);
    }

    /**
     * @param sosSensorML
     */
    public void addSosSensorML(SensorML sosSensorML) {
        if (this.sosSensorML == null) {
            this.sosSensorML = new ArrayList<SensorML>();
        }
        this.sosSensorML.add(sosSensorML);
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

}
