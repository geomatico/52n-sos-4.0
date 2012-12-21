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
import net.opengis.sos.x20.GetResultDocument;
import net.opengis.sos.x20.GetResultResponseDocument;
import net.opengis.sos.x20.GetResultResponseType;
import net.opengis.sos.x20.GetResultTemplateDocument;
import net.opengis.sos.x20.GetResultTemplateResponseDocument;
import net.opengis.sos.x20.GetResultTemplateResponseType;
import net.opengis.sos.x20.GetResultTemplateResponseType.ResultEncoding;
import net.opengis.sos.x20.GetResultTemplateResponseType.ResultStructure;
import net.opengis.sos.x20.GetResultTemplateType;
import net.opengis.sos.x20.GetResultType;
import net.opengis.sos.x20.GetResultType.SpatialFilter;
import net.opengis.sos.x20.InsertObservationResponseDocument;
import net.opengis.sos.x20.InsertResultResponseDocument;
import net.opengis.sos.x20.InsertResultTemplateResponseDocument;
import net.opengis.sos.x20.InsertResultTemplateResponseType;
import net.opengis.sos.x20.InsertionCapabilitiesDocument;
import net.opengis.sos.x20.InsertionCapabilitiesType;
import net.opengis.sos.x20.ObservationOfferingType;
import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.TextEncodingDocument;
import net.opengis.swes.x20.AbstractContentsType.Offering;
import net.opengis.swes.x20.FeatureRelationshipType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.GMLConstants;
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
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.GetCapabilitiesResponse;
import org.n52.sos.response.GetFeatureOfInterestResponse;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.response.GetResultResponse;
import org.n52.sos.response.GetResultTemplateResponse;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.response.InsertResultResponse;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;

