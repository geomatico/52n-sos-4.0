package org.n52.sos.service.it.soap.v2;

import net.opengis.swes.x20.ExtensibleRequestType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.it.AbstractSoapTest;
import org.n52.sos.service.it.SosServiceV2Test;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Abstract class for SOS 2.0 SOAP request tests
 * 
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * @since 4.0.0
 * 
 */
public abstract class AbstractSosV2SoapTest extends AbstractSoapTest implements SosServiceV2Test {

    protected void addServiceParameter(ExtensibleRequestType extensibleRequestType) {
        extensibleRequestType.setService(SERVICE);
    }

    protected void addVersionParameter(ExtensibleRequestType extensibleRequestType) {
        extensibleRequestType.setVersion(VERSION);
    }

    public void missingServiceParameter(ExtensibleRequestType extensibleRequestType, XmlObject xmlDocument) {
        MockHttpServletResponse res = execute(xmlDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res),
                is(invalidRequestMissingParameterExceptionFault(OWSConstants.RequestParams.service.name())));
    }

    @Test
    public abstract void missingServiceParameter() throws XmlException;

    public void emptyServiceParameter(ExtensibleRequestType extensibleRequestType, XmlObject xmlDocument) {
        extensibleRequestType.setService("");
        MockHttpServletResponse res = execute(xmlDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res), is(missingServiceParameterValueExceptionFault()));
    }

    @Test
    public abstract void emptyServiceParameter() throws XmlException;

    public void invalidServiceParameter(ExtensibleRequestType extensibleRequestType, XmlObject xmlDocument) {
        extensibleRequestType.setService("INVALID");
        MockHttpServletResponse res = execute(xmlDocument);
        assertThat(res.getStatus(), is(400));
        assertThat(getResponseAsNode(res), is(invalidServiceParameterValueExceptionFault("INVALID")));
    }

    @Test
    public abstract void invalidServiceParameter() throws XmlException;
}
