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
package org.n52.sos.request;

import org.n52.sos.exception.ows.concrete.MissingServiceParameterException;
import org.n52.sos.exception.ows.concrete.MissingVersionParameterException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swes.SwesExtensions;
import org.n52.sos.service.AbstractServiceCommunicationObject;
import org.n52.sos.service.operator.ServiceOperatorKeyType;

/**
 * abstract super class for all service request classes
 *
 */
public abstract class AbstractServiceRequest extends AbstractServiceCommunicationObject {
    protected ServiceOperatorKeyType[] serviceOperatorKeyTypes;
    protected SwesExtensions extensions;

    /**
     * @return the operationName
     */
    public abstract String getOperationName();

    public ServiceOperatorKeyType[] getServiceOperatorKeyType() throws OwsExceptionReport {
        if (serviceOperatorKeyTypes == null) {
            checkServiceAndVersionParameter();
            serviceOperatorKeyTypes = new ServiceOperatorKeyType[] {
				new ServiceOperatorKeyType(getService(), getVersion())
			};
        }
        return serviceOperatorKeyTypes;
    }

    private void checkServiceAndVersionParameter() throws OwsExceptionReport {
        CompositeOwsException exceptions = new CompositeOwsException();
        if (getService() == null) {
            exceptions.add(new MissingServiceParameterException());
        } else if (getService().isEmpty()) {
            exceptions.add(new MissingServiceParameterException());
        } else if (getVersion() == null) {
            exceptions.add(new MissingVersionParameterException());
        } else if (getVersion().isEmpty()) {
            exceptions.add(new MissingVersionParameterException());
        }
        exceptions.throwIfNotEmpty();
    }

    public SwesExtensions getExtensions() {
        return extensions;
    }

    public void setExtensions(SwesExtensions extensions) {
        this.extensions = extensions;
    }

	@Override
	public String toString() {
		return String.format("%s[service=%s, version=%s, operation=%s]",
				getClass().getName(),
				getService(), getVersion(), getOperationName());
	}

}
