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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hamcrest.Matcher;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.BeforeClass;
import org.n52.sos.ds.hibernate.H2Configuration;
import org.n52.sos.ds.hibernate.HibernateTestCase;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.util.ScrollableIterable;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.SosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk
 *         J&uuml;rrens</a>
 * @author Carsten Hollmann <c.hollmann@52north.org>
 * 
 * @since 4.0.0
 * 
 *        TODO Review @After and @Before calling between sub and super classes
 * 
 */
public abstract class AbstractSosServiceTest extends HibernateTestCase {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSosServiceTest.class);

    private static SosService service;

    private static final ServletContext servletContext = new MockServletContext();

    private static final ServletConfig servletConfig = new MockServletConfig(servletContext);

    private static final NamespaceContext namespaceContext = new SosNamespaceContext();

    /**
     * "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation"
     * , "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
     * "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEArrayObservation"
     * ,
     * "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation"
     * ,
     * "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation"
     * ,
     * "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation"
     */
    private final String[] defaultObservationTypes = {
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_SWEArrayObservation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CategoryObservation",
            "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TextObservation" };

    public static final String ENCODING = "UTF-8";

    @BeforeClass
    public static void setUp() throws ServletException, IOException {
        H2Configuration.assertInitialized();
        service = new SosService();
        service.init(servletConfig);
    }

    /**
     * Removes all entries of entity {@link ObservationType} from the database.
     * 
     * @throws OwsExceptionReport
     */
    protected void removeObservationTypes() throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            final ScrollableIterable<ObservationType> i =
                    ScrollableIterable.fromCriteria(session.createCriteria(ObservationType.class));
            for (final ObservationType o : i) {
                session.delete(o);
            }
            i.close();
            session.flush();
            transaction.commit();
        } catch (final HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        } finally {
            returnSession(session);
        }
    }

    /**
     * Add some default entries of entity {@link ObservationType} to the test
     * database.
     * 
     * @throws OwsExceptionReport
     * @see {@link #defaultObservationTypes}
     */
    protected void addObservationTypes() throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            for (int i = 0; i < defaultObservationTypes.length; i++) {
                final ObservationType ot = new ObservationType();
                ot.setObservationTypeId(i);
                ot.setObservationType(defaultObservationTypes[i]);
                session.save(ot);
            }
            session.flush();
            transaction.commit();
        } catch (final HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        } finally {
            returnSession(session);
        }
    }

    public static Matcher<Node> missingParameterValueException(final Enum<?> parameter) {
        return exception(OwsExceptionCode.MissingParameterValue, parameter,
                String.format("The value for the parameter '%s' is missing in the request!", parameter.name()));
    }

    public static Matcher<Node> missingServiceParameterValueException() {
        return exception(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.service,
                "The value for the parameter 'service' is missing in the request!");
    }

    public static Matcher<Node> invalidServiceParameterValueException(final String value) {
        return exception(OwsExceptionCode.InvalidParameterValue, OWSConstants.RequestParams.service,
                "The value of the mandatory parameter 'service' must be 'SOS'. Delivered value was: " + value);
    }

    public static Matcher<Node> missingVersionParameterValueException() {
        return exception(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.version,
                "The value for the parameter 'version' is missing in the request!");
    }

    public static Matcher<Node> invalidVersionParameterValueException(final String value) {
        return exception(OwsExceptionCode.InvalidParameterValue, OWSConstants.RequestParams.version,
                "The requested version is not supported!");
    }

    public static Matcher<Node> exception(final Enum<?> code, final Enum<?> locator, final String text) {
        return allOf(
                hasXPath("//ows:ExceptionReport/ows:Exception/@exceptionCode", namespaceContext, is(code.name())),
                hasXPath("//ows:ExceptionReport/ows:Exception/@locator", namespaceContext, is(locator.name())),
                hasXPath("//ows:ExceptionReport/ows:Exception/ows:ExceptionText", namespaceContext, is(text)));
    }

    protected MockHttpServletResponse execute(final RequestBuilder b) {
        try {
            final MockHttpServletResponse res = new MockHttpServletResponse();
            final HttpServletRequest req = b.context(servletContext).build();
            service.service(req, res);
            return res;
        } catch (final ServletException ex) {
            LOG.error("Error in servlet", ex);
            throw new AssertionError("Error in servlet");
        } catch (final IOException ex) {
            LOG.error("Error in servlet", ex);
            throw new AssertionError("Error in servlet");
        }
    }

    protected Element getResponseAsNode(final MockHttpServletResponse res) {
        try {
            assertThat(res, is(not(nullValue())));
            final byte[] response = res.getContentAsByteArray();
            assertThat(response, is(not(nullValue())));
            assertThat(response.length, is(not(0)));
            final Element node =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder()
                            .parse(new ByteArrayInputStream(response)).getDocumentElement();
            return node;
        } catch (final ParserConfigurationException ex) {
            LOG.error("Error parsing response", ex);
            throw new AssertionError("Error parsing response");
        } catch (final SAXException ex) {
            LOG.error("Error parsing response", ex);
            throw new AssertionError("Error parsing response");
        } catch (final IOException ex) {
            LOG.error("Error parsing response", ex);
            throw new AssertionError("Error parsing response");
        }
    }
}
