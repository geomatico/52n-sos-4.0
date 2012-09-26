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
package org.n52.sos.soap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.n52.sos.exception.IExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.Operations;
import org.n52.sos.ogc.sos.SosConstants.SosExceptionCode;
import org.n52.sos.ogc.sos.SosSoapConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SwesExceptionCode;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

/**
 * Utility class for SOAP requests
 * 
 */
public class SoapHelper {

    /** the logger, used to log exceptions and additonaly information */
    private static final Logger LOGGER = LoggerFactory.getLogger(SoapHelper.class);

    /**
     * Checks the HTTP-Header for action or SOAPAction elements.
     * 
     * @param request
     *            HTTP request
     * @return SOAP action element
     */
    public static String checkSoapHeader(HttpServletRequest request) {
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerNameKey = (String) headerNames.nextElement();
            if (headerNameKey.equalsIgnoreCase("type")) {
                String type = request.getHeader(headerNameKey);
                String[] typeArray = type.split(";");
                for (String string : typeArray) {
                    if (string.startsWith("action")) {
                        String soapAction = string.replace("action=", "");
                        soapAction = soapAction.replace("\"", "");
                        soapAction = soapAction.trim();
                        return soapAction;
                    }
                }
            } else if (headerNameKey.equalsIgnoreCase("SOAPAction")) {
                return request.getHeader(headerNameKey);
            }
        }
        return null;
    }

    /**
     * Get text content from element by namespace.
     * 
     * @param soapHeader
     *            SOAPHeader element
     * @param namespaceURI
     *            Namespace URI
     * @param localName
     *            local name
     * @return Text content.
     */
    public static String getContentFromElement(SOAPHeader soapHeader, String namespaceURI, String localName) {
        String elementContent = null;
        NodeList nodes = soapHeader.getElementsByTagNameNS(namespaceURI, localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            elementContent = nodes.item(i).getTextContent();
        }
        return elementContent;
    }

    /**
     * Creates a SOAP message for SOAP 1.2 or 1.1
     * 
     * @param version
     *            SOAP version
     * @return Version depending SOAP message
     * @throws SOAPException
     *             if an error occurs.
     */
    public static SOAPMessage getSoapMessageForProtocol(String soapVersion) throws SOAPException {
        return MessageFactory.newInstance(soapVersion).createMessage();
    }

    public static SOAPMessage getSoapMessageForProtocol(String soapVersion, InputStream inputStream)
            throws SOAPException, IOException {
        return MessageFactory.newInstance(soapVersion).createMessage(new MimeHeaders(), inputStream);
    }

    /**
     * Get SOAP action URI depending on Exception code
     * 
     * @param exceptionCode
     *            Exception code
     * @return SOAP action URI
     */
    public static String getExceptionActionURI(IExceptionCode exceptionCode) {
        if (exceptionCode.equals(OwsExceptionCode.InvalidParameterValue)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(SwesExceptionCode.InvalidRequest)) {
            return SosSoapConstants.RESP_ACTION_SWES;
        } else if (exceptionCode.equals(OwsExceptionCode.InvalidUpdateSequence)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(OwsExceptionCode.MissingParameterValue)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(OwsExceptionCode.NoApplicableCode)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(OwsExceptionCode.NoDataAvailable)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(OwsExceptionCode.OperationNotSupported)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(OwsExceptionCode.OptionNotSupported)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(SwesExceptionCode.RequestExtensionNotSupported)) {
            return SosSoapConstants.RESP_ACTION_SWES;
        } else if (exceptionCode.equals(OwsExceptionCode.VersionNegotiationFailed)) {
            return SosSoapConstants.RESP_ACTION_OWS;
        } else if (exceptionCode.equals(SosExceptionCode.InvalidPropertyOfferingCombination)) {
            return SosSoapConstants.RESP_ACTION_SOS;
        } else if (exceptionCode.equals(SosExceptionCode.ResponseExceedsSizeLimit)) {
            return SosSoapConstants.RESP_ACTION_SOS;
        } else {
            return SosSoapConstants.RESP_ACTION_OWS;
        }
    }

    /**
     * Get the reason for a SOAP fault from Exception code
     * 
     * @param exceptionCode
     *            OWS exception code to get reason for.
     * @return Text for SOAP fault reason
     */
    public static String getSoapFaultReasonText(IExceptionCode exceptionCode) {
        if (exceptionCode.equals(OwsExceptionCode.InvalidParameterValue)) {
            return OWSConstants.SOAP_REASON_INVALID_PARAMETER_VALUE;
        } else if (exceptionCode.equals(SwesExceptionCode.InvalidRequest)) {
            return SWEConstants.SOAP_REASON_INVALID_REQUEST;
        } else if (exceptionCode.equals(OwsExceptionCode.InvalidUpdateSequence)) {
            return OWSConstants.SOAP_REASON_INVALID_UPDATE_SEQUENCES;
        } else if (exceptionCode.equals(OwsExceptionCode.MissingParameterValue)) {
            return OWSConstants.SOAP_REASON_MISSING_PARAMETER_VALUE;
        } else if (exceptionCode.equals(OwsExceptionCode.NoApplicableCode)) {
            return OWSConstants.SOAP_REASON_NO_APPLICABLE_CODE;
        } else if (exceptionCode.equals(OwsExceptionCode.NoDataAvailable)) {
            return OWSConstants.SOAP_REASON_NO_DATA_AVAILABLE;
        } else if (exceptionCode.equals(OwsExceptionCode.OperationNotSupported)) {
            return OWSConstants.SOAP_REASON_OPERATION_NOT_SUPPORTED;
        } else if (exceptionCode.equals(OwsExceptionCode.OptionNotSupported)) {
            return OWSConstants.SOAP_REASON_OPTION_NOT_SUPPORTED;
        } else if (exceptionCode.equals(SwesExceptionCode.RequestExtensionNotSupported)) {
            return OWSConstants.SOAP_REASON_REQUEST_EXTENSION_NOT_SUPPORTED;
        } else if (exceptionCode.equals(OwsExceptionCode.VersionNegotiationFailed)) {
            return OWSConstants.SOAP_REASON_VERSION_NEGOTIATION_FAILED;
        } else if (exceptionCode.equals(SosExceptionCode.InvalidPropertyOfferingCombination)) {
            return OWSConstants.SOAP_REASON_INVALID_PROPERTY_OFFERING_COMBINATION;
        } else if (exceptionCode.equals(SosExceptionCode.ResponseExceedsSizeLimit)) {
            return OWSConstants.SOAP_REASON_RESPONSE_EXCEEDS_SIZE_LIMIT;
        } else {
            return OWSConstants.SOAP_REASON_UNKNOWN;
        }
    }

    public static String checkActionURIWithBodyContent(String soapAction, String operationName)
            throws OwsExceptionReport {
        if (soapAction != null && !soapAction.isEmpty()) {
            if (operationName.equals(Operations.GetCapabilities.name())
                    && soapAction.equals(SosSoapConstants.REQ_ACTION_GETCAPABILITIES)) {
                LOGGER.debug("ActionURI and SOAPBody content are valid!");
                return SosSoapConstants.RESP_ACTION_GETCAPABILITIES;
            } else if (operationName.equals(Operations.DescribeSensor.name())
                    && soapAction.equals(SosSoapConstants.REQ_ACTION_DESCRIBESENSOR)) {
                LOGGER.debug("ActionURI and SOAPBody content are valid!");
                return SosSoapConstants.RESP_ACTION_DESCRIBESENSOR;
            } else if (operationName.equals(Operations.GetObservation.name())
                    && soapAction.equals(SosSoapConstants.REQ_ACTION_GETOBSERVATION)) {
                LOGGER.debug("ActionURI and SOAPBody content are valid!");
                return SosSoapConstants.RESP_ACTION_GETOBSERVATION;
            } else {
                throw Util4Exceptions.createNoApplicableCodeException(null, "Error while actionURI (" + soapAction
                        + ") is not compatible with the SOAPBody content (" + operationName + " request)!");
            }
        }
        return null;
    }

    // private ISOAPDecoder soapDecoder;
    //
    // private DocumentBuilderFactory docBuildFactory;
    //
    // private static final String acceptedVersion100 = ">"
    // + Sos1Constants.SERVICEVERSION + "<";
    //
    // private static final String acceptedVersion200 = ">"
    // + Sos2Constants.SERVICEVERSION + "<";
    //
    // /**
    // * handles all POST requests, the request will be passed to the
    // * requestOperator
    // *
    // * @param req
    // * the incomming request
    // *
    // * @param resp
    // * the response for the incoming request
    // */
    // public void doPost(HttpServletRequest req, HttpServletResponse resp) {
    //
    // LOGGER.debug("\n**********\n(POST) Connected from: "
    // + req.getRemoteAddr() + " " + req.getRemoteHost());
    //
    // this.setCorsHeaders(resp);
    //
    // // Set service URL in configurator
    // if (SosConfigurator.getInstance().getServiceURL() == null) {
    // SosConfigurator.getInstance().setServiceURL(
    // req.getRequestURL().toString());
    // }
    //
    // SOAPMessage soapMessageRequest = null;
    // ISosResponse sosResp = null;
    // SOAPFault fault = null;
    // String inputString = "";
    // BufferedReader br = null;
    // try {
    // String encoding = req.getCharacterEncoding();
    // if (encoding == null) {
    // encoding = SosConfigurator.getInstance().getCharacterEncoding();
    // }
    // LOGGER.debug(req.getCharacterEncoding());
    //
    // InputStream in = req.getInputStream();
    //
    // br = new BufferedReader(new InputStreamReader(in));
    // String line;
    // StringBuffer sb = new StringBuffer();
    // while ((line = br.readLine()) != null) {
    // sb.append(line);
    // sb.append("\n");
    // }
    // inputString = sb.toString();
    // LOGGER.debug("New Post Request is:" + inputString);
    //
    // // discard "request="-Input String header
    // if (inputString.startsWith("request=")) {
    // inputString = inputString.substring(8, inputString.length());
    // inputString = java.net.URLDecoder.decode(inputString, encoding);
    // LOGGER.debug("Decoded Post Request is: " + inputString);
    // }
    //
    // // parse request to Document
    // Document fullRequestDoc = null;
    // try {
    // synchronized (docBuildFactory) {
    // DocumentBuilder docBuilder = docBuildFactory
    // .newDocumentBuilder();
    // fullRequestDoc = docBuilder.parse(new ByteArrayInputStream(
    // inputString.getBytes(encoding)));
    // }
    // } catch (ParserConfigurationException pce) {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(
    // OwsExceptionCode.InvalidRequest, null,
    // pce.getMessage());
    // LOGGER.error("Error while parsing request!", pce);
    // throw se;
    // } catch (SAXException saxe) {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(
    // OwsExceptionCode.InvalidRequest, null,
    // saxe.getMessage());
    // LOGGER.error("Error while parsing request!", saxe);
    // throw se;
    // } finally {
    // if (br != null) {
    // br.close();
    // }
    // }
    //
    // fullRequestDoc.getDocumentElement().normalize();
    //
    // // checks if inputString is a SOAP envelope
    // if (fullRequestDoc.getDocumentElement().getLocalName()
    // .equalsIgnoreCase("Envelope")) {
    // String soapAction = "";
    // // SOAP 1.2
    // if (fullRequestDoc
    // .getDocumentElement()
    // .getNamespaceURI()
    // .equalsIgnoreCase(
    // SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
    // soapMessageRequest = SosConfigurator
    // .getInstance()
    // .getSoap12MeassageFactory()
    // .createMessage(
    // new MimeHeaders(),
    // new ByteArrayInputStream(inputString
    // .getBytes()));
    // Enumeration<?> headerNames = req.getHeaderNames();
    // while (headerNames.hasMoreElements()) {
    // String headerNameKey = (String) headerNames
    // .nextElement();
    // if (headerNameKey.equalsIgnoreCase("type")) {
    // String type = req.getHeader(headerNameKey);
    // String[] typeArray = type.split(";");
    // for (String string : typeArray) {
    // if (string.startsWith("action")) {
    // soapAction = soapAction.replace("action=",
    // "");
    // soapAction = soapAction.replace("\"", "");
    // soapAction = soapAction.trim();
    // }
    // }
    // break;
    // }
    // }
    // }
    // // SOAP 1.1
    // else if (fullRequestDoc
    // .getDocumentElement()
    // .getNamespaceURI()
    // .equalsIgnoreCase(
    // SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
    // soapMessageRequest = SosConfigurator
    // .getInstance()
    // .getSoap11MeassageFactory()
    // .createMessage(
    // new MimeHeaders(),
    // new ByteArrayInputStream(inputString
    // .getBytes()));
    // // look for SOAPAction param in HTTP-Header
    // Enumeration<?> headerNames = req.getHeaderNames();
    // while (headerNames.hasMoreElements()) {
    // String headerNameKey = (String) headerNames
    // .nextElement();
    // if (headerNameKey.equalsIgnoreCase("SOAPAction")) {
    // soapAction = req.getHeader(headerNameKey);
    // break;
    // }
    // }
    // // if SOAPAction is not spec conform, create SOAPFault
    // if (soapAction.equals("")
    // || !soapAction.startsWith("SOAPAction:")) {
    // // TODO: Exception
    // fault = soapMessageRequest.getSOAPBody().addFault();
    // QName qname = new QName(soapMessageRequest
    // .getSOAPPart().getEnvelope().getNamespaceURI(),
    // "Client", soapMessageRequest.getSOAPPart()
    // .getEnvelope().getPrefix());
    // fault.setFaultCode(qname);
    // fault.setFaultString(
    // "The SOAPAction parameter in the HTTP-Header is missing or not valid!",
    // Locale.ENGLISH);
    // }
    // // trim SOAPAction value
    // else {
    // soapAction = soapAction.replace("\"", "");
    // soapAction = soapAction.replace(" ", "");
    // soapAction = soapAction.replace("SOAPAction:", "");
    // soapAction = soapAction.trim();
    // }
    // }
    // // Not known SOAP version
    // else {
    // soapMessageRequest = SosConfigurator.getInstance()
    // .getSoap11MeassageFactory().createMessage();
    // soapMessageRequest.getSOAPHeader().detachNode();
    // fault = soapMessageRequest.getSOAPBody().addFault();
    // QName qname = new QName(soapMessageRequest.getSOAPPart()
    // .getEnvelope().getNamespaceURI(),
    // SOAPConstants.SOAP_VERSIONMISMATCH_FAULT
    // .getLocalPart(), soapMessageRequest
    // .getSOAPPart().getEnvelope().getPrefix());
    // fault.setFaultCode(qname);
    // fault.setFaultString("The SOAP version is unknown!",
    // Locale.ENGLISH);
    // Detail detail = fault.addDetail();
    // detail.setTextContent("The SOAP versions with SOAPNamespace '"
    // + fullRequestDoc.getDocumentElement()
    // .getNamespaceURI()
    // + "' is unknown! Valid versions are SOAP 1.1 with SOAPNamespace '"
    // + SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE
    // + "' and SOAP 1.2 with SOAPNamespace '"
    // + SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + "'!");
    // }
    // // if no fault, get document from SOAPBody
    // if (fault == null) {
    // soapAction = soapAction.replace("\"", "");
    // String[] saArray = soapAction.split("#");
    // String localNameRequestType = saArray[saArray.length - 1]
    // .trim();
    // if (soapMessageRequest.getSOAPBody() != null) {
    // Document bodyRequestDoc = soapMessageRequest
    // .getSOAPBody().extractContentAsDocument();
    // Element rootRequestDoc = bodyRequestDoc
    // .getDocumentElement();
    // if (localNameRequestType != null
    // && !localNameRequestType.equals("")) {
    // // check if SOAPAction is valid with
    // // SOAPBodyElement, else create SOAPFault
    // if (!rootRequestDoc.getLocalName()
    // .equalsIgnoreCase(localNameRequestType)
    // || !saArray[0]
    // .equals(OMConstants.NS_SOS_V1)) {
    // // TODO: Exception, different params
    // SOAPBody body = soapMessageRequest
    // .getSOAPBody();
    // fault = body.addFault();
    // if (soapMessageRequest
    // .getSOAPPart()
    // .getEnvelope()
    // .getNamespaceURI()
    // .equalsIgnoreCase(
    // SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
    // QName qname = new QName(soapMessageRequest
    // .getSOAPPart().getEnvelope()
    // .getNamespaceURI(), "Client",
    // soapMessageRequest.getSOAPPart()
    // .getEnvelope().getPrefix());
    // fault.setFaultCode(qname);
    // } else {
    // fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
    // }
    // fault.setFaultString(
    // "The ActionURI value is not valid with the SOAPBodyElement!",
    // Locale.ENGLISH);
    // if (!rootRequestDoc.getLocalName()
    // .equalsIgnoreCase(localNameRequestType)
    // && !saArray[0]
    // .equals(OMConstants.NS_SOS_V1)) {
    // fault.addDetail()
    // .setTextContent(
    // "The local names are different. LocalName is '"
    // + rootRequestDoc
    // .getLocalName()
    // + "' and the SOAPAction value is '"
    // + localNameRequestType
    // +
    // "'! And the NamespareURIs are different. The SOAPAction NamespaceURI is '"
    // + saArray[0]
    // + "' and not '"
    // + OMConstants.NS_SOS_V1
    // + "'!");
    // } else if (!saArray[0]
    // .equals(OMConstants.NS_SOS_V1)) {
    // fault.addDetail().setTextContent(
    // "The NamespareURIs are different. The SOAPAction NamespaceURI is '"
    // + saArray[0]
    // + "' and not '"
    // + OMConstants.NS_SOS_V1
    // + "'!");
    // } else {
    // fault.addDetail()
    // .setTextContent(
    // "The local names are different. LocalName is '"
    // + rootRequestDoc
    // .getLocalName()
    // + "' and the SOAPAction value is '"
    // + localNameRequestType
    // + "'!");
    // }
    // }
    // }
    // // add missing param in SOS request
    // if (!rootRequestDoc.hasAttribute("service")) {
    // rootRequestDoc.setAttribute("service", "SOS");
    // }
    // inputString = nodeToXmlString(rootRequestDoc);
    // sosResp = SosConfigurator.getInstance()
    // .getRequestOperator()
    // .doPostOperation(inputString);
    // }
    // // if mandatory SOAPBody is missing, create SOAPFault
    // else {
    // if (soapMessageRequest
    // .getSOAPPart()
    // .getEnvelope()
    // .getNamespaceURI()
    // .equalsIgnoreCase(
    // SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
    // soapMessageRequest = SosConfigurator.getInstance()
    // .getSoap11MeassageFactory().createMessage();
    // fault = soapMessageRequest.getSOAPBody().addFault();
    // QName qname = new QName(soapMessageRequest
    // .getSOAPPart().getEnvelope()
    // .getNamespaceURI(),
    // SOAPConstants.SOAP_VERSIONMISMATCH_FAULT
    // .getLocalPart(), soapMessageRequest
    // .getSOAPPart().getEnvelope()
    // .getPrefix());
    // fault.setFaultCode(qname);
    // } else {
    // soapMessageRequest = SosConfigurator.getInstance()
    // .getSoap12MeassageFactory().createMessage();
    // fault = soapMessageRequest.getSOAPBody().addFault();
    // fault.setFaultCode(SOAPConstants.SOAP_VERSIONMISMATCH_FAULT);
    //
    // }
    // fault.setFaultString(
    // "The Element 'SOAPBody' is missing in the request!",
    // Locale.ENGLISH);
    // }
    // }
    // doSoapResponse(resp, sosResp, soapMessageRequest, fault);
    // } else {
    // sosResp = SosConfigurator.getInstance().getRequestOperator()
    // .doPostOperation(inputString);
    // doResponse(resp, sosResp);
    // }
    // } catch (IOException ioe) {
    // LOGGER.error("Could not open input stream from request!");
    // } catch (SOAPException soape) {
    // LOGGER.error("Error in SOAP request!", soape);
    // } catch (OwsExceptionReport owse) {
    // if (SosConfigurator.getInstance().getSupportedVersions()
    // .equals(SosConstants.Versions.SOS_1)
    // || (inputString.contains(acceptedVersion100) && !inputString
    // .contains(acceptedVersion200))
    // || (inputString.contains(acceptedVersion100)
    // && inputString.contains(acceptedVersion200) && inputString
    // .indexOf(acceptedVersion100) < inputString
    // .indexOf(acceptedVersion200))
    // || inputString.contains(OMConstants.NS_SOS_V1)) {
    // owse.setVersion(Sos1Constants.SERVICEVERSION);
    // } else {
    // owse.setVersion(Sos2Constants.SERVICEVERSION);
    // }
    // if (inputString.contains(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)
    // || inputString
    // .contains(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
    // try {
    // if (inputString
    // .contains(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
    // soapMessageRequest = SosConfigurator.getInstance()
    // .getSoap11MeassageFactory().createMessage();
    // // fault = soapMessageRequest.getSOAPBody().addFault();
    // // QName qname =
    // // new
    // //
    // QName(soapMessageRequest.getSOAPPart().getEnvelope().getNamespaceURI(),
    // // "Client",
    // // soapMessageRequest.getSOAPPart().getEnvelope().getPrefix());
    // // fault.setFaultCode(qname);
    // } else if (inputString
    // .contains(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
    // soapMessageRequest = SosConfigurator.getInstance()
    // .getSoap12MeassageFactory().createMessage();
    // // fault = soapMessageRequest.getSOAPBody().addFault();
    // // fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
    // }
    // // StringBuilder faultString = new StringBuilder();
    // // for (ExceptionType exceptionType :
    // // owse.getDocument().getExceptionReport().getExceptionArray())
    // // {
    // // for (String exceptionText :
    // // exceptionType.getExceptionTextArray()) {
    // // faultString.append(exceptionText + "\n");
    // // }
    // // faultString.append("\n");
    // // }
    // // fault.setFaultString(faultString.toString());
    // } catch (SOAPException soape) {
    // LOGGER.error("Error in SOAP request!", soape);
    // }
    // doSoapResponse(resp, createExceptionResponse(owse),
    // soapMessageRequest, fault);
    // } else {
    // doResponse(resp, createExceptionResponse(owse));
    // }
    //
    // }
    // }
    //
    // /**
    // * handles all GET requests, the request will be passed to the
    // * RequestOperator
    // *
    // * @param req
    // * the incoming request
    // *
    // * @param resp
    // * the response for the incomming request
    // *
    // */
    // public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    //
    // this.setCorsHeaders(resp);
    //
    // String request = null;
    // request = req.getParameter(SosConstants.REQUEST);
    //
    // // Set service URL in configurator
    // if (SosConfigurator.getInstance().getServiceURL() == null) {
    // SosConfigurator.getInstance().setServiceURL(
    // req.getRequestURL().toString());
    // }
    //
    // // ////////////////////////////////////////////
    // // methods for refreshing cached data from database
    // if (request != null && request.equals(SosConstants.REFRESH_REQUEST)) {
    // try {
    // SosConfigurator.getInstance().getCapsCacheController()
    // .update(false); // refreshMetadata();
    // } catch (OwsExceptionReport se) {
    // doResponse(resp, createExceptionResponse(se));
    // }
    // }
    //
    // // /////////////////////////////////////////////
    // // method for refreshing cached fois from database
    // else if (request != null
    // && request.equals(SosConstants.REFRESH_FOIS_REQUEST)) {
    // try {
    // SosConfigurator.getInstance().getCapsCacheController()
    // .updateFoisCache(); // refreshFOIs();
    // } catch (OwsExceptionReport se) {
    // doResponse(resp, createExceptionResponse(se));
    // }
    // }
    //
    // // ///////////////////////////////////////////////
    // // forward GET-request to RequestOperator
    // else {
    // LOGGER.debug("\n**********\n(GET) Connected from: "
    // + req.getRemoteAddr() + " " + req.getRemoteHost());
    // LOGGER.trace("Query String: " + req.getQueryString());
    // ISosResponse sosResp = SosConfigurator.getInstance()
    // .getRequestOperator().doGetOperation(req);
    // doResponse(resp, sosResp);
    // }
    // }
    //
    // /**
    // * writes the content of the SosResponse to the outputStream of the
    // * HttpServletResponse
    // *
    // * @param resp
    // * the HttpServletResponse to which the content will be written
    // *
    // * @param sosResponse
    // * the SosResponse, whose content will be written to the
    // * outputStream of resp param
    // *
    // */
    // public void doResponse(HttpServletResponse resp, ISosResponse
    // sosResponse) {
    // OutputStream out = null;
    // GZIPOutputStream gzip = null;
    // try {
    // String contentType = sosResponse.getContentType();
    // int contentLength = sosResponse.getContentLength();
    // resp.setContentLength(contentLength);
    // out = resp.getOutputStream();
    // resp.setContentType(contentType);
    // sosResponse.writeToOutputStream(out);
    // out.flush();
    // } catch (IOException ioe) {
    // LOGGER.error("doResponse", ioe);
    // } finally {
    // try {
    // if (gzip != null) {
    // gzip.close();
    // }
    // if (out != null) {
    // out.close();
    // }
    // } catch (IOException ioe) {
    // LOGGER.error("doSoapResponse, close streams", ioe);
    // }
    // }
    // }
    //
    // /**
    // * writes the content of the SosResponse as SOAPMessage to the
    // outputStream
    // * of the HttpServletResponse
    // *
    // * @param resp
    // * the HttpServletResponse to which the content will be written
    // *
    // * @param sosResponse
    // * the SosResponse, whose content will be written to the
    // * outputStream of resp param
    // *
    // * @param soapRequestMessage
    // * the SOAP request message, used for response
    // */
    // public void doSoapResponse(HttpServletResponse resp,
    // ISosResponse sosResponse, SOAPMessage soapRequestMessage,
    // SOAPFault soapFault) {
    // ByteArrayOutputStream byteOutStream = null;
    // OutputStream out = null;
    // GZIPOutputStream gzip = null;
    // try {
    // SOAPMessage soapResponseMessage = soapRequestMessage;
    // // remove old SOAPBody contents
    // soapResponseMessage.getSOAPBody().removeContents();
    // String contentType = "text/xml";
    // boolean gzipCompression = false;
    //
    // // parse response as Document and add it to SOAPBody
    // // if SOAPFault, add Fault to SOAP response
    // if (soapFault != null) {
    // SOAPBody body = soapResponseMessage.getSOAPBody();
    // body.addChildElement(soapFault);
    // }
    // // add sosResponse to SOAPBody
    // else {
    // contentType = sosResponse.getContentType();
    // gzipCompression = sosResponse.getApplyGzipCompression();
    // // if sosResponse is an Exception
    // if (sosResponse instanceof SosExceptionResponse) {
    // createSOAPFaultFromExceptionResponse(soapResponseMessage
    // .getSOAPBody().addFault(), sosResponse);
    // }
    // // if sosResponse is a Response
    // else {
    // Document doc = null;
    // try {
    // synchronized (docBuildFactory) {
    // DocumentBuilder docBuilder = docBuildFactory
    // .newDocumentBuilder();
    // doc = docBuilder.parse(new ByteArrayInputStream(
    // sosResponse.getByteArray()));
    // }
    // } catch (ParserConfigurationException pce) {
    // LOGGER.error("Error while parsing request!", pce);
    // }
    // doc.getDocumentElement().normalize();
    // soapResponseMessage.getSOAPBody().addDocument(doc);
    // }
    // }
    // // remove SOAPAction from SOAPHeader
    // if (soapResponseMessage.getSOAPHeader() != null) {
    // NodeList nodes = soapResponseMessage.getSOAPHeader()
    // .getChildNodes();
    // for (int i = 0; i < nodes.getLength(); i++) {
    // if (nodes.item(i).getLocalName() != null
    // && nodes.item(i).getLocalName()
    // .equalsIgnoreCase("Action")) {
    // soapResponseMessage.getSOAPHeader().removeChild(
    // nodes.item(i));
    // }
    // }
    // }
    //
    // // response to OutputStream
    // byteOutStream = new ByteArrayOutputStream();
    // soapResponseMessage.writeTo(byteOutStream);
    //
    // int contentLength = byteOutStream.size();
    // byte[] bytes = byteOutStream.toByteArray();
    // resp.setContentLength(contentLength);
    // out = resp.getOutputStream();
    // if (gzipCompression) {
    // resp.setContentType(contentType);
    // gzip = new GZIPOutputStream(out);
    // gzip.write(bytes);
    // gzip.flush();
    // gzip.finish();
    // } else {
    // resp.setContentType(contentType);
    // out.write(bytes);
    // out.flush();
    // }
    // } catch (IOException ioe) {
    // LOGGER.error("doSoapResponse", ioe);
    // } catch (SOAPException soape) {
    // LOGGER.error("doSoapResponse", soape);
    // } catch (SAXException saxe) {
    // LOGGER.error("doSoapResponse", saxe);
    // } finally {
    // try {
    // if (gzip != null) {
    // gzip.close();
    // }
    // if (out != null) {
    // out.close();
    // }
    // if (byteOutStream != null) {
    // byteOutStream.close();
    // }
    // } catch (IOException ioe) {
    // LOGGER.error("doSoapResponse, close streams", ioe);
    // }
    // }
    // }
    //
    // /**
    // * Parses w3c.Node to String
    // *
    // * @param node
    // * Node to parse.
    // *
    // * @return Node as String.
    // * @throws OwsExceptionReport
    // */
    // private String nodeToXmlString(Node node) throws OwsExceptionReport {
    // String xmlString = "";
    // StringWriter sw = null;
    // try {
    // sw = new StringWriter();
    // Transformer t = TransformerFactory.newInstance().newTransformer();
    // t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    // t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    // t.transform(new DOMSource(node), new StreamResult(sw));
    // xmlString = sw.toString();
    // } catch (TransformerException te) {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(
    // OwsExceptionCode.InvalidRequest,
    // null,
    // "The request was sent in an unknown format or is invalid! Please use the SOS version 1.0 schemata to build your request and validate the requests before sending them to the SOS!");
    // LOGGER.error("nodeToString Transformer Exception", te);
    // throw se;
    // } finally {
    // try {
    // if (sw != null) {
    // sw.close();
    // }
    // } catch (IOException ioe) {
    // LOGGER.error("cannot close string writer", ioe);
    // }
    // }
    // return xmlString;
    // }
    //
    // /**
    // * Creates a SOAPFault element from SOS exception
    // *
    // * @param soapFault
    // * SOAPFault element
    // * @param sosResponse
    // * SOS exception
    // * @return SOAP action URI.
    // * @throws SOAPException
    // * if an error occurs.
    // */
    // private String createSOAPFaultFromExceptionResponse(SOAPFault soapFault,
    // ISosResponse sosResponse) throws SOAPException {
    // try {
    // if (sosResponse != null && sosResponse instanceof SosExceptionResponse) {
    // ExceptionReportDocument erd = ExceptionReportDocument.Factory
    // .parse(new ByteArrayInputStream(sosResponse
    // .getByteArray()));
    // ExceptionType exception = erd.getExceptionReport()
    // .getExceptionArray(0);
    // // set fault code (and subcode(SOAP 1.2)) element
    // if (soapFault.getNamespaceURI().equalsIgnoreCase(
    // SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
    // QName qname = new QName(soapFault.getNamespaceURI(),
    // "Client", soapFault.getPrefix());
    // soapFault.setFaultCode(qname);
    // } else {
    // soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
    // if (exception.getExceptionCode() != null
    // && !exception.getExceptionCode().isEmpty()) {
    // soapFault.appendFaultSubcode(new QName(
    // OMConstants.NS_OWS, exception
    // .getExceptionCode(),
    // OMConstants.NS_OWS_PREFIX));
    // } else {
    // soapFault
    // .appendFaultSubcode(new QName(
    // OMConstants.NS_OWS,
    // OwsExceptionCode.NoApplicableCode
    // .name(),
    // OMConstants.NS_OWS_PREFIX));
    // }
    // }
    // // set fault reason
    // soapFault.addFaultReasonText(
    // getSoapFaultReasonText(exception.getExceptionCode()),
    // Locale.ENGLISH);
    // // set fault detail
    // Detail detail = soapFault.addDetail();
    // createSOAPFaultDetail(detail, erd);
    // return getExceptionActionURI(exception.getExceptionCode());
    // }
    // } catch (XmlException xmle) {
    // LOGGER.error("Error while parsing request!", xmle);
    // } catch (IOException ioe) {
    // LOGGER.error("Error while parsing request!", ioe);
    // }
    // return null;
    // }
    //
    // /**
    // * Creates a SOAPDetail element from SOS exception document.
    // *
    // * @param detail
    // * SOAPDetail
    // * @param erd
    // * SOS Exception document
    // * @throws SOAPException
    // * if an error occurs.
    // */
    // private void createSOAPFaultDetail(Detail detail,
    // ExceptionReportDocument erd) throws SOAPException {
    // for (ExceptionType exceptionType : erd.getExceptionReport()
    // .getExceptionArray()) {
    // // SOAPElement exRep =
    // // detail.addChildElement(new QName(OMConstants.NS_OWS,
    // // exceptionType.getDomNode().getLocalName(),
    // // OMConstants.NS_OWS_PREFIX));
    // // exRep.addNamespaceDeclaration(OMConstants.NS_OWS_PREFIX,
    // // OMConstants.NS_OWS);
    // // exRep.setAttribute("version",
    // // erd.getExceptionReport().getVersion());
    // // if (erd.getExceptionReport().getLang() != null &&
    // // !erd.getExceptionReport().getLang().equals("")) {
    // // exRep.setAttribute("xml:lang",
    // // erd.getExceptionReport().getLang());
    // // }
    // String code = exceptionType.getExceptionCode();
    // String locator = exceptionType.getLocator();
    // StringBuffer text = new StringBuffer();
    // for (String textArrayText : exceptionType.getExceptionTextArray()) {
    // text.append(textArrayText);
    // }
    // SOAPElement exec = detail.addChildElement(new QName(
    // OMConstants.NS_OWS, erd.getExceptionReport()
    // .getExceptionArray(0).getDomNode().getLocalName(),
    // OMConstants.NS_OWS_PREFIX));
    // exec.addAttribute(new QName(OMConstants.NS_OWS, "exceptionCode",
    // OMConstants.NS_OWS_PREFIX), code);
    // if (locator != null && !locator.equals("")) {
    // exec.addAttribute(new QName(OMConstants.NS_OWS, "locator",
    // OMConstants.NS_OWS_PREFIX), locator);
    // }
    // if (text.length() != 0) {
    // SOAPElement execText = exec.addChildElement(new QName(
    // OMConstants.NS_OWS, "ExceptionText",
    // OMConstants.NS_OWS_PREFIX));
    // execText.setTextContent(text.toString());
    // }
    // }
    // }
    //
    // /**
    // * Get SOAP action URI depending on Exception code
    // *
    // * @param exceptionCode
    // * Exception code
    // * @return SOAP action URI
    // */
    // private String getExceptionActionURI(String exceptionCode) {
    // if (exceptionCode
    // .equals(OwsExceptionCode.InvalidParameterValue
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.InvalidRequest.name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.InvalidUpdateSequence
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.MissingParameterValue
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.NoApplicableCode
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.NoDataAvailable.name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.OperationNotSupported
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.OptionNotSupported
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.RequestExtensionNotSupported
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.VersionNegotiationFailed
    // .name())) {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // } else {
    // return SosSOAPConstants.RESP_ACTION_SWES;
    // }
    // }
    //
    // private ISosResponse createExceptionResponse(
    // OwsExceptionReport exceptionReport) {
    // ExceptionReportDocument erd = exceptionReport.getDocument();
    // XmlCursor cursor = erd.newCursor();
    // if (cursor.toFirstChild()) {
    // cursor.setAttributeText(new QName(
    // "http://www.w3.org/2001/XMLSchema-instance",
    // "schemaLocation"), OMConstants.NS_OWS + " "
    // + OMConstants.SCHEMA_LOCATION_OWS);
    // }
    // cursor.dispose();
    // ByteArrayOutputStream baos = new ByteArrayOutputStream();
    // try {
    // erd.save(baos, SosXmlUtilities.getInstance().getXmlOptions());
    // } catch (IOException ioe) {
    // LOGGER.info("Error while creating Exception response!", ioe);
    // }
    // return new SosExceptionResponse(baos, SosConstants.CONTENT_TYPE_XML,
    // false,
    // exceptionReport.getVersion());
    // }
    //
    // /**
    // * @param exceptionCode
    // * @return
    // */
    // private String getSoapFaultReasonText(String exceptionCode) {
    // if (exceptionCode
    // .equals(OwsExceptionCode.InvalidParameterValue
    // .name())) {
    // return "The request contained an invalid parameter value.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.InvalidRequest.name())) {
    // return "The request did not conform to its XML Schema definition.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.InvalidUpdateSequence
    // .name())) {
    // return
    // "The value of the updateSequence parameter in the GetCapabilities operation request was greater than the current value of the service metadata updateSequence number.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.MissingParameterValue
    // .name())) {
    // return
    // "The request did not include a value for a required parameter and this server does not declare a default value for it.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.NoApplicableCode
    // .name())) {
    // return "A server exception was encountered.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.NoDataAvailable.name())) {
    // return "There are no data available.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.OperationNotSupported
    // .name())) {
    // return "The requested operation is not supported by this server.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.OptionNotSupported
    // .name())) {
    // return
    // "The request included/targeted an option that is not supported by this server.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.RequestExtensionNotSupported
    // .name())) {
    // return
    // "The request included an extension that is not supported by this server.";
    // } else if (exceptionCode
    // .equals(OwsExceptionCode.VersionNegotiationFailed
    // .name())) {
    // return
    // "The list of versions in the â€˜AcceptVersionsâ€™ parameter value of the GetCapabilities operation request did not include any version supported by this server.";
    // } else {
    // return "A server exception was encountered.";
    // }
    // }
}
