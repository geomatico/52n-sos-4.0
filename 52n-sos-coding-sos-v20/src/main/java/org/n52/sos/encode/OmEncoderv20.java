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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.ogc.gml.CodeWithAuthority;
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
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.profile.IProfile;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.GmlHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.SweHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import net.opengis.gml.x32.FeaturePropertyType;

public class OmEncoderv20 implements IObservationEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OmEncoderv20.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(OMConstants.NS_OM_2,
            SosObservation.class);

    // TODO: change to correct conformance class
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(ConformanceClasses.OM_V2_MEASUREMENT,
            ConformanceClasses.OM_V2_CATEGORY_OBSERVATION, ConformanceClasses.OM_V2_COUNT_OBSERVATION,
            ConformanceClasses.OM_V2_TRUTH_OBSERVATION, ConformanceClasses.OM_V2_GEOMETRY_OBSERVATION,
            ConformanceClasses.OM_V2_TEXT_OBSERVATION);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.ObservationType, CollectionHelper.set(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION,
                    OMConstants.OBS_TYPE_COUNT_OBSERVATION,
                    // OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION,
                    OMConstants.OBS_TYPE_MEASUREMENT, OMConstants.OBS_TYPE_TEXT_OBSERVATION,
                    OMConstants.OBS_TYPE_TRUTH_OBSERVATION, OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION));

    private static final Map<String, Map<String, Set<String>>> SUPPORTED_RESPONSE_FORMATS = Collections.singletonMap(
            SosConstants.SOS,
            Collections.singletonMap(Sos2Constants.SERVICEVERSION, Collections.singleton(OMConstants.NS_OM_2)));

    private boolean supported = true;

    public OmEncoderv20() {
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
        nameSpacePrefixMap.put(OMConstants.NS_OM_2, OMConstants.NS_OM_PREFIX);
    }

    @Override
    public boolean isObservationAndMeasurmentV20Type() {
        return true;
    }

    @Override
    public Set<String> getSupportedResponseFormats(String service, String version) {
        if (supported && SUPPORTED_RESPONSE_FORMATS.get(service) != null) {
            if (SUPPORTED_RESPONSE_FORMATS.get(service).get(version) != null) {
                return SUPPORTED_RESPONSE_FORMATS.get(service).get(version);
            }
        }
        return new HashSet<String>(0);
    }

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public void setSupported(boolean supportted) {
        this.supported = supportted;
    }

    @Override
    public boolean shouldObservationsWithSameXBeMerged() {
        return false;
    }

    @Override
    public String getContentType() {
        return OMConstants.CONTENT_TYPE_OM_2;
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
        OMObservationType xbObs =
                OMObservationType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID;
        if (sosObservation.getObservationID() != null) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObs.getId().replace("o_", "");
        }
        if (sosObservation.getIdentifier() != null && sosObservation.getIdentifier().isSetValue()) {
            IEncoder<?, CodeWithAuthority> encoder =
                    Configurator
                            .getInstance()
                            .getCodingRepository()
                            .getEncoder(
                                    CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, sosObservation.getIdentifier()));
            if (encoder != null) {
                XmlObject xmlObject = (XmlObject) encoder.encode(sosObservation.getIdentifier());
                xbObs.addNewIdentifier().set(xmlObject);
            } else {
                String exceptionText = "Error while encoding geometry value, needed encoder is missing!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        }

        String observationType = sosObservation.getObservationConstellation().getObservationType();
        xbObs.addNewType().setHref(observationType);
        /* SosMultiObservationValues will generate always a new ITime... */
        ITime phenomenonTime = sosObservation.getPhenomenonTime();
        // set phenomenonTime
        if (phenomenonTime.getGmlId() == null) {
            phenomenonTime.setGmlId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }
        addPhenomenonTime(xbObs.addNewPhenomenonTime(), phenomenonTime);
        // set resultTime
        addResultTime(xbObs, sosObservation);

        // set procedure
        addProcedure(xbObs.addNewProcedure(), sosObservation.getObservationConstellation().getProcedure(), observationID);
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
        addFeatureOfInterest(xbObs, sosObservation.getObservationConstellation().getFeatureOfInterest());

        // set result
        addResultToObservation(xbObs.addNewResult(), sosObservation, phenComponents, observationID);
        // TODO use devMode to switch on
        // XmlHelper.validateDocument(xbObs);
        return xbObs;

    }
    
    private void addProcedure(OMProcessPropertyType procedure, SosProcedureDescription procedureDescription,
            String observationID) throws OwsExceptionReport {
        if (Configurator.getInstance().getActiveProfile().isEncodeProcedureInObservation(OMConstants.NS_OM_2)) {
            XmlObject encodeProcedure = CodingHelper.encodeObjectToXml(procedureDescription.getDescriptionFormat(), procedureDescription);
            if (encodeProcedure != null) {
                procedure.set(encodeProcedure);
            } else {
                procedure.setHref(procedureDescription.getProcedureIdentifier());   
            }
        } else {
            procedure.setHref(procedureDescription.getProcedureIdentifier());
        }
    }
        
    private void addPhenomenonTime(TimeObjectPropertyType timeObjectPropertyType, ITime iTime)
            throws OwsExceptionReport {
        IEncoder<?, ITime> encoder =
                Configurator.getInstance().getCodingRepository()
                        .getEncoder(CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, iTime));
        if (encoder != null) {
            XmlObject xmlObject = (XmlObject) encoder.encode(iTime);
            XmlObject substitution =
                    timeObjectPropertyType.addNewAbstractTimeObject().substitute(GmlHelper.getQnameForITime(iTime),
                            xmlObject.schemaType());
            substitution.set(xmlObject);
        } else {
            String exceptionText = "Error while encoding phenomenon time, needed encoder is missing!";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    private void addResultTime(OMObservationType xbObs, SosObservation sosObservation) throws OwsExceptionReport {
        TimeInstant resultTime = sosObservation.getResultTime();
        ITime phenomenonTime = sosObservation.getPhenomenonTime();
        if (sosObservation.getResultTime() != null) {
            if (resultTime.equals(phenomenonTime)) {
                xbObs.addNewResultTime().setHref("#" + phenomenonTime.getGmlId());
            } else {
                addResultTime(xbObs, resultTime);
            }
        } else {
            if (phenomenonTime instanceof TimeInstant) {
                xbObs.addNewResultTime().setHref("#" + phenomenonTime.getGmlId());
            } else if (phenomenonTime instanceof TimePeriod) {
                TimeInstant rsTime = new TimeInstant(((TimePeriod) sosObservation.getPhenomenonTime()).getEnd());
                addResultTime(xbObs, rsTime);
            }
        }
    }

    private void addResultTime(OMObservationType xbObs, TimeInstant iTime) throws OwsExceptionReport {
        XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, iTime);
        xbObs.addNewResultTime().addNewTimeInstant().set(xmlObject);
        XmlObject substitution =
                xbObs.getResultTime().getTimeInstant()
                        .substitute(GmlHelper.getQnameForITime(iTime), xmlObject.schemaType());
        substitution.set(xmlObject);
    }

    private XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
    }

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

    // FIXME String.equals(QName)!?
    private void addSingleObservationToResult(XmlObject xbResult, SosObservation sosObservation, String observationID)
            throws OwsExceptionReport {
        String observationType = sosObservation.getObservationConstellation().getObservationType();
        SosSingleObservationValue<?> observationValue = (SosSingleObservationValue) sosObservation.getValue();
        if ((observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT) || observationType
                .equals(OMConstants.RESULT_MODEL_MEASUREMENT)) && observationValue.getValue() instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) observationValue.getValue();
            xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, quantityValue));
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
                XmlObject xmlObject =
                        CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, categoryValue, additionalValue);
                xbResult.set(xmlObject);
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
                XmlObject xmlObject =
                        CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, geometryValue.getValue(),
                                additionalValue);
                xbResult.set(xmlObject);
            } else {
                xbResult.setNil();
            }
        } else if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)
                || observationType.equals(OMConstants.RESULT_MODEL_OBSERVATION)) {
            // TODO create SosSweDataArray
            SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
            Map<HelperValues, String> additionalValues =
                    new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
            additionalValues.put(HelperValues.FOR_OBSERVATION, null);
            // TODO create SosSweDataArray
            Object encodedObj = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_20, dataArray, additionalValues);
            if (encodedObj != null && encodedObj instanceof XmlObject) {
                xbResult.set((XmlObject) encodedObj);
            } else {
                String exceptionMsg =
                        String.format("Encoding of observation value of type \"%s\" failed. Result: %s",
                                observationValue.getValue() != null ? observationValue.getValue().getClass().getName()
                                        : observationValue.getValue(), encodedObj != null ? encodedObj.getClass()
                                        .getName() : encodedObj);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
            }
        }
    }

    private void addMultiObservationValueToResult(XmlObject xbResult, SosObservation sosObservation)
            throws OwsExceptionReport {
        SosMultiObservationValues<?> observationValue = (SosMultiObservationValues) sosObservation.getValue();
        // TODO create SosSweDataArray
        SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
        Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
        additionalValues.put(HelperValues.FOR_OBSERVATION, null);
        Object encodedObj = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_20, dataArray, additionalValues);
        if (encodedObj != null && encodedObj instanceof XmlObject) {
            xbResult.set((XmlObject) encodedObj);
        } else {
            String exceptionMsg =
                    String.format("Encoding of observation value of type \"%s\" failed. Result: %s",
                            observationValue.getValue() != null ? observationValue.getValue().getClass().getName()
                                    : observationValue.getValue(), encodedObj != null ? encodedObj.getClass()
                                    .getName() : encodedObj);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionMsg);
        }
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
    private void addFeatureOfInterest(OMObservationType observation, SosAbstractFeature feature)
            throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues = new HashMap<SosConstants.HelperValues, String>(1);
        IProfile activeProfile = Configurator.getInstance().getActiveProfile();
        additionalValues.put(HelperValues.ENCODE, Boolean.toString(activeProfile.isEncodeFeatureOfInterestInObservations()));
        if (activeProfile.isSetEncodeFeatureOfInterestNamespace()) {
            additionalValues.put(HelperValues.ENCODE_NAMESPACE, activeProfile.getEncodingNamespaceForFeatureOfInterest());
        }
        XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, feature, additionalValues);
        observation.addNewFeatureOfInterest().set(encodeObjectToXml);
     
    }

}
