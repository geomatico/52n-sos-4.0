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
package org.n52.sos.ds.hibernate.util;

import static org.n52.sos.util.DateTimeHelper.formatDateTime2IsoString;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.BlobObservation;
import org.n52.sos.ds.hibernate.entities.BooleanObservation;
import org.n52.sos.ds.hibernate.entities.CategoryObservation;
import org.n52.sos.ds.hibernate.entities.CountObservation;
import org.n52.sos.ds.hibernate.entities.GeometryObservation;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.TextObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SweAbstractSimpleType;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.DateTimeHelper;

import com.vividsolutions.jts.io.WKTWriter;

public class ResultHandlingHelper {

    private static final String RESULT_TIME = "http://www.opengis.net/def/property/OGC/0/ResultTime";

    private static final String PHENOMENON_TIME = "http://www.opengis.net/def/property/OGC/0/PhenomenonTime";
    
    public static SosResultEncoding createSosResultEncoding(final String resultEncoding) {
        final SosResultEncoding sosResultEncoding = new SosResultEncoding();
        sosResultEncoding.setXml(resultEncoding);
        return sosResultEncoding;
    }

    public static SosResultStructure createSosResultStructure(final String resultStructure) {
        final SosResultStructure sosResultStructure = new SosResultStructure();
        sosResultStructure.setXml(resultStructure);
        return sosResultStructure;
    }

