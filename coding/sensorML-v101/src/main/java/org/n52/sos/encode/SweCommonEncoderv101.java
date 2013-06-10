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

import static org.n52.sos.util.HTTPConstants.StatusCode.BAD_REQUEST;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.StringOrRefType;
import net.opengis.swe.x101.AbstractDataComponentType;
import net.opengis.swe.x101.AbstractEncodingType;
import net.opengis.swe.x101.AnyScalarPropertyType;
import net.opengis.swe.x101.BlockEncodingPropertyType;
import net.opengis.swe.x101.CategoryDocument.Category;
import net.opengis.swe.x101.CountDocument.Count;
import net.opengis.swe.x101.DataArrayDocument;
import net.opengis.swe.x101.DataArrayType;
import net.opengis.swe.x101.DataComponentPropertyType;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.DataValuePropertyType;
import net.opengis.swe.x101.EnvelopeType;
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.QuantityRangeDocument.QuantityRange;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TextBlockDocument.TextBlock;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.TimeGeometricPrimitivePropertyType;
import net.opengis.swe.x101.TimeRangeDocument.TimeRange;
import net.opengis.swe.x101.UomPropertyType;
import net.opengis.swe.x101.VectorPropertyType;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.joda.time.DateTime;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweCoordinate;
import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweEnvelope;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.SweSimpleDataRecord;
import org.n52.sos.ogc.swe.SweVector;
import org.n52.sos.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SweBoolean;
import org.n52.sos.ogc.swe.simpleType.SweCategory;
import org.n52.sos.ogc.swe.simpleType.SweCount;
import org.n52.sos.ogc.swe.simpleType.SweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.ogc.swe.simpleType.SweQuantityRange;
import org.n52.sos.ogc.swe.simpleType.SweText;
import org.n52.sos.ogc.swe.simpleType.SweTime;
import org.n52.sos.ogc.swe.simpleType.SweTimeRange;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encoder class for SWE Common 1.0.1
 */
