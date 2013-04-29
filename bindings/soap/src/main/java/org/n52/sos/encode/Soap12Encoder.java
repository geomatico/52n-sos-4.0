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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.soap.SoapFault;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.Envelope;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.Fault;
import org.w3.x2003.x05.soapEnvelope.FaultDocument;
import org.w3.x2003.x05.soapEnvelope.Faultcode;
import org.w3.x2003.x05.soapEnvelope.Reasontext;
import org.w3.x2003.x05.soapEnvelope.Subcode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Soap12Encoder extends AbstractSoapEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(Soap12Encoder.class);

    public Soap12Encoder() {
        super(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                     StringHelper.join(", ", getEncoderKeyType()));
    }


    @Override
    public ServiceResponse encode(SoapResponse response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (response == null) {
            throw new UnsupportedEncoderInputException(this, response);
        }
        String action = null;
        EnvelopeDocument envelopeDoc = EnvelopeDocument.Factory.newInstance();
        Envelope envelope = envelopeDoc.addNewEnvelope();
        Body body = envelope.addNewBody();
        if (response.getSoapFault() != null) {
            body.set(createSOAP12Fault(response.getSoapFault()));
        } else {
            if (response.getException() != null) {
                if (!response.getException().getExceptions().isEmpty()) {
                    CodedException firstException = response.getException().getExceptions().get(0);
                    action = getExceptionActionURI(firstException.getCode());
                }
                body.set(createSOAP12FaultFromExceptionResponse(response.getException()));
                List<String> schemaLocations = new ArrayList<String>(2);
                schemaLocations.add(N52XmlHelper.getSchemaLocationForSOAP12());
                schemaLocations.add(N52XmlHelper.getSchemaLocationForOWS110Exception());
                N52XmlHelper.setSchemaLocationsToDocument(envelopeDoc, schemaLocations);
            } else {
                action = response.getSoapAction();
                XmlObject bodyContent = createSOAP12Body(response.getSoapBodyContent());
                String value = null;
                Node nodeToRemove = null;
                NamedNodeMap attributeMap = bodyContent.getDomNode().getFirstChild().getAttributes();
                for (int i = 0; i < attributeMap.getLength(); i++) {
                    Node node = attributeMap.item(i);
                    if (node.getLocalName().equals(W3CConstants.AN_SCHEMA_LOCATION)) {
                        value = node.getNodeValue();
                        nodeToRemove = node;
                    }
                }
                if (nodeToRemove != null) {
                    attributeMap.removeNamedItem(nodeToRemove.getNodeName());
                }
                List<String> schemaLocations = new ArrayList<String>(2);
                schemaLocations.add(N52XmlHelper.getSchemaLocationForSOAP12());
                if (value != null && !value.isEmpty()) {
                    schemaLocations.add(value);
                }
                N52XmlHelper.setSchemaLocationsToDocument(envelopeDoc, schemaLocations);
                body.set(bodyContent);
            }
        }

        // if (response.getHeader() != null) {
        // Map<String, SoapHeader> headers = response.getHeader();
        // for (String namespace : headers.keySet()) {
        // SoapHeader header = headers.get(namespace);
        // if (namespace.equals(WsaConstants.NS_WSA)) {
        // WsaHeader wsa = (WsaHeader) header;
        // wsa.setActionValue(action);
        // }
        // try {
        // Encoder encoder = Configurator.getInstance().getEncoder(namespace);
        // if (encoder != null) {
        // Map<QName, String> headerElements = (Map<QName, String>)
        // encoder.encode(header);
        // for (QName qName : headerElements.keySet()) {
        // soapResponseMessage.getSOAPHeader().addChildElement(qName)
        // .setTextContent(headerElements.get(qName));
        // }
        // }
        // } catch (OwsExceptionReport owse) {
        // throw owse;
        // }
        // }
        //
        // } else {
        // soapResponseMessage.getSOAPHeader().detachNode();
        // }

        // TODO for testing an validating
        // checkAndValidateSoapMessage(envelopeDoc);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            envelopeDoc.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());

            boolean applicationZip = false;
            if (response.getSoapBodyContent() != null) {
                applicationZip = response.getSoapBodyContent().getApplyGzipCompression();
            }
            return new ServiceResponse(baos, SosConstants.CONTENT_TYPE_XML, applicationZip, true);
        } catch (IOException e) {
            throw new NoApplicableCodeException().causedBy(e)
                    .withMessage("Error while encoding SOAPMessage!");
        }
    }

    private XmlObject createSOAP12Body(ServiceResponse response) throws OwsExceptionReport {
        try {
            return XmlObject.Factory.parse(new String(response.getByteArray()), XmlOptionsHelper.getInstance()
                    .getXmlOptions());
        } catch (XmlException xmle) {
            throw new NoApplicableCodeException().causedBy(xmle)
                    .withMessage("Error while creating SOAP body!");
        }
    }

    private XmlObject createSOAP12Fault(SoapFault soapFault) {
        FaultDocument faultDoc = FaultDocument.Factory.newInstance();
        Fault fault = faultDoc.addNewFault();
        fault.addNewCode().setValue(soapFault.getFaultCode());
        Reasontext addNewText = fault.addNewReason().addNewText();
        addNewText.setLang(soapFault.getLocale().getDisplayLanguage());
        addNewText.setStringValue(soapFault.getFaultReason());
        if (soapFault.getDetailText() != null) {
            XmlString xmlString = XmlString.Factory.newInstance();
            xmlString.setStringValue(soapFault.getDetailText());
            fault.addNewDetail().set(xmlString);
        }
        return faultDoc;
    }

    private XmlObject createSOAP12FaultFromExceptionResponse(OwsExceptionReport owsExceptionReport) throws
            OwsExceptionReport {
        FaultDocument faultDoc = FaultDocument.Factory.newInstance();
        Fault fault = faultDoc.addNewFault();
        Faultcode code = fault.addNewCode();
        code.setValue(SOAPConstants.SOAP_SENDER_FAULT);

        if (!owsExceptionReport.getExceptions().isEmpty()) {
            CodedException firstException = owsExceptionReport.getExceptions().get(0);
            Subcode subcode = code.addNewSubcode();
            QName qName;
            if (firstException.getCode() != null) {
                qName = OwsHelper.getQNameForLocalName(firstException.getCode().toString());
            } else {
                qName = OwsHelper.getQNameForLocalName(OwsExceptionCode.NoApplicableCode.name());
            }
            subcode.setValue(qName);
            Reasontext addNewText = fault.addNewReason().addNewText();
            addNewText.setLang(Locale.ENGLISH.getLanguage());
            addNewText.setStringValue(SoapHelper.getSoapFaultReasonText(firstException.getCode()));

            for (OwsExceptionReport owsException : owsExceptionReport.getExceptions()) {
                fault.addNewDetail().set(CodingHelper.encodeObjectToXml(OWSConstants.NS_OWS, owsException));
                break;
            }
        }
        return faultDoc;
    }

//    private void checkAndValidateSoapMessage(XmlObject response) {
//        try {
//            XmlHelper.validateDocument(response);
//        } catch (OwsExceptionReport e) {
//            LOGGER.info("Error while checking SOAP response", e);
//        }
//    }
}
