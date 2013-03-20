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

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.n52.sos.request.operator.RequestOperatorKeyType;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Embeddable
public class OperationKey implements Serializable {
    private static final long serialVersionUID = 6880719913150275598L;

    private String operation;
    private String service;
    private String version;

    public OperationKey() {
        this(null, null, null);
    }

    public OperationKey(RequestOperatorKeyType key) {
        this(key.getOperationName(),
             key.getServiceOperatorKeyType().getService(),
             key.getServiceOperatorKeyType().getVersion());
    }

    public OperationKey(String operation, String service, String version) {
        this.operation = operation;
        this.service = service;
        this.version = version;
    }

    public String getOperationName() {
        return operation;
    }

    public OperationKey setOperationName(String operationName) {
        this.operation = operationName;
        return this;
    }

    public String getService() {
        return service;
    }

    public OperationKey setService(String service) {
        this.service = service;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public OperationKey setVersion(String version) {
        this.version = version;
        return this;
    }
}
