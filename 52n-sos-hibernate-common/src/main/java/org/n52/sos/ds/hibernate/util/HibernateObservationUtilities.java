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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.CountValue;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.GeometryValue;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Quality;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.entities.TextValue;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.IObservationValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.om.quality.SosQuality.QualityType;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
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
import org.n52.sos.ogc.swes.SwesExtensions;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import org.n52.sos.ogc.ows.OWSConstants;

public class HibernateObservationUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateObservationUtilities.class);

    /**
     * Create SOS internal observation from Observation objects
     * 
     * @param responseFormat
     * 
     * @param observations
     *            List of Observation objects
     * @param request
     *            the request
     * @param session
     *            Hibernate session
     * @return SOS internal observation
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public static List<SosObservation> createSosObservationsFromObservations(List<Observation> observations,
            AbstractServiceRequest request, Session session) throws OwsExceptionReport {
        List<SosObservation> observationCollection = new ArrayList<SosObservation>();

        Map<String, SosAbstractFeature> features = new HashMap<String, SosAbstractFeature>();
        Map<String, SosObservation> antiSubsettingObservations = new HashMap<String, SosObservation>();
        Map<String, AbstractSosPhenomenon> obsProps = new HashMap<String, AbstractSosPhenomenon>();
        Map<Integer, SosObservationConstellation> observationConstellations =
                new HashMap<Integer, SosObservationConstellation>();
        Map<Integer, List<ResultTemplate>> template4ObsConst = new HashMap<Integer, List<ResultTemplate>>();
        Map<Integer, SosObservation> templatedObservations = new HashMap<Integer, SosObservation>();

        // Map<String, DateTime> featureTimeForDynamicPosition = new
        // HashMap<String, DateTime>();
        // String observationType = OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION;
        if (observations != null) {
            // now iterate over resultset and create Measurement for each
            // row
            for (Observation hObservation : observations) {
                // check remaining heap size
                SosHelper.checkFreeMemory();

                ObservationConstellation hObservationConstellation = hObservation.getObservationConstellation();
                FeatureOfInterest hFeatureOfInterest = hObservation.getFeatureOfInterest();

                String procID = hObservationConstellation.getProcedure().getIdentifier();
                String observationType = hObservationConstellation.getObservationType().getObservationType();

                DateTime timeDateTime = new DateTime(hObservation.getPhenomenonTimeStart());

                // feature of interest
                String foiID = hFeatureOfInterest.getIdentifier();
                if (!features.containsKey(foiID)) {
                    features.put(
                            foiID,
                            Configurator.getInstance().getFeatureQueryHandler()
                                    .getFeatureByID(foiID, session, request.getVersion()));
                }

                // phenomenon
                String phenID = hObservationConstellation.getObservableProperty().getIdentifier();
                String description = hObservationConstellation.getObservableProperty().getDescription();
                if (!obsProps.containsKey(phenID)) {
                    obsProps.put(phenID, new SosObservableProperty(phenID, description, null, null));
                }
                // if (!version.equals(Sos2Constants.SERVICEVERSION)
                // && Configurator.getInstance().isSetFoiLocationDynamically()
                // &&
                // phenID.equals(Configurator.getInstance().getSpatialObsProp4DynymicLocation()))
                // {
                // featureTimeForDynamicPosition.put(foiID, timeDateTime);
                // }

                // TODO: add offering ids to response if needed later.
                // String offeringID =
                // hObservationConstellation.getOffering().getIdentifier();
                // String mimeType = SosConstants.PARAMETER_NOT_SET;

                // create time element
                ITime phenomenonTime;
                if (hObservation.getPhenomenonTimeEnd() == null) {
                    phenomenonTime = new TimeInstant(timeDateTime, "");
                } else {
                    phenomenonTime = new TimePeriod(timeDateTime, new DateTime(hObservation.getPhenomenonTimeEnd()));
                }
                // create quality
                ArrayList<SosQuality> qualityList = null;
                if (Configurator.getInstance().isSupportsQuality()) {
                    hObservation.getQualities();
                    for (Quality hQuality : (Set<Quality>) hObservation.getQualities()) {
                        String qualityTypeString = hQuality.getSweType().getSweType();
                        String qualityUnit = hQuality.getUnit().getUnit();
                        String qualityName = hQuality.getName();
                        String qualityValue = hQuality.getValue();
                        qualityList = new ArrayList<SosQuality>();
                        if (qualityValue != null) {
                            QualityType qualityType = QualityType.valueOf(qualityTypeString);
                            SosQuality quality = new SosQuality(qualityName, qualityUnit, qualityValue, qualityType);
                            qualityList.add(quality);
                        }
                    }
                }
                IValue value = getValueFromAllTable(hObservation);
                if (hObservation.getUnit() != null) {
                    value.setUnit(hObservation.getUnit().getUnit());
                }
                checkOrSetObservablePropertyUnit(obsProps.get(phenID), value.getUnit());
                final SosObservationConstellation obsConst =
                        new SosObservationConstellation(procID, obsProps.get(phenID), null, features.get(foiID),
                                observationType);
                /* get the offerings to find the templates */
                if (obsConst.getOfferings() == null) {
                    Set<String> offerings =
                            new HashSet<String>(Configurator.getInstance().getCapabilitiesCacheController()
                                    .getOfferings4Procedure(obsConst.getProcedure()));
                    offerings.retainAll(new HashSet<String>(Configurator.getInstance()
                            .getCapabilitiesCacheController().getOfferings4Procedure(obsConst.getProcedure())));
                    obsConst.setOfferings(offerings);
                }
                int obsConstHash = obsConst.hashCode();
                if (!observationConstellations.containsKey(obsConstHash)) {
                    if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)) {
                        List<ResultTemplate> templates = template4ObsConst.get(obsConstHash);
                        if (templates == null) {
                            template4ObsConst.put(obsConstHash, templates = new LinkedList<ResultTemplate>());
                        }
                        for (String offering : obsConst.getOfferings()) {
                            ResultTemplate t =
                                    HibernateCriteriaQueryUtilities.getResultTemplateObject(offering, obsConst
                                            .getObservableProperty().getIdentifier(), session);
                            if (t != null) {
                                templates.add(t);
                                if (!templatedObservations.containsKey(obsConstHash)) {
                                    templatedObservations.put(obsConstHash, null);
                                }
                            }
                        }
                    }
                    observationConstellations.put(obsConstHash, obsConst);
                }

                // TODO: compositePhenomenon
                if (isSubsetIdAvailable(hObservation) &&
                        !isSubsettingExtensionSet(request.getExtensions())) {
                    if (antiSubsettingObservations.containsKey(hObservation.getAntiSubsetting()))
                    {
                        // observation already create => merge values
                        SosObservation sosObservation =
                                antiSubsettingObservations.get(hObservation.getAntiSubsetting());
                        SosMultiObservationValues sosMultiObservationValues =
                                (SosMultiObservationValues) sosObservation.getValue();
                        SweDataArrayValue sweDataArrayValue =
                                ((SweDataArrayValue) sosMultiObservationValues.getValue());
                        List<String> newBlock =
                                createBlock(sweDataArrayValue.getValue().getElementType(), phenomenonTime, phenID,
                                        value);
                        sweDataArrayValue.addBlock(newBlock);
                    } else {
                        // observation new => create new one
                        SosObservation sosObservation = new SosObservation();
                        sosObservation.setObservationID(Long.toString(hObservation.getObservationId()));
                        sosObservation.setIdentifier(new CodeWithAuthority(hObservation.getIdentifier()));
                        sosObservation.setNoDataValue(Configurator.getInstance().getNoDataValue());
                        sosObservation.setTokenSeparator(Configurator.getInstance().getTokenSeperator());
                        sosObservation.setTupleSeparator(Configurator.getInstance().getTupleSeperator());
                        sosObservation.setObservationConstellation(observationConstellations.get(obsConstHash));

                        SosSweDataArray dataArray = new SosSweDataArray();
                        dataArray.setElementType(createElementType(sosObservation.getObservationConstellation(),
                                hObservation));

                        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
                        // FIXME where to get element type from? ->
                        // ObservationConstellation -> Annahme: 1 ObsProp.
                        // phenTime, resultTime?, phenID, value
                        // MetaPhen -> Not Supported now
                        //
                        dataArrayValue.setValue(dataArray);
                        List<String> newBlock = createBlock(dataArray.getElementType(), phenomenonTime, phenID, value);
                        dataArrayValue.addBlock(newBlock);

                        SosMultiObservationValues observationValue = new SosMultiObservationValues();
                        observationValue.setValue(dataArrayValue);
                        sosObservation.setValue(observationValue);
                        antiSubsettingObservations.put(hObservation.getAntiSubsetting(), sosObservation);
                    }
                } else if (templatedObservations.containsKey(obsConstHash)) {
                    SosObservation o = templatedObservations.get(obsConstHash);
                    SweDataArrayValue dataArrayValue;
                    if (o == null) {
                        List<ResultTemplate> resultTemplates = template4ObsConst.get(obsConstHash);
                        /* TODO choose the right template ... */
                        ResultTemplate resultTemplate = resultTemplates.iterator().next();
                        SosResultEncoding encoding = new SosResultEncoding(resultTemplate.getResultEncoding());
                        SosSweTextEncoding sweTextEncoding = (SosSweTextEncoding) encoding.getEncoding();
                        SosResultStructure structure = new SosResultStructure(resultTemplate.getResultStructure());
                        SosSweAbstractDataComponent sosSweStructure = structure.getResultStructure();

                        o = new SosObservation();
                        o.setObservationID(Long.toString(hObservation.getObservationId()));
                        o.setIdentifier(new CodeWithAuthority(hObservation.getIdentifier()));
                        o.setNoDataValue(Configurator.getInstance().getNoDataValue());
                        o.setTokenSeparator(sweTextEncoding.getTokenSeparator());
                        o.setTupleSeparator(sweTextEncoding.getBlockSeparator());
                        o.setObservationConstellation(observationConstellations.get(obsConstHash));

                        SosSweAbstractDataComponent comp =
                                new SosResultStructure(resultTemplate.getResultStructure()).getResultStructure();
                        if (comp instanceof SosSweDataArray
                                && ((SosSweDataArray) comp).getElementType() instanceof SosSweDataRecord) {
                            o.setResultStructure((SosSweDataRecord) ((SosSweDataArray) comp).getElementType());
                        } else if (comp instanceof SosSweDataRecord) {
                            o.setResultStructure((SosSweDataRecord) comp);
                        }
                        dataArrayValue = new SweDataArrayValue();
                        SosSweDataArray da = new SosSweDataArray();
                        da.setEncoding(sweTextEncoding);
                        da.setElementType(sosSweStructure);
                        dataArrayValue.setValue(da);
                        SosMultiObservationValues observationValue = new SosMultiObservationValues();
                        observationValue.setValue(dataArrayValue);
                        o.setValue(observationValue);
                        templatedObservations.put(obsConstHash, o);
                    } else {
                        dataArrayValue = (SweDataArrayValue) ((SosMultiObservationValues) o.getValue()).getValue();
                    }
                    // TODO check for NPE in next statement
                    dataArrayValue.addBlock(createBlock(o.getResultStructure(), phenomenonTime, phenID, value));
                } else {
                    SosObservation sosObservation = new SosObservation();
                    sosObservation.setObservationID(Long.toString(hObservation.getObservationId()));
                    sosObservation.setIdentifier(new CodeWithAuthority(hObservation.getIdentifier()));
                    sosObservation.setNoDataValue(Configurator.getInstance().getNoDataValue());
                    sosObservation.setTokenSeparator(Configurator.getInstance().getTokenSeperator());
                    sosObservation.setTupleSeparator(Configurator.getInstance().getTupleSeperator());
                    sosObservation.setObservationConstellation(observationConstellations.get(obsConstHash));
                    sosObservation.setResultTime(new TimeInstant(new DateTime(hObservation.getResultTime())));
                    sosObservation.setValue(new SosSingleObservationValue(phenomenonTime, value, qualityList));
                    observationCollection.add(sosObservation);
                }
                if (antiSubsettingObservations.values() != null && !antiSubsettingObservations.values().isEmpty()) {
                    observationCollection.addAll(antiSubsettingObservations.values());
                }
            }
            observationCollection.addAll(templatedObservations.values());
        }
        return observationCollection;
    }

	private static boolean isSubsetIdAvailable(Observation hObservation)
	{
		return hObservation.getAntiSubsetting() != null && 
		        !hObservation.getAntiSubsetting().isEmpty();
	}

    private static SosSweAbstractDataComponent createElementType(
    		SosObservationConstellation observationConstellation,
            Observation hObservation)
    {
        SosSweDataRecord elementType = new SosSweDataRecord();
        String observationType = observationConstellation.getObservationType();
        String observedProperty = observationConstellation.getObservableProperty().getIdentifier();

        addObservationResultField(elementType, hObservation, observationType, observedProperty);

        if (hObservation.getResultTime() != null) {
            addResultTimeField(elementType);
        }

        addPhenomenonTimeField(hObservation, elementType);

        return elementType;
    }

    private static void addPhenomenonTimeField(Observation hObservation, SosSweDataRecord elementType) {
        SosSweTime timeFieldElement;
        if (hObservation.getPhenomenonTimeEnd() != null) {
            // it is a time range -> definition constant, uom constant
            // swe:TimeRange
            timeFieldElement = new SosSweTimeRange();
        } else {
            // it is a time instant -> swe:Time
            timeFieldElement = new SosSweTime();
        }
        timeFieldElement.setDefinition(OMConstants.PHENOMENON_TIME);
        timeFieldElement.setUom(OMConstants.PHEN_UOM_ISO8601);
        SosSweField phenTimeField = new SosSweField("phenomenonTime", timeFieldElement);
        elementType.addField(phenTimeField);
    }

    private static void addResultTimeField(SosSweDataRecord elementType) {
        // add time field for result time
        SosSweTime resultTimeFieldElement = new SosSweTime();
        // TODO is this the correct constants for resultTime?
        resultTimeFieldElement.setDefinition(OMConstants.PHEN_SAMPLING_TIME);
        resultTimeFieldElement.setUom(OMConstants.PHEN_UOM_ISO8601);
        SosSweField resultTimeField = new SosSweField("resultTime", resultTimeFieldElement);
        elementType.addField(resultTimeField);
    }

    private static void addObservationResultField(SosSweDataRecord elementType, Observation hObservation,
            String observationType, String observedProperty) {
        SosSweField observationResultField;
        SosSweAbstractDataComponent observedValueFieldElement;
        if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_MEASUREMENT)) {
            observedValueFieldElement = new SosSweQuantity();
            ((SosSweQuantity) observedValueFieldElement).setUom(hObservation.getUnit().getUnit());
        } 
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)) {
            observedValueFieldElement = new SosSweCategory();
            ((SosSweCategory) observedValueFieldElement).setCodeSpace(hObservation.getUnit().getUnit());
        } 
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_COUNT_OBSERVATION)) {
            observedValueFieldElement = new SosSweCount();
        } 
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_COMPLEX_OBSERVATION)) {
            // TODO what todo in the case of complex observations?
            String exceptionMsg = String.format("Received observation type is not supported: %s", observationType);
            LOGGER.debug(exceptionMsg);
            throw new IllegalArgumentException(exceptionMsg);
        } 
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_OBSERVATION)) {
            // TODO what todo in the case of a generic observation?
            String exceptionMsg = String.format("Received observation type is not supported: %s", observationType);
            LOGGER.debug(exceptionMsg);
            throw new IllegalArgumentException(exceptionMsg);
        } 
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_TEXT_OBSERVATION)) {
            observedValueFieldElement = new SosSweText();
        } 
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
            observedValueFieldElement = new SosSweBoolean();
        }
        else if (observationType.equalsIgnoreCase(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION))
        {
        	observedValueFieldElement = new SosSweDataArray();
        }
        else
        {
            String exceptionMsg = String.format("Received observation type is not supported: %s", observationType);
            LOGGER.debug(exceptionMsg);
            throw new IllegalArgumentException(exceptionMsg);
        }
        observedValueFieldElement.setDefinition(observedProperty);
        observationResultField = new SosSweField("result", observedValueFieldElement);
        elementType.addField(observationResultField);
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

    private static boolean isSubsettingExtensionSet(SwesExtensions extensions)
    {
        return extensions!=null?
        		extensions.isBooleanExentsionSet(Sos2Constants.Extensions.Subsetting.name()):
        			false;
    }

    private static void checkOrSetObservablePropertyUnit(AbstractSosPhenomenon abstractSosPhenomenon, String unit) {
        if (abstractSosPhenomenon instanceof SosObservableProperty) {
            SosObservableProperty obsProp = (SosObservableProperty) abstractSosPhenomenon;
            if (obsProp.getUnit() == null && unit != null) {
                obsProp.setUnit(unit);
            }
        }
    }

    /**
     * Get observation value from all value tables for an Observation object
     * 
     * @param hObservation
     *            Observation object
     * @return Observation value
     */
    private static IValue getValueFromAllTable(Observation hObservation) {
        if (hObservation.getBooleanValues() != null && !hObservation.getBooleanValues().isEmpty()) {
            return new org.n52.sos.ogc.om.values.BooleanValue(
                    getValueFromBooleanValueTable(hObservation.getBooleanValues()));
        } else if (hObservation.getCategoryValues() != null && !hObservation.getCategoryValues().isEmpty()) {
            return new org.n52.sos.ogc.om.values.CategoryValue(
                    getValueFromCategoryValueTable(hObservation.getCategoryValues()));
        } else if (hObservation.getCountValues() != null && !hObservation.getCountValues().isEmpty()) {
            return new org.n52.sos.ogc.om.values.CountValue(getValueFromCountValueTable(hObservation.getCountValues()));
        } else if (hObservation.getNumericValues() != null && !hObservation.getNumericValues().isEmpty()) {
            return new QuantityValue(getValueFromNumericValueTable(hObservation.getNumericValues()));
        } else if (hObservation.getTextValues() != null && !hObservation.getTextValues().isEmpty()) {
            return new org.n52.sos.ogc.om.values.TextValue(getValueFromTextValueTable(hObservation.getTextValues()));
        } else if (hObservation.getGeometryValues() != null && !hObservation.getGeometryValues().isEmpty()) {
            return new org.n52.sos.ogc.om.values.GeometryValue(
                    getValueFromGeometryValueTable(hObservation.getGeometryValues()));
        }
        return null;
    }

    /**
     * Get observation value from numeric table
     * 
     * @param numericValues
     *            Numeric values
     * @return Numeric value
     */
    private static Double getValueFromNumericValueTable(Set<NumericValue> numericValues) {
        for (NumericValue numericValue : numericValues) {
            return new Double(numericValue.getValue());
        }
        return Double.NaN;
    }

    private static Boolean getValueFromBooleanValueTable(Set<BooleanValue> booleanValues) {
        for (BooleanValue booleanValue : booleanValues) {
            return Boolean.valueOf(booleanValue.getValue());
        }
        return null;
    }

    private static Integer getValueFromCountValueTable(Set<CountValue> countValues) {
        for (CountValue countValue : countValues) {
            return new Integer(countValue.getValue());
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Get observation value from text table
     * 
     * @param textValues
     *            Text values
     * @return Text value
     */
    private static String getValueFromTextValueTable(Set<TextValue> textValues) {
        for (TextValue textValue : textValues) {
            return textValue.getValue();
        }
        return "";
    }

    private static String getValueFromCategoryValueTable(Set<CategoryValue> categoryValues) {
        for (CategoryValue categoryValue : categoryValues) {
            return categoryValue.getValue();
        }
        return "";
    }

    /**
     * Get observation value from spatial table
     * 
     * @param geometryValues
     *            Spatial values
     * @return Spatial value
     */
    private static Geometry getValueFromGeometryValueTable(Set<GeometryValue> geometryValues) {
        for (GeometryValue geometryValue : geometryValues) {
            return (Geometry) geometryValue.getValue();
        }
        return null;
    }

    /**
     * Adds a FOI to the map with FOIs for procedures
     * 
     * @param feature4proc
     *            FOIs for procedure map
     * @param procID
     *            procedure identifier
     * @param foiID
     *            FOI identifier
     * @return updated map
     */
    private static Map<String, Set<String>> setFeatureForProcedure(Map<String, Set<String>> feature4proc,
            String procID, String foiID) {
        Set<String> features;
        if (feature4proc.containsKey(procID)) {
            features = feature4proc.get(procID);

        } else {
            features = new HashSet<String>();
        }
        if (!features.contains(foiID)) {

        }
        features.add(foiID);
        feature4proc.put(procID, features);
        return feature4proc;
    }

    public static List<SosObservation> unfoldObservation(SosObservation multiObservation) throws OwsExceptionReport {
        if (multiObservation.getValue() instanceof SosSingleObservationValue) {
            return Collections.singletonList(multiObservation);
        } else {
            SweDataArrayValue arrayValue =
                    ((SweDataArrayValue) ((SosMultiObservationValues) multiObservation.getValue()).getValue());
            List<List<String>> values = arrayValue.getValue().getValues();
            List<SosObservation> observationCollection = new ArrayList<SosObservation>(values.size());
            SosSweDataRecord elementType = null;
            if (arrayValue.getValue().getElementType() != null
                    && arrayValue.getValue().getElementType() instanceof SosSweDataRecord) {
                elementType = (SosSweDataRecord) arrayValue.getValue().getElementType();
            } else {
                String exceptionMsg =
                        String.format("sweElementType type \"%s\" not supported", elementType != null ? elementType
                                .getClass().getName() : "null");
                LOGGER.debug(exceptionMsg);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
            }

            // FIXME each block represents one observation <-- this is not
            // always true!
            for (List<String> block : values) {
                int tokenIndex = 0;
                ITime phenomenonTime = null;
                List<IValue> observedValues = new ArrayList<IValue>();
                // map to store the observed properties
                Map<IValue, String> definitionsForObservedValues = new HashMap<IValue, String>();
                IValue observedValue = null;
                for (String token : block) {
                    // get values from block via definition in
                    // SosSweDataArray#getElementType
                    SosSweAbstractDataComponent fieldForToken = elementType.getFields().get(tokenIndex).getElement();
                    /*
                     * get phenomenon time
                     */

                    if (fieldForToken instanceof SosSweTime) {
                        try {
                            if (fieldForToken instanceof SosSweTimeRange) {
                                String[] subTokens = token.split("/");
                                phenomenonTime =
                                        new TimePeriod(DateTimeHelper.parseIsoString2DateTime(subTokens[0]),
                                                DateTimeHelper.parseIsoString2DateTime(subTokens[1]));
                            } else {
                                phenomenonTime = new TimeInstant(DateTimeHelper.parseIsoString2DateTime(token));
                            }
                        } catch (Exception e) {
                            if (e instanceof OwsExceptionReport) {
                                throw (OwsExceptionReport) e;
                            } else {
                                OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                                String exceptionMsg = "Error while parse time String to DateTime!";
                                LOGGER.error(exceptionMsg, e);
                                /*
                                 * FIXME what is the valid exception code if the
                                 * result is not correct?
                                 */
                                owse.addCodedException(OWSConstants.OwsExceptionCode.NoApplicableCode, null,
                                        exceptionMsg);
                                throw owse;
                            }
                        }
                    }
                    /*
                     * observation values
                     */
                    else if (fieldForToken instanceof SosSweQuantity) {
                        observedValue = new QuantityValue(Double.parseDouble(token));
                        observedValue.setUnit(((SosSweQuantity) fieldForToken).getUom());
                    } else if (fieldForToken instanceof SosSweBoolean) {
                        observedValue = new org.n52.sos.ogc.om.values.BooleanValue(Boolean.parseBoolean(token));
                    } else if (fieldForToken instanceof SosSweText) {
                        observedValue = new org.n52.sos.ogc.om.values.TextValue(token);
                    } else if (fieldForToken instanceof SosSweCategory) {
                        observedValue = new org.n52.sos.ogc.om.values.CategoryValue(token);
                        observedValue.setUnit(((SosSweCategory) fieldForToken).getCodeSpace());
                    } else if (fieldForToken instanceof SosSweCount) {
                        observedValue = new org.n52.sos.ogc.om.values.CountValue(Integer.parseInt(token));
                    } else {
                        String exceptionMsg =
                                String.format("sweField type \"%s\" not supported",
                                        fieldForToken != null ? fieldForToken.getClass().getName() : "null");
                        LOGGER.debug(exceptionMsg);
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
                    }
                    if (observedValue != null) {
                        definitionsForObservedValues.put(observedValue, fieldForToken.getDefinition());
                        observedValues.add(observedValue);
                        observedValue = null;
                    }
                    tokenIndex++;
                }
                // TODO: Eike implement usage of elementType
                for (IValue iValue : observedValues) {
                    IObservationValue value = new SosSingleObservationValue(phenomenonTime, iValue);
                    SosObservation newObservation = new SosObservation();
                    newObservation.setNoDataValue(multiObservation.getNoDataValue());
                    /*
                     * TODO create new ObservationConstellation only with the
                     * specified observed property and observation type
                     */
                    SosObservationConstellation obsConst = multiObservation.getObservationConstellation();/*createObservationConstellationForSubObservation(
                    		multiObservation.getObservationConstellation(),
                    		iValue,
                    		definitionsForObservedValues.get(iValue))*/;
                    newObservation.setObservationConstellation(obsConst);
                    newObservation.setValidTime(multiObservation.getValidTime());
                    newObservation.setResultTime(multiObservation.getResultTime());
                    newObservation.setTokenSeparator(multiObservation.getTokenSeparator());
                    newObservation.setTupleSeparator(multiObservation.getTupleSeparator());
                    newObservation.setResultType(multiObservation.getResultType());
                    newObservation.setValue(value);
                    observationCollection.add(newObservation);
                }
            }
            return observationCollection;
        }
    }

    private static SosObservationConstellation createObservationConstellationForSubObservation(
            SosObservationConstellation observationConstellation, IValue iValue, String phenomenonID) {
        SosObservationConstellation constellation = new SosObservationConstellation();
        constellation.setFeatureOfInterest(observationConstellation.getFeatureOfInterest());
        constellation.setObservableProperty(new AbstractSosPhenomenon(phenomenonID));
        constellation.setObservationType(OMHelper.getObservationTypeFromValue(iValue));
        constellation.setOfferings(observationConstellation.getOfferings());
        constellation.setProcedure(observationConstellation.getProcedure());
        return constellation;
    }

}
