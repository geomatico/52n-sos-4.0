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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayDocument;
import net.opengis.swe.x20.DataArrayPropertyType;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.UnitReference;
import net.opengis.swe.x20.VectorType.Coordinate;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.io.WKTWriter;

public class SweCommonEncoderv20 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public SweCommonEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SWEConstants.NS_SWE_20));
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
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/core");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-simple-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-record-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-block-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/uml-simple-encodings");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-simple-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-record-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-block-components");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/xsd-simple-encodings");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/general-encoding-rules");
        conformanceClasses.add("http://www.opengis.net/spec/SWE/2.0/conf/text-encoding-rules");
        return conformanceClasses;
    }
    
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWE_20, SWEConstants.NS_SWE_PREFIX);
    }
    
    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, null);
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof SosSweQuantity) {
            return addValuesToSimpleTypeQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweText) {
            return addValuesToSimpleTypeText((SosSweText) element);
        } else if (element instanceof SosSweTime) {
            return addValuesToSimpleTypeTime((SosSweTime) element);
        } else if (element instanceof SosSweCoordinate) {
            return addValuesToCoordinate((SosSweCoordinate) element);
        } else if (element instanceof SosSweAbstractSimpleType) {
            return addSweSimpleTypToField((SosSweAbstractSimpleType) element);
        } else if (element instanceof SosSweAbstractEncoding) {
            return createAbstractEncoding((SosSweAbstractEncoding)element);
        } else if (element instanceof SosSweAbstractDataComponent) {
            return createAbstractDataComponent((SosSweAbstractDataComponent)element);
        } else if (element instanceof SosMultiObservationValues) {
            return createDataArrayResult((SosMultiObservationValues)element);
        }
        return null;
    }

    /**
     * @param element
     * @return
     * @throws OwsExceptionReport
     */
    private AbstractDataComponentType addSweSimpleTypToField(SosSweAbstractSimpleType element) throws OwsExceptionReport {
        if (element instanceof SosSweQuantity) {
            return addValuesToSimpleTypeQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweText) {
            return addValuesToSimpleTypeText((SosSweText) element);
        } else if (element instanceof SosSweTime) {
            return addValuesToSimpleTypeTime((SosSweTime) element);
        } else {
            // TODO: NOT SUPPORTED EXCEPTION
            throw new OwsExceptionReport();
        }
    }

    /**
     * Adds values to SWE quantity
     * 
     * @param quantity
     *            SOS internal representation
     */
    private QuantityType addValuesToSimpleTypeQuantity(SosSweQuantity quantity) {
        QuantityType xbQuantity = QuantityType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (quantity.getDefinition() != null && !quantity.getDefinition().isEmpty()) {
            xbQuantity.setDefinition(quantity.getDefinition());
        }
        if (quantity.getDescription() != null && !quantity.getDescription().isEmpty()) {
            xbQuantity.setDescription(quantity.getDescription());
        }
        if (quantity.getAxisID() != null && !quantity.getAxisID().isEmpty()) {
            xbQuantity.setAxisID(quantity.getDescription());
        }
        if (quantity.getValue() != null && !quantity.getValue().isEmpty()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.getUom() != null && !quantity.getUom().isEmpty()) {
            xbQuantity.addNewUom().setCode(quantity.getUom());
        }
        if (quantity.getQuality() != null) {
            // TODO
        }
        return xbQuantity;
    }

    /**
     * Adds values to SWE text
     * 
     * @param text
     *            SOS internal representation
     */
    private TextType addValuesToSimpleTypeText(SosSweText text) {
        TextType xbText = TextType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (text.getDefinition() != null && !text.getDefinition().isEmpty()) {
            xbText.setDefinition(text.getDefinition());
        }
        if (text.getDescription() != null && !text.getDescription().isEmpty()) {
            xbText.setDescription(text.getDescription());
        }
        if (text.getValue() != null && !text.getValue().isEmpty()) {
            xbText.setValue(text.getValue());
        }
        return xbText;
    }

    private TimeType addValuesToSimpleTypeTime(SosSweTime time) {
        TimeType xbTime = TimeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (time.getDefinition() != null && !time.getDefinition().isEmpty()) {
            xbTime.setDefinition(time.getDefinition());
        }
        if (time.getDescription() != null && !time.getDescription().isEmpty()) {
            xbTime.setDescription(time.getDescription());
        }
        if (time.getValue() != null && !time.getValue().isEmpty()) {
            xbTime.setValue(time.getValue());
        }
        if (time.getUom() != null && !time.getUom().isEmpty()) {
            xbTime.addNewUom().setHref(time.getUom());
        }
        if (time.getQuality() != null) {
            // TODO
        }
        return xbTime;
    }

    /**
     * Adds values to SWE coordinates
     * 
     * @param coordinate
     *            SOS internal representation
     */
    private Coordinate addValuesToCoordinate(SosSweCoordinate coordinate) {
        Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(addValuesToSimpleTypeQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

    private XmlObject createAbstractDataComponent(SosSweAbstractDataComponent sosSweAbstractDataComponent) throws OwsExceptionReport {
        try {
            // TODO implement encoding if getXml() returns nothing
            if (sosSweAbstractDataComponent instanceof SosSweDataRecord)
            {
                // TODO Eike: create SweDataRecordDocument from xml
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
            else if (sosSweAbstractDataComponent instanceof SosSweDataArray)
            {
                // TODO Eike: create SweDataArrayDocument from xml
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
            else if (sosSweAbstractDataComponent.getXml() != null && !sosSweAbstractDataComponent.getXml().isEmpty()) {
                XmlObject xmlObject = XmlObject.Factory.parse(sosSweAbstractDataComponent.getXml());
                return xmlObject;
            } else {
                String exceptionText = "AbstractDataComponent can not be encoded!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText); 
            }
        } catch (XmlException e) {
            String exceptionText = "Error while encoding AbstractDataComponent!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
        }
    }

    private XmlObject createAbstractEncoding(SosSweAbstractEncoding sosSweAbstractEncoding) throws OwsExceptionReport {
        try {
            if (sosSweAbstractEncoding.getXml() != null && !sosSweAbstractEncoding.getXml().isEmpty()) {
                XmlObject xmlObject = XmlObject.Factory.parse(sosSweAbstractEncoding.getXml());
                return xmlObject;
            } else {
                String exceptionText = "AbstractEncoding can not be encoded!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText); 
            }
        } catch (XmlException e) {
            String exceptionText = "Error while encoding AbstractEncoding!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
        }
    }
    
    /**
     * creates the swe:DataArray, which is used for the result element of a
     * generic om:Observation
     * 
     * @param absObs
     * @param phenComponents
     * 
     * @param phenComponents
     *            ids of the phenomena of the common observation
     * @return DataDefinitionType representing the DataDefinition element of a
     *         CommonObservation
     * @throws OwsExceptionReport
     */
    private DataArrayPropertyType createDataArrayResult(SosMultiObservationValues sosObservationValue) throws OwsExceptionReport {
        if (sosObservationValue.getValue() instanceof SosSweDataArray) {
            DataArrayPropertyType dataArrayProperty =
                    DataArrayPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            XmlObject xbAbstractDataComponent = createAbstractDataComponent((SosSweAbstractDataComponent) sosObservationValue.getValue());
            if (xbAbstractDataComponent.schemaType() == DataArrayDocument.type) {
                dataArrayProperty.setDataArray1(((DataArrayDocument)xbAbstractDataComponent).getDataArray1());
            }
            else if (xbAbstractDataComponent.schemaType() == DataArrayType.type) {
                dataArrayProperty.setDataArray1((DataArrayType) xbAbstractDataComponent);
            }}
        return null;
    }
    
    /**
     * creates a result string representing the value matrix of the common
     * observation or getResult response document
     * 
     * @param phenComponents
     *            the phenomenon components of the values of the value matrix
     * @param valueMap
     * @param valueMap
     *            HashMap containing the time as key, and an ArrayList with
     *            pairs of phenomena and values at the key time as values
     * @return String representing the value matrix of the result element
     * @throws OwsExceptionReport
     * @throws ServiceException
     */
    private String createResultString(List<SosObservableProperty> phenComponents, SosObservation sosObservation,
            Map<ITime, Map<String, IValue>> valueMap) throws OwsExceptionReport {

        if (!(phenComponents instanceof ArrayList)) {
            phenComponents = new ArrayList<SosObservableProperty>(phenComponents);
        }
        String noDataValue = sosObservation.getNoDataValue();
        String tokenSeperator = sosObservation.getTokenSeparator();
        String tupleSeperator = sosObservation.getTupleSeparator();
        SosSweDataRecord r = sosObservation.getResultStructure();
        
        String[] phens = new String[phenComponents.size() + 1];
        int timeIndex = -1;
        if (r == null) {
            phens[timeIndex = 0] = OMConstants.PHENOMENON_TIME;
            for (int i = 0; i < phenComponents.size(); ++i) {
                phens[i+1] = phenComponents.get(i).getIdentifier();
            }
        } else {
            int i = 0;
            for (SosSweField f : r.getFields()) {
                if (f.getElement().getDefinition().equals(OMConstants.PHENOMENON_TIME)) {
                    phens[timeIndex = i] = OMConstants.PHENOMENON_TIME; 
                } else {
                    phens[i] = f.getElement().getDefinition();
                }
                ++i;
            }
        }
        if (timeIndex < 0) {
            /* TODO no phentimeindex found... */
        }
        ITime[] times = new ArrayList<ITime>(valueMap.keySet())
            .toArray(new ITime[valueMap.keySet().size()]);
        Arrays.sort(times);
        StringBuilder b = new StringBuilder();
        
        /* dimensions will always be greater than (1,1).. 
         * so partly roll out the loop to gain some performance */
        b.append(getValue(0, 0, times, phens, timeIndex, noDataValue, valueMap));
        for (int j = 1; j < phens.length; ++j) {
            b.append(tokenSeperator);
            b.append(getValue(0, j, times, phens, timeIndex, noDataValue, valueMap));
        }
        for (int i = 1; i < times.length; ++i) {
            b.append(tupleSeperator);
            b.append(getValue(i, 0, times, phens, timeIndex, noDataValue, valueMap));
            for (int j = 1; j < phens.length; ++j) {
                b.append(tokenSeperator);
                b.append(getValue(i, j, times, phens, timeIndex, noDataValue, valueMap));
            }
        }
        b.append(tupleSeperator);
        return b.toString();
    }
    
    private String getValue(int i, int j, ITime[] times, String[] phens, int phenTimeIndex,
            String noDataValue, Map<ITime, Map<String, IValue>> valueMap) throws OwsExceptionReport {
        if (j == phenTimeIndex) {
            return getTimeString(times[i]);
        } else {
            Map<String, IValue> value = valueMap.get(times[i]);
            return (value == null) ? noDataValue 
                    : getStringValue(value.get(phens[j]), noDataValue); 
        }
    }
    
    /**
     * Get time as text for TimePeriod or TimeInstant
     * 
     * @param time
     *            To to get text from.
     * @return time string
     * @throws OwsExceptionReport
     */
    private String getTimeString(ITime time) throws OwsExceptionReport {
        try {
            StringBuilder timeString = new StringBuilder();
            if (time instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) time;
                timeString.append(DateTimeHelper.formatDateTime2ResponseString(ti.getValue()));
            } else if (time instanceof TimePeriod) {
                TimePeriod tp = (TimePeriod) time;
                timeString.append(DateTimeHelper.formatDateTime2ResponseString(tp.getStart()));
                timeString.append("/");
                timeString.append(DateTimeHelper.formatDateTime2ResponseString(tp.getEnd()));
            }
            return timeString.toString(); 
        } catch (DateTimeException dte) {
            String exceptionText = "Error while creating time string!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
        
    }
    
    /**
     * Get value as text.
     * 
     * @param noDataValue
     * 
     * @param obsPropID
     *            observableProperty
     * @param object
     *            value
     * @return value as text.
     */
    private String getStringValue(IValue value, String noDataValue) {
        if (value == null) {
            return noDataValue;
        }
        WKTWriter wktWriter = new WKTWriter();
        if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            if (booleanValue.getValue() == null) {
                return noDataValue;
            } else {
                return Boolean.toString(booleanValue.getValue().booleanValue());
            }
        } else if (value instanceof CountValue) {
            CountValue countValue = (CountValue) value;
            if (countValue.getValue() == null
                    || (countValue.getValue() != null && countValue.getValue() == Integer.MIN_VALUE)) {
                return noDataValue;
            } else {
                return Integer.toString(countValue.getValue().intValue());
            }
        } else if (value instanceof QuantityValue) {
            /* TODO customizable decimal seperator */
            QuantityValue quantityValue = (QuantityValue) value;
            if (quantityValue.getValue() == null
                    || (quantityValue.getValue() != null && quantityValue.getValue().equals(Double.NaN))) {
                return noDataValue;
            } else {
                return Double.toString(quantityValue.getValue().doubleValue());
            }
        }
        // else if (value instanceof t) {
        // TimeType xbTime =
        // (TimeType) field.addNewAbstractDataComponent().substitute(
        // SWEConstants.QN_TIME_SWE_200, TimeType.type);
        // xbTime.setDefinition(observableProperty.getIdentifier());
        // xbTime.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
        // }
        else if (value instanceof TextValue) {
            TextValue textValue = (TextValue) value;
            /*  TODO should it really be testet for empty strings? isn't that a valid observation value? */
            if (textValue.getValue() == null || (textValue.getValue() != null && textValue.getValue().isEmpty())) {
                return noDataValue;
            } else {
                return textValue.getValue().toString();
            }
        } else if (value instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) value;
            if (categoryValue.getValue() == null
                    || (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty())) {
                return noDataValue;
            } else {
                return categoryValue.getValue().toString();
            }
        } else {
            if (value.getValue() == null) {
                return noDataValue;
            } else {
                return value.getValue().toString();
            }
        }
    }



    private void addDataComponentToField(Field field, SosObservableProperty observableProperty,
            Collection<Map<String, IValue>> values) {
        IValue value = getValueForObservableProperty(values, observableProperty.getIdentifier());
        if (value != null) {
            if (value instanceof BooleanValue) {
                BooleanType xbBool =
                        (BooleanType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_BOOLEAN_SWE_200,
                                BooleanType.type);
                xbBool.setDefinition(observableProperty.getIdentifier());
            } else if (value instanceof CountValue) {
                CountType xbCount =
                        (CountType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_COUNT_SWE_200,
                                CountType.type);
                xbCount.setDefinition(observableProperty.getIdentifier());
            } else if (value instanceof QuantityValue) {
                QuantityType xbQuantity =
                        (QuantityType) field.addNewAbstractDataComponent().substitute(
                                SWEConstants.QN_QUANTITY_SWE_200, QuantityType.type);
                xbQuantity.setDefinition(observableProperty.getIdentifier());
                UnitReference xb_uom = xbQuantity.addNewUom();
                /* FIXME set the unit of the observed property while inserting result */
                String uom = observableProperty.getUnit();
                if (uom == null || uom.trim().isEmpty()) {
                    uom = value.getUnit() == null ? "" : value.getUnit();
                }
                xb_uom.setCode(uom);
            }
            // else if (value instanceof t) {
            // TimeType xbTime =
            // (TimeType) field.addNewAbstractDataComponent().substitute(
            // SWEConstants.QN_TIME_SWE_200, TimeType.type);
            // xbTime.setDefinition(observableProperty.getIdentifier());
            // xbTime.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
            // }
            else if (value instanceof TextValue) {
                TextType xbText =
                        (TextType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                TextType.type);
                xbText.setDefinition(observableProperty.getIdentifier());
            } else if (value instanceof CategoryValue) {
                CategoryType xbCategory =
                        (CategoryType) field.addNewAbstractDataComponent().substitute(
                                SWEConstants.QN_CATEGORY_SWE_200, CategoryType.type);
                xbCategory.setDefinition(observableProperty.getIdentifier());
            } else {
                TextType xbText =
                        (TextType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                TextType.type);
                xbText.setDefinition(observableProperty.getIdentifier());
            }
            String[] uriParts = observableProperty.getIdentifier().split("/|:");
//            field.setName(uriParts[uriParts.length - 1]);
            field.setName("_" + new DateTime().getMillis());
        } else {
//            field.setName(observableProperty.getIdentifier().replace(SosConstants.PHENOMENON_PREFIX, ""));
            field.setName("_" + new DateTime().getMillis());
            TextType xbText =
                    (TextType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                            TextType.type);
            xbText.setDefinition(observableProperty.getIdentifier());
        }

    }

    private IValue getValueForObservableProperty(Collection<Map<String, IValue>> values, String identifier) {
        for (Map<String, IValue> map : values) {
            if (map.get(identifier) != null) {
                return map.get(identifier);
            }
        }
        return null;
    }

}
