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
import static org.n52.sos.ogc.om.OMConstants.*;
import static org.n52.sos.ogc.om.features.SFConstants.FT_SAMPLINGPOINT;
import static org.n52.sos.util.builder.DataRecordBuilder.aDataRecord;
import static org.n52.sos.util.builder.InsertObservationRequestBuilder.aInsertObservationRequest;
import static org.n52.sos.util.builder.InsertResultTemplateRequestBuilder.anInsertResultTemplateRequest;
import static org.n52.sos.util.builder.InsertResultTemplateResponseBuilder.anInsertResultTemplateResponse;
import static org.n52.sos.util.builder.InsertSensorRequestBuilder.anInsertSensorRequest;
import static org.n52.sos.util.builder.InsertSensorResponseBuilder.anInsertSensorResponse;
import static org.n52.sos.util.builder.ObservablePropertyBuilder.aObservableProperty;
import static org.n52.sos.util.builder.ObservationBuilder.anObservation;
import static org.n52.sos.util.builder.ObservationConstellationBuilder.anObservationConstellation;
import static org.n52.sos.util.builder.ProcedureDescriptionBuilder.aSensorMLProcedureDescription;
import static org.n52.sos.util.builder.QuantityObservationValueBuilder.aQuantityValue;
import static org.n52.sos.util.builder.QuantityValueBuilder.aQuantitiy;
import static org.n52.sos.util.builder.SamplingFeatureBuilder.aSamplingFeature;
import static org.n52.sos.util.builder.SweDataArrayBuilder.aSweDataArray;
import static org.n52.sos.util.builder.SweDataArrayValueBuilder.aSweDataArrayValue;
import static org.n52.sos.util.builder.SweTimeBuilder.aSweTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.AbstractServiceResponse;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.util.builder.DeleteSensorRequestBuilder;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * Test after DeleteObservation => not possible with InMemory because of bounding box issues, for example.
 */
public class InMemoryCacheControllerTest
{
	/* FIXTURES */
	private static final String RELATED_FEATURE_ROLE_2 = "test-role-2";
	private static final String RELATED_FEATURE_ROLE = "test-role-1";
	private static final String FEATURE_2 = "test-related-feature-2";
	private static final String OBSERVATION_TYPE_2 = "test-observation-type-2";
	private static final String OBSERVATION_TYPE = "test-observation-type";
	private static final String OFFERING_NAME = "test-offering-name";
	private static final String OFFERING_EXTENSION_FOR_PROCEDURE_NAME = "-offering";
	private static final String OBSERVATION_ID = "test-observation-id";
	private static final String CODESPACE = "test-codespace";
	private static final String FEATURE = "test-feature";
	private static final String OBSERVABLE_PROPERTY = "test-observable-property";
	private static final String PROCEDURE = "test-procedure";
	private static final String PROCEDURE_2 = "test-procedure-2";
	private static final String RESULT_TEMPLATE_IDENTIFIER = "test-result-template";
	private AbstractServiceRequest request;
	private InMemoryCacheController controller;
	private AbstractServiceResponse response;
	private SosObservation observation;

	@Before public void
	initControllerAndStopTimer() 
	{
		controller = new TestableInMemoryCacheController();
		controller.cancel(); // <-- we don't want no timer to run!
	}

	@After public void 
	setAllFixturesToNullAfterEachTest()
	{
		request = null;
		controller = null;
		observation = null;
		response = null;
	}
	
	/* TESTS */
	
	/* Update after InsertObservation */
	
	@Test (expected=IllegalArgumentException.class)	public void 
	should_throw_IllegalArgumentException_when_receiving_null_parameter_after_InsertObservation() 
			throws OwsExceptionReport {
		controller.updateAfterObservationInsertion(null);
	}
	
	@Test public void
	should_update_global_temporal_BoundingBox_after_InsertObservation()
			throws OwsExceptionReport {
		updateCacheWithSingleObservation();
		
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
		updateCacheWithSingleObservation();
		
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
		updateCacheWithSingleObservation();
		
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
		updateCacheWithSingleObservation();
		
		assertTrue("feature type of observation is NOT in cache",
				controller.getFeatureOfInterestTypes().contains(
						FT_SAMPLINGPOINT));
	}

