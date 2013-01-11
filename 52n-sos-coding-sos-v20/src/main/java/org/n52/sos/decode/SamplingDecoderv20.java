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
import net.opengis.gml.x32.ReferenceType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DecoderHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SamplingDecoderv20 implements IDecoder<SosAbstractFeature, XmlObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingDecoderv20.class);

    private List<DecoderKeyType> decoderKeyTypes;

    private Set<String> supportedFeatureTypes;

    public SamplingDecoderv20() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(SFConstants.NS_SAMS));
        decoderKeyTypes.add(new DecoderKeyType(SFConstants.NS_SF));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());

        supportedFeatureTypes = new HashSet<String>(0);
        supportedFeatureTypes.add(OGCConstants.UNKNOWN);
        supportedFeatureTypes.add(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT);
        supportedFeatureTypes.add(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE);
        supportedFeatureTypes.add(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE);

        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        Map<SupportedTypeKey, Set<String>> map = new HashMap<SupportedTypeKey, Set<String>>();
        map.put(SupportedTypeKey.FeatureType, supportedFeatureTypes);
        return map;
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/spatialSampling");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingPoint");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingCurve");
        conformanceClasses.add("http://www.opengis.net/spec/OMXML/2.0/conf/samplingSurface");
        return conformanceClasses;
    }

    @Override
    public SosAbstractFeature decode(XmlObject element) throws OwsExceptionReport {
        if (element instanceof SFSpatialSamplingFeatureDocument) {
            return parseSpatialSamplingFeature(((SFSpatialSamplingFeatureDocument) element)
                    .getSFSpatialSamplingFeature());
        } else if (element instanceof SFSpatialSamplingFeatureType) {
            return parseSpatialSamplingFeature(((SFSpatialSamplingFeatureType) element));
        }
        return null;
    }

    private SosAbstractFeature parseSpatialSamplingFeature(SFSpatialSamplingFeatureType spatialSamplingFeature)
            throws OwsExceptionReport {
        SosSamplingFeature sosFeat = new SosSamplingFeature(null, spatialSamplingFeature.getId());
        if (spatialSamplingFeature.getIdentifier() != null
                && spatialSamplingFeature.getIdentifier().getStringValue() != null
                && !spatialSamplingFeature.getIdentifier().getStringValue().isEmpty()) {
            sosFeat.setIdentifier(spatialSamplingFeature.getIdentifier().getStringValue());
        }
        if (spatialSamplingFeature.getNameArray() != null) {
              sosFeat.setName(getNames(spatialSamplingFeature));
        }
        sosFeat.setFeatureType(getFeatureType(spatialSamplingFeature.getType()));
        sosFeat.setSampledFeatures(getSampledFeatures(spatialSamplingFeature.getSampledFeature()));
        sosFeat.setXmlDescription(getXmlDescription(spatialSamplingFeature));
        sosFeat.setGeometry(getGeometry(spatialSamplingFeature.getShape()));
        checkTypeAndGeometry(sosFeat);
        return sosFeat;
    }

    private String getXmlDescription(SFSpatialSamplingFeatureType spatialSamplingFeature) {
        SFSpatialSamplingFeatureDocument featureDoc =
                SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        featureDoc.setSFSpatialSamplingFeature(spatialSamplingFeature);
        return featureDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions());
    }

    private List<CodeType> getNames(SFSpatialSamplingFeatureType spatialSamplingFeature) throws OwsExceptionReport {
        List<CodeType> names = new ArrayList<CodeType>();
        int length = spatialSamplingFeature.getNameArray().length;
        for (int i = 0; i < length; i++) {
            Object decodedElement = DecoderHelper.decodeXmlElement(spatialSamplingFeature.getNameArray(i));
            if (decodedElement != null && decodedElement instanceof CodeType) {
                names.add((CodeType)decodedElement);
            }
        }
        return names;
    }

    private String getFeatureType(ReferenceType type) {
        if (type != null && type.getHref() != null && !type.getHref().isEmpty()) {
            return type.getHref();
        }
        return null;
    }

    private List<SosAbstractFeature> getSampledFeatures(FeaturePropertyType sampledFeature) throws OwsExceptionReport {
        List<SosAbstractFeature> sampledFeatures = new ArrayList<SosAbstractFeature>(1);
        if (sampledFeature != null && !sampledFeature.isNil()) {
            // if xlink:href is set
            if (sampledFeature.getHref() != null && !sampledFeature.getHref().isEmpty()) {
                if (sampledFeature.getHref().startsWith("#")) {
                    sampledFeatures.add(new SosSamplingFeature(null, sampledFeature.getHref().replace("#", "")));
                } else {
                    SosSamplingFeature sampFeat = new SosSamplingFeature(sampledFeature.getHref());
                    if (sampledFeature.getTitle() != null && !sampledFeature.getTitle().isEmpty()) {
                        
                        sampFeat.addName(new CodeType(sampledFeature.getTitle()));
                    }
                    sampledFeatures.add(sampFeat);
                }
            } else {
                XmlObject abstractFeature = null;
                if (sampledFeature.getAbstractFeature() != null) {
                    abstractFeature = sampledFeature.getAbstractFeature();
                } else if (sampledFeature.getDomNode().hasChildNodes()) {
                    try {
                        abstractFeature =
                                XmlObject.Factory.parse(XmlHelper.getNodeFromNodeList(sampledFeature.getDomNode()
                                        .getChildNodes()));
                    } catch (XmlException xmle) {
                        String exceptionText = "Error while parsing feature request!";
                        LOGGER.error(exceptionText, xmle);
                        throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                    }
                }
                if (abstractFeature != null) {
                    Object decodedObject = DecoderHelper.decodeXmlElement(abstractFeature);
                    if (decodedObject != null && decodedObject instanceof SosAbstractFeature) {
                        sampledFeatures.add((SosAbstractFeature) decodedObject);
                    }
                }
                String exceptionText = "The requested sampledFeature type is not supported by this service!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
            }
        }
        return sampledFeatures;
    }

    private Geometry getGeometry(ShapeType shape) throws OwsExceptionReport {
        Object decodedObject = DecoderHelper.decodeXmlElement(shape.getAbstractGeometry());
        if (decodedObject != null && decodedObject instanceof Geometry) {
            return (Geometry) decodedObject;
        }
        String exceptionText = "The requested geometry type of featureOfInterest is not supported by this service!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createInvalidParameterValueException(
                Sos2Constants.InsertObservationParams.observation.name(), exceptionText);
    }

    private void checkTypeAndGeometry(SosSamplingFeature sosFeat) throws OwsExceptionReport {
        String featTypeForGeometry = getFeatTypeForGeometry(sosFeat.getGeometry());
        if (sosFeat.getFeatureType() == null) {
            sosFeat.setFeatureType(featTypeForGeometry);
        } else {
            if (!featTypeForGeometry.equals(sosFeat.getFeatureType())) {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The requested observation is invalid!");
                exceptionText.append("The featureOfInterest type does not comply with the defined type (");
                exceptionText.append(sosFeat.getFeatureType());
                exceptionText.append(")!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        }

    }

    private String getFeatTypeForGeometry(Geometry geometry) {
        if (geometry instanceof Point) {
            return SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT;
        } else if (geometry instanceof LineString) {
            return SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE;
        } else if (geometry instanceof Polygon) {
            return SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE;
        }
        return OGCConstants.UNKNOWN;
    }

}