public class SosEncoderv20 implements IEncoder<XmlObject, AbstractServiceCommunicationObject> {

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

    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(Sos2Constants.NS_SOS_20, SosConstants.NS_SOS_PREFIX);
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public XmlObject encode(AbstractServiceCommunicationObject communicationObject) throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
        additionalValues.put(HelperValues.VERSION, Sos2Constants.SERVICEVERSION);
        return encode(communicationObject, additionalValues);
    }

    @Override
    public XmlObject encode(AbstractServiceCommunicationObject communicationObject,
            Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (communicationObject instanceof AbstractServiceRequest) {
            return encodeRequests((AbstractServiceRequest) communicationObject);
        } else if (communicationObject instanceof AbstractServiceResponse) {
            return encodeResponse((AbstractServiceResponse) communicationObject);
        }
        return null;
    }

    private XmlObject encodeRequests(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof GetResultTemplateRequest) {
            return createGetResultTemplateRequest((GetResultTemplateRequest) request);
        } else if (request instanceof GetResultRequest) {
            return createGetResultRequest((GetResultRequest) request);
        } else if (request instanceof GetCapabilitiesRequest) {
            return createGetCapabilitiesRequest((GetCapabilitiesRequest) request);
        }
        return null;
    }

    private XmlObject encodeResponse(AbstractServiceResponse response) throws OwsExceptionReport {
        if (response instanceof GetCapabilitiesResponse) {
            return createCapabilitiesDocument((GetCapabilitiesResponse) response);
        } else if (response instanceof GetObservationResponse) {
            return createGetObservationResponseDocument((GetObservationResponse) response);
        } else if (response instanceof GetFeatureOfInterestResponse) {
            return createGetFeatureOfInterestResponse((GetFeatureOfInterestResponse) response);
        } else if (response instanceof GetObservationByIdResponse) {
            return createGetObservationByIdResponse((GetObservationByIdResponse) response);
        } else if (response instanceof InsertObservationResponse) {
            return createInsertObservationResponse((InsertObservationResponse) response);
        } else if (response instanceof InsertResultTemplateResponse) {
            return createInsertResultTemplateResponseDocument((InsertResultTemplateResponse) response);
        } else if (response instanceof InsertResultResponse) {
            return createInsertResultResponseDocument((InsertResultResponse) response);
        } else if (response instanceof GetResultResponse) {
            return createGetResultResponseDocument((GetResultResponse) response);
        } else if (response instanceof GetResultTemplateResponse) {
            return createGetResultTemplateResponseDocument((GetResultTemplateResponse) response);
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
                && !sosCapabilities.getOperationsMetadata().getOperations().isEmpty()) {
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

    private XmlObject createGetObservationResponseDocument(GetObservationResponse response) throws OwsExceptionReport {
        GetObservationResponseDocument xbGetObsRespDoc =
                GetObservationResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetObservationResponseType xbGetObsResp = xbGetObsRespDoc.addNewGetObservationResponse();
        Collection<SosObservation> observationCollection = null;
        IEncoder encoder = Configurator.getInstance().getEncoder(response.getResponseFormat());
        if (encoder == null) {
            String exceptionText = "Error while encoding GetObservation response, missing encoder!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        } else if (encoder instanceof IObservationEncoder) {
            IObservationEncoder iObservationEncoder = (IObservationEncoder) encoder;
            /* TODO uncomment when WaterML support is activated
             * if (iObservationEncoder.shouldObservationsWithSameXBeMerged()) {
                // TODO get List of markable ObsTyps, ...
                observationCollection =
                        response.mergeObservations(
                                iObservationEncoder.shouldObservationsWithSameXBeMerged()
                                );
            } else {*/
                observationCollection = response.getObservationCollection();
            /*}*/
        } else {
            String exceptionText =
                    "Error while encoding GetObservation response, encoder is not of type IObservationEncoder!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
//        HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>();
//        int sfIdCounter = 1;
        for (SosObservation sosObservation : observationCollection) {
//            Map<HelperValues, String> foiHelper = new HashMap<SosConstants.HelperValues, String>();
//            String gmlId = null;
//            if (gmlID4sfIdentifier.containsKey(sosObservation.getObservationConstellation().getFeatureOfInterest()
//                    .getIdentifier())) {
//                gmlId =
//                        gmlID4sfIdentifier.get(sosObservation.getObservationConstellation().getFeatureOfInterest()
//                                .getIdentifier());
//                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(true));
//            } else {
//                gmlId = "sf_" + sfIdCounter++;
//                gmlID4sfIdentifier.put(sosObservation.getObservationConstellation().getFeatureOfInterest()
//                        .getIdentifier(), gmlId);
//                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(false));
//            }
//            foiHelper.put(HelperValues.GMLID, gmlId);

            xbGetObsResp.addNewObservationData().setOMObservation(
                    (OMObservationType) encoder.encode(sosObservation, null));
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
//        int sfIdCounter = 1;
//        HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>();
        GetFeatureOfInterestResponseDocument xbGetFoiResponseDoc =
                GetFeatureOfInterestResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        GetFeatureOfInterestResponseType xbGetFoiResponse = xbGetFoiResponseDoc.addNewGetFeatureOfInterestResponse();
        SosAbstractFeature sosAbstractFeature = response.getAbstractFeature();
        if (sosAbstractFeature instanceof SosFeatureCollection) {
            Map<String, SosAbstractFeature> sosFeatColMap = ((SosFeatureCollection) sosAbstractFeature).getMembers();
            for (String sosFeatID : sosFeatColMap.keySet()) {
                FeaturePropertyType featureProperty = xbGetFoiResponse.addNewFeatureMember();
                SosAbstractFeature feature = sosFeatColMap.get(sosFeatID);
                String identifier = null;
                if (feature.getIdentifier() != null && !feature.getIdentifier().isEmpty()) {
                    identifier = feature.getIdentifier();
                } else {
                    identifier = sosFeatID;
                }
                SosSamplingFeature sampFeat = (SosSamplingFeature) feature;
                if (sampFeat.getUrl() != null) {
                    featureProperty.setHref(sampFeat.getUrl());
                    if (sampFeat.isSetNames()) {
                        featureProperty.setTitle(sampFeat.getFirstName());
                    }
                } else {
                    IEncoder encoder =
                            Configurator.getInstance().getEncoder(
                                    OMHelper.getNamespaceForFeatureType(sampFeat.getFeatureType()));
                    if (encoder != null) {
                        featureProperty.set((XmlObject) encoder.encode(sampFeat));
                    } else {
                        if (sampFeat.getXmlDescription() != null) {
                            try {
                                featureProperty.set(XmlObject.Factory.parse(sampFeat.getXmlDescription()));
                            } catch (XmlException xmle) {
                                String exceptionText = "Error while encoding featureOfInterest in OMObservation!";
                                LOGGER.error(exceptionText, xmle);
                                throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                            }
                        } else {
                            featureProperty.setHref(identifier);
                            if (sampFeat.isSetNames()) {
                                featureProperty.setTitle(sampFeat.getFirstName());
                            }
                        }
                    }
                }
            }
        } else {
            if (sosAbstractFeature instanceof SosSamplingFeature) {
                SosSamplingFeature sampFeat = (SosSamplingFeature) sosAbstractFeature;

                IEncoder encoder = Configurator.getInstance().getEncoder(sampFeat.getFeatureType());
                if (encoder == null) {
                    String exceptionText = "Error while encoding GetFeatureOfInterest response, missing encoder!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
                xbGetFoiResponse.set((XmlObject) encoder.encode(sampFeat));
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

    private XmlObject createInsertResultTemplateResponseDocument(InsertResultTemplateResponse response)
            throws OwsExceptionReport {
        InsertResultTemplateResponseDocument insertResultTemplateResponseDoc =
                InsertResultTemplateResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        InsertResultTemplateResponseType insertResultTemplateResponse =
                insertResultTemplateResponseDoc.addNewInsertResultTemplateResponse();
        insertResultTemplateResponse.setAcceptedTemplate(response.getAcceptedTemplate());
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        N52XmlHelper.setSchemaLocationsToDocument(insertResultTemplateResponseDoc, schemaLocations);
        return insertResultTemplateResponseDoc;
    }

    private XmlObject createInsertResultResponseDocument(InsertResultResponse response) throws OwsExceptionReport {
        InsertResultResponseDocument insertResultTemplateResponseDoc =
                InsertResultResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        insertResultTemplateResponseDoc.addNewInsertResultResponse();
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        N52XmlHelper.setSchemaLocationsToDocument(insertResultTemplateResponseDoc, schemaLocations);
        return insertResultTemplateResponseDoc;
    }

    private XmlObject createGetResultResponseDocument(GetResultResponse response) {
        GetResultResponseDocument getResultResponseDoc =
                GetResultResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetResultResponseType getResultResponse = getResultResponseDoc.addNewGetResultResponse();
        XmlObject resultValues = getResultResponse.addNewResultValues();
        if (response.hasResultValues()) {
            XmlString xmlString = XmlString.Factory.newInstance();
            xmlString.setStringValue(response.getResultValues());
            resultValues.set(xmlString);
        }
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        N52XmlHelper.setSchemaLocationsToDocument(getResultResponseDoc, schemaLocations);
        return getResultResponseDoc;
    }

    private XmlObject createGetResultTemplateResponseDocument(GetResultTemplateResponse response) throws OwsExceptionReport {
        GetResultTemplateResponseDocument getResultTemplateResponseDoc =
                GetResultTemplateResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetResultTemplateResponseType getResultTemplateResponse =
                getResultTemplateResponseDoc.addNewGetResultTemplateResponse();
        getResultTemplateResponse.setResultEncoding(createResultEncoding(response.getResultEncoding()));
        getResultTemplateResponse.setResultStructure(createResultStructure(response.getResultStructure()));
        // set schema location
        List<String> schemaLocations = new ArrayList<String>();
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS200());
        N52XmlHelper.setSchemaLocationsToDocument(getResultTemplateResponseDoc, schemaLocations);
        return getResultTemplateResponseDoc;
    }

    private XmlObject createGetCapabilitiesRequest(GetCapabilitiesRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    private XmlObject createGetResultTemplateRequest(GetResultTemplateRequest request) {
        GetResultTemplateDocument getResultTemplateDoc =
                GetResultTemplateDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetResultTemplateType getResultTemplate = getResultTemplateDoc.addNewGetResultTemplate();
        getResultTemplate.setService(request.getService());
        getResultTemplate.setVersion(request.getVersion());
        getResultTemplate.setOffering(request.getOffering());
        getResultTemplate.setObservedProperty(request.getObservedProperty());
        return getResultTemplateDoc;
    }

    private XmlObject createGetResultRequest(GetResultRequest request) throws OwsExceptionReport {
        GetResultDocument getResultDoc =
                GetResultDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GetResultType getResult = getResultDoc.addNewGetResult();
        getResult.setService(request.getService());
        getResult.setVersion(request.getVersion());
        getResult.setOffering(request.getOffering());
        getResult.setObservedProperty(request.getObservedProperty());
        if (request.hasFeatureOfInterest()) {
            for (String featureOfInterest : request.getFeatureIdentifiers()) {
                getResult.addFeatureOfInterest(featureOfInterest);
            }
        }
        if (request.hasTemporalFilter()) {
            for (TemporalFilter temporalFilter : request.getTemporalFilter()) {
                createTemporalFilter(getResult.addNewTemporalFilter(), temporalFilter);
            }
        }
        if (request.hasSpatialFilter()) {
            createSpatialFilter(getResult.addNewSpatialFilter(), request.getSpatialFilter());
        }

        return getResultDoc;
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
            if (offering.getOfferingName() != null && !offering.getOfferingName().isEmpty()) {
                xbObsOff.addNewName().setStringValue(offering.getOfferingName());
            }
            for (String procedure : offering.getProcedures()) {
                xbObsOff.setProcedure(procedure);
            }
            // TODO: procedureDescriptionFormat [0..*]
            // set observableProperties [0..*]
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
                    IEncoder encoder = Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
                    if (encoder != null) {
                        Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                        XmlObject xmlObject = (XmlObject) encoder.encode(offering.getTime());
                        if (xmlObject instanceof TimePeriodType) {
                            xbObsOff.addNewPhenomenonTime().setTimePeriod((TimePeriodType) xmlObject);
                            xbObsOff.getPhenomenonTime().substitute(
                                    new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_PHENOMENON_TIME,
                                            SosConstants.NS_SOS_PREFIX), xbObsOff.getPhenomenonTime().schemaType());
                            // XmlObject substitution =
                            // timeObjectPropertyType.addNewAbstractTimeObject().substitute(getQnameForITime(iTime),
                            // xmlObject.schemaType());
                            // substitution.set((AbstractTimeObjectType)
                            // xmlObject);
                        } else {
                            // TODO: Exception
                        }
                    } else {
                        String exceptionText = "Error while encoding phenomenon time, needed encoder is missing!";
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                    }
                    // PhenomenonTime xbPhenTime =
                    // xbObsOff.addNewPhenomenonTime();
                    // TimePeriodType xbTime = xbPhenTime.addNewTimePeriod();
                    // xbTime.setId("pt_" + normalize(offering.getOffering()));
                    // xbTime.addNewBeginPosition().setStringValue(
                    // DateTimeHelper.formatDateTime2ResponseString(tp.getStart()));
                    // xbTime.addNewEndPosition().setStringValue(
                    // DateTimeHelper.formatDateTime2ResponseString(tp.getEnd()));
                    // xbObsOff.getPhenomenonTime().substitute(
                    // new QName(Sos2Constants.NS_SOS_20,
                    // Sos2Constants.EN_PHENOMENON_TIME,
                    // SosConstants.NS_SOS_PREFIX),
                    // xbObsOff.getPhenomenonTime().schemaType());
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

            if (offering.getProcedureDescriptionFormat() != null
                    && !offering.getProcedureDescriptionFormat().isEmpty()) {
                for (String procedureDescriptionFormat : offering.getProcedureDescriptionFormat()) {
                    xbObsOff.addProcedureDescriptionFormat(procedureDescriptionFormat);
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
            if (!Configurator.getInstance().reversedAxisOrderRequired(srsID)) {
                lowerCorner.setStringValue(minx + " " + miny);
            } else {
                lowerCorner.setStringValue(miny + " " + minx);
            }

            // set upper corner
            // TODO for full 3D support add maxz to parameter in setStringValue
            if (!Configurator.getInstance().reversedAxisOrderRequired(srsID)) {
                upperCorner.setStringValue(maxx + " " + maxy);
            } else {
                upperCorner.setStringValue(maxy + " " + maxx);
            }
            // set SRS
            envelopeType.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + srsID);
        }

        return envelopeType;
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
                cursor.toParent();
            }
            if (cursor.toChild(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_FEATURE_OF_INTEREST_TYPE))) {
                cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_FEATURE_OF_INTEREST_TYPE,
                        SosConstants.NS_SOS_PREFIX));
                while (cursor.toNextSibling(new QName(Sos2Constants.NS_SOS_20,
                        Sos2Constants.EN_FEATURE_OF_INTEREST_TYPE))) {
                    cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_FEATURE_OF_INTEREST_TYPE,
                            SosConstants.NS_SOS_PREFIX));
                }
            }
            cursor.dispose();
        }
    }

    private void createTemporalFilter(net.opengis.sos.x20.GetResultType.TemporalFilter temporalFilter,
            TemporalFilter sosTemporalFilter) throws OwsExceptionReport {
        IEncoder encoder = Configurator.getInstance().getEncoder(FilterConstants.NS_FES_2);
        XmlObject encodedObject = (XmlObject) encoder.encode(sosTemporalFilter);
        temporalFilter.set(encodedObject);
    }

    private void createSpatialFilter(SpatialFilter spatialFilter, org.n52.sos.ogc.filter.SpatialFilter sosSpatialFilter)
            throws OwsExceptionReport {
        IEncoder encoder = Configurator.getInstance().getEncoder(FilterConstants.NS_FES_2);
        XmlObject encodedObject = (XmlObject) encoder.encode(sosSpatialFilter);
        spatialFilter.set(encodedObject);
    }

    private ResultEncoding createResultEncoding(SosResultEncoding sosResultEncoding) throws OwsExceptionReport {
        ResultEncoding resultEncoding =
                ResultEncoding.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // TODO move encoding to SWECommonEncoder
        if (sosResultEncoding.getXml() != null && !sosResultEncoding.getXml().isEmpty()) {
            try {
                TextEncodingDocument textEncodingDoc = (TextEncodingDocument) XmlObject.Factory.parse(sosResultEncoding.getXml());
                resultEncoding.addNewAbstractEncoding().set(textEncodingDoc.getTextEncoding());
                XmlHelper.substituteElement(resultEncoding.getAbstractEncoding(), textEncodingDoc.getTextEncoding());
            } catch (XmlException e) {
                String exceptionText = "ResultEncoding element encoding is not supported!";
               throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }
        } else {
            IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE_20);
            if (encoder == null) {
                String exceptionText = "Missing encoder for ResultEncoding!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            Object encodedObject = encoder.encode(sosResultEncoding.getEncoding());
            if (encodedObject instanceof XmlObject) {
                TextEncodingDocument textEncodingDoc = (TextEncodingDocument) encodedObject;
                resultEncoding.addNewAbstractEncoding().set(textEncodingDoc.getTextEncoding());
                XmlHelper.substituteElement(resultEncoding.getAbstractEncoding(), textEncodingDoc.getTextEncoding());
            } else {
                String exceptionText = "ResultEncoding element encoding is not supported!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        }
        return resultEncoding;
    }

    private ResultStructure createResultStructure(SosResultStructure sosResultStructure) throws OwsExceptionReport {
        ResultStructure resultStructure =
                ResultStructure.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // TODO move encoding to SWECommonEncoder
        if (sosResultStructure.getXml() != null && !sosResultStructure.getXml().isEmpty()) {
            try {
                DataRecordDocument dataRecordDoc = (DataRecordDocument) XmlObject.Factory.parse(sosResultStructure.getXml());
                resultStructure.addNewAbstractDataComponent().set(dataRecordDoc.getDataRecord());
                XmlHelper.substituteElement(resultStructure.getAbstractDataComponent(), dataRecordDoc.getDataRecord());
            } catch (XmlException e) {
                String exceptionText = "ResultStructure element encoding is not supported!";
               throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }
        } else {
            IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE_20);
            if (encoder == null) {
                String exceptionText = "Missing encoder for ResultStructure!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            Object encodedObject = encoder.encode(sosResultStructure.getResultStructure());
            if (encodedObject instanceof XmlObject) {
                DataRecordDocument dataRecordDoc = (DataRecordDocument) encodedObject;
                resultStructure.addNewAbstractDataComponent().set(dataRecordDoc.getDataRecord());
                XmlHelper.substituteElement(resultStructure.getAbstractDataComponent(), dataRecordDoc.getDataRecord());
            } else {
                String exceptionText = "ResultStructure element encoding is not supported!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        }
        return resultStructure;
    }

}
