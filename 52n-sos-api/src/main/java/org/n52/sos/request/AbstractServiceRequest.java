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
package org.n52.sos.request;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.Util4Exceptions;

/**
 * abstract super class for all service request classes
 * 
 */
public abstract class AbstractServiceRequest extends AbstractServiceCommunicationObject {

    protected ServiceOperatorKeyType[] serviceOperatorKeyTypes;

    /**
     * @return the operationName
     */
    public abstract String getOperationName();

    public ServiceOperatorKeyType[] getServiceOperatorKeyType() throws OwsExceptionReport {
        if (serviceOperatorKeyTypes == null) {
            checkServiceAndVersionParameter();
            serviceOperatorKeyTypes = new ServiceOperatorKeyType[1];
            serviceOperatorKeyTypes[0] = new ServiceOperatorKeyType(getService(), getVersion());
        }
        return serviceOperatorKeyTypes;
    }
    
    private void checkServiceAndVersionParameter() throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
        if (getService() == null) {
            exceptions.add(Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.service.name()));
        } else if ( (getService() != null && getService().isEmpty())) {
            exceptions.add(Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.service.name()));
        } else if (getVersion() == null) {
            exceptions.add(Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.version.name()));
        } else if ( (getVersion() != null && getVersion().isEmpty())) {
            exceptions.add(Util4Exceptions.createMissingParameterValueException(OWSConstants.RequestParams.version.name()));
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

}
