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
package org.n52.sos.cache;

import static org.junit.Assert.*;
import static org.n52.sos.util.builder.InsertObservationRequestBuilder.aInsertObservationRequest;
import static org.n52.sos.util.builder.ObservablePropertyBuilder.aObservableProperty;
import static org.n52.sos.util.builder.ObservationBuilder.anObservation;
import static org.n52.sos.util.builder.ObservationConstellationBuilder.aObservationConstellation;
import static org.n52.sos.util.builder.ProcedureDescriptionBuilder.aSensorMLProcedureDescription;
import static org.n52.sos.util.builder.QuantityObservationValueBuilder.aQuantityValue;
import static org.n52.sos.util.builder.QuantityValueBuilder.aQuantitiy;
import static org.n52.sos.util.builder.SamplingFeatureBuilder.aSamplingFeature;

import java.util.Collections;

import org.junit.Test;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.request.InsertObservationRequest;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 */
public class InMemoryCacheControllerTest
{
	private InsertObservationRequest request;
	private InMemoryCacheController controller;

	@Test (expected=IllegalArgumentException.class)	public void 
	should_throw_IllegalArgumentException_when_receiving_null_parameter() 
			throws OwsExceptionReport {
		InMemoryCacheController controller = new TestableInMemoryCacheController();
		controller.updateAfterObservationInsertion(null);
	}
	
	@Test public void
	should_update_global_temporal_BoundingBox_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertEquals("maxtime",
				controller.getMaxEventTime(),
				((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue());
		
		assertEquals("mintime",
				controller.getMinEventTime(),
				((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue());
	}
	
	@Test public void
	should_contain_procedure_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("procedure NOT in cache",
				controller.getProcedures().contains(request.getAssignedSensorId()));
		
		assertTrue("offering -> procedure relation not in cache",
				controller.getProcedures4Offering(request.getOfferings().get(0)).contains(request.getAssignedSensorId()));
		
		assertTrue("observable-property -> procedure relation NOT in cache",
				controller.getKObservablePropertyVProcedures().get(getObservablePropertyIdentifier(request)).contains(request.getAssignedSensorId()));
		
		assertTrue("procedure -> observable-property relation NOT in cache",
				controller.getKProcedureVObservableProperties().get(request.getAssignedSensorId()).contains(getObservablePropertyIdentifier(request)) );
		
		assertTrue("procedure -> offering relation NOT in cache",
				controller.getOfferings4Procedure(request.getAssignedSensorId()).contains(request.getOfferings().get(0)));
		
	}

	@Test public void 
	should_contain_FeatureOfInterest_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("feature NOT in cache",
				controller.getFeatureOfInterest().contains(getFoiIdFromInsertObservationRequest(request)));
		
		assertTrue("feature -> procedure relation NOT in cache",
				controller.getProcedures4FeatureOfInterest(getFoiIdFromInsertObservationRequest(request)).contains(request.getAssignedSensorId()));
		
		assertTrue("no parent features for feature",
				controller.getParentFeatures(Collections.singletonList(getFoiIdFromInsertObservationRequest(request)), true, false).isEmpty());
		
		assertTrue("no child features for feature",
				controller.getParentFeatures(Collections.singletonList(getFoiIdFromInsertObservationRequest(request)), true, false).isEmpty());
		
		assertTrue("feature to offering relation",
				controller.getKOfferingVFeatures().get(request.getOfferings().get(0)).contains(getFoiIdFromInsertObservationRequest(request)));
		
	}
	
	@Test public void 
	should_contain_feature_type_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("feature type of observation is NOT in cache",
				controller.getFeatureOfInterestTypes().contains(
						((SosSamplingFeature)request.getObservations().get(0).getObservationConstellation().getFeatureOfInterest()).getFeatureType()));
	}
	
	@Test public void 
	should_contain_envelopes_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertEquals("global envelope",
				controller.getGlobalEnvelope(),
				createEnvelopeWithDefaultEPSG(request, controller));
		
		assertEquals("offering envelop",
				controller.getEnvelopeForOffering(request.getOfferings().get(0)),
				createEnvelopeWithDefaultEPSG(request, controller));
	}

	@Test public void
	should_contain_observation_timestamp_in_temporal_envelope_of_offering_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("temporal envelope of does NOT contain observation timestamp",
			(
				controller.getMinTimeForOffering(request.getOfferings().get(0))
				.isBefore(((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue())
				||
				controller.getMinTimeForOffering(request.getOfferings().get(0))
				.isEqual(((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue())
			)
			&&
			(
				controller.getMaxTimeForOffering(request.getOfferings().get(0))
				.isAfter(((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue())
				|| 
				controller.getMaxTimeForOffering(request.getOfferings().get(0))
				.isEqual(((TimeInstant)request.getObservations().get(0).getPhenomenonTime()).getValue())
			)
		);
	}
		
	@Test public void
	should_contain_observalbe_property_after_InsertObservation()
		throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("offering -> observable property NOT in cache",
				controller.getObservablePropertiesForOffering(request.getOfferings().get(0)).contains(getObservablePropertyIdentifier(request)));
		
		assertTrue("observable property -> offering NOT in cache",
				controller.getOfferings4ObservableProperty(getObservablePropertyIdentifier(request)).contains(request.getOfferings().get(0)));
	}

	private void updateEmptyCacheWithSingleObservation() throws OwsExceptionReport
	{
		request = insertObservationRequestExample();
		controller = new TestableInMemoryCacheController();
		controller.cancel(); // <-- we don't want no timer to run!
		controller.updateAfterObservationInsertion(request);
	}

	private SosEnvelope createEnvelopeWithDefaultEPSG(InsertObservationRequest request,
			InMemoryCacheController controller)
	{
		return new SosEnvelope(
				((SosSamplingFeature)request.getObservations().get(0).getObservationConstellation().getFeatureOfInterest()).getGeometry().getEnvelopeInternal(),
				controller.getDefaultEPSG());
	}

	private String getFoiIdFromInsertObservationRequest(InsertObservationRequest request)
	{
		return request.getObservations().get(0).getObservationConstellation().getFeatureOfInterest().getIdentifier().getValue();
	}

	private InsertObservationRequest insertObservationRequestExample()
	{
		String sensor = "test-procedure";
		InsertObservationRequest request = aInsertObservationRequest()
				.setProcedureId(sensor)
				.addOffering("test-offering")
				.addObservation(anObservation()
					.setObservationConstellation(aObservationConstellation()
						.setFeature(aSamplingFeature()
							.setIdentifier("test-feature")
							.setFeatureType(SFConstants.FT_SAMPLINGPOINT)
							.setGeometry(52.0,7.5,4326)
							.build())
						.setProcedure(aSensorMLProcedureDescription()
							.setIdentifier(sensor)
							.build())
						.setObservationType(OMConstants.OBS_TYPE_MEASUREMENT)
						.setObservableProperty(aObservableProperty()
							.setIdentifier("test-observed-property")
							.build())
						.build())
					.setValue(aQuantityValue()
							.setValue(
								aQuantitiy()
									.setValue(2.0)
									.setUnit("m")
									.build())
							.setPhenomenonTime(System.currentTimeMillis())
							.build()
							)
					.build())
				.build();
		return request;
	}
	
	private String getObservablePropertyIdentifier(InsertObservationRequest request)
	{
		return request.getObservations().get(0).getObservationConstellation().getObservableProperty().getIdentifier();
	}

	private class TestableInMemoryCacheController extends InMemoryCacheController
	{
		protected long getUpdateInterval()
		{
			return 60000;
		}
		
		protected int getDefaultEPSG()
		{
			return 4326;
		}
	}
}
