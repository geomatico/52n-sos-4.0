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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.IGetResultDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.QueryHelper;
import org.n52.sos.ds.hibernate.util.ResultHandlingHelper;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.response.GetResultResponse;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResultDAO extends AbstractHibernateOperationDao implements IGetResultDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetResultDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetResult.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }
    
    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session)
            throws OwsExceptionReport {
        List<ResultTemplate> resultTemplates = HibernateCriteriaQueryUtilities.getResultTemplateObjects(session);
        Set<String> offerings = null;
        Set<String> observableProperties = null;
        Set<String> featureOfInterest = null;
        Set<String> templateIdentifiers;
        if (resultTemplates != null && !resultTemplates.isEmpty()) {
            offerings = new HashSet<String>(0);
            observableProperties = new HashSet<String>(0);
            featureOfInterest = new HashSet<String>(0);
            templateIdentifiers = new HashSet<String>(0);
            for (ResultTemplate resultTemplate : resultTemplates) {
                templateIdentifiers.add(resultTemplate.getIdentifier());
                ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType =
                        resultTemplate.getObservationConstellationOfferingObservationType();
                ObservationConstellation observationConstellation = observationConstellationOfferingObservationType.getObservationConstellation();
                offerings.add(observationConstellationOfferingObservationType.getOffering().getIdentifier());
                observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
            }
        }
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // TODO set parameter for SOS 1.0
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultParams.offering, offerings);
            opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultParams.observedProperty, observableProperties);
            opsMeta.addPossibleValuesParameter(Sos2Constants.GetResultParams.featureOfInterest, featureOfInterest);
            // TODO get the values for temporal and spatial filtering
            // set param temporalFilter
            // opsMeta.addParameterValue(Sos2Constants.GetResultParams.temporalFilter.name(),
            // new OWSParameterValuePossibleValues(null));
            // // set param spatialFilter
            // opsMeta.addParameterValue(Sos2Constants.GetResultParams.spatialFilter.name(),
            // new OWSParameterValuePossibleValues(null));
        }
    }

    @Override
    public GetResultResponse getResult(GetResultRequest request) throws OwsExceptionReport {
        Session session = null;
        try {
            session = getSession();
            GetResultResponse response = new GetResultResponse();
            response.setService(request.getService());
            response.setVersion(request.getVersion());
            Set<String> featureIdentifier =
                    QueryHelper.getFeatureIdentifier(request.getSpatialFilter(), request.getFeatureIdentifiers(),
                            session);
            List<ResultTemplate> resultTemplates = queryResultTemplate(request, featureIdentifier, session);
            if (resultTemplates != null && !resultTemplates.isEmpty()) {
                SosResultEncoding sosResultEncoding =
                        ResultHandlingHelper.createSosResultEncoding(resultTemplates.get(0).getResultEncoding());
                SosResultStructure sosResultStructure =
                        ResultHandlingHelper.createSosResultStructure(resultTemplates.get(0).getResultStructure());
                List<Observation> observations = queryObservation(request, featureIdentifier, session);
                response.setResultValues(ResultHandlingHelper.createResultValuesFromObservations(observations,
                        sosResultEncoding, sosResultStructure));
            }
            return response;
        } catch (HibernateException he) {
            String exceptionText = "Error while querying result data!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
    }

    private List<ResultTemplate> queryResultTemplate(GetResultRequest request, Set<String> featureIdentifier,
            Session session) {
        List<ResultTemplate> resultTemplates =
                HibernateCriteriaQueryUtilities.getResultTemplateObject(request.getOffering(),
                        request.getObservedProperty(), featureIdentifier, session);
        return resultTemplates;
    }

    /**
     * Query observations from database depending on requested filters
     * 
     * @param request
     *            GetObservation request
     * @param featureIdentifier
     * @param session
     *            Hibernate session
     * @return List of Observation objects
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    protected List<Observation> queryObservation(GetResultRequest request, Set<String> featureIdentifier,
            Session session) throws OwsExceptionReport {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = HibernateCriteriaQueryUtilities.addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String obsConstAlias = HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, null);
        // offering
        String offAlias = HibernateCriteriaQueryUtilities.addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(offAlias), request.getOffering()));
        // observableProperties
        String obsPropAlias = HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, obsConstAlias);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(obsPropAlias), request.getObservedProperty()));
        // deleted
        // XXX DeleteObservation Extension
        queryObject.addCriterion(Restrictions.eq(HibernateConstants.DELETED, false));
        // feature identifier
        if (featureIdentifier != null && featureIdentifier.isEmpty()) {
            return null;
        } else if (featureIdentifier != null && !featureIdentifier.isEmpty()) {
            String foiAlias = HibernateCriteriaQueryUtilities.addFeatureOfInterestAliasToMap(aliases, null);
            queryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(foiAlias), new ArrayList<String>(
                            featureIdentifier)));
        }
        // temporal filters
        if (request.hasTemporalFilter()) {
            queryObject.addCriterion(HibernateCriteriaQueryUtilities.getCriterionForTemporalFilters(request.getTemporalFilter()));
        }
        queryObject.setAliases(aliases);
        // ...
        List<Observation> observations =
                HibernateCriteriaQueryUtilities.getObservations(queryObject, session);
        return observations;

    }
}
