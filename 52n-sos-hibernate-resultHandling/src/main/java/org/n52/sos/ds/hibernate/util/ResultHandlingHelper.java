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
package org.n52.sos.ds.hibernate.util;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.CountValue;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeHelper;

public class ResultHandlingHelper {
    

    private static final String RESULT_TIME = "http://www.opengis.net/def/property/OGC/0/ResultTime";

    private static final String PHENOMENON_TIME = "http://www.opengis.net/def/property/OGC/0/PhenomenonTime";
    
    public static SosResultEncoding createSosResultEncoding(String resultEncoding) {
        SosResultEncoding sosResultEncoding = new SosResultEncoding();
        sosResultEncoding.setXml(resultEncoding);
        return sosResultEncoding;
    }

    public static SosResultStructure createSosResultStructure(String resultStructure) {
        SosResultStructure sosResultStructure = new SosResultStructure();
        sosResultStructure.setXml(resultStructure);
        return sosResultStructure;
    }

    public static String createResultValuesFromObservations(List<Observation> observations,
            SosResultEncoding sosResultEncoding, SosResultStructure sosResultStructure) throws OwsExceptionReport {
        StringBuilder builder = new StringBuilder();
        String tokenSeparator = getTokenSeparator(sosResultEncoding.getEncoding());
        String blockSeparator = getBlockSeparator(sosResultEncoding.getEncoding());
        TreeMap<Integer, String> valueOrder = getValueOrderMap(sosResultStructure.getResultStructure());
        for (Observation observation : observations) {
            for (Integer intger : valueOrder.keySet()) {
                String definition = valueOrder.get(intger);
                if (definition.equals(PHENOMENON_TIME)) {
                    builder.append(getTimeStringForPhenomenonTime(observation.getPhenomenonTimeStart(), observation.getPhenomenonTimeEnd()));
                } else if (definition.equals(RESULT_TIME)) {
                    builder.append(getTimeStringForResultTime(observation.getResultTime()));
                } else {
                    builder.append(getValueAsStringForObservedProperty(observation, definition));
                }
                builder.append(tokenSeparator);
            }
            builder.delete(builder.lastIndexOf(tokenSeparator), builder.length());
            builder.append(blockSeparator);
        }
        builder.delete(builder.lastIndexOf(blockSeparator), builder.length());
        return builder.toString();
    }

    private static Object getTimeStringForResultTime(Date resultTime) {
        if (resultTime != null) {
            return DateTimeHelper.formatDateTime2IsoString(new DateTime(resultTime));
        }
        return Configurator.getInstance().getNoDataValue();
    }

    private static Object getTimeStringForPhenomenonTime(Date phenomenonTimeStart, Date phenomenonTimeEnd) {
        StringBuilder builder = new StringBuilder();
        if (phenomenonTimeStart != null && phenomenonTimeEnd != null) {
            builder.append(DateTimeHelper.formatDateTime2IsoString(new DateTime(phenomenonTimeStart)));
            builder.append("/");
            builder.append(DateTimeHelper.formatDateTime2IsoString(new DateTime(phenomenonTimeStart)));
        } else if (phenomenonTimeStart != null && phenomenonTimeEnd == null) {
            builder.append(DateTimeHelper.formatDateTime2IsoString(new DateTime(phenomenonTimeStart)));
        } else {
            builder.append(Configurator.getInstance().getNoDataValue());
        }
        return builder.toString();
    }

    private static TreeMap<Integer, String> getValueOrderMap(
            SosSweAbstractDataComponent sweDataElement) {
        Map<Integer, String> valueOrder = new HashMap<Integer, String>(0);
        if (sweDataElement instanceof SosSweDataArray) {
            SosSweDataArray dataArray = (SosSweDataArray) sweDataElement;
            addOrderAndDefinitionToMap(dataArray.getElementType().getFields(), valueOrder);
        } else if (sweDataElement instanceof SosSweDataRecord) {
            SosSweDataRecord dataRecord = (SosSweDataRecord) sweDataElement;
            addOrderAndDefinitionToMap(dataRecord.getFields(), valueOrder);
        }
        return new TreeMap<Integer, String>(valueOrder);
    }

