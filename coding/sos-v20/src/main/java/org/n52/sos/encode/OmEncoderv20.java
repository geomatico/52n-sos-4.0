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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.opengis.om.x20.OMObservationType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.NamedValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.SweHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import net.opengis.gml.x32.FeaturePropertyType;

public class OmEncoderv20 extends AbstractOmEncoderv20 {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OmEncoderv20.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(OMConstants.NS_OM_2,
            SosObservation.class, NamedValue.class);

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
    public boolean isObservationAndMeasurmentV20Type() {
        return true;
    }

    @Override
    public Set<String> getSupportedResponseFormats(String service, String version) {
        if (SUPPORTED_RESPONSE_FORMATS.get(service) != null
                && SUPPORTED_RESPONSE_FORMATS.get(service).get(version) != null) {
            return SUPPORTED_RESPONSE_FORMATS.get(service).get(version);
        }
        return new HashSet<String>(0);
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
    protected XmlObject createResult(SosObservation sosObservation) throws OwsExceptionReport {
        // TODO if OM_SWEArrayObservation and get ResultEncoding and
        // ResultStructure exists,
        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
            return createSingleObservationToResult(sosObservation);
        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
            return createMultiObservationValueToResult(sosObservation);
        }
        return null;
    }

    @Override
    protected void addObservationType(OMObservationType xbObservation, String observationType) {
        if (StringHelper.isNotEmpty(observationType)) {
            xbObservation.addNewType().setHref(observationType);
        }
    }

    @Override
    protected String getDefaultFeatureEncodingNamespace() {
        return SFConstants.NS_SAMS;
    }

    @Override
    protected String getDefaultProcedureEncodingNamspace() {
        return SensorMLConstants.NS_SML;
    }

    @Override
    protected boolean convertEncodedProcedure() {
        return false;
    }

    private XmlObject createCompositePhenomenon(String compPhenId, Collection<String> phenComponents) {
        // Currently not used for SOS 2.0 and OM 2.0 encoding.
        return null;
    }

    // FIXME String.equals(QName)!?
    /**
     * Create a om:result content XmlBeans object from a SOS single value
     * observation object
     * 
     * @param sosObservation
     *            SOS observation
     * @return XmlBeans object for om:result
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private XmlObject createSingleObservationToResult(SosObservation sosObservation) throws OwsExceptionReport {
        SosSingleObservationValue<?> observationValue = (SosSingleObservationValue) sosObservation.getValue();
        String observationType = "";
        if (sosObservation.getObservationConstellation().isSetObservationType()) {
            observationType = sosObservation.getObservationConstellation().getObservationType();
        } else {
            observationType = OMHelper.getObservationTypeFor(observationValue.getValue());
        }
        if ((observationType.equals(OMConstants.OBS_TYPE_MEASUREMENT))
                && observationValue.getValue() instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) observationValue.getValue();
            return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, quantityValue);
        } else if ((observationType.equals(OMConstants.OBS_TYPE_COUNT_OBSERVATION))
                && observationValue.getValue() instanceof CountValue) {
            CountValue countValue = (CountValue) observationValue.getValue();
            XmlInteger xbInteger = XmlInteger.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (countValue.getValue() != null && countValue.getValue() != Integer.MIN_VALUE) {
                xbInteger.setBigIntegerValue(new BigInteger(countValue.getValue().toString()));
            } else {
                xbInteger.setNil();
            }
            return xbInteger;
        } else if ((observationType.equals(OMConstants.OBS_TYPE_TEXT_OBSERVATION))
                && observationValue.getValue() instanceof TextValue) {
            TextValue textValue = (TextValue) observationValue.getValue();
            XmlString xbString = XmlString.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (textValue.getValue() != null && !textValue.getValue().isEmpty()) {
                xbString.setStringValue(textValue.getValue());
            } else {
                xbString.setNil();
            }
            return xbString;
        } else if ((observationType.equals(OMConstants.OBS_TYPE_TRUTH_OBSERVATION))
                && observationValue.getValue() instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) observationValue.getValue();
            XmlBoolean xbBoolean = XmlBoolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (booleanValue.getValue() != null) {
                xbBoolean.setBooleanValue(booleanValue.getValue());
            } else {
                xbBoolean.setNil();
            }
            return xbBoolean;
        } else if ((observationType.equals(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION))
                && observationValue.getValue() instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) observationValue.getValue();
            if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
                Map<HelperValues, String> additionalValue = new EnumMap<HelperValues, String>(HelperValues.class);
                additionalValue
                        .put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + sosObservation.getObservationID());
                XmlObject xmlObject =
                        CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, categoryValue, additionalValue);
                return xmlObject;
            } else {
                return null;
            }
        } else if ((observationType.equals(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION))
                && observationValue.getValue() instanceof GeometryValue) {

            GeometryValue geometryValue = (GeometryValue) observationValue.getValue();
            if (geometryValue.getValue() != null) {
                Map<HelperValues, String> additionalValue = new EnumMap<HelperValues, String>(HelperValues.class);
                additionalValue
                        .put(HelperValues.GMLID, SosConstants.OBS_ID_PREFIX + sosObservation.getObservationID());
                XmlObject xmlObject =
                        CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, geometryValue.getValue(),
                                additionalValue);
                return xmlObject;
            } else {
                return null;
            }
        } else if (observationType.equals(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION)) {
            // TODO create SosSweDataArray
            SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
            Map<HelperValues, String> additionalValues =
                    new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
            additionalValues.put(HelperValues.FOR_OBSERVATION, null);
            // TODO create SosSweDataArray
            Object encodedObj = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_20, dataArray, additionalValues);
            if (encodedObj instanceof XmlObject) {
                return (XmlObject) encodedObj;
            } else {
                throw new NoApplicableCodeException().withMessage(
                        "Encoding of observation value of type \"%s\" failed. Result: %s",
                        observationValue.getValue() != null ? observationValue.getValue().getClass().getName()
                                : observationValue.getValue(), encodedObj != null ? encodedObj.getClass().getName()
                                : encodedObj);
            }
        }
        return null;
    }

    /**
     * Create a om:result content XmlBeans object from a SOS multi value
     * observation object
     * 
     * @param sosObservation
     *            SOS observation
     * @return XmlBeans object for om:result
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private XmlObject createMultiObservationValueToResult(SosObservation sosObservation) throws OwsExceptionReport {
        SosMultiObservationValues<?> observationValue = (SosMultiObservationValues) sosObservation.getValue();
        // TODO create SosSweDataArray
        SosSweDataArray dataArray = SweHelper.createSosSweDataArrayFromObservationValue(sosObservation);
        Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(SosConstants.HelperValues.class);
        additionalValues.put(HelperValues.FOR_OBSERVATION, null);
        Object encodedObj = CodingHelper.encodeObjectToXml(SWEConstants.NS_SWE_20, dataArray, additionalValues);
        if (encodedObj instanceof XmlObject) {
            return (XmlObject) encodedObj;
        } else {
            throw new NoApplicableCodeException().withMessage(
                    "Encoding of observation value of type \"%s\" failed. Result: %s",
                    observationValue.getValue() != null ? observationValue.getValue().getClass().getName()
                            : observationValue.getValue(), encodedObj != null ? encodedObj.getClass().getName()
                            : encodedObj);
        }
    }
}
