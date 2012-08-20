
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

package org.n52.sos.decode;

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
import net.opengis.sos.x20.InsertObservationDocument;
import net.opengis.sos.x20.InsertObservationType;
import net.opengis.sos.x20.InsertObservationType.Observation;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public AbstractServiceRequest decode(XmlObject xmlObject) throws OwsExceptionReport {
        AbstractServiceRequest response = null;
        LOGGER.debug("REQUESTTYPE:" + xmlObject.getClass());

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

        // validate document
        XmlHelper.validateDocument(getCapsDoc);

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
        // validate document
        XmlHelper.validateDocument(getObsDoc);

        GetObservationRequest getObsRequest = new GetObservationRequest();
        GetObservationType getObsType = getObsDoc.getGetObservation();
        // TODO: check
        getObsRequest.setService(getObsType.getService());
        getObsRequest.setVersion(getObsType.getVersion());
        getObsRequest.setOfferings(Arrays.asList(getObsType.getOfferingArray()));
        getObsRequest.setObservedProperties(Arrays.asList(getObsType.getObservedPropertyArray()));
        getObsRequest.setProcedures(Arrays.asList(getObsType.getProcedureArray()));
        getObsRequest.setEventTimes(Arrays.asList(parseTemporalFilters(getObsType.getTemporalFilterArray())));
        if (getObsType.isSetSpatialFilter()) {
            getObsRequest.setSpatialFilter(parseSpatialFilter4GetObs(getObsType.getSpatialFilter()));
        }
        getObsRequest.setFeatureIdentifiers(Arrays.asList(getObsType.getFeatureOfInterestArray()));
        if (getObsType.isSetResponseFormat()) {
            getObsRequest.setResponseFormat(getObsType.getResponseFormat());
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
        // validate document
        XmlHelper.validateDocument(getFoiDoc);

        GetFeatureOfInterestRequest getFoiRequest = new GetFeatureOfInterestRequest();
        GetFeatureOfInterestType getFoiType = getFoiDoc.getGetFeatureOfInterest();
        getFoiRequest.setService(getFoiType.getService());
        getFoiRequest.setVersion(getFoiType.getVersion());
        getFoiRequest.setFeatureIdentifiers(Arrays.asList(getFoiType.getFeatureOfInterestArray()));
        getFoiRequest.setObservedProperties(Arrays.asList(getFoiType.getObservedPropertyArray()));
        getFoiRequest.setProcedures(Arrays.asList(getFoiType.getProcedureArray()));
        getFoiRequest.setSpatialFilters(parseSpatialFilters4GetFoi(getFoiType.getSpatialFilterArray()));

        return getFoiRequest;
    }

    private AbstractServiceRequest parseGetObservationById(GetObservationByIdDocument getObsByIdDoc)
            throws OwsExceptionReport {
        // validate document
        XmlHelper.validateDocument(getObsByIdDoc);
        GetObservationByIdRequest getObsByIdRequest = new GetObservationByIdRequest();
        GetObservationByIdType getObsByIdType = getObsByIdDoc.getGetObservationById();
        getObsByIdRequest.setService(getObsByIdType.getService());
        getObsByIdRequest.setVersion(getObsByIdType.getVersion());
        getObsByIdRequest.setObservationIdentifier(Arrays.asList(getObsByIdType.getObservationArray()));
        return getObsByIdRequest;
    }

    private AbstractServiceRequest parseInsertObservation(InsertObservationDocument insertObservationDoc)
            throws OwsExceptionReport {
        // set namespace for default XML type (e.g. xs:string, xs:integer, xs:boolean, ...)
        // Fix for problem with XmlBeans: namespace is not set in child elements when defined in root of request (SOAP)
        XmlCursor cursor = insertObservationDoc.newCursor();
        if (cursor.toFirstChild()) {
            if (cursor.namespaceForPrefix(W3CConstants.NS_XS_PREFIX) == null) {
                cursor.prefixForNamespace(W3CConstants.NS_XS);
            }
        }
        cursor.dispose();
        // validate document
        XmlHelper.validateDocument(insertObservationDoc);
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
                List<IDecoder> decoderList = Configurator.getInstance().getDecoder(new DecoderKeyType(XmlHelper.getNamespace(observation.getOMObservation())));
                if (decoderList != null) {
                    for (IDecoder decoder : decoderList) {
                        Object decodedObject = decoder.decode(observation.getOMObservation());
                        if (decodedObject != null && decodedObject instanceof SosObservation) {
                            insertObservationRequest.addObservation((SosObservation)decodedObject);
                            break;
                        }
                    }
                } else {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested observation type (");
                    exceptionText.append(observation.getOMObservation().getDomNode().getNodeName());
                    exceptionText.append(") is not supported by this server!");
                    LOGGER.debug(exceptionText.toString());
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(Sos2Constants.InsertObservationParams.observation.name(), exceptionText.toString()));
                }
            }
            Util4Exceptions.mergeExceptions(exceptions);
            // TODO: add offering to observationConstellation (duplicate obs if more than one offering)
        }
        return insertObservationRequest;

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
    private SpatialFilter parseSpatialFilter4GetObs(net.opengis.sos.x20.GetObservationType.SpatialFilter xbFilter)
            throws OwsExceptionReport {
        if (xbFilter != null && xbFilter.getSpatialOps() != null) {
            List<IDecoder> decoderList =
                    Configurator.getInstance().getDecoder(xbFilter.getSpatialOps().getDomNode().getNamespaceURI());
            Object filter = null;
            for (IDecoder decoder : decoderList) {
                filter = decoder.decode(xbFilter.getSpatialOps());
                if (filter != null) {
                    break;
                }
            }
            if (filter != null && filter instanceof SpatialFilter) {
                return (SpatialFilter) filter;
            }
        }
        return null;
    }

    /**
     * Parses the spatial filters of a GetFeatureOfInterest request.
     * 
     * @param xbFilters
     *            XmlBean representing the spatial filter parameter of the
     *            request
     * @return Returns SpatialFilter created from the passed foi request
     *         parameter
     * @throws OwsExceptionReport
     *             if creation of the SpatialFilter failed
     */
    private List<SpatialFilter> parseSpatialFilters4GetFoi(
            net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter[] xbFilters) throws OwsExceptionReport {
        List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>(xbFilters.length);
        for (net.opengis.sos.x20.GetFeatureOfInterestType.SpatialFilter xbFilter : xbFilters) {
            List<IDecoder> decoderList =
                    Configurator.getInstance().getDecoder(xbFilter.getSpatialOps().getDomNode().getNamespaceURI());
            Object filter = null;
            for (IDecoder decoder : decoderList) {
                filter = decoder.decode(xbFilter.getSpatialOps());
                if (filter != null) {
                    break;
                }
            }
            if (filter != null && filter instanceof SpatialFilter) {
                spatialFilters.add((SpatialFilter) filter);
            }
        }
        return spatialFilters;
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
    private TemporalFilter[] parseTemporalFilters(
            net.opengis.sos.x20.GetObservationType.TemporalFilter[] temporalFilters) throws OwsExceptionReport {
        List<TemporalFilter> sosTemporalFilters = new ArrayList<TemporalFilter>();
        for (net.opengis.sos.x20.GetObservationType.TemporalFilter temporalFilter : temporalFilters) {
            List<IDecoder> decoderList =
                    Configurator.getInstance().getDecoder(
                            temporalFilter.getTemporalOps().getDomNode().getNamespaceURI());
            Object filter = null;
            for (IDecoder decoder : decoderList) {
                filter = decoder.decode(temporalFilter.getTemporalOps());
            }
            if (filter != null && filter instanceof TemporalFilter) {
                sosTemporalFilters.add((TemporalFilter) filter);
            }
        }
        return sosTemporalFilters.toArray(new TemporalFilter[0]);
    }
    
}
