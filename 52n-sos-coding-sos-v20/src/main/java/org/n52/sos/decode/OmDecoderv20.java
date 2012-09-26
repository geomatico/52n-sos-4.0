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
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.IObservationValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
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
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
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

        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        Map<SupportedTypeKey, Set<String>> map = new HashMap<SupportedTypeKey, Set<String>>();
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
        SosObservationConstellation observationCollection = getObservationConstellation(omObservation);
        SosAbstractFeature featureOfInterest = getFeatureOfInterest(omObservation.getFeatureOfInterest());
        observationCollection.setFeatureOfInterest(checkFeatureWithMap(featureOfInterest, featureMap));
        sosObservation.setObservationConstellation(observationCollection);
        sosObservation.setResultTime(getResultTime(omObservation));
        sosObservation.setValidTime(getValidTime(omObservation));

        // TODO: later for spatial filtering profile
        // omObservation.getParameterArray();

        sosObservation.setValue(getObservationValue(omObservation));
        checkOrSetObservationType(sosObservation);
        return sosObservation;
    }

    private String getIdentifier(OMObservationType omObservation) {
        if (omObservation.getIdentifier() != null) {
            return omObservation.getIdentifier().getStringValue();
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
        if (featureOfInterest.getHref() != null && !featureOfInterest.getHref().isEmpty()) {
            if (featureOfInterest.getHref().startsWith("#")) {
                feature = new SosSamplingFeature(null, featureOfInterest.getHref().replace("#", ""));
            } else {
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
        String exceptionText = "The requested phenomenonTime type is not supported by this service!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createInvalidParameterValueException(
                Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
    }

    private TimeInstant getResultTime(OMObservationType omObservation) throws OwsExceptionReport {
        if (omObservation.getResultTime().isSetHref()) {
            TimeInstant timeInstant = new TimeInstant();
            timeInstant.setIndeterminateValue(omObservation.getResultTime().getHref());
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
        IObservationValue observationValue = getResult(omObservation);
        observationValue.setPhenomenonTime(getPhenomenonTime(omObservation));
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
        // result elements with other encoding
        else {
            String namespace = omObservation.getResult().schemaType().getName().getNamespaceURI();
            List<IDecoder> decoderList = Configurator.getInstance().getDecoder(namespace);
            if (decoderList != null) {
                for (IDecoder decoder : decoderList) {
                    Object decodedObject = decoder.decode(omObservation.getResult());
                    if (decodedObject != null && decodedObject instanceof IObservationValue) {
                        return (IObservationValue) decodedObject;
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
        String obsTypeFromValue = getObservationTypeFromValue(sosObservation.getValue().getValue());
        if (sosObservation.getObservationConstellation().getObservationType() == null) {
            sosObservation.getObservationConstellation().setObservationType(obsTypeFromValue);
        } else {
            if (!sosObservation.getObservationConstellation().getObservationType().equals(obsTypeFromValue)) {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The requested observation is invalid!");
                exceptionText.append("The result element does not comply with the defined type (");
                exceptionText.append(sosObservation.getObservationConstellation().getObservationType());
                exceptionText.append(")!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        }
    }

    private String getObservationTypeFromValue(Object value) {
        if (value instanceof BooleanValue) {
            return OMConstants.OBS_TYPE_TRUTH_OBSERVATION;
        } else if (value instanceof CategoryValue) {
            return OMConstants.OBS_TYPE_CATEGORY_OBSERVATION;
        } else if (value instanceof CountValue) {
            return OMConstants.OBS_TYPE_COUNT_OBSERVATION;
        } else if (value instanceof QuantityValue) {
            return OMConstants.OBS_TYPE_MEASUREMENT;
        } else if (value instanceof TextValue) {
            return OMConstants.OBS_TYPE_TEXT_OBSERVATION;
        }
        return OMConstants.OBS_TYPE_OBSERVATION;
    }

    private SosAbstractFeature checkFeatureWithMap(SosAbstractFeature featureOfInterest,
            Map<String, SosAbstractFeature> featureMap) {
        if (featureOfInterest.getGmlId() != null || !featureOfInterest.getGmlId().isEmpty()) {
            if (featureMap.containsKey(featureOfInterest.getGmlId())) {
                return featureMap.get(featureOfInterest.getGmlId());
            } else {
                featureMap.put(featureOfInterest.getGmlId(), featureOfInterest);
            }
        }
        return featureOfInterest;
    }

}
