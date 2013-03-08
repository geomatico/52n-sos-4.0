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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.ConformanceClass;

/**
 * Abstract Super class for binding implementations<br />
 * 
 * Context:<br />
 * The <code>Binding.check*()</code> methods are called during GetCapabilities processing when collecting the
 * 		operations metadata.
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a> 
 */
public abstract class Binding implements ConformanceClass {

    /**
     * The default response for each binding not overriding one of the doHTTP-Method methods.
     * 
     * @return SOS response
     */
    protected ServiceResponse createNotImplementedContentLessServiceResponse()
    {
        return new ServiceResponse(null, HttpServletResponse.SC_NOT_IMPLEMENTED);
    }

    /**
     * HTTP-Delete request handling method
     * 
     * @param request
     *            HTTP-Delete request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doDeleteperation(HttpServletRequest request) throws OwsExceptionReport
    {
        return createNotImplementedContentLessServiceResponse();
    }

    /**
     * HTTP-Post request handling method
     * 
     * @param request
     *            HTTP-Post request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doGetOperation(HttpServletRequest request) throws OwsExceptionReport
    {
        return createNotImplementedContentLessServiceResponse();
    }
    
    /**
     * HTTP-Options request handling method
     * 
     * @param request
     *            HTTP-Options request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doOptionsOperation(HttpServletRequest request) throws OwsExceptionReport
    {
        return createNotImplementedContentLessServiceResponse();
    }

    /**
     * HTTP-Post request handling method
     * 
     * @param request
     *            HTTP-Post request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doPostOperation(HttpServletRequest request) throws OwsExceptionReport
    {
        return createNotImplementedContentLessServiceResponse();
    }

    /**
     * HTTP-Put request handling method
     * 
     * @param request
     *            HTTP-Put request
     * @return SOS response
     * @throws OwsExceptionReport 
     */
    public ServiceResponse doPutOperation(HttpServletRequest request) throws OwsExceptionReport
    {
        return createNotImplementedContentLessServiceResponse();
    }

    /**
     * Get URL pattern for the operator.<br />
     * The URL pattern MUST start with "/sos", 
     * MUST NOT contain any additional "/", 
     * and MUST be unique over all bindings present in the SOS at runtime.<br />
     * For example, a kvp binding could have the pattern "/sos/kvp".
     * 
     * @return URL pattern
     */
    public abstract String getUrlPattern();

    /**
     * Check, if the operation is supported by the decoder by the HTTP-Delete method.
     * 
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Delete for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpDeleteSupported(OperationDecoderKey decoderKey) throws OwsExceptionReport
    {
        return false;
    }

    /**
     * Check, if the operation is supported by the decoder by the HTTP-Get method.
     * 
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Get for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpGetSupported(OperationDecoderKey decoderKey) throws OwsExceptionReport
    {
        return false;
    }

    /**
     * Check, if the operation is supported by the decoder by the HTTP-Post method.
     * 
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Post for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpPostSupported(OperationDecoderKey decoderKey) throws OwsExceptionReport
    {
        return false;
    }
    
    /**
     * Check, if the operation is supported by the decoder by the HTTP-Options method.
     * 
     * @param decoderKey
     *              identifier of the decoder
     * @return
     *              true, if the decoder <code>decoderKey</code> supports HTTP-Post for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpOptionsSupported(OperationDecoderKey decoderKey) throws OwsExceptionReport
    {
        return false;
    }
    
    /**
     * Check, if the operation is supported by the decoder by the HTTP-Put method.
     * 
     * @param decoderKey
     * 				identifier of the decoder
     * @return
     * 				true, if the decoder <code>decoderKey</code> supports HTTP-Put for operation <code>operationName</code>
     * @throws OwsExceptionReport
     */
    public boolean checkOperationHttpPutSupported(OperationDecoderKey decoderKey) throws OwsExceptionReport
    {
        return false;
    }

}
