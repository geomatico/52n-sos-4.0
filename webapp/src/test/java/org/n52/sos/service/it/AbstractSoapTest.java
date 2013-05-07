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

package org.n52.sos.service.it;

import static org.hamcrest.Matchers.*;

import javax.xml.namespace.NamespaceContext;

import org.apache.xmlbeans.XmlObject;
import org.hamcrest.Matcher;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3c.dom.Node;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class AbstractSoapTest extends AbstractTransactionalTestv2{
    protected static final NamespaceContext CTX = new SosNamespaceContext();

    public static Matcher<Node> invalidServiceParameterValueExceptionFault(final String parameter) {
        return soapFault(OwsExceptionCode.InvalidParameterValue, OWSConstants.RequestParams.service,
                         "The value of the mandatory parameter 'service' must be 'SOS'. Delivered value was: " +
                         parameter);
    }

    public static Matcher<Node> soapFault(final Enum<?> code, final Enum<?> locator, final String text) {
        return allOf(hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Code/soap:Subcode/soap:Value",
                              CTX, endsWith(":" + code.name())),
                     hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Reason/soap:Text[@xml:lang='en']",
                              CTX, is("The request contained an invalid parameter value.")),
                     hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/@exceptionCode",
                              CTX, is(code.name())),
                     hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/@locator",
                              CTX, is(locator.name())),
                     hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/ows:ExceptionText",
                              CTX, is(text)));
    }

    protected XmlObject envelope(final XmlObject r) {
        final EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        envDoc.addNewEnvelope().addNewBody().set(r);
        return envDoc;
    }


    protected MockHttpServletResponse execute(final XmlObject doc) {
        return execute(RequestBuilder.post("/soap")
                .accept("application/soap+xml")
                .contentType("application/soap+xml")
                .entity(envelope(doc).xmlText()));
    }
}
