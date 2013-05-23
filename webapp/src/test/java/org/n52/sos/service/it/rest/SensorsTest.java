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

import static org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName.*;
import static org.n52.sos.service.it.RequestBuilder.post;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.opengis.sensorML.x101.SystemType;
import net.opengis.sosREST.x10.SensorDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import org.n52.sos.encode.SensorMLEncoderv101;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.springframework.mock.web.MockHttpServletResponse;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class SensorsTest extends RestBindingTest {

	/**
	 * 
	 */
	private static final ServiceConfiguration SERVICE_CONFIG = ServiceConfiguration.getInstance();

	@Before
	public void initTestDatabase() throws OwsExceptionReport
	{
		addObservationTypes();
	}
	
	@Test
	public void should_return_valid_sosREST_Sensor() throws UnsupportedEncodingException, XmlException, OwsExceptionReport
	{
		final MockHttpServletResponse mockResponse = execute(post(REST_URL + "/" + CONFIG.getResourceSensors())
				.accept(CONTENT_TYPE)
				.contentType(CONTENT_TYPE)
				.entity(createRestSensor("test-sensor-id","test-offering-id")));
		
		final Node response = getResponseAsNode(mockResponse);
		final XmlObject xbResponse = XmlObject.Factory.parse(mockResponse.getContentAsString());
		
		assertThat(xbResponse, instanceOf(SensorDocument.class));
		assertThat(response, hasXPath("//sosREST:Sensor/sml:System", NS_CTXT));
	}
	
	private String createRestSensor(final String sensorId,
			final String offeringId) throws OwsExceptionReport
	{
		final System system = (System) new System()
		.setPosition(new SosSMLPosition("test-sensor-position",true,SERVICE_CONFIG.getSrsNamePrefixSosV2()+4326,createCoordinates(52.0,7.5,42.0)))
		.setInputs(createInputList("test-observable-property"))
		.setOutputs(createOutputList("test-observable-property"))
		.setIdentifications(createIdentifications(sensorId,offeringId))
		.addCapabilities(new SosSMLCapabilities("InsertionMetadata", 
				new SosSweSimpleDataRecord()
						.addField(new SosSweField("sos:ObservationType", 
								new SosSweText().setValue("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")))
						.addField(new SosSweField("sos:FeatureOfInterestType",
								new SosSweText().setValue("http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint")))))
		.setIdentifier(sensorId);
		final SystemType xbSystem = (SystemType) new SensorMLEncoderv101().encode(system);
		final SensorDocument restSensor = SensorDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		final SystemType substitute = (SystemType) restSensor.addNewSensor().addNewProcess().substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
		substitute.set(xbSystem);
		return restSensor.xmlText();
	}

	private List<SosSMLIdentifier> createIdentifications(final String sensorId,
			final String offeringId)
	{
		return CollectionHelper.asList(new SosSMLIdentifier("uniqueId", "urn:ogc:def:identifier:OGC:1.0:uniqueID", sensorId), 
				new SosSMLIdentifier("offerings", "urn:ogc:def:identifier:OGC:offeringID", offeringId));
	}

	private List<SosSMLIo<?>> createOutputList(final String string)
	{
		final SosSMLIo<Double> quanti = new SosSMLIo<Double>(
				(SosSweQuantity) new SosSweQuantity()
				.setUom("m")
				.setDefinition("http://www.52north.org/test/observableProperty/42")
				.setIdentifier("test-observable-property"));
		final List<SosSMLIo<?>> outputs = new ArrayList<SosSMLIo<?>>(1);
		outputs.add(quanti);
		return Collections.unmodifiableList(outputs);
	}

	private List<SosSMLIo<?>> createInputList(final String string)
	{
		final SosSMLIo<String> io = new SosSMLIo<String>(
				(SosSweObservableProperty) new SosSweObservableProperty()
				.setDefinition("http://www.52north.org/test/observableProperty/42")
				.setIdentifier("test-observable-property"));
		final List<SosSMLIo<?>> inputs = new ArrayList<SosSMLIo<?>>(1);
		inputs.add(io);
		return Collections.unmodifiableList(inputs);
	}

	private List<SosSweCoordinate<?>> createCoordinates(final double latitude,
			final double longitude, final double altitudeV)
	{
		final List<SosSweCoordinate<?>> sweCoordinates = new ArrayList<SosSweCoordinate<?>>(3);
        sweCoordinates.add(new SosSweCoordinate<Double>(northing, createSweQuantity(latitude, "y", "deg")));
        sweCoordinates.add(new SosSweCoordinate<Double>(easting, createSweQuantity(longitude, "x", "deg")));
        sweCoordinates.add(new SosSweCoordinate<Double>(altitude, createSweQuantity(altitudeV, "z", "m")));
        return sweCoordinates;
	}
	
    private SosSweAbstractSimpleType<Double> createSweQuantity(final Double value, final String asixID, final String uom) {
    	return new SosSweQuantity()
    	.setValue(JavaHelper.asDouble(value))
    	.setAxisID(asixID)
    	.setUom(uom);
    }

}
