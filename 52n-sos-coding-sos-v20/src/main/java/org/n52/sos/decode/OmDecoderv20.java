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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.TimeObjectPropertyType;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlString;
import org.n52.sos.exception.ows.OwsExceptionCode;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.MissingParameterValueException;
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
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.NilTemplateValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.CodedException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OmDecoderv20 implements Decoder<SosObservation, OMObservationType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OmDecoderv20.class);
    
    private static final Set<DecoderKey> DECODER_KEYS = CodingHelper.decoderKeysForElements(OMConstants.NS_OM_2, OMObservationType.class);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
        SupportedTypeKey.ObservationType, 
        CollectionHelper.set(
            //OMConstants.OBS_TYPE_GEOMETRY_OBSERVATION,
            OMConstants.OBS_TYPE_CATEGORY_OBSERVATION,
            OMConstants.OBS_TYPE_COUNT_OBSERVATION,
            OMConstants.OBS_TYPE_MEASUREMENT,
            OMConstants.OBS_TYPE_TEXT_OBSERVATION,
            OMConstants.OBS_TYPE_TRUTH_OBSERVATION,
            OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION
        )
    );
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
        ConformanceClasses.OM_V2_MEASUREMENT, 
        ConformanceClasses.OM_V2_CATEGORY_OBSERVATION, 
        ConformanceClasses.OM_V2_COUNT_OBSERVATION, 
        ConformanceClasses.OM_V2_TRUTH_OBSERVATION, 
        ConformanceClasses.OM_V2_GEOMETRY_OBSERVATION, 
        ConformanceClasses.OM_V2_TEXT_OBSERVATION);

    public OmDecoderv20() {
        LOGGER.debug("Decoder for the following keys initialized successfully: {}!", StringHelper.join(", ", DECODER_KEYS));
    }

    @Override
    public Set<DecoderKey> getDecoderKeyTypes() {
        return Collections.unmodifiableSet(DECODER_KEYS);
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
    public SosObservation decode(OMObservationType omObservation) throws OwsExceptionReport {
        Map<String, SosAbstractFeature> featureMap = new HashMap<String, SosAbstractFeature>();
        SosObservation sosObservation = new SosObservation();
        sosObservation.setIdentifier(getIdentifier(omObservation));
        SosObservationConstellation observationConstallation = getObservationConstellation(omObservation);
        sosObservation.setObservationConstellation(observationConstallation);
        sosObservation.setResultTime(getResultTime(omObservation));
        sosObservation.setValidTime(getValidTime(omObservation));
        sosObservation.setValue(getObservationValue(omObservation));
        try {
            Object decodeXmlElement = CodingHelper.decodeXmlElement(omObservation.getFeatureOfInterest());
            if (decodeXmlElement instanceof SosAbstractFeature) {
                SosAbstractFeature featureOfInterest = (SosAbstractFeature) decodeXmlElement;
                observationConstallation.setFeatureOfInterest(checkFeatureWithMap(featureOfInterest, featureMap));
            }
        } catch (OwsExceptionReport e) {
            if (sosObservation.getValue().getPhenomenonTime().getIndeterminateValue().equals("template")) {
                for (CodedException exception : e.getExceptions()) {
                    if (exception.getCode().equals(OwsExceptionCode.InvalidParameterValue)) {
                        throw new InvalidParameterValueException().at(exception.getLocator())
                                .withMessage(exception.getMessage());
                    } else if (exception.getCode().equals(OwsExceptionCode.MissingParameterValue)) {
                        throw new MissingParameterValueException(exception.getLocator());
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
            Object decodedObject = CodingHelper.decodeXmlObject(omObservation.getIdentifier());
            if (decodedObject != null && decodedObject instanceof CodeWithAuthority) {
                return (CodeWithAuthority) decodedObject;
            }
        }
        return null;
    }

    private SosObservationConstellation getObservationConstellation(OMObservationType omObservation)
            throws OwsExceptionReport {
        SosObservationConstellation observationConstellation = new SosObservationConstellation();
        observationConstellation.setObservationType(getObservationType(omObservation));
        observationConstellation.setProcedure(createProcedure(getProcedure(omObservation)));
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

    private ITime getPhenomenonTime(OMObservationType omObservation) throws OwsExceptionReport {
        TimeObjectPropertyType phenomenonTime = omObservation.getPhenomenonTime();
        if (phenomenonTime.isSetHref() && phenomenonTime.getHref().startsWith("#")) {
            TimeInstant timeInstant = new TimeInstant();
            timeInstant.setGmlId(phenomenonTime.getHref());
            return timeInstant;
        } else if (phenomenonTime.isSetNilReason()
                && phenomenonTime.getNilReason() instanceof String
                && ((String) phenomenonTime.getNilReason()).equals("template")) {
            TimeInstant timeInstant = new TimeInstant();
            timeInstant.setIndeterminateValue((String) phenomenonTime.getNilReason());
            return timeInstant;
        } else {
            Object decodedObject = CodingHelper.decodeXmlObject(phenomenonTime.getAbstractTimeObject());
            if (decodedObject != null && decodedObject instanceof ITime) {
                return (ITime) decodedObject;
            }
        }
        throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                .withMessage("The requested phenomenonTime type is not supported by this service!");
    }

    private TimeInstant getResultTime(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.getResultTime().isSetHref()) {
        	TimeInstant timeInstant = new TimeInstant();
        	timeInstant.setGmlId(omObservation.getResultTime().getHref());
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
            Object decodedObject = CodingHelper.decodeXmlObject(omObservation.getResultTime().getTimeInstant());
            if (decodedObject != null && decodedObject instanceof TimeInstant) {
                return (TimeInstant) decodedObject;
            }
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested resultTime type is not supported by this service!");
        }
    }

    private TimePeriod getValidTime(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.isSetValidTime()) {
            Object decodedObject = CodingHelper.decodeXmlObject(omObservation.getValidTime().getTimePeriod());
            if (decodedObject != null && decodedObject instanceof TimePeriod) {
                return (TimePeriod) decodedObject;
            }
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested validTime type is not supported by this service!");
        }
        return null;
    }

    private IObservationValue<?> getObservationValue(OMObservationType omObservation) throws OwsExceptionReport {
        ITime phenomenonTime = getPhenomenonTime(omObservation);
        IObservationValue<?> observationValue;
        if (phenomenonTime.getIndeterminateValue() != null 
                && phenomenonTime.getIndeterminateValue().equals("template")) {
            observationValue = new SosSingleObservationValue<String>(new NilTemplateValue());
        } else {
            observationValue = getResult(omObservation);
        }
        observationValue.setPhenomenonTime(phenomenonTime);
        return observationValue;
    }

    private IObservationValue<?> getResult(OMObservationType omObservation) throws OwsExceptionReport {
        // TruthObservation
        if (omObservation.getResult().schemaType() == XmlBoolean.type) {
            XmlBoolean xbBoolean = (XmlBoolean) omObservation.getResult();
            BooleanValue booleanValue = new BooleanValue(xbBoolean.getBooleanValue());
            return new SosSingleObservationValue<Boolean>(booleanValue);
        }
        // CountObservation
        else if (omObservation.getResult().schemaType() == XmlInteger.type) {
            XmlInteger xbInteger = (XmlInteger) omObservation.getResult();
            CountValue countValue = new CountValue(Integer.parseInt(xbInteger.getBigIntegerValue().toString()));
            return new SosSingleObservationValue<Integer>(countValue);
        }
        // TextObservation
        else if (omObservation.getResult().schemaType() == XmlString.type) {
            XmlString xbString = (XmlString) omObservation.getResult();
            TextValue stringValue = new TextValue(xbString.getStringValue());
            return new SosSingleObservationValue<String>(stringValue);
        }
        // result elements with other encoding like SWE_ARRAY_OBSERVATION
        else {
            Object decodedObject = CodingHelper.decodeXmlObject(omObservation.getResult());
            if (decodedObject != null && decodedObject instanceof IObservationValue) {
                return (IObservationValue) decodedObject;
            } else if (decodedObject != null && decodedObject instanceof SosSweDataArray) {
                    SosMultiObservationValues<SosSweDataArray> result = new SosMultiObservationValues<SosSweDataArray>();
                    SweDataArrayValue value = new SweDataArrayValue();
                    value.setValue((SosSweDataArray) decodedObject);
                    result.setValue(value);
                    return result;
            } else if (decodedObject != null && decodedObject instanceof SosSweDataArray) {
                SosMultiObservationValues<SosSweDataArray> result = new SosMultiObservationValues<SosSweDataArray>();
                SweDataArrayValue value = new SweDataArrayValue();
                value.setValue((SosSweDataArray) decodedObject);
                result.setValue(value);
                return result;
            }
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested result type is not supported by this service!");
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

    private SosProcedureDescription createProcedure(String procedureIdentifier) {
        SensorML procedure = new SensorML();
        SosSMLIdentifier identifier = new SosSMLIdentifier("uniqueID", "urn:ogc:def:identifier:OGC:uniqueID", procedureIdentifier);
        List<SosSMLIdentifier> identifiers = new ArrayList<SosSMLIdentifier>(1);
        identifiers.add(identifier);
        procedure.setIdentifications(identifiers);
        return procedure;
    }

}
