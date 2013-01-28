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
import static org.n52.sos.util.builder.InsertSensorRequestBuilder.anInsertSensorRequest;
import static org.n52.sos.util.builder.InsertSensorResponseBuilder.anInsertSensorResponse;
import static org.n52.sos.util.builder.ObservablePropertyBuilder.aObservableProperty;
import static org.n52.sos.util.builder.ObservationBuilder.anObservation;
import static org.n52.sos.util.builder.ObservationConstellationBuilder.aObservationConstellation;
import static org.n52.sos.util.builder.ProcedureDescriptionBuilder.aSensorMLProcedureDescription;
import static org.n52.sos.util.builder.QuantityObservationValueBuilder.aQuantityValue;
import static org.n52.sos.util.builder.QuantityValueBuilder.aQuantitiy;
import static org.n52.sos.util.builder.SamplingFeatureBuilder.aSamplingFeature;

import java.util.Collections;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.InsertSensorResponse;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * TODO Eike: Test after InsertSensor
 * TODO Eike: Test after DeleteSensor
 * TODO Eike: Test after InsertResultTemplate
 * TODO Eike: Test after InsertResult
 * TODO Eike: Test after DeleteObservation => Store observation count for each offering
 */
public class InMemoryCacheControllerTest
{
	private AbstractServiceRequest request;
	private InMemoryCacheController controller;
	private AbstractServiceResponse response;

	@Before public void
	initController() 
	{
		controller = new TestableInMemoryCacheController();
		controller.cancel(); // <-- we don't want no timer to run!
	}

	@After public void 
	cleanUpAfterEachTest()
	{
		request = null;
		controller = null;
	}
	
	/* TESTS */
	
