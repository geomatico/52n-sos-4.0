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

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.AbstractGetResultDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.QueryHelper;
import org.n52.sos.ds.hibernate.util.ResultHandlingHelper;
import org.n52.sos.ds.hibernate.util.TemporalRestrictions;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.response.GetResultResponse;

public class GetResultDAO extends AbstractGetResultDAO {

    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();

    @Override
    public GetResultResponse getResult(GetResultRequest request) throws OwsExceptionReport {
        Session session = null;
        try {
            session = sessionHolder.getSession();
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
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while querying result data!");
        } finally {
            sessionHolder.returnSession(session);
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

     *
     * @throws OwsExceptionReport If an error occurs.
     */
    @SuppressWarnings("unchecked")
    protected List<Observation> queryObservation(GetResultRequest request, Set<String> featureIdentifier,
                                                 Session session) throws OwsExceptionReport {
        if (featureIdentifier != null && featureIdentifier.isEmpty()) {
            return null;
        }

        Criteria c = session.createCriteria(Observation.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq(Observation.DELETED, false));

        if (featureIdentifier != null) {
            c.createCriteria(Observation.FEATURE_OF_INTEREST)
                    .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureIdentifier));
        }
        if (request.getObservedProperty() != null) {
            c.createCriteria(Observation.OBSERVABLE_PROPERTY)
                    .add(Restrictions.eq(ObservableProperty.IDENTIFIER, request.getObservedProperty()));
        }
        if (request.getOffering() != null) {
            c.createCriteria(Observation.OFFERINGS)
                    .add(Restrictions.eq(Offering.IDENTIFIER, request.getOffering()));

        }
        if (request.getTemporalFilter() != null && !request.getTemporalFilter().isEmpty()) {
            c.add(TemporalRestrictions.filter(request.getTemporalFilter()));
        }
        return c.list();

    }
}
