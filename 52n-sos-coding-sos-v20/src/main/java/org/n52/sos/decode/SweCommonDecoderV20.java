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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.AbstractEncodingType;
import net.opengis.swe.x20.AnyScalarPropertyType;
import net.opengis.swe.x20.CountRangeType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayDocument;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.EncodedValuesPropertyType;
import net.opengis.swe.x20.QuantityRangeType;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextEncodingDocument;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeRangeType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.VectorType.Coordinate;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweQuality;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweCommonDecoderV20 implements IDecoder<Object, Object> {
    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonDecoderV20.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public SweCommonDecoderV20() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(SWEConstants.NS_SWE_20));
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
            DataArrayDocument dataArrayDoc = (DataArrayDocument) element;
            SosSweDataArray sosDataArray = parseDataArray(dataArrayDoc.getDataArray1());
            sosDataArray.setXml(dataArrayDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
            return sosDataArray;
        } else if (element instanceof DataArrayType) {
            DataArrayType dataArrayType = (DataArrayType) element;
            SosSweDataArray sosDataArray = parseDataArray(dataArrayType);
            return sosDataArray;
        } else if (element instanceof DataRecordDocument) {
            DataRecordDocument dataRecordDoc = (DataRecordDocument) element;
            SosSweDataRecord sosDataRecord = parseDataRecord(dataRecordDoc.getDataRecord());
            sosDataRecord.setXml(dataRecordDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
            return sosDataRecord;
        } else if (element instanceof DataRecordType) {
            DataRecordType dataRecord = (DataRecordType) element;
            SosSweDataRecord sosDataRecord = parseDataRecord(dataRecord);
            DataRecordDocument dataRecordDoc =
                    DataRecordDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            dataRecordDoc.setDataRecord(dataRecord);
            sosDataRecord.setXml(dataRecordDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
            return sosDataRecord;
        } else if (element instanceof CountType) {
            return parseCount((CountType) element);
        } else if (element instanceof QuantityType) {
            return parseQuantity((QuantityType) element);
        } else if (element instanceof TextType) {
            return parseText((TextType) element);
        } else if (element instanceof Coordinate[]) {
            return parseCoordinates((Coordinate[]) element);
        } else if (element instanceof AnyScalarPropertyType[]) {
            return parseSimpleDataRecordFieldArray((AnyScalarPropertyType[]) element);
        } else if (element instanceof TextEncodingDocument) {
            TextEncodingDocument textEncodingDoc = (TextEncodingDocument) element;
            SosSweTextEncoding sosTextEncoding = parseTextEncoding(textEncodingDoc.getTextEncoding());
            sosTextEncoding.setXml(textEncodingDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
            return sosTextEncoding;
        } else if (element instanceof TextEncodingType) {
            TextEncodingDocument textEncodingDoc =
                    TextEncodingDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            TextEncodingType textEncoding = (TextEncodingType) element;
            textEncodingDoc.setTextEncoding(textEncoding);
            SosSweTextEncoding sosTextEncoding = parseTextEncoding(textEncoding);
            sosTextEncoding.setXml(textEncodingDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
            return sosTextEncoding;
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested element");
            if (element instanceof XmlObject) {
                exceptionText.append(" '");
                exceptionText.append(((XmlObject) element).getDomNode().getLocalName());
                exceptionText.append("' ");
            }
            exceptionText.append("is not supported by this server!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    private SosSweDataArray parseDataArray(DataArrayType xbDataArray) throws OwsExceptionReport {
        SosSweDataArray sosSweDataArray = new SosSweDataArray();
        sosSweDataArray.setDefinition(xbDataArray.getDefinition());
        sosSweDataArray.setDescription(xbDataArray.getDescription());
        sosSweDataArray.setElementCount(parseCount(xbDataArray.getElementCount().getCount()));
        
        // parse data record to elementType
        DataArrayType.ElementType xbElementType = xbDataArray.getElementType();
        if (xbElementType != null && xbElementType.getAbstractDataComponent() != null)
        {
            sosSweDataArray.setElementType(parseAbstractDataComponent(xbElementType.getAbstractDataComponent()));
        }

        sosSweDataArray.setEncoding(parseEncoding(xbDataArray.getEncoding().getAbstractEncoding()));
        
        // parse values
        if (xbDataArray.isSetValues())
        {
            sosSweDataArray.setValues(parseValues(sosSweDataArray.getElementCount(),
                    sosSweDataArray.getElementType(),
                    sosSweDataArray.getEncoding(),
                    xbDataArray.getValues()));
        }
        // set XML
        DataArrayDocument dataArrayDoc =
                DataArrayDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        dataArrayDoc.setDataArray1(xbDataArray);
        sosSweDataArray.setXml(dataArrayDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
        return sosSweDataArray;
    }

    private List<List<String>> parseValues(SosSweCount elementCount,
            SosSweAbstractDataComponent elementType,
            SosSweAbstractEncoding encoding,
            EncodedValuesPropertyType encodedValuesPropertyType) throws OwsExceptionReport
    {
        assert elementCount != null;
        assert elementType != null;
        assert encoding != null;
        if (checkParameterTypes(elementType, encoding))
        {
            // Get swe values String via cursor as String
            String values = null;
            // TODO replace XmlCursor
            /* if (encodedValuesPropertyType.schemaType() == XmlString.type)
            {
                XmlString xbString */
            // @see SosDecoderv20#parseResultValues
            XmlCursor xbCursor = encodedValuesPropertyType.newCursor();
            xbCursor.toFirstContentToken();
            if (xbCursor.isText())
            {
                values = xbCursor.getTextValue().trim();
                xbCursor.dispose();
                if (values != null && !values.isEmpty())
                {
                    SosSweTextEncoding textEncoding = (SosSweTextEncoding)encoding;
                    
                    String[] blocks = values.split(textEncoding.getBlockSeparator());
                    List<List<String>> resultValues = new ArrayList<List<String>>();
                    for (String block : blocks)
                    {
                        String[] tokens = block.split(textEncoding.getTokenSeparator());
                        List<String> tokenList = Arrays.asList(tokens);
                        resultValues.add(tokenList);
                    }
                    return resultValues;
                }
            }
        }
        assert false;
        return null;
    }

    private boolean checkParameterTypes(SosSweAbstractDataComponent elementType,
            SosSweAbstractEncoding encoding) throws OwsExceptionReport
    {
        if (!(encoding instanceof SosSweTextEncoding))
        {
            String exceptionMsg = String.format("Received encoding type \"%s\" of swe:values is not supported.",
                    encoding!=null?encoding.getClass().getName():encoding);
            LOGGER.debug(exceptionMsg);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
        }
        if (!(elementType instanceof SosSweDataRecord))
        {
            String exceptionMsg = String.format("Received elementType \"%s\" in combination with swe:values is not supported.",
                    elementType!=null?elementType.getClass().getName():elementType);
            LOGGER.debug(exceptionMsg);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
        }
        return true;
    }

    private SosSweAbstractEncoding parseEncoding(AbstractEncodingType abstractEncodingType) throws OwsExceptionReport
    {
        assert abstractEncodingType != null;
        if (abstractEncodingType instanceof TextEncodingType)
        {
            return parseTextEncoding((TextEncodingType) abstractEncodingType);
        }
        String exceptionMsg = String.format("Encoding type not supported: %s. Currently supported: %s",
                abstractEncodingType!=null?abstractEncodingType.getClass().getName():abstractEncodingType,
                        TextEncodingType.type.getName());
        LOGGER.debug(exceptionMsg);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
    }

    private SosSweDataRecord parseDataRecord(DataRecordType dataRecord) throws OwsExceptionReport {
        SosSweDataRecord sosSweDataRecord = new SosSweDataRecord();
        for (Field field : dataRecord.getFieldArray()) {
            sosSweDataRecord.addField(new SosSweField(field.getName(), parseAbstractDataComponent(field
                    .getAbstractDataComponent())));
        }
        return sosSweDataRecord;
    }

    // private List<SosSweField>
    // parseDataRecordFieldArray(DataComponentPropertyType[] fieldArray)
    // throws OwsExceptionReport {
    // List<SosSweField> sosFields = new ArrayList<SosSweField>();
    // for (DataComponentPropertyType xbField : fieldArray) {
    // if (xbField.isSetBoolean()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseBoolean(xbField.getBoolean())));
    // } else if (xbField.isSetCategory()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseCategory(xbField.getCategory())));
    // } else if (xbField.isSetCount()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseCount(xbField.getCount())));
    // } else if (xbField.isSetCountRange()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseCountRange(xbField.getCountRange())));
    // } else if (xbField.isSetQuantity()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseQuantity(xbField.getQuantity())));
    // } else if (xbField.isSetQuantityRange()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseQuantityRange(xbField.getQuantityRange())));
    // } else if (xbField.isSetText()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseText(xbField.getText())));
    // } else if (xbField.isSetTime()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseTime(xbField.getTime())));
    // } else if (xbField.isSetTimeRange()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseTimeRange(xbField.getTimeRange())));
    // }
    // }
    // return sosFields;
    // }

    private SosSweAbstractDataComponent parseAbstractDataComponent(AbstractDataComponentType abstractDataComponent)
            throws OwsExceptionReport {
        if (abstractDataComponent instanceof CountType) {
            return parseCount((CountType) abstractDataComponent);
        } else if (abstractDataComponent instanceof QuantityType) {
            return parseQuantity((QuantityType) abstractDataComponent);
        } else if (abstractDataComponent instanceof TimeType) {
            return parseTime((TimeType) abstractDataComponent);
        } else if (abstractDataComponent instanceof DataArrayDocument) {
            return parseDataArray(((DataArrayDocument) abstractDataComponent).getDataArray1());
        } else if (abstractDataComponent instanceof DataRecordType) {
            return parseDataRecord((DataRecordType) abstractDataComponent);
        }
        return null;
    }

    // private List<SosSweField>
    // parseDataRecordFieldArray(DataComponentPropertyType[] fieldArray)
    // throws OwsExceptionReport {
    // List<SosSweField> sosFields = new ArrayList<SosSweField>();
    // for (DataComponentPropertyType xbField : fieldArray) {
    // if (xbField.isSetBoolean()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseBoolean(xbField.getBoolean())));
    // } else if (xbField.isSetCategory()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseCategory(xbField.getCategory())));
    // } else if (xbField.isSetCount()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseCount(xbField.getCount())));
    // } else if (xbField.isSetCountRange()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseCountRange(xbField.getCountRange())));
    // } else if (xbField.isSetQuantity()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseQuantity(xbField.getQuantity())));
    // } else if (xbField.isSetQuantityRange()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseQuantityRange(xbField.getQuantityRange())));
    // } else if (xbField.isSetText()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseText(xbField.getText())));
    // } else if (xbField.isSetTime()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseTime(xbField.getTime())));
    // } else if (xbField.isSetTimeRange()) {
    // sosFields.add(new SosSweField(xbField.getName(),
    // parseTimeRange(xbField.getTimeRange())));
    // }
    // }
    // return sosFields;
    // }
    private SosSweBoolean parseBoolean(XmlObject xbBoolean) throws OwsExceptionReport {
        String exceptionText = "The Boolean is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType parseCategory(XmlObject xbCategory) throws OwsExceptionReport {
        String exceptionText = "The Category is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweCount parseCount(CountType count) {
        SosSweCount sosCount = new SosSweCount();
        if (count.isSetDefinition()) {
            sosCount.setDefinition(count.getDefinition());
        }
        if (count.isSetDescription()) {
            sosCount.setDescription(count.getDescription());
        }
        if (count.getQualityArray() != null) {
            sosCount.setQuality(parseQuality(count.getQualityArray()));
        }
        if (count.isSetValue()) {
            sosCount.setValue(Integer.toString(count.getValue().intValue()));
        }
        return sosCount;
    }

    private SosSweAbstractSimpleType parseCountRange(CountRangeType countRange) throws OwsExceptionReport {
        String exceptionText = "The CountRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    // private SosSweAbstractSimpleType parseObservableProperty(ObservableProperty
    // observableProperty) {
    // ObservableProperty xbObsProp = (ObservableProperty) observableProperty;
    // SosSweText sosObservableProperty = new SosSweText();
    // if (xbObsProp.isSetDefinition()) {
    // sosObservableProperty.setDefinition(xbObsProp.getDefinition());
    // }
    // return sosObservableProperty;
    // }

    private SosSweAbstractSimpleType parseQuantity(QuantityType xbQuantity) {
        SosSweQuantity sosQuantity = new SosSweQuantity();
        if (xbQuantity.isSetAxisID()) {
            sosQuantity.setAxisID(xbQuantity.getAxisID());
        }
        if (xbQuantity.isSetDefinition()) {
            sosQuantity.setDefinition(xbQuantity.getDefinition());
        }
        if (xbQuantity.isSetDescription()) {
            sosQuantity.setDescription(xbQuantity.getDescription());
        }
        if (xbQuantity.getQualityArray() != null) {
            sosQuantity.setQuality(parseQuality(xbQuantity.getQualityArray()));
        }
        sosQuantity.setUom(xbQuantity.getUom().getCode());
        if (xbQuantity.isSetValue()) {
            sosQuantity.setValue(Double.toString(xbQuantity.getValue()));
        }
        return sosQuantity;
    }

    private SosSweAbstractSimpleType parseQuantityRange(QuantityRangeType quantityRange) throws OwsExceptionReport {
        String exceptionText = "The QuantityRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType parseText(TextType xbText) {
        SosSweText sosText = new SosSweText();
        if (xbText.isSetDefinition()) {
            sosText.setDefinition(xbText.getDefinition());
        }
        if (xbText.isSetDescription()) {
            sosText.setDescription(xbText.getDescription());
        }
        if (xbText.isSetValue()) {
            sosText.setValue(xbText.getValue());
        }
        return sosText;
    }

    private SosSweAbstractSimpleType parseTime(TimeType time) {
        SosSweTime sosTime = new SosSweTime();
        if (time.isSetDefinition()) {
            sosTime.setDefinition(time.getDefinition());
        }
        if (time.isSetDescription()) {
            sosTime.setDescription(time.getDescription());
        }
        if (time.isSetValue()) {
            sosTime.setValue(time.getValue().toString());
        }
        return sosTime;
    }

    private SosSweAbstractSimpleType parseTimeRange(TimeRangeType timeRange) throws OwsExceptionReport {
        String exceptionText = "The TimeRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweQuality parseQuality(XmlObject[] qualityArray) {
        return new SosSweQuality();
    }

    // private SosSMLPosition parsePosition(PositionType position) throws
    // OwsExceptionReport {
    // SosSMLPosition sosSMLPosition = new SosSMLPosition();
    // if (position.isSetLocation()) {
    // if (position.getLocation().isSetVector()) {
    // if (position.getLocation().getVector().isSetReferenceFrame()) {
    // sosSMLPosition.setReferenceFrame(position.getLocation().getVector().getReferenceFrame());
    // }
    // sosSMLPosition.setPosition(parseCoordinates(position.getLocation().getVector().getCoordinateArray()));
    // }
    // }
    // return sosSMLPosition;
    // }

    private List<SosSweCoordinate> parseCoordinates(Coordinate[] coordinateArray) throws OwsExceptionReport {
        List<SosSweCoordinate> sosCoordinates = new ArrayList<SosSweCoordinate>();
        for (Coordinate xbCoordinate : coordinateArray) {
            if (xbCoordinate.isSetQuantity()) {
                sosCoordinates.add(new SosSweCoordinate(checkCoordinateName(xbCoordinate.getName()),
                        parseQuantity(xbCoordinate.getQuantity())));
            } else {
                String exceptionText = "Error when parsing the Coordinates of Position: It must be of type Quantity!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException("Position", exceptionText);
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
            String exceptionText = "The coordinate name is neighter 'easting' nor 'northing' nor 'altitude'!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException("Position", exceptionText);
        }
    }

    private List<SosSweField> parseSimpleDataRecordFieldArray(AnyScalarPropertyType[] fieldArray)
            throws OwsExceptionReport {
        List<SosSweField> sosFields = new ArrayList<SosSweField>();
        for (AnyScalarPropertyType xbField : (AnyScalarPropertyType[]) fieldArray) {
            // if (xbField.isSetBoolean()) {
            // sosFields.add(new SosSweField(xbField.getName(),
            // parseBoolean(xbField.getBoolean())));
            // } else if (xbField.isSetCategory()) {
            // sosFields.add(new SosSweField(xbField.getName(),
            // parseCategory(xbField.getCategory())));
            // } else if (xbField.isSetCount()) {
            // sosFields.add(new SosSweField(xbField.getName(),
            // parseCount(xbField.getCount())));
            // } else if (xbField.isSetQuantity()) {
            // sosFields.add(new SosSweField(xbField.getName(),
            // parseQuantity(xbField.getQuantity())));
            // } else if (xbField.isSetText()) {
            // sosFields.add(new SosSweField(xbField.getName(),
            // parseText(xbField.getText())));
            // } else if (xbField.isSetTime()) {
            // sosFields.add(new SosSweField(xbField.getName(),
            // parseTime(xbField.getTime())));
            // }
        }
        return sosFields;
    }

    private SosSweTextEncoding parseTextEncoding(TextEncodingType textEncoding) {
        SosSweTextEncoding sosTextEncoding = new SosSweTextEncoding();
        sosTextEncoding.setBlockSeparator(textEncoding.getBlockSeparator());
        sosTextEncoding.setTokenSeparator(textEncoding.getTokenSeparator());
        if (textEncoding.isSetDecimalSeparator()) {
            sosTextEncoding.setDecimalSeparator(textEncoding.getDecimalSeparator());
        }
        if (textEncoding.isSetCollapseWhiteSpaces()) {
            sosTextEncoding.setCollapseWhiteSpaces(textEncoding.getCollapseWhiteSpaces());
        }
        return sosTextEncoding;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }
}
