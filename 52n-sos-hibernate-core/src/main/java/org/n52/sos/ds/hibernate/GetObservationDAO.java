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
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.AbstractGetObservationDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateObservationUtilities;
import org.n52.sos.ds.hibernate.util.QueryHelper;
import org.n52.sos.exception.ows.MissingParameterValueException.MissingObservedPropertyParameterException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SosHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface IGetObservationDAO
 * 
 */
public class GetObservationDAO extends AbstractGetObservationDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetObservationDAO.class);
    
    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();

    @Override
    public GetObservationResponse getObservation(GetObservationRequest sosRequest) throws OwsExceptionReport {
        Session session = null;
        try {
            session = sessionHolder.getSession();
            if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)
                && sosRequest.getObservedProperties().isEmpty()) {
                throw new MissingObservedPropertyParameterException();
            } else {
                GetObservationResponse sosResponse = new GetObservationResponse();
                sosResponse.setService(sosRequest.getService());
                sosResponse.setVersion(sosRequest.getVersion());
                sosResponse.setResponseFormat(sosRequest.getResponseFormat());
                if (getConfigurator().getProfileHandler().getActiveProfile().isShowMetadataOfEmptyObservations()) {
                    // TODO Hydro-Profile adds empty observation metadata to response
                    sosResponse.setObservationCollection(queryObservationHydro(sosRequest, session));
                } else {
                    sosResponse.setObservationCollection(queryObservation(sosRequest, session));
                }
                return sosResponse;
            }
        } catch (HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while querying data observation data!");
        } finally {
            sessionHolder.returnSession(session);
        }
    }
    
    protected List<SosObservation> queryObservation(GetObservationRequest request, Session session)
            throws OwsExceptionReport {
        long start = System.currentTimeMillis();
        Map<String, String> observationAliases = new HashMap<String, String>();
        HibernateQueryObject observationQueryObject = new HibernateQueryObject();
        String obsConstAlias =
                HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(observationAliases, null);
        String obsConstOffObsTypeAlias = HibernateCriteriaQueryUtilities.addObservationConstellationOfferingObservationTypesAliasToMap(
                observationAliases, obsConstAlias);

        // offering
        if (request.isSetOffering()) {
            observationQueryObject.addCriterion(getCriterionForOffering(observationAliases,
                    obsConstOffObsTypeAlias, request.getOfferings()));
        }
        // observableProperties
        if (request.isSetObservableProperty()) {
            observationQueryObject.addCriterion(getCriterionForObservableProperties(observationAliases, obsConstAlias,
                    request.getObservedProperties()));
        }
        // procedures
        if (request.isSetProcedure()) {
            observationQueryObject.addCriterion(getCriterionForProcedures(observationAliases, obsConstAlias,
                    request.getProcedures()));
        }
        observationQueryObject.addCriterion(Restrictions.isNotNull(HibernateCriteriaQueryUtilities
                .getParameterWithPrefix(HibernateConstants.PARAMETER_OBSERVATION_TYPE, obsConstOffObsTypeAlias)));

        // feature identifier
        Set<String> featureIdentifier =
                QueryHelper.getFeatureIdentifier(request.getSpatialFilter(), request.getFeatureIdentifiers(), session);
        if (featureIdentifier != null && featureIdentifier.isEmpty()) {
            return null;
        } else if (featureIdentifier != null && !featureIdentifier.isEmpty()) {
            String foiAlias = HibernateCriteriaQueryUtilities.addFeatureOfInterestAliasToMap(observationAliases, null);
            observationQueryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(foiAlias), new ArrayList<String>(
                            featureIdentifier)));
        }

        observationQueryObject.setAliases(observationAliases);
        List<SosObservation> sosObservations = new LinkedList<SosObservation>();

        // temporal filters
        List<FirstLatest> firstLatestTemporalFilter = null;
        Criterion criterionForTemporalFilters = null;
        if (request.hasTemporalFilters()) {
            firstLatestTemporalFilter = SosHelper.getFirstLatestTemporalFilter(request.getTemporalFilters());
            List<TemporalFilter> nonFirstLatestTemporalFilter =
                    SosHelper.getNonFirstLatestTemporalFilter(request.getTemporalFilters());
            if (nonFirstLatestTemporalFilter != null && !nonFirstLatestTemporalFilter.isEmpty()) {
                criterionForTemporalFilters =
                        HibernateCriteriaQueryUtilities.getCriterionForTemporalFilters(nonFirstLatestTemporalFilter);
            }
        }
        // query observations
        // TODO Threadable !?!
        // TODO How to ensure no duplicated observations ?!
        // TODO How to ensure that anti subsetting observation are also
        // included ?!
        Set<Observation> observations = new HashSet<Observation>(0);
            if (request.hasTemporalFilters()) {
                if (firstLatestTemporalFilter != null && !firstLatestTemporalFilter.isEmpty()) {
                    for (FirstLatest firstLatest : firstLatestTemporalFilter) {
                        HibernateQueryObject firstLatestQueryObject = observationQueryObject.clone();
                        firstLatestQueryObject.addOrder(HibernateCriteriaQueryUtilities.getOrderForEnum(firstLatest));
                        firstLatestQueryObject.setMaxResult(1);
                        observations.addAll(HibernateCriteriaQueryUtilities.getObservations(firstLatestQueryObject,
                                session));
                    }
                } else if (criterionForTemporalFilters != null) {
                    observationQueryObject.addCriterion(criterionForTemporalFilters);
                    observations.addAll(HibernateCriteriaQueryUtilities.getObservations(observationQueryObject, session));
                }
            } else {
                observations.addAll(HibernateCriteriaQueryUtilities.getObservations(observationQueryObject, session));
            }
        LOGGER.debug("Time to query observations needs {} ms!", (System.currentTimeMillis()-start));
        if (!observations.isEmpty()) {
            long startProcess = System.currentTimeMillis();
            sosObservations.addAll(HibernateObservationUtilities.createSosObservationsFromObservations(
                    observations, request.getVersion(), session));
            LOGGER.debug("Time to process observations needs {} ms!", (System.currentTimeMillis()-startProcess));
        }
        LOGGER.debug("Time to query and process observations needs {} ms!", (System.currentTimeMillis()-start));
        return sosObservations;
        
    }

        
    /**
     * Query observations from database depending on requested filters
     * 
     * @param request
     *            GetObservation request
     * @param session
     *            Hibernate session
     * @return List of Observation objects

     *
     * @throws OwsExceptionReport * If an error occurs.
     */
    protected List<SosObservation> queryObservationHydro(GetObservationRequest request, Session session)
            throws OwsExceptionReport {
        long start = System.currentTimeMillis();
        Map<String, String> observationConstellationAliases = new HashMap<String, String>();
        HibernateQueryObject observationConstellationQueryObject = new HibernateQueryObject();
        String obsConstOffObsTypeAlias =
                HibernateCriteriaQueryUtilities.addObservationConstellationOfferingObservationTypesAliasToMap(
                        observationConstellationAliases, null);
        
        Map<String, String> observationAliases = new HashMap<String, String>();
        HibernateQueryObject observationQueryObject = new HibernateQueryObject();
        String obsConstAlias =
                HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(observationAliases, null);
        HibernateCriteriaQueryUtilities.addObservationConstellationOfferingObservationTypesAliasToMap(
                observationAliases, null);

        // offering
        if (request.isSetOffering()) {
            observationConstellationQueryObject.addCriterion(getCriterionForOffering(observationConstellationAliases,
                    obsConstOffObsTypeAlias, request.getOfferings()));
        }
        // observableProperties
        if (request.isSetObservableProperty()) {
            observationConstellationQueryObject.addCriterion(getCriterionForObservableProperties(
                    observationConstellationAliases, null, request.getObservedProperties()));
            observationQueryObject.addCriterion(getCriterionForObservableProperties(observationAliases, obsConstAlias,
                    request.getObservedProperties()));
        }
        // procedures
        if (request.isSetProcedure()) {
            observationConstellationQueryObject.addCriterion(getCriterionForProcedures(
                    observationConstellationAliases, null, request.getProcedures()));
            observationQueryObject.addCriterion(getCriterionForProcedures(observationAliases, obsConstAlias,
                    request.getProcedures()));
        }
        observationConstellationQueryObject.addCriterion(Restrictions.isNotNull(HibernateCriteriaQueryUtilities
                .getParameterWithPrefix(HibernateConstants.PARAMETER_OBSERVATION_TYPE, obsConstOffObsTypeAlias)));
        observationQueryObject.addCriterion(Restrictions.isNotNull(HibernateCriteriaQueryUtilities
                .getParameterWithPrefix(HibernateConstants.PARAMETER_OBSERVATION_TYPE, obsConstOffObsTypeAlias)));

        observationConstellationQueryObject.setAliases(observationConstellationAliases);
        List<ObservationConstellation> observationConstallations =
                HibernateCriteriaQueryUtilities.getObservationConstellations(observationConstellationQueryObject,
                        session);

        // feature identifier
        Set<String> featureIdentifier =
                QueryHelper.getFeatureIdentifier(request.getSpatialFilter(), request.getFeatureIdentifiers(), session);
        if (featureIdentifier != null && featureIdentifier.isEmpty()) {
            return null;
        } else if (featureIdentifier != null && !featureIdentifier.isEmpty()) {
            String foiAlias = HibernateCriteriaQueryUtilities.addFeatureOfInterestAliasToMap(observationAliases, null);
            observationQueryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(foiAlias), new ArrayList<String>(
                            featureIdentifier)));
        }

        observationQueryObject.setAliases(observationAliases);
        List<SosObservation> sosObservations = new LinkedList<SosObservation>();

        // temporal filters
        List<FirstLatest> firstLatestTemporalFilter = null;
        Criterion criterionForTemporalFilters = null;
        if (request.hasTemporalFilters()) {
            firstLatestTemporalFilter = SosHelper.getFirstLatestTemporalFilter(request.getTemporalFilters());
            List<TemporalFilter> nonFirstLatestTemporalFilter =
                    SosHelper.getNonFirstLatestTemporalFilter(request.getTemporalFilters());
            if (nonFirstLatestTemporalFilter != null && !nonFirstLatestTemporalFilter.isEmpty()) {
                criterionForTemporalFilters =
                        HibernateCriteriaQueryUtilities.getCriterionForTemporalFilters(nonFirstLatestTemporalFilter);
            }
        }
        // query observations
        // TODO Threadable !?!
        // TODO How to ensure no duplicated observations ?!
        // TODO How to ensure that anti subsetting observation are also
        // included ?!
        Set<Observation> allObservations = new HashSet<Observation>(0);
        for (ObservationConstellation observationConstellation : observationConstallations) {
            Set<Observation> observations = new HashSet<Observation>(0);
            HibernateQueryObject defaultQueryObject = observationQueryObject.clone();

            String id = HibernateCriteriaQueryUtilities.getParameterWithPrefix(HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION, null);
            defaultQueryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(id,
                    observationConstellation));

            if (request.hasTemporalFilters()) {
                if (firstLatestTemporalFilter != null && !firstLatestTemporalFilter.isEmpty()) {
                    for (FirstLatest firstLatest : firstLatestTemporalFilter) {
                        HibernateQueryObject firstLatestQueryObject = defaultQueryObject.clone();
                        firstLatestQueryObject.addOrder(HibernateCriteriaQueryUtilities.getOrderForEnum(firstLatest));
                        firstLatestQueryObject.setMaxResult(1);
                        observations.addAll(HibernateCriteriaQueryUtilities.getObservations(firstLatestQueryObject,
                                session));
                    }
                } else if (criterionForTemporalFilters != null) {
                    defaultQueryObject.addCriterion(criterionForTemporalFilters);
                    observations.addAll(HibernateCriteriaQueryUtilities.getObservations(defaultQueryObject, session));
                }
            } else {
                observations.addAll(HibernateCriteriaQueryUtilities.getObservations(defaultQueryObject, session));
            }
            // create SosObservations
            if (!observations.isEmpty()) {
                allObservations.addAll(observations);
            } else {
                    List<String> featureOfInterestIdentifiers =
                            getAndCheckFeatureOfInterest(observationConstellation, featureIdentifier, session);
                    sosObservations.addAll(HibernateObservationUtilities
                            .createSosObservationFromObservationConstellation(observationConstellation,
                                    featureOfInterestIdentifiers, request.getVersion(), session));
            }
        }
        LOGGER.debug("Time to query observations needs {} ms!", (System.currentTimeMillis()-start));
        if (!allObservations.isEmpty()) {
            long startProcess = System.currentTimeMillis();
            sosObservations.addAll(HibernateObservationUtilities.createSosObservationsFromObservations(
                    allObservations, request.getVersion(), session));
            LOGGER.debug("Time to process observations needs {} ms!", (System.currentTimeMillis()-startProcess));
        }
        LOGGER.debug("Time to query and process observations needs {} ms!", (System.currentTimeMillis()-start));
        return sosObservations;
    }

    private List<String> getAndCheckFeatureOfInterest(ObservationConstellation observationConstellation,
            Set<String> featureIdentifier, Session session) {
        List<String> featureOfInterestIdentifiersForObservationConstellation =
                HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForObservationConstellation(
                        observationConstellation, session);
        if (featureIdentifier == null) {
            return featureOfInterestIdentifiersForObservationConstellation;
        } else {
            return CollectionHelper.conjunctCollections(featureOfInterestIdentifiersForObservationConstellation,
                    featureIdentifier);
        }
    }

    private Criterion getCriterionForOffering(Map<String, String> aliasMap, String prefix, List<String> offerings) {
        String offAlias = HibernateCriteriaQueryUtilities.addOfferingAliasToMap(aliasMap, prefix);
        return HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(offAlias), offerings);
    }

    private Criterion getCriterionForObservableProperties(Map<String, String> aliasMap, String prefix,
            List<String> observedProperties) {
        String obsPropAlias = HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliasMap, prefix);
        return HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(obsPropAlias), observedProperties);
    }

    private Criterion getCriterionForProcedures(Map<String, String> aliasMap, String prefix, List<String> procedures) {
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliasMap, prefix);
        return HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), procedures);
    }
}
