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

import static org.n52.sos.service.it.RequestBuilder.post;

import java.io.UnsupportedEncodingException;

import net.opengis.sosREST.x10.SensorDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SensorsTest extends RestBindingTest {

	@Test public void
	should_return_valid_sosREST_Sensor()
			throws UnsupportedEncodingException, XmlException, OwsExceptionReport
	{
		final MockHttpServletResponse mockResponse = addSensor("test-sensor-id","test-offering-id");

		final Node response = getResponseAsNode(mockResponse);
		final XmlObject xbResponse = XmlObject.Factory.parse(mockResponse.getContentAsString());

		assertThat(xbResponse, instanceOf(SensorDocument.class));
		assertThat(response, hasXPath("//sosREST:Sensor/sml:System", NS_CTXT));
	}

	@Test public void
	should_return_list_with_sosRest_Sensors() 
			throws OwsExceptionReport 
	{
		final String sensorId1 = "test-sensor-1";
		final String sensorId2 = "test-sensor-2";
		
		addSensor(sensorId1,"test-offering-1");
		addSensor(sensorId2,"test-offering-2");

		final MockHttpServletResponse mockResponse = getResource(REST_CONFIG.getResourceSensors());
		final Node response = getResponseAsNode(mockResponse);
		
		assertThat(response, hasXPath("//sosREST:SensorCollection", NS_CTXT));
		assertThat(response, hasXPath(link(REST_CONFIG.getResourceRelationSelf(), REST_CONFIG.getResourceSensors()), NS_CTXT));
		assertThat(response, hasXPath(link(REST_CONFIG.getResourceRelationSensorGet(), REST_CONFIG.getResourceSensors() + "/" + sensorId1), NS_CTXT));
		assertThat(response, hasXPath(link(REST_CONFIG.getResourceRelationSensorGet(), REST_CONFIG.getResourceSensors() + "/" + sensorId2), NS_CTXT));
	}

	protected MockHttpServletResponse 
	addSensor(final String sensorId, final String offeringId)
			throws OwsExceptionReport
	{
		return execute(post(REST_URL + "/" + REST_CONFIG.getResourceSensors())
				.accept(CONTENT_TYPE)
				.contentType(CONTENT_TYPE)
				.entity(createRestSensor(sensorId,offeringId)));
	}


	@Before public void 
	initTestDatabase()
			throws OwsExceptionReport
	{
		addObservationTypes();
	}

}
