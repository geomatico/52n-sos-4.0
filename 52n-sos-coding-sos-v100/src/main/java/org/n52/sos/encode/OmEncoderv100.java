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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.MeasureType;
import net.opengis.om.x10.ObservationType;
import net.opengis.swe.x101.TimeObjectPropertyType;
import org.apache.xerces.util.XML11Char;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosCompositePhenomenon;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.GmlHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.SweHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmEncoderv100 implements IObservationEncoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OmEncoderv100.class);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
        SupportedTypeKey.ObservationType, CollectionHelper.set(
            OMConstants.OBS_TYPE_CATEGORY_OBSERVATION,
            OMConstants.OBS_TYPE_COUNT_OBSERVATION,
            // OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION,
            OMConstants.OBS_TYPE_MEASUREMENT,
            OMConstants.OBS_TYPE_TEXT_OBSERVATION,
            OMConstants.OBS_TYPE_TRUTH_OBSERVATION,
            OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION
        )
    );
    
    // TODO: change to correct conformance class
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
        "http://www.opengis.net/spec/OMXML/1.0/conf/measurement",
        "http://www.opengis.net/spec/OMXML/1.0/conf/categoryObservation",
        "http://www.opengis.net/spec/OMXML/1.0/conf/countObservation",
        "http://www.opengis.net/spec/OMXML/1.0/conf/truthObservation",
        "http://www.opengis.net/spec/OMXML/1.0/conf/geometryObservation",
        "http://www.opengis.net/spec/OMXML/1.0/conf/textObservation"
    );
    
    private static final Map<String, Map<String, Set<String>>> supportedResponseFormats
        = Collections.singletonMap(SosConstants.SOS, 
            Collections.singletonMap(Sos1Constants.SERVICEVERSION, 
                CollectionHelper.set(OMConstants.NS_OM,OMConstants.CONTENT_TYPE_OM)));
    
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
        CodingHelper.encoderKeysForElements(OMConstants.NS_OM, SosObservation.class),
        CodingHelper.encoderKeysForElements(OMConstants.CONTENT_TYPE_OM, SosObservation.class)
    );
    
    private boolean supported = true;

    public OmEncoderv100() {
        LOGGER.info("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEYS));
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
        return true;
    }

    @Override
    public Set<String> getSupportedResponseFormats(String service, String version) {
        if (supported && supportedResponseFormats.get(service) != null) {
            if (supportedResponseFormats.get(service).get(version) != null) {
                return supportedResponseFormats.get(service).get(version);
            }
        }
        return new HashSet<String>(0);
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    @Override
    public boolean shouldObservationsWithSameXBeMerged() {
        return false;
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
            return createObservation((SosObservation) element, additionalValues);
        }
        return null;
    }

    private XmlObject createObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        ObservationType xbObs =
                ObservationType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID;
        if (sosObservation.getObservationID() != null) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObs.getId().replace("o_", "");
        }
        if (sosObservation.getIdentifier() != null && sosObservation.getIdentifier().isSetValue()) {
            xbObs.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, sosObservation.getIdentifier()));
        }

        String observationType = sosObservation.getObservationConstellation().getObservationType();
        // FIXME, was addNewName
        xbObs.addNewParameter().setHref(observationType);
        /* SosMultiObservationValues will generate always a new ITime... */
        ITime phenomenonTime = sosObservation.getPhenomenonTime();
        // set phenomenonTime
        if (phenomenonTime.getId() == null) {
            phenomenonTime.setId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }
        addPhenomenonTime(xbObs.addNewSamplingTime(), phenomenonTime);
        // set resultTime
        xbObs.addNewResultTime().setHref("#" + phenomenonTime.getId());
        // set procedure
        xbObs.addNewProcedure().setHref(
                sosObservation.getObservationConstellation().getProcedure().getProcedureIdentifier());
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

        // ----------------------------------------------
        // // reset spatialFeatureID counter and spatialFeatureIdentifier/gmlID
        // map
        // GetObservationResponseType getObservationResponse =
        // GetObservationResponseType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // if (sosObsCol.getObservationMembers() != null &&
        // !sosObsCol.getObservationMembers().isEmpty()) {
        // Collection<SosObservation> mergedObsCol =
        // SosHelper.mergeObservationsForGenericObservation(sosObsCol.getObservationMembers());
        // Iterator<SosObservation> obsIter = mergedObsCol.iterator();
        //
        // while (obsIter.hasNext()) {
        // SosObservation sosObs = obsIter.next();
        // String observationType =
        // sosObs.getObservationConstellation().getObservationType();
        // if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
        // || observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
        // || observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
        // || observationType.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION)
        // || observationType.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION)
        // || observationType.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
        // for (String phenID : sosObs.getValues().keySet()) {
        // List<SosObservationValue> sosObsValues =
        // sosObs.getValues().get(phenID);
        // for (SosObservationValue sosObsValue : sosObsValues) {
        // OMObservationType xbObs =
        // getObservationResponse.addNewObservationData().addNewOMObservation();
        // createObservation(xbObs, sosObs, sosObsCol.getFeatures(), phenID,
        // sosObsValue,
        // observationType);
        // }
        // }
        // } else if
        // (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION))
        // {
        // OMObservationType xbObs =
        // getObservationResponse.addNewObservationData().addNewOMObservation();
        // createGenericObservation(xbObs, sosObs, sosObsCol.getFeatures());
        // }
        // }
        // }

    }

    // /**
    // * Creates XML representation of OM 2.0 observation type from SOS
    // * observation.
    // *
    // * @param xbObs
    // * OM 2.0 observation
    // * @param phenID
    // * @param object
    // * @param observationType
    // * @param map
    // * @param absObs
    // * SOS observation
    // * @throws OwsExceptionReport
    // * if an error occurs during creation.
    // */
    // private void createObservation(OMObservationType xbObs, SosObservation
    // sosObs,
    // Map<String, SosAbstractFeature> features, String phenID,
    // SosObservationValue sosObsValue,
    // String observationType) throws OwsExceptionReport {
    //
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = "";
    // if (sosObsValue.getObservationID() != null) {
    // observationID = sosObsValue.getObservationID();
    // xbObs.addNewIdentifier().setStringValue(observationID);
    // } else {
    // observationID = Long.toString(System.currentTimeMillis());
    // }
    // xbObs.addNewType().setHref(observationType);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    // // AbstractTimeObjectType xbAbsTimeObject =
    // // xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
    // // IEncoder encoder =
    // //
    // Configurator.getInstance().getEncoder(xbAbsTimeObject.getDomNode().getNamespaceURI());
    // // if (encoder != null) {
    // // Map<HelperValues, String> additionalValues = new
    // // HashMap<HelperValues, String>();
    // // additionalValues.put(HelperValues.GMLID, phenTimeId);
    // // XmlObject xmlObject = (XmlObject)
    // // encoder.encode(sosObsValue.getPhenomenonTime(), additionalValues);
    // // xbAbsTimeObject.set(xmlObject);
    // // if (sosObsValue.getPhenomenonTime() instanceof TimeInstant) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_INSTANT,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // } else if (sosObsValue.getPhenomenonTime() instanceof TimePeriod) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_PERIOD,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // }
    // // } else {
    // // String exceptionText =
    // // "Error while encoding phenomenon time, needed encoder is missing!";
    // // throw Util4Exceptions.createNoApplicableCodeException(null,
    // // exceptionText);
    // // }
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // set observedProperty (phenomenon)
    // xbObs.addNewObservedProperty().setHref(phenID);
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // // Currently not used for SOS 2.0 and OM 2.0 encoding.
    // // // // add quality, if set
    // // if (meas.getQuality() != null) {
    // // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
    // // xbQuality.set(createQualityProperty(sosObs.getQuality()));
    // // }
    //
    // // set result
    // if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
    // || observationType.equals(OMConstants.RESULT_MODEL_MEASUREMENT)) {
    // MeasureType xbMeasureType = MeasureType.Factory.newInstance();
    // if (((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty()).getUnit()
    // != null) {
    // xbMeasureType.setUom(((SosObservableProperty)
    // sosObs.getObservationConstellation()
    // .getObservableProperty()).getUnit());
    // } else {
    // xbMeasureType.setUom("");
    // }
    //
    // if (!((Double) sosObsValue.getValue()).equals(Double.NaN)) {
    // xbMeasureType.setDoubleValue((Double) sosObsValue.getValue());
    // }
    //
    // else {
    // xbMeasureType.setNil();
    // }
    // xbObs.addNewResult().set(xbMeasureType);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION))
    // {
    // Reference xbRef = Reference.Factory.newInstance();
    // xbRef.setHref((String) sosObsValue.getValue());
    // xbObs.addNewResult().set(xbRef);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION))
    // {
    // IEncoder geomEncoder =
    // Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
    // if (geomEncoder != null) {
    // Map<HelperValues, String> additionalValues = new HashMap<HelperValues,
    // String>();
    // additionalValues.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX +
    // observationID);
    // XmlObject xmlObject =
    // (XmlObject) geomEncoder.encode((Geometry) sosObsValue.getValue(),
    // additionalValues);
    // xbObs.addNewResult().set(xmlObject);
    // } else {
    // String exceptionText =
    // "Error while encoding geometry value, needed encoder is missing!";
    // throw Util4Exceptions.createNoApplicableCodeException(null,
    // exceptionText);
    // }
    // }
    //
    // }
    //
    // private void createGenericObservation(OMObservationType xbObs,
    // SosObservation sosObs,
    // Map<String, SosAbstractFeature> features) throws OwsExceptionReport {
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = SosConstants.OBS_ID_PREFIX + new
    // DateTime().getMillis();
    // xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    //
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // phenomena of the common observation
    // List<SosObservableProperty> phenComponents = null;
    // if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosObservableProperty) {
    // xbObs.addNewObservedProperty().setHref(
    // sosObs.getObservationConstellation().getObservableProperty().getIdentifier());
    // phenComponents = new ArrayList<SosObservableProperty>();
    // phenComponents.add((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty());
    // } else if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosCompositePhenomenon) {
    // SosCompositePhenomenon compPhen =
    // (SosCompositePhenomenon)
    // sosObs.getObservationConstellation().getObservableProperty();
    // xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
    // phenComponents = compPhen.getPhenomenonComponents();
    // }
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // set result
    // // add resultDefinition
    // XmlObject xbRresult = xbObs.addNewResult();
    // DataArrayDocument xb_dataArrayDoc = createDataArrayResult(phenComponents,
    // sosObs);
    // xbRresult.set(xb_dataArrayDoc);
    // XmlCursor cursor = xbRresult.newCursor();
    // cursor.setAttributeText(new QName(W3CConstants.NS_XSI, "type"),
    // "swe:DataArrayPropertyType");
    // cursor.dispose();
    // }

    private void addPhenomenonTime(TimeObjectPropertyType timeObjectPropertyType, ITime iTime)
            throws OwsExceptionReport {
        XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, iTime);
        XmlObject substitution =
                timeObjectPropertyType.addNewTimeObject().substitute(
                GmlHelper.getQnameForITime(iTime), xmlObject.schemaType());
        substitution.set(xmlObject);
    }

    private XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
    }

    // /**
    // * Creates XML representation of OM 2.0 observation type from SOS
    // * observation.
    // *
    // * @param xbObs
    // * OM 2.0 observation
    // * @param phenID
    // * @param object
    // * @param observationType
    // * @param map
    // * @param absObs
    // * SOS observation
    // * @throws OwsExceptionReport
    // * if an error occurs during creation.
    // */
    // private void createObservation(OMObservationType xbObs, SosObservation
    // sosObs,
    // Map<String, SosAbstractFeature> features, String phenID,
    // SosObservationValue sosObsValue,
    // String observationType) throws OwsExceptionReport {
    //
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = "";
    // if (sosObsValue.getObservationID() != null) {
    // observationID = sosObsValue.getObservationID();
    // xbObs.addNewIdentifier().setStringValue(observationID);
    // } else {
    // observationID = Long.toString(System.currentTimeMillis());
    // }
    // xbObs.addNewType().setHref(observationType);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    // // AbstractTimeObjectType xbAbsTimeObject =
    // // xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
    // // IEncoder encoder =
    // //
    // Configurator.getInstance().getEncoder(xbAbsTimeObject.getDomNode().getNamespaceURI());
    // // if (encoder != null) {
    // // Map<HelperValues, String> additionalValues = new
    // // HashMap<HelperValues, String>();
    // // additionalValues.put(HelperValues.GMLID, phenTimeId);
    // // XmlObject xmlObject = (XmlObject)
    // // encoder.encode(sosObsValue.getPhenomenonTime(), additionalValues);
    // // xbAbsTimeObject.set(xmlObject);
    // // if (sosObsValue.getPhenomenonTime() instanceof TimeInstant) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_INSTANT,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // } else if (sosObsValue.getPhenomenonTime() instanceof TimePeriod) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_PERIOD,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // }
    // // } else {
    // // String exceptionText =
    // // "Error while encoding phenomenon time, needed encoder is missing!";
    // // throw Util4Exceptions.createNoApplicableCodeException(null,
    // // exceptionText);
    // // }
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // set observedProperty (phenomenon)
    // xbObs.addNewObservedProperty().setHref(phenID);
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // // Currently not used for SOS 2.0 and OM 2.0 encoding.
    // // // // add quality, if set
    // // if (meas.getQuality() != null) {
    // // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
    // // xbQuality.set(createQualityProperty(sosObs.getQuality()));
    // // }
    //
    // // set result
    // if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
    // || observationType.equals(OMConstants.RESULT_MODEL_MEASUREMENT)) {
    // MeasureType xbMeasureType = MeasureType.Factory.newInstance();
    // if (((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty()).getUnit()
    // != null) {
    // xbMeasureType.setUom(((SosObservableProperty)
    // sosObs.getObservationConstellation()
    // .getObservableProperty()).getUnit());
    // } else {
    // xbMeasureType.setUom("");
    // }
    //
    // if (!((Double) sosObsValue.getValue()).equals(Double.NaN)) {
    // xbMeasureType.setDoubleValue((Double) sosObsValue.getValue());
    // }
    //
    // else {
    // xbMeasureType.setNil();
    // }
    // xbObs.addNewResult().set(xbMeasureType);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION))
    // {
    // Reference xbRef = Reference.Factory.newInstance();
    // xbRef.setHref((String) sosObsValue.getValue());
    // xbObs.addNewResult().set(xbRef);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION))
    // {
    // IEncoder geomEncoder =
    // Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
    // if (geomEncoder != null) {
    // Map<HelperValues, String> additionalValues = new HashMap<HelperValues,
    // String>();
    // additionalValues.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX +
    // observationID);
    // XmlObject xmlObject =
    // (XmlObject) geomEncoder.encode((Geometry) sosObsValue.getValue(),
    // additionalValues);
    // xbObs.addNewResult().set(xmlObject);
    // } else {
    // String exceptionText =
    // "Error while encoding geometry value, needed encoder is missing!";
    // throw Util4Exceptions.createNoApplicableCodeException(null,
    // exceptionText);
    // }
    // }
    //
    // }
    //
    // private void createGenericObservation(OMObservationType xbObs,
    // SosObservation sosObs,
    // Map<String, SosAbstractFeature> features) throws OwsExceptionReport {
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = SosConstants.OBS_ID_PREFIX + new
    // DateTime().getMillis();
    // xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    //
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // phenomena of the common observation
    // List<SosObservableProperty> phenComponents = null;
    // if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosObservableProperty) {
    // xbObs.addNewObservedProperty().setHref(
    // sosObs.getObservationConstellation().getObservableProperty().getIdentifier());
    // phenComponents = new ArrayList<SosObservableProperty>();
    // phenComponents.add((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty());
    // } else if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosCompositePhenomenon) {
    // SosCompositePhenomenon compPhen =
    // (SosCompositePhenomenon)
    // sosObs.getObservationConstellation().getObservableProperty();
    // xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
    // phenComponents = compPhen.getPhenomenonComponents();
    // }
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // set result
    // // add resultDefinition
    // XmlObject xbRresult = xbObs.addNewResult();
    // DataArrayDocument xb_dataArrayDoc = createDataArrayResult(phenComponents,
    // sosObs);
    // xbRresult.set(xb_dataArrayDoc);
    // XmlCursor cursor = xbRresult.newCursor();
    // cursor.setAttributeText(new QName(W3CConstants.NS_XSI, "type"),
    // "swe:DataArrayPropertyType");
    // cursor.dispose();
    // }

    // /**
    // * Creates XML representation of OM 2.0 observation type from SOS
    // * observation.
    // *
    // * @param xbObs
    // * OM 2.0 observation
    // * @param phenID
    // * @param object
    // * @param observationType
    // * @param map
    // * @param absObs
    // * SOS observation
    // * @throws OwsExceptionReport
    // * if an error occurs during creation.
    // */
    // private void createObservation(OMObservationType xbObs, SosObservation
    // sosObs,
    // Map<String, SosAbstractFeature> features, String phenID,
    // SosObservationValue sosObsValue,
    // String observationType) throws OwsExceptionReport {
    //
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = "";
    // if (sosObsValue.getObservationID() != null) {
    // observationID = sosObsValue.getObservationID();
    // xbObs.addNewIdentifier().setStringValue(observationID);
    // } else {
    // observationID = Long.toString(System.currentTimeMillis());
    // }
    // xbObs.addNewType().setHref(observationType);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    // // AbstractTimeObjectType xbAbsTimeObject =
    // // xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
    // // IEncoder encoder =
    // //
    // Configurator.getInstance().getEncoder(xbAbsTimeObject.getDomNode().getNamespaceURI());
    // // if (encoder != null) {
    // // Map<HelperValues, String> additionalValues = new
    // // HashMap<HelperValues, String>();
    // // additionalValues.put(HelperValues.GMLID, phenTimeId);
    // // XmlObject xmlObject = (XmlObject)
    // // encoder.encode(sosObsValue.getPhenomenonTime(), additionalValues);
    // // xbAbsTimeObject.set(xmlObject);
    // // if (sosObsValue.getPhenomenonTime() instanceof TimeInstant) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_INSTANT,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // } else if (sosObsValue.getPhenomenonTime() instanceof TimePeriod) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_PERIOD,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // }
    // // } else {
    // // String exceptionText =
    // // "Error while encoding phenomenon time, needed encoder is missing!";
    // // throw Util4Exceptions.createNoApplicableCodeException(null,
    // // exceptionText);
    // // }
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // set observedProperty (phenomenon)
    // xbObs.addNewObservedProperty().setHref(phenID);
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // // Currently not used for SOS 2.0 and OM 2.0 encoding.
    // // // // add quality, if set
    // // if (meas.getQuality() != null) {
    // // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
    // // xbQuality.set(createQualityProperty(sosObs.getQuality()));
    // // }
    //
    // // set result
    // if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
    // || observationType.equals(OMConstants.RESULT_MODEL_MEASUREMENT)) {
    // MeasureType xbMeasureType = MeasureType.Factory.newInstance();
    // if (((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty()).getUnit()
    // != null) {
    // xbMeasureType.setUom(((SosObservableProperty)
    // sosObs.getObservationConstellation()
    // .getObservableProperty()).getUnit());
    // } else {
    // xbMeasureType.setUom("");
    // }
    //
    // if (!((Double) sosObsValue.getValue()).equals(Double.NaN)) {
    // xbMeasureType.setDoubleValue((Double) sosObsValue.getValue());
    // }
    //
    // else {
    // xbMeasureType.setNil();
    // }
    // xbObs.addNewResult().set(xbMeasureType);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION))
    // {
    // Reference xbRef = Reference.Factory.newInstance();
    // xbRef.setHref((String) sosObsValue.getValue());
    // xbObs.addNewResult().set(xbRef);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION))
    // {
    // IEncoder geomEncoder =
    // Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
    // if (geomEncoder != null) {
    // Map<HelperValues, String> additionalValues = new HashMap<HelperValues,
    // String>();
    // additionalValues.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX +
    // observationID);
    // XmlObject xmlObject =
    // (XmlObject) geomEncoder.encode((Geometry) sosObsValue.getValue(),
    // additionalValues);
    // xbObs.addNewResult().set(xmlObject);
    // } else {
    // String exceptionText =
    // "Error while encoding geometry value, needed encoder is missing!";
    // throw Util4Exceptions.createNoApplicableCodeException(null,
    // exceptionText);
    // }
    // }
    //
    // }
    //
    // private void createGenericObservation(OMObservationType xbObs,
    // SosObservation sosObs,
    // Map<String, SosAbstractFeature> features) throws OwsExceptionReport {
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = SosConstants.OBS_ID_PREFIX + new
    // DateTime().getMillis();
    // xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    //
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // phenomena of the common observation
    // List<SosObservableProperty> phenComponents = null;
    // if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosObservableProperty) {
    // xbObs.addNewObservedProperty().setHref(
    // sosObs.getObservationConstellation().getObservableProperty().getIdentifier());
    // phenComponents = new ArrayList<SosObservableProperty>();
    // phenComponents.add((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty());
    // } else if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosCompositePhenomenon) {
    // SosCompositePhenomenon compPhen =
    // (SosCompositePhenomenon)
    // sosObs.getObservationConstellation().getObservableProperty();
    // xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
    // phenComponents = compPhen.getPhenomenonComponents();
    // }
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // set result
    // // add resultDefinition
    // XmlObject xbRresult = xbObs.addNewResult();
    // DataArrayDocument xb_dataArrayDoc = createDataArrayResult(phenComponents,
    // sosObs);
    // xbRresult.set(xb_dataArrayDoc);
    // XmlCursor cursor = xbRresult.newCursor();
    // cursor.setAttributeText(new QName(W3CConstants.NS_XSI, "type"),
    // "swe:DataArrayPropertyType");
    // cursor.dispose();
    // }

    private void addResultToObservation(XmlObject xbResult, SosObservation sosObservation,
            List<SosObservableProperty> phenComponents, String observationID) throws OwsExceptionReport {
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
        if ((observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT) || observationType
                .equals(OMConstants.RESULT_MODEL_MEASUREMENT)) && observationValue.getValue() instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) observationValue.getValue();
            MeasureType xbMeasureType =
                    MeasureType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (quantityValue.getUnit() != null) {
                xbMeasureType.setUom(quantityValue.getUnit());
            } else {
                xbMeasureType.setUom("");
            }
            if (!quantityValue.getValue().equals(Double.NaN)) {
                xbMeasureType.setDoubleValue(quantityValue.getValue());
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
                xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, categoryValue, additionalValue));
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
                xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, geometryValue.getValue(), additionalValue));
            } else {
                xbResult.setNil();
            }
        } else if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)
                || observationType.equals(OMConstants.RESULT_MODEL_OBSERVATION)) {
            // TODO create SosSweDataArray
            SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
            Map<HelperValues, String> additionalValues = new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
            additionalValues.put(HelperValues.FOR_OBSERVATION, null);
            // TODO create SosSweDataArray
            xbResult.set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_20, dataArray, additionalValues));
        }
    }

    private void addMultiObservationValueToResult(XmlObject xbResult, SosObservation sosObservation)
            throws OwsExceptionReport {
        SosMultiObservationValues<?> observationValue = (SosMultiObservationValues) sosObservation.getValue();
        Map<HelperValues, String> additionalValues = new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
        additionalValues.put(HelperValues.FOR_OBSERVATION, null);
        SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
        xbResult.set(CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_20, dataArray, additionalValues));
    }

    /**
     * Encodes a SosAbstractFeature to an SpatialSamplingFeature under
     * consideration of duplicated SpatialSamplingFeature in the XML document.
     * 
     * @param observation
     *            XmlObject O&M observation
     * @param absObs
     *            SOS observation
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
        if (!Configurator.getInstance().isFoiEncodedInObservation() || !(feature instanceof SosSamplingFeature)) {
            // if (urlPattern != null) {
            // featureProperty.setHref(SosHelper.createFoiGetUrl(feature.getIdentifier(),
            // Sos2Constants.SERVICEVERSION,
            // Configurator.getInstance().getServiceURL(), urlPattern));
            // } else {
            featureProperty.setHref(feature.getIdentifier());
            // }
            if (samplingFeature.isSetNames()) {
                featureProperty.setTitle(samplingFeature.getFirstName().getValue());
            }
        } else {
            if (samplingFeature.getUrl() != null) {
                featureProperty.setHref(samplingFeature.getUrl());
            } else {
                try {
                    featureProperty.set(CodingHelper.encodeObjectToXml(OMHelper.getNamespaceForFeatureType(samplingFeature.getFeatureType()), samplingFeature));
                } catch (OwsExceptionReport e) {
                    if (samplingFeature.getXmlDescription() != null) {
                        try {
                            // TODO how set gml:id in already existing
                            // XmlDescription?
                            featureProperty.set(XmlObject.Factory.parse(samplingFeature.getXmlDescription()));
                        } catch (XmlException xmle) {
                            String exceptionText = "Error while encoding featureOfInterest in OMObservation!";
                            LOGGER.error(exceptionText, xmle);
                            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                        }
                    } else {
                        featureProperty.setHref(samplingFeature.getIdentifier());
                        if (samplingFeature.isSetNames()) {
                            featureProperty.setTitle(samplingFeature.getFirstName().getValue());
                        }
                    }
                }
            }
        }
    }
    // /**
    // * Creates XML representation of OM 2.0 observation type from SOS
    // * observation.
    // *
    // * @param xbObs
    // * OM 2.0 observation
    // * @param phenID
    // * @param object
    // * @param observationType
    // * @param map
    // * @param absObs
    // * SOS observation
    // * @throws OwsExceptionReport
    // * if an error occurs during creation.
    // */
    // private void createObservation(OMObservationType xbObs, SosObservation
    // sosObs,
    // Map<String, SosAbstractFeature> features, String phenID,
    // SosObservationValue sosObsValue,
    // String observationType) throws OwsExceptionReport {
    //
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = "";
    // if (sosObsValue.getObservationID() != null) {
    // observationID = sosObsValue.getObservationID();
    // xbObs.addNewIdentifier().setStringValue(observationID);
    // } else {
    // observationID = Long.toString(System.currentTimeMillis());
    // }
    // xbObs.addNewType().setHref(observationType);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    // // AbstractTimeObjectType xbAbsTimeObject =
    // // xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
    // // IEncoder encoder =
    // //
    // Configurator.getInstance().getEncoder(xbAbsTimeObject.getDomNode().getNamespaceURI());
    // // if (encoder != null) {
    // // Map<HelperValues, String> additionalValues = new
    // // HashMap<HelperValues, String>();
    // // additionalValues.put(HelperValues.GMLID, phenTimeId);
    // // XmlObject xmlObject = (XmlObject)
    // // encoder.encode(sosObsValue.getPhenomenonTime(), additionalValues);
    // // xbAbsTimeObject.set(xmlObject);
    // // if (sosObsValue.getPhenomenonTime() instanceof TimeInstant) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_INSTANT,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // } else if (sosObsValue.getPhenomenonTime() instanceof TimePeriod) {
    // // xbAbsTimeObject.substitute(new QName(GMLConstants.NS_GML_32,
    // // GMLConstants.EN_TIME_PERIOD,
    // // GMLConstants.NS_GML), xmlObject.schemaType());
    // // }
    // // } else {
    // // String exceptionText =
    // // "Error while encoding phenomenon time, needed encoder is missing!";
    // // throw Util4Exceptions.createNoApplicableCodeException(null,
    // // exceptionText);
    // // }
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // set observedProperty (phenomenon)
    // xbObs.addNewObservedProperty().setHref(phenID);
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // // Currently not used for SOS 2.0 and OM 2.0 encoding.
    // // // // add quality, if set
    // // if (meas.getQuality() != null) {
    // // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
    // // xbQuality.set(createQualityProperty(sosObs.getQuality()));
    // // }
    //
    // // set result
    // if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
    // || observationType.equals(OMConstants.RESULT_MODEL_MEASUREMENT)) {
    // MeasureType xbMeasureType = MeasureType.Factory.newInstance();
    // if (((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty()).getUnit()
    // != null) {
    // xbMeasureType.setUom(((SosObservableProperty)
    // sosObs.getObservationConstellation()
    // .getObservableProperty()).getUnit());
    // } else {
    // xbMeasureType.setUom("");
    // }
    //
    // if (!((Double) sosObsValue.getValue()).equals(Double.NaN)) {
    // xbMeasureType.setDoubleValue((Double) sosObsValue.getValue());
    // }
    //
    // else {
    // xbMeasureType.setNil();
    // }
    // xbObs.addNewResult().set(xbMeasureType);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION))
    // {
    // Reference xbRef = Reference.Factory.newInstance();
    // xbRef.setHref((String) sosObsValue.getValue());
    // xbObs.addNewResult().set(xbRef);
    // } else if
    // (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
    // || observationType.equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION))
    // {
    // IEncoder geomEncoder =
    // Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
    // if (geomEncoder != null) {
    // Map<HelperValues, String> additionalValues = new HashMap<HelperValues,
    // String>();
    // additionalValues.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX +
    // observationID);
    // XmlObject xmlObject =
    // (XmlObject) geomEncoder.encode((Geometry) sosObsValue.getValue(),
    // additionalValues);
    // xbObs.addNewResult().set(xmlObject);
    // } else {
    // String exceptionText =
    // "Error while encoding geometry value, needed encoder is missing!";
    // throw Util4Exceptions.createNoApplicableCodeException(null,
    // exceptionText);
    // }
    // }
    //
    // }
    //
    // private void createGenericObservation(OMObservationType xbObs,
    // SosObservation sosObs,
    // Map<String, SosAbstractFeature> features) throws OwsExceptionReport {
    // xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
    // String observationID = SosConstants.OBS_ID_PREFIX + new
    // DateTime().getMillis();
    // xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
    //
    // // set eventTime
    // String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" +
    // observationID;
    // addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenTimeId,
    // sosObs.getPhenomenonTime());
    //
    // xbObs.addNewResultTime().setHref("#" + phenTimeId);
    //
    // // set procedure
    // xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());
    //
    // // phenomena of the common observation
    // List<SosObservableProperty> phenComponents = null;
    // if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosObservableProperty) {
    // xbObs.addNewObservedProperty().setHref(
    // sosObs.getObservationConstellation().getObservableProperty().getIdentifier());
    // phenComponents = new ArrayList<SosObservableProperty>();
    // phenComponents.add((SosObservableProperty)
    // sosObs.getObservationConstellation().getObservableProperty());
    // } else if (sosObs.getObservationConstellation().getObservableProperty()
    // instanceof SosCompositePhenomenon) {
    // SosCompositePhenomenon compPhen =
    // (SosCompositePhenomenon)
    // sosObs.getObservationConstellation().getObservableProperty();
    // xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
    // phenComponents = compPhen.getPhenomenonComponents();
    // }
    //
    // // set feature
    // encodeFeatureOfInterest(xbObs,
    // sosObs.getObservationConstellation().getFeatureOfInterest(),
    // features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));
    //
    // // set result
    // // add resultDefinition
    // XmlObject xbRresult = xbObs.addNewResult();
    // DataArrayDocument xb_dataArrayDoc = createDataArrayResult(phenComponents,
    
    // sosObs);
    // xbRresult.set(xb_dataArrayDoc);
    // XmlCursor cursor = xbRresult.newCursor();
    // cursor.setAttributeText(new QName(W3CConstants.NS_XSI, "type"),
    // "swe:DataArrayPropertyType");
    // cursor.dispose();
    // }

}