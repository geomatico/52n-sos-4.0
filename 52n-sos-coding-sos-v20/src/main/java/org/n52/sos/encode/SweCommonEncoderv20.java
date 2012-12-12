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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.swe.x20.AbstractDataComponentType;
import net.opengis.swe.x20.AbstractEncodingType;
import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayType;
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
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
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
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweCommonEncoderv20 implements IEncoder<XmlObject, Object> {

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
    public XmlObject encode(Object sosSweType) throws OwsExceptionReport {
        return encode(sosSweType, null);
    }

    @Override
    public XmlObject encode(Object sosSweType, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
    	
        if (sosSweType instanceof SosSweCoordinate) {
            return createCoordinate((SosSweCoordinate) sosSweType);
        }
        else if (sosSweType instanceof SosSweAbstractSimpleType) {
            return createSimpleType((SosSweAbstractSimpleType) sosSweType);
        } 
        else if (sosSweType instanceof SosSweAbstractEncoding) {
            return createAbstractEncoding((SosSweAbstractEncoding)sosSweType);
        } 
        else if (sosSweType instanceof SosSweAbstractDataComponent) {
            return createAbstractDataComponent((SosSweAbstractDataComponent)sosSweType);
        } 
        else if (sosSweType instanceof SosMultiObservationValues) {
            return createDataArrayResult((SosMultiObservationValues)sosSweType);
        }
        // TODO throw exception that element could not be encoded?
        return null;
    }

    private XmlObject createAbstractDataComponent(SosSweAbstractDataComponent sosSweAbstractDataComponent) throws OwsExceptionReport {
	    try {
	    	if (sosSweAbstractDataComponent instanceof SosSweAbstractSimpleType) {
	            return createSimpleType((SosSweAbstractSimpleType) sosSweAbstractDataComponent);
	        } 
	        if (sosSweAbstractDataComponent instanceof SosSweDataRecord)
	        {
	        	return createDataRecord((SosSweDataRecord) sosSweAbstractDataComponent);
	        }
	        else if (sosSweAbstractDataComponent instanceof SosSweDataArray)
	        {
	            return createDataArray((SosSweDataArray) sosSweAbstractDataComponent);
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

	private DataRecordType createDataRecord(SosSweDataRecord sosDataRecord) throws OwsExceptionReport
	{
		List<SosSweField> sosFields = sosDataRecord.getFields();
		DataRecordType xbDataRecord = DataRecordType.Factory.newInstance();
		if (sosDataRecord.isSetDefinition())
		{
			xbDataRecord.setDefinition(sosDataRecord.getDefinition());
		}
		if (sosDataRecord.isSetDescription())
		{
			xbDataRecord.setDescription(sosDataRecord.getDescription());
		}
		if (sosDataRecord.isSetIdentifier())
		{
			xbDataRecord.setIdentifier(sosDataRecord.getIdentifier());
		}
		if (sosFields != null) {
			Field[] xbFields = new Field[sosFields.size()];
			int xbFieldIndex = 0;
			for (SosSweField sosSweField : sosFields) {
				Field xbField = (DataRecordType.Field) createField(sosSweField);
				xbFields[xbFieldIndex] = xbField;
				xbFieldIndex++;
			}
			xbDataRecord.setFieldArray(xbFields);
		}
		return xbDataRecord;
	}
	private DataArrayType createDataArray(SosSweDataArray sosDataArray) throws OwsExceptionReport
	{
		if (sosDataArray != null) {
	
			DataArrayType xbDataArray = DataArrayType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
			if (sosDataArray.isSetDefinition())
			{
				xbDataArray.setDefinition(sosDataArray.getDefinition());
			}
			if (sosDataArray.isSetDescription())
			{
				xbDataArray.setDescription(sosDataArray.getDescription());
			}
			if (sosDataArray.isSetIdentifier())
			{
				xbDataArray.setIdentifier(sosDataArray.getIdentifier());
			}
			if (sosDataArray.getElementCount() != null)
			{
				xbDataArray.addNewElementCount().set(createCount(sosDataArray.getElementCount()));
			}
			if (sosDataArray.getElementType() != null)
			{
				xbDataArray.addNewElementType().addNewAbstractDataComponent();
				xbDataArray.getElementType().getAbstractDataComponent().set(createDataRecord((SosSweDataRecord) sosDataArray.getElementType()));
				xbDataArray.getElementType().getAbstractDataComponent().substitute(new QName(SWEConstants.NS_SWE_20, SWEConstants.EN_DATA_RECORD, SWEConstants.NS_SWE_PREFIX), DataRecordType.type);
			}
			if (sosDataArray.getEncoding() != null)
			{
				xbDataArray.addNewEncoding().addNewAbstractEncoding();
				xbDataArray.getEncoding().getAbstractEncoding().set(createAbstractEncoding(sosDataArray.getEncoding()));
				xbDataArray.getEncoding().getAbstractEncoding().substitute(new QName(SWEConstants.NS_SWE_20, SWEConstants.EN_TEXT_ENCODING, SWEConstants.NS_SWE_PREFIX), TextEncodingType.type);
			}
			if (sosDataArray.isSetValues())
			{
				xbDataArray.addNewValues().set(createValues(sosDataArray.getValues(),sosDataArray.getEncoding()));
			}
			return xbDataArray;
		}
		return null;
	}

	private XmlString createValues(List<List<String>> values,
			SosSweAbstractEncoding encoding)
	{
		// TODO How to deal with the decimal separator - is it an issue here?
		StringBuilder valueStringBuilder = new StringBuilder(256);
		SosSweTextEncoding textEncoding = (SosSweTextEncoding) encoding;
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

	/**
	 * @deprecated use {@link #createDataArray(SosSweDataArray)}
	 */
	@SuppressWarnings("rawtypes")
	private DataArrayType createDataArrayResult(SosMultiObservationValues sosObservationValue) throws OwsExceptionReport {
	    if (sosObservationValue.getValue() != null &&
	    		sosObservationValue.getValue() instanceof SweDataArrayValue &&
	    		sosObservationValue.getValue().getValue() != null &&
	    		sosObservationValue.getValue().getValue() instanceof SosSweDataArray) {
	        return createDataArray((SosSweDataArray) sosObservationValue.getValue().getValue());
	    }
	    return null;
	}

	private DataRecordType.Field createField(SosSweField sweField) throws OwsExceptionReport
	{
		SosSweAbstractDataComponent sosElement = sweField.getElement();
		DataRecordType.Field xbField = DataRecordType.Field.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		if (sweField.getName() != null)
		{
			xbField.setName(sweField.getName());
		}
		AbstractDataComponentType xbDCD = xbField.addNewAbstractDataComponent();
		if (sosElement instanceof SosSweBoolean)
		{
			xbDCD.set(createBoolean((SosSweBoolean) sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_BOOLEAN_SWE_200, BooleanType.type);
		}
		else if (sosElement instanceof SosSweCategory)
		{
			xbDCD.set(createCategoy((SosSweCategory) sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_CATEGORY_SWE_200, CategoryType.type);
		}
		else if (sosElement instanceof SosSweCount)
		{
			xbDCD.set(createCount((SosSweCount) sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_COUNT_SWE_200, CountType.type);
		}
		else if (sosElement instanceof SosSweQuantity)
		{
			xbDCD.set(createQuantity((SosSweQuantity) sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_QUANTITY_SWE_200, QuantityType.type);
		}
		else if (sosElement instanceof SosSweText)
		{
			xbDCD.set(createText((SosSweText) sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_TEXT_ENCODING_SWE_200, TextType.type);
		}
		else if (sosElement instanceof SosSweTimeRange)
		{
			xbDCD.set(createTimeRange((SosSweTimeRange)sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_TIME_RANGE_SWE_200, TimeRangeType.type);
		}
		else if (sosElement instanceof SosSweTime)
		{
			xbDCD.set(createTime((SosSweTime)sosElement));
			xbField.getAbstractDataComponent().substitute(SWEConstants.QN_TIME_SWE_200, TimeType.type);
		}
		else 
		{
			String errorMsg = String.format("The element type \"%s\" of the received %s is not supported by this encoder \"%s\".",
					sosElement!=null?sosElement.getClass().getName():"null",
					sweField!=null?sweField.getClass().getName():"null",
					this.getClass().getName());
			LOGGER.error(errorMsg);
			throw Util4Exceptions.createNoApplicableCodeException(null, errorMsg);
		}
		return xbField;
	}

	/*
	 * 
	 * 	SIMPLE TYPES
	 * 
	 */
	private AbstractDataComponentType createSimpleType(SosSweAbstractSimpleType sosSimpleType) throws OwsExceptionReport {
        
    	if (sosSimpleType instanceof SosSweBoolean)
        {
        	return createBoolean((SosSweBoolean) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweCategory)
        {
        	return createCategoy((SosSweCategory) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweCount)
        {
        	return createCount((SosSweCount) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweObservableProperty)
        {
        	return createObservableProperty((SosSweObservableProperty) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweQuantity)
        {
        	return createQuantity((SosSweQuantity) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweText)
        {
        	return createText((SosSweText) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweTimeRange)
        {
        	return createTimeRange((SosSweTimeRange) sosSimpleType);
        }
        else if (sosSimpleType instanceof SosSweTime)
        {
        	return createTime((SosSweTime) sosSimpleType);
        }
    	// TODO: NOT SUPPORTED EXCEPTION
    	throw new OwsExceptionReport();
    }

    private BooleanType createBoolean(SosSweBoolean sosElement)
	{
		BooleanType xbBoolean = BooleanType.Factory.newInstance();
		xbBoolean.setValue(sosElement.getValue());
		return xbBoolean;
	}

	private CategoryType createCategoy(SosSweCategory sosCategory)
	{
		CategoryType xbCategory = CategoryType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		if (sosCategory.getCodeSpace() != null)
		{
			Reference xbCodespace = xbCategory.addNewCodeSpace();
			xbCodespace.setHref(sosCategory.getCodeSpace());
		}
		if (sosCategory.isSetDefinition())
		{
			xbCategory.setDefinition(sosCategory.getDefinition());
		}
		return xbCategory;
	}

	private CountType createCount(SosSweCount sosCount)
	{
		CountType xbCount = CountType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		if (sosCount.getValue() != null)
		{
			BigInteger bigInt = new BigInteger(Integer.toString(sosCount.getValue().intValue()));
			xbCount.setValue(bigInt);
		}
		if (sosCount.isSetDefinition())
		{
			xbCount.setDefinition(sosCount.getDefinition());
		}
		return xbCount;
	}

	private AbstractDataComponentType createObservableProperty(SosSweObservableProperty sosSweAbstractDataComponent)
	{
		throw new RuntimeException("NOT YET IMPLEMENTED: encoding of swe:ObservableProperty");
	}

	private QuantityType createQuantity(SosSweQuantity quantity) {
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

    private TextType createText(SosSweText text) {
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

	private TimeType createTime(SosSweTime sosTime) {
	    TimeType xbTime = TimeType.Factory.newInstance();
	    if (sosTime.isSetDefinition()) {
	        xbTime.setDefinition(sosTime.getDefinition());
	    }
	    if (sosTime.isSetDescription()) {
	        xbTime.setDescription(sosTime.getDescription());
	    }
	    if (sosTime.isSetValue()) {
	        xbTime.setValue(sosTime.getValue());
	    }
	    if (sosTime.isSetUom()) {
	        xbTime.addNewUom().setHref(sosTime.getUom());
	    }
	    if (sosTime.getQuality() != null) {
	        // TODO
	    }
	    return xbTime;
	}

	private TimeRangeType createTimeRange(SosSweTimeRange sosTimeRange)
	{
		TimeRangeType xbTimeRange = TimeRangeType.Factory.newInstance();
		if (sosTimeRange.isSetDefinition())
		{
			xbTimeRange.setDefinition(sosTimeRange.getDefinition());
		}
		if (sosTimeRange.isSetDescription())
		{
			xbTimeRange.setDescription(sosTimeRange.getDescription());
		}
		if (sosTimeRange.isSetIdentifier())
		{
			xbTimeRange.setIdentifier(sosTimeRange.getIdentifier());
		}
		if (sosTimeRange.isSetUom())
		{
			xbTimeRange.addNewUom().setHref(sosTimeRange.getUom());
		}
		if (sosTimeRange.isSetValue())
		{
			xbTimeRange.setValue(sosTimeRange.getValue().getRangeAsStringList());
		}
		if (sosTimeRange.isSetQuality())
		{
			// TODO 
		}
		return xbTimeRange;
	}

	private Coordinate createCoordinate(SosSweCoordinate coordinate) {
        Coordinate xbCoordinate = Coordinate.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbCoordinate.setName(coordinate.getName().name());
        xbCoordinate.setQuantity(createQuantity((SosSweQuantity) coordinate.getValue()));
        return xbCoordinate;
    }

    private AbstractEncodingType createAbstractEncoding(SosSweAbstractEncoding sosSweAbstractEncoding) throws OwsExceptionReport {

    	if (sosSweAbstractEncoding instanceof SosSweTextEncoding)
		{
			return createTextEncoding((SosSweTextEncoding)sosSweAbstractEncoding);
		}
		
        try {
            if (sosSweAbstractEncoding.getXml() != null && !sosSweAbstractEncoding.getXml().isEmpty()) {
                XmlObject xmlObject = XmlObject.Factory.parse(sosSweAbstractEncoding.getXml());
                if (xmlObject instanceof AbstractEncodingType) {
                	return (AbstractEncodingType) xmlObject;
                }
            } 
            String exceptionText = "AbstractEncoding can not be encoded!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText); 
        } catch (XmlException e) {
            String exceptionText = "Error while encoding AbstractEncoding!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
        }
    }
    
	private TextEncodingType createTextEncoding(SosSweTextEncoding sosTextEncoding)
	{
		TextEncodingType xbTextEncoding = TextEncodingType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
		if (sosTextEncoding.getBlockSeparator() != null)
		{
			xbTextEncoding.setBlockSeparator(sosTextEncoding.getBlockSeparator());
		}
		if (sosTextEncoding.isSetCollapseWhiteSpaces())
		{
			xbTextEncoding.setCollapseWhiteSpaces(sosTextEncoding.isCollapseWhiteSpaces());
		}
		if (sosTextEncoding.getDecimalSeparator() != null)
		{
			xbTextEncoding.setDecimalSeparator(sosTextEncoding.getDecimalSeparator());
		}
		if (sosTextEncoding.getTokenSeparator() != null)
		{
			xbTextEncoding.setTokenSeparator(sosTextEncoding.getTokenSeparator());
		}
		return xbTextEncoding;
	}

	
    
    /**************************************************************************
     * 
     *  FIXME Clarify: Are these methods still required?
     * 
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
            // TODO no phentimeindex found...
        }
        ITime[] times = new ArrayList<ITime>(valueMap.keySet())
            .toArray(new ITime[valueMap.keySet().size()]);
        Arrays.sort(times);
        StringBuilder b = new StringBuilder();
        
        // dimensions will always be greater than (1,1).. 
        // so partly roll out the loop to gain some performance
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
            return DateTimeHelper.format(times[i]);
        } else {
            Map<String, IValue> value = valueMap.get(times[i]);
            return (value == null) ? noDataValue 
                    : getStringValue(value.get(phens[j]), noDataValue); 
        }
    }
 
    
    private String getStringValue(IValue value, String noDataValue) {
        if (value == null) {
            return noDataValue;
        }
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
            // TODO customizable decimal seperator
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
            // TODO should it really be tested for empty strings? isn't that a valid observation value? 
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
                // FIXME set the unit of the observed property while inserting result
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
*/
}
