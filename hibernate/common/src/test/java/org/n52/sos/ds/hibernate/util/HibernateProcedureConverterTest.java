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
package org.n52.sos.ds.hibernate.util;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.OrderingComparison.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.n52.sos.ogc.OGCConstants.*;
import static org.n52.sos.ogc.sensorML.SensorMLConstants.*;
import static org.n52.sos.ogc.sensorML.elements.SosSMLClassifier.*;
import static org.n52.sos.util.StringHelper.join;

import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.ProcessMethod;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.RulesDefinition;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.service.ProcedureDescriptionSettings;
import org.n52.sos.service.ServiceConfiguration;


/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0.0
 */
public class HibernateProcedureConverterTest {

	private static final ProcedureDescriptionFormat PROCEDURE_DESCRIPTION_FORMAT = new ProcedureDescriptionFormat()
	.setProcedureDescriptionFormat(SensorMLConstants.NS_SML);

	private static final String PROCEDURE_IDENTIFIER = "test-procedure-identifier";

	private static String PROCEDURE_DESCRIPTION_NON_SPATIAL;
	
	private static final String[] OBSERVABLE_PROPERTIES = {"test-obserable-property-1","test-obserable-property-2"};

	private static HibernateProcedureConverter converter;
	
	private static Procedure spatialProcedure;
	
	private static Procedure nonSpatialProc;

	private static String PROCEDURE_DESCRIPTION_SPATIAL;
	
	@BeforeClass
	public static void initFixtures()
	{
		// init settings
		ProcedureDescriptionSettings.getInstance();
		
		spatialProcedure = (Procedure)(new Procedure()
		.setProcedureDescriptionFormat(PROCEDURE_DESCRIPTION_FORMAT)
		.setIdentifier(PROCEDURE_IDENTIFIER)
		.setSrid(4326)
		.setAltitude(42.0)
		.setLongitude(7.2)
		.setLatitude(52.0));
		
		nonSpatialProc = (Procedure)(new Procedure()
		.setProcedureDescriptionFormat(PROCEDURE_DESCRIPTION_FORMAT)
		.setIdentifier(PROCEDURE_IDENTIFIER));
		
		PROCEDURE_DESCRIPTION_NON_SPATIAL = format(
				ProcedureDescriptionSettings.getInstance().getDescriptionTemplate(),
				"procedure",
				PROCEDURE_IDENTIFIER,
				join(",", (Object[])OBSERVABLE_PROPERTIES));
		
		PROCEDURE_DESCRIPTION_SPATIAL = format(
				ProcedureDescriptionSettings.getInstance().getDescriptionTemplate(),
				"sensor system",
				PROCEDURE_IDENTIFIER,
				join(",", (Object[])OBSERVABLE_PROPERTIES));
	}
	
	@BeforeClass
	public static void initConverterMockup() throws OwsExceptionReport
	{
		converter = mock(HibernateProcedureConverter.class);
		when(converter.getObservablePropertiesForProcedure(anyString())).thenReturn(OBSERVABLE_PROPERTIES);
		
		when(converter.getServiceProvider()).thenReturn(mock(SosServiceProvider.class));
		
		when(converter.getExampleObservation(anyString(), anyString())).thenReturn(new NumericObservation());
		
		when(converter.getServiceConfig()).thenReturn(mock(ServiceConfiguration.class));
		
		when(converter.createSosProcedureDescription(any(Procedure.class), anyString(), anyString())).thenCallRealMethod();
	}
	
	@Test(expected=NoApplicableCodeException.class) public void 
	should_throw_exception_with_null_parameters()
			throws OwsExceptionReport {
		converter.createSosProcedureDescription(null, null, null);
	}
	
	@Test public void 
	should_return_sml_system_for_spatial_procedure()
			throws OwsExceptionReport {
		final SosProcedureDescription description = converter.createSosProcedureDescription(
				spatialProcedure, 
				PROCEDURE_IDENTIFIER,
				NS_SML);
		assertThat(description, is(instanceOf(SensorML.class)));
		final SensorML smlDesc = (SensorML)description; 
		assertThat(smlDesc.getMembers().get(0), instanceOf(System.class));
	}
	
	@Test public void
	should_return_sml_process_model_for_smlProcessModel()
			throws OwsExceptionReport {
		final SosProcedureDescription description = converter.createSosProcedureDescription(
				nonSpatialProc,
				PROCEDURE_IDENTIFIER,
				NS_SML);
		assertThat(description, is(instanceOf(SensorML.class)));
		final SensorML smlDesc = (SensorML)description; 
		assertThat(smlDesc.getMembers().get(0), instanceOf(ProcessModel.class));
	}

	@Test public void 
	should_set_description_for_smlProcessModel()
			throws OwsExceptionReport {
		final AbstractProcess process = setupAbstractProcess();
		assertThat(process.getDescriptions().size(), is(1));
		assertThat(process.getDescriptions().get(0), is(PROCEDURE_DESCRIPTION_NON_SPATIAL));
	}
	
	@Test public void
	should_set_name_for_smlProcessModel()
	throws OwsExceptionReport {
		final AbstractProcess process = setupAbstractProcess();
		assertThat(process.getNames().size(), is(1));
		assertThat(process.getNames().get(0).getValue(), is(PROCEDURE_IDENTIFIER));
	}
	
	@Test public void
	should_set_method_for_smlProcessModel()
	throws OwsExceptionReport {
		final ProcessModel pModel = setupProcessModel();
		assertThat(pModel.getMethod(), instanceOf(ProcessMethod.class));
	}
	
