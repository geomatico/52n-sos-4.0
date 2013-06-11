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
package org.n52.sos.util;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.om.MultiObservationValues;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.SingleObservationValue;
import org.n52.sos.ogc.om.TimeValuePair;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.NilTemplateValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TVPValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.om.values.Value;
import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SweBoolean;
import org.n52.sos.ogc.swe.simpleType.SweCategory;
import org.n52.sos.ogc.swe.simpleType.SweCount;
import org.n52.sos.ogc.swe.simpleType.SweObservableProperty;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
import org.n52.sos.ogc.swe.simpleType.SweText;
import org.n52.sos.ogc.swe.simpleType.SweTime;
import org.n52.sos.service.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SweHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweHelper.class);

//    public static SweDataArray createSosSweDataArrayFromObservationValue(OmObservation sosObservation)
//            throws OwsExceptionReport {
//        if (sosObservation.getv) {
//            return createSosSweDataArrayWithResultTemplate(sosObservation);
//        } else {
//            return createSosSweDataArrayWithoutResultTemplate(sosObservation);
//        }
//    }
//
//    private static SweDataArray createSosSweDataArrayWithResultTemplate(OmObservation sosObservation)
//            throws OwsExceptionReport {
//        SosResultTemplate sosResultTemplate = sosObservation.getObservationConstellation().getResultTemplate();
//        String observablePropertyIdentifier =
//                sosObservation.getObservationConstellation().getObservableProperty().getIdentifier();
//        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
//        SweDataArray dataArray = new SweDataArray();
//        dataArray.setElementType(sosResultTemplate.getResultStructure());
//        dataArray.setEncoding(sosResultTemplate.getResultEncoding());
//        dataArrayValue.setValue(dataArray);
//        if (sosObservation.getValue() instanceof SingleObservationValue) {
//            SingleObservationValue<?> singleValue = (SingleObservationValue) sosObservation.getValue();
//            dataArrayValue.addBlock(createBlock(dataArray.getElementType(), sosObservation.getPhenomenonTime(),
//                    observablePropertyIdentifier, singleValue.getValue()));
//        } else if (sosObservation.getValue() instanceof MultiObservationValues) {
//            MultiObservationValues<?> multiValue = (MultiObservationValues) sosObservation.getValue();
//            if (multiValue.getValue() instanceof SweDataArrayValue) {
//                return ((SweDataArrayValue) multiValue.getValue()).getValue();
//            } else if (multiValue.getValue() instanceof TVPValue) {
//                TVPValue tvpValues = (TVPValue) multiValue.getValue();
//                for (TimeValuePair timeValuePair : tvpValues.getValue()) {
//                    List<String> newBlock =
//                            createBlock(dataArray.getElementType(), timeValuePair.getTime(),
//                                    observablePropertyIdentifier, timeValuePair.getValue());
//                    dataArrayValue.addBlock(newBlock);
//                }
//            }
//        }
//        return dataArrayValue.getValue();
//    }

    public static SweDataArray createSosSweDataArray(OmObservation sosObservation) {
        String observablePropertyIdentifier =
                sosObservation.getObservationConstellation().getObservableProperty().getIdentifier();
        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
        SweDataArray dataArray = new SweDataArray();
        dataArray.setEncoding(createTextEncoding(sosObservation));
        dataArrayValue.setValue(dataArray);
        if (sosObservation.getValue() instanceof SingleObservationValue) {
            SingleObservationValue<?> singleValue = (SingleObservationValue) sosObservation.getValue();
            if (singleValue.getValue() instanceof SweDataArrayValue) {
                return (SweDataArray)singleValue.getValue().getValue();
            } else {
            dataArray.setElementType(createElementType(singleValue.getValue(), observablePropertyIdentifier));
            dataArrayValue.addBlock(createBlock(dataArray.getElementType(), sosObservation.getPhenomenonTime(),
                    observablePropertyIdentifier, singleValue.getValue()));
            }
        } else if (sosObservation.getValue() instanceof MultiObservationValues) {
            MultiObservationValues<?> multiValue = (MultiObservationValues) sosObservation.getValue();
            if (multiValue.getValue() instanceof SweDataArrayValue) {
                return ((SweDataArrayValue) multiValue.getValue()).getValue();
            } else if (multiValue.getValue() instanceof TVPValue) {
                TVPValue tvpValues = (TVPValue) multiValue.getValue();
                for (TimeValuePair timeValuePair : tvpValues.getValue()) {
                    if (!dataArray.isSetElementTyp()) {
                        dataArray.setElementType(createElementType(timeValuePair.getValue(),
                                observablePropertyIdentifier));
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

    private static SweAbstractDataComponent createElementType(Value<?> iValue, String name) {
        SweDataRecord dataRecord = new SweDataRecord();
        dataRecord.addField(getPhenomenonTimeField());
        dataRecord.addField(getFieldForValue(iValue, name));
        return dataRecord;
    }

    private static SweField getPhenomenonTimeField() {
        SweTime time = new SweTime();
        time.setDefinition(OMConstants.PHENOMENON_TIME);
        time.setUom(OMConstants.PHEN_UOM_ISO8601);
        return new SweField(OMConstants.PHENOMENON_TIME_NAME, time);
    }

    private static SweField getFieldForValue(Value<?> iValue, String name) {
        SweAbstractDataComponent value = getValue(iValue);
        value.setDefinition(name);
        return new SweField(name, value);
    }

    private static SweAbstractDataComponent getValue(Value<?> iValue) {
        if (iValue instanceof BooleanValue) {
            return new SweBoolean();
        } else if (iValue instanceof CategoryValue) {
            SweCategory sosSweCategory = new SweCategory();
            sosSweCategory.setCodeSpace(((CategoryValue) iValue).getUnit());
            return sosSweCategory;
        } else if (iValue instanceof CountValue) {
            return new SweCount();
        } else if (iValue instanceof QuantityValue) {
            SweQuantity sosSweQuantity = new SweQuantity();
            sosSweQuantity.setUom(((QuantityValue) iValue).getUnit());
            return sosSweQuantity;
        } else if (iValue instanceof TextValue) {
            return new SweText();
        } else if (iValue instanceof NilTemplateValue) {
            return new SweText();
        }
        return null;
    }

    /**
     * Create a TextEncoding object for token and tuple separators from
     * SosObservation. If separators not set, definitions from Configurator are
     * used.
     * 
     * @param sosObservation
     *            SosObservation with token and tuple separator
     * @return TextEncoding
     */
    private static SweAbstractEncoding createTextEncoding(OmObservation sosObservation) {
        SweTextEncoding sosTextEncoding = new SweTextEncoding();
        if (sosObservation.isSetTupleSeparator()) {
            sosTextEncoding.setBlockSeparator(sosObservation.getTupleSeparator());
        } else {
            sosTextEncoding.setBlockSeparator(ServiceConfiguration.getInstance().getTupleSeparator());
        }
        if (sosObservation.isSetTokenSeparator()) {
            sosTextEncoding.setTokenSeparator(sosObservation.getTokenSeparator());
        } else {
            sosTextEncoding.setTokenSeparator(ServiceConfiguration.getInstance().getTokenSeparator());
        }
        return sosTextEncoding;
    }

    private static List<String> createBlock(SweAbstractDataComponent elementType, Time phenomenonTime,
            String phenID, Value<?> value) {
        if (elementType instanceof SweDataRecord) {
            SweDataRecord elementTypeRecord = (SweDataRecord) elementType;
            List<String> block = new ArrayList<String>(elementTypeRecord.getFields().size());
            for (SweField sweField : elementTypeRecord.getFields()) {
                if (!(value instanceof NilTemplateValue)) {
                    if (sweField.getElement() instanceof SweTime) {
                        block.add(DateTimeHelper.format(phenomenonTime));
                    } else if (sweField.getElement() instanceof SweAbstractDataComponent
                            && sweField.getElement().getDefinition().equals(phenID)) {
                        block.add(value.getValue().toString());
                    } else if (sweField.getElement() instanceof SweObservableProperty) {
                        block.add(phenID);
                    }
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

    private SweHelper() {
    }

}
