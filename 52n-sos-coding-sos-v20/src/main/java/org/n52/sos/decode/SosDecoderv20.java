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
package org.n52.sos.decode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.sos.x20.GetCapabilitiesDocument;
import net.opengis.sos.x20.GetCapabilitiesType;
import net.opengis.sos.x20.GetFeatureOfInterestDocument;
import net.opengis.sos.x20.GetFeatureOfInterestType;
import net.opengis.sos.x20.GetObservationByIdDocument;
import net.opengis.sos.x20.GetObservationByIdType;
import net.opengis.sos.x20.GetObservationDocument;
import net.opengis.sos.x20.GetObservationType;
import net.opengis.sos.x20.GetResultDocument;
import net.opengis.sos.x20.GetResultResponseDocument;
import net.opengis.sos.x20.GetResultResponseType;
import net.opengis.sos.x20.GetResultTemplateDocument;
import net.opengis.sos.x20.GetResultTemplateResponseDocument;
import net.opengis.sos.x20.GetResultTemplateResponseType;
import net.opengis.sos.x20.GetResultTemplateType;
import net.opengis.sos.x20.GetResultType;
import net.opengis.sos.x20.InsertObservationDocument;
import net.opengis.sos.x20.InsertObservationType;
import net.opengis.sos.x20.InsertObservationType.Observation;
import net.opengis.sos.x20.InsertResultDocument;
import net.opengis.sos.x20.InsertResultTemplateDocument;
import net.opengis.sos.x20.InsertResultTemplateType;
import net.opengis.sos.x20.InsertResultType;
import net.opengis.sos.x20.ResultTemplateType;
import net.opengis.sos.x20.ResultTemplateType.ObservationTemplate;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.MissingResultValuesException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.GetResultResponse;
import org.n52.sos.response.GetResultTemplateResponse;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SosDecoderv20 implements Decoder<AbstractServiceCommunicationObject, XmlObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosDecoderv20.class);
    @SuppressWarnings("unchecked")
    private Set<DecoderKey> DECODER_KEYS = CollectionHelper.union(
        CodingHelper.decoderKeysForElements(Sos2Constants.NS_SOS_20,
            GetCapabilitiesDocument.class,
            GetObservationDocument.class,
            GetFeatureOfInterestDocument.class,
            GetObservationByIdDocument.class,
            InsertObservationDocument.class,
            InsertResultTemplateDocument.class,
            InsertResultDocument.class,
            GetResultTemplateDocument.class,
            GetResultDocument.class,
            GetResultTemplateResponseDocument.class,
            GetResultResponseDocument.class
        ),
       CodingHelper.xmlDecoderKeysForOperation(
            SosConstants.SOS, 
            Sos2Constants.SERVICEVERSION,
            SosConstants.Operations.GetCapabilities,
            SosConstants.Operations.GetObservation,
            SosConstants.Operations.GetFeatureOfInterest,
            SosConstants.Operations.GetObservationById,
            SosConstants.Operations.InsertObservation,
            Sos2Constants.Operations.InsertResultTemplate,
            Sos2Constants.Operations.InsertResult,
            Sos2Constants.Operations.GetResultTemplate,
            SosConstants.Operations.GetResult
        )
    );

    public SosDecoderv20() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
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
    public AbstractServiceCommunicationObject decode(XmlObject xmlObject) throws OwsExceptionReport {
        AbstractServiceCommunicationObject response = null;
        LOGGER.debug("REQUESTTYPE:" + xmlObject.getClass());

        // validate document
        XmlHelper.validateDocument(xmlObject);

        // getCapabilities request
        if (xmlObject instanceof GetCapabilitiesDocument) {
            GetCapabilitiesDocument getCapsDoc = (GetCapabilitiesDocument) xmlObject;
            response = parseGetCapabilities(getCapsDoc);
        }

        // getObservation request
        else if (xmlObject instanceof GetObservationDocument) {
            GetObservationDocument getObsDoc = (GetObservationDocument) xmlObject;
            response = parseGetObservation(getObsDoc);
        }

        // getFeatureOfInterest request
        else if (xmlObject instanceof GetFeatureOfInterestDocument) {
            GetFeatureOfInterestDocument getFoiDoc = (GetFeatureOfInterestDocument) xmlObject;
            response = parseGetFeatureOfInterest(getFoiDoc);
        }

        else if (xmlObject instanceof GetObservationByIdDocument) {
            GetObservationByIdDocument getObsByIdDoc = (GetObservationByIdDocument) xmlObject;
            response = parseGetObservationById(getObsByIdDoc);
        }

        else if (xmlObject instanceof InsertObservationDocument) {
            InsertObservationDocument insertObservationDoc = (InsertObservationDocument) xmlObject;
            response = parseInsertObservation(insertObservationDoc);
        }

        else if (xmlObject instanceof InsertResultTemplateDocument) {
            InsertResultTemplateDocument insertResultTemplateDoc = (InsertResultTemplateDocument) xmlObject;
            response = parseInsertResultTemplate(insertResultTemplateDoc);
        }

        else if (xmlObject instanceof InsertResultDocument) {
            InsertResultDocument insertResultDoc = (InsertResultDocument) xmlObject;
            response = parseInsertResult(insertResultDoc);
        }

        else if (xmlObject instanceof GetResultTemplateDocument) {
            GetResultTemplateDocument getResultTemplateDoc = (GetResultTemplateDocument) xmlObject;
            response = parseGetResultTemplate(getResultTemplateDoc);
        }

        else if (xmlObject instanceof GetResultDocument) {
            GetResultDocument getResultTemplateDoc = (GetResultDocument) xmlObject;
            response = parseGetResult(getResultTemplateDoc);
        }

        else if (xmlObject instanceof GetResultTemplateResponseDocument) {
            GetResultTemplateResponseDocument getResultTemplateResponseDoc =
                    (GetResultTemplateResponseDocument) xmlObject;
            response = parseGetResultTemplateResponse(getResultTemplateResponseDoc);
        }

        else if (xmlObject instanceof GetResultResponseDocument) {
            GetResultResponseDocument getResultResponseDoc = (GetResultResponseDocument) xmlObject;
            response = parseGetResultResponse(getResultResponseDoc);
        }

        else {
            throw new NoApplicableCodeException().withMessage("The request is not supported by this server!");
        }
        return response;
    }

    /**
     * parses the XmlBean representing the getCapabilities request and creates a
     * SosGetCapabilities request
     * 
     * @param getCapsDoc
     *            XmlBean created from the incoming request stream
     * @return Returns SosGetCapabilitiesRequest representing the request

     *
     * @throws OwsExceptionReport * If parsing the XmlBean failed
     */
    private AbstractServiceRequest parseGetCapabilities(GetCapabilitiesDocument getCapsDoc) throws OwsExceptionReport {
        GetCapabilitiesRequest request = new GetCapabilitiesRequest();

        GetCapabilitiesType getCapsType = getCapsDoc.getGetCapabilities2();

        request.setService(getCapsType.getService());

        if (getCapsType.getAcceptFormats() != null && getCapsType.getAcceptFormats().sizeOfOutputFormatArray() != 0) {
            request.setAcceptFormats(Arrays.asList(getCapsType.getAcceptFormats().getOutputFormatArray()));
        }

        if (getCapsType.getAcceptVersions() != null && getCapsType.getAcceptVersions().sizeOfVersionArray() != 0) {
            request.setAcceptVersions(getCapsType.getAcceptVersions().getVersionArray());
        }

        if (getCapsType.getSections() != null && getCapsType.getSections().getSectionArray().length != 0) {
            request.setSections(Arrays.asList(getCapsType.getSections().getSectionArray()));
        }

        if (getCapsType.getExtensionArray() != null && getCapsType.getExtensionArray().length != 0) {
            request.setExtensionArray(Arrays.asList(getCapsType.getExtensionArray()));
        }

        return request;
    }

    /**
     * parses the XmlBean representing the getObservation request and creates a
     * SoSGetObservation request
     * 
     * @param getObsDoc
     *            XmlBean created from the incoming request stream
     * @return Returns SosGetObservationRequest representing the request

     *
     * @throws OwsExceptionReport * If parsing the XmlBean failed
     */
    private AbstractServiceRequest parseGetObservation(GetObservationDocument getObsDoc) throws OwsExceptionReport {
        GetObservationRequest getObsRequest = new GetObservationRequest();
        GetObservationType getObsType = getObsDoc.getGetObservation();
        // TODO: check
        getObsRequest.setService(getObsType.getService());
        getObsRequest.setVersion(getObsType.getVersion());
        getObsRequest.setOfferings(Arrays.asList(getObsType.getOfferingArray()));
        getObsRequest.setObservedProperties(Arrays.asList(getObsType.getObservedPropertyArray()));
        getObsRequest.setProcedures(Arrays.asList(getObsType.getProcedureArray()));
        getObsRequest.setTemporalFilters(parseTemporalFilters4GetObservation(getObsType.getTemporalFilterArray()));
        if (getObsType.isSetSpatialFilter()) {
            getObsRequest.setSpatialFilter(parseSpatialFilter4GetObservation(getObsType.getSpatialFilter()));
        }
        getObsRequest.setFeatureIdentifiers(Arrays.asList(getObsType.getFeatureOfInterestArray()));
        if (getObsType.isSetResponseFormat()) {
            try {
                String responseFormat = URLDecoder.decode(getObsType.getResponseFormat(), "UTF-8");
                getObsRequest.setResponseFormat(responseFormat);
            } catch (UnsupportedEncodingException e) {
                throw new NoApplicableCodeException().causedBy(e).withMessage("Error while encoding response format!");
            }
        }

        return getObsRequest;
    }

    /**
     * parses the passes XmlBeans document and creates a SOS
     * getFeatureOfInterest request
     * 
     * @param getFoiDoc
     *            XmlBeans document representing the getFeatureOfInterest
     *            request
     * @return Returns SOS getFeatureOfInterest request

     *
     * @throws OwsExceptionReport * if validation of the request failed
     */
    private AbstractServiceRequest parseGetFeatureOfInterest(GetFeatureOfInterestDocument getFoiDoc)
            throws OwsExceptionReport {

        GetFeatureOfInterestRequest getFoiRequest = new GetFeatureOfInterestRequest();
        GetFeatureOfInterestType getFoiType = getFoiDoc.getGetFeatureOfInterest();
        getFoiRequest.setService(getFoiType.getService());
        getFoiRequest.setVersion(getFoiType.getVersion());
        getFoiRequest.setFeatureIdentifiers(Arrays.asList(getFoiType.getFeatureOfInterestArray()));
        getFoiRequest.setObservedProperties(Arrays.asList(getFoiType.getObservedPropertyArray()));
        getFoiRequest.setProcedures(Arrays.asList(getFoiType.getProcedureArray()));
        getFoiRequest.setSpatialFilters(parseSpatialFilters4GetFeatureOfInterest(getFoiType.getSpatialFilterArray()));

        return getFoiRequest;
    }

    private AbstractServiceRequest parseGetObservationById(GetObservationByIdDocument getObsByIdDoc)
            throws OwsExceptionReport {
        GetObservationByIdRequest getObsByIdRequest = new GetObservationByIdRequest();
        GetObservationByIdType getObsByIdType = getObsByIdDoc.getGetObservationById();
        getObsByIdRequest.setService(getObsByIdType.getService());
        getObsByIdRequest.setVersion(getObsByIdType.getVersion());
        getObsByIdRequest.setObservationIdentifier(Arrays.asList(getObsByIdType.getObservationArray()));
        return getObsByIdRequest;
    }

    private AbstractServiceRequest parseInsertObservation(InsertObservationDocument insertObservationDoc)
            throws OwsExceptionReport {
        // set namespace for default XML type (e.g. xs:string, xs:integer,
        // xs:boolean, ...)
        // Fix for problem with XmlBeans: namespace is not set in child elements
        // when defined in root of request (SOAP)
        XmlCursor cursor = insertObservationDoc.newCursor();
        if (cursor.toFirstChild()) {
            if (cursor.namespaceForPrefix(W3CConstants.NS_XS_PREFIX) == null) {
                cursor.prefixForNamespace(W3CConstants.NS_XS);
            }
        }
        cursor.dispose();
        InsertObservationRequest insertObservationRequest = new InsertObservationRequest();
        InsertObservationType insertObservationType = insertObservationDoc.getInsertObservation();
        insertObservationRequest.setService(insertObservationType.getService());
        insertObservationRequest.setVersion(insertObservationType.getVersion());
        if (insertObservationDoc.getInsertObservation().getOfferingArray() != null) {
            insertObservationRequest.setOfferings(Arrays.asList(insertObservationType.getOfferingArray()));
        }

        if (insertObservationType.getObservationArray() != null) {
            int length = insertObservationType.getObservationArray().length;
            Map<String, ITime> phenomenonTimes = new HashMap<String, ITime>(length);
            Map<String, TimeInstant> resultTimes = new HashMap<String, TimeInstant>(length);
            Map<String, SosAbstractFeature> features = new HashMap<String, SosAbstractFeature>(length);
            CompositeOwsException exceptions = new CompositeOwsException();
            for (Observation observation : insertObservationType.getObservationArray()) {
                Object decodedObject = CodingHelper.decodeXmlElement(observation.getOMObservation());
                if (decodedObject != null && decodedObject instanceof SosObservation) {
                    SosObservation sosObservation = (SosObservation) decodedObject;
                    checkAndAddPhenomenonTime(sosObservation.getPhenomenonTime(), phenomenonTimes);
                    checkAndAddResultTime(sosObservation.getResultTime(), resultTimes);
                    checkAndAddFeatures(sosObservation.getObservationConstellation().getFeatureOfInterest(), features);
                    insertObservationRequest.addObservation(sosObservation);
                } else {
                    throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                            .withMessage("The requested observation type (%s) is not supported by this server!",
                                         observation.getOMObservation().getDomNode().getNodeName());
                }
            }
            checkReferencedElements(insertObservationRequest.getObservations(), phenomenonTimes, resultTimes, features);
            exceptions.throwIfNotEmpty();
        } else {
            //TODO MissingMandatoryParameterException?
            throw new InvalidParameterValueException()
                    .at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The request does not contain an observation");
        }
        return insertObservationRequest;

    }

    private AbstractServiceRequest parseInsertResultTemplate(InsertResultTemplateDocument insertResultTemplateDoc)
            throws OwsExceptionReport {
        InsertResultTemplateRequest sosInsertResultTemplate = new InsertResultTemplateRequest();
        InsertResultTemplateType insertResultTemplate = insertResultTemplateDoc.getInsertResultTemplate();
        sosInsertResultTemplate.setService(insertResultTemplate.getService());
        sosInsertResultTemplate.setVersion(insertResultTemplate.getVersion());
        ResultTemplateType resultTemplate = insertResultTemplate.getProposedTemplate().getResultTemplate();
        sosInsertResultTemplate.setIdentifier(resultTemplate.getIdentifier());
        SosObservationConstellation sosObservationConstellation =
                parseObservationTemplate(resultTemplate.getObservationTemplate());
        sosObservationConstellation.addOffering(resultTemplate.getOffering());
        sosInsertResultTemplate.setObservationTemplate(sosObservationConstellation);
        sosInsertResultTemplate.setResultStructure(parseResultStructure(resultTemplate.getResultStructure()
                .getAbstractDataComponent()));
        sosInsertResultTemplate.setResultEncoding(parseResultEncoding(resultTemplate.getResultEncoding()
                .getAbstractEncoding()));
        return sosInsertResultTemplate;
    }

    private AbstractServiceRequest parseInsertResult(InsertResultDocument insertResultDoc) throws OwsExceptionReport {
        InsertResultType insertResult = insertResultDoc.getInsertResult();
        InsertResultRequest sosInsertResultRequest = new InsertResultRequest();
        sosInsertResultRequest.setService(insertResult.getService());
        sosInsertResultRequest.setVersion(insertResult.getVersion());
        sosInsertResultRequest.setTemplateIdentifier(insertResult.getTemplate());
        sosInsertResultRequest.setResultValues(parseResultValues(insertResult.getResultValues()));
        return sosInsertResultRequest;
    }

    private AbstractServiceRequest parseGetResult(GetResultDocument getResultDoc) throws OwsExceptionReport {
        GetResultType getResult = getResultDoc.getGetResult();
        GetResultRequest sosGetResultRequest = new GetResultRequest();
        sosGetResultRequest.setService(getResult.getService());
        sosGetResultRequest.setVersion(getResult.getVersion());
        sosGetResultRequest.setOffering(getResult.getOffering());
        sosGetResultRequest.setObservedProperty(getResult.getObservedProperty());
        sosGetResultRequest.setFeatureIdentifiers(Arrays.asList(getResult.getFeatureOfInterestArray()));
        getResult.getFeatureOfInterestArray();
        if (getResult.isSetSpatialFilter()) {
            sosGetResultRequest.setSpatialFilter(parseSpatialFilter4GetResult(getResult.getSpatialFilter()));
        }
        sosGetResultRequest.setTemporalFilter(parseTemporalFilters4GetResult(getResult.getTemporalFilterArray()));
        return sosGetResultRequest;
    }

    private AbstractServiceRequest parseGetResultTemplate(GetResultTemplateDocument getResultTemplateDoc) {
        GetResultTemplateType getResultTemplate = getResultTemplateDoc.getGetResultTemplate();
        GetResultTemplateRequest sosGetResultTemplateRequest = new GetResultTemplateRequest();
        sosGetResultTemplateRequest.setService(getResultTemplate.getService());
        sosGetResultTemplateRequest.setVersion(getResultTemplate.getVersion());
        sosGetResultTemplateRequest.setOffering(getResultTemplate.getOffering());
        sosGetResultTemplateRequest.setObservedProperty(getResultTemplate.getObservedProperty());
        return sosGetResultTemplateRequest;
    }

    private AbstractServiceResponse parseGetResultTemplateResponse(
            GetResultTemplateResponseDocument getResultTemplateResponseDoc) throws OwsExceptionReport {
        GetResultTemplateResponse sosGetResultTemplateResponse = new GetResultTemplateResponse();
        GetResultTemplateResponseType getResultTemplateResponse =
                getResultTemplateResponseDoc.getGetResultTemplateResponse();
        SosResultEncoding resultEncoding =
                parseResultEncoding(getResultTemplateResponse.getResultEncoding().getAbstractEncoding());
        SosResultStructure resultStructure =
                parseResultStructure(getResultTemplateResponse.getResultStructure().getAbstractDataComponent());
        sosGetResultTemplateResponse.setResultEncoding(resultEncoding);
        sosGetResultTemplateResponse.setResultStructure(resultStructure);
        return sosGetResultTemplateResponse;
    }

    private AbstractServiceResponse parseGetResultResponse(GetResultResponseDocument getResultResponseDoc)
            throws OwsExceptionReport {
        GetResultResponse sosGetResultResponse = new GetResultResponse();
        GetResultResponseType getResultResponse = getResultResponseDoc.getGetResultResponse();
        String resultValues = parseResultValues(getResultResponse.getResultValues());
        // sosGetResultResponse.setResultValues(resultValues);
        return sosGetResultResponse;
    }

    /**
     * Parses the spatial filter of a GetObservation request.
     * 
     * @param xbFilter
     *            XmlBean representing the spatial filter parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter

     *
     * @throws OwsExceptionReport * if creation of the SpatialFilter failed
     */
    private SpatialFilter parseSpatialFilter4GetObservation(
            net.opengis.sos.x20.GetObservationType.SpatialFilter spatialFilter) throws OwsExceptionReport {
        if (spatialFilter != null && spatialFilter.getSpatialOps() != null) {
            Object filter = CodingHelper.decodeXmlElement(spatialFilter.getSpatialOps());
            if (filter != null && filter instanceof SpatialFilter) {
                return (SpatialFilter) filter;
            }
        }
        return null;
    }

    /**
     * Parses the spatial filters of a GetFeatureOfInterest request.
     * 
     * @param spatialFilters
     *            XmlBean representing the spatial filter parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter

     *
     * @throws OwsExceptionReport * if creation of the SpatialFilter failed
     */
    private List<SpatialFilter> parseSpatialFilters4GetFeatureOfInterest(
            net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter[] spatialFilters) throws OwsExceptionReport {
        List<SpatialFilter> sosSpatialFilters = new ArrayList<SpatialFilter>(spatialFilters.length);
        for (net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter spatialFilter : spatialFilters) {
            Object filter = CodingHelper.decodeXmlElement(spatialFilter.getSpatialOps());
            if (filter != null && filter instanceof SpatialFilter) {
                sosSpatialFilters.add((SpatialFilter) filter);
            }
        }
        return sosSpatialFilters;
    }

    private SpatialFilter parseSpatialFilter4GetResult(net.opengis.sos.x20.GetResultType.SpatialFilter spatialFilter)
            throws OwsExceptionReport {
        if (spatialFilter != null && spatialFilter.getSpatialOps() != null) {
            Object filter = CodingHelper.decodeXmlElement(spatialFilter.getSpatialOps());
            if (filter != null && filter instanceof SpatialFilter) {
                return (SpatialFilter) filter;
            }
        }
        return null;
    }

    /**
     * parses the Time of the requests and returns an array representing the
     * temporal filters
     * 
     * @param xbTemporalFilters
     *            array of XmlObjects representing the Time element in the
     *            request
     * @return Returns array representing the temporal filters

     *
     * @throws OwsExceptionReport * if parsing of the element failed
     */
    private List<TemporalFilter> parseTemporalFilters4GetObservation(
            net.opengis.sos.x20.GetObservationType.TemporalFilter[] temporalFilters) throws OwsExceptionReport {
        List<TemporalFilter> sosTemporalFilters = new ArrayList<TemporalFilter>(temporalFilters.length);
        for (net.opengis.sos.x20.GetObservationType.TemporalFilter temporalFilter : temporalFilters) {
            Object filter = CodingHelper.decodeXmlElement(temporalFilter.getTemporalOps());
            if (filter != null && filter instanceof TemporalFilter) {
                sosTemporalFilters.add((TemporalFilter) filter);
            }
        }
        return sosTemporalFilters;
    }

    private List<TemporalFilter> parseTemporalFilters4GetResult(
            net.opengis.sos.x20.GetResultType.TemporalFilter[] temporalFilters) throws OwsExceptionReport {
        List<TemporalFilter> sosTemporalFilters = new ArrayList<TemporalFilter>(temporalFilters.length);
        for (net.opengis.sos.x20.GetResultType.TemporalFilter temporalFilter : temporalFilters) {
            Object filter = CodingHelper.decodeXmlElement(temporalFilter.getTemporalOps());
            if (filter != null && filter instanceof TemporalFilter) {
                sosTemporalFilters.add((TemporalFilter) filter);
            }
        }
        return sosTemporalFilters;
    }

    private SosObservationConstellation parseObservationTemplate(ObservationTemplate observationTemplate)
            throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlElement(observationTemplate.getOMObservation());
        if (decodedObject instanceof SosObservation) {
            SosObservation observation = (SosObservation) decodedObject;
            return observation.getObservationConstellation();
        }
        return null;
    }

    private SosResultStructure parseResultStructure(XmlObject resultStructure) throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlElement(resultStructure);
        if (decodedObject != null && decodedObject instanceof SosSweAbstractDataComponent) {
            SosSweAbstractDataComponent sosSweData = (SosSweAbstractDataComponent) decodedObject;
            SosResultStructure sosResultStructure = new SosResultStructure();
            sosResultStructure.setResultStructure(sosSweData);
            return sosResultStructure;
        } else {
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested result structure (%s) is not supported by this server!",
                                 resultStructure.getDomNode().getNodeName());
        }
    }

    private SosResultEncoding parseResultEncoding(XmlObject resultEncoding) throws OwsExceptionReport {
        Object decodedObject = CodingHelper.decodeXmlElement(resultEncoding);
        if (decodedObject != null && decodedObject instanceof SosSweAbstractEncoding) {
            SosSweAbstractEncoding sosSweEncoding = (SosSweAbstractEncoding) decodedObject;
            SosResultEncoding encoding = new SosResultEncoding();
            encoding.setEncoding(sosSweEncoding);
            return encoding;
        } else {
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested result encoding (%s) is not supported by this server!",
                                 resultEncoding.getDomNode().getNodeName());
        }
    }

    private String parseResultValues(XmlObject resultValues) throws OwsExceptionReport {
        if (resultValues.schemaType() == XmlString.type) {
            return ((XmlString) resultValues).getStringValue().trim();
        } else if (resultValues.schemaType() == XmlObject.type) {
            Node resultValuesNode = resultValues.getDomNode();
            if (resultValuesNode.hasChildNodes()) {
                NodeList childNodes = resultValuesNode.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeType() == Node.TEXT_NODE) {
                        return childNode.getNodeValue().trim();
                    }
                }
            }
            throw new MissingResultValuesException();
        } else {
            throw new NoApplicableCodeException().withMessage("The requested resultValue type is not supported");
        }
    }

    private void checkAndAddPhenomenonTime(ITime phenomenonTime, Map<String, ITime> phenomenonTimes) {
        if (!phenomenonTime.isReferenced()) {
            phenomenonTimes.put(phenomenonTime.getGmlId(), phenomenonTime);
        }
    }

    private void checkAndAddResultTime(TimeInstant resultTime, Map<String, TimeInstant> resultTimes) {
        if (!resultTime.isReferenced()) {
            resultTimes.put(resultTime.getGmlId(), resultTime);
        }
    }

    private void checkAndAddFeatures(SosAbstractFeature featureOfInterest, Map<String, SosAbstractFeature> features) {
        if (!featureOfInterest.isReferenced()) {
            features.put(featureOfInterest.getGmlId(), featureOfInterest);
        }
    }

    private void checkReferencedElements(List<SosObservation> observations, Map<String, ITime> phenomenonTimes,
                                         Map<String, TimeInstant> resultTimes, Map<String, SosAbstractFeature> features)
            throws OwsExceptionReport {
        for (SosObservation observation : observations) {
            // phenomenonTime
            ITime phenomenonTime = observation.getPhenomenonTime();
            if (phenomenonTime.isReferenced()) {
                observation.getValue().setPhenomenonTime(phenomenonTimes.get(phenomenonTime.getGmlId()));
            }
            // resultTime
            TimeInstant resultTime = observation.getResultTime();
            if (resultTime.isReferenced()) {
                if (resultTimes.containsKey(resultTime.getGmlId())) {
                    observation.setResultTime(resultTimes.get(resultTime.getGmlId()));
                } else if (phenomenonTimes.containsKey(resultTime.getGmlId())) {
                    ITime iTime = phenomenonTimes.get(resultTime.getGmlId());
                    if (iTime instanceof TimeInstant) {
                        observation.setResultTime((TimeInstant)iTime);
                    } else if (iTime instanceof TimePeriod) {
                        TimePeriod timePeriod = (TimePeriod) iTime;
                        observation.setResultTime(new TimeInstant(timePeriod.getEnd()));
                    } else {
                        throw new InvalidParameterValueException().at("observation.resultTime")
                                .withMessage("The time value type is not supported");
                    }
                        
                }
            }
            // featureOfInterest
            SosAbstractFeature featureOfInterest = observation.getObservationConstellation().getFeatureOfInterest();
            if (featureOfInterest.isReferenced()) {
                observation.getObservationConstellation().setFeatureOfInterest(features.get(featureOfInterest.getGmlId()));
            }
            
        }
    }
}
