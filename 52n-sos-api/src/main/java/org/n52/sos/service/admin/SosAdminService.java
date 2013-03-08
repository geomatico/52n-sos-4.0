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
package org.n52.sos.service.admin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.encode.Encoder;
import org.n52.sos.encode.XmlEncoderKey;
import org.n52.sos.exception.AdministratorException;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.ConfiguratedHttpServlet;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The servlet of the SOS administration backend which receives the incoming
 * HttpGet requests and sends the operation result documents to the client
 * 
 */
public class SosAdminService extends ConfiguratedHttpServlet {

    private static final long serialVersionUID = 1L;

    /** the logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosAdminService.class);

	/**
	 * initializes the Servlet
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		LOGGER.info("Admin endpoint initalized successfully!");
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
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        LOGGER.debug("\n**********\n(GET) Connected from: " + req.getRemoteAddr() + " " + req.getRemoteHost());
        LOGGER.trace("Query String: " + req.getQueryString());

        this.setCorsHeaders(resp);

        ServiceResponse sosResp;
        try {
            sosResp = Configurator.getInstance().getAdminServiceOperator().doGetOperation(req);
        } catch (AdministratorException e) {
            OwsExceptionReport owsExceptionReport = new OwsExceptionReport();
            owsExceptionReport.addCodedException(OwsExceptionCode.NoApplicableCode, null, "Error", e);
            sosResp = handleException(owsExceptionReport);
        } catch (OwsExceptionReport e) {
            sosResp = handleException(e);
        } 
        doResponse(resp, sosResp);
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
     * 
     */
    public void doResponse(HttpServletResponse resp, ServiceResponse sosResponse) {
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
            LOGGER.error("doResponse", ioe);
        } finally {
            try {
                if (gzip != null) {
                    gzip.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                LOGGER.error("doSoapResponse, close streams", ioe);
            }
        }
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
    public void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
        this.setCorsHeaders(resp);
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
    
    private ServiceResponse handleException(OwsExceptionReport owsExceptionReport) throws ServletException {
        try {
            
            EncoderKey key = new XmlEncoderKey(owsExceptionReport.getNamespace(), owsExceptionReport.getClass());
            Encoder<?, OwsExceptionReport> encoder = Configurator.getInstance().getCodingRepository().getEncoder(key);
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

}
