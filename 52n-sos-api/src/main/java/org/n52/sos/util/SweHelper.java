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
package org.n52.sos.util;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.TimeValuePair;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TVPValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ResultTemplate;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SweHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweHelper.class);

    public static SosSweDataArray createSosSweDataArrayFromObservationValue(SosObservation sosObservation) throws OwsExceptionReport {
        if (sosObservation.getObservationConstellation().isSetResultTemplate()) {
            return createSosSweDataArrayWithResultTemplate(sosObservation);
        } else {
            return createSosSweDataArrayWithoutResultTemplate(sosObservation);
        }
    }

    private static SosSweDataArray createSosSweDataArrayWithResultTemplate(SosObservation sosObservation) throws OwsExceptionReport {
        ResultTemplate resultTemplate = sosObservation.getObservationConstellation().getResultTemplate();
        String observablePropertyIdentifier =
                sosObservation.getObservationConstellation().getObservableProperty().getIdentifier();
        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
        SosSweDataArray dataArray = new SosSweDataArray();
        dataArray.setElementType(resultTemplate.getResultStructure());
        dataArray.setEncoding(resultTemplate.getResultEncoding());
        dataArrayValue.setValue(dataArray);
        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            SosSingleObservationValue singleValue = (SosSingleObservationValue) sosObservation.getValue();
            dataArrayValue.addBlock(createBlock(dataArray.getElementType(), sosObservation.getPhenomenonTime(),
                    observablePropertyIdentifier, singleValue.getValue()));
        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
            SosMultiObservationValues multiValue = (SosMultiObservationValues) sosObservation.getValue();
            if (multiValue.getValue() instanceof SweDataArrayValue) {
                return ((SweDataArrayValue) multiValue.getValue()).getValue();
            } else if (multiValue.getValue() instanceof TVPValue) {
                TVPValue tvpValues = (TVPValue) multiValue.getValue();
                for (TimeValuePair timeValuePair : tvpValues.getValue()) {
                    List<String> newBlock =
                            createBlock(dataArray.getElementType(), timeValuePair.getTime(),
                                    observablePropertyIdentifier, timeValuePair.getValue());
                    dataArrayValue.addBlock(newBlock);
                }
            }
        }
        return dataArrayValue.getValue();
    }

    private static SosSweDataArray createSosSweDataArrayWithoutResultTemplate(SosObservation sosObservation) {
        String observablePropertyIdentifier =
                sosObservation.getObservationConstellation().getObservableProperty().getIdentifier();
        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
        SosSweDataArray dataArray = new SosSweDataArray();
        dataArray.setEncoding(createTextEncoding(sosObservation));
        dataArrayValue.setValue(dataArray);
        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            SosSingleObservationValue singleValue = (SosSingleObservationValue) sosObservation.getValue();
            dataArray.setElementType(createElementType(singleValue.getValue(), observablePropertyIdentifier));
            dataArrayValue.addBlock(createBlock(dataArray.getElementType(), sosObservation.getPhenomenonTime(),
                    observablePropertyIdentifier, singleValue.getValue()));
        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
            SosMultiObservationValues multiValue = (SosMultiObservationValues) sosObservation.getValue();
            if (multiValue.getValue() instanceof SweDataArrayValue) {
                return ((SweDataArrayValue) multiValue.getValue()).getValue();
            } else if (multiValue.getValue() instanceof TVPValue) {
                TVPValue tvpValues = (TVPValue) multiValue.getValue();
                for (TimeValuePair timeValuePair : tvpValues.getValue()) {
                    if (!dataArray.isSetElementTyp()) {
                        dataArray.setElementType(createElementType(timeValuePair.getValue(), observablePropertyIdentifier));
                    }
                    List<String> newBlock =
                            createBlock(dataArray.getElementType(), timeValuePair.getTime(),
                                    observablePropertyIdentifier, timeValuePair.getValue());
                    dataArrayValue.addBlock(newBlock);
                }
            }
        }
        return dataArray;
    }

    private static SosSweAbstractDataComponent createElementType(IValue iValue, String name) {
        SosSweDataRecord dataRecord = new SosSweDataRecord();
        dataRecord.addField(getPhenomenonTimeField());
        dataRecord.addField(getFieldForValue(iValue, name));
        return dataRecord;
    }

    private static SosSweField getPhenomenonTimeField() {
        SosSweTime time = new SosSweTime();
        time.setDefinition(OMConstants.PHENOMENON_TIME);
        time.setUom(OMConstants.PHEN_UOM_ISO8601);
        return new SosSweField(OMConstants.PHENOMENON_TIME_NAME, time);
    }

    private static SosSweField getFieldForValue(IValue iValue, String name) {
        SosSweAbstractDataComponent value = getValue(iValue);
        value.setDefinition(name);
        return new SosSweField(name, value);
    }

    private static SosSweAbstractDataComponent getValue(IValue iValue) {
        if (iValue instanceof BooleanValue) {
            return new SosSweBoolean();
        } else if (iValue instanceof CategoryValue) {
            SosSweCategory sosSweCategory = new SosSweCategory();
            sosSweCategory.setCodeSpace(((CategoryValue)iValue).getUnit());
            return sosSweCategory;
        } else if (iValue instanceof CountValue) {
            return new SosSweCount();
        } else if (iValue instanceof QuantityValue) {
            SosSweQuantity sosSweQuantity = new SosSweQuantity();
            sosSweQuantity.setUom(((QuantityValue)iValue).getUnit());
            return sosSweQuantity;
        } else if (iValue instanceof TextValue) {
            return new SosSweText();
        }
        return null;
    }

    private static SosSweAbstractEncoding createTextEncoding(SosObservation sosObservation) {
        SosSweTextEncoding sosTextEncoding = new SosSweTextEncoding();
        sosTextEncoding.setBlockSeparator(sosObservation.getTupleSeparator());
        sosTextEncoding.setTokenSeparator(sosObservation.getTokenSeparator());
        return sosTextEncoding;
    }

    private static List<String> createBlock(SosSweAbstractDataComponent elementType, ITime phenomenonTime,
            String phenID, IValue value) {
        if (elementType != null && elementType instanceof SosSweDataRecord) {
            SosSweDataRecord elementTypeRecord = (SosSweDataRecord) elementType;
            List<String> block = new ArrayList<String>();
            for (SosSweField sweField : elementTypeRecord.getFields()) {
                if (sweField.getElement() instanceof SosSweTime) {
                    block.add(DateTimeHelper.format(phenomenonTime));
                } else if (sweField.getElement() instanceof SosSweAbstractSimpleType
                        && sweField.getElement().getDefinition().equals(phenID)) {
                    block.add(value.getValue().toString());
                } else if (sweField.getElement() instanceof SosSweObservableProperty) {
                    block.add(phenID);
                }
            }
            return block;
        }
        String exceptionMsg =
                String.format("Type of ElementType is not supported: %s", elementType != null ? elementType.getClass()
                        .getName() : "null");
        LOGGER.debug(exceptionMsg);
        throw new IllegalArgumentException(exceptionMsg);
    }

}
