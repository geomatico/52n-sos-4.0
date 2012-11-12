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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateResultUtilities;
import org.n52.sos.ds.hibernate.util.QueryHelper;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSConstants.MinMax;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OWSParameterValueRange;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.GetObservationParams;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import org.n52.sos.ds.hibernate.util.HibernateConstants;

/**
 * Implementation of the interface IGetObservationDAO
 * 
 */
public class GetObservationDAO implements IGetObservationDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetObservationDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservation.name();

    /**
     * actual time
     */
    private DateTime now = null;

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public GetObservationDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();

    }

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
     * org.n52.sos.ds.ISosOperationDAO#getOperationsMetadata(java.lang.String,
     * java.lang.Object)
     */
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
            // set parameter for both versions
            // set param offering
            opsMeta.addParameterValue(SosConstants.GetObservationParams.offering.name(),
                    new OWSParameterValuePossibleValues(Configurator.getInstance().getCapabilitiesCacheController()
                            .getOfferings()));
            // set param procedure
            opsMeta.addParameterValue(SosConstants.GetObservationParams.procedure.name(),
                    new OWSParameterValuePossibleValues(Configurator.getInstance().getCapabilitiesCacheController()
                            .getProcedures()));
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
            // responseFormat
            opsMeta.addParameterValue(SosConstants.GetObservationParams.responseFormat.name(),
                    new OWSParameterValuePossibleValues(SosHelper.getSupportedResponseFormats("SOS", version)));

            // SOS 2.0 parameter
            if (version.equals(Sos2Constants.SERVICEVERSION)) {
                // set param temporal filter
                opsMeta.addParameterValue(Sos2Constants.GetObservationParams.temporalFilter.name(),
                        new OWSParameterValueRange(getEventTime(session)));
                // set param spatial filter
                Envelope envelope = null;
                if (featureIDs != null && !featureIDs.isEmpty()) {
                    envelope = Configurator.getInstance().getCapabilitiesCacheController().getEnvelopeForFeatures();
                }
                if (envelope != null) {
                    opsMeta.addParameterValue(Sos2Constants.GetObservationParams.spatialFilter.name(),
                            new OWSParameterValueRange(SosHelper.getMinMaxMapFromEnvelope(envelope)));
                }
            }
            // SOS 1.0.0 parameter
            else if (version.equals(Sos1Constants.SERVICEVERSION)) {
                // set param srsName
                List<String> srsNameValues = new ArrayList<String>(1);
                srsNameValues.add(SosConstants.PARAMETER_ANY);
                opsMeta.addParameterValue(SosConstants.GetObservationParams.srsName.name(),
                        new OWSParameterValuePossibleValues(srsNameValues));
                // set param eventTime
                opsMeta.addParameterValue(Sos1Constants.GetObservationParams.eventTime.name(),
                        new OWSParameterValueRange(getEventTime(session)));
                // set param result
                List<String> resultValues = new ArrayList<String>(1);
                resultValues.add(SosConstants.PARAMETER_ANY);
                opsMeta.addParameterValue(SosConstants.GetObservationParams.result.name(),
                        new OWSParameterValuePossibleValues(resultValues));
                // set param resultModel
                List<String> resultModelsList = new ArrayList<String>();
                for (QName qname : Arrays.asList(OMConstants.getResultModels())) {
                    resultModelsList.add(qname.getPrefix() + ":" + qname.getLocalPart());
                }
                opsMeta.addParameterValue(SosConstants.GetObservationParams.resultModel.name(),
                        new OWSParameterValuePossibleValues(resultModelsList));
                // set param reponseMode
                opsMeta.addParameterValue(SosConstants.GetObservationParams.responseMode.name(),
                        new OWSParameterValuePossibleValues(Arrays.asList(SosConstants.getResponseModes())));
            }
            return opsMeta;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.IGetObservationDAO#getObservation(org.n52.sos.request.
     * AbstractSosRequest)
     */
    @Override
    public GetObservationResponse getObservation(GetObservationRequest request) throws OwsExceptionReport {
        GetObservationRequest sosRequest = (GetObservationRequest) request;
        // setting a global "now" for this request
        now = new DateTime();
        Session session = null;
        try {
            session = (Session) connectionProvider.getConnection();
            if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)
                    && sosRequest.getObservedProperties().isEmpty()) {
                throw Util4Exceptions.createMissingParameterValueException(GetObservationParams.observedProperty
                        .name());
            } else {
                boolean hasSpatialPhen = false;
                if (sosRequest.getObservedProperties().contains(
                        Configurator.getInstance().getSpatialObsProp4DynymicLocation())) {
                    hasSpatialPhen = true;
                }
                List<Observation> observations = queryObservation(sosRequest, session);

                GetObservationResponse response = new GetObservationResponse();
                response.setService(request.getService());
                response.setVersion(request.getVersion());
                response.setResponseFormat(request.getResponseFormat());
                response.setObservationCollection(HibernateResultUtilities.createSosObservationFromObservations(
                        observations, sosRequest.getVersion(), session));
                return response;
            }
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data observation data!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
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
    protected List<Observation> queryObservation(GetObservationRequest request, Session session)
            throws OwsExceptionReport {
        Map<String, String> aliases = new HashMap<String, String>();
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        if ((request.getOfferings() != null && !request.getOfferings().isEmpty())
                || (request.getObservedProperties() != null && !request.getObservedProperties().isEmpty())
                || (request.getProcedures() != null && !request.getProcedures().isEmpty())) {
            String obsConstAlias =
                    HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, null);
            // offering
            if (request.getOfferings() != null && !request.getOfferings().isEmpty()) {
                String offAlias = HibernateCriteriaQueryUtilities.addOfferingAliasToMap(aliases, obsConstAlias);
                criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                        HibernateCriteriaQueryUtilities.getIdentifierParameter(offAlias), request.getOfferings()));
            }
            // observableProperties
            if (request.getObservedProperties() != null && !request.getObservedProperties().isEmpty()) {
                String obsPropAlias =
                        HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, obsConstAlias);
                criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                        HibernateCriteriaQueryUtilities.getIdentifierParameter(obsPropAlias),
                        request.getObservedProperties()));
            }
            // procedures
            if (request.getProcedures() != null && !request.getProcedures().isEmpty()) {
                String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, obsConstAlias);
                criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                        HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), request.getProcedures()));
            }
            // deleted
            // XXX DeleteObservation Extension
            criterions.add(Restrictions.eq(HibernateConstants.DELETED, false));
        }
        // temporal filters
        if (request.getEventTimes() != null && !request.getEventTimes().isEmpty()) {
            criterions.add(HibernateCriteriaQueryUtilities.getCriterionForTemporalFilters(request.getEventTimes()));
        }
        Set<String> featureIdentifier = QueryHelper.getFeatureIdentifier(request.getSpatialFilter(), request.getFeatureIdentifiers(), session);
        if (featureIdentifier != null && featureIdentifier.isEmpty()) {
            return null;
        } else if (featureIdentifier != null && !featureIdentifier.isEmpty()) {
            String foiAlias = HibernateCriteriaQueryUtilities.addFeatureOfInterestAliasToMap(aliases, null);
            criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                    HibernateCriteriaQueryUtilities.getIdentifierParameter(foiAlias), new ArrayList<String>(
                            featureIdentifier)));
        }
        // ...
        List<Observation> observations =
                HibernateCriteriaQueryUtilities.getObservations(aliases, criterions, projections, session);
        return observations;

    }

    // /**
    // * Create SOS internal observation from Observation objects
    // *
    // * @param responseFormat
    // *
    // * @param observations
    // * List of Observation objects
    // * @param version
    // * SOS version
    // * @param session
    // * Hibernate session
    // * @return SOS internal observation
    // * @throws OwsExceptionReport
    // * If an error occurs
    // */
    // private SosObservationCollection
    // createSosObservationCollectionFromObservations(String responseFormat,
    // List<Observation> observations, String version, Session session) throws
    // OwsExceptionReport {
    // SosObservationCollection sosObservationCollection = new
    // SosObservationCollection(responseFormat);
    //
    // Map<SosObservationConstellation, SosObservation> obsConstObsMap =
    // new HashMap<SosObservationConstellation, SosObservation>();
    // List<String> features = new ArrayList<String>();
    // Map<String, Set<String>> feature4proc = new HashMap<String,
    // Set<String>>();
    // Map<String, DateTime> featureTimeForDynamicPosition = new HashMap<String,
    // DateTime>();
    //
    // // QName observationType = null;
    // // if (request.getResultModel() != null) {
    // // observationType = request.getResultModel();
    // // } else {
    // // if (request.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
    // // observationType = SosConstants.RESULT_MODEL_OBSERVATION;
    // // } else if (request.getVersion().equals(Sos2Constants.SERVICEVERSION))
    // // {
    // // observationType =
    // // new QName(OMConstants.NS_OM_2,
    // // OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION,
    // // OMConstants.NS_OM_PREFIX);
    // // }
    // //
    // // }
    // String observationType = OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION;
    // Envelope boundedBy = null;
    // int srid = 0;
    // if (observations != null) {
    // // now iterate over resultset and create Measurement for each
    // // row
    // for (Observation hObservation : observations) {
    // ObservationConstellation hObservationConstellation =
    // hObservation.getObservationConstellation();
    // FeatureOfInterest hFeatureOfInterest =
    // hObservation.getFeatureOfInterest();
    //
    // // check remaining heap size
    // SosHelper.checkFreeMemory();
    //
    // long obsID = hObservation.getObservationId();
    //
    // String phenID =
    // hObservationConstellation.getObservableProperty().getIdentifier();
    // String procID = hObservationConstellation.getProcedure().getIdentifier();
    // observationType =
    // hObservationConstellation.getObservationType().getObservationType();
    //
    // DateTime timeDateTime = new
    // DateTime(hObservation.getPhenomenonTimeStart());
    //
    // // feature of interest
    // String foiID = hFeatureOfInterest.getIdentifier();
    // if (!features.contains(foiID)) {
    // features.add(foiID);
    // }
    // if (!version.equals(Sos2Constants.SERVICEVERSION)
    // && Configurator.getInstance().isSetFoiLocationDynamically()
    // &&
    // phenID.equals(Configurator.getInstance().getSpatialObsProp4DynymicLocation()))
    // {
    // featureTimeForDynamicPosition.put(foiID, timeDateTime);
    // }
    // feature4proc = setFeatureForProcedure(feature4proc, procID, foiID);
    // String offeringID =
    // hObservationConstellation.getOffering().getIdentifier();
    // String mimeType = SosConstants.PARAMETER_NOT_SET;
    // String valueType =
    // hObservationConstellation.getObservableProperty().getValueType().getValueType();
    //
    // // create time element
    // ISosTime phenomenonTime = null;
    // if (hObservation.getPhenomenonTimeEnd() == null) {
    // phenomenonTime = new TimeInstant(timeDateTime, "");
    // } else {
    // phenomenonTime = new TimePeriod(timeDateTime, new
    // DateTime(hObservation.getPhenomenonTimeEnd()));
    // }
    //
    // String unit =
    // hObservationConstellation.getObservableProperty().getUnit().getUnit();
    //
    // // create quality
    // ArrayList<SosQuality> qualityList = null;
    // if (supportsQuality) {
    // hObservation.getQualities();
    // for (Quality hQuality : (Set<Quality>) hObservation.getQualities()) {
    // String qualityTypeString = hQuality.getSweType().getSweType();
    // String qualityUnit = hQuality.getUnit().getUnit();
    // String qualityName = hQuality.getName();
    // String qualityValue = hQuality.getValue();
    // qualityList = new ArrayList<SosQuality>();
    // if (qualityValue != null) {
    // QualityType qualityType = QualityType.valueOf(qualityTypeString);
    // SosQuality quality = new SosQuality(qualityName, qualityUnit,
    // qualityValue, qualityType);
    // qualityList.add(quality);
    // }
    // }
    // }
    //
    // // if (request.getResponseMode() != null
    // // && !request.getResponseMode().equals(
    // // SosConstants.PARAMETER_NOT_SET)) {
    // // // if responseMode is resultTemplate, then create
    // // // observation template and return it
    // // if (request.getResponseMode() ==
    // // SosConstants.RESPONSE_RESULT_TEMPLATE) {
    // // return getResultTemplate(resultSet, request, features);
    // // } else {
    // // checkResponseModeInline(request.getResponseMode());
    // // }
    // // }
    // Object value;
    // if
    // (valueType.equalsIgnoreCase(SosConstants.ValueTypes.booleanType.name()))
    // {
    // value =
    // Boolean.parseBoolean(getValueFromTextValueTable(hObservation.getTextValues()));
    // } else if
    // (valueType.equalsIgnoreCase(SosConstants.ValueTypes.countType.name())) {
    // value = (Integer)
    // getValueFromNumericValueTable(hObservation.getNumericValues()).intValue();
    // } else if
    // (valueType.equalsIgnoreCase(SosConstants.ValueTypes.numericType.name()))
    // {
    // value = getValueFromNumericValueTable(hObservation.getNumericValues());
    // // } else if (valueType
    // // .equalsIgnoreCase(SosConstants.ValueTypes.isoTimeType
    // // .name())) {
    // // value = new DateTime(resultSet.getLong(PGDAOConstants
    // // .getNumericValueCn()));
    // } else if
    // (valueType.equalsIgnoreCase(SosConstants.ValueTypes.textType.name())) {
    // value = getValueFromTextValueTable(hObservation.getTextValues());
    // } else if
    // (valueType.equalsIgnoreCase(SosConstants.ValueTypes.categoryType.name()))
    // {
    // value = getValueFromTextValueTable(hObservation.getTextValues());
    // } else if
    // (valueType.equalsIgnoreCase(SosConstants.ValueTypes.spatialType.name()))
    // {
    // value = getValueFromGeometryValueTable(hObservation.getGeometryValues());
    // } else {
    // value = getValueFromAllTable(hObservation);
    // }
    // SosObservationValue sosObsValue =
    // new SosObservationValue(Long.toString(obsID), phenomenonTime, null,
    // value);
    // if (qualityList != null) {
    // sosObsValue.setQuality(qualityList);
    // }
    // SosObservableProperty phen = new SosObservableProperty(phenID, null,
    // unit, null, valueType);
    // SosObservationConstellation obsConst =
    // new SosObservationConstellation(procID, phen, null, foiID,
    // observationType);
    // if (obsConstObsMap.containsKey(obsConst)) {
    // SosObservation sosObs = obsConstObsMap.get(obsConst);
    // if (supportsQuality && qualityList != null
    // && sosObs.containsObservationValues(phenID, sosObsValue)) {
    // sosObs.addQualityToSosObservationValue(phenID, sosObsValue, qualityList);
    // } else {
    // sosObs.addValue(phenID, sosObsValue);
    // }
    // } else {
    // SosObservation sosObs = new SosObservation();
    // sosObs.setObservationConstellation(obsConst);
    // sosObs.setNoDataValue(noDataValue);
    // sosObs.setTokenSeparator(tokenSeparator);
    // sosObs.setTupleSeparator(tupleSeparator);
    // sosObs.addValue(phenID, sosObsValue);
    // obsConstObsMap.put(obsConst, sosObs);
    // }
    // }
    // }
    // if (!obsConstObsMap.isEmpty()) {
    // Map<String, SosAbstractFeature> sosAbstractFeatures =
    // Configurator.getInstance().getFeatureQueryHandler()
    // .getFeatures(features, null, session, version);
    // for (SosAbstractFeature feat : sosAbstractFeatures.values()) {
    // SosSamplingFeature feature = (SosSamplingFeature) feat;
    // boundedBy = SosHelper.checkEnvelope(boundedBy, feature.getGeometry());
    // }
    // sosObservationCollection.setObservationMembers(obsConstObsMap.values());
    // sosObservationCollection.setFeatures(sosAbstractFeatures);
    // sosObservationCollection.setBoundedBy(boundedBy);
    // sosObservationCollection.setSrid(srid);
    //
    // }
    // return sosObservationCollection;
    // }
    //
    // /**
    // * Get observation value from all value tables for an Observation object
    // *
    // * @param hObservation
    // * Observation object
    // * @return Observation value
    // */
    // private Object getValueFromAllTable(Observation hObservation) {
    // if (hObservation.getNumericValues() != null &&
    // !hObservation.getNumericValues().isEmpty()) {
    // return getValueFromNumericValueTable(hObservation.getNumericValues());
    // } else if (hObservation.getTextValues() != null &&
    // !hObservation.getTextValues().isEmpty()) {
    // return getValueFromTextValueTable(hObservation.getTextValues());
    // } else if (hObservation.getGeometryValues() != null &&
    // !hObservation.getGeometryValues().isEmpty()) {
    // return getValueFromGeometryValueTable(hObservation.getGeometryValues());
    // }
    // return null;
    // }
    //
    // /**
    // * Get observation value from numeric table
    // *
    // * @param numericValues
    // * Numeric values
    // * @return Numeric value
    // */
    // private Double getValueFromNumericValueTable(Set<NumericValue>
    // numericValues) {
    // for (NumericValue numericValue : numericValues) {
    // return (Double) numericValue.getValue();
    // }
    // return Double.NaN;
    // }
    //
    // /**
    // * Get observation value from text table
    // *
    // * @param textValues
    // * Text values
    // * @return Text value
    // */
    // private String getValueFromTextValueTable(Set<TextValue> textValues) {
    // for (TextValue textValue : textValues) {
    // return (String) textValue.getValue();
    // }
    // return "";
    // }
    //
    // /**
    // * Get observation value from spatial table
    // *
    // * @param geometryValues
    // * Spatial values
    // * @return Spatial value
    // */
    // private Geometry getValueFromGeometryValueTable(Set<GeometryValue>
    // geometryValues) {
    // for (GeometryValue geometryValue : geometryValues) {
    // return (Geometry) geometryValue.getValue();
    // }
    // return null;
    // }
    //
    // /**
    // * Adds a FOI to the map with FOIs for procedures
    // *
    // * @param feature4proc
    // * FOIs for procedure map
    // * @param procID
    // * procedure identifier
    // * @param foiID
    // * FOI identifier
    // * @return updated map
    // */
    // private Map<String, Set<String>> setFeatureForProcedure(Map<String,
    // Set<String>> feature4proc, String procID,
    // String foiID) {
    // Set<String> features;
    // if (feature4proc.containsKey(procID)) {
    // features = feature4proc.get(procID);
    //
    // } else {
    // features = new HashSet<String>();
    // }
    // if (!features.contains(foiID)) {
    //
    // }
    // features.add(foiID);
    // feature4proc.put(procID, features);
    // return feature4proc;
    // }

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
            Map<MinMax, String> eventTime = new HashMap<MinMax, String>(2);
            DateTime minDate = Configurator.getInstance().getCapabilitiesCacheController().getMinEventTime();
            DateTime maxDate = Configurator.getInstance().getCapabilitiesCacheController().getMaxEventTime();
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

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

}
