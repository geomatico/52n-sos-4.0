/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.cache.ACapabilitiesCache;
import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.hibernate.entities.CompositePhenomenon;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.ds.hibernate.entities.SpatialRefSys;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.ValueTypes;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
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
        } catch (HibernateException he) {
            String exceptionText = "Error while initializing CapabilitiesCache!";
            LOGGER.debug(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
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
            String exceptionText = "Error while updateing CapabilitiesCache after sensor insertion!";
            LOGGER.debug(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
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
            session.close();
        } catch (HibernateException he) {
            String exceptionText = "Error while updateing CapabilitiesCache after observation insertion!";
            LOGGER.debug(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
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
            String exceptionText = "Error while updateing CapabilitiesCache after sensor deletion!";
            LOGGER.debug(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
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
    private void setOfferingValues(CapabilitiesCache cache, Session session) {
        Map<String, String> kOfferingVName = new HashMap<String, String>();
        Map<String, Collection<String>> kOfferingVProcedures = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kOfferingVObservableProperties = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kOfferingVRelatedFeatures = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kOfferingVObservationTypes = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> allowedkOfferingVObservationTypes = new HashMap<String, Collection<String>>();
        List<Offering> hOfferings = session.createCriteria(Offering.class).list();
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
            }
        }
        cache.setKOfferingVName(kOfferingVName);
        cache.setKOfferingVObservableProperties(kOfferingVObservableProperties);
        cache.setKOfferingVProcedures(kOfferingVProcedures);
        cache.setKOfferingVRelatedFeatures(kOfferingVRelatedFeatures);
        cache.setKOfferingVObservationTypes(kOfferingVObservationTypes);
        cache.setKOffrtingVFeatures(getFeaturesFromObservationForOfferings(kOfferingVName.keySet(), session));
        cache.setAllowedKOfferingVObservationType(allowedkOfferingVObservationTypes);
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
        List<String> procedures = new ArrayList<String>();
        Map<String, Collection<String>> kProcedureVOffering = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kProcedureVObservableProperties = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> parentProcs = new HashMap<String, Collection<String>>();
        List<Procedure> hProcedures = session.createCriteria(Procedure.class).list();
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
        List<String> observableProperties = new ArrayList<String>();
        Map<String, Collection<String>> kObservablePropertyVOffering = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> kObservablePropertyVProcedures = new HashMap<String, Collection<String>>();
        Map<String, ValueTypes> kObservablePropertyVValutType = new HashMap<String, ValueTypes>();
        List<ObservableProperty> hObservableProperties = session.createCriteria(ObservableProperty.class).list();
        for (ObservableProperty observableProperty : hObservableProperties) {
            observableProperties.add(observableProperty.getIdentifier());
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
        List<String> featurOfInterestList = new ArrayList<String>();
        Map<String, Collection<String>> kFeatureOfInterestVProcedure = new HashMap<String, Collection<String>>();
        Map<String, Collection<String>> parentFeatures = new HashMap<String, Collection<String>>();
        List<FeatureOfInterest> hFeaturesOfInterest = session.createCriteria(FeatureOfInterest.class).list();
        for (FeatureOfInterest featureOfInterest : hFeaturesOfInterest) {
            kFeatureOfInterestVProcedure.put(featureOfInterest.getIdentifier(),
                    HibernateCriteriaQueryUtilities.getProceduresForFeatureOfInterest(session, featureOfInterest));
            parentFeatures.put(featureOfInterest.getIdentifier(),
                    getFeatureIDsFromFeatures(featureOfInterest.getFeatureOfInterestsForChildFeatureId()));
        }
        cache.setFeaturesOfInterest(getFeatureOfInterestIdentifier(hFeaturesOfInterest));
        cache.setAllFeatures(getFeatureIdentifier(hFeaturesOfInterest));
        cache.setKFeatureOfInterestVProcedures(kFeatureOfInterestVProcedure);
        cache.setFeatureHierarchies(parentFeatures);
    }

    private void setRelatedFeatures(CapabilitiesCache cache, Session session) {
        Map<String, Collection<String>> relatedFeatureList = new HashMap<String, Collection<String>>();
        List<RelatedFeature> relatedFeatures = session.createCriteria(RelatedFeature.class).list();
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
        List<CompositePhenomenon> compositePhenomenons = session.createCriteria(CompositePhenomenon.class).list();
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
        List<SpatialRefSys> spatialRefSyss = session.createCriteria(SpatialRefSys.class).list();
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
            Criteria criteria = session.createCriteria(Observation.class);
            criteria.createAlias("observationConstellation", "oc");
            criteria.createAlias("oc.offering", "off");
            criteria.add(Restrictions.eq("off.identifier", offering));
            criteria.setProjection(Projections.distinct(Projections.property("featureOfInterest")));
            List<FeatureOfInterest> featureOfInterests = criteria.list();
            kOfferingVFeatureOfInterest.put(offering, getFeatureOfInterestIdentifier(featureOfInterests));
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
            observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
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

    private Collection<String> getObservationTypesFromObservationType(Set observationTypes) {
        Set<String> obsTypes = new HashSet<String>();
        for (ObservationType obsType : (Set<ObservationType>) observationTypes) {
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

    /**
     * Get FOI identifiers
     * 
     * @param featureOfInterests
     *            FOI objects
     * @return List wiht FOI identifiers
     */
    private List<String> getFeatureOfInterestIdentifier(List<FeatureOfInterest> featureOfInterests) {
        List<String> featureOfInterestList = new ArrayList<String>();
        for (FeatureOfInterest featureOfInterest : featureOfInterests) {
            if (featureOfInterest.isSamplingFeature()) {
                featureOfInterestList.add(featureOfInterest.getIdentifier());
            }
        }
        return featureOfInterestList;
    }

    private Collection<String> getFeatureIdentifier(List<FeatureOfInterest> featuresOfInterest) {
        List<String> featureList = new ArrayList<String>();
        for (FeatureOfInterest featureOfInterest : featuresOfInterest) {
            if (featureOfInterest.isSamplingFeature()) {
                featureList.add(featureOfInterest.getIdentifier());
            }
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
}
