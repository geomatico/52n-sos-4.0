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

import net.opengis.sensorML.x101.AbstractComponentType;
import net.opengis.sensorML.x101.AbstractDerivableComponentType;
import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.AbstractPureProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentType;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.ContactDocument.Contact;
import net.opengis.sensorML.x101.DocumentationDocument.Documentation;
import net.opengis.sensorML.x101.HistoryDocument.History;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.KeywordsDocument.Keywords;
import net.opengis.sensorML.x101.MethodPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.ParametersDocument.Parameters;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.ProcessModelType;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.ValidTimeDocument.ValidTime;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractComponent;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessMethod;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.AbstractSosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.AbstractDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DecoderHelper;
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
                if (xbMember.getProcess() instanceof AbstractProcessType) {
                    AbstractProcessType xbAbstractProcess = xbMember.getProcess();
                    AbstractProcess abstractProcess = null;
                    if (xbAbstractProcess.schemaType() == SystemType.type) {
                        abstractProcess = parseSystem((SystemType) xbAbstractProcess);
                    } else if (xbAbstractProcess.schemaType() == ProcessModelType.type) {
                        abstractProcess = parseProcessModel((ProcessModelType) xbAbstractProcess);
                    } else if (xbAbstractProcess.schemaType() == ComponentType.type) {
                        abstractProcess = parseComponent((ComponentType) xbAbstractProcess);
                    } else {
                        StringBuilder exceptionText = new StringBuilder();
                        exceptionText.append("The process of a member of the SensorML Document (");
                        exceptionText.append(xbMember.getProcess().getDomNode().getNodeName());
                        exceptionText.append(") is not supported!");
                        LOGGER.debug(exceptionText.toString());
                        throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(xbMember), exceptionText.toString());
                    }
                    sensorML.addMember(abstractProcess);
                } else {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The process of a member of the SensorML Document (");
                    exceptionText.append(xbMember.getProcess().getDomNode().getNodeName());
                    exceptionText.append(") is not supported!");
                    LOGGER.debug(exceptionText.toString());
                    throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(xbMember),
                            exceptionText.toString());
                }
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The process of a member of the SensorML Document is null (");
                exceptionText.append(xbMember.getProcess());
                exceptionText.append(")!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(xbMember),
                        exceptionText.toString());
            }
        }
        sensorML.setSensorDescriptionXmlString(xbSensorML.xmlText());
        return sensorML;
    }

    private void parseAbstractProcess(AbstractProcessType xbAbstractProcess, AbstractProcess abstractProcess) throws OwsExceptionReport {
        if (xbAbstractProcess.getIdentificationArray() != null) {
            abstractProcess.setIdentifications(parseIdentification(xbAbstractProcess.getIdentificationArray()));
            List<Integer> identificationsToRemove =
                    checkIdentificationsForRemoval(xbAbstractProcess.getIdentificationArray());
            for (Integer integer : identificationsToRemove) {
                xbAbstractProcess.removeIdentification(integer);
            }
        }
        if (xbAbstractProcess.getClassificationArray() != null) {
            abstractProcess.setClassifications(parseClassification(xbAbstractProcess.getClassificationArray()));
        }
        if (xbAbstractProcess.getCharacteristicsArray() != null) {
            abstractProcess.setCharacteristics(parseCharacteristics(xbAbstractProcess.getCharacteristicsArray()));
        }
        if (xbAbstractProcess.getCapabilitiesArray() != null) {
            abstractProcess.setCapabilities(parseCapabilities(xbAbstractProcess.getCapabilitiesArray()));
            List<Integer> capsToRemove = checkCapabilitiesForRemoval(xbAbstractProcess.getCapabilitiesArray());
            for (Integer integer : capsToRemove) {
                xbAbstractProcess.removeCapabilities(integer);
            }
        }
        if (xbAbstractProcess.isSetDescription()) {
            abstractProcess.addDescription(xbAbstractProcess.getDescription().getStringValue());
        }
        if (xbAbstractProcess.isSetValidTime()) {
            abstractProcess.setValidTime(parseValidTime(xbAbstractProcess.getValidTime()));
        }
        if (xbAbstractProcess.getContactArray() != null) {
            abstractProcess.setContact(parseContact(xbAbstractProcess.getContactArray()));
        }
        if (xbAbstractProcess.getDocumentationArray() != null) {
            abstractProcess.setDocumentation(parseDocumentation(xbAbstractProcess.getDocumentationArray()));
        }
        if (xbAbstractProcess.getHistoryArray() != null) {
            abstractProcess.setHistory(parseHistory(xbAbstractProcess.getHistoryArray()));
        }
        if (xbAbstractProcess.getKeywordsArray() != null) {
            abstractProcess.setKeywords(parseKeywords(xbAbstractProcess.getKeywordsArray()));
        }
        if (xbAbstractProcess.getNameArray() != null) {
            int length = xbAbstractProcess.getNameArray().length;
            for (int i = 0; i < length; i++) {
                Object decodedElement = DecoderHelper.decodeXmlElement(xbAbstractProcess.getNameArray(i));
                if (decodedElement != null && decodedElement instanceof CodeType) {
                    abstractProcess.addName((CodeType)decodedElement);
                }
            }
        }
    }

    private void parseAbstractDerivableComponent(AbstractDerivableComponentType xbAbstractDerivableComponent, AbstractComponent abstractComponent) throws OwsExceptionReport {
        if (xbAbstractDerivableComponent.isSetPosition()) {
            abstractComponent.setPosition(parsePosition(xbAbstractDerivableComponent.getPosition()));
        }
        // TODO ...
    }

    private void parseAbstractComponent(AbstractComponentType xbAbstractComponent, AbstractProcess abstractProcess) throws OwsExceptionReport {
        if (xbAbstractComponent.isSetInputs()) {
            abstractProcess.setInputs(parseInputs(xbAbstractComponent.getInputs()));
        }
        if (xbAbstractComponent.isSetOutputs()) {
            abstractProcess.setOutputs(parseOutputs(xbAbstractComponent.getOutputs()));
        }
        if (xbAbstractComponent.isSetParameters()) {
            abstractProcess.setParameters(parseParameters(xbAbstractComponent.getParameters()));
        }
    }

    private void parseAbstractPureProcess(AbstractPureProcessType xbAbstractPureProcess, ProcessModel processModel) throws OwsExceptionReport {
        if (xbAbstractPureProcess.isSetInputs()) {
            processModel.setInputs(parseInputs(xbAbstractPureProcess.getInputs()));
        }
        if (xbAbstractPureProcess.isSetOutputs()) {
            processModel.setOutputs(parseOutputs(xbAbstractPureProcess.getOutputs()));
        }
        if (xbAbstractPureProcess.isSetParameters()) {
            processModel.setParameters(parseParameters(xbAbstractPureProcess.getParameters()));
        }
        
    }

    private System parseSystem(SystemType xbSystemType) throws OwsExceptionReport {
        System system = new System();
        parseAbstractProcess(xbSystemType, system);
        parseAbstractComponent(xbSystemType, system);
        parseAbstractDerivableComponent(xbSystemType, system);
        if (xbSystemType.isSetComponents()) {
            system.setComponents(parseComponents(xbSystemType.getComponents()));
            List<Integer> compsToRemove = checkComponentsForRemoval(xbSystemType.getComponents().getComponentList());
            for (Integer integer : compsToRemove) {
                xbSystemType.getComponents().getComponentList().removeComponent(integer);
            }
            checkAndRemoveEmptyComponents(xbSystemType);
        }
        String xmlDescription = addSensorMLWrapperForXmlDescription(xbSystemType);
        system.setSensorDescriptionXmlString(xmlDescription);
        return system;
    }

    private AbstractProcess parseComponent(ComponentType componentType) throws OwsExceptionReport {
        org.n52.sos.ogc.sensorML.Component component = new org.n52.sos.ogc.sensorML.Component();
        parseAbstractProcess(componentType, component);
        parseAbstractDerivableComponent(componentType, component);
        parseAbstractComponent(componentType, component);
        if (componentType.isSetPosition()) {
            component.setPosition(parsePosition(componentType.getPosition()));
        }
        component.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(componentType));
        return component;
    }

    private ProcessModel parseProcessModel(ProcessModelType xbProcessModel) throws OwsExceptionReport {
        ProcessModel processModel = new ProcessModel();
        parseAbstractProcess(xbProcessModel, processModel);
        parseAbstractPureProcess(xbProcessModel, processModel);
        if (xbProcessModel.getMethod() != null) {
            processModel.setMethod(parseProcessMethod(xbProcessModel.getMethod()));
        }
        processModel.setSensorDescriptionXmlString(addSensorMLWrapperForXmlDescription(xbProcessModel));
        return processModel;
    }

    private ProcessMethod parseProcessMethod(MethodPropertyType method) {
        ProcessMethod processMethod = new ProcessMethod();
        // TODO
        return processMethod;
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
                sosClassifiers.add(new SosSMLClassifier(xbClassifier.getName(),
                        xbClassifier.getTerm().getDefinition(), xbClassifier.getTerm().getValue()));
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
            Object decodedObject = DecoderHelper.decodeXmlElement(xbCharacteristics.getAbstractDataRecord());
            if (decodedObject != null && decodedObject instanceof AbstractDataRecord) {
                sosCharacteristics.setDataRecord((AbstractDataRecord)decodedObject);
            } else {
                String exceptionText =
                        "Error while parsing the characteristics of the SensorML (the characteristics' data record is not of type DataRecordPropertyType)!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(xbCharacteristics), exceptionText);
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
            Object decodedObject = DecoderHelper.decodeXmlElement(xbCpabilities.getAbstractDataRecord());
            if (decodedObject != null && decodedObject instanceof AbstractDataRecord) {
                sosCapabilities.setDataRecord((AbstractDataRecord)decodedObject);
            } else {
                String exceptionText =
                        "Error while parsing the capabilities of the SensorML (the capabilities data record is not of type DataRecordPropertyType)!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(xbCpabilities),
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
            Object pos = DecoderHelper.decodeXmlElement(position.getPosition());
            if (pos != null && pos instanceof SosSMLPosition) {
                return (SosSMLPosition) pos;
            }
        } else {
            String exceptionText = "Error while parsing the position of the SensorML (the position is not set)!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(position),
                    exceptionText);
        }
        return sosSMLPosition;
    }

    private ITime parseValidTime(ValidTime validTime) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<String> parseParameters(Parameters parameters) {
        List<String> sosParameters = new ArrayList<String>(0);
        // TODO Auto-generated method stub
        return sosParameters;
    }

    private String parseContact(Contact[] contactArray) {
        // TODO Auto-generated method stub
        return "";
    }

    private List<AbstractSosSMLDocumentation> parseDocumentation(Documentation[] documentationArray) {
        List<AbstractSosSMLDocumentation> abstractDocumentation = new ArrayList<AbstractSosSMLDocumentation>(0);
        // TODO Auto-generated method stub
        return abstractDocumentation;
    }

    private List<String> parseKeywords(Keywords[] keywordsArray) {
        List<String> keywords = new ArrayList<String>(0);
        // TODO Auto-generated method stub
        return keywords;
    }

    private String parseHistory(History[] historyArray) {
        // TODO Auto-generated method stub
        return "";
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
        XmlObject toDecode = null;
        if (xbIoCompPropType.isSetBoolean()) {
            toDecode = xbIoCompPropType.getBoolean();
        } else if (xbIoCompPropType.isSetCategory()) {
            toDecode = xbIoCompPropType.getCategory();
        } else if (xbIoCompPropType.isSetCount()) {
            toDecode = xbIoCompPropType.getCount();
        } else if (xbIoCompPropType.isSetCountRange()) {
            toDecode = xbIoCompPropType.getCountRange();
        } else if (xbIoCompPropType.isSetObservableProperty()) {
            toDecode = xbIoCompPropType.getObservableProperty();
        } else if (xbIoCompPropType.isSetQuantity()) {
            toDecode = xbIoCompPropType.getQuantity();
        } else if (xbIoCompPropType.isSetQuantityRange()) {
            toDecode = xbIoCompPropType.getQuantityRange();
        } else if (xbIoCompPropType.isSetText()) {
            toDecode = xbIoCompPropType.getText();
        } else if (xbIoCompPropType.isSetTime()) {
            toDecode = xbIoCompPropType.getTime();
        } else if (xbIoCompPropType.isSetTimeRange()) {
            toDecode = xbIoCompPropType.getTimeRange();
        } else if (xbIoCompPropType.isSetAbstractDataArray1()) {
            toDecode = xbIoCompPropType.getAbstractDataArray1();
        } else if (xbIoCompPropType.isSetAbstractDataRecord()) {
            toDecode = xbIoCompPropType.getAbstractDataRecord();
        } else {
            String exceptionText = "An \"IoComponentProperty\" is not supported";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(XmlHelper.getLocalName(xbIoCompPropType),
                    exceptionText);
        }

        Object decodedObject = DecoderHelper.decodeXmlElement(toDecode);
        if (decodedObject != null && decodedObject instanceof SosSweAbstractSimpleType) {
            sosIo.setIoValue((SosSweAbstractSimpleType) decodedObject);
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
            system.unsetComponents();
        }
    }
}
