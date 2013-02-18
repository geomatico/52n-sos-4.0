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
package org.n52.sos.config.sqlite.entities;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.n52.sos.request.operator.RequestOperatorKeyType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Entity(name = "operations")
public class Operation {

    @EmbeddedId
    private OperationKey key;
    private boolean active;

    public Operation() {
        this(null, null, null);
    }

    public Operation(RequestOperatorKeyType key) {
        this(key.getOperationName(),
             key.getServiceOperatorKeyType().getService(),
             key.getServiceOperatorKeyType().getVersion());
    }

    public Operation(String operation, String service, String version) {
        this.key = new OperationKey()
                .setOperationName(operation)
                .setService(service)
                .setVersion(version);
    }

    protected OperationKey getKey() {
        return this.key;
    }

    public Operation setKey(OperationKey key) {
        this.key = key;
        return this;
    }

    public String getOperationName() {
        return getKey().getOperationName();
    }

    public Operation setOperationName(String operationName) {
        getKey().setOperationName(operationName);
        return this;
    }

    public String getService() {
        return getKey().getService();
    }

    public Operation setService(String service) {
        getKey().setService(service);
        return this;
    }

    public String getVersion() {
        return getKey().getVersion();
    }

    public Operation setVersion(String version) {
        getKey().setVersion(version);
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public Operation setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public String toString() {
        return String.format("Operation[key=%s, active=%b", getKey(), isActive());
    }

    @Override
    public int hashCode() {
        return getKey() != null ? getKey().hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Operation) {
            Operation o = (Operation) obj;
            return getKey() == null ? o.getKey() == null : getKey().equals(o.getKey());
        }
        return false;
    }
}
