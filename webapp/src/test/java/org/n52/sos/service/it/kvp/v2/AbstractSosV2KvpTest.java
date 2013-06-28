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
package org.n52.sos.service.it.kvp.v2;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.service.it.AbstractKvpTest;
import org.n52.sos.service.it.RequestBuilder;
import org.n52.sos.service.it.SosServiceV2Test;
import org.w3c.dom.Node;

/**
 * . Abstract class for SOS 2.0 KVP tests. Contains tests for service and
 * version parameter
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.0.0
 * 
 */
public abstract class AbstractSosV2KvpTest extends AbstractKvpTest implements SosServiceV2Test {

    /**
     * Request parameter value
     */
    protected String request;

    /**
     * Abstract method to set the specific request parameter.
     */
    @Before
    public abstract void setRequest();

    @Override
    @Test
    public void missingServiceParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request).query(
                        OWSConstants.RequestParams.version, SERVICE)));
        assertThat(node, is(missingServiceParameterValueException()));
    }

    @Override
    @Test
    public void emptyServiceParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request)
                        .query(OWSConstants.RequestParams.service, "")
                        .query(OWSConstants.RequestParams.version, VERSION)));
        assertThat(node, is(missingServiceParameterValueException()));
    }

    @Override
    @Test
    public void invalidServiceParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request)
                        .query(OWSConstants.RequestParams.service, "INVALID")
                        .query(OWSConstants.RequestParams.version, VERSION)));
        assertThat(node, is(invalidServiceParameterValueException("INVALID")));
    }

    @Override
    @Test
    public void missingVersionParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request).query(
                        OWSConstants.RequestParams.service, SERVICE)));
        assertThat(node, is(missingVersionParameterValueException()));
    }

    @Override
    @Test
    public void emptyVersionParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request)
                        .query(OWSConstants.RequestParams.service, SERVICE)
                        .query(OWSConstants.RequestParams.version, "")));
        assertThat(node, is(missingVersionParameterValueException()));
    }

    @Override
    @Test
    public void invalidVersionParameter() {
        Node node =
                getResponseAsNode(execute(builder().query(OWSConstants.RequestParams.request, request)
                        .query(OWSConstants.RequestParams.service, SERVICE)
                        .query(OWSConstants.RequestParams.version, "1.2.3")));
        assertThat(node, is(invalidVersionParameterValueException("1.2.3")));
    }

    @Override
    public RequestBuilder getBuilderWithRequestServiceVersion() {
        return builder().query(OWSConstants.RequestParams.request, request)
                .query(OWSConstants.RequestParams.service, SERVICE).query(OWSConstants.RequestParams.version, VERSION);
    }
}
