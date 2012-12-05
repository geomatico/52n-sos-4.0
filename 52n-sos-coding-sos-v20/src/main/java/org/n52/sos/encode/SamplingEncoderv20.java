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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.x32.AbstractGeometryType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionDocument;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionType;
import net.opengis.sampling.x20.SFSamplingFeaturePropertyType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.GmlHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SamplingEncoderv20 implements IEncoder<XmlObject, SosAbstractFeature> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

    private Set<String> supportedFeatureTypes;

    public SamplingEncoderv20() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(SFConstants.NS_SAMS));
        encoderKeyTypes.add(new EncoderKeyType(SFConstants.NS_SF));
        StringBuilder builder = new StringBuilder();
        for (EncoderKeyType encoderKeyType : encoderKeyTypes) {
            builder.append(encoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());

        supportedFeatureTypes = new HashSet<String>(0);
        supportedFeatureTypes.add(OGCConstants.UNKNOWN);
        supportedFeatureTypes.add(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT);
        supportedFeatureTypes.add(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE);
        supportedFeatureTypes.add(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE);

        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
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

    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SFConstants.NS_SAMS, SFConstants.NS_SAMS_PREFIX);
        nameSpacePrefixMap.put(SFConstants.NS_SF, SFConstants.NS_SF_PREFIX);
    }

    @Override
    public String getContentType() {
        return "text/xml";
    }

    @Override
    public XmlObject encode(SosAbstractFeature response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(SosAbstractFeature response, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (response instanceof SosAbstractFeature) {
            return createFeature((SosAbstractFeature) response);
        }
        return null;
    }

    private XmlObject createFeature(SosAbstractFeature absFeature) throws OwsExceptionReport {
        SosSamplingFeature sampFeat = (SosSamplingFeature) absFeature;
        if (absFeature.isSetGmlID()) {
            FeaturePropertyType featureProperty = FeaturePropertyType.Factory.newInstance();
            featureProperty.setHref("#" + absFeature.getGmlId());
            return featureProperty;
        } else {
            absFeature.setGmlId(SosHelper.generateID(absFeature.getIdentifier()));

            SFSpatialSamplingFeatureDocument xbSampFeatDoc =
                    SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                            .getXmlOptions());
            if (sampFeat.getXmlDescription() != null) {
                try {
                    XmlObject feature = XmlObject.Factory.parse(sampFeat.getXmlDescription());
                    if (feature instanceof SFSpatialSamplingFeatureDocument) {
                        xbSampFeatDoc = (SFSpatialSamplingFeatureDocument) feature;
                    } else if (feature instanceof SFSpatialSamplingFeatureType) {
                        xbSampFeatDoc.setSFSpatialSamplingFeature((SFSpatialSamplingFeatureType) feature);
                    }
                    XmlHelper.updateGmlIDs(xbSampFeatDoc.getDomNode().getFirstChild(), absFeature.getGmlId(), null);
                } catch (XmlException xmle) {
                    String exceptionText =
                            "Error while encoding GetFeatureOfInterest response, invalid samplingFeature description!";
                    LOGGER.debug(exceptionText, xmle);
                    throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                }
            } else {
                SFSpatialSamplingFeatureType xbSampFeature = xbSampFeatDoc.addNewSFSpatialSamplingFeature();
                // TODO: CHECK for all fields
                // set gml:id
                xbSampFeature.setId(absFeature.getGmlId());

                if (sampFeat.getIdentifier() != null
                        && !sampFeat.getIdentifier().isEmpty()
                        && SosHelper.checkFeatureOfInterestIdentifierForSosV2(sampFeat.getIdentifier(),
                                Sos2Constants.SERVICEVERSION)) {
                    // set identifier
                    CodeWithAuthorityType identifier = xbSampFeature.addNewIdentifier();
                    identifier.setCodeSpace("");
                    identifier.setStringValue(sampFeat.getIdentifier());
                }

                // set type
                xbSampFeature.addNewType().setHref(sampFeat.getFeatureType());

                // set sampledFeatures
                // TODO: CHECK
                if (sampFeat.getSampledFeatures() != null && !sampFeat.getSampledFeatures().isEmpty()) {
                    for (SosAbstractFeature sampledFeature : sampFeat.getSampledFeatures()) {
                        FeaturePropertyType sampledFeat = xbSampFeature.addNewSampledFeature();
                        sampledFeat.set(createFeature(sampledFeature));
                    }
                } else {
                    FeaturePropertyType sampledFeat = xbSampFeature.addNewSampledFeature();
                    sampledFeat.setHref(GMLConstants.NIL_UNKNOWN);
                }

                // set position
                ShapeType xbShape = xbSampFeature.addNewShape();
                IEncoder encoder = Configurator.getInstance().getEncoder(GMLConstants.NS_GML_32);
                if (encoder != null) {
                    Map<HelperValues, String> additionalValues = new HashMap<HelperValues, String>();
                    additionalValues.put(HelperValues.GMLID, absFeature.getGmlId());
                    XmlObject xmlObject = (XmlObject) encoder.encode(sampFeat.getGeometry(), additionalValues);
                    if (xmlObject instanceof AbstractGeometryType) {
                        XmlObject substitution =
                                xbShape.addNewAbstractGeometry().substitute(
                                        GmlHelper.getQnameForGeometry(sampFeat.getGeometry()), xmlObject.schemaType());
                        substitution.set((AbstractGeometryType) xmlObject);
                    } else {
                        // TODO: Exception
                    }
                } else {
                    String exceptionText = "Error while encoding geometry for feature, needed encoder is missing!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
            }
            return xbSampFeatDoc;
        }
    }

    private XmlObject createFeatureCollection(List<SosAbstractFeature> features, boolean forObservation)
            throws OwsExceptionReport {
        SFSamplingFeatureCollectionDocument xbSampFeatCollDoc =
                SFSamplingFeatureCollectionDocument.Factory
                        .newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        SFSamplingFeatureCollectionType xbSampFeatColl = xbSampFeatCollDoc.addNewSFSamplingFeatureCollection();
        xbSampFeatColl.setId("sfc_" + Long.toString(new DateTime().getMillis()));
        for (SosAbstractFeature sosAbstractFeature : features) {
            SFSamplingFeaturePropertyType xbFeatMember = xbSampFeatColl.addNewMember();
            xbFeatMember.set(createFeature(sosAbstractFeature));
        }
        return xbSampFeatCollDoc;
    }

}
