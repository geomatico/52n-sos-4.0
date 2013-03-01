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
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.StringHelper;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingDecoderv20.class);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
        SupportedTypeKey.FeatureType,
        CollectionHelper.set(
            OGCConstants.UNKNOWN,
            SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT,
            SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE,
            SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE
        )
    );
    
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
        ConformanceClasses.OM_V2_SPATIAL_SAMPLING, 
        ConformanceClasses.OM_V2_SAMPLING_POINT, 
        ConformanceClasses.OM_V2_SAMPLING_CURVE, 
        ConformanceClasses.OM_V2_SAMPLING_SURFACE);
    @SuppressWarnings("unchecked")
    private static final Set<DecoderKey> DECODER_KEYS = CollectionHelper.union(
            CodingHelper.decoderKeysForElements(SFConstants.NS_SF,
                SFSpatialSamplingFeatureDocument.class,
                SFSpatialSamplingFeatureType.class),
            CodingHelper.decoderKeysForElements(SFConstants.NS_SAMS,
                SFSpatialSamplingFeatureDocument.class,
                SFSpatialSamplingFeatureType.class)
    );
    
    
    public SamplingDecoderv20() {
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
            CodeWithAuthority identifier = (CodeWithAuthority)CodingHelper.decodeXmlElement(spatialSamplingFeature.getIdentifier());
            sosFeat.setIdentifier(identifier);
        }
        if (spatialSamplingFeature.getNameArray() != null) {
              sosFeat.setName(getNames(spatialSamplingFeature));
        }
        sosFeat.setFeatureType(getFeatureType(spatialSamplingFeature.getType()));
        sosFeat.setSampledFeatures(getSampledFeatures(spatialSamplingFeature.getSampledFeature()));
        sosFeat.setXmlDescription(getXmlDescription(spatialSamplingFeature));
        sosFeat.setGeometry(getGeometry(spatialSamplingFeature.getShape()));
        checkTypeAndGeometry(sosFeat);
        sosFeat.setGmlId(spatialSamplingFeature.getId());
        return sosFeat;
    }

    private String getXmlDescription(SFSpatialSamplingFeatureType spatialSamplingFeature) {
        SFSpatialSamplingFeatureDocument featureDoc =
                SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        featureDoc.setSFSpatialSamplingFeature(spatialSamplingFeature);
        return featureDoc.xmlText(XmlOptionsHelper.getInstance().getXmlOptions());
    }
    private List<CodeType> getNames(SFSpatialSamplingFeatureType spatialSamplingFeature) throws OwsExceptionReport {
        final int length = spatialSamplingFeature.getNameArray().length;
        List<CodeType> names = new ArrayList<CodeType>(length);
        for (int i = 0; i < length; i++) {
            Object decodedElement = CodingHelper.decodeXmlObject(spatialSamplingFeature.getNameArray(i));
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
                    SosSamplingFeature sampFeat = new SosSamplingFeature(new CodeWithAuthority(sampledFeature.getHref()));
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
                    Object decodedObject = CodingHelper.decodeXmlObject(abstractFeature);
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
        Object decodedObject = CodingHelper.decodeXmlElement(shape.getAbstractGeometry());
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
