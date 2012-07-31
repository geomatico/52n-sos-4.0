/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetCapabilitiesDAO;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.filter.FilterCapabilities;
import org.n52.sos.ogc.filter.FilterConstants.ComparisonOperator;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSOperationsMetadata;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.sos.SosOfferingsForContents;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.SosGetCapabilitiesRequest;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
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
     * @see
     * org.n52.sos.ds.IGetCapabilitiesDAO#getCapabilities(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public SosCapabilities getCapabilities(AbstractServiceRequest request) throws OwsExceptionReport {
        Session session = null;
        try {
            SosGetCapabilitiesRequest sosRequest = (SosGetCapabilitiesRequest) request;
            session = (Session) connectionProvider.getConnection();

            SosCapabilities sosCapabilities = new SosCapabilities();
            sosCapabilities.setService(SosConstants.SOS);
            if (request.getVersion() == null) {
                if (sosRequest.getAcceptVersions() != null) {
                    String[] acceptedVersion = sosRequest.getAcceptVersions();
                    for (int i = 0; i < acceptedVersion.length; i++) {
                        if (Configurator.getInstance().isVersionSupported(acceptedVersion[i])) {
                            sosCapabilities.setVersion(acceptedVersion[i]);
                            break;
                        }
                    }
                } else {
                    for (String supportedVersion : Configurator.getInstance().getSupportedVersions()) {
                        sosCapabilities.setVersion(supportedVersion);
                        break;
                    }
                }
            } else {
                sosCapabilities.setVersion(request.getVersion());
            }
            if (sosCapabilities.getVersion() == null) {
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
                            String exceptionText = "The requested section '" + section + "' does not exist!";
                            LOGGER.debug(exceptionText);
                            throw Util4Exceptions.createInvalidParameterValueException(
                                    SosConstants.GetCapabilitiesParams.Sections.name(), exceptionText);
                        }

                        // if name is correct, check which section is requested
                        // and
                        // set boolean on true
                        if (section.equals(SosConstants.CapabilitiesSections.All.toString())) {
                            all = true;
                            break;
                        } else if (section.equals(SosConstants.CapabilitiesSections.ServiceIdentification.toString())) {
                            serviceIdentificationSection = true;
                        } else if (section.equals(SosConstants.CapabilitiesSections.ServiceProvider.toString())) {
                            serviceProviderSection = true;
                        } else if (section.equals(SosConstants.CapabilitiesSections.OperationsMetadata.toString())) {
                            operationsMetadataSection = true;
                        } else if (section.equals(Sos1Constants.CapabilitiesSections.Filter_Capabilities.toString())
                                || section.equals(Sos2Constants.CapabilitiesSections.FilterCapabilities.toString())) {
                            filter_CapabilitiesSection = true;
                        } else if (section.equals(SosConstants.CapabilitiesSections.Contents.toString())) {
                            contentsSection = true;
                        }
                    }
                }
            } else {
                all = true;
            }

            // response with all sections should be created
            if (all) {
                sosCapabilities.setServiceIdentification(getServiceIdentification(sosCapabilities.getVersion()));
                sosCapabilities.setServiceProvider(getServiceProvicer());
                sosCapabilities.setFilterCapabilities(getFilterCapabilities(sosCapabilities));
                sosCapabilities.setOperationsMetadata(getOperationsMetadata(sosCapabilities, session));
                if (sosCapabilities.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                    sosCapabilities.setContents(getContentsForSosV2(sosCapabilities, session));
                    sosCapabilities.setExensions(getExtensions());
                } else {

                    sosCapabilities.setContents(getContents(sosCapabilities, session));
                }
            }
            if (serviceIdentificationSection) {
                sosCapabilities.setServiceIdentification(getServiceIdentification(sosCapabilities.getVersion()));
            }
            if (serviceProviderSection) {
                sosCapabilities.setServiceProvider(getServiceProvicer());
            }
            if (operationsMetadataSection) {
                sosCapabilities.setOperationsMetadata(getOperationsMetadata(sosCapabilities, session));
            }
            if (filter_CapabilitiesSection) {
                sosCapabilities.setFilterCapabilities(getFilterCapabilities(sosCapabilities));
            }
            if (contentsSection) {
                if (sosCapabilities.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
                    sosCapabilities.setContents(getContentsForSosV2(sosCapabilities, session));
                } else {
                    sosCapabilities.setContents(getContents(sosCapabilities, session));
                }
            }
            if (sosCapabilities.getVersion().equals(Sos2Constants.SERVICEVERSION)
                    && sosRequest.getExtensionArray() != null && sosRequest.getExtensionArray().size() != 0) {
                sosCapabilities.setExensions(getExtensions());
            }
            return sosCapabilities;
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for Capabilities document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
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
    public OWSOperation getOperationsMetadata(String service, String version, Object connection) {
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
        List<String> profiles = new ArrayList<String>();
        profiles.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingPoint");
        profiles.add("http://www.opengis.net/spec/SOS/2.0/conf/soap");
        return profiles;
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
    private OWSOperationsMetadata getOperationsMetadata(SosCapabilities sosCapabilities, Session session)
            throws OwsExceptionReport {
        OWSOperationsMetadata operationsMetadata = new OWSOperationsMetadata();
        List<OWSOperation> opsMetadata = new ArrayList<OWSOperation>();

        Map<RequestOperatorKeyType, IRequestOperator> requestOperators =
                Configurator.getInstance().getRequestOperator();
        opsMetadata.add(getOpsGetCapabilities(sosCapabilities, sosCapabilities.getService(),
                sosCapabilities.getVersion()));
        for (RequestOperatorKeyType requestOperatorKeyType : requestOperators.keySet()) {
            if (!requestOperatorKeyType.getOperationName().equals(OPERATION_NAME)
                    && requestOperatorKeyType.getServiceOperatorKeyType().getVersion()
                            .equals(sosCapabilities.getVersion())) {
                opsMetadata.add(requestOperators.get(requestOperatorKeyType).getOperationMetadata(
                        sosCapabilities.getService(), sosCapabilities.getVersion(), session));
            }
        }
        operationsMetadata.setOperations(opsMetadata);
        operationsMetadata.addCommonValue(OWSConstants.RequestParams.service.name(), SosConstants.SOS);
        operationsMetadata.addCommonValues(OWSConstants.RequestParams.version.name(), new HashSet<String>(Configurator
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
    private FilterCapabilities getFilterCapabilities(SosCapabilities sosCapabilities) {
        FilterCapabilities filterCapabilities = new FilterCapabilities();

        // !!! Modify methods addicted to your implementation !!!
        if (sosCapabilities.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
            getScalarFilterCapabilities(filterCapabilities);
        }
        getSpatialFilterCapabilities(filterCapabilities, sosCapabilities.getVersion());
        getTemporalFilterCapabilities(filterCapabilities, sosCapabilities.getVersion());

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

        Collection<String> offerings = Configurator.getInstance().getCapsCacheController().getOfferings();

        for (String offering : offerings) {
            SosOfferingsForContents sosOffering = new SosOfferingsForContents();

            sosOffering.setOffering(offering);

            // only if fois are contained for the offering set the values of the
            // envelope
            SosEnvelope sosEnvelope = getBBOX4Offering(offering, session);
            sosOffering.setBoundeBy(sosEnvelope.getEnvelope());
            sosOffering.setSrid(sosEnvelope.getSrid());

            // TODO: add intended application
            // xb_oo.addIntendedApplication("");

            // add offering name
            sosOffering.setOfferingName(Configurator.getInstance().getCapsCacheController().getOfferingName(offering));

            // set up phenomena
            sosOffering.setObservableProperties(Configurator.getInstance().getCapsCacheController()
                    .getObservablePropertiesForOffering(offering));
            sosOffering.setCompositePhenomena(Configurator.getInstance().getCapsCacheController()
                    .getKOfferingVCompositePhenomenons().get(offering));
            Map<String, Collection<String>> phens4CompPhens = new HashMap<String, Collection<String>>();
            if (Configurator.getInstance().getCapsCacheController().getKOfferingVCompositePhenomenons().get(offering) != null) {
                for (String compositePhenomenon : Configurator.getInstance().getCapsCacheController()
                        .getKOfferingVCompositePhenomenons().get(offering)) {
                    phens4CompPhens.put(compositePhenomenon, Configurator.getInstance().getCapsCacheController()
                            .getKCompositePhenomenonVObservableProperty().get(compositePhenomenon));
                }
            }
            sosOffering.setPhens4CompPhens(phens4CompPhens);

            // set up time
            DateTime minDate = HibernateCriteriaQueryUtilities.getMinDate4Offering(offering, session);
            DateTime maxDate = HibernateCriteriaQueryUtilities.getMaxDate4Offering(offering, session);
            sosOffering.setTime(new TimePeriod(minDate, maxDate));

            // add feature of interests
            if (Configurator.getInstance().isFoiListedInOfferings()) {
                sosOffering.setFeatureOfInterest(getFOI4offering(offering, session));
            }

            // set procedures
            Collection<String> procedures =
                    Configurator.getInstance().getCapsCacheController().getProcedures4Offering(offering);
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
                    getQNamesForResultModel(Configurator.getInstance().getCapsCacheController()
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
            sosOffering.setResponseFormats(Arrays.asList(Sos1Constants.getResponseFormats()));

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
    private List<SosOfferingsForContents> getContentsForSosV2(SosCapabilities sosCapabilities, Session session)
            throws OwsExceptionReport {
        List<SosOfferingsForContents> sosOfferings = new ArrayList<SosOfferingsForContents>();

        Collection<String> offerings = Configurator.getInstance().getCapsCacheController().getOfferings();

        for (String offering : offerings) {
            Collection<String> procedures =
                    Configurator.getInstance().getCapsCacheController().getProcedures4Offering(offering);
            if (procedures == null || procedures.isEmpty()) {
                String exceptionText =
                        "No procedures are contained in the database for the offering: " + offering
                                + "! Please contact the admin of this SOS.";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

            for (String procedure : procedures) {
                SosOfferingsForContents sosOffering = new SosOfferingsForContents();
                if (procedures.size() > 1) {
                    sosOffering.setOffering(offering + SosConstants.SEPARATOR_4_OFFERINGS + procedure);
                    // only if fois are contained for the offering set the
                    // values of the envelope
                    SosEnvelope sosEnvelope = getBBOX4Offering(offering, session);
                    sosOffering.setBoundeBy(sosEnvelope.getEnvelope());
                    sosOffering.setSrid(sosEnvelope.getSrid());
                } else {
                    sosOffering.setOffering(offering);
                    SosEnvelope sosEnvelope = getBBOX4Offering(offering, session);
                    sosOffering.setBoundeBy(sosEnvelope.getEnvelope());
                    sosOffering.setSrid(sosEnvelope.getSrid());
                }

                Collection<String> offProc = new ArrayList<String>();
                offProc.add(procedure);
                sosOffering.setProcedures(offProc);

                // TODO: add intended application
                // xb_oo.addIntendedApplication("");

                // add offering name
                sosOffering.setOfferingName(Configurator.getInstance().getCapsCacheController()
                        .getOfferingName(offering));

                // set up phenomena
                Collection<String> phenomenons = new ArrayList<String>();
                Map<String, Collection<String>> phenProcs =
                        Configurator.getInstance().getCapsCacheController().getKObservablePropertyVProcedures();
                Collection<String> phens4Off =
                        Configurator.getInstance().getCapsCacheController()
                                .getObservablePropertiesForOffering(offering);
                for (String phenID : phens4Off) {
                    if (phenProcs.get(phenID).contains(procedure)) {
                        phenomenons.add(phenID);
                    }
                }
                sosOffering.setObservableProperties(phenomenons);
                sosOffering.setCompositePhenomena(Configurator.getInstance().getCapsCacheController()
                        .getKOfferingVCompositePhenomenons().get(offering));
                Map<String, Collection<String>> phens4CompPhens = new HashMap<String, Collection<String>>();
                if (Configurator.getInstance().getCapsCacheController().getKOfferingVCompositePhenomenons()
                        .get(offering) != null) {
                    for (String compositePhenomenon : Configurator.getInstance().getCapsCacheController()
                            .getKOfferingVCompositePhenomenons().get(offering)) {
                        phens4CompPhens.put(compositePhenomenon, Configurator.getInstance().getCapsCacheController()
                                .getKCompositePhenomenonVObservableProperty().get(compositePhenomenon));
                    }
                }
                sosOffering.setPhens4CompPhens(phens4CompPhens);

                // set up time
                DateTime minDate = HibernateCriteriaQueryUtilities.getMinDate4Offering(offering, session);
                DateTime maxDate = HibernateCriteriaQueryUtilities.getMaxDate4Offering(offering, session);
                sosOffering.setTime(new TimePeriod(minDate, maxDate));

                // add related feature
                Map<String, Collection<String>> relatedFeatures = new HashMap<String, Collection<String>>();
                // // related feature
                if (Configurator.getInstance().getCapsCacheController().getKOfferingVRelatedFeatures() != null
                        && !Configurator.getInstance().getCapsCacheController().getKOfferingVRelatedFeatures()
                                .isEmpty()) {
                    Collection<String> relatedFeatureMap =
                            Configurator.getInstance().getCapsCacheController().getKOfferingVRelatedFeatures()
                                    .get(offering);
                    for (String relatedFeature : relatedFeatureMap) {
                        if (relatedFeature.contains("http") || relatedFeature.contains("HTTP")) {
                            relatedFeatures.put(relatedFeature, Configurator.getInstance().getCapsCacheController()
                                    .getKRelatedFeaturesVRole().get(relatedFeature));
                        } else {
                            String relatedFeatureID =
                                    getRelatedFeatureID(relatedFeature, session, sosCapabilities.getVersion());
                            if (relatedFeatureID != null) {
                                relatedFeatures.put(relatedFeatureID, Configurator.getInstance()
                                        .getCapsCacheController().getKRelatedFeaturesVRole().get(relatedFeature));
                            }
                        }
                    }
                }
                // feature of interest
                else {
                    List<String> role = new ArrayList<String>();
                    role.add("featureOfInterestID");
                    for (String foiID : Configurator.getInstance().getCapsCacheController().getKOfferingVFeatures()
                            .get(offering)) {
                        if (Configurator.getInstance().getCapsCacheController().getProcedures4FeatureOfInterest(foiID)
                                .contains(procedure)) {
                            relatedFeatures.put(foiID, role);
                        }
                    }
                }
                sosOffering.setRelatedFeatures(relatedFeatures);

                // insert observation type and observation result type
                List<String> observationTypes = new ArrayList<String>(1);
                Map<String, Collection<String>> observationResultTypes = new HashMap<String, Collection<String>>(1);
                observationTypes.add(OMConstants.OBS_TYPE_OBSERVATION);
                List<String> resultTypes = new ArrayList<String>(1);
                resultTypes.add(OMConstants.OBS_RESULT_TYPE_OBSERVATION);
                observationResultTypes.put(OMConstants.OBS_TYPE_OBSERVATION, resultTypes);
                sosOffering.setObservationTypes(observationTypes);
                sosOffering.setObservationResultTypes(observationResultTypes);

                // set response format
                sosOffering.setResponseFormats(Arrays.asList(Sos2Constants.getResponseFormats()));

                // set response Mode
                sosOffering.setResponseModes(Arrays.asList(SosConstants.getResponseModes()));

                sosOfferings.add(sosOffering);
            }
        }

        return sosOfferings;
    }

    /**
     * Get extensions
     * 
     * @return Extensions
     */
    private List<Object> getExtensions() {
        // TODO Auto-generated method stub
        return null;
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
    private OWSOperation getOpsGetCapabilities(SosCapabilities sosCapabilities, String service, String version)
            throws OwsExceptionReport {
        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(SosConstants.Operations.GetCapabilities.name());
        // set DCP
        opsMeta.setDcp(SosHelper.getDCP(SosConstants.Operations.GetCapabilities.name(), service, version, Configurator
                .getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set param updateSequence
        List<String> updateSequenceValues = new ArrayList<String>();
        updateSequenceValues.add(SosConstants.PARAMETER_ANY);
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.updateSequence.name(), updateSequenceValues);
        // set param AcceptVersions
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.AcceptVersions.name(), Configurator.getInstance()
                .getSupportedVersions());
        // set param Sections
        List<String> sectionsValues = new ArrayList<String>();
        sectionsValues.add(SosConstants.CapabilitiesSections.ServiceIdentification.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.ServiceProvider.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.OperationsMetadata.name());
        if (sosCapabilities.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
            sectionsValues.add(Sos1Constants.CapabilitiesSections.Filter_Capabilities.name());
        } else if (sosCapabilities.getVersion().equals(Sos2Constants.SERVICEVERSION)) {
            sectionsValues.add(Sos2Constants.CapabilitiesSections.FilterCapabilities.name());
        }
        sectionsValues.add(SosConstants.CapabilitiesSections.Contents.name());
        sectionsValues.add(SosConstants.CapabilitiesSections.All.name());
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.Sections.name(), sectionsValues);
        // set param AcceptFormats
        opsMeta.addParameterValue(SosConstants.GetCapabilitiesParams.AcceptFormats.name(),
                Arrays.asList(SosConstants.getAcceptFormats()));

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
        Map<SpatialOperator, List<QName>> spatialOperators = new HashMap<SpatialOperator, List<QName>>();
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
        Map<TimeOperator, List<QName>> temporalOperators = new HashMap<TimeOperator, List<QName>>();
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
     * Get Envelope for offering
     * 
     * @param offeringID
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return Envelope for offering from FOIs or values
     *         (SpatialFilertingProfile)
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private SosEnvelope getBBOX4Offering(String offeringID, Session session) throws OwsExceptionReport {
        List<String> featureIDs =
                HibernateCriteriaQueryUtilities.getFeatureOfInterestIdentifiersForOffering(offeringID, session);
        session.clear();
        Envelope envelope =
                Configurator.getInstance().getFeatureQueryHandler().getEnvelopeforFeatureIDs(featureIDs, session);
        SosEnvelope sosEnvelope = new SosEnvelope(envelope, Configurator.getInstance().getDefaultEPSG());
        return sosEnvelope;
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
                            SosConstants.Operations.GetFeatureOfInterest.name(), version);
            return SosHelper.createFoiGetUrl(identifier, version, Configurator.getInstance().getServiceURL(),
                    urlPattern);
        }
    }

}