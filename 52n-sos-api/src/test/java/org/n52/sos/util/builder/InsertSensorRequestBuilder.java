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
import java.util.List;

import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.ogc.swe.SosMetadata;
import org.n52.sos.request.InsertSensorRequest;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class InsertSensorRequestBuilder {

	private SosProcedureDescription procedureDescription;
	private List<String> observableProperties;
	private List<String> observationTypes;
	private ArrayList<SosFeatureRelationship> featureRelationships;
	
	public static InsertSensorRequestBuilder anInsertSensorRequest()
	{
		return new InsertSensorRequestBuilder();
	}

	public InsertSensorRequestBuilder setProcedure(SosProcedureDescription procedureDescription)
	{
		this.procedureDescription = procedureDescription;
		return this;
	}


	public InsertSensorRequestBuilder addObservableProperty(String observableProperty)
	{
		if (observableProperties == null)
		{
			observableProperties = new ArrayList<String>();
		}
		observableProperties.add(observableProperty);
		return this;
	}
	
	public InsertSensorRequestBuilder addObservationType(String observationType)
	{
		if (observationTypes == null)
		{
			observationTypes = new ArrayList<String>();
		}
		observationTypes.add(observationType);
		return this;
	}
	
	public InsertSensorRequestBuilder addRelatedFeature(SosAbstractFeature relatedFeature, String featureRole)
	{
		if (featureRelationships == null) 
		{
			featureRelationships = new ArrayList<SosFeatureRelationship>();
		}
		SosFeatureRelationship rel = new SosFeatureRelationship(); 
		rel.setFeature(relatedFeature);
		rel.setRole(featureRole);
		featureRelationships.add(rel);
		return this;
	}
	
	public InsertSensorRequest build()
	{
		InsertSensorRequest request = new InsertSensorRequest();
		if (procedureDescription != null)
		{
			request.setProcedureDescription(procedureDescription);
		}
		if (observableProperties != null && !observableProperties.isEmpty())
		{
			request.setObservableProperty(observableProperties);
		}
		if (featureRelationships != null && !featureRelationships.isEmpty())
		{
			request.setRelatedFeature(featureRelationships);
		}
		SosMetadata meta = null;
		if (observationTypes != null && !observationTypes.isEmpty())
		{
			meta = new SosMetadata();
			meta.setObservationTypes(observationTypes);
		}
		if (meta != null)
		{
			request.setMetadata(meta);
		}
		return request;
	}

}
