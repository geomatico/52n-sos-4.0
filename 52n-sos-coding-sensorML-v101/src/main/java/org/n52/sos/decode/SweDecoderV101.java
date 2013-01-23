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
import net.opengis.swe.x101.TextDocument;
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
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweQuality;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
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
            return parseAbstractDataComponentType(((DataArrayDocument) element).getDataArray1());
        } else if (element instanceof AbstractDataComponentType) {
            return parseAbstractDataComponentType((AbstractDataComponentType)element);
        } else if (element instanceof BooleanDocument) {
            return parseAbstractDataComponentType(((BooleanDocument)element).getBoolean());
        } else if (element instanceof CategoryDocument) {
            return parseAbstractDataComponentType(((CategoryDocument)element).getCategory());
        } else if (element instanceof CountDocument) {
            return parseAbstractDataComponentType(((CountDocument)element).getCount());
        } else if (element instanceof CountRangeDocument) {
            return parseAbstractDataComponentType(((CountRangeDocument)element).getCountRange());
        } else if (element instanceof ObservablePropertyDocument) {
            return parseAbstractDataComponentType(((ObservablePropertyDocument)element).getObservableProperty());
        } else if (element instanceof QuantityDocument) {
            return parseAbstractDataComponentType(((QuantityDocument)element).getQuantity());
        } else if (element instanceof QuantityRangeDocument) {
            return parseAbstractDataComponentType(((QuantityRangeDocument)element).getQuantityRange());
        } else if (element instanceof TextDocument) {
            return parseAbstractDataComponentType(((TextDocument)element).getText());
        } else if (element instanceof TimeDocument) {
            return parseAbstractDataComponentType(((TimeDocument)element).getTime());
        } else if (element instanceof TimeRangeDocument) {
            return parseAbstractDataComponentType(((TimeRangeDocument)element).getTimeRange());
        } else if (element instanceof DataComponentPropertyType[]) {
            return parseDataComponentPropertyArray((DataComponentPropertyType[]) element);
        } else if (element instanceof Coordinate[]) {
            return parseCoordinates((Coordinate[]) element);
        } else if (element instanceof AnyScalarPropertyType[]) {
            return parseAnyScalarPropertyArray((AnyScalarPropertyType[]) element);
        } else if (element instanceof AbstractDataRecordDocument) {
            return parseAbstractDataComponentType(((AbstractDataRecordDocument) element).getAbstractDataRecord());
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
    
    private SosSweAbstractDataComponent parseAbstractDataComponentType(AbstractDataComponentType abstractDataComponent) throws OwsExceptionReport {
        SosSweAbstractDataComponent sosAbstractDataComponent = null;
        if (abstractDataComponent instanceof net.opengis.swe.x101.BooleanDocument.Boolean) {
            sosAbstractDataComponent = parseBoolean((net.opengis.swe.x101.BooleanDocument.Boolean)abstractDataComponent);
        } else if (abstractDataComponent instanceof Category) {
            sosAbstractDataComponent = parseCategory((Category)abstractDataComponent);
        } else if (abstractDataComponent instanceof Count) {
            sosAbstractDataComponent = parseCount((Count)abstractDataComponent);
        } else if (abstractDataComponent instanceof CountRange) {
            sosAbstractDataComponent = parseCountRange((CountRange)abstractDataComponent);
        } else if (abstractDataComponent instanceof ObservableProperty) {
            sosAbstractDataComponent = parseObservableProperty((ObservableProperty)abstractDataComponent);
        } else if (abstractDataComponent instanceof Quantity) {
            sosAbstractDataComponent = parseQuantity((Quantity)abstractDataComponent);
        } else if (abstractDataComponent instanceof QuantityRange) {
            sosAbstractDataComponent = parseQuantityRange((QuantityRange)abstractDataComponent);
        } else if (abstractDataComponent instanceof Text) {
            sosAbstractDataComponent = parseText((Text)abstractDataComponent);
        } else if (abstractDataComponent instanceof Time) {
            sosAbstractDataComponent = parseTime((Time)abstractDataComponent);
        } else if (abstractDataComponent instanceof TimeRange) {
            sosAbstractDataComponent = parseTimeRange((TimeRange)abstractDataComponent);
        } else if (abstractDataComponent instanceof PositionType) {
            sosAbstractDataComponent = parsePosition((PositionType) abstractDataComponent);
        } else if (abstractDataComponent instanceof DataRecordPropertyType) {
            return parseDataRecordProperty((DataRecordPropertyType) abstractDataComponent);
        } else if (abstractDataComponent instanceof SimpleDataRecordType) {
            return parseSimpleDataRecord((SimpleDataRecordType) abstractDataComponent);
        } else if (abstractDataComponent instanceof DataArrayType) {
            sosAbstractDataComponent = parseSweDataArrayType((DataArrayType) abstractDataComponent);
        } else if (abstractDataComponent instanceof DataRecordType) {
            sosAbstractDataComponent = parseDataRecord((DataRecordType) abstractDataComponent);
        }
        if (sosAbstractDataComponent != null) {
            if (abstractDataComponent.isSetDefinition()) {
                sosAbstractDataComponent.setDefinition(abstractDataComponent.getDefinition());
            }
            if (abstractDataComponent.isSetDescription()) {
                sosAbstractDataComponent.setDescription(abstractDataComponent.getDescription().getStringValue());
            }
        }
        return sosAbstractDataComponent;
    }

//    private SosSweAbstractDataComponent parseAbstractDataRecord(AbstractDataRecordType abstractDataRecord) throws OwsExceptionReport {
//        if (abstractDataRecord instanceof DataRecordPropertyType) {
//            return parseDataRecordProperty((DataRecordPropertyType) abstractDataRecord);
//        } else if (abstractDataRecord instanceof SimpleDataRecordType) {
//            return parseSimpleDataRecord((SimpleDataRecordType) abstractDataRecord);
//        }
//        return null;
//    }

    private SosSweDataRecord parseDataRecordProperty(DataRecordPropertyType dataRecordProperty) throws OwsExceptionReport {
        DataRecordType dataRecord = dataRecordProperty.getDataRecord();
        return parseDataRecord(dataRecord);
    }

    private SosSweDataRecord parseDataRecord(DataRecordType dataRecord) throws OwsExceptionReport {
        SosSweDataRecord sosDataRecord = new SosSweDataRecord();
        if (dataRecord.getFieldArray() != null) {
            sosDataRecord.setFields(parseDataComponentPropertyArray(dataRecord.getFieldArray()));
        }
        return sosDataRecord;
    }

    private SosSweSimpleDataRecord parseSimpleDataRecord(SimpleDataRecordType simpleDataRecord) throws OwsExceptionReport {
        SosSweSimpleDataRecord sosSimpleDataRecord = new SosSweSimpleDataRecord();
        if (simpleDataRecord.getFieldArray() != null) {
            sosSimpleDataRecord.setFields(parseAnyScalarPropertyArray(simpleDataRecord.getFieldArray()));
        }
        return sosSimpleDataRecord;
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
            SosSweAbstractDataComponent sosAbstractDataComponentType = null;
            if (xbField.isSetBoolean()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getBoolean());
            } else if (xbField.isSetCategory()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getCategory());
            } else if (xbField.isSetCount()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getCount());
            } else if (xbField.isSetCountRange()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getCountRange());
            } else if (xbField.isSetQuantity()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getQuantity());
            } else if (xbField.isSetQuantityRange()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getQuantityRange());
            } else if (xbField.isSetText()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getText());
            } else if (xbField.isSetTime()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getTime());
            } else if (xbField.isSetTimeRange()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getTimeRange());
            }
            if (sosAbstractDataComponentType != null) {
                sosFields.add(new SosSweField(xbField.getName(), sosAbstractDataComponentType));
            }
        }
        return sosFields;
    }

    private SosSweAbstractSimpleType<Boolean> parseBoolean(net.opengis.swe.x101.BooleanDocument.Boolean xbBoolean) throws OwsExceptionReport {
        String exceptionText = "The Boolean is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<String> parseCategory(Category category) throws OwsExceptionReport {
        String exceptionText = "The Category is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<Integer> parseCount(Count xbCount) throws OwsExceptionReport {
        SosSweCount sosCount = new SosSweCount();
        if (xbCount.getQualityArray() != null) {
            sosCount.setQuality(parseQuality(xbCount.getQualityArray()));
        }
        if (xbCount.isSetValue()) {
            sosCount.setValue(xbCount.getValue().intValue());
        }
        return sosCount;
    }

    private SosSweAbstractSimpleType<RangeValue<Integer>> parseCountRange(CountRange countRange) throws OwsExceptionReport {
        String exceptionText = "The CountRange is not supported";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    private SosSweAbstractSimpleType<String> parseObservableProperty(ObservableProperty observableProperty) {
        SosSweText sosObservableProperty = new SosSweText();
        return sosObservableProperty;
    }

    private SosSweAbstractSimpleType<Double> parseQuantity(Quantity xbQuantity) {
        SosSweQuantity sosQuantity = new SosSweQuantity();
        if (xbQuantity.isSetAxisID()) {
            sosQuantity.setAxisID(xbQuantity.getAxisID());
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
        if (xbText.isSetValue()) {
            sosText.setValue(xbText.getValue());
        }
        return sosText;
    }

    private SosSweAbstractSimpleType<DateTime> parseTime(Time time) throws OwsExceptionReport {
        SosSweTime sosTime = new SosSweTime();
        if (time.isSetValue()) {
            try {
                sosTime.setValue(DateTimeHelper.parseIsoString2DateTime(time.getValue().toString()));
            } catch (DateTimeException e) {
                String exceptionText = "Error while parsing Time!";
                LOGGER.debug(exceptionText, e);
                throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }
        }
        if (time.getUom() != null) {
            sosTime.setUom(time.getUom().getHref());
        }
        return sosTime;
    }

    private SosSweAbstractSimpleType<RangeValue<DateTime>> parseTimeRange(TimeRange timeRange) throws OwsExceptionReport {
        SosSweTimeRange sosTimeRange = new SosSweTimeRange();
        if (timeRange.isSetValue()) {
            try {
             // FIXME check if this parses correct
                List<?> value = timeRange.getValue();
                if (value != null && !value.isEmpty()) {
                    RangeValue<DateTime> range = new RangeValue<DateTime>();
                    boolean first = true;
                    for (Object object : value) {
                        if (first) {
                            range.setRangeStart(DateTimeHelper.parseIsoString2DateTime(timeRange.getValue().toString()));
                            first = false;
                        }
                        range.setRangeEnd(DateTimeHelper.parseIsoString2DateTime(timeRange.getValue().toString()));
                    }
                    sosTimeRange.setValue(range);
                }

            } catch (DateTimeException e) {
                String exceptionText = "Error while parsing TimeRange!";
                LOGGER.debug(exceptionText, e);
                throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }
        }
        if (timeRange.getUom() != null) {
            sosTimeRange.setUom(timeRange.getUom().getHref());
        }
        return sosTimeRange;
    }

    private SosSweQuality parseQuality(XmlObject[] qualityArray) {
        return new SosSweQuality();
    }

    private SosSMLPosition parsePosition(PositionType position) throws OwsExceptionReport {
        SosSMLPosition sosSMLPosition = new SosSMLPosition();
        if (position.isSetReferenceFrame()) {
            sosSMLPosition.setReferenceFrame(position.getReferenceFrame());
        }
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
                        (SosSweAbstractSimpleType)parseAbstractDataComponentType(xbCoordinate.getQuantity())));
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
            SosSweAbstractDataComponent sosAbstractDataComponentType = null;
            if (xbField.isSetBoolean()) {
                sosAbstractDataComponentType = parseAbstractDataComponentType(xbField.getBoolean());
            } else if (xbField.isSetCategory()) {
                sosAbstractDataComponentType =  parseAbstractDataComponentType(xbField.getCategory());
            } else if (xbField.isSetCount()) {
                sosAbstractDataComponentType =  parseAbstractDataComponentType(xbField.getCount());
            } else if (xbField.isSetQuantity()) {
                sosAbstractDataComponentType =  parseAbstractDataComponentType(xbField.getQuantity());
            } else if (xbField.isSetText()) {
                sosAbstractDataComponentType =  parseAbstractDataComponentType(xbField.getText());
            } else if (xbField.isSetTime()) {
                sosAbstractDataComponentType =  parseAbstractDataComponentType(xbField.getTime());
            }
            if (sosAbstractDataComponentType != null) {
                sosFields.add(new SosSweField(xbField.getName(), sosAbstractDataComponentType));
            }
        }
        return sosFields;
    }
}
