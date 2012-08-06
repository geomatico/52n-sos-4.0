package org.n52.sos.ds.hibernate.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.CountValue;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.GeometryValue;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Quality;
import org.n52.sos.ds.hibernate.entities.TextValue;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.om.quality.SosQuality.QualityType;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;

import com.vividsolutions.jts.geom.Geometry;

public class HibernateResultUtilities {

    /**
     * Create SOS internal observation from Observation objects
     * 
     * @param responseFormat
     * 
     * @param observations
     *            List of Observation objects
     * @param version
     *            SOS version
     * @param session
     *            Hibernate session
     * @return SOS internal observation
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public static List<SosObservation> createSosObservationFromObservations(List<Observation> observations,
            String version, Session session) throws OwsExceptionReport {
        List<SosObservation> observationCollection = new ArrayList<SosObservation>();

        Map<String, SosAbstractFeature> features = new HashMap<String, SosAbstractFeature>();
        Map<String, SosObservation> antiSubsettingObservations = new HashMap<String, SosObservation>();
        Map<String, AbstractSosPhenomenon> obsProps = new HashMap<String, AbstractSosPhenomenon>();
        Map<Integer, SosObservationConstellation> observationConstellations = new HashMap<Integer, SosObservationConstellation>();

//        Map<String, DateTime> featureTimeForDynamicPosition = new HashMap<String, DateTime>();
        String observationType = OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION;
        if (observations != null) {
            // now iterate over resultset and create Measurement for each
            // row
            for (Observation hObservation : observations) {
                ObservationConstellation hObservationConstellation = hObservation.getObservationConstellation();
                FeatureOfInterest hFeatureOfInterest = hObservation.getFeatureOfInterest();

                // check remaining heap size
                SosHelper.checkFreeMemory();

                String procID = hObservationConstellation.getProcedure().getIdentifier();
                observationType = hObservationConstellation.getObservationType().getObservationType();

                DateTime timeDateTime = new DateTime(hObservation.getPhenomenonTimeStart());

                // feature of interest
                String foiID = hFeatureOfInterest.getIdentifier();
                if (!features.containsKey(foiID)) {
                    features.put(foiID,
                            Configurator.getInstance().getFeatureQueryHandler()
                                    .getFeatureByID(foiID, session, version));
                }

                // phenomenon
                String phenID = hObservationConstellation.getObservableProperty().getIdentifier();
                String valueType = hObservationConstellation.getObservableProperty().getValueType().getValueType();
                String unit = hObservationConstellation.getObservableProperty().getUnit().getUnit();
                if (!obsProps.containsKey(phenID)) {
                    obsProps.put(phenID, new SosObservableProperty(phenID, null, unit, null, valueType));
                }
//                if (!version.equals(Sos2Constants.SERVICEVERSION)
//                        && Configurator.getInstance().isSetFoiLocationDynamically()
//                        && phenID.equals(Configurator.getInstance().getSpatialObsProp4DynymicLocation())) {
//                    featureTimeForDynamicPosition.put(foiID, timeDateTime);
//                }

                String offeringID = hObservationConstellation.getOffering().getIdentifier();
                String mimeType = SosConstants.PARAMETER_NOT_SET;

                // create time element
                ITime phenomenonTime = null;
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
                Object value;
                if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.booleanType.name())) {
                    value = Boolean.parseBoolean(getValueFromTextValueTable(hObservation.getTextValues()));
                } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.countType.name())) {
                    value = (Long) getValueFromCountValueTable(hObservation.getCountValues());
                } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.numericType.name())) {
                    value = getValueFromNumericValueTable(hObservation.getNumericValues());
                    // } else if (valueType
                    // .equalsIgnoreCase(SosConstants.ValueTypes.isoTimeType
                    // .name())) {
                    // value = new DateTime(resultSet.getLong(PGDAOConstants
                    // .getNumericValueCn()));
                } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.textType.name())) {
                    value = getValueFromTextValueTable(hObservation.getTextValues());
                } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.categoryType.name())) {
                    value = getValueFromTextValueTable(hObservation.getTextValues());
                } else if (valueType.equalsIgnoreCase(SosConstants.ValueTypes.spatialType.name())) {
                    value = getValueFromGeometryValueTable(hObservation.getGeometryValues());
                } else {
                    value = getValueFromAllTable(hObservation);
                }
                SosObservationConstellation obsConst = new SosObservationConstellation(procID, obsProps.get(phenID),
                        null, features.get(foiID), observationType);
                int obsConstHash = obsConst.hashCode();
                if (!observationConstellations.containsKey(obsConstHash)) {
                    observationConstellations.put(obsConstHash, obsConst);
                }

                if (hObservation.getAntiSubsetting() != null) {
                    if (antiSubsettingObservations.containsKey(hObservation.getAntiSubsetting())) {
                        // TODO: code antiSubsetting
                    } else {
                     // TODO: code antiSubsetting
                    }
                } else {
                    SosObservation sosObservation = new SosObservation();
                    sosObservation.setObservationID(Long.toString(hObservation.getObservationId()));
                    sosObservation.setIdentifier(hObservation.getIdentifier());
                    sosObservation.setNoDataValue(Configurator.getInstance().getNoDataValue());
                    sosObservation.setTokenSeparator(Configurator.getInstance().getTokenSeperator());
                    sosObservation.setTupleSeparator(Configurator.getInstance().getTupleSeperator());
                    sosObservation.setObservationConstellation(observationConstellations.get(obsConstHash));
                    sosObservation.setValue(new SosSingleObservationValue(phenomenonTime, value, qualityList));
                    observationCollection.add(sosObservation);
                }
            }
        }
//        if (!obsConstObsMap.isEmpty()) {
            // Map<String, SosAbstractFeature> sosAbstractFeatures =
            // Configurator.getInstance().getFeatureQueryHandler()
            // .getFeatures(features, null, session, version);
            // for (SosAbstractFeature feat : sosAbstractFeatures.values()) {
            // SosSamplingFeature feature = (SosSamplingFeature) feat;
            // boundedBy = SosHelper.checkEnvelope(boundedBy,
            // feature.getGeometry());
            // }
            // features.add(foiID);
            // sosObservationCollection.setObservationMembers(obsConstObsMap.values());
            // sosObservationCollection.setFeatures(sosAbstractFeatures);
            // sosObservationCollection.setBoundedBy(boundedBy);
            // sosObservationCollection.setSrid(srid);

//        }
        return observationCollection;
    }

    /**
     * Get observation value from all value tables for an Observation object
     * 
     * @param hObservation
     *            Observation object
     * @return Observation value
     */
    private static Object getValueFromAllTable(Observation hObservation) {
        if (hObservation.getNumericValues() != null && !hObservation.getNumericValues().isEmpty()) {
            return getValueFromNumericValueTable(hObservation.getNumericValues());
        } else if (hObservation.getTextValues() != null && !hObservation.getTextValues().isEmpty()) {
            return getValueFromTextValueTable(hObservation.getTextValues());
        } else if (hObservation.getGeometryValues() != null && !hObservation.getGeometryValues().isEmpty()) {
            return getValueFromGeometryValueTable(hObservation.getGeometryValues());
        } else if (hObservation.getCountValues() != null && !hObservation.getCountValues().isEmpty()) {
            return getValueFromCountValueTable(hObservation.getCountValues());
        } else if (hObservation.getCategoryValues() != null && !hObservation.getCategoryValues().isEmpty()) {
            return getValueFromCategoryValueTable(hObservation.getCategoryValues());
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
            return (Double) numericValue.getValue();
        }
        return Double.NaN;
    }

    private static Long getValueFromCountValueTable(Set<CountValue> countValues) {
        for (CountValue countValue : countValues) {
            return (long) countValue.getValue();
        }
        return Long.MIN_VALUE;
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
            return (String) textValue.getValue();
        }
        return "";
    }

    private static String getValueFromCategoryValueTable(Set<CategoryValue> categoryValues) {
        for (CategoryValue categoryValue : categoryValues) {
            return (String) categoryValue.getValue();
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

}
