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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.om.x20.NamedValueType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.convert.Converter;
import org.n52.sos.convert.ConverterException;
import org.n52.sos.convert.ConverterRepository;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.NamedValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.OmCompositePhenomenon;
import org.n52.sos.ogc.om.OmObservableProperty;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.om.values.Value;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.profile.Profile;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.GmlHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;

/**
 * Abstract Observation & Measurement 2.0 encoder should be extended by all O&M
 * subclasses.
 * 
 * Contains encoding for - Observation - NamedValue
 * 
 * @author CarstenHollmann
 * @since 4.0
 * 
 */
public abstract class AbstractOmEncoderv20 implements ObservationEncoder<XmlObject, Object> {

    /**
     * Method to create the om:result element content
     * 
     * @param sosObservation
     *            SosObservation to be encoded
     * @return XML encoded result object, e.g a gml:MeasureType
     * @throws OwsExceptionReport
     *             if an error occurs
     */
    protected abstract XmlObject createResult(OmObservation sosObservation)
            throws OwsExceptionReport;

    /**
     * Method to add the observation type to the om:Observation. Subclasses
     * should have mappings to set the correct type, e.g. O&M .../Measurement ==
     * .../MeasurementTimeseriesTVPObservation in WaterML 2.0
     * 
     * @param xbObservation
     *            XmlBeans object of observation
     * @param observationType
     *            Observation type
     */
    protected abstract void addObservationType(OMObservationType xbObservation, String observationType);

    /**
     * Get the default encoding Namespace for FeatureOfInterest
     * 
     * @return Encoding namespace
     */
    protected abstract String getDefaultFeatureEncodingNamespace();

    /**
     * Get the default encoding Namespace for Procedures
     * 
     * @return Encoding namespace
     */
    protected abstract String getDefaultProcedureEncodingNamspace();

    /**
     * Indicator whether the procedure is to be encoded
     * 
     * @return Indicator
     */
    protected abstract boolean convertEncodedProcedure();

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(element, new EnumMap<HelperValues, String>(HelperValues.class));
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport,
            UnsupportedEncoderInputException {
        if (element instanceof OmObservation) {
            return createOmObservation((OmObservation) element, additionalValues);
        } else if (element instanceof NamedValue) {
            return createNamedValue((NamedValue) element);
        } else {
            throw new UnsupportedEncoderInputException(this, element);
        }
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(OMConstants.NS_OM_2, OMConstants.NS_OM_PREFIX);
    }

