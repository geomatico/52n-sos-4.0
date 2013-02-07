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
package org.n52.sos.cache.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added and some settings are updated in cache:<ul>
 * <li>Procedure</li>
 * <li>Offering &harr; procedure</li>
 * <li>Offering &rarr; name</li></ul>
 * <li>Offering &rarr; allowed observation type</li>
 * <li>Offering &rarr; related feature</li>
 * <li>Related features &rarr; role</li>
 * <li>Observable Property &harr; Procedure</li>
 * <li>Offering &harr; observable property</li>
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 *
 */
public class SensorInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate {

	private static final Logger LOGGER = LoggerFactory.getLogger(SensorInsertionInMemoryCacheUpdate.class);
	
	private InsertSensorResponse sosResponse;
	private InsertSensorRequest sosRequest;

	public SensorInsertionInMemoryCacheUpdate(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse) {
		if (sosRequest == null || sosResponse == null)
		{
			String msg = String.format("Missing argument: '{}': {}; '{}': {}", 
					InsertSensorRequest.class.getName(),
					sosRequest,
					InsertSensorResponse.class.getName(),
					sosResponse);
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.sosResponse = sosResponse;
		this.sosRequest = sosRequest;
	}

	@Override
	public void execute()
	{
		// procedure relations
		addProcedureToCache(sosResponse.getAssignedProcedure());
		addOfferingToProcedureRelation(sosResponse.getAssignedOffering(), sosResponse.getAssignedProcedure());
		addProcedureToOfferingRelation(sosResponse.getAssignedProcedure(), sosResponse.getAssignedOffering());

		// offering name
		for (SosOffering sosOffering : sosRequest.getProcedureDescription().getOfferingIdentifiers()) {
			addOfferingNameToCache(sosOffering);
		}

		// allowed observation types
		addAllowedObservationTypes(sosResponse.getAssignedOffering(), sosRequest.getMetadata().getObservationTypes());

		// related features
		addRelatedFeatures(sosRequest.getRelatedFeatures(), sosResponse.getAssignedOffering());
		addRelatedFeatureRoles(sosRequest.getRelatedFeatures());

		// observable property relations
		for (String observableProperty : sosRequest.getObservableProperty()) {
			addObservablePropertyToProcedureRelation(observableProperty, sosResponse.getAssignedProcedure());
			addProcedureToObservablePropertyRelation(sosResponse.getAssignedProcedure(), observableProperty);
			addObservablePropertiesToOfferingRelation(observableProperty, sosResponse.getAssignedOffering());
			addOfferingToObservablePropertyRelation(sosResponse.getAssignedOffering(), observableProperty);
		}
	}
	
	private void addRelatedFeatures(List<SosFeatureRelationship> relatedFeatures,
			String offeringId)
	{
		if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
			if (getCache().getKOfferingVRelatedFeatures().get(offeringId) == null) {
				Collection<String> relatedFeatureIdentifiers = new ArrayList<String>(relatedFeatures.size());
				for (SosFeatureRelationship sosFeatureRelationship : relatedFeatures) {
					relatedFeatureIdentifiers.add(getFeatureIdentifier(sosFeatureRelationship));
				}
				getCache().getKOfferingVRelatedFeatures().put(offeringId, relatedFeatureIdentifiers);
			} else {
				for (SosFeatureRelationship sosFeatureRelationship : relatedFeatures) {
					if (!getCache().getKOfferingVRelatedFeatures().get(offeringId).contains(getFeatureIdentifier(sosFeatureRelationship))) {
						getCache().getKOfferingVRelatedFeatures().get(offeringId).add(getFeatureIdentifier(sosFeatureRelationship));
					}
				}
			}
		}
	}
	
	private String getFeatureIdentifier(SosFeatureRelationship sosFeatureRelationship)
	{
		return sosFeatureRelationship.getFeature().getIdentifier().getValue();
	}
	
	private void addAllowedObservationTypes(String assignedOffering,
			List<String> observationTypes)
	{
		if (!getCache().getAllowedKOfferingVObservationType().containsKey(assignedOffering)) {
			getCache().getAllowedKOfferingVObservationType().put(assignedOffering, observationTypes);
		} else {
			for (String observationType : observationTypes) {
				getCache().getAllowedKOfferingVObservationType().get(assignedOffering).add(observationType);
			}
		}
	}
	
	private void addRelatedFeatureRoles(List<SosFeatureRelationship> relatedFeatures)
	{
		if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
			for (SosFeatureRelationship featureRelation : relatedFeatures) {
				// add new
				if (!getCache().getKRelatedFeatureVRole().containsKey(getFeatureIdentifier(featureRelation))) {
					List<String> roles = new ArrayList<String>(1);
					roles.add(featureRelation.getRole());
					getCache().getKRelatedFeatureVRole().put(getFeatureIdentifier(featureRelation), roles);
				}
				// update
				else if (!getCache().getKRelatedFeatureVRole().get(getFeatureIdentifier(featureRelation)).contains(featureRelation.getRole())) {
					getCache().getKRelatedFeatureVRole().get(getFeatureIdentifier(featureRelation)).add(featureRelation.getRole());
				}
			}
		}
	}
	
	private void addOfferingNameToCache(SosOffering sosOffering)
	{
		if (!getCache().getKOfferingVName().containsKey(sosOffering.getOfferingIdentifier())) {
			getCache().getKOfferingVName().put(sosOffering.getOfferingIdentifier(), sosOffering.getOfferingName());
			LOGGER.debug("Added offering '{}' to name '{}' relation to cache? {}",
					sosOffering.getOfferingIdentifier(),
					sosOffering.getOfferingName());
		}
	}

}
