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
package org.n52.sos.decode;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.swe.x101.AbstractDataArrayType;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.BooleanDocument.Boolean;
import net.opengis.swe.x101.DataArrayType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.SimpleDataRecordType;

import org.junit.AfterClass;
import org.junit.Test;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweDataComponentType;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;

/**
 * @author Shane StClair
 */
public class SensorMLDecoderV101Test {
    private static final String TEST_ID_1 = "test-id-1";

    private static final String TEST_NAME_1 = "test-name-1";

    private static final String TEST_ID_2 = "test-id-2";

    private static final String TEST_NAME_2 = "test-name-2";

    @AfterClass
    public static void cleanUp() {
        SettingsManager.getInstance().cleanup();
    }

    @Test
    public void should_set_identifier_by_identifier_name() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        IdentifierList xbIdentifierList = xbSystem.addNewIdentification().addNewIdentifierList();
        addIdentifier(xbIdentifierList, OGCConstants.URN_UNIQUE_IDENTIFIER_END, null, TEST_ID_1);
        addIdentifier(xbIdentifierList, "any name", null, TEST_ID_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getIdentifier(), is(TEST_ID_1));
        assertThat(absProcess.getIdentifications().size(), is(2));
    }

    private SensorMLDocument getSensorMLDoc() {
        SensorMLDocument xbSmlDoc =
                SensorMLDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        net.opengis.sensorML.x101.SensorMLDocument.SensorML xbSensorML = xbSmlDoc.addNewSensorML();
        xbSensorML.setVersion(SensorMLConstants.VERSION_V101);
        return xbSmlDoc;
    }

    @Test
    public void should_set_identifier_by_identifier_definition() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        IdentifierList xbIdentifierList = xbSystem.addNewIdentification().addNewIdentifierList();
        addIdentifier(xbIdentifierList, "any name", OGCConstants.URN_UNIQUE_IDENTIFIER, TEST_ID_1);
        addIdentifier(xbIdentifierList, "any other name", null, TEST_ID_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getIdentifier(), is(TEST_ID_1));
        assertThat(absProcess.getIdentifications().size(), is(2));
    }

    @Test
    public void should_set_identifier_by_identifier_prefix_and_suffix() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        IdentifierList xbIdentifierList = xbSystem.addNewIdentification().addNewIdentifierList();
        String definiton =
                OGCConstants.URN_UNIQUE_IDENTIFIER_START + "anything" + OGCConstants.URN_UNIQUE_IDENTIFIER_END;
        addIdentifier(xbIdentifierList, "any name", definiton, TEST_ID_1);
        addIdentifier(xbIdentifierList, "any other name", null, TEST_ID_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getIdentifier(), is(TEST_ID_1));
        assertThat(absProcess.getIdentifications().size(), is(2));
    }

    private void addIdentifier(IdentifierList xbIdentifierList, String name, String definition, String value) {
        Identifier xbIdentifier = xbIdentifierList.addNewIdentifier();
        xbIdentifier.setName(name);
        Term xbTerm = xbIdentifier.addNewTerm();
        xbTerm.setDefinition(definition);
        xbTerm.setValue(value);
    }

    @Test
    public void should_decode_offerings_from_sml() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        Capabilities xbCapabilities = xbSystem.addNewCapabilities();
        xbCapabilities.setName(SensorMLConstants.ELEMENT_NAME_OFFERINGS);
        SimpleDataRecordType xbSimpleDataRecord =
                (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                        SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
        addCapabilitiesInsertionMetadata(xbSimpleDataRecord, TEST_ID_1, TEST_NAME_1);
        addCapabilitiesInsertionMetadata(xbSimpleDataRecord, TEST_ID_2, TEST_NAME_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getOfferings().size(), is(2));
        assertThat(absProcess.getCapabilities().size(), is(0));
        List<SosOffering> sosOfferings = new ArrayList<SosOffering>(absProcess.getOfferings());
        Collections.sort(sosOfferings);
        assertThat(sosOfferings.get(0).getOfferingIdentifier(), is(TEST_ID_1));
        assertThat(sosOfferings.get(0).getOfferingName(), is(TEST_NAME_1));
        assertThat(sosOfferings.get(1).getOfferingIdentifier(), is(TEST_ID_2));
        assertThat(sosOfferings.get(1).getOfferingName(), is(TEST_NAME_2));
    }

    @Test
    public void should_decode_parent_procedures_from_sml() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        Capabilities xbCapabilities = xbSystem.addNewCapabilities();
        xbCapabilities.setName(SensorMLConstants.ELEMENT_NAME_PARENT_PROCEDURES);
        SimpleDataRecordType xbSimpleDataRecord =
                (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                        SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
        addCapabilitiesInsertionMetadata(xbSimpleDataRecord, TEST_ID_1, TEST_NAME_1);
        addCapabilitiesInsertionMetadata(xbSimpleDataRecord, TEST_ID_2, TEST_NAME_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getParentProcedures().size(), is(2));
        assertThat(absProcess.getCapabilities().size(), is(0));
        List<String> parentProcedures = new ArrayList<String>(absProcess.getParentProcedures());
        Collections.sort(parentProcedures);
        assertThat(parentProcedures.get(0), is(TEST_ID_1));
        assertThat(parentProcedures.get(1), is(TEST_ID_2));
    }

    @Test
    public void should_decode_features_of_interest_from_sml() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        Capabilities xbCapabilities = xbSystem.addNewCapabilities();
        xbCapabilities.setName(SensorMLConstants.ELEMENT_NAME_FEATURES_OF_INTEREST);
        SimpleDataRecordType xbSimpleDataRecord =
                (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                        SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
        addCapabilitiesInsertionMetadata(xbSimpleDataRecord, TEST_ID_1, TEST_NAME_1);
        addCapabilitiesInsertionMetadata(xbSimpleDataRecord, TEST_ID_2, TEST_NAME_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getFeaturesOfInterest().size(), is(2));
        assertThat(absProcess.getCapabilities().size(), is(0));
        List<String> featuresOfInterest = new ArrayList<String>(absProcess.getFeaturesOfInterest());
        Collections.sort(featuresOfInterest);
        assertThat(featuresOfInterest.get(0), is(TEST_ID_1));
        assertThat(featuresOfInterest.get(1), is(TEST_ID_2));
    }

    private void addCapabilitiesInsertionMetadata(SimpleDataRecordType xbSimpleDataRecord, String value, String name) {
        AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
        xbField.setName(name);
        xbField.addNewText().setValue(value);
    }

    private AbstractProcess decodeAbstractProcess(SensorMLDocument xbSmlDoc) throws OwsExceptionReport {
        Object decoded = CodingHelper.decodeXmlObject(xbSmlDoc);
        assertThat(decoded, instanceOf(SensorML.class));
        SensorML sml = (SensorML) decoded;
        assertThat(sml.getMembers().size(), is(1));
        return sml.getMembers().get(0);
    }

    @Test
    public void should_decode_child_procedure_from_sml() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        IdentifierList xbIdentifierList = xbSystem.addNewIdentification().addNewIdentifierList();
        addIdentifier(xbIdentifierList, "anyname", OGCConstants.URN_UNIQUE_IDENTIFIER, TEST_ID_1);

        ComponentList xbComponentList = xbSystem.addNewComponents().addNewComponentList();
        addChildProcedure(xbComponentList, TEST_ID_2);
        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getIdentifier(), is(TEST_ID_1));
        assertThat(absProcess.getChildProcedures().size(), is(1));
        SosProcedureDescription childProcedure = absProcess.getChildProcedures().iterator().next();
        assertThat(childProcedure, instanceOf(System.class));
        assertThat(childProcedure.getIdentifier(), is(TEST_ID_2));
    }

    private void addChildProcedure(ComponentList xbComponentList, String identifier) {
        Component xbComponent = xbComponentList.addNewComponent();
        xbComponent.setName(SensorMLConstants.ELEMENT_NAME_CHILD_PROCEDURES);
        SystemType xbSystem =
                (SystemType) xbComponent.addNewProcess().substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        IdentifierList xbIdentifierList = xbSystem.addNewIdentification().addNewIdentifierList();
        addIdentifier(xbIdentifierList, "anyname", OGCConstants.URN_UNIQUE_IDENTIFIER, identifier);
    }

    @Test
    public void should_decode_io_from_sml() throws OwsExceptionReport {
        SensorMLDocument xbSmlDoc = getSensorMLDoc();
        SystemType xbSystem =
                (SystemType) xbSmlDoc.getSensorML().addNewMember().addNewProcess()
                        .substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
        InputList xbInputList = xbSystem.addNewInputs().addNewInputList();
        OutputList xbOutputList = xbSystem.addNewOutputs().addNewOutputList();
        IoComponentPropertyType input1 = xbInputList.addNewInput();
        input1.setName("input1");
        input1.addNewBoolean();
        IoComponentPropertyType input2 = xbInputList.addNewInput();
        input2.setName("input2");
        input2.addNewCount();

        IoComponentPropertyType output1 = xbOutputList.addNewOutput();
        output1.setName("output1");
        output1.addNewQuantity();
        IoComponentPropertyType output2 = xbOutputList.addNewOutput();
        output2.setName("output2");
        DataArrayType dataArray = getDataArray();
        output2.setAbstractDataArray1(dataArray);
        XmlHelper.substituteElement(output2.getAbstractDataArray1(), dataArray);
        IoComponentPropertyType output3 = xbOutputList.addNewOutput();
        output3.setName("output3");
        DataRecordType dataRecord = DataRecordType.Factory.newInstance();
        output3.setAbstractDataRecord(dataRecord);
        XmlHelper.substituteElement(output3.getAbstractDataRecord(), dataRecord);

        AbstractProcess absProcess = decodeAbstractProcess(xbSmlDoc);
        assertThat(absProcess.getInputs().get(0).getIoValue().getDataComponentType(), is(SweDataComponentType.Boolean));
        // assertThat(
        // ((SweAbstractSimpleType)absProcess.getInputs().get(0).getIoValue()).getDataComponentType(),
        // is (SweDataComponentType.Boolean));
        assertThat(absProcess.getInputs().get(1).getIoValue().getDataComponentType(), is(SweDataComponentType.Count));
        // assertThat(
        // ((SweAbstractSimpleType)absProcess.getInputs().get(1).getIoValue()).getDataComponentType(),
        // is (SweDataComponentType.Count));

        assertThat(absProcess.getOutputs().get(0).getIoValue().getDataComponentType(),
                is(SweDataComponentType.Quantity));
        // assertThat(
        // ((SweAbstractSimpleType)absProcess.getOutputs().get(0).getIoValue()).getDataComponentType(),
        // is (SweDataComponentType.Quantity));
        assertThat(absProcess.getOutputs().get(1).getIoValue().getDataComponentType(),
                is(SweDataComponentType.DataArray));
        assertThat(absProcess.getOutputs().get(2).getIoValue().getDataComponentType(),
                is(SweDataComponentType.DataRecord));
    }

    private DataArrayType getDataArray() {
        DataArrayType dataArray = DataArrayType.Factory.newInstance();
        dataArray.addNewElementCount().addNewCount().setValue(new BigInteger("1"));
        DataComponentPropertyType addNewElementType = dataArray.addNewElementType();
        addNewElementType.setName("elementType");
        addNewElementType.addNewBoolean();
        return dataArray;
    }

}
