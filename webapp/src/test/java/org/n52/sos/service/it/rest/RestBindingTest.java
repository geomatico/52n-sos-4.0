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
import static org.n52.sos.service.it.RequestBuilder.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.NamespaceContext;

import net.opengis.om.x20.OMObservationType;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sosREST.x10.LinkType;
import net.opengis.sosREST.x10.ObservationDocument;
import net.opengis.sosREST.x10.ObservationType;
import net.opengis.sosREST.x10.SensorDocument;

import org.joda.time.DateTime;
import org.n52.sos.binding.rest.Constants;
import org.n52.sos.encode.OmEncoderv20;
import org.n52.sos.encode.SensorMLEncoderv101;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.service.it.AbstractTransactionalTestv2;
import org.n52.sos.service.it.SosNamespaceContext;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class RestBindingTest extends AbstractTransactionalTestv2{

	protected static final String REST_URL = "/rest";
	protected static final String CONTENT_TYPE = "application/gml+xml";
	protected static final NamespaceContext NS_CTXT = new SosNamespaceContext();
	protected static final Constants REST_CONFIG = Constants.getInstance();
	protected static final Configurator SOS_CONFIG = Configurator.getInstance();
	protected static final ServiceConfiguration SERVICE_CONFIG = ServiceConfiguration.getInstance();
	
	protected String link(final String relType, final String resTypeWithOrWithoutId)
	{
		return "sosREST:link[" +
				"@rel='" + REST_CONFIG.getEncodingNamespace() + "/" + relType + "'" +
				" and " + 
				"@href='" + REST_CONFIG.getServiceUrl() + REST_URL + "/" + resTypeWithOrWithoutId + "'" +
				" and " + 
				"@type='" + CONTENT_TYPE + "'" +		
				"]";
	}

	protected MockHttpServletResponse getResource(final String resType)
	{
		return execute(	get(REST_URL + "/" + resType).accept(CONTENT_TYPE));
	}

	/**
	 * Creating example sensor with id <tt>sensorId</tt> and offering <tt>offeringId</tt> observing <tt>test-observable-property</tt>
	 *  with type <tt>http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement</tt>
	 *  with feature <tt>http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint</tt>
	 */
	protected String createRestSensor(final String sensorId,
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
	
	private String createRestMeasurement(final String sensorId,
			final String offeringId,
			final long timestamp,
			final double value,
			final String featureId,
			final String observableProperty) throws OwsExceptionReport
	{
		final SosObservation o = new SosObservation();
		o.setValidTime(new TimePeriod(new DateTime(timestamp), new DateTime(timestamp)));
		o.setObservationConstellation(
				new SosObservationConstellation(
						new SosProcedureDescriptionUnknowType(sensorId, null, null),
						new AbstractSosPhenomenon(observableProperty), 
						new SosSamplingFeature(new CodeWithAuthority(featureId))));
		o.setResultTime(new TimeInstant(new DateTime(timestamp)));
		final QuantityValue sosValue = new QuantityValue(new BigDecimal(value));
		sosValue.setUnit("test-unit");
		o.setValue(
				new SosSingleObservationValue<BigDecimal>(
						new TimeInstant(new DateTime(timestamp)),
						sosValue));
		final OMObservationType xbObservation = (OMObservationType) new OmEncoderv20().encode(o);
		final ObservationDocument restObsDoc = ObservationDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		final ObservationType restObservation = restObsDoc.addNewObservation();
		restObservation.setOMObservation(xbObservation);
		final LinkType link = restObservation.addNewLink();
		link.setType(CONTENT_TYPE);
		link.setRel(REST_CONFIG.getEncodingNamespace() + "/" + REST_CONFIG.getResourceRelationOfferingGet());
		link.setHref(REST_CONFIG.getServiceUrl() + REST_CONFIG.getUrlPattern() + "/" + REST_CONFIG.getResourceOfferings() + "/" + offeringId);
		return restObsDoc.xmlText();
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
			final double longitude,
			final double altitudeV)
	{
		final List<SosSweCoordinate<?>> sweCoordinates = new ArrayList<SosSweCoordinate<?>>(3);
	    sweCoordinates.add(new SosSweCoordinate<Double>(northing, createSweQuantity(latitude, "y", "deg")));
	    sweCoordinates.add(new SosSweCoordinate<Double>(easting, createSweQuantity(longitude, "x", "deg")));
	    sweCoordinates.add(new SosSweCoordinate<Double>(altitude, createSweQuantity(altitudeV, "z", "m")));
	    return sweCoordinates;
	}

	private SosSweAbstractSimpleType<Double> createSweQuantity(final Double value,
			final String asixID,
			final String uom)
	{
		return new SosSweQuantity()
		.setValue(JavaHelper.asDouble(value))
		.setAxisID(asixID)
		.setUom(uom);
	}

	/**
	 * @see #createRestSensor(String, String)
	 */
	protected MockHttpServletResponse addSensor(final String sensorId,
			final String offeringId) throws OwsExceptionReport
	{
		return execute(post(REST_URL + "/" + REST_CONFIG.getResourceSensors())
				.accept(CONTENT_TYPE)
				.contentType(CONTENT_TYPE)
				.entity(createRestSensor(sensorId,offeringId)));
	}
	
	protected String selfLink(final String resType)
	{
		return selfLink(resType, null);
	}

	protected String selfLink(final String resType,
			final String resourceId)
	{
		return link(REST_CONFIG.getResourceRelationSelf(),resType + (resourceId!=null?"/" + resourceId:""));
	}

	protected String sensorLink(final String sensorId1)
	{
		return link(REST_CONFIG.getResourceRelationSensorGet(), REST_CONFIG.getResourceSensors() + "/" + sensorId1);
	}

	protected MockHttpServletResponse addMeasurement(final String sensorId,
			final String offeringId,
			final long timestamp,
			final double value,
			final String featureId,
			final String observableProperty) throws OwsExceptionReport
	{
		return execute(post(REST_URL + "/" + REST_CONFIG.getResourceObservations())
				.accept(CONTENT_TYPE)
				.contentType(CONTENT_TYPE)
				.entity(createRestMeasurement(sensorId,offeringId,timestamp,value,featureId,observableProperty)));		
	}
}