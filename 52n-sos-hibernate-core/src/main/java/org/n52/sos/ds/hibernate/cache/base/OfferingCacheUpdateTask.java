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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.hibernate.Session;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.ThreadLocalSessionFactory;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
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
    private OfferingCache offeringCache;

    OfferingCacheUpdateTask(
            CountDownLatch countDownLatch, 
            ThreadLocalSessionFactory sessionFactory,
            OfferingCache offeringCache, 
            Offering offering,
            List<OwsExceptionReport> error) {
        this.countDownLatch = countDownLatch;
        this.sessionFactory = sessionFactory;
        this.offeringCache = offeringCache;
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

    protected OfferingCache getOfferingCache() {
        return offeringCache;
    }
    
    public ThreadLocalSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void getOfferingInformationFromDbAndAddItToCacheMaps(Session session) throws OwsExceptionReport {
        String offeringId = getOffering().getIdentifier();
        getOfferingCache().setName(offeringId, getOffering().getName());
        // Procedures
        getOfferingCache().setProcedures(offeringId, getProcedureIdentifierFrom(getOffering().getObservationConstellationOfferingObservationTypes()));
        // Observable properties
        getOfferingCache().setObservableProperties(offeringId, getObservablePropertyIdentifierFrom(getOffering().getObservationConstellationOfferingObservationTypes()));
        // Related features
        getOfferingCache().setRelatedFeatures(offeringId, getRelatedFeatureIdentifiersFrom(getOffering()));
        // Observation types
        getOfferingCache().setObservationTypes(offeringId, getObservationTypesFrom(getOffering().getObservationConstellationOfferingObservationTypes()));
        getOfferingCache().setAllowedObservationType(offeringId, getObservationTypesFromObservationType(getOffering().getObservationTypes()));
        // Spatial Envelope
        getOfferingCache().setEnvelope(offeringId, getEnvelopeForOffering(offeringId, session));
        // Features of Interest
        List<String> featureOfInterestIdentifiers = HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(getOffering().getName(), session);
        getOfferingCache().setFeaturesOfInterest(getOffering().getName(), featureOfInterestIdentifiers);
        // Temporal Envelope
        getOfferingCache().setMinTime(offeringId, HibernateCriteriaQueryUtilities.getMinDate4Offering(offeringId, session));
        getOfferingCache().setMaxTime(offeringId, HibernateCriteriaQueryUtilities.getMaxDate4Offering(offeringId, session));
    }

    protected List<String> getProcedureIdentifierFrom(Set<ObservationConstellationOfferingObservationType> set) {
        Set<String> procedures = new HashSet<String>(set.size());
        for (ObservationConstellationOfferingObservationType ocoot : set) {
            procedures.add(ocoot.getObservationConstellation().getProcedure().getIdentifier());
        }
        return new ArrayList<String>(procedures);
    }

    protected Collection<String> getRelatedFeatureIdentifiersFrom(Offering hOffering) {
        List<String> relatedFeatureList = new ArrayList<String>(hOffering.getRelatedFeatures().size());
        for (RelatedFeature hRelatedFeature : hOffering.getRelatedFeatures()) {
            relatedFeatureList.add(hRelatedFeature.getFeatureOfInterest().getIdentifier());
        }
        return relatedFeatureList;
    }

    protected List<String> getObservablePropertyIdentifierFrom(Set<ObservationConstellationOfferingObservationType> set) {
        Set<String> observableProperties = new HashSet<String>(set.size());
        for (ObservationConstellationOfferingObservationType ocoot : set) {
            if (ocoot.getObservationConstellation().getObservableProperty() != null) {
                observableProperties.add(ocoot.getObservationConstellation().getObservableProperty().getIdentifier());
            }
        }
        return new ArrayList<String>(observableProperties);
    }

    protected List<String> getObservationTypesFrom(Set<ObservationConstellationOfferingObservationType> set) {
        Set<String> observationTypes = new HashSet<String>(set.size());
        for (ObservationConstellationOfferingObservationType ocoot : set) {
            if (ocoot.getObservationType() != null) {
                observationTypes.add(ocoot.getObservationType().getObservationType());
            }
        }
        return new ArrayList<String>(observationTypes);
    }

    protected SosEnvelope getEnvelopeForOffering(String offeringID, Session session) throws OwsExceptionReport {
        List<String> featureIDs = getFeatureOfInterestIdentifiersForOffering(offeringID, session);
        if (featureIDs != null && !featureIDs.isEmpty()) {
            return Configurator.getInstance().getFeatureQueryHandler().getEnvelopeForFeatureIDs(featureIDs, session);
        }
        return null;
    }

    protected Collection<String> getObservationTypesFromObservationType(Set<ObservationType> observationTypes) {
        Set<String> obsTypes = new HashSet<String>(observationTypes.size());
        for (ObservationType obsType : observationTypes) {
            obsTypes.add(obsType.getObservationType());
        }
        return new ArrayList<String>(obsTypes);
    }
    
    @Override
    public void execute() {
        try {
            getOfferingInformationFromDbAndAddItToCacheMaps(getSessionFactory().getSession());
        } catch (OwsExceptionReport e) {
            LOGGER.error(String.format("Exception thrown: %s", e.getMessage()), e);
            getErrors().add(e);
        } finally {
            LOGGER.debug("OfferingTask finished, latch.countDown().");
            getCountDownLatch().countDown();
        }
    }
}
