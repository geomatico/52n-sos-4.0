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
package org.n52.sos.binding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPConstants;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.decode.IXmlRequestDecoder;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapBinding extends Binding {

    /** the logger, used to log exceptions and additional information */
    private static final Logger LOGGER = LoggerFactory.getLogger(SoapBinding.class);

    private static final String urlPattern = "/soap";

    public SoapBinding() {
    }

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest request) throws OwsExceptionReport {
        SoapResponse soapResponse = new SoapResponse();
        String message = "HTTP GET is no supported for SOAP binding!";
        OwsExceptionReport owse = Util4Exceptions.createNoApplicableCodeException(null, message);
        if (Configurator.getInstance().isVersionSupported(Sos1Constants.SERVICEVERSION)) {
            owse.setVersion(Sos1Constants.SERVICEVERSION);
        } else {
            owse.setVersion(Sos2Constants.SERVICEVERSION);
        }
        soapResponse.setException(owse);
        soapResponse.setSoapVersion(SOAPConstants.SOAP_1_2_PROTOCOL);
        soapResponse.setSoapNamespace(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
        IEncoder encoder = Configurator.getInstance().getEncoder(soapResponse.getSoapNamespace());
        if (encoder != null) {
            return (ServiceResponse) encoder.encode(soapResponse);
        } else {
            throw Util4Exceptions.createNoApplicableCodeException(null,
                    "Error while encoding exception with SOAP envelope!");
        }
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
            String reqNamespaceURI = XmlHelper.getNamespace(doc);
            IDecoder decoder = getDecoder(new DecoderKeyType(reqNamespaceURI));
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
                    IXmlRequestDecoder bodyDecoder =
                            getIXmlRequestDecoder(new DecoderKeyType(XmlHelper.getNamespace(xmlObject)));
                    // Decode SOAPBody content
                    Object aBodyRequest = bodyDecoder.decode(xmlObject);
                    if (aBodyRequest instanceof AbstractServiceRequest) {
                        AbstractServiceRequest bodyRequest = (AbstractServiceRequest) aBodyRequest;
                        checkServiceOperatorKeyTypes(bodyRequest);
                        for (ServiceOperatorKeyType serviceVersionIdentifier : bodyRequest.getServiceOperatorKeyType()) {
                            IServiceOperator serviceOperator =
                                    Configurator.getInstance().getServiceOperator(serviceVersionIdentifier);
                            if (serviceOperator != null) {
                                ServiceResponse bodyResponse = serviceOperator.receiveRequest(bodyRequest);
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
                        throw Util4Exceptions.createNoApplicableCodeException(null,
                                "The returned object is not an AbstractServiceRequest implementation");
                    }
                } else {
                    soapResponse.setSoapFault(soapRequest.getSoapFault());
                }
                // Encode SOAP response
                IEncoder encoder = Configurator.getInstance().getEncoder(soapResponse.getSoapNamespace());
                if (encoder != null) {
                    return (ServiceResponse) encoder.encode(soapResponse);
                } else {
                    // FIXME: valid exception
                    OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    throw owse;
                }
            } else {
                // FIXME: valid exception
                OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                throw owse;
            }
        } catch (OwsExceptionReport owse) {
            // FIXME: valid debug text
            LOGGER.debug("", owse);
            if (version != null) {
                owse.setVersion(version);
            } else {
                if (Configurator.getInstance().isVersionSupported(Sos1Constants.SERVICEVERSION)) {
                    owse.setVersion(Sos1Constants.SERVICEVERSION);
                } else {
                    owse.setVersion(Sos2Constants.SERVICEVERSION);
                }
            }
            soapResponse.setException(owse);
            if (soapVersion == null || (soapVersion != null && !soapVersion.isEmpty())) {
                soapResponse.setSoapVersion(SOAPConstants.SOAP_1_2_PROTOCOL);
            } else {
                soapResponse.setSoapVersion(soapVersion);
            }
            if (soapNamespace == null || (soapVersion != null && !soapVersion.isEmpty())) {
                soapResponse.setSoapNamespace(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
            } else {
                soapResponse.setSoapNamespace(soapNamespace);
            }
            IEncoder encoder = Configurator.getInstance().getEncoder(soapResponse.getSoapNamespace());
            if (encoder != null) {
                return (ServiceResponse) encoder.encode(soapResponse);
            } else {
                throw Util4Exceptions.createNoApplicableCodeException(null,
                        "Error while encoding exception with SOAP envelope!");
            }
        }
    }

    private OwsExceptionReport createOperationNotSupportedException() {
        String exceptionText = "The requested service URL only supports HTTP-Post SOAP requests!";
        OwsExceptionReport owse = Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        if (Configurator.getInstance().isVersionSupported(Sos2Constants.SERVICEVERSION)) {
            owse.setVersion(Sos2Constants.SERVICEVERSION);
        } else {
            owse.setVersion(Sos1Constants.SERVICEVERSION);
        }
        return owse;
    }

    private void checkServiceOperatorKeyTypes(AbstractServiceRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
        for (ServiceOperatorKeyType serviceVersionIdentifier : request.getServiceOperatorKeyType()) {
            if (serviceVersionIdentifier.getService() != null) {
                if (serviceVersionIdentifier.getService().isEmpty()) {
                    exceptions.add(Util4Exceptions.createMissingParameterValueException(RequestParams.service.name()));
                } else {
                    if (!Configurator.getInstance().isServiceSupported(serviceVersionIdentifier.getService())) {
                        String exceptionText = "The requested service is not supported!";
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                RequestParams.service.name(), exceptionText));
                    }
                }
            }
            if (request instanceof GetCapabilitiesRequest) {
                GetCapabilitiesRequest getCapsRequest = (GetCapabilitiesRequest)request;
                if (getCapsRequest.isSetAcceptVersions()) {
                    boolean hasSupportedVersion = false;
                    for (String accaptVersion : getCapsRequest.getAcceptVersions()) {
                        if (Configurator.getInstance().isVersionSupported(accaptVersion)) {
                            hasSupportedVersion = true;
                        }
                    }
                    if (!hasSupportedVersion) {
                        String exceptionText = "The requested acceptedVersions are not supported by this service!"; 
                        exceptions.add(Util4Exceptions.createVersionNegotiationFailedException(exceptionText));
                    }
                }
            } else {
                if (serviceVersionIdentifier.getVersion() != null) {
                    if (serviceVersionIdentifier.getVersion().isEmpty()) {
                        exceptions.add(Util4Exceptions.createMissingParameterValueException(RequestParams.version
                                .name()));
                    } else {
                        if (!Configurator.getInstance().isVersionSupported(serviceVersionIdentifier.getVersion())) {
                            String exceptionText = "The requested version is not supported!";
                            exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                    RequestParams.version.name(), exceptionText));
                        }
                    }
                }
            }
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

    @Override
    public String getUrlPattern() {
        return urlPattern;
    }

    @Override
    public boolean checkOperationHttpPostSupported(String operationName, DecoderKeyType decoderKey)
            throws OwsExceptionReport {
        IXmlRequestDecoder decoder = getIXmlRequestDecoder(decoderKey);
        if (decoder != null) {
            return SosHelper.checkMethodeImplementation4DCP(decoder.getClass().getDeclaredMethods(), operationName);
        }
        return false;
    }

    private IDecoder getDecoder(DecoderKeyType decoderKey) throws OwsExceptionReport {
        List<IDecoder> decoder = Configurator.getInstance().getDecoder(decoderKey);
        for (IDecoder iDecoder : decoder) {
            return iDecoder;
        }
        return null;
    }

    private IXmlRequestDecoder getIXmlRequestDecoder(DecoderKeyType decoderKey) throws OwsExceptionReport {
        List<IDecoder> decoder = Configurator.getInstance().getDecoder(decoderKey);
        for (IDecoder iDecoder : decoder) {
            if (iDecoder instanceof IXmlRequestDecoder) {
                return (IXmlRequestDecoder) iDecoder;
            }
        }
        return null;
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/soap");
        return conformanceClasses;
    }
}
