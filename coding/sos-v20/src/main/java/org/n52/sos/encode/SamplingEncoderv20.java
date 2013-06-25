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
import org.apache.xmlbeans.XmlOptions;
import org.joda.time.DateTime;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.NamedValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.FeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class SamplingEncoderv20 implements Encoder<XmlObject, AbstractFeature> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEncoderv20.class);

    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
            CodingHelper.encoderKeysForElements(SFConstants.NS_SAMS, AbstractFeature.class),
            CodingHelper.encoderKeysForElements(SFConstants.NS_SF, AbstractFeature.class));

    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            ConformanceClasses.OM_V2_SPATIAL_SAMPLING, ConformanceClasses.OM_V2_SAMPLING_POINT,
            ConformanceClasses.OM_V2_SAMPLING_CURVE, ConformanceClasses.OM_V2_SAMPLING_SURFACE);

    private static final Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
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
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.asSet(SFConstants.SF_SCHEMA_LOCATION, SFConstants.SAMS_SCHEMA_LOCATION);
    }

    @Override
    public XmlObject encode(AbstractFeature response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(AbstractFeature abstractFeature, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        XmlObject encodedObject = createFeature(abstractFeature);
        LOGGER.debug("Encoded object {} is valid: {}", encodedObject.schemaType().toString(),
                XmlHelper.validateDocument(encodedObject));
        return encodedObject;
    }

    private XmlObject createFeature(AbstractFeature absFeature) throws OwsExceptionReport {
        if (absFeature instanceof SamplingFeature) {
            SamplingFeature sampFeat = (SamplingFeature) absFeature;
            StringBuilder builder = new StringBuilder();
            builder.append("ssf_");
            builder.append(JavaHelper.generateID(absFeature.getIdentifier().getValue()));
            absFeature.setGmlId(builder.toString());

            SFSpatialSamplingFeatureDocument xbSampFeatDoc =
                    SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                            .getXmlOptions());
            if (sampFeat.getXmlDescription() != null) {
                try {
                    XmlObject feature = XmlObject.Factory.parse(sampFeat.getXmlDescription(), XmlOptionsHelper.getInstance().getXmlOptions());
                    XmlHelper.updateGmlIDs(feature.getDomNode().getFirstChild(), absFeature.getGmlId(), null);
                    if (XmlHelper.getNamespace(feature).equals(SFConstants.NS_SAMS)) {
                        if (feature instanceof SFSpatialSamplingFeatureType) {
                           
                            xbSampFeatDoc.setSFSpatialSamplingFeature((SFSpatialSamplingFeatureType) feature);
                            return xbSampFeatDoc;
                        }
                    } 
                    return feature;
                } catch (XmlException xmle) {
                    throw new NoApplicableCodeException()
                            .causedBy(xmle)
                            .withMessage(
                                    "Error while encoding GetFeatureOfInterest response, invalid samplingFeature description!");
                }
            }
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
                if (sampFeat.getSampledFeatures().size() == 1) {
                    XmlObject encodeObjectToXml =
                            CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, sampFeat.getSampledFeatures()
                                    .get(0));
                    xbSampFeature.addNewSampledFeature().set(encodeObjectToXml);
                } else {
                    FeatureCollection featureCollection = new FeatureCollection();
                    featureCollection.setGmlId("sampledFeatures_" + absFeature.getGmlId());
                    for (AbstractFeature sampledFeature : sampFeat.getSampledFeatures()) {
                        featureCollection.addMember(sampledFeature);
                    }
                    XmlObject encodeObjectToXml =
                            CodingHelper.encodeObjectToXml(GMLConstants.NS_GML_32, featureCollection);
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
            Encoder<XmlObject, Geometry> encoder =
                    CodingRepository.getInstance().getEncoder(
                            CodingHelper.getEncoderKey(GMLConstants.NS_GML_32, sampFeat.getGeometry()));
            if (encoder != null) {
                Map<HelperValues, String> gmlAdditionalValues = new EnumMap<HelperValues, String>(HelperValues.class);
                gmlAdditionalValues.put(HelperValues.GMLID, absFeature.getGmlId());
                XmlObject xmlObject = encoder.encode(sampFeat.getGeometry(), gmlAdditionalValues);
                xbShape.addNewAbstractGeometry().set(xmlObject);
                XmlHelper.substituteElement(xbShape.getAbstractGeometry(), xmlObject);
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("Error while encoding geometry for feature, needed encoder is missing!");
            }
            return xbSampFeatDoc;
        }
        throw new UnsupportedEncoderInputException(this, absFeature);
    }

    private void addParameter(SFSpatialSamplingFeatureType xbSampFeature, SamplingFeature sampFeat)
            throws OwsExceptionReport {
        for (NamedValue<?> namedValuePair : sampFeat.getParameters()) {
            XmlObject encodeObjectToXml = CodingHelper.encodeObjectToXml(OMConstants.NS_OM_2, namedValuePair);
            if (encodeObjectToXml != null) {
                xbSampFeature.addNewParameter().addNewNamedValue().set(encodeObjectToXml);
            }
        }
    }

    private XmlObject createFeatureCollection(List<AbstractFeature> features, boolean forObservation)
            throws OwsExceptionReport {
        SFSamplingFeatureCollectionDocument xbSampFeatCollDoc =
                SFSamplingFeatureCollectionDocument.Factory
                        .newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        SFSamplingFeatureCollectionType xbSampFeatColl = xbSampFeatCollDoc.addNewSFSamplingFeatureCollection();
        xbSampFeatColl.setId("sfc_" + Long.toString(new DateTime().getMillis()));
        for (AbstractFeature sosAbstractFeature : features) {
            SFSamplingFeaturePropertyType xbFeatMember = xbSampFeatColl.addNewMember();
            xbFeatMember.set(createFeature(sosAbstractFeature));
        }
        return xbSampFeatCollDoc;
    }
}
