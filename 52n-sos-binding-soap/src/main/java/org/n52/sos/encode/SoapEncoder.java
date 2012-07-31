package org.n52.sos.encode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosSoapConstants;
import org.n52.sos.response.IServiceResponse;
import org.n52.sos.response.SosResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHeader;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.wsa.WsaConstants;
import org.n52.sos.wsa.WsaHeader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SoapEncoder implements IEncoder<IServiceResponse, SoapResponse> {

    /** the logger, used to log exceptions and additonaly information */
    private static Logger LOGGER = Logger.getLogger(SoapEncoder.class);

    private List<EncoderKeyType> encoderKeyTypes;

    /**
     * constructor
     */
    public SoapEncoder() {
        super();
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE));
        encoderKeyTypes.add(new EncoderKeyType(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType);
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following key initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public IServiceResponse encode(SoapResponse response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public IServiceResponse encode(SoapResponse response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        SoapResponse soapResponse = (SoapResponse) response;
        String soapVersion = soapResponse.getSoapVersion();
        SOAPMessage soapResponseMessage = null;
        String action = null;
        try {
            soapResponseMessage = SoapHelper.getSoapMessageForProtocol(soapVersion);
            if (soapResponse.getSoapFault() != null) {
                createSOAPFault(soapResponseMessage.getSOAPBody().addFault(), soapResponse.getSoapFault());
            } else {
                // if (soapResponse.getSoapBodyContent() instanceof
                // IServiceResponse) {
                // action =
                // createSOAPFaultFromExceptionResponse(soapResponseMessage.getSOAPBody().addFault(),
                // soapResponse.getSosResponse());
                // } else {
                action =
                        createSOAPBody(soapResponseMessage, soapResponse.getSoapBodyContent(),
                                soapResponse.getSoapAction());
                // }
            }
            if (soapResponse.getHeader() != null) {
                Map<String, SoapHeader> headers = soapResponse.getHeader();
                for (String namespace : headers.keySet()) {
                    SoapHeader header = headers.get(namespace);
                    if (namespace.equals(WsaConstants.NS_WSA)) {
                        WsaHeader wsa = (WsaHeader) header;
                        wsa.setActionValue(action);
                    }
                    try {
                        IEncoder encoder = Configurator.getInstance().getEncoder(namespace);
                        if (encoder != null) {
                            Map<QName, String> headerElements = (Map<QName, String>) encoder.encode(header);
                            for (QName qName : headerElements.keySet()) {
                                soapResponseMessage.getSOAPHeader().addChildElement(qName).setTextContent(
                                        headerElements.get(qName));
                            }
                        }
                    } catch (OwsExceptionReport owse) {
                        throw owse;
                    }
                }

            } else {
                soapResponseMessage.getSOAPHeader().detachNode();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            soapResponseMessage.writeTo(baos);
            return new SosResponse(baos, SosConstants.CONTENT_TYPE_XML, soapResponse.getSoapBodyContent()
                    .getApplyGzipCompression(), null, true);
        } catch (SOAPException soape) {
            String exceptionText = "Error while encoding SOAPMessage!";
            LOGGER.debug(exceptionText, soape);
            throw Util4Exceptions.createNoApplicableCodeException(soape, exceptionText);
        } catch (IOException ioe) {
            String exceptionText = "Error while encoding SOAPMessage!";
            LOGGER.debug(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    /**
     * Creates a SOAPBody element from SOS response
     * 
     * @param soapResponseMessage
     *            SOAPBody element
     * @param sosResponse
     *            SOS response
     * @throws SOAPException
     *             if an error occurs.
     */
    private String createSOAPBody(SOAPMessage soapResponseMessage, IServiceResponse sosResponse, String actionURI)
            throws SOAPException {
        try {
            if (sosResponse != null) {

                XmlObject xmlObject = XmlObject.Factory.parse(new String(sosResponse.getByteArray()));
                if (xmlObject instanceof ExceptionReportDocument) {
                    return createSOAPFaultFromExceptionResponse(soapResponseMessage.getSOAPBody().addFault(),
                            xmlObject);
                } else {
                    addAndRemoveSchemaLocationForSOAP(xmlObject, soapResponseMessage);
                    // Document doc = parseSosResponseToDocument(xmlObject);
                    // soapResponseMessage.getSOAPBody().addDocument(doc);
                    soapResponseMessage.getSOAPBody().addDocument((Document) xmlObject.getDomNode());
                    return actionURI;
                }
            } else {
                SoapFault fault = new SoapFault();
                fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
                fault.setFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode.name(),
                        OWSConstants.NS_OWS_PREFIX));
                fault.setFaultReason("A server exception was encountered.");
                fault.setLocale(Locale.ENGLISH);
                fault.setDetailText("Missing SOS response document!");
                createSOAPFault(soapResponseMessage.getSOAPBody().addFault(), fault);
            }
        } catch (XmlException xmle) {
            LOGGER.error("Error while creating SOAP body !", xmle);
        }
        return null;
    }

    /**
     * Creates a SOAPFault element from SOS internal fault
     * 
     * @param fault
     *            SOAPFault element
     * @param sosSOAPFault
     *            SOS internal fault
     * @throws SOAPException
     *             if an error occurs.
     */
    private void createSOAPFault(SOAPFault fault, SoapFault soapFault) throws SOAPException {
        fault.setFaultCode(soapFault.getFaultCode());
        fault.setFaultString(soapFault.getFaultReason(), soapFault.getLocale());
        if (soapFault.getDetailText() != null) {
            fault.addDetail().setTextContent(soapFault.getDetailText());
        }
    }

    /**
     * Creates a SOAPFault element from SOS exception
     * 
     * @param soapFault
     *            SOAPFault element
     * @param sosResponse
     *            SOS exception
     * @return SOAP action URI.
     * @throws SOAPException
     *             if an error occurs.
     */
    private String createSOAPFaultFromExceptionResponse(SOAPFault soapFault, XmlObject xmlObject) throws SOAPException {
        if (xmlObject instanceof ExceptionReportDocument) {
            ExceptionReportDocument erd = (ExceptionReportDocument) xmlObject;
            if (erd.getExceptionReport() != null && erd.getExceptionReport().sizeOfExceptionArray() > 0) {
                ExceptionType exception = erd.getExceptionReport().getExceptionArray(0);
                // set fault code (and subcode(SOAP 1.2)) element
                if (soapFault.getNamespaceURI().equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
                    QName qname = new QName(soapFault.getNamespaceURI(), "Client", soapFault.getPrefix());
                    soapFault.setFaultCode(qname);
                } else {
                    soapFault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                    if (exception.getExceptionCode() != null && !exception.getExceptionCode().isEmpty()) {
                        soapFault.appendFaultSubcode(new QName(OWSConstants.NS_OWS, exception.getExceptionCode(),
                                OWSConstants.NS_OWS_PREFIX));
                    } else {
                        soapFault.appendFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode
                                .name(), OWSConstants.NS_OWS_PREFIX));
                    }
                }
                // set fault reason
                soapFault.addFaultReasonText(SoapHelper.getSoapFaultReasonText(exception.getExceptionCode()),
                        Locale.ENGLISH);
                // set fault detail
                Detail detail = soapFault.addDetail();
                createSOAPFaultDetail(detail, erd);
                return SoapHelper.getExceptionActionURI(exception.getExceptionCode());
            }
        }
        SoapFault fault = new SoapFault();
        fault.setFaultCode(SOAPConstants.SOAP_RECEIVER_FAULT);
        fault.setFaultSubcode(new QName(OWSConstants.NS_OWS, OwsExceptionCode.NoApplicableCode.name(),
                OWSConstants.NS_OWS_PREFIX));
        fault.setFaultReason("A server exception was encountered.");
        fault.setLocale(Locale.ENGLISH);
        fault.setDetailText("Error while creating SOAPFault element from OWSException! OWSException is missing!");
        createSOAPFault(soapFault, fault);
        return SosSoapConstants.RESP_ACTION_SOS;
    }

    /**
     * Creates a SOAPDetail element from SOS exception document.
     * 
     * @param detail
     *            SOAPDetail
     * @param erd
     *            SOS Exception document
     * @throws SOAPException
     *             if an error occurs.
     */
    private void createSOAPFaultDetail(Detail detail, ExceptionReportDocument erd) throws SOAPException {
        for (ExceptionType exceptionType : erd.getExceptionReport().getExceptionArray()) {
            SOAPElement exRep =
                    detail.addChildElement(new QName(OWSConstants.NS_OWS, exceptionType.getDomNode().getLocalName(),
                            OWSConstants.NS_OWS_PREFIX));
            exRep.addNamespaceDeclaration(OWSConstants.NS_OWS_PREFIX, OWSConstants.NS_OWS);
            if (erd.getExceptionReport().getLang() != null && !erd.getExceptionReport().getLang().equals("")) {
                exRep.setAttribute("xml:lang", erd.getExceptionReport().getLang());
            }
            String code = exceptionType.getExceptionCode();
            String locator = exceptionType.getLocator();
            StringBuffer text = new StringBuffer();
            for (String textArrayText : exceptionType.getExceptionTextArray()) {
                text.append(textArrayText);
            }
            exRep.addAttribute(new QName(OWSConstants.NS_OWS, "exceptionCode", OWSConstants.NS_OWS_PREFIX), code);
            if (locator != null && !locator.equals("")) {
                exRep.addAttribute(new QName(OWSConstants.NS_OWS, "locator", OWSConstants.NS_OWS_PREFIX), locator);
            }
            if (text.length() != 0) {
                SOAPElement execText =
                        exRep.addChildElement(new QName(OWSConstants.NS_OWS, "ExceptionText",
                                OWSConstants.NS_OWS_PREFIX));
                execText.setTextContent(text.toString());
            }
        }
    }

    /**
     * Check SOS response for xsi:schemaLocation, remove attribute and add
     * attribute to SOAP message
     * 
     * @param soapResponseMessage
     * @param xmlObject
     * 
     * @param SOS
     *            response
     * @param soapResponseMessage
     *            SOAP response message
     * @throws SOAPException
     *             If an error occurs
     */
    private void addAndRemoveSchemaLocationForSOAP(XmlObject xmlObject, SOAPMessage soapResponseMessage)
            throws SOAPException {
        String value = null;
        Node nodeToRemove = null;
        NamedNodeMap attributeMap = xmlObject.getDomNode().getFirstChild().getAttributes();
        for (int i = 0; i < attributeMap.getLength(); i++) {
            Node node = attributeMap.item(i);
            if (node.getLocalName().equals(W3CConstants.AN_SCHEMA_LOCATION)) {
                value=  node.getNodeValue();
                nodeToRemove = node;
            }
        }
        attributeMap.removeNamedItem(nodeToRemove.getNodeName());
        SOAPEnvelope envelope = soapResponseMessage.getSOAPPart().getEnvelope();
        StringBuilder string = new StringBuilder();
        if (soapResponseMessage.getSOAPPart().getEnvelope().getNamespaceURI()
                .equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE)) {
            string.append(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE + " http://schemas.xmlsoap.org/soap/envelope");
        } else if (soapResponseMessage.getSOAPPart().getEnvelope().getNamespaceURI()
                .equalsIgnoreCase(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE)) {
            string.append(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE + " http://www.w3.org/2003/05/soap-envelope");

        }
        if (value != null && !value.isEmpty()) {
            string.append(" ");
            string.append(value);
        }
        QName qName = new QName(W3CConstants.NS_XSI, W3CConstants.AN_SCHEMA_LOCATION, W3CConstants.NS_XSI_PREFIX);
        envelope.addAttribute(qName, string.toString());
        value = string.toString();
    }
}
