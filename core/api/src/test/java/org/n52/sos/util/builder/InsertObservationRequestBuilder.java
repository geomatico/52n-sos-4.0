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
package org.n52.sos.util.builder;

import java.util.ArrayList;

import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.request.InsertObservationRequest;



/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class InsertObservationRequestBuilder
{
	
	
	
	public static InsertObservationRequestBuilder aInsertObservationRequest()
	{
		return new InsertObservationRequestBuilder();
	}

	private String procedureId;
	private ArrayList<String> offerings;
	private ArrayList<OmObservation> observations;

	public InsertObservationRequestBuilder setProcedureId(String procedureId)
	{
		this.procedureId = procedureId;
		return this;
	}

	public InsertObservationRequestBuilder addOffering(String offeringId)
	{
		if (offerings == null)
		{
			offerings = new ArrayList<String>();
		}
		offerings.add(offeringId);
		return this;
	}

	public InsertObservationRequestBuilder addObservation(OmObservation sosObservation)
	{
		if (observations == null)
		{
			observations = new ArrayList<OmObservation>(1);
		}
		observations.add(sosObservation);
		return this;
	}

	public InsertObservationRequest build()
	{
		InsertObservationRequest request = new InsertObservationRequest();
		request.setObservation(observations);
		request.setOfferings(offerings);
		request.setAssignedSensorId(procedureId);
		return request;
	}

}
