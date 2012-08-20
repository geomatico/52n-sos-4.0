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

package org.n52.sos.binding;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.IConformanceClass;

/**
 * Interface for binding implementations
 * 
 */
public interface IBinding extends IConformanceClass {

    /**
     * HTTP-Post request handling method
     * 
     * @param request
     *            HTTP-Post request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doGetOperation(HttpServletRequest request) throws OwsExceptionReport;

    /**
     * HTTP-Post request handling method
     * 
     * @param request
     *            HTTP-Post request
     * @return SOS response
     * @throws ServletException
     */
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport;

    /**
     * Get URL pattern for the operator
     * 
     * @return URL pattern
     */
    public String getUrlPattern();

    boolean checkOperationHttpGetSupported(String operationName, DecoderKeyType decoderKey) throws OwsExceptionReport;

    boolean checkOperationHttpPostSupported(String operationName, DecoderKeyType decoderKey) throws OwsExceptionReport;

}
