/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.List;

import net.opengis.sensorML.x101.AbstractProcessType;
import net.opengis.sensorML.x101.CapabilitiesDocument.Capabilities;
import net.opengis.sensorML.x101.CharacteristicsDocument.Characteristics;
import net.opengis.sensorML.x101.ClassificationDocument.Classification;
import net.opengis.sensorML.x101.ClassificationDocument.Classification.ClassifierList.Classifier;
import net.opengis.sensorML.x101.IdentificationDocument.Identification;
import net.opengis.sensorML.x101.IdentificationDocument.Identification.IdentifierList.Identifier;
import net.opengis.sensorML.x101.InputsDocument.Inputs;
import net.opengis.sensorML.x101.IoComponentPropertyType;
import net.opengis.sensorML.x101.OutputsDocument.Outputs;
import net.opengis.sensorML.x101.PositionDocument.Position;
import net.opengis.sensorML.x101.SensorMLDocument;
import net.opengis.sensorML.x101.SensorMLDocument.SensorML.Member;
import net.opengis.sensorML.x101.SystemDocument;
import net.opengis.sensorML.x101.SystemType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordPropertyType;
import net.opengis.swe.x101.SimpleDataRecordType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.filter.FilterConstants;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.System;
import org.n52.sos.ogc.sensorML.elements.SosSMLCapabilities;
import org.n52.sos.ogc.sensorML.elements.SosSMLCharacteristics;
import org.n52.sos.ogc.sensorML.elements.SosSMLClassifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.SWEConstants.SensorMLType;
import org.n52.sos.ogc.swe.SWEConstants.SweAggregateType;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Geometry;

