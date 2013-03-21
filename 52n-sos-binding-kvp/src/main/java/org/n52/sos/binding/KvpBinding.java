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

import org.n52.sos.decode.Decoder;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.KvpOperationDecoderKey;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.InvalidAcceptVersionsParameterException;
import org.n52.sos.exception.ows.concrete.InvalidServiceOrVersionException;
import org.n52.sos.exception.ows.concrete.MethodNotSupportedException;
import org.n52.sos.exception.ows.concrete.MissingRequestParameterException;
import org.n52.sos.exception.ows.concrete.NoDecoderForKeyException;
import org.n52.sos.exception.ows.concrete.ServiceNotSupportedException;
import org.n52.sos.exception.ows.concrete.VersionNotSupportedException;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.operator.ServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.KvpHelper;
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
                throw new MissingRequestParameterException();
            }
            Map<String, String> parameterValueMap = KvpHelper.getKvpParameterValueMap(req);
            // check if request contains request parameter
            String operation = getRequestParameterValue(parameterValueMap);
            KvpHelper.checkParameterValue(operation, RequestParams.request);
            String service = getServiceParameterValue(parameterValueMap);
            String version = getVersionParameterValue(parameterValueMap);


            if (version != null && !isVersionSupported(version)) {
                throw new VersionNotSupportedException();
            }
            DecoderKey k = new KvpOperationDecoderKey(service, version, operation);
            Decoder<AbstractServiceRequest, Map<String, String>> decoder = getDecoder(k);

            if (decoder != null) {
                request = decoder.decode(parameterValueMap);
            } else {
                throw new NoDecoderForKeyException(k);
            }

            for (ServiceOperatorKeyType serviceVersionIdentifier : request.getServiceOperatorKeyType()) {
                ServiceOperator serviceOperator = getServiceOperatorRepository()
                        .getServiceOperator(serviceVersionIdentifier);
                if (serviceOperator != null) {
                    response = serviceOperator.receiveRequest(request);
                    LOGGER.debug("{} operation executed successfully!", request.getOperationName());
                    break;
                }
            }
            if (response == null) {
                if (request instanceof GetCapabilitiesRequest) {
                    GetCapabilitiesRequest gcr = (GetCapabilitiesRequest) request;
                    throw new InvalidAcceptVersionsParameterException(gcr.getAcceptVersions());
                } else {
                    throw new InvalidServiceOrVersionException(request.getService(), request.getVersion());
                }
            }
        } catch (Throwable t) {
            OwsExceptionReport owse;
            if (t instanceof OwsExceptionReport) {
                owse = (OwsExceptionReport) t;
            } else {
                owse = new NoApplicableCodeException().causedBy(t);
            }
            throw owse.setVersion(request != null ? request.getVersion() : null);
        }
        return response;
    }

    @Override
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport {
        throw new MethodNotSupportedException("KVP", "POST");
    }

    private String getServiceParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String service = KvpHelper.getParameterValue(RequestParams.service, parameterValueMap);
        boolean isGetCapabilities = checkForGetCapabilities(parameterValueMap);
        if (isGetCapabilities && service == null) {
            return SosConstants.SOS;
        } else {
            KvpHelper.checkParameterValue(service, RequestParams.service);
        }
        if (!isServiceSupported(service)) {
            throw new ServiceNotSupportedException();
        }
        return service;
    }

    private String getVersionParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String version = KvpHelper.getParameterValue(RequestParams.version, parameterValueMap);
        boolean isGetCapabilities = checkForGetCapabilities(parameterValueMap);
        if (!isGetCapabilities) {
            KvpHelper.checkParameterValue(version, RequestParams.version);
            if (!isVersionSupported(version)) {
                throw new VersionNotSupportedException();
            }
        }
        return version;
    }

    protected boolean checkForGetCapabilities(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String requestValue = getRequestParameterValue(parameterValueMap);
        if (requestValue != null && requestValue.equals(SosConstants.Operations.GetCapabilities.name())) {
            return true;
        }
        return false;
    }

    public String getRequestParameterValue(Map<String, String> parameterValueMap) throws OwsExceptionReport {
        String requestParameterValue = KvpHelper.getParameterValue(RequestParams.request, parameterValueMap);
        KvpHelper.checkParameterValue(requestParameterValue, RequestParams.request);
        return requestParameterValue;
    }

    @Override
    public boolean checkOperationHttpGetSupported(OperationDecoderKey k) throws OwsExceptionReport {
        return getDecoder(new KvpOperationDecoderKey(k)) != null;
    }
}
