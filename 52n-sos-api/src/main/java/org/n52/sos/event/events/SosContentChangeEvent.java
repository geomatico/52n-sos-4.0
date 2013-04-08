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
package org.n52.sos.event.events;

import org.n52.sos.event.SosEvent;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.AbstractServiceResponse;

/**
 * Abstract event that can be fired if a successfull request changed the contents of this service.
 *
 * @param <I> the request type
 * @param <O> the response type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class SosContentChangeEvent<I extends AbstractServiceRequest, O extends AbstractServiceResponse> implements SosEvent {
    private I request;
    private O response;

    public SosContentChangeEvent(I request, O response) {
        this.request = request;
        this.response = response;
    }

    public I getRequest() {
        return request;
    }

    public O getResponse() {
        return response;
    }

    @Override
    public String toString() {
        return String.format("%s[request=%s, response=%s]", getClass().getSimpleName(), getRequest(), getResponse());
    }
}
