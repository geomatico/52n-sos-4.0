/***************************************************************
Copyright (C) 2010
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

Author: <LIST OF AUTHORS/EDITORS (optional line)>
Created: <CREATION DATE (optional line)>
Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/
package org.n52.sos.encode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;

/**
 * Interface for a response encoder
 * 
 */
public interface ISosResponseEncoder {

    /**
     * creates RegisterSensorResponse
     * 
     * @param assignedSensorID
     *            id of new registered sensor
     * @return Returns XMLBeans representation of RegisterSensorResponse
     */
    public XmlObject createRegisterSensorResponse(String assignedSensorID);

    /**
     * creates InsertObservationRespones XMLBean
     * 
     * @param observation_id
     *            assigned id of observation
     * @return Returns assigned id of observation
     */
    public XmlObject createInsertObservationResponse(int observation_id);

    /**
     * creates an XmlBeans document representing the GetResultResponse document
     * from the passed generic observation
     * 
     * @param sosObs
     * @return XMLBeans representation of GetResultResponse
     * @throws OwsExceptionReport
     */
    public XmlObject createResultRespDoc(List<SosObservation> sosObs) throws OwsExceptionReport;

    public XmlObject createDescribeSensorResponse(SensorML sensorDesc, Collection<String> parentProcedureIds,
            Map<String, SensorML> childProcedureIds) throws OwsExceptionReport;

}
