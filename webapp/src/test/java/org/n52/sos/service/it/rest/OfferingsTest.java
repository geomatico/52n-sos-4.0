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
package org.n52.sos.service.it.rest;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.UnsupportedEncodingException;

import org.apache.xmlbeans.XmlException;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class OfferingsTest extends RestBindingTest {

	@Test
	public void should_return_status_ok()
	{
		final MockHttpServletResponse response = getOfferings();
		
		assertThat(response.getStatus(), is(SC_OK));
	}
	
	@Test
	public void should_return_correct_content_type()
	{
		final MockHttpServletResponse response = getOfferings();
		
		assertThat(response.getContentType(), is(CONTENT_TYPE));
	}
	
	@Test
	public void should_return_valid_sosRest_offeringslist()
	{
		final Node response = getResponseAsNode(getOfferings());
		
		assertThat(response, hasXPath("//sosREST:OfferingCollection",NS_CTXT));
		assertThat(response, hasXPath(selfLink(REST_CONFIG.getResourceOfferings()), NS_CTXT));
	}
	
	@Test
	public void should_contain_self_link()
	{
		final Node xbResponse = getResponseAsNode(getOfferings());
		
		assertThat(xbResponse, hasXPath(offeringsLink(REST_CONFIG.getResourceRelationSelf(), REST_CONFIG.getResourceOfferings()), NS_CTXT));
	}
	
	@Test
	@Ignore("Init DB with testdata not possible atm")
	public void should_contain_all_offering_links() throws UnsupportedEncodingException, XmlException, OwsExceptionReport
	{
		final String sensorId = "test-sensor-id";
		final String offeringId = "test-offering-id";
		addSensor(sensorId, offeringId);
		// FIXME the offeringId is not contained in the cache after adding the sensor
		addMeasurement(sensorId,offeringId,System.currentTimeMillis(),0.5,"test-feature","test-observable-property");
		final Node xbResponse = getResponseAsNode(getOfferings());
		
		assertThat(xbResponse, hasXPath("add count check here", NS_CTXT));
	}
	
	private String offeringsLink(final String relType,
			final String resType)
	{
		return "//sosREST:OfferingCollection/" + link(relType, resType);
	}

	private MockHttpServletResponse getOfferings()
	{
		return getResource(REST_CONFIG.getResourceOfferings());
	}
	
}