	@Test public void 
	should_contain_envelopes_after_InsertObservation()
			throws OwsExceptionReport {
		updateCacheWithSingleObservation();
		
		assertEquals("global envelope",
				controller.getGlobalEnvelope(),
				getSosEnvelopeFromObservation(((InsertObservationRequest) request).getObservations().get(0)));
		
		assertEquals("offering envelop",
				controller.getEnvelopeForOffering(getFirstOffering()),
				getSosEnvelopeFromObservation(((InsertObservationRequest) request).getObservations().get(0)));
		
		assertTrue("spatial bounding box of offering NOT contained in cache",
				controller.getEnvelopeForOffering(getFirstOffering()).isSetEnvelope());
		
		assertEquals("spatial bounding box of offering NOT same as feature envelope",
				controller.getEnvelopeForOffering(getFirstOffering()),
				getSosEnvelopeFromObservation(((InsertObservationRequest) request).getObservations().get(0)));
	}

	@Test public void
	should_contain_observation_timestamp_in_temporal_envelope_of_offering_after_InsertObservation()
			throws OwsExceptionReport {
		updateCacheWithSingleObservation();
		
		assertTrue("temporal envelope of does NOT contain observation timestamp",
			(
				controller.getMinTimeForOffering(getFirstOffering())
				.isBefore(getPhenomenonTimeFromObservation())
				||
				controller.getMinTimeForOffering(getFirstOffering())
				.isEqual(getPhenomenonTimeFromObservation())
			)
			&&
			(
				controller.getMaxTimeForOffering(getFirstOffering())
				.isAfter(getPhenomenonTimeFromObservation())
				|| 
				controller.getMaxTimeForOffering(getFirstOffering())
				.isEqual(getPhenomenonTimeFromObservation())
			)
		);
	}

	@Test public void
	should_contain_observable_property_after_InsertObservation()
		throws OwsExceptionReport {
		updateCacheWithSingleObservation();
		
		assertTrue("offering -> observable property NOT in cache",
				controller.getObservablePropertiesForOffering(getFirstOffering()).contains(getObservablePropertyFromInsertObservation()));
		
		assertTrue("observable property -> offering NOT in cache",
				controller.getOfferings4ObservableProperty(getObservablePropertyFromInsertObservation()).contains(getFirstOffering()));
	}

	@Test public void
	should_contain_offering_observation_type_relation_after_InsertObservation()
			throws OwsExceptionReport {
		updateCacheWithSingleObservation();
		
		assertTrue("observation type NOT in cache",
				controller.getObservationTypes().contains(getObservationTypeFromFirstObservation()));
		
		assertTrue("offering -> observation type relation NOT in cache",
				controller.getObservationTypes4Offering(getFirstOffering()).contains(getObservationTypeFromFirstObservation()));
	}

	@Test public void 
	should_contain_observation_id_after_InsertObservation()
			throws OwsExceptionReport{
		updateCacheWithSingleObservation();
		
		assertTrue("observation id NOT in cache",
				controller.getObservationIdentifiers().contains( OBSERVATION_ID ));
	}

	@Test public void 
	should_contain_observation_id_to_offering_relation_after_InsertObservation()
			throws OwsExceptionReport {
		updateCacheWithInsertSensor(PROCEDURE);
		updateCacheWithSingleObservation();
		
		assertTrue("procedure -> observation-identifier relation NOT in cache", 
				controller.getCache().getKProcedureVObservationIdentifiers()
				.containsKey(PROCEDURE)
				&&
				controller.getCache().getKProcedureVObservationIdentifiers()
				.get(PROCEDURE)
				.contains(OBSERVATION_ID));
	}
	
	/* Update after InsertSensor */
	
	@Test (expected=IllegalArgumentException.class) public void 
	should_throw_IllegalArgumentException_if_called_with_one_or_more_null_parameters_after_InsertSensor()
			throws OwsExceptionReport{
		controller.updateAfterSensorInsertion(null, null);
		insertSensorRequestExample(PROCEDURE);
		controller.updateAfterSensorInsertion((InsertSensorRequest) request, null);
		request = null;
		insertSensorResponseExample(PROCEDURE);
		controller.updateAfterSensorInsertion(null, (InsertSensorResponse) response);
		
	}
	
	@Test public void 
	should_contain_procedure_after_InsertSensor()
		throws OwsExceptionReport{
		
		updateCacheWithInsertSensor(PROCEDURE);
		
		assertTrue("procedure NOT in cache",
				controller.getProcedures().contains(getSensorIdFromInsertSensorRequest()));
	}
	
	@Test public void 
	should_contain_procedure_offering_relations_after_InsertSensor()
			throws OwsExceptionReport{
		updateCacheWithInsertSensor(PROCEDURE);
		
		assertTrue("offering -> procedure relation NOT in cache",
				controller.getProcedures4Offering( getAssignedOfferingId() ).contains( getSensorIdFromInsertSensorRequest() ));
		
		assertTrue("procedure -> offering relation NOT in cache",
				controller.getOfferings4Procedure(getSensorIdFromInsertSensorRequest()).contains( getAssignedOfferingId() )  );
	}
	
	@Test public void
	should_contain_observable_property_relations_after_InsertSensor()
			throws OwsExceptionReport {
		updateCacheWithInsertSensor(PROCEDURE);

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
				.getOfferings4ObservableProperty( getObservablePropertyFromInsertSensor() )
				.contains( getAssignedOfferingId() )
				);
		
