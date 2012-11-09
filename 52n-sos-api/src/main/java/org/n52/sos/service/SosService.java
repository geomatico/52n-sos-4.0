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
package org.n52.sos.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.binding.Binding;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The servlet of the SOS which receives the incoming HttpPost and HttpGet
 * requests and sends the operation result documents to the client
 * 
 */
public class SosService extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** the logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosService.class);

    private static final String ACCEPT_ENCODING = "Accept-Encoding";

    private static final String CONTENT_ENCODING = "Content-Encoding";

    private static final String GZIP = "gzip";
    
    // TODO make this value configurable
    private static final int MINIMUM_GZIP_SIZE = 1000000;

    /**
     * initializes the Servlet
     */
    public void init() throws ServletException {
        if (Configurator.getInstance().getAdminServiceOperator() == null) {
            String exceptionText = "The instanziation of this service failed!";
            LOGGER.error(exceptionText);
            throw new UnavailableException(exceptionText);
        }

        LOGGER.info("SOS endpoint initalized successfully!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        Configurator.getInstance().cleanup();
        super.destroy();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LOGGER.debug("\n**********\n(DELETE) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        boolean clientSupportsGzip = checkForClientGZipSupport(req);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForRequestURI(req.getRequestURI()).doDeleteperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
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
     *            the response for the incomming request
     * 
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        LOGGER.debug("\n**********\n(GET) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        LOGGER.trace("Query String: " + req.getQueryString());
        boolean clientSupportsGzip = checkForClientGZipSupport(req);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForRequestURI(req.getRequestURI()).doGetOperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        LOGGER.debug("\n**********\n(POST) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        boolean clientSupportsGzip = checkForClientGZipSupport(req);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForRequestURI(req.getRequestURI()).doPostOperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
        }
        doResponse(resp, sosResp, clientSupportsGzip);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LOGGER.debug("\n**********\n(PUT) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        boolean clientSupportsGzip = checkForClientGZipSupport(req);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForRequestURI(req.getRequestURI()).doPutOperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
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
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.debug("\n**********\n(OPTIONS) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        boolean clientSupportsGzip = checkForClientGZipSupport(req);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForRequestURI(req.getRequestURI()).doOptionsOperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
        }
        if (sosResp.getHttpResponseCode() == HttpServletResponse.SC_NOT_IMPLEMENTED) {
            super.doOptions(req, resp);
            this.setCorsHeaders(resp);
        }
        doResponse(resp, sosResp, clientSupportsGzip);
    }

    private ServiceResponse handleOwsExceptionReport(OwsExceptionReport owsExceptionReport) throws ServletException {
        try {
            IEncoder encoder = Configurator.getInstance().getEncoder(owsExceptionReport.getNamespace());
            if (encoder != null) {
                Object encodedObject = encoder.encode(owsExceptionReport);
                if (encodedObject instanceof XmlObject) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                    return new ServiceResponse(baos, SosConstants.CONTENT_TYPE_XML, false, true);
                } else if (encodedObject instanceof ServiceResponse) {
                    return (ServiceResponse) encodedObject;
                } else {
                    throw logExceptionAndCreateServletException(null);
                }
            } else {
                throw logExceptionAndCreateServletException(null);
            }
        } catch (Exception owse) {
            throw logExceptionAndCreateServletException(owse);
        }
    }

    private ServletException logExceptionAndCreateServletException(Exception e) {
        String exceptionText = "Error while encoding exception response!";
        if (e != null) {
            LOGGER.debug(exceptionText, e);
        } else {
            LOGGER.debug(exceptionText);
        }
        return new ServletException(exceptionText);
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
    private void doResponse(HttpServletResponse resp, ServiceResponse sosResponse, boolean clientAcceptsGzip)
            throws ServletException {
        OutputStream out = null;
        GZIPOutputStream gzip = null;
        try {
            String contentType = sosResponse.getContentType();
            resp.setContentType(contentType);
            this.setCorsHeaders(resp);

            if (sosResponse.isSetHeaderMap()) {
                this.setSpecifiedHeaders(sosResponse.getHeaderMap(), resp);
            }

            int httpResponseCode = sosResponse.getHttpResponseCode();
            if (httpResponseCode != -1) {
                resp.setStatus(httpResponseCode);
            }

            // TODO check for which contentLength gzip is faster than sending unzipped response
            if (!sosResponse.isContentLess()) {
                out = resp.getOutputStream();
                int contentLength = sosResponse.getContentLength();
                resp.setContentType(contentType);
                if ((sosResponse.getApplyGzipCompression() || clientAcceptsGzip) && contentLength > MINIMUM_GZIP_SIZE ) {
                    if (clientAcceptsGzip) {
                        resp.addHeader(CONTENT_ENCODING, GZIP);
                    } else {
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

        } catch (IOException ioe) {
            String exceptionText = "Error while writing SOS response to ServletResponse!";
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
            } catch (IOException ioe) {
                LOGGER.debug("Error while closing output stream(s)!", ioe);
            }
        }
    }

    private void setSpecifiedHeaders(Map<String, String> headerMap, HttpServletResponse resp) {
        for (String headerIdentifier : headerMap.keySet()) {
            String value = headerMap.get(headerIdentifier).toString();
            if (isHeaderAndValueSet(headerIdentifier, value)) {
                resp.setHeader(headerIdentifier, value);
            }
        }
    }

    private boolean isHeaderAndValueSet(String headerIdentifier, String value) {
        return headerIdentifier != null && !headerIdentifier.equalsIgnoreCase("") && value != null;
    }
    
    // FIXME current version checks only first accept header and not all
    private boolean checkForClientGZipSupport(HttpServletRequest req) {
        String header = req.getHeader(ACCEPT_ENCODING);
        if (header != null && !header.isEmpty()) {
            String[] split = header.split(",");
            for (String string : split) {
                if (string.equalsIgnoreCase(GZIP)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set Headers according to CORS to enable Cross-Domain JavaScript access.
     * 
     * @see <a href="http://www.w3.org/TR/cors/">http://www.w3.org/TR/cors/</a>
     */
    // TODO Add HTTP-PUT and -DELETE?
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Get the implementation of {@link Binding} that is registered for the
     * given <code>urlPattern</code>.
     * 
     * @param requestURI
     *            URL pattern from request URL
     * @return The implementation of {@link Binding} that is registered for the
     *         given <code>urlPattern</code>.
     * @throws OwsExceptionReport
     *             If the URL pattern is not supported by this SOS.
     */
    private Binding getBindingOperatorForRequestURI(String requestURI) throws OwsExceptionReport {
        Binding bindingOperator = null;
        for (String bindingOperatorKey : Configurator.getInstance().getBindingOperators().keySet()) {
            if (requestURI.contains(bindingOperatorKey)) {
                bindingOperator = Configurator.getInstance().getBindingOperator(bindingOperatorKey);
                break;
            }
        }
        if (bindingOperator == null) {
            String exceptionText =
                    "The requested servlet path with pattern '" + requestURI + "' is not supported by this SOS!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        return bindingOperator;
    }

}
