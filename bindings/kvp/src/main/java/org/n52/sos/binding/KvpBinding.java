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
import org.n52.sos.exception.HTTPException;
import org.n52.sos.exception.OwsExceptionReportEncodingFailedException;
import org.n52.sos.exception.ows.concrete.InvalidAcceptVersionsParameterException;
import org.n52.sos.exception.ows.concrete.InvalidServiceOrVersionException;
import org.n52.sos.exception.ows.concrete.InvalidServiceParameterException;
import org.n52.sos.exception.ows.concrete.MissingRequestParameterException;
import org.n52.sos.exception.ows.concrete.NoDecoderForKeyException;
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
    private static final String ENCODING = "kvp";

	@Override
	public Set<String> getConformanceClasses() {
		return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
	}

	@Override
	public String getUrlPattern() {
		return BindingConstants.KVP_BINDING_ENDPOINT;
	}

	@Override
	public ServiceResponse doGetOperation(final HttpServletRequest req) throws OwsExceptionReportEncodingFailedException {
		LOGGER.debug("KVP-REQUEST: {}", req.getQueryString());
		ServiceResponse response = null;
		AbstractServiceRequest serviceRequest = null;
		try {
			if ((req.getParameterMap() == null) || ((req.getParameterMap() != null) && req.getParameterMap().isEmpty())) {
				throw new MissingRequestParameterException();
			}
			final Map<String, String> parameterValueMap = KvpHelper.getKvpParameterValueMap(req);
			// check if request contains request parameter
			final String operation = getRequestParameterValue(parameterValueMap);
			KvpHelper.checkParameterValue(operation, RequestParams.request);
			final String service = getServiceParameterValue(parameterValueMap);
			final String version = getVersionParameterValue(parameterValueMap);


			if ((version != null) && !isVersionSupported(service,version)) {
				throw new VersionNotSupportedException();
			}
			final DecoderKey k = new KvpOperationDecoderKey(service, version, operation);
			final Decoder<AbstractServiceRequest, Map<String, String>> decoder = getDecoder(k);

			if (decoder != null) {
				serviceRequest = decoder.decode(parameterValueMap);
			} else {
				throw new NoDecoderForKeyException(k);
			}

			for (final ServiceOperatorKeyType serviceVersionIdentifier : serviceRequest.getServiceOperatorKeyType()) {
				final ServiceOperator serviceOperator = getServiceOperatorRepository()
						.getServiceOperator(serviceVersionIdentifier);
				if (serviceOperator != null) {
					response = serviceOperator.receiveRequest(serviceRequest);
					LOGGER.debug("{} operation executed successfully!", serviceRequest.getOperationName());
					break;
				}
			}
			if (response == null) {
				if (serviceRequest instanceof GetCapabilitiesRequest) {
					final GetCapabilitiesRequest gcr = (GetCapabilitiesRequest) serviceRequest;
					throw new InvalidAcceptVersionsParameterException(gcr.getAcceptVersions());
				} else {
					throw new InvalidServiceOrVersionException(serviceRequest.getService(), serviceRequest.getVersion());
				}
			}
		} catch (final OwsExceptionReport oer) {
			oer.setVersion(serviceRequest != null ? serviceRequest.getVersion() : null);
			return encodeOwsExceptionReport(oer, false);
		}
		return response;
	}

	private String getServiceParameterValue(final Map<String, String> parameterValueMap) throws OwsExceptionReport {
		final String service = KvpHelper.getParameterValue(RequestParams.service, parameterValueMap);
		final boolean isGetCapabilities = checkForGetCapabilities(parameterValueMap);
        if (isGetCapabilities && (service == null)) {
            // unclear behaviour because of imprecise spec:
            // OGC 12-006 8.1.1 and OGC 12-006 13.2.1/OGC 06-121r3 7.2.3
			return SosConstants.SOS;
		} else {
			KvpHelper.checkParameterValue(service, RequestParams.service);
		}
		if (!isServiceSupported(service)) {
			throw new InvalidServiceParameterException(service);
		}
		return service;
	}

	private String getVersionParameterValue(final Map<String, String> parameterValueMap) throws OwsExceptionReport {
		final String version = KvpHelper.getParameterValue(RequestParams.version, parameterValueMap);
		final String service = KvpHelper.getParameterValue(RequestParams.service, parameterValueMap);
		final boolean isGetCapabilities = checkForGetCapabilities(parameterValueMap);
		if (!isGetCapabilities) {
			KvpHelper.checkParameterValue(version, RequestParams.version);
			KvpHelper.checkParameterValue(service, RequestParams.service);
			if (!isVersionSupported(service,version)) {
				throw new VersionNotSupportedException();
			}
		}
		return version;
	}

	protected boolean checkForGetCapabilities(final Map<String, String> parameterValueMap) throws OwsExceptionReport {
		final String requestValue = getRequestParameterValue(parameterValueMap);
		if ((requestValue != null) && requestValue.equals(SosConstants.Operations.GetCapabilities.name())) {
			return true;
		}
		return false;
	}

	public String getRequestParameterValue(final Map<String, String> parameterValueMap) throws OwsExceptionReport {
		final String requestParameterValue = KvpHelper.getParameterValue(RequestParams.request, parameterValueMap);
		KvpHelper.checkParameterValue(requestParameterValue, RequestParams.request);
		return requestParameterValue;
	}

	@Override
	public boolean checkOperationHttpGetSupported(final OperationDecoderKey k) throws HTTPException{
		return getDecoder(new KvpOperationDecoderKey(k)) != null;
	}

    @Override
    public String getEncoding() {
        return ENCODING;
    }
}
