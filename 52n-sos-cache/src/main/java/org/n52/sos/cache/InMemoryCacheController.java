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
import org.n52.sos.cache.action.ObservationInsertionInMemoryCacheUpdate;
import org.n52.sos.cache.action.ResultTemplateInsertionInMemoryCacheUpdate;
import org.n52.sos.cache.action.SensorInsertionInMemoryCacheUpdate;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
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
		update(new SensorInsertionInMemoryCacheUpdate(sosRequest, sosResponse));
	}

	/**
	 * Updates the underlying capabilities cache without accessing the data
	 * store
	 * @see ObservationInsertionInMemoryCacheUpdate
	 */
	public void updateAfterObservationInsertion(InsertObservationRequest sosRequest) throws OwsExceptionReport
	{
		update(new ObservationInsertionInMemoryCacheUpdate(sosRequest));
	}

	/*
	 * Updates the underlying capabilities cache without accessing the data
	 * store
	 */
	public void updateAfterSensorDeletion(DeleteSensorRequest sosRequest) throws OwsExceptionReport
	{
		update(Case.SENSOR_DELETION, sosRequest);
	}

	public void updateAfterResultTemplateInsertion(InsertResultTemplateRequest sosRequest, InsertResultTemplateResponse sosResponse) throws OwsExceptionReport
	{
		update(new ResultTemplateInsertionInMemoryCacheUpdate(sosRequest,sosResponse));
	}

	public void updateAfterResultInsertion(SosObservation observation)
	{
		doUpdateAfterResultInsertion(observation);
	}

	private void update(Case c,
			AbstractServiceRequest sosRequest) throws OwsExceptionReport
	{
		update(c, sosRequest, null);
	}

	/**
	 * TODO Eike: continue implementation here: Call this method from public methods and create the required actions 
	 * TODO Eike: test removal of locking mechanisms
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
			cacheUpdate.setCache(getCache());
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
	
	private void doUpdateAfterSensorDeletion(DeleteSensorRequest sosRequest)
		{
			removeProcedureFromCache(sosRequest.getProcedureIdentifier());
			
			removeFeatureToProcedureRelationsFromCache(sosRequest.getProcedureIdentifier());
			
			removeOfferingsToProcedureRelation(sosRequest.getProcedureIdentifier());
			
			removeProcedureToObservationIdentifierRelations(sosRequest.getProcedureIdentifier());
	
			for (String offeringId : getCache().getOfferings4Procedure(sosRequest.getProcedureIdentifier()))
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
			
			if (getCache().getKOfferingVEnvelope().isEmpty())
			{
				removeGlobalEnvelope();
			}
			
			// observable property relations
			removeObservablePropertyRelations(sosRequest.getProcedureIdentifier());
			
			// At the latest
			removeProcedureToOfferingsRelation(sosRequest.getProcedureIdentifier());
			
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
	
	private void removeOfferintgToResultTemplatesRelation(String offeringId)
	{
		Collection<String> resultTemplateIdentifiersToRemove = getCache().getKOfferingVResultTemplates().remove(offeringId);
		LOGGER.debug("offering '{}' to result templates removed from map? {}",
				offeringId,
				getCache().getKOfferingVResultTemplates().containsKey(offeringId));
		if (resultTemplateIdentifiersToRemove != null)
		{
			for (String resultTemplateIdentiferToRemove : resultTemplateIdentifiersToRemove)
			{
				getCache().getResultTemplates().remove(resultTemplateIdentiferToRemove);
				LOGGER.debug("Removed result template identifier '{}' from cache? {}",
						resultTemplateIdentiferToRemove,
						getCache().getResultTemplates().contains(resultTemplateIdentiferToRemove));
			}
		}
	}

	private void addProcedureToObservationIdRelationToCache(String procedureIdentifier, String observationIdentifier)
	{
		if (!getCache().getKProcedureVObservationIdentifiers().containsKey(procedureIdentifier))
		{
			Collection<String> value = Collections.synchronizedList(new ArrayList<String>());
			getCache().getKProcedureVObservationIdentifiers().put(procedureIdentifier, value);
		}
		getCache().getKProcedureVObservationIdentifiers().get(procedureIdentifier).add(observationIdentifier);
		LOGGER.debug("procedure \"{}\" to observation id \"{}\" relation added to cache? {}",
				procedureIdentifier,
				observationIdentifier,
				getCache().getKProcedureVObservationIdentifiers().get(procedureIdentifier).contains(observationIdentifier));
	}

	
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
		List<String> allowedObservationIdentifiers = getAllowedEntries(getCache().getKProcedureVObservationIdentifiers().values());
		List<String> featuresToRemove = getEntriesToRemove(allowedObservationIdentifiers,getCache().getObservationIdentifiers());
		removeEntries(featuresToRemove,getCache().getObservationIdentifiers());
	}
	
	private void removeOfferingToObservablePropertyRelation(String offeringId)
	{
		getCache().getKOfferingVObservableProperties().remove(offeringId);
		LOGGER.debug("offering \"{}\" to observable properties relations removed? {}",
				offeringId,
				getCache().getKOfferingVObservableProperties().containsKey(offeringId));
	}

	private void removeProcedureToObservationIdentifierRelations(String procedureIdentifier)
	{
		getCache().getKProcedureVObservationIdentifiers().remove(procedureIdentifier);
		LOGGER.debug("observation identifiers removed for procedure \"{}\"? {}",
				procedureIdentifier,
				getCache().getKProcedureVObservationIdentifiers().containsKey(procedureIdentifier));
	}

	private void removeFeatureToProcedureRelationsFromCache(String procedureIdentifier)
	{
		List<String> featuresToRemove = new ArrayList<String>();
		for (String feature : getCache().getKFeatureOfInterestVProcedures().keySet())
		{
			getCache().getKFeatureOfInterestVProcedures().get(feature).remove(procedureIdentifier);
			LOGGER.debug("removed feature \"{}\" -> procedure \"{}\" relation from cache? {}",
					feature,
					procedureIdentifier,
					getCache().getKFeatureOfInterestVProcedures().get(feature).contains(procedureIdentifier));
			if (getCache().getKFeatureOfInterestVProcedures().get(feature) == null ||
					getCache().getKFeatureOfInterestVProcedures().get(feature).isEmpty())
			{
				featuresToRemove.add(feature);
			}
		}
		for (String featureToRemove : featuresToRemove) {
			getCache().getKFeatureOfInterestVProcedures().remove(featureToRemove);
			LOGGER.debug("removed feature \"{}\" from featur -> procedure map? {}",
					featureToRemove,
					procedureIdentifier,
					getCache().getKFeatureOfInterestVProcedures().containsKey(featureToRemove));
		}
	}

	private void removeOfferingToObservationTypesRelations(String offeringId)
	{
		getCache().getKOfferingVObservationTypes().remove(offeringId);
		LOGGER.debug("observation types removed for offering \"{}\"? {}",
				offeringId,
				getCache().getKOfferingVObservationTypes().containsKey(offeringId));
	}

	private void removeOfferingToFeaturesRelations(String offeringId)
	{
		getCache().getKOfferingVFeaturesOfInterest().remove(offeringId);
		LOGGER.debug("features removed for offering \"{}\"? {}",
				offeringId,
				getCache().getKOfferingVFeaturesOfInterest().containsKey(offeringId));
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
		getCache().getKOfferingVName().remove(offeringId);
		LOGGER.debug("offering name removed for offering \"{}\"? {}",
				offeringId,
				getCache().getKOfferingVName().containsKey(offeringId));
	}

	private void removeRemovedRelatedFeaturesFromRoleMap()
	{
		List<String> allowedRelatedFeatures = getAllowedRelatedFeatures();
		List<String> featuresToRemove = getEntriesToRemove(allowedRelatedFeatures, 
				getCache().getKRelatedFeatureVRole().keySet());
		removeEntries(featuresToRemove, getCache().getKRelatedFeatureVRole().keySet());
	}

	protected List<String> getAllowedRelatedFeatures()
	{
		return getAllowedEntries(getCache().getKOfferingVRelatedFeatures().values());
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
		LOGGER.debug("Related features removed for offering \"{}\"? {}",
				offeringId,
				getCache().getKOfferingVRelatedFeatures().containsKey(offeringId));
	}

	private void removeGlobalEnvelope()
	{
		getCache().setGlobalEnvelope(new SosEnvelope(null, getSrid()));
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
		LOGGER.debug("Temporal boundingbox removed for offering \"{}\"? max time: {}; min time: {}",
				offeringId,
				getCache().getKOfferingVMaxTime().containsKey(offeringId),
				getCache().getKOfferingVMinTime().containsKey(offeringId));
	}

	private void removeOfferingEnvelope(String offeringId)
	{
		getCache().getKOfferingVEnvelope().remove(offeringId);
		LOGGER.debug("Envelope removed for offering \"{}\"? {}",
				offeringId,
				getCache().getKOfferingVEnvelope().containsKey(offeringId));
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
		LOGGER.debug("Removed procedure to observable properties relations from cache for procedure \"{}\"? {}",
				procedureIdentifier,
				getCache().getKProcedureVObservableProperties().containsKey(procedureIdentifier));
	}

	private void removeObservablePropertyToProcedureRelation(String observableProperty,
			String procedureIdentifier)
	{
		if (getCache().getKObservablePropertyVProcedures().get(observableProperty).remove(procedureIdentifier))
		{
			LOGGER.debug("Removed observable property \"{}\" -> procedure \"{}\" relation from cache",
					observableProperty,procedureIdentifier);
			if (getCache().getKObservablePropertyVProcedures().get(observableProperty) != null &&
					getCache().getKObservablePropertyVProcedures().get(observableProperty).isEmpty() &&
					getCache().getKObservablePropertyVProcedures().remove(observableProperty) == null)
			{
				LOGGER.debug("Removed entry for observable property \"{}\" from cache map",observableProperty);
			}
		}
	}

	private void removeOfferingsToProcedureRelation(String procedureIdentifier)
	{
		List<String> offeringsToRemove = new ArrayList<String>();
		for (Entry<String, List<String>> offeringToProcedureRelation : getCache().getKOfferingVProcedures().entrySet()) {
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
			getCache().getKOfferingVProcedures().remove(offeringToRemove);
			LOGGER.debug("offering \"{}\" removed from offering->procedure map ? {}",
					offeringsToRemove,
					getCache().getKOfferingVProcedures().containsKey(offeringToRemove));
		}
	}

	private void removeProcedureToOfferingsRelation(String procedureIdentifer)
	{
		getCache().getKProcedureVOffering().remove(procedureIdentifer);
		LOGGER.debug("procedure to offerings relation removed from cache for procedure \"{}\"? {}",
					procedureIdentifer,
					getCache().getKProcedureVOffering().containsKey(procedureIdentifer));
	}

	private void removeProcedureFromCache(String procedureIdentifier)
	{
		getCache().getProcedures().remove(procedureIdentifier);
		LOGGER.debug("Procedure \"{}\" removed from list of procedures? {}",
				procedureIdentifier,
				getCache().getProcedures().contains(procedureIdentifier));
	}

	private void addObservationIdToCache(SosObservation sosObservation)
	{
		getCache().getObservationIdentifiers().add(sosObservation.getIdentifier().getValue());
		LOGGER.debug("Added observation id '{}' to cache? {}",
				sosObservation.getIdentifier().getValue(),
				getCache().getObservationIdentifiers().contains(sosObservation.getIdentifier().getValue()));
	}

	private void addObservationTypeToCache(SosObservation sosObservation)
	{
		if (!getObservationTypes().contains(sosObservation.getObservationConstellation().getObservationType())) {
			getCache().getObservationTypes().add(sosObservation.getObservationConstellation().getObservationType());
		}
	}

	private void addOfferingToObservablePropertyRelation(String offeringIdentifier,
			String observablePropertyIdentifier)
	{
		// offering -> observableProperties
		if (getCache().getKOfferingVObservableProperties().get(offeringIdentifier) == null) {
			List<String> propertiesForOffering = Collections.synchronizedList(new ArrayList<String>());
			propertiesForOffering.add(observablePropertyIdentifier);
			getCache().getKOfferingVObservableProperties().put(offeringIdentifier, propertiesForOffering);
		} else if (!getCache().getKOfferingVObservableProperties().get(offeringIdentifier).contains(observablePropertyIdentifier)) {
			getCache().getKOfferingVObservableProperties().get(offeringIdentifier).add(observablePropertyIdentifier);
		}
	}

	private void addFeatureToProcedureRelationToCache(String observedFeatureIdentifier,
			String procedureIdentifier)
	{
		if (getCache().getProceduresForFeature(observedFeatureIdentifier) == null) {
			List<String> procedures4Feature = Collections.synchronizedList(new ArrayList<String>());
			procedures4Feature.add(procedureIdentifier);
			getCache().getKFeatureOfInterestVProcedures().put(observedFeatureIdentifier, procedures4Feature);
		} else if (!getCache().getProceduresForFeature(observedFeatureIdentifier).contains(procedureIdentifier)) {
			getCache().getProceduresForFeature(observedFeatureIdentifier).add(procedureIdentifier);
		}
	}

	private void addProcedureToOfferingRelation(String procedureIdentifier,
			String offeringIdentifier)
	{
		// offering-procedures
		if (getCache().getKProcedureVOffering().get(procedureIdentifier) == null) {
			List<String> offerings4Procedure = Collections.synchronizedList(new ArrayList<String>());
			offerings4Procedure.add(offeringIdentifier);
			getCache().getKProcedureVOffering().put(procedureIdentifier, offerings4Procedure);
		} else if (!getCache().getKProcedureVOffering().get(procedureIdentifier).contains(offeringIdentifier)) {
			getCache().getKProcedureVOffering().get(procedureIdentifier).add(offeringIdentifier);
		}
	}

	private void addProcedureToObservablePropertyRelation(String procedureIdentifier,
			String observablePropertyIdentifier)
	{
		if (getCache().getKProcedureVObservableProperties().get(procedureIdentifier) == null) {
			List<String> relatedProperties = Collections.synchronizedList(new ArrayList<String>());
			relatedProperties.add(observablePropertyIdentifier);
			getCache().getKProcedureVObservableProperties().put(procedureIdentifier, relatedProperties);
		} else if (!getCache().getKProcedureVObservableProperties().get(procedureIdentifier).contains(observablePropertyIdentifier)) {
			getCache().getKProcedureVObservableProperties().get(procedureIdentifier).add(observablePropertyIdentifier);
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
		if (getCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier) == null)
		{
			List<String> relatedProcedures = Collections.synchronizedList(new ArrayList<String>());
			relatedProcedures.add(procedureIdentifier);
			getCache().getKObservablePropertyVProcedures().put(observablePropertyIdentifier, relatedProcedures);
		}
		else if (!getCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier).contains(procedureIdentifier))
		{
			getCache().getKObservablePropertyVProcedures().get(observablePropertyIdentifier).add(procedureIdentifier);
		}
	}

	private void addFeatureTypeToCache(String featureType)
	{
		if (!getCache().getFeatureOfInterestTypes().contains(featureType)) {
			getCache().getFeatureOfInterestTypes().add(featureType);
			LOGGER.debug("feature type '{}' added to cache? {}",
					featureType,
					getCache().getFeatureOfInterestTypes().contains(featureType));
		}
	}

	private void addProcedureToCache(String procedureIdentifier)
	{
		if (!getCache().getProcedures().contains(procedureIdentifier)) {
			getCache().getProcedures().add(procedureIdentifier);
		}
	}

	private void addOfferingFeatureRelationToCache(String observedFeatureIdentifier,
			String offeringIdentifier)
	{
		// offering-feature
		if (!getCache().getKOfferingVFeaturesOfInterest().containsKey(offeringIdentifier)) {
			List<String> relatedFeatures = Collections.synchronizedList(new ArrayList<String>());
			relatedFeatures.add(observedFeatureIdentifier);
			getCache().getKOfferingVFeaturesOfInterest().put(offeringIdentifier, relatedFeatures);
		} else if (!getCache().getKOfferingVFeaturesOfInterest().get(offeringIdentifier).contains(observedFeatureIdentifier)) {
			getCache().getKOfferingVFeaturesOfInterest().get(offeringIdentifier).add(observedFeatureIdentifier);
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
		if (getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier) == null) {
			List<String> offeringsForProperty = Collections.synchronizedList(new ArrayList<String>());
			offeringsForProperty.add(offeringIdentifier);
			getCache().getKObservablePropertyVOffering().put(observablePropertyIdentifier, offeringsForProperty);
		} else if (!getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).contains(offeringIdentifier)) {
			getCache().getKObservablePropertyVOffering().get(observablePropertyIdentifier).add(offeringIdentifier);
		}
	}

	private void addOfferingToProcedureRelation(String offeringIdentifier,
			String procedureIdentifier)
	{
		Collection<String> procedures4Offering = getCache().getProceduresForOffering(offeringIdentifier);
		if (procedures4Offering == null) {
			procedures4Offering = Collections.synchronizedList(new ArrayList<String>());
			procedures4Offering.add(procedureIdentifier);
			getCache().getKOfferingVProcedures().put(offeringIdentifier, (List<String>) procedures4Offering);
		} else if (!procedures4Offering.contains(procedureIdentifier)) {
			procedures4Offering.add(procedureIdentifier);
		}
	}

	private void addOfferingToObservationTypeRelation(String offeringIdentifier,
			String observationType)
	{
		Map<String, Collection<String>> observationTypes4Offerings = getCache().getKOfferingVObservationTypes();
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
		if (getCache().getKOfferingVMaxTime().containsKey(offeringIdentifier)
				&& getCache().getKOfferingVMaxTime().get(offeringIdentifier).isBefore(observationEventTime.getEnd())) {
			getCache().getKOfferingVMaxTime().put(offeringIdentifier, observationEventTime.getEnd());
		} else {
			// add new
			getCache().getKOfferingVMaxTime().put(offeringIdentifier, observationEventTime.getEnd());
		}
		// offering-mintime
		// check and update if before
		if (getCache().getKOfferingVMinTime().containsKey(offeringIdentifier)
				&& getCache().getKOfferingVMinTime().get(offeringIdentifier).isAfter(observationEventTime.getStart())) {
			getCache().getKOfferingVMinTime().put(offeringIdentifier, observationEventTime.getStart());
		} else {
			// add new
			getCache().getKOfferingVMinTime().put(offeringIdentifier, observationEventTime.getStart());
		}
	}

	private void updateGlobalEnvelopeUsing(Envelope observedFeatureEnvelope)
	{
		SosEnvelope globalEnvelope = getCache().getGlobalEnvelope();
		if (!globalEnvelope.isSetEnvelope()) {
			// add new envelope
			SosEnvelope newFeatureEnvelope = new SosEnvelope(observedFeatureEnvelope, getDefaultEPSG());
			getCache().setGlobalEnvelope(newFeatureEnvelope);
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
		if (getCache().getMinEventTime() == null || getCache().getMinEventTime().isAfter(observationEventTime.getStart())) {
			getCache().setMinEventTime(observationEventTime.getStart());
		}
		if (getCache().getMaxEventTime() == null || getCache().getMaxEventTime().isBefore(observationEventTime.getEnd())) {
			getCache().setMaxEventTime(observationEventTime.getEnd());
		}
	}

	protected int getDefaultEPSG()
	{
		return getCache().getDatabaseEPSGCode();
	}
	
	private void addFeatureIdentifierToCache(String observedFeatureIdentifier)
	{
		if (!getCache().getFeatureOfInterest().contains(observedFeatureIdentifier)) {
			getCache().getFeatureOfInterest().add(observedFeatureIdentifier);
			LOGGER.debug("feature identifier '{}' added to cache? {}",
					observedFeatureIdentifier,
					getCache().getFeatureOfInterest().contains(observedFeatureIdentifier));
		}
	}
	
	private void updateOfferingEnvelope(Envelope observedFeatureEnvelope,
			int observedFeatureEnvelopeSRID,
			String offeringIdentifier)
	{
		// offering-envelope
		if (getCache().getKOfferingVEnvelope().containsKey(offeringIdentifier) && getCache().getKOfferingVEnvelope().get(offeringIdentifier).isSetEnvelope()
				&& !getCache().getKOfferingVEnvelope().get(offeringIdentifier).getEnvelope().contains(observedFeatureEnvelope)) {
			// update envelope
			getCache().getKOfferingVEnvelope().get(offeringIdentifier).getEnvelope().expandToInclude(observedFeatureEnvelope);
		} else {
			// add new envelope
			SosEnvelope newOfferingEnvelope = new SosEnvelope(observedFeatureEnvelope, observedFeatureEnvelopeSRID);
			getCache().getKOfferingVEnvelope().put(offeringIdentifier, newOfferingEnvelope);
		}
	}

}
