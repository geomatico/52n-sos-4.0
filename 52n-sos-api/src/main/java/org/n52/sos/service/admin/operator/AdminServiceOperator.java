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
package org.n52.sos.service.admin.operator;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.n52.sos.exception.AdministratorException;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.admin.AdministratorConstants.AdministratorParams;
import org.n52.sos.service.admin.request.AdminRequest;
import org.n52.sos.service.admin.request.operator.IAdminRequestOperator;
import org.n52.sos.util.KvpHelper;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminServiceOperator.class);

    public AdminServiceOperator() {
        super();
    }

	@Override
    public ServiceResponse doGetOperation(HttpServletRequest req) throws AdministratorException, OwsExceptionReport {

        AdminRequest request = null;
        if (req.getParameterMap() == null || (req.getParameterMap() != null && req.getParameterMap().isEmpty())) {
            LOGGER.debug("The mandatory parameter '" + OWSConstants.RequestParams.request.name() + "' is missing!");
            throw Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.request.name());
        }
        Map<String, String> parameterValueMap = KvpHelper.getKvpParameterValueMap(req);
        request = getRequestFromValues(parameterValueMap);
        IAdminRequestOperator requestOperator = Configurator.getInstance().getAdminRequestOperatorRepository()
				.getAdminRequestOperator(request.getService());
        if (requestOperator != null) {
            return requestOperator.receiveRequest(request);
        }
        String exceptionText = "The service administrator is not supported!";
        throw new AdministratorException(exceptionText);
    }

    private AdminRequest getRequestFromValues(Map<String, String> kvp) throws AdministratorException {
        if (kvp.isEmpty()) {
            String exceptionText = "The request is empty!";
            LOGGER.debug(exceptionText);
            throw new AdministratorException(exceptionText);
        } else if (!kvp.isEmpty() && !kvp.containsKey(AdministratorParams.request.name())) {
            String exceptionText = "The request does not contain mandatory '" + AdministratorParams.request.name() + "' parameter!";
            LOGGER.debug(exceptionText);
            throw new AdministratorException(exceptionText);
        } else if (!kvp.isEmpty() && !kvp.containsKey(AdministratorParams.service.name())) {
            String exceptionText = "The request does not contain mandatory '" + AdministratorParams.service.name() + "' parameter!";
            LOGGER.debug(exceptionText);
            throw new AdministratorException(exceptionText);
        }
        AdminRequest request = new AdminRequest();
        request.setService(kvp.get(AdministratorParams.service.name()));
        request.setRequest(kvp.get(AdministratorParams.request.name()));
        request.setParameters(kvp.get(AdministratorParams.parameter.name()));
        return request;
    }
}
