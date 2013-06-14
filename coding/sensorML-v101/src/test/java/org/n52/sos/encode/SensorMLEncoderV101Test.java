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
package org.n52.sos.encode;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.SimpleDataRecordType;

import org.apache.xmlbeans.XmlObject;
import org.junit.AfterClass;
import org.junit.Test;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SmlIdentifier;
import org.n52.sos.ogc.sensorML.elements.SmlIo;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.util.CodingHelper;

/**
 * @author Shane StClair
 */
public class SensorMLEncoderV101Test {
	private static final String TEST_ID_1 = "test-id-1";
	private static final String TEST_NAME_1 = "test-name-1";
	private static final String TEST_ID_2 = "test-id-2";
	private static final String TEST_NAME_2 = "test-name-2";
	
	@AfterClass
	public static void cleanUp(){
		SettingsManager.getInstance().cleanup();
	}

	@Test
	public void should_set_identifier() throws OwsExceptionReport
	{
		SensorML sensorMl = new SensorML();
		System system = new System();
		sensorMl.addMember(system);		
		system.addIdentifier(new SmlIdentifier(TEST_NAME_1, OGCConstants.URN_UNIQUE_IDENTIFIER, TEST_ID_1));
		SystemType xbSystem = encodeSystem(sensorMl);
		assertThat(xbSystem.getIdentificationArray().length, is(1));
		IdentifierList xbIdentifierList = xbSystem.getIdentificationArray()[0].getIdentifierList();
		assertThat(xbIdentifierList.sizeOfIdentifierArray(), is(1));
		Identifier xbIdentifier = xbIdentifierList.getIdentifierArray(0);
		assertThat(xbIdentifier.getName(), is(TEST_NAME_1));
		assertThat(xbIdentifier.getTerm().getDefinition(), is(OGCConstants.URN_UNIQUE_IDENTIFIER));
		assertThat(xbIdentifier.getTerm().getValue(), is(TEST_ID_1));
	}

	private SystemType encodeSystem(SensorML sensorMl) throws OwsExceptionReport {
		XmlObject encodedSml = CodingHelper.encodeObjectToXml(SensorMLConstants.NS_SML, sensorMl);
		assertThat(encodedSml, instanceOf(SensorMLDocument.class));
		net.opengis.sensorML.x101.SensorMLDocument.SensorML xbSml = ((SensorMLDocument) encodedSml).getSensorML();
		assertThat(xbSml.getMemberArray().length, is(1));
		assertThat(xbSml.getMemberArray()[0].getProcess(), instanceOf(SystemType.class));
		return (SystemType) xbSml.getMemberArray()[0].getProcess();
	}

	private SimpleDataRecordType encodeSimpleDataRecord(SensorML sensorMl, String capName, int fields)
			throws OwsExceptionReport {
		return encodeSimpleDataRecord(encodeSystem(sensorMl), capName, fields);
	}
	
