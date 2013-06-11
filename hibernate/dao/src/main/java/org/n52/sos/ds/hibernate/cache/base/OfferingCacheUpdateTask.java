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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.hibernate.Session;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ds.hibernate.ThreadLocalSessionFactory;
import org.n52.sos.ds.hibernate.cache.DatasourceCacheUpdateHelper;
import org.n52.sos.ds.hibernate.dao.FeatureOfInterestDAO;
import org.n52.sos.ds.hibernate.dao.ObservablePropertyDAO;
import org.n52.sos.ds.hibernate.dao.ObservationDAO;
import org.n52.sos.ds.hibernate.dao.OfferingDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.TOffering;
import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CacheHelper;
import org.n52.sos.util.CollectionHelper;
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

    private List<OwsExceptionReport> errors = CollectionHelper.list();

    private Offering offering;

    private WritableContentCache cache;

    private List<ObservationConstellation> observationConstellations = CollectionHelper.list();

    OfferingCacheUpdateTask() {
    }

    protected OfferingCacheUpdateTask setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
        return this;
    }

    protected OfferingCacheUpdateTask setThreadLocalSessionFactory(ThreadLocalSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        return this;
    }

    protected OfferingCacheUpdateTask setWritableContentCache(WritableContentCache offeringCache) {
        this.cache = offeringCache;
        return this;
    }

    protected OfferingCacheUpdateTask setOffering(Offering offering) {
        this.offering = offering;
        return this;
    }

    protected OfferingCacheUpdateTask setErrorList(List<OwsExceptionReport> error) {
        this.errors = error;
        return this;
    }

    protected OfferingCacheUpdateTask setObservationConstellations(
            List<ObservationConstellation> observationConstellations) {
        this.observationConstellations = observationConstellations;
        return this;
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

    protected List<ObservationConstellation> getObservationConstellations() {
        return observationConstellations;
    }

    protected void getOfferingInformationFromDbAndAddItToCacheMaps(Session session) throws OwsExceptionReport {
        String dsOfferingId = getOffering().getIdentifier();
        String offeringId = CacheHelper.addPrefixOrGetOfferingIdentifier(dsOfferingId);

        getCache().addOffering(offeringId);
        getCache().setNameForOffering(offeringId, getOffering().getName());
        // Procedures
        final Map<ProcedureFlag, Set<String>> procedureIdentifiers = getProcedureIdentifier(session);

        getCache().setProceduresForOffering(offeringId, procedureIdentifiers.get(ProcedureFlag.PARENT));
        getCache().setHiddenChildProceduresForOffering(offeringId,
                procedureIdentifiers.get(ProcedureFlag.HIDDEN_CHILD));
        // Observable properties
        getCache().setObservablePropertiesForOffering(offeringId, getObservablePropertyIdentifier(session));

        // Observation types
        getCache().setObservationTypesForOffering(offeringId, getObservationTypes(session));
        if (getOffering() instanceof TOffering) {
            // Related features
            getCache().setRelatedFeaturesForOffering(offeringId,
                    getRelatedFeatureIdentifiersFrom((TOffering) getOffering()));
            getCache().setAllowedObservationTypeForOffering(offeringId,
                    getObservationTypesFromObservationType(((TOffering) getOffering()).getObservationTypes()));
        }
        // Spatial Envelope
        getCache().setEnvelopeForOffering(offeringId, getEnvelopeForOffering(dsOfferingId, session));
        // Features of Interest
        List<String> featureOfInterestIdentifiers =
                new FeatureOfInterestDAO().getFeatureOfInterestIdentifiersForOffering(dsOfferingId, session);
        getCache().setFeaturesOfInterestForOffering(offeringId,
                getValidFeaturesOfInterestFrom(featureOfInterestIdentifiers));
        // Temporal Envelope
        OfferingDAO offeringDAO = new OfferingDAO();
        getCache().setMinPhenomenonTimeForOffering(offeringId,
                offeringDAO.getMinDate4Offering(dsOfferingId, session));
        getCache().setMaxPhenomenonTimeForOffering(offeringId,
                offeringDAO.getMaxDate4Offering(dsOfferingId, session));
        getCache().setMinResultTimeForOffering(offeringId,
                offeringDAO.getMinResultTime4Offering(dsOfferingId, session));
        getCache().setMaxResultTimeForOffering(offeringId,
                offeringDAO.getMaxResultTime4Offering(dsOfferingId, session));
    }

    protected Map<ProcedureFlag, Set<String>> getProcedureIdentifier(Session session) {
        Set<String> procedures = new HashSet<String>(0);
        Set<String> hiddenChilds = new HashSet<String>(0);
        if (CollectionHelper.isNotEmpty(getObservationConstellations())) {
            for (ObservationConstellation oc : getObservationConstellations()) {
                if (oc.isHiddenChild()) {
                    hiddenChilds.add(CacheHelper.addPrefixOrGetProcedureIdentifier(oc.getProcedure().getIdentifier()));
                } else {
                    procedures.add(CacheHelper.addPrefixOrGetProcedureIdentifier(oc.getProcedure().getIdentifier()));
                }
            }
        } else {
            List<String> list = new ProcedureDAO().getProcedureIdentifiersForOffering(getOffering().getIdentifier(), session);
            if (list.size() > 1) {
                throw new RuntimeException(String.format("There are more than one procedures defined for the offering '%s'!", getOffering().getIdentifier()));
            }
            for (String procedureIdentifier : list) {
                procedures.add(CacheHelper.addPrefixOrGetProcedureIdentifier(procedureIdentifier));
            }
        }
        Map<ProcedureFlag, Set<String>> allProcedures = new HashMap<ProcedureFlag, Set<String>>();
        allProcedures.put(ProcedureFlag.PARENT, procedures);
        allProcedures.put(ProcedureFlag.HIDDEN_CHILD, hiddenChilds);
        return allProcedures;
    }

    protected Set<String> getRelatedFeatureIdentifiersFrom(TOffering hOffering) {
        Set<String> relatedFeatureList = new HashSet<String>(hOffering.getRelatedFeatures().size());
        for (RelatedFeature hRelatedFeature : hOffering.getRelatedFeatures()) {
            if (hRelatedFeature.getFeatureOfInterest() != null
                    && hRelatedFeature.getFeatureOfInterest().getIdentifier() != null) {
                relatedFeatureList.add(hRelatedFeature.getFeatureOfInterest().getIdentifier());
            }
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

    protected Set<String> getObservablePropertyIdentifier(Session session) {
        if (CollectionHelper.isNotEmpty(getObservationConstellations())) {
            return DatasourceCacheUpdateHelper.getAllObservablePropertyIdentifiersFrom(getObservationConstellations());
        } else {
            Set<String> observableProperties = CollectionHelper.set();
            List<String> list = new ObservablePropertyDAO().getObservablePropertyIdentifiersForOffering(getOffering().getIdentifier(), session);
            for (String observablePropertyIdentifier : list) {
                observableProperties.add(CacheHelper.addPrefixOrGetObservablePropertyIdentifier(observablePropertyIdentifier));
            }
            return observableProperties;
        }
    }

    protected Set<String> getObservationTypes(Session session) {
        if (CollectionHelper.isNotEmpty(getObservationConstellations())) {
            Set<String> observationTypes = CollectionHelper.set();
            for (ObservationConstellation oc : getObservationConstellations()) {
                if (oc.getObservationType() != null) {
                    observationTypes.add(oc.getObservationType().getObservationType());
                }
            }
            return observationTypes;
        } else {
           return getObservationTypesFromObservations(session); 
        }
    }

    private Set<String> getObservationTypesFromObservations(Session session) {
        ObservationDAO observationDAO = new ObservationDAO();
        Set<String> observationTypes = CollectionHelper.set();
        if (observationDAO.checkNumericObservationsFor(getOffering().getIdentifier(), session)){
            observationTypes.add(OMConstants.OBS_TYPE_MEASUREMENT);
        } else if (observationDAO.checkCategoryObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION);
        } else if (observationDAO.checkCountObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_COUNT_OBSERVATION);
        } else if (observationDAO.checkTextObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_TEXT_OBSERVATION);
        } else if (observationDAO.checkBooleanObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_TRUTH_OBSERVATION);
        } else if (observationDAO.checkBlobObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_OBSERVATION);
        } else if (observationDAO.checkGeometryObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION);
        } else if (observationDAO.checkSweDataArrayObservationsFor(getOffering().getIdentifier(), session)) {
            observationTypes.add(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        }
        return observationTypes;
    }

    protected SosEnvelope getEnvelopeForOffering(String offeringID, Session session) throws OwsExceptionReport {
        List<String> featureIDs = new FeatureOfInterestDAO().getFeatureOfInterestIdentifiersForOffering(offeringID, session);
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

    private enum ProcedureFlag {
        PARENT, HIDDEN_CHILD;
    }

    @Override
    public void execute() {
        try {
            getOfferingInformationFromDbAndAddItToCacheMaps(getSessionFactory().getSession());
        } catch (OwsExceptionReport owse) {
            getErrors().add(owse);
        } catch (Exception e) {
            getErrors().add(
                    new GenericThrowableWrapperException(e)
                            .withMessage("Error while processing offering cache update task!"));
        } finally {
            LOGGER.debug("OfferingTask finished, latch.countDown().");
            getCountDownLatch().countDown();
        }
    }
}
