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
package org.n52.sos.request.operator;

import org.n52.sos.ds.IOperationDAO;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> the IOperationDAO of this operator
 * @param <R> The AbstractServiceRequest to handle
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractRequestOperator<T extends IOperationDAO, R extends AbstractServiceRequest> implements IRequestOperator {
    private static final Logger log = LoggerFactory.getLogger(AbstractRequestOperator.class);
    private final T dao;
    private final String operationName;
    private final RequestOperatorKeyType requestOperatorKeyType;
    private final Class<R> requestType;

    public AbstractRequestOperator(String service, String version, String operatioName, Class<R> requestType) {
        this.operationName = operatioName;
        this.requestOperatorKeyType = new RequestOperatorKeyType(new ServiceOperatorKeyType(service, version), operationName);
        this.requestType = requestType;
        this.dao = (T) Configurator.getInstance().getOperationDaoRepository().getOperationDAO(operationName);
        log.info("{} initialized successfully!", getClass().getSimpleName());

    }

    protected final T getDao() {
        return dao;
    }

    @Override
    public final IExtension getExtension(Object connection) throws OwsExceptionReport {
        return getDao().getExtension(connection);
    }

    @Override
    public final OWSOperation getOperationMetadata(String service, String version, Object connection) throws OwsExceptionReport {
        return getDao().getOperationsMetadata(service, version, connection);
    }

    protected final String getOperationName() {
        return this.operationName;
    }

    @Override
    public final boolean hasImplementedDAO() {
        return getDao() != null;
    }

    @Override
    public final RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    protected abstract ServiceResponse receive(R request) throws OwsExceptionReport;

    @Override
    public final ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (requestType.isAssignableFrom(request.getClass())) {
            return receive(requestType.cast(request));
        } else {
            String exceptionText = String.format("Received request is not a %s!", requestType.getSimpleName());
            log.debug(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }

}
