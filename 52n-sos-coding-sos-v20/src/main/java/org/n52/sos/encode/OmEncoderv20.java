/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.encode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractTimeObjectType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.sos.x20.GetObservationResponseType;
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
import net.opengis.swe.x20.Reference;
import net.opengis.swe.x20.TextEncodingType;
import net.opengis.swe.x20.TextType;
import net.opengis.swe.x20.TimeType;
import net.opengis.swe.x20.UnitReference;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosCompositePhenomenon;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.om.SosObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosConstants.ValueTypes;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.W3CConstants;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class OmEncoderv20 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OmEncoderv20.class);

    private int sfIdCounter = 1;

    private HashMap<String, String> gmlID4sfIdentifier;

    private List<EncoderKeyType> encoderKeyTypes;

    public OmEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(OMConstants.NS_OM_2));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, new HashMap<HelperValues, String>());
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof SosObservationCollection) {
            return createObservations((SosObservationCollection) element);
        }
        return null;
    }

    private XmlObject createObservations(SosObservationCollection sosObsCol) throws OwsExceptionReport {
        // reset spatialFeatureID counter and spatialFeatureIdentifier/gmlID map
        gmlID4sfIdentifier = new HashMap<String, String>();
        sfIdCounter = 1;
        GetObservationResponseType getObservationResponse =
                GetObservationResponseType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sosObsCol.getObservationMembers() != null && !sosObsCol.getObservationMembers().isEmpty()) {
            Collection<SosObservation> mergedObsCol =
                    SosHelper.mergeObservationsForGenericObservation(sosObsCol.getObservationMembers());
            Iterator<SosObservation> obsIter = mergedObsCol.iterator();

            while (obsIter.hasNext()) {
                SosObservation sosObs = obsIter.next();
                String observationType = sosObs.getObservationConstellation().getObservationType();
                if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
                        || observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
                        || observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
                        || observationType.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION)
                        || observationType.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION)
                        || observationType.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION)) {
                    for (String phenID : sosObs.getValues().keySet()) {
                        List<SosObservationValue> sosObsValues = sosObs.getValues().get(phenID);
                        for (SosObservationValue sosObsValue : sosObsValues) {
                            OMObservationType xbObs =
                                    getObservationResponse.addNewObservationData().addNewOMObservation();
                            createObservation(xbObs, sosObs, sosObsCol.getFeatures(), phenID, sosObsValue,
                                    observationType);
                        }
                    }
                } else if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)) {
                    OMObservationType xbObs = getObservationResponse.addNewObservationData().addNewOMObservation();
                    createGenericObservation(xbObs, sosObs, sosObsCol.getFeatures());
                }
            }
        }
        return getObservationResponse;
    }

    /**
     * Creates XML representation of OM 2.0 observation type from SOS
     * observation.
     * 
     * @param xbObs
     *            OM 2.0 observation
     * @param phenID
     * @param object
     * @param observationType
     * @param map
     * @param absObs
     *            SOS observation
     * @throws OwsExceptionReport
     *             if an error occurs during creation.
     */
    private void createObservation(OMObservationType xbObs, SosObservation sosObs,
            Map<String, SosAbstractFeature> features, String phenID, SosObservationValue sosObsValue,
            String observationType) throws OwsExceptionReport {

        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID = "";
        if (sosObsValue.getObservationID() != null) {
            observationID = sosObsValue.getObservationID();
            xbObs.addNewIdentifier().setStringValue(observationID);
        } else {
            observationID = Long.toString(System.currentTimeMillis());
        }
        xbObs.addNewType().setHref(observationType);

        // set eventTime
        String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" + observationID;
        AbstractTimeObjectType xbAbsTimeObject;
        if (sosObsValue.getPhenomenonTime() instanceof TimeInstant) {
            xbAbsTimeObject = (TimeInstantType)xbObs.addNewPhenomenonTime().addNewAbstractTimeObject().substitute(new QName(GMLConstants.NS_GML_32, GMLConstants.EN_TIME_INSTANT, GMLConstants.NS_GML), TimeInstantType.type);
        } else if (sosObsValue.getPhenomenonTime() instanceof TimePeriod) {
            xbAbsTimeObject = (TimePeriodType)xbObs.addNewPhenomenonTime().addNewAbstractTimeObject().substitute(new QName(GMLConstants.NS_GML_32, GMLConstants.EN_TIME_PERIOD, GMLConstants.NS_GML), TimePeriodType.type);
        } else {
            xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        }
        IEncoder encoder = Configurator.getInstance().getEncoder(xbAbsTimeObject.getDomNode().getNamespaceURI());
        if (encoder != null) {
            Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
            additionalValues.put(HelperValues.GMLID, phenTimeId);
            XmlObject xmlObject = (XmlObject) encoder.encode(sosObsValue.getPhenomenonTime(), additionalValues);
            xbAbsTimeObject.set(xmlObject);
        } else {
            String exceptionText = "Error while encoding phenomenon time, needed encoder is missing!";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());

        // set observedProperty (phenomenon)
        xbObs.addNewObservedProperty().setHref(phenID);

        // set feature
        encodeFeatureOfInterest(xbObs, sosObs.getObservationConstellation().getFeatureOfInterest(),
                features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));

        // // Currently not used for SOS 2.0 and OM 2.0 encoding.
        // // // add quality, if set
        // if (meas.getQuality() != null) {
        // DQElementPropertyType xbQuality = xbObs.addNewResultQuality();
        // xbQuality.set(createQualityProperty(sosObs.getQuality()));
        // }

        // set result
        if (observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT)
                || observationType.equals(OMConstants.RESULT_MODEL_MEASUREMENT)) {
            MeasureType xbMeasureType = MeasureType.Factory.newInstance();
            if (((SosObservableProperty) sosObs.getObservationConstellation().getObservableProperty()).getUnit() != null) {
                xbMeasureType.setUom(((SosObservableProperty) sosObs.getObservationConstellation()
                        .getObservableProperty()).getUnit());
            } else {
                xbMeasureType.setUom("");
            }

            if (!((Double) sosObsValue.getValue()).equals(Double.NaN)) {
                xbMeasureType.setDoubleValue((Double) sosObsValue.getValue());
            }

            else {
                xbMeasureType.setNil();
            }
            xbObs.addNewResult().set(xbMeasureType);
        } else if (observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION)
                || observationType.equals(OMConstants.RESULT_MODEL_CATEGORY_OBSERVATION)) {
            Reference xbRef = Reference.Factory.newInstance();
            xbRef.setHref((String) sosObsValue.getValue());
            xbObs.addNewResult().set(xbRef);
        } else if (observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION)
                || observationType.equals(OMConstants.RESULT_MODEL_GEOMETRY_OBSERVATION)) {
            IEncoder geomEncoder = Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
            if (geomEncoder != null) {
                Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                additionalValues.put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + observationID);
                XmlObject xmlObject =
                        (XmlObject) geomEncoder.encode((Geometry) sosObsValue.getValue(), additionalValues);
                xbObs.addNewResult().set(xmlObject);
            } else {
                String exceptionText = "Error while encoding geometry value, needed encoder is missing!";
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        }

    }

    private void createGenericObservation(OMObservationType xbObs, SosObservation sosObs,
            Map<String, SosAbstractFeature> features) throws OwsExceptionReport {
        xbObs.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID = SosConstants.OBS_ID_PREFIX + new DateTime().getMillis();
        xbObs.addNewType().setHref(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);

        // set eventTime
        String phenTimeId = OMConstants.PHENOMENON_TIME_NAME + "_" + observationID;
        AbstractTimeObjectType xbAbsTimeObject;
        if (sosObs.getPhenomenonTime() instanceof TimeInstant) {
            xbAbsTimeObject = (TimeInstantType)xbObs.addNewPhenomenonTime().addNewAbstractTimeObject().substitute(new QName(GMLConstants.NS_GML_32, GMLConstants.EN_TIME_INSTANT, GMLConstants.NS_GML), TimeInstantType.type);
        } else if (sosObs.getPhenomenonTime() instanceof TimePeriod) {
            xbAbsTimeObject = (TimePeriodType)xbObs.addNewPhenomenonTime().addNewAbstractTimeObject().substitute(new QName(GMLConstants.NS_GML_32, GMLConstants.EN_TIME_PERIOD, GMLConstants.NS_GML), TimePeriodType.type);
        } else {
            xbAbsTimeObject = xbObs.addNewPhenomenonTime().addNewAbstractTimeObject();
        }
        IEncoder encoder = Configurator.getInstance().getEncoder(xbAbsTimeObject.getDomNode().getNamespaceURI());
        if (encoder != null) {
            Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
            additionalValues.put(HelperValues.GMLID, phenTimeId);
            XmlObject xmlObject = (XmlObject) encoder.encode(sosObs.getPhenomenonTime(), additionalValues);
            xbAbsTimeObject.set(xmlObject);
        } else {
            String exceptionText = "Error while encoding phenomenon time, needed encoder is missing!";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        xbObs.addNewResultTime().setHref("#" + phenTimeId);

        // set procedure
        xbObs.addNewProcedure().setHref(sosObs.getObservationConstellation().getProcedure());

        // phenomena of the common observation
        List<SosObservableProperty> phenComponents = null;
        if (sosObs.getObservationConstellation().getObservableProperty() instanceof SosObservableProperty) {
            xbObs.addNewObservedProperty().setHref(
                    sosObs.getObservationConstellation().getObservableProperty().getIdentifier());
            phenComponents = new ArrayList<SosObservableProperty>();
            phenComponents.add((SosObservableProperty) sosObs.getObservationConstellation().getObservableProperty());
        } else if (sosObs.getObservationConstellation().getObservableProperty() instanceof SosCompositePhenomenon) {
            SosCompositePhenomenon compPhen =
                    (SosCompositePhenomenon) sosObs.getObservationConstellation().getObservableProperty();
            xbObs.addNewObservedProperty().setHref(compPhen.getIdentifier());
            phenComponents = compPhen.getPhenomenonComponents();
        }

        // set feature
        encodeFeatureOfInterest(xbObs, sosObs.getObservationConstellation().getFeatureOfInterest(),
                features.get(sosObs.getObservationConstellation().getFeatureOfInterest()));

        // set result
        // add resultDefinition
        XmlObject xbRresult = xbObs.addNewResult();
        DataArrayDocument xb_dataArrayDoc = createDataArrayResult(phenComponents, sosObs);
        xbRresult.set(xb_dataArrayDoc);
        XmlCursor cursor = xbRresult.newCursor();
        cursor.setAttributeText(new QName(W3CConstants.NS_XSI, "type"), "swe:DataArrayPropertyType");
        cursor.dispose();
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
    private DataArrayDocument createDataArrayResult(List<SosObservableProperty> phenComponents, SosObservation sosObs)
            throws OwsExceptionReport {

        // create DataArray
        // DataArrayDocument xbDataArrayDoc =
        // DataArrayDocument.Factory.newInstance(SosXmlOptionsUtility.getInstance().getXmlOptions4Sos2Swe200());
        DataArrayDocument xbDataArrayDoc =
                DataArrayDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        DataArrayType xbDataArray = xbDataArrayDoc.addNewDataArray1();

        // set element count
        CountPropertyType xb_elementCount = xbDataArray.addNewElementCount();
        // TODOD: CHECK SIZE
        int count = 0;
        for (List<SosObservationValue> timeValueTuple : sosObs.getValues().values()) {
            count += timeValueTuple.size();
        }
        xb_elementCount.addNewCount().setValue(BigInteger.valueOf(count));

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
        Map<String, ValueTypes> valueTypes4phens =
                Configurator.getInstance().getCapsCacheController().getKObservablePropertyVValueType();
        for (SosObservableProperty phenComponent : phenComponents) {

            ValueTypes valueType = valueTypes4phens.get(phenComponent.getIdentifier());
            Field xb_field = xb_dataRecord.addNewField();
            if (valueType != null) {
                switch (valueType) {
                case booleanType: {
                    BooleanType xbBool =
                            (BooleanType) xb_field.addNewAbstractDataComponent().substitute(
                                    SWEConstants.QN_BOOLEAN_SWE_200, BooleanType.type);
                    xbBool.setDefinition(phenComponent.getIdentifier());
                    break;
                }
                case countType: {
                    CountType xbCount =
                            (CountType) xb_field.addNewAbstractDataComponent().substitute(
                                    SWEConstants.QN_COUNT_SWE_200, CountType.type);
                    xbCount.setDefinition(phenComponent.getIdentifier());
                    break;
                }
                case numericType: {
                    QuantityType xbQuantity =
                            (QuantityType) xb_field.addNewAbstractDataComponent().substitute(
                                    SWEConstants.QN_QUANTITY_SWE_200, QuantityType.type);
                    xbQuantity.setDefinition(phenComponent.getIdentifier());
                    UnitReference xb_uom = xbQuantity.addNewUom();
                    xb_uom.setCode(phenComponent.getUnit());
                    break;
                }
                case isoTimeType: {
                    TimeType xbTime =
                            (TimeType) xb_field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TIME_SWE_200,
                                    TimeType.type);
                    xbTime.setDefinition(phenComponent.getIdentifier());
                    xbTime.addNewUom().setHref(OMConstants.PHEN_UOM_ISO8601);
                    break;
                }
                case textType: {
                    TextType xbText =
                            (TextType) xb_field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                    TextType.type);
                    xbText.setDefinition(phenComponent.getIdentifier());
                    break;
                }
                case categoryType: {
                    CategoryType xbCategory =
                            (CategoryType) xb_field.addNewAbstractDataComponent().substitute(
                                    SWEConstants.QN_CATEGORY_SWE_200, CategoryType.type);
                    xbCategory.setDefinition(phenComponent.getIdentifier());
                    break;
                }
                default:
                    TextType xbText =
                            (TextType) xb_field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                    TextType.type);
                    xbText.setDefinition(phenComponent.getIdentifier());
                    break;
                }
                String[] uriParts = phenComponent.getIdentifier().split("/|:");
                xb_field.setName(uriParts[uriParts.length - 1]);
            } else {
                xb_field.setName(phenComponent.getIdentifier().replace(SosConstants.PHENOMENON_PREFIX, ""));
                TextType xbText =
                        (TextType) xb_field.addNewAbstractDataComponent().substitute(SWEConstants.QN_TEXT_SWE_200,
                                TextType.type);
                xbText.setDefinition(phenComponent.getIdentifier());
            }

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
        xb_values.newCursor().setTextValue(createResultString(phenComponents, sosObs));

        return xbDataArrayDoc;
    }

    /**
     * creates a result string representing the value matrix of the common
     * observation or getResult response document
     * 
     * @param phenComponents
     *            the phenomenon components of the values of the value matrix
     * @param values
     *            HashMap containing the time as key, and an ArrayList with
     *            pairs of phenomena and values at the key time as values
     * @return String representing the value matrix of the result element
     * @throws OwsExceptionReport
     * @throws ServiceException
     */
    private String createResultString(List<SosObservableProperty> phenComponents, SosObservation sosObs)
            throws OwsExceptionReport {

        // save the position for values of each phenomenon in a hash map with
        // the phen id as key
        // and the postion as value
        HashMap<String, Integer> phenIdsAndValueStringPos = new HashMap<String, Integer>();
        int i = 2;
        for (SosObservableProperty phenComp : phenComponents) {
            phenIdsAndValueStringPos.put(phenComp.getIdentifier(), Integer.valueOf(i));
            i++;
        }

        Map<ITime, Map<String, String>> values = new HashMap<ITime, Map<String, String>>();
        for (SosObservableProperty sosObservableProperty : phenComponents) {
            List<SosObservationValue> timeValueTupel = sosObs.getValues().get(sosObservableProperty.getIdentifier());
            for (SosObservationValue sosObservationValue : timeValueTupel) {

                Map<String, String> map = null;
                if (values.containsKey(sosObservationValue.getPhenomenonTime())) {
                    map = values.get(sosObservationValue.getPhenomenonTime());
                } else {
                    map = new HashMap<String, String>();
                }
                map.put(sosObservableProperty.getIdentifier(),
                        getStringValue(sosObservableProperty.getIdentifier(), sosObservationValue.getValue()));
                values.put(sosObservationValue.getPhenomenonTime(), map);
            }
        }

        String noDataValue = sosObs.getNoDataValue();
        String tokenSeperator = sosObs.getTokenSeparator();
        String tupleSeperator = sosObs.getTupleSeparator();

        // value matrix which should be built
        StringBuffer valueMatrix = new StringBuffer();
        List<ITime> times = new ArrayList<ITime>(values.keySet());
        Collections.sort(times);
        for (ITime time : times) {
            String timeString = getTimeString(time);
            valueMatrix.append(timeString);
            valueMatrix.append(tokenSeperator);
            Map<String, String> map = values.get(time);
            for (SosObservableProperty obsProp : phenComponents) {
                if (map.containsKey(obsProp.getIdentifier())) {
                    String value = map.get(obsProp.getIdentifier());
                    if (value == null || (value != null && value.isEmpty())) {
                        valueMatrix.append(noDataValue);
                    } else {
                        valueMatrix.append(value);
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
     * @param obsPropID
     *            observableProperty
     * @param object
     *            value
     * @return value as text.
     */
    private String getStringValue(String obsPropID, Object object) {
        WKTWriter wktWriter = new WKTWriter();
        Map<String, ValueTypes> valueTypes4phens =
                Configurator.getInstance().getCapsCacheController().getKObservablePropertyVValueType();
        ValueTypes valueType = valueTypes4phens.get(obsPropID);
        switch (valueType) {
        case booleanType:
            return ((Boolean) object).toString();
        case countType:
            return ((Integer) object).toString();
        case numericType:
            return ((Double) object).toString();
        case isoTimeType:
            try {
                return DateTimeHelper.formatDateTime2ResponseString((DateTime) object);
            } catch (OwsExceptionReport owse) {
                LOGGER.info("Value could not be parsed to ISO time stamp!");
                return null;
            }
        case textType:
            return ((String) object);
        case categoryType:
            return ((String) object);
        case spatialType:
            Geometry geom = ((Geometry) object);
            return wktWriter.write(geom) + "#" + geom.getSRID();
        case notDefined:
            if (object instanceof Double) {
                return ((Double) object).toString();
            } else if (object instanceof Boolean) {
                return ((Boolean) object).toString();
            } else if (object instanceof Integer) {
                return ((Integer) object).toString();
            } else if (object instanceof DateTime) {
                try {
                    return DateTimeHelper.formatDateTime2ResponseString((DateTime) object);
                } catch (OwsExceptionReport owse) {
                    LOGGER.info("Value could not be parsed to ISO time stamp!");
                    return null;
                }
            } else if (object instanceof String) {
                return ((String) object);
            } else if (object instanceof Geometry) {
                Geometry geom2 = ((Geometry) object);
                return wktWriter.write(geom2) + "#" + geom2.getSRID();
            }
        default:
            return null;
        }
    }

    private XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
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
    private void encodeFeatureOfInterest(OMObservationType xbObs, String identifier, SosAbstractFeature foi)
            throws OwsExceptionReport {
        String urlPattern =
                SosHelper.getUrlPatternForHttpGetMethod(Configurator.getInstance().getBindingOperators().values(),
                        SosConstants.Operations.GetFeatureOfInterest.name(), Sos2Constants.SERVICEVERSION);
        FeaturePropertyType xbFoiType = xbObs.addNewFeatureOfInterest();
        if (!Configurator.getInstance().isFoiEncodedInObservation()) {
            if (urlPattern != null) {
                xbFoiType.setHref(SosHelper.createFoiGetUrl(foi.getIdentifier(), Sos2Constants.SERVICEVERSION,
                        Configurator.getInstance().getServiceURL(), urlPattern));
            } else {
                xbFoiType.setHref(foi.getIdentifier());
            }
        } else {
            if (foi != null) {
                if (gmlID4sfIdentifier.containsKey(identifier)) {
                    xbFoiType.setHref("#" + gmlID4sfIdentifier.get(identifier));
                } else {
                    String gmlId = "sf_" + sfIdCounter;
                    sfIdCounter++;
                    if (foi instanceof SosSamplingFeature) {
                        SosSamplingFeature sampFeat = (SosSamplingFeature) foi;
                        if (sampFeat.getXmlDescription() != null) {
                            try {
                                xbFoiType.set(XmlObject.Factory.parse(sampFeat.getXmlDescription()));
                                gmlID4sfIdentifier.put(identifier, gmlId);
                            } catch (XmlException xmle) {
                                OwsExceptionReport owse = new OwsExceptionReport(xmle);
                                LOGGER.error("ERROR encoder", owse);
                                throw owse;
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
                                gmlID4sfIdentifier.put(identifier, gmlId);
                            } else {
                                String exceptionText =
                                        "Error while encoding geometry for featureOfInterest, needed encoder is missing!";
                                LOGGER.debug(exceptionText);
                                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                            }
                        }

                    }
                }
            } else {
                if (urlPattern != null) {
                    xbFoiType.setHref(SosHelper.createFoiGetUrl(identifier, Sos2Constants.SERVICEVERSION, Configurator
                            .getInstance().getServiceURL(), urlPattern));
                } else {
                    xbFoiType.setHref(identifier);
                }
            }
        }
    }
}
