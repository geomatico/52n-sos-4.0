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

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.LocationPropertyType;
import net.opengis.sampling.x10.SamplingFeatureCollectionDocument;
import net.opengis.sampling.x10.SamplingFeatureCollectionType;
import net.opengis.sampling.x10.SamplingFeatureDocument;
import net.opengis.sampling.x10.SamplingFeaturePropertyType;
import net.opengis.sampling.x10.SamplingFeatureType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos1Constants;
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

public class SamplingEncoderv100 implements IEncoder<XmlObject, SosAbstractFeature> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEncoderv100.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(
        CodingHelper.encoderKeysForElements(SFConstants.NS_SA, SosAbstractFeature.class)
    );

    // TODO here also the question, sa:samplingPoint sampling/1.0 vs 2.0 mapping or not and where and how to handle
    private Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
        SupportedTypeKey.FeatureType,
        CollectionHelper.set(
            OGCConstants.UNKNOWN,
            SFConstants.EN_SAMPLINGPOINT,
            SFConstants.EN_SAMPLINGSURFACE
        )
    );

    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            ConformanceClasses.OM_V2_SPATIAL_SAMPLING, ConformanceClasses.OM_V2_SAMPLING_POINT,
            ConformanceClasses.OM_V2_SAMPLING_CURVE, ConformanceClasses.OM_V2_SAMPLING_SURFACE);

    public SamplingEncoderv100() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEYS));
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
        nameSpacePrefixMap.put(SFConstants.NS_SA, SFConstants.NS_SA_PREFIX);
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
        return createFeature( abstractFeature);
    }

    private XmlObject createFeature(SosAbstractFeature absFeature) throws OwsExceptionReport {
        SosSamplingFeature sampFeat = (SosSamplingFeature) absFeature;
        if (absFeature.isSetGmlID()) {
            FeaturePropertyType featureProperty = FeaturePropertyType.Factory.newInstance();
            featureProperty.setHref("#" + absFeature.getGmlId());
            return featureProperty;
        } else {
            if (!sampFeat.isSetGeometry()) {
                FeaturePropertyType featureProperty = FeaturePropertyType.Factory.newInstance();
                featureProperty.setHref(absFeature.getIdentifier().getValue());
                if (sampFeat.isSetNames()) {
                    featureProperty.setTitle(sampFeat.getFirstName().getValue());
                }
                return featureProperty;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("foi_");
            builder.append(JavaHelper.generateID(absFeature.getIdentifier().getValue()));
            absFeature.setGmlId(builder.toString());

            SamplingFeatureDocument xbSampFeatDoc =
            		SamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance()
                            .getXmlOptions());
            // ignore for now, we are in the 1.0 encoder
            // if (sampFeat.getXmlDescription() != null) {
            if (absFeature == null) {
                try {
                    XmlObject feature = XmlObject.Factory.parse(sampFeat.getXmlDescription());
                    if (feature instanceof SamplingFeatureDocument) {
                        xbSampFeatDoc = (SamplingFeatureDocument) feature;
                    } else if (feature instanceof SamplingFeatureDocument) {
                        xbSampFeatDoc.setSamplingFeature((SamplingFeatureType) feature);
                    }
                    XmlHelper.updateGmlIDs(xbSampFeatDoc.getDomNode().getFirstChild(), absFeature.getGmlId(), null);
                } catch (XmlException xmle) {
                    String exceptionText =
                            "Error while encoding GetFeatureOfInterest response, invalid samplingFeature description!";
                    LOGGER.debug(exceptionText, xmle);
                    throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText);
                }
            } else {
            	SamplingFeatureType xbSampFeature = (SamplingFeatureType) xbSampFeatDoc.addNewSamplingFeature();
                // TODO: CHECK for all fields
                // set gml:id
                xbSampFeature.setId(absFeature.getGmlId());

                if (sampFeat.isSetIdentifier()
                        && SosHelper.checkFeatureOfInterestIdentifierForSosV2(sampFeat.getIdentifier().getValue(),
                                Sos1Constants.SERVICEVERSION)) {
                    xbSampFeature.addNewName().set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, sampFeat.getIdentifier()));
                }

                // set type (is done through addNewName already?!
                // xbSampFeature. ().setHref(sampFeat.getFeatureType());

//                if (sampFeat.isSetNames()) {
//                    for (CodeType sosName : sampFeat.getName()) {
//                        xbSampFeature.addNewName().set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, sosName));
//                    }
//                }

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
                LocationPropertyType xbShape = xbSampFeature.addNewLocation();
                IEncoder<XmlObject, Geometry> encoder = Configurator.getInstance().getCodingRepository()
                        .getEncoder(CodingHelper.getEncoderKey(GMLConstants.NS_GML, sampFeat.getGeometry()));
                if (encoder != null) {
                    Map<HelperValues, String> additionalValues = new EnumMap<HelperValues, String>(HelperValues.class);
                    additionalValues.put(HelperValues.GMLID, absFeature.getGmlId());
                    XmlObject xmlObject = encoder.encode(sampFeat.getGeometry(), additionalValues);
                    xbShape.addNewGeometry().set(xmlObject);
                    XmlHelper.substituteElement(xbShape.getGeometry(), xmlObject);
//                    encoder.substitute(xbShape.getAbstractGeometry(), xmlObject);
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
        SamplingFeatureCollectionDocument xbSampFeatCollDoc =
                SamplingFeatureCollectionDocument.Factory
                        .newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        SamplingFeatureCollectionType xbSampFeatColl = xbSampFeatCollDoc.addNewSamplingFeatureCollection();
        xbSampFeatColl.setId("sfc_" + Long.toString(new DateTime().getMillis()));
        for (SosAbstractFeature sosAbstractFeature : features) {
            SamplingFeaturePropertyType xbFeatMember = xbSampFeatColl.addNewMember();
            xbFeatMember.set(createFeature(sosAbstractFeature));
        }
        return xbSampFeatCollDoc;
    }
}
