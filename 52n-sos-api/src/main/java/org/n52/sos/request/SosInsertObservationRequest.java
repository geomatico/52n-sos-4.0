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

import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.operator.RequestOperatorKeyType;

/**
 * SOS InsertObservation request
 * 
 */
public class SosInsertObservationRequest extends AbstractServiceRequest {

	/**
	 * InsertObservation operation name
	 */
	private final String operationName = SosConstants.Operations.InsertObservation
			.name();

	/**
	 * Assigned sensor id
	 */
	private String assignedSensorId;

	/**
	 * SOS observation collection with observations to insert
	 */
	private SosObservationCollection observations;

	/**
	 * constructor
	 * 
	 * @param assignedSensorId
	 *            assigned sensor id
	 * @param observations
	 *            Observations to insert
	 */
	public SosInsertObservationRequest(String assignedSensorId,
			SosObservationCollection observations) {
		this.assignedSensorId = assignedSensorId;
		this.observations = observations;
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
	public SosObservationCollection getObservation() {
		return observations;
	}

	/**
	 * Set observations to insert
	 * 
	 * @param observation
	 *            observations to insert
	 */
	public void setObservation(SosObservationCollection observation) {
		this.observations = observation;
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
