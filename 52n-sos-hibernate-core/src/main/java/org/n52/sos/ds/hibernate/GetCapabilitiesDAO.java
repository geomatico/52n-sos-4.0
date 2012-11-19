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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.binding.Binding;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetCapabilitiesDAO;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.filter.FilterCapabilities;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosInsertionCapabilities;
import org.n52.sos.ogc.sos.SosOfferingsForContents;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Implementation of the interface IGetCapabilitiesDAO
 * 
 */
public class GetCapabilitiesDAO implements IGetCapabilitiesDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetCapabilitiesDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetCapabilities.name();

    /**
     * XML object of the service identification section, loaded from file
     */
    private XmlObject serviceIdentification;

    /**
     * XML object of the service provider section, loaded from file
     */
    private XmlObject serviceProvider;

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     * 
     * @throws OwsExceptionReport
     *             If an error occurs If a file could not be loaded
     */
    public GetCapabilitiesDAO() throws OwsExceptionReport {
        this.serviceIdentification =
                XmlHelper.loadXmlDocumentFromFile(Configurator.getInstance().getServiceIdentification());
        this.serviceProvider = XmlHelper.loadXmlDocumentFromFile(Configurator.getInstance().getServiceProvider());
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
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ds.IGetCapabilitiesDAO#getCapabilities(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public GetCapabilitiesResponse getCapabilities(GetCapabilitiesRequest request) throws OwsExceptionReport {
        Session session = null;
        try {
            GetCapabilitiesRequest sosRequest = (GetCapabilitiesRequest) request;
            session = (Session) connectionProvider.getConnection();
            GetCapabilitiesResponse response = new GetCapabilitiesResponse();
            response.setService(SosConstants.SOS);
            if (request.getVersion() == null) {
                if (sosRequest.getAcceptVersions() != null) {
                    String[] acceptedVersion = sosRequest.getAcceptVersions();
                    for (int i = 0; i < acceptedVersion.length; i++) {
                        if (Configurator.getInstance().isVersionSupported(acceptedVersion[i])) {
                            response.setVersion(acceptedVersion[i]);
                            break;
                        }
                    }
                } else {
                    for (String supportedVersion : Configurator.getInstance().getSupportedVersions()) {
                        response.setVersion(supportedVersion);
                        break;
                    }
                }
            } else {
                response.setVersion(request.getVersion());
            }
            if (response.getVersion() == null) {
                String exceptionText =
                        "The requested '" + SosConstants.GetCapabilitiesParams.AcceptVersions.name()
                                + "' values are not supported by this service!";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createVersionNegotiationFailedException(exceptionText);
            }

            // booleans for sections (true if section is selected
            // explicitly)
            boolean serviceIdentificationSection = false;
            boolean serviceProviderSection = false;
            boolean operationsMetadataSection = false;
            boolean filter_CapabilitiesSection = false;
            boolean contentsSection = false;
            boolean insertionCapabilities = false;
            boolean all = false;

            // handle sections array and set requested sections 'true'
            if (sosRequest.getSections() != null) {
                for (String section : sosRequest.getSections()) {

                    if (!section.isEmpty()) {
                        /*
                         * if name of requested section is incorrect (e.g.
                         * conten), throw Exception! (Case sensitive!!)
                         */
                        if (!Sos1Constants.CapabilitiesSections.contains(section)
                                && !Sos2Constants.CapabilitiesSections.contains(section)) {
                            String exceptionText = "The requested section '" + section + "' does not exist or is not supported!";
                            LOGGER.debug(exceptionText);
                            throw Util4Exceptions.createInvalidParameterValueException(
                                    SosConstants.GetCapabilitiesParams.Section.name(), exceptionText);
                        }

                        // if name is correct, check which section is requested
                        // and
                        // set boolean on true
                        if (section.equals(SosConstants.CapabilitiesSections.All.name())) {
                            all = true;
                            break;
                        } else if (section.equals(SosConstants.CapabilitiesSections.ServiceIdentification.name())) {
                            serviceIdentificationSection = true;
                        } else if (section.equals(SosConstants.CapabilitiesSections.ServiceProvider.name())) {
                            serviceProviderSection = true;
                        } else if (section.equals(SosConstants.CapabilitiesSections.OperationsMetadata.name())) {
                            operationsMetadataSection = true;
                        } else if (section.equals(Sos1Constants.CapabilitiesSections.Filter_Capabilities.name())
                                || section.equals(Sos2Constants.CapabilitiesSections.FilterCapabilities.name())) {
                            filter_CapabilitiesSection = true;
                        } else if (section.equals(SosConstants.CapabilitiesSections.Contents.name())) {
                            contentsSection = true;
                        } else if (section.equals(Sos2Constants.CapabilitiesSections.InsertionCapabilities.name())) {
                            insertionCapabilities = true;
                        }
                    }
                }
            } else {
                all = true;
            }

            List<IExtension> extensions = null;
            if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                extensions = getExtensions(session);
            }

            // response with all sections should be created
            SosCapabilities sosCapabilities = new SosCapabilities();
            if (all) {
                sosCapabilities.setServiceIdentification(getServiceIdentification(response.getVersion()));
                sosCapabilities.setServiceProvider(getServiceProvicer());
                sosCapabilities.setFilterCapabilities(getFilterCapabilities(response.getVersion()));
                sosCapabilities.setOperationsMetadata(getOperationsMetadataForOperations(response.getService(),
                        response.getVersion(), extensions, session));
                if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                    sosCapabilities.setContents(getContentsForSosV2(response.getVersion(), session));
                    sosCapabilities.setExensions(extensions);
                } else {
                    sosCapabilities.setContents(getContents(sosCapabilities, session));
                }
            }
            if (serviceIdentificationSection) {
                sosCapabilities.setServiceIdentification(getServiceIdentification(response.getVersion()));
            }
            if (serviceProviderSection) {
                sosCapabilities.setServiceProvider(getServiceProvicer());
            }
            if (operationsMetadataSection) {
                sosCapabilities.setOperationsMetadata(getOperationsMetadataForOperations(response.getService(),
                        response.getVersion(), extensions, session));
            }
            if (filter_CapabilitiesSection) {
                sosCapabilities.setFilterCapabilities(getFilterCapabilities(response.getVersion()));
            }
            if (contentsSection) {
                if (response.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                    sosCapabilities.setContents(getContentsForSosV2(response.getVersion(), session));
                } else {
                    sosCapabilities.setContents(getContents(sosCapabilities, session));
                }
            }
            if (response.getVersion().equals(Sos2Constants.SERVICEVERSION) && insertionCapabilities) {
                sosCapabilities.setExensions(extensions);
            }
            response.setCapabilities(sosCapabilities);
            return response;
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for Capabilities document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    private SosServiceIdentification getServiceIdentification(String version) {
        SosServiceIdentification serviceIdentification = new SosServiceIdentification();
        serviceIdentification.setServiceIdentification(this.serviceIdentification);
        serviceIdentification.setVersions(Configurator.getInstance().getSupportedVersions());
        serviceIdentification.setKeywords(getKeywords());
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            serviceIdentification.setProfiles(getProfiles());
        }
        return serviceIdentification;
    }

    private SosServiceProvider getServiceProvicer() {
        SosServiceProvider serviceProvider = new SosServiceProvider();
        serviceProvider.setServiceProvider(this.serviceProvider);
        return serviceProvider;
    }

    private List<String> getKeywords() {
        return Arrays.asList(Configurator.getInstance().getServiceIdentificationKeywords());
    }

    private List<String> getProfiles() {
        Set<String> profiles = new HashSet<String>();
        for (Binding bindig : Configurator.getInstance().getBindingOperators().values()) {
            profiles.addAll(bindig.getConformanceClasses());
        }
        for (IRequestOperator requestOperator : Configurator.getInstance().getRequestOperator().values()) {
            profiles.addAll(requestOperator.getConformanceClasses());
        }
        for (List<IDecoder> decoderList : Configurator.getInstance().getDecoderMap().values()) {
            for (IDecoder decoder : decoderList) {
                profiles.addAll(decoder.getConformanceClasses());
            }
        }
        for (IEncoder encoder : Configurator.getInstance().getEncoderMap().values()) {
            profiles.addAll(encoder.getConformanceClasses());
        }
        return new ArrayList<String>(profiles);
    }

    /**
     * Get the OperationsMetadat for all supported operations
     * 
     * @param sosCapabilities
     *            SOS internal capabilities SOS internal capabilities
     * @param session
     *            Hibernate session Hibernate session
     * @return List of OperationsMetadata
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private OWSOperationsMetadata getOperationsMetadataForOperations(String service, String version,
            List<IExtension> extensions, Session session) throws OwsExceptionReport {
        OWSOperationsMetadata operationsMetadata = new OWSOperationsMetadata();
        List<OWSOperation> opsMetadata = new ArrayList<OWSOperation>();

        // FIXME: OpsMeata for InsertSensor, InsertObservation SOS 2.0
        Map<RequestOperatorKeyType, IRequestOperator> requestOperators =
                Configurator.getInstance().getRequestOperator();
        opsMetadata.add(getOpsGetCapabilities(service, version, extensions));
        for (RequestOperatorKeyType requestOperatorKeyType : requestOperators.keySet()) {
            if (!requestOperatorKeyType.getOperationName().equals(OPERATION_NAME)
                    && requestOperatorKeyType.getServiceOperatorKeyType().getVersion().equals(version)) {
                OWSOperation operationMetadata = requestOperators.get(requestOperatorKeyType).getOperationMetadata(service, version,
                        session);
                if (operationMetadata != null) {
                    opsMetadata.add(operationMetadata);
                }
            }
        }
        operationsMetadata.setOperations(opsMetadata);
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.service.name(), new OWSParameterValuePossibleValues(SosConstants.SOS));
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.version.name(), new OWSParameterValuePossibleValues(Configurator
                .getInstance().getSupportedVersions()));
        return operationsMetadata;
    }

    /**
     * Get the FilterCapabilities
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @return FilterCapabilities
     */
    private FilterCapabilities getFilterCapabilities(String version) {
        FilterCapabilities filterCapabilities = new FilterCapabilities();

        // !!! Modify methods addicted to your implementation !!!
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            getScalarFilterCapabilities(filterCapabilities);
        }
        getSpatialFilterCapabilities(filterCapabilities, version);
        getTemporalFilterCapabilities(filterCapabilities, version);

        return filterCapabilities;
    }

    /**
     * Get the contents for SOS 1.0.0 capabilities
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @param session
     *            Hibernate session
     * @return Offerings for contents
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private List<SosOfferingsForContents> getContents(SosCapabilities sosCapabilities, Session session)
            throws OwsExceptionReport {
        List<SosOfferingsForContents> sosOfferings = new ArrayList<SosOfferingsForContents>();

        Collection<String> offerings = Configurator.getInstance().getCapabilitiesCacheController().getOfferings();

        for (String offering : offerings) {
            SosOfferingsForContents sosOffering = new SosOfferingsForContents();

            sosOffering.setOffering(offering);

            // only if fois are contained for the offering set the values of the
            // envelope
            sosOffering.setObservedArea(Configurator.getInstance().getCapabilitiesCacheController().getEnvelopeForOffering(offering));
            // SosEnvelope sosEnvelope = getBBOX4Offering(offering, session);
            // sosOffering.setBoundeBy(sosEnvelope.getEnvelope());
            // sosOffering.setSrid(sosEnvelope.getSrid());

            // TODO: add intended application
            // xb_oo.addIntendedApplication("");

            // add offering name
            sosOffering.setOfferingName(Configurator.getInstance().getCapabilitiesCacheController()
                    .getOfferingName(offering));

            // set up phenomena
            sosOffering.setObservableProperties(Configurator.getInstance().getCapabilitiesCacheController()
                    .getObservablePropertiesForOffering(offering));
            sosOffering.setCompositePhenomena(Configurator.getInstance().getCapabilitiesCacheController()
                    .getKOfferingVCompositePhenomenons().get(offering));
            Map<String, Collection<String>> phens4CompPhens = new HashMap<String, Collection<String>>();
            if (Configurator.getInstance().getCapabilitiesCacheController().getKOfferingVCompositePhenomenons()
                    .get(offering) != null) {
                for (String compositePhenomenon : Configurator.getInstance().getCapabilitiesCacheController()
                        .getKOfferingVCompositePhenomenons().get(offering)) {
                    phens4CompPhens.put(compositePhenomenon,
                            Configurator.getInstance().getCapabilitiesCacheController()
                                    .getKCompositePhenomenonVObservableProperty().get(compositePhenomenon));
                }
            }
            sosOffering.setPhens4CompPhens(phens4CompPhens);

            // set up time
            DateTime minDate = Configurator.getInstance().getCapabilitiesCacheController().getMinTimeForOffering(offering);
            DateTime maxDate = Configurator.getInstance().getCapabilitiesCacheController().getMaxTimeForOffering(offering);
            sosOffering.setTime(new TimePeriod(minDate, maxDate));

            // add feature of interests
            if (Configurator.getInstance().isFoiListedInOfferings()) {
                sosOffering.setFeatureOfInterest(getFOI4offering(offering, session));
            }

            // set procedures
            Collection<String> procedures =
                    Configurator.getInstance().getCapabilitiesCacheController().getProcedures4Offering(offering);
            if (procedures == null || procedures.isEmpty()) {
                String exceptionText =
                        "No procedures are contained in the database for the offering: " + offering
                                + "! Please contact the admin of this SOS.";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            sosOffering.setProcedures(procedures);

            // insert result models
            Collection<QName> resultModels =
                    getQNamesForResultModel(Configurator.getInstance().getCapabilitiesCacheController()
                            .getResultModels4Offering(offering));

            if (resultModels == null || resultModels.isEmpty()) {
                String exceptionText =
                        "No result models are contained in the database for the offering: " + offering
                                + "! Please contact the admin of this SOS.";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            sosOffering.setResultModels(resultModels);

            // set response format
            Collection<String> responseFormats = SosHelper.getSupportedResponseFormats(SosConstants.SOS, "1.0.0");
            responseFormats.add(SosConstants.CONTENT_TYPE_ZIP);
            sosOffering.setResponseFormats(responseFormats);

            // set response Mode
            sosOffering.setResponseModes(Arrays.asList(SosConstants.getResponseModes()));

            sosOfferings.add(sosOffering);
        }

        return sosOfferings;
    }

    /**
     * Get the contents for SOS 2.0 capabilities
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @param session
     *            Hibernate session
     * @return Offerings for contents
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private List<SosOfferingsForContents> getContentsForSosV2(String version, Session session)
            throws OwsExceptionReport {
        int phenTimeCounter = 1;
        List<SosOfferingsForContents> sosOfferings = new ArrayList<SosOfferingsForContents>();

        Collection<String> offerings = Configurator.getInstance().getCapabilitiesCacheController().getOfferings();

        for (String offering : offerings) {
            Collection<String> procedures =
                    Configurator.getInstance().getCapabilitiesCacheController().getProcedures4Offering(offering);
            if (procedures == null || procedures.isEmpty()) {
                String exceptionText =
                        "No procedures are contained in the database for the offering: " + offering
                                + "! Please contact the admin of this SOS.";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

            for (String procedure : procedures) {
                SosOfferingsForContents sosOffering = new SosOfferingsForContents();
                sosOffering.setOffering(offering);
                sosOffering.setObservedArea(Configurator.getInstance().getCapabilitiesCacheController().getEnvelopeForOffering(offering));
                // if (sosEnvelope != null) {
                // sosOffering.setBoundeBy(sosEnvelope.getEnvelope());
                // sosOffering.setSrid(sosEnvelope.getSrid());
                // }

                Collection<String> offProc = new ArrayList<String>();
                offProc.add(procedure);
                sosOffering.setProcedures(offProc);

                // TODO: add intended application
                // xb_oo.addIntendedApplication("");

                // add offering name
                sosOffering.setOfferingName(Configurator.getInstance().getCapabilitiesCacheController()
                        .getOfferingName(offering));

                // set up phenomena
                Collection<String> phenomenons = new ArrayList<String>();
                Map<String, Collection<String>> phenProcs =
                        Configurator.getInstance().getCapabilitiesCacheController()
                                .getKObservablePropertyVProcedures();
                Collection<String> phens4Off =
                        Configurator.getInstance().getCapabilitiesCacheController()
                                .getObservablePropertiesForOffering(offering);
                for (String phenID : phens4Off) {
                    if (phenProcs.get(phenID).contains(procedure)) {
                        phenomenons.add(phenID);
                    }
                }
                sosOffering.setObservableProperties(phenomenons);
                sosOffering.setCompositePhenomena(Configurator.getInstance().getCapabilitiesCacheController()
                        .getKOfferingVCompositePhenomenons().get(offering));
                Map<String, Collection<String>> phens4CompPhens = new HashMap<String, Collection<String>>();
                if (Configurator.getInstance().getCapabilitiesCacheController().getKOfferingVCompositePhenomenons()
                        .get(offering) != null) {
                    for (String compositePhenomenon : Configurator.getInstance().getCapabilitiesCacheController()
                            .getKOfferingVCompositePhenomenons().get(offering)) {
                        phens4CompPhens.put(compositePhenomenon,
                                Configurator.getInstance().getCapabilitiesCacheController()
                                        .getKCompositePhenomenonVObservableProperty().get(compositePhenomenon));
                    }
                }
                sosOffering.setPhens4CompPhens(phens4CompPhens);

                // set up time
                DateTime minDate = HibernateCriteriaQueryUtilities.getMinDate4Offering(offering, session);
                DateTime maxDate = HibernateCriteriaQueryUtilities.getMaxDate4Offering(offering, session);
                String phenTimeId = Sos2Constants.EN_PHENOMENON_TIME + "_" + phenTimeCounter++;
                sosOffering.setTime(new TimePeriod(minDate, maxDate, phenTimeId));

                // add related feature
                Map<String, Collection<String>> relatedFeatures = new HashMap<String, Collection<String>>();
                // // related feature
                if (Configurator.getInstance().getCapabilitiesCacheController().getKOfferingVRelatedFeatures() != null
                        && !Configurator.getInstance().getCapabilitiesCacheController().getKOfferingVRelatedFeatures()
                                .isEmpty()) {
                    Collection<String> relatedFeatureMap =
                            Configurator.getInstance().getCapabilitiesCacheController().getKOfferingVRelatedFeatures()
                                    .get(offering);
                    for (String relatedFeature : relatedFeatureMap) {
                        if (relatedFeature.contains("http") || relatedFeature.contains("HTTP")) {
                            relatedFeatures.put(relatedFeature, Configurator.getInstance()
                                    .getCapabilitiesCacheController().getKRelatedFeaturesVRole().get(relatedFeature));
                        } else {
                            String relatedFeatureID = getRelatedFeatureID(relatedFeature, session, version);
                            if (relatedFeatureID != null) {
                                relatedFeatures.put(relatedFeatureID,
                                        Configurator.getInstance().getCapabilitiesCacheController()
                                                .getKRelatedFeaturesVRole().get(relatedFeature));
                            }
                        }
                    }
                }
                // feature of interest
                else {
                    List<String> role = new ArrayList<String>();
                    role.add("featureOfInterestID");
                    for (String foiID : Configurator.getInstance().getCapabilitiesCacheController()
                            .getKOfferingVFeatures().get(offering)) {
                        if (Configurator.getInstance().getCapabilitiesCacheController()
                                .getProcedures4FeatureOfInterest(foiID).contains(procedure)) {
                            relatedFeatures.put(foiID, role);
                        }
                    }
                }
                sosOffering.setRelatedFeatures(relatedFeatures);

                // insert observationTypes
                sosOffering.setObservationTypes(getObservationTypes(offering));

                // TODO: if no foi contained, set allowed foitypes
                // insert featureOfInterestTypes
                Collection<String> featureTypes = getFeatureOfInterestTypes(offering, session);
                if (featureTypes == null || (featureTypes != null && featureTypes.isEmpty())) {
                    featureTypes = HibernateCriteriaQueryUtilities.getFeatureOfInterestTypes(session);
                }
                sosOffering.setFeatureOfInterestTypes(featureTypes);
                
                // TODO: set procDescFormat
                sosOffering.setProcedureDescriptionFormat(HibernateCriteriaQueryUtilities.getProcedureDescriptionFormatIdentifiers(session));

                // set response format
                Collection<String> responseFormats = SosHelper.getSupportedResponseFormats(SosConstants.SOS, version);
                sosOffering.setResponseFormats(responseFormats);
                // TODO set as property
                if (true) {
                    responseFormats.add(SosConstants.CONTENT_TYPE_ZIP);
                }
                

                sosOfferings.add(sosOffering);
            }
        }

        return sosOfferings;
    }

    /**
     * Get extensions
     * 
     * @return Extensions
     * @throws OwsExceptionReport
     */
    private List<IExtension> getExtensions(Session session) throws OwsExceptionReport {
        List<IExtension> extensions = new ArrayList<IExtension>();
        Map<RequestOperatorKeyType, IRequestOperator> requestOperators =
                Configurator.getInstance().getRequestOperator();
        for (IRequestOperator requestOperator : requestOperators.values()) {
            IExtension extension = requestOperator.getExtension(session);
            if (extension != null) {
                extensions.add(extension);
            }
        }
        return extensions;
    }

    /**
     * Get OperationsMetadata for the GetCapabilities operation
     * 
     * @param sosCapabilities
     *            SOS internal capabilities
     * @param version
     *            SOS version
     * @param string
     * @return OperationsMetadata for GetCapabilities
     * @throws OwsExceptionReport
     */
    private OWSOperation getOpsGetCapabilities(String service, String version, List<IExtension> extensions)
            throws OwsExceptionReport {
        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(SosConstants.Operations.GetCapabilities.name());
        // set DCP
        DecoderKeyType dkt;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        }
        opsMeta.setDcp(SosHelper.getDCP(SosConstants.Operations.GetCapabilities.name(), dkt, Configurator
                .getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set param updateSequence
        List<String> updateSequenceValues = new ArrayList<String>();
        updateSequenceValues.add(SosConstants.PARAMETER_ANY);
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.updateSequence.name(), new OWSParameterValuePossibleValues(updateSequenceValues));
        // set param AcceptVersions
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.AcceptVersions.name(), new OWSParameterValuePossibleValues(Configurator.getInstance()
                .getSupportedVersions()));
        // set param Sections
        List<String> sectionsValues = new ArrayList<String>();
        sectionsValues.add(SosConstants.CapabilitiesSections.ServiceIdentification.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.ServiceProvider.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.OperationsMetadata.name());
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            sectionsValues.add(Sos1Constants.CapabilitiesSections.Filter_Capabilities.name());
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            sectionsValues.add(Sos2Constants.CapabilitiesSections.FilterCapabilities.name());
            if (checkAndMergeInsertionCapabilities(extensions)) {
                sectionsValues.add(Sos2Constants.CapabilitiesSections.InsertionCapabilities.name());
            }
        }
        sectionsValues.add(SosConstants.CapabilitiesSections.Contents.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.All.name());
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.Sections.name(), new OWSParameterValuePossibleValues(sectionsValues));
        // set param AcceptFormats
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.AcceptFormats.name(),
                new OWSParameterValuePossibleValues(Arrays.asList(SosConstants.getAcceptFormats())));

        return opsMeta;
    }

    /**
     * Set SpatialFilterCapabilities to FilterCapabilities
     * 
     * @param filterCapabilities
     *            FilterCapabilities
     * @param version
     *            SOS version
     */
    private void getSpatialFilterCapabilities(FilterCapabilities filterCapabilities, String version) {

        // set GeometryOperands
        List<QName> operands = new ArrayList<QName>();
        operands.add(GMLConstants.QN_ENVELOPE);
        // additional spatial operands for SOS 1.0
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            operands.add(GMLConstants.QN_POINT);
            operands.add(GMLConstants.QN_LINESTRING);
            operands.add(GMLConstants.QN_POLYGON);
        }

        filterCapabilities.setSpatialOperands(operands);

        // set SpatialOperators
        Map<SpatialOperator, List<QName>> spatialOperators = new EnumMap<SpatialOperator, List<QName>>(SpatialOperator.class);
        // set BBOX
        List<QName> operands4BBox = new ArrayList<QName>();
        operands4BBox.add(GMLConstants.QN_ENVELOPE);
        spatialOperators.put(SpatialOperator.BBOX, operands4BBox);

        // additional spatial operators for SOS 1.0
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // set Contains
            List<QName> operands4Contains = new ArrayList<QName>();
            operands4Contains.add(GMLConstants.QN_POINT);
            operands4Contains.add(GMLConstants.QN_LINESTRING);
            operands4Contains.add(GMLConstants.QN_POLYGON);
            spatialOperators.put(SpatialOperator.Contains, operands4Contains);
            // set Intersects
            List<QName> operands4Intersects = new ArrayList<QName>();
            operands4Intersects.add(GMLConstants.QN_POINT);
            operands4Intersects.add(GMLConstants.QN_LINESTRING);
            operands4Intersects.add(GMLConstants.QN_POLYGON);
            spatialOperators.put(SpatialOperator.Intersects, operands4Intersects);
            // set Overlaps
            List<QName> operands4Overlaps = new ArrayList<QName>();
            operands4Overlaps.add(GMLConstants.QN_POINT);
            operands4Overlaps.add(GMLConstants.QN_LINESTRING);
            operands4Overlaps.add(GMLConstants.QN_POLYGON);
            spatialOperators.put(SpatialOperator.Overlaps, operands4Overlaps);
        }

        filterCapabilities.setSpatialOperators(spatialOperators);
    }

    /**
     * Set TemporalFilterCapabilities to FilterCapabilities
     * 
     * @param filterCapabilities
     *            FilterCapabilities
     * @param version
     *            SOS version
     */
    private void getTemporalFilterCapabilities(FilterCapabilities filterCapabilities, String version) {

        // set TemporalOperands
        List<QName> operands = new ArrayList<QName>();
        operands.add(GMLConstants.QN_TIME_PERIOD);
        operands.add(GMLConstants.QN_TIME_INSTANT);

        filterCapabilities.setTemporalOperands(operands);

        // set TemporalOperators
        Map<TimeOperator, List<QName>> temporalOperators = new EnumMap<TimeOperator, List<QName>>(TimeOperator.class);
        // set TM_During
        List<QName> operands4During = new ArrayList<QName>();
        operands4During.add(GMLConstants.QN_TIME_PERIOD);
        temporalOperators.put(TimeOperator.TM_During, operands4During);
        // set TM_Equals
        List<QName> operands4Equals = new ArrayList<QName>();
        operands4Equals.add(GMLConstants.QN_TIME_INSTANT);
        temporalOperators.put(TimeOperator.TM_Equals, operands4Equals);
        // additional temporal operators for SOS 1.0
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            // set TM_After
            List<QName> operands4After = new ArrayList<QName>();
            operands4After.add(GMLConstants.QN_TIME_INSTANT);
            temporalOperators.put(TimeOperator.TM_After, operands4After);
            // set TM_Before
            List<QName> operands4Before = new ArrayList<QName>();
            operands4Before.add(GMLConstants.QN_TIME_INSTANT);
            temporalOperators.put(TimeOperator.TM_Before, operands4Before);
        }

        filterCapabilities.setTempporalOperators(temporalOperators);
    }

    /**
     * Set ScalarFilterCapabilities to FilterCapabilities
     * 
     * @param filterCapabilities
     *            FilterCapabilities
     */
    private void getScalarFilterCapabilities(FilterCapabilities filterCapabilities) {
        List<ComparisonOperator> comparisonOperators = new ArrayList<ComparisonOperator>();
        comparisonOperators.add(ComparisonOperator.PropertyIsBetween);
        comparisonOperators.add(ComparisonOperator.PropertyIsEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsNotEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsLessThan);
        comparisonOperators.add(ComparisonOperator.PropertyIsLessThanOrEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsGreaterThan);
        comparisonOperators.add(ComparisonOperator.PropertyIsGreaterThanOrEqualTo);
        comparisonOperators.add(ComparisonOperator.PropertyIsLike);
        filterCapabilities.setComparisonOperators(comparisonOperators);
	}

    /**
     * Get FOIs contained in an offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return FOI identifiers
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private List<String> getFOI4offering(String offering, Session session) throws OwsExceptionReport {
        List<String> featureIDs =
                HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(offering, session);
        return featureIDs;
    }

    /**
     * Get the QName for resultModels from observationType constant
     * 
     * @param resultModels4Offering
     *            Observation types
     * @return QNames for resultModel parameter
     */
    private Collection<QName> getQNamesForResultModel(Collection<String> resultModels4Offering) {
        List<QName> resultModels = new ArrayList<QName>();
        for (String string : resultModels4Offering) {
            if (string.equals(OMConstants.OBS_TYPE_MEASUREMENT)) {
                resultModels.add(OMConstants.RESULT_MODEL_MEASUREMENT);
            } else if (string.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_COUNT_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_TRUTH_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_TEXT_OBSERVATION);
            } else if (string.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)) {
                resultModels.add(OMConstants.RESULT_MODEL_OBSERVATION);
            } else {
                resultModels.add(OMConstants.RESULT_MODEL_OBSERVATION);
            }
        }
        return resultModels;
    }

    private String getRelatedFeatureID(String identifier, Session session, String version) throws OwsExceptionReport {
        SosSamplingFeature feature =
                (SosSamplingFeature) Configurator.getInstance().getFeatureQueryHandler()
                        .getFeatureByID(identifier, session, version);
        if (feature.getUrl() != null && !feature.getUrl().isEmpty()) {
            return feature.getUrl();
        } else {
            String urlPattern =
                    SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                            SosConstants.Operations.GetFeatureOfInterest.name(), new DecoderKeyType(SosConstants.SOS,
                                    version));
            return SosHelper.createFoiGetUrl(identifier, version, Configurator.getInstance().getServiceURL(),
                    urlPattern);
        }
    }

    private Collection<String> getObservationTypes(String offering) {
        List<String> observationTypes = new ArrayList<String>();
        for (String observationType : Configurator.getInstance().getCapabilitiesCacheController()
                .getObservationTypes4Offering(offering)) {
            if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                observationTypes.add(observationType);
            }
        }
        if (observationTypes.isEmpty()) {
            for (String observationType : Configurator.getInstance().getCapabilitiesCacheController()
                    .getAllowedObservationTypes4Offering(offering)) {
                if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                    observationTypes.add(observationType);
                }
            } 
        }
        return observationTypes;
    }

    private Collection<String> getFeatureOfInterestTypes(String offering, Session session) {
        Collection<String> featureIDs =
                Configurator.getInstance().getCapabilitiesCacheController().getKOfferingVFeatures().get(offering);
        if (featureIDs == null || (featureIDs != null && featureIDs.isEmpty())) {
            return HibernateCriteriaQueryUtilities.getFeatureOfInterestTypesForFeatureOfInterest(featureIDs, session);
        }
        return new ArrayList<String>(0);

    }

    private boolean checkAndMergeInsertionCapabilities(List<IExtension> extensions) {
        SosInsertionCapabilities insertionCapabilities = null;
        for (IExtension iExtension : extensions) {
            if (iExtension instanceof SosInsertionCapabilities) {
                if (insertionCapabilities == null) {
                    insertionCapabilities = (SosInsertionCapabilities) iExtension;
                } else {
                    insertionCapabilities.addInsertionCapabilities((SosInsertionCapabilities) iExtension);
                }
            }
        }
        if (insertionCapabilities != null) {
            return true;
        }
        return false;
    }

}