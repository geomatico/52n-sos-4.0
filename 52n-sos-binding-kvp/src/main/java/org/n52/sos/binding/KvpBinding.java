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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.IServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.SosRequestToResponseHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOS operator for Key-Value-Pair (HTTP-Get) requests
 * 
 */
public class KvpBinding implements IBinding {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KvpBinding.class);

    /**
     * URL pattern for KVP requests
     */
    private static final String urlPattern = "/sos/kvp";

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ISosRequestOperator#doGetOperation(javax.servlet.http.
     * HttpServletRequest)
     */
    @Override
    public IServiceResponse doGetOperation(HttpServletRequest req) {
        IServiceResponse response = null;
        AbstractServiceRequest request = null;
        try {
            if (req.getParameterMap() == null || (req.getParameterMap() != null && req.getParameterMap().isEmpty())) {
                OwsExceptionReport owse =
                        Util4Exceptions
                                .createMissingParameterValueException(OWSConstants.RequestParams.request.name());
                throw owse;
            }
            Map<String, String> parameterValueMap = KvpHelper.getKvpParameterValueMap(req);
            DecoderKeyType dkt = new DecoderKeyType(getServiceParameterValue(parameterValueMap),
                            getParameterValue(OWSConstants.RequestParams.version.name(), parameterValueMap));
            dkt.setUrlPattern(urlPattern);
            IDecoder<AbstractServiceRequest, Map<String, String>> decoder =
                    Configurator.getInstance().getDecoder(dkt);
            if (decoder != null) {
                request = decoder.decode(parameterValueMap);
            } else {
                String exceptionText =
                        "No decoder implementation is available for the key '" + dkt.toString() + "'!";
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
                String exceptionText =
                        "The requested service (" + request.getService() + ") and/or version (" + request.getVersion()
                                + ") is not supported by this server!";
                throw Util4Exceptions.createInvalidParameterValueException(OWSConstants.RequestParams.service.name(),
                        exceptionText);
            }
        } catch (OwsExceptionReport owse) {
            LOGGER.debug("Error while performing KVP resquest", owse);
            if (request.getVersion() != null) {
                owse.setVersion(request.getVersion());
            } else {
                if (Configurator.getInstance().isVersionSupported(Sos2Constants.SERVICEVERSION)) {
                    owse.setVersion(Sos2Constants.SERVICEVERSION);
                } else {
                    owse.setVersion(Sos1Constants.SERVICEVERSION);
                }
            }
            response = SosRequestToResponseHelper.createExceptionResponse(owse);
        }
        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.ISosRequestOperator#doPostOperation(javax.servlet.http.
     * HttpServletRequest)
     */
    @Override
    public IServiceResponse doPostOperation(HttpServletRequest request) {
        String exceptionText = "This SOS URL only supports KVP requests!";
        LOGGER.info(exceptionText);
        return SosRequestToResponseHelper.createExceptionResponse(Util4Exceptions.createNoApplicableCodeException(
                null, exceptionText));
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

    private String getServiceParameterValue(Map<String, String> parameterValueMap) {
        if (getParameterValue(RequestParams.request.name(), parameterValueMap).equals(
                SosConstants.Operations.GetCapabilities.name())) {
            return SosConstants.SOS;
        }
        return getParameterValue(OWSConstants.RequestParams.service.name(), parameterValueMap);
    }

    private String getParameterValue(String parameterName, Map<String, String> parameterMap) {
        for (String key : parameterMap.keySet()) {
            if (key.equalsIgnoreCase(parameterName)) {
                return parameterMap.get(key);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ISosRequestOperator#checkOperationHttpGetSupported(java.lang
     * .String, java.lang.String)
     */
    @Override
    public boolean checkOperationHttpGetSupported(String operationName, String service, String version)
            throws OwsExceptionReport {
        DecoderKeyType dkt = new DecoderKeyType(service, version);
        dkt.setUrlPattern(urlPattern);
        IDecoder decoder = Configurator.getInstance().getDecoder(dkt);
        if (decoder != null) {
            return SosHelper.checkMethodeImplementation4DCP(decoder.getClass().getDeclaredMethods(), operationName);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ISosRequestOperator#checkOperationHttpPostSupported(java.
     * lang.String, java.lang.String)
     */
    @Override
    public boolean checkOperationHttpPostSupported(String operationName,  String service, String version)
            throws OwsExceptionReport {
        return false;
    }

}
