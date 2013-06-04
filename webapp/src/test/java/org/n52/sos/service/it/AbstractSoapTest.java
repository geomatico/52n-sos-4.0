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

import javax.xml.namespace.NamespaceContext;

import org.apache.xmlbeans.XmlObject;
import org.hamcrest.Matcher;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.exception.swes.SwesExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3c.dom.Node;

/**
 * Abstract class for SOS SOAP requests tests
 * 
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk
 *         J&uuml;rrens</a>
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * 
 * @since 4.0.0
 */
public class AbstractSoapTest extends AbstractTransactionalTestv2 {
    protected static final NamespaceContext CTX = new SosNamespaceContext();

    private static final String INVALID_PARAMETER_VALUE = "The request contained an invalid parameter value.";

    private static final String MISSING_PARAMETER_VALUE =
            "The request did not include a value for a required parameter and this server does not declare a default value for it.";

    private static final String MISSING_PARAMETER = "[XmlBeans validation error:] Expected attribute: ";

    private static final String INVALID_REQUEST = "The request did not conform to its XML Schema definition.";

    public static Matcher<Node> invalidServiceParameterValueExceptionFault(final String parameter) {
        return soapFault(OwsExceptionCode.InvalidParameterValue, INVALID_PARAMETER_VALUE,
                OWSConstants.RequestParams.service,
                "The value of the mandatory parameter 'service' must be 'SOS'. Delivered value was: " + parameter);
    }

    public static Matcher<Node> invalidRequestMissingParameterExceptionFault(final String parameter) {
        return soapFault(SwesExceptionCode.InvalidRequest, INVALID_REQUEST, "Expected attribute: " + parameter,
                MISSING_PARAMETER + parameter);
    }

    public static Matcher<Node> missingServiceParameterValueExceptionFault() {
        return soapFault(OwsExceptionCode.MissingParameterValue, MISSING_PARAMETER_VALUE,
                OWSConstants.RequestParams.service, "The value for the parameter 'service' is missing in the request!");
    }

    public static Matcher<Node> invalidVersionParameterValueExceptionFault(final String parameter) {
        return soapFault(OwsExceptionCode.InvalidParameterValue, INVALID_PARAMETER_VALUE,
                OWSConstants.RequestParams.version, "The requested version is not supported!");
    }

    public static Matcher<Node> missingVersionParameterValueExceptionFault() {
        return soapFault(OwsExceptionCode.MissingParameterValue, MISSING_PARAMETER_VALUE,
                OWSConstants.RequestParams.version, "The value for the parameter 'version' is missing in the request!");
    }

    public static Matcher<Node> soapFault(final Enum<?> code, final String text, final Enum<?> locator,
            final String exceptionText) {
        return allOf(
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Code/soap:Subcode/soap:Value", CTX, endsWith(":"
                        + code.name())),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Reason/soap:Text[@xml:lang='en']", CTX, is(text)),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/@exceptionCode", CTX,
                        is(code.name())),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/@locator", CTX,
                        is(locator.name())),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/ows:ExceptionText", CTX,
                        is(exceptionText)));
    }

    public static Matcher<Node> soapFault(final Enum<?> code, final String text, final String locator,
            final String exceptionText) {
        return allOf(
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Code/soap:Subcode/soap:Value", CTX, endsWith(":"
                        + code.name())),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Reason/soap:Text[@xml:lang='en']", CTX, is(text)),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/@exceptionCode", CTX,
                        is(code.name())),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/@locator", CTX,
                        startsWith(locator)),
                hasXPath("//soap:Envelope/soap:Body/soap:Fault/soap:Detail/ows:Exception/ows:ExceptionText", CTX,
                        startsWith(exceptionText)));
    }

    protected XmlObject envelope(final XmlObject r) {
        final EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        envDoc.addNewEnvelope().addNewBody().set(r);
        return envDoc;
    }

    protected MockHttpServletResponse execute(final XmlObject doc) {
        return execute(RequestBuilder.post("/soap").accept("application/soap+xml").contentType("application/soap+xml")
                .entity(envelope(doc).xmlText()));
    }
}
