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

package org.n52.sos.encode;

import org.n52.sos.service.operator.ServiceOperatorKeyType;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ResponseFormatKeyType {
    private ServiceOperatorKeyType serviceOperatorKeyType;
    private String responseFormat;

    public ResponseFormatKeyType(ServiceOperatorKeyType serviceOperatorKeyType, String responseFormat) {
        this.serviceOperatorKeyType = serviceOperatorKeyType;
        this.responseFormat = responseFormat;
    }

    public ResponseFormatKeyType() {
        this(null, null);
    }

    public ServiceOperatorKeyType getServiceOperatorKeyType() {
        return serviceOperatorKeyType;
    }

    public void setServiceOperatorKeyType(ServiceOperatorKeyType serviceOperatorKeyType) {
        this.serviceOperatorKeyType = serviceOperatorKeyType;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public String getService() {
        return getServiceOperatorKeyType() != null ? getServiceOperatorKeyType().getService() : null;
    }

    public String getVersion() {
        return getServiceOperatorKeyType() != null ? getServiceOperatorKeyType().getVersion() : null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.getServiceOperatorKeyType() != null ? this.getServiceOperatorKeyType().hashCode() : 0);
        hash = 71 * hash + (this.getResponseFormat() != null ? this.getResponseFormat().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResponseFormatKeyType other = (ResponseFormatKeyType) obj;
        if (this.getServiceOperatorKeyType() != other.getServiceOperatorKeyType()
            && (this.getServiceOperatorKeyType() == null
                || !this.getServiceOperatorKeyType().equals(other.getServiceOperatorKeyType()))) {
            return false;
        }
        if ((this.getResponseFormat() == null) ? (other.getResponseFormat() != null)
            : !this.getResponseFormat().equals(other.getResponseFormat())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[serviceOperatorKeyType=%s, responseFormat=%s]", getClass().getSimpleName(),
                             getServiceOperatorKeyType(), getResponseFormat());
    }
}