	/* Update after InsertObservation */
	
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
				((TimeInstant)((InsertObservationRequest) request).getObservations().get(0).getPhenomenonTime()).getValue());
		
		assertEquals("mintime",
				controller.getMinEventTime(),
				((TimeInstant)((InsertObservationRequest) request).getObservations().get(0).getPhenomenonTime()).getValue());
	}
	
	@Test public void
	should_contain_procedure_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("procedure NOT in cache",
				controller.getProcedures().contains(getSensorIdFromInsertObservation()));
		
		assertTrue("offering -> procedure relation not in cache",
				controller.getProcedures4Offering(getFirstOffering()).contains(getSensorIdFromInsertObservation()));
		
		assertTrue("observable-property -> procedure relation NOT in cache",
				controller.getKObservablePropertyVProcedures().get(getObservablePropertyFromInsertObservation()).contains(getSensorIdFromInsertObservation()));
		
		assertTrue("procedure -> observable-property relation NOT in cache",
				controller.getKProcedureVObservableProperties().get(getSensorIdFromInsertObservation()).contains(getObservablePropertyFromInsertObservation()) );
		
		assertTrue("procedure -> offering relation NOT in cache",
				controller.getOfferings4Procedure(getSensorIdFromInsertObservation()).contains(getFirstOffering()));
		
	}

	@Test public void 
	should_contain_FeatureOfInterest_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("feature NOT in cache",
				controller.getFeatureOfInterest().contains(getFoiIdFromInsertObservationRequest()));
		
		assertTrue("feature -> procedure relation NOT in cache",
				controller.getProcedures4FeatureOfInterest(getFoiIdFromInsertObservationRequest()).contains(getSensorIdFromInsertObservation()));
		
		assertTrue("no parent features for feature",
				controller.getParentFeatures(Collections.singletonList(getFoiIdFromInsertObservationRequest()), true, false).isEmpty());
		
		assertTrue("no child features for feature",
				controller.getParentFeatures(Collections.singletonList(getFoiIdFromInsertObservationRequest()), true, false).isEmpty());
		
		assertTrue("offering -> feature relation",
				controller.getKOfferingVFeatures().get(getFirstOffering()).contains(getFoiIdFromInsertObservationRequest()));
		
	}
	
	@Test public void 
	should_contain_feature_type_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("feature type of observation is NOT in cache",
				controller.getFeatureOfInterestTypes().contains(
						getFeatureTypeFromFirstObservation()));
	}

	@Test public void 
	should_contain_envelopes_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertEquals("global envelope",
				controller.getGlobalEnvelope(),
				getSosEnvelopeFromFirstObservation());
		
		assertEquals("offering envelop",
				controller.getEnvelopeForOffering(getFirstOffering()),
				getSosEnvelopeFromFirstObservation());
	}

	@Test public void
	should_contain_observation_timestamp_in_temporal_envelope_of_offering_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("temporal envelope of does NOT contain observation timestamp",
			(
				controller.getMinTimeForOffering(getFirstOffering())
				.isBefore(getPhenomenonTimeFromFirstObservation())
				||
				controller.getMinTimeForOffering(getFirstOffering())
				.isEqual(getPhenomenonTimeFromFirstObservation())
			)
			&&
			(
				controller.getMaxTimeForOffering(getFirstOffering())
				.isAfter(getPhenomenonTimeFromFirstObservation())
				|| 
				controller.getMaxTimeForOffering(getFirstOffering())
				.isEqual(getPhenomenonTimeFromFirstObservation())
			)
		);
	}

	@Test public void
	should_contain_observalbe_property_after_InsertObservation()
		throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("offering -> observable property NOT in cache",
				controller.getObservablePropertiesForOffering(getFirstOffering()).contains(getObservablePropertyFromInsertObservation()));
		
		assertTrue("observable property -> offering NOT in cache",
				controller.getOfferings4ObservableProperty(getObservablePropertyFromInsertObservation()).contains(getFirstOffering()));
	}

	@Test public void
	should_contain_offering_spatial_boundingbox_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("spatial bounding box of offering NOT contained in cache",
				controller.getEnvelopeForOffering(getFirstOffering()).isSetEnvelope());
		
		assertEquals("spatial bounding box of offering NOT same as feature envelope",
				controller.getEnvelopeForOffering(getFirstOffering()),
				getSosEnvelopeFromFirstObservation());
	}

	@Test public void
	should_contain_offering_observation_type_relation_after_InsertObservation()
			throws OwsExceptionReport {
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("observation type NOT in cache",
				controller.getObservationTypes().contains(getObservationTypeFromFirstObservation()));
		
		assertTrue("offering -> observation type relation NOT in cache",
				controller.getObservationTypes4Offering(getFirstOffering()).contains(getObservationTypeFromFirstObservation()));
	}

	@Test public void 
	should_contain_observation_id_after_InsertObservation()
			throws OwsExceptionReport{
		updateEmptyCacheWithSingleObservation();
		
		assertTrue("observation id NOT in cache",
				controller.getObservationIdentifiers().contains(((InsertObservationRequest) request).getObservations().get(0).getIdentifier().getValue()));
	}
	
	/* Update after InsertSensor */
	
	@Test (expected=IllegalArgumentException.class) public void 
	should_throw_IllegalArgumentException_if_called_with_one_or_more_null_parameters()
			throws OwsExceptionReport{
		controller.updateAfterSensorInsertion(null, null);
		insertSensorRequestExample();
		controller.updateAfterSensorInsertion((InsertSensorRequest) request, null);
		request = null;
		insertSensorResponseExample();
		controller.updateAfterSensorInsertion(null, (InsertSensorResponse) response);
		
	}
	
	@Test public void 
	should_contain_procedure_after_InsertSensor()
		throws OwsExceptionReport{
		
		updateEmptyCacheWithInsertSensor();
		
		assertTrue("procedure NOT in cache",
				controller.getProcedures().contains(getSensorIdFromInsertSensorRequest()));
	}
	
	@Test public void 
	should_contain_procedure_offering_relations_after_InsertSensor()
			throws OwsExceptionReport{
		updateEmptyCacheWithInsertSensor();
		
		assertTrue("offering -> procedure relation NOT in cache",
				controller.getProcedures4Offering( getAssignedOfferingId() ).contains( getSensorIdFromInsertSensorRequest() ));
		
		assertTrue("procedure -> offering relation NOT in cache",
				controller.getOfferings4Procedure(getSensorIdFromInsertSensorRequest()).contains( getAssignedOfferingId() )  );
	}
	
	@Test public void
	should_contain_observable_property_relations_after_InsertSensor()
			throws OwsExceptionReport {
		updateEmptyCacheWithInsertSensor();

		assertTrue("observable property -> procedure relation NOT in cache",
				controller
				.getKObservablePropertyVProcedures()
				.get( getObservablePropertyFromInsertSensor() )
				.contains(getAssignedProcedure())
				);

		assertTrue("procedure -> observable property relation NOT in cache",
				controller
				.getProcPhens()
				.get( getAssignedProcedure() ) 
				.contains( getObservablePropertyFromInsertSensor() )
				);
		
		assertTrue("observable property -> offering relation NOT in cache",
				controller
				.getOfferings4ObservableProperty(getObservablePropertyFromInsertSensor())
				.contains( getAssignedOfferingId() )
				);
		
		assertTrue("offering -> observable property relation NOT in cache",
				controller
				.getPhenomenons4Offering(getAssignedOfferingId())
				.contains(getObservablePropertyFromInsertSensor())
				);
		
	}

	@Test public void 
	should_contain_offering_name_after_InsertSensor()
			throws OwsExceptionReport{
		updateEmptyCacheWithInsertSensor();
		
		assertTrue("offering NOT in cache",
				controller.getOfferings().contains(getAssignedOfferingId()) );
	}
	
	@Test public void 
	should_contain_allowed_observation_types_after_InsertSensor()
			throws OwsExceptionReport{
		updateEmptyCacheWithInsertSensor();
		
		for (String observationType : ((InsertSensorRequest)request).getMetadata().getObservationTypes()) {
			assertTrue("observation type NOT in cache",
					controller.getAllowedKOfferingVObservationTypes().get(getAssignedOfferingId()).contains(observationType));
		}
	}
	
	@Test public void 
	should_contain_related_features_after_InsertObservation()
			throws OwsExceptionReport{
		updateEmptyCacheWithInsertSensor();
		
		assertTrue("offering -> related feature relations NOT in cache",
				controller.getKOfferingVRelatedFeatures().containsKey(getAssignedOfferingId()));
		
		for (SosFeatureRelationship relatedFeature : ((InsertSensorRequest)request).getRelatedFeatures()) {
			assertTrue("single \"offering -> related features relation\" NOT in cache",
					controller.getKOfferingVRelatedFeatures().get(getAssignedOfferingId())
					.contains(relatedFeature.getFeature().getIdentifier().getValue()));
			
			assertTrue("single \"related feature -> role relation\" NOT in cache",
					controller.getKRelatedFeaturesVRole().get(relatedFeature.getFeature().getIdentifier().getValue()).contains(relatedFeature.getRole()) );
		}
	}
	
	/* HELPER */
	
	private void 
	updateEmptyCacheWithSingleObservation()
			throws OwsExceptionReport {
		insertObservationRequestExample();
		controller.updateAfterObservationInsertion((InsertObservationRequest) request);
	}

	
	private void 
	updateEmptyCacheWithInsertSensor() 
			throws OwsExceptionReport {
		insertSensorRequestExample();
		insertSensorResponseExample();
		controller.updateAfterSensorInsertion((InsertSensorRequest)request,(InsertSensorResponse)response);
	}


	private 
	DateTime getPhenomenonTimeFromFirstObservation()
	{
		return ((TimeInstant)((InsertObservationRequest) request).getObservations().get(0).getPhenomenonTime()).getValue();
	}
	
	private 
	String getFeatureTypeFromFirstObservation()
	{
		return ((SosSamplingFeature)((InsertObservationRequest) request).getObservations().get(0).getObservationConstellation().getFeatureOfInterest()).getFeatureType();
	}
	
	private 
	String getAssignedProcedure()
	{
		return ((InsertSensorResponse)response).getAssignedProcedure();
	}

	private 
	String getObservablePropertyFromInsertSensor()
	{
		return ((InsertSensorRequest)request).getObservableProperty().get(0);
	}
	
	private
	String getAssignedOfferingId()
	{
		return ((InsertSensorResponse)response).getAssignedOffering();
	}
	
	private void
	insertSensorResponseExample()
	{
		response = anInsertSensorResponse()
				.setOffering("test-offering-identifier")
				.setProcedure("test-procedure")
				.build();
	}

	private void
	insertSensorRequestExample()
	{
		request = anInsertSensorRequest()
				.setProcedure(aSensorMLProcedureDescription()
						.setIdentifier("test-procedure")
						.setOffering("test-offering-identifier","test-offering-name")
						.build())
				.addObservableProperty("test-observable-property")
				.addObservationType("test-observation-type-1")
				.addObservationType("test-observation-type-2")
				.addRelatedFeature(aSamplingFeature()
						.setIdentifier("test-related-feature-1")
						.build(),
						"test-role-1")
				.addRelatedFeature(aSamplingFeature()
						.setIdentifier("test-related-feature-2")
						.build(),
						"test-role-2")
				.build();
	}

	private 
	String getSensorIdFromInsertObservation()
	{
		return ((InsertObservationRequest) request).getAssignedSensorId();
	}
	
	private 
	String getObservationTypeFromFirstObservation()
	{
		return ((InsertObservationRequest) request).getObservations().get(0).getObservationConstellation().getObservationType();
	}
	
	private 
	String getFirstOffering()
	{
		return ((InsertObservationRequest) request).getOfferings().get(0);
	}

	private
	SosEnvelope getSosEnvelopeFromFirstObservation()
	{
		return new SosEnvelope(
				((SosSamplingFeature)((InsertObservationRequest) request).getObservations().get(0).getObservationConstellation().getFeatureOfInterest()).getGeometry().getEnvelopeInternal(),
				controller.getDefaultEPSG());
	}
	
	private
	String getFoiIdFromInsertObservationRequest()
	{
		return ((InsertObservationRequest) request).getObservations().get(0).getObservationConstellation().getFeatureOfInterest().getIdentifier().getValue();
	}

	private void
	insertObservationRequestExample()
	{
		String sensor = "test-procedure";
		request = aInsertObservationRequest()
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
					.setIdentifier("codespace","my-id")
					.build())
				.build();
	}
	
	private
	String getObservablePropertyFromInsertObservation()
	{
		return ((InsertObservationRequest) request).getObservations().get(0).getObservationConstellation().getObservableProperty().getIdentifier();
	}

	private 
	String getSensorIdFromInsertSensorRequest()
	{
		return ((InsertSensorRequest)request).getProcedureDescription().getProcedureIdentifier();
	}
	
	private class 
	TestableInMemoryCacheController extends InMemoryCacheController
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