public class SweCommonEncoderv101 implements Encoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweCommonEncoderv101.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(SWEConstants.NS_SWE_101,
            SweBoolean.class, SweCategory.class, SweCount.class, SweObservableProperty.class,
            SweQuantity.class, SweQuantityRange.class, SweText.class, SweTime.class,
            SweTimeRange.class, SweEnvelope.class, SweCoordinate.class, SweDataArray.class,
            SweDataRecord.class, SweSimpleDataRecord.class, TimePeriod.class);

    public SweCommonEncoderv101() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(final Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWE_101, SWEConstants.NS_SWE_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.set(SWEConstants.SWE_101_SCHEMA_LOCATION);
    }

    @Override
    public XmlObject encode(final Object element) throws OwsExceptionReport {
        return encode(element, null);
    }

    @Override
    public XmlObject encode(final Object element, final Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (element instanceof SweBoolean) {
            return createBoolean((SweBoolean) element);
        } else if (element instanceof SweCategory) {
            return createCategory((SweCategory) element);
        } else if (element instanceof SweCount) {
            return createCount((SweCount) element);
        } else if (element instanceof SweObservableProperty) {
            return createObservableProperty((SweObservableProperty) element);
        } else if (element instanceof SweQuantity) {
            return createQuantity((SweQuantity) element);
        } else if (element instanceof SweQuantityRange) {
            return createQuantityRange((SweQuantityRange) element);
        } else if (element instanceof SweText) {
            return createText((SweText) element);
        } else if (element instanceof SweTime) {
            return createTime((SweTime) element);
        } else if (element instanceof SweTimeRange) {
            return createTimeRange((SweTimeRange) element);
        } else if (element instanceof SweCoordinate) {
            return createCoordinate((SweCoordinate<?>) element);
        } else if (element instanceof SweDataArray) {
            return createDataArray((SweDataArray) element);
        } else if (element instanceof SweDataRecord) {
        	return createDataRecord((SweDataRecord) element);
        } else if (element instanceof SweEnvelope) {
            return createEnvelope((SweEnvelope) element);
        } else if (element instanceof SweSimpleDataRecord) {
            return createSimpleDataRecord((SweSimpleDataRecord) element);
        } else if (element instanceof TimePeriod) {
            return createTimeGeometricPrimitivePropertyType((TimePeriod) element);
        }
        throw new UnsupportedEncoderInputException(this, element);
    }

    private SimpleDataRecordType createSimpleDataRecord(final SweSimpleDataRecord simpleDataRecord)
            throws OwsExceptionReport {
        final SimpleDataRecordType xbSimpleDataRecord =
                SimpleDataRecordType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (simpleDataRecord.isSetDefinition()) {
            xbSimpleDataRecord.setDefinition(simpleDataRecord.getDefinition());
        }
        if (simpleDataRecord.isSetDescription()) {
            final StringOrRefType xbSoR = StringOrRefType.Factory.newInstance();
            xbSoR.setStringValue(simpleDataRecord.getDefinition());
            xbSimpleDataRecord.setDescription(xbSoR);
        }
        if (simpleDataRecord.isSetFields()) {
            final AnyScalarPropertyType[] xbFields = new AnyScalarPropertyType[simpleDataRecord.getFields().size()];
            int xbFieldIndex = 0;
            for (final SweField sweField : simpleDataRecord.getFields()) {
                final AnyScalarPropertyType xbField = createFieldForSimpleDataRecord(sweField);
                xbFields[xbFieldIndex] = xbField;
                xbFieldIndex++;
            }
            xbSimpleDataRecord.setFieldArray(xbFields);
        }
        return xbSimpleDataRecord;
    }

    private AnyScalarPropertyType createFieldForSimpleDataRecord(final SweField sweField) throws OwsExceptionReport {
        final SweAbstractDataComponent sosElement = sweField.getElement();
        final AnyScalarPropertyType xbField =
                AnyScalarPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sweField.getName() != null) {
            xbField.setName(sweField.getName());
        }
        final AbstractDataComponentType xbDCD;
        if (sosElement instanceof SweBoolean) {
            xbDCD = xbField.addNewBoolean();
            xbDCD.set(createBoolean((SweBoolean) sosElement));
        } else if (sosElement instanceof SweCategory) {
            xbDCD = xbField.addNewCategory();
            xbDCD.set(createCategory((SweCategory) sosElement));
        } else if (sosElement instanceof SweCount) {
            xbDCD = xbField.addNewCount();
            xbDCD.set(createCount((SweCount) sosElement));
        } else if (sosElement instanceof SweQuantity) {
            xbDCD = xbField.addNewQuantity();
            xbDCD.set(createQuantity((SweQuantity) sosElement));
        } else if (sosElement instanceof SweText) {
            xbDCD = xbField.addNewText();
            xbDCD.set(createText((SweText) sosElement));
        } else if (sosElement instanceof SweTime) {
            xbDCD = xbField.addNewTime();
            xbDCD.set(createTime((SweTime) sosElement));
        } else {
            throw new NoApplicableCodeException().withMessage(
                    "The element type '%s' of the received %s is not supported by this encoder '%s'.",
                    sosElement != null ? sosElement.getClass().getName() : null, sweField.getClass().getName(),
                    getClass().getName()).setStatus(BAD_REQUEST);
        }
        return xbField;
    }

    private DataComponentPropertyType createField(final SweField sweField) throws OwsExceptionReport {
        final SweAbstractDataComponent sosElement = sweField.getElement();
        final DataComponentPropertyType xbField =
                DataComponentPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sweField.getName() != null) {
            xbField.setName(sweField.getName());
        }
        final AbstractDataComponentType xbDCD = xbField.addNewAbstractDataArray1();
        if (sosElement instanceof SweBoolean) {
            xbDCD.set(createBoolean((SweBoolean) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_BOOLEAN_SWE_101,
                    net.opengis.swe.x101.BooleanDocument.Boolean.type);
        } else if (sosElement instanceof SweCategory) {
            xbDCD.set(createCategory((SweCategory) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_CATEGORY_SWE_101, Category.type);
        } else if (sosElement instanceof SweCount) {
            xbDCD.set(createCount((SweCount) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_COUNT_SWE_101, Count.type);
        } else if (sosElement instanceof SweQuantity) {
            xbDCD.set(createQuantity((SweQuantity) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_QUANTITY_SWE_101, Quantity.type);
        } else if (sosElement instanceof SweText) {
            xbDCD.set(createText((SweText) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_TEXT_ENCODING_SWE_101, Text.type);
        } else if (sosElement instanceof SweTimeRange) {
            xbDCD.set(createTimeRange((SweTimeRange) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_TIME_RANGE_SWE_101, TimeRange.type);
        } else if (sosElement instanceof SweTime) {
            xbDCD.set(createTime((SweTime) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_TIME_SWE_101, Time.type);
        } else if (sosElement instanceof SweEnvelope) {
            xbDCD.set(createEnvelope((SweEnvelope) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_ENVELOPE_SWE_101, EnvelopeType.type);
        } else {
            throw new NoApplicableCodeException().withMessage(
                    "The element type '%s' of the received %s is not supported by this encoder '%s'.",
                    sosElement != null ? sosElement.getClass().getName() : null, sweField.getClass().getName(),
                    getClass().getName()).setStatus(BAD_REQUEST);
        }
        return xbField;
    }

    private net.opengis.swe.x101.BooleanDocument.Boolean createBoolean(final SweBoolean bool) {
        final net.opengis.swe.x101.BooleanDocument.Boolean xbBoolean =
                net.opengis.swe.x101.BooleanDocument.Boolean.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        addAbstractDataComponentValues(xbBoolean, bool);
        if (bool.isSetValue()) {
            xbBoolean.setValue(bool.getValue().booleanValue());
        }
        if (bool.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbBoolean.schemaType());
        }
        return xbBoolean;
    }

    private Category createCategory(final SweCategory category) {
        final Category xbCategory = Category.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCategory, category);
        if (category.isSetValue()) {
            xbCategory.setValue(category.getValue());
        }
        if (category.isSetCodeSpace()) {
            xbCategory.addNewCodeSpace().setHref(category.getCodeSpace());
        }
        if (category.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbCategory.schemaType());
        }
        return xbCategory;
    }

    private Count createCount(final SweCount count) {
        final Count xbCount = Count.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCount, count);
        if (count.isSetValue()) {
            xbCount.setValue(new BigInteger(Integer.toString(count.getValue().intValue())));
        }
        if (count.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbCount.schemaType());
        }
        return xbCount;
    }

    private ObservableProperty createObservableProperty(final SweObservableProperty observableProperty) {
        final ObservableProperty xbObservableProperty =
                ObservableProperty.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbObservableProperty, observableProperty);
        if (observableProperty.isSetQuality()) {
            // TODO implementschemaType()
            LOGGER.warn("Quality encoding is not supported for {}", xbObservableProperty.schemaType());
        }
        return xbObservableProperty;
    }

    /**
     * Adds values to SWE quantity
     * 
     * @param quantity
     *            SOS internal representation
     */
    protected Quantity createQuantity(final SweQuantity quantity) {
        final Quantity xbQuantity = Quantity.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbQuantity, quantity);
        if (quantity.isSetAxisID()) {
            xbQuantity.setAxisID(quantity.getAxisID());
        }
        if (quantity.isSetValue()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.isSetUom()) {
            xbQuantity.addNewUom().set(createUom(quantity.getUom()));
        }
        if (quantity.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbQuantity.schemaType());
        }
        return xbQuantity;
    }

    protected QuantityRange createQuantityRange(final SweQuantityRange quantityRange) {
        final QuantityRange xbQuantityRange =
                QuantityRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbQuantityRange, quantityRange);
        if (quantityRange.isSetAxisID()) {
            xbQuantityRange.setAxisID(quantityRange.getDescription());
        }
        if (quantityRange.isSetValue()) {
            xbQuantityRange.setValue(quantityRange.getValue().getRangeAsList());
        }
        if (quantityRange.isSetUom()) {
            xbQuantityRange.addNewUom().set(createUom(quantityRange.getUom()));
        }
        if (quantityRange.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbQuantityRange.schemaType());
        }
        return xbQuantityRange;
    }

    /**
     * Adds values to SWE text
     * 
     * @param text
     *            SOS internal representation
     */
    private Text createText(final SweText text) {
        final Text xbText = Text.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbText, text);
        if (text.isSetValue()) {
            xbText.setValue(text.getValue());
        }
        if (text.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbText.toString());
        }
        return xbText;
    }

    private Time createTime(final SweTime time) {
        final Time xbTime = Time.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbTime, time);
        if (time.isSetValue()) {
            final XmlDateTime xbDateTime = createDateTime(time.getValue());
            xbTime.setValue(xbDateTime);
        }
        if (time.isSetUom()) {
            xbTime.addNewUom().setCode(time.getUom());
        }
        if (time.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbTime.schemaType());
        }
        return xbTime;
    }

    private XmlDateTime createDateTime(final DateTime sosDateTime) {
        final XmlDateTime xbDateTime = XmlDateTime.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbDateTime.setDateValue(sosDateTime.toDate());
        return xbDateTime;
    }

    private EnvelopeType createEnvelope(final SweEnvelope sosSweEnvelope) {
        final EnvelopeType envelopeType = EnvelopeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(envelopeType, sosSweEnvelope);
        if (sosSweEnvelope.isReferenceFrameSet()) {
            envelopeType.setReferenceFrame(sosSweEnvelope.getReferenceFrame());
        }
        if (sosSweEnvelope.isLowerCornerSet()) {
            envelopeType.setLowerCorner(createVectorProperty(sosSweEnvelope.getLowerCorner()));
        }
        if (sosSweEnvelope.isUpperCornerSet()) {
            envelopeType.setUpperCorner(createVectorProperty(sosSweEnvelope.getUpperCorner()));
        }
        if (sosSweEnvelope.isTimeSet()) {
            envelopeType.addNewTime().setTimeRange(createTimeRange(sosSweEnvelope.getTime()));
        }
        return envelopeType;
    }

    private VectorPropertyType createVectorProperty(final SweVector sosSweVector) {
        final VectorPropertyType vectorPropertyType =
                VectorPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        vectorPropertyType.setVector(createVector(sosSweVector.getCoordinates()));
        return vectorPropertyType;
    }

    private VectorType createVector(final List<SweCoordinate<?>> coordinates) {
        final VectorType vectorType = VectorType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        vectorType.setCoordinateArray(createCoordinates(coordinates));
        return vectorType;
    }

    private TimeRange createTimeRange(final SweTimeRange timeRange) {
        final TimeRange xbTimeRange = TimeRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbTimeRange, timeRange);
        if (timeRange.isSetValue()) {
            xbTimeRange.setValue(timeRange.getValue().getRangeAsStringList());
        }
        if (timeRange.isSetUom()) {
            xbTimeRange.addNewUom().setCode(timeRange.getUom());
        }
        if (timeRange.isSetQuality()) {
            // TODO implement
            LOGGER.warn("Quality encoding is not supported for {}", xbTimeRange.schemaType());
        }
        return xbTimeRange;
    }

    private void addAbstractDataComponentValues(final AbstractDataComponentType xbComponent,
            final SweAbstractDataComponent component) {
        if (component.isSetDefinition()) {
            xbComponent.setDefinition(component.getDefinition());
        }
        if (component.isSetDescription()) {
            xbComponent.addNewDescription().setStringValue(component.getDescription());
        }
    }

    /**
     * Adds values to SWE coordinates
     * 
     * @param coordinate
     *            SOS internal representation
     */
    private Coordinate createCoordinate(final SweCoordinate<?> coordinate) {
        final Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(createQuantity((SweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

    /**
     * Adds values to SWE coordinates
     * 
     * @param coordinates
     *            SOS internal representation
     */
    private Coordinate[] createCoordinates(final List<SweCoordinate<?>> coordinates) {
        if (coordinates != null) {
            final ArrayList<Coordinate> xbCoordinates = new ArrayList<Coordinate>(coordinates.size());
            for (final SweCoordinate<?> coordinate : coordinates) {
                xbCoordinates.add(createCoordinate(coordinate));
            }
            return xbCoordinates.toArray(new Coordinate[xbCoordinates.size()]);
        }
        return null;
    }

    // TODO check types for SWE101
    private DataRecordType createDataRecord(final SweDataRecord sosDataRecord) throws OwsExceptionReport {

        final List<SweField> sosFields = sosDataRecord.getFields();

        final DataRecordType xbDataRecord = DataRecordType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());

        if (sosDataRecord.isSetFields()) {
            final DataComponentPropertyType[] xbFields = new DataComponentPropertyType[sosFields.size()];
            int xbFieldIndex = 0;
            for (final SweField sosSweField : sosFields) {
                final DataComponentPropertyType xbField = createField(sosSweField);
                xbFields[xbFieldIndex] = xbField;
                xbFieldIndex++;
            }
            xbDataRecord.setFieldArray(xbFields);
        }
        return xbDataRecord;
    }

    private DataArrayDocument createDataArray(final SweDataArray sosDataArray) throws OwsExceptionReport {
        if (sosDataArray != null) {

            final DataArrayDocument xbDataArrayDoc =
                    DataArrayDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            final DataArrayType xbDataArray = xbDataArrayDoc.addNewDataArray1();

            // set element count
            if (sosDataArray.getElementCount() != null) {
                xbDataArray.addNewElementCount().addNewCount().set(createCount(sosDataArray.getElementCount()));
            }

            if (sosDataArray.getElementType() != null) {
                final DataComponentPropertyType xbElementType = xbDataArray.addNewElementType();
                xbDataArray.getElementType().setName("Components");

                final DataRecordType xbDataRecord =
                        createDataRecord((SweDataRecord) sosDataArray.getElementType());
                xbElementType.set(xbDataRecord);
            }

            if (sosDataArray.getEncoding() != null) {

                final BlockEncodingPropertyType xbEncoding = xbDataArray.addNewEncoding();
                xbEncoding.set(createBlockEncoding(sosDataArray.getEncoding()));
                // xbDataArray.getEncoding().substitute(
                // new QName(SWEConstants.NS_SWE_101,
                // SWEConstants.EN_TEXT_ENCODING,
                // SWEConstants.NS_SWE_PREFIX), TextBlock.type);
            }
            final DataValuePropertyType xbValues = xbDataArray.addNewValues();
            // if (absObs.getObservationTemplateIDs() == null
            // || (absObs.getObservationTemplateIDs() != null &&
            // absObs.getObservationTemplateIDs().isEmpty())) {
            // xbValues.newCursor().setTextValue(createResultString(phenComponents,
            // absObs));
            // }
            if (sosDataArray.isSetValues()) {
                xbValues.set(createValues(sosDataArray.getValues(), sosDataArray.getEncoding()));
            }
            return xbDataArrayDoc;
        }
        return null;
    }

    private XmlString createValues(final List<List<String>> values, final SweAbstractEncoding encoding) {
        // TODO How to deal with the decimal separator - is it an issue here?
        final StringBuilder valueStringBuilder = new StringBuilder(256);
        final SweTextEncoding textEncoding = (SweTextEncoding) encoding;
        // would also work, hmm
        // Configurator.getInstance().getDecimalSeparator();
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

    private BlockEncodingPropertyType createBlockEncoding(final SweAbstractEncoding sosSweAbstractEncoding)
            throws OwsExceptionReport {

        try {
            if (sosSweAbstractEncoding instanceof SweTextEncoding) {
                return createTextEncoding((SweTextEncoding) sosSweAbstractEncoding);
            }
            if (sosSweAbstractEncoding.getXml() != null && !sosSweAbstractEncoding.getXml().isEmpty()) {
                final XmlObject xmlObject = XmlObject.Factory.parse(sosSweAbstractEncoding.getXml());
                if (xmlObject instanceof AbstractEncodingType) {
                    return (BlockEncodingPropertyType) xmlObject;
                }
                throw new NoApplicableCodeException().withMessage("AbstractEncoding can not be encoded!");
            }

        } catch (final XmlException e) {
            throw new NoApplicableCodeException().causedBy(e).withMessage("Error while encoding AbstractEncoding!");
        } catch (final XmlValueDisconnectedException xvde) {
            throw new NoApplicableCodeException().causedBy(xvde).withMessage("Error while encoding AbstractEncoding!");
        } catch (final Exception ge) {
            throw new NoApplicableCodeException().causedBy(ge).withMessage("Error while encoding AbstractEncoding!");
        }
        return null;
    }

    private BlockEncodingPropertyType createTextEncoding(final SweTextEncoding sosTextEncoding) {
        final BlockEncodingPropertyType xbTextEncodingType =
                BlockEncodingPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final TextBlock xbTextEncoding = xbTextEncodingType.addNewTextBlock();

        if (sosTextEncoding.getBlockSeparator() != null) {
            xbTextEncoding.setBlockSeparator(sosTextEncoding.getBlockSeparator());
        }
        // TODO check not used in SWE101
        // if (sosTextEncoding.isSetCollapseWhiteSpaces()) {
        // xbTextEncoding.setCollapseWhiteSpaces(sosTextEncoding.isCollapseWhiteSpaces());
        // }
        if (sosTextEncoding.getDecimalSeparator() != null) {
            xbTextEncoding.setDecimalSeparator(sosTextEncoding.getDecimalSeparator());
        }
        if (sosTextEncoding.getTokenSeparator() != null) {
            xbTextEncoding.setTokenSeparator(sosTextEncoding.getTokenSeparator());
        }
        // wont cast !!! net.opengis.swe.x101.impl.BlockEncodingPropertyTypeImpl
        // cannot be cast to net.opengis.swe.x101.AbstractEncodingType
        return xbTextEncodingType;
    }

    private XmlObject createTimeGeometricPrimitivePropertyType(final TimePeriod timePeriod) throws OwsExceptionReport {
        final TimeGeometricPrimitivePropertyType xbTimeGeometricPrimitiveProperty =
                TimeGeometricPrimitivePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (timePeriod.isSetStart() && timePeriod.isSetEnd()) {
            xbTimeGeometricPrimitiveProperty.addNewTimeGeometricPrimitive().set(
                    CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, timePeriod));
        }
        // TODO check GML 311 rename nodename of geometric primitive to
        // gml:timePeriod
        final XmlCursor timeCursor = xbTimeGeometricPrimitiveProperty.newCursor();
        final boolean hasTimePrimitive =
                timeCursor.toChild(new QName(GMLConstants.NS_GML, GMLConstants.EN_ABSTRACT_TIME_GEOM_PRIM));
        if (hasTimePrimitive) {
            timeCursor.setName(new QName(GMLConstants.NS_GML, GMLConstants.EN_TIME_PERIOD));
        }
        timeCursor.dispose();
        return xbTimeGeometricPrimitiveProperty;
    }
    
    private UomPropertyType createUom(String uom) {
        UomPropertyType xbUom = UomPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (uom.startsWith("urn:") || uom.startsWith("http://")) {
            xbUom.setHref(uom);
        } else {
            xbUom.setCode(uom);
        }
        return xbUom;
    }    
}
