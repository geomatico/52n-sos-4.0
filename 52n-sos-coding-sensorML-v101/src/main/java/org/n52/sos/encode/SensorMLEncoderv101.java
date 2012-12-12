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
package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.n52.sos.ogc.sensorML.AbstractMultiProcess;
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
import org.n52.sos.ogc.sensorML.elements.SosSMLDocumentationList.SosSMLDocumentationListMember;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SWEConstants.SweSimpleType;
import org.n52.sos.ogc.swe.SosMetadata;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorMLEncoderv101 implements IEncoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLEncoderv101.class);

    private List<EncoderKeyType> encoderKeyTypes;

    private Map<SupportedTypeKey, Set<String>> supportedTypes;

    private Set<String> conformanceClasses;

    public SensorMLEncoderv101() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SensorMLConstants.NS_SML));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        Set<String> outputFormatSet = new HashSet<String>(0);
        outputFormatSet.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
        Map<SupportedTypeKey, Set<String>> map = new HashMap<SupportedTypeKey, Set<String>>();
        map.put(SupportedTypeKey.ProcedureDescriptionFormat, outputFormatSet);
        conformanceClasses = new HashSet<String>(0);
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return supportedTypes;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return conformanceClasses;
    }

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
                            if (member.getProcess() instanceof SystemType
                                    && absProcess instanceof AbstractMultiProcess) {
                                addAbstractMultiProcessValuesToSystem((SystemType) member.getProcess(),
                                        (AbstractMultiProcess) absProcess);
                            }
                        }
                    } else if (sensorDesc instanceof AbstractProcess) {
                        if (member.getProcess() instanceof SystemType && sensorDesc instanceof AbstractMultiProcess) {
                            addValuesToSystem((SystemType) member.getProcess(), (System) sensorDesc);
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
            addValuesToSystem(xbSystem, system);
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

    private void addValuesToSystem(SystemType xbSystem, System system) throws OwsExceptionReport {
        addAbstractMultiProcessValuesToSystem(xbSystem, system);
        // set position
        if (system.isSetPosition()) {
            xbSystem.setPosition(createPosition(system.getPosition()));
        }
    }

    private void addAbstractMultiProcessValuesToSystem(SystemType xbSystem, AbstractMultiProcess absProcess)
            throws OwsExceptionReport {
        // set identification
        if (absProcess.isSetIdentifications()) {
            xbSystem.setIdentificationArray(createIdentification(absProcess.getIdentifications()));
        }
        // set classification
        if (absProcess.isSetClassifications()) {
            xbSystem.setClassificationArray(createClassification(absProcess.getClassifications()));
        }
        // set characteristics
        if (absProcess.isSetCharacteristics()) {
            xbSystem.setCharacteristicsArray(createCharacteristics(absProcess.getCharacteristics()));
        }
        // set capabilities
        if (absProcess.isSetCapabilities()) {
            xbSystem.setCapabilitiesArray(createCapabilities(xbSystem, absProcess.getCapabilities()));
        }
        // set inputs
        if (absProcess.isSetInputs()) {
            xbSystem.setInputs(createInputs(absProcess.getInputs()));
        }
        // set outputs
        if (absProcess.isSetOutputs()) {
            xbSystem.setOutputs(createOutputs(absProcess.getOutputs()));
        }

        if (absProcess.isSetDocumentation()) {
            xbSystem.setDocumentationArray(createDocumentationArray(absProcess.getDocumentation()));
        }
        // set components
        if (absProcess.isSetComponents()) {
            Components components = createComponents(absProcess.getComponents());
            if (components != null && components.getComponentList() != null
                    && components.getComponentList().sizeOfComponentArray() > 0) {
                xbSystem.setComponents(createComponents(absProcess.getComponents()));
            }
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
        List<Characteristics> characteristicsList = new ArrayList<Characteristics>();
        for (SosSMLCharacteristics sosSMLCharacteristics : smlCharacteristics) {
            Characteristics xbCharacteristics =
                    Characteristics.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (sosSMLCharacteristics.getCharacteristicsType().equals(SweAggregateType.SimpleDataRecord)) {
                SimpleDataRecordType xbSimpleDataRecord =
                        (SimpleDataRecordType) xbCharacteristics.addNewAbstractDataRecord().substitute(
                                SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
                if (sosSMLCharacteristics.getTypeDefinition() != null
                        && !sosSMLCharacteristics.getTypeDefinition().isEmpty()) {
                    xbSimpleDataRecord.setDefinition(sosSMLCharacteristics.getTypeDefinition());
                }
                for (SosSweField field : sosSMLCharacteristics.getFields()) {
                    AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                    xbField.setName(field.getName());
                    addSweSimpleTypeToField(xbField, field.getElement());
                }
            } else if (sosSMLCharacteristics.getCharacteristicsType().equals(SweAggregateType.DataRecord)) {
                String exceptionText =
                        "The SWE characteristics type '" + SweAggregateType.DataRecord.name()
                                + "' is not supported by this SOS for SensorML characteristics!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            } else {
                String exceptionText =
                        "The SWE characteristics type '" + sosSMLCharacteristics.getCharacteristicsType().name()
                                + "' is not supported by this SOS for SensorML characteristics!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
            characteristicsList.add(xbCharacteristics);
        }
        return characteristicsList.toArray(new Characteristics[0]);
    }

    /**
     * Creates the capabilities section of the SensorML description.
     * 
     * @throws OwsExceptionReport
     */
    private Capabilities[] createCapabilities(SystemType xbSystem, List<SosSMLCapabilities> smlCapabilities)
            throws OwsExceptionReport {
        List<Capabilities> capabilitiesList = new ArrayList<Capabilities>();
        if (xbSystem != null || smlCapabilities != null) {
            if (isCapabilitiesArrayAlreadyAvailable(xbSystem)) {
                for (Capabilities capabilities : xbSystem.getCapabilitiesArray()) {
                    capabilitiesList.add(capabilities);
                }
            }
            for (SosSMLCapabilities capabilities : smlCapabilities) {
                if (capabilities != null) { // List could contain null elements
                    Capabilities xbCapabilities =
                            Capabilities.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                    if (capabilities.getName() != null) {
                        xbCapabilities.setName(capabilities.getName());
                    }
                    if (capabilities.getCapabilitiesType().equals(SweAggregateType.SimpleDataRecord)) {
                        SimpleDataRecordType xbSimpleDataRecord =
                                (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                                        SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
                        if (capabilities.getFields() != null) {
                            for (SosSweField field : capabilities.getFields()) {
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
                    } else if (capabilities.getCapabilitiesType().equals(SweAggregateType.DataRecord)) {
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

    private boolean isCapabilitiesArrayAlreadyAvailable(SystemType xbSystem) {
        return xbSystem.getCapabilitiesArray() != null && xbSystem.getCapabilitiesArray().length > 0;
    }

    private Documentation[] createDocumentationArray(List<AbstractSosSMLDocumentation> sosDocumentation) {
        List<Documentation> documentationList = new ArrayList<Documentation>();
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
        for (SosSweCoordinate coordinate : position.getPosition()) {
            if (coordinate.getValue().getValue() != null
                    && (!coordinate.getValue().isSetValue() || !coordinate.getValue().getValue()
                            .equals(Double.NaN))) {
                // FIXME: SWE Common NS
                IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE);
                if (encoder != null) {
                    xbVector.addNewCoordinate().set((XmlObject) encoder.encode(coordinate));
                } else {
                    String exceptionText =
                            "Error while encoding position for sensor description, needed encoder is missing!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
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
    private Inputs createInputs(List<SosSMLIo> inputs) throws OwsExceptionReport {
        Inputs xbInputs = Inputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        InputList xbInputList = xbInputs.addNewInputList();
        for (SosSMLIo sosSMLIo : inputs) {
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
    private Outputs createOutputs(List<SosSMLIo> sosOutputs) throws OwsExceptionReport {
        Outputs outputs = Outputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        OutputList outputList = outputs.addNewOutputList();
        for (SosSMLIo sosSMLIo : sosOutputs) {
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
            SosSweAbstractSimpleType sosSweSimpleType = (SosSweAbstractSimpleType) sosSweData;
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
                IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE);
                if (encoder != null) {
                    xbField.setText((Text) encoder.encode((SosSweText) sosSweData));
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
            IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE);
            if (encoder != null) {
                xbField.set((XmlObject) encoder.encode((SosSweDataArray) sosSweData));
            } else {
                String exceptionTextText = "The SweDataArray is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextText);
            }
        } else if (sosSweData instanceof SosSweDataRecord) {
            IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE);
            if (encoder != null) {
                xbField.set((XmlObject) encoder.encode((SosSweDataRecord) sosSweData));
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
    private void addIoComponentPropertyType(IoComponentPropertyType ioComponentPopertyType, SosSMLIo sosSMLIO)
            throws OwsExceptionReport {
        ioComponentPopertyType.setName(sosSMLIO.getIoName());
        switch (sosSMLIO.getIoValue().getSimpleType()) {
        case Boolean:
            String exceptionTextBool =
                    "The SWE simpleType '" + SweSimpleType.Boolean.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextBool);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextBool);
        case Category:
            String exceptionTextCategory =
                    "The SWE simpleType '" + SweSimpleType.Category.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextCategory);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextCategory);
        case Count:
            String exceptionTextCount =
                    "The SWE simpleType '" + SweSimpleType.Count.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextCount);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextCount);
        case CountRange:
            String exceptionTextCountRange =
                    "The SWE simpleType '" + SweSimpleType.CountRange.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextCountRange);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextCountRange);
        case ObservableProperty:
            ioComponentPopertyType.addNewObservableProperty().setDefinition(
                    ((SosSweObservableProperty) sosSMLIO.getIoValue()).getDefinition());
            break;
        case Quantity:
            // FIXME: SWE Common NS
            IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE);
            if (encoder != null) {
                ioComponentPopertyType.addNewQuantity().set(
                        (XmlObject) encoder.encode((SosSweQuantity) sosSMLIO.getIoValue()));
            } else {
                String exceptionTextText =
                        "The SWE simpleType '" + SweSimpleType.Quantity.name()
                                + "' is not supported by this SOS for SWE fields!";
                LOGGER.debug(exceptionTextText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextText);
            }
            break;
        case QuantityRange:
            String exceptionTextQuantityRange =
                    "The SWE simpleType '" + SweSimpleType.QuantityRange.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextQuantityRange);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextQuantityRange);
        case Text:
            String exceptionTextText =
                    "The SWE simpleType '" + SweSimpleType.Text.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextText);
        case Time:
            String exceptionTextTime =
                    "The SWE simpleType '" + SweSimpleType.Time.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextTime);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextTime);
        case TimeRange:
            String exceptionTextTimeRange =
                    "The SWE simpleType '" + SweSimpleType.TimeRange.name()
                            + "' is not supported by this SOS SensorML input/output!";
            LOGGER.debug(exceptionTextTimeRange);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionTextTimeRange);
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