    public static String createResultValuesFromObservations(final List<Observation> observations,
                                                            final SosResultEncoding sosResultEncoding,
                                                            final SosResultStructure sosResultStructure) throws OwsExceptionReport {
        final StringBuilder builder = new StringBuilder();
        if (CollectionHelper.isNotEmpty(observations)) {
            final String tokenSeparator = getTokenSeparator(sosResultEncoding.getEncoding());
            final String blockSeparator = getBlockSeparator(sosResultEncoding.getEncoding());
            final Map<Integer, String> valueOrder = getValueOrderMap(sosResultStructure.getResultStructure());
            addElementCount(builder,observations.size(),blockSeparator);
            for (final Observation observation : observations) {
                for (final Integer intger : valueOrder.keySet()) {
                    final String definition = valueOrder.get(intger);
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
            if (builder.length() > 0) {
                builder.delete(builder.lastIndexOf(blockSeparator), builder.length());
            }
        }
        return builder.toString();
    }

	private static void addElementCount(final StringBuilder builder,
			final int size,
			final String blockSeparator)
	{
		builder.append(String.valueOf(size));
		builder.append(blockSeparator);
	}

	private static Object getTimeStringForResultTime(final Date resultTime) {
        if (resultTime != null) {
            return DateTimeHelper.formatDateTime2IsoString(new DateTime(resultTime));
        }
        return Configurator.getInstance().getProfileHandler().getActiveProfile().getResponseNoDataPlaceholder();
    }

    private static Object getTimeStringForPhenomenonTime(final Date phenomenonTimeStart, final Date phenomenonTimeEnd) {
    	if (phenomenonTimeStart == null && phenomenonTimeEnd == null)
    	{
    		return Configurator.getInstance().getProfileHandler().getActiveProfile()
    				.getResponseNoDataPlaceholder();
    	}
    	
    	final StringBuilder builder = new StringBuilder();
    	if (phenomenonTimeStart.equals(phenomenonTimeEnd))
    	{
    		builder.append(formatDateTime2IsoString(new DateTime(phenomenonTimeStart)));
    	}
    	else
    	{
    		builder.append(formatDateTime2IsoString(new DateTime(phenomenonTimeStart)));
    		builder.append('/');
    		builder.append(formatDateTime2IsoString(new DateTime(phenomenonTimeStart)));
    	}
    	return builder.toString();
    }

    private static Map<Integer, String> getValueOrderMap(
            final SweAbstractDataComponent sweDataElement) {
        final Map<Integer, String> valueOrder = new HashMap<Integer, String>(0);
        if (sweDataElement instanceof SweDataArray && ((SweDataArray) sweDataElement).getElementType() instanceof SweDataRecord) {
            final SweDataArray dataArray = (SweDataArray) sweDataElement;
            addOrderAndDefinitionToMap(((SweDataRecord) dataArray.getElementType()).getFields(), valueOrder);
        } else if (sweDataElement instanceof SweDataRecord) {
            final SweDataRecord dataRecord = (SweDataRecord) sweDataElement;
            addOrderAndDefinitionToMap(dataRecord.getFields(), valueOrder);
        }
        return new TreeMap<Integer, String>(valueOrder);
    }

    private static void addOrderAndDefinitionToMap(final List<SweField> fields, final Map<Integer, String> valueOrder) {
        for (int i = 0; i < fields.size(); i++) {
            final SweAbstractDataComponent element = fields.get(i).getElement();
            if (element instanceof SweAbstractSimpleType) {
                final SweAbstractSimpleType<?> simpleType = (SweAbstractSimpleType<?>) element;
                if (simpleType.isSetDefinition()) {
                    addValueToValueOrderMap(valueOrder, i, simpleType.getDefinition());
                }
            }
        }
    }

    private static void addValueToValueOrderMap(final Map<Integer, String> valueOrder, final int index, final String value) {
        if (index >= 0) {
            valueOrder.put(index, value);
        }
    }

    private static String getValueAsStringForObservedProperty(final Observation observation, final String definition) {
        final String observedProperty = observation.getObservableProperty().getIdentifier();
        
		if (observedProperty.equals(definition)) {
		        if (observation instanceof NumericObservation) {
		            return String.valueOf(((NumericObservation) observation).getValue());
		        } else if (observation instanceof BooleanObservation) {
		            return String.valueOf(((BooleanObservation) observation).getValue());
		        } else if (observation instanceof CategoryObservation) {
		            return String.valueOf(((CategoryObservation) observation).getValue());
		        } else if (observation instanceof CountObservation) {
		            return String.valueOf(((CountObservation) observation).getValue());
		        } else if (observation instanceof TextObservation) {
		            return String.valueOf(((TextObservation) observation).getValue());
		        } else if (observation instanceof GeometryObservation) {
		            final WKTWriter writer = new WKTWriter();
		            return writer.write(((GeometryObservation) observation).getValue());
		        } else if (observation instanceof BlobObservation) {
		            return String.valueOf(((BlobObservation) observation).getValue());
		        }
//            // TODO multiple values?
//			Set<BooleanValue> booleanValues = observation.getBooleanValue();
//			if (booleanValues != null && !booleanValues.isEmpty()) {
//				return String.valueOf(booleanValues.iterator().next().getValue());
//			}
//			
//			Set<CategoryValue> categoryValues = observation.getCategoryValue();
//			if (categoryValues != null && !categoryValues.isEmpty()) {
//				return categoryValues.iterator().next().getValue();
//			}
//			
//			Set<CountValue> countValues = observation.getCountValue();
//			if (countValues != null && !countValues.isEmpty()) {
//				return String.valueOf(countValues.iterator().next().getValue());
//			}
//			
//			Set<NumericValue> numericValues = observation.getNumericValues();
//			if (numericValues != null && !numericValues.isEmpty()) {
//				return String.valueOf(numericValues.iterator().next().getValue());
//			}
//			
//			//TODO geometry values;
//			
//            Set<TextValue> textValues = observation.getTextValues();
//            if (textValues != null && !textValues.isEmpty()) {
//                StringBuilder builder = new StringBuilder();
//                for (TextValue textValue : textValues) {
//                    builder.append(textValue.getValue());
//                }
//                return builder.toString();
//            }
        }
        return Configurator.getInstance().getProfileHandler().getActiveProfile().getResponseNoDataPlaceholder();
    }

    public static String getTokenSeparator(final SweAbstractEncoding encoding) {
        if (encoding instanceof SweTextEncoding) {
            return ((SweTextEncoding) encoding).getTokenSeparator();
        }
        return null;
    }
    
    public static String getBlockSeparator(final SweAbstractEncoding encoding) {
        if (encoding instanceof SweTextEncoding) {
            return ((SweTextEncoding) encoding).getBlockSeparator();
        }
        return null;
    }
    
    public static int hasResultTime(final SweAbstractDataComponent sweDataElement) {
        if (sweDataElement instanceof SweDataArray && ((SweDataArray) sweDataElement).getElementType() instanceof SweDataRecord) {
            final SweDataArray dataArray = (SweDataArray) sweDataElement;
            return checkFields(((SweDataRecord) dataArray.getElementType()).getFields(), RESULT_TIME);
        } else if (sweDataElement instanceof SweDataRecord) {
            final SweDataRecord dataRecord = (SweDataRecord) sweDataElement;
            return checkFields(dataRecord.getFields(), RESULT_TIME);
        }
        return -1;
    }
    
    public static int hasPhenomenonTime(final SweAbstractDataComponent sweDataElement) {
        if (sweDataElement instanceof SweDataArray && ((SweDataArray) sweDataElement).getElementType() instanceof SweDataRecord) {
            final SweDataArray dataArray = (SweDataArray) sweDataElement;
            return checkFields(((SweDataRecord) dataArray.getElementType()).getFields(), PHENOMENON_TIME);
        } else if (sweDataElement instanceof SweDataRecord) {
            final SweDataRecord dataRecord = (SweDataRecord) sweDataElement;
            return checkFields(dataRecord.getFields(), PHENOMENON_TIME);
        }
        return -1;
    }

    public static int checkFields(final List<SweField> fields, final String definition) {
		int i = 0;
		for (final SweField f : fields) {
			final SweAbstractDataComponent element = f.getElement();
            if (element instanceof SweAbstractSimpleType) {
                final SweAbstractSimpleType<?> simpleType = (SweAbstractSimpleType<?>) element;
                if (simpleType.isSetDefinition() && simpleType.getDefinition().equals(definition)) {
                    return i;
                }
            }
			++i;
		}
        return -1;
    }

    private ResultHandlingHelper() {
    }

}
