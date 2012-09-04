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
 * Interface for binding implementations<br />
 * 
 * <b>Context</b>: The <code>IBinding.check*()</code> methods are called during GetCapabilities processing when collecting the
 * 		operations metadata.
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
     * HTTP-Delete request handling method
     * 
     * @param request
     *            HTTP-Delete request
     * @return SOS response
     * @throws ServletException
     */
    public ServiceResponse doDeleteperation(HttpServletRequest request) throws OwsExceptionReport;
    
    /**
     * HTTP-Put request handling method
     * 
     * @param request
     *            HTTP-Put request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doPutOperation(HttpServletRequest request) throws OwsExceptionReport;

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
     * Get URL pattern for the operator.<br />
     * The URL pattern MUST start with "/", 
     * MUST NOT contain any additional "/", 
     * and MUST be unique over all bindings present in the SOS at runtime.<br />
     * For example, a kvp binding could have the pattern "/kvp".
     * 
     * @return URL pattern
     */
    public String getUrlPattern();

    /**
     * Check, if the operation is supported by the decoder by the HTTP-Get method.
     * 
     * @param operationName
     * 				name of the OGC::SOS operation like "GetCapabilities"
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Get for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpGetSupported(String operationName, DecoderKeyType decoderKey) throws OwsExceptionReport;

    /**
     * Check, if the operation is supported by the decoder by the HTTP-Post method.
     * 
     * @param operationName
     * 				name of the OGC::SOS operation like "GetCapabilities"
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Post for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpPostSupported(String operationName, DecoderKeyType decoderKey) throws OwsExceptionReport;
    
    /**
     * Check, if the operation is supported by the decoder by the HTTP-Put method.
     * 
     * @param operationName
     * 				name of the OGC::SOS operation like "GetCapabilities"
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Put for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpPutSupported(String operationName, DecoderKeyType decoderKey) throws OwsExceptionReport;
    
    /**
     * Check, if the operation is supported by the decoder by the HTTP-Delete method.
     * 
     * @param operationName
     * 				name of the OGC::SOS operation like "GetCapabilities"
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Delete for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpDeleteSupported(String operationName, DecoderKeyType decoderKey) throws OwsExceptionReport;

}
