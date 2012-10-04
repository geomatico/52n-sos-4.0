/**
 * Copyright (C) 2012
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

import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosProcedureDescription;

/**
 * class represents a updateSensor request
 * 
 */
public class UpdateSensorRequest extends AbstractServiceRequest {

    private final String operationName = Sos2Constants.Operations.UpdateSensorDescription.name();

    private String procedureIdentifier;
    
    private String procedureDescriptionFormat;

//    /** String representing xml sensor description */
//    private String procedureXmlDescription;

    /** SOS SensorML description */
    private List<SosProcedureDescription> procedureDescriptions;

    /**
     * default constructor
     */
    public UpdateSensorRequest() {

    }

    /**
     * @return the procedureIdentifier
     */
    public String getProcedureIdentifier() {
        return procedureIdentifier;
    }

    /**
     * @param procedureIdentifier
     *            the procedureIdentifier to set
     */
    public void setProcedureIdentifier(String procedureIdentifier) {
        this.procedureIdentifier = procedureIdentifier;
    }


    public String getProcedureDescriptionFormat() {
        return procedureDescriptionFormat;
    }

    public void setProcedureDescriptionFormat(String procedureDescriptionFormat) {
        this.procedureDescriptionFormat = procedureDescriptionFormat;
    }

//    public String getProcedureXmlDescription() {
//        return procedureXmlDescription;
//    }
//
//    public void setProcedureXmlDescription(String procedureXmlDescription) {
//        this.procedureXmlDescription = procedureXmlDescription;
//    }

    public List<SosProcedureDescription> getProcedureDescriptions() {
        return procedureDescriptions;
    }

    public void setProcedureDescriptions(List<SosProcedureDescription> procedureDescriptions) {
        this.procedureDescriptions = procedureDescriptions;
    }

    public void addProcedureDescriptionString(SosProcedureDescription procedureDescription) {
       if (procedureDescriptions == null) {
           procedureDescriptions = new ArrayList<SosProcedureDescription>();
       }
       procedureDescriptions.add(procedureDescription);
    }

    @Override
    public String getOperationName() {
        return operationName;
    }
}
