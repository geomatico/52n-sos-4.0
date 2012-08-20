package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.fes.x20.FilterCapabilitiesDocument;
import net.opengis.fes.x20.FilterCapabilitiesDocument.FilterCapabilities;
import net.opengis.gml.x32.AbstractFeatureType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.ows.x11.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.x11.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.x11.ServiceProviderDocument.ServiceProvider;
import net.opengis.sos.x20.CapabilitiesDocument;
import net.opengis.sos.x20.CapabilitiesType;
import net.opengis.sos.x20.CapabilitiesType.Contents;
import net.opengis.sos.x20.ContentsType;
import net.opengis.sos.x20.GetFeatureOfInterestResponseDocument;
import net.opengis.sos.x20.GetFeatureOfInterestResponseType;
import net.opengis.sos.x20.GetObservationByIdResponseDocument;
import net.opengis.sos.x20.GetObservationByIdResponseType;
import net.opengis.sos.x20.GetObservationResponseDocument;
import net.opengis.sos.x20.GetObservationResponseType;
import net.opengis.sos.x20.InsertObservationResponseDocument;
import net.opengis.sos.x20.InsertionCapabilitiesDocument;
import net.opengis.sos.x20.InsertionCapabilitiesType;
import net.opengis.sos.x20.ObservationOfferingType;
import net.opengis.sos.x20.ObservationOfferingType.PhenomenonTime;
import net.opengis.swes.x20.AbstractContentsType.Offering;
import net.opengis.swes.x20.FeatureRelationshipType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosCapabilities;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosInsertionCapabilities;
import org.n52.sos.ogc.sos.SosOfferingsForContents;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class SosEncoderv20 implements IEncoder<XmlObject, AbstractServiceResponse> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public SosEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(Sos2Constants.NS_SOS_20));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }

    @Override
    public XmlObject encode(AbstractServiceResponse response) throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
        additionalValues.put(HelperValues.VERSION, Sos2Constants.SERVICEVERSION);
        return encode(response, additionalValues);
    }

    @Override
    public XmlObject encode(AbstractServiceResponse response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (response instanceof GetCapabilitiesResponse) {
            return createCapabilitiesDocument((GetCapabilitiesResponse) response);
        } else if (response instanceof GetObservationResponse) {
            return createObservationResponseDocument((GetObservationResponse) response);
        } else if (response instanceof GetFeatureOfInterestResponse) {
            return createGetFeatureOfInterestResponse((GetFeatureOfInterestResponse) response);
        } else if (response instanceof GetObservationByIdResponse) {
            return createGetObservationByIdResponse((GetObservationByIdResponse) response);
        } else if (response instanceof InsertObservationResponse) {
            return createInsertObservationResponse((InsertObservationResponse) response);
        }
        return null;
    }

    private XmlObject createCapabilitiesDocument(GetCapabilitiesResponse response) throws OwsExceptionReport {
        CapabilitiesDocument xbCapsDoc =
                CapabilitiesDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // cursor for getting prefixes
        CapabilitiesType xbCaps = xbCapsDoc.addNewCapabilities();

        // set version.
        xbCaps.setVersion(response.getVersion());

        SosCapabilities sosCapabilities = response.getCapabilities();

        IEncoder owsEncoder = Configurator.getInstance().getEncoder(OWSConstants.NS_OWS);
        if (owsEncoder == null) {
            String exceptionText = "Error while encoding GetCapabilities response, missing encoder!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

        if (sosCapabilities.getServiceIdentification() != null) {
            xbCaps.setServiceIdentification((ServiceIdentification) owsEncoder.encode(sosCapabilities
                    .getServiceIdentification()));
        }
        if (sosCapabilities.getServiceProvider() != null) {
            xbCaps.setServiceProvider((ServiceProvider) owsEncoder.encode(sosCapabilities.getServiceProvider()));

        }
        if (sosCapabilities.getOperationsMetadata() != null
                && sosCapabilities.getOperationsMetadata().getOperations() != null
                && sosCapabilities.getOperationsMetadata().getOperations().isEmpty()) {
            xbCaps.setOperationsMetadata((OperationsMetadata) owsEncoder.encode(sosCapabilities
                    .getOperationsMetadata()));
        }
        if (sosCapabilities.getFilterCapabilities() != null) {
            IEncoder filterEncoder =
                    Configurator.getInstance().getEncoder(
                            XmlHelper.getNamespace(FilterCapabilitiesDocument.Factory.newInstance()
                                    .addNewFilterCapabilities()));
            if (filterEncoder == null) {
                String exceptionText = "Error while encoding GetCapabilities response, missing encoder!";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            xbCaps.addNewFilterCapabilities().setFilterCapabilities(
                    (FilterCapabilities) filterEncoder.encode(sosCapabilities.getFilterCapabilities()));
        }
        if (sosCapabilities.getContents() != null && !sosCapabilities.getContents().isEmpty()) {
            setContents(xbCaps.addNewContents(), sosCapabilities.getContents(), response.getVersion());
        }

        if (sosCapabilities.getExtensions() != null && !sosCapabilities.getExtensions().isEmpty()) {
            for (IExtension extension : sosCapabilities.getExtensions()) {
                setExensions(xbCaps.addNewExtension(), extension);
            }

        }

        N52XmlHelper.setSchemaLocationToDocument(xbCapsDoc, N52XmlHelper.getSchemaLocationForSOS200());

        return xbCapsDoc;
    }

    private XmlObject createObservationResponseDocument(GetObservationResponse response) throws OwsExceptionReport {
        // GetObservationResponseDocument xbGetObsRespDoc =
        // GetObservationResponseDocument.Factory.newInstance(SosXmlOptionsUtility.getInstance()
        // .getXmlOptions4Sos2Swe200());
        GetObservationResponseDocument xbGetObsRespDoc =
                GetObservationResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetObservationResponseType xbGetObsResp = xbGetObsRespDoc.addNewGetObservationResponse();
        List<SosObservation> observationCollection = response.getObservationCollection();
        IEncoder encoder = Configurator.getInstance().getEncoder(response.getResponseFormat());
        if (encoder == null) {
            String exceptionText = "Error while encoding GetObservation response, missing encoder!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>();
        int sfIdCounter = 1;
        for (SosObservation sosObservation : observationCollection) {
            Map<HelperValues, String> foiHelper = new HashMap<SosConstants.HelperValues, String>();
            String gmlId = null;
            if (gmlID4sfIdentifier.containsKey(sosObservation.getObservationConstellation().getFeatureOfInterest()
                    .getIdentifier())) {
                gmlId =
                        gmlID4sfIdentifier.get(sosObservation.getObservationConstellation().getFeatureOfInterest()
                                .getIdentifier());
                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(true));
            } else {
                gmlId = "sf_" + sfIdCounter;
                gmlID4sfIdentifier.put(sosObservation.getObservationConstellation().getFeatureOfInterest()
                        .getIdentifier(), gmlId);
                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(false));
            }
            foiHelper.put(HelperValues.GMLID, gmlId);

            xbGetObsResp.addNewObservationData().setOMObservation(
                    (OMObservationType) encoder.encode(sosObservation, foiHelper));
        }
        // set schema location
        XmlHelper.makeGmlIdsUnique(xbGetObsRespDoc.getDomNode());
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForOM200());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSF200());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSAMS200());
        N52XmlHelper.setSchemaLocationsToDocument(xbGetObsRespDoc, schemaLocations);
        return xbGetObsRespDoc;
    }

    private XmlObject createGetFeatureOfInterestResponse(GetFeatureOfInterestResponse response)
            throws OwsExceptionReport {
        int sfIdCounter = 1;
        HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>();
        GetFeatureOfInterestResponseDocument xbGetFoiResponseDoc =
                GetFeatureOfInterestResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        GetFeatureOfInterestResponseType xbGetFoiResponse = xbGetFoiResponseDoc.addNewGetFeatureOfInterestResponse();
        SosAbstractFeature sosAbstractFeature = response.getAbstractFeature();
        if (sosAbstractFeature instanceof SosFeatureCollection) {
            Map<String, SosAbstractFeature> sosFeatColMap = ((SosFeatureCollection) sosAbstractFeature).getMembers();
            for (String sosFeatID : sosFeatColMap.keySet()) {
                FeaturePropertyType xbFeatMember = xbGetFoiResponse.addNewFeatureMember();
                SosAbstractFeature feature = sosFeatColMap.get(sosFeatID);
                String identifier = null;
                if (feature.getIdentifier() != null && !feature.getIdentifier().isEmpty()) {
                    identifier = feature.getIdentifier();
                } else {
                    identifier = sosFeatID;
                }
                if (gmlID4sfIdentifier.containsKey(identifier)) {
                    xbFeatMember.setHref("#" + gmlID4sfIdentifier.get(identifier));
                } else {
                    String gmlId = "sf_" + sfIdCounter++;
                    if (feature instanceof SosSamplingFeature) {
                        SosSamplingFeature sampFeat = (SosSamplingFeature) feature;
                        if (sampFeat.getFeatureType() != null
                                && !sampFeat.getFeatureType().equalsIgnoreCase(OGCConstants.UNKNOWN)) {
                            IEncoder encoder =
                                    Configurator.getInstance().getEncoder(
                                            OMHelper.getNamespaceForFeatureType(sampFeat.getFeatureType()));
                            if (encoder == null) {
                                String exceptionText =
                                        "Error while encoding GetFeatureOfInterest response, missing encoder!";
                                LOGGER.debug(exceptionText);
                                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                            }
                            Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                            additionalValues.put(HelperValues.GMLID, gmlId);
                            xbFeatMember.set((XmlObject) encoder.encode(sampFeat, additionalValues));
                            gmlID4sfIdentifier.put(identifier, gmlId);
                        } else if (sampFeat.getXmlDescription() != null) {
                            try {
                                xbFeatMember.setAbstractFeature((AbstractFeatureType) XmlObject.Factory.parse(sampFeat
                                        .getXmlDescription()));
                                gmlID4sfIdentifier.put(identifier, gmlId);
                            } catch (XmlException xmle) {
                                String exceptionText =
                                        "Error while encoding GetFeatureOfInterest response, invalid samplingFeature description!";
                                LOGGER.debug(exceptionText, xmle);
                                throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                            }
                        } else if (sampFeat.getUrl() != null) {
                            xbFeatMember.setHref(identifier);
                            gmlID4sfIdentifier.put(identifier, gmlId);
                        } else {
                            xbFeatMember.setHref(sampFeat.getIdentifier());
                            gmlID4sfIdentifier.put(identifier, gmlId);
                        }

                    }
                }
            }
        } else {
            if (sosAbstractFeature instanceof SosSamplingFeature) {
                SosSamplingFeature sampFeat = (SosSamplingFeature) sosAbstractFeature;

                String gmlId = "sf_" + sfIdCounter;
                IEncoder encoder = Configurator.getInstance().getEncoder(sampFeat.getFeatureType());
                if (encoder == null) {
                    String exceptionText = "Error while encoding GetFeatureOfInterest response, missing encoder!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
                Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                additionalValues.put(HelperValues.GMLID, gmlId);
                xbGetFoiResponse.set((XmlObject) encoder.encode(sampFeat, additionalValues));
            }
        }
        // set schemLocation
        StringBuilder schemaLocation = new StringBuilder();
        schemaLocation.append(N52XmlHelper.getSchemaLocationForSOS200());
        schemaLocation.append(" ");
        schemaLocation.append(N52XmlHelper.getSchemaLocationForSAMS200());
        N52XmlHelper.setSchemaLocationToDocument(xbGetFoiResponseDoc, schemaLocation.toString());
        return xbGetFoiResponseDoc;
    }

    private XmlObject createGetObservationByIdResponse(GetObservationByIdResponse response) throws OwsExceptionReport {
        GetObservationByIdResponseDocument xbGetObsByIdRespDoc =
                GetObservationByIdResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetObservationByIdResponseType xbGetObsByIdResp = xbGetObsByIdRespDoc.addNewGetObservationByIdResponse();
        List<SosObservation> observationCollection = response.getObservationCollection();
        IEncoder encoder = Configurator.getInstance().getEncoder(response.getResponseFormat());
        if (encoder == null) {
            String exceptionText = "Error while encoding GetObservationById response, missing encoder!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>();
        int sfIdCounter = 1;
        for (SosObservation sosObservation : observationCollection) {
            Map<HelperValues, String> foiHelper = new HashMap<SosConstants.HelperValues, String>();
            String gmlId = null;
            if (gmlID4sfIdentifier.containsKey(sosObservation.getObservationConstellation().getFeatureOfInterest()
                    .getIdentifier())) {
                gmlId =
                        gmlID4sfIdentifier.get(sosObservation.getObservationConstellation().getFeatureOfInterest()
                                .getIdentifier());
                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(true));
            } else {
                gmlId = "sf_" + sfIdCounter;
                gmlID4sfIdentifier.put(sosObservation.getObservationConstellation().getFeatureOfInterest()
                        .getIdentifier(), gmlId);
                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(false));
            }
            foiHelper.put(HelperValues.GMLID, gmlId);

            xbGetObsByIdResp.addNewObservation().setOMObservation(
                    (OMObservationType) encoder.encode(sosObservation, foiHelper));
        }
        XmlHelper.makeGmlIdsUnique(xbGetObsByIdResp.getDomNode());
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForOM200());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSF200());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSAMS200());
        N52XmlHelper.setSchemaLocationsToDocument(xbGetObsByIdResp, schemaLocations);
        xbGetObsByIdResp.addNewObservation().addNewOMObservation();
        return xbGetObsByIdRespDoc;
    }

    private XmlObject createInsertObservationResponse(InsertObservationResponse response) {
        InsertObservationResponseDocument xbInsObsRespDoc =
                InsertObservationResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbInsObsRespDoc.addNewInsertObservationResponse();
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        N52XmlHelper.setSchemaLocationsToDocument(xbInsObsRespDoc, schemaLocations);
        return xbInsObsRespDoc;
    }

    /**
     * Sets the content section to the Capabilities document.
     * 
     * @param xbContents
     *            SOS 2.0 contents section
     * @param offerings
     *            SOS offerings for contents
     * @param version
     *            SOS response version
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private void setContents(Contents xbContents, Collection<SosOfferingsForContents> offerings, String version)
            throws OwsExceptionReport {
        ContentsType xbContType = xbContents.addNewContents();
        for (SosOfferingsForContents offering : offerings) {
            ObservationOfferingType xbObsOff =
                    ObservationOfferingType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbObsOff.setIdentifier(offering.getOffering());
            for (String procedure : offering.getProcedures()) {
                xbObsOff.setProcedure(procedure);
            }
            // TODO: procedureDescriptionFormat [0..*]
            // set phenomenons [0..*]
            for (String phenomenon : offering.getObservableProperties()) {
                xbObsOff.addObservableProperty(phenomenon);
            }

            // set relatedFeatures [0..*]
            if (offering.getRelatedFeatures() != null) {
                for (String relatedFeatureTarget : offering.getRelatedFeatures().keySet()) {
                    createRelatedFeature(xbObsOff.addNewRelatedFeature().addNewFeatureRelationship(),
                            relatedFeatureTarget, offering.getRelatedFeatures().get(relatedFeatureTarget));
                }
            }

            // set observed area [0..1]
            if (offering.getObservedArea() != null) {
                if (offering.getObservedArea().getEnvelope() != null && offering.getObservedArea().getSrid() != -1) {
                    xbObsOff.addNewObservedArea().setEnvelope(
                            getBBOX4Offering(offering.getObservedArea().getEnvelope(), offering.getObservedArea()
                                    .getSrid()));
                }
            }

            // set up phenomenon time [0..1]
            if (offering.getTime() instanceof TimePeriod) {
                TimePeriod tp = (TimePeriod) offering.getTime();
                if (tp.getStart() != null && tp.getEnd() != null) {
                    PhenomenonTime xbPhenTime = xbObsOff.addNewPhenomenonTime();
                    TimePeriodType xbTime = xbPhenTime.addNewTimePeriod();
                    xbTime.setId("pt_" + normalize(offering.getOffering()));
                    xbTime.addNewBeginPosition().setStringValue(
                            DateTimeHelper.formatDateTime2ResponseString(tp.getStart()));
                    xbTime.addNewEndPosition().setStringValue(
                            DateTimeHelper.formatDateTime2ResponseString(tp.getEnd()));
                    xbObsOff.getPhenomenonTime().substitute(
                            new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_PHENOMENON_TIME,
                                    SosConstants.NS_SOS_PREFIX), xbObsOff.getPhenomenonTime().schemaType());
                }
            }

            // set resultTime [0..1]

            // set responseFormat [0..*]
            if (offering.getResponseFormats() != null) {
                for (String responseFormat : offering.getResponseFormats()) {
                    xbObsOff.addResponseFormat(responseFormat);
                }
            }

            // set observationType [0..*]
            if (offering.getObservationTypes() != null) {
                for (String obsType : offering.getObservationTypes()) {
                    xbObsOff.addObservationType(obsType);
                }
            }

            // set featureOfInterestType [0..1]
            if (offering.getFeatureOfInterestTypes() != null) {
                for (String featureOfInterestType : offering.getFeatureOfInterestTypes()) {
                    xbObsOff.addFeatureOfInterestType(featureOfInterestType);
                }
            }

            xbContType.addNewOffering().setAbstractOffering(xbObsOff);
        }
        // FIXME: change swes:AbstractOffering to sos:ObservationOffering and
        // the namespace prefix ns to sos due to
        // XMLBeans problems with substitution
        // (http://www.mail-archive.com/dev%40xmlbeans.apache.org/msg00962.html).
        renameContentsElementNames(xbContents);
    }

    /**
     * Creates a XML FeatureRelationship for the relatedFeature
     * 
     * @param relatedFeature
     *            XML feature relationship
     * @param map
     *            Feature id
     * @param role
     *            Features role
     */
    private void createRelatedFeature(FeatureRelationshipType featureRelationchip, String relatedFeatureTarget,
            Collection<String> roles) {
        featureRelationchip.addNewTarget().setHref(relatedFeatureTarget);
        if (roles != null) {
            for (String role : roles) {
                featureRelationchip.setRole(role);
            }
        }
    }

    private void setExensions(XmlObject addNewExtension, IExtension extension) {
        if (extension instanceof SosInsertionCapabilities) {
            addNewExtension.set(createInsertionCapabilities((SosInsertionCapabilities) extension));
        } else {
            // TODO: not supported
        }
    }

    private XmlObject createInsertionCapabilities(SosInsertionCapabilities sosInsertionCapabilities) {
        InsertionCapabilitiesDocument insertionCapabilitiesDoc = InsertionCapabilitiesDocument.Factory.newInstance();
        // InsertionCapabilitiesType insertionCapabilities =
        // InsertionCapabilitiesType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        InsertionCapabilitiesType insertionCapabilities = insertionCapabilitiesDoc.addNewInsertionCapabilities();
        for (String featureOfInterestType : sosInsertionCapabilities.getFeatureOfInterestTypes()) {
            if (!featureOfInterestType.equals(SosConstants.NOT_DEFINED)) {
                insertionCapabilities.addFeatureOfInterestType(featureOfInterestType);
            }
        }
        for (String observationType : sosInsertionCapabilities.getObservationTypes()) {
            if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                insertionCapabilities.addObservationType(observationType);
            }
        }
        for (String procedureDescriptionFormat : sosInsertionCapabilities.getProcedureDescriptionFormats()) {
            if (!procedureDescriptionFormat.equals(SosConstants.NOT_DEFINED)) {
                insertionCapabilities.addProcedureDescriptionFormat(procedureDescriptionFormat);
            }
        }
        if (sosInsertionCapabilities.getSupportedEncodings() != null) {
            for (String supportedEncoding : sosInsertionCapabilities.getSupportedEncodings()) {
                if (!supportedEncoding.equals(SosConstants.NOT_DEFINED)) {
                    insertionCapabilities.addSupportedEncoding(supportedEncoding);
                }
            }
        }
        // return insertionCapabilities;
        return insertionCapabilitiesDoc;
    }

    /**
     * queries the bounding box of all requested feature of interest IDs
     * 
     * @param envelope
     * 
     * @param foiIDs
     *            ArrayList with String[]s containing the ids of the feature of
     *            interests for which the BBOX should be returned
     * @return Returns EnvelopeType XmlBean which represents the BBOX of the
     *         requested feature of interests
     * @throws OwsExceptionReport
     *             if query of the BBOX failed
     */
    private EnvelopeType getBBOX4Offering(Envelope envelope, int srsID) throws OwsExceptionReport {

        double minx = envelope.getMinX();
        double maxx = envelope.getMaxX();
        double miny = envelope.getMinY();
        double maxy = envelope.getMaxY();
        @SuppressWarnings("unused")
        String minz = null;
        @SuppressWarnings("unused")
        String maxz = null;

        EnvelopeType envelopeType = EnvelopeType.Factory.newInstance();

        // set lower corner
        // TODO for full 3D support add minz to parameter in setStringValue
        DirectPositionType lowerCorner = envelopeType.addNewLowerCorner();
        DirectPositionType upperCorner = envelopeType.addNewUpperCorner();
        if (srsID > 0) {
            if (!Configurator.getInstance().switchCoordinatesForEPSG(srsID)) {
                lowerCorner.setStringValue(minx + " " + miny);
            } else {
                lowerCorner.setStringValue(miny + " " + minx);
            }

            // set upper corner
            // TODO for full 3D support add maxz to parameter in setStringValue
            if (!Configurator.getInstance().switchCoordinatesForEPSG(srsID)) {
                upperCorner.setStringValue(maxx + " " + maxy);
            } else {
                upperCorner.setStringValue(maxy + " " + maxx);
            }
            // set SRS
            envelopeType.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + srsID);
        }

        return envelopeType;
    }

    /**
     * @return a normalized String for use in a file path, i.e. all
     *         [\,/,:,*,?,",<,>,;] characters are replaced by '_'.
     */
    private String normalize(String toNormalize) {
        // toNormalize = toNormalize.replaceAll("ä", "ae");
        // toNormalize = toNormalize.replaceAll("ö", "oe");
        // toNormalize = toNormalize.replaceAll("ü", "ue");
        // toNormalize = toNormalize.replaceAll("Ä", "AE");
        // toNormalize = toNormalize.replaceAll("Ö", "OE");
        // toNormalize = toNormalize.replaceAll("Ü", "UE");
        // toNormalize = toNormalize.replaceAll("ß", "ss");
        return toNormalize.replaceAll("[\\\\,/,:,\\*,?,\",<,>,;,#,%,=,@]", "_");
    }

    private void renameContentsElementNames(Contents xbContents) {
        for (Offering offering : xbContents.getContents().getOfferingArray()) {
            XmlCursor cursor = offering.getAbstractOffering().newCursor();
            cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVATION_OFFERING,
                    SosConstants.NS_SOS_PREFIX));
            cursor.removeAttribute(new QName(W3CConstants.NS_XSI, "type"));
            if (cursor.toChild(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVED_AREA))) {
                cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVED_AREA,
                        SosConstants.NS_SOS_PREFIX));
                cursor.toParent();
            }
            if (cursor.toChild(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_PHENOMENON_TIME))) {
                cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_PHENOMENON_TIME,
                        SosConstants.NS_SOS_PREFIX));
                cursor.toParent();
            }
            if (cursor.toChild(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESPONSE_FORMAT))) {
                cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESPONSE_FORMAT,
                        SosConstants.NS_SOS_PREFIX));
                while (cursor.toNextSibling(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESPONSE_FORMAT))) {
                    cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESPONSE_FORMAT,
                            SosConstants.NS_SOS_PREFIX));
                }
                cursor.toParent();
            }
            if (cursor.toChild(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVATION_TYPE))) {
                cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVATION_TYPE,
                        SosConstants.NS_SOS_PREFIX));
                while (cursor.toNextSibling(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVATION_TYPE))) {
                    cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_OBSERVATION_TYPE,
                            SosConstants.NS_SOS_PREFIX));
                }
            }
            cursor.dispose();
        }
    }

}
