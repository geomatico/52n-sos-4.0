package org.n52.sos.encode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public XmlObject encode(SosAbstractFeature response) throws OwsExceptionReport {
        return encode(response, null);
    }

    @Override
    public XmlObject encode(SosAbstractFeature response, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (response instanceof SosAbstractFeature) {
            return createFeature((SosAbstractFeature) response, additionalValues.get(HelperValues.GMLID));
        }
        return null;
    }
    
    private XmlObject createFeature(SosAbstractFeature absFeature, String gmlID) throws OwsExceptionReport {
        SosSamplingFeature sampFeat = (SosSamplingFeature) absFeature;
        SFSpatialSamplingFeatureDocument xbSampFeatDoc =
                SFSpatialSamplingFeatureDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (sampFeat.getXmlDescription() != null) {
            try {
                XmlObject feature = XmlObject.Factory.parse(sampFeat.getXmlDescription());
                if (feature instanceof SFSpatialSamplingFeatureDocument) {
                    xbSampFeatDoc = (SFSpatialSamplingFeatureDocument)feature;
               } else if (feature instanceof SFSpatialSamplingFeatureType) {
                   xbSampFeatDoc.setSFSpatialSamplingFeature((SFSpatialSamplingFeatureType)feature);
               }
                XmlHelper.updateGmlIDs(xbSampFeatDoc.getDomNode(), gmlID, null);
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
                        XmlObject substitution =
                                xbShape.addNewAbstractGeometry().substitute(GmlHelper.getQnameForGeometry(sampFeat.getGeometry()),
                                        xmlObject.schemaType());
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
        }
        return xbSampFeatDoc;
    }

    private XmlObject createFeatureCollection(Map<SosAbstractFeature, String> foiGmlIds, boolean forObservation)
            throws OwsExceptionReport {

        if (foiGmlIds.size() == 1) {
            for (SosAbstractFeature sosAbstractFeature : foiGmlIds.keySet()) {
                return createFeature(sosAbstractFeature, foiGmlIds.get(sosAbstractFeature));
            }
        }
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
