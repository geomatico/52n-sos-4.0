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

package org.n52.sos.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.sos.binding.IBinding;
import org.n52.sos.response.IServiceResponse;
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

    /**
     * initializes the Servlet
     */
    public void init() throws ServletException {
        if (Configurator.getInstance().getAdminRequestOperator() == null) {
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
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        LOGGER.debug("\n**********\n(POST) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());

        this.setCorsHeaders(resp);

        IServiceResponse sosResp = getBindingOperatorForServletPath(req.getServletPath()).doPostOperation(req);
        doResponse(resp, sosResp);
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
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        LOGGER.debug("\n**********\n(GET) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        LOGGER.trace("Query String: " + req.getQueryString());

        this.setCorsHeaders(resp);

        IServiceResponse sosResp = getBindingOperatorForServletPath(req.getServletPath()).doGetOperation(req);
        doResponse(resp, sosResp);
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
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
        this.setCorsHeaders(resp);
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
    private void doResponse(HttpServletResponse resp, IServiceResponse sosResponse) throws ServletException {
        OutputStream out = null;
        GZIPOutputStream gzip = null;
        try {
            String contentType = sosResponse.getContentType();
            int contentLength = sosResponse.getContentLength();
            resp.setContentLength(contentLength);
            out = resp.getOutputStream();
            resp.setContentType(contentType);
            sosResponse.writeToOutputStream(out);
            out.flush();
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

    /**
     * Set Headers according to CORS to enable Cross-Domain JavaScript access.
     * 
     * @see <a href="http://www.w3.org/TR/cors/">http://www.w3.org/TR/cors/</a>
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        resp.addHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /**
     * Get the request operator for the URL pattern
     * 
     * @param servletPath
     *            URL pattern from request URL
     * @return SOS request operator
     * @throws UnavailableException
     *             If the URL pattern is not supported by this SOS.
     */
    private IBinding getBindingOperatorForServletPath(String urlPattern) throws UnavailableException {
        IBinding bindingOperator = Configurator.getInstance().getBindingOperator(urlPattern);
        if (bindingOperator == null) {
            String exceptionText = "The requested servlet path with pattern '" + urlPattern + "' is not supported by this SOS!";
            LOGGER.debug(exceptionText);
            throw new UnavailableException(exceptionText);
        }
        return bindingOperator;
    }

}
