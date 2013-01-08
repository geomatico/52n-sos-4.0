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
package org.n52.sos.decode;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
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
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SosDecoderv20 implements IXmlRequestDecoder {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosDecoderv20.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public SosDecoderv20() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        DecoderKeyType namespaceDKT = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        decoderKeyTypes.add(namespaceDKT);
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
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
            String exceptionText = "The request is not supported by this server!";
            LOGGER.debug(exceptionText);
            Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
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
     * @throws OwsExceptionReport
     *             If parsing the XmlBean failed
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
     * @throws OwsExceptionReport
     *             If parsing the XmlBean failed
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
                String exceptionText = "Error while encoding response format!";
                throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }

        } else {
            getObsRequest.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
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
     * @throws OwsExceptionReport
     *             if validation of the request failed
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
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (Observation observation : insertObservationType.getObservationArray()) {
                Object decodedObject = decodeXmlToObject(observation.getOMObservation());
                if (decodedObject != null && decodedObject instanceof SosObservation) {
                    insertObservationRequest.addObservation((SosObservation) decodedObject);
                    break;
                } else {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested observation type (");
                    exceptionText.append(observation.getOMObservation().getDomNode().getNodeName());
                    exceptionText.append(") is not supported by this server!");
                    LOGGER.debug(exceptionText.toString());
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.observation.name(), exceptionText.toString()));
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
            // TODO: add offering to observationConstellation (duplicate obs if
            // more than one offering)
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
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private SpatialFilter parseSpatialFilter4GetObservation(
            net.opengis.sos.x20.GetObservationType.SpatialFilter spatialFilter) throws OwsExceptionReport {
        if (spatialFilter != null && spatialFilter.getSpatialOps() != null) {
            Object filter = decodeXmlToObject(spatialFilter.getSpatialOps());
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
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private List<SpatialFilter> parseSpatialFilters4GetFeatureOfInterest(
            net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter[] spatialFilters) throws OwsExceptionReport {
        List<SpatialFilter> sosSpatialFilters = new ArrayList<SpatialFilter>(spatialFilters.length);
        for (net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter spatialFilter : spatialFilters) {
            Object filter = decodeXmlToObject(spatialFilter.getSpatialOps());
            if (filter != null && filter instanceof SpatialFilter) {
                sosSpatialFilters.add((SpatialFilter) filter);
            }
        }
        return sosSpatialFilters;
    }

    private SpatialFilter parseSpatialFilter4GetResult(net.opengis.sos.x20.GetResultType.SpatialFilter spatialFilter)
            throws OwsExceptionReport {
        if (spatialFilter != null && spatialFilter.getSpatialOps() != null) {
            Object filter = decodeXmlToObject(spatialFilter.getSpatialOps());
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
     * @throws OwsExceptionReport
     *             if parsing of the element failed
     */
    private List<TemporalFilter> parseTemporalFilters4GetObservation(
            net.opengis.sos.x20.GetObservationType.TemporalFilter[] temporalFilters) throws OwsExceptionReport {
        List<TemporalFilter> sosTemporalFilters = new ArrayList<TemporalFilter>();
        for (net.opengis.sos.x20.GetObservationType.TemporalFilter temporalFilter : temporalFilters) {
            Object filter = decodeXmlToObject(temporalFilter.getTemporalOps());
            if (filter != null && filter instanceof TemporalFilter) {
                sosTemporalFilters.add((TemporalFilter) filter);
            }
        }
        return sosTemporalFilters;
    }

    private List<TemporalFilter> parseTemporalFilters4GetResult(
            net.opengis.sos.x20.GetResultType.TemporalFilter[] temporalFilters) throws OwsExceptionReport {
        List<TemporalFilter> sosTemporalFilters = new ArrayList<TemporalFilter>();
        for (net.opengis.sos.x20.GetResultType.TemporalFilter temporalFilter : temporalFilters) {
            Object filter = decodeXmlToObject(temporalFilter.getTemporalOps());
            if (filter != null && filter instanceof TemporalFilter) {
                sosTemporalFilters.add((TemporalFilter) filter);
            }
        }
        return sosTemporalFilters;
    }

    private SosObservationConstellation parseObservationTemplate(ObservationTemplate observationTemplate)
            throws OwsExceptionReport {
        Object decodedObject = decodeXmlToObject(observationTemplate.getOMObservation());
        if (decodedObject instanceof SosObservation) {
            SosObservation observation = (SosObservation) decodedObject;
            return observation.getObservationConstellation();
        }
        return null;
    }

    private SosResultStructure parseResultStructure(XmlObject resultStructure) throws OwsExceptionReport {
        Object decodedObject = decodeXmlToObject(resultStructure);
        if (decodedObject != null && decodedObject instanceof SosSweAbstractDataComponent) {
            SosSweAbstractDataComponent sosSweData = (SosSweAbstractDataComponent) decodedObject;
            SosResultStructure sosResultStructure = new SosResultStructure();
            sosResultStructure.setResultStructure(sosSweData);
            return sosResultStructure;
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested result structure (");
            exceptionText.append(resultStructure.getDomNode().getNodeName());
            exceptionText.append(") is not supported by this server!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText.toString());
        }
    }

    private SosResultEncoding parseResultEncoding(XmlObject resultEncoding) throws OwsExceptionReport {
        Object decodedObject = decodeXmlToObject(resultEncoding);
        if (decodedObject != null && decodedObject instanceof SosSweAbstractEncoding) {
            SosSweAbstractEncoding sosSweEncoding = (SosSweAbstractEncoding) decodedObject;
            SosResultEncoding encoding = new SosResultEncoding();
            encoding.setEncoding(sosSweEncoding);
            return encoding;
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested result structure (");
            exceptionText.append(resultEncoding.getDomNode().getNodeName());
            exceptionText.append(") is not supported by this server!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText.toString());
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
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertResult.resultValues.name());
        } else {
            throw Util4Exceptions.createNoApplicableCodeException(null,
                    "The requested resultValue type is not supported");
        }
    }

    private Object decodeXmlToObject(XmlObject xmlObject) throws OwsExceptionReport {
        List<IDecoder> decoderList = Configurator.getInstance().getDecoder(XmlHelper.getNamespace(xmlObject));
        if (decoderList != null) {
            for (IDecoder decoder : decoderList) {
                // TODO: check if decoding returns null or throws exception: in
                // both cases try next decoder in list
                return decoder.decode(xmlObject);
            }
        }
        return null;
    }

}
