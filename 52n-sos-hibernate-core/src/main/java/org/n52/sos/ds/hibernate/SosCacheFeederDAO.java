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

import com.vividsolutions.jts.geom.Envelope;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.cache.ACapabilitiesCache;
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
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface ICacheFeederDAO
 * 
 */
public class SosCacheFeederDAO implements ICacheFeederDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosCacheFeederDAO.class);

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public SosCacheFeederDAO() {
        connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.ICacheFeederDAO#initalizeCache(org.n52.sos.cache.
     * ACapabilitiesCache)
     */
    @Override
    public void initalizeCache(ACapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        // TODO: cache minMax Times for Offering/all (reduce GetCaps query time)
        // TODO: cache BBOX for Offering/all (reduce GetCaps query time)
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
        Session session = null;
        try {
            session = (Session) connectionProvider.getConnection();
            setOfferingValues(cache, session);
            setProcedureValues(cache, session);
            setObservablePropertyValues(cache, session);
            setFeatureOfInterestValues(cache, session);
            setRelatedFeatures(cache, session);
            setCompositePhenomenonValues(cache, session);
            setSridValues(cache, session);
            setObservationTypes(cache, session);
            setResultTemplateValues(cache, session);
        } catch (HibernateException he) {
            String exceptionText = "Error while initializing CapabilitiesCache!";
            LOGGER.error(exceptionText, he);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    @Override
    public void updateAfterSensorInsertion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = (Session) connectionProvider.getConnection();
            setOfferingValues(cache, session);
            setProcedureValues(cache, session);
            setObservablePropertyValues(cache, session);
            setFeatureOfInterestValues(cache, session);
            setRelatedFeatures(cache, session);
            setCompositePhenomenonValues(cache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after sensor insertion!";
            LOGGER.error(exceptionText, he);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    @Override
    public void updateAfterSensorDeletion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = (Session) connectionProvider.getConnection();
            setOfferingValues(cache, session);
            setProcedureValues(cache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after sensor deletion!";
            LOGGER.error(exceptionText, he);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    @Override
    public void updateAfterObservationInsertion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = (Session) connectionProvider.getConnection();
            setFeatureOfInterestValues(cache, session);
			setOfferingValues(cache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after observation insertion!";
            LOGGER.error(exceptionText, he);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    @Override
    public void updateAfterObservationDeletion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport
    {
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
        Session session = null;
        try {
            // TODO: check which setter are necessary (see updateAfterObservationInsertion)
            session = (Session) connectionProvider.getConnection();
            setFeatureOfInterestValues(cache, session);
            setOfferingValues(cache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after observation deletion!";
            LOGGER.error(exceptionText, he);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    @Override
    public void updateAfterResultTemplateInsertion(CapabilitiesCache capabilitiesCache) throws OwsExceptionReport {
        CapabilitiesCache cache = (CapabilitiesCache) capabilitiesCache;
        Session session = null;
        try {
            // TODO: check which setter are necessary
            session = (Session) connectionProvider.getConnection();
            setOfferingValues(cache, session);
            setProcedureValues(cache, session);
            setObservablePropertyValues(cache, session);
            setFeatureOfInterestValues(cache, session);
            setRelatedFeatures(cache, session);
            setCompositePhenomenonValues(cache, session);
            setResultTemplateValues(cache, session);
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache after resultTemplate insertion!";
            LOGGER.error(exceptionText, he);
        } finally {
            connectionProvider.returnConnection(session);
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
        Map<String, String> kOfferingVName = new HashMap<String, String>();
        Map<String, Collection<String>> kOfferingVProcedures = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kOfferingVObservableProperties = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kOfferingVRelatedFeatures = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kOfferingVObservationTypes = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> allowedkOfferingVObservationTypes = new HashMap<String, Collection<String>>();
		Map<String, DateTime> kOfferingVMinTime = new HashMap<String, DateTime>();
		Map<String, DateTime> kOfferingVMaxTime = new HashMap<String, DateTime>();
		Map<String, SosEnvelope> kOfferingVEnvelope = new HashMap<String, SosEnvelope>();
				
        List<Offering> hOfferings = HibernateCriteriaQueryUtilities.getOfferingObjects(session);
        for (Offering offering : hOfferings) {
            if (!checkOfferingForDeletedProcedure(offering.getObservationConstellations())) {
                kOfferingVName.put(offering.getIdentifier(), offering.getName());
                kOfferingVProcedures.put(offering.getIdentifier(),
                        getProceduresFromObservationConstellation(offering.getObservationConstellations()));
                kOfferingVObservableProperties.put(offering.getIdentifier(),
                        getObservablePropertiesFromObservationConstellation(offering.getObservationConstellations()));
                kOfferingVRelatedFeatures.put(offering.getIdentifier(),
                        getRelatedFeatureIDsFromOffering(offering.getRelatedFeatures()));
                kOfferingVObservationTypes.put(offering.getIdentifier(),
                        getObservationTypesFromObservationConstellation(offering.getObservationConstellations()));
                allowedkOfferingVObservationTypes.put(offering.getIdentifier(),
                        getObservationTypesFromObservationType(offering.getObservationTypes()));
				kOfferingVMinTime.put(offering.getIdentifier(),
						HibernateCriteriaQueryUtilities.getMinDate4Offering(offering.getIdentifier(), session));
				kOfferingVMaxTime.put(offering.getIdentifier(),
						HibernateCriteriaQueryUtilities.getMaxDate4Offering(offering.getIdentifier(), session));
				kOfferingVEnvelope.put(offering.getIdentifier(),
						getEnvelopeForOffering(offering.getIdentifier(), session));
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
        session.clear();
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
        Set<String> procedures = new HashSet<String>();
        Map<String, Collection<String>> kProcedureVOffering = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kProcedureVObservableProperties = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> parentProcs = new HashMap<String, Collection<String>>();
        List<Procedure> hProcedures = HibernateCriteriaQueryUtilities.getProcedureObjects(session);
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
        Map<String, Collection<String>> kObservablePropertyVOffering = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kObservablePropertyVProcedures = new HashMap<String, Collection<String>>();
        List<ObservableProperty> hObservableProperties = HibernateCriteriaQueryUtilities.getObservablePropertyObjects(session);
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
    private void setFeatureOfInterestValues(CapabilitiesCache cache, Session session) {
        Map<String, Collection<String>> kFeatureOfInterestVProcedure = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> parentFeatures = new HashMap<String, Collection<String>>();
        List<FeatureOfInterest> hFeaturesOfInterest = HibernateCriteriaQueryUtilities.getFeatureOfInterestObjects(session);
        for (FeatureOfInterest featureOfInterest : hFeaturesOfInterest) {
            kFeatureOfInterestVProcedure.put(featureOfInterest.getIdentifier(),
                    HibernateCriteriaQueryUtilities.getProceduresForFeatureOfInterest(session, featureOfInterest));
            parentFeatures.put(featureOfInterest.getIdentifier(),
                    getFeatureIDsFromFeatures(featureOfInterest.getFeatureOfInterestsForChildFeatureId()));
        }
        cache.setFeatureOfInterest(getFeatureIdentifier(hFeaturesOfInterest));
        cache.setKFeatureOfInterestVProcedures(kFeatureOfInterestVProcedure);
        cache.setFeatureHierarchies(parentFeatures);
    }

    private void setRelatedFeatures(CapabilitiesCache cache, Session session) {
        Map<String, Collection<String>> relatedFeatureList = new HashMap<String, Collection<String>>();
        List<RelatedFeature> relatedFeatures = HibernateCriteriaQueryUtilities.getRelatedFeatureObjects(session);
        for (RelatedFeature relatedFeature : relatedFeatures) {
            Set<String> roles = new HashSet<String>();
            for (RelatedFeatureRole relatedFeatureRole : (Set<RelatedFeatureRole>) relatedFeature
                    .getRelatedFeatureRoles()) {
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
        Map<String, Collection<String>> kCompositePhenomenonVObservableProperty =
                new HashMap<String, Collection<String>>();
        List<CompositePhenomenon> compositePhenomenons = HibernateCriteriaQueryUtilities.getCompositePhenomenonObjects(session);
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
        List<Integer> srids = new ArrayList<Integer>();
        List<SpatialRefSys> spatialRefSyss = HibernateCriteriaQueryUtilities.getSpatialReySysObjects(session);
        for (SpatialRefSys spatialRefSys : spatialRefSyss) {
            srids.add(spatialRefSys.getSrid());
        }
        cache.setSrids(srids);
    }

    private void setObservationTypes(CapabilitiesCache cache, Session session) {
        cache.setObservationTypes(HibernateCriteriaQueryUtilities.getObservationTypes(session));
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
        Map<String, Collection<String>> kOfferingVFeatureOfInterest = new HashMap<String, Collection<String>>();
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
        Set<String> offerings = new HashSet<String>();
        Iterator<ObservationConstellation> iter = observationConstellations.iterator();
        while (iter.hasNext()) {
            ObservationConstellation observationConstellation = (ObservationConstellation) iter.next();
            offerings.add(observationConstellation.getOffering().getIdentifier());
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
        Set<String> procedures = new HashSet<String>();
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
        Set<String> observableProperties = new HashSet<String>();
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
        List<String> observablePropertyIdentifiers = new ArrayList<String>();
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
        Set<String> observationTypes = new HashSet<String>();
        for (ObservationConstellation observationConstellation : observationConstellations) {
            if (observationConstellation.getObservationType() != null) {
                observationTypes.add(observationConstellation.getObservationType().getObservationType());
            }
        }
        return new ArrayList<String>(observationTypes);
    }

    private Collection<String> getObservationTypesFromObservationType(Set<ObservationType> observationTypes) {
        Set<String> obsTypes = new HashSet<String>();
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
        List<String> relatedFeatureList = new ArrayList<String>();
        for (RelatedFeature relatedFeature : relatedFeatures) {
            relatedFeatureList.add(relatedFeature.getFeatureOfInterest().getIdentifier());
        }
        return relatedFeatureList;
    }

    private Collection<String> getFeatureIdentifier(List<FeatureOfInterest> featuresOfInterest) {
        List<String> featureList = new ArrayList<String>();
        for (FeatureOfInterest featureOfInterest : featuresOfInterest) {
            featureList.add(featureOfInterest.getIdentifier());
        }
        return featureList;
    }

    private Collection<String> getProcedureIDsFromProcedures(Set<Procedure> proceduresForChildSensorId) {
        List<String> procedureIDs = new ArrayList<String>();
        for (Procedure procedure : proceduresForChildSensorId) {
            procedureIDs.add(procedure.getIdentifier());
        }
        return procedureIDs;
    }

    private Collection<String> getFeatureIDsFromFeatures(Set<FeatureOfInterest> featureOfInterestsForChildFeatureId) {
        List<String> featureIDs = new ArrayList<String>();
        for (FeatureOfInterest feature : featureOfInterestsForChildFeatureId) {
            featureIDs.add(feature.getIdentifier());
        }
        return featureIDs;
    }

    private void setResultTemplateValues(CapabilitiesCache cache, Session session) {
        List<ResultTemplate> resultTemplateObjects = HibernateCriteriaQueryUtilities.getResultTemplateObjects(session);
        List<String> resultTemplates = new ArrayList<String>();
        for (ResultTemplate resultTemplateObject : resultTemplateObjects) {
            resultTemplates.add(resultTemplateObject.getIdentifier());
        }
        cache.setResultTemplates(resultTemplates);
    }

	
}