    private static void addOrderAndDefinitionToMap(List<SosSweField> fields, Map<Integer, String> valueOrder) {
        for (int i = 0; i < fields.size(); i++) {
            SosSweAbstractDataComponent element = fields.get(i).getElement();
            if (element instanceof SosSweAbstractSimpleType) {
                SosSweAbstractSimpleType simpleType = (SosSweAbstractSimpleType) element;
                if (simpleType.isSetDefinition()) {
                    addValueToValueOrderMap(valueOrder, i, simpleType.getDefinition());
                }
            }
        }
    }

    private static void addValueToValueOrderMap(Map<Integer, String> valueOrder, int index, String value) {
        if (index >= 0) {
            valueOrder.put(index, value);
        }
    }

    private static String getValueAsStringForObservedProperty(Observation observation, String definition) {
        String observedProperty = observation.getObservationConstellation().getObservableProperty().getIdentifier();
        
		if (observedProperty.equals(definition)) {
            // TODO multiple values?
			Set<BooleanValue> booleanValues = observation.getBooleanValues();
			if (booleanValues != null && !booleanValues.isEmpty()) {
				return String.valueOf(booleanValues.iterator().next().getValue());
			}
			
			Set<CategoryValue> categoryValues = observation.getCategoryValues();
			if (categoryValues != null && !categoryValues.isEmpty()) {
				return categoryValues.iterator().next().getValue();
			}
			
			Set<CountValue> countValues = observation.getCountValues();
			if (countValues != null && !countValues.isEmpty()) {
				return String.valueOf(countValues.iterator().next().getValue());
			}
			
			Set<NumericValue> numericValues = observation.getNumericValues();
			if (numericValues != null && !numericValues.isEmpty()) {
				return String.valueOf(numericValues.iterator().next().getValue());
			}
			
			//TODO geometry values;
			
            Set<TextValue> textValues = observation.getTextValues();
            if (textValues != null && !textValues.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (TextValue textValue : textValues) {
                    builder.append(textValue.getValue());
                }
                return builder.toString();
            }
        }
        return Configurator.getInstance().getNoDataValue();
    }

    public static String getTokenSeparator(SosSweAbstractEncoding encoding) {
        if (encoding instanceof SosSweTextEncoding) {
            return ((SosSweTextEncoding) encoding).getTokenSeparator();
        }
        return null;
    }
    
    public static String getBlockSeparator(SosSweAbstractEncoding encoding) {
        if (encoding instanceof SosSweTextEncoding) {
            return ((SosSweTextEncoding) encoding).getBlockSeparator();
        }
        return null;
    }
    
    public static int hasResultTime(SosSweAbstractDataComponent sweDataElement) {
        if (sweDataElement instanceof SosSweDataArray) {
            SosSweDataArray dataArray = (SosSweDataArray) sweDataElement;
            return checkFields(dataArray.getElementType().getFields(), RESULT_TIME);
        } else if (sweDataElement instanceof SosSweDataRecord) {
            SosSweDataRecord dataRecord = (SosSweDataRecord) sweDataElement;
            return checkFields(dataRecord.getFields(), RESULT_TIME);
        }
        return -1;
    }
    
    public static int hasPhenomenonTime(SosSweAbstractDataComponent sweDataElement) {
        if (sweDataElement instanceof SosSweDataArray) {
            SosSweDataArray dataArray = (SosSweDataArray) sweDataElement;
            return checkFields(dataArray.getElementType().getFields(), PHENOMENON_TIME);
        } else if (sweDataElement instanceof SosSweDataRecord) {
            SosSweDataRecord dataRecord = (SosSweDataRecord) sweDataElement;
            return checkFields(dataRecord.getFields(), PHENOMENON_TIME);
        }
        return -1;
    }

    public static int checkFields(List<SosSweField> fields, String definition) {
		int i = 0;
		for (SosSweField f : fields) {
			SosSweAbstractDataComponent element = f.getElement();
            if (element instanceof SosSweAbstractSimpleType) {
                SosSweAbstractSimpleType simpleType = (SosSweAbstractSimpleType) element;
                if (simpleType.isSetDefinition() && simpleType.getDefinition().equals(definition)) {
                    return i;
                }
            }
			++i;
		}
        return -1;
    }

}
