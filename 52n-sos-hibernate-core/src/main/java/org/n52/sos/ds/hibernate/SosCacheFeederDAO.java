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
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.hibernate.entities.CompositePhenomenon;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.entities.SpatialRefSys;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implementation of the interface ICacheFeederDAO
 * 
 */
public class SosCacheFeederDAO extends AbstractHibernateDao implements ICacheFeederDAO {

	/**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosCacheFeederDAO.class);

	/**
	 * Defines the number of threads available in the thread pool of the cache
	 * update executor service.
	 */
    // TODO read from configuration
	private static final int CACHE_THREAD_COUNT = 5;

    public enum CacheCreationStrategy {
    	SINGLE_THREAD, MULTI_THREAD, COMPLEX_DB_QUERIES
    }

    private final CacheCreationStrategy DEFAULT_STRATEGY = CacheCreationStrategy.MULTI_THREAD;
    
	private CacheCreationStrategy strategy = DEFAULT_STRATEGY;
	
    @Override
    public void updateCache(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
    	if (capabilitiesCache == null)
    	{
    		String errorMsg = "CapabilitiesCache object is null";
    		IllegalArgumentException e = new IllegalArgumentException(errorMsg);
    		
    		LOGGER.debug("Exception thrown:",e);
    		LOGGER.error(errorMsg);
    		
    		throw Util4Exceptions.createNoApplicableCodeException(e,errorMsg);
    	}
        updateCache(capabilitiesCache,null);
    }
    
