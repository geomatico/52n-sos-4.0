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
package org.n52.sos.wsdl;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLOperationBuilder {
    private String name;
    private String version;
    private URI requestAction;
    private URI responseAction;
    private QName request;
    private QName response;
    private Collection<WSDLFault> faults;

    public WSDLOperationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public WSDLOperationBuilder setRequestAction(URI requestAction) {
        this.requestAction = requestAction;
        return this;
    }

    public WSDLOperationBuilder setResponseAction(URI responseAction) {
        this.responseAction = responseAction;
        return this;
    }

    public WSDLOperationBuilder setRequest(QName request) {
        this.request = request;
        return this;
    }

    public WSDLOperationBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    public WSDLOperationBuilder setRequest(String namespace, String localpart) {
        return setRequest(new QName(namespace, localpart));
    }

    public WSDLOperationBuilder setResponse(QName response) {
        this.response = response;
        return this;
    }

    public WSDLOperationBuilder setResponse(String namespace, String localpart) {
        return setResponse(new QName(namespace, localpart));
    }

    public WSDLOperationBuilder addFault(WSDLFault fault) {
        if (this.faults == null) {
            this.faults = new LinkedList<WSDLFault>();
        }
        this.faults.add(fault);
        return this;
    }

    public WSDLOperationBuilder setFaults(Collection<WSDLFault> faults) {
        this.faults = new LinkedList<WSDLFault>(faults);
        return this;
    }

    public WSDLOperationBuilder addFault(String name, URI action) {
        return addFault(new WSDLFault(name, action));
    }

    public WSDLOperation build() {
        return new WSDLOperation(name, version, requestAction, responseAction, request, response, faults);
    }
}
