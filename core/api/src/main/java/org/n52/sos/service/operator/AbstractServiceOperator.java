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
package org.n52.sos.service.operator;

import org.n52.sos.exception.ows.OperationNotSupportedException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.operator.RequestOperator;
import org.n52.sos.request.operator.RequestOperatorRepository;
import org.n52.sos.response.ServiceResponse;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class AbstractServiceOperator implements ServiceOperator {

    private final ServiceOperatorKeyType key;

    public AbstractServiceOperator(String service, String version) {
        this.key = new ServiceOperatorKeyType(service, version);
    }

    @Override
    public ServiceOperatorKeyType getServiceOperatorKeyType() {
        return key;
    }

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        ServiceResponse response = null;
        RequestOperator requestOperator = RequestOperatorRepository.getInstance()
				.getRequestOperator(getServiceOperatorKeyType(), request.getOperationName());
        if (requestOperator != null) {
            response = requestOperator.receiveRequest(request);
        }
        if (response != null) {
            return response;
        }
        throw new OperationNotSupportedException(request.getOperationName());
    }
}
