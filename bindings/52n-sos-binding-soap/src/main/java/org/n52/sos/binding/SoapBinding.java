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

import static org.n52.sos.util.HTTPConstants.StatusCode.BAD_REQUEST;

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
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.ExceptionEvent;
import org.n52.sos.exception.OwsExceptionReportEncodingFailedException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
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
    private static final String ENCODING = "soap";
	private static final Logger LOGGER = LoggerFactory.getLogger(SoapBinding.class);
	private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_SOAP_BINDING);
	private static final String URL_PATTERN = "/soap";

	@Override
	public ServiceResponse doPostOperation(final HttpServletRequest request) throws OwsExceptionReportEncodingFailedException {
		final String version = null;
		String soapVersion = null;
		String soapNamespace = null;
		final SoapResponse soapResponse = new SoapResponse();
		try {
			final String soapAction = SoapHelper.checkSoapHeader(request);
			final XmlObject doc = XmlHelper.parseXmlSosRequest(request);
			LOGGER.debug("SOAP-REQUEST: {}", doc.xmlText());
			// TODO add null check to decoder
			final Decoder<?,XmlObject> decoder = getDecoder(CodingHelper.getDecoderKey(doc));
			// decode SOAP message
			final Object abstractRequest = decoder.decode(doc);
			if (!(abstractRequest instanceof SoapRequest))
			{
				throw new NoApplicableCodeException()
				.withMessage("Request type '%s' not supported. Expected '%s'.",
						abstractRequest!=null?abstractRequest.getClass().getName():abstractRequest,
								SoapRequest.class.getName())
								.setStatus(BAD_REQUEST);
			}
			else
			{
				final SoapRequest soapRequest = (SoapRequest) abstractRequest;
				if ((soapRequest.getSoapAction() == null) && (soapAction != null)) {
					soapRequest.setAction(soapAction);
				}
				soapResponse.setSoapVersion(soapRequest.getSoapVersion());
				soapVersion = soapRequest.getSoapVersion();
				soapNamespace = soapRequest.getSoapNamespace();
				soapResponse.setSoapNamespace(soapNamespace);
				soapResponse.setHeader(soapRequest.getSoapHeader());
				if (soapRequest.getSoapFault() == null) {
					final XmlObject xmlObject = soapRequest.getSoapBodyContent();
					// TODO add null check to decoder
					final Decoder<?, XmlObject> bodyDecoder = getDecoder(CodingHelper.getDecoderKey(xmlObject));
					// Decode SOAPBody content
					final Object aBodyRequest = bodyDecoder.decode(xmlObject);
					if (!(aBodyRequest instanceof AbstractServiceRequest)) {
						throw new NoApplicableCodeException()
						.withMessage("The returned object is not an AbstractServiceRequest implementation")
						.setStatus(BAD_REQUEST);
					} else {
						final AbstractServiceRequest bodyRequest = (AbstractServiceRequest) aBodyRequest;
						checkServiceOperatorKeyTypes(bodyRequest);
						for (final ServiceOperatorKeyType sokt : bodyRequest.getServiceOperatorKeyType()) {
							final ServiceOperator so = getServiceOperatorRepository().getServiceOperator(sokt);
							if (so != null) {
								final ServiceResponse bodyResponse = so.receiveRequest(bodyRequest);
								if (!bodyResponse.isXmlResponse()) {
									// FIXME how to encode non xml encoded data
									// in soap responses?
									return bodyResponse;
								}
								soapResponse.setSoapBodyContent(bodyResponse);
								break;
							}
						}
					}
				}
				// Encode SOAP response
				final EncoderKey key = CodingHelper.getEncoderKey(soapResponse.getSoapNamespace(), soapResponse);
				final Encoder<?, SoapResponse> encoder = getEncoder(key);
				if (encoder != null) {
					return (ServiceResponse) encoder.encode(soapResponse);
				} else {
					throw new NoEncoderForKeyException(key);
				}
			}
		} catch (final OwsExceptionReport t) {
			return encodeOwsExceptionReport(version, soapVersion, soapNamespace, soapResponse, t);
		}
	}

	private ServiceResponse encodeOwsExceptionReport(final String version,
			final String soapVersion,
			final String soapNamespace,
			final SoapResponse soapResponse,
			final OwsExceptionReport owse) throws OwsExceptionReportEncodingFailedException {

		soapResponse.setException(owse.setVersion(version));
		// set SOAP version
		if ((soapVersion == null) || !soapVersion.isEmpty()) {
			soapResponse.setSoapVersion(SOAPConstants.SOAP_1_2_PROTOCOL);
		} else {
			soapResponse.setSoapVersion(soapVersion);
		}
		// set SOAP namespace
		if ((soapNamespace == null) || ((soapVersion != null) && !soapVersion.isEmpty())) {
			soapResponse.setSoapNamespace(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE);
		} else {
			soapResponse.setSoapNamespace(soapNamespace);
		}
		// encode SOAP body
		final EncoderKey key = CodingHelper.getEncoderKey(soapResponse.getSoapNamespace(), soapResponse);
		final Encoder<?, SoapResponse> encoder = getEncoder(key);
		try {
			if (encoder != null) {
				final ServiceResponse response = (ServiceResponse) encoder.encode(soapResponse);
				SosEventBus.fire(new ExceptionEvent(owse));
				if (owse.hasResponseCode())
				{
					response.setHttpResponseCode(owse.getStatus().getCode());
				}
				return response;
			} else {
				throw new NoEncoderForKeyException(key);
			}
		} catch (final Throwable t2) {
			final OwsExceptionReportEncodingFailedException oerfe = new OwsExceptionReportEncodingFailedException();
			oerfe.initCause(t2);
			throw oerfe;
		}
	}

	@Override
	public String getUrlPattern() {
		return URL_PATTERN;
	}

	@Override
	public boolean checkOperationHttpPostSupported(final OperationDecoderKey k) {
		return getDecoder(new XmlOperationDecoderKey(k)) != null;
	}

	@Override
	public Set<String> getConformanceClasses() {
		return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
	}

    @Override
    public String getEncoding() {
        return ENCODING;
    }
}
