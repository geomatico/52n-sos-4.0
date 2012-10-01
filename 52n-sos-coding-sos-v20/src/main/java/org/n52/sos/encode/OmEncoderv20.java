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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimeObjectType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.TimeObjectPropertyType;
import net.opengis.swe.x20.BooleanType;
import net.opengis.swe.x20.CategoryType;
import net.opengis.swe.x20.CountPropertyType;
import net.opengis.swe.x20.CountType;
import net.opengis.swe.x20.DataArrayDocument;
import net.opengis.swe.x20.DataArrayType;
import net.opengis.swe.x20.DataArrayType.ElementType;
import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.DataRecordType.Field;
import net.opengis.swe.x20.EncodedValuesPropertyType;
import net.opengis.swe.x20.QuantityType;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.UnitReference;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.decode.DecoderKeyType;
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
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.GmlHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class OmEncoderv20 implements IObservationEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OmEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    private Map<SupportedTypeKey, Set<String>> supportedObservationTypes;

    private Set<String> conformanceClasses;

    private Map<String, Map<String, Set<String>>> supportedResponseFormats;
    
    private boolean supported = true;

    public OmEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(OMConstants.NS_OM_2));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        setSupportedObservationTypes();
        setConformaceClasses();
        setSupportedResponseFormats();
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return supportedObservationTypes;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return conformanceClasses;
    }

    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(OMConstants.NS_OM_2, OMConstants.NS_OM_PREFIX);
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
    public void setSupported(boolean supportted) {
        this.supported = supportted;
    }

    @Override
    public boolean mergeObservationValuesWithSameParameters() {
        return false;
    }

    @Override
    public String getContentType() {
        return OMConstants.CONTENT_TYPE_OM_2;
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, new HashMap<HelperValues, String>());
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
        String observationID = "";
        if (sosObservation.getObservationID() != null) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObs.getId().replace("o_", "");
        }
        if (sosObservation.getIdentifier() != null) {
            xbObs.addNewIdentifier().setStringValue(sosObservation.getIdentifier());
        }

        String observationType = sosObservation.getObservationConstellation().getObservationType();
        xbObs.addNewType().setHref(observationType);
        // set phenomenonTime
        if (sosObservation.getPhenomenonTime().getId() == null) {
            sosObservation.getPhenomenonTime().setId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }
        addPhenomenonTime(xbObs.addNewPhenomenonTime(), sosObservation.getPhenomenonTime());
        // set resultTime
        xbObs.addNewResultTime().setHref("#" + sosObservation.getPhenomenonTime().getId());
        // set procedure
        xbObs.addNewProcedure().setHref(sosObservation.getObservationConstellation().getProcedure());
        // set observedProperty (phenomenon)
        List<SosObservableProperty> phenComponents = null;
        if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosObservableProperty) {
            xbObs.addNewObservedProperty().setHref(
                    sosObservation.getObservationConstellation().getObservableProperty().getIdentifier());
            phenComponents = new ArrayList<SosObservableProperty>();
            phenComponents.add((SosObservableProperty) sosObservation.getObservationConstellation()
                    .getObservableProperty());
        } else if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosCompositePhenomenon) {
            SosCompositePhenomenon compPhen =
                    (SosCompositePhenomenon) sosObservation.getObservationConstellation().getObservableProperty();
            xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
            phenComponents = compPhen.getPhenomenonComponents();
        }
        // set feature
        String gmlID = additionalValues.get(HelperValues.GMLID);
        boolean existFoiInDoc = Boolean.parseBoolean(additionalValues.get(HelperValues.EXIST_FOI_IN_DOC));
        encodeFeatureOfInterest(xbObs, sosObservation.getObservationConstellation().getFeatureOfInterest(), gmlID,
                existFoiInDoc);

        // set result
        addResultToObservation(xbObs.addNewResult(), sosObservation, phenComponents, observationID);

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
        IEncoder encoder = Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
        if (encoder != null) {
            Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
            XmlObject xmlObject = (XmlObject) encoder.encode(iTime);
            if (xmlObject instanceof AbstractTimeObjectType) {
                XmlObject substitution =
                        timeObjectPropertyType.addNewAbstractTimeObject().substitute(
                                GmlHelper.getQnameForITime(iTime), xmlObject.schemaType());
                substitution.set((AbstractTimeObjectType) xmlObject);
            } else {
                // TODO: Exception
            }
        } else {
            String exceptionText = "Error while encoding phenomenon time, needed encoder is missing!";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    /**
     * creates the swe:DataArray, which is used for the result element of a
     * generic om:Observation
     * 
     * @param absObs
     * @param phenComponents
     * 
     * @param phenComponents
     *            ids of the phenomena of the common observation
     * @return DataDefinitionType representing the DataDefinition element of a
     *         CommonObservation
     * @throws OwsExceptionReport
     */
    private DataArrayDocument createDataArrayResult(List<SosObservableProperty> phenComponents,
            SosObservation sosObservation) throws OwsExceptionReport {
        SosMultiObservationValues sosObservationValue = (SosMultiObservationValues) sosObservation.getValue();
        if (sosObservationValue.getValue() instanceof SweDataArrayValue) {
            // TODO: move this to SweCommonEncoderv20
            DataArrayDocument xbDataArrayDoc =
                    DataArrayDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            DataArrayType xbDataArray = xbDataArrayDoc.addNewDataArray1();

            // set element count
            CountPropertyType xb_elementCount = xbDataArray.addNewElementCount();
            // TODOD: CHECK SIZE

            // element count

            Map<ITime, Map<String, IValue>> valueMap = ((SweDataArrayValue) sosObservationValue.getValue()).getValue();

            xb_elementCount.addNewCount().setValue(new BigInteger(Integer.toString(valueMap.size())));

            // create data definition
            ElementType xb_elementType = xbDataArray.addNewElementType();

            DataRecordDocument xbDataRecordDoc = DataRecordDocument.Factory.newInstance();
            DataRecordType xb_dataRecord = xbDataRecordDoc.addNewDataRecord();

            // add time component
            Field xbField = xb_dataRecord.addNewField();
            xbField.setName(OMConstants.PHENOMENON_TIME_NAME);
            TimeType xbTimeComponent =
                    (TimeType) xbField.addNewAbstractDataComponent().substitute(SWEConstants.QN_TIME_SWE_200,
                            TimeType.type);
            xbTimeComponent.setDefinition(OMConstants.PHENOMENON_TIME);
            xbTimeComponent.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);

            // add phenomenon components
            for (SosObservableProperty observableProperty : phenComponents) {
                Field field = xb_dataRecord.addNewField();
                addDataComponentToField(field, observableProperty, valueMap.values());
            }

            // set components to SimpleDataRecord
            xb_elementType.set(xbDataRecordDoc);
            xb_elementType.setName("Components");

            // add encoding element
            TextEncodingType xb_textBlock =
                    (TextEncodingType) xbDataArray.addNewEncoding().addNewAbstractEncoding()
                            .substitute(SWEConstants.QN_TEXT_ENCODING_SWE_200, TextEncodingType.type);

            xb_textBlock.setDecimalSeparator(Configurator.getInstance().getDecimalSeparator());
            xb_textBlock.setTokenSeparator(Configurator.getInstance().getTokenSeperator());
            xb_textBlock.setBlockSeparator(Configurator.getInstance().getTupleSeperator());

            EncodedValuesPropertyType xb_values = xbDataArray.addNewValues();
            xb_values.newCursor().setTextValue(createResultString(phenComponents, sosObservation, valueMap));
            return xbDataArrayDoc;
        }
        return null;
    }

    private void addDataComponentToField(Field field, SosObservableProperty observableProperty,
            Collection<Map<String, IValue>> values) {
        IValue value = getValueForObservableProperty(values, observableProperty.getIdentifier());
        if (value != null) {
            if (value instanceof BooleanValue) {
                BooleanType xbBool =
                        (BooleanType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_BOOLEAN_SWE_200,
                                BooleanType.type);
                xbBool.setDefinition(observableProperty.getIdentifier());
            } else if (value instanceof CountValue) {
                CountType xbCount =
                        (CountType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_COUNT_SWE_200,
                                CountType.type);
                xbCount.setDefinition(observableProperty.getIdentifier());
            } else if (value instanceof QuantityValue) {
                QuantityType xbQuantity =
                        (QuantityType) field.addNewAbstractDataComponent().substitute(
                                SWEConstants.QN_QUANTITY_SWE_200, QuantityType.type);
                xbQuantity.setDefinition(observableProperty.getIdentifier());
                UnitReference xb_uom = xbQuantity.addNewUom();
                xb_uom.setCode(observableProperty.getUnit());
            }
            // else if (value instanceof t) {
            // TimeType xbTime =
            // (TimeType) field.addNewAbstractDataComponent().substitute(
            // SWEConstants.QN_TIME_SWE_200, TimeType.type);
            // xbTime.setDefinition(observableProperty.getIdentifier());
            // xbTime.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
            // }
            else if (value instanceof TextValue) {
                TextType xbText =
                        (TextType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                TextType.type);
                xbText.setDefinition(observableProperty.getIdentifier());
            } else if (value instanceof CategoryValue) {
                CategoryType xbCategory =
                        (CategoryType) field.addNewAbstractDataComponent().substitute(
                                SWEConstants.QN_CATEGORY_SWE_200, CategoryType.type);
                xbCategory.setDefinition(observableProperty.getIdentifier());
            } else {
                TextType xbText =
                        (TextType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                TextType.type);
                xbText.setDefinition(observableProperty.getIdentifier());
            }
            String[] uriParts = observableProperty.getIdentifier().split("/|:");
            field.setName(uriParts[uriParts.length - 1]);
        } else {
            field.setName(observableProperty.getIdentifier().replace(SosConstants.PHENOMENON_PREFIX, ""));
            TextType xbText =
                    (TextType) field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                            TextType.type);
            xbText.setDefinition(observableProperty.getIdentifier());
        }

    }

    private IValue getValueForObservableProperty(Collection<Map<String, IValue>> values, String identifier) {
        for (Map<String, IValue> map : values) {
            if (map.containsKey(identifier) && map.get(identifier) != null) {
                return map.get(identifier);
            }
        }
        return null;
    }

    /**
     * creates a result string representing the value matrix of the common
     * observation or getResult response document
     * 
     * @param phenComponents
     *            the phenomenon components of the values of the value matrix
     * @param valueMap
     * @param valueMap
     *            HashMap containing the time as key, and an ArrayList with
     *            pairs of phenomena and values at the key time as values
     * @return String representing the value matrix of the result element
     * @throws OwsExceptionReport
     * @throws ServiceException
     */
    private String createResultString(List<SosObservableProperty> phenComponents, SosObservation sosObservation,
            Map<ITime, Map<String, IValue>> valueMap) throws OwsExceptionReport {

        // save the position for values of each phenomenon in a hash map with
        // the phen id as key
        // and the postion as value
        HashMap<String, Integer> phenIdsAndValueStringPos = new HashMap<String, Integer>();
        int i = 2;
        for (SosObservableProperty phenComp : phenComponents) {
            phenIdsAndValueStringPos.put(phenComp.getIdentifier(), Integer.valueOf(i));
            i++;
        }

        // Map<ITime, Map<String, String>> values = new HashMap<ITime,
        // Map<String, String>>();
        // for (SosObservableProperty sosObservableProperty : phenComponents) {
        // List<SosObservationValue> timeValueTupel =
        // sosObs.getValues().get(sosObservableProperty.getIdentifier());
        // for (SosObservationValue sosObservationValue : timeValueTupel) {
        //
        // Map<String, String> map = null;
        // if (values.containsKey(sosObservationValue.getPhenomenonTime())) {
        // map = values.get(sosObservationValue.getPhenomenonTime());
        // } else {
        // map = new HashMap<String, String>();
        // }
        // map.put(sosObservableProperty.getIdentifier(),
        // getStringValue(sosObservableProperty.getIdentifier(),
        // sosObservationValue.getValue()));
        // values.put(sosObservationValue.getPhenomenonTime(), map);
        // }
        // }

        String noDataValue = sosObservation.getNoDataValue();
        String tokenSeperator = sosObservation.getTokenSeparator();
        String tupleSeperator = sosObservation.getTupleSeparator();

        // value matrix which should be built
        StringBuffer valueMatrix = new StringBuffer();
        List<ITime> times = new ArrayList<ITime>(valueMap.keySet());
        Collections.sort(times);
        for (ITime time : times) {
            String timeString = getTimeString(time);
            valueMatrix.append(timeString);
            valueMatrix.append(tokenSeperator);
            Map<String, IValue> map = valueMap.get(time);
            for (SosObservableProperty obsProp : phenComponents) {
                if (map.containsKey(obsProp.getIdentifier())) {
                    IValue value = map.get(obsProp.getIdentifier());
                    if (value == null) {
                        valueMatrix.append(noDataValue);
                    } else {
                        valueMatrix.append(getStringValue(value, noDataValue));
                    }
                } else {
                    valueMatrix.append(noDataValue);
                }
                valueMatrix.append(tokenSeperator);
            }
            // delete last TokenSeperator
            int tokenSepLength = tokenSeperator.length();
            valueMatrix.delete(valueMatrix.length() - tokenSepLength, valueMatrix.length());
            valueMatrix.append(tupleSeperator);
        }
        return valueMatrix.toString();
    }

    /**
     * Get time as text for TimePeriod or TimeInstant
     * 
     * @param time
     *            To to get text from.
     * @return time string
     * @throws OwsExceptionReport
     */
    private String getTimeString(ITime time) throws OwsExceptionReport {
        StringBuilder timeString = new StringBuilder();
        if (time instanceof TimeInstant) {
            TimeInstant ti = (TimeInstant) time;
            timeString.append(DateTimeHelper.formatDateTime2ResponseString(ti.getValue()));
        } else if (time instanceof TimePeriod) {
            TimePeriod tp = (TimePeriod) time;
            timeString.append(DateTimeHelper.formatDateTime2ResponseString(tp.getStart()));
            timeString.append("/");
            timeString.append(DateTimeHelper.formatDateTime2ResponseString(tp.getEnd()));
        }
        return timeString.toString();
    }

    /**
     * Get value as text.
     * 
     * @param noDataValue
     * 
     * @param obsPropID
     *            observableProperty
     * @param object
     *            value
     * @return value as text.
     */
    private String getStringValue(IValue value, String noDataValue) {
        WKTWriter wktWriter = new WKTWriter();
        if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            if (booleanValue.getValue() == null) {
                return noDataValue;
            } else {
                return Boolean.toString(booleanValue.getValue().booleanValue());
            }
        } else if (value instanceof CountValue) {
            CountValue countValue = (CountValue) value;
            if (countValue.getValue() == null
                    || (countValue.getValue() != null && countValue.getValue() == Integer.MIN_VALUE)) {
                return noDataValue;
            } else {
                return Integer.toString(countValue.getValue().intValue());
            }
        } else if (value instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) value;
            if (quantityValue.getValue() == null
                    || (quantityValue.getValue() != null && quantityValue.getValue().equals(Double.NaN))) {
                return noDataValue;
            } else {
                return Double.toString(quantityValue.getValue().doubleValue());
            }
        }
        // else if (value instanceof t) {
        // TimeType xbTime =
        // (TimeType) field.addNewAbstractDataComponent().substitute(
        // SWEConstants.QN_TIME_SWE_200, TimeType.type);
        // xbTime.setDefinition(observableProperty.getIdentifier());
        // xbTime.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
        // }
        else if (value instanceof TextValue) {
            TextValue textValue = (TextValue) value;
            if (textValue.getValue() == null || (textValue.getValue() != null && !textValue.getValue().isEmpty())) {
                return noDataValue;
            } else {
                return textValue.getValue().toString();
            }
        } else if (value instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) value;
            if (categoryValue.getValue() == null
                    || (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty())) {
                return noDataValue;
            } else {
                return categoryValue.getValue().toString();
            }
        } else {
            if (value.getValue() == null) {
                return noDataValue;
            } else {
                return value.getValue().toString();
            }
        }
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

    private void addResultToObservation(XmlObject addNewResult, SosObservation sosObservation,
            List<SosObservableProperty> phenComponents, String observationID) throws OwsExceptionReport {
        String observationType = sosObservation.getObservationConstellation().getObservationType();
        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            addSingleObservationToResultSet(addNewResult, (SosSingleObservationValue) sosObservation.getValue(),
                    observationID, sosObservation.getObservationConstellation().getObservationType());
        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
            addMultiObservationValueToResult(addNewResult, sosObservation, phenComponents);
        }

    }

    private void addSingleObservationToResultSet(XmlObject addNewResult, SosSingleObservationValue observationValue,
            String observationID, String observationType) throws OwsExceptionReport {
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
            addNewResult.set(xbMeasureType);
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
            addNewResult.set(xbInteger);
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
            addNewResult.set(xbString);
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
            addNewResult.set(xbBoolean);
        } else if ((observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION))
                && observationValue.getValue() instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) observationValue.getValue();
            if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
                IEncoder encoder = Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
                if (encoder != null) {
                    Map<HelperValues, String> additionalValue = new HashMap<HelperValues, String>();
                    additionalValue.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + observationID);
                    XmlObject xmlObject = (XmlObject) encoder.encode(categoryValue, additionalValue);
                    addNewResult.set(xmlObject);
                } else {
                    String exceptionText = "Error while encoding category value, needed encoder is missing!";
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
            } else {
                addNewResult.setNil();
            }
        } else if ((observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION) || observationType
                .equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION))
                && observationValue.getValue() instanceof GeometryValue) {

            GeometryValue geometryValue = (GeometryValue) observationValue.getValue();
            if (geometryValue.getValue() != null) {
                IEncoder geomEncoder = Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
                if (geomEncoder != null) {
                    Map<HelperValues, String> additionalValue = new HashMap<HelperValues, String>();
                    additionalValue.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + observationID);
                    XmlObject xmlObject =
                            (XmlObject) geomEncoder.encode((Geometry) geometryValue.getValue(), additionalValue);
                    addNewResult.set(xmlObject);
                } else {
                    String exceptionText = "Error while encoding geometry value, needed encoder is missing!";
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
            } else {
                addNewResult.setNil();
            }
        }
    }

    private void addMultiObservationValueToResult(XmlObject addNewResult, SosObservation sosObservation,
            List<SosObservableProperty> phenComponents) throws OwsExceptionReport {
        String observationType = sosObservation.getObservationConstellation().getObservationType();
        if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)
                || observationType.equals(OMConstants.RESULT_MODEL_OBSERVATION)) {
            XmlObject xbRresult = addNewResult;
            DataArrayDocument xb_dataArrayDoc = createDataArrayResult(phenComponents, sosObservation);
            xbRresult.set(xb_dataArrayDoc);
            XmlCursor cursor = xbRresult.newCursor();
            cursor.setAttributeText(new QName(W3CConstants.NS_XSI, "type"), "swe:DataArrayPropertyType");
            cursor.dispose();
        }
    }

    /**
     * Encodes a SosAbstractFeature to an SpatialSamplingFeature under
     * consideration of duplicated SpatialSamplingFeature in the XML document.
     * 
     * @param xbObs
     *            XmlObject O&M observation
     * @param absObs
     *            SOS observation
     * @throws OwsExceptionReport
     */
    private void encodeFeatureOfInterest(OMObservationType xbObs, SosAbstractFeature foi, String gmlId,
            boolean existFoiInDoc) throws OwsExceptionReport {
        String urlPattern =
                SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                        SosConstants.Operations.GetFeatureOfInterest.name(), new DecoderKeyType(SosConstants.SOS,
                                Sos2Constants.SERVICEVERSION));
        FeaturePropertyType xbFoiType = xbObs.addNewFeatureOfInterest();
        if (!Configurator.getInstance().isFoiEncodedInObservation()) {
            if (urlPattern != null) {
                xbFoiType.setHref(SosHelper.createFoiGetUrl(foi.getIdentifier(), Sos2Constants.SERVICEVERSION,
                        Configurator.getInstance().getServiceURL(), urlPattern));
            } else {
                xbFoiType.setHref(foi.getIdentifier());
            }
        } else {
            if (!existFoiInDoc) {
                if (foi instanceof SosSamplingFeature) {
                    SosSamplingFeature sampFeat = (SosSamplingFeature) foi;
                    if (sampFeat.getXmlDescription() != null) {
                        try {
                            xbFoiType.set(XmlObject.Factory.parse(sampFeat.getXmlDescription()));
                        } catch (XmlException xmle) {
                            String exceptionText = "Error while encoding featureOfInterest in OMObservation!";
                            LOGGER.error(exceptionText, xmle);
                            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                        }

                    } else if (sampFeat.getUrl() != null) {
                        xbFoiType.setHref(sampFeat.getUrl());
                    } else {
                        IEncoder encoder =
                                Configurator.getInstance().getEncoder(
                                        OMHelper.getNamespaceForFeatureType(sampFeat.getFeatureType()));
                        if (encoder != null) {
                            Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                            additionalValues.put(HelperValues.GMLID, gmlId);
                            xbFoiType.set((XmlObject) encoder.encode(sampFeat, additionalValues));
                        } else {
                            String exceptionText =
                                    "Error while encoding geometry for featureOfInterest, needed encoder is missing!";
                            LOGGER.debug(exceptionText);
                            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                        }
                    }
                } else {
                    if (urlPattern != null) {
                        xbFoiType.setHref(SosHelper.createFoiGetUrl(foi.getIdentifier(), Sos2Constants.SERVICEVERSION,
                                Configurator.getInstance().getServiceURL(), urlPattern));
                    } else {
                        xbFoiType.setHref(foi.getIdentifier());
                    }
                }
            } else {
                xbFoiType.setHref("#" + gmlId);
            }
        }
    }

    private void setSupportedObservationTypes() {
        Map<SupportedTypeKey, Set<String>> supportedObservationTypes = new HashMap<SupportedTypeKey, Set<String>>();
        Set<String> observationTypes = new HashSet<String>(0);
        observationTypes.add(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION);
        observationTypes.add(OMConstants.OBS_TYPE_COUNT_OBSERVATION);
        // observationTypes.add(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION);
        observationTypes.add(OMConstants.OBS_TYPE_MEASUREMENT);
        observationTypes.add(OMConstants.OBS_TYPE_TEXT_OBSERVATION);
        observationTypes.add(OMConstants.OBS_TYPE_TRUTH_OBSERVATION);
        observationTypes.add(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        supportedObservationTypes.put(SupportedTypeKey.ObservationType, observationTypes);
    }

    private void setSupportedResponseFormats() {
        supportedResponseFormats = new HashMap<String, Map<String, Set<String>>>(0);
        Set<String> format = new HashSet<String>(0);
        format.add(OMConstants.NS_OM_2);
        // TODO: set this if service supports
        format.add(SosConstants.CONTENT_TYPE_ZIP);
        Map<String, Set<String>> versionMap = new HashMap<String, Set<String>>(0);
        versionMap.put("2.0.0", format);
        supportedResponseFormats.put("SOS", versionMap);
    }

    private void setConformaceClasses() {
        conformanceClasses = new HashSet<String>(0);
        // TODO: change to correct conformance class
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/measurement");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/categoryObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/countObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/truthObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/geometryObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/textObservation");
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
