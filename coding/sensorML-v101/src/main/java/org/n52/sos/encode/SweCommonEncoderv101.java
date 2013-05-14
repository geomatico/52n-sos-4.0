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
import net.opengis.swe.x101.DataRecordDocument;
import net.opengis.swe.x101.DataRecordType;
import net.opengis.swe.x101.DataValuePropertyType;
import net.opengis.swe.x101.EnvelopeDocument;
import net.opengis.swe.x101.EnvelopeType;
import net.opengis.swe.x101.TimeGeometricPrimitivePropertyType;
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.QuantityRangeDocument.QuantityRange;
import net.opengis.swe.x101.SimpleDataRecordType;
import net.opengis.swe.x101.TextBlockDocument.TextBlock;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.TimeRangeDocument.TimeRange;
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
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweCoordinate;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweEnvelope;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.SosSweSimpleDataRecord;
import org.n52.sos.ogc.swe.SosSweVector;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantityRange;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
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
            SosSweBoolean.class, SosSweCategory.class, SosSweCount.class, SosSweObservableProperty.class,
            SosSweQuantity.class, SosSweQuantityRange.class, SosSweText.class, SosSweTime.class,
            SosSweTimeRange.class, SosSweEnvelope.class, SosSweCoordinate.class, SosSweDataArray.class,
            SosSweSimpleDataRecord.class, TimePeriod.class);

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
    public XmlObject encode(final Object element) throws OwsExceptionReport {
        return encode(element, null);
    }

    @Override
    public XmlObject encode(final Object element, final Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (element instanceof SosSweBoolean) {
            return createBoolean((SosSweBoolean) element);
        } else if (element instanceof SosSweCategory) {
            return createCategory((SosSweCategory) element);
        } else if (element instanceof SosSweCount) {
            return createCount((SosSweCount) element);
        } else if (element instanceof SosSweObservableProperty) {
            return createObservableProperty((SosSweObservableProperty) element);
        } else if (element instanceof SosSweQuantity) {
            return createQuantity((SosSweQuantity) element);
        } else if (element instanceof SosSweQuantityRange) {
            return createQuantityRange((SosSweQuantityRange) element);
        } else if (element instanceof SosSweText) {
            return createText((SosSweText) element);
        } else if (element instanceof SosSweTime) {
            return createTime((SosSweTime) element);
        } else if (element instanceof SosSweTimeRange) {
            return createTimeRange((SosSweTimeRange) element);
        } else if (element instanceof SosSweCoordinate) {
            return createCoordinate((SosSweCoordinate<?>) element);
        } else if (element instanceof SosSweDataArray) {
            return createDataArray((SosSweDataArray) element);
        } else if (element instanceof SosSweEnvelope) {
            return createEnvelope((SosSweEnvelope) element);
        } else if (element instanceof SosSweSimpleDataRecord) {
            return createSimpleDataRecord((SosSweSimpleDataRecord) element);
        } else if (element instanceof TimePeriod) {
            return createTimeGeometricPrimitivePropertyType((TimePeriod) element);
        }
        throw new UnsupportedEncoderInputException(this, element);
    }

    private SimpleDataRecordType createSimpleDataRecord(final SosSweSimpleDataRecord simpleDataRecord)
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
            for (final SosSweField sweField : simpleDataRecord.getFields()) {
                final AnyScalarPropertyType xbField = createFieldForSimpleDataRecord(sweField);
                xbFields[xbFieldIndex] = xbField;
                xbFieldIndex++;
            }
            xbSimpleDataRecord.setFieldArray(xbFields);
        }
        return xbSimpleDataRecord;
    }

    private AnyScalarPropertyType createFieldForSimpleDataRecord(final SosSweField sweField) throws OwsExceptionReport {
        final SosSweAbstractDataComponent sosElement = sweField.getElement();
        final AnyScalarPropertyType xbField =
                AnyScalarPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sweField.getName() != null) {
            xbField.setName(sweField.getName());
        }
        final AbstractDataComponentType xbDCD;
        if (sosElement instanceof SosSweBoolean) {
            xbDCD = xbField.addNewBoolean();
            xbDCD.set(createBoolean((SosSweBoolean) sosElement));
        } else if (sosElement instanceof SosSweCategory) {
            xbDCD = xbField.addNewCategory();
            xbDCD.set(createCategory((SosSweCategory) sosElement));
        } else if (sosElement instanceof SosSweCount) {
            xbDCD = xbField.addNewCount();
            xbDCD.set(createCount((SosSweCount) sosElement));
        } else if (sosElement instanceof SosSweQuantity) {
            xbDCD = xbField.addNewQuantity();
            xbDCD.set(createQuantity((SosSweQuantity) sosElement));
        } else if (sosElement instanceof SosSweText) {
            xbDCD = xbField.addNewText();
            xbDCD.set(createText((SosSweText) sosElement));
        } else if (sosElement instanceof SosSweTime) {
            xbDCD = xbField.addNewTime();
            xbDCD.set(createTime((SosSweTime) sosElement));
        } else {
            throw new NoApplicableCodeException().withMessage(
                    "The element type '%s' of the received %s is not supported by this encoder '%s'.",
                    sosElement != null ? sosElement.getClass().getName() : null, sweField.getClass().getName(),
                    getClass().getName()).setStatus(BAD_REQUEST);
        }
        return xbField;
    }

    private DataComponentPropertyType createField(final SosSweField sweField) throws OwsExceptionReport {
        final SosSweAbstractDataComponent sosElement = sweField.getElement();
        final DataComponentPropertyType xbField =
                DataComponentPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sweField.getName() != null) {
            xbField.setName(sweField.getName());
        }
        final AbstractDataComponentType xbDCD = xbField.addNewAbstractDataArray1();
        if (sosElement instanceof SosSweBoolean) {
            xbDCD.set(createBoolean((SosSweBoolean) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_BOOLEAN_SWE_101,
                    net.opengis.swe.x101.BooleanDocument.Boolean.type);
        } else if (sosElement instanceof SosSweCategory) {
            xbDCD.set(createCategory((SosSweCategory) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_CATEGORY_SWE_101, Category.type);
        } else if (sosElement instanceof SosSweCount) {
            xbDCD.set(createCount((SosSweCount) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_COUNT_SWE_101, Count.type);
        } else if (sosElement instanceof SosSweQuantity) {
            xbDCD.set(createQuantity((SosSweQuantity) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_QUANTITY_SWE_101, Quantity.type);
        } else if (sosElement instanceof SosSweText) {
            xbDCD.set(createText((SosSweText) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_TEXT_ENCODING_SWE_101, Text.type);
        } else if (sosElement instanceof SosSweTimeRange) {
            xbDCD.set(createTimeRange((SosSweTimeRange) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_TIME_RANGE_SWE_101, TimeRange.type);
        } else if (sosElement instanceof SosSweTime) {
            xbDCD.set(createTime((SosSweTime) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_TIME_SWE_101, Time.type);
        } else if (sosElement instanceof SosSweEnvelope) {
            xbDCD.set(createEnvelope((SosSweEnvelope) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_ENVELOPE_SWE_101, EnvelopeType.type);
        } else {
            throw new NoApplicableCodeException().withMessage(
                    "The element type '%s' of the received %s is not supported by this encoder '%s'.",
                    sosElement != null ? sosElement.getClass().getName() : null, sweField.getClass().getName(),
                    getClass().getName()).setStatus(BAD_REQUEST);
        }
        return xbField;
    }

    private net.opengis.swe.x101.BooleanDocument.Boolean createBoolean(final SosSweBoolean bool) {
        final net.opengis.swe.x101.BooleanDocument.Boolean xbBoolean =
                net.opengis.swe.x101.BooleanDocument.Boolean.Factory.newInstance(XmlOptionsHelper.getInstance()
                        .getXmlOptions());
        addAbstractDataComponentValues(xbBoolean, bool);
        if (bool.isSetValue()) {
            xbBoolean.setValue(bool.getValue().booleanValue());
        }
        if (bool.isSetQuality()) {
            // TODO
        }
        return xbBoolean;
    }

    private Category createCategory(final SosSweCategory category) {
        final Category xbCategory = Category.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCategory, category);
        if (category.isSetValue()) {
            xbCategory.setValue(category.getValue());
        }
        if (category.isSetCodeSpace()) {
            xbCategory.addNewCodeSpace().setHref(category.getCodeSpace());
        }
        if (category.isSetQuality()) {
            // TODO
        }
        return xbCategory;
    }

    private Count createCount(final SosSweCount count) {
        final Count xbCount = Count.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCount, count);
        if (count.isSetValue()) {
            xbCount.setValue(new BigInteger(Integer.toString(count.getValue().intValue())));
        }
        if (count.isSetQuality()) {
            // TODO
        }
        return xbCount;
    }

    private ObservableProperty createObservableProperty(final SosSweObservableProperty observableProperty) {
        final ObservableProperty xbObservableProperty =
                ObservableProperty.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbObservableProperty, observableProperty);
        if (observableProperty.isSetQuality()) {
            // TODO
        }
        return xbObservableProperty;
    }

    /**
     * Adds values to SWE quantity
     * 
     * @param quantity
     *            SOS internal representation
     */
    private Quantity createQuantity(final SosSweQuantity quantity) {
        final Quantity xbQuantity = Quantity.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbQuantity, quantity);
        if (quantity.isSetAxisID()) {
            xbQuantity.setAxisID(quantity.getAxisID());
        }
        if (quantity.isSetValue()) {
            xbQuantity.setValue(Double.valueOf(quantity.getValue()));
        }
        if (quantity.isSetUom()) {
            xbQuantity.addNewUom().setCode(quantity.getUom());
        }
        if (quantity.isSetQuality()) {
            // TODO
        }
        return xbQuantity;
    }

    private QuantityRange createQuantityRange(final SosSweQuantityRange quantityRange) {
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
            xbQuantityRange.addNewUom().setCode(quantityRange.getUom());
        }
        if (quantityRange.isSetQuality()) {
            // TODO
        }
        return xbQuantityRange;
    }

    /**
     * Adds values to SWE text
     * 
     * @param text
     *            SOS internal representation
     */
    private Text createText(final SosSweText text) {
        final Text xbText = Text.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbText, text);
        if (text.isSetValue()) {
            xbText.setValue(text.getValue());
        }
        if (text.isSetQuality()) {
            // TODO
        }
        return xbText;
    }

    private Time createTime(final SosSweTime time) {
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
            // TODO
        }
        return xbTime;
    }

    private XmlDateTime createDateTime(final DateTime sosDateTime) {
        final XmlDateTime xbDateTime = XmlDateTime.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbDateTime.setDateValue(sosDateTime.toDate());
        return xbDateTime;
    }

    private EnvelopeDocument createEnvelope(final SosSweEnvelope sosSweEnvelope) {
        final EnvelopeDocument envelopeDocument =
                EnvelopeDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final EnvelopeType envelopeType = envelopeDocument.addNewEnvelope();
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
        return envelopeDocument;
    }

    private VectorPropertyType createVectorProperty(final SosSweVector sosSweVector) {
        final VectorPropertyType vectorPropertyType =
                VectorPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        vectorPropertyType.setVector(createVector(sosSweVector.getCoordinates()));
        return vectorPropertyType;
    }

    private VectorType createVector(final List<SosSweCoordinate<?>> coordinates) {
        final VectorType vectorType = VectorType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        vectorType.setCoordinateArray(createCoordinates(coordinates));
        return vectorType;
    }

    private TimeRange createTimeRange(final SosSweTimeRange timeRange) {
        final TimeRange xbTimeRange = TimeRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbTimeRange, timeRange);
        if (timeRange.isSetValue()) {
            xbTimeRange.setValue(timeRange.getValue().getRangeAsStringList());
        }
        if (timeRange.isSetUom()) {
            xbTimeRange.addNewUom().setCode(timeRange.getUom());
        }
        if (timeRange.isSetQuality()) {
            // TODO
        }
        return xbTimeRange;
    }

    private void addAbstractDataComponentValues(final AbstractDataComponentType xbComponent,
            final SosSweAbstractDataComponent component) {
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
    private Coordinate createCoordinate(final SosSweCoordinate<?> coordinate) {
        final Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(createQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

    /**
     * Adds values to SWE coordinates
     * 
     * @param coordinates
     *            SOS internal representation
     */
    private Coordinate[] createCoordinates(final List<SosSweCoordinate<?>> coordinates) {
        if (coordinates != null) {
            final ArrayList<Coordinate> xbCoordinates = new ArrayList<Coordinate>(coordinates.size());
            for (final SosSweCoordinate<?> coordinate : coordinates) {
                xbCoordinates.add(createCoordinate(coordinate));
            }
            return xbCoordinates.toArray(new Coordinate[xbCoordinates.size()]);
        }
        return null;
    }

    // TODO check types for SWE101
    private DataRecordDocument createDataRecord(final SosSweDataRecord sosDataRecord) throws OwsExceptionReport {

        final List<SosSweField> sosFields = sosDataRecord.getFields();

        final DataRecordDocument xbDataRecordDoc =
                DataRecordDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        final DataRecordType xbDataRecord = xbDataRecordDoc.addNewDataRecord();

        if (sosDataRecord.isSetFields()) {
            final DataComponentPropertyType[] xbFields = new DataComponentPropertyType[sosFields.size()];
            int xbFieldIndex = 0;
            for (final SosSweField sosSweField : sosFields) {
                final DataComponentPropertyType xbField = createField(sosSweField);
                xbFields[xbFieldIndex] = xbField;
                xbFieldIndex++;
            }
            xbDataRecord.setFieldArray(xbFields);
        }
        return xbDataRecordDoc;
    }

    private DataArrayDocument createDataArray(final SosSweDataArray sosDataArray) throws OwsExceptionReport {
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

                final DataRecordDocument xbDataRecordDoc =
                        createDataRecord((SosSweDataRecord) sosDataArray.getElementType());
                xbElementType.set(xbDataRecordDoc);
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

    private XmlString createValues(final List<List<String>> values, final SosSweAbstractEncoding encoding) {
        // TODO How to deal with the decimal separator - is it an issue here?
        final StringBuilder valueStringBuilder = new StringBuilder(256);
        final SosSweTextEncoding textEncoding = (SosSweTextEncoding) encoding;
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

    private BlockEncodingPropertyType createBlockEncoding(final SosSweAbstractEncoding sosSweAbstractEncoding)
            throws OwsExceptionReport {

        try {
            if (sosSweAbstractEncoding instanceof SosSweTextEncoding) {
                return createTextEncoding((SosSweTextEncoding) sosSweAbstractEncoding);
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

    private BlockEncodingPropertyType createTextEncoding(final SosSweTextEncoding sosTextEncoding) {
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

    private XmlObject createTimeGeometricPrimitivePropertyType(TimePeriod timePeriod) throws OwsExceptionReport {
        TimeGeometricPrimitivePropertyType xbTimeGeometricPrimitiveProperty =
                TimeGeometricPrimitivePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (timePeriod.isSetStart() && timePeriod.isSetEnd()) {
            xbTimeGeometricPrimitiveProperty.addNewTimeGeometricPrimitive().set(
                    CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, timePeriod));
        }
        // TODO check GML 311 rename nodename of geometric primitive to
        // gml:timePeriod
        XmlCursor timeCursor = xbTimeGeometricPrimitiveProperty.newCursor();
        boolean hasTimePrimitive =
                timeCursor.toChild(new QName(GMLConstants.NS_GML, GMLConstants.EN_ABSTRACT_TIME_GEOM_PRIM));
        if (hasTimePrimitive) {
            timeCursor.setName(new QName(GMLConstants.NS_GML, GMLConstants.EN_TIME_PERIOD));
        }
        timeCursor.dispose();
        return xbTimeGeometricPrimitiveProperty;
    }
}
