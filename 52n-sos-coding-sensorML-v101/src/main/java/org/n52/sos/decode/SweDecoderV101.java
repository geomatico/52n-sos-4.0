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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x101.AbstractDataComponentType;
import net.opengis.swe.x101.AbstractDataRecordDocument;
import net.opengis.swe.x101.AbstractDataRecordType;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.BooleanDocument;
import net.opengis.swe.x101.CategoryDocument;
import net.opengis.swe.x101.TextDocument;
import net.opengis.swe.x101.CategoryDocument.Category;
import net.opengis.swe.x101.CountDocument;
import net.opengis.swe.x101.CountDocument.Count;
import net.opengis.swe.x101.CountRangeDocument;
import net.opengis.swe.x101.CountRangeDocument.CountRange;
import net.opengis.swe.x101.DataArrayDocument;
import net.opengis.swe.x101.DataArrayType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.ObservablePropertyDocument;
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.PositionType;
import net.opengis.swe.x101.QuantityDocument;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.QuantityRangeDocument;
import net.opengis.swe.x101.QuantityRangeDocument.QuantityRange;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.TimeRangeDocument;
import net.opengis.swe.x101.TimeRangeDocument.TimeRange;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;
import org.n52.sos.ogc.swe.RangeValue;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SweCoordinateName;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuality;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweDecoderV101 implements IDecoder<Object, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweDecoderV101.class);

    private static final Set<DecoderKey> DECODER_KEYS = CodingHelper.decoderKeysForElements(SWEConstants.NS_SWE,
            DataArrayDocument.class,
            DataArrayType.class,
            AbstractDataComponentType.class,
            BooleanDocument.class, net.opengis.swe.x101.BooleanDocument.Boolean.class,
            CategoryDocument.class, Category.class,
            CountDocument.class, Count.class,
            CountRangeDocument.class, CountRange.class,
            ObservablePropertyDocument.class, ObservableProperty.class,
            QuantityDocument.class, Quantity.class,
            QuantityRangeDocument.class, QuantityRange.class,
            TextDocument.class, Text.class,
            TimeDocument.class, Time.class,
            TimeRangeDocument.class, TimeRange.class,
            DataComponentPropertyType[].class,
            PositionType.class,
            Coordinate[].class,
            AnyScalarPropertyType[].class,
            AbstractDataRecordDocument.class,
            AbstractDataRecordType.class);

    public SweDecoderV101() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
    }
    
    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }
    
    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }
                
    @Override
    public Object decode(Object element) throws OwsExceptionReport {
        if (element instanceof DataArrayDocument) {
            return parseSweDataArray((DataArrayDocument) element);
        } else if (element instanceof AbstractDataComponentType) {
            return parseAbstractDataComponentType((AbstractDataComponentType)element);
        } else if (element instanceof BooleanDocument) {
            return parseBoolean(((BooleanDocument)element).getBoolean());
        } else if (element instanceof CategoryDocument) {
            return parseCategory(((CategoryDocument)element).getCategory());
        } else if (element instanceof CountDocument) {
            return parseCount(((CountDocument)element).getCount());
        } else if (element instanceof CountRangeDocument) {
            return parseCountRange(((CountRangeDocument)element).getCountRange());
        } else if (element instanceof ObservablePropertyDocument) {
            return parseObservableProperty(((ObservablePropertyDocument)element).getObservableProperty());
        } else if (element instanceof QuantityDocument) {
            return parseQuantity(((QuantityDocument)element).getQuantity());
        } else if (element instanceof QuantityRangeDocument) {
            return parseQuantityRange(((QuantityRangeDocument)element).getQuantityRange());
        } else if (element instanceof TextDocument) {
            return parseText(((TextDocument)element).getText());
        } else if (element instanceof TimeDocument) {
            return parseTime(((TimeDocument)element).getTime());
        } else if (element instanceof TimeRangeDocument) {
            return parseTimeRange(((TimeRangeDocument)element).getTimeRange());
        } else if (element instanceof DataComponentPropertyType[]) {
            return parseDataComponentPropertyArray((DataComponentPropertyType[]) element);
        } else if (element instanceof Coordinate[]) {
            return parseCoordinates((Coordinate[]) element);
        } else if (element instanceof AnyScalarPropertyType[]) {
            return parseAnyScalarPropertyArray((AnyScalarPropertyType[]) element);
        } else if (element instanceof AbstractDataRecordDocument) {
            return parseAbstractDataRecord(((AbstractDataRecordDocument) element).getAbstractDataRecord());
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested element");
            if (element instanceof XmlObject) {
                exceptionText.append(" '");
                exceptionText.append(XmlHelper.getLocalName(((XmlObject) element)));
                exceptionText.append("' ");
            }
            exceptionText.append("is not supported by this server!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }
    }
    
    private Object parseAbstractDataComponentType(AbstractDataComponentType abstractDataComponentType) throws OwsExceptionReport {
        // TODO get parse definitions from AbstractDataComponentType here
        if (abstractDataComponentType instanceof net.opengis.swe.x101.BooleanDocument.Boolean) {
            return parseBoolean((net.opengis.swe.x101.BooleanDocument.Boolean)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof Category) {
            return parseCategory((Category)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof Count) {
            return parseCount((Count)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof CountRange) {
            return parseCountRange((CountRange)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof ObservableProperty) {
            return parseObservableProperty((ObservableProperty)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof Quantity) {
            return parseQuantity((Quantity)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof QuantityRange) {
            return parseQuantityRange((QuantityRange)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof Text) {
            return parseText((Text)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof Time) {
            return parseTime((Time)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof TimeRange) {
            return parseTimeRange((TimeRange)abstractDataComponentType);
        } else if (abstractDataComponentType instanceof PositionType) {
            return parsePosition((PositionType) abstractDataComponentType);
        } else if (abstractDataComponentType instanceof AbstractDataRecordType) {
            return parseAbstractDataRecord((AbstractDataRecordType) abstractDataComponentType);
        } else if (abstractDataComponentType instanceof DataArrayType) {
            return parseSweDataArrayType((DataArrayType) abstractDataComponentType);
        }
        return null;
    }

    private Object parseAbstractDataRecord(AbstractDataRecordType abstractDataRecord) throws OwsExceptionReport {
        if (abstractDataRecord instanceof DataRecordPropertyType) {
            return parseDataRecordProperty((DataRecordPropertyType) abstractDataRecord);
        } else if (abstractDataRecord instanceof SimpleDataRecordType) {
            return parseSimpleDataRecord((SimpleDataRecordType) abstractDataRecord);
        }
        return null;
    }

    private SosSweDataRecord parseDataRecordProperty(DataRecordPropertyType dataRecordProperty) throws OwsExceptionReport {
        DataRecordType dataRecord = dataRecordProperty.getDataRecord();
        return parseDataRecord(dataRecord);
    }

    private SosSweDataRecord parseDataRecord(DataRecordType dataRecord) throws OwsExceptionReport {
        SosSweDataRecord sosDataRecord = new SosSweDataRecord();
        if (dataRecord.isSetDefinition()) {
            sosDataRecord.setDefinition(dataRecord.getDefinition());
        }
        if (dataRecord.isSetDescription()) {
            sosDataRecord.setDescription(dataRecord.getDescription().getStringValue());
        }
        if (dataRecord.getFieldArray() != null) {
            sosDataRecord.setFields(parseDataComponentPropertyArray(dataRecord.getFieldArray()));
        }
        return sosDataRecord;
    }

    private Object parseSimpleDataRecord(SimpleDataRecordType simpleDataRecord) throws OwsExceptionReport {
        SosSweSimpleDataRecord sosSimpleDataRecord = new SosSweSimpleDataRecord();
        if (simpleDataRecord.isSetDefinition()) {
            sosSimpleDataRecord.setDefinition(simpleDataRecord.getDefinition());
        }
        if (simpleDataRecord.isSetDescription()) {
            sosSimpleDataRecord.setDescription(simpleDataRecord.getDescription().getStringValue());
        }
        if (simpleDataRecord.getFieldArray() != null) {
            sosSimpleDataRecord.setFields(parseAnyScalarPropertyArray(simpleDataRecord.getFieldArray()));
        }
        return sosSimpleDataRecord;
    }

    private SosSweDataArray parseSweDataArray(DataArrayDocument xbDataArray) throws OwsExceptionReport {
        return parseSweDataArrayType(xbDataArray.getDataArray1());
    }

    private SosSweDataArray parseSweDataArrayType(DataArrayType xbDataArray) throws OwsExceptionReport {
        SosSweDataArray dataArray = new SosSweDataArray();
        // TODO
        return dataArray;
    }

    private List<SosSweField> parseDataComponentPropertyArray(DataComponentPropertyType[] fieldArray)
            throws OwsExceptionReport {
        List<SosSweField> sosFields = new ArrayList<SosSweField>(fieldArray.length);
        for (DataComponentPropertyType xbField : fieldArray) {
            if (xbField.isSetBoolean()) {
                sosFields.add(new SosSweField(xbField.getName(), parseBoolean(xbField.getBoolean())));
            } else if (xbField.isSetCategory()) {
                sosFields.add(new SosSweField(xbField.getName(), parseCategory(xbField.getCategory())));
            } else if (xbField.isSetCount()) {
                sosFields.add(new SosSweField(xbField.getName(), parseCount(xbField.getCount())));
            } else if (xbField.isSetCountRange()) {
                sosFields.add(new SosSweField(xbField.getName(), parseCountRange(xbField.getCountRange())));
            } else if (xbField.isSetQuantity()) {
                sosFields.add(new SosSweField(xbField.getName(), parseQuantity(xbField.getQuantity())));
            } else if (xbField.isSetQuantityRange()) {
                sosFields.add(new SosSweField(xbField.getName(), parseQuantityRange(xbField.getQuantityRange())));
            } else if (xbField.isSetText()) {
                sosFields.add(new SosSweField(xbField.getName(), parseText(xbField.getText())));
            } else if (xbField.isSetTime()) {
                sosFields.add(new SosSweField(xbField.getName(), parseTime(xbField.getTime())));
            } else if (xbField.isSetTimeRange()) {
                sosFields.add(new SosSweField(xbField.getName(), parseTimeRange(xbField.getTimeRange())));
            }
        }
        return sosFields;
    }

    private SosSweAbstractSimpleType<Boolean> parseBoolean(net.opengis.swe.x101.BooleanDocument.Boolean xbBoolean) throws OwsExceptionReport {
        String exceptionText = "The Boolean is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<String> parseCategory(Category xbCategory) throws OwsExceptionReport {
        String exceptionText = "The Category is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<Integer> parseCount(Count xbCount) throws OwsExceptionReport {
        String exceptionText = "The Count is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<RangeValue<Integer>> parseCountRange(CountRange countRange) throws OwsExceptionReport {
        String exceptionText = "The CountRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<String> parseObservableProperty(ObservableProperty observableProperty) {
        ObservableProperty xbObsProp = observableProperty;
        SosSweText sosObservableProperty = new SosSweText();
        if (xbObsProp.isSetDefinition()) {
            sosObservableProperty.setDefinition(xbObsProp.getDefinition());
        }
        return sosObservableProperty;
    }

    private SosSweAbstractSimpleType<Double> parseQuantity(Quantity xbQuantity) {
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
            sosQuantity.setValue(Double.valueOf(xbQuantity.getValue()));
        }
        return sosQuantity;
    }

    private SosSweAbstractSimpleType<RangeValue<Double>> parseQuantityRange(QuantityRange quantityRange) throws OwsExceptionReport {
        String exceptionText = "The QuantityRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<?> parseText(Text xbText) {
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

    private SosSweAbstractSimpleType<DateTime> parseTime(Time time) throws OwsExceptionReport {
        String exceptionText = "The Time is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<RangeValue<DateTime>> parseTimeRange(TimeRange timeRange) throws OwsExceptionReport {
        String exceptionText = "The TimeRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweQuality parseQuality(XmlObject[] qualityArray) {
        return new SosSweQuality();
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

    private List<SosSweCoordinate<?>> parseCoordinates(Coordinate[] coordinateArray) throws OwsExceptionReport {
        List<SosSweCoordinate<?>> sosCoordinates = new ArrayList<SosSweCoordinate<?>>(coordinateArray.length);
        for (Coordinate xbCoordinate : coordinateArray) {
            if (xbCoordinate.isSetQuantity()) {
                sosCoordinates.add(new SosSweCoordinate<Double>(checkCoordinateName(xbCoordinate.getName()),
                        parseQuantity(xbCoordinate.getQuantity())));
            } else {
                String exceptionText = "Error when parsing the Coordinates of Position: It must be of type Quantity!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException("Position", exceptionText);
            }
        }
        return sosCoordinates;
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

    private List<SosSweField> parseAnyScalarPropertyArray(AnyScalarPropertyType[] fieldArray)
            throws OwsExceptionReport {
        List<SosSweField> sosFields = new ArrayList<SosSweField>(fieldArray.length);
        for (AnyScalarPropertyType xbField : fieldArray) {
            if (xbField.isSetBoolean()) {
                sosFields.add(new SosSweField(xbField.getName(), parseBoolean(xbField.getBoolean())));
            } else if (xbField.isSetCategory()) {
                sosFields.add(new SosSweField(xbField.getName(), parseCategory(xbField.getCategory())));
            } else if (xbField.isSetCount()) {
                sosFields.add(new SosSweField(xbField.getName(), parseCount(xbField.getCount())));
            } else if (xbField.isSetQuantity()) {
                sosFields.add(new SosSweField(xbField.getName(), parseQuantity(xbField.getQuantity())));
            } else if (xbField.isSetText()) {
                sosFields.add(new SosSweField(xbField.getName(), parseText(xbField.getText())));
            } else if (xbField.isSetTime()) {
                sosFields.add(new SosSweField(xbField.getName(), parseTime(xbField.getTime())));
            }
        }
        return sosFields;
    }
}
