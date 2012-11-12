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
import java.util.HashMap;
import java.util.HashSet;
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
import org.n52.sos.ds.hibernate.entities.TextValue;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
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
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;

import com.vividsolutions.jts.geom.Geometry;
import java.util.LinkedList;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;

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
        Map<Integer, SosObservation> antiSubsettingObservations = new HashMap<Integer, SosObservation>();
        Map<String, AbstractSosPhenomenon> obsProps = new HashMap<String, AbstractSosPhenomenon>();
        Map<Integer, SosObservationConstellation> observationConstellations =
                new HashMap<Integer, SosObservationConstellation>();
		Map<Integer, List<ResultTemplate>> template4ObsConst  = new HashMap<Integer, List<ResultTemplate>>();
		Map<Integer, SosObservation> templatedObservations = new HashMap<Integer, SosObservation>();
		
        // Map<String, DateTime> featureTimeForDynamicPosition = new
        // HashMap<String, DateTime>();
        //String observationType = OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION;
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
                    features.put(foiID,
                            Configurator.getInstance().getFeatureQueryHandler()
                            .getFeatureByID(foiID, session, version));
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
                //                    String offeringID = hObservationConstellation.getOffering().getIdentifier();
                //                    String mimeType = SosConstants.PARAMETER_NOT_SET;

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
					Set<String> offerings = new HashSet<String>(Configurator.getInstance()
							.getCapabilitiesCacheController()
							.getOfferings4Procedure(obsConst.getProcedure()));
					offerings.retainAll(new HashSet<String>(Configurator.getInstance()
							.getCapabilitiesCacheController()
							.getOfferings4Procedure(obsConst.getProcedure())));
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
							ResultTemplate t = HibernateCriteriaQueryUtilities.getResultTemplateObject(
									offering, obsConst.getObservableProperty().getIdentifier(), session);
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
                if (hObservation.getAntiSubsetting() != null) {
                    if (antiSubsettingObservations.containsKey(hObservation.getAntiSubsetting())) {
                        SosObservation sosObservation =
                                antiSubsettingObservations.get(hObservation.getAntiSubsetting());
                        ((SweDataArrayValue) ((SosMultiObservationValues) sosObservation.getValue()).getValue())
                        .addValue(phenomenonTime, phenID, value);
                    } else {
                        SosObservation sosObservation = new SosObservation();
                        sosObservation.setObservationID(Long.toString(hObservation.getObservationId()));
                        sosObservation.setIdentifier(hObservation.getIdentifier());
                        sosObservation.setNoDataValue(Configurator.getInstance().getNoDataValue());
                        sosObservation.setTokenSeparator(Configurator.getInstance().getTokenSeperator());
                        sosObservation.setTupleSeparator(Configurator.getInstance().getTupleSeperator());
                        sosObservation.setObservationConstellation(observationConstellations.get(obsConstHash));
                        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
                        dataArrayValue.addValue(phenomenonTime, phenID, value);
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
						o = new SosObservation();
						o.setObservationID(Long.toString(hObservation.getObservationId()));
                        o.setIdentifier(hObservation.getIdentifier());
                        o.setNoDataValue(Configurator.getInstance().getNoDataValue());
                        o.setTokenSeparator(sweTextEncoding.getTokenSeparator());
                        o.setTupleSeparator(sweTextEncoding.getBlockSeparator());
                        o.setObservationConstellation(observationConstellations.get(obsConstHash));
						
						SosSweAbstractDataComponent comp = new SosResultStructure
								(resultTemplate.getResultStructure()).getResultStructure();
						if (comp instanceof SosSweDataArray) {
							o.setResultStructure(((SosSweDataArray) comp).getElementType());
						} else if (comp instanceof SosSweDataRecord) {
							o.setResultStructure((SosSweDataRecord) comp);						
						}
						dataArrayValue = new SweDataArrayValue();
                        SosMultiObservationValues observationValue = new SosMultiObservationValues();
                        observationValue.setValue(dataArrayValue);
                        o.setValue(observationValue);
						templatedObservations.put(obsConstHash, o);
					} else {
						dataArrayValue = (SweDataArrayValue) ((SosMultiObservationValues) o.getValue()).getValue();
					}
					
					dataArrayValue.addValue(phenomenonTime, phenID, value);
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
                if (antiSubsettingObservations.values() != null && !antiSubsettingObservations.values().isEmpty()) {
                    observationCollection.addAll(antiSubsettingObservations.values());
                }
            }
			observationCollection.addAll(templatedObservations.values());
        }
        return observationCollection;
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

}
