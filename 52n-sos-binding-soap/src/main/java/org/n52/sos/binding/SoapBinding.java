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

package org.n52.sos.binding;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPConstants;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.decode.IXmlRequestDecoder;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.soap.SoapRequest;
import org.n52.sos.soap.SoapResponse;
import org.n52.sos.soap.SoapHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.SosRequestToResponseHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoapBinding implements IBinding {

    /** the logger, used to log exceptions and additonaly information */
    private static final Logger LOGGER = LoggerFactory.getLogger(SoapBinding.class);

    private static final String urlPattern = "/sos/soap";

    public SoapBinding() {
    }

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest req) {
        OwsExceptionReport owse =
                Util4Exceptions.createNoApplicableCodeException(null, "The requested operations is not supported!");
        if (Configurator.getInstance().isVersionSupported(Sos2Constants.SERVICEVERSION)) {
            owse.setVersion(Sos2Constants.SERVICEVERSION);
        } else {
            owse.setVersion(Sos1Constants.SERVICEVERSION);
        }
        return SosRequestToResponseHelper.createExceptionResponse(owse);
    }

    @Override
    public ServiceResponse doPostOperation(HttpServletRequest request) throws ServletException {
        String version = null;
        String soapVersion = null;
        String soapNamespace = null;
        SoapResponse soapResponse = new SoapResponse();
        try {
            SoapHelper.checkSoapHeader(request);
            XmlObject doc = XmlHelper.parseXmlSosRequest(request);
            String reqNamespaceURI = XmlHelper.getNamespace(doc);
            IDecoder decoder = getDecoder(new DecoderKeyType(reqNamespaceURI));
            // decode SOAP message
            Object abstractRequest = decoder.decode(doc);
            if (abstractRequest instanceof SoapRequest) {
                SoapRequest soapRequest = (SoapRequest) abstractRequest;
                soapResponse.setSoapVersion(soapRequest.getSoapVersion());
                soapVersion = soapRequest.getSoapVersion();
                soapNamespace = soapRequest.getSoapNamespace();
                soapResponse.setSoapNamespace(soapNamespace);
                soapResponse.setHeader(soapRequest.getSoapHeader());
                if (soapRequest.getSoapFault() == null) {
                    XmlObject xmlObject = soapRequest.getSoapBodyContent();
                    IXmlRequestDecoder bodyDecoder = getIXmlRequestDecoder(new DecoderKeyType(XmlHelper.getNamespace(xmlObject)));
                    // Decode SOAPBody content
                    Object aBodyRequest = bodyDecoder.decode(xmlObject);
                    if (aBodyRequest instanceof AbstractServiceRequest) {
                        AbstractServiceRequest bodyRequest = (AbstractServiceRequest) aBodyRequest;
                        for (ServiceOperatorKeyType serviceVersionIdentifier : bodyRequest.getServiceOperatorKeyType()) {
                            IServiceOperator serviceOperator =
                                    Configurator.getInstance().getServiceOperator(serviceVersionIdentifier);
                            if (serviceOperator != null) {
                                ServiceResponse bodyResponse = serviceOperator.receiveRequest(bodyRequest);
                                if (!bodyResponse.isXmlResponse()) {
                                    return bodyResponse;
                                }
                                soapResponse.setSoapBodyContent(bodyResponse);
                            }
                            break;
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
                    OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    throw owse;
                }
            } else {
                OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                throw owse;
            }
        } catch (OwsExceptionReport owse) {
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
            soapResponse.setSoapBodyContent(SosRequestToResponseHelper.createExceptionResponse(owse));
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
            try {
                IEncoder encoder = Configurator.getInstance().getEncoder(soapResponse.getSoapNamespace());
                if (encoder != null) {
                    return (ServiceResponse) encoder.encode(soapResponse);
                } else {
                    throw Util4Exceptions.createNoApplicableCodeException(null,
                            "Error while encoding exception with SOAP envelope!");
                }
            } catch (OwsExceptionReport exc) {
                LOGGER.debug("ERROR while encoding OWSException to SOAP message", exc);
                throw new ServletException("ERROR", exc);
            }

        }
    }

    @Override
    public String getUrlPattern() {
        return urlPattern;
    }

    @Override
    public boolean checkOperationHttpGetSupported(String operationName, DecoderKeyType decoderKey)
            throws OwsExceptionReport {
        return false;
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
                return (IXmlRequestDecoder)iDecoder;
            }
        }
        return null;
    }
}
