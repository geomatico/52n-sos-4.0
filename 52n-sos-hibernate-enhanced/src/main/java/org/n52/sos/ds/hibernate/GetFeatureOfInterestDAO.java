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

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetFeatureOfInterestDAO;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OWSParameterValueRange;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class GetFeatureOfInterestDAO implements IGetFeatureOfInterestDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetFeatureOfInterestDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetFeatureOfInterest.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public GetFeatureOfInterestDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        Session session = null;
        if (connection instanceof Session) {
            session = (Session) connection;
        } else {
            String exceptionText = "The parameter connection is not an Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        // get DCP
        DecoderKeyType dkt = null;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        }
        Map<String, List<String>> dcpMap =
                SosHelper.getDCP(OPERATION_NAME, dkt, Configurator.getInstance().getBindingOperators().values(),
                        Configurator.getInstance().getServiceURL());
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
            // set DCP
            opsMeta.setDcp(dcpMap);
            // set param procedure
            if (Configurator.getInstance().isShowFullOperationsMetadata4Observations()) {

                opsMeta.addParameterValue(SosConstants.GetObservationParams.procedure.name(),
                        new OWSParameterValuePossibleValues(Configurator.getInstance()
                                .getCapabilitiesCacheController().getProcedures()));
            } else {
                List<String> phenomenonValues = new ArrayList<String>(1);
                phenomenonValues.add(SosConstants.PARAMETER_ANY);
                opsMeta.addParameterValue(SosConstants.GetObservationParams.procedure.name(),
                        new OWSParameterValuePossibleValues(phenomenonValues));
            }
            // set param observedProperty
            if (Configurator.getInstance().isShowFullOperationsMetadata4Observations()) {
                opsMeta.addParameterValue(SosConstants.GetObservationParams.observedProperty.name(),
                        new OWSParameterValuePossibleValues(Configurator.getInstance()
                                .getCapabilitiesCacheController().getObservableProperties()));
            } else {
                List<String> phenomenonValues = new ArrayList<String>(1);
                phenomenonValues.add(SosConstants.PARAMETER_ANY);
                opsMeta.addParameterValue(SosConstants.GetObservationParams.observedProperty.name(),
                        new OWSParameterValuePossibleValues(phenomenonValues));
            }
            // set param foi
            Collection<String> featureIDs =
                    SosHelper.getFeatureIDs(Configurator.getInstance().getCapabilitiesCacheController()
                            .getFeatureOfInterest(), version);
            if (Configurator.getInstance().isShowFullOperationsMetadata4Observations()) {
                opsMeta.addParameterValue(SosConstants.GetObservationParams.featureOfInterest.name(),
                        new OWSParameterValuePossibleValues(featureIDs));
            } else {
                List<String> foiValues = new ArrayList<String>(1);
                foiValues.add(SosConstants.PARAMETER_ANY);
                opsMeta.addParameterValue(SosConstants.GetObservationParams.featureOfInterest.name(),
                        new OWSParameterValuePossibleValues(foiValues));
            }

            // set param spatial filter
            String parameterName = Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name();
            if (version.equals(Sos1Constants.SERVICEVERSION)) {
                parameterName = Sos1Constants.GetFeatureOfInterestParams.location.name();
            }
            Envelope envelope = null;
            if (featureIDs != null && !featureIDs.isEmpty()) {
                envelope = Configurator.getInstance().getCapabilitiesCacheController().getEnvelopeForFeatures();
            }
            if (envelope != null) {
                opsMeta.addParameterValue(parameterName,
                        new OWSParameterValueRange(SosHelper.getMinMaxMapFromEnvelope(envelope)));
            } else {
                List<String> locationValues = new ArrayList<String>(1);
                locationValues.add(SosConstants.PARAMETER_ANY);
                opsMeta.addParameterValue(parameterName, new OWSParameterValuePossibleValues(locationValues));
            }
            return opsMeta;
        }
        return null;
    }

    @Override
    public GetFeatureOfInterestResponse getFeatureOfInterest(GetFeatureOfInterestRequest request)
            throws OwsExceptionReport {
        if (request instanceof GetFeatureOfInterestRequest) {
            GetFeatureOfInterestRequest sosRequest = (GetFeatureOfInterestRequest) request;
            Session session = null;
            session = (Session) connectionProvider.getConnection();
            if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)
                    && ((sosRequest.getFeatureIdentifiers() != null && !sosRequest.getFeatureIdentifiers().isEmpty()) || (sosRequest
                            .getSpatialFilters() != null && !sosRequest.getSpatialFilters().isEmpty()))) {
                OwsExceptionReport owse =
                        Util4Exceptions
                                .createMissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID
                                        .name());
                owse.addOwsExceptionReport(Util4Exceptions
                        .createMissingParameterValueException(Sos1Constants.GetFeatureOfInterestParams.location.name()));
                throw owse;
            } else {
                Set<String> foiIDs = new HashSet<String>(queryFeatureIdentifiersForParameter(sosRequest, session));
                // feature of interest
                if (sosRequest.getFeatureIdentifiers() != null && !sosRequest.getFeatureIdentifiers().isEmpty()) {
                    foiIDs.addAll(sosRequest.getFeatureIdentifiers());
                }
                SosFeatureCollection featureCollection =
                        new SosFeatureCollection(Configurator
                                .getInstance()
                                .getFeatureQueryHandler()
                                .getFeatures(new ArrayList<String>(foiIDs), sosRequest.getSpatialFilters(), session,
                                        sosRequest.getVersion()));
                GetFeatureOfInterestResponse response = new GetFeatureOfInterestResponse();
                response.setService(request.getService());
                response.setVersion(request.getVersion());
                response.setAbstractFeature(featureCollection);
                return response;
            }
        } else {
            String exceptionText = "The SOS request is not a SosGetObservationRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    private List<String> queryFeatureIdentifiersForParameter(GetFeatureOfInterestRequest sosRequest, Session session)
            throws OwsExceptionReport {
        Map<String, String> aliases = new HashMap<String, String>();
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        String obsAlias = HibernateCriteriaQueryUtilities.addObservationAliasToMap(aliases, null);
        String obsConstAlias =
                HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, obsAlias);
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
        if (!criterions.isEmpty()) {
            return HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifier(aliases, criterions, projections,
                    session);
        }

        return new ArrayList<String>();
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

}
