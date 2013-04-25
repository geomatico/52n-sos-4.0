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
package org.n52.sos.ds;


public class OperationDAOKeyType implements Comparable<OperationDAOKeyType> {
    
    private String operationName;
    private String service;
    
    public OperationDAOKeyType() {
        super();
    }

    public OperationDAOKeyType(String service, String operationName) {
        super();
        this.service = service;
        this.operationName = operationName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public int compareTo(OperationDAOKeyType o) {
        if (o instanceof OperationDAOKeyType) {
            if (service.equals(o.service) && operationName.equals(o.operationName)) {
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
        if (service != null && operationName != null && paramObject instanceof OperationDAOKeyType) {
            OperationDAOKeyType toCheck = (OperationDAOKeyType) paramObject;
            return (service.equals(toCheck.service) && operationName.equals(toCheck.operationName));
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
        final int prime = 31;
        int hash = 7;
        hash = prime * hash + ((this.service != null) ? this.service.hashCode() : 0);
        hash = prime * hash + ((this.operationName != null) ? this.operationName.hashCode() : 0);
        return hash;
    }

}
