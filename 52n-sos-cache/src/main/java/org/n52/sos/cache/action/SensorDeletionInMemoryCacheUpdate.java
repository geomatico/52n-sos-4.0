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
import java.util.Map.Entry;

import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.util.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When executing this &auml;ction (see {@link Action}), the following relations are deleted, settings are updated in cache:<ul>
 * <li>Result template</li>
 * <li>Offering &rarr; Result template</li></ul>
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class SensorDeletionInMemoryCacheUpdate extends InMemoryCacheUpdate
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SensorDeletionInMemoryCacheUpdate.class);
	
	private final DeleteSensorRequest sosRequest;

	public SensorDeletionInMemoryCacheUpdate(DeleteSensorRequest sosRequest)
	{
		if (sosRequest == null)
		{
			String msg = String.format("Missing argument: '{}': {}", 
					DeleteSensorRequest.class.getName(),
					sosRequest);
			LOGGER.error(msg);
			throw new IllegalArgumentException(msg);
		}
		this.sosRequest = sosRequest;
	}

	@Override
	public void execute()
	{
		removeProcedureFromCache(sosRequest.getProcedureIdentifier());
		
		removeFeatureToProcedureRelationsFromCache(sosRequest.getProcedureIdentifier());
		
		removeOfferingsToProcedureRelation(sosRequest.getProcedureIdentifier());
		
		removeProcedureToObservationIdentifierRelations(sosRequest.getProcedureIdentifier());

		for (String offeringId : getCache().getKProcedureVOffering().get(sosRequest.getProcedureIdentifier()))
		{
			removeTemporalBoundingBoxFromCache(offeringId);
		
			removeOfferingName(offeringId);

			removeOfferingToFeaturesRelations(offeringId);

			removeOfferingToRelatedFeaturesRelations(offeringId);

			removeOfferingToObservationTypesRelations(offeringId);

			removeOfferingEnvelope(offeringId);
			
//			removeObservablePropertiesToOfferingRelation(offeringId);
			
			removeOfferingToObservablePropertyRelation(offeringId);
			
			removeOfferintgToResultTemplatesRelation(offeringId);
		}
		
		removeRemovedRelatedFeaturesFromRoleMap();
		
		removeRemovedFeaturesFromCache();
		
		removeRemovedObservationIdentifiers();
		
		if (areOfferingToTimeLimitMapsEmpty())
		{
			removeGlobalTemporalBoundingBox();
		}
		else
		{
			// TODO Eike: implement incl. tests
//			resetGlobalTemporalBoundingBox();
		}
		
		if (getCache().getKOfferingVEnvelope().isEmpty())
		{
			removeGlobalEnvelope();
		}
		else
		{
			// TODO Eike: implement incl. tests
//			resetGlobalSpatialBoundingBox();
		}
		// observable property relations
		removeObservablePropertyRelations(sosRequest.getProcedureIdentifier());
		
		// At the latest
		removeProcedureToOfferingsRelation(sosRequest.getProcedureIdentifier());
	}
	
	/* HELPER */
	
	private void removeOfferintgToResultTemplatesRelation(String offeringId)
	{
		Collection<String> resultTemplateIdentifiersToRemove = getCache().getKOfferingVResultTemplates().remove(offeringId);
		LOGGER.debug("offering '{}' to result templates removed from map? {}",
				offeringId,
				!getCache().getKOfferingVResultTemplates().containsKey(offeringId));
		
		if (resultTemplateIdentifiersToRemove != null)
		{
			for (String resultTemplateIdentiferToRemove : resultTemplateIdentifiersToRemove)
			{
				getCache().getResultTemplates().remove(resultTemplateIdentiferToRemove);
				LOGGER.debug("Removed result template identifier '{}' from cache? {}",
						resultTemplateIdentiferToRemove,
						!getCache().getResultTemplates().contains(resultTemplateIdentiferToRemove));
			}
		}
	}

	private void removeRemovedObservationIdentifiers()
	{
		List<String> allowedObservationIdentifiers = getAllowedEntries(getCache().getKProcedureVObservationIdentifiers().values());
		List<String> featuresToRemove = getEntriesToRemove(allowedObservationIdentifiers,getCache().getObservationIdentifiers());
		removeEntries(featuresToRemove,getCache().getObservationIdentifiers());
	}
	
	private void removeOfferingToObservablePropertyRelation(String offeringId)
	{
		getCache().getKOfferingVObservableProperties().remove(offeringId);
		LOGGER.debug("offering '{}' to observable properties relations removed? {}",
				offeringId,
				!getCache().getKOfferingVObservableProperties().containsKey(offeringId));
	}

	private void removeProcedureToObservationIdentifierRelations(String procedureIdentifier)
	{
		getCache().getKProcedureVObservationIdentifiers().remove(procedureIdentifier);
		LOGGER.debug("observation identifiers removed for procedure '{}'? {}",
				procedureIdentifier,
				!getCache().getKProcedureVObservationIdentifiers().containsKey(procedureIdentifier));
	}

	private void removeFeatureToProcedureRelationsFromCache(String procedureIdentifier)
	{
		List<String> featuresToRemove = new ArrayList<String>();
		for (String feature : getCache().getKFeatureOfInterestVProcedures().keySet())
		{
			getCache().getKFeatureOfInterestVProcedures().get(feature).remove(procedureIdentifier);
			LOGGER.debug("removed feature '{}' -> procedure '{}' relation from cache? {}",
					feature,
					procedureIdentifier,
					!getCache().getKFeatureOfInterestVProcedures().get(feature).contains(procedureIdentifier));
			
			if (getCache().getKFeatureOfInterestVProcedures().get(feature) == null ||
					getCache().getKFeatureOfInterestVProcedures().get(feature).isEmpty())
			{
				featuresToRemove.add(feature);
			}
		}
		for (String featureToRemove : featuresToRemove) {
			getCache().getKFeatureOfInterestVProcedures().remove(featureToRemove);
			LOGGER.debug("removed feature '{}' from featur -> procedure map? {}",
					featureToRemove,
					procedureIdentifier,
					!getCache().getKFeatureOfInterestVProcedures().containsKey(featureToRemove));
		}
	}

	private void removeOfferingToObservationTypesRelations(String offeringId)
	{
		getCache().getKOfferingVObservationTypes().remove(offeringId);
		LOGGER.debug("observation types removed for offering '{}'? {}",
				offeringId,
				!getCache().getKOfferingVObservationTypes().containsKey(offeringId));
	}

	private void removeOfferingToFeaturesRelations(String offeringId)
	{
		getCache().getKOfferingVFeaturesOfInterest().remove(offeringId);
		LOGGER.debug("features removed for offering '{}'? {}",
				offeringId,
				!getCache().getKOfferingVFeaturesOfInterest().containsKey(offeringId));
	}
	
	private void removeRemovedFeaturesFromCache()
	{
		List<String> allowedFeatures = getAllowedEntries(getCache().getKOfferingVFeaturesOfInterest().values());
		List<String> featuresToRemove = getEntriesToRemove(allowedFeatures,getCache().getFeatureOfInterest());
		removeEntries(featuresToRemove,getCache().getFeatureOfInterest());
	}

	private void removeEntries(List<String> entriesToRemove, Collection<String> listToRemoveFrom)
	{
		for (String entryToRemove : entriesToRemove)
		{
			listToRemoveFrom.remove(entryToRemove);
			LOGGER.debug("entry '{}' removed from list in cache? {}",
					entryToRemove,
					!listToRemoveFrom.contains(entryToRemove));
		}
	}

	private List<String> getEntriesToRemove(List<String> allowedEntries, Collection<String> currentEntries)
	{
		List<String> entriesToRemove = new ArrayList<String>();
		for (String entry : currentEntries) 
		{
			if (!allowedEntries.contains(entry))
			{
				entriesToRemove.add(entry);
			}
		}
		return entriesToRemove;
	}

	private void removeOfferingName(String offeringId)
	{
		getCache().getKOfferingVName().remove(offeringId);
		LOGGER.debug("offering name removed for offering '{}'? {}",
				offeringId,
				!getCache().getKOfferingVName().containsKey(offeringId));
	}
	
	private List<String> getAllowedRelatedFeatures()
	{
		return getAllowedEntries(getCache().getKOfferingVRelatedFeatures().values());
	}

	private void removeRemovedRelatedFeaturesFromRoleMap()
	{
		List<String> allowedRelatedFeatures = getAllowedRelatedFeatures();
		List<String> featuresToRemove = getEntriesToRemove(allowedRelatedFeatures, 
				getCache().getKRelatedFeatureVRole().keySet());
		removeEntries(featuresToRemove, getCache().getKRelatedFeatureVRole().keySet());
	}

	private List<String> getAllowedEntries(Collection<Collection<String>> values)
	{
		List<String> allowedEntries = new ArrayList<String>();
		for (Collection<String> entries : values)
		{
			for (String entry : entries)
			{
				if (!allowedEntries.contains(entry))
				{
					allowedEntries.add(entry);
				}
			}
		}
		return allowedEntries;
	}

	private void removeOfferingToRelatedFeaturesRelations(String offeringId)
	{
		getCache().getKOfferingVRelatedFeatures().remove(offeringId);
		LOGGER.debug("Related features removed for offering '{}'? {}",
				offeringId,
				!getCache().getKOfferingVRelatedFeatures().containsKey(offeringId));
	}

	private void removeGlobalEnvelope()
	{
		getCache().setGlobalEnvelope(new SosEnvelope(null, getCache().getDefaultEPSGCode()));
		LOGGER.debug("global envelope: {}",getCache().getGlobalEnvelope());
	}

	private boolean areOfferingToTimeLimitMapsEmpty()
	{
		return getCache().getKOfferingVMinTime().isEmpty() &&
				getCache().getKOfferingVMaxTime().isEmpty();
	}

	private void removeGlobalTemporalBoundingBox()
	{
		getCache().setMaxEventTime(null);
		getCache().setMinEventTime(null);
		LOGGER.debug("Global temporal bounding box: max time: {}, min time: {}",
				getCache().getMaxEventTime(),
				getCache().getMinEventTime());
	}

	private void removeTemporalBoundingBoxFromCache(String offeringId)
	{
		getCache().getKOfferingVMaxTime().remove(offeringId);
		getCache().getKOfferingVMinTime().remove(offeringId);
		LOGGER.debug("Temporal boundingbox removed for offering '{}'? max time: {}; min time: {}",
				offeringId,
				!getCache().getKOfferingVMaxTime().containsKey(offeringId),
				!getCache().getKOfferingVMinTime().containsKey(offeringId));
	}

	private void removeOfferingEnvelope(String offeringId)
	{
		getCache().getKOfferingVEnvelope().remove(offeringId);
		LOGGER.debug("Envelope removed for offering '{}'? {}",
				offeringId,
				!getCache().getKOfferingVEnvelope().containsKey(offeringId));
	}
	
	private void removeObservablePropertyRelations(String procedureIdentifier)
	{
		for (String observableProperty : getCache().getKProcedureVObservableProperties().get(procedureIdentifier)) {
			removeObservablePropertyToProcedureRelation(observableProperty, procedureIdentifier);
			removeProcedureToObservablePropertyRelations(procedureIdentifier);
		}
	}

	private void removeProcedureToObservablePropertyRelations(String procedureIdentifier)
	{
		getCache().getKProcedureVObservableProperties().remove(procedureIdentifier);
		LOGGER.debug("Removed procedure to observable properties relations from cache for procedure '{}'? {}",
				procedureIdentifier,
				!getCache().getKProcedureVObservableProperties().containsKey(procedureIdentifier));
	}

	private void removeObservablePropertyToProcedureRelation(String observableProperty,
			String procedureIdentifier)
	{
		if (getCache().getKObservablePropertyVProcedures().get(observableProperty).remove(procedureIdentifier))
		{
			LOGGER.debug("Removed observable property '{}' -> procedure '{}' relation from cache",
					observableProperty,procedureIdentifier);
			if (getCache().getKObservablePropertyVProcedures().get(observableProperty) != null &&
					getCache().getKObservablePropertyVProcedures().get(observableProperty).isEmpty() &&
					getCache().getKObservablePropertyVProcedures().remove(observableProperty) == null)
			{
				LOGGER.debug("Removed entry for observable property '{}' from cache map",observableProperty);
			}
		}
	}

	private void removeOfferingsToProcedureRelation(String procedureIdentifier)
	{
		List<String> offeringsToRemove = new ArrayList<String>();
		for (Entry<String, List<String>> offeringToProcedureRelation : getCache().getKOfferingVProcedures().entrySet()) {
			if (offeringToProcedureRelation.getValue().remove(procedureIdentifier))
			{
				LOGGER.debug("procedure to offering relation removed for '{}'->'{}'",
						procedureIdentifier,
						offeringToProcedureRelation.getKey());
				if (offeringToProcedureRelation.getValue().isEmpty())
				{
					offeringsToRemove.add(offeringToProcedureRelation.getKey());
				}
			}
		}
		// this to by-pass concurrent modification exceptions
		for (String offeringToRemove : offeringsToRemove) {
			getCache().getKOfferingVProcedures().remove(offeringToRemove);
			LOGGER.debug("offering '{}' removed from offering->procedure map ? {}",
					offeringsToRemove,
					!getCache().getKOfferingVProcedures().containsKey(offeringToRemove));
		}
	}

	private void removeProcedureToOfferingsRelation(String procedureIdentifer)
	{
		getCache().getKProcedureVOffering().remove(procedureIdentifer);
		LOGGER.debug("procedure to offerings relation removed from cache for procedure '{}'? {}",
					procedureIdentifer,
					!getCache().getKProcedureVOffering().containsKey(procedureIdentifer));
	}

	private void removeProcedureFromCache(String procedureIdentifier)
	{
		getCache().getProcedures().remove(procedureIdentifier);
		LOGGER.debug("Procedure '{}' removed from list of procedures? {}",
				procedureIdentifier,
				!getCache().getProcedures().contains(procedureIdentifier));
	}

}