    protected void updateCache(CapabilitiesCache cache, CacheCreationStrategy strategy) throws OwsExceptionReport {
    	if (strategy != null)
    	{
    		this.strategy = strategy;
    	}
        Session session = null;
        try {
            session = getSession();
            setOfferingValues(cache, session);
            setProcedureValues(cache, session);
            setObservablePropertyValues(cache, session);
            setFeatureOfInterestValues(cache, session);
            setRelatedFeatures(cache, session);
            setCompositePhenomenonValues(cache, session);
            setSridValues(cache, session);
            setObservationTypes(cache, session);
            setFeatureOfInterestTypes(cache, session);
            setObservationIdentifiers(cache, session);
            setResultTemplateValues(cache, session);
			setEventTimeValues(cache, session);
        } catch (HibernateException he) {
            String exceptionText = "Error while initializing CapabilitiesCache!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public void updateAfterSensorInsertion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = getSession();
            setOfferingValues(capabilitiesCache, session);
            setProcedureValues(capabilitiesCache, session);
            setObservablePropertyValues(capabilitiesCache, session);
            setFeatureOfInterestValues(capabilitiesCache, session);
            setRelatedFeatures(capabilitiesCache, session);
            setCompositePhenomenonValues(capabilitiesCache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after sensor insertion!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public void updateAfterSensorDeletion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = getSession();
            setOfferingValues(capabilitiesCache, session);
            setProcedureValues(capabilitiesCache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after sensor deletion!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public void updateAfterObservationInsertion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = getSession();
            setFeatureOfInterestValues(capabilitiesCache, session);
			setOfferingValues(capabilitiesCache, session);
			setEventTimeValues(capabilitiesCache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after observation insertion!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public void updateAfterObservationDeletion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport
    {
        Session session = null;
        try {
            // TODO: check which setter are necessary (see updateAfterObservationInsertion)
            session = getSession();
            setFeatureOfInterestValues(capabilitiesCache, session);
            setOfferingValues(capabilitiesCache, session);
			setEventTimeValues(capabilitiesCache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after observation deletion!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public void updateAfterResultTemplateInsertion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = getSession();
            setOfferingValues(capabilitiesCache, session);
            setProcedureValues(capabilitiesCache, session);
            setObservablePropertyValues(capabilitiesCache, session);
            setFeatureOfInterestValues(capabilitiesCache, session);
            setRelatedFeatures(capabilitiesCache, session);
            setCompositePhenomenonValues(capabilitiesCache, session);
            setResultTemplateValues(capabilitiesCache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after resultTemplate insertion!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
    }

    private void setOfferingValues(CapabilitiesCache cache, Session session) throws OwsExceptionReport {
        List<Offering> hOfferings = HibernateCriteriaQueryUtilities.getOfferingObjects(session);
        
        Map<String, String> 			kOfferingVName 						= new Hashtable<String, String>(hOfferings.size(),1.0f);
        Map<String, Collection<String>> kOfferingVProcedures 				= new Hashtable<String, Collection<String>>(hOfferings.size(),1.0f);
        Map<String, Collection<String>> kOfferingVObservableProperties 		= new Hashtable<String, Collection<String>>(hOfferings.size(),1.0f);
        Map<String, Collection<String>> kOfferingVRelatedFeatures 			= new Hashtable<String, Collection<String>>(hOfferings.size(),1.0f);
        Map<String, Collection<String>> kOfferingVObservationTypes			= new Hashtable<String, Collection<String>>(hOfferings.size(),1.0f);
        Map<String, Collection<String>> allowedkOfferingVObservationTypes 	= new Hashtable<String, Collection<String>>(hOfferings.size(),1.0f);
		Map<String, DateTime> 			kOfferingVMinTime 					= new Hashtable<String, DateTime>(hOfferings.size(),1.0f);
		Map<String, DateTime> 			kOfferingVMaxTime 					= new Hashtable<String, DateTime>(hOfferings.size(),1.0f);
		Map<String, SosEnvelope> 		kOfferingVEnvelope	 				= new Hashtable<String, SosEnvelope>(hOfferings.size(),1.0f);
		Map<String, Collection<String>>	kOfferingVFeaturesOfInterest		= new Hashtable<String, Collection<String>>(hOfferings.size(),1.0f);
		
		// fields required for multithreading
		ExecutorService executor = null;
		CountDownLatch offeringThreadsRunning = null;
		IConnectionProvider connectionProvider = null;
		Vector<OwsExceptionReport> owsReportsThrownByOfferingThreads = null;
		
		if (strategy == CacheCreationStrategy.COMPLEX_DB_QUERIES)
		{
			getTemporalBBoxesOfOfferingsAndSaveInMap(session, kOfferingVMinTime, kOfferingVMaxTime);
		}
		else if (strategy == CacheCreationStrategy.MULTI_THREAD)
		{
			LOGGER.debug("multithreading init");
			executor = Executors.newFixedThreadPool(CACHE_THREAD_COUNT);
			offeringThreadsRunning = new CountDownLatch(hOfferings.size());
			connectionProvider = Configurator.getInstance().getConnectionProvider();
			owsReportsThrownByOfferingThreads = new Vector<OwsExceptionReport>(hOfferings.size());
		}
		for (Offering offering : hOfferings) {
			if (!checkOfferingForDeletedProcedure(offering.getObservationConstellations())) {
				if (strategy == CacheCreationStrategy.MULTI_THREAD)
				{
					// create runnable for offeringId
					Runnable task = new OfferingTask(offeringThreadsRunning,connectionProvider, kOfferingVName,
							kOfferingVProcedures, kOfferingVObservableProperties, kOfferingVRelatedFeatures,
							kOfferingVObservationTypes,	allowedkOfferingVObservationTypes, kOfferingVMinTime,
							kOfferingVMaxTime, kOfferingVEnvelope, kOfferingVFeaturesOfInterest, offering, owsReportsThrownByOfferingThreads);
					// put runnable in executor service
					executor.submit(task);
				} 
				else
				{
				getOfferingInformationFromDbAndAddItToCacheMaps(session,
						kOfferingVName, kOfferingVProcedures, kOfferingVObservableProperties,
						kOfferingVRelatedFeatures, kOfferingVObservationTypes, allowedkOfferingVObservationTypes,
						kOfferingVMinTime, kOfferingVMaxTime, kOfferingVEnvelope, kOfferingVFeaturesOfInterest, offering);
				}
			}
		}
		if (strategy == CacheCreationStrategy.MULTI_THREAD)
		{
			executor.shutdown(); // <-- will finish all submitted tasks
			// wait for all threads to finish
			try 
			{
				LOGGER.debug("Waiting for {} threads to finish",hOfferings.size());
				offeringThreadsRunning.await();
			}
			catch (InterruptedException e) {}
			LOGGER.debug("Finished waiting for other threads");
			if (!owsReportsThrownByOfferingThreads.isEmpty())
			{
				Util4Exceptions.mergeAndThrowExceptions(owsReportsThrownByOfferingThreads);
			}
		}
		// save all information in cache
        cache.setKOfferingVName(kOfferingVName);
        cache.setKOfferingVObservableProperties(kOfferingVObservableProperties);
        cache.setKOfferingVProcedures(kOfferingVProcedures);
        cache.setKOfferingVRelatedFeatures(kOfferingVRelatedFeatures);
        cache.setKOfferingVObservationTypes(kOfferingVObservationTypes);
        cache.setKOfferingVFeatures(kOfferingVFeaturesOfInterest);
        cache.setAllowedKOfferingVObservationType(allowedkOfferingVObservationTypes);
		cache.setKOfferingVEnvelope(kOfferingVEnvelope);
		cache.setKOfferingVMinTime(kOfferingVMinTime);
		cache.setKOfferingVMaxTime(kOfferingVMaxTime);
    }
    
    private void getOfferingInformationFromDbAndAddItToCacheMaps(Session session,
			Map<String, String> kOfferingVName,
			Map<String, Collection<String>> kOfferingVProcedures,
			Map<String, Collection<String>> kOfferingVObservableProperties,
			Map<String, Collection<String>> kOfferingVRelatedFeatures,
			Map<String, Collection<String>> kOfferingVObservationTypes,
			Map<String, Collection<String>> allowedkOfferingVObservationTypes,
			Map<String, DateTime> kOfferingVMinTime,
			Map<String, DateTime> kOfferingVMaxTime,
			Map<String, SosEnvelope> kOfferingVEnvelope,
			Map<String, Collection<String>> kOfferingVFeaturesOfInterest, Offering offering) throws OwsExceptionReport
	{
		String offeringId = offering.getIdentifier();
		kOfferingVName.put(offeringId, offering.getName());
		// Procedures
		kOfferingVProcedures.put(offeringId,
				getProcedureIdentifierFrom(offering.getObservationConstellations()));
		// Observable properties
		kOfferingVObservableProperties.put(offeringId,
				getObservablePropertyIdentifierFrom(offering.getObservationConstellations()));
		// Related features
		kOfferingVRelatedFeatures.put(offeringId,
				getRelatedFeatureIdentifiersFrom(offering));
		// Observation types
		kOfferingVObservationTypes.put(offeringId,
				getObservationTypesFrom(offering.getObservationConstellations()));
		allowedkOfferingVObservationTypes.put(offeringId,
				getObservationTypesFromObservationType(offering.getObservationTypes()));
		// Spatial Envelope
		kOfferingVEnvelope.put(offeringId,
				getEnvelopeForOffering(offeringId, session));
		// Features of Interest
		List<String> featureOfInterestIdentifiers = HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(offering.getName(), session);
        kOfferingVFeaturesOfInterest.put(offering.getName(), featureOfInterestIdentifiers);
		// Temporal Envelope
		if  (!this.strategy.equals(CacheCreationStrategy.COMPLEX_DB_QUERIES))
		{
			kOfferingVMinTime.put(offeringId,
					HibernateCriteriaQueryUtilities.getMinDate4Offering(offeringId, session));
			kOfferingVMaxTime.put(offeringId,
					HibernateCriteriaQueryUtilities.getMaxDate4Offering(offeringId, session));
		}
	}

	private void getTemporalBBoxesOfOfferingsAndSaveInMap(Session session,
			Map<String, DateTime> kOfferingVMinTime,
			Map<String, DateTime> kOfferingVMaxTime)
	{
		Map<String, TimePeriod> temporalBoundingBoxesOfOfferings = HibernateCriteriaQueryUtilities.getTemporalBoundingBoxesForOfferings(session);
		for (String offeringId : temporalBoundingBoxesOfOfferings.keySet()) {
			TimePeriod temporalBBox = temporalBoundingBoxesOfOfferings.get(offeringId);
			if (temporalBBox != null && temporalBBox.getStart() != null && temporalBBox.getEnd() != null)
			{
				kOfferingVMinTime.put(offeringId,temporalBBox.getStart());
				kOfferingVMaxTime.put(offeringId,temporalBBox.getEnd());
			}
		}
	}
	
	private SosEnvelope getEnvelopeForOffering(String offeringID, Session session) throws OwsExceptionReport {
		List<String> featureIDs =
                HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(offeringID, session);
        if (featureIDs != null && !featureIDs.isEmpty()) {
            Envelope envelope = Configurator.getInstance().getFeatureQueryHandler()
						.getEnvelopeForFeatureIDs(featureIDs, session);
            SosEnvelope sosEnvelope = new SosEnvelope(envelope, Configurator.getInstance().getDefaultEPSG());
            return sosEnvelope;
        }
        return null;
	}

    private void setProcedureValues(CapabilitiesCache cache, Session session) {
        List<Procedure> hProcedures = HibernateCriteriaQueryUtilities.getProcedureObjects(session);
        Set<String> procedures = new HashSet<String>(hProcedures.size());
        Map<String, Collection<String>> kProcedureVOffering = new HashMap<String, Collection<String>>(hProcedures.size());
        Map<String, Collection<String>> kProcedureVObservableProperties = new HashMap<String, Collection<String>>(hProcedures.size());
        Map<String, Collection<String>> parentProcs = new HashMap<String, Collection<String>>(hProcedures.size());
        for (Procedure procedure : hProcedures) {
            if (!procedure.isDeleted()) {
                procedures.add(procedure.getIdentifier());
                kProcedureVOffering.put(procedure.getIdentifier(),
                        getAllOfferingIdentifiersFrom(procedure.getObservationConstellations()));
                kProcedureVObservableProperties.put(procedure.getIdentifier(),
                        getObservablePropertyIdentifierFrom(procedure.getObservationConstellations()));
                parentProcs.put(procedure.getIdentifier(),
                        getProcedureIDsFromProcedures(procedure.getProceduresForChildSensorId()));
            }
        }
        cache.setProcedures(procedures);
        cache.setKProcedureVOfferings(kProcedureVOffering);
        cache.setProcPhens(kProcedureVObservableProperties);
        cache.setProcedureHierarchies(parentProcs);
    }

    private void setObservablePropertyValues(CapabilitiesCache cache, Session session) {
        //List<String> observableProperties = new ArrayList<String>();
        List<ObservableProperty> hObservableProperties = HibernateCriteriaQueryUtilities.getObservablePropertyObjects(session);
        Map<String, Collection<String>> kObservablePropertyVOffering = new HashMap<String, Collection<String>>(hObservableProperties.size());
        Map<String, Collection<String>> kObservablePropertyVProcedures = new HashMap<String, Collection<String>>(hObservableProperties.size());
        for (ObservableProperty observableProperty : hObservableProperties) {
			//observableProperties.add(observableProperty.getIdentifier());
            kObservablePropertyVOffering.put(observableProperty.getIdentifier(),
                    getAllOfferingIdentifiersFrom(observableProperty.getObservationConstellations()));
            kObservablePropertyVProcedures.put(observableProperty.getIdentifier(),
                    getProcedureIdentifierFrom(observableProperty.getObservationConstellations()));
        }
        cache.setKObservablePropertyVOfferings(kObservablePropertyVOffering);
        cache.setKObservablePropertyKProcedures(kObservablePropertyVProcedures);
    }

    private void setFeatureOfInterestValues(CapabilitiesCache cache, Session session) throws OwsExceptionReport {
        List<FeatureOfInterest> hFeaturesOfInterest = HibernateCriteriaQueryUtilities.getFeatureOfInterestObjects(session);
        Map<String, Collection<String>> kFeatureOfInterestVProcedure = new HashMap<String, Collection<String>>(hFeaturesOfInterest.size());
        Map<String, Collection<String>> parentFeatures = new HashMap<String, Collection<String>>(hFeaturesOfInterest.size());
        for (FeatureOfInterest featureOfInterest : hFeaturesOfInterest) {
            kFeatureOfInterestVProcedure.put(featureOfInterest.getIdentifier(),
                    HibernateCriteriaQueryUtilities.getProceduresForFeatureOfInterest(session, featureOfInterest));
            parentFeatures.put(featureOfInterest.getIdentifier(),
                    getFeatureIDsFromFeatures(featureOfInterest.getFeatureOfInterestsForChildFeatureId()));
        }
		List<String> ids = getFeatureIdentifier(hFeaturesOfInterest);
        cache.setFeatureOfInterest(ids);
        cache.setKFeatureOfInterestVProcedures(kFeatureOfInterestVProcedure);
        cache.setFeatureHierarchies(parentFeatures);
		cache.setEnvelopeForFeatureOfInterest(Configurator.getInstance().getFeatureQueryHandler()
			.getEnvelopeForFeatureIDs(ids, session));
    }
	
	private void setEventTimeValues(CapabilitiesCache cache, Session session) {
		switch (this.strategy) {
		case COMPLEX_DB_QUERIES:
			TimePeriod globalTemporalBBox = HibernateCriteriaQueryUtilities.getGlobalTemporalBoundingBox(session);
			cache.setMinEventTime(globalTemporalBBox.getStart());
			cache.setMaxEventTime(globalTemporalBBox.getEnd());
			break;
		case SINGLE_THREAD:
		default:
			setEventTimesSingleThread(cache, session);
		}
	}

	private void setEventTimesSingleThread(CapabilitiesCache cache,
			Session session)
	{
		cache.setMinEventTime(HibernateCriteriaQueryUtilities.getMinObservationTime(session));
		cache.setMaxEventTime(HibernateCriteriaQueryUtilities.getMaxObservationTime(session));
	}

    private void setRelatedFeatures(CapabilitiesCache cache, Session session) {
        // TODO Carsten use RelatedFeatures and query...
        List<RelatedFeature> relatedFeatures = HibernateCriteriaQueryUtilities.getRelatedFeatureObjects(session);
        Map<String, Collection<String>> relatedFeatureList = new HashMap<String, Collection<String>>(relatedFeatures.size());
        for (RelatedFeature relatedFeature : relatedFeatures) {
            Set<String> roles = new HashSet<String>(relatedFeature.getRelatedFeatureRoles().size());
            for (RelatedFeatureRole relatedFeatureRole : relatedFeature.getRelatedFeatureRoles()) {
                roles.add(relatedFeatureRole.getRelatedFeatureRole());
            }
            relatedFeatureList.put(relatedFeature.getFeatureOfInterest().getIdentifier(), roles);
        }
        cache.setKRelatedFeaturesVRole(relatedFeatureList);
    }

    private void setCompositePhenomenonValues(CapabilitiesCache cache, Session session) {
        List<CompositePhenomenon> compositePhenomenons = HibernateCriteriaQueryUtilities.getCompositePhenomenonObjects(session);
        Map<String, Collection<String>> kCompositePhenomenonVObservableProperty =
                new HashMap<String, Collection<String>>(compositePhenomenons.size());
        
        for (CompositePhenomenon compositePhenomenon : compositePhenomenons) {
            kCompositePhenomenonVObservableProperty.put(compositePhenomenon.getIdentifier(),
                    getObservablePropertyIdentifierFromObservableProperties(compositePhenomenon
                            .getObservableProperties()));
        }
        cache.setKOfferingVCompositePhenomenon(null);
        cache.setKCompositePhenomenonVObservableProperties(kCompositePhenomenonVObservableProperty);
    }

    private void setSridValues(CapabilitiesCache cache, Session session) {
        List<SpatialRefSys> spatialRefSyss = HibernateCriteriaQueryUtilities.getSpatialReySysObjects(session);
        List<Integer> srids = new ArrayList<Integer>(spatialRefSyss.size());
        for (SpatialRefSys spatialRefSys : spatialRefSyss) {
            srids.add(spatialRefSys.getSrid());
        }
        cache.setSrids(srids);
    }

    private void setObservationTypes(CapabilitiesCache cache, Session session) {
        cache.setObservationTypes(HibernateCriteriaQueryUtilities.getObservationTypes(session));
    }
    
    private void setFeatureOfInterestTypes(CapabilitiesCache cache, Session session) {
        cache.setFeatureOfInterestTypes(HibernateCriteriaQueryUtilities.getFeatureOfInterestTypes(session));
    }
    
    private void setObservationIdentifiers(CapabilitiesCache cache, Session session) {
        cache.setObservationIdentifiers(HibernateCriteriaQueryUtilities.getObservationIdentifiers(session));
    }

    private boolean checkOfferingForDeletedProcedure(Set<ObservationConstellation> observationConstellations) {
        for (ObservationConstellation observationConstellation : observationConstellations) {
            return observationConstellation.getProcedure().isDeleted();
        }
        return true;
    }

    private List<String> getAllOfferingIdentifiersFrom(Set<ObservationConstellation> observationConstellations) {
        Set<String> offerings = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation oc : observationConstellations) {
            offerings.add(oc.getOffering().getIdentifier());
        }
        return new ArrayList<String>(offerings);
    }

    private List<String> getProcedureIdentifierFrom(Set<ObservationConstellation> observationConstellations) {
        Set<String> procedures = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation observationConstellation : observationConstellations) {
            procedures.add(observationConstellation.getProcedure().getIdentifier());
        }
        return new ArrayList<String>(procedures);
    }

    private List<String> getObservablePropertyIdentifierFrom(Set<ObservationConstellation> observationConstellations) {
        Set<String> observableProperties = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation observationConstellation : observationConstellations) {
            if (observationConstellation.getObservableProperty() != null) {
                observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
            }
        }
        return new ArrayList<String>(observableProperties);
    }

    private List<String> getObservablePropertyIdentifierFromObservableProperties(
            Set<ObservableProperty> observableProperties) {
        List<String> observablePropertyIdentifiers = new ArrayList<String>(observableProperties.size());
        for (ObservableProperty observableProperty : observableProperties) {
            observablePropertyIdentifiers.add(observableProperty.getIdentifier());
        }
        return observablePropertyIdentifiers;
    }

    private List<String> getObservationTypesFrom(
            Set<ObservationConstellation> observationConstellations) {
        Set<String> observationTypes = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation observationConstellation : observationConstellations) {
            if (observationConstellation.getObservationType() != null) {
                observationTypes.add(observationConstellation.getObservationType().getObservationType());
            }
        }
        return new ArrayList<String>(observationTypes);
    }

    private Collection<String> getObservationTypesFromObservationType(Set<ObservationType> observationTypes) {
        Set<String> obsTypes = new HashSet<String>(observationTypes.size());
        for (ObservationType obsType : observationTypes) {
            obsTypes.add(obsType.getObservationType());
        }
        return new ArrayList<String>(obsTypes);
    }

    private Collection<String> getRelatedFeatureIdentifiersFrom(Offering offering) {
        List<String> relatedFeatureList = new ArrayList<String>(offering.getRelatedFeatures().size());
        for (RelatedFeature relatedFeature : offering.getRelatedFeatures()) {
            relatedFeatureList.add(relatedFeature.getFeatureOfInterest().getIdentifier());
        }
        return relatedFeatureList;
    }

    private List<String> getFeatureIdentifier(List<FeatureOfInterest> featuresOfInterest) {
        List<String> featureList = new ArrayList<String>(featuresOfInterest.size());
        for (FeatureOfInterest featureOfInterest : featuresOfInterest) {
            featureList.add(featureOfInterest.getIdentifier());
        }
        return featureList;
    }

    private Collection<String> getProcedureIDsFromProcedures(Set<Procedure> proceduresForChildSensorId) {
        List<String> procedureIDs = new ArrayList<String>(proceduresForChildSensorId.size());
        for (Procedure procedure : proceduresForChildSensorId) {
            procedureIDs.add(procedure.getIdentifier());
        }
        return procedureIDs;
    }

    private Collection<String> getFeatureIDsFromFeatures(Set<FeatureOfInterest> featureOfInterestsForChildFeatureId) {
        List<String> featureIDs = new ArrayList<String>(featureOfInterestsForChildFeatureId.size());
        for (FeatureOfInterest feature : featureOfInterestsForChildFeatureId) {
            featureIDs.add(feature.getIdentifier());
        }
        return featureIDs;
    }

    private void setResultTemplateValues(CapabilitiesCache cache, Session session) {
        List<ResultTemplate> resultTemplateObjects = HibernateCriteriaQueryUtilities.getResultTemplateObjects(session);
        List<String> resultTemplates = new ArrayList<String>(resultTemplateObjects.size());
        for (ResultTemplate resultTemplateObject : resultTemplateObjects) {
            resultTemplates.add(resultTemplateObject.getIdentifier());
        }
        cache.setResultTemplates(resultTemplates);
    }

	private class OfferingTask implements Runnable
	{
		private Map<String, String> kOfferingVName;
		private Map<String, Collection<String>> kOfferingVObservableProperties;
		private Map<String, Collection<String>> kOfferingVObservationTypes;
		private Map<String, Collection<String>> kOfferingVProcedures;
		private Map<String, Collection<String>> kOfferingVRelatedFeatures;
		private CountDownLatch countDownLatch;
		private IConnectionProvider connectionProvider;
		private Map<String, Collection<String>> allowedkOfferingVObservationTypes;
		private Offering offering;
		private Map<String, SosEnvelope> kOfferingVEnvelope;
		private Map<String, DateTime> kOfferingVMaxTime;
		private Map<String, DateTime> kOfferingVMinTime;
		private Vector<OwsExceptionReport> owsReports;
		private Map<String, Collection<String>> kOfferingVFeaturesOfInterest;
	
		public OfferingTask(CountDownLatch countDownLatch, IConnectionProvider connectionProvider, Map<String, String> kOfferingVName, Map<String, Collection<String>> kOfferingVProcedures,
				Map<String, Collection<String>> kOfferingVObservableProperties, Map<String, Collection<String>> kOfferingVRelatedFeatures,
				Map<String, Collection<String>> kOfferingVObservationTypes, Map<String, Collection<String>> allowedkOfferingVObservationTypes, Map<String, DateTime> kOfferingVMinTime,
				Map<String, DateTime> kOfferingVMaxTime, Map<String, SosEnvelope> kOfferingVEnvelope, Map<String, Collection<String>> kOfferingVFeaturesOfInterest, Offering offering, Vector<OwsExceptionReport> owsReports) {
			this.countDownLatch = countDownLatch;
			this.connectionProvider = connectionProvider;
			this.kOfferingVName = kOfferingVName;
			this.kOfferingVObservableProperties = kOfferingVObservableProperties;
			this.kOfferingVObservationTypes = kOfferingVObservationTypes;
			this.kOfferingVProcedures = kOfferingVProcedures;
			this.kOfferingVRelatedFeatures = kOfferingVRelatedFeatures;
			this.allowedkOfferingVObservationTypes = allowedkOfferingVObservationTypes;
			this.kOfferingVMinTime = kOfferingVMinTime;
			this.kOfferingVMaxTime = kOfferingVMaxTime;
			this.kOfferingVEnvelope = kOfferingVEnvelope;
			this.offering = offering;
			this.owsReports = owsReports;
			this.kOfferingVFeaturesOfInterest = kOfferingVFeaturesOfInterest;
		}
	
		@Override
		public void run()
		{
			Session session = null;
			try
			{
				session = (Session) connectionProvider.getConnection();
				getOfferingInformationFromDbAndAddItToCacheMaps(session, kOfferingVName, kOfferingVProcedures,
						kOfferingVObservableProperties, kOfferingVRelatedFeatures, kOfferingVObservationTypes,
						allowedkOfferingVObservationTypes, kOfferingVMinTime, kOfferingVMaxTime, kOfferingVEnvelope,
						kOfferingVFeaturesOfInterest, offering);
			} catch (OwsExceptionReport e) {
				LOGGER.error(String.format("Exception thrown: %s",
						e.getMessage()),
						e);
				owsReports.add(e);
			}
			finally
			{
				if (session != null)
				{
					connectionProvider.returnConnection(session);
				}
				LOGGER.debug("OfferingTask finished, latch.countDown().");
				countDownLatch.countDown();
			}
		}
		
	}

	
}
