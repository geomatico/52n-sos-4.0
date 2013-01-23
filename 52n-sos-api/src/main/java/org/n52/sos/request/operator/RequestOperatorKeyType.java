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
package org.n52.sos.request.operator;

import org.n52.sos.service.operator.ServiceOperatorKeyType;

public class RequestOperatorKeyType implements Comparable<RequestOperatorKeyType> {

    private ServiceOperatorKeyType serviceOperatorKeyType;

    private String operationName;

    public RequestOperatorKeyType() {
        super();
    }

    public RequestOperatorKeyType(ServiceOperatorKeyType serviceOperatorKeyType, String operationName) {
        super();
        this.serviceOperatorKeyType = serviceOperatorKeyType;
        this.operationName = operationName;
    }

    public ServiceOperatorKeyType getServiceOperatorKeyType() {
        return serviceOperatorKeyType;
    }

    public void setServiceOperatorKeyType(ServiceOperatorKeyType serviceOperatorKeyType) {
        this.serviceOperatorKeyType = serviceOperatorKeyType;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public int compareTo(RequestOperatorKeyType o) {
        if (o instanceof RequestOperatorKeyType) {
            if (serviceOperatorKeyType.equals(o.getServiceOperatorKeyType())
                    && operationName.equals(o.getOperationName())) {
                return 0;
            }
            return 1;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object paramObject) {
        if (getServiceOperatorKeyType() != null && getOperationName() != null && paramObject instanceof RequestOperatorKeyType) {
            RequestOperatorKeyType toCheck = (RequestOperatorKeyType) paramObject;
            return (getServiceOperatorKeyType().equals(toCheck.getServiceOperatorKeyType()) && getOperationName()
                    .equals(toCheck.getOperationName()));
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + serviceOperatorKeyType.hashCode();
        hash += 31 * hash + operationName.hashCode();
        return hash;
    }

}
