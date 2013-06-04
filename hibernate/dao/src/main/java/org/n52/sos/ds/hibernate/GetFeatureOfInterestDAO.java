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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.AbstractGetFeatureOfInterestDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.util.TemporalRestrictions;
import org.n52.sos.exception.ows.MissingParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.om.features.FeatureCollection;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;

public class GetFeatureOfInterestDAO extends AbstractGetFeatureOfInterestDAO {

    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    
    public GetFeatureOfInterestDAO() {
        super(SosConstants.SOS);
    }

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
                    throw new NoApplicableCodeException()
                            .withMessage("Only one out of featureofinterestid or location possible");
                } else if ((request.getFeatureIdentifiers() != null && !request.getFeatureIdentifiers().isEmpty())
                        || (request.getSpatialFilters() != null && !request.getSpatialFilters().isEmpty())) {
                    // good
                    Set<String> foiIDs = new HashSet<String>(queryFeatureIdentifiersForParameter(request, session));
                    // feature of interest
                    FeatureCollection featureCollection =
                            new FeatureCollection(getConfigurator().getFeatureQueryHandler().getFeatures(
                                    new ArrayList<String>(foiIDs), request.getSpatialFilters(), session,
                                    request.getVersion(), -1));
                    GetFeatureOfInterestResponse response = new GetFeatureOfInterestResponse();
                    response.setService(request.getService());
                    response.setVersion(request.getVersion());
                    response.setAbstractFeature(featureCollection);
                    return response;
                } else {
                    throw new CompositeOwsException(
                            new MissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID),
                            new MissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.location));
                }
            } else {
                Set<String> foiIDs = new HashSet<String>(queryFeatureIdentifiersForParameter(request, session));
                // feature of interest
                FeatureCollection featureCollection =
                        new FeatureCollection(getConfigurator().getFeatureQueryHandler().getFeatures(
                                new ArrayList<String>(foiIDs), request.getSpatialFilters(), session,
                                request.getVersion(), -1));
                GetFeatureOfInterestResponse response = new GetFeatureOfInterestResponse();
                response.setService(request.getService());
                response.setVersion(request.getVersion());
                response.setAbstractFeature(featureCollection);
                return response;
            }
        } catch (HibernateException he) {
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while querying feature of interest data!");
        } finally {
            sessionHolder.returnSession(session);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> queryFeatureIdentifiersForParameter(GetFeatureOfInterestRequest req, Session session)
            throws OwsExceptionReport {
        // TODO get foi ids from foi table. Else only fois returned which
        Criteria c = session.createCriteria(Observation.class);
        Criteria fc = c.createCriteria(Observation.FEATURE_OF_INTEREST);

        fc.setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)));

        // relates to observations.
        if (req.isSetFeatureOfInterestIdentifiers()) {
            fc.add(Restrictions.in(FeatureOfInterest.IDENTIFIER,
                                   checkFeatureIdentifiersForRelatedFeatures(req.getFeatureIdentifiers())));
        }
        // observableProperties
        if (req.isSetObservableProperties()) {
            c.createCriteria(Observation.OBSERVABLE_PROPERTY)
                    .add(Restrictions.in(ObservableProperty.IDENTIFIER, req.getObservedProperties()));
        }
        // procedures
        if (req.isSetProcedures()) {
            c.createCriteria(Observation.PROCEDURE)
                    .add(Restrictions.in(Procedure.IDENTIFIER, req.getProcedures()));
        }
        // temporal filters
        if (req.isSetTemporalFilters()) {
            c.add(TemporalRestrictions.filter(req.getTemporalFilters()));
        }

        return c.list();
    }
}
