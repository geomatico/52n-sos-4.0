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

import net.opengis.om.x10.CategoryObservationDocument;
import net.opengis.om.x10.CategoryObservationType;
import net.opengis.om.x10.CountObservationDocument;
import net.opengis.om.x10.CountObservationType;
import net.opengis.om.x10.GeometryObservationDocument;
import net.opengis.om.x10.GeometryObservationType;
import net.opengis.om.x10.MeasurementDocument;
import net.opengis.om.x10.MeasurementType;
import net.opengis.om.x10.ObservationCollectionDocument;
import net.opengis.om.x10.ObservationCollectionType;
import net.opengis.om.x10.ObservationDocument;
import net.opengis.om.x10.ObservationPropertyType;
import net.opengis.om.x10.ObservationType;
import net.opengis.om.x10.TruthObservationDocument;
import net.opengis.om.x10.TruthObservationType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.joda.time.DateTime;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
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
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.service.profile.Profile;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.GmlHelper;
import org.n52.sos.util.N52XmlHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.SweHelper;
import org.n52.sos.util.XmlHelper;
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
            Collections.singletonMap(Sos1Constants.SERVICEVERSION, CollectionHelper.set(OMConstants.CONTENT_TYPE_OM)));

    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(CodingHelper.encoderKeysForElements(
            OMConstants.NS_OM, SosObservation.class, GetObservationResponse.class, GetObservationByIdResponse.class),
            CodingHelper.encoderKeysForElements(OMConstants.CONTENT_TYPE_OM, SosObservation.class,
                    GetObservationResponse.class, GetObservationByIdResponse.class));

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
        if (SUPPORTED_RESPONSE_FORMATS.get(service) != null) {
            if (SUPPORTED_RESPONSE_FORMATS.get(service).get(version) != null) {
                return SUPPORTED_RESPONSE_FORMATS.get(service).get(version);
            }
        }
        return Collections.emptySet();
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
            return createObservation((SosObservation) element, additionalValues);
        } else if (element instanceof GetObservationResponse) {
            GetObservationResponse response = (GetObservationResponse) element;
            return createObservationCollection(response.getObservationCollection(), response.getResultModel());
        } else if (element instanceof GetObservationByIdResponse) {
            GetObservationByIdResponse response = (GetObservationByIdResponse) element;
            return createObservationCollection(response.getObservationCollection(), response.getResultModel());
        }
        throw new UnsupportedEncoderInputException(this, element);
    }

    private XmlObject createObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        String observationType = checkObservationType(sosObservation);
        if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)) {
            return createMeasurement(sosObservation, additionalValues);
        } else if (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)) {
            return createCategoryObservation(sosObservation, additionalValues);
        } else if (observationType.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION)) {
            return createCountObservation(sosObservation, additionalValues);
        } else if (observationType.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
            return createTruthObservation(sosObservation, additionalValues);
        } else if (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)) {
            return createGeometryObservation(sosObservation, additionalValues);
        } else {
            return createOmObservation(sosObservation, additionalValues);
        }
    }

    private String checkObservationType(SosObservation sosObservation) {
        if (sosObservation.isSetResultType()) {
            return sosObservation.getResultType();
        } else if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            SosSingleObservationValue<?> observationValue = (SosSingleObservationValue<?>) sosObservation.getValue();
            return OMHelper.getObservationTypeFor(observationValue.getValue());
        }
        return OMConstants.OBS_TYPE_OBSERVATION;
    }

    private XmlObject createObservationCollection(List<SosObservation> sosObservationCollection, String resultModel)
            throws OwsExceptionReport {
        ObservationCollectionDocument xbObservationCollectionDoc =
                ObservationCollectionDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ObservationCollectionType xbObservationCollection = xbObservationCollectionDoc.addNewObservationCollection();
        xbObservationCollection.setId(SosConstants.OBS_COL_ID_PREFIX + new DateTime().getMillis());
        if (CollectionHelper.isNotEmpty(sosObservationCollection)) {
            SosEnvelope sosEnvelope = getEnvelope(sosObservationCollection);
            Encoder<XmlObject, SosEnvelope> envEncoder = CodingHelper.getEncoder(GMLConstants.NS_GML, sosEnvelope);
            xbObservationCollection.addNewBoundedBy().addNewEnvelope().set(envEncoder.encode(sosEnvelope));
            for (SosObservation sosObservation : sosObservationCollection) {
                String observationType = checkObservationType(sosObservation);
                if (StringHelper.isNullOrEmpty(resultModel)
                        || (StringHelper.isNotEmpty(resultModel) && resultModel.equals(observationType))) {
                    xbObservationCollection.addNewMember().set(createObservation(sosObservation, null));
                } else {
                   throw new InvalidParameterValueException().at(
                            Sos1Constants.GetObservationParams.resultModel).withMessage(
                            "The requested resultModel '%s' is invalid for the resulting observations!", OMHelper.getEncodedResultModelFor(resultModel));
                }
            }
        } else {
            ObservationPropertyType xbObservation = xbObservationCollection.addNewMember();
            xbObservation.setHref(GMLConstants.NIL_INAPPLICABLE);
        }
        XmlHelper.makeGmlIdsUnique(xbObservationCollectionDoc.getDomNode());
        List<String> schemaLocations = new ArrayList<String>(3);
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSOS100());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForOM100());
        schemaLocations.add(N52XmlHelper.getSchemaLocationForSA100());
        // schemaLocations.add(N52XmlHelper.getSchemaLocationForSWE101());
        N52XmlHelper.setSchemaLocationsToDocument(xbObservationCollectionDoc, schemaLocations);
        return xbObservationCollectionDoc;
    }

    private SosEnvelope getEnvelope(List<SosObservation> sosObservationCollection) {
        SosEnvelope sosEnvelope = new SosEnvelope();
        for (SosObservation sosObservation : sosObservationCollection) {
            sosObservation.getObservationConstellation().getFeatureOfInterest();
            SosSamplingFeature samplingFeature =
                    (SosSamplingFeature) sosObservation.getObservationConstellation().getFeatureOfInterest();
            sosEnvelope.setSrid(samplingFeature.getGeometry().getSRID());
            sosEnvelope.expandToInclude(samplingFeature.getGeometry().getEnvelopeInternal());
        }
        return sosEnvelope;
    }

    private XmlObject createMeasurement(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        MeasurementDocument xbMeasurementDoc =
                MeasurementDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        MeasurementType xbObs = xbMeasurementDoc.addNewMeasurement();
        addValuesToObservation(xbObs, sosObservation, additionalValues);
        addSingleObservationToResult(xbObs.addNewResult(), sosObservation);
        return xbMeasurementDoc;
    }

    private XmlObject createCategoryObservation(SosObservation sosObservation,
            Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        CategoryObservationDocument xbCategoryObservationDoc =
                CategoryObservationDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        CategoryObservationType xbObs = xbCategoryObservationDoc.addNewCategoryObservation();
        addValuesToObservation(xbObs, sosObservation, additionalValues);
        addSingleObservationToResult(xbObs.addNewResult(), sosObservation);
        return xbCategoryObservationDoc;
    }

    private XmlObject createCountObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        CountObservationDocument xbCountObservationDoc =
                CountObservationDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        CountObservationType xbObs = xbCountObservationDoc.addNewCountObservation();
        addValuesToObservation(xbObs, sosObservation, additionalValues);
        addSingleObservationToResult(xbObs.addNewResult(), sosObservation);
        return xbCountObservationDoc;
    }

    private XmlObject createTruthObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        TruthObservationDocument xbTruthObservationDoc =
                TruthObservationDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        TruthObservationType xbObs = xbTruthObservationDoc.addNewTruthObservation();
        addValuesToObservation(xbObs, sosObservation, additionalValues);
        addSingleObservationToResult(xbObs.addNewResult(), sosObservation);
        return xbTruthObservationDoc;
    }

    private XmlObject createGeometryObservation(SosObservation sosObservation,
            Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        GeometryObservationDocument xbGeometryObservationDoc =
                GeometryObservationDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        GeometryObservationType xbObs = xbGeometryObservationDoc.addNewGeometryObservation();
        addValuesToObservation(xbObs, sosObservation, additionalValues);
        addSingleObservationToResult(xbObs.addNewResult(), sosObservation);
        return xbGeometryObservationDoc;
    }

    private XmlObject createOmObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        ObservationDocument xbObservationDoc =
                ObservationDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        ObservationType xbObs = xbObservationDoc.addNewObservation();
        List<SosObservableProperty> phenComponents = addValuesToObservation(xbObs, sosObservation, additionalValues);
        addResultToObservation(xbObs.addNewResult(), sosObservation, phenComponents);
        return xbObservationDoc;
    }

    private List<SosObservableProperty> addValuesToObservation(ObservationType xbObs, SosObservation sosObservation,
            Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        if (!sosObservation.isSetObservationID()) {
            sosObservation.setObservationID(xbObs.getId().replace("o_", ""));
        }
        String observationID = sosObservation.getObservationID();
        // set samplingTime
        ITime samplingTime = sosObservation.getPhenomenonTime();
        if (samplingTime.getGmlId() == null) {
            samplingTime.setGmlId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }
        addSamplingTime(xbObs, samplingTime);
        // set resultTime
        addResultTime(xbObs, sosObservation);

        // set procedure
        xbObs.addNewProcedure().setHref(sosObservation.getObservationConstellation().getProcedure().getIdentifier());
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
        return phenComponents;
    }

    private void addSamplingTime(ObservationType xbObservation, ITime iTime) throws OwsExceptionReport {
        XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, iTime);
        XmlObject substitution =
                xbObservation.addNewSamplingTime().addNewTimeObject()
                        .substitute(GmlHelper.getGml311QnameForITime(iTime), xmlObject.schemaType());
        substitution.set(xmlObject);
    }

    private void addResultTime(ObservationType xbObs, SosObservation sosObservation) throws OwsExceptionReport {
        ITime phenomenonTime = sosObservation.getPhenomenonTime();
        if (sosObservation.isSetResultTime()) {
            if (sosObservation.getResultTime().equals(phenomenonTime)) {
                xbObs.addNewResultTime().setHref("#" + phenomenonTime.getGmlId());
            } else {
                TimeInstant resultTime = sosObservation.getResultTime();
                if (!resultTime.isSetGmlId()) {
                    resultTime.setGmlId("resultTime_" + sosObservation.getObservationID());
                }
                addResultTime(xbObs, resultTime);
            }
        } else {
            if (phenomenonTime instanceof TimeInstant) {
                xbObs.addNewResultTime().setHref("#" + phenomenonTime.getGmlId());
            } else if (phenomenonTime instanceof TimePeriod) {
                TimeInstant resultTime = new TimeInstant(((TimePeriod) sosObservation.getPhenomenonTime()).getEnd());
                resultTime.setGmlId("resultTime_" + sosObservation.getObservationID());
                addResultTime(xbObs, resultTime);
            }
        }
    }

    private void addResultTime(ObservationType xbObs, TimeInstant iTime) throws OwsExceptionReport {
        XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, iTime);
        XmlObject substitution =
                xbObs.addNewResultTime().addNewTimeObject()
                        .substitute(GmlHelper.getGml311QnameForITime(iTime), xmlObject.schemaType());
        substitution.set(xmlObject);
    }

    private XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
    }

    private void addResultToObservation(XmlObject xbResult, SosObservation sosObservation,
            List<SosObservableProperty> phenComponents) throws OwsExceptionReport {
        // TODO if OM_SWEArrayObservation and get ResultEncoding and
        // ResultStructure exists,
        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            addSingleObservationToResult(xbResult, sosObservation);
        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
            addMultiObservationValueToResult(xbResult, sosObservation);
        }
    }

    // FIXME String.equals(QName) !?
    private void addSingleObservationToResult(XmlObject xbResult, SosObservation sosObservation)
            throws OwsExceptionReport {
        String observationType = sosObservation.getObservationConstellation().getObservationType();
        SosSingleObservationValue<?> observationValue = (SosSingleObservationValue<?>) sosObservation.getValue();
        if (observationValue.getValue() instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) observationValue.getValue();
            xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, quantityValue));
        } else if (observationValue.getValue() instanceof CountValue) {
            CountValue countValue = (CountValue) observationValue.getValue();
            XmlInteger xbInteger = XmlInteger.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (countValue.getValue() != null && countValue.getValue() != Integer.MIN_VALUE) {
                xbInteger.setBigIntegerValue(new BigInteger(countValue.getValue().toString()));
            } else {
                xbInteger.setNil();
            }
            xbResult.set(xbInteger);
        } else if (observationValue.getValue() instanceof TextValue) {
            TextValue textValue = (TextValue) observationValue.getValue();
            XmlString xbString = XmlString.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (textValue.getValue() != null && !textValue.getValue().isEmpty()) {
                xbString.setStringValue(textValue.getValue());
            } else {
                xbString.setNil();
            }
            xbResult.set(xbString);
        } else if (observationValue.getValue() instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) observationValue.getValue();
            XmlBoolean xbBoolean = XmlBoolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (booleanValue.getValue() != null) {
                xbBoolean.setBooleanValue(booleanValue.getValue());
            } else {
                xbBoolean.setNil();
            }
            xbResult.set(xbBoolean);
        } else if (observationValue.getValue() instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) observationValue.getValue();
            if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
                Map<HelperValues, String> additionalValue = new EnumMap<HelperValues, String>(HelperValues.class);
                additionalValue
                        .put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + sosObservation.getObservationID());
                xbResult.set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, categoryValue, additionalValue));
            } else {
                xbResult.setNil();
            }
        } else if (observationValue.getValue() instanceof GeometryValue) {
            GeometryValue geometryValue = (GeometryValue) observationValue.getValue();
            if (geometryValue.getValue() != null) {
                Map<HelperValues, String> additionalValue = new EnumMap<HelperValues, String>(HelperValues.class);
                additionalValue
                        .put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + sosObservation.getObservationID());
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
     * 
     * @throws OwsExceptionReport
     */
    private void addFeatureOfInterest(ObservationType observation, SosAbstractFeature feature)
            throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(HelperValues.class);
        Profile activeProfile = Configurator.getInstance().getProfileHandler().getActiveProfile();
        additionalValues.put(HelperValues.ENCODE,
                Boolean.toString(activeProfile.isEncodeFeatureOfInterestInObservations()));
        XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, feature, additionalValues);
        observation.addNewFeatureOfInterest().set(encodeObjectToXml);
    }
}
