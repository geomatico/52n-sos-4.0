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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.n52.sos.ds.hibernate.H2Configuration;
import org.n52.sos.service.SosService;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class AbstractSosServiceTest {
    private static SosService service;
    private static final ServletContext servletContext = new MockServletContext();
    private static final ServletConfig servletConfig = new MockServletConfig(servletContext);

    @BeforeClass
    public static void setUp() throws ServletException, IOException {
        H2Configuration.assertInitialized();
        service = new SosService();
        service.init(servletConfig);
    }

    protected MockHttpServletResponse execute(RequestBuilder b){
        try {
            MockHttpServletResponse res = new MockHttpServletResponse();
            HttpServletRequest req = b.context(servletContext).build();
            service.service(req, res);
            return res;
        } catch (ServletException ex) {
            throw new AssertionError("Error in servlet", ex);
        } catch (IOException ex) {
            throw new AssertionError("Error in servlet", ex);
        }
    }

    protected Element getResponseAsNode(MockHttpServletResponse res) {
        try {
            assertThat(res, is(not(nullValue())));
            byte[] response = res.getContentAsByteArray();
            assertThat(response, is(not(nullValue())));
            assertThat(response.length, is(not(0)));
            Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(response)).getDocumentElement();
            return node;
        } catch (ParserConfigurationException ex) {
            throw new AssertionError("Error parsing response", ex);
        } catch (SAXException ex) {
            throw new AssertionError("Error parsing response", ex);
        } catch (IOException ex) {
            throw new AssertionError("Error parsing response", ex);
        }
    }
}
