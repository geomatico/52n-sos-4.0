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
import org.n52.sos.ds.AbstractGetFeatureOfInterestDAO;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFeatureOfInterestDAO extends AbstractGetFeatureOfInterestDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetFeatureOfInterestDAO.class);
    
    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();

    @Override
    public GetFeatureOfInterestResponse getFeatureOfInterest(GetFeatureOfInterestRequest request)
            throws OwsExceptionReport {
        Session session = null;
        try {
            session = sessionHolder.getSession();
            if (request.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                // sos 1.0.0 either or
                if ((request.getFeatureIdentifiers() != null && !request.getFeatureIdentifiers().isEmpty())
                        && (request.getSpatialFilters() != null && !request.getSpatialFilters().isEmpty())) {
                    String exceptionText = "Only one out of featureofinterestid or location possible";
                    OwsExceptionReport owse = new OwsExceptionReport();
                    Util4Exceptions.createNoApplicableCodeException(owse, exceptionText);
                    throw owse;
                } else if ((request.getFeatureIdentifiers() != null && !request.getFeatureIdentifiers().isEmpty())
                        || (request.getSpatialFilters() != null && !request.getSpatialFilters().isEmpty())) {
                    // good
                    Set<String> foiIDs = new HashSet<String>(queryFeatureIdentifiersForParameter(request, session));
                    // feature of interest
                    SosFeatureCollection featureCollection =
                            new SosFeatureCollection(getConfigurator().getFeatureQueryHandler().getFeatures(
                                    new ArrayList<String>(foiIDs), request.getSpatialFilters(), session,
                                    request.getVersion(), -1));
                    GetFeatureOfInterestResponse response = new GetFeatureOfInterestResponse();
                    response.setService(request.getService());
                    response.setVersion(request.getVersion());
                    response.setAbstractFeature(featureCollection);
                    return response;
                } else {
                    OwsExceptionReport owse =
                            Util4Exceptions
                                    .createMissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID
                                            .name());
                    owse.addOwsExceptionReport(Util4Exceptions
                            .createMissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.location
                                    .name()));
                    throw owse;
                }
            } else {
                Set<String> foiIDs = new HashSet<String>(queryFeatureIdentifiersForParameter(request, session));
                // feature of interest
                SosFeatureCollection featureCollection =
                        new SosFeatureCollection(getConfigurator().getFeatureQueryHandler().getFeatures(
                                new ArrayList<String>(foiIDs), request.getSpatialFilters(), session,
                                request.getVersion(), -1));
                GetFeatureOfInterestResponse response = new GetFeatureOfInterestResponse();
                response.setService(request.getService());
                response.setVersion(request.getVersion());
                response.setAbstractFeature(featureCollection);
                return response;
            }
        } catch (HibernateException he) {
            String exceptionText = "Error while querying feature of interest data!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            sessionHolder.returnSession(session);
        }
    }

    private List<String> queryFeatureIdentifiersForParameter(GetFeatureOfInterestRequest sosRequest, Session session)
            throws OwsExceptionReport {
        // TODO get foi ids from foi table. Else only fois returned which
        // relates to observations.
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        // String obsAlias =
        // HibernateCriteriaQueryUtilities.addObservationAliasToMap(aliases,
        // null);
        String obsConstAlias = HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, null);
        // featureOfInterest identifiers
        if (sosRequest.isSetFeatureOfInterestIdentifiers()) {
            String foiAlias = HibernateCriteriaQueryUtilities.addFeatureOfInterestAliasToMap(aliases, null);
            queryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(foiAlias),
                    sosRequest.getFeatureIdentifiers()));
        }
        // observableProperties
        if (sosRequest.isSetObservableProperties()) {
            String obsPropAlias =
                    HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, obsConstAlias);
            queryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(obsPropAlias),
                    sosRequest.getObservedProperties()));
        }
        // procedures
        if (sosRequest.isSetProcedures()) {
            String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, obsConstAlias);
            queryObject.addCriterion(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), sosRequest.getProcedures()));
        }
        // temporal filters
        if (sosRequest.isSetTemporalFilters()) {
            queryObject.addCriterion(HibernateCriteriaQueryUtilities.getCriterionForTemporalFilters(sosRequest
                    .getTemporalFilters()));
        }
        queryObject.setAliases(aliases);
        return HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifier(queryObject, session);
    }
}
