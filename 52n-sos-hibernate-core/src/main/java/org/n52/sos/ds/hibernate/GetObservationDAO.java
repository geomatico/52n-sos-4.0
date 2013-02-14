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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateObservationUtilities;
import org.n52.sos.ds.hibernate.util.QueryHelper;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OWSConstants.MinMax;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.n52.sos.ogc.sos.SosConstants.GetObservationParams;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface IGetObservationDAO
 * 
 */
public class GetObservationDAO extends AbstractHibernateOperationDao implements IGetObservationDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetObservationDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservation.name();

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ds.ISosOperationDAO#getOperationName()
     */
    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.hibernate.AbstractHibernateOperationDao#getOperationsMetadata
     * (java.lang.String, org.hibernate.Session)
     */
    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session)
            throws OwsExceptionReport {

        Collection<String> featureIDs = SosHelper.getFeatureIDs(getCache().getFeatureOfInterest(), version);

        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.offering, getCache().getOfferings());
        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.procedure, getCache().getProcedures());
        opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.responseFormat,
                SosHelper.getSupportedResponseFormats(SosConstants.SOS, version));

        if (getConfigurator().getActiveProfile().isShowFullOperationsMetadataForObservations()) {
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.observedProperty, getCache()
                    .getObservableProperties());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.featureOfInterest, featureIDs);
        } else {
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.observedProperty);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.featureOfInterest);
        }

        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            // SOS 2.0 parameter
            opsMeta.addRangeParameterValue(Sos2Constants.GetObservationParams.temporalFilter, getEventTime(session));
            SosEnvelope envelope = null;
            if (featureIDs != null && !featureIDs.isEmpty()) {
                envelope = getCache().getGlobalEnvelope();
            }
            if (envelope != null) {
                opsMeta.addRangeParameterValue(Sos2Constants.GetObservationParams.spatialFilter,
                        SosHelper.getMinMaxMapFromEnvelope(envelope.getEnvelope()));
            }
        } else if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // SOS 1.0.0 parameter
            opsMeta.addRangeParameterValue(Sos1Constants.GetObservationParams.eventTime, getEventTime(session));
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.srsName);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.result);
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.resultModel, getResultModels());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.responseMode,
                    Arrays.asList(SosConstants.getResponseModes()));
        }
    }

    private List<String> getResultModels() {
        QName[] resultModels = OMConstants.getResultModels();
        List<String> resultModelsList = new ArrayList<String>(resultModels.length);
        for (QName qname : resultModels) {
            resultModelsList.add(qname.getPrefix() + ":" + qname.getLocalPart());
        }
        return resultModelsList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.IGetObservationDAO#getObservation(org.n52.sos.request.
     * AbstractSosRequest)
     */
    @Override
    public GetObservationResponse getObservation(GetObservationRequest sosRequest) throws OwsExceptionReport {
        Session session = null;
        try {
            session = getSession();
            if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)
                    && sosRequest.getObservedProperties().isEmpty()) {
                throw Util4Exceptions.createMissingParameterValueException(GetObservationParams.observedProperty
                        .name());
            } else {
                GetObservationResponse sosResponse = new GetObservationResponse();
                sosResponse.setService(sosRequest.getService());
                sosResponse.setVersion(sosRequest.getVersion());
                sosResponse.setResponseFormat(sosRequest.getResponseFormat());
                sosResponse.setObservationCollection(queryObservation(sosRequest, session));
                return sosResponse;
            }
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data observation data!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
    }

    /**
     * Query observations from database depending on requested filters
     * 
     * @param request
     *            GetObservation request
     * @param session
     *            Hibernate session
     * @return List of Observation objects
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    protected List<SosObservation> queryObservation(GetObservationRequest request, Session session)
            throws OwsExceptionReport {

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
        List<SosObservation> sosObservations = new ArrayList<SosObservation>();

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
                        firstLatestQueryObject.setOrder(HibernateCriteriaQueryUtilities.getOrderForEnum(firstLatest));
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
            if (observations != null && !observations.isEmpty()) {
                allObservations.addAll(observations);
            } else {
                // TODO Hydro-Profile add empty observation metadata as
                // SosObservation (add FOI)
                if (getConfigurator().getActiveProfile().isShowMetadataOfEmptyObservations()) {
                    List<String> featureOfInterestIdentifiers =
                            getAndCheckFeatureOfInterest(observationConstellation, featureIdentifier, session);
                    sosObservations.addAll(HibernateObservationUtilities
                            .createSosObservationFromObservationConstellation(observationConstellation,
                                    featureOfInterestIdentifiers, request.getVersion(), session));
                }
            }
        }

        if (allObservations != null && !allObservations.isEmpty()) {
            sosObservations.addAll(HibernateObservationUtilities.createSosObservationsFromObservations(
                    allObservations, request.getVersion(), session));
        }
        return sosObservations;
    }

    /**
     * Get the min/max time of contained observations
     * 
     * @param session
     *            Hibernate session
     * @return min/max observation time
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    private Map<MinMax, String> getEventTime(Session session) throws OwsExceptionReport {
        try {
            Map<MinMax, String> eventTime = new EnumMap<MinMax, String>(MinMax.class);
            DateTime minDate = getCache().getMinEventTime();
            DateTime maxDate = getCache().getMaxEventTime();
            if (minDate != null && maxDate != null) {
                eventTime.put(MinMax.MIN, DateTimeHelper.formatDateTime2ResponseString(minDate));
                eventTime.put(MinMax.MAX, DateTimeHelper.formatDateTime2ResponseString(maxDate));
            }
            return eventTime;
        } catch (DateTimeException dte) {
            String exceptionText = "Error while getting min/max time for OwsMetadata!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
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