	@Test public void
	should_set_identifier_for_smlProcessModel()
	throws OwsExceptionReport {
		final ProcessModel pModel = setupProcessModel();
		assertThat(pModel.getIdentifier(), is(PROCEDURE_IDENTIFIER));
		assertThat(uniqueIdIdentifierOf(pModel), is(PROCEDURE_IDENTIFIER));
	}

	@Test public void
	should_set_longname_and_shortname_identifier_for_smlProcessModel()
	throws OwsExceptionReport {
		final ProcessModel pModel = setupProcessModel();
		assertThat(pModel.getIdentifications().size(), is(greaterThanOrEqualTo(2)));
		assertThat(longNameIdentifierOf(pModel), is(PROCEDURE_IDENTIFIER));
		assertThat(shortNameIdentifierOf(pModel), is(PROCEDURE_IDENTIFIER));
	}
	
	@Test public void
	should_set_outputs_for_smlProcessModel()
	throws OwsExceptionReport {
		final ProcessModel pModel = setupProcessModel();
		assertThat(pModel.getOutputs().size(), is(greaterThanOrEqualTo(2)));
		assertThat(outputDefinition(0,pModel), is(OBSERVABLE_PROPERTIES[0]));
		assertThat(outputDefinition(1,pModel), is(OBSERVABLE_PROPERTIES[1]));
	}
	
	@Test public void
	should_set_processMethod_description_for_smlProcessModel()
	throws OwsExceptionReport {
		final ProcessModel pModel = setupProcessModel();
		assertThat(pModel.getMethod().getRulesDefinition(), instanceOf(RulesDefinition.class));
		assertThat(pModel.getMethod().getRulesDefinition().getDescription(), instanceOf(String.class)); // FIXME how to use assertThat with boolean values
		assertThat(pModel.getMethod().getRulesDefinition().getDescription().length(), is(greaterThan(0)));
	}
	
	@Test public void 
	should_set_classifiers_for_smlProcessModel()
	throws OwsExceptionReport {
		// local fixtures
		final String intendedApplicationValue = "test-intended-application-value";
		final String intendedApplicationDefinition = "test-intended-application-definition";
		final String procedureTypeValue = "test-sensor-type-value";
		final String procedureTypeDefinition = "test-sensor-type-definition";
		final ProcedureDescriptionSettings sdgs = ProcedureDescriptionSettings.getInstance();
		
		sdgs.setClassifierIntendedApplicationValue(intendedApplicationValue);
		sdgs.setClassifierIntendedApplicationDefinition(intendedApplicationDefinition);
		sdgs.setClassifierProcedureTypeValue(procedureTypeValue);
		sdgs.setClassifierProcedureTypeDefinition(procedureTypeDefinition);
		
		final ProcessModel pModel = setupProcessModel();
		
		assertThat(pModel.getClassifications().size(), is(2));
		
		for (final SosSMLClassifier classifier : pModel.getClassifications()) 
		{
			if (classifier.getName().equalsIgnoreCase(INTENDED_APPLICATION))
			{
				assertThat(classifier.getDefinition(), is(intendedApplicationDefinition));
				assertThat(classifier.getValue(), is(intendedApplicationValue));
			}
			else if (classifier.getName().equalsIgnoreCase(PROCEDURE_TYPE))
			{
				assertThat(classifier.getDefinition(), is(procedureTypeDefinition));
				assertThat(classifier.getValue(), is(procedureTypeValue));
			}
		}
	}
	
	private String outputDefinition(final int i,
			final ProcessModel pModel)
	{
		return pModel.getOutputs().get(i).getIoValue().getDefinition();
	}

	private String shortNameIdentifierOf(final ProcessModel pModel)
	{
		return getIdentifier(pModel, ELEMENT_NAME_SHORT_NAME, "urn:ogc:def:identifier:OGC:1.0:shortname");
	}

	private String longNameIdentifierOf(final ProcessModel pModel)
	{
		return getIdentifier(pModel, ELEMENT_NAME_LONG_NAME, "urn:ogc:def:identifier:OGC:1.0:longname");
	}

	private String uniqueIdIdentifierOf(final ProcessModel pModel)
	{
		return getIdentifier(pModel,URN_UNIQUE_IDENTIFIER_END,URN_UNIQUE_IDENTIFIER);
	}
	
	private String getIdentifier(final ProcessModel pModel, final String idName, final String idDefinition)
	{
		for (final SosSMLIdentifier identifier : pModel.getIdentifications()) 
		{
			if (identifier.getName().equalsIgnoreCase(idName) && 
					identifier.getDefinition().equalsIgnoreCase(idDefinition))
			{
				return identifier.getValue();
			}
		}
		return null;
	}

	protected ProcessModel setupProcessModel() throws OwsExceptionReport
	{
		final AbstractProcess process = setupAbstractProcess();
		assertThat(process, instanceOf(ProcessModel.class));
		final ProcessModel pModel = (ProcessModel) process;
		return pModel;
	}
	
	protected AbstractProcess setupAbstractProcess() throws OwsExceptionReport
	{
		final SensorML description = (SensorML) converter.createSosProcedureDescription(nonSpatialProc, PROCEDURE_IDENTIFIER, NS_SML);
		assertThat(description.getMembers().size(), is(1));
		final AbstractProcess process = description.getMembers().get(0);
		return process;
	}
	
}
