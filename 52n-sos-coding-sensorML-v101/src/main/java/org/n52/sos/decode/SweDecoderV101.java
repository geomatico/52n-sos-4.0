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

import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.CountDocument.Count;
import net.opengis.swe.x101.DataArrayDocument;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.simpleType.ISosSweSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuality;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweDecoderV101 implements IDecoder<Object, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SweDecoderV101.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public SweDecoderV101() {
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
    public Object decode(Object element) throws OwsExceptionReport {
        if (element instanceof DataArrayDocument) {
            return parseSweDataArray((DataArrayDocument) element);
        } else if (element instanceof DataComponentPropertyType[]) {
            return parseDataRecordFieldArray((DataComponentPropertyType[]) element);
        } else if (element instanceof Count) {
            return parseCount((Count) element);
        } else if (element instanceof Quantity) {
            return parseQuantity((Quantity) element);
        } else if (element instanceof Text) {
            return parseText((Text) element);
        } else if (element instanceof ObservableProperty) {
            return parseObservableProperty((ObservableProperty) element);
        } else if (element instanceof PositionType) {
            return parsePosition((PositionType) element);
        } else if (element instanceof Coordinate[]) {
            return parseCoordinates((Coordinate[]) element);
        } else if (element instanceof AnyScalarPropertyType[]) {
            return parseSimpleDataRecordFieldArray((AnyScalarPropertyType[]) element);
        }
        return null;
    }

    private SosSweDataArray parseSweDataArray(DataArrayDocument xbDataArray) throws OwsExceptionReport {
        if (xbDataArray instanceof DataArrayDocument) {

        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                    "Error when parsing the SweDataArray: It is not of type DataArrayDocument");
            LOGGER.error("Error when parsing the SweDataArray: It is not of type DataArrayDocument");
            throw se;
        }
        return null;
    }

    private List<SosSweField> parseDataRecordFieldArray(DataComponentPropertyType[] fieldArray)
            throws OwsExceptionReport {
        List<SosSweField> sosFields = new ArrayList<SosSweField>();
        for (DataComponentPropertyType xbField : fieldArray) {
            if (xbField.isSetBoolean()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                throw se;
            }
            if (xbField.isSetBoolean()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                throw se;
            }
            if (xbField.isSetCategory()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Category");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Category");
                throw se;
            }
            if (xbField.isSetCount()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Count");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Count");
                throw se;
            }
            if (xbField.isSetCountRange()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type CountRange");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Count Range");
                throw se;
            }
            if (xbField.isSetQuantity()) {
                sosFields.add(new SosSweField(xbField.getName(), parseQuantity(xbField.getQuantity())));
            }
            if (xbField.isSetQuantityRange()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Quantity Range");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Quantity Range");
                throw se;
            }
            if (xbField.isSetText()) {
                sosFields.add(new SosSweField(xbField.getName(), parseText(xbField.getText())));
            }
            if (xbField.isSetTime()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Time");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Time");
                throw se;
            }
        }
        return sosFields;
    }

    private ISosSweSimpleType parseQuantity(Quantity xbQuantity) {
        SosSweQuantity sosQuantity = new SosSweQuantity();
        if (xbQuantity.isSetAxisID()) {
            sosQuantity.setAxisID(xbQuantity.getAxisID());
        }
        if (xbQuantity.isSetDefinition()) {
            sosQuantity.setDefinition(xbQuantity.getDefinition());
        }
        if (xbQuantity.isSetDescription()) {
            sosQuantity.setDescription(xbQuantity.getDescription().getStringValue());
        }
        if (xbQuantity.getQualityArray() != null) {
            sosQuantity.setQuality(parseQuality(xbQuantity.getQualityArray()));
        }
        if (xbQuantity.isSetUom()) {
            sosQuantity.setUom(xbQuantity.getUom().getCode());
        }
        if (xbQuantity.isSetValue()) {
            sosQuantity.setValue(Double.toString(xbQuantity.getValue()));
        }
        return sosQuantity;
    }

    private ISosSweSimpleType parseText(Text xbText) {
        SosSweText sosText = new SosSweText();
        if (xbText.isSetDefinition()) {
            sosText.setDefinition(xbText.getDefinition());
        }
        if (xbText.isSetDescription()) {
            sosText.setDescription(xbText.getDescription().getStringValue());
        }
        if (xbText.isSetValue()) {
            sosText.setValue(xbText.getValue());
        }
        return sosText;
    }

    private SosSweQuality parseQuality(XmlObject[] qualityArray) {
        return new SosSweQuality();
    }

    private ISosSweSimpleType parseObservableProperty(ObservableProperty observableProperty) {
        ObservableProperty xbObsProp = (ObservableProperty) observableProperty;
        SosSweText sosObservableProperty = new SosSweText();
        if (xbObsProp.isSetDefinition()) {
            sosObservableProperty.setDefinition(xbObsProp.getDefinition());
        }
        return sosObservableProperty;
    }

    private SosSMLPosition parsePosition(PositionType position) throws OwsExceptionReport {
        SosSMLPosition sosSMLPosition = new SosSMLPosition();
        if (position.isSetLocation()) {
            if (position.getLocation().isSetVector()) {
                if (position.getLocation().getVector().isSetReferenceFrame()) {
                    sosSMLPosition.setReferenceFrame(position.getLocation().getVector().getReferenceFrame());
                }
                sosSMLPosition.setPosition(parseCoordinates(position.getLocation().getVector().getCoordinateArray()));
            }
        }
        return sosSMLPosition;
    }

    private List<SosSweCoordinate> parseCoordinates(Coordinate[] coordinateArray) throws OwsExceptionReport {
        List<SosSweCoordinate> sosCoordinates = new ArrayList<SosSweCoordinate>();
        for (Coordinate xbCoordinate : coordinateArray) {
            if (xbCoordinate.isSetQuantity()) {
                sosCoordinates.add(new SosSweCoordinate(checkCoordinateName(xbCoordinate.getName()),
                        parseQuantity(xbCoordinate.getQuantity())));
            } else {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - Position",
                        "Error when parsing the DataRecordFieldArray of coordinates: It must not be of type Quantity");
                LOGGER.error("Error when parsing the DataRecordFieldArray of coordinates: It must not be of type Quantity");
                throw se;
            }
        }
        return null;
    }

    private SweCoordinateName checkCoordinateName(String name) throws OwsExceptionReport {
        if (name.equals(SweCoordinateName.easting.name())) {
            return SweCoordinateName.easting;
        } else if (name.equals(SweCoordinateName.northing.name())) {
            return SweCoordinateName.northing;
        } else if (name.equals(SweCoordinateName.altitude.name())) {
            return SweCoordinateName.altitude;
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "SensorML - Position",
                    "The coordinate name is neighter 'easting' nor 'northing' nor 'altitude'");
            LOGGER.error("The coordinate name is neighter 'easting' nor 'northing' nor 'altitude'");
            throw se;
        }
    }

    private List<SosSweField> parseSimpleDataRecordFieldArray(AnyScalarPropertyType[] fieldArray)
            throws OwsExceptionReport {
        List<SosSweField> sosFields = new ArrayList<SosSweField>();
        for (AnyScalarPropertyType xbField : (AnyScalarPropertyType[]) fieldArray) {
            if (xbField.isSetBoolean()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                throw se;
            }
            if (xbField.isSetBoolean()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Boolean");
                throw se;
            }
            if (xbField.isSetCategory()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Category");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Category");
                throw se;
            }
            if (xbField.isSetCount()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Count");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Count");
                throw se;
            }
            if (xbField.isSetQuantity()) {
                sosFields.add(new SosSweField(xbField.getName(), parseQuantity(xbField.getQuantity())));
            }
            if (xbField.isSetText()) {
                sosFields.add(new SosSweField(xbField.getName(), parseText(xbField.getText())));
            }
            if (xbField.isSetTime()) {
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.InvalidParameterValue, "Observation-Result",
                        "Error when parsing the DataRecordFieldArray: It must not be of type Time");
                LOGGER.error("Error when parsing the DataRecordFieldArray: It must not be of type Time");
                throw se;
            }
        }
        return sosFields;
    }

    private ISosSweSimpleType parseCount(XmlObject xbCount) {
        return null;
    }

}
