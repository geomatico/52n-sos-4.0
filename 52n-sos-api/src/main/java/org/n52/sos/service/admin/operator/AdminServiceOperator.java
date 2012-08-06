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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.util.SosRequestToResponseHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the different Listeners which are registered through the
 * config file. After parsing the request through the doOperation() method, the
 * request is send up to the specific Listener (e.g. GetCapabilitiesListener)
 * 
 */
public class AdminServiceOperator extends IAdminServiceOperator {

    /** the logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceOperator.class);

    /**
     * constructor
     * 
     */
    public AdminServiceOperator() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ASosAdminRequestOperator#doGetOperation(javax.servlet.http
     * .HttpServletRequest)
     */
    @Override
    public ServiceResponse doGetOperation(HttpServletRequest req) {

        ServiceResponse response = null;
        AbstractServiceRequest request = null;
        try {
            Map<String, String> kvp = new HashMap<String, String>();
            Iterator<?> parameterNames = req.getParameterMap().keySet().iterator();
            while (parameterNames.hasNext()) {
                // all key names to lower case
                String parameterName = (String) parameterNames.next();
                String parameterValue = (String) req.getParameterMap().get(parameterName);
                kvp.put(parameterName, parameterValue);
            }
            if (kvp.isEmpty()) {
                OwsExceptionReport owse =  Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.service.name());
                owse.addServiceException(Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.request.name()));
                throw owse;
            } else if (!kvp.isEmpty() && !kvp.containsKey(SosConstants.GetCapabilitiesParams.request.name())) {
                throw Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.request.name());
            } else if (!kvp.isEmpty() && !kvp.containsKey(SosConstants.GetCapabilitiesParams.service.name())) {
                throw Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.service.name());
            }
            IServiceOperator requestOperator = null;

            // receive request
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(
                    OwsExceptionCode.OperationNotSupported,
                    null,
                    "Please add the missing Listener into the pom.xml (new deploy) or into the sos.config (reload webapp), you find in your webapp/WEB-INF/conf.");
            LOGGER.error("Listener is unknown!", se);
            throw se;
        } catch (OwsExceptionReport owse) {
            LOGGER.debug("Error while performin GetOperation!", owse);
            response = SosRequestToResponseHelper.createExceptionResponse(owse);
        }

        return response;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.ASosAdminRequestOperator#doPostOperation(javax.servlet.http
     * .HttpServletRequest)
     */
    @Override
    public ServiceResponse doPostOperation(HttpServletRequest req) {
        String exceptionText = "The SOS administration backend does not support HTTP-Post requests!";
        LOGGER.error(exceptionText);
        OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
        owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
        return SosRequestToResponseHelper.createExceptionResponse(owse);
    }

//    @Override
//    public boolean checkOperationHttpGetSupported(String operationName, String version, String namespace)
//            throws Exception {
//        return true;
//    }
//
//    @Override
//    public boolean checkOperationHttpPostSupported(String operationName, String version, String namespace)
//            throws Exception {
//        return false;
//    }
//
//    @Override
//    public boolean hasImplementedDAO() {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    @Override
//    public String getOperationName() {
//        // TODO Auto-generated method stub
//        return null;
//    }


}
