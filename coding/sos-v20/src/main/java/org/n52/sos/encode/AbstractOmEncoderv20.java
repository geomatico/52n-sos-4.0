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
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.NamedValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosCompositePhenomenon;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
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

public abstract class AbstractOmEncoderv20 implements ObservationEncoder<XmlObject, Object> {

    protected abstract XmlObject createResult(SosObservation sosObservation, String phenomenonTimeID)
            throws OwsExceptionReport;

    protected abstract void addObservationType(OMObservationType xbObservation, String observationType);
    
    protected abstract String getDefaultFeatureEncodingNamespace();
    
    protected abstract String getDefaultProcedureEncodingNamspace();
    
    protected abstract boolean convertEncodedProcedure();
    
    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport, UnsupportedEncoderInputException {
        return encode(element, new EnumMap<HelperValues, String>(HelperValues.class));
    }
    
    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport, UnsupportedEncoderInputException {
        if (element instanceof SosObservation) {
            return createOmObservation((SosObservation) element, additionalValues);
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
    
    protected XmlObject createOmObservation(SosObservation sosObservation, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        OMObservationType xbObservation =
                OMObservationType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        xbObservation.setId("o_" + Long.toString(System.currentTimeMillis()));
        String observationID;
        if (sosObservation.isSetObservationID()) {
            observationID = sosObservation.getObservationID();
        } else {
            observationID = xbObservation.getId().replace("o_", "");
            sosObservation.setObservationID(observationID);
        }
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

        addObservationType(xbObservation, sosObservation.getObservationConstellation().getObservationType());

        // set phenomenonTime
        ITime phenomenonTime = sosObservation.getPhenomenonTime();
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
        List<SosObservableProperty> phenComponents;
        if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosObservableProperty) {
            xbObservation.addNewObservedProperty().setHref(
                    sosObservation.getObservationConstellation().getObservableProperty().getIdentifier());
            phenComponents = new ArrayList<SosObservableProperty>(1);
            phenComponents.add((SosObservableProperty) sosObservation.getObservationConstellation()
                    .getObservableProperty());
        } else if (sosObservation.getObservationConstellation().getObservableProperty() instanceof SosCompositePhenomenon) {
            SosCompositePhenomenon compPhen =
                    (SosCompositePhenomenon) sosObservation.getObservationConstellation().getObservableProperty();
            xbObservation.addNewObservedProperty().setHref(compPhen.getIdentifier());
            phenComponents = compPhen.getPhenomenonComponents();
        }
        // set feature
        addFeatureOfInterest(xbObservation, sosObservation.getObservationConstellation().getFeatureOfInterest());
        // set result
        XmlObject createResult = createResult(sosObservation, phenomenonTime.getGmlId());
        XmlObject addNewResult = xbObservation.addNewResult();
        if (createResult != null) {
            addNewResult.set(createResult(sosObservation, phenomenonTime.getGmlId()));
        }
        return xbObservation;
    }

    private void addProcedure(OMProcessPropertyType procedure, SosProcedureDescription procedureDescription,
            String observationID) throws OwsExceptionReport {
        if (checkEncodProcedureForEncoderKeys()) {
            SosProcedureDescription procedureToEncode = null;
            if (convertEncodedProcedure()) {
                Converter<?, SosProcedureDescription> converter = ConverterRepository.getInstance()
                        .getConverter(procedureDescription.getDescriptionFormat(), getDefaultProcedureEncodingNamspace());
                if (converter != null) {
                    try {
                        procedureToEncode = (SosProcedureDescription)converter.convert(procedureDescription);
                    } catch (ConverterException e) {
                        throw new NoApplicableCodeException().withMessage("Error while converting procedureDescription!");
                    }
                }
            } else {
                procedureToEncode = procedureDescription;
            }
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

    private void addPhenomenonTime(TimeObjectPropertyType timeObjectPropertyType, ITime iTime)
            throws OwsExceptionReport {
        Encoder<?, ITime> encoder =
                CodingRepository.getInstance().getEncoder(CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, iTime));
        if (encoder != null) {
            XmlObject xmlObject = (XmlObject) encoder.encode(iTime);
            XmlObject substitution =
                    timeObjectPropertyType.addNewAbstractTimeObject().substitute(
                            GmlHelper.getGml321QnameForITime(iTime), xmlObject.schemaType());
            substitution.set(xmlObject);
        } else {
            throw new NoApplicableCodeException()
                    .withMessage("Error while encoding phenomenon time, needed encoder is missing!");
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
                        .substitute(GmlHelper.getGml321QnameForITime(iTime), xmlObject.schemaType());
        substitution.set(xmlObject);
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
    private void addFeatureOfInterest(OMObservationType observation, SosAbstractFeature feature)
            throws OwsExceptionReport {
        Map<HelperValues, String> additionalValues =
                new EnumMap<SosConstants.HelperValues, String>(HelperValues.class);
        Profile activeProfile = Configurator.getInstance().getProfileHandler().getActiveProfile();
        additionalValues.put(HelperValues.ENCODE,
                Boolean.toString(activeProfile.isEncodeFeatureOfInterestInObservations()));
        if (StringHelper.isNotEmpty(activeProfile.getEncodingNamespaceForFeatureOfInterest())) {
            additionalValues.put(HelperValues.ENCODE_NAMESPACE, activeProfile.getEncodingNamespaceForFeatureOfInterest());
        } else {
            additionalValues.put(HelperValues.ENCODE_NAMESPACE, getDefaultFeatureEncodingNamespace());
        }
        XmlObject encodeObjectToXml =
                CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, feature, additionalValues);
        observation.addNewFeatureOfInterest().set(encodeObjectToXml);

    }

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

    private XmlObject getNamedValueValue(Value<?> iValue) throws OwsExceptionReport {
        if (iValue instanceof BooleanValue) {
            BooleanValue booleanValue = (BooleanValue) iValue;
            XmlBoolean xbBoolean = XmlBoolean.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (booleanValue.getValue() != null) {
                xbBoolean.setBooleanValue(booleanValue.getValue());
                return xbBoolean;
            }
        } else if (iValue instanceof CategoryValue) {
            CategoryValue categoryValue = (CategoryValue) iValue;
            if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
                return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, categoryValue);
            }
        } else if (iValue instanceof CountValue) {
            CountValue countValue = (CountValue) iValue;
            XmlInteger xbInteger = XmlInteger.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            if (countValue.getValue() != null && countValue.getValue() != Integer.MIN_VALUE) {
                xbInteger.setBigIntegerValue(new BigInteger(countValue.getValue().toString()));
                return xbInteger;
            }
        } else if (iValue instanceof GeometryValue) {
            GeometryValue geometryValue = (GeometryValue) iValue;
            if (geometryValue.getValue() != null) {
                return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, geometryValue.getValue());
            }
        } else if (iValue instanceof QuantityValue) {
            QuantityValue quantityValue = (QuantityValue) iValue;
            return CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, quantityValue);
        } else if (iValue instanceof TextValue) {
            TextValue textValue = (TextValue) iValue;
            XmlString xbString = XmlString.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbString.setStringValue(textValue.getValue());
            return xbString;
        }
        return null;
    }
}
