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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.MeasureType;
import net.opengis.om.x10.MeasurementDocument;
import net.opengis.om.x10.MeasurementType;
import net.opengis.om.x10.ObservationType;
import net.opengis.swe.x101.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosCompositePhenomenon;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.profile.Profile;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.SweHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmEncoderv100 implements ObservationEncoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OmEncoderv100.class);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.ObservationType, CollectionHelper.set(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION,
                    OMConstants.OBS_TYPE_COUNT_OBSERVATION,
                    // OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION,
                    OMConstants.OBS_TYPE_MEASUREMENT, OMConstants.OBS_TYPE_TEXT_OBSERVATION,
                    OMConstants.OBS_TYPE_TRUTH_OBSERVATION, OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION));

    // TODO: change to correct conformance class
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            "http://www.opengis.net/spec/OMXML/1.0/conf/measurement",
            "http://www.opengis.net/spec/OMXML/1.0/conf/categoryObservation",
            "http://www.opengis.net/spec/OMXML/1.0/conf/countObservation",
            "http://www.opengis.net/spec/OMXML/1.0/conf/truthObservation",
            "http://www.opengis.net/spec/OMXML/1.0/conf/geometryObservation",
            "http://www.opengis.net/spec/OMXML/1.0/conf/textObservation");

    private static final Map<String, Map<String, Set<String>>> SUPPORTED_RESPONSE_FORMATS = Collections.singletonMap(
            SosConstants.SOS,
            Collections.singletonMap(Sos1Constants.SERVICEVERSION,
                    CollectionHelper.set(OMConstants.CONTENT_TYPE_OM)));
    
    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
            CodingHelper.encoderKeysForElements(OMConstants.NS_OM, SosObservation.class),
            CodingHelper.encoderKeysForElements(OMConstants.CONTENT_TYPE_OM, SosObservation.class));

    @Deprecated
    private boolean supported = true;

    public OmEncoderv100() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.unmodifiableMap(SUPPORTED_TYPES);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(OMConstants.NS_OM, OMConstants.NS_OM_PREFIX);
    }

    @Override
    public boolean isObservationAndMeasurmentV20Type() {
        return false;
    }

    @Override
    public Set<String> getSupportedResponseFormats(String service, String version) {
        if (supported && SUPPORTED_RESPONSE_FORMATS.get(service) != null) {
            if (SUPPORTED_RESPONSE_FORMATS.get(service).get(version) != null) {
                return SUPPORTED_RESPONSE_FORMATS.get(service).get(version);
            }
        }
        return Collections.emptySet();
    }

    @Override
    @Deprecated
    public boolean isSupported() {
        return supported;
    }

    @Override
    @Deprecated
    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    @Override
    public boolean shouldObservationsWithSameXBeMerged() {
        return true;
    }

    @Override
    public String getContentType() {
        return OMConstants.CONTENT_TYPE_OM;
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, new EnumMap<HelperValues, String>(HelperValues.class));
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
    	if (element instanceof SosObservation) {
        	SosObservation sosObs = (SosObservation) element;
        	if (sosObs.getResultType() != null && sosObs.getResultType().equalsIgnoreCase(OMConstants.EN_MEASUREMENT)){
        		return createMeasurement(sosObs, additionalValues);
        	} else {
        		return createObservation(sosObs, additionalValues);
        	}
        }
        throw new UnsupportedEncoderInputException(this, element);
    }

    private XmlObject createMeasurement(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
    	
//      MeasurementType xbObs = MeasurementType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        MeasurementDocument xbObsDoc = MeasurementDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        MeasurementType xbObs = xbObsDoc.addNewMeasurement();
        
        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID;
        if (sosObservation.getObservationID() != null) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObs.getId().replace("o_", "");
        }
        if (sosObservation.getIdentifier() != null && sosObservation.getIdentifier().isSetValue()) {
        	// I reckon that might cause problems in SOS / OM 1.0 I get XmlValueDisconnectedExceptions 
        	// either here or with the addSamplingTime for TimePeriod
            // xbObs.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, sosObservation.getIdentifier()));
        }

        String observationType = sosObservation.getObservationConstellation().getObservationType();
        // FIXME, was addNewName - not here actually necessary 
        // OGC-OM/2.0/OM_SWEArrayObservation won't be used for the om:Observation OM1
        // xbObs.addNewParameter().setHref(observationType);
        /* SosMultiObservationValues will generate always a new ITime... */
        ITime samplingTime = sosObservation.getPhenomenonTime();
        // set phenomenonTime
        if (samplingTime.getGmlId() == null) {
            samplingTime.setGmlId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }
        
    	addSamplingTime(xbObs.addNewSamplingTime(), samplingTime);
        
        // set resultTime not in OM1, ?
        // xbObs.addNewResultTime().setHref("#" + phenomenonTime.getId());
        // set procedure
        xbObs.addNewProcedure().setHref(
                sosObservation.getObservationConstellation().getProcedure().getIdentifier());
        // set observedProperty (phenomenon)
        List<SosObservableProperty> phenComponents = null;
        if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosObservableProperty) {
            xbObs.addNewObservedProperty().setHref(
                    sosObservation.getObservationConstellation().getObservableProperty().getIdentifier());
            phenComponents = new ArrayList<SosObservableProperty>(1);
            phenComponents.add((SosObservableProperty) sosObservation.getObservationConstellation()
                    .getObservableProperty());
        } else if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosCompositePhenomenon) {
            SosCompositePhenomenon compPhen =
                    (SosCompositePhenomenon) sosObservation.getObservationConstellation().getObservableProperty();
            xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
            phenComponents = compPhen.getPhenomenonComponents();
        }
        // set feature
        encodeFeatureOfInterest(xbObs, sosObservation.getObservationConstellation().getFeatureOfInterest());

        // set result
        addResultToObservation(xbObs.addNewResult(), sosObservation, phenComponents, observationID);
        // TODO use devMode to switch on
        // XmlHelper.validateDocument(xbObs);
        return xbObsDoc;

    }
    
    private XmlObject createObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {

        ObservationType xbObs = ObservationType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID;
        if (sosObservation.getObservationID() != null) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObs.getId().replace("o_", "");
        }
        if (sosObservation.getIdentifier() != null && sosObservation.getIdentifier().isSetValue()) {
            // I reckon that might cause problems in SOS / OM 1.0 I get
            // XmlValueDisconnectedExceptions
            // either here or with the addSamplingTime for TimePeriod
            // xbObs.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML,
            // sosObservation.getIdentifier()));
        }

        String observationType = sosObservation.getObservationConstellation().getObservationType();
        // FIXME, was addNewName - not here actually necessary
        // OGC-OM/2.0/OM_SWEArrayObservation won't be used for the
        // om:Observation OM1
        // xbObs.addNewParameter().setHref(observationType);
        /* SosMultiObservationValues will generate always a new ITime... */
        ITime samplingTime = sosObservation.getPhenomenonTime();
        // set phenomenonTime
        if (samplingTime.getGmlId() == null) {
            samplingTime.setGmlId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }

        addSamplingTime(xbObs.addNewSamplingTime(), samplingTime);

        // set resultTime not in OM1, ?
        // xbObs.addNewResultTime().setHref("#" + phenomenonTime.getId());
        // set procedure
        xbObs.addNewProcedure().setHref(
                sosObservation.getObservationConstellation().getProcedure().getIdentifier());
        // set observedProperty (phenomenon)
        List<SosObservableProperty> phenComponents = null;
        if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosObservableProperty) {
            xbObs.addNewObservedProperty().setHref(
                    sosObservation.getObservationConstellation().getObservableProperty().getIdentifier());
            phenComponents = new ArrayList<SosObservableProperty>(1);
            phenComponents.add((SosObservableProperty) sosObservation.getObservationConstellation()
                    .getObservableProperty());
        } else if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosCompositePhenomenon) {
            SosCompositePhenomenon compPhen =
                    (SosCompositePhenomenon) sosObservation.getObservationConstellation().getObservableProperty();
            xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
            phenComponents = compPhen.getPhenomenonComponents();
        }
        // set feature
        encodeFeatureOfInterest(xbObs, sosObservation.getObservationConstellation().getFeatureOfInterest());

        // set result
        addResultToObservation(xbObs.addNewResult(), sosObservation, phenComponents, observationID);
        // TODO use devMode to switch on
        // XmlHelper.validateDocument(xbObs);
        return xbObs;

    }

    private void addSamplingTime(TimeObjectPropertyType timeObjectPropertyType, ITime iTime) throws OwsExceptionReport {

        XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, iTime);

        // FIXME should go into GmlHelper
        javax.xml.namespace.QName timeObj = null;
        if (iTime instanceof TimeInstant) {
            timeObj = GMLConstants.QN_TIME_INSTANT;
        } else if (iTime instanceof TimePeriod) {
            timeObj = GMLConstants.QN_TIME_PERIOD;
        }
        XmlObject substitution = timeObjectPropertyType.addNewTimeObject().substitute(timeObj, xmlObject.schemaType());

        substitution.set(xmlObject);
    }

    private XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
    }

    private void addResultToObservation(XmlObject xbResult, SosObservation sosObservation,
                                        List<SosObservableProperty> phenComponents, String observationID) throws
            OwsExceptionReport {
        // TODO if OM_SWEArrayObservation and get ResultEncoding and
        // ResultStructure exists,
        String observationType = sosObservation.getObservationConstellation().getObservationType();
        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            addSingleObservationToResult(xbResult, sosObservation, observationID);
        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
            addMultiObservationValueToResult(xbResult, sosObservation);
        }
    }

    // FIXME String.equals(QName) !?
    private void addSingleObservationToResult(XmlObject xbResult, SosObservation sosObservation, String observationID)
            throws OwsExceptionReport {
        String observationType = sosObservation.getObservationConstellation().getObservationType();
        SosSingleObservationValue<?> observationValue = (SosSingleObservationValue) sosObservation.getValue();
        if ((observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)) && observationValue.getValue() instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) observationValue.getValue();
            MeasureType xbMeasureType =
                    MeasureType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (quantityValue.getUnit() != null) {
                xbMeasureType.setUom(quantityValue.getUnit());
            } else {
                xbMeasureType.setUom("");
            }
            if (!quantityValue.getValue().equals(Double.NaN)) {
                xbMeasureType.setDoubleValue(quantityValue.getValue().doubleValue());
            } else {
                xbMeasureType.setNil();
            }
            xbResult.set(xbMeasureType);
        } else if ((observationType.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_COUNT_OBSERVATION))
                && observationValue.getValue() instanceof CountValue) {
            CountValue countValue = (CountValue) observationValue.getValue();
            XmlInteger xbInteger = XmlInteger.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (countValue.getValue() != null && countValue.getValue() != Integer.MIN_VALUE) {
                xbInteger.setBigIntegerValue(new BigInteger(countValue.getValue().toString()));
            } else {
                xbInteger.setNil();
            }
            xbResult.set(xbInteger);
        } else if ((observationType.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_TEXT_OBSERVATION))
                && observationValue.getValue() instanceof TextValue) {
            TextValue textValue = (TextValue) observationValue.getValue();
            XmlString xbString = XmlString.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (textValue.getValue() != null && !textValue.getValue().isEmpty()) {
                xbString.setStringValue(textValue.getValue());
            } else {
                xbString.setNil();
            }
            xbResult.set(xbString);
        } else if ((observationType.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_TRUTH_OBSERVATION))
                && observationValue.getValue() instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) observationValue.getValue();
            XmlBoolean xbBoolean = XmlBoolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (booleanValue.getValue() != null) {
                xbBoolean.setBooleanValue(booleanValue.getValue());
            } else {
                xbBoolean.setNil();
            }
            xbResult.set(xbBoolean);
        } else if ((observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION))
                && observationValue.getValue() instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) observationValue.getValue();
            if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
                Map<HelperValues, String> additionalValue = new EnumMap<HelperValues, String>(HelperValues.class);
                additionalValue.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + observationID);
                xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, categoryValue, additionalValue));
            } else {
                xbResult.setNil();
            }
        } else if ((observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION))
                && observationValue.getValue() instanceof GeometryValue) {

            GeometryValue geometryValue = (GeometryValue) observationValue.getValue();
            if (geometryValue.getValue() != null) {
                Map<HelperValues, String> additionalValue = new EnumMap<HelperValues, String>(HelperValues.class);
                additionalValue.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + observationID);
                xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, geometryValue.getValue(),
                        additionalValue));
            } else {
                xbResult.setNil();
            }
        } else if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)
                || observationType.equals(OMConstants.RESULT_MODEL_OBSERVATION)) {
            SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
            Map<HelperValues, String> additionalValues =
                    new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
            additionalValues.put(HelperValues.FOR_OBSERVATION, null);
            xbResult.set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_101, dataArray, additionalValues));
        }
    }

    private void addMultiObservationValueToResult(XmlObject xbResult, SosObservation sosObservation)
            throws OwsExceptionReport {
        SosMultiObservationValues<?> observationValue = (SosMultiObservationValues) sosObservation.getValue();
        Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
        additionalValues.put(HelperValues.FOR_OBSERVATION, null);
        SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
        xbResult.set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_101, dataArray, additionalValues));
    }

    /**
     * Encodes a SosAbstractFeature to an SpatialSamplingFeature under
     * consideration of duplicated SpatialSamplingFeature in the XML document.
     * 
     * @param observation
     *            XmlObject O&M observation
     * @param absObs
     *            SOS observation

     *
     * @throws OwsExceptionReport
     */
    private void encodeFeatureOfInterest(ObservationType observation, SosAbstractFeature feature)
            throws OwsExceptionReport {
        // String urlPattern =
        // SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
        // SosConstants.Operations.GetFeatureOfInterest.name(), new
        // DecoderKeyType(SosConstants.SOS,
        // Sos2Constants.SERVICEVERSION));
        SosSamplingFeature samplingFeature = (SosSamplingFeature) feature;
        FeaturePropertyType featureProperty = observation.addNewFeatureOfInterest();
        Profile activeProfile = Configurator.getInstance().getProfileHandler().getActiveProfile();
        if (!activeProfile.isEncodeFeatureOfInterestInObservations() || !(feature instanceof SosSamplingFeature)) {
            // if (urlPattern != null) {
            // featureProperty.setHref(SosHelper.createFoiGetUrl(feature.getIdentifier(),
            // Sos2Constants.SERVICEVERSION,
            // Configurator.getInstance().getServiceURL(), urlPattern));
            // } else {
            featureProperty.setHref(feature.getIdentifier().getValue());
            // }
            if (samplingFeature.isSetNames()) {
                featureProperty.setTitle(samplingFeature.getFirstName().getValue());
            }
        } else {
            if (samplingFeature.getUrl() != null) {
                featureProperty.setHref(samplingFeature.getUrl());
            } else {
                try {
                    // TODO agree on fetureType handling for SOS 1.0 ->
                    // OMHelper?!
                    String featureType = null;
                    String featureNamespace = null;
                    if (samplingFeature.getFeatureType().equalsIgnoreCase(SFConstants.FT_SAMPLINGPOINT)) {
                        featureType = SFConstants.FT_SAMPLINGPOINT;
                        featureNamespace = SFConstants.NS_SA;
                    } else if (samplingFeature.getFeatureType().equalsIgnoreCase(SFConstants.FT_SAMPLINGSURFACE)) {
                        featureType = SFConstants.FT_SAMPLINGSURFACE;
                        featureNamespace = SFConstants.NS_SA;
                    } else if (samplingFeature.getFeatureType().equalsIgnoreCase(SFConstants.FT_SAMPLINGCURVE)) {
                        featureType = SFConstants.FT_SAMPLINGCURVE;
                        featureNamespace = SFConstants.NS_SA;
                    } else if (samplingFeature.getFeatureType().equalsIgnoreCase(
                            SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT)) {
                        featureType = SFConstants.FT_SAMPLINGPOINT;
                        featureNamespace = SFConstants.NS_SA;
                    } else if (samplingFeature.getFeatureType().equalsIgnoreCase(
                            SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE)) {
                        featureType = SFConstants.FT_SAMPLINGSURFACE;
                        featureNamespace = SFConstants.NS_SA;
                    } else if (samplingFeature.getFeatureType().equalsIgnoreCase(
                            SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE)) {
                        featureType = SFConstants.FT_SAMPLINGCURVE;
                        featureNamespace = SFConstants.NS_SA;
                    } else {
                        throw new InvalidParameterValueException().at("sa:SamplingFeature")
                                .withMessage("Error while encoding featureOfInterest in om:Observation!");
                    }

                    featureProperty.set(CodingHelper.encodeObjectToXml(featureNamespace, samplingFeature));
                } catch (OwsExceptionReport e) {
                    if (samplingFeature.getXmlDescription() != null) {
                        try {
                            // TODO how set gml:id in already existing
                            // XmlDescription?
                            featureProperty.set(XmlObject.Factory.parse(samplingFeature.getXmlDescription()));
                        } catch (XmlException xmle) {
                            throw new NoApplicableCodeException().causedBy(xmle)
                                    .withMessage("Error while encoding featureOfInterest in om:Observation!");
                        }
                    } else {
                        featureProperty.setHref(samplingFeature.getIdentifier().getValue());
                        if (samplingFeature.isSetNames()) {
                            featureProperty.setTitle(samplingFeature.getFirstName().getValue());
                        }
                    }
                }
            }
        }
    }
}
