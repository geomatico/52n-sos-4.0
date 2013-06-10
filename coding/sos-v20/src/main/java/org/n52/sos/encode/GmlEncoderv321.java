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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.AbstractRingType;
import net.opengis.gml.x32.CodeType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.FeatureCollectionDocument;
import net.opengis.gml.x32.FeatureCollectionType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.FeatureCollection;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.ogc.sos.SosEnvelope;
import org.n52.sos.service.ServiceConfiguration;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.MinMax;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class GmlEncoderv321 implements Encoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GmlEncoderv321.class);

    private static final Set<EncoderKey> ENCODER_KEY_TYPES = CodingHelper.encoderKeysForElements(
            GMLConstants.NS_GML_32,
            org.n52.sos.ogc.gml.time.Time.class,
            com.vividsolutions.jts.geom.Geometry.class,
            org.n52.sos.ogc.om.values.CategoryValue.class,
            org.n52.sos.ogc.gml.ReferenceType.class,
            org.n52.sos.ogc.om.values.QuantityValue.class,
            org.n52.sos.ogc.gml.CodeWithAuthority.class,
            org.n52.sos.ogc.gml.CodeType.class,
            SamplingFeature.class,
            SosEnvelope.class,
            FeatureCollection.class
            );

    public GmlEncoderv321() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEY_TYPES));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEY_TYPES);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(GMLConstants.NS_GML_32, GMLConstants.NS_GML_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.set(GMLConstants.GML_32_SCHEMAL_LOCATION);
    }

    @Override
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, new EnumMap<HelperValues, String>(HelperValues.class));
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof Time) {
            return createTime((Time) element, additionalValues);
        } else if (element instanceof Geometry) {
            return createPosition((Geometry) element, additionalValues.get(HelperValues.GMLID));
        } else if (element instanceof CategoryValue) {
            return createReferenceTypeForCategroyValue((CategoryValue) element);
        } else if (element instanceof org.n52.sos.ogc.gml.ReferenceType) {
            return createReferencType((org.n52.sos.ogc.gml.ReferenceType) element);
        } else if (element instanceof CodeWithAuthority) {
            return createCodeWithAuthorityType((CodeWithAuthority) element);
        } else if (element instanceof QuantityValue) {
            return createMeasureType((QuantityValue)element);
        } else if (element instanceof org.n52.sos.ogc.gml.CodeType) {
            return createCodeType((org.n52.sos.ogc.gml.CodeType) element);
        } else if (element instanceof AbstractFeature) {
            return createFeaturePropertyType((AbstractFeature)element, additionalValues);
        } else if (element instanceof SosEnvelope) {
            return createEnvelope((SosEnvelope)element);
        } else {
            throw new UnsupportedEncoderInputException(this, element);
        }
    }

    private XmlObject createFeaturePropertyType(AbstractFeature feature, Map<HelperValues, String> additionalValues)
            throws OwsExceptionReport {
        if (feature instanceof FeatureCollection) {
             return createFeatureCollection((FeatureCollection)feature, additionalValues);
        } else if (feature instanceof SamplingFeature) {
             return createFeature((SamplingFeature)feature, additionalValues);
        } else {
            throw new UnsupportedEncoderInputException(this, feature);
        }
    }

    private XmlObject createFeatureCollection(FeatureCollection element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        FeatureCollectionDocument featureCollectionDoc = FeatureCollectionDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        FeatureCollectionType featureCollection = featureCollectionDoc.addNewFeatureCollection();
        featureCollection.setId(element.getGmlId());
        if (element.isSetMembers()) {
            for (AbstractFeature abstractFeature : element.getMembers().values()) {
                featureCollection.addNewFeatureMember().set(createFeaturePropertyType(abstractFeature, new HashMap<HelperValues, String>(0)));
            }
        } 
        if (additionalValues.containsKey(HelperValues.DOCUMENT)) {
           return featureCollectionDoc;
        }
        FeaturePropertyType featurePropertyType = FeaturePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        featurePropertyType.addNewAbstractFeature().set(featureCollection);
        return XmlHelper.substituteElement(featurePropertyType.getAbstractFeature(), featurePropertyType);
//        return featureCollection;
    }

    private XmlObject createFeature(AbstractFeature feature, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        FeaturePropertyType featurePropertyType = FeaturePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (!(feature instanceof SamplingFeature)) {
            featurePropertyType.setHref(feature.getIdentifier().getValue());
            return featurePropertyType;
        } else {
            SamplingFeature samplingFeature = (SamplingFeature) feature;
            if (samplingFeature.isSetGmlID()) {
                featurePropertyType.setHref("#" + samplingFeature.getGmlId());
                return featurePropertyType;
            } else {
                if (additionalValues.containsKey(HelperValues.ENCODE) && additionalValues.get(HelperValues.ENCODE).equals("false") || !samplingFeature.isEncode()) {
                    featurePropertyType.setHref(feature.getIdentifier().getValue());
                    if (feature instanceof SamplingFeature && samplingFeature.isSetNames()) {
                        featurePropertyType.setTitle(samplingFeature.getFirstName().getValue());
                    } 
                }
                if (!samplingFeature.isSetGeometry()) {
                    featurePropertyType.setHref(samplingFeature.getIdentifier().getValue());
                    if (samplingFeature.isSetNames()) {
                        featurePropertyType.setTitle(samplingFeature.getFirstName().getValue());
                    }
                    return featurePropertyType;
                } 
                if (samplingFeature.getUrl() != null) {
                    featurePropertyType.setHref(samplingFeature.getUrl());
                    if (samplingFeature.isSetNames()) {
                        featurePropertyType.setTitle(samplingFeature.getFirstName().getValue());
                    }
                    return featurePropertyType;
                } else {
                    String namespace;
                    if (additionalValues.containsKey(HelperValues.ENCODE_NAMESPACE)) {
                        namespace = additionalValues.get(HelperValues.ENCODE_NAMESPACE);
                    } else {
                        namespace = OMHelper.getNamespaceForFeatureType(samplingFeature.getFeatureType());
                    }
                    XmlObject encodedXmlObject = CodingHelper.encodeObjectToXml(namespace, samplingFeature);
    
                    if (encodedXmlObject != null) {
                        return encodedXmlObject;
                    } else {
                        if (samplingFeature.getXmlDescription() != null) {
                            try {
                                // TODO how set gml:id in already existing
                                // XmlDescription?
                                return XmlObject.Factory.parse(samplingFeature.getXmlDescription());
                            } catch (XmlException xmle) {
                                throw new NoApplicableCodeException().causedBy(xmle)
                                        .withMessage("Error while encoding featurePropertyType!");
                            }
                        } else {
                            featurePropertyType.setHref(samplingFeature.getIdentifier().getValue());
                            if (samplingFeature.isSetNames()) {
                                featurePropertyType.setTitle(samplingFeature.getFirstName().getValue());
                            }
                            return featurePropertyType;
                        }
                    }
                }
            }
        }
    }

    private XmlObject createEnvelope(SosEnvelope sosEnvelope) {
        Envelope envelope = sosEnvelope.getEnvelope();
        int srid = sosEnvelope.getSrid();
        EnvelopeType envelopeType = EnvelopeType.Factory.newInstance();
        MinMax<String> minmax = SosHelper.getMinMaxFromEnvelope(envelope);
        envelopeType.addNewLowerCorner().setStringValue(minmax.getMinimum());
        envelopeType.addNewUpperCorner().setStringValue(minmax.getMaximum());
        envelopeType.setSrsName(getSrsName(srid));
        return envelopeType;
    }

    private XmlObject createTime(Time time, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (time != null) {
            if (time instanceof TimeInstant) {
                if (additionalValues.containsKey(HelperValues.DOCUMENT)) {
                    return createTimeInstantDocument((TimeInstant) time);
                } else {
                    return createTimeInstantType((TimeInstant) time, null);
                }
            } else if (time instanceof TimePeriod) {
                if (additionalValues.containsKey(HelperValues.DOCUMENT)) {
                    return createTimePeriodDocument((TimePeriod) time);
                } else {
                    return createTimePeriodType((TimePeriod) time, null);
                }
            } else {
                throw new UnsupportedEncoderInputException(this, time);
            }
        }
        return null;
    }

    private XmlObject createTimePeriodDocument(TimePeriod time) throws OwsExceptionReport {
        TimePeriodDocument timePeriodDoc =
                TimePeriodDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        createTimePeriodType(time, timePeriodDoc.addNewTimePeriod());
        return timePeriodDoc;
    }

    /**
     * Creates a XML TimePeriod from the SOS time object.
     *
     * @param timePeriod
     *            SOS time object
     * @param timePeriodType
     * @return XML TimePeriod

     *
     * @throws OwsExceptionReport * if an error occurs.
     */
    private TimePeriodType createTimePeriodType(TimePeriod timePeriod, TimePeriodType timePeriodType)
            throws OwsExceptionReport {
        if (timePeriodType == null) {
            timePeriodType = TimePeriodType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        }
        if (timePeriod.getGmlId() != null && !timePeriod.getGmlId().isEmpty()) {
            timePeriodType.setId(timePeriod.getGmlId());
        } else {
            timePeriodType.setId("tp_" + JavaHelper.generateID(timePeriod.toString() + System.currentTimeMillis()));
        }
        // beginPosition
        TimePositionType xbTimePositionBegin = TimePositionType.Factory.newInstance();
        String beginString =  DateTimeHelper.formatDateTime2String(timePeriod.getStart(), timePeriod.getTimeFormat());

        // concat minutes for timeZone offset, because gml requires
        // xs:dateTime, which needs minutes in
        // timezone offset
        // TODO enable really
        xbTimePositionBegin.setStringValue(beginString);

        // endPosition
        TimePositionType xbTimePositionEnd = TimePositionType.Factory.newInstance();
        String endString =  DateTimeHelper.formatDateTime2String(timePeriod.getEnd(), timePeriod.getTimeFormat());

        // concat minutes for timeZone offset, because gml requires
        // xs:dateTime, which needs minutes in
        // timezone offset
        // TODO enable really
        xbTimePositionEnd.setStringValue(endString);

        timePeriodType.setBeginPosition(xbTimePositionBegin);
        timePeriodType.setEndPosition(xbTimePositionEnd);

        return timePeriodType;
    }

    private XmlObject createTimeInstantDocument(TimeInstant time) throws OwsExceptionReport {
        TimeInstantDocument timeInstantDoc =
                TimeInstantDocument.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        createTimeInstantType(time, timeInstantDoc.addNewTimeInstant());
        return timeInstantDoc;
    }

    /**
     * Creates a XML TimeInstant from the SOS time object.
     *
     * @param timeInstant
     *            SOS time object
     * @param timeInstantType
     * @param xbTime
     * @return XML TimeInstant

     *
     * @throws OwsExceptionReport * if an error occurs.
     */
    private TimeInstantType createTimeInstantType(TimeInstant timeInstant, TimeInstantType timeInstantType)
            throws OwsExceptionReport {
        // create time instant
        if (timeInstantType == null) {
            timeInstantType = TimeInstantType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        }
        if (timeInstant.isSetGmlId()) {
            timeInstantType.setId(timeInstant.getGmlId());
        } else {
            timeInstantType.setId("ti_" + JavaHelper.generateID(timeInstantType.toString() + System.currentTimeMillis()));
        }
        TimePositionType xbTimePosition = timeInstantType.addNewTimePosition();

        String timeString = OGCConstants.UNKNOWN;
        if(timeInstant.isSetValue()) {
            // parse db date string and format into GML format
            timeString =  DateTimeHelper.formatDateTime2String(timeInstant.getValue(), timeInstant.getTimeFormat());
            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime,
            // which needs minutes in
            // timezone offset
            // TODO enable really
        } else if (timeInstant.isSetIndeterminateValue()) {
            timeString = timeInstant.getIndeterminateValue();
        }
        xbTimePosition.setStringValue(timeString);
        return timeInstantType;
    }

    private XmlObject createPosition(Geometry geom, String foiId) throws OwsExceptionReport {

        if (geom instanceof Point) {
            PointType xbPoint = PointType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbPoint.setId("point_" + foiId);
            createPointFromJtsGeometry((Point) geom, xbPoint);
            return xbPoint;
        }

        else if (geom instanceof LineString) {
            LineStringType xbLineString =
                    LineStringType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbLineString.setId("lineString_" + foiId);
            createLineStringFromJtsGeometry((LineString) geom, xbLineString);
            return xbLineString;
        }

        else if (geom instanceof Polygon) {
            PolygonType xbPolygon = PolygonType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbPolygon.setId("polygon_" + foiId);
            createPolygonFromJtsGeometry((Polygon) geom, xbPolygon);
            return xbPolygon;
        } else {
            throw new UnsupportedEncoderInputException(this, geom);
        }
    }

    /**
     * Creates a XML Point from a SOS Point.
     *
     * @param jtsPoint
     *            SOS Point
     * @param xbPoint
     *            XML Point
     */
    private void createPointFromJtsGeometry(Point jtsPoint, PointType xbPoint) throws OwsExceptionReport {
        DirectPositionType xbPos = xbPoint.addNewPos();
        xbPos.setSrsName(getSrsName(jtsPoint));
        xbPos.setStringValue(JTSHelper.getCoordinatesString(jtsPoint));
    }

    /**
     * Creates a XML LineString from a SOS LineString.
     *
     * @param jtsLineString
     *            SOS LineString
     * @param xbLst
     *            XML LinetSring
     */
    private void createLineStringFromJtsGeometry(LineString jtsLineString, LineStringType xbLst) throws
            OwsExceptionReport {
        final String srsName = getSrsName(jtsLineString);
        xbLst.setSrsName(srsName);
        DirectPositionListType xbPosList = xbLst.addNewPosList();
        xbPosList.setSrsName(srsName);
        xbPosList.setStringValue(JTSHelper.getCoordinatesString(jtsLineString));

    }

    /**
     * Creates a XML Polygon from a SOS Polygon.
     *
     * @param jtsPolygon
     *            SOS Polygon
     * @param xbPolType
     *            XML Polygon
     */
    private void createPolygonFromJtsGeometry(Polygon jtsPolygon, PolygonType xbPolType) throws OwsExceptionReport {
        List<?> jtsPolygons = PolygonExtracter.getPolygons(jtsPolygon);
        final String srsName = getSrsName(jtsPolygon);
        
        for (int i = 0; i < jtsPolygons.size(); i++) {

            Polygon pol = (Polygon) jtsPolygons.get(i);

            AbstractRingPropertyType xbArpt = xbPolType.addNewExterior();
            AbstractRingType xbArt = xbArpt.addNewAbstractRing();

            LinearRingType xbLrt = LinearRingType.Factory.newInstance();

            // Exterior ring
            LineString ring = pol.getExteriorRing();
            DirectPositionListType xbPosList = xbLrt.addNewPosList();
            
            xbPosList.setSrsName(srsName);
            xbPosList.setStringValue(JTSHelper.getCoordinatesString(ring));
            xbArt.set(xbLrt);

            // Rename element name for output
            XmlCursor cursor = xbArpt.newCursor();
            if (cursor.toChild(GMLConstants.QN_ABSTRACT_RING_32)) {
                cursor.setName(GMLConstants.QN_LINEAR_RING_32);
            }

            // Interior ring
            int numberOfInteriorRings = pol.getNumInteriorRing();
            for (int ringNumber = 0; ringNumber < numberOfInteriorRings; ringNumber++) {
                xbArpt = xbPolType.addNewInterior();
                xbArt = xbArpt.addNewAbstractRing();

                xbLrt = LinearRingType.Factory.newInstance();

                ring = pol.getInteriorRingN(ringNumber);

                xbPosList = xbLrt.addNewPosList();
                xbPosList.setSrsName(srsName);
                xbPosList.setStringValue(JTSHelper.getCoordinatesString(ring));
                xbArt.set(xbLrt);

                // Rename element name for output
                cursor = xbArpt.newCursor();
                if (cursor.toChild(GMLConstants.QN_ABSTRACT_RING_32)) {
                    cursor.setName(GMLConstants.QN_LINEAR_RING_32);
                }
            }
        }
    }

    private XmlObject createReferenceTypeForCategroyValue(CategoryValue categoryValue) {
        ReferenceType xbRef = ReferenceType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (categoryValue.isSetValue()) {
            if (categoryValue.getValue().startsWith("http://")) {
                xbRef.setHref(categoryValue.getValue());
            } else {
                xbRef.setTitle(categoryValue.getValue());
            }
        } else {
            xbRef.setNil();
        }
        return xbRef;
    }

    private ReferenceType createReferencType(org.n52.sos.ogc.gml.ReferenceType sosReferenceType) {
        if (!sosReferenceType.isSetHref()) {
            String exceptionText = String.format("The required 'href' parameter is empty for encoding %s!", ReferenceType.class.getName());
            LOGGER.error(exceptionText);
            throw new IllegalArgumentException(exceptionText);
        }
        ReferenceType referenceType = ReferenceType.Factory.newInstance();
        referenceType.setHref(sosReferenceType.getHref());
        if (sosReferenceType.isSetTitle()) {
            referenceType.setTitle(sosReferenceType.getTitle());
        }
        if (sosReferenceType.isSetRole()) {
            referenceType.setRole(sosReferenceType.getRole());
        }
        return referenceType;
    }

    private CodeWithAuthorityType createCodeWithAuthorityType(CodeWithAuthority sosCodeWithAuthority) {
        if (!sosCodeWithAuthority.isSetValue()) {
            String exceptionText = String.format("The required 'value' parameter is empty for encoding %s!", CodeWithAuthorityType.class.getName());
            LOGGER.error(exceptionText);
            throw new IllegalArgumentException(exceptionText);
        }
        CodeWithAuthorityType codeWithAuthority = CodeWithAuthorityType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        codeWithAuthority.setStringValue(sosCodeWithAuthority.getValue());
        if (sosCodeWithAuthority.isSetCodeSpace()) {
            codeWithAuthority.setCodeSpace(sosCodeWithAuthority.getCodeSpace());
        } else {
            codeWithAuthority.setCodeSpace(OGCConstants.UNKNOWN);
        }
        return codeWithAuthority;
    }

    private CodeType createCodeType(org.n52.sos.ogc.gml.CodeType sosCodeType) {
        if (!sosCodeType.isSetValue()) {
            String exceptionText = String.format("The required 'value' parameter is empty for encoding %s!", CodeType.class.getName());
            LOGGER.error(exceptionText);
            throw new IllegalArgumentException(exceptionText);
        }
        CodeType codeType = CodeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        codeType.setStringValue(sosCodeType.getValue());
        if (sosCodeType.isSetCodeSpace()) {
            codeType.setCodeSpace(sosCodeType.getCodeSpace());
        } else {
            codeType.setCodeSpace(OGCConstants.UNKNOWN);
        }
        return codeType;
    }

    protected MeasureType createMeasureType(QuantityValue quantityValue) throws OwsExceptionReport {
        if (!quantityValue.isSetValue()) {
            throw new NoApplicableCodeException()
                    .withMessage("The required 'value' parameter is empty for encoding %s!", MeasureType.class.getName());
        }
        MeasureType measureType =
                MeasureType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        measureType.setDoubleValue(quantityValue.getValue().doubleValue());
        if (quantityValue.isSetUnit()) {
            measureType.setUom(quantityValue.getUnit());
        } else {
            measureType.setUom(OGCConstants.UNKNOWN);
        }

        return measureType;
    }

    protected String getSrsName(Geometry geom) {
        return getSrsName(geom.getSRID());
    }

    protected String getSrsName(int srid) {
        return ServiceConfiguration.getInstance().getSrsNamePrefixSosV2() + srid;
    }
}