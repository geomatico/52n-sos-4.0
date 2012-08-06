package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.gml.x32.AbstractGeometryType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionDocument;
import net.opengis.sampling.x20.SFSamplingFeatureCollectionType;
import net.opengis.sampling.x20.SFSamplingFeaturePropertyType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureDocument;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.om.features.SFConstants;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SamplingEncoderv20 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SamplingEncoderv20.class);

    private List<EncoderKeyType> encoderKeyTypes;

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
        LOGGER.info("Encoder for the following keys initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<EncoderKeyType> getEncoderKeyType() {
        return encoderKeyTypes;
    }

    @Override
    public XmlObject encode(Object response) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XmlObject encode(Object response, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (response instanceof SosAbstractFeature) {
            return createFeature((SosAbstractFeature) response, additionalValues.get(HelperValues.GMLID));
        }
        return null;
    }

    private XmlObject createFeature(SosAbstractFeature absFeature, String gmlID) throws OwsExceptionReport {
        // SFSpatialSamplingFeatureDocument xbSampFeatDoc =
        // SFSpatialSamplingFeatureDocument.Factory
        // .newInstance(SosXmlOptionsUtility.getInstance()
        // .getXmlOptions4Sos2Swe200());
        SFSpatialSamplingFeatureDocument xbSampFeatDoc =
                SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        SFSpatialSamplingFeatureType xbSampFeature = xbSampFeatDoc.addNewSFSpatialSamplingFeature();
        SosSamplingFeature sampFeat = (SosSamplingFeature) absFeature;
        // TODO: CHECK for all fields
        // set gml:id
        xbSampFeature.setId(gmlID);
        if (!gmlID.startsWith("#")) {

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
                    sampledFeat.set(createFeature(sampledFeature, gmlID));
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
                additionalValues.put(HelperValues.GMLID, gmlID);
                XmlObject xmlObject = (XmlObject) encoder.encode(sampFeat.getGeometry(), additionalValues);
                if (xmlObject instanceof AbstractGeometryType) {
                    XmlObject substitution = xbShape.addNewAbstractGeometry().substitute(getQnameForGeometry(sampFeat.getGeometry()), xmlObject.schemaType());
                    substitution.set((AbstractGeometryType)xmlObject);
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

    private QName getQnameForGeometry(Geometry geom) {
        if (geom instanceof Point) {
            return new QName(GMLConstants.NS_GML_32, GMLConstants.EN_POINT, GMLConstants.NS_GML);
        } else if (geom instanceof LineString) {
            return new QName(GMLConstants.NS_GML_32, GMLConstants.EN_LINE_STRING, GMLConstants.NS_GML);
        } else if (geom instanceof Polygon) {
            return new QName(GMLConstants.NS_GML_32, GMLConstants.EN_POLYGON, GMLConstants.NS_GML);
        }
        return new QName(GMLConstants.NS_GML_32, GMLConstants.EN_ABSTRACT_GEOMETRY, GMLConstants.NS_GML);
    }

    private XmlObject createFeatureCollection(Map<SosAbstractFeature, String> foiGmlIds, boolean forObservation)
            throws OwsExceptionReport {

        if (foiGmlIds.size() == 1) {
            for (SosAbstractFeature sosAbstractFeature : foiGmlIds.keySet()) {
                return createFeature(sosAbstractFeature, foiGmlIds.get(sosAbstractFeature));
            }
        }
        // SFSamplingFeatureCollectionDocument xbSampFeatCollDoc =
        // SFSamplingFeatureCollectionDocument.Factory
        // .newInstance(SosXmlOptionsUtility.getInstance()
        // .getXmlOptions4Sos2Swe200());
        SFSamplingFeatureCollectionDocument xbSampFeatCollDoc =
                SFSamplingFeatureCollectionDocument.Factory
                        .newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        SFSamplingFeatureCollectionType xbSampFeatColl = xbSampFeatCollDoc.addNewSFSamplingFeatureCollection();
        xbSampFeatColl.setId("sfc_" + Long.toString(new DateTime().getMillis()));
        for (SosAbstractFeature sosAbstractFeature : foiGmlIds.keySet()) {
            SFSamplingFeaturePropertyType xbFeatMember = xbSampFeatColl.addNewMember();
            if (foiGmlIds.get(sosAbstractFeature).startsWith("#")) {
                xbFeatMember.setHref("#" + foiGmlIds.get(sosAbstractFeature));
            } else {
                xbFeatMember.set(createFeature(sosAbstractFeature, foiGmlIds.get(sosAbstractFeature)));
            }
        }
        return xbSampFeatCollDoc;
    }

}
