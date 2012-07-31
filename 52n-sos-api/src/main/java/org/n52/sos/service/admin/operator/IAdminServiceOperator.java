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

package org.n52.sos.service.admin.operator;

import javax.servlet.http.HttpServletRequest;

import org.n52.sos.response.IServiceResponse;

/**
 * Abstract operator class for the administration interface
 * 
 */
public abstract class IAdminServiceOperator {

	/**
	 * URL pattern for the administration interface
	 */
	private static final String urlPattern = "/admin";

	/**
	 * HTTP-Get request handling method
	 * 
	 * @param request
	 *            HTTP-Get request
	 * @return SOS response
	 */
	public abstract IServiceResponse doGetOperation(HttpServletRequest request);

	/**
	 * HTTP-Post request handling method
	 * 
	 * @param request
	 *            HTTP-Post request
	 * @return SOS response
	 */
	public abstract IServiceResponse doPostOperation(HttpServletRequest request);

	/**
	 * Get URL pattern for the administration interface
	 * 
	 * @return URL pattern
	 */
	public String getUrlPattern() {
		return urlPattern;
	}

}
