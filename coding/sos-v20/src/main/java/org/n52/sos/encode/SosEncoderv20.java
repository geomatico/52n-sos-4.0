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
package org.n52.sos.encode;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

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
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.OptionNotSupportedException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.FeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SwesExtension;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosCapabilities;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosInsertionCapabilities;
import org.n52.sos.ogc.sos.SosObservationOffering;
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
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.profile.Profile;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.NcNameResolver;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosEncoderv20 implements Encoder<XmlObject, AbstractServiceCommunicationObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosEncoderv20.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(Sos2Constants.NS_SOS_20,
            AbstractServiceResponse.class, GetResultTemplateResponse.class, GetResultResponse.class,
            InsertResultResponse.class, InsertResultTemplateResponse.class, InsertObservationResponse.class,
            GetObservationByIdResponse.class, GetFeatureOfInterestResponse.class, GetObservationResponse.class,
            GetCapabilitiesResponse.class, AbstractServiceRequest.class, GetCapabilitiesRequest.class,
            GetResultTemplateRequest.class, GetResultRequest.class);

    public SosEncoderv20() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(final Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(Sos2Constants.NS_SOS_20, SosConstants.NS_SOS_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.set(Sos2Constants.SOS_SCHEMA_LOCATION);
    }

    @Override
    public XmlObject encode(final AbstractServiceCommunicationObject communicationObject) throws OwsExceptionReport {
        final Map<HelperValues, String> additionalValues = new EnumMap<HelperValues, String>(HelperValues.class);
        additionalValues.put(HelperValues.VERSION, Sos2Constants.SERVICEVERSION);
        return encode(communicationObject, additionalValues);
    }

    @Override
    public XmlObject encode(final AbstractServiceCommunicationObject communicationObject,
            final Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        XmlObject encodedObject = null;
        if (communicationObject instanceof AbstractServiceRequest) {
            encodedObject = encodeRequests((AbstractServiceRequest) communicationObject);
        } else if (communicationObject instanceof AbstractServiceResponse) {
            encodedObject = encodeResponse((AbstractServiceResponse) communicationObject);
        } else {
            throw new UnsupportedEncoderInputException(this, communicationObject);
        }
        LOGGER.debug("Encoded object {} is valid: {}", encodedObject.schemaType().toString(),
                XmlHelper.validateDocument(encodedObject));
        return encodedObject;
    }

    private XmlObject encodeRequests(final AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof GetResultTemplateRequest) {
            return createGetResultTemplateRequest((GetResultTemplateRequest) request);
        } else if (request instanceof GetResultRequest) {
            return createGetResultRequest((GetResultRequest) request);
        } else if (request instanceof GetCapabilitiesRequest) {
            return createGetCapabilitiesRequest((GetCapabilitiesRequest) request);
        }
        throw new UnsupportedEncoderInputException(this, request);
    }

    private XmlObject encodeResponse(final AbstractServiceResponse response) throws OwsExceptionReport {
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
        throw new UnsupportedEncoderInputException(this, response);
    }

    private XmlObject createCapabilitiesDocument(final GetCapabilitiesResponse response) throws OwsExceptionReport {
        final CapabilitiesDocument xbCapsDoc =
                CapabilitiesDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // cursor for getting prefixes
        final CapabilitiesType xbCaps = xbCapsDoc.addNewCapabilities();

        // set version.
        xbCaps.setVersion(response.getVersion());

        final SosCapabilities sosCapabilities = response.getCapabilities();

        if (sosCapabilities.getServiceIdentification() != null) {
            xbCaps.addNewServiceIdentification().set(
                    CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, sosCapabilities.getServiceIdentification()));
        }
        if (sosCapabilities.getServiceProvider() != null) {
            xbCaps.addNewServiceProvider().set(
                    CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, sosCapabilities.getServiceProvider()));

        }
        if (sosCapabilities.getOperationsMetadata() != null
                && sosCapabilities.getOperationsMetadata().getOperations() != null
                && !sosCapabilities.getOperationsMetadata().getOperations().isEmpty()) {

            xbCaps.addNewOperationsMetadata().set(
                    CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, sosCapabilities.getOperationsMetadata()));
        }
        if (sosCapabilities.getFilterCapabilities() != null) {
            xbCaps.addNewFilterCapabilities()
                    .addNewFilterCapabilities()
                    .set(CodingHelper.encodeObjectToXml(FilterConstants.NS_FES_2,
                            sosCapabilities.getFilterCapabilities()));
        }
        if (sosCapabilities.isSetContents()) {
            setContents(xbCaps.addNewContents(), sosCapabilities.getContents(), response.getVersion());
        }

        if (sosCapabilities.getExtensions() != null && !sosCapabilities.getExtensions().isEmpty()) {
            for (final SwesExtension extension : sosCapabilities.getExtensions()) {
                setExensions(xbCaps.addNewExtension(), extension);
            }
        }
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_GET_CAPABILITIES_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbCapsDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbCapsDoc, schemaLocations);
        return xbCapsDoc;
    }

    private XmlObject createGetObservationResponseDocument(final GetObservationResponse response)
            throws OwsExceptionReport {
        final GetObservationResponseDocument xbGetObsRespDoc =
                GetObservationResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final GetObservationResponseType xbGetObsResp = xbGetObsRespDoc.addNewGetObservationResponse();
        final Encoder<XmlObject, OmObservation> encoder =
                CodingHelper.getEncoder(response.getResponseFormat(), new OmObservation());
        if (!(encoder instanceof ObservationEncoder)) {
            throw new NoApplicableCodeException()
                    .withMessage("Error while encoding GetObservation response, encoder is not of type ObservationEncoder!");
        }
        final ObservationEncoder<XmlObject, OmObservation> iObservationEncoder =
                (ObservationEncoder<XmlObject, OmObservation>) encoder;
        if (iObservationEncoder.shouldObservationsWithSameXBeMerged()) {
            response.mergeObservationsWithSameX();
        }
        for (final OmObservation sosObservation : response.getObservationCollection()) {
            xbGetObsResp.addNewObservationData().addNewOMObservation().set(encoder.encode(sosObservation, null));
        }
        // set schema location
        XmlHelper.makeGmlIdsUnique(xbGetObsRespDoc.getDomNode());
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_GET_OBSERVATION_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbGetObsRespDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbGetObsRespDoc, schemaLocations);
        return xbGetObsRespDoc;
    }

    private XmlObject createGetFeatureOfInterestResponse(final GetFeatureOfInterestResponse response)
            throws OwsExceptionReport {
        final GetFeatureOfInterestResponseDocument xbGetFoiResponseDoc =
                GetFeatureOfInterestResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        final GetFeatureOfInterestResponseType xbGetFoiResponse =
                xbGetFoiResponseDoc.addNewGetFeatureOfInterestResponse();
        final AbstractFeature sosAbstractFeature = response.getAbstractFeature();
        if (sosAbstractFeature instanceof FeatureCollection && ((FeatureCollection) sosAbstractFeature).isSetMembers()) {
            for (final AbstractFeature feature : ((FeatureCollection) sosAbstractFeature).getMembers().values()) {
                addFeatureOfInterestGetFeatureOfInterestResponse(feature, xbGetFoiResponse);
            }
        } else {
            if (sosAbstractFeature instanceof SamplingFeature) {
                addFeatureOfInterestGetFeatureOfInterestResponse(sosAbstractFeature, xbGetFoiResponse);
            }
        }
        // set schemLocation
        XmlHelper.makeGmlIdsUnique(xbGetFoiResponseDoc.getDomNode());
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_GET_FEATURE_OF_INTEREST_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbGetFoiResponseDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbGetFoiResponseDoc, schemaLocations);
        return xbGetFoiResponseDoc;
    }

    private void addFeatureOfInterestGetFeatureOfInterestResponse(final AbstractFeature feature,
            final GetFeatureOfInterestResponseType getFoiResponse) throws OwsExceptionReport {
        final Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(HelperValues.class);
        final Profile activeProfile = Configurator.getInstance().getProfileHandler().getActiveProfile();
        if (activeProfile.isSetEncodeFeatureOfInterestNamespace()) {
            additionalValues.put(HelperValues.ENCODE_NAMESPACE,
                    activeProfile.getEncodingNamespaceForFeatureOfInterest());
        }
        final XmlObject encodeObjectToXml =
                CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, feature, additionalValues);
        getFoiResponse.addNewFeatureMember().set(encodeObjectToXml);
    }

    private XmlObject createGetObservationByIdResponse(final GetObservationByIdResponse response)
            throws OwsExceptionReport {
        final GetObservationByIdResponseDocument xbGetObsByIdRespDoc =
                GetObservationByIdResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final GetObservationByIdResponseType xbGetObsByIdResp = xbGetObsByIdRespDoc.addNewGetObservationByIdResponse();
        final List<OmObservation> observationCollection = response.getObservationCollection();
        final Encoder<XmlObject, OmObservation> encoder =
                CodingHelper.getEncoder(response.getResponseFormat(), new OmObservation());
        final HashMap<String, String> gmlID4sfIdentifier = new HashMap<String, String>(observationCollection.size());
        final int sfIdCounter = 1;
        for (final OmObservation sosObservation : observationCollection) {
            final Map<HelperValues, String> foiHelper =
                    new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
            String gmlId;
            // FIXME CodeWithAuthority in Map<String,String>
            if (gmlID4sfIdentifier.containsKey(sosObservation.getObservationConstellation().getFeatureOfInterest()
                    .getIdentifier())) {
                gmlId =
                        gmlID4sfIdentifier.get(sosObservation.getObservationConstellation().getFeatureOfInterest()
                                .getIdentifier());
                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(true));
            } else {
                gmlId = "sf_" + sfIdCounter;
                gmlID4sfIdentifier.put(sosObservation.getObservationConstellation().getFeatureOfInterest()
                        .getIdentifier().getValue(), gmlId);
                foiHelper.put(HelperValues.EXIST_FOI_IN_DOC, Boolean.toString(false));
            }
            foiHelper.put(HelperValues.GMLID, gmlId);
            xbGetObsByIdResp.addNewObservation().addNewOMObservation().set(encoder.encode(sosObservation, foiHelper));
        }
        XmlHelper.makeGmlIdsUnique(xbGetObsByIdResp.getDomNode());
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_GET_OBSERVATION_BY_ID_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(xbGetObsByIdRespDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(xbGetObsByIdRespDoc, schemaLocations);
        return xbGetObsByIdRespDoc;
    }

    private XmlObject createInsertObservationResponse(final InsertObservationResponse response) {
        final InsertObservationResponseDocument xbInsObsRespDoc =
                InsertObservationResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbInsObsRespDoc.addNewInsertObservationResponse();
        // set schema location
        N52XmlHelper.setSchemaLocationsToDocument(xbInsObsRespDoc,
                CollectionHelper.set(N52XmlHelper.getSchemaLocationForSOS200()));

        return xbInsObsRespDoc;
    }

    private XmlObject createInsertResultTemplateResponseDocument(final InsertResultTemplateResponse response)
            throws OwsExceptionReport {
        final InsertResultTemplateResponseDocument insertResultTemplateResponseDoc =
                InsertResultTemplateResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        final InsertResultTemplateResponseType insertResultTemplateResponse =
                insertResultTemplateResponseDoc.addNewInsertResultTemplateResponse();
        insertResultTemplateResponse.setAcceptedTemplate(response.getAcceptedTemplate());
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_INSERT_RESULT_TEMPLATE_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(insertResultTemplateResponseDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(insertResultTemplateResponseDoc, schemaLocations);
        return insertResultTemplateResponseDoc;
    }

    private XmlObject createInsertResultResponseDocument(final InsertResultResponse response)
            throws OwsExceptionReport {
        final InsertResultResponseDocument insertResultResponseDoc =
                InsertResultResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        insertResultResponseDoc.addNewInsertResultResponse();
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_INSERT_RESULT_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(insertResultResponseDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(insertResultResponseDoc, schemaLocations);
        return insertResultResponseDoc;
    }

    private XmlObject createGetResultResponseDocument(final GetResultResponse response) {
        final GetResultResponseDocument getResultResponseDoc =
                GetResultResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final GetResultResponseType getResultResponse = getResultResponseDoc.addNewGetResultResponse();
        final XmlObject resultValues = getResultResponse.addNewResultValues();
        if (response.hasResultValues()) {
            final XmlString xmlString = XmlString.Factory.newInstance();
            xmlString.setStringValue(response.getResultValues());
            resultValues.set(xmlString);
        }
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_GET_RESULT_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(getResultResponseDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(getResultResponseDoc, schemaLocations);
        return getResultResponseDoc;
    }

    private XmlObject createGetResultTemplateResponseDocument(final GetResultTemplateResponse response)
            throws OwsExceptionReport {
        final GetResultTemplateResponseDocument getResultTemplateResponseDoc =
                GetResultTemplateResponseDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final GetResultTemplateResponseType getResultTemplateResponse =
                getResultTemplateResponseDoc.addNewGetResultTemplateResponse();
        getResultTemplateResponse.setResultEncoding(createResultEncoding(response.getResultEncoding()));
        getResultTemplateResponse.setResultStructure(createResultStructure(response.getResultStructure()));
        // set schema location
        final Set<SchemaLocation> schemaLocations = CollectionHelper.set();
        schemaLocations.add(Sos2Constants.SOS_GET_RESULT_TEMPLATE_SCHEMA_LOCATION);
        N52XmlHelper.addSchemaLocationsForTo(getResultTemplateResponseDoc, schemaLocations);
        N52XmlHelper.setSchemaLocationsToDocument(getResultTemplateResponseDoc, schemaLocations);
        return getResultTemplateResponseDoc;
    }

    private XmlObject createGetCapabilitiesRequest(final GetCapabilitiesRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    private XmlObject createGetResultTemplateRequest(final GetResultTemplateRequest request) {
        final GetResultTemplateDocument getResultTemplateDoc =
                GetResultTemplateDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final GetResultTemplateType getResultTemplate = getResultTemplateDoc.addNewGetResultTemplate();
        getResultTemplate.setService(request.getService());
        getResultTemplate.setVersion(request.getVersion());
        getResultTemplate.setOffering(request.getOffering());
        getResultTemplate.setObservedProperty(request.getObservedProperty());
        return getResultTemplateDoc;
    }

    private XmlObject createGetResultRequest(final GetResultRequest request) throws OwsExceptionReport {
        final GetResultDocument getResultDoc =
                GetResultDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final GetResultType getResult = getResultDoc.addNewGetResult();
        getResult.setService(request.getService());
        getResult.setVersion(request.getVersion());
        getResult.setOffering(request.getOffering());
        getResult.setObservedProperty(request.getObservedProperty());
        if (request.hasFeatureOfInterest()) {
            for (final String featureOfInterest : request.getFeatureIdentifiers()) {
                getResult.addFeatureOfInterest(featureOfInterest);
            }
        }
        if (request.hasTemporalFilter()) {
            for (final TemporalFilter temporalFilter : request.getTemporalFilter()) {
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
     * 
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs.
     */
    protected void setContents(final Contents xbContents, final Collection<SosObservationOffering> offerings,
            final String version) throws OwsExceptionReport {
        final ContentsType xbContType = xbContents.addNewContents();

        int offeringCounter = 0; // for gml:id generation
        for (final SosObservationOffering offering : offerings) {
            if (offering.isValidObservationOffering()) {
                ++offeringCounter;

                final ObservationOfferingType xbObsOff =
                        ObservationOfferingType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                xbObsOff.setIdentifier(offering.getOffering());
                if (offering.getOfferingName() != null && !offering.getOfferingName().isEmpty()) {
                    xbObsOff.addNewName().setStringValue(offering.getOfferingName());
                }
                for (final String procedure : offering.getProcedures()) {
                    xbObsOff.setProcedure(procedure);
                }
                // TODO: procedureDescriptionFormat [0..*]
                // set observableProperties [0..*]
                for (final String phenomenon : offering.getObservableProperties()) {
                    xbObsOff.addObservableProperty(phenomenon);
                }

                // set relatedFeatures [0..*]
                if (offering.getRelatedFeatures() != null) {
                    for (final String relatedFeatureTarget : offering.getRelatedFeatures().keySet()) {
                        createRelatedFeature(xbObsOff.addNewRelatedFeature().addNewFeatureRelationship(),
                                relatedFeatureTarget, offering.getRelatedFeatures().get(relatedFeatureTarget));
                    }
                }

                // set observed area [0..1]
                if (offering.getObservedArea() != null && offering.getObservedArea().getEnvelope() != null
                        && offering.getObservedArea().getSrid() != -1) {
                    final XmlObject encodeObjectToXml =
                            CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, offering.getObservedArea());
                    xbObsOff.addNewObservedArea().addNewEnvelope().set(encodeObjectToXml);
                }

                // set up phenomenon time [0..1]
                if (offering.getPhenomenonTime() instanceof TimePeriod) {
                    final TimePeriod tp = (TimePeriod) offering.getPhenomenonTime();
                    tp.setGmlId(String.format("%s_%d", Sos2Constants.EN_PHENOMENON_TIME, offeringCounter));
                    if (!tp.isEmpty()) {
                        final XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, tp);
                        xbObsOff.addNewPhenomenonTime().addNewTimePeriod().set(xmlObject);
                        xbObsOff.getPhenomenonTime().substitute(
                                new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_PHENOMENON_TIME,
                                        SosConstants.NS_SOS_PREFIX), xbObsOff.getPhenomenonTime().schemaType());
                    }
                }

                // set resultTime [0..1]
                if (offering.getResultTime() instanceof TimePeriod) {
                    final TimePeriod tp = (TimePeriod) offering.getResultTime();
                    tp.setGmlId(String.format("%s_%d", Sos2Constants.EN_RESULT_TIME, offeringCounter));
                    if (!tp.isEmpty()) {
                        final XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, tp);
                        xbObsOff.addNewResultTime().addNewTimePeriod().set(xmlObject);
                        xbObsOff.getResultTime().substitute(
                                new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESULT_TIME,
                                        SosConstants.NS_SOS_PREFIX), xbObsOff.getResultTime().schemaType());
                    }
                }

                // set responseFormat [0..*]
                if (offering.getResponseFormats() != null) {
                    for (final String responseFormat : offering.getResponseFormats()) {
                        xbObsOff.addResponseFormat(responseFormat);
                    }
                }

                // set observationType [0..*]
                if (offering.getObservationTypes() != null) {
                    for (final String obsType : offering.getObservationTypes()) {
                        xbObsOff.addObservationType(obsType);
                    }
                }

                // set featureOfInterestType [0..1]
                if (offering.getFeatureOfInterestTypes() != null) {
                    for (final String featureOfInterestType : offering.getFeatureOfInterestTypes()) {
                        xbObsOff.addFeatureOfInterestType(featureOfInterestType);
                    }
                }

                if (offering.getProcedureDescriptionFormat() != null
                        && !offering.getProcedureDescriptionFormat().isEmpty()) {
                    for (final String procedureDescriptionFormat : offering.getProcedureDescriptionFormat()) {
                        xbObsOff.addProcedureDescriptionFormat(procedureDescriptionFormat);
                    }
                }
                xbContType.addNewOffering().setAbstractOffering(xbObsOff);
                // Offering addNewOffering = xbContType.addNewOffering();
                // addNewOffering.addNewAbstractOffering().set(xbObsOff);
                // XmlHelper.substituteElement(addNewOffering.getAbstractOffering(),
                // xbObsOff);
            }
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
    private void createRelatedFeature(final FeatureRelationshipType featureRelationchip,
            final String relatedFeatureTarget, final Collection<String> roles) {
        featureRelationchip.addNewTarget().setHref(relatedFeatureTarget);
        if (roles != null) {
            for (final String role : roles) {
                featureRelationchip.setRole(role);
            }
        }
    }

    private void setExensions(final XmlObject addNewExtension, final SwesExtension extension) throws CodedException {
        if (extension instanceof SosInsertionCapabilities) {
            addNewExtension.set(createInsertionCapabilities((SosInsertionCapabilities) extension));
        } else {
            throw new OptionNotSupportedException()
                    .withMessage("The extension element is not supported by this service!");
        }
    }

    private XmlObject createInsertionCapabilities(final SosInsertionCapabilities sosInsertionCapabilities) {
        final InsertionCapabilitiesDocument insertionCapabilitiesDoc =
                InsertionCapabilitiesDocument.Factory.newInstance();
        // InsertionCapabilitiesType insertionCapabilities =
        // InsertionCapabilitiesType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final InsertionCapabilitiesType insertionCapabilities = insertionCapabilitiesDoc.addNewInsertionCapabilities();
        for (final String featureOfInterestType : sosInsertionCapabilities.getFeatureOfInterestTypes()) {
            if (!featureOfInterestType.equals(SosConstants.NOT_DEFINED)) {
                insertionCapabilities.addFeatureOfInterestType(featureOfInterestType);
            }
        }
        for (final String observationType : sosInsertionCapabilities.getObservationTypes()) {
            if (!observationType.equals(SosConstants.NOT_DEFINED)) {
                insertionCapabilities.addObservationType(observationType);
            }
        }
        for (final String procedureDescriptionFormat : sosInsertionCapabilities.getProcedureDescriptionFormats()) {
            if (!procedureDescriptionFormat.equals(SosConstants.NOT_DEFINED)) {
                insertionCapabilities.addProcedureDescriptionFormat(procedureDescriptionFormat);
            }
        }
        if (sosInsertionCapabilities.getSupportedEncodings() != null) {
            for (final String supportedEncoding : sosInsertionCapabilities.getSupportedEncodings()) {
                if (!supportedEncoding.equals(SosConstants.NOT_DEFINED)) {
                    insertionCapabilities.addSupportedEncoding(supportedEncoding);
                }
            }
        }
        // return insertionCapabilities;
        return insertionCapabilitiesDoc;
    }

    // /**
    // * queries the bounding box of all requested feature of interest IDs
    // *
    // * @param envelope
    // *
    // * @param foiIDs
    // * ArrayList with String[]s containing the ids of the feature of
    // * interests for which the BBOX should be returned
    // * @return Returns EnvelopeType XmlBean which represents the BBOX of the
    // * requested feature of interests
    // * @throws OwsExceptionReport
    // * if query of the BBOX failed
    // */
    // private EnvelopeType getBBOX4Offering(Envelope envelope, int srsID)
    // throws OwsExceptionReport {
    //
    //
    // }

    private void renameContentsElementNames(final Contents xbContents) {
        for (final Offering offering : xbContents.getContents().getOfferingArray()) {
            final XmlCursor cursor = offering.getAbstractOffering().newCursor();
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
            if (cursor.toChild(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESULT_TIME))) {
                cursor.setName(new QName(Sos2Constants.NS_SOS_20, Sos2Constants.EN_RESULT_TIME,
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

    private void createTemporalFilter(final net.opengis.sos.x20.GetResultType.TemporalFilter temporalFilter,
            final TemporalFilter sosTemporalFilter) throws OwsExceptionReport {
        final Encoder<XmlObject, TemporalFilter> encoder =
                CodingRepository.getInstance().getEncoder(
                        CodingHelper.getEncoderKey(FilterConstants.NS_FES_2, sosTemporalFilter));
        final XmlObject encodedObject = encoder.encode(sosTemporalFilter);
        temporalFilter.set(encodedObject);
    }

    private void createSpatialFilter(final SpatialFilter spatialFilter,
            final org.n52.sos.ogc.filter.SpatialFilter sosSpatialFilter) throws OwsExceptionReport {
        final Encoder<XmlObject, org.n52.sos.ogc.filter.SpatialFilter> encoder =
                CodingRepository.getInstance().getEncoder(
                        CodingHelper.getEncoderKey(FilterConstants.NS_FES_2, sosSpatialFilter));
        final XmlObject encodedObject = encoder.encode(sosSpatialFilter);
        spatialFilter.set(encodedObject);
    }

    private ResultEncoding createResultEncoding(final SosResultEncoding sosResultEncoding) throws OwsExceptionReport {
        final ResultEncoding resultEncoding =
                ResultEncoding.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // TODO move encoding to SWECommonEncoder
        if (sosResultEncoding.getXml() != null && !sosResultEncoding.getXml().isEmpty()) {
            try {
                final TextEncodingDocument textEncodingDoc =
                        (TextEncodingDocument) XmlObject.Factory.parse(sosResultEncoding.getXml());
                resultEncoding.addNewAbstractEncoding().set(textEncodingDoc.getTextEncoding());
                XmlHelper.substituteElement(resultEncoding.getAbstractEncoding(), textEncodingDoc.getTextEncoding());
            } catch (final XmlException e) {
                throw new NoApplicableCodeException().causedBy(e).withMessage(
                        "ResultEncoding element encoding is not supported!");
            }
        } else {
            final Encoder<XmlObject, Object> encoder =
                    CodingRepository.getInstance().getEncoder(
                            CodingHelper.getEncoderKey(SWEConstants.NS_SWE_20, sosResultEncoding.getEncoding()));
            if (encoder == null) {
                throw new NoApplicableCodeException().withMessage("Missing encoder for ResultEncoding!");
            }
            final Object encodedObject = encoder.encode(sosResultEncoding.getEncoding());
            if (encodedObject instanceof XmlObject) {
                final TextEncodingDocument textEncodingDoc = (TextEncodingDocument) encodedObject;
                resultEncoding.addNewAbstractEncoding().set(textEncodingDoc.getTextEncoding());
                XmlHelper.substituteElement(resultEncoding.getAbstractEncoding(), textEncodingDoc.getTextEncoding());
            } else {
                throw new NoApplicableCodeException().withMessage("ResultEncoding element encoding is not supported!");
            }
        }
        return resultEncoding;
    }

    private ResultStructure createResultStructure(final SosResultStructure sosResultStructure)
            throws OwsExceptionReport {
        final ResultStructure resultStructure =
                ResultStructure.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // TODO move encoding to SWECommonEncoder
        if (sosResultStructure.getXml() != null && !sosResultStructure.getXml().isEmpty()) {
            try {
                final DataRecordDocument dataRecordDoc =
                        (DataRecordDocument) XmlObject.Factory.parse(sosResultStructure.getXml());
                resultStructure.addNewAbstractDataComponent().set(dataRecordDoc.getDataRecord());
                XmlHelper.substituteElement(resultStructure.getAbstractDataComponent(), dataRecordDoc.getDataRecord());
            } catch (final XmlException e) {
                throw new NoApplicableCodeException()
                        .withMessage("ResultStructure element encoding is not supported!");
            }
        } else {
            final Encoder<XmlObject, Object> encoder =
                    CodingRepository.getInstance()
                            .getEncoder(
                                    CodingHelper.getEncoderKey(SWEConstants.NS_SWE_20,
                                            sosResultStructure.getResultStructure()));
            if (encoder == null) {
                throw new NoApplicableCodeException().withMessage("Missing encoder for ResultStructure!");
            }
            final Object encodedObject = encoder.encode(sosResultStructure.getResultStructure());
            if (encodedObject instanceof XmlObject) {
                final DataRecordDocument dataRecordDoc = (DataRecordDocument) encodedObject;
                resultStructure.addNewAbstractDataComponent().set(dataRecordDoc.getDataRecord());
                XmlHelper.substituteElement(resultStructure.getAbstractDataComponent(), dataRecordDoc.getDataRecord());
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("ResultStructure element encoding is not supported!");
            }
        }
        return resultStructure;
    }
}
