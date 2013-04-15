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
package org.n52.sos.ds.hibernate.cache.base;


import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ds.hibernate.ThreadLocalSessionFactory;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CacheHelper;
import org.n52.sos.util.RunnableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
class OfferingCacheUpdateTask extends RunnableAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferingCacheUpdateTask.class);
    private CountDownLatch countDownLatch;
    private ThreadLocalSessionFactory sessionFactory;
    private List<OwsExceptionReport> errors;
    private Offering offering;
    private WritableContentCache cache;

    OfferingCacheUpdateTask(
            CountDownLatch countDownLatch, 
            ThreadLocalSessionFactory sessionFactory,
            WritableContentCache offeringCache,
            Offering offering,
            List<OwsExceptionReport> error) {
        this.countDownLatch = countDownLatch;
        this.sessionFactory = sessionFactory;
        this.cache = offeringCache;
        this.offering = offering;
        this.errors = error;
    }
    
    protected CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    protected List<OwsExceptionReport> getErrors() {
        return errors;
    }

    protected Offering getOffering() {
        return offering;
    }

    protected WritableContentCache getCache() {
        return cache;
    }
    
    public ThreadLocalSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void getOfferingInformationFromDbAndAddItToCacheMaps(Session session) throws OwsExceptionReport {
        String dsOfferingId = getOffering().getIdentifier();
        String offeringId = CacheHelper.addPrefixOrGetOfferingIdentifier(dsOfferingId);

        getCache().addOffering(offeringId);
        getCache().setNameForOffering(offeringId, getOffering().getName());
        // Procedures
        @SuppressWarnings("unchecked")
        List<ObservationConstellation> observationConstellations = session
                .createCriteria(ObservationConstellation.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq(ObservationConstellation.DELETED, false))
                .add(Restrictions.eq(ObservationConstellation.OFFERING, offering))
                .list();
        final Set<String> procedureIdentifiers = getProcedureIdentifierFrom(observationConstellations);


        getCache().setProceduresForOffering(offeringId, procedureIdentifiers);
        // Observable properties
        getCache().setObservablePropertiesForOffering(offeringId, getObservablePropertyIdentifierFrom(observationConstellations));
        // Related features
        getCache().setRelatedFeaturesForOffering(offeringId, getRelatedFeatureIdentifiersFrom(getOffering()));
        // Observation types
        getCache().setObservationTypesForOffering(offeringId, getObservationTypesFrom(observationConstellations));
        getCache()
                .setAllowedObservationTypeForOffering(offeringId, getObservationTypesFromObservationType(getOffering()
                .getObservationTypes()));
        // Spatial Envelope
        getCache().setEnvelopeForOffering(offeringId, getEnvelopeForOffering(dsOfferingId, session));
        // Features of Interest
        List<String> featureOfInterestIdentifiers = HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(dsOfferingId, session);
        getCache()
                .setFeaturesOfInterestForOffering(offeringId, getValidFeaturesOfInterestFrom(featureOfInterestIdentifiers));
        // Temporal Envelope
        getCache().setMinPhenomenonTimeForOffering(offeringId, HibernateCriteriaQueryUtilities
                .getMinDate4Offering(dsOfferingId, session));
        getCache().setMaxPhenomenonTimeForOffering(offeringId, HibernateCriteriaQueryUtilities
                .getMaxDate4Offering(dsOfferingId, session));
        getCache().setMinResultTimeForOffering(offeringId, HibernateCriteriaQueryUtilities
                .getMinResultTime4Offering(dsOfferingId, session));
        getCache().setMaxResultTimeForOffering(offeringId, HibernateCriteriaQueryUtilities
                .getMaxResultTime4Offering(dsOfferingId, session));
    }

    protected Set<String> getProcedureIdentifierFrom(Collection<ObservationConstellation> set) {
        Set<String> procedures = new HashSet<String>(set.size());
        for (ObservationConstellation oc : set) {
            procedures.add(CacheHelper.addPrefixOrGetProcedureIdentifier(oc.getProcedure().getIdentifier()));
        }
        return procedures;
    }

    protected Set<String> getRelatedFeatureIdentifiersFrom(Offering hOffering) {
        Set<String> relatedFeatureList = new HashSet<String>(hOffering.getRelatedFeatures().size());
        for (RelatedFeature hRelatedFeature : hOffering.getRelatedFeatures()) {
            relatedFeatureList.add(hRelatedFeature.getFeatureOfInterest().getIdentifier());
        }
        return relatedFeatureList;
    }

    protected Collection<String> getValidFeaturesOfInterestFrom(List<String> featureOfInterestIdentifiers) {
       Set<String> features = new HashSet<String>(featureOfInterestIdentifiers.size());
       for (String featureIdentifier : featureOfInterestIdentifiers) {
           features.add(CacheHelper.addPrefixOrGetFeatureIdentifier(featureIdentifier));
       }
       return features;
    }

    protected Set<String> getObservablePropertyIdentifierFrom(Collection<ObservationConstellation> set) {
        Set<String> observableProperties = new HashSet<String>(set.size());
        for (ObservationConstellation oc : set) {
            if (oc.getObservableProperty() != null) {
                observableProperties.add(CacheHelper.addPrefixOrGetObservablePropertyIdentifier(oc.getObservableProperty().getIdentifier()));
            }
        }
        return observableProperties;
    }

    protected Set<String> getObservationTypesFrom(Collection<ObservationConstellation> set) {
        Set<String> observationTypes = new HashSet<String>(set.size());
        for (ObservationConstellation oc : set) {
            if (oc.getObservationType() != null) {
                observationTypes.add(oc.getObservationType().getObservationType());
            }
        }
        return observationTypes;
    }

    protected SosEnvelope getEnvelopeForOffering(String offeringID, Session session) throws OwsExceptionReport {
        List<String> featureIDs = getFeatureOfInterestIdentifiersForOffering(offeringID, session);
        if (featureIDs != null && !featureIDs.isEmpty()) {
            return Configurator.getInstance().getFeatureQueryHandler().getEnvelopeForFeatureIDs(featureIDs, session);
        }
        return null;
    }

    protected Set<String> getObservationTypesFromObservationType(Set<ObservationType> observationTypes) {
        Set<String> obsTypes = new HashSet<String>(observationTypes.size());
        for (ObservationType obsType : observationTypes) {
            obsTypes.add(obsType.getObservationType());
        }
        return obsTypes;
    }
    
    @Override
    public void execute() {
        try {
            getOfferingInformationFromDbAndAddItToCacheMaps(getSessionFactory().getSession());
        } catch (OwsExceptionReport owse) {
            getErrors().add(owse);
        } catch (Exception e) {
            getErrors().add(new GenericThrowableWrapperException(e)
                    .withMessage("Error while processing offering cache update task!"));
        } finally {
            LOGGER.debug("OfferingTask finished, latch.countDown().");
            getCountDownLatch().countDown();
        }
    }
}
