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
package org.n52.sos.binding;

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPConstants;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.Decoder;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.decode.XmlOperationDecoderKey;
import org.n52.sos.encode.Encoder;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.MethodNotSupportedException;
import org.n52.sos.exception.ows.concrete.NoEncoderForKeyException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.operator.ServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapBinding extends Binding {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoapBinding.class);
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_SOAP_BINDING);
    private static final String urlPattern = "/soap";

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest request) throws OwsExceptionReport {
        SoapResponse soapResponse = new SoapResponse();
        OwsExceptionReport owse = new MethodNotSupportedException("SOAP", "GET");
        soapResponse.setException(owse);
        soapResponse.setSoapVersion(SOAPConstants.SOAP_1_2_PROTOCOL);
        soapResponse.setSoapNamespace(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);

        return (ServiceResponse) CodingHelper.encodeObjectToXml(soapResponse.getSoapNamespace(), soapResponse);
    }

    @Override
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport {
        String version = null;
        String soapVersion = null;
        String soapNamespace = null;
        SoapResponse soapResponse = new SoapResponse();
        try {
            String soapAction = SoapHelper.checkSoapHeader(request);
            XmlObject doc = XmlHelper.parseXmlSosRequest(request);
            LOGGER.debug("SOAP-REQUEST: {}", doc.xmlText());
            Decoder<?,XmlObject> decoder = getDecoder(CodingHelper.getDecoderKey(doc));
            // decode SOAP message
            Object abstractRequest = decoder.decode(doc);
            if (abstractRequest instanceof SoapRequest) {
                SoapRequest soapRequest = (SoapRequest) abstractRequest;
                if (soapRequest.getSoapAction() == null && soapAction != null) {
                    soapRequest.setAction(soapAction);
                }
                soapResponse.setSoapVersion(soapRequest.getSoapVersion());
                soapVersion = soapRequest.getSoapVersion();
                soapNamespace = soapRequest.getSoapNamespace();
                soapResponse.setSoapNamespace(soapNamespace);
                soapResponse.setHeader(soapRequest.getSoapHeader());
                if (soapRequest.getSoapFault() == null) {
                    XmlObject xmlObject = soapRequest.getSoapBodyContent();
                    Decoder<?, XmlObject> bodyDecoder = getDecoder(CodingHelper.getDecoderKey(xmlObject));
                    // Decode SOAPBody content
                    Object aBodyRequest = bodyDecoder.decode(xmlObject);
                    if (aBodyRequest instanceof AbstractServiceRequest) {
                        AbstractServiceRequest bodyRequest = (AbstractServiceRequest) aBodyRequest;
                        checkServiceOperatorKeyTypes(bodyRequest);
                        for (ServiceOperatorKeyType sokt : bodyRequest.getServiceOperatorKeyType()) {
                            ServiceOperator so = getServiceOperatorRepository().getServiceOperator(sokt);
                            if (so != null) {
                                ServiceResponse bodyResponse = so.receiveRequest(bodyRequest);
                                if (!bodyResponse.isXmlResponse()) {
                                    // FIXME how to encode non xml encoded data
                                    // in soap responses?
                                    return bodyResponse;
                                }
                                soapResponse.setSoapBodyContent(bodyResponse);
                                break;
                            }
                        }
                    } else {
                        throw new NoApplicableCodeException()
                                .withMessage("The returned object is not an AbstractServiceRequest implementation");
                    }
                } else {
                    soapResponse.setSoapFault(soapRequest.getSoapFault());
                }
                // Encode SOAP response
                EncoderKey key = CodingHelper.getEncoderKey(soapResponse.getSoapNamespace(), soapResponse);
                Encoder<?, SoapResponse> encoder = getEncoder(key);
                if (encoder != null) {
                    return (ServiceResponse) encoder.encode(soapResponse);
                } else {
                    throw new NoEncoderForKeyException(key);
                }
            } else {
                // FIXME: valid exception
                throw new NoApplicableCodeException();
            }
        } catch (Throwable t) {
            OwsExceptionReport owse;
            if (t instanceof OwsExceptionReport) {
                owse = (OwsExceptionReport) t;
            } else {
                owse = new NoApplicableCodeException().causedBy(t);
            }
            LOGGER.warn("Error processing request", owse);
            soapResponse.setException(owse.setVersion(version));
            if (soapVersion == null || !soapVersion.isEmpty()) {
                soapResponse.setSoapVersion(SOAPConstants.SOAP_1_2_PROTOCOL);
            } else {
                soapResponse.setSoapVersion(soapVersion);
            }
            if (soapNamespace == null || (soapVersion != null && !soapVersion.isEmpty())) {
                soapResponse.setSoapNamespace(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
            } else {
                soapResponse.setSoapNamespace(soapNamespace);
            }
            EncoderKey key = CodingHelper.getEncoderKey(soapResponse.getSoapNamespace(), soapResponse);
            Encoder<?, SoapResponse> encoder = getEncoder(key);
            if (encoder != null) {
                return (ServiceResponse) encoder.encode(soapResponse);
            } else {
                throw new NoEncoderForKeyException(key);
            }
        }
    }

    @Override
    public String getUrlPattern() {
        return urlPattern;
    }

    @Override
    public boolean checkOperationHttpPostSupported(OperationDecoderKey k) throws OwsExceptionReport {
        return getDecoder(new XmlOperationDecoderKey(k)) != null;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }
}
