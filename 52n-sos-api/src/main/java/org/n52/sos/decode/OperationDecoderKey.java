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
package org.n52.sos.decode;

public class OperationDecoderKey extends DecoderKey {

    private final String service;
    private final String version;
    private final String operation;

    public OperationDecoderKey(String service, String version, String operation) {
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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            final OperationDecoderKey o = (OperationDecoderKey) obj;
            return (eq(getService(), o.getService()))
                    && (eq(getVersion(), o.getVersion()))
                    && (eq(getOperation(), o.getOperation()));
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[service=%s, version=%s, operation=%s]",
                getClass().getSimpleName(), getService(), getVersion(), getOperation());
    }

    @Override
    public int hashCode() {
        return hash(7, 67, getService(), getVersion(), getOperation());
    }

    @Override
    public int getSimilarity(DecoderKey key) {
        return this.equals(key) ? 0 : -1;
    }
}
