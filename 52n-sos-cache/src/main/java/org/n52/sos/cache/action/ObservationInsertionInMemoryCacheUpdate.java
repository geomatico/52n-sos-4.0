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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are added, settings are updated in cache:<ul>
 * <li>Procedure</li>
 * <li>Observation Type</li>
 * <li>Observation identifier (OPTIONAL)</li>
 * <li>Procedure &rarr; Observation identifier (OPTIONAL)</li>
 * <li>Observable Property &harr; Procedure</li>
 * <li>Global spatiakl bounding box</li>
 * <li>Feature identifier</li>
 * <li>Feature types</li>
 * <li>Feature &harr; procedure</li>
 * <li>Feature &harr; feature</li>
 * <li>Offering &harr; related feature</li>
 * <li>Offering &harr; procedure</li>
 * <li>Offering &harr; observable property</li>
 * <li>Offering &rarr; observation type</li>
 * <li>Offering &rarr; temporal bounding box</li>
 * <li>Offering &rarr; spatial bounding box</li>
 * <li>Global temporal bounding box</li></ul>
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 *
 */
public class ObservationInsertionInMemoryCacheUpdate extends InMemoryCacheUpdate {

	private final InsertObservationRequest sosRequest;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ObservationInsertionInMemoryCacheUpdate.class);

	public ObservationInsertionInMemoryCacheUpdate(InsertObservationRequest sosRequest) {
		if (sosRequest == null)
		{
			String msg = String.format("Missing argument: '{}': {}", 
					InsertObservationRequest.class.getName(),
					sosRequest);
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.sosRequest = sosRequest;
	}

	@Override
	public void execute()
	{
		// TODO Review required methods and update test accordingly (@see SensorInsertionInMemoryCacheUpdate)
		// Always update the javadoc when changing this method!
		for (SosObservation sosObservation : sosRequest.getObservations())
		{
			addProcedureToCache(getProcedureIdentifier(sosObservation));
			addObservablePropertyToProcedureRelation(getObservablePropertyIdentifier(sosObservation), getProcedureIdentifier(sosObservation));
			addProcedureToObservablePropertyRelation(getProcedureIdentifier(sosObservation), getObservablePropertyIdentifier(sosObservation));

			updateGlobalTemporalBBoxUsingNew(phenomenonTimeFrom(sosObservation));

			addObservationTypeToCache(sosObservation);

			if (sosObservation.getIdentifier() != null)
			{
				addObservationIdToCache(sosObservation);
				addProcedureToObservationIdRelationToCache(getProcedureIdentifier(sosObservation),sosObservation.getIdentifier().getValue());
			}

			// update features
			int observedFeatureEnvelopeSRID = getCache().getDefaultEPSGCode();
			List<SosSamplingFeature> observedFeatures = sosFeaturesToList(sosObservation.getObservationConstellation().getFeatureOfInterest());

			Envelope observedFeatureEnvelope = createEnvelopeFrom(observedFeatures);
			updateGlobalEnvelopeUsing(observedFeatureEnvelope);

			for (SosSamplingFeature sosSamplingFeature : observedFeatures)
			{
				String observedFeatureIdentifier = sosSamplingFeature.getIdentifier().getValue();

				addFeatureIdentifierToCache(observedFeatureIdentifier);
				addFeatureTypeToCache(sosSamplingFeature.getFeatureType());
				addProcedureToFeatureRelationToCache(getProcedureIdentifier(sosObservation), observedFeatureIdentifier);
				addFeatureToProcedureRelationToCache(observedFeatureIdentifier, getProcedureIdentifier(sosObservation));
				updateInterFeatureRelations(sosSamplingFeature);
				for (String offeringIdentifier : sosRequest.getOfferings())
				{
					addOfferingRelatedFeatureRelationToCache(observedFeatureIdentifier, offeringIdentifier);
					addOfferingToFeatureRelationToCache(observedFeatureIdentifier, offeringIdentifier);
				}
			}

			// update offerings
			for (String offeringIdentifier : sosRequest.getOfferings())
			{
				// procedure
				addOfferingToProcedureRelation(offeringIdentifier, getProcedureIdentifier(sosObservation));
				addProcedureToOfferingRelation(getProcedureIdentifier(sosObservation), offeringIdentifier);
				// observable property
				addObservablePropertiesToOfferingRelation(getObservablePropertyIdentifier(sosObservation), offeringIdentifier);
				addOfferingToObservablePropertyRelation(offeringIdentifier, getObservablePropertyIdentifier(sosObservation));
				// observation type
				addOfferingToObservationTypeRelation(offeringIdentifier, sosObservation.getObservationConstellation().getObservationType());
				// envelopes/bounding boxes (spatial and temporal)
				updateTemporalBoundingBoxOf(offeringIdentifier, phenomenonTimeFrom(sosObservation));
				updateOfferingEnvelope(observedFeatureEnvelope, observedFeatureEnvelopeSRID, offeringIdentifier);
			}

		}
	}
	
	private void addOfferingRelatedFeatureRelationToCache(String observedFeatureIdentifier,
			String offeringIdentifier)
	{
		// offering-relatedFeatures
		Map<String, Collection<String>> offeringRelatedFeaturesMap = getCache().getKOfferingVRelatedFeatures();
		if (offeringRelatedFeaturesMap != null) {
			if (offeringRelatedFeaturesMap.containsKey(offeringIdentifier) && !offeringRelatedFeaturesMap.get(offeringIdentifier).contains(observedFeatureIdentifier)) {
				// if offering is already there and feature not contained -> add
				// to list
				offeringRelatedFeaturesMap.get(offeringIdentifier).add(observedFeatureIdentifier);
			} else {
				// if not -> add new list
				ArrayList<String> relatedFeatures = new ArrayList<String>(1);
				relatedFeatures.add(observedFeatureIdentifier);
				offeringRelatedFeaturesMap.put(offeringIdentifier, relatedFeatures);
			}
		}
	}
	
	private void updateInterFeatureRelations(SosSamplingFeature sosSamplingFeature)
	{
		// add foi-foi relations
		// sampledFeatures are parentFeatures
		List<SosAbstractFeature> parentFeatures = sosSamplingFeature.getSampledFeatures();
		List<String> parentFeaturesIdentifiers = new ArrayList<String>(parentFeatures.size());
		for (SosAbstractFeature sosAbstractFeature : parentFeatures) {
			parentFeaturesIdentifiers.add(sosAbstractFeature.getIdentifier().getValue());
		}
		if (parentFeatures != null && !parentFeatures.isEmpty()) {
			Collection<String> parentFeaturesFromCache = null;

			updateParentFeatures(sosSamplingFeature.getIdentifier().getValue(), parentFeaturesIdentifiers, parentFeaturesFromCache);
			updateChildFeatures(sosSamplingFeature.getIdentifier().getValue(), parentFeaturesFromCache);
		}
	}
	
	private void updateChildFeatures(String observedFeatureIdentifier,
			Collection<String> parentFeaturesFromCache)
	{
		// update child features
		Map<String, Collection<String>> childFeaturesMapFromCache = getCache().getChildFeatures();
		if (childFeaturesMapFromCache != null && !childFeaturesMapFromCache.isEmpty() && parentFeaturesFromCache != null) {
			// 1 check for the identifier of the parent features of this feature
			// of this observation
			for (String parentFeatureIdentifier : parentFeaturesFromCache) {
				// if not -> add new list with one element
				if (!childFeaturesMapFromCache.containsKey(parentFeatureIdentifier)) {
					ArrayList<String> newChildList = new ArrayList<String>(1);
					newChildList.add(observedFeatureIdentifier);
					childFeaturesMapFromCache.put(parentFeatureIdentifier, newChildList);
				} else // if yes -> get list and update if required
				{
					Collection<String> childFeatures = childFeaturesMapFromCache.get(parentFeatureIdentifier);
					if (!childFeatures.contains(observedFeatureIdentifier)) {
						childFeatures.add(observedFeatureIdentifier);
					}
				}
			}
		}
	}

	private void updateParentFeatures(String observedFeatureIdentifier,
			List<String> parentFeaturesIdentifiers,
			Collection<String> parentFeaturesFromCache)
	{
		// update parent features
		Map<String, Collection<String>> parentFeaturesMapFromCache = getCache().getParentFeatures();
		// 1 are parent features already available for this feature
		if (parentFeaturesMapFromCache != null && !parentFeaturesMapFromCache.isEmpty() && parentFeaturesMapFromCache.containsKey(observedFeatureIdentifier)) {
			// if yes -> check list and add all not already contained ones
			parentFeaturesFromCache = parentFeaturesMapFromCache.get(observedFeatureIdentifier);
			for (String parentFeatureIdentifier : parentFeaturesIdentifiers) {
				if (!parentFeaturesFromCache.contains(parentFeatureIdentifier)) {
					parentFeaturesFromCache.add(parentFeatureIdentifier);
				}
			}
		} else // if not -> add parent features and done
		{
			parentFeaturesMapFromCache.put(observedFeatureIdentifier, parentFeaturesIdentifiers);
		}
	}

	private void addProcedureToFeatureRelationToCache(String procedureIdentifier,
			String observedFeatureIdentifier)
	{
		if (getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier) == null)
		{
			getCache().getKFeatureOfInterestVProcedures().put(observedFeatureIdentifier, Collections.synchronizedList(new ArrayList<String>()));
		}
		if (!getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier).contains(procedureIdentifier))
		{
			getCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier).add(procedureIdentifier);
		}
	}
	
}
