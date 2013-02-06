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
package org.n52.sos.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.n52.sos.cache.action.InMemoryCacheUpdate;
import org.n52.sos.cache.action.ResultTemplateInsertionInMemoryCacheUpdate;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * <b>TODO</b> add more log statements for debug level on
 *         failed or successful operation<br />
 * <b>TODO</b> use commands design as in {@link CacheControllerImpl}      
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk
 *         J&uuml;rrens</a> 
 */
public class InMemoryCacheController extends CacheControllerImpl {

	private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheController.class);

	public void updateAfterSensorInsertion(InsertSensorRequest sosRequest,
			InsertSensorResponse sosResponse) throws OwsExceptionReport
	{
		update(Case.SENSOR_INSERTION, sosRequest, sosResponse);
	}

	/**
	 * Updates the underlying capabilities cache without accessing the data
	 * store
	 */
	public void updateAfterObservationInsertion(InsertObservationRequest sosRequest) throws OwsExceptionReport
	{
		update(Case.OBSERVATION_INSERTION, sosRequest);
	}

	/*
	 * Updates the underlying capabilities cache without accessing the data
	 * store
	 */
	public void updateAfterSensorDeletion(DeleteSensorRequest sosRequest) throws OwsExceptionReport
	{
		update(Case.SENSOR_DELETION, sosRequest);
	}

	public void updateAfterResultTemplateInsertion(InsertResultTemplateResponse sosResponse) throws OwsExceptionReport
	{
//		update(Case.RESULT_TEMPLATE_INSERTION,null,sosResponse);
		update(new ResultTemplateInsertionInMemoryCacheUpdate(sosResponse));
	}

	public void updateAfterResultInsertion(SosObservation observation)
	{
		doUpdateAfterResultInsertion(observation);
	}

	private void update(Case observationInsertion,
			AbstractServiceRequest sosRequest) throws OwsExceptionReport
	{
		update(observationInsertion, sosRequest, null);
	}

	/**
	 * TODO Eike: continue implementation here: Call this method from public methods and create the required actions 
	 */
	private void update(InMemoryCacheUpdate cacheUpdate)
	{
		if (cacheUpdate == null ) {
			String errorMsg = String.format("Missing argument: InMemoryCacheUpdate: '%s'",
					cacheUpdate);
			LOGGER.warn(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
		boolean timeNotElapsed = true;
		try {
			// thread safe updating of the cache map
			timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

			// has waiting for lock got a time out?
			if (!timeNotElapsed) {
				LOGGER.warn("\n******\n{} not successful because of time out while waiting for update lock." + "\nWaited {} milliseconds.\n******\n", 
						cacheUpdate, 
						SosConstants.UPDATE_TIMEOUT);
				return;
			}
			while (!isUpdateIsFree()) {
				getUpdateFree().await();
			}
			setUpdateIsFree(false);
			cacheUpdate.setCache(getCapabilitiesCache());
			cacheUpdate.execute();
		} catch (InterruptedException e) {
			LOGGER.error("Problem while threadsafe capabilities cache update", e);
		} finally {
			if (timeNotElapsed) {
				getUpdateLock().unlock();
				setUpdateIsFree(true);
			}
		}
	}
	
	private void update(Case c,
			AbstractServiceRequest sosRequest,
			AbstractServiceResponse sosResponse) throws OwsExceptionReport
	{
		if (c == null || 
				(sosRequest == null && sosResponse == null) || 
				(sosRequest instanceof InsertSensorRequest && (sosResponse == null))) {
			String errorMsg = String.format("Missing arguments: Case: %s, AbstractServiceRequest: %s, AbstractServiceResponse: %s", c, sosRequest, sosResponse);
			LOGGER.warn(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
		boolean timeNotElapsed = true;
		try {
			// thread safe updating of the cache map
			timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

			// has waiting for lock got a time out?
			if (!timeNotElapsed) {
				LOGGER.warn("\n******\nupdate after {} not successful because of time out while waiting for update lock." + "\nWaited {} milliseconds.\n******\n", c, SosConstants.UPDATE_TIMEOUT);
				return;
			}
			while (!isUpdateIsFree()) {
				getUpdateFree().await();
			}
			setUpdateIsFree(false);
			switch (c) {
			case OBSERVATION_DELETION:
				throw new RuntimeException("NOT IMPLEMENTED");
				// break;
			case SENSOR_DELETION:
				doUpdateAfterSensorDeletion((DeleteSensorRequest) sosRequest);
				break;
			case OBSERVATION_INSERTION:
				doUpdateAfterObservationInsertion((InsertObservationRequest) sosRequest);
				break;
			case SENSOR_INSERTION:
				doUpdateAfterSensorInsertion((InsertSensorRequest) sosRequest, (InsertSensorResponse) sosResponse);
				break;
			}

		} catch (InterruptedException e) {
			LOGGER.error("Problem while threadsafe capabilities cache update", e);
		} finally {
			if (timeNotElapsed) {
				getUpdateLock().unlock();
				setUpdateIsFree(true);
			}
		}
	}

	/* WORKER */
	
	private void doUpdateAfterSensorInsertion(InsertSensorRequest sosRequest,
			InsertSensorResponse sosResponse)
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

	private void doUpdateAfterObservationInsertion(InsertObservationRequest sosRequest)
	{
		// update cache maps
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
			int observedFeatureEnvelopeSRID = getDefaultEPSG();
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
					addOfferingFeatureRelationToCache(observedFeatureIdentifier, offeringIdentifier);
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

	private void doUpdateAfterSensorDeletion(DeleteSensorRequest sosRequest)
		{
			removeProcedureFromCache(sosRequest.getProcedureIdentifier());
			
			removeFeatureToProcedureRelationsFromCache(sosRequest.getProcedureIdentifier());
			
			removeOfferingsToProcedureRelation(sosRequest.getProcedureIdentifier());
			
			removeProcedureToObservationIdentifierRelations(sosRequest.getProcedureIdentifier());
	
			for (String offeringId : getCapabilitiesCache().getOfferings4Procedure(sosRequest.getProcedureIdentifier()))
			{
				removeTemporalBoundingBoxFromCache(offeringId);
			
				removeOfferingName(offeringId);
	
				removeOfferingToFeaturesRelations(offeringId);
	
				removeOfferingToRelatedFeaturesRelations(offeringId);
	
				removeOfferingToObservationTypesRelations(offeringId);
	
				removeOfferingEnvelope(offeringId);
				
	//			removeObservablePropertiesToOfferingRelation(offeringId);
				
				removeOfferingToObservablePropertyRelation(offeringId);
			}
			
			removeRemovedRelatedFeaturesFromRoleMap();
			
			removeRemovedFeaturesFromCache();
			
			removeRemovedObservationIdentifiers();
			
			if (areOfferingToTimeLimitMapsEmpty())
			{
				removeGlobalTemporalBoundingBox();
			}
			
			if (getCapabilitiesCache().getKOfferingVEnvelope().isEmpty())
			{
				removeGlobalEnvelope();
			}
			
			// observable property relations
			removeObservablePropertyRelations(sosRequest.getProcedureIdentifier());
			
			// At the latest
			removeProcedureToOfferingsRelation(sosRequest.getProcedureIdentifier());
			
		}

	private void addProcedureToObservationIdRelationToCache(String procedureIdentifier, String observationIdentifier)
	{
		if (!getCapabilitiesCache().getKProcedureVObservationIdentifiers().containsKey(procedureIdentifier))
		{
			Collection<String> value = Collections.synchronizedList(new ArrayList<String>());
			getCapabilitiesCache().getKProcedureVObservationIdentifiers().put(procedureIdentifier, value);
		}
		getCapabilitiesCache().getKProcedureVObservationIdentifiers().get(procedureIdentifier).add(observationIdentifier);
		LOGGER.debug("procedure \"{}\" to observation id \"{}\" relation added to cache? {}",
				procedureIdentifier,
				observationIdentifier,
				getCapabilitiesCache().getKProcedureVObservationIdentifiers().get(procedureIdentifier).contains(observationIdentifier));
	}

	// TODO extract same behaviour as in doUpdateAfterObservationInsertion to methods 
	private void doUpdateAfterResultInsertion(SosObservation sosObservation)
	{
		if (sosObservation == null) {
			String errorMsg = String.format("Missing argument: SosObservation: %s", sosObservation);
			LOGGER.warn(errorMsg);
			throw new IllegalArgumentException(errorMsg);
		}
		boolean timeNotElapsed = true;
		try {
			// thread safe updating of the cache map
			timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

			// has waiting for lock got a time out?
			if (!timeNotElapsed) {
				LOGGER.warn("\n******\nupdate after InsertResult not successful because of time out while waiting for update lock." + "\nWaited {} milliseconds.\n******\n", SosConstants.UPDATE_TIMEOUT);
				return;
			}
			while (!isUpdateIsFree()) {
				getUpdateFree().await();
			}
			setUpdateIsFree(false);
			// do "real update" here
			addObservationTypeToCache(sosObservation);
			updateGlobalTemporalBoundingBox(sosObservation.getPhenomenonTime());
			addProcedureToCache(getProcedureIdentifier(sosObservation));
			addObservablePropertyToProcedureRelation(getObservablePropertyIdentifier(sosObservation), getProcedureIdentifier(sosObservation));
			addProcedureToObservablePropertyRelation(getProcedureIdentifier(sosObservation), getObservablePropertyIdentifier(sosObservation));
			
			if (sosObservation.getIdentifier() != null)
			{
				addObservationIdToCache(sosObservation);
				addProcedureToObservationIdRelationToCache(getProcedureIdentifier(sosObservation),sosObservation.getIdentifier().getValue());
			}
			
			List<SosSamplingFeature> observedFeatures = sosFeaturesToList(sosObservation.getObservationConstellation().getFeatureOfInterest());

			Envelope observedFeatureEnvelope = createEnvelopeFrom(observedFeatures);
			updateGlobalEnvelopeUsing(observedFeatureEnvelope);

			for (SosSamplingFeature sosSamplingFeature : observedFeatures)
			{
				String observedFeatureIdentifier = sosSamplingFeature.getIdentifier().getValue();
				addFeatureIdentifierToCache(observedFeatureIdentifier);
				addFeatureToProcedureRelationToCache(observedFeatureIdentifier, getProcedureIdentifier(sosObservation));
				addFeatureTypeToCache(sosSamplingFeature.getFeatureType());
				for (String offeringIdentifier : sosObservation.getObservationConstellation().getOfferings())
				{
					addOfferingFeatureRelationToCache(observedFeatureIdentifier, offeringIdentifier);
				}
			}
			for (String offeringIdentifier : sosObservation.getObservationConstellation().getOfferings())
			{
				addOfferingToProcedureRelation(offeringIdentifier, getProcedureIdentifier(sosObservation));
				addProcedureToOfferingRelation(getProcedureIdentifier(sosObservation), offeringIdentifier);
				updateOfferingEnvelope(observedFeatureEnvelope, getDefaultEPSG(), offeringIdentifier);
				updateTemporalBoundingBoxOf(offeringIdentifier, phenomenonTimeFrom(sosObservation));
				// observable property
				addObservablePropertiesToOfferingRelation(getObservablePropertyIdentifier(sosObservation), offeringIdentifier);
				addOfferingToObservablePropertyRelation(offeringIdentifier, getObservablePropertyIdentifier(sosObservation));
				// observation type
				addOfferingToObservationTypeRelation(offeringIdentifier, sosObservation.getObservationConstellation().getObservationType());
			}
			// End of "real update"

		} catch (InterruptedException e) {
			LOGGER.error("Problem while threadsafe capabilities cache update", e);
		} finally {
			if (timeNotElapsed) {
				getUpdateLock().unlock();
				setUpdateIsFree(true);
			}
		}
	}

	/* HELPER */
	
	private void updateGlobalTemporalBoundingBox(ITime phenomenonTime)
	{
		if (phenomenonTime instanceof TimeInstant)
		{
			updateGlobalTemporalBBoxUsingNew(new TimePeriod(((TimeInstant)phenomenonTime).getValue(), ((TimeInstant)phenomenonTime).getValue()));
		}
		else if (phenomenonTime instanceof TimePeriod)
		{
			updateGlobalTemporalBBoxUsingNew((TimePeriod) phenomenonTime);
		}
		else
		{
			// TODO throw exception?
			LOGGER.error("phenomenon time type '{}' is not supported implementation of '{}'",
					phenomenonTime.getClass().getName(),
					ITime.class.getName());
		}
	}
	
	private void removeRemovedObservationIdentifiers()
	{
		List<String> allowedObservationIdentifiers = getAllowedEntries(getCapabilitiesCache().getKProcedureVObservationIdentifiers().values());
		List<String> featuresToRemove = getEntriesToRemove(allowedObservationIdentifiers,getCapabilitiesCache().getObservationIdentifiers());
		removeEntries(featuresToRemove,getCapabilitiesCache().getObservationIdentifiers());
	}
	
	private void removeOfferingToObservablePropertyRelation(String offeringId)
	{
		getCapabilitiesCache().getKOfferingVObservableProperties().remove(offeringId);
		LOGGER.debug("offering \"{}\" to observable properties relations removed? {}",
				offeringId,
				getCapabilitiesCache().getKOfferingVObservableProperties().containsKey(offeringId));
	}

	private void removeProcedureToObservationIdentifierRelations(String procedureIdentifier)
	{
		getCapabilitiesCache().getKProcedureVObservationIdentifiers().remove(procedureIdentifier);
		LOGGER.debug("observation identifiers removed for procedure \"{}\"? {}",
				procedureIdentifier,
				getCapabilitiesCache().getKProcedureVObservationIdentifiers().containsKey(procedureIdentifier));
	}

	private void removeFeatureToProcedureRelationsFromCache(String procedureIdentifier)
	{
		List<String> featuresToRemove = new ArrayList<String>();
		for (String feature : getCapabilitiesCache().getKFeatureOfInterestVProcedures().keySet())
		{
			getCapabilitiesCache().getKFeatureOfInterestVProcedures().get(feature).remove(procedureIdentifier);
			LOGGER.debug("removed feature \"{}\" -> procedure \"{}\" relation from cache? {}",
					feature,
					procedureIdentifier,
					getCapabilitiesCache().getKFeatureOfInterestVProcedures().get(feature).contains(procedureIdentifier));
			if (getCapabilitiesCache().getKFeatureOfInterestVProcedures().get(feature) == null ||
					getCapabilitiesCache().getKFeatureOfInterestVProcedures().get(feature).isEmpty())
			{
				featuresToRemove.add(feature);
			}
		}
		for (String featureToRemove : featuresToRemove) {
			getCapabilitiesCache().getKFeatureOfInterestVProcedures().remove(featureToRemove);
			LOGGER.debug("removed feature \"{}\" from featur -> procedure map? {}",
					featureToRemove,
					procedureIdentifier,
					getCapabilitiesCache().getKFeatureOfInterestVProcedures().containsKey(featureToRemove));
		}
	}

	private void removeOfferingToObservationTypesRelations(String offeringId)
	{
		getCapabilitiesCache().getKOfferingVObservationTypes().remove(offeringId);
		LOGGER.debug("observation types removed for offering \"{}\"? {}",
				offeringId,
				getCapabilitiesCache().getKOfferingVObservationTypes().containsKey(offeringId));
	}

	private void removeOfferingToFeaturesRelations(String offeringId)
	{
		getCapabilitiesCache().getOffFeatures().remove(offeringId);
		LOGGER.debug("features removed for offering \"{}\"? {}",
				offeringId,
				getCapabilitiesCache().getOffFeatures().containsKey(offeringId));
	}
	
	private void removeRemovedFeaturesFromCache()
	{
		List<String> allowedFeatures = getAllowedEntries(getCapabilitiesCache().getOffFeatures().values());
		List<String> featuresToRemove = getEntriesToRemove(allowedFeatures,getCapabilitiesCache().getFeatureOfInterest());
		removeEntries(featuresToRemove,getCapabilitiesCache().getFeatureOfInterest());
	}

	private void removeEntries(List<String> entriesToRemove, Collection<String> listToRemoveFrom)
	{
		for (String entryToRemove : entriesToRemove)
		{
			listToRemoveFrom.remove(entryToRemove);
			LOGGER.debug("entry \"{}\" removed from list in cache? {}",
					entryToRemove,
					listToRemoveFrom.contains(entryToRemove));
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
		getCapabilitiesCache().getOffName().remove(offeringId);
		LOGGER.debug("offering name removed for offering \"{}\"? {}",
				offeringId,
				getCapabilitiesCache().getOffName().containsKey(offeringId));
	}

	private void removeRemovedRelatedFeaturesFromRoleMap()
	{
		List<String> allowedRelatedFeatures = getAllowedRelatedFeatures();
		List<String> featuresToRemove = getEntriesToRemove(allowedRelatedFeatures, 
				getCapabilitiesCache().getKRelatedFeatureVRole().keySet());
		removeEntries(featuresToRemove, getCapabilitiesCache().getKRelatedFeatureVRole().keySet());
	}

	protected List<String> getAllowedRelatedFeatures()
	{
		return getAllowedEntries(getCapabilitiesCache().getKOfferingVRelatedFeatures().values());
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
		getCapabilitiesCache().getKOfferingVRelatedFeatures().remove(offeringId);
		LOGGER.debug("Related features removed for offering \"{}\"? {}",
				offeringId,
				getCapabilitiesCache().getKOfferingVRelatedFeatures().containsKey(offeringId));
	}

	private void removeGlobalEnvelope()
	{
		getCapabilitiesCache().setGlobalEnvelope(new SosEnvelope(null, getSrid()));
		LOGGER.debug("global envelope: {}",getCapabilitiesCache().getGlobalEnvelope());
	}

	private boolean areOfferingToTimeLimitMapsEmpty()
	{
		return getCapabilitiesCache().getKOfferingVMinTime().isEmpty() &&
				getCapabilitiesCache().getKOfferingVMaxTime().isEmpty();
	}

	private void removeGlobalTemporalBoundingBox()
	{
		getCapabilitiesCache().setMaxEventTime(null);
		getCapabilitiesCache().setMinEventTime(null);
		LOGGER.debug("Global temporal bounding box: max time: {}, min time: {}",
				getCapabilitiesCache().getMaxEventTime(),
				getCapabilitiesCache().getMinEventTime());
	}

	private void removeTemporalBoundingBoxFromCache(String offeringId)
	{
		getCapabilitiesCache().getKOfferingVMaxTime().remove(offeringId);
		getCapabilitiesCache().getKOfferingVMinTime().remove(offeringId);
		LOGGER.debug("Temporal boundingbox removed for offering \"{}\"? max time: {}; min time: {}",
				offeringId,
				getCapabilitiesCache().getKOfferingVMaxTime().containsKey(offeringId),
				getCapabilitiesCache().getKOfferingVMinTime().containsKey(offeringId));
	}

	private void removeOfferingEnvelope(String offeringId)
	{
		getCapabilitiesCache().getKOfferingVEnvelope().remove(offeringId);
		LOGGER.debug("Envelope removed for offering \"{}\"? {}",
				offeringId,
				getCapabilitiesCache().getKOfferingVEnvelope().containsKey(offeringId));
	}
	
	private void removeObservablePropertyRelations(String procedureIdentifier)
	{
		for (String observableProperty : getCapabilitiesCache().getKProcedureVObservableProperties().get(procedureIdentifier)) {
			removeObservablePropertyToProcedureRelation(observableProperty, procedureIdentifier);
			removeProcedureToObservablePropertyRelations(procedureIdentifier);
		}
	}

	private void removeProcedureToObservablePropertyRelations(String procedureIdentifier)
	{
		getCapabilitiesCache().getKProcedureVObservableProperties().remove(procedureIdentifier);
		LOGGER.debug("Removed procedure to observable properties relations from cache for procedure \"{}\"? {}",
				procedureIdentifier,
				getCapabilitiesCache().getKProcedureVObservableProperties().containsKey(procedureIdentifier));
	}

	private void removeObservablePropertyToProcedureRelation(String observableProperty,
			String procedureIdentifier)
	{
		if (getCapabilitiesCache().getKObservablePropertyVProcedures().get(observableProperty).remove(procedureIdentifier))
		{
			LOGGER.debug("Removed observable property \"{}\" -> procedure \"{}\" relation from cache",
					observableProperty,procedureIdentifier);
			if (getCapabilitiesCache().getKObservablePropertyVProcedures().get(observableProperty) != null &&
					getCapabilitiesCache().getKObservablePropertyVProcedures().get(observableProperty).isEmpty() &&
					getCapabilitiesCache().getKObservablePropertyVProcedures().remove(observableProperty) == null)
			{
				LOGGER.debug("Removed entry for observable property \"{}\" from cache map",observableProperty);
			}
		}
	}

	private void removeOfferingsToProcedureRelation(String procedureIdentifier)
	{
		List<String> offeringsToRemove = new ArrayList<String>();
		for (Entry<String, List<String>> offeringToProcedureRelation : getCapabilitiesCache().getKOfferingVProcedures().entrySet()) {
			if (offeringToProcedureRelation.getValue().remove(procedureIdentifier))
			{
				LOGGER.debug("procedure to offering relation removed for \"{}\"->\"{}\"",
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
			getCapabilitiesCache().getKOfferingVProcedures().remove(offeringToRemove);
			LOGGER.debug("offering \"{}\" removed from offering->procedure map ? {}",
					offeringsToRemove,
					getCapabilitiesCache().getKOfferingVProcedures().containsKey(offeringToRemove));
		}
	}

	private void removeProcedureToOfferingsRelation(String procedureIdentifer)
	{
		getCapabilitiesCache().getKProcedureVOffering().remove(procedureIdentifer);
		LOGGER.debug("procedure to offerings relation removed from cache for procedure \"{}\"? {}",
					procedureIdentifer,
					getCapabilitiesCache().getKProcedureVOffering().containsKey(procedureIdentifer));
	}

	private void removeProcedureFromCache(String procedureIdentifier)
	{
		getCapabilitiesCache().getProcedures().remove(procedureIdentifier);
		LOGGER.debug("Procedure \"{}\" removed from list of procedures? {}",
				procedureIdentifier,
				getCapabilitiesCache().getProcedures().contains(procedureIdentifier));
	}

	private void addRelatedFeatureRoles(List<SosFeatureRelationship> relatedFeatures)
	{
		if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
			for (SosFeatureRelationship featureRelation : relatedFeatures) {
				// add new
				if (!getCapabilitiesCache().getKRelatedFeatureVRole().containsKey(getFeatureIdentifier(featureRelation))) {
					List<String> roles = new ArrayList<String>(1);
					roles.add(featureRelation.getRole());
					getCapabilitiesCache().getKRelatedFeatureVRole().put(getFeatureIdentifier(featureRelation), roles);
				}
				// update
				else if (!getCapabilitiesCache().getKRelatedFeatureVRole().get(getFeatureIdentifier(featureRelation)).contains(featureRelation.getRole())) {
					getCapabilitiesCache().getKRelatedFeatureVRole().get(getFeatureIdentifier(featureRelation)).add(featureRelation.getRole());
				}
			}
		}
	}

	private String getFeatureIdentifier(SosFeatureRelationship sosFeatureRelationship)
	{
		return sosFeatureRelationship.getFeature().getIdentifier().getValue();
	}

	private void addRelatedFeatures(List<SosFeatureRelationship> relatedFeatures,
			String offeringId)
	{
		if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
			if (getCapabilitiesCache().getKOfferingVRelatedFeatures().get(offeringId) == null) {
				Collection<String> relatedFeatureIdentifiers = new ArrayList<String>(relatedFeatures.size());
				for (SosFeatureRelationship sosFeatureRelationship : relatedFeatures) {
					relatedFeatureIdentifiers.add(getFeatureIdentifier(sosFeatureRelationship));
				}
				getCapabilitiesCache().getKOfferingVRelatedFeatures().put(offeringId, relatedFeatureIdentifiers);
			} else {
				for (SosFeatureRelationship sosFeatureRelationship : relatedFeatures) {
					if (!getCapabilitiesCache().getKOfferingVRelatedFeatures().get(offeringId).contains(getFeatureIdentifier(sosFeatureRelationship))) {
						getCapabilitiesCache().getKOfferingVRelatedFeatures().get(offeringId).add(getFeatureIdentifier(sosFeatureRelationship));
					}
				}
			}
		}
	}

	private void addAllowedObservationTypes(String assignedOffering,
			List<String> observationTypes)
	{
		if (!getCapabilitiesCache().getAllowedKOfferingVObservationType().containsKey(assignedOffering)) {
			getCapabilitiesCache().getAllowedKOfferingVObservationType().put(assignedOffering, observationTypes);
		} else {
			for (String observationType : observationTypes) {
				getCapabilitiesCache().getAllowedKOfferingVObservationType().get(assignedOffering).add(observationType);
			}
		}
	}

	private void addOfferingNameToCache(SosOffering offeringIdentifier)
	{
		if (!getCapabilitiesCache().getOffName().containsKey(offeringIdentifier.getOfferingIdentifier())) {
			getCapabilitiesCache().getOffName().put(offeringIdentifier.getOfferingIdentifier(), offeringIdentifier.getOfferingName());
		}
	}

	private void addObservationIdToCache(SosObservation sosObservation)
	{
		getCapabilitiesCache().getObservationIdentifiers().add(sosObservation.getIdentifier().getValue());
		LOGGER.debug("Added observation id '{}' to cache? {}",
				sosObservation.getIdentifier().getValue(),
				getCapabilitiesCache().getObservationIdentifiers().contains(sosObservation.getIdentifier().getValue()));
	}

	private void addObservationTypeToCache(SosObservation sosObservation)
	{
		if (!getObservationTypes().contains(sosObservation.getObservationConstellation().getObservationType())) {
			getCapabilitiesCache().getObservationTypes().add(sosObservation.getObservationConstellation().getObservationType());
		}
	}

	private void addOfferingToObservablePropertyRelation(String offeringIdentifier,
			String observablePropertyIdentifier)
	{
		// offering -> observableProperties
		if (getCapabilitiesCache().getKOfferingVObservableProperties().get(offeringIdentifier) == null) {
			List<String> propertiesForOffering = Collections.synchronizedList(new ArrayList<String>());
			propertiesForOffering.add(observablePropertyIdentifier);
			getCapabilitiesCache().getKOfferingVObservableProperties().put(offeringIdentifier, propertiesForOffering);
		} else if (!getCapabilitiesCache().getKOfferingVObservableProperties().get(offeringIdentifier).contains(observablePropertyIdentifier)) {
			getCapabilitiesCache().getKOfferingVObservableProperties().get(offeringIdentifier).add(observablePropertyIdentifier);
		}
	}

	private void addFeatureToProcedureRelationToCache(String observedFeatureIdentifier,
			String procedureIdentifier)
	{
		if (getCapabilitiesCache().getProceduresForFeature(observedFeatureIdentifier) == null) {
			List<String> procedures4Feature = Collections.synchronizedList(new ArrayList<String>());
			procedures4Feature.add(procedureIdentifier);
			getCapabilitiesCache().getKFeatureOfInterestVProcedures().put(observedFeatureIdentifier, procedures4Feature);
		} else if (!getCapabilitiesCache().getProceduresForFeature(observedFeatureIdentifier).contains(procedureIdentifier)) {
			getCapabilitiesCache().getProceduresForFeature(observedFeatureIdentifier).add(procedureIdentifier);
		}
	}

	private void addProcedureToOfferingRelation(String procedureIdentifier,
			String offeringIdentifier)
	{
		// offering-procedures
		if (getCapabilitiesCache().getKProcedureVOffering().get(procedureIdentifier) == null) {
			List<String> offerings4Procedure = Collections.synchronizedList(new ArrayList<String>());
			offerings4Procedure.add(offeringIdentifier);
			getCapabilitiesCache().getKProcedureVOffering().put(procedureIdentifier, offerings4Procedure);
		} else if (!getCapabilitiesCache().getKProcedureVOffering().get(procedureIdentifier).contains(offeringIdentifier)) {
			getCapabilitiesCache().getKProcedureVOffering().get(procedureIdentifier).add(offeringIdentifier);
		}
	}

	private void addProcedureToObservablePropertyRelation(String procedureIdentifier,
			String observablePropertyIdentifier)
	{
		if (getCapabilitiesCache().getKProcedureVObservableProperties().get(procedureIdentifier) == null) {
			List<String> relatedProperties = Collections.synchronizedList(new ArrayList<String>());
			relatedProperties.add(observablePropertyIdentifier);
			getCapabilitiesCache().getKProcedureVObservableProperties().put(procedureIdentifier, relatedProperties);
		} else if (!getCapabilitiesCache().getKProcedureVObservableProperties().get(procedureIdentifier).contains(observablePropertyIdentifier)) {
			getCapabilitiesCache().getKProcedureVObservableProperties().get(procedureIdentifier).add(observablePropertyIdentifier);
		}
	}

	private String getProcedureIdentifier(SosObservation sosObservation)
	{
		return sosObservation.getObservationConstellation().getProcedure().getProcedureIdentifier();
	}

	private String getObservablePropertyIdentifier(SosObservation sosObservation)
	{
		return sosObservation.getObservationConstellation().getObservableProperty().getIdentifier();
	}

	private void addObservablePropertyToProcedureRelation(String observablePropertyIdentifier,
			String procedureIdentifier)
	{
		if (getCapabilitiesCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier) == null)
		{
			List<String> relatedProcedures = Collections.synchronizedList(new ArrayList<String>());
			relatedProcedures.add(procedureIdentifier);
			getCapabilitiesCache().getKObservablePropertyVProcedures().put(observablePropertyIdentifier, relatedProcedures);
		}
		else if (!getCapabilitiesCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier).contains(procedureIdentifier))
		{
			getCapabilitiesCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier).add(procedureIdentifier);
		}
	}

	private void addFeatureTypeToCache(String featureType)
	{
		if (!getCapabilitiesCache().getFeatureOfInterestTypes().contains(featureType)) {
			getCapabilitiesCache().getFeatureOfInterestTypes().add(featureType);
		}
	}

	private void addProcedureToCache(String procedureIdentifier)
	{
		if (!getCapabilitiesCache().getProcedures().contains(procedureIdentifier)) {
			getCapabilitiesCache().getProcedures().add(procedureIdentifier);
		}
	}

	private void addOfferingFeatureRelationToCache(String observedFeatureIdentifier,
			String offeringIdentifier)
	{
		// offering-feature
		if (!getCapabilitiesCache().getOffFeatures().containsKey(offeringIdentifier)) {
			List<String> relatedFeatures = Collections.synchronizedList(new ArrayList<String>());
			relatedFeatures.add(observedFeatureIdentifier);
			getCapabilitiesCache().getOffFeatures().put(offeringIdentifier, relatedFeatures);
		} else if (!getCapabilitiesCache().getOffFeatures().get(offeringIdentifier).contains(observedFeatureIdentifier)) {
			getCapabilitiesCache().getOffFeatures().get(offeringIdentifier).add(observedFeatureIdentifier);
		}
	}

	private void addOfferingRelatedFeatureRelationToCache(String observedFeatureIdentifier,
			String offeringIdentifier)
	{
		// offering-relatedFeatures
		Map<String, Collection<String>> offeringRelatedFeaturesMap = getCapabilitiesCache().getKOfferingVRelatedFeatures();
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

	private void updateOfferingEnvelope(Envelope observedFeatureEnvelope,
			int observedFeatureEnvelopeSRID,
			String offeringIdentifier)
	{
		// offering-envelope
		if (getCapabilitiesCache().getKOfferingVEnvelope().containsKey(offeringIdentifier) && getCapabilitiesCache().getKOfferingVEnvelope().get(offeringIdentifier).isSetEnvelope()
				&& !getCapabilitiesCache().getKOfferingVEnvelope().get(offeringIdentifier).getEnvelope().contains(observedFeatureEnvelope)) {
			// update envelope
			getCapabilitiesCache().getKOfferingVEnvelope().get(offeringIdentifier).getEnvelope().expandToInclude(observedFeatureEnvelope);
		} else {
			// add new envelope
			SosEnvelope newOfferingEnvelope = new SosEnvelope(observedFeatureEnvelope, observedFeatureEnvelopeSRID);
			getCapabilitiesCache().getKOfferingVEnvelope().put(offeringIdentifier, newOfferingEnvelope);
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
		Map<String, Collection<String>> childFeaturesMapFromCache = getCapabilitiesCache().getChildFeatures();
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
		Map<String, Collection<String>> parentFeaturesMapFromCache = getCapabilitiesCache().getParentFeatures();
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
		// add foi-sensor-relation
		if (getCapabilitiesCache().getProceduresForFeature(observedFeatureIdentifier) == null) {
			List<String> procedures4Feature = Collections.synchronizedList(new ArrayList<String>());
			procedures4Feature.add(procedureIdentifier);
			getCapabilitiesCache().getKFeatureOfInterestVProcedures().put(observedFeatureIdentifier, procedures4Feature);
		} else if (!getCapabilitiesCache().getProceduresForFeature(observedFeatureIdentifier).contains(procedureIdentifier)) {
			getCapabilitiesCache().getKFeatureOfInterestVProcedures().get(observedFeatureIdentifier).add(procedureIdentifier);
		}
	}

	private void addFeatureIdentifierToCache(String observedFeatureIdentifier)
	{
		// add feature
		Collection<String> featureIdentifiers = getCapabilitiesCache().getFeatureOfInterest();
		if (!featureIdentifiers.contains(observedFeatureIdentifier)) {
			featureIdentifiers.add(observedFeatureIdentifier);
		}
	}

	private List<SosSamplingFeature> sosFeaturesToList(SosAbstractFeature sosFeatureOfInterest)
	{
		if (sosFeatureOfInterest instanceof SosFeatureCollection) {
			return getAllFeaturesFrom((SosFeatureCollection) sosFeatureOfInterest);
		} else if (sosFeatureOfInterest instanceof SosSamplingFeature) {
			List<SosSamplingFeature> observedFeatures = new ArrayList<SosSamplingFeature>(1);
			observedFeatures.add((SosSamplingFeature) sosFeatureOfInterest);
			return observedFeatures;
		} else {
			String errorMessage = String.format("Feature Type \"%s\" not supported.", sosFeatureOfInterest != null ? sosFeatureOfInterest.getClass().getName() : sosFeatureOfInterest);
			LOGGER.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
	}

	private Envelope createEnvelopeFrom(List<SosSamplingFeature> observedFeatures)
	{
		Envelope featureEnvelope = new Envelope();
		for (SosSamplingFeature sosSamplingFeature : observedFeatures) {
			featureEnvelope.expandToInclude(sosSamplingFeature.getGeometry().getEnvelopeInternal());
		}
		return featureEnvelope;
	}

	private List<SosSamplingFeature> getAllFeaturesFrom(SosFeatureCollection sosFeatureOfInterest)
	{
		List<SosSamplingFeature> allFeatures = new ArrayList<SosSamplingFeature>(((SosFeatureCollection) sosFeatureOfInterest).getMembers().size());
		for (Entry<String, SosAbstractFeature> entry : ((SosFeatureCollection) sosFeatureOfInterest).getMembers().entrySet()) {
			if (entry.getValue() instanceof SosSamplingFeature) {
				allFeatures.add((SosSamplingFeature) entry.getValue());
			} else if (entry.getValue() instanceof SosFeatureCollection) {
				allFeatures.addAll(getAllFeaturesFrom((SosFeatureCollection) entry.getValue()));
			}
		}
		return allFeatures;
	}

	private void addObservablePropertiesToOfferingRelation(String observablePropertyIdentifier,
			String offeringIdentifier)
	{
		// observableProperties-offering
		if (getCapabilitiesCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier) == null) {
			List<String> offeringsForProperty = Collections.synchronizedList(new ArrayList<String>());
			offeringsForProperty.add(offeringIdentifier);
			getCapabilitiesCache().getKObservablePropertyVOffering().put(observablePropertyIdentifier, offeringsForProperty);
		} else if (!getCapabilitiesCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).contains(offeringIdentifier)) {
			getCapabilitiesCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).add(offeringIdentifier);
		}
	}

	private void addOfferingToProcedureRelation(String offeringIdentifier,
			String procedureIdentifier)
	{
		// offering-procedures
		Collection<String> procedures4Offering = getCapabilitiesCache().getProceduresForOffering(offeringIdentifier);
		if (procedures4Offering == null) {
			procedures4Offering = Collections.synchronizedList(new ArrayList<String>());
			procedures4Offering.add(procedureIdentifier);
			getCapabilitiesCache().getKOfferingVProcedures().put(offeringIdentifier, (List<String>) procedures4Offering);
		} else if (!procedures4Offering.contains(procedureIdentifier)) {
			procedures4Offering.add(procedureIdentifier);
		}
	}

	private void addOfferingToObservationTypeRelation(String offeringIdentifier,
			String observationType)
	{
		// offering-observationTypes
		Map<String, Collection<String>> observationTypes4Offerings = getCapabilitiesCache().getKOfferingVObservationTypes();
		if (observationTypes4Offerings != null) {
			if (observationTypes4Offerings.containsKey(offeringIdentifier) && !observationTypes4Offerings.get(offeringIdentifier).contains(observationType)) {
				observationTypes4Offerings.get(offeringIdentifier).add(observationType);
			} else {
				// add new list
				List<String> observationTypes = Collections.synchronizedList(new ArrayList<String>());
				observationTypes.add(observationType);
				observationTypes4Offerings.put(offeringIdentifier, observationTypes);
			}
		}
	}

	private void updateTemporalBoundingBoxOf(String offeringIdentifier,
			TimePeriod observationEventTime)
	{
		// offering-maxtime
		// check and update if later
		if (getCapabilitiesCache().getKOfferingVMaxTime().containsKey(offeringIdentifier)
				&& getCapabilitiesCache().getKOfferingVMaxTime().get(offeringIdentifier).isBefore(observationEventTime.getEnd())) {
			getCapabilitiesCache().getKOfferingVMaxTime().put(offeringIdentifier, observationEventTime.getEnd());
		} else {
			// add new
			getCapabilitiesCache().getKOfferingVMaxTime().put(offeringIdentifier, observationEventTime.getEnd());
		}
		// offering-mintime
		// check and update if before
		if (getCapabilitiesCache().getKOfferingVMinTime().containsKey(offeringIdentifier)
				&& getCapabilitiesCache().getKOfferingVMinTime().get(offeringIdentifier).isAfter(observationEventTime.getStart())) {
			getCapabilitiesCache().getKOfferingVMinTime().put(offeringIdentifier, observationEventTime.getStart());
		} else {
			// add new
			getCapabilitiesCache().getKOfferingVMinTime().put(offeringIdentifier, observationEventTime.getStart());
		}
	}

	private void updateGlobalEnvelopeUsing(Envelope observedFeatureEnvelope)
	{
		SosEnvelope globalEnvelope = getCapabilitiesCache().getGlobalEnvelope();
		if (!globalEnvelope.isSetEnvelope()) {
			// add new envelope
			SosEnvelope newFeatureEnvelope = new SosEnvelope(observedFeatureEnvelope, getDefaultEPSG());
			getCapabilitiesCache().setGlobalEnvelope(newFeatureEnvelope);
		} else if (!globalEnvelope.getEnvelope().contains(observedFeatureEnvelope)) {
			// extend envelope
			globalEnvelope.getEnvelope().expandToInclude(observedFeatureEnvelope);
		}
	}

	private TimePeriod phenomenonTimeFrom(SosObservation sosObservation)
	{
		ITime phenomenonTime = sosObservation.getPhenomenonTime();
		if (phenomenonTime instanceof TimeInstant) {
			return new TimePeriod(((TimeInstant) phenomenonTime).getValue(), ((TimeInstant) phenomenonTime).getValue());
		} else {
			return (TimePeriod) phenomenonTime;
		}
	}

	private void updateGlobalTemporalBBoxUsingNew(TimePeriod observationEventTime)
	{
		if (getCapabilitiesCache().getMinEventTime() == null || getCapabilitiesCache().getMinEventTime().isAfter(observationEventTime.getStart())) {
			getCapabilitiesCache().setMinEventTime(observationEventTime.getStart());
		}
		if (getCapabilitiesCache().getMaxEventTime() == null || getCapabilitiesCache().getMaxEventTime().isBefore(observationEventTime.getEnd())) {
			getCapabilitiesCache().setMaxEventTime(observationEventTime.getEnd());
		}
	}

	protected int getDefaultEPSG()
	{
		return Configurator.getInstance().getDefaultEPSG();
	}

}
