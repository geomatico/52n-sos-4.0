package org.n52.sos.service.it.kvp.v2;

import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.service.it.AbstractKvpTest;
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
}
