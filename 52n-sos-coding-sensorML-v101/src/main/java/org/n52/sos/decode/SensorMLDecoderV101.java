/**
 * Copyright (C) 2012
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentType;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.ProcessModelType;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordPropertyType;
import net.opengis.swe.x101.SimpleDataRecordType;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMLDecoderV101 implements IDecoder<AbstractSensorML, XmlObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLDecoderV101.class);

    private List<DecoderKeyType> decoderKeyTypes;

    private Set<String> supportedProcedureDescriptionFormats;

    private List<String> removableCapabilitiesNames;

    private List<String> removableComponentsRoles;

    private List<String> removableIdentifier;

    {
        removableCapabilitiesNames = new ArrayList<String>();
        removableCapabilitiesNames.add("parentProcedures");
        removableCapabilitiesNames.add("featureOfInterest");

        removableComponentsRoles = new ArrayList<String>();
        removableComponentsRoles.add("childProcedure");

        removableIdentifier = new ArrayList<String>();
        removableIdentifier.add("offerings");
    }

    public SensorMLDecoderV101() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(SensorMLConstants.NS_SML));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());

        supportedProcedureDescriptionFormats = new HashSet<String>(0);
        supportedProcedureDescriptionFormats.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);

        LOGGER.debug("Decoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }

    @Override
    public AbstractSensorML decode(XmlObject element) throws OwsExceptionReport {
        if (element instanceof SensorMLDocument) {
            return parseSensorML((SensorMLDocument) element);
        } else if (element instanceof SystemType) {
            return parseSystem((SystemType) element);
        } else if (element instanceof ProcessModelType) {
            return parseProcessModel((ProcessModelType) element);
        } else {
            String exceptionText = "";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        Map<SupportedTypeKey, Set<String>> map = new HashMap<SupportedTypeKey, Set<String>>();
        map.put(SupportedTypeKey.ProcedureDescriptionFormat, supportedProcedureDescriptionFormats);
        return map;
    }

    private SensorML parseSensorML(SensorMLDocument xbSensorML) throws OwsExceptionReport {
        SensorML sensorML = new SensorML();
        // get member process
        for (Member xbMember : xbSensorML.getSensorML().getMemberArray()) {
            if (xbMember.getProcess() != null) {
                if (xbMember.getProcess() instanceof SystemType) {
                    sensorML.addMember(parseSystem((SystemType) xbMember.getProcess()));
                } else if (xbMember.getProcess() instanceof ProcessModelType) {
                    sensorML.addMember(parseProcessModel((ProcessModelType) xbMember.getProcess()));
                } else if (xbMember.getProcess() instanceof ComponentType) {
                    sensorML.addMember(parseComponent((ComponentType)xbMember.getProcess()));
                }
                else {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The process of a member of the SensorML Document (");
                    exceptionText.append(xbMember.getProcess().getDomNode().getNodeName());
                    exceptionText.append(") is not supported!");
                    LOGGER.debug(exceptionText.toString());
                    throw Util4Exceptions.createInvalidParameterValueException(xbMember.getDomNode().getLocalName(),
                            exceptionText.toString());
                }
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The process of a member of the SensorML Document is null (");
                exceptionText.append(xbMember.getProcess());
                exceptionText.append(")!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createInvalidParameterValueException(xbMember.getDomNode().getLocalName(),
                        exceptionText.toString());
            }
        }
        sensorML.setSensorDescriptionXmlString(xbSensorML.xmlText());
        return sensorML;
    }

    private System parseSystem(SystemType xbSystemType) throws OwsExceptionReport {
        System system = new System();
        if (xbSystemType.getIdentificationArray() != null) {
            system.setIdentifications(parseIdentification(xbSystemType.getIdentificationArray()));
            List<Integer> identificationsToRemove =
                    checkIdentificationsForRemoval(xbSystemType.getIdentificationArray());
            for (Integer integer : identificationsToRemove) {
                xbSystemType.removeIdentification(integer);
            }
        }
        if (xbSystemType.getClassificationArray() != null) {
            system.setClassifications(parseClassification(xbSystemType.getClassificationArray()));
        }
        if (xbSystemType.getCharacteristicsArray() != null) {
            system.setCharacteristics(parseCharacteristics(xbSystemType.getCharacteristicsArray()));
        }
        if (xbSystemType.getCapabilitiesArray() != null) {
            system.setCapabilities(parseCapabilities(xbSystemType.getCapabilitiesArray()));
            List<Integer> capsToRemove = checkCapabilitiesForRemoval(xbSystemType.getCapabilitiesArray());
            for (Integer integer : capsToRemove) {
                xbSystemType.removeCapabilities(integer);
            }
        }
        if (xbSystemType.isSetPosition()) {
            system.setPosition(parsePosition(xbSystemType.getPosition()));
        }
        if (xbSystemType.isSetInputs()) {
            system.setInputs(parseInputs(xbSystemType.getInputs()));
        }
        if (xbSystemType.isSetOutputs()) {
            system.setOutputs(parseOutputs(xbSystemType.getOutputs()));
        }
        if (xbSystemType.isSetComponents()) {
            system.setComponents(parseComponents(xbSystemType.getComponents()));
            List<Integer> compsToRemove = checkComponentsForRemoval(xbSystemType.getComponents().getComponentList());
            for (Integer integer : compsToRemove) {
                xbSystemType.getComponents().getComponentList().removeComponent(integer);
            }
            checkAndRemoveEmptyComponents(xbSystemType);
        }
        system.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(xbSystemType));
        return system;
    }

    private AbstractProcess parseComponent(ComponentType componentType) throws OwsExceptionReport {
        org.n52.sos.ogc.sensorML.Component component = new org.n52.sos.ogc.sensorML.Component();
        if (componentType.getIdentificationArray() != null) {
            component.setIdentifications(parseIdentification(componentType.getIdentificationArray()));
            List<Integer> identificationsToRemove =
                    checkIdentificationsForRemoval(componentType.getIdentificationArray());
            for (Integer integer : identificationsToRemove) {
                componentType.removeIdentification(integer);
            }
        }
        if (componentType.getClassificationArray() != null) {
            component.setClassifications(parseClassification(componentType.getClassificationArray()));
        }
        if (componentType.getCharacteristicsArray() != null) {
            component.setCharacteristics(parseCharacteristics(componentType.getCharacteristicsArray()));
        }
        if (componentType.getCapabilitiesArray() != null) {
            component.setCapabilities(parseCapabilities(componentType.getCapabilitiesArray()));
            List<Integer> capsToRemove = checkCapabilitiesForRemoval(componentType.getCapabilitiesArray());
            for (Integer integer : capsToRemove) {
                componentType.removeCapabilities(integer);
            }
        }
        if (componentType.isSetPosition()) {
            component.setPosition(parsePosition(componentType.getPosition()));
        }
        if (componentType.isSetInputs()) {
            component.setInputs(parseInputs(componentType.getInputs()));
        }
        if (componentType.isSetOutputs()) {
            component.setOutputs(parseOutputs(componentType.getOutputs()));
        }
        component.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(componentType));
        return component;
    }

    private ProcessModel parseProcessModel(ProcessModelType xbProcessModel) {
        ProcessModel processModel = new ProcessModel();
        // TODO Auto-generated method stub
        processModel.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(xbProcessModel));
        return processModel;
    }

    /**
     * Parses the identification
     * 
     * @param identificationArray
     *            XML identification
     * @return SOS identification
     */
    private List<SosSMLIdentifier> parseIdentification(Identification[] identificationArray) {
        List<SosSMLIdentifier> sosIdentifiers = new ArrayList<SosSMLIdentifier>();
        for (Identification xbIdentification : identificationArray) {
            for (Identifier xbIdentifier : xbIdentification.getIdentifierList().getIdentifierArray()) {
                sosIdentifiers.add(new SosSMLIdentifier(xbIdentifier.getName(),
                        xbIdentifier.getTerm().getDefinition(), xbIdentifier.getTerm().getValue()));
            }
        }
        return sosIdentifiers;
    }

    /**
     * Parses the classification
     * 
     * @param classificationArray
     *            XML classification
     * @return SOS classification
     */
    private List<SosSMLClassifier> parseClassification(Classification[] classificationArray) {
        List<SosSMLClassifier> sosClassifiers = new ArrayList<SosSMLClassifier>();
        for (Classification xbClassification : classificationArray) {
            for (Classifier xbClassifier : xbClassification.getClassifierList().getClassifierArray()) {
                sosClassifiers.add(new SosSMLClassifier(xbClassifier.getName(), xbClassifier.getTerm().getValue()));
            }
        }
        return sosClassifiers;
    }

    /**
     * Parses the characteristics
     * 
     * @param characteristicsArray
     *            XML characteristics
     * @return SOS characteristics
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private List<SosSMLCharacteristics> parseCharacteristics(Characteristics[] characteristicsArray)
            throws OwsExceptionReport {
        List<SosSMLCharacteristics> sosCharacteristicsList = new ArrayList<SosSMLCharacteristics>();
        SosSMLCharacteristics sosCharacteristics = new SosSMLCharacteristics();
        for (Characteristics xbCharacteristics : characteristicsArray) {
            if (xbCharacteristics.getAbstractDataRecord() instanceof DataRecordPropertyType) {
                DataRecordPropertyType xbDataRecordc =
                        (DataRecordPropertyType) xbCharacteristics.getAbstractDataRecord();
                for (DataComponentPropertyType xbField : xbDataRecordc.getDataRecord().getFieldArray()) {
                    // not supported yet
                    LOGGER.warn("The characteristics are not paresed from SensorML sensor description!");
                }
                sosCharacteristics.setCharacteristicsType(SweAggregateType.DataRecord);
            } else {
                String exceptionText =
                        "Error while parsing the characteristics of the SensorML (the characteristics' data record is not of type DataRecordPropertyType)!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(xbCharacteristics.getDomNode()
                        .getLocalName(), exceptionText);
            }
        }
        sosCharacteristicsList.add(sosCharacteristics);
        return sosCharacteristicsList;
    }

    /**
     * Parses the capabilities
     * 
     * @param capabilitiesArray
     *            XML capabilities
     * @return SOS capabilities
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private List<SosSMLCapabilities> parseCapabilities(Capabilities[] capabilitiesArray) throws OwsExceptionReport {
        List<SosSMLCapabilities> sosCapabilitiesList = new ArrayList<SosSMLCapabilities>();
        SosSMLCapabilities sosCapabilities = new SosSMLCapabilities();
        for (Capabilities xbCpabilities : capabilitiesArray) {
            if (xbCpabilities.getAbstractDataRecord() instanceof DataRecordPropertyType) {
                DataRecordPropertyType xbDataRecord = (DataRecordPropertyType) xbCpabilities.getAbstractDataRecord();
                sosCapabilities.setCapabilitiesType(SweAggregateType.DataRecord);
                List<IDecoder> decoderList =
                        Configurator.getInstance().getDecoder(
                                xbDataRecord.getDataRecord().getDomNode().getNamespaceURI());
                Object fieldArray = null;
                for (IDecoder decoder : decoderList) {
                    fieldArray = decoder.decode(xbDataRecord.getDataRecord().getFieldArray());
                    if (fieldArray != null) {
                        break;
                    }
                }
                if (fieldArray != null && fieldArray instanceof List<?>) {
                    sosCapabilities.setFields((List<SosSweField>) fieldArray);
                }
            } else if (xbCpabilities.getAbstractDataRecord() instanceof SimpleDataRecordType) {
                SimpleDataRecordType xbSimpleDataRecord = (SimpleDataRecordType) xbCpabilities.getAbstractDataRecord();
                sosCapabilities.setCapabilitiesType(SweAggregateType.SimpleDataRecord);
                List<IDecoder> decoderList =
                        Configurator.getInstance().getDecoder(xbSimpleDataRecord.getDomNode().getNamespaceURI());
                Object fieldArray = null;
                for (IDecoder decoder : decoderList) {
                    fieldArray = decoder.decode(xbSimpleDataRecord.getFieldArray());
                    if (fieldArray != null) {
                        break;
                    }
                }
                if (fieldArray != null && fieldArray instanceof List<?>) {
                    sosCapabilities.setFields((List<SosSweField>) fieldArray);
                }

            } else {
                String exceptionText =
                        "Error while parsing the capabilities of the SensorML (the capabilities data record is not of type DataRecordPropertyType)!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(xbCpabilities.getDomNode().getLocalName(),
                        exceptionText);
            }
        }
        sosCapabilitiesList.add(sosCapabilities);
        return sosCapabilitiesList;
    }

    /**
     * Parses the position
     * 
     * @param position
     *            XML position
     * @return SOS position
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private SosSMLPosition parsePosition(Position position) throws OwsExceptionReport {
        SosSMLPosition sosSMLPosition = new SosSMLPosition();
        if (position.getName() != null) {
            sosSMLPosition.setName(position.getName());
        }
        if (position.isSetPosition()) {
            List<IDecoder> decoderList =
                    Configurator.getInstance().getDecoder(XmlHelper.getNamespace(position.getPosition()));
            Object pos = null;
            for (IDecoder decoder : decoderList) {
                pos = decoder.decode(position.getPosition());
                if (pos != null) {
                    break;
                }
            }
            if (pos != null && pos instanceof SosSMLPosition) {
                return (SosSMLPosition) pos;
            }
        } else {
            String exceptionText = "Error while parsing the position of the SensorML (the position is not set)!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(position.getDomNode().getLocalName(),
                    exceptionText);
        }
        return sosSMLPosition;
    }

    /**
     * Parses the inputs
     * 
     * @param inputs
     *            XML inputs
     * @return SOS inputs
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private List<SosSMLIo> parseInputs(Inputs inputs) throws OwsExceptionReport {
        List<SosSMLIo> sosInputs = new ArrayList<SosSMLIo>();
        for (IoComponentPropertyType xbInput : inputs.getInputList().getInputArray()) {
            sosInputs.add(parseIoComponentPropertyType(xbInput));
        }
        return sosInputs;
    }

    /**
     * Parses the outputs
     * 
     * @param outputs
     *            XML outputs
     * @return SOS outputs
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private List<SosSMLIo> parseOutputs(Outputs outputs) throws OwsExceptionReport {
        List<SosSMLIo> sosOutputs = new ArrayList<SosSMLIo>();
        for (IoComponentPropertyType xbOutput : outputs.getOutputList().getOutputArray()) {
            sosOutputs.add(parseIoComponentPropertyType(xbOutput));
        }
        return sosOutputs;
    }

    private List<SosSMLComponent> parseComponents(Components components) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Parses the components
     * 
     * @param xbIoCompPropType
     *            XML components
     * @return SOS components
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    private SosSMLIo parseIoComponentPropertyType(IoComponentPropertyType xbIoCompPropType) throws OwsExceptionReport {
        SosSMLIo sosIo = new SosSMLIo();
        sosIo.setIoName(xbIoCompPropType.getName());
        XmlObject toEncode = null;
        if (xbIoCompPropType.isSetBoolean()) {
            toEncode = xbIoCompPropType.getBoolean();
        } else if (xbIoCompPropType.isSetCategory()) {
            toEncode = xbIoCompPropType.getCategory();
        } else if (xbIoCompPropType.isSetCount()) {
            toEncode = xbIoCompPropType.getCount();
        } else if (xbIoCompPropType.isSetCountRange()) {
            toEncode = xbIoCompPropType.getCountRange();
        } else if (xbIoCompPropType.isSetObservableProperty()) {
            toEncode = xbIoCompPropType.getObservableProperty();
        } else if (xbIoCompPropType.isSetQuantity()) {
            toEncode = xbIoCompPropType.getQuantity();
        } else if (xbIoCompPropType.isSetQuantityRange()) {
            toEncode = xbIoCompPropType.getQuantityRange();
        } else if (xbIoCompPropType.isSetText()) {
            toEncode = xbIoCompPropType.getText();
        } else if (xbIoCompPropType.isSetTime()) {
            toEncode = xbIoCompPropType.getTime();
        } else if (xbIoCompPropType.isSetTimeRange()) {
            toEncode = xbIoCompPropType.getTimeRange();
        } else {
            String exceptionText = "An \"IoComponentProperty\" is not supported";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(xbIoCompPropType.getDomNode().getLocalName(),
                    exceptionText);
        }

        List<IDecoder> decoderList = Configurator.getInstance().getDecoder(toEncode.getDomNode().getNamespaceURI());
        Object encodedObject = null;
        for (IDecoder decoder : decoderList) {
            encodedObject = decoder.decode(toEncode);
            if (encodedObject != null) {
                break;
            }
        }
        if (encodedObject != null && encodedObject instanceof SosSweAbstractSimpleType) {
            sosIo.setIoValue((SosSweAbstractSimpleType) encodedObject);
        }
        return sosIo;
    }

    private String addSensorMLWrapperForXmlDescription(AbstractProcessType xbProcessType) {
        SensorMLDocument xbSensorMLDoc =
                SensorMLDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        net.opengis.sensorML.x101.SensorMLDocument.SensorML xbSensorML = xbSensorMLDoc.addNewSensorML();
        xbSensorML.setVersion(SensorMLConstants.VERSION_V101);
        Member member = xbSensorML.addNewMember();
        member.setProcess(xbProcessType);
        member.getProcess().substitute(getQnameForType(xbProcessType.schemaType()), xbProcessType.schemaType());
        return xbSensorMLDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions());
    }

    private QName getQnameForType(SchemaType type) {
        if (type == SystemType.type) {
            return SensorMLConstants.SYSTEM_QNAME;
        } else if (type == ProcessModelType.type) {
            return SensorMLConstants.PROCESS_MODEL_QNAME;
        }
        return SensorMLConstants.ABSTRACT_PROCESS_QNAME;
    }

    private List<Integer> checkCapabilitiesForRemoval(Capabilities[] capabilitiesArray) {
        List<Integer> removeableCaps = new ArrayList<Integer>();
        for (int i = 0; i < capabilitiesArray.length; i++) {
            if (capabilitiesArray[i].getName() != null
                    && removableCapabilitiesNames.contains(capabilitiesArray[i].getName())) {
                removeableCaps.add(i);
            }
        }
        Collections.sort(removeableCaps);
        Collections.reverse(removeableCaps);
        return removeableCaps;
    }

    private List<Integer> checkComponentsForRemoval(ComponentList componentList) {
        List<Integer> removeableComponents = new ArrayList<Integer>(0);
        if (componentList != null && componentList.getComponentArray() != null) {
            Component[] componentArray = componentList.getComponentArray();
            for (int i = 0; i < componentArray.length; i++) {
                if (componentArray[i].getRole() != null
                        && removableComponentsRoles.contains(componentArray[i].getRole())) {
                    removeableComponents.add(i);
                }
            }
        }
        return removeableComponents;
    }

    private List<Integer> checkIdentificationsForRemoval(Identification[] identifications) {
        List<Integer> removeableIdentification = new ArrayList<Integer>();
        for (int i = 0; i < identifications.length; i++) {
            if (identifications[i].getTitle() != null && removableIdentifier.contains(identifications[i].getTitle())) {
                removeableIdentification.add(i);
            }
        }
        return removeableIdentification;
    }

    private void checkAndRemoveEmptyComponents(SystemType system) {
        boolean removeComponents = false;
        Components components = system.getComponents();
        if (components != null) {
            if (components.getComponentList() == null) {
                removeComponents = true;
            } else if (components.getComponentList().getComponentArray() == null
                    || ((components.getComponentList().getComponentArray() != null && components.getComponentList()
                            .getComponentArray().length == 0))) {
                removeComponents = true;
            }
        }
        if (removeComponents) {
            system.setComponents(null);
        }

    }
}
