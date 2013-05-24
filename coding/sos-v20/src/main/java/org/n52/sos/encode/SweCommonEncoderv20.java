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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.AbstractEncodingType;
import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayPropertyType;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.DataArrayType.ElementType;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.Reference;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeRangeType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.VectorType.Coordinate;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.NotYetSupportedException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.exception.ows.concrete.XmlDecodingException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweCommonEncoderv20 implements Encoder<XmlObject, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonEncoderv20.class);
    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(
            SWEConstants.NS_SWE_20,
            SosSweCoordinate.class,
            SosSweAbstractSimpleType.class,
            SosSweAbstractEncoding.class,
            SosSweAbstractDataComponent.class,
            SosSweDataArray.class);
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            ConformanceClasses.SWE_V2_CORE,
            ConformanceClasses.SWE_V2_UML_SIMPLE_COMPONENTS,
            ConformanceClasses.SWE_V2_UML_RECORD_COMPONENTS,
            ConformanceClasses.SWE_V2_UML_BLOCK_ENCODINGS,
            ConformanceClasses.SWE_V2_UML_SIMPLE_ENCODINGS,
            ConformanceClasses.SWE_V2_XSD_SIMPLE_COMPONENTS,
            ConformanceClasses.SWE_V2_XSD_RECORD_COMPONENTS,
            ConformanceClasses.SWE_V2_XSD_BLOCK_COMPONENTS,
            ConformanceClasses.SWE_V2_XSD_SIMPLE_ENCODINGS,
            ConformanceClasses.SWE_V2_GENERAL_ENCODING_RULES,
            ConformanceClasses.SWE_V2_TEXT_ENCODING_RULES);

    public SweCommonEncoderv20() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper
                .join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public void addNamespacePrefixToMap(final Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWE_20, SWEConstants.NS_SWE_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public XmlObject encode(final Object sosSweType) throws OwsExceptionReport {
        return encode(sosSweType, null);
    }

    @Override
    public XmlObject encode(final Object sosSweType, final Map<HelperValues, String> additionalValues) throws OwsExceptionReport {

        if (sosSweType instanceof SosSweCoordinate) {
            return createCoordinate((SosSweCoordinate) sosSweType);
//        } else if (sosSweType instanceof SosSweAbstractSimpleType) {
//            return createSimpleType((SosSweAbstractSimpleType) sosSweType);
        } else if (sosSweType instanceof SosSweAbstractEncoding) {
            return createAbstractEncoding((SosSweAbstractEncoding) sosSweType);
        } else if (sosSweType instanceof SosSweAbstractDataComponent) {
            return createAbstractDataComponent((SosSweAbstractDataComponent) sosSweType, additionalValues);
//        } else if (sosSweType instanceof SosMultiObservationValues) {
//            SosMultiObservationValues sosObservationValue = (SosMultiObservationValues) sosSweType;
//            if (sosObservationValue.getValue() != null && sosObservationValue.getValue() instanceof SweDataArrayValue
//                    && sosObservationValue.getValue().getValue() != null
//                    && sosObservationValue.getValue().getValue() instanceof SosSweDataArray) {
//                DataArrayType dataArrayType =
//                        createDataArray((SosSweDataArray) sosObservationValue.getValue().getValue());
//                if (additionalValues.containsKey(HelperValues.FOR_OBSERVATION)) {
//                    DataArrayPropertyType dataArrayProperty = DataArrayPropertyType.Factory.newInstance();
//                    dataArrayProperty.setDataArray1(dataArrayType);
//                    return dataArrayProperty;
//                }
//                return dataArrayType;
//            }
//        } else if (sosSweType instanceof SosSingleObservationValue) {
//            // TODO
//            DataArrayType dataArrayType = createDataArray((SosSingleObservationValue) sosSweType);
//            if (additionalValues.containsKey(HelperValues.FOR_OBSERVATION)) {
//                DataArrayPropertyType dataArrayProperty = DataArrayPropertyType.Factory.newInstance();
//                dataArrayProperty.setDataArray1(dataArrayType);
//                return dataArrayProperty;
//            }
//            return dataArrayType;
        } else if (sosSweType instanceof SosSweDataArray) {
            final DataArrayType dataArrayType =
                          createDataArray((SosSweDataArray) sosSweType);
            if (additionalValues.containsKey(HelperValues.FOR_OBSERVATION)) {
                final DataArrayPropertyType dataArrayProperty = DataArrayPropertyType.Factory.newInstance(XmlOptionsHelper
                        .getInstance().getXmlOptions());
                dataArrayProperty.setDataArray1(dataArrayType);
                return dataArrayProperty;
            }
            return dataArrayType;
        }
        throw new UnsupportedEncoderInputException(this, sosSweType);
    }

    private XmlObject createAbstractDataComponent(final SosSweAbstractDataComponent sosSweAbstractDataComponent,
                                                  final Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        AbstractDataComponentType abstractDataComponentType = null;
        if (sosSweAbstractDataComponent instanceof SosSweAbstractSimpleType) {
            abstractDataComponentType = createSimpleType((SosSweAbstractSimpleType) sosSweAbstractDataComponent);
        } else if (sosSweAbstractDataComponent instanceof SosSweDataRecord) {
            abstractDataComponentType = createDataRecord((SosSweDataRecord) sosSweAbstractDataComponent);
        } else if (sosSweAbstractDataComponent instanceof SosSweDataArray) {
            abstractDataComponentType = createDataArray((SosSweDataArray) sosSweAbstractDataComponent);
        } else if ((sosSweAbstractDataComponent.getXml() != null) && !sosSweAbstractDataComponent.getXml().isEmpty()) {
            try {
                return XmlObject.Factory.parse(sosSweAbstractDataComponent.getXml());
            } catch (final XmlException ex) {
                throw new XmlDecodingException(SosSweAbstractDataComponent.class.getName(),
                                               sosSweAbstractDataComponent.getXml(), ex);
            }
        } else {
            throw new NotYetSupportedException(SosSweAbstractDataComponent.class.getName(), sosSweAbstractDataComponent);
        }
        // add AbstractDataComponentType information
        if (abstractDataComponentType != null) {
            if (sosSweAbstractDataComponent.isSetDefinition()) {
                abstractDataComponentType.setDefinition(sosSweAbstractDataComponent.getDefinition());
            }
            if (sosSweAbstractDataComponent.isSetDescription()) {
                abstractDataComponentType.setDescription(sosSweAbstractDataComponent.getDescription());
            }
            if (sosSweAbstractDataComponent.isSetIdentifier()) {
                abstractDataComponentType.setIdentifier(sosSweAbstractDataComponent.getIdentifier());
            }
            if (sosSweAbstractDataComponent.isSetLabel()) {
                abstractDataComponentType.setLabel(sosSweAbstractDataComponent.getLabel());
            }
        }
        if ((abstractDataComponentType instanceof DataArrayType) && additionalValues
                .containsKey(HelperValues.FOR_OBSERVATION)) {
            final DataArrayPropertyType dataArrayProperty = DataArrayPropertyType.Factory.newInstance();
            dataArrayProperty.setDataArray1((DataArrayType) abstractDataComponentType);
            return dataArrayProperty;
        }
        return abstractDataComponentType;
    }

    private DataRecordType createDataRecord(final SosSweDataRecord sosDataRecord) throws OwsExceptionReport {
        final List<SosSweField> sosFields = sosDataRecord.getFields();
        final DataRecordType xbDataRecord = DataRecordType.Factory.newInstance();
        if (sosFields != null) {
            final ArrayList<Field> xbFields = new ArrayList<DataRecordType.Field>(sosFields.size());
            for (final SosSweField sosSweField : sosFields) {
                if (sosSweField != null) {
                    final Field xbField = createField(sosSweField);
                    xbFields.add(xbField);
                } else {
                    LOGGER.error("sosSweField is null is sosDataRecord");
                }
            }
            xbDataRecord.setFieldArray(xbFields.toArray(new Field[xbFields.size()]));
        } else {
            LOGGER.error("sosDataRecord contained no fields");
        }
        return xbDataRecord;
    }

    private DataArrayType createDataArray(final SosSweDataArray sosDataArray) throws OwsExceptionReport {
        if (sosDataArray != null) {

            final DataArrayType xbDataArray =
                          DataArrayType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (sosDataArray.isSetElementCount()) {
                xbDataArray.addNewElementCount().setCount(createCount(sosDataArray.getElementCount()));
            }
            if (sosDataArray.isSetElementTyp()) {
                final ElementType elementType = xbDataArray.addNewElementType();
                if (sosDataArray.getElementType().isSetDefinition()) {
                    elementType.setName(sosDataArray.getElementType().getDefinition());
                } else {
                    elementType.setName("Components");
                }

                elementType.addNewAbstractDataComponent()
                        .set(createDataRecord((SosSweDataRecord) sosDataArray.getElementType()));
                elementType
                        .getAbstractDataComponent()
                        .substitute(
                        new QName(SWEConstants.NS_SWE_20, SWEConstants.EN_DATA_RECORD,
                                  SWEConstants.NS_SWE_PREFIX), DataRecordType.type);
            }
            if (sosDataArray.isSetEncoding()) {
                xbDataArray.addNewEncoding().addNewAbstractEncoding();
                xbDataArray.getEncoding().getAbstractEncoding()
                        .set(createAbstractEncoding(sosDataArray.getEncoding()));
                xbDataArray
                        .getEncoding()
                        .getAbstractEncoding()
                        .substitute(
                        new QName(SWEConstants.NS_SWE_20, SWEConstants.EN_TEXT_ENCODING,
                                  SWEConstants.NS_SWE_PREFIX), TextEncodingType.type);
            }
            if (sosDataArray.isSetValues()) {
                xbDataArray.addNewValues().set(createValues(sosDataArray.getValues(), sosDataArray.getEncoding()));
            }
            return xbDataArray;
        }
        return null;
    }

    private XmlString createValues(final List<List<String>> values, final SosSweAbstractEncoding encoding) {
        // TODO How to deal with the decimal separator - is it an issue here?
        final StringBuilder valueStringBuilder = new StringBuilder(256);
        final SosSweTextEncoding textEncoding = (SosSweTextEncoding) encoding;
        final String tokenSeparator = textEncoding.getTokenSeparator();
        final String blockSeparator = textEncoding.getBlockSeparator();
        for (final List<String> block : values) {
            final StringBuilder blockStringBuilder = new StringBuilder();
            for (final String token : block) {
                blockStringBuilder.append(token);
                blockStringBuilder.append(tokenSeparator);
            }
            String blockString = blockStringBuilder.toString();
            // remove last token sep
            blockString = blockString.substring(0, blockString.lastIndexOf(tokenSeparator));
            valueStringBuilder.append(blockString);
            valueStringBuilder.append(blockSeparator);
        }
        String valueString = valueStringBuilder.toString();
        // remove last block sep
        valueString = valueString.substring(0, valueString.lastIndexOf(blockSeparator));
        // create XB result object
        final XmlString xbValueString = XmlString.Factory.newInstance();
        xbValueString.setStringValue(valueString);
        return xbValueString;
    }

    private DataRecordType.Field createField(final SosSweField sweField) throws OwsExceptionReport {
        final SosSweAbstractDataComponent sosElement = sweField.getElement();
        LOGGER.trace("sweField: {}, sosElement: {}", sweField, sosElement);
        final DataRecordType.Field xbField =
                             DataRecordType.Field.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sweField.getName() != null) {
            xbField.setName(sweField.getName());
        }
        final AbstractDataComponentType xbDCD = xbField.addNewAbstractDataComponent();
        xbDCD
                .set(createAbstractDataComponent(sosElement, new EnumMap<SosConstants.HelperValues, String>(HelperValues.class)));
        if (sosElement instanceof SosSweBoolean) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_BOOLEAN_SWE_200, BooleanType.type);
        } else if (sosElement instanceof SosSweCategory) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_CATEGORY_SWE_200, CategoryType.type);
        } else if (sosElement instanceof SosSweCount) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_COUNT_SWE_200, CountType.type);
        } else if (sosElement instanceof SosSweQuantity) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_QUANTITY_SWE_200, QuantityType.type);
        } else if (sosElement instanceof SosSweText) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_TEXT_ENCODING_SWE_200, TextType.type);
        } else if (sosElement instanceof SosSweTimeRange) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_TIME_RANGE_SWE_200, TimeRangeType.type);
        } else if (sosElement instanceof SosSweTime) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_TIME_SWE_200, TimeType.type);
        } else if (sosElement instanceof SosSweDataArray) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_DATA_ARRAY_SWE_200, DataArrayType.type);
        } else if (sosElement instanceof SosSweDataRecord) {
            xbField.getAbstractDataComponent().substitute(SWEConstants.QN_DATA_RECORD_SWE_200, DataRecordType.type);
        } else {
            throw new NotYetSupportedException(SosSweAbstractDataComponent.class.getName(), sosElement);
        }
        return xbField;
    }

    /*
     *
     * SIMPLE TYPES
     */
    private AbstractDataComponentType createSimpleType(final SosSweAbstractSimpleType<?> sosSimpleType)
            throws OwsExceptionReport {
        if (sosSimpleType instanceof SosSweBoolean) {
            return createBoolean((SosSweBoolean) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweCategory) {
            return createCategoy((SosSweCategory) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweCount) {
            return createCount((SosSweCount) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweObservableProperty) {
            return createObservableProperty((SosSweObservableProperty) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweQuantity) {
            return createQuantity((SosSweQuantity) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweText) {
            return createText((SosSweText) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweTimeRange) {
            return createTimeRange((SosSweTimeRange) sosSimpleType);
        } else if (sosSimpleType instanceof SosSweTime) {
            return createTime((SosSweTime) sosSimpleType);
        }
        throw new NotYetSupportedException(SosSweAbstractSimpleType.class.getSimpleName(), sosSimpleType);
    }

    private BooleanType createBoolean(final SosSweBoolean sosElement) {
        final BooleanType xbBoolean = BooleanType.Factory.newInstance();
        if (sosElement.isSetValue())
        {
        	xbBoolean.setValue(sosElement.getValue());
        }
        return xbBoolean;
    }

    private CategoryType createCategoy(final SosSweCategory sosCategory) {
        final CategoryType xbCategory = CategoryType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sosCategory.getCodeSpace() != null) {
            final Reference xbCodespace = xbCategory.addNewCodeSpace();
            xbCodespace.setHref(sosCategory.getCodeSpace());
        }
        return xbCategory;
    }

    private CountType createCount(final SosSweCount sosCount) {
        final CountType xbCount = CountType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sosCount.isSetValue()) {
            final BigInteger bigInt = new BigInteger(Integer.toString(sosCount.getValue().intValue()));
            xbCount.setValue(bigInt);
        }
        return xbCount;
    }

    private AbstractDataComponentType createObservableProperty(final SosSweObservableProperty sosSweAbstractDataComponent) {
        throw new RuntimeException("NOT YET IMPLEMENTED: encoding of swe:ObservableProperty");
    }

    private QuantityType createQuantity(final SosSweQuantity quantity) {
        final QuantityType xbQuantity = QuantityType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (quantity.isSetAxisID()) {
            xbQuantity.setAxisID(quantity.getDescription());
        }
        if (quantity.isSetValue()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.isSetUom()) {
            xbQuantity.addNewUom().setCode(quantity.getUom());
        }
        if (quantity.getQuality() != null) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbQuantity.schemaType());
        }
        return xbQuantity;
    }

    private TextType createText(final SosSweText text) {
        final TextType xbText = TextType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (text.isSetValue()) {
            xbText.setValue(text.getValue());
        }
        return xbText;
    }

    private TimeType createTime(final SosSweTime sosTime) {
        final TimeType xbTime = TimeType.Factory.newInstance();
        if (sosTime.isSetValue()) {
            xbTime.setValue(sosTime.getValue());
        }
        if (sosTime.isSetUom()) {
            xbTime.addNewUom().setHref(sosTime.getUom());
        }
        if (sosTime.getQuality() != null) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbTime.schemaType());
        }
        return xbTime;
    }

    private TimeRangeType createTimeRange(final SosSweTimeRange sosTimeRange) {
        final TimeRangeType xbTimeRange = TimeRangeType.Factory.newInstance();
        if (sosTimeRange.isSetUom()) {
            xbTimeRange.addNewUom().setHref(sosTimeRange.getUom());
        }
        if (sosTimeRange.isSetValue()) {
            xbTimeRange.setValue(sosTimeRange.getValue().getRangeAsStringList());
        }
        if (sosTimeRange.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbTimeRange.schemaType());
        }
        return xbTimeRange;
    }

    private Coordinate createCoordinate(final SosSweCoordinate<?> coordinate) {
        final Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(createQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

    private AbstractEncodingType createAbstractEncoding(final SosSweAbstractEncoding sosSweAbstractEncoding)
            throws OwsExceptionReport {

        if (sosSweAbstractEncoding instanceof SosSweTextEncoding) {
            return createTextEncoding((SosSweTextEncoding) sosSweAbstractEncoding);
        }

        try {
            if ((sosSweAbstractEncoding.getXml() != null) && !sosSweAbstractEncoding.getXml().isEmpty()) {
                final XmlObject xmlObject = XmlObject.Factory.parse(sosSweAbstractEncoding.getXml());
                if (xmlObject instanceof AbstractEncodingType) {
                    return (AbstractEncodingType) xmlObject;
                }
            }
            throw new NoApplicableCodeException().withMessage("AbstractEncoding can not be encoded!");
        } catch (final XmlException e) {
            throw new NoApplicableCodeException().withMessage("Error while encoding AbstractEncoding!");
        }
    }

    private TextEncodingType createTextEncoding(final SosSweTextEncoding sosTextEncoding) {
        final TextEncodingType xbTextEncoding =
                         TextEncodingType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sosTextEncoding.getBlockSeparator() != null) {
            xbTextEncoding.setBlockSeparator(sosTextEncoding.getBlockSeparator());
        }
        if (sosTextEncoding.isSetCollapseWhiteSpaces()) {
            xbTextEncoding.setCollapseWhiteSpaces(sosTextEncoding.isCollapseWhiteSpaces());
        }
        if (sosTextEncoding.getDecimalSeparator() != null) {
            xbTextEncoding.setDecimalSeparator(sosTextEncoding.getDecimalSeparator());
        }
        if (sosTextEncoding.getTokenSeparator() != null) {
            xbTextEncoding.setTokenSeparator(sosTextEncoding.getTokenSeparator());
        }
        return xbTextEncoding;
    }
}
