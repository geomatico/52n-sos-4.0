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
package org.n52.sos.decode;

import org.n52.sos.ogc.sos.SosConstants;

/**
 *
 * @author auti
 */
public class RequestDecoderKey {
    
    private String service;
    private String version;
    private String operation;
    
    public RequestDecoderKey(String operation) {
        this(null, operation);
    }

    public RequestDecoderKey(String version, String operation) {
        this(SosConstants.SOS, version, operation);
    }

    public RequestDecoderKey(String service, String version, String operation) {
        this.service = service;
        this.version = version;
        this.operation = operation;
    }

    public String getService() {
        return service;
    }

    public String getVersion() {
        return version;
    }

    public String getOperation() {
        return operation;
    }

    public boolean isServiceSet() {
        return service != null;
    }

    public boolean isVersionSet() {
        return version != null;
    }

    public boolean isOperationSet() {
        return operation != null;
    }

    public boolean isCompatible(RequestDecoderKey toDecode) {
        if ((isServiceSet() && !getService().equals(toDecode.getService()))
            || (isVersionSet() && !getVersion().equals(toDecode.getVersion()))
            || (isOperationSet() && !getOperation().equals(toDecode.getOperation()))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return String.format("%s[service=%s, version=%s, operation=%s]",
                getClass().getSimpleName(), getService(), getVersion(), getOperation());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (isServiceSet()   ? getService().hashCode()   : 0);
        hash = 53 * hash + (isVersionSet()   ? getVersion().hashCode()   : 0);
        hash = 53 * hash + (isOperationSet() ? getOperation().hashCode() : 0);
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
        final RequestDecoderKey other = (RequestDecoderKey) obj;
        if ((!isServiceSet()) ? (other.isServiceSet())
                : !this.getService().equals(other.getService())) {
            return false;
        }
        if ((!isVersionSet()) ? (other.isVersionSet())
                : !this.getVersion().equals(other.getVersion())) {
            return false;
        }
        if ((!isOperationSet()) ? (other.isOperationSet())
                : !this.getOperation().equals(other.getOperation())) {
            return false;
        }
        return true;
    }
}
