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
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.swe.x101.AbstractDataComponentType;
import net.opengis.swe.x101.AbstractEncodingType;
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
import net.opengis.swe.x101.ObservablePropertyDocument.ObservableProperty;
import net.opengis.swe.x101.QuantityDocument.Quantity;
import net.opengis.swe.x101.QuantityRangeDocument.QuantityRange;
import net.opengis.swe.x101.TextBlockDocument.TextBlock;
import net.opengis.swe.x101.TextDocument.Text;
import net.opengis.swe.x101.TimeDocument.Time;
import net.opengis.swe.x101.TimeRangeDocument.TimeRange;
import net.opengis.swe.x101.VectorPropertyType;
import net.opengis.swe.x101.VectorType;
import net.opengis.swe.x101.VectorType.Coordinate;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
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

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(SWEConstants.NS_SWE,
                                                                                            SosSweBoolean.class,
                                                                                            SosSweCategory.class,
                                                                                            SosSweCount.class,
                                                                                            SosSweObservableProperty.class,
                                                                                            SosSweQuantity.class,
                                                                                            SosSweQuantityRange.class,
                                                                                            SosSweText.class,
                                                                                            SosSweTime.class,
                                                                                            SosSweTimeRange.class,
                                                                                            SosSweEnvelope.class,
                                                                                            SosSweCoordinate.class,
                                                                                            SosSweDataArray.class);
    
    
    public SweCommonEncoderv101() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEYS));
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
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SWEConstants.NS_SWE, SWEConstants.NS_SWE_PREFIX);
    }
    
    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }
    
    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, null);
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
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
            return createCoordinate((SosSweCoordinate) element);
        } else if (element instanceof SosSweDataArray) {
            return createDataArray((SosSweDataArray) element);
        } else if (element instanceof SosSweEnvelope) {
            return createEnvelope((SosSweEnvelope) element);
        }
        throw new UnsupportedEncoderInputException(this, element);
    }
    
    private DataComponentPropertyType createField(SosSweField sweField) throws OwsExceptionReport {
        SosSweAbstractDataComponent sosElement = sweField.getElement();
        DataComponentPropertyType xbField =
        		DataComponentPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sweField.getName() != null) {
            xbField.setName(sweField.getName());
        }
        AbstractDataComponentType xbDCD = xbField.addNewAbstractDataArray1();
        if (sosElement instanceof SosSweBoolean) {
            xbDCD.set(createBoolean((SosSweBoolean) sosElement));
            xbField.getAbstractDataArray1().substitute(SWEConstants.QN_BOOLEAN_SWE_101, net.opengis.swe.x101.BooleanDocument.Boolean.type);
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
            throw new NoApplicableCodeException()
                    .withMessage("The element type '%s' of the received %s is not supported by this encoder '%s'.",
                                 sosElement != null ? sosElement.getClass().getName() : null,
                                 sweField.getClass().getName(), getClass().getName());
        }
        return xbField;
    }
    
    private net.opengis.swe.x101.BooleanDocument.Boolean createBoolean(SosSweBoolean bool) {
        net.opengis.swe.x101.BooleanDocument.Boolean xbBoolean = net.opengis.swe.x101.BooleanDocument.Boolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbBoolean, bool);
        if (bool.isSetValue()) {
            xbBoolean.setValue(bool.getValue().booleanValue());
        }
        if (bool.isSetQuality()) {
            // TODO
        }
        return xbBoolean;
    }
    
    private Category createCategory(SosSweCategory category) {
        Category xbCategory = Category.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCategory, category);
        if (category.isSetValue()) {
            xbCategory.setValue(category.getValue());
        }
        if (category.isSetUom()) {
            xbCategory.addNewCodeSpace().setHref(category.getUom());
        }
        if (category.isSetQuality()) {
            // TODO
        }
        return xbCategory;
    }

    private Count createCount(SosSweCount count) {
        Count xbCount = Count.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbCount, count);
        if (count.isSetValue()) {
            xbCount.setValue(new BigInteger(Integer.toString(count.getValue().intValue())));
        }
        if (count.isSetQuality()) {
            // TODO
        }
        return xbCount;
    }

    private ObservableProperty createObservableProperty(SosSweObservableProperty observableProperty) {
        ObservableProperty xbObservableProperty = ObservableProperty.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
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
    private Quantity createQuantity(SosSweQuantity quantity) {
        Quantity xbQuantity = Quantity.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
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

    private QuantityRange createQuantityRange(SosSweQuantityRange quantityRange) {
        QuantityRange xbQuantityRange = QuantityRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
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
    private Text createText(SosSweText text) {
        Text xbText = Text.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbText, text);
        if (text.isSetValue()) {
            xbText.setValue(text.getValue());
        }
        if (text.isSetQuality()) {
            // TODO
        }
        return xbText;
    }
    
    private Time createTime(SosSweTime time) {
        Time xbTime = Time.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        addAbstractDataComponentValues(xbTime, time);
        if (time.isSetValue()) {
            xbTime.setValue(time.getValue());
        }
        if (time.isSetUom()) {
            xbTime.addNewUom().setCode(time.getUom());
        }
        if (time.isSetQuality()) {
            // TODO
        }
        return xbTime;
    }

    private EnvelopeDocument createEnvelope(SosSweEnvelope sosSweEnvelope) {
        EnvelopeDocument envelopeDocument = EnvelopeDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                .getXmlOptions());
        EnvelopeType envelopeType = envelopeDocument.addNewEnvelope();
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

    private VectorPropertyType createVectorProperty(SosSweVector sosSweVector) {
        VectorPropertyType vectorPropertyType = VectorPropertyType.Factory
                .newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        vectorPropertyType.setVector(createVector(sosSweVector.getCoordinates()));
        return vectorPropertyType;
    }

    private VectorType createVector(List<SosSweCoordinate<?>> coordinates) {
        VectorType vectorType = VectorType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        vectorType.setCoordinateArray(createCoordinates(coordinates));
        return vectorType;
    }
    
    private TimeRange createTimeRange(SosSweTimeRange timeRange) {
        TimeRange xbTimeRange = TimeRange.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
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
    
    private void addAbstractDataComponentValues(AbstractDataComponentType xbComponent, SosSweAbstractDataComponent component) {
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
    private Coordinate createCoordinate(SosSweCoordinate<?> coordinate) {
        Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(createQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

    /**
     * Adds values to SWE coordinates
     *
     * @param coordinates SOS internal representation
     */
    private Coordinate[] createCoordinates(List<SosSweCoordinate<?>> coordinates) {
        if (coordinates != null) {
            ArrayList<Coordinate> xbCoordinates = new ArrayList<Coordinate>(coordinates.size());
            for (SosSweCoordinate<?> coordinate : coordinates) {
                xbCoordinates.add(createCoordinate(coordinate));
            }
            return xbCoordinates.toArray(new Coordinate[xbCoordinates.size()]);
        }
        return null;
    }
    
 // TODO check types for SWE101
    private DataRecordDocument createDataRecord(SosSweDataRecord sosDataRecord) throws OwsExceptionReport {
    	
        List<SosSweField> sosFields = sosDataRecord.getFields();
        
        DataRecordDocument xbDataRecordDoc = DataRecordDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        DataRecordType xbDataRecord = xbDataRecordDoc.addNewDataRecord();
        
        if (sosFields != null) {
        	DataComponentPropertyType[] xbFields = new DataComponentPropertyType[sosFields.size()];
            int xbFieldIndex = 0;
            for (SosSweField sosSweField : sosFields) {
            	DataComponentPropertyType xbField = createField(sosSweField);
                xbFields[xbFieldIndex] = xbField;
                xbFieldIndex++;
            }
            xbDataRecord.setFieldArray(xbFields);
        }
        return xbDataRecordDoc;
    }
    
    private DataArrayDocument createDataArray(SosSweDataArray sosDataArray) throws OwsExceptionReport {
        if (sosDataArray != null) {
        	

        	DataArrayDocument xbDataArrayDoc = DataArrayDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            DataArrayType xbDataArray = xbDataArrayDoc.addNewDataArray1();
                    
            // set element count
            if (sosDataArray.getElementCount() != null) {
                xbDataArray.addNewElementCount().addNewCount().set(createCount(sosDataArray.getElementCount()));
            }
            
            if (sosDataArray.getElementType() != null) {
            	DataComponentPropertyType xbElementType = xbDataArray.addNewElementType();
            	xbDataArray.getElementType().setName("Components");
            	
            	DataRecordDocument xbDataRecordDoc = createDataRecord((SosSweDataRecord) sosDataArray.getElementType());
            	xbElementType.set(xbDataRecordDoc);
            }
            
            if (sosDataArray.getEncoding() != null) {
            	
            	BlockEncodingPropertyType xbEncoding = xbDataArray.addNewEncoding();
            	xbEncoding.set(createBlockEncoding(sosDataArray.getEncoding()));
//            	xbDataArray.getEncoding().substitute(
//                                new QName(SWEConstants.NS_SWE, SWEConstants.EN_TEXT_ENCODING,
//                                        SWEConstants.NS_SWE_PREFIX), TextBlock.type);
            }
            DataValuePropertyType xb_values = xbDataArray.addNewValues();
//            if (absObs.getObservationTemplateIDs() == null
//                    || (absObs.getObservationTemplateIDs() != null && absObs.getObservationTemplateIDs().isEmpty())) {
//                xb_values.newCursor().setTextValue(createResultString(phenComponents, absObs));
//            }
            if (sosDataArray.isSetValues()) {
            	xb_values.set(createValues(sosDataArray.getValues(), sosDataArray.getEncoding()));
            }
            return xbDataArrayDoc;
        }
        return null;
    }
    
    private XmlString createValues(List<List<String>> values, SosSweAbstractEncoding encoding) {
        // TODO How to deal with the decimal separator - is it an issue here?
        StringBuilder valueStringBuilder = new StringBuilder(256);
        SosSweTextEncoding textEncoding = (SosSweTextEncoding) encoding;
        // would also work, hmm
        // Configurator.getInstance().getDecimalSeparator();
        String tokenSeparator = textEncoding.getTokenSeparator();
        String blockSeparator = textEncoding.getBlockSeparator();
        for (List<String> block : values) {
            StringBuilder blockStringBuilder = new StringBuilder();
            for (String token : block) {
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
        XmlString xbValueString = XmlString.Factory.newInstance();
        xbValueString.setStringValue(valueString);
        return xbValueString;
    }
    
    private BlockEncodingPropertyType createBlockEncoding(SosSweAbstractEncoding sosSweAbstractEncoding)
            throws OwsExceptionReport {

    	try {
        	if (sosSweAbstractEncoding instanceof SosSweTextEncoding) {
                return createTextEncoding((SosSweTextEncoding) sosSweAbstractEncoding);
            }
            if (sosSweAbstractEncoding.getXml() != null && !sosSweAbstractEncoding.getXml().isEmpty()) {
                XmlObject xmlObject = XmlObject.Factory.parse(sosSweAbstractEncoding.getXml());
                if (xmlObject instanceof AbstractEncodingType) {
                    return (BlockEncodingPropertyType) xmlObject;
                }
                throw new NoApplicableCodeException().withMessage("AbstractEncoding can not be encoded!");
            }
            
        } catch (XmlException e) {
            throw new NoApplicableCodeException().causedBy(e)
                    .withMessage("Error while encoding AbstractEncoding!");
        } catch (XmlValueDisconnectedException xvde) {
            throw new NoApplicableCodeException().causedBy(xvde)
                    .withMessage("Error while encoding AbstractEncoding!");
        } catch (Exception ge) {
            throw new NoApplicableCodeException().causedBy(ge)
                    .withMessage("Error while encoding AbstractEncoding!");
        }
    	return null;
    }
    
    private BlockEncodingPropertyType createTextEncoding(SosSweTextEncoding sosTextEncoding) {
    	BlockEncodingPropertyType xbTextEncodingType =
    			BlockEncodingPropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
    	TextBlock xbTextEncoding = xbTextEncodingType.addNewTextBlock();
        
        if (sosTextEncoding.getBlockSeparator() != null) {
            xbTextEncoding.setBlockSeparator(sosTextEncoding.getBlockSeparator());
        }
        // TODO check not used in SWE101
//        if (sosTextEncoding.isSetCollapseWhiteSpaces()) {
//            xbTextEncoding.setCollapseWhiteSpaces(sosTextEncoding.isCollapseWhiteSpaces());
//        }
        if (sosTextEncoding.getDecimalSeparator() != null) {
            xbTextEncoding.setDecimalSeparator(sosTextEncoding.getDecimalSeparator());
        }
        if (sosTextEncoding.getTokenSeparator() != null) {
            xbTextEncoding.setTokenSeparator(sosTextEncoding.getTokenSeparator());
        }
        // wont cast !!! net.opengis.swe.x101.impl.BlockEncodingPropertyTypeImpl cannot be cast to net.opengis.swe.x101.AbstractEncodingType
        return xbTextEncodingType;
    }
}
