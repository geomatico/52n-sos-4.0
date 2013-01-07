/**
 * Copyright (C) 2012
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IGetFeatureOfInterestDAO;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class GetFeatureOfInterestDAO extends AbstractHibernateOperationDao implements IGetFeatureOfInterestDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetFeatureOfInterestDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetFeatureOfInterest.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }
    
     @Override
    protected DecoderKeyType getKeyTypeForDcp(String version) {
        return new DecoderKeyType(version.equals(Sos1Constants.SERVICEVERSION) ? Sos1Constants.NS_SOS : Sos2Constants.NS_SOS_20);
    }

    @Override
    public void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session connection)
            throws OwsExceptionReport {
        
        Collection<String> featureIDs = SosHelper.getFeatureIDs(getCache().getFeatureOfInterest(), version);
        
        if (getConfigurator().isShowFullOperationsMetadata4Observations()) {
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.procedure, getCache().getProcedures());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.observedProperty, getCache().getObservableProperties());
            opsMeta.addPossibleValuesParameter(SosConstants.GetObservationParams.featureOfInterest, featureIDs);
        } else {
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.procedure);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.observedProperty);
            opsMeta.addAnyParameterValue(SosConstants.GetObservationParams.featureOfInterest);
        }
        
        
        String parameterName = Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name();
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            parameterName = Sos1Constants.GetFeatureOfInterestParams.location.name();
        }

        Envelope envelope = null;
        if (featureIDs != null && !featureIDs.isEmpty()) {
            envelope = getCache().getEnvelopeForFeatures();
        }
        
        if (envelope != null) {
            opsMeta.addRangeParameterValue(parameterName, SosHelper.getMinMaxMapFromEnvelope(envelope));
        } else {
            opsMeta.addAnyParameterValue(parameterName);
        }
    }

    @Override
    public GetFeatureOfInterestResponse getFeatureOfInterest(GetFeatureOfInterestRequest request)
            throws OwsExceptionReport {
        if (request instanceof GetFeatureOfInterestRequest) {
            Session session = null;
            try {
                session = getSession();
                if (request.getVersion().equals(Sos1Constants.SERVICEVERSION)
                        && ((request.getFeatureIdentifiers() != null && !request.getFeatureIdentifiers()
                                .isEmpty()) || (request.getSpatialFilters() != null && !request
                                .getSpatialFilters().isEmpty()))) {
                    OwsExceptionReport owse =
                            Util4Exceptions
                                    .createMissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID
                                            .name());
                    owse.addOwsExceptionReport(Util4Exceptions
                            .createMissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.location
                                    .name()));
                    throw owse;
                } else {
                    Set<String> foiIDs = new HashSet<String>(queryFeatureIdentifiersForParameter(request, session));
                    // feature of interest
                    SosFeatureCollection featureCollection =
                            new SosFeatureCollection(getConfigurator().getFeatureQueryHandler()
                                    .getFeatures(new ArrayList<String>(foiIDs), request.getSpatialFilters(),
                                            session, request.getVersion()));
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
                returnSession(session);
            }
        } else {
            String exceptionText = "The SOS request is not a SosGetObservationRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    private List<String> queryFeatureIdentifiersForParameter(GetFeatureOfInterestRequest sosRequest, Session session)
            throws OwsExceptionReport {
        // TODO get foi ids from foi table. Else only fois returned which relates to observations.
        Map<String, String> aliases = new HashMap<String, String>();
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        String obsAlias = HibernateCriteriaQueryUtilities.addObservationAliasToMap(aliases, null);
        String obsConstAlias =
                HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, obsAlias);
        // featureOfInterest identifiers
        if (sosRequest.getFeatureIdentifiers() != null && !sosRequest.getFeatureIdentifiers().isEmpty()) {
            String foiAlias = HibernateCriteriaQueryUtilities.addFeatureOfInterestAliasToMap(aliases, obsAlias);
            criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(foiAlias),
                    sosRequest.getFeatureIdentifiers()));
        }
        // observableProperties
        if (sosRequest.getObservedProperties() != null && !sosRequest.getObservedProperties().isEmpty()) {
            String obsPropAlias =
                    HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, obsConstAlias);
            criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(obsPropAlias),
                    sosRequest.getObservedProperties()));
        }
        // procedures
        if (sosRequest.getProcedures() != null && !sosRequest.getProcedures().isEmpty()) {
            String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, obsConstAlias);
            criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), sosRequest.getProcedures()));
        }
        // temporal filters
        if (sosRequest.getEventTimes() != null && !sosRequest.getEventTimes().isEmpty()) {
            criterions.add(HibernateCriteriaQueryUtilities.getCriterionForTemporalFilters(sosRequest.getEventTimes()));
        }
        return HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifier(aliases, criterions, projections,
                session);
    }
}
