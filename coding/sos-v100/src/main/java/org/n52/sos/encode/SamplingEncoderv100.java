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
import java.util.Map;
import java.util.Set;

import net.opengis.sampling.x10.SamplingCurveDocument;
import net.opengis.sampling.x10.SamplingCurveType;
import net.opengis.sampling.x10.SamplingFeatureCollectionDocument;
import net.opengis.sampling.x10.SamplingFeatureCollectionType;
import net.opengis.sampling.x10.SamplingFeaturePropertyType;
import net.opengis.sampling.x10.SamplingFeatureType;
import net.opengis.sampling.x10.SamplingPointDocument;
import net.opengis.sampling.x10.SamplingPointType;
import net.opengis.sampling.x10.SamplingSurfaceDocument;
import net.opengis.sampling.x10.SamplingSurfaceType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.SosFeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SamplingEncoderv100 implements Encoder<XmlObject, SosAbstractFeature> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEncoderv100.class);

    @SuppressWarnings("unchecked")
    private static final Set<EncoderKey> ENCODER_KEYS = CollectionHelper.union(CodingHelper.encoderKeysForElements(
            SFConstants.NS_SA, SosAbstractFeature.class));

    // TODO here also the question, sa:samplingPoint sampling/1.0 vs 2.0 mapping
    // or not and where and how to handle
    private Map<SupportedTypeKey, Set<String>> SUPPORTED_TYPES = Collections.singletonMap(
            SupportedTypeKey.FeatureType, CollectionHelper.set(OGCConstants.UNKNOWN, SFConstants.EN_SAMPLINGPOINT,
                    SFConstants.EN_SAMPLINGSURFACE, SFConstants.EN_SAMPLINGCURVE));

    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            ConformanceClasses.OM_V2_SPATIAL_SAMPLING, ConformanceClasses.OM_V2_SAMPLING_POINT,
            ConformanceClasses.OM_V2_SAMPLING_CURVE, ConformanceClasses.OM_V2_SAMPLING_SURFACE);

    public SamplingEncoderv100() {
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
        return createFeature(abstractFeature);
    }

    private XmlObject createFeature(SosAbstractFeature absFeature) throws OwsExceptionReport {
        if (absFeature instanceof SosSamplingFeature) {
            SosSamplingFeature sampFeat = (SosSamplingFeature) absFeature;
            if (sampFeat.getFeatureType().equals(SFConstants.FT_SAMPLINGPOINT)
                    || sampFeat.getFeatureType().equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT)
                    || sampFeat.getGeometry() instanceof Point) {
                SamplingPointDocument xbSamplingPointDoc =
                        SamplingPointDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                SamplingPointType xbSamplingPoint = xbSamplingPointDoc.addNewSamplingPoint();
                addValuesToFeature(xbSamplingPoint, sampFeat);
                XmlObject xbGeomety = getEncodedGeometry(sampFeat.getGeometry(), absFeature.getGmlId());
                xbSamplingPoint.addNewPosition().addNewPoint().set(xbGeomety);
                return xbSamplingPointDoc;
            } else if (sampFeat.getFeatureType().equals(SFConstants.FT_SAMPLINGCURVE)
                    || sampFeat.getFeatureType().equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_CURVE)
                    || sampFeat.getGeometry() instanceof LineString) {
                SamplingCurveDocument xbSamplingCurveDoc =
                        SamplingCurveDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                SamplingCurveType xbSamplingCurve = xbSamplingCurveDoc.addNewSamplingCurve();
                addValuesToFeature(xbSamplingCurve, sampFeat);
                XmlObject xbGeomety = getEncodedGeometry(sampFeat.getGeometry(), absFeature.getGmlId());
                xbSamplingCurve.addNewShape().addNewCurve().set(xbGeomety);
                return xbSamplingCurveDoc;
            } else if (sampFeat.getFeatureType().equals(SFConstants.FT_SAMPLINGSURFACE)
                    || sampFeat.getFeatureType().equals(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE)
                    || sampFeat.getGeometry() instanceof Polygon) {
                SamplingSurfaceDocument xbSamplingSurfaceDoc =
                        SamplingSurfaceDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                SamplingSurfaceType xbSamplingSurface = xbSamplingSurfaceDoc.addNewSamplingSurface();
                addValuesToFeature(xbSamplingSurface, sampFeat);
                XmlObject xbGeomety = getEncodedGeometry(sampFeat.getGeometry(), absFeature.getGmlId());
                xbSamplingSurface.addNewShape().addNewSurface().set(xbGeomety);
                return xbSamplingSurfaceDoc;
            }
        } else if (absFeature instanceof SosFeatureCollection) {
            createFeatureCollection((SosFeatureCollection) absFeature);
        }
        throw new UnsupportedEncoderInputException(this, absFeature);
    }

    private XmlObject getEncodedGeometry(Geometry geometry, String gmlId) throws UnsupportedEncoderInputException,
            OwsExceptionReport {
        Encoder<XmlObject, Geometry> encoder =
                CodingRepository.getInstance().getEncoder(CodingHelper.getEncoderKey(GMLConstants.NS_GML, geometry));
        if (encoder != null) {
            Map<HelperValues, String> additionalValues = new EnumMap<HelperValues, String>(HelperValues.class);
            additionalValues.put(HelperValues.GMLID, gmlId);
            return encoder.encode(geometry, additionalValues);
        } else {
            throw new NoApplicableCodeException()
                    .withMessage("Error while encoding geometry for feature, needed encoder is missing!");
        }
    }

    private void addValuesToFeature(SamplingFeatureType xbSamplingFeature, SosSamplingFeature sampFeat)
            throws OwsExceptionReport {
        xbSamplingFeature.setId(sampFeat.getGmlId());
        if (sampFeat.isSetIdentifier()
                && SosHelper.checkFeatureOfInterestIdentifierForSosV2(sampFeat.getIdentifier().getValue(),
                        Sos1Constants.SERVICEVERSION)) {
            xbSamplingFeature.addNewName().set(
                    CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, sampFeat.getIdentifier()));
        }

        if (sampFeat.isSetNames()) {
            for (CodeType sosName : sampFeat.getName()) {
                xbSamplingFeature.addNewName().set(CodingHelper.encodeObjectToXml(GMLConstants.NS_GML, sosName));
            }
        }

        // set sampledFeatures
        // TODO: CHECK
        if (sampFeat.getSampledFeatures() != null && !sampFeat.getSampledFeatures().isEmpty()) {
            for (SosAbstractFeature sampledFeature : sampFeat.getSampledFeatures()) {
                xbSamplingFeature.addNewSampledFeature().set(createFeature(sampledFeature));
            }
        } else {
            xbSamplingFeature.addNewSampledFeature().setHref(GMLConstants.NIL_UNKNOWN);
        }
    }

    private XmlObject createFeatureCollection(SosFeatureCollection sosFeatureCollection) throws OwsExceptionReport {
        SamplingFeatureCollectionDocument xbSampFeatCollDoc =
                SamplingFeatureCollectionDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        SamplingFeatureCollectionType xbSampFeatColl = xbSampFeatCollDoc.addNewSamplingFeatureCollection();
        xbSampFeatColl.setId("sfc_" + Long.toString(new DateTime().getMillis()));
        for (SosAbstractFeature sosAbstractFeature : sosFeatureCollection.getMembers().values()) {
            SamplingFeaturePropertyType xbFeatMember = xbSampFeatColl.addNewMember();
            xbFeatMember.set(createFeature(sosAbstractFeature));
        }
        return xbSampFeatCollDoc;
    }
}
