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

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.sampling.x20.SFSamplingFeatureCollectionDocument;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionType;
import net.opengis.sampling.x20.SFSamplingFeaturePropertyType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.NamedValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class SamplingEncoderv20 implements IEncoder<XmlObject, SosAbstractFeature> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEncoderv20.class);

    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
            CodingHelper.encoderKeysForElements(SFConstants.NS_SAMS, SosAbstractFeature.class),
            CodingHelper.encoderKeysForElements(SFConstants.NS_SF, SosAbstractFeature.class));

    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            ConformanceClasses.OM_V2_SPATIAL_SAMPLING, ConformanceClasses.OM_V2_SAMPLING_POINT,
            ConformanceClasses.OM_V2_SAMPLING_CURVE, ConformanceClasses.OM_V2_SAMPLING_SURFACE);
    private Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.FeatureType, CollectionHelper.set(OGCConstants.UNKNOWN,
                    SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT,
                    SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE,
                    SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE));

    public SamplingEncoderv20() {
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
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(SFConstants.NS_SAMS, SFConstants.NS_SAMS_PREFIX);
        nameSpacePrefixMap.put(SFConstants.NS_SF, SFConstants.NS_SF_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public XmlObject encode(SosAbstractFeature response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(SosAbstractFeature abstractFeature, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        return createFeature(abstractFeature);
    }

    private XmlObject createFeature(SosAbstractFeature absFeature) throws OwsExceptionReport {
        SosSamplingFeature sampFeat = (SosSamplingFeature) absFeature;
        StringBuilder builder = new StringBuilder();
        builder.append("foi_");
        builder.append(JavaHelper.generateID(absFeature.getIdentifier().getValue()));
        absFeature.setGmlId(builder.toString());

        SFSpatialSamplingFeatureDocument xbSampFeatDoc =
                SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
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

            if (sampFeat.isSetIdentifier()
                    && SosHelper.checkFeatureOfInterestIdentifierForSosV2(sampFeat.getIdentifier().getValue(),
                            Sos2Constants.SERVICEVERSION)) {
                xbSampFeature.addNewIdentifier().set(
                        CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, sampFeat.getIdentifier()));
            }

            // set type
            xbSampFeature.addNewType().setHref(sampFeat.getFeatureType());

            if (sampFeat.isSetNames()) {
                for (CodeType sosName : sampFeat.getName()) {
                    xbSampFeature.addNewName().set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, sosName));
                }
            }

            // set sampledFeatures
            // TODO: CHECK
            if (sampFeat.getSampledFeatures() != null && !sampFeat.getSampledFeatures().isEmpty()) {
                for (SosAbstractFeature sampledFeature : sampFeat.getSampledFeatures()) {
                    XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, sampledFeature);
                    xbSampFeature.addNewSampledFeature().set(encodeObjectToXml);
                }
            } else {
                xbSampFeature.addNewSampledFeature().setHref(GMLConstants.NIL_UNKNOWN);
            }
            
            if (sampFeat.isSetParameter()) {
                addParameter(xbSampFeature, sampFeat);
            }

            // set position
            ShapeType xbShape = xbSampFeature.addNewShape();
            IEncoder<XmlObject, Geometry> encoder =
                    Configurator.getInstance().getCodingRepository()
                            .getEncoder(CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, sampFeat.getGeometry()));
            if (encoder != null) {
                Map<HelperValues, String> gmlAdditionalValues = new EnumMap<HelperValues, String>(HelperValues.class);
                gmlAdditionalValues.put(HelperValues.GMLID, absFeature.getGmlId());
                XmlObject xmlObject = encoder.encode(sampFeat.getGeometry(), gmlAdditionalValues);
                xbShape.addNewAbstractGeometry().set(xmlObject);
                XmlHelper.substituteElement(xbShape.getAbstractGeometry(), xmlObject);
                // encoder.substitute(xbShape.getAbstractGeometry(), xmlObject);
            } else {
                String exceptionText = "Error while encoding geometry for feature, needed encoder is missing!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        }
        return xbSampFeatDoc;
    }

    private void addParameter(SFSpatialSamplingFeatureType xbSampFeature, SosSamplingFeature sampFeat) throws OwsExceptionReport {
        for (NamedValue<?> namedValuePair : sampFeat.getParameters()) {
            XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(OMConstants.NS_OM_2, namedValuePair);
            if (encodeObjectToXml != null) {
                xbSampFeature.addNewParameter().addNewNamedValue().set(encodeObjectToXml);
            }
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
