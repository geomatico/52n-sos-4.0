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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.om.x20.OMObservationType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.IObservationValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.NilTemplateValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmDecoderv20 implements IDecoder<SosObservation, OMObservationType> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OmDecoderv20.class);

    private List<DecoderKeyType> decoderKeyTypes;

    private Set<String> supportedObservationTypes;

    public OmDecoderv20() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>(1);
        decoderKeyTypes.add(new DecoderKeyType(OMConstants.NS_OM_2));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        supportedObservationTypes = new HashSet<String>(0);
        supportedObservationTypes.add(OMConstants.OBS_TYPE_CATEGORY_OBSERVATION);
        supportedObservationTypes.add(OMConstants.OBS_TYPE_COUNT_OBSERVATION);
        // supportedObservationTypes.add(OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION);
        supportedObservationTypes.add(OMConstants.OBS_TYPE_MEASUREMENT);
        supportedObservationTypes.add(OMConstants.OBS_TYPE_TEXT_OBSERVATION);
        supportedObservationTypes.add(OMConstants.OBS_TYPE_TRUTH_OBSERVATION);
        supportedObservationTypes.add(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        Map<SupportedTypeKey, Set<String>> map = new EnumMap<SupportedTypeKey, Set<String>>(SupportedTypeKey.class);
        map.put(SupportedTypeKey.ObservationType, supportedObservationTypes);
        return map;
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/measurement");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/categoryObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/countObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/truthObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/geometryObservation");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/textObservation");
        return conformanceClasses;
    }

    @Override
    public SosObservation decode(OMObservationType omObservation) throws OwsExceptionReport {
        Map<String, SosAbstractFeature> featureMap = new HashMap<String, SosAbstractFeature>();
        SosObservation sosObservation = new SosObservation();
        sosObservation.setIdentifier(getIdentifier(omObservation));
        SosObservationConstellation observationConstallation = getObservationConstellation(omObservation);
        sosObservation.setObservationConstellation(observationConstallation);
        sosObservation.setResultTime(getResultTime(omObservation));
        sosObservation.setValidTime(getValidTime(omObservation));
        sosObservation.setValue(getObservationValue(omObservation));
        checkOrSetObservationType(sosObservation);
        try {
            SosAbstractFeature featureOfInterest = getFeatureOfInterest(omObservation.getFeatureOfInterest());
            observationConstallation.setFeatureOfInterest(checkFeatureWithMap(featureOfInterest, featureMap));
        } catch (OwsExceptionReport e) {
            if (sosObservation.getValue().getPhenomenonTime().getIndeterminateValue().equals("template")) {
                for (OwsException exception : e.getExceptions()) {
                    if (exception.getCode().equals(OwsExceptionCode.InvalidParameterValue)) {
                       throw Util4Exceptions.createInvalidParameterValueException(exception.getLocator(), exception.getMessages()[0]);
                    } else if (exception.getCode().equals(OwsExceptionCode.MissingParameterValue)) {
                        throw Util4Exceptions.createMissingParameterValueException(exception.getLocator());
                    }
                }
            } 
            throw e;
        }
        // TODO: later for spatial filtering profile
        // omObservation.getParameterArray();

        return sosObservation;
    }

    private CodeWithAuthority getIdentifier(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.getIdentifier() != null) {
            String namespace = XmlHelper.getNamespace(omObservation.getPhenomenonTime().getAbstractTimeObject());
            List<IDecoder> decoderList = Configurator.getInstance().getDecoder(XmlHelper.getNamespace(omObservation.getIdentifier()));
            if (decoderList != null) {
                for (IDecoder decoder : decoderList) {
                    Object decodedObject = decoder.decode(omObservation.getIdentifier());
                    if (decodedObject != null && decodedObject instanceof CodeWithAuthority) {
                        return (CodeWithAuthority) decodedObject;
                    }
                }
            }
        }
        return null;
    }

    private SosObservationConstellation getObservationConstellation(OMObservationType omObservation)
            throws OwsExceptionReport {
        SosObservationConstellation observationConstellation = new SosObservationConstellation();
        observationConstellation.setObservationType(getObservationType(omObservation));
        observationConstellation.setProcedure(getProcedure(omObservation));
        observationConstellation.setObservableProperty(getObservableProperty(omObservation));
        return observationConstellation;
    }

    private String getObservationType(OMObservationType omObservation) {
        if (omObservation.getType() != null) {
            return omObservation.getType().getHref();
        }
        return null;

    }

    private String getProcedure(OMObservationType omObservation) {
        if (omObservation.getProcedure() != null) {
            return omObservation.getProcedure().getHref();
        }
        return null;
    }

    private AbstractSosPhenomenon getObservableProperty(OMObservationType omObservation) {
        if (omObservation.getObservedProperty() != null) {
            return new SosObservableProperty(omObservation.getObservedProperty().getHref());
        }
        return null;
    }

    private SosAbstractFeature getFeatureOfInterest(FeaturePropertyType featureOfInterest) throws OwsExceptionReport {
        SosSamplingFeature feature = null;
        // if xlink:href is set
        if (featureOfInterest.getHref() != null) {
            if (featureOfInterest.getHref().isEmpty()) {
                String exceptionText = "The requested featureOfInterest has a missing xlink:href definition!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createMissingParameterValueException(
                        Sos2Constants.InsertObservationParams.observation.name());
            }
                if (featureOfInterest.getHref().startsWith("#")) {
                    feature = new SosSamplingFeature(null, featureOfInterest.getHref().replace("#", ""));
                } else {
                    SosHelper.checkHref(featureOfInterest.getHref(), Sos2Constants.InsertObservationParams.observation.name());
                    feature = new SosSamplingFeature(featureOfInterest.getHref());
                    if (featureOfInterest.getTitle() != null && !featureOfInterest.getTitle().isEmpty()) {
                        feature.addName(featureOfInterest.getTitle());
                    }
                }
        }
        // if feature is encoded
        else {
            XmlObject abstractFeature = null;
            if (featureOfInterest.getAbstractFeature() != null) {
                abstractFeature = featureOfInterest.getAbstractFeature();
            } else if (featureOfInterest.getDomNode().hasChildNodes()) {
                try {
                    abstractFeature =
                            XmlObject.Factory.parse(XmlHelper.getNodeFromNodeList(featureOfInterest.getDomNode()
                                    .getChildNodes()));
                } catch (XmlException xmle) {
                    String exceptionText = "Error while parsing feature request!";
                    LOGGER.error(exceptionText, xmle);
                    throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                }
            }
            if (abstractFeature != null) {
                String namespace = XmlHelper.getNamespace(abstractFeature);
                List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
                if (decoderList != null) {
                    for (IDecoder decoder : decoderList) {
                        Object decodedObject = decoder.decode(abstractFeature);
                        if (decodedObject != null && decodedObject instanceof SosSamplingFeature) {
                            feature = (SosSamplingFeature) decodedObject;
                        }
                    }
                } else {
                    String exceptionText = "The requested featureOfInterest type is not supported by this service!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
                }
            }
        }
        if (feature == null) {
            String exceptionText = "The requested featureOfInterest type is not supported by this service!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
        }
        return feature;
    }

    private ITime getPhenomenonTime(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.getPhenomenonTime().isSetNilReason()
                && omObservation.getPhenomenonTime().getNilReason() instanceof String
                && ((String) omObservation.getPhenomenonTime().getNilReason()).equals("template")) {
            TimeInstant timeInstant = new TimeInstant();
            timeInstant.setIndeterminateValue((String) omObservation.getPhenomenonTime().getNilReason());
            return timeInstant;
        } else {
            String namespace = XmlHelper.getNamespace(omObservation.getPhenomenonTime().getAbstractTimeObject());
            List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
            if (decoderList != null) {
                for (IDecoder decoder : decoderList) {
                    Object decodedObject = decoder.decode(omObservation.getPhenomenonTime().getAbstractTimeObject());
                    if (decodedObject != null && decodedObject instanceof ITime) {
                        return (ITime) decodedObject;
                    }
                }
            }
        }
        String exceptionText = "The requested phenomenonTime type is not supported by this service!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createInvalidParameterValueException(
                Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
    }

    private TimeInstant getResultTime(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.getResultTime().isSetHref()) {
        	TimeInstant timeInstant = new TimeInstant();
        	if (omObservation.getResultTime().getHref().charAt(0) == '#') {
        		// document internal link
        		// TODO parse linked element
        		timeInstant.setIndeterminateValue(Sos2Constants.EN_PHENOMENON_TIME);
        	}
        	else {
        		timeInstant.setIndeterminateValue(omObservation.getResultTime().getHref());
        	}
        	return timeInstant;
        } else if (omObservation.getResultTime().isSetNilReason()
                && omObservation.getResultTime().getNilReason() instanceof String
                && ((String) omObservation.getResultTime().getNilReason()).equals("template")) {
            TimeInstant timeInstant = new TimeInstant();
            timeInstant.setIndeterminateValue((String) omObservation.getResultTime().getNilReason());
            return timeInstant;
        } else {
            String namespace = XmlHelper.getNamespace(omObservation.getResultTime().getTimeInstant());
            List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
            if (decoderList != null) {
                for (IDecoder decoder : decoderList) {
                    Object decodedObject = decoder.decode(omObservation.getResultTime().getTimeInstant());
                    if (decodedObject != null && decodedObject instanceof TimeInstant) {
                        return (TimeInstant) decodedObject;
                    }
                }
            }
            String exceptionText = "The requested resultTime type is not supported by this service!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
        }
    }

    private TimePeriod getValidTime(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.isSetValidTime()) {
            String namespace = XmlHelper.getNamespace(omObservation.getValidTime().getTimePeriod());
            List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
            if (decoderList != null) {
                for (IDecoder decoder : decoderList) {
                    Object decodedObject = decoder.decode(omObservation.getValidTime().getTimePeriod());
                    if (decodedObject != null && decodedObject instanceof TimePeriod) {
                        return (TimePeriod) decodedObject;
                    }
                }
            }
            String exceptionText = "The requested validTime type is not supported by this service!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
        }
        return null;
    }

    private IObservationValue getObservationValue(OMObservationType omObservation) throws OwsExceptionReport {
        ITime phenomenonTime = getPhenomenonTime(omObservation);
        IObservationValue observationValue = null;
        if (phenomenonTime.getIndeterminateValue() != null && phenomenonTime.getIndeterminateValue().equals("template")) {
            observationValue = new SosSingleObservationValue();
            observationValue.setPhenomenonTime(phenomenonTime);
            observationValue.setValue(new NilTemplateValue());
        } else {
            observationValue = getResult(omObservation);
            observationValue.setPhenomenonTime(phenomenonTime);
        }
        return observationValue;
    }

    private IObservationValue getResult(OMObservationType omObservation) throws OwsExceptionReport {
        // TruthObservation
        if (omObservation.getResult().schemaType() == XmlBoolean.type) {
            XmlBoolean xbBoolean = (XmlBoolean) omObservation.getResult();
            BooleanValue booleanValue = new BooleanValue(xbBoolean.getBooleanValue());
            return new SosSingleObservationValue(booleanValue);
        }
        // CountObservation
        else if (omObservation.getResult().schemaType() == XmlInteger.type) {
            XmlInteger xbInteger = (XmlInteger) omObservation.getResult();
            CountValue countValue = new CountValue(Integer.parseInt(xbInteger.getBigIntegerValue().toString()));
            return new SosSingleObservationValue(countValue);
        }
        // TextObservation
        else if (omObservation.getResult().schemaType() == XmlString.type) {
            XmlString xbString = (XmlString) omObservation.getResult();
            TextValue StringValue = new TextValue(xbString.getStringValue());
            return new SosSingleObservationValue(StringValue);
        }
        // result elements with other encoding like SWE_ARRAY_OBSERVATION
        else {
            String namespace = omObservation.getResult().schemaType().getName().getNamespaceURI();
            List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
            if (decoderList != null) {
                for (IDecoder decoder : decoderList) {
                    Object decodedObject = decoder.decode(omObservation.getResult());
                    if (decodedObject != null && decodedObject instanceof IObservationValue){
                        return (IObservationValue) decodedObject;
                    }
                    else if (decodedObject != null && decodedObject instanceof SosSweDataArray)
                    {
                        SosMultiObservationValues result = new SosMultiObservationValues();
                        SweDataArrayValue value = new SweDataArrayValue();
                        value.setValue((SosSweDataArray) decodedObject);
                        result.setValue(value);
                        return result;
                    }
                }
            }
            String exceptionText = "The requested result type is not supported by this service!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
        }
    }

    private void checkOrSetObservationType(SosObservation sosObservation) throws OwsExceptionReport {
        String obsTypeFromValue = OMHelper.getObservationTypeFromValue(sosObservation.getValue().getValue());
        if (sosObservation.getObservationConstellation().getObservationType() == null) {
            sosObservation.getObservationConstellation().setObservationType(obsTypeFromValue);
        } else {
            if (!sosObservation.getObservationConstellation().getObservationType().equals(obsTypeFromValue)) {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The requested observation is invalid!");
                exceptionText.append(" The result element does not comply with the defined type (");
                exceptionText.append(sosObservation.getObservationConstellation().getObservationType());
                exceptionText.append(")!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        }
    }

    private SosAbstractFeature checkFeatureWithMap(SosAbstractFeature featureOfInterest,
            Map<String, SosAbstractFeature> featureMap) {
        if (featureOfInterest.getGmlId() != null && !featureOfInterest.getGmlId().isEmpty()) {
            if (featureMap.containsKey(featureOfInterest.getGmlId())) {
                return featureMap.get(featureOfInterest.getGmlId());
            } else {
                featureMap.put(featureOfInterest.getGmlId(), featureOfInterest);
            }
        }
        return featureOfInterest;
    }

}
