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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import org.n52.sos.decode.DecoderKey;

import org.n52.sos.decode.IDecoder;
import org.n52.sos.decode.KvpOperationDecoderKey;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
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
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SOS operator for Key-Value-Pair (HTTP-Get) requests
 *
 */
public class KvpBinding extends Binding {

    private static final Logger LOGGER = LoggerFactory.getLogger(KvpBinding.class);

    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_KVP_CORE_BINDING);

    private static final String URL_PATTERN = "/kvp";

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public String getUrlPattern() {
        return URL_PATTERN;
    }

    @Override
    public ServiceResponse doGetOperation(HttpServletRequest req) throws OwsExceptionReport {
        LOGGER.debug("KVP-REQUEST: {}", req.getQueryString());
        ServiceResponse response = null;
        AbstractServiceRequest request = null;
        try {
            if (req.getParameterMap() == null || (req.getParameterMap() != null && req.getParameterMap().isEmpty())) {
                LOGGER.debug("The mandatory parameter '{}' is missing!", RequestParams.request.name());
                throw Util4Exceptions.createMissingParameterValueException(RequestParams.request.name());
            }
            Map<String, String> parameterValueMap = KvpHelper.getKvpParameterValueMap(req);
            // check if request contains request parameter
            String operation = KvpHelper.getRequestParameterValue(parameterValueMap);
            KvpHelper.checkParameterValue(operation, RequestParams.request.name());
            String service = getServiceParameterValue(parameterValueMap);
            String version = getVersionParameterValue(parameterValueMap);


            if (version != null && !Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(version)) {
                throw Util4Exceptions.createInvalidParameterValueException(
                        RequestParams.version.name(), "The requested version is not supported!");
            }
            DecoderKey k = new KvpOperationDecoderKey(service, version, operation);
            IDecoder<AbstractServiceRequest, Map<String, String>> decoder = Configurator.getInstance()
                    .getCodingRepository().getDecoder(k);

            if (decoder != null) {
                request = decoder.decode(parameterValueMap);
            } else {
                String exceptionText = String.format("No decoder implementation is available for KvpBinding (%s)!", k);
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

            for (ServiceOperatorKeyType serviceVersionIdentifier : request.getServiceOperatorKeyType()) {
                IServiceOperator serviceOperator = Configurator.getInstance().getServiceOperatorRepository()
						.getServiceOperator(serviceVersionIdentifier);
                if (serviceOperator != null) {
                    response = serviceOperator.receiveRequest(request);
                    LOGGER.debug("{} operation executed successfully!", request.getOperationName());
                    break;
                }
            }
            if (response == null) {
                if (request instanceof GetCapabilitiesRequest) {
                    String exceptionText = String.format("The requested %s values (%s) are not supported by this server!",
                            SosConstants.GetCapabilitiesParams.AcceptVersions.name(),
                            StringHelper.join(", ", (Object[])((GetCapabilitiesRequest) request).getAcceptVersions()));
                    throw Util4Exceptions.createVersionNegotiationFailedException(exceptionText);
                } else {
                    String exceptionText =
                            String.format(
                                    "The requested service (%s) and/or version (%s) is not supported by this server!",
                                    request.getService(), request.getVersion());

                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            OWSConstants.RequestParams.service.name(), exceptionText);
                }
            }
        } catch (OwsExceptionReport owse) {
            LOGGER.debug("Error while performing KVP request", owse);
            if (request != null && request.getVersion() != null) {
                owse.setVersion(request.getVersion());
            } else {
                if (Configurator.getInstance().getServiceOperatorRepository()
						.isVersionSupported(Sos2Constants.SERVICEVERSION)) {
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
        if (Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(Sos1Constants.SERVICEVERSION)) {
            owse.setVersion(Sos1Constants.SERVICEVERSION);
        } else {
            owse.setVersion(Sos2Constants.SERVICEVERSION);
        }
        throw owse;
    }

    private String getServiceParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String service = KvpHelper.getParameterValue(RequestParams.service.name(), parameterValueMap);
        boolean isGetCapabilities = KvpHelper.checkForGetCapabilities(parameterValueMap);
        if (isGetCapabilities && service == null) {
            return SosConstants.SOS;
        } else {
            KvpHelper.checkParameterValue(service, RequestParams.service.name());
        }
        if (!Configurator.getInstance().getServiceOperatorRepository().isServiceSupported(service)) {
            String exceptionText = String.format(
                    "The value of parameter '%s' is invalid or the service is not supported!",
                    OWSConstants.RequestParams.service.name());
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(RequestParams.service.name(),
                    exceptionText);
        }
        return service;
    }

    private String getVersionParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String version = KvpHelper.getParameterValue(RequestParams.version.name(), parameterValueMap);
        boolean isGetCapabilities = KvpHelper.checkForGetCapabilities(parameterValueMap);
        if (!isGetCapabilities) {
            KvpHelper.checkParameterValue(version, RequestParams.version.name());
            if (!Configurator.getInstance().getServiceOperatorRepository().isVersionSupported(version)) {
                String exceptionText = String.format("The value of parameter'%s' is invalid or the version is not supported!",
                        OWSConstants.RequestParams.version.name());
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(RequestParams.version.name(), exceptionText);
            }
        }
        return version;
    }

    @Override
    public boolean checkOperationHttpGetSupported(OperationDecoderKey k) throws OwsExceptionReport {
        return Configurator.getInstance().getCodingRepository().getDecoder(
                new KvpOperationDecoderKey(k.getService(), k.getVersion(), k.getOperation())) != null;
    }
}
