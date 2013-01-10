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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.cache.ACapabilitiesCache;
import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ds.ICacheFeederDAO;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.ICacheFeederDAO#initalizeCache(org.n52.sos.cache.
     * ACapabilitiesCache)
     */
    @Override
    public void initalizeCache(ACapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
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

    /**
     * Set cache values related to offering
     * 
     * @param cache
     *            CapabilitiesCache instance CapabilitiesCache instance
     * @param session
     *            Hibernate session
     */
    private void setOfferingValues(CapabilitiesCache cache, Session session) throws OwsExceptionReport {
        List<Offering> hOfferings = HibernateCriteriaQueryUtilities.getOfferingObjects(session);
        Map<String, String> kOfferingVName = new HashMap<String, String>(hOfferings.size());
        Map<String, Collection<String>> kOfferingVProcedures = new HashMap<String, Collection<String>>(hOfferings.size());
        Map<String, Collection<String>> kOfferingVObservableProperties = new HashMap<String, Collection<String>>(hOfferings.size());
        Map<String, Collection<String>> kOfferingVRelatedFeatures = new HashMap<String, Collection<String>>(hOfferings.size());
        Map<String, Collection<String>> kOfferingVObservationTypes = new HashMap<String, Collection<String>>(hOfferings.size());
        Map<String, Collection<String>> allowedkOfferingVObservationTypes = new HashMap<String, Collection<String>>(hOfferings.size());
		Map<String, DateTime> kOfferingVMinTime = new HashMap<String, DateTime>(hOfferings.size());
		Map<String, DateTime> kOfferingVMaxTime = new HashMap<String, DateTime>(hOfferings.size());
		Map<String, SosEnvelope> kOfferingVEnvelope = new HashMap<String, SosEnvelope>(hOfferings.size());
		// TODO Eike: use new one-time request
//		Map<String, TimePeriod> temporalBoundingBoxesOfOfferings = HibernateCriteriaQueryUtilities.getTemporalBoundingBoxesForOfferings(session);
//		TimePeriod temporalBoundingBox = HibernateCriteriaQueryUtilities.getGlobalTemporalBoundingBox(session);
		// TODO Eike: add multi threading here
		// create new executor service with fixed thread pool size (size from configuration) this size limits the number of connections to;
		// constructor should support parameter 'buffer' which limits the number of connections taken; 'buffer' defines the left over connections
        for (Offering offering : hOfferings) {
            if (!checkOfferingForDeletedProcedure(offering.getObservationConstellations())) {
            	// TODO add new runnable for this offering
            	String offeringId = offering.getIdentifier();
            	kOfferingVName.put(offeringId, offering.getName());
            	// Procedures
            	kOfferingVProcedures.put(offeringId,
            			getProceduresFromObservationConstellation(offering.getObservationConstellations()));
            	// Observable properties
            	kOfferingVObservableProperties.put(offeringId,
            			getObservablePropertiesFromObservationConstellation(offering.getObservationConstellations()));
            	// Related features
            	kOfferingVRelatedFeatures.put(offeringId,
            			getRelatedFeatureIDsFromOffering(offering.getRelatedFeatures()));
            	// Observation types
            	kOfferingVObservationTypes.put(offeringId,
            			getObservationTypesFromObservationConstellation(offering.getObservationConstellations()));
            	allowedkOfferingVObservationTypes.put(offeringId,
            			getObservationTypesFromObservationType(offering.getObservationTypes()));
            	// Temporal Envelope
            	kOfferingVMinTime.put(offeringId,
            			HibernateCriteriaQueryUtilities.getMinDate4Offering(offeringId, session));
            	kOfferingVMaxTime.put(offeringId,
            			HibernateCriteriaQueryUtilities.getMaxDate4Offering(offeringId, session));
            	// Spatial Envelope
            	kOfferingVEnvelope.put(offeringId,
            			getEnvelopeForOffering(offeringId, session));
            }
        }
        cache.setKOfferingVName(kOfferingVName);
        cache.setKOfferingVObservableProperties(kOfferingVObservableProperties);
        cache.setKOfferingVProcedures(kOfferingVProcedures);
        cache.setKOfferingVRelatedFeatures(kOfferingVRelatedFeatures);
        cache.setKOfferingVObservationTypes(kOfferingVObservationTypes);
        cache.setKOffrtingVFeatures(getFeaturesFromObservationForOfferings(kOfferingVName.keySet(), session));
        cache.setAllowedKOfferingVObservationType(allowedkOfferingVObservationTypes);
		cache.setKOfferingVEnvelope(kOfferingVEnvelope);
		cache.setKOfferingVMinTime(kOfferingVMinTime);
		cache.setKOfferingVMaxTime(kOfferingVMaxTime);
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

    /**
     * Set cache values related to procedure
     * 
     * @param cache
     *            CapabilitiesCache instance
     * @param session
     *            Hibernate session
     */
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
                        getOfferingsFromObservationCollection(procedure.getObservationConstellations()));
                kProcedureVObservableProperties.put(procedure.getIdentifier(),
                        getObservablePropertiesFromObservationConstellation(procedure.getObservationConstellations()));
                parentProcs.put(procedure.getIdentifier(),
                        getProcedureIDsFromProcedures(procedure.getProceduresForChildSensorId()));
            }
        }
        cache.setProcedures(procedures);
        cache.setKProcedureVOfferings(kProcedureVOffering);
        cache.setProcPhens(kProcedureVObservableProperties);
        cache.setProcedureHierarchies(parentProcs);
    }

    /**
     * Set cache values related to observableProperty
     * 
     * @param cache
     *            CapabilitiesCache instance
     * @param session
     *            Hibernate session
     */
    private void setObservablePropertyValues(CapabilitiesCache cache, Session session) {
        //List<String> observableProperties = new ArrayList<String>();
        List<ObservableProperty> hObservableProperties = HibernateCriteriaQueryUtilities.getObservablePropertyObjects(session);
        Map<String, Collection<String>> kObservablePropertyVOffering = new HashMap<String, Collection<String>>(hObservableProperties.size());
        Map<String, Collection<String>> kObservablePropertyVProcedures = new HashMap<String, Collection<String>>(hObservableProperties.size());
        for (ObservableProperty observableProperty : hObservableProperties) {
			//observableProperties.add(observableProperty.getIdentifier());
            kObservablePropertyVOffering.put(observableProperty.getIdentifier(),
                    getOfferingsFromObservationCollection(observableProperty.getObservationConstellations()));
            kObservablePropertyVProcedures.put(observableProperty.getIdentifier(),
                    getProceduresFromObservationConstellation(observableProperty.getObservationConstellations()));
        }
        cache.setKObservablePropertyVOfferings(kObservablePropertyVOffering);
        cache.setKObservablePropertyKProcedures(kObservablePropertyVProcedures);
    }

    /**
     * Set cache values related to featureOfInterest
     * 
     * @param cache
     *            CapabilitiesCache instance
     * @param session
     *            Hibernate session
     */
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

    /**
     * Set cache values related to compositePhenomenon
     * 
     * @param cache
     *            CapabilitiesCache instance
     * @param session
     *            Hibernate session
     */
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

    /**
     * Set cache values for SRID
     * 
     * @param cache
     *            CapabilitiesCache instance
     * @param session
     *            Hibernate session
     */
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

    /**
     * Get all FOIs related to an offering
     * 
     * @param offerings
     *            Offering list
     * @param session
     *            Hibernate session
     * @return Map with FOIs for each offering
     */
    private Map<String, Collection<String>> getFeaturesFromObservationForOfferings(Set<String> offerings,
            Session session) {
        Map<String, Collection<String>> kOfferingVFeatureOfInterest = new HashMap<String, Collection<String>>(offerings.size());
        for (String offering : offerings) {
            List<String> featureOfInterestIdentifiers = HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(offering, session);
            kOfferingVFeatureOfInterest.put(offering, featureOfInterestIdentifiers);
        }
        return kOfferingVFeatureOfInterest;
    }

    /**
     * Get all offering identifiers from ObservationConstellation objects
     * 
     * @param observationConstellations
     *            ObservationConstellation objects
     * @return List with offering identifiers
     */
    private List<String> getOfferingsFromObservationCollection(Set<ObservationConstellation> observationConstellations) {
        Set<String> offerings = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation oc : observationConstellations) {
            offerings.add(oc.getOffering().getIdentifier());
        }
        return new ArrayList<String>(offerings);
    }

    /**
     * Get all procedure identifiers from ObservationConstellation objects
     * 
     * @param observationConstellations
     *            ObservationConstellation objects
     * @return List with procedure identifiers
     */
    private List<String> getProceduresFromObservationConstellation(
            Set<ObservationConstellation> observationConstellations) {
        Set<String> procedures = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation observationConstellation : observationConstellations) {
            procedures.add(observationConstellation.getProcedure().getIdentifier());
        }
        return new ArrayList<String>(procedures);
    }

    /**
     * Get all observableProperty identifiers from ObservationConstellation
     * objects
     * 
     * @param observationConstellations
     *            ObservationConstellation objects
     * @return List with observableProperty identifiers
     */
    private List<String> getObservablePropertiesFromObservationConstellation(
            Set<ObservationConstellation> observationConstellations) {
        Set<String> observableProperties = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation observationConstellation : observationConstellations) {
            if (observationConstellation.getObservableProperty() != null) {
                observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
            }
        }
        return new ArrayList<String>(observableProperties);
    }

    /**
     * Get all observableProperty identifiers from ObservableProperty objects
     * 
     * @param observableProperties
     *            ObservableProperty objects
     * @return List with observableProperty identifiers
     */
    private List<String> getObservablePropertyIdentifierFromObservableProperties(
            Set<ObservableProperty> observableProperties) {
        List<String> observablePropertyIdentifiers = new ArrayList<String>(observableProperties.size());
        for (ObservableProperty observableProperty : observableProperties) {
            observablePropertyIdentifiers.add(observableProperty.getIdentifier());
        }
        return observablePropertyIdentifiers;
    }

    /**
     * Get all observationTypes from ObservationConstellation objects
     * 
     * @param observationConstellations
     *            ObservationConstellation objects
     * @return List with observationTypes
     */
    private List<String> getObservationTypesFromObservationConstellation(
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

    /**
     * Get relatedFeature identifiers
     * 
     * @param relatedFeatures
     *            RelatedFeature objects
     * @return List with relatedFeature identifiers
     */
    private Collection<String> getRelatedFeatureIDsFromOffering(Set<RelatedFeature> relatedFeatures) {
        List<String> relatedFeatureList = new ArrayList<String>(relatedFeatures.size());
        for (RelatedFeature relatedFeature : relatedFeatures) {
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

	
}
