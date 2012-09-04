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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.binding.IBinding;
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
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		LOGGER.debug("\n**********\n(DELETE) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());

        this.setCorsHeaders(resp);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForServletPath(req.getServletPath()).doDeleteperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
        }
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
	
	    ServiceResponse sosResp = null;
	    try {
	        sosResp = getBindingOperatorForServletPath(req.getServletPath()).doGetOperation(req);
	    } catch (OwsExceptionReport owse) {
	        sosResp = handleOwsExceptionReport(owse);
	    }
	    doResponse(resp, sosResp);
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
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForServletPath(req.getServletPath()).doPostOperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
        }
        doResponse(resp, sosResp);
    }
      
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		LOGGER.debug("\n**********\n(PUT) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());

        this.setCorsHeaders(resp);
        ServiceResponse sosResp = null;
        try {
            sosResp = getBindingOperatorForServletPath(req.getServletPath()).doPutOperation(req);
        } catch (OwsExceptionReport owse) {
            sosResp = handleOwsExceptionReport(owse);
        }
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
                    String exceptionText = "Error while handle exception message response!";
                    LOGGER.debug(exceptionText);
                    throw new ServletException(exceptionText);
                }
            } else {
                String exceptionText = "Error while handle exception message response!";
                LOGGER.debug(exceptionText);
                throw new ServletException(exceptionText);
            }
        } catch (OwsExceptionReport owse) {
            String exceptionText = "Error while handle exception message response!";
            LOGGER.debug(exceptionText, owse);
            throw new ServletException(exceptionText);
        } catch (IOException ioe) {
            String exceptionText = "Error while handle exception message response!";
            LOGGER.debug(exceptionText, ioe);
            throw new ServletException(exceptionText);
        }
        
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
    private void doResponse(HttpServletResponse resp, ServiceResponse sosResponse) throws ServletException {
        OutputStream out = null;
        GZIPOutputStream gzip = null;
        try {
            String contentType = sosResponse.getContentType();
            resp.setContentType(contentType);
            
        	if (!sosResponse.isContentLess()){
        		int contentLength = sosResponse.getContentLength();
        		resp.setContentLength(contentLength);
            	out = resp.getOutputStream();
            	sosResponse.writeToOutputStream(out);
            	out.flush();
            }
        	
        	int httpResponseCode = sosResponse.getHttpResponseCode();
        	if	(httpResponseCode != -1) {
        		resp.setStatus(httpResponseCode);
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
     * Get the implementation of {@link IBinding} that is registered for the given <code>urlPattern</code>.
     * 
     * @param urlPattern
     *          URL pattern from request URL
     * @return
     * 			The implementation of {@link IBinding} that is registered for the given <code>urlPattern</code>.
     * @throws OwsExceptionReport 
     * 			If the URL pattern is not supported by this SOS.
     */
    private IBinding getBindingOperatorForServletPath(String urlPattern) throws OwsExceptionReport {
        IBinding bindingOperator = Configurator.getInstance().getBindingOperator(urlPattern);
        if (bindingOperator == null) {
            String exceptionText =
                    "The requested servlet path with pattern '" + urlPattern + "' is not supported by this SOS!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        return bindingOperator;
    }

}
