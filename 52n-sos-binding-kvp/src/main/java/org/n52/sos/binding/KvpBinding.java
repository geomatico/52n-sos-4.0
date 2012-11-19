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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.decode.IKvpDecoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOS operator for Key-Value-Pair (HTTP-Get) requests
 * 
 */
public class KvpBinding extends Binding {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KvpBinding.class);

    /**
     * URL pattern for KVP requests
     */
    private static final String urlPattern = "/sos/kvp";

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest req) throws OwsExceptionReport {
        LOGGER.debug("KVP-REQUEST: {}", req.getQueryString());
        ServiceResponse response = null;
        AbstractServiceRequest request = null;
        try {
            if (req.getParameterMap() == null || (req.getParameterMap() != null && req.getParameterMap().isEmpty())) {
                LOGGER.debug("The mandatory parameter '" + RequestParams.request.name() + "' is missing!");
                throw Util4Exceptions.createMissingParameterValueException(RequestParams.request.name());
            }
            Map<String, String> parameterValueMap = KvpHelper.getKvpParameterValueMap(req);
            // check if request contains request parameter
            checkParameterValue(getRequestParameterValue(parameterValueMap), RequestParams.request.name());
            String service = getServiceParameterValue(parameterValueMap);
            String version = getVersionParameterValue(RequestParams.version.name(), parameterValueMap);
            IDecoder<AbstractServiceRequest, Map<String, String>> decoder =
                    getDecoder(new DecoderKeyType(service, version));
            if (decoder != null) {
                request = decoder.decode(parameterValueMap);
            } else {
                String exceptionText = "No decoder implementation is available for KvpBinding!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

            for (ServiceOperatorKeyType serviceVersionIdentifier : request.getServiceOperatorKeyType()) {
                IServiceOperator serviceOperator =
                        Configurator.getInstance().getServiceOperator(serviceVersionIdentifier);
                if (serviceOperator != null) {
                    response = serviceOperator.receiveRequest(request);
                    LOGGER.debug(request.getOperationName() + " operation executed successfully!");
                    break;
                }
            }
            if (response == null) {
                if (request instanceof GetCapabilitiesRequest) {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested ");
                    exceptionText.append(SosConstants.GetCapabilitiesParams.AcceptVersions.name());
                    exceptionText.append(" values (");
                    for (String acceptVersion : ((GetCapabilitiesRequest) request).getAcceptVersions()) {
                        exceptionText.append(acceptVersion);
                        exceptionText.append(", ");
                    }
                    exceptionText.delete(exceptionText.lastIndexOf(", "), exceptionText.length());
                    exceptionText.append(") are not supported by this server!");
                    throw Util4Exceptions.createVersionNegotiationFailedException(exceptionText.toString());
                } else {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested service (");
                    exceptionText.append(request.getService());
                    exceptionText.append(") and/or version (");
                    exceptionText.append(request.getVersion());
                    exceptionText.append(") is not supported by this server!");
                    LOGGER.debug(exceptionText.toString());
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.service.name(), exceptionText.toString());
                }
            }
        } catch (OwsExceptionReport owse) {
            LOGGER.debug("Error while performing KVP resquest", owse);
            if (request != null && request.getVersion() != null) {
                owse.setVersion(request.getVersion());
            } else {
                if (Configurator.getInstance().isVersionSupported(Sos2Constants.SERVICEVERSION)) {
                    owse.setVersion(Sos2Constants.SERVICEVERSION);
                } else {
                    owse.setVersion(Sos1Constants.SERVICEVERSION);
                }
            }
            throw owse;
        }
        return response;
    }
    
    @Override
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport {
        String message = "HTTP POST is no supported for KVP binding!";
         OwsExceptionReport owse = Util4Exceptions.createNoApplicableCodeException(null, message);
        if (Configurator.getInstance().isVersionSupported(Sos1Constants.SERVICEVERSION)) {
            owse.setVersion(Sos1Constants.SERVICEVERSION);
        } else {
            owse.setVersion(Sos2Constants.SERVICEVERSION);
        }
        throw owse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ISosRequestOperator#getUrlPattern()
     */
    @Override
    public String getUrlPattern() {
        return urlPattern;
    }

    private String getServiceParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String service = getParameterValue(RequestParams.service.name(), parameterValueMap);
        boolean isGetCapabilities = checkForGetCapabilities(parameterValueMap);
        if (isGetCapabilities && service == null) {
            return SosConstants.SOS;
        } else {
            checkParameterValue(service, RequestParams.service.name());
        }
        if (!Configurator.getInstance().isServiceSupported(service)) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The value of parameter '");
            exceptionText.append(OWSConstants.RequestParams.service.name());
            exceptionText.append("' is invalid or the service is not supported!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createInvalidParameterValueException(RequestParams.service.name(),
                    exceptionText.toString());
        }
        return service;
    }

    private String getVersionParameterValue(String name, Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String version = getParameterValue(RequestParams.version.name(), parameterValueMap);
        boolean isGetCapabilities = checkForGetCapabilities(parameterValueMap);
        if (!isGetCapabilities) {
            checkParameterValue(version, RequestParams.version.name());
            if (!Configurator.getInstance().isVersionSupported(version)) {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The value of parameter '");
                exceptionText.append(OWSConstants.RequestParams.version.name());
                exceptionText.append("' is invalid or the version is not supported!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createInvalidParameterValueException(RequestParams.version.name(),
                        exceptionText.toString());
            }
        } 
        return version;
    }
    
    private boolean checkForGetCapabilities(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String requestValue = getRequestParameterValue(parameterValueMap);
        if (requestValue != null && requestValue.equals(SosConstants.Operations.GetCapabilities.name())) {
            return true;
        }
        return false;
    }

    private String getRequestParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String requestParameterValue = getParameterValue(RequestParams.request.name(), parameterValueMap);
        checkParameterValue(requestParameterValue, RequestParams.request.name());
        return requestParameterValue;
    }

    private String getParameterValue(String parameterName, Map<String, String> parameterMap) throws OwsExceptionReport {
        for (String key : parameterMap.keySet()) {
            if (key.equalsIgnoreCase(parameterName)) {
                 return parameterMap.get(key);
            }
        }
        return null;
    }

    private void checkParameterValue(String parameterValue, String parameterName) throws OwsExceptionReport {
        if (parameterValue == null || (parameterValue != null && parameterValue.isEmpty())) {
            LOGGER.debug("The value for parameter '" + parameterName + "' is missing!");
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
    }

    @Override
    public boolean checkOperationHttpGetSupported(String operationName, DecoderKeyType decoderKey)
            throws OwsExceptionReport {
        IDecoder decoder = getDecoder(decoderKey);
        if (decoder != null) {
            return SosHelper.checkMethodeImplementation4DCP(decoder.getClass().getDeclaredMethods(), operationName);
        }
        return false;
    }

    private IKvpDecoder getDecoder(DecoderKeyType decoderKey) throws OwsExceptionReport {
        Configurator configurator = Configurator.getInstance();
        List<IDecoder> decoder = configurator.getDecoder(decoderKey);
        if (decoder != null) {
            for (IDecoder iDecoder : decoder) {
                if (iDecoder instanceof IKvpDecoder) {
                    return (IKvpDecoder) iDecoder;
                }
            }
        }
        if (!configurator.isVersionSupported(decoderKey.getVersion())) {
            String exceptionText = "The requested version is not supported!";
            throw Util4Exceptions.createInvalidParameterValueException(RequestParams.version.name(), exceptionText);
        } 
//        else if (!configurator.isServiceSupported(decoderKey.getService())) {
//            String exceptionText = "The requested service is not supported!";
//            throw Util4Exceptions.createInvalidParameterValueException(RequestParams.service.name(), exceptionText);
//        }
        return null;
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/kvp-core");
        return conformanceClasses;
    }
}