		assertTrue("offering -> observable property relation NOT in cache",
				controller
				.getPhenomenons4Offering( getAssignedOfferingId() )
				.contains( getObservablePropertyFromInsertSensor() )
				);
		
	}

	@Test public void 
	should_contain_offering_name_after_InsertSensor()
			throws OwsExceptionReport{
		updateCacheWithInsertSensor(PROCEDURE);
		
		assertTrue("offering NOT in cache",
				controller.getOfferings().contains(getAssignedOfferingId()) );
	}
	
	@Test public void 
	should_contain_allowed_observation_types_after_InsertSensor()
			throws OwsExceptionReport{
		updateCacheWithInsertSensor(PROCEDURE);
		
		for (String observationType : ((InsertSensorRequest)request).getMetadata().getObservationTypes()) {
			assertTrue("observation type NOT in cache",
					controller.getAllowedKOfferingVObservationTypes().get(getAssignedOfferingId()).contains(observationType));
		}
	}
	
	@Test public void 
	should_contain_related_features_after_InsertObservation()
			throws OwsExceptionReport{
		updateCacheWithInsertSensor(PROCEDURE);
		
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
	
	/* Update after DeleteSensor */
	
	@Test (expected=IllegalArgumentException.class) public void 
	should_throw_IllegalArgumentException_if_receiving_null_parameter_after_DeleteSensor()
			throws OwsExceptionReport{
		controller.updateAfterSensorDeletion(null);
	}
	
	@Test public void
	should_not_contain_procedure_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertFalse("procedure STILL in cache",
				controller.getProcedures().contains(getProcedureIdentifier())  );
		
	}
	
	@Test public void 
	should_not_contain_procedure_offering_relations_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertFalse("offering -> procedure relation STILL in cache",
				controller.getProcedures4Offering( getProcedureIdentifier()+OFFERING_EXTENSION_FOR_PROCEDURE_NAME ).contains( getProcedureIdentifier() ));
		
		assertFalse("procedure -> offering relation STILL in cache",
				controller.getOfferings4Procedure( getProcedureIdentifier() ).contains( getProcedureIdentifier()+OFFERING_EXTENSION_FOR_PROCEDURE_NAME )  );
	}
	
	@Test public void 
	should_not_contain_observable_properties_relations_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertTrue("observable property -> procedure relation STILL in cache",
				controller
				.getKObservablePropertyVProcedures()
				.get( OBSERVABLE_PROPERTY ) == null
				|| 
				!controller
				.getKObservablePropertyVProcedures()
				.get( OBSERVABLE_PROPERTY ).contains( getProcedureIdentifier() )
				);

		assertTrue("procedure -> observable property relation STILL in cache",
				controller
				.getProcPhens()
				.get( getProcedureIdentifier() ) == null
				||
				!controller
				.getProcPhens()
				.get( getProcedureIdentifier() )
				.contains( OBSERVABLE_PROPERTY )
				);
		
		assertTrue("observable property -> offering relation STILL in cache",
				controller
				.getOfferings4ObservableProperty( OBSERVABLE_PROPERTY ) == null
				||
				!controller
				.getOfferings4ObservableProperty( OBSERVABLE_PROPERTY )
				.contains( getProcedureIdentifier() )
				);

		assertTrue("offering -> observable property relation STILL in cache",
				controller
				.getPhenomenons4Offering( getProcedureIdentifier() ) == null
				||
				!controller
				.getPhenomenons4Offering( getProcedureIdentifier() )
				.contains( OBSERVABLE_PROPERTY )
				);
	}
	
	@Test public void 
	should_not_contain_parent_procedures_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertTrue("parent procedures STILL available in cache",
				controller.getParentProcedures(PROCEDURE, true, false) == null
				||
				controller.getParentProcedures(PROCEDURE, true, false).isEmpty());
	}
	
	@Test public void 
	should_not_contain_child_procedures_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertTrue("parent procedures STILL available in cache",
				controller.getChildProcedures(PROCEDURE, true, false) == null
				||
				controller.getChildProcedures(PROCEDURE, true, false).isEmpty());
	}
	
	@Test public void 
	should_not_contain_an_envlope_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertTrue("envolpe for offering STILL in cache",
				controller.getEnvelopeForOffering( getProcedureIdentifier()+OFFERING_EXTENSION_FOR_PROCEDURE_NAME ) == null);
	}
	
	@Test public void 
	should_not_contain_global_envelope_if_deleted_sensor_was_last_one_available()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertFalse("global envelope STILL in cache after deletion of last sensor",
				controller.getGlobalEnvelope() != null
				&&
				controller.getGlobalEnvelope().isSetEnvelope());
	}
	
	@Test public void 
	should_not_contain_temporal_bounding_box_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertTrue("temporal bounding box STILL in cache",
				controller.getMaxTimeForOffering( getProcedureIdentifier()+OFFERING_EXTENSION_FOR_PROCEDURE_NAME ) == null
				&&
				controller.getMinTimeForOffering( getProcedureIdentifier()+OFFERING_EXTENSION_FOR_PROCEDURE_NAME ) == null);
	}	
	
	@Test public void 
	should_not_contain_global_temporal_bouding_box_if_deleted_sensor_was_last_one_available()
			throws OwsExceptionReport {
		updateCacheWithInsertSensor(PROCEDURE_2);
		deleteSensorPreparation();
		
		assertTrue("global temporal bounding box still in cache after deletion of last sensor",
				controller.getMaxEventTime() == null
				&&
				controller.getMinEventTime() == null);
	}
	
	@Test public void 
	should_not_contain_related_features_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertFalse("related features for offering STILL in cache",
				controller.getKOfferingVRelatedFeatures().containsKey(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
	}
	
	@Test public void 
	should_not_contain_roles_for_deleted_related_features_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertTrue("roles for deleted related features are STILL in cache",
				controller.getKRelatedFeaturesVRole().isEmpty() 
				||
				onlyValidRelatedFeaturesAreInRoleMap());
	}
	
	@Test public void 
	should_not_contain_offering_names_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertNull("offering name STILL in cache",
				controller.getOfferingName(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
	}
	
	@Test public void 
	should_not_contain_composite_phenomenons_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertTrue("composite phenomenons STILL in cache for deleted sensor",
				controller.getKOfferingVCompositePhenomenons().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME) == null
				||
				controller.getKOfferingVCompositePhenomenons().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).isEmpty());
	}
	
	@Test public void 
	should_not_contain_features_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertTrue("features STILL related to offering",
				controller.getKOfferingVFeatures() == null
				||
				controller.getKOfferingVFeatures().isEmpty());
		
		assertFalse("features STILL in cache",
				controller.getFeatureOfInterest().contains(FEATURE));
	}
	
	@Test public void 
	should_not_contain_observation_types_for_the_offerings_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertFalse("observation types for offering STILL in cache",
				controller.getKOfferingVObservationTypes().containsKey(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
	}
	
	@Test public void 
	should_not_contain_feature_to_procedure_relations_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertFalse("feature -> procedure relation STILL in cache",
				controller.getProc4FOI(FEATURE).contains(PROCEDURE));
		
		assertTrue("feature -> procedure map is NOT emtpy", 
				controller.getCache().getKFeatureOfInterestVProcedures().isEmpty());
	}
	
	@Test public void 
	should_not_contain_procedure_to_observation_ids_relations_after_DeleteSensor()
			throws OwsExceptionReport {
		deleteSensorPreparation();
		
		assertFalse("procedure -> observation ids relations STILL in cache",
				controller.getCache().getKProcedureVObservationIdentifiers().containsKey(PROCEDURE));
	}
	
	@Test public void 
	should_not_contain_removed_observation_ids_after_DeleteSensor()
	throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertTrue("observation identifiers STILL in cache", 
				controller.getObservationIdentifiers().isEmpty());
	}
	
	@Test public void 
	should_not_contain_offering_observable_property_relations_after_DeleteSensor()
			throws OwsExceptionReport{
		deleteSensorPreparation();
		
		assertFalse("offering to observable property relation STILL in cache",
				controller.getCache().getKOfferingVObservableProperties().containsKey(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
		
		assertFalse("observable property to offering relation STILL in cache",
				controller.getCache().getKObservablePropertyVOffering().containsKey(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
	}
	
	@Test public void 
	should_not_contain_related_result_templates_after_DeleteSenosr()
			throws OwsExceptionReport{
		updateCacheWithInsertResultTemplate(RESULT_TEMPLATE_IDENTIFIER);
		deleteSensorPreparation();
		
		assertTrue("offering -> result templates relations STILL in cache",
				controller.getCache().getKOfferingVResultTemplates().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME) == null
				|| 
				controller.getCache().getKOfferingVResultTemplates().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).isEmpty());
		
		assertFalse("result template identifier STILL in cache",
				controller.getResultTemplates().contains(RESULT_TEMPLATE_IDENTIFIER));
	}
	
	/* Update after InsertResultTemplate */
	
	@Test public void 
	should_contain_resulttemplate_identifier_after_InsertResultTemplate()
			throws OwsExceptionReport{
		insertResultTemplatePreparation();
		
		assertTrue("result template identifier NOT in cache",
				controller.getCache().getResultTemplates().contains(RESULT_TEMPLATE_IDENTIFIER));
	}
	
	@Test public void 
	should_contain_result_template_to_offering_relation_after_InsertResultTemplate()
			throws OwsExceptionReport {
		insertResultTemplatePreparation();
		
		assertTrue("offering -> result template relation NOT in cache",
				controller.getCache().getKOfferingVResultTemplates().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME) != null
				&&
				controller.getCache().getKOfferingVResultTemplates().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).contains(RESULT_TEMPLATE_IDENTIFIER));
	}
	
	/* Update after InsertResult */
	
	@Test(expected=IllegalArgumentException.class) public void
	should_throw_IllegalArgumentException_when_receiving_null_parameter_after_InsertResult() 
			throws OwsExceptionReport {
		controller.updateAfterResultInsertion(null);
	}
	
	@Test public void
	should_update_global_temporal_BoundingBox_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertEquals("maxtime",
				controller.getMaxEventTime(),
				((TimePeriod)observation.getPhenomenonTime()).getEnd()
				);
		
		assertEquals("mintime",
				controller.getMinEventTime(),
				((TimePeriod)observation.getPhenomenonTime()).getStart()				
				);
	}
	
	@Test public void
	should_contain_procedure_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertTrue("procedure NOT in cache",
				controller.getProcedures().contains(PROCEDURE));
		
		assertTrue("offering -> procedure relation not in cache",
				controller.getProcedures4Offering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).contains(PROCEDURE));
		
		assertTrue("procedure -> offering relation NOT in cache",
				controller.getOfferings4Procedure(PROCEDURE).contains(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
		
	}

	@Test public void 
	should_contain_FeatureOfInterest_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertTrue("feature NOT in cache",
				controller.getFeatureOfInterest().contains( FEATURE ));
		
		assertTrue("feature -> procedure relation NOT in cache",
				controller.getProcedures4FeatureOfInterest( FEATURE ).contains(PROCEDURE));
		
		assertTrue("no parent features for feature",
				controller.getParentFeatures(Collections.singletonList( FEATURE ), true, false).isEmpty());
		
		assertTrue("no child features for feature",
				controller.getParentFeatures(Collections.singletonList( FEATURE ), true, false).isEmpty());
		
		assertTrue("offering -> feature relation",
				controller.getKOfferingVFeatures().get(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).contains( FEATURE ));
		
	}
	
	@Test public void 
	should_contain_feature_type_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertTrue("feature type of observation is NOT in cache",
				controller.getFeatureOfInterestTypes().contains(
						FT_SAMPLINGPOINT));
	}
	
	@Test public void 
	should_contain_envelopes_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertEquals("global envelope",
				controller.getGlobalEnvelope(),
				getSosEnvelopeFromObservation(observation));
		
		assertEquals("offering envelop",
				controller.getEnvelopeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME),
				getSosEnvelopeFromObservation(observation));
		
		assertTrue("spatial bounding box of offering NOT contained in cache",
				controller.getEnvelopeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).isSetEnvelope());
		
		assertEquals("spatial bounding box of offering NOT same as feature envelope",
				controller.getEnvelopeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME),
				getSosEnvelopeFromObservation(observation));
	}

	@Test public void
	should_contain_observation_timestamp_in_temporal_envelope_of_offering_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertTrue("temporal envelope of does NOT contain observation timestamp",
			(
				controller.getMinTimeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
				.isBefore( ((TimePeriod)observation.getPhenomenonTime()).getStart() )
				||
				controller.getMinTimeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
				.isEqual( ((TimePeriod)observation.getPhenomenonTime()).getStart() )
			)
			&&
			(
				controller.getMaxTimeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
				.isAfter( ((TimePeriod)observation.getPhenomenonTime()).getEnd() )
				|| 
				controller.getMaxTimeForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
				.isEqual( ((TimePeriod)observation.getPhenomenonTime()).getEnd() )
			)
		);
	}

	@Test public void
	should_contain_observable_property_after_InsertResult()
		throws OwsExceptionReport {
		insertResultPreparation();
		
		assertTrue("offering -> observable property NOT in cache",
				controller.getObservablePropertiesForOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).contains( OBSERVABLE_PROPERTY ));
		
		assertTrue("observable property -> offering NOT in cache",
				controller.getOfferings4ObservableProperty( OBSERVABLE_PROPERTY ).contains(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME));
				
		assertTrue("observable-property -> procedure relation NOT in cache",
				controller.getKObservablePropertyVProcedures().get( OBSERVABLE_PROPERTY ).contains(PROCEDURE));
		
		assertTrue("procedure -> observable-property relation NOT in cache",
				controller.getKProcedureVObservableProperties().get(PROCEDURE).contains( OBSERVABLE_PROPERTY ));
	}

	@Test public void
	should_contain_offering_observation_type_relation_after_InsertResult()
			throws OwsExceptionReport {
		insertResultPreparation();
		
		assertTrue("observation type NOT in cache",
				controller.getObservationTypes().contains(OBS_TYPE_SWE_ARRAY_OBSERVATION));
		
		assertTrue("offering -> observation type relation NOT in cache",
				controller.getObservationTypes4Offering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME).contains(OBS_TYPE_SWE_ARRAY_OBSERVATION));
	}
	
	@Test public void 
	should_contain_observation_id_after_InsertResult()
			throws OwsExceptionReport{
		insertResultPreparation();
		
		assertTrue("observation id NOT in cache",
				controller.getObservationIdentifiers().contains( OBSERVATION_ID ));
	}
	
	@Test public void 
	should_contain_observation_id_to_offering_relation_after_InsertResult()
			throws OwsExceptionReport {
		updateCacheWithInsertSensor(PROCEDURE);
		insertResultPreparation();
		
		assertTrue("procedure -> observation-identifier relation NOT in cache", 
				controller.getCache().getKProcedureVObservationIdentifiers()
				.containsKey(PROCEDURE)
				&&
				controller.getCache().getKProcedureVObservationIdentifiers()
				.get(PROCEDURE)
				.contains( OBSERVATION_ID ));
	}
	
	/* HELPER */

	private void 
	insertResultTemplatePreparation()
			throws OwsExceptionReport {
		updateCacheWithInsertSensor(PROCEDURE);
		updateCacheWithInsertResultTemplate(RESULT_TEMPLATE_IDENTIFIER);
	}

	private void 
	updateCacheWithInsertResultTemplate(String resultTemplateIdentifier)
			 throws OwsExceptionReport {
		insertResultTemplateResponse(resultTemplateIdentifier);
		insertResultTemplateRequest(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME);
		controller.updateAfterResultTemplateInsertion((InsertResultTemplateRequest) request,(InsertResultTemplateResponse) response);
	}
	
	private void 
	insertResultTemplateRequest(String offeringForResultTemplate)
	{
		request = anInsertResultTemplateRequest()
				.setOffering(offeringForResultTemplate)
				.build();
	}

	private	void 
	insertResultPreparation()
	{
		observation = anObservation()
				.setObservationConstellation(
						anObservationConstellation()
							.setProcedure(aSensorMLProcedureDescription()
									.setIdentifier(PROCEDURE)
									.build())
							.addOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
							.setFeature(aSamplingFeature()
									.setIdentifier(FEATURE)
									.setFeatureType(FT_SAMPLINGPOINT)
									.setGeometry(52.0,7.5,4326)
									.build())
							.setObservableProperty(aObservableProperty()
									.setIdentifier(OBSERVABLE_PROPERTY)
									.build())
							.setObservationType(OBS_TYPE_SWE_ARRAY_OBSERVATION)
							.build())
				.setValue(
						aSweDataArrayValue()
							.setSweDataArray(
									aSweDataArray()
										.setElementType(
											aDataRecord()
												.addField(
														aSweTime()
															.build())
											.build())
										.setEncoding("text","@",";",".")
										.addBlock("2013-02-06T10:28:00","2.5")
										.build())
							.build())
				.setIdentifier(CODESPACE, OBSERVATION_ID)
				.build();
		
		controller.updateAfterResultInsertion(observation);
	}
	
	private void 
	insertResultTemplateResponse(String resultTemplateIdentifier)
	{
		response = anInsertResultTemplateResponse()
				.setTemplateIdentifier(resultTemplateIdentifier)
				.build();
	}

	private 
	boolean onlyValidRelatedFeaturesAreInRoleMap()
	{
		List<String> allowedRelatedFeatures = getAllowedRelatedFeatures();
		for (String relatedFeatureWithRole : controller.getCache().getKRelatedFeatureVRole().keySet())
		{
			if (!allowedRelatedFeatures.contains(relatedFeatureWithRole))
			{
				return false;
			}
		}
		return true;
	}

	private 
	List<String> getAllowedRelatedFeatures()
	{
		List<String> allowedEntries = new ArrayList<String>();
		for (Collection<String> entries : controller.getCache().getKOfferingVRelatedFeatures().values())
		{
			for (String entry : entries)
			{
				if (!allowedEntries.contains(entry))
				{
					allowedEntries.add(entry);
				}
			}
		}
		return allowedEntries;
	}

	private String 
	getProcedureIdentifier()
	{
		return ((DeleteSensorRequest)request).getProcedureIdentifier();
	}

	private void 
	deleteSensorPreparation()
			throws OwsExceptionReport{
		updateCacheWithInsertSensor(PROCEDURE);
		updateCacheWithSingleObservation();
		updateCacheWithDeleteSensor();
	}
	
	private void 
	updateCacheWithDeleteSensor()
			throws OwsExceptionReport {
		request = DeleteSensorRequestBuilder.aDeleteSensorRequest()
				.setProcedure(PROCEDURE)
				.build();
		controller.updateAfterSensorDeletion((DeleteSensorRequest) request);
	}

	private void 
	updateCacheWithSingleObservation()
			throws OwsExceptionReport {
		insertObservationRequestExample();
		controller.updateAfterObservationInsertion((InsertObservationRequest) request);
	}
	
	private void 
	updateCacheWithInsertSensor(String procedureIdentifier) 
			throws OwsExceptionReport {
		insertSensorRequestExample(procedureIdentifier);
		insertSensorResponseExample(procedureIdentifier);
		controller.updateAfterSensorInsertion((InsertSensorRequest)request,(InsertSensorResponse)response);
	}

	private 
	DateTime getPhenomenonTimeFromObservation()
	{
		return ((TimeInstant)((InsertObservationRequest) request).getObservations().get(0).getPhenomenonTime()).getValue();
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
	insertSensorResponseExample(String procedureIdentifier)
	{
		response = anInsertSensorResponse()
				.setOffering(procedureIdentifier + OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
				.setProcedure(procedureIdentifier)
				.build();
	}

	private void
	insertSensorRequestExample(String procedureIdentifier)
	{
		request = anInsertSensorRequest()
				.setProcedure(aSensorMLProcedureDescription()
						.setIdentifier(procedureIdentifier)
						.setOffering(procedureIdentifier+OFFERING_EXTENSION_FOR_PROCEDURE_NAME,OFFERING_NAME)
						.build())
				.addObservableProperty(OBSERVABLE_PROPERTY)
				.addObservationType(OBSERVATION_TYPE)
				.addObservationType(OBSERVATION_TYPE_2)
				.addRelatedFeature(aSamplingFeature()
						.setIdentifier(FEATURE)
						.build(),
						RELATED_FEATURE_ROLE)
				.addRelatedFeature(aSamplingFeature()
						.setIdentifier(FEATURE_2)
						.build(),
						RELATED_FEATURE_ROLE_2)
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
	SosEnvelope getSosEnvelopeFromObservation(SosObservation sosObservation)
	{
		return new SosEnvelope(
				((SosSamplingFeature)sosObservation.getObservationConstellation().getFeatureOfInterest()).getGeometry().getEnvelopeInternal(),
				controller.getCache().getDatabaseEPSGCode());
	}
	
	private
	String getFoiIdFromInsertObservationRequest()
	{
		return ((InsertObservationRequest) request).getObservations().get(0).getObservationConstellation().getFeatureOfInterest().getIdentifier().getValue();
	}

	private void
	insertObservationRequestExample()
	{
		request = aInsertObservationRequest()
				.setProcedureId(PROCEDURE)
				.addOffering(PROCEDURE+OFFERING_EXTENSION_FOR_PROCEDURE_NAME)
				.addObservation(anObservation()
					.setObservationConstellation(anObservationConstellation()
						.setFeature(aSamplingFeature()
							.setIdentifier(FEATURE)
							.setFeatureType(FT_SAMPLINGPOINT)
							.setGeometry(52.0,7.5,4326)
							.build())
						.setProcedure(aSensorMLProcedureDescription()
							.setIdentifier(PROCEDURE)
							.build())
						.setObservationType(OBS_TYPE_MEASUREMENT)
						.setObservableProperty(aObservableProperty()
							.setIdentifier(OBSERVABLE_PROPERTY)
							.build())
						.build())
					.setValue(aQuantityValue()
							.setValue(
								aQuantitiy()
									.setValue(new BigDecimal(2.0))
									.setUnit("m")
									.build())
							.setPhenomenonTime(System.currentTimeMillis())
							.build()
							)
					.setIdentifier(CODESPACE,OBSERVATION_ID)
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
		
		protected ICacheFeederDAO getCacheDAO() {
			return null;
		}
	}
}
