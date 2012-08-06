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

package org.n52.sos.service.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.admin.operator.IAdminServiceOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The servlet of the SOS administration backend which receives the incoming
 * HttpGet requests and sends the operation result documents to the client
 * 
 */
public class SosAdminService extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/** the logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SosAdminService.class);

	/** The init parameter of the configFile */
	private static final String INIT_PARAM_CONFIG_FILE = "configFile";

	/**
	 * The request operator for the adminstration backend
	 */
	private IAdminServiceOperator sosAdminOperator;

	/**
	 * initializes the Servlet
	 */
	public void init() throws ServletException {

		// get ServletContext
		ServletContext context = getServletContext();
		String basepath = context.getRealPath("/");

		// get configFile as InputStream
		InputStream configStream = context
				.getResourceAsStream(getInitParameter(INIT_PARAM_CONFIG_FILE));

		if (configStream == null) {
			throw new UnavailableException("could not open the config file");
		}
		// initialize configurator
		try {
			Configurator.getInstance(configStream, basepath);
			configStream.close();
		} catch (OwsExceptionReport se) {
			throw new UnavailableException(se.getMessage());
		} catch (IOException ioe) {
			throw new UnavailableException(ioe.getMessage());
		} finally {
			try {
				configStream.close();
			} catch (IOException ioe) {
				LOGGER.error("cannot close input streams!", ioe);
			}

		}
		sosAdminOperator = Configurator.getInstance().getAdminRequestOperator();
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
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		LOGGER.debug("\n**********\n(POST) Connected from: "
				+ req.getRemoteAddr() + " " + req.getRemoteHost());

		this.setCorsHeaders(resp);

		// Set service URL in configurator
		if (Configurator.getInstance().getServiceURL() == null) {
			Configurator.getInstance().setServiceURL(
					req.getRequestURL().toString());
		}

		ServiceResponse sosResp = sosAdminOperator.doPostOperation(req);
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
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		LOGGER.debug("\n**********\n(GET) Connected from: "
				+ req.getRemoteAddr() + " " + req.getRemoteHost());
		LOGGER.trace("Query String: " + req.getQueryString());

		this.setCorsHeaders(resp);

		// Set service URL in configurator
		if (Configurator.getInstance().getServiceURL() == null) {
			Configurator.getInstance().setServiceURL(
					req.getRequestURL().toString());
		}
		// ///////////////////////////////////////////////
		// forward GET-request to RequestOperator

		ServiceResponse sosResp = sosAdminOperator.doGetOperation(req);
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
	public void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
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

}
