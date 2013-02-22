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
import java.util.Collections;
import java.util.LinkedList;
import javax.xml.namespace.QName;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class WSDLOperation {
    
    private String name;
    private String version;
    private URI requestAction;
    private URI responseAction;
    private QName request;
    private QName response;
    private Collection<WSDLFault> faults;

    public static WSDLOperationBuilder newWSDLOperation() {
        return new WSDLOperationBuilder();
    }
    
    public WSDLOperation(String name, String version, URI requestAction, URI responseAction, QName request, QName response, Collection<WSDLFault> faults) {
        this.name = name;
        this.version = version;
        this.requestAction = requestAction;
        this.responseAction = responseAction;
        this.request = request;
        this.response = response;
        this.faults = faults;
    }

    public String getName() {
        return name;
    }

    public URI getRequestAction() {
        return requestAction;
    }

    public URI getResponseAction() {
        return responseAction;
    }

    public QName getRequest() {
        return request;
    }

    public QName getResponse() {
        return response;
    }
    
    public String getVersion() {
        return version;
    }

    public Collection<WSDLFault> getFaults() {
        return Collections.unmodifiableCollection(faults);
    }
}
