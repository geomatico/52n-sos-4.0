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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.hibernate.entities.BlobObservation;
import org.n52.sos.ds.hibernate.entities.BlobValue;
import org.n52.sos.ds.hibernate.entities.BooleanObservation;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.CategoryObservation;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.CountObservation;
import org.n52.sos.ds.hibernate.entities.CountValue;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.GeometryObservation;
import org.n52.sos.ds.hibernate.entities.GeometryValue;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.Quality;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.entities.TextObservation;
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
import org.n52.sos.ogc.om.values.NilTemplateValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.UnknownValue;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.simpleType.SosSweBoolean;
import org.n52.sos.ogc.swe.simpleType.SosSweCategory;
import org.n52.sos.ogc.swe.simpleType.SosSweCount;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweText;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.ogc.swe.simpleType.SosSweTimeRange;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.profile.IProfile;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class HibernateObservationUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateObservationUtilities.class);
    private static Configuration configuration;

    protected static Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return configuration;
    }

    /**
     * Set the configuration for this Helper to decouple it from the Configurator.
     *
     * @param configuration the configuration
     */
    protected static void setConfiguration(Configuration configuration) {
        HibernateObservationUtilities.configuration = configuration;
    }

    public static ContentCache getCache() {
        return getConfiguration().getCache();
    }

    public static IProfile getActiveProfile() {
        return getConfiguration().getActiveProfile();
    }

    @Deprecated
    public static String getTokenSeperator() {
        return getConfiguration().getTokenSeperator();
    }

    @Deprecated
    public static String getTupleSeperator() {
        return getConfiguration().getTupleSeperator();
    }
    
    public static String getTokenSeparator() {
        return getConfiguration().getTokenSeparator();
    }

    public static String getTupleSeparator() {
        return getConfiguration().getTupleSeparator();
    }

    public static IFeatureQueryHandler getFeatureQueryHandler() {
        return getConfiguration().getFeatureQueryHandler();
    }

    public static boolean isSupportsQuality() {
        return getConfiguration().isSupportsQuality();
    }
    
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
    @SuppressWarnings("unchecked")
    public static List<SosObservation> createSosObservationsFromObservations(Collection<Observation> observations,
                                                                             String version, Session session) throws OwsExceptionReport {
        List<SosObservation> observationCollection = new ArrayList<SosObservation>(0);

        Map<String, SosAbstractFeature> features = new HashMap<String, SosAbstractFeature>(0);
        Map<String, AbstractSosPhenomenon> obsProps = new HashMap<String, AbstractSosPhenomenon>(0);
        Map<String, SosProcedureDescription> procedures = new HashMap<String, SosProcedureDescription>(0);
        Map<Integer, SosObservationConstellation> observationConstellations =
                new HashMap<Integer, SosObservationConstellation>(0);
        Map<String, org.n52.sos.ogc.sos.ResultTemplate> resultTemplates =
                new HashMap<String, org.n52.sos.ogc.sos.ResultTemplate>(0);
        if (observations != null) {
            // now iterate over resultset and create Measurement for each row
            for (Observation hObservation : observations) {
                // check remaining heap size and throw exception if minimum is
                // reached
                SosHelper.checkFreeMemory();

                Set<ObservationConstellationOfferingObservationType> observationConstellationOfferingObservationTypes =
                        hObservation.getObservationConstellationOfferingObservationTypes();
                Iterator<ObservationConstellationOfferingObservationType> iterator =
                        observationConstellationOfferingObservationTypes.iterator();
                ObservationConstellationOfferingObservationType hObservationConstellationOfferingObservationType =
                        null;
                while (iterator.hasNext()) {
                    hObservationConstellationOfferingObservationType = iterator.next();
                    break;
                }

                ObservationConstellation hObservationConstellation = hObservation.getObservationConstellation();
                FeatureOfInterest hFeatureOfInterest = hObservation.getFeatureOfInterest();

                // TODO get full description
                Procedure hProcedure = hObservationConstellation.getProcedure();
                String procedureIdentifier = hProcedure.getIdentifier();
                SosProcedureDescription procedure;
                if (procedures.containsKey(procedureIdentifier)) {
                    procedure = procedures.get(procedureIdentifier);
                } else {
                    procedure =
                            HibernateProcedureUtilities.createSosProcedureDescription(hProcedure, hProcedure
                                    .getIdentifier(), hProcedure.getProcedureDescriptionFormat()
                                    .getProcedureDescriptionFormat());
                    procedures.put(procedureIdentifier, procedure);
                }

                //FIXME possible NPE
                String observationType =
                        hObservationConstellationOfferingObservationType.getObservationType().getObservationType();

                // feature of interest
                String foiID = hFeatureOfInterest.getIdentifier();
                if (!features.containsKey(foiID)) {
                    SosAbstractFeature featureByID =
                            getConfiguration().getFeatureQueryHandler()
                                    .getFeatureByID(foiID, session, version, -1);
                    features.put(foiID, featureByID);
                }

                // phenomenon
                String phenID = hObservationConstellation.getObservableProperty().getIdentifier();
                String description = hObservationConstellation.getObservableProperty().getDescription();
                if (!obsProps.containsKey(phenID)) {
                    obsProps.put(phenID, new SosObservableProperty(phenID, description, null, null));
                }
                // TODO: remove or add comment why it is here
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

                // create quality
                ArrayList<SosQuality> qualityList = null;
                if (isSupportsQuality()) {
                    hObservation.getQualities();
                    for (Quality hQuality : hObservation.getQualities()) {
                        String qualityTypeString = hQuality.getSweType().getSweType();
                        String qualityUnit = hQuality.getUnit().getUnit();
                        String qualityName = hQuality.getName();
                        String qualityValue = hQuality.getValue();
                        qualityList = new ArrayList<SosQuality>(1);
                        if (qualityValue != null) {
                            QualityType qualityType = QualityType.valueOf(qualityTypeString);
                            SosQuality quality = new SosQuality(qualityName, qualityUnit, qualityValue, qualityType);
                            qualityList.add(quality);
                        }
                    }
                }
                IValue<?> value = getValueFromObservation(hObservation);
                if (hObservation.getUnit() != null) {
                    value.setUnit(hObservation.getUnit().getUnit());
                }
                checkOrSetObservablePropertyUnit(obsProps.get(phenID), value.getUnit());
                final SosObservationConstellation obsConst =
                        new SosObservationConstellation(procedure, obsProps.get(phenID), null, features.get(foiID),
                                observationType);
                /* get the offerings to find the templates */
                if (obsConst.getOfferings() == null) {
                    HashSet<String> offerings = new HashSet<String>(getCache()
                            .getOfferingsForObservableProperty(obsConst.getObservableProperty().getIdentifier()));
                    offerings.retainAll(getCache().getOfferingsForProcedure(obsConst.getProcedure()
                            .getProcedureIdentifier()));
                    obsConst.setOfferings(offerings);
                }
                int obsConstHash = obsConst.hashCode();
                if (!observationConstellations.containsKey(obsConstHash)) {
                    if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)) {
                        List<ResultTemplate> hResultTemplates =
                                HibernateCriteriaQueryUtilities.getResultTemplateObjectsForObservationConstellation(
                                        hObservationConstellation, session);
                        // Set<ResultTemplate> hResultTemplates =
                        // hObservationConstellation.getResultTemplates();
                        if (hResultTemplates != null && !hResultTemplates.isEmpty()) {
                            for (ResultTemplate hResultTemplate : hResultTemplates) {
                                if (hResultTemplate.getIdentifier() != null
                                        && !hResultTemplate.getIdentifier().isEmpty()) {
                                    org.n52.sos.ogc.sos.ResultTemplate resultTemplate = null;
                                    if (resultTemplates.containsKey(hResultTemplate.getIdentifier())) {
                                        resultTemplate = resultTemplates.get(hResultTemplate.getIdentifier());
                                    } else {
                                        resultTemplate = new org.n52.sos.ogc.sos.ResultTemplate();
                                        resultTemplate.setXmlResultStructure(hResultTemplate.getResultStructure());
                                        resultTemplate.setXmResultEncoding(hResultTemplate.getResultEncoding());
                                        resultTemplates.put(hResultTemplate.getIdentifier(), resultTemplate);
                                    }
                                    obsConst.setResultTemplate(resultTemplate);
                                    break;
                                }
                            }
                        }
                    }
                    observationConstellations.put(obsConstHash, obsConst);
                }
                SosObservation sosObservation =
                        createNewObservation(observationConstellations, hObservation, qualityList, value, obsConstHash);
                if (hObservation.getSetId() != null && !hObservation.getSetId().isEmpty()) {
                    sosObservation.setSetId(hObservation.getSetId());
                }
                observationCollection.add(sosObservation);
            }
        }
        return observationCollection;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Collection<? extends SosObservation> createSosObservationFromObservationConstellation(
            ObservationConstellation observationConstellation, List<String> featureOfInterestIdentifiers,
            String version, Session session) throws OwsExceptionReport {
        List<SosObservation> observations = new ArrayList<SosObservation>(0);
        if (observationConstellation != null && featureOfInterestIdentifiers != null) {
            String procID = observationConstellation.getProcedure().getIdentifier();
            SensorML procedure = new SensorML();
            SosSMLIdentifier identifier =
                    new SosSMLIdentifier("uniqueID", "urn:ogc:def:identifier:OGC:uniqueID", procID);
            List<SosSMLIdentifier> identifiers = new ArrayList<SosSMLIdentifier>(1);
            identifiers.add(identifier);
            procedure.setIdentifications(identifiers);

            // phenomenon
            String phenID = observationConstellation.getObservableProperty().getIdentifier();
            String description = observationConstellation.getObservableProperty().getDescription();
            SosObservableProperty obsProp = new SosObservableProperty(phenID, description, null, null);

            for (String featureIdentifier : featureOfInterestIdentifiers) {
                SosAbstractFeature feature =
                        getFeatureQueryHandler()
                                .getFeatureByID(featureIdentifier, session, version, -1);

                final SosObservationConstellation obsConst =
                        new SosObservationConstellation(procedure, obsProp, null, feature, null);
                /* get the offerings to find the templates */
                if (obsConst.getOfferings() == null) {
                    Set<String> offerings = new HashSet<String>(getCache().getOfferingsForProcedure(
                            obsConst.getProcedure().getProcedureIdentifier()));
                    offerings.retainAll(new HashSet<String>(getCache().getOfferingsForProcedure(
                            obsConst.getProcedure().getProcedureIdentifier())));
                    obsConst.setOfferings(offerings);
                }
                SosObservation sosObservation = new SosObservation();
                sosObservation.setNoDataValue(getActiveProfile().getResponseNoDataPlaceholder());
                sosObservation.setTokenSeparator(getTokenSeparator());
                sosObservation.setTupleSeparator(getTupleSeparator());
                sosObservation.setObservationConstellation(obsConst);
                NilTemplateValue value = new NilTemplateValue();
                value.setUnit(obsProp.getUnit());
                sosObservation.setValue(new SosSingleObservationValue(new TimeInstant(), value,
                        new ArrayList<SosQuality>(0)));
                observations.add(sosObservation);
            }
        }
        return observations;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static SosObservation createNewObservation(
            Map<Integer, SosObservationConstellation> observationConstellations, Observation hObservation,
            ArrayList<SosQuality> qualityList, IValue<?> value, int obsConstHash) {
        SosObservation sosObservation = new SosObservation();
        sosObservation.setObservationID(Long.toString(hObservation.getObservationId()));
        if (hObservation.getIdentifier() != null && !hObservation.getIdentifier().isEmpty()
                && !hObservation.getIdentifier().startsWith(SosConstants.GENERATED_IDENTIFIER_PREFIX)) {
            sosObservation.setIdentifier(new CodeWithAuthority(hObservation.getIdentifier()));
        }
        sosObservation.setNoDataValue(getActiveProfile().getResponseNoDataPlaceholder());
        sosObservation.setTokenSeparator(getTokenSeparator());
        sosObservation.setTupleSeparator(getTupleSeparator());
        sosObservation.setObservationConstellation(observationConstellations.get(obsConstHash));
        sosObservation.setResultTime(new TimeInstant(new DateTime(hObservation.getResultTime())));
        sosObservation.setValue(new SosSingleObservationValue(getPhenomenonTime(hObservation), value, qualityList));
        return sosObservation;
    }

    private static ITime getPhenomenonTime(Observation hObservation) {
        // create time element
        DateTime phenStartTime = new DateTime(hObservation.getPhenomenonTimeStart());
        DateTime phenEndTime;
        if (hObservation.getPhenomenonTimeEnd() != null) {
            phenEndTime = new DateTime(hObservation.getPhenomenonTimeEnd());
        } else {
            phenEndTime = phenStartTime;
        }
        ITime phenomenonTime;
        if (phenStartTime.equals(phenEndTime)) {
            phenomenonTime = new TimeInstant(phenStartTime, "");
        } else {
            phenomenonTime = new TimePeriod(phenStartTime, phenEndTime);
        }
        return phenomenonTime;
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
    private static IValue<?> getValueFromObservation(Observation hObservation) {
        if (hObservation instanceof NumericObservation) {
            return new QuantityValue(getNumericValueTable(((NumericObservation) hObservation).getValue()));
        } else if (hObservation instanceof BooleanObservation) {
            return new org.n52.sos.ogc.om.values.BooleanValue(getBooleanValueTable(((BooleanObservation) hObservation).getValue()));
        } else if (hObservation instanceof CategoryObservation) {
            return new org.n52.sos.ogc.om.values.CategoryValue(getCategoryValueTable(((CategoryObservation) hObservation).getValue()));
        } else if (hObservation instanceof CountObservation) {
            return new org.n52.sos.ogc.om.values.CountValue(getCountValueTable(((CountObservation) hObservation).getValue()));
        } else if (hObservation instanceof TextObservation) {
            return new org.n52.sos.ogc.om.values.TextValue(getTextValueTable(((TextObservation) hObservation).getValue()));
        } else if (hObservation instanceof GeometryObservation) {
            return new org.n52.sos.ogc.om.values.GeometryValue(getGeometryValueTable(((GeometryObservation) hObservation).getValue()));
        } else if (hObservation instanceof BlobObservation) {
            return new UnknownValue(getBlobValueTable(((BlobObservation) hObservation).getValue()));
        }
        return null;

        // if (hObservation.getBooleanValues() != null &&
        // !hObservation.getBooleanValues().isEmpty()) {
        // return new org.n52.sos.ogc.om.values.BooleanValue(
        // getValueFromBooleanValueTable(hObservation.getBooleanValues()));
        // } else if (hObservation.getCategoryValues() != null &&
        // !hObservation.getCategoryValues().isEmpty()) {
        // return new org.n52.sos.ogc.om.values.CategoryValue(
        // getValueFromCategoryValueTable(hObservation.getCategoryValues()));
        // } else if (hObservation.getCountValues() != null &&
        // !hObservation.getCountValues().isEmpty()) {
        // return new
        // org.n52.sos.ogc.om.values.CountValue(getValueFromCountValueTable(hObservation.getCountValues()));
        // } else if (hObservation.getNumericValues() != null &&
        // !hObservation.getNumericValues().isEmpty()) {
        // return new
        // QuantityValue(getValueFromNumericValueTable(hObservation.getNumericValues()));
        // } else if (hObservation.getTextValues() != null &&
        // !hObservation.getTextValues().isEmpty()) {
        // return new
        // org.n52.sos.ogc.om.values.TextValue(getValueFromTextValueTable(hObservation.getTextValues()));
        // } else if (hObservation.getGeometryValues() != null &&
        // !hObservation.getGeometryValues().isEmpty()) {
        // return new org.n52.sos.ogc.om.values.GeometryValue(
        // getValueFromGeometryValueTable(hObservation.getGeometryValues()));
        // }
        // return null;
    }

    private static Object getBlobValueTable(BlobValue value) {
        return value.getValue();
    }

    private static Boolean getBooleanValueTable(BooleanValue value) {
        return Boolean.valueOf(value.getValue());
    }

    private static String getCategoryValueTable(CategoryValue value) {
        return value.getValue();
    }

    private static Integer getCountValueTable(CountValue value) {
        return Integer.valueOf(value.getValue());
    }

    private static Geometry getGeometryValueTable(GeometryValue value) {
        return value.getValue();
    }

    private static BigDecimal getNumericValueTable(NumericValue value) {
        return value.getValue();
    }

    private static String getTextValueTable(TextValue value) {
        return value.getValue();
    }

    // /**
    // * Get observation value from numeric table
    // *
    // * @param numericValues
    // * Numeric values
    // * @return Numeric value
    // */
    // private static Double getValueFromNumericValueTable(Set<NumericValue>
    // numericValues) {
    // for (NumericValue numericValue : numericValues) {
    // return new Double(numericValue.getValue());
    // }
    // return Double.NaN;
    // }
    //
    // private static Boolean getValueFromBooleanValueTable(Set<BooleanValue>
    // booleanValues) {
    // for (BooleanValue booleanValue : booleanValues) {
    // return Boolean.valueOf(booleanValue.getValue());
    // }
    // return null;
    // }
    //
    // private static Integer getValueFromCountValueTable(Set<CountValue>
    // countValues) {
    // for (CountValue countValue : countValues) {
    // return Integer.valueOf(countValue.getValue());
    // }
    // return Integer.MIN_VALUE;
    // }
    //
    // /**
    // * Get observation value from text table
    // *
    // * @param textValues
    // * Text values
    // * @return Text value
    // */
    // private static String getValueFromTextValueTable(Set<TextValue>
    // textValues) {
    // for (TextValue textValue : textValues) {
    // return textValue.getValue();
    // }
    // return "";
    // }
    //
    // private static String getValueFromCategoryValueTable(Set<CategoryValue>
    // categoryValues) {
    // for (CategoryValue categoryValue : categoryValues) {
    // return categoryValue.getValue();
    // }
    // return "";
    // }
    //
    // /**
    // * Get observation value from spatial table
    // *
    // * @param geometryValues
    // * Spatial values
    // * @return Spatial value
    // */
    // private static Geometry getValueFromGeometryValueTable(Set<GeometryValue>
    // geometryValues) {
    // for (GeometryValue geometryValue : geometryValues) {
    // return geometryValue.getValue();
    // }
    // return null;
    // }

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

            for (List<String> block : values) {
                int tokenIndex = 0;
                ITime phenomenonTime = null;
                List<IValue<?>> observedValues = new ArrayList<IValue<?>>();
                // map to store the observed properties
                Map<IValue<?>, String> definitionsForObservedValues = new HashMap<IValue<?>, String>();
                IValue<?> observedValue = null;
                for (String token : block) {
                    // get values from block via definition in
                    // SosSweDataArray#getElementType
                    SosSweAbstractDataComponent fieldForToken = elementType.getFields().get(tokenIndex).getElement();
                    /*
                     * get phenomenon time
                     */
                    if (fieldForToken instanceof SosSweTime) {
                        try {
                            phenomenonTime = new TimeInstant(DateTimeHelper.parseIsoString2DateTime(token));
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
                    } else if (fieldForToken instanceof SosSweTimeRange) {
                        try {
                            String[] subTokens = token.split("/");
                            phenomenonTime =
                                    new TimePeriod(DateTimeHelper.parseIsoString2DateTime(subTokens[0]),
                                            DateTimeHelper.parseIsoString2DateTime(subTokens[1]));
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
                        observedValue = new QuantityValue(new BigDecimal(token));
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
                for (IValue<?> iValue : observedValues) {
                    SosObservation newObservation =
                            createSingleValueObservation(multiObservation, phenomenonTime, iValue);
                    observationCollection.add(newObservation);
                }
            }
            return observationCollection;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static SosObservation createSingleValueObservation(SosObservation multiObservation, ITime phenomenonTime,
                                                               IValue<?> iValue) {
        IObservationValue<?> value = new SosSingleObservationValue(phenomenonTime, iValue);
        SosObservation newObservation = new SosObservation();
        newObservation.setNoDataValue(multiObservation.getNoDataValue());
        /*
         * TODO create new ObservationConstellation only with the specified
         * observed property and observation type
         */
        SosObservationConstellation obsConst = multiObservation.getObservationConstellation();
        /*
         * createObservationConstellationForSubObservation ( multiObservation .
         * getObservationConstellation ( ) , iValue ,
         * definitionsForObservedValues . get ( iValue ) )
         */
        newObservation.setObservationConstellation(obsConst);
        newObservation.setValidTime(multiObservation.getValidTime());
        newObservation.setResultTime(multiObservation.getResultTime());
        newObservation.setTokenSeparator(multiObservation.getTokenSeparator());
        newObservation.setTupleSeparator(multiObservation.getTupleSeparator());
        newObservation.setResultType(multiObservation.getResultType());
        newObservation.setValue(value);
        return newObservation;
    }

    /**
     * Class to make this Helper more testable. Test cases may overwrite methods to decouple this class from the
     * Configurator.
     */
    protected static class Configuration {
        /**
         * @see Configurator#getTupleSeperator()
         */
        @Deprecated
        protected String getTupleSeperator() {
            return Configurator.getInstance().getTupleSeperator();
        }

        /**
         * @see Configurator#getTokenSeperator()
         */
        @Deprecated
        protected String getTokenSeperator() {
            return Configurator.getInstance().getTokenSeperator();
        }
        
        
        /**
         * @see Configurator#getTupleSeparator()
         */
        protected String getTupleSeparator() {
            return Configurator.getInstance().getTupleSeparator();
        }

        /**
         * @see Configurator#getTokenSeparator()
         */
        protected String getTokenSeparator() {
            return Configurator.getInstance().getTokenSeparator();
        }

        /**
         * @see Configurator#getCapabilitiesCacheController()
         */
        protected ContentCache getCache() {
            return Configurator.getInstance().getCache();
        }

        /**
         * @see Configurator#getActiveProfile()
         */
        protected IProfile getActiveProfile() {
            return Configurator.getInstance().getActiveProfile();
        }

        /**
         * @see Configurator#getFeatureQueryHandler()
         */
        protected IFeatureQueryHandler getFeatureQueryHandler() {
            return Configurator.getInstance().getFeatureQueryHandler();
        }

        /**
         * @see Configurator#isSupportsQuality()
         */
        protected boolean isSupportsQuality() {
            return Configurator.getInstance().isSupportsQuality();
        }
    }
}