public class SensorMLDecoderV101 implements IDecoder<AbstractSensorML, XmlObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorMLDecoderV101.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public SensorMLDecoderV101() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(SensorMLConstants.NS_SML));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.debug("Decoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public AbstractSensorML decode(XmlObject element) throws OwsExceptionReport {
        if (element instanceof SensorMLDocument) {
            return parseSensorML((SensorMLDocument) element);
        } else if (element instanceof SystemType) {
            return parseSystem((SystemType) element);
        } else {
            String exceptionText = "";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

    }

    private SensorML parseSensorML(SensorMLDocument xbSensorML) throws OwsExceptionReport {
        SensorML sensorML = new SensorML();
        sensorML.setSensorDescriptionXmlString(xbSensorML.xmlText());
        
        AbstractProcessType xbAbsProcessType = null;
        // get member process
        for (Member xbMember : xbSensorML.getSensorML().getMemberArray()) {
            if (xbMember.getProcess() == null) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "locator",
                        "The process of a member of the SensorML Document is null (" + xbMember.getProcess() + "))");
                LOGGER.error("The process of a member of the SensorML Document is null (" + xbMember.getProcess()
                        + "))");
                throw se;
            } else {
                xbAbsProcessType = xbMember.getProcess();
            }
        }
        if (xbAbsProcessType == null) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "locator",
                    "The process of a member of the SensorML Document is null (" + xbAbsProcessType + "))");
            LOGGER.error("The process of a member of the SensorML Document is null (" + xbAbsProcessType + "))");
            throw se;
        }
        // parse sensor description
        else if (xbAbsProcessType instanceof SystemType) {
            sensorML.addMember(parseSystem((SystemType) xbAbsProcessType));
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "locator",
                    "The process of a member of the SensorML Document (" + xbAbsProcessType
                            + ") is not of type 'SystemType'");
            LOGGER.error("M1 The process of a member of the SensorML Document (" + xbAbsProcessType
                    + ") is missing or not of type 'SystemType'");
            throw se;
        }

        return sensorML;
    }

    private System parseSystem(SystemType xbSystemType) throws OwsExceptionReport {
        System system = new System();
        if (xbSystemType.getIdentificationArray() != null) {
            system.setIdentifications(parseIdentification(xbSystemType.getIdentificationArray()));
        }
        if (xbSystemType.getClassificationArray() != null) {
            system.setClassifications(parseClassification(xbSystemType.getClassificationArray()));
        }
        if (xbSystemType.getCharacteristicsArray() != null) {
            system.setCharacteristics(parseCharacteristics(xbSystemType.getCharacteristicsArray()));
        }
        if (xbSystemType.getCapabilitiesArray() != null) {
            system.setCapabilities(parseCapabilities(xbSystemType.getCapabilitiesArray()));
        }
        if (xbSystemType.getPosition() != null) {
            system.setPosition(parsePosition(xbSystemType.getPosition()));
        }
        if (xbSystemType.getInputs() != null) {
            system.setInputs(parseInputs(xbSystemType.getInputs()));
        }
        if (xbSystemType.getOutputs() != null) {
            system.setOutputs(parseOutputs(xbSystemType.getOutputs()));
        }
        return system;
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
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(
                        OwsExceptionCode.InvalidParameterValue,
                        "Characteristics of SensorML",
                        "Error while parsing the characteristics of the SensorML (the characteristics' data record is not of type DataRecordPropertyType");
                LOGGER.error("Error while parsing the characteristics of the SensorML (the characteristics' data record is not of type DataRecordPropertyType");
                throw se;
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
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(
                        OwsExceptionCode.InvalidParameterValue,
                        "Capabilities of SensorML",
                        "Error while parsing the capabilities of the SensorML (the capabilities' data record is not of type DataRecordPropertyType");
                LOGGER.error("Error while parsing the capabilities of the SensorML (the capabilities' data record is not of type DataRecordPropertyType");
                throw se;
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
                    Configurator.getInstance().getDecoder(position.getPosition().getDomNode().getNamespaceURI());
            Object pos = null;
            for (IDecoder decoder : decoderList) {
                pos = decoder.decode(position.getPosition());
                if (pos != null) {
                    break;
                }
            }
            if (pos != null && pos instanceof SosSMLPosition) {
                sosSMLPosition = (SosSMLPosition) pos;
            }
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.NoApplicableCode, "SensorML - Position",
                    "Error while parsing the position of the SensorML Document (the position is not set)");
            LOGGER.error("Error while parsing the position of the SensorML Document (the position is not set)");
            throw se;
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
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type Boolean  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type Boolean  is not supported");
            throw se;
        } else if (xbIoCompPropType.isSetCategory()) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type Category  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type Category  is not supported");
            throw se;
        } else if (xbIoCompPropType.isSetCount()) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type Count  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type Count  is not supported");
            throw se;
        } else if (xbIoCompPropType.isSetCountRange()) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type CountRange  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type CountRange  is not supported");
            throw se;
        } else if (xbIoCompPropType.isSetObservableProperty()) {
            toEncode = xbIoCompPropType.getObservableProperty();
        } else if (xbIoCompPropType.isSetQuantity()) {
            toEncode = xbIoCompPropType.getQuantity();
        } else if (xbIoCompPropType.isSetQuantityRange()) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type Quantity Range  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type Quantity Range  is not supported");
            throw se;
        } else if (xbIoCompPropType.isSetText()) {
            toEncode = xbIoCompPropType.getText();
        } else if (xbIoCompPropType.isSetTime()) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type Time  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type Time  is not supported");
            throw se;
        } else if (xbIoCompPropType.isSetTimeRange()) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An \"IoComponentProperty\" of type Time Range  is not supported");
            LOGGER.error("An \"IoComponentProperty\" of type Time Range  is not supported");
            throw se;
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - IoComponentProperty",
                    "An requested \"IoComponentProperty\" is not supported: " + xbIoCompPropType.xmlText());
            LOGGER.error("An requested \"IoComponentProperty\" is not supported: " + xbIoCompPropType.xmlText());
            throw se;
        }
        
        List<IDecoder> decoderList =
                Configurator.getInstance().getDecoder(toEncode.getDomNode().getNamespaceURI());
        Object encodedObject = null;
        for (IDecoder decoder : decoderList) {
            encodedObject = decoder.decode(toEncode);
            if (encodedObject != null) {
                break;
            }
        }
        if (encodedObject != null && encodedObject instanceof ISosSweSimpleType) {
            sosIo.setIoValue((ISosSweSimpleType) encodedObject);
        }
        return sosIo;
    }

}