    /**
     * Method to create an O&M 2.0 observation XmlBeans object
     * 
     * @param sosObservation
     *            SosObservation to be encoded
     * @param additionalValues
     *            Additional values which are used during the encoding
     * @return XmlBeans representation of O&M 2.0 observation
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    protected XmlObject createOmObservation(OmObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        OMObservationType xbObservation =
                OMObservationType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        // set a unique gml:id
        xbObservation.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID;
        if (sosObservation.isSetObservationID()) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObservation.getId().replace("o_", "");
            sosObservation.setObservationID(observationID);
        }
        // set observation identifier if available
        if (sosObservation.isSetIdentifier()) {
            Encoder<?, CodeWithAuthority> encoder =
                    CodingRepository.getInstance().getEncoder(
                            CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, sosObservation.getIdentifier()));
            if (encoder != null) {
                XmlObject xmlObject = (XmlObject) encoder.encode(sosObservation.getIdentifier());
                xbObservation.addNewIdentifier().set(xmlObject);
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("Error while encoding geometry value, needed encoder is missing!");
            }
        }

        // add observationType if set
        addObservationType(xbObservation, sosObservation.getObservationConstellation().getObservationType());

        // set phenomenonTime
        Time phenomenonTime = sosObservation.getPhenomenonTime();
        if (phenomenonTime.getGmlId() == null) {
            phenomenonTime.setGmlId(OMConstants.PHENOMENON_TIME_NAME + "_" + observationID);
        }
        addPhenomenonTime(xbObservation.addNewPhenomenonTime(), phenomenonTime);

        // set resultTime
        addResultTime(xbObservation, sosObservation);

        // set procedure
        addProcedure(xbObservation.addNewProcedure(), sosObservation.getObservationConstellation().getProcedure(),
                observationID);
        // set observedProperty (phenomenon)
        List<OmObservableProperty> phenComponents;
        if (sosObservation.getObservationConstellation().getObservableProperty() instanceof OmObservableProperty) {
            xbObservation.addNewObservedProperty().setHref(
                    sosObservation.getObservationConstellation().getObservableProperty().getIdentifier());
            phenComponents = new ArrayList<OmObservableProperty>(1);
            phenComponents.add((OmObservableProperty) sosObservation.getObservationConstellation()
                    .getObservableProperty());
        } else if (sosObservation.getObservationConstellation().getObservableProperty() instanceof OmCompositePhenomenon) {
            OmCompositePhenomenon compPhen =
                    (OmCompositePhenomenon) sosObservation.getObservationConstellation().getObservableProperty();
            xbObservation.addNewObservedProperty().setHref(compPhen.getIdentifier());
            phenComponents = compPhen.getPhenomenonComponents();
        }
        // set feature
        xbObservation.addNewFeatureOfInterest().set(
                addFeatureOfInterest(sosObservation.getObservationConstellation().getFeatureOfInterest()));
        // set result
        XmlObject createResult = createResult(sosObservation);
        XmlObject addNewResult = xbObservation.addNewResult();
        if (createResult != null) {
            addNewResult.set(createResult(sosObservation));
        }
        return xbObservation;
    }

    /**
     * Method that adds the procedure as reference or as encoded object to the
     * XML observation object
     * 
     * @param procedure
     *            XML process type
     * @param procedureDescription
     *            SosProcedureDescription to be encoded
     * @param observationID
     *            GML observation id.
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private void addProcedure(OMProcessPropertyType procedure, SosProcedureDescription procedureDescription,
            String observationID) throws OwsExceptionReport {
        if (checkEncodProcedureForEncoderKeys()) {
            SosProcedureDescription procedureToEncode = null;
            // should the procedure be converted
            if (convertEncodedProcedure()) {
                Converter<?, SosProcedureDescription> converter =
                        ConverterRepository.getInstance().getConverter(procedureDescription.getDescriptionFormat(),
                                getDefaultProcedureEncodingNamspace());
                if (converter != null) {
                    try {
                        procedureToEncode = (SosProcedureDescription) converter.convert(procedureDescription);
                    } catch (ConverterException e) {
                        throw new NoApplicableCodeException()
                                .withMessage("Error while converting procedureDescription!");
                    }
                }
            } else {
                procedureToEncode = procedureDescription;
            }
            // encode procedure or add reference
            XmlObject encodedProcedure =
                    CodingHelper.encodeObjectToXml(procedureToEncode.getDescriptionFormat(), procedureToEncode);
            if (encodedProcedure != null) {
                procedure.set(encodedProcedure);
            } else {
                procedure.setHref(procedureDescription.getIdentifier());
            }
        } else {
            procedure.setHref(procedureDescription.getIdentifier());
        }
    }

    /**
     * Method to check whether the procedure should be encoded
     * 
     * @return True or false
     */
    private boolean checkEncodProcedureForEncoderKeys() {
        Set<EncoderKey> encoderKeyType = getEncoderKeyType();
        for (EncoderKey encoderKey : encoderKeyType) {
            if (encoderKey instanceof XmlEncoderKey) {
                if (Configurator.getInstance().getProfileHandler().getActiveProfile()
                        .isEncodeProcedureInObservation(((XmlEncoderKey) encoderKey).getNamespace())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method to add the phenomenon time to the XML observation object
     * 
     * @param timeObjectPropertyType
     *            XML time object from XML observation object
     * @param time
     *            SOS phenomenon time representation
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    private void addPhenomenonTime(TimeObjectPropertyType timeObjectPropertyType, Time time)
            throws OwsExceptionReport {
        Encoder<?, Time> encoder =
                CodingRepository.getInstance().getEncoder(CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, time));
        if (encoder != null) {
            XmlObject xmlObject = (XmlObject) encoder.encode(time);
            XmlObject substitution =
                    timeObjectPropertyType.addNewAbstractTimeObject().substitute(
                            GmlHelper.getGml321QnameForITime(time), xmlObject.schemaType());
            substitution.set(xmlObject);
        } else {
            throw new NoApplicableCodeException()
                    .withMessage("Error while encoding phenomenon time, needed encoder is missing!");
        }
    }

    /**
     * Method to add the result time to the XML observation object
     * 
     * @param xbObs
     *            XML observation object
     * @param sosObservation
     *            SOS observation object
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    private void addResultTime(OMObservationType xbObs, OmObservation sosObservation) throws OwsExceptionReport {
        TimeInstant resultTime = sosObservation.getResultTime();
        Time phenomenonTime = sosObservation.getPhenomenonTime();
        // get result time from SOS result time representation
        if (sosObservation.getResultTime() != null) {
            if (resultTime.equals(phenomenonTime)) {
                xbObs.addNewResultTime().setHref("#" + phenomenonTime.getGmlId());
            } else {
                addResultTime(xbObs, resultTime);
            }
        }
        // if result time is not set, get result time from phenomenon time
        // representation
        else {
            if (phenomenonTime instanceof TimeInstant) {
                xbObs.addNewResultTime().setHref("#" + phenomenonTime.getGmlId());
            } else if (phenomenonTime instanceof TimePeriod) {
                TimeInstant rsTime = new TimeInstant(((TimePeriod) sosObservation.getPhenomenonTime()).getEnd());
                addResultTime(xbObs, rsTime);
            }
        }
    }

    /**
     * Method to add the result time to the XML observation object
     * 
     * @param xbObs
     *            XML observation object
     * @param time
     *            SOS result time representation
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    private void addResultTime(OMObservationType xbObs, TimeInstant time) throws OwsExceptionReport {
        XmlObject xmlObject = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, time);
        xbObs.addNewResultTime().addNewTimeInstant().set(xmlObject);
        XmlObject substitution =
                xbObs.getResultTime().getTimeInstant()
                        .substitute(GmlHelper.getGml321QnameForITime(time), xmlObject.schemaType());
        substitution.set(xmlObject);
    }

    /**
     * Method to add the featureOfInterest to the XML observation object
     * 
     * @param feature
     *            SOS feature representation
     * @return Encoded featureOfInterest
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    private XmlObject addFeatureOfInterest(AbstractFeature feature) throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(HelperValues.class);
        Profile activeProfile = Configurator.getInstance().getProfileHandler().getActiveProfile();
        additionalValues.put(HelperValues.ENCODE,
                Boolean.toString(activeProfile.isEncodeFeatureOfInterestInObservations()));
        if (StringHelper.isNotEmpty(activeProfile.getEncodingNamespaceForFeatureOfInterest())) {
            additionalValues.put(HelperValues.ENCODE_NAMESPACE,
                    activeProfile.getEncodingNamespaceForFeatureOfInterest());
        } else {
            additionalValues.put(HelperValues.ENCODE_NAMESPACE, getDefaultFeatureEncodingNamespace());
        }
        return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, feature, additionalValues);
    }

    /**
     * Method to encode a SOS NamedValue to an XmlBeans representation
     * 
     * @param sosNamedValue
     *            SOS NamedValue
     * @return XmlBeans object
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    protected XmlObject createNamedValue(NamedValue<?> sosNamedValue) throws OwsExceptionReport {
        // encode value (any)
        XmlObject namedValuePropertyValue = getNamedValueValue(sosNamedValue.getValue());
        if (namedValuePropertyValue != null) {
            NamedValueType xbNamedValue =
                    NamedValueType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            // encode gml:ReferenceType
            XmlObject encodeObjectToXml =
                    CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, sosNamedValue.getName());
            xbNamedValue.addNewName().set(encodeObjectToXml);
            // set value (any)
            xbNamedValue.setValue(namedValuePropertyValue);
            return xbNamedValue;
        }
        return null;
    }

    /**
     * Get the XmlBeans object for SOS value
     * 
     * @param value
     *            SOS value object
     * @return XmlBeans object
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    private XmlObject getNamedValueValue(Value<?> value) throws OwsExceptionReport {
        if (value instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) value;
            XmlBoolean xbBoolean = XmlBoolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (booleanValue.getValue() != null) {
                xbBoolean.setBooleanValue(booleanValue.getValue());
                return xbBoolean;
            }
        } else if (value instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) value;
            if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
                return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, categoryValue);
            }
        } else if (value instanceof CountValue) {
            CountValue countValue = (CountValue) value;
            XmlInteger xbInteger = XmlInteger.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (countValue.getValue() != null && countValue.getValue() != Integer.MIN_VALUE) {
                xbInteger.setBigIntegerValue(new BigInteger(countValue.getValue().toString()));
                return xbInteger;
            }
        } else if (value instanceof GeometryValue) {
            GeometryValue geometryValue = (GeometryValue) value;
            if (geometryValue.getValue() != null) {
                return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, geometryValue.getValue());
            }
        } else if (value instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) value;
            return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, quantityValue);
        } else if (value instanceof TextValue) {
            TextValue textValue = (TextValue) value;
            XmlString xbString = XmlString.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbString.setStringValue(textValue.getValue());
            return xbString;
        }
        return null;
    }
}
