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

import javax.persistence.Entity;

import org.n52.sos.request.operator.RequestOperatorKeyType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Entity(name = "operations")
public class Operation extends Activatable<OperationKey, Operation> {
    private static final long serialVersionUID = 6816894177423976948L;
    public Operation() {
        this(null, null, null);
    }

    public Operation(RequestOperatorKeyType key) {
        this(key.getOperationName(),
             key.getServiceOperatorKeyType().getService(),
             key.getServiceOperatorKeyType().getVersion());
    }

    public Operation(String operation, String service, String version) {
        super(new OperationKey()
                .setOperationName(operation)
                .setService(service)
                .setVersion(version));
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
}
