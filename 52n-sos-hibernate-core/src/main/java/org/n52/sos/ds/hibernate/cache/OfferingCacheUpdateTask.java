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
package org.n52.sos.ds.hibernate.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.hibernate.Session;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OfferingCacheUpdateTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfferingCacheUpdateTask.class);
    private CountDownLatch countDownLatch;
    private IConnectionProvider connectionProvider;
    private List<OwsExceptionReport> errors;
    private Offering offering;
    private OfferingCache offeringCache;

    public OfferingCacheUpdateTask(CountDownLatch countDownLatch, IConnectionProvider connectionProvider,
            OfferingCache offeringCache, Offering offering,
            List<OwsExceptionReport> error) {
        this.countDownLatch = countDownLatch;
        this.connectionProvider = connectionProvider;
        this.offeringCache = offeringCache;
        this.offering = offering;
        this.errors = error;
    }
    
    protected CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    protected IConnectionProvider getConnectionProvider() {
        return connectionProvider;
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

    protected void getOfferingInformationFromDbAndAddItToCacheMaps(Session session) throws OwsExceptionReport {
        String offeringId = getOffering().getIdentifier();
        getOfferingCache().getKOfferingVName().put(offeringId, getOffering().getName());
        // Procedures
        getOfferingCache().getKOfferingVProcedures().put(offeringId, getProcedureIdentifierFrom(getOffering().getObservationConstellationOfferingObservationTypes()));
        // Observable properties
        getOfferingCache().getKOfferingVObservableProperties().put(offeringId, getObservablePropertyIdentifierFrom(getOffering().getObservationConstellationOfferingObservationTypes()));
        // Related features
        getOfferingCache().getKOfferingVRelatedFeatures().put(offeringId, getRelatedFeatureIdentifiersFrom(getOffering()));
        // Observation types
        getOfferingCache().getKOfferingVObservationTypes().put(offeringId, getObservationTypesFrom(getOffering().getObservationConstellationOfferingObservationTypes()));
        getOfferingCache().getAllowedkOfferingVObservationTypes().put(offeringId, getObservationTypesFromObservationType(getOffering().getObservationTypes()));
        // Spatial Envelope
        getOfferingCache().getKOfferingVEnvelope().put(offeringId, getEnvelopeForOffering(offeringId, session));
        // Features of Interest
        List<String> featureOfInterestIdentifiers = HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(getOffering().getName(), session);
        getOfferingCache().getKOfferingVFeaturesOfInterest().put(getOffering().getName(), featureOfInterestIdentifiers);
        // Temporal Envelope
        getOfferingCache().getKOfferingVMinTime().put(offeringId, HibernateCriteriaQueryUtilities.getMinDate4Offering(offeringId, session));
        getOfferingCache().getKOfferingVMaxTime().put(offeringId, HibernateCriteriaQueryUtilities.getMaxDate4Offering(offeringId, session));
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
        List<String> featureIDs = HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(offeringID, session);
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
    public void run() {
        Session session = null;
        try {
            session = (Session) getConnectionProvider().getConnection();
            getOfferingInformationFromDbAndAddItToCacheMaps(session);
        } catch (OwsExceptionReport e) {
            LOGGER.error(String.format("Exception thrown: %s", e.getMessage()), e);
            getErrors().add(e);
        } finally {
            if (session != null) {
                getConnectionProvider().returnConnection(session);
            }
            LOGGER.debug("OfferingTask finished, latch.countDown().");
            getCountDownLatch().countDown();
        }
    }
}