	private SimpleDataRecordType encodeSimpleDataRecord(SystemType xbSystem, String capName, int fields) {
		assertThat(xbSystem.getCapabilitiesArray().length, is(1));
		Capabilities xbCapabilities = xbSystem.getCapabilitiesArray()[0];
		assertThat(xbCapabilities.getName(), is(capName));
		assertThat(xbCapabilities.getAbstractDataRecord(), notNullValue());
		assertThat(xbCapabilities.getAbstractDataRecord(), instanceOf(SimpleDataRecordType.class));
		SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) xbCapabilities.getAbstractDataRecord();
		assertThat(xbSimpleDataRecord.getFieldArray().length, is(fields));
		return xbSimpleDataRecord;
	}

	private void validateField(AnyScalarPropertyType field, String name, String definition, String value) {
		assertThat(field.getName(), is(name));
		assertThat(field.isSetText(), is(true));
		assertThat(field.getText().getDefinition(), is(definition));
		assertThat(field.getText().getValue(), is(value));
	}
	
	@Test
	public void should_encode_features_of_interest() throws OwsExceptionReport
	{
		SensorML sensorMl = new SensorML();
		System system = new System();
		sensorMl.addMember(system);		
		system.addFeatureOfInterest(TEST_ID_1);
		system.addFeatureOfInterest(TEST_ID_2);
		SimpleDataRecordType xbSimpleDataRecord = encodeSimpleDataRecord(sensorMl,
				SensorMLConstants.ELEMENT_NAME_FEATURES_OF_INTEREST, 2);
		validateField(xbSimpleDataRecord.getFieldArray()[0],
				SensorMLConstants.FEATURE_OF_INTEREST_FIELD_NAME + 1,
				SensorMLConstants.FEATURE_OF_INTEREST_FIELD_DEFINITION, TEST_ID_1);
		validateField(xbSimpleDataRecord.getFieldArray()[1],
				SensorMLConstants.FEATURE_OF_INTEREST_FIELD_NAME + 2,
				SensorMLConstants.FEATURE_OF_INTEREST_FIELD_DEFINITION, TEST_ID_2);		
	}

	@Test
	public void should_encode_offerings() throws OwsExceptionReport
	{
		SensorML sensorMl = new SensorML();
		System system = new System();
		sensorMl.addMember(system);		
		system.addOffering(new SosOffering(TEST_ID_1,TEST_NAME_1));
		system.addOffering(new SosOffering(TEST_ID_2,TEST_NAME_2));
		SimpleDataRecordType xbSimpleDataRecord = encodeSimpleDataRecord(sensorMl,
				SensorMLConstants.ELEMENT_NAME_OFFERINGS, 2);
		validateField(xbSimpleDataRecord.getFieldArray()[0], TEST_NAME_1,
				SensorMLConstants.OFFERING_FIELD_DEFINITION, TEST_ID_1);
		validateField(xbSimpleDataRecord.getFieldArray()[1], TEST_NAME_2,
				SensorMLConstants.OFFERING_FIELD_DEFINITION, TEST_ID_2);
	}

	@Test
	public void should_encode_parent_procedures() throws OwsExceptionReport
	{
		SensorML sensorMl = new SensorML();
		System system = new System();
		sensorMl.addMember(system);		
		system.addParentProcedure(TEST_ID_1);
		system.addParentProcedure(TEST_ID_2);
		SimpleDataRecordType xbSimpleDataRecord = encodeSimpleDataRecord(sensorMl,
				SensorMLConstants.ELEMENT_NAME_PARENT_PROCEDURES, 2);
		validateField(xbSimpleDataRecord.getFieldArray()[0],
				SensorMLConstants.PARENT_PROCEDURE_FIELD_NAME + 1,
				SensorMLConstants.PARENT_PROCEDURE_FIELD_DEFINITION, TEST_ID_1);
		validateField(xbSimpleDataRecord.getFieldArray()[1],
				SensorMLConstants.PARENT_PROCEDURE_FIELD_NAME + 2,
				SensorMLConstants.PARENT_PROCEDURE_FIELD_DEFINITION, TEST_ID_2);
	}

	@Test
	public void should_encode_child_procedures() throws OwsExceptionReport
	{
		SensorML sensorMl = new SensorML();
		System system = new System();
		sensorMl.addMember(system);
		System childProcedure = new System();
		system.addChildProcedure(childProcedure);
		childProcedure.addFeatureOfInterest(TEST_ID_1);
		SystemType xbSystemType = encodeSystem(sensorMl);
		assertThat(xbSystemType.getComponents().getComponentList().sizeOfComponentArray(), is(1));
		Component xbComponent = xbSystemType.getComponents().getComponentList().getComponentArray(0);
		assertThat(xbComponent.getProcess(), instanceOf(SystemType.class));
		SystemType xbComponentSystem = (SystemType) xbComponent.getProcess();
		SimpleDataRecordType xbSimpleDataRecord = encodeSimpleDataRecord(xbComponentSystem,
				SensorMLConstants.ELEMENT_NAME_FEATURES_OF_INTEREST, 1);
		validateField(xbSimpleDataRecord.getFieldArray(0), SensorMLConstants.FEATURE_OF_INTEREST_FIELD_NAME,
				SensorMLConstants.FEATURE_OF_INTEREST_FIELD_DEFINITION, TEST_ID_1);
	}

	
    @Test
    public void should_aggregate_child_outputs() throws OwsExceptionReport
    {
        SweQuantity q1 = new SweQuantity();
        q1.setDefinition("def1");
        q1.setUom("uom1");
        final SmlIo<?> output1 = new SmlIo<SweQuantity>(q1);

        SweQuantity q2 = new SweQuantity();
        q2.setDefinition("def2");
        q2.setUom("uom2");
        final SmlIo<?> output2 = new SmlIo<SweQuantity>(q2);
        
        SweQuantity q3 = new SweQuantity();
        q3.setDefinition("def3");
        q3.setUom("uom3");
        final SmlIo<?> output3 = new SmlIo<SweQuantity>(q3);
        
        SensorML sensorMl = new SensorML();
        sensorMl.setIdentifier("sensorMl");
        System system = new System();
        system.setIdentifier("system");
        sensorMl.addMember(system);
        system.getOutputs().add(output1);
        
        SensorML childSml = new SensorML();
        childSml.setIdentifier("childSml");
        System childSystem = new System();
        childSystem.setIdentifier("childSystem");
        childSml.addMember(childSystem);
        system.addChildProcedure(childSml);
        childSystem.getOutputs().add(output2);
        
        SensorML grandchildSml = new SensorML();
        grandchildSml.setIdentifier("grandchildSml");
        System grandchildSystem = new System();
        grandchildSystem.setIdentifier("grandchildSystem");
        grandchildSml.addMember(grandchildSystem);
        childSystem.addChildProcedure(grandchildSml);
        grandchildSystem.getOutputs().add(output3);
        
        encodeSystem(sensorMl);
                
        assertThat(system.getOutputs(), hasItems(output1, output2, output3));
        assertThat(childSystem.getOutputs(), hasItems(output2, output3));
        assertThat(grandchildSystem.getOutputs(), hasItem(output3));
    }
}