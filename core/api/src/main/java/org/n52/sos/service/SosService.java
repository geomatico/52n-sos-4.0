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
package org.n52.sos.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.sos.binding.Binding;
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.ExceptionEvent;
import org.n52.sos.exception.HTTPException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The servlet of the SOS which receives the incoming HttpPost and HttpGet
 * requests and sends the operation result documents to the client
 * TODO review exception handling
 */
public class SosService extends ConfiguratedHttpServlet {
	private static final long serialVersionUID = -2103692310137045855L;
	private static final Logger LOGGER = LoggerFactory.getLogger(SosService.class);

	private static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static final String CONTENT_ENCODING = "Content-Encoding";

	private static final String GZIP = "gzip";

	/**
	 * initializes the Servlet
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		LOGGER.info("SOS endpoint initalized successfully!");
	}

	@Override
	protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.debug("\n**********\n(DELETE) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
		final boolean clientSupportsGzip = checkForClientGZipSupport(req);
		ServiceResponse sosResp;
		try {
			sosResp = getBinding(req).doDeleteOperation(req);
		} catch (final HTTPException owse) {
			sosResp = createServiceResponse(owse);
		}
		doResponse(resp, sosResp, clientSupportsGzip);
	}

	/**
	 * handles all GET requests, the request will be passed to the
	 * RequestOperator
	 *
	 * @param req
	 *            the incoming request
	 *
	 * @param resp
	 *            the response for the incoming request
	 *
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
		LOGGER.debug("\n**********\n(GET) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
		LOGGER.trace("Query String: " + req.getQueryString());
		final boolean clientSupportsGzip = checkForClientGZipSupport(req);
		ServiceResponse sosResp;
		try {
			sosResp = getBinding(req).doGetOperation(req);
		} catch (final HTTPException owse) {
			sosResp = createServiceResponse(owse);
		}
		doResponse(resp, sosResp, clientSupportsGzip);
	}

	/**
	 * handles all POST requests, the request will be passed to the
	 * requestOperator
	 *
	 * @param req
	 *            the incomming request
	 *
	 * @param resp
	 *            the response for the incoming request
	 */
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException {
		LOGGER.debug("\n**********\n(POST) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
		final boolean clientSupportsGzip = checkForClientGZipSupport(req);
		ServiceResponse sosResp;
		try {
			sosResp = getBinding(req).doPostOperation(req);
		} catch (final HTTPException owse) {
			sosResp = createServiceResponse(owse);
		}
		doResponse(resp, sosResp, clientSupportsGzip);
	}

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.debug("\n**********\n(PUT) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
		final boolean clientSupportsGzip = checkForClientGZipSupport(req);
		ServiceResponse sosResp;
		try {
			sosResp = getBinding(req).doPutOperation(req);
		} catch (final HTTPException owse) {
			sosResp = createServiceResponse(owse);
		}
		doResponse(resp, sosResp, clientSupportsGzip);

	}

	/**
	 * Handles OPTIONS request to enable Cross-Origin Resource Sharing.
	 *
	 * @param req
	 *            the incoming request
	 *
	 * @param resp
	 *            the response for the incoming request
	 */
	@Override
	protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		LOGGER.debug("\n**********\n(OPTIONS) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
		final boolean clientSupportsGzip = checkForClientGZipSupport(req);
		ServiceResponse sosResp;
		try {
			sosResp = getBinding(req).doOptionsOperation(req);
		} catch (final HTTPException owse) {
			sosResp = createServiceResponse(owse);
		}
		if (sosResp.getHttpResponseCode() == HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
			super.doOptions(req, resp);
		}
		doResponse(resp, sosResp, clientSupportsGzip);
	}

	/**
	 * writes the content of the SosResponse to the outputStream of the
	 * HttpServletResponse
	 *
	 * @param resp
	 *            the HttpServletResponse to which the content will be written
	 *
	 * @param sosResponse
	 *            the SosResponse, whose content will be written to the
	 *            outputStream of resp param
	 * @throws ServletException
	 *
	 */
	// FIXME what happens with responses having no output stream
	private void doResponse(final HttpServletResponse resp, final ServiceResponse sosResponse, final boolean clientAcceptsGzip)
			throws ServletException {
		OutputStream out = null;
		GZIPOutputStream gzip = null;

		LOGGER.debug("SERVICE-RESPONSE: {}",sosResponse);

		try {
			final String contentType = sosResponse.getContentType();
			resp.setContentType(contentType);

			if (sosResponse.isSetHeaderMap()) {
				setSpecifiedHeaders(sosResponse.getHeaderMap(), resp);
			}

			final int httpResponseCode = sosResponse.getHttpResponseCode();
			if (httpResponseCode != -1) {
				resp.setStatus(httpResponseCode);
			}

			// TODO check for which contentLength gzip is faster than sending unzipped response
			if (!sosResponse.isContentLess()) {
				out = resp.getOutputStream();
				final int contentLength = sosResponse.getContentLength();
				resp.setContentType(contentType);
				if (sosResponse.getApplyGzipCompression() || (clientAcceptsGzip
						&& (contentLength > Configurator.getInstance().getMinimumGzipSize()))) {
					if (clientAcceptsGzip) {
						resp.addHeader(CONTENT_ENCODING, GZIP);
					}
					if (sosResponse.getApplyGzipCompression()) {
						resp.setContentType(SosConstants.CONTENT_TYPE_ZIP);
					}
					gzip = new GZIPOutputStream(out);
					sosResponse.writeToOutputStream(gzip);
					gzip.flush();
					gzip.finish();
				} else {
					resp.setContentLength(contentLength);
					sosResponse.writeToOutputStream(out);
					out.flush();
				}
			}

		} catch (final IOException ioe) {
			final String exceptionText = "Error while writing SOS response to ServletResponse!";
			LOGGER.error(exceptionText, ioe);
			throw new ServletException(exceptionText, ioe);
		} finally {
			try {
				if (gzip != null) {
					gzip.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (final IOException ioe) {
				LOGGER.debug("Error while closing output stream(s)!", ioe);
			}
		}
	}

	private void setSpecifiedHeaders(final Map<String, String> headerMap, final HttpServletResponse resp) {
		for (final String headerIdentifier : headerMap.keySet()) {
			final String value = headerMap.get(headerIdentifier);
			if (isHeaderAndValueSet(headerIdentifier, value)) {
				resp.setHeader(headerIdentifier, value);
			}
		}
	}

	private boolean isHeaderAndValueSet(final String headerIdentifier, final String value) {
		return (headerIdentifier != null) && !headerIdentifier.equalsIgnoreCase("") && (value != null);
	}

	private boolean checkForClientGZipSupport(final HttpServletRequest req) {
		final Enumeration<?> headers = req.getHeaders(ACCEPT_ENCODING);
		while (headers.hasMoreElements()) {
			final String header = (String) headers.nextElement();
			if ((header != null) && !header.isEmpty()) {
				final String[] split = header.split(",");
				for (final String string : split) {
					if (string.equalsIgnoreCase(GZIP)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get the implementation of {@link Binding} that is registered for the
	 * given <code>request</code>.
	 *
	 * @param req
	 *            URL pattern from request URL
	 * @return The implementation of {@link Binding} that is registered for the
	 *         given <code>urlPattern</code>.

	 *
	 * @throws OwsExceptionReport * If the URL pattern is not supported by this SOS.
	 */
	private Binding getBinding(final HttpServletRequest req) throws HTTPException {
		Binding bindingOperator = null;
        final String requestURI = req.getPathInfo();

		for (final String bindingOperatorKey : Configurator.getInstance().getBindingRepository().getBindings().keySet()) {

			if (requestURI.startsWith(bindingOperatorKey)) {
				bindingOperator = Configurator.getInstance().getBindingRepository().getBinding(bindingOperatorKey);
				break;
			}
		}
		if (bindingOperator == null) {
			throw new HTTPException(HTTPConstants.StatusCode.NOT_FOUND);
		}
		return bindingOperator;
	}

	protected ServiceResponse createServiceResponse(final HTTPException owse) {
		ServiceResponse sosResp;
		sosResp = new ServiceResponse(null, owse.getStatus().getCode());
		SosEventBus.fire(new ExceptionEvent(owse));
		return sosResp;
	}
}
