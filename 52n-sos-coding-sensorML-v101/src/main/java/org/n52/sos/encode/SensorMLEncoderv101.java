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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.MetaDataPropertyType;
import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.DocumentDocument.Document;
import net.opengis.sensorML.x101.DocumentListDocument.DocumentList;
import net.opengis.sensorML.x101.DocumentationDocument.Documentation;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.ProcessModelDocument;
import net.opengis.sensorML.x101.ProcessModelType;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.VectorType;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.SosGmlMetaDataProperty;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.ProcessModel;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.AbstractSosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentation;
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentationList;
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentationListMember;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosProcedureDescriptionUnknowType;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SWEConstants.SweSimpleType;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweCountRange;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantityRange;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMLEncoderv101 implements Encoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLEncoderv101.class);
    
    private Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.ProcedureDescriptionFormat, Collections.singleton(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL));
    private Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(
    		SensorMLConstants.NS_SML, 
    		SosProcedureDescription.class, 
    		AbstractSensorML.class);

    public SensorMLEncoderv101() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.unmodifiableMap(SUPPORTED_TYPES);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SensorMLConstants.NS_SML, SensorMLConstants.NS_SML_PREFIX);
        // remove if GML 3.1.1 encoder is available
        nameSpacePrefixMap.put(GMLConstants.NS_GML, GMLConstants.NS_GML_PREFIX);
    }

    @Override
    public String getContentType() {
        return SensorMLConstants.SENSORML_CONTENT_TYPE;
    }

    @Override
    public XmlObject encode(Object response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(Object response, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (response instanceof AbstractSensorML) {
            return createSensorDescription((AbstractSensorML) response);
        }
     // FIXME workaround? if of type UnknowProcedureType try to parse the description string, UNIT is missing "NOT_DEFINED"?!
        if (response instanceof SosProcedureDescriptionUnknowType) {
        	
        	String procDescXMLString = ((SosProcedureDescription) response).getSensorDescriptionXmlString();
        	AbstractSensorML sensorDesc = new AbstractSensorML();
        	sensorDesc.setSensorDescriptionXmlString(procDescXMLString);
            return createSensorDescriptionFromString(sensorDesc);
        }
        
        return null;
    }

    /**
     * creates sml:System
     * 
     * @param sensorDesc
     *            SensorML encoded system description
     * @param parentProcedureIds
     *            collection of parent procedure ids to include in the response
     * @param childProcedures
     *            map of child procedure SosSensorMLs keyed by child procedure
     *            id, if any
     * 
     * @return Returns XMLBeans representation of sml:System
     * @throws OwsExceptionReport
     */
    private XmlObject createSensorDescription(AbstractSensorML sensorDesc) throws OwsExceptionReport {
        if (sensorDesc.getSensorDescriptionXmlString() != null
                && !sensorDesc.getSensorDescriptionXmlString().isEmpty()) {
            return createSensorDescriptionFromString(sensorDesc);
        } else {
            return createSensorDescriptionFromObject(sensorDesc);
        }
    }

    private XmlObject createSensorDescriptionFromString(AbstractSensorML sensorDesc) throws OwsExceptionReport {
        try {
            XmlObject xmlObject = XmlObject.Factory.parse(sensorDesc.getSensorDescriptionXmlString());
            if (xmlObject instanceof SensorMLDocument) {
                SensorMLDocument sensorML = (SensorMLDocument) xmlObject;
                sensorML.getSensorML().getMemberArray();
                for (Member member : sensorML.getSensorML().getMemberArray()) {
                    if (sensorDesc instanceof SensorML) {
                        for (AbstractProcess absProcess : ((SensorML) sensorDesc).getMembers()) {
                            absProcess.setFeatureOfInterest(sensorDesc.getFeatureOfInterest());
                            addAbstractProcessValues(member.getProcess(), absProcess);
                            if (member.getProcess() instanceof SystemType
                                    && absProcess instanceof System) {
                                addSystemValues((SystemType) member.getProcess(),
                                        (System) absProcess);
                            } else if (member.getProcess() instanceof ProcessModelType && absProcess instanceof ProcessModel) {
                                addProcessModelValues((ProcessModelType)member.getProcess(),
                                        (ProcessModel) absProcess);
                            }
                        }
                    } else if (sensorDesc instanceof AbstractProcess) {
                        addAbstractProcessValues(member.getProcess(), (AbstractProcess)sensorDesc);
                        if (member.getProcess() instanceof SystemType && sensorDesc instanceof System) {
                            addSystemValues((SystemType) member.getProcess(), (System) sensorDesc);
                        }
                    }
                }

            } else if (xmlObject instanceof AbstractProcessType) {
                // TODO add values
            }
            return xmlObject;
        } catch (XmlException xmle) {
            throw Util4Exceptions.createNoApplicableCodeException(xmle, "");
        }
    }

    private XmlObject createSensorDescriptionFromObject(AbstractSensorML sensorDesc) throws OwsExceptionReport {
        if (sensorDesc instanceof SensorML) {
            return createSensorMLDescription((SensorML) sensorDesc);
        } else if (sensorDesc instanceof AbstractProcess) {
            return createProcessDescription((AbstractProcess) sensorDesc);
        } else {
            String exceptionText = "The sensor description type is not supported by this service!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

    }

    private XmlObject createSensorMLDescription(SensorML sensorDesc) {
        SensorMLDocument sensorMLDoc =
                SensorMLDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        net.opengis.sensorML.x101.SensorMLDocument.SensorML sensorML = sensorMLDoc.addNewSensorML();
        sensorML.setVersion(SensorMLConstants.VERSION_V101);
        // TODO: set all other elements
        return sensorMLDoc;
    }

    private XmlObject createProcessDescription(AbstractProcess sensorDesc) throws OwsExceptionReport {
        if (sensorDesc instanceof System) {
            System system = (System) sensorDesc;
            SystemDocument xbSystemDoc =
                    SystemDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            SystemType xbSystem = xbSystemDoc.addNewSystem();
            addSystemValues(xbSystem, system);
            return xbSystemDoc;
        } else if (sensorDesc instanceof ProcessModel) {
            // TODO: set values
            // ProcessModel processModel = (ProcessModel) sensorDesc;
            ProcessModelDocument xbProcessModel =
                    ProcessModelDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            return xbProcessModel;
        } else {
            String exceptionText = "The sensor description type is not supported by this service!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    // TODO refactor/rename
    private void addAbstractProcessValues( AbstractProcessType abstractProcess, AbstractProcess sosAbstractProcess)
            throws OwsExceptionReport {
        if (sosAbstractProcess.isSetFeatureOfInterest(sosAbstractProcess.getProcedureIdentifier())) {
            SosSMLCapabilities featureCapabilities = createCapabilitiesFromFeatures(sosAbstractProcess.getFeatureOfInterest(sosAbstractProcess.getProcedureIdentifier()));
            sosAbstractProcess.addCapabilities(featureCapabilities);
        }
        
        // set identification
        if (sosAbstractProcess.isSetIdentifications()) {
            abstractProcess.setIdentificationArray(createIdentification(sosAbstractProcess.getIdentifications()));
        }
        // set classification
        if (sosAbstractProcess.isSetClassifications()) {
            abstractProcess.setClassificationArray(createClassification(sosAbstractProcess.getClassifications()));
        }
        // set characteristics
        if (sosAbstractProcess.isSetCharacteristics()) {
            abstractProcess.setCharacteristicsArray(createCharacteristics(sosAbstractProcess.getCharacteristics()));
        }
        // set capabilities
        if (sosAbstractProcess.isSetCapabilities()) {
            abstractProcess.setCapabilitiesArray(createCapabilities(abstractProcess, sosAbstractProcess.getCapabilities()));
        }
        if (sosAbstractProcess.isSetDocumentation()) {
            abstractProcess.setDocumentationArray(createDocumentationArray(sosAbstractProcess.getDocumentation()));
        }
    }

    private void addSystemValues(SystemType xbSystem, System system) throws OwsExceptionReport {
        // set inputs
        if (system.isSetInputs()) {
            xbSystem.setInputs(createInputs(system.getInputs()));
        }
        // set outputs
        if (system.isSetOutputs()) {
            xbSystem.setOutputs(createOutputs(system.getOutputs()));
        }
        // set position
        if (system.isSetPosition()) {
            xbSystem.setPosition(createPosition(system.getPosition()));
        }
        // set components
        if (system.isSetComponents()) {
            Components components = createComponents(system.getComponents());
            if (components != null && components.getComponentList() != null
                    && components.getComponentList().sizeOfComponentArray() > 0) {
                xbSystem.setComponents(createComponents(system.getComponents()));
            }
        }
    }

    private void addProcessModelValues(ProcessModelType processModel, ProcessModel sosProcessModel) throws OwsExceptionReport {
        // set inputs
        if (sosProcessModel.isSetInputs()) {
            processModel.setInputs(createInputs(sosProcessModel.getInputs()));
        }
        // set outputs
        if (sosProcessModel.isSetOutputs()) {
            processModel.setOutputs(createOutputs(sosProcessModel.getOutputs()));
        }
    }

    /**
     * Creates the identification section of the SensorML description.
     * 
     * @param xbIdentification
     *            Xml identification object
     * @param identifications
     *            SOS SWE representation.
     */
    private Identification[] createIdentification(List<SosSMLIdentifier> identifications) {
        Identification xbIdentification =
                Identification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        IdentifierList xbIdentifierList = xbIdentification.addNewIdentifierList();
        for (SosSMLIdentifier sosSMLIdentifier : identifications) {
            Identifier xbIdentifier = xbIdentifierList.addNewIdentifier();
            xbIdentifier.setName(sosSMLIdentifier.getName());
            Term xbTerm = xbIdentifier.addNewTerm();
            xbTerm.setDefinition(sosSMLIdentifier.getDefinition());
            xbTerm.setValue(sosSMLIdentifier.getValue());
        }
        Identification[] identificationArray = { xbIdentification };
        return identificationArray;
    }

    /**
     * Creates the classification section of the SensorML description.
     * 
     * @param xbClassification
     *            Xml classifications object
     * @param classifications
     *            SOS SWE representation.
     */
    private Classification[] createClassification(List<SosSMLClassifier> classifications) {
        Classification xbClassification =
                Classification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ClassifierList xbClassifierList = xbClassification.addNewClassifierList();
        for (SosSMLClassifier sosSMLClassifier : classifications) {
            Classifier xbClassifier = xbClassifierList.addNewClassifier();
            xbClassifier.setName(sosSMLClassifier.getName());
            Term xbTerm = xbClassifier.addNewTerm();
            xbTerm.setValue(sosSMLClassifier.getValue());
        }
        Classification[] classificationArray = { xbClassification };
        return classificationArray;
    }

    /**
     * Creates the characteristics section of the SensorML description.
     * 
     * @param xbCharacteristics
     *            Xml characteristics object
     * @param list
     *            SOS SWE representation.
     * @throws OwsExceptionReport
     */
    private Characteristics[] createCharacteristics(List<SosSMLCharacteristics> smlCharacteristics)
            throws OwsExceptionReport {
        List<Characteristics> characteristicsList = new ArrayList<Characteristics>(smlCharacteristics.size());
        for (SosSMLCharacteristics sosSMLCharacteristics : smlCharacteristics) {
            if (sosSMLCharacteristics.isSetAbstractDataRecord()) {
                Characteristics xbCharacteristics =
                        Characteristics.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                if (sosSMLCharacteristics.getDataRecord() instanceof SosSweSimpleDataRecord) {
                    SimpleDataRecordType xbSimpleDataRecord =
                            (SimpleDataRecordType) xbCharacteristics.addNewAbstractDataRecord().substitute(
                                    SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
                    if (sosSMLCharacteristics.isSetTypeDefinition()) {
                        xbSimpleDataRecord.setDefinition(sosSMLCharacteristics.getTypeDefinition());
                    }
                    if (sosSMLCharacteristics.getDataRecord().isSetFields()) {
                        for (SosSweField field : sosSMLCharacteristics.getDataRecord().getFields()) {
                            AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                            xbField.setName(field.getName());
                            addSweSimpleTypeToField(xbField, field.getElement());
                        }
                    }
                } else if (sosSMLCharacteristics.getDataRecord() instanceof SosSweDataRecord) {
                    String exceptionText =
                            "The SWE characteristics type '" + SweAggregateType.DataRecord.name()
                                    + "' is not supported by this SOS for SensorML characteristics!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                } else {
                    String exceptionText =
                            "The SWE characteristics type '"
                                    + sosSMLCharacteristics.getDataRecord().getClass().getName()
                                    + "' is not supported by this SOS for SensorML characteristics!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
                characteristicsList.add(xbCharacteristics);
            }
        }
        return characteristicsList.toArray(new Characteristics[characteristicsList.size()]);
    }

    private SosSMLCapabilities createCapabilitiesFromFeatures(Set<String> featureOfInterest) {
        SosSMLCapabilities capabilities = new SosSMLCapabilities();
        capabilities.setName("featureOfInterest");
        SosSweSimpleDataRecord simpleDataRecord = new SosSweSimpleDataRecord();
        List<SosSweField> fields = new ArrayList<SosSweField>(featureOfInterest.size());
        for (String foiID : featureOfInterest) {
            SosSweText text = new SosSweText();
            text.setDefinition("FeatureOfInterest identifier");
            text.setValue(foiID);
            fields.add(new SosSweField("FeatureOfInterestID", text));
        }
        simpleDataRecord.setFields(fields);
        capabilities.setDataRecord(simpleDataRecord);
        return capabilities;
    }

    /**
     * Creates the capabilities section of the SensorML description.
     * 
     * @throws OwsExceptionReport
     */
    private Capabilities[] createCapabilities(AbstractProcessType abstractProcess, List<SosSMLCapabilities> smlCapabilities)
            throws OwsExceptionReport {
        if (smlCapabilities == null) {
            return new Capabilities[0];
        } else {
            List<Capabilities> capabilitiesList = new ArrayList<Capabilities>(smlCapabilities.size());
            if (abstractProcess != null) {
                if (isCapabilitiesArrayAlreadyAvailable(abstractProcess)) {
                    capabilitiesList.addAll(Arrays.asList(abstractProcess.getCapabilitiesArray()));
                }
                for (SosSMLCapabilities capabilities : smlCapabilities) {
                    if (capabilities != null && capabilities.isSetAbstractDataRecord()) { // List could contain null elements
                        Capabilities xbCapabilities =
                                     Capabilities.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                        if (capabilities.getName() != null) {
                            xbCapabilities.setName(capabilities.getName());
                        }
                        if (capabilities.getDataRecord() instanceof SosSweSimpleDataRecord) {
                            SimpleDataRecordType xbSimpleDataRecord =
                                                 (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord()
                                    .substitute(
                                    SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
                            if (capabilities.getDataRecord().isSetFields()) {
                                for (SosSweField field : capabilities.getDataRecord().getFields()) {
                                    AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                                    xbField.setName(field.getName());
                                    addSweSimpleTypeToField(xbField, field.getElement());
                                }
                            }
                            if (capabilities.getMetaDataProperties() != null) {
                                for (SosGmlMetaDataProperty metadata : capabilities.getMetaDataProperties()) {
                                    MetaDataPropertyType xb_metaData = xbSimpleDataRecord.addNewMetaDataProperty();
                                    if (metadata.getTitle() != null) {
                                        xb_metaData.setTitle(metadata.getTitle());
                                    }
                                    if (metadata.getRole() != null) {
                                        xb_metaData.setRole(metadata.getRole());
                                    }
                                    if (metadata.getHref() != null) {
                                        xb_metaData.setHref(metadata.getHref());
                                    }
                                }
                            }
                        } else if (capabilities.getDataRecord() instanceof SosSweDataRecord) {
                            String exceptionText =
                                   "The SWE capabilities type '" + SweAggregateType.DataRecord.name()
                                   + "' is not supported by this SOS for SensorML!";
                            LOGGER.debug(exceptionText);
                            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                        } else {
                            String exceptionText =
                                   "The SWE capabilities type '" + SweAggregateType.DataRecord.name()
                                   + "' is not supported by this SOS for SensorML!";
                            LOGGER.debug(exceptionText);
                            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                        }
                        capabilitiesList.add(xbCapabilities);
                    }
                }
            }

            return capabilitiesList.toArray(new Capabilities[capabilitiesList.size()]);
        }
    }

    private boolean isCapabilitiesArrayAlreadyAvailable(AbstractProcessType abstractProcess) {
        return abstractProcess.getCapabilitiesArray() != null && abstractProcess.getCapabilitiesArray().length > 0;
    }

    private Documentation[] createDocumentationArray(List<AbstractSosSMLDocumentation> sosDocumentation) {
        List<Documentation> documentationList = new ArrayList<Documentation>(sosDocumentation.size());
        for (AbstractSosSMLDocumentation abstractSosSMLDocumentation : sosDocumentation) {
            Documentation documentation = Documentation.Factory.newInstance();
            if (abstractSosSMLDocumentation instanceof SosSMLDocumentation) {
                documentation.setDocument(createDocument((SosSMLDocumentation) abstractSosSMLDocumentation));
            } else if (abstractSosSMLDocumentation instanceof SosSMLDocumentationList) {
                documentation
                        .setDocumentList(createDocumentationList((SosSMLDocumentationList) abstractSosSMLDocumentation));
            }
            documentationList.add(documentation);
        }
        return documentationList.toArray(new Documentation[documentationList.size()]);
    }

    private Document createDocument(SosSMLDocumentation sosDocumentation) {
        Document document = Document.Factory.newInstance();
        if (sosDocumentation.isSetDescription()) {
            document.addNewDescription().setStringValue(sosDocumentation.getDescription());
        } else {
            document.addNewDescription().setStringValue("");
        }
        if (sosDocumentation.isSetDate()) {
            document.setDate(sosDocumentation.getDate().getValue().toDate());
        }
        if (sosDocumentation.isSetContact()) {
            document.addNewContact().addNewResponsibleParty().setIndividualName(sosDocumentation.getContact());
        }
        if (sosDocumentation.isSetFormat()) {
            document.setFormat(sosDocumentation.getFormat());
        }
        if (sosDocumentation.isSetVersion()) {
            document.setVersion(sosDocumentation.getVersion());
        }
        return document;
    }

    private DocumentList createDocumentationList(SosSMLDocumentationList sosDocumentationList) {
        DocumentList documentList = DocumentList.Factory.newInstance();
        if (sosDocumentationList.isSetDescription()) {
            documentList.addNewDescription().setStringValue(sosDocumentationList.getDescription());
        }
        if (sosDocumentationList.isSetMembers()) {
            for (SosSMLDocumentationListMember sosMmember : sosDocumentationList.getMember()) {
                net.opengis.sensorML.x101.DocumentListDocument.DocumentList.Member member =
                        documentList.addNewMember();
                member.setName(sosMmember.getName());
                member.setDocument(createDocument(sosMmember.getDocumentation()));
            }
        }
        return documentList;
    }

    /**
     * Creates the position section of the SensorML description.
     * 
     * @param xbPosition
     *            Xml position object
     * @param position
     *            SOS SWE representation.
     * @throws OwsExceptionReport
     */
    private Position createPosition(SosSMLPosition position) throws OwsExceptionReport {
        Position xbPosition = Position.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (position.getName() != null && !position.getName().isEmpty()) {
            xbPosition.setName(position.getName());
        }
        PositionType xbSwePosition = xbPosition.addNewPosition();
        xbSwePosition.setFixed(position.isFixed());
        xbSwePosition.setReferenceFrame(position.getReferenceFrame());
        VectorType xbVector = xbSwePosition.addNewLocation().addNewVector();
        for (SosSweCoordinate<?> coordinate : position.getPosition()) {
            if (coordinate.getValue().getValue() != null
                    && (!coordinate.getValue().isSetValue() || !coordinate.getValue().getValue().equals(Double.NaN))) {
                // FIXME: SWE Common NS
                xbVector.addNewCoordinate().set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, coordinate));
            }
        }
        return xbPosition;
    }

    /**
     * Creates the inputs section of the SensorML description.
     * 
     * @param xbInputs
     *            Xml inputs object
     * @param inputs
     *            SOS SWE representation.
     * @throws OwsExceptionReport
     */
    private Inputs createInputs(List<SosSMLIo<?>> inputs) throws OwsExceptionReport {
        Inputs xbInputs = Inputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        InputList xbInputList = xbInputs.addNewInputList();
        for (SosSMLIo<?> sosSMLIo : inputs) {
            addIoComponentPropertyType(xbInputList.addNewInput(), sosSMLIo);
        }
        return xbInputs;
    }

    /**
     * Creates the outputs section of the SensorML description.
     * 
     * @param xbOutputs
     *            Xml outputs object
     * @param outputs
     *            SOS SWE representation.
     * @throws OwsExceptionReport
     */
    private Outputs createOutputs(List<SosSMLIo<?>> sosOutputs) throws OwsExceptionReport {
        Outputs outputs = Outputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        OutputList outputList = outputs.addNewOutputList();
        for (SosSMLIo<?> sosSMLIo : sosOutputs) {
            addIoComponentPropertyType(outputList.addNewOutput(), sosSMLIo);
        }
        return outputs;
    }

    /**
     * Creates the components section of the SensorML description.
     * 
     * @param xbComponents
     *            Xml components object
     * @param sosComponents
     *            SOS SWE representation.
     * @param components
     * @throws OwsExceptionReport
     */
    private Components createComponents(List<SosSMLComponent> sosComponents) throws OwsExceptionReport {
        Components components = Components.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ComponentList componentList = components.addNewComponentList();
        for (SosSMLComponent sosSMLComponent : sosComponents) {
            Component component = componentList.addNewComponent();
            if (sosSMLComponent.getName() != null) {
                component.setName(sosSMLComponent.getName());
            }
            if (sosSMLComponent.getTitle() != null) {
                component.setTitle(sosSMLComponent.getTitle());
            }
            if (sosSMLComponent.getHref() != null) {
                component.setHref(sosSMLComponent.getHref());
            }
            if (sosSMLComponent.getProcess() != null) {
                XmlObject xmlObject = null;
                if (sosSMLComponent.getProcess().getSensorDescriptionXmlString() != null
                        && !sosSMLComponent.getProcess().getSensorDescriptionXmlString().isEmpty()) {
                    try {
                        xmlObject =
                                XmlObject.Factory.parse(sosSMLComponent.getProcess().getSensorDescriptionXmlString());

                    } catch (XmlException xmle) {
                        String exceptionText =
                                "Error while encoding SensorML child procedure description from stored SensorML"
                                        + " encoded sensor description with XMLBeans";
                        LOGGER.debug(exceptionText);
                        throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                    }
                } else {
                    xmlObject = createSensorDescriptionFromObject(sosSMLComponent.getProcess());
                }
                if (xmlObject != null) {
                    AbstractProcessType xbProcess = null;
                    SchemaType schemaType = null;
                    if (xmlObject instanceof SensorMLDocument) {
                        SensorMLDocument smlDoc = (SensorMLDocument) xmlObject;
                        for (Member member : smlDoc.getSensorML().getMemberArray()) {
                            schemaType = member.getProcess().schemaType();
                            xbProcess = member.getProcess();
                        }
                    } else if (xmlObject instanceof AbstractProcessType) {
                        schemaType = xmlObject.schemaType();
                        xbProcess = (AbstractProcessType) xmlObject;
                    } else {
                        String exceptionText = "The sensor type is not supported by this SOS";
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                    }
                    component.setProcess(xbProcess);
                    if (schemaType == null) {
                        schemaType = xbProcess.schemaType();
                    }
                    component.getProcess().substitute(getQnameForType(schemaType), schemaType);
                }
            }
        }
        return components;
    }

    /**
     * Adds a SOS SWE simple type to a XML SWE field.
     * 
     * @param iSosSweSimpleType
     * @param xbField
     * 
     * @param xbField
     *            XML SWE field
     * @param sosSweData
     *            SOS SWE simple type.
     * @throws OwsExceptionReport
     */
    private void addSweSimpleTypeToField(AnyScalarPropertyType xbField, SosSweAbstractDataComponent sosSweData)
            throws OwsExceptionReport {
        if (sosSweData instanceof SosSweAbstractSimpleType) {
            SosSweAbstractSimpleType<?> sosSweSimpleType = (SosSweAbstractSimpleType) sosSweData;
            switch (sosSweSimpleType.getSimpleType()) {
            case Boolean:
                String exceptionTextBool =
                        "The SWE simpleType '" + SweSimpleType.Boolean.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextBool);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextBool);
            case Category:
                String exceptionTextCategory =
                        "The SWE simpleType '" + SweSimpleType.Category.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextCategory);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextCategory);
            case Count:
                String exceptionTextCount =
                        "The SWE simpleType '" + SweSimpleType.Count.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextCount);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextCount);
            case CountRange:
                String exceptionTextCountRange =
                        "The SWE simpleType '" + SweSimpleType.CountRange.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextCountRange);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextCountRange);

            case Quantity:
                String exceptionTextQuantity =
                        "The SWE simpleType '" + SweSimpleType.Quantity.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextQuantity);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextQuantity);
            case QuantityRange:
                String exceptionTextQuantityRange =
                        "The SWE simpleType '" + SweSimpleType.QuantityRange.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextQuantityRange);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextQuantityRange);
            case Text:
                // FIXME: SWE Common NS
                Encoder<?, SosSweAbstractDataComponent> encoder = Configurator.getInstance().getCodingRepository()
                        .getEncoder(new XmlEncoderKey(SWEConstants.NS_SWE, sosSweData.getClass()));
                if (encoder != null) {
                    xbField.setText((Text) encoder.encode(sosSweData));
                } else {
                    String exceptionTextText =
                            "The SWE simpleType '" + SweSimpleType.Text.name()
                                    + "' is not supported by this SOS for SWE fields!";
                    LOGGER.debug(exceptionTextText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextText);
                }
                break;
            case Time:
                String exceptionTextTime =
                        "The SWE simpleType '" + SweSimpleType.Time.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextTime);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextTime);
            case TimeRange:
                String exceptionTextTimeRange =
                        "The SWE simpleType '" + SweSimpleType.TimeRange.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextTimeRange);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextTimeRange);
            default:
                String exceptionTextDefault =
                        "The SWE simpleType '" + sosSweSimpleType.getSimpleType().name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextDefault);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextDefault);
            }
        } else if (sosSweData instanceof SosSweDataArray) {
            Encoder<?, SosSweAbstractDataComponent>  encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(new XmlEncoderKey(SWEConstants.NS_SWE, SosSweDataArray.class));
            if (encoder != null) {
                xbField.set((XmlObject) encoder.encode(sosSweData));
            } else {
                String exceptionTextText = "The SweDataArray is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextText);
            }
        } else if (sosSweData instanceof SosSweDataRecord) {
            Encoder<?, SosSweAbstractDataComponent> encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(new XmlEncoderKey(SWEConstants.NS_SWE, SosSweDataRecord.class));
            if (encoder != null) {
                xbField.set((XmlObject) encoder.encode(sosSweData));
            } else {
                String exceptionTextText = "The SosSweDataRecord is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextText);
            }
        } else {
            String exceptionTextDefault = "The SWE type is not supported by this SOS for SWE fields!";
            LOGGER.debug(exceptionTextDefault);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextDefault);
        }
    }

    /**
     * Adds a SOS SWE simple type to a XML SML IO component.
     * 
     * @param ioComponentPopertyType
     *            SML IO component
     * @param sosSMLInput
     *            SOS SWE simple type.
     * @throws OwsExceptionReport
     */
    private void addIoComponentPropertyType(IoComponentPropertyType ioComponentPopertyType, SosSMLIo<?> sosSMLIO)
            throws OwsExceptionReport {
        ioComponentPopertyType.setName(sosSMLIO.getIoName());
        switch (sosSMLIO.getIoValue().getSimpleType()) {
        case Boolean:
            ioComponentPopertyType.addNewBoolean().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweBoolean) sosSMLIO.getIoValue()));
            break;
        case Category:
            ioComponentPopertyType.addNewCategory().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweCategory) sosSMLIO.getIoValue()));
            break;
        case Count:
            ioComponentPopertyType.addNewCount().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweCount) sosSMLIO.getIoValue()));
            break;
        case CountRange:
            ioComponentPopertyType.addNewCountRange().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweCountRange) sosSMLIO.getIoValue()));
            break;
        case ObservableProperty:
            ioComponentPopertyType.addNewObservableProperty().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweObservableProperty) sosSMLIO.getIoValue()));
            break;
        case Quantity:
                ioComponentPopertyType.addNewQuantity().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweQuantity) sosSMLIO.getIoValue()));
            break;
        case QuantityRange:
            ioComponentPopertyType.addNewQuantityRange().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweQuantityRange) sosSMLIO.getIoValue()));
            break;
        case Text:
            ioComponentPopertyType.addNewText().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweText) sosSMLIO.getIoValue()));
            break;
        case Time:
            ioComponentPopertyType.addNewTime().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweTime) sosSMLIO.getIoValue()));
            break;
        case TimeRange:
            ioComponentPopertyType.addNewTimeRange().set(
                    CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE, (SosSweTimeRange) sosSMLIO.getIoValue()));
            break;
        default:
            String exceptionTextDefault =
                    "The SWE simpleType '" + sosSMLIO.getIoValue().getSimpleType().name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextDefault);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextDefault);
        }
    }

    private QName getQnameForType(SchemaType type) {
        if (type == SystemType.type) {
            return SensorMLConstants.SYSTEM_QNAME;
        } else if (type == ProcessModelType.type) {
            return SensorMLConstants.PROCESS_MODEL_QNAME;
        }
        return SensorMLConstants.ABSTRACT_PROCESS_QNAME;
    }

}
