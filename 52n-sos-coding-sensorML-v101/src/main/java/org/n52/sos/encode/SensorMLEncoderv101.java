package org.n52.sos.encode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.opengis.gml.MetaDataPropertyType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.ComponentsDocument.Components;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList;
import net.opengis.sensorML.x101.ComponentsDocument.Components.ComponentList.Component;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.InputsDocument.Inputs.InputList;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.OutputsDocument.Outputs.OutputList;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
import net.opengis.sensorML.x101.SystemDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.sensorML.x101.TermDocument.Term;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.VectorPropertyType;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SosSensorML;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SensorMLType;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SWEConstants.SweSimpleType;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Point;

public class SensorMLEncoderv101 implements IEncoder<XmlObject, Object> {

    /** the logger, used to log exceptions and additonaly information */
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLEncoderv101.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public SensorMLEncoderv101() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SensorMLConstants.NS_SML));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public XmlObject encode(Object response) throws OwsExceptionReport {
            return encode(response, null);
    }

    @Override
    public XmlObject encode(Object response, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (response instanceof SosSensorML) {
            return createSensor((SosSensorML) response);
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
    private SensorMLDocument createSensor(SosSensorML sensorDesc) throws OwsExceptionReport {

        SensorMLDocument xbSmlDoc = null;
        SystemDocument xbSystemDoc = null;
        // sensor description as a string
        if (sensorDesc != null) {
            if (sensorDesc.getSosSensorDescriptionType()
                    .equals(SWEConstants.SosSensorDescription.XmlStringDescription)) {
                // get SystemDocument
                try {
                    xbSmlDoc = SensorMLDocument.Factory.parse(sensorDesc.getSensorDescriptionString());
                    SensorML sml = xbSmlDoc.getSensorML();
                    SensorMLDocument.SensorML.Member[] memb = sml.getMemberArray();

                    xbSystemDoc = SystemDocument.Factory.parse(memb[0].toString());

                } catch (XmlException xmle) {
                    String exceptionText =
                            "Error while encoding SensorML description from stored SensorML encoded sensor description with XMLBeans!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                }
            }
            // sensor description as SOS internal representation.
            else if (sensorDesc.getSosSensorDescriptionType().equals(SWEConstants.SosSensorDescription.SosDescription)) {
                switch (sensorDesc.getSensorMLType()) {
                case System:
                    // xbSystemDoc =
                    // SystemDocument.Factory.newInstance(SosXmlOptionsUtility.getInstance()
                    // .getXmlOptions4Sos2Swe101());
                    xbSystemDoc = SystemDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                    SystemType xbSystem = xbSystemDoc.addNewSystem();
                    // set identification
                    if (sensorDesc.getIdentifications() != null && !sensorDesc.getIdentifications().isEmpty()) {
                        xbSystem.setIdentificationArray(createIdentification(sensorDesc.getIdentifications()));
                    }
                    // set classification
                    if (sensorDesc.getClassifications() != null && !sensorDesc.getClassifications().isEmpty()) {
                        xbSystem.setClassificationArray(createClassification(sensorDesc.getClassifications()));
                    }
                    // set characteristics
                    if (sensorDesc.getCharacteristics() != null) {
                        xbSystem.setCharacteristicsArray(createCharacteristics(sensorDesc.getCharacteristics()));
                    }
                    // set capabilities
                    if (sensorDesc.getCapabilities() != null) {
                        xbSystem.setCapabilitiesArray(createCapabilities(sensorDesc.getCapabilities()));
                    }
                    // set position
                    if (sensorDesc.getPosition() != null) {
                        xbSystem.setPosition(createPosition(sensorDesc.getPosition()));
                    }
                    // set inputs
                    if (sensorDesc.getInputs() != null && !sensorDesc.getInputs().isEmpty()) {
                        xbSystem.setInputs(createInputs(sensorDesc.getInputs()));
                    }
                    // set outputs
                    if (sensorDesc.getOutputs() != null && !sensorDesc.getOutputs().isEmpty()) {
                        xbSystem.setOutputs(createOutputs(sensorDesc.getOutputs()));
                    }
                    // set components
                    if (sensorDesc.getComponents() != null && !sensorDesc.getComponents().isEmpty()) {
                        xbSystem.setComponents(createComponents(sensorDesc.getComponents()));
                    }
                    break;
                case Component:
                    String exceptionText =
                            "The SensorML member '" + SensorMLType.Component.name()
                                    + "' is not supported by this service!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                case ProcessModel:
                    String exceptionText2 =
                            "The SensorML member '" + SensorMLType.ProcessModel.name()
                                    + "' is not supported by this service!";
                    LOGGER.debug(exceptionText2);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText2);
                case ProcessChain:
                    String exceptionText3 =
                            "The SensorML member '" + SensorMLType.ProcessChain.name()
                                    + "' is not supported by this service!";
                    LOGGER.debug(exceptionText3);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText3);
                default:
                    break;
                }
            } else {
                String exceptionText =
                        "The SOS internal sensor description type '" + sensorDesc.getSosSensorDescriptionType()
                                + "' is not supported by this SOS!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        }

        // xb_smlDoc.getSensorML().getMemberArray(0).set(xb_systemDoc);
        return xbSmlDoc;
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
        Identification xbIdentification = Identification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        IdentifierList xbIdentifierList = xbIdentification.addNewIdentifierList();
        for (SosSMLIdentifier sosSMLIdentifier : identifications) {
            Identifier xbIdentifier = xbIdentifierList.addNewIdentifier();
            xbIdentifier.setName(sosSMLIdentifier.getName());
            Term xbTerm = xbIdentifier.addNewTerm();
            xbTerm.setDefinition(sosSMLIdentifier.getDefinition());
            xbTerm.setValue(sosSMLIdentifier.getValue());
        }
        Identification[] identificationArray = {xbIdentification};
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
        Classification xbClassification = Classification.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ClassifierList xbClassifierList = xbClassification.addNewClassifierList();
        for (SosSMLClassifier sosSMLClassifier : classifications) {
            Classifier xbClassifier = xbClassifierList.addNewClassifier();
            xbClassifier.setName(sosSMLClassifier.getName());
            Term xbTerm = xbClassifier.addNewTerm();
            xbTerm.setValue(sosSMLClassifier.getValue());
        }
        Classification[] classificationArray = {xbClassification};
        return classificationArray;
    }

    /**
     * Creates the characteristics section of the SensorML description.
     * 
     * @param xbCharacteristics
     *            Xml characteristics object
     * @param characteristics
     *            SOS SWE representation.
     * @throws OwsExceptionReport
     */
    private Characteristics[] createCharacteristics(SosSMLCharacteristics characteristics)
            throws OwsExceptionReport {
        Characteristics xbCharacteristics = Characteristics.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (characteristics.getCharacteristicsType().equals(SweAggregateType.SimpleDataRecord)) {
            SimpleDataRecordType xbSimpleDataRecord =
                    (SimpleDataRecordType) xbCharacteristics.addNewAbstractDataRecord().substitute(
                            SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
            if (characteristics.getTypeDefinition() != null && !characteristics.getTypeDefinition().isEmpty()) {
                xbSimpleDataRecord.setDefinition(characteristics.getTypeDefinition());
            }
            for (SosSweField field : characteristics.getFields()) {
                AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                xbField.setName(field.getName());
                addSweSimpleTypeToField(xbField, field.getElement());
            }
        } else if (characteristics.getCharacteristicsType().equals(SweAggregateType.DataRecord)) {
            String exceptionText =
                    "The SWE characteristics type '" + SweAggregateType.DataRecord.name()
                            + "' is not supported by this SOS for SensorML characteristics!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        } else {
            String exceptionText =
                    "The SWE characteristics type '" + characteristics.getCharacteristicsType().name()
                            + "' is not supported by this SOS for SensorML characteristics!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        Characteristics[] characteristicsArray = {xbCharacteristics};
        return characteristicsArray;
    }

    /**
     * Creates the capabilities section of the SensorML description.
     * 
     * @param xbCapabilities
     *            Xml capabilities object
     * @param capabilities
     *            SOS SWE representation.
     * @throws OwsExceptionReport
     */
    private Capabilities[] createCapabilities(SosSMLCapabilities capabilities)
            throws OwsExceptionReport {
        Capabilities xbCapabilities = Capabilities.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (capabilities.getCapabilitiesType().equals(SweAggregateType.SimpleDataRecord)) {
            SimpleDataRecordType xbSimpleDataRecord =
                    (SimpleDataRecordType) xbCapabilities.addNewAbstractDataRecord().substitute(
                            SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
            for (SosSweField field : capabilities.getFields()) {
                AnyScalarPropertyType xbField = xbSimpleDataRecord.addNewField();
                xbField.setName(field.getName());
                addSweSimpleTypeToField(xbField, field.getElement());
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
        Capabilities[] capabilitiesArray = {xbCapabilities};
        return capabilitiesArray;
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
                    && (!coordinate.getValue().getValue().isEmpty() || !coordinate.getValue().getValue()
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
        List<IoComponentPropertyType> inputList = new ArrayList<IoComponentPropertyType>();
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
    private Outputs createOutputs(List<SosSMLIo> outputs) throws OwsExceptionReport {
        Outputs xbOutputs = Outputs.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        OutputList xbOutputList = xbOutputs.addNewOutputList();
        for (SosSMLIo sosSMLIo : outputs) {
            addIoComponentPropertyType(xbOutputList.addNewOutput(), sosSMLIo);
        }
        return xbOutputs;
    }

    /**
     * Creates the components section of the SensorML description.
     * 
     * @param xbComponents
     *            Xml components object
     * @param sosComponents
     *            SOS SWE representation.
     */
    private Components createComponents(List<SosSMLComponent> sosComponents) {
        Components xbComponents = Components.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ComponentList xbComList = xbComponents.addNewComponentList();
        for (SosSMLComponent sosSMLComponent : sosComponents) {
            xbComList.addNewComponent().setName(sosSMLComponent.getIdentifier());
        }
        return xbComponents;
    }

    /**
     * Adds a SOS SWE simple type to a XML SWE field.
     * @param iSosSweSimpleType 
     * @param xbField 
     * 
     * @param xbField
     *            XML SWE field
     * @param sosSweSimpleType
     *            SOS SWE simple type.
     * @throws OwsExceptionReport
     */
    private void addSweSimpleTypeToField(AnyScalarPropertyType xbField, ISosSweSimpleType sosSweSimpleType)
            throws OwsExceptionReport {
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
                xbField.setText((Text)encoder.encode((SosSweText) sosSweSimpleType));
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
    }

    /**
     * Adds a SOS SWE simple type to a XML SML IO component.
     * 
     * @param xbIoComponentPopertyType
     *            SML IO component
     * @param sosSMLInput
     *            SOS SWE simple type.
     * @throws OwsExceptionReport
     */
    private void addIoComponentPropertyType(IoComponentPropertyType xbIoComponentPopertyType, SosSMLIo sosSMLIO)
            throws OwsExceptionReport {
        xbIoComponentPopertyType.setName(sosSMLIO.getIoName());
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
            xbIoComponentPopertyType.addNewObservableProperty().setDefinition(
                    ((SosSweObservableProperty) sosSMLIO.getIoValue()).getDefinition());
            break;
        case Quantity:
            // FIXME: SWE Common NS
            IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE);
            if (encoder != null) {
                xbIoComponentPopertyType.addNewQuantity().set((XmlObject)encoder.encode(
                        (SosSweQuantity) sosSMLIO.getIoValue()));
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

    /**
     * creates swe:Position element from passed JTS Point
     * 
     * @param point
     *            JTS point containing the coords for swe:POsition
     * @return Returns XMLBeans representation of swe:Position
     */
    // public PositionDocument createPosition(Point point) {
    private Position createPosition(Point point) {

        Position xb_pos = Position.Factory.newInstance();
        PositionType xb_posType = xb_pos.addNewPosition();

        xb_posType.setReferenceFrame(Configurator.getInstance().getSrsNamePrefixSosV2() + point.getSRID());

        VectorPropertyType xb_location = xb_posType.addNewLocation();
        VectorType xb_vector = xb_location.addNewVector();

        Coordinate xb_coord = xb_vector.addNewCoordinate();
        xb_coord.setName("xcoord");
        Quantity xb_quantity = xb_coord.addNewQuantity();
        xb_quantity.setValue(point.getX());

        xb_coord = xb_vector.addNewCoordinate();
        xb_coord.setName("ycoord");
        xb_quantity = xb_coord.addNewQuantity();
        xb_quantity.setValue(point.getY());

        // return xb_posDoc;
        return xb_pos;
    }

    /**
     * Add parent procedures to a SystemDocument
     * 
     * @param xb_systemDoc
     *            System document to add parent procedures to
     * @param parentProcedureIds
     *            The parent procedures to add
     * @throws OwsExceptionReport
     */
    protected void addParentProcedures(SystemDocument xb_systemDoc, Collection<String> parentProcedureIds)
            throws OwsExceptionReport {
        if (parentProcedureIds != null && !parentProcedureIds.isEmpty()) {
            Capabilities xb_cap = xb_systemDoc.getSystem().addNewCapabilities();
            xb_cap.setName(SosConstants.SYS_CAP_PARENT_PROCEDURES_NAME);
            SimpleDataRecordType xb_sdr =
                    (SimpleDataRecordType) xb_cap.addNewAbstractDataRecord().substitute(
                            SWEConstants.QN_SIMPLEDATARECORD_SWE_101, SimpleDataRecordType.type);
            String urlPattern =
                    SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                            SosConstants.Operations.DescribeSensor.name(), new DecoderKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION));
            for (String parentProcedureId : parentProcedureIds) {
                MetaDataPropertyType xb_metaData = xb_sdr.addNewMetaDataProperty();
                xb_metaData.setTitle(parentProcedureId);
                // TODO: add parentProcedure xlink:role? it needs to be a URL(?)
                try {
                    xb_metaData.setHref(SosHelper.getDescribeSensorUrl(Sos2Constants.SERVICEVERSION, Configurator
                            .getInstance().getServiceURL(), parentProcedureId, urlPattern));
                } catch (UnsupportedEncodingException uee) {
                    String exceptionText = "Error while encoding DescribeSensor URL";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(uee, exceptionText);
                }
            }
        }
    }

    /**
     * Add a collection of child procedures to a SystemDocument
     * 
     * @param xb_systemDoc
     *            System document to add child procedures to
     * @param childProcedures
     *            The child procedures to add
     * @throws OwsExceptionReport
     */
    protected void addChildProcedures(SystemDocument xb_systemDoc, Map<String, SosSensorML> childProcedures)
            throws OwsExceptionReport {
        if (childProcedures != null && !childProcedures.isEmpty()) {
            Components xb_components = xb_systemDoc.getSystem().getComponents();
            if (xb_components == null) {
                xb_components = xb_systemDoc.getSystem().addNewComponents();
            }

            ComponentList xb_componentList = xb_components.getComponentList();
            if (xb_componentList == null) {
                xb_componentList = xb_components.addNewComponentList();
            }

            int childCount = 0;
            List<String> childProcedureIds = new ArrayList<String>(childProcedures.keySet());
            Collections.sort(childProcedureIds);

            String urlPattern =
                    SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                            SosConstants.Operations.DescribeSensor.name(), new DecoderKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION));
            for (String childProcedureId : childProcedureIds) {
                childCount++;
                Component xb_component = xb_componentList.addNewComponent();
                xb_component.setName("component" + childCount);
                xb_component.setTitle(childProcedureId);
                // TODO: add childProcedure xlink:role? it needs to be a URL(?)
                try {
                    xb_component.setHref(SosHelper.getDescribeSensorUrl(Sos2Constants.SERVICEVERSION, Configurator
                            .getInstance().getServiceURL(), childProcedureId, urlPattern));
                } catch (UnsupportedEncodingException uee) {
                    String exceptionText = "Error while encoding DescribeSensor URL";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(uee, exceptionText);
                }

                if (Configurator.getInstance().isChildProceduresEncodedInParentsDescribeSensor()) {
                    SosSensorML childProcSensorDesc = childProcedures.get(childProcedureId);
                    SystemDocument xb_childSystemDoc = null;
                    SensorMLDocument xb_childSmlDoc = null;

                    try {
                        xb_childSmlDoc =
                                SensorMLDocument.Factory.parse(childProcSensorDesc.getSensorDescriptionString());
                        SensorML xb_childSml = xb_childSmlDoc.getSensorML();
                        xb_childSystemDoc = SystemDocument.Factory.parse(xb_childSml.getMemberArray()[0].toString());
                    } catch (XmlException xmle) {
                        String exceptionText =
                                "Error while encoding SensorML child procedure description from stored SensorML"
                                        + " encoded sensor description with XMLBeans";
                        LOGGER.debug(exceptionText);
                        throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                    }

                    xb_component.setProcess(xb_childSystemDoc.getSystem());
                    xb_component.getProcess().substitute(SensorMLConstants.SYSTEM_QNAME, SystemType.type);
                }
            }
        }
    }

}
