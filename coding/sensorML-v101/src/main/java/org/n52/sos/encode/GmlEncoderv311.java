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

import javax.xml.namespace.QName;

import net.opengis.gml.AbstractFeatureCollectionType;
import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.CodeType;
import net.opengis.gml.DirectPositionListType;
import net.opengis.gml.DirectPositionType;
import net.opengis.gml.EnvelopeType;
import net.opengis.gml.FeatureCollectionDocument2;
import net.opengis.gml.FeaturePropertyType;
import net.opengis.gml.LineStringType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.MeasureType;
import net.opengis.gml.PointType;
import net.opengis.gml.PolygonType;
import net.opengis.gml.ReferenceType;
import net.opengis.gml.TimeInstantDocument;
import net.opengis.gml.TimeInstantType;
import net.opengis.gml.TimePeriodDocument;
import net.opengis.gml.TimePeriodType;
import net.opengis.gml.TimePositionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlRuntimeException;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.joda.time.DateTime;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.features.SFConstants;
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
import org.n52.sos.util.SchemaLocation;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class GmlEncoderv311 implements Encoder<XmlObject, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GmlEncoderv311.class);

    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(GMLConstants.NS_GML,
            org.n52.sos.ogc.gml.time.Time.class, com.vividsolutions.jts.geom.Geometry.class,
            org.n52.sos.ogc.om.values.CategoryValue.class, org.n52.sos.ogc.gml.ReferenceType.class,
            org.n52.sos.ogc.om.values.QuantityValue.class, org.n52.sos.ogc.gml.CodeWithAuthority.class,
            org.n52.sos.ogc.gml.CodeType.class, AbstractFeature.class, SosEnvelope.class);

    public GmlEncoderv311() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!",
                StringHelper.join(", ", ENCODER_KEYS));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(ENCODER_KEYS);
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
        nameSpacePrefixMap.put(GMLConstants.NS_GML, GMLConstants.NS_GML_PREFIX);
    }

    @Override
    public String getContentType() {
        return SosConstants.CONTENT_TYPE_XML;
    }

    @Override
    public Set<SchemaLocation> getSchemaLocations() {
        return CollectionHelper.set(GMLConstants.GML_311_SCHEMAL_LOCATION);
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
            return createMeasureType((QuantityValue) element);
        } else if (element instanceof org.n52.sos.ogc.gml.CodeType) {
            return createCodeType((org.n52.sos.ogc.gml.CodeType) element);
        } else if (element instanceof AbstractFeature) {
            return createFeature((AbstractFeature) element);
        } else if (element instanceof SosEnvelope) {
            return createEnvelope((SosEnvelope)element);
        } else {
            throw new UnsupportedEncoderInputException(this, element);
        }
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
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs.
     */
    private TimePeriodType createTimePeriodType(TimePeriod timePeriod, TimePeriodType timePeriodType)
            throws OwsExceptionReport {
        try {
            if (timePeriodType == null) {
                timePeriodType = TimePeriodType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            }
            if (timePeriod.getGmlId() != null && !timePeriod.getGmlId().isEmpty()) {
                timePeriodType.setId(timePeriod.getGmlId());
            }
            // beginPosition
            TimePositionType xbTimePositionBegin =
                    TimePositionType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            String beginString = DateTimeHelper.formatDateTime2ResponseString(timePeriod.getStart());

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime, which needs minutes in
            // timezone offset
            // TODO enable really
            xbTimePositionBegin.setStringValue(beginString);

            // endPosition
            TimePositionType xbTimePositionEnd =
                    TimePositionType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            String endString = DateTimeHelper.formatDateTime2ResponseString(timePeriod.getEnd());

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime, which needs minutes in
            // timezone offset
            // TODO enable really
            xbTimePositionEnd.setStringValue(endString);

            timePeriodType.setBeginPosition(xbTimePositionBegin);
            timePeriodType.setEndPosition(xbTimePositionEnd);

            return timePeriodType;
        } catch (XmlRuntimeException x) {
            throw new NoApplicableCodeException().causedBy(x).withMessage("Error while creating TimePeriod!");
        } catch (XmlValueDisconnectedException x) {
            throw new NoApplicableCodeException().causedBy(x).withMessage("Error while creating TimePeriod!");
        }
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
     * 
     * @throws OwsExceptionReport
     *             * if an error occurs.
     */
    private TimeInstantType createTimeInstantType(TimeInstant timeInstant, TimeInstantType timeInstantType)
            throws OwsExceptionReport {
        // create time instant
        if (timeInstantType == null) {
            timeInstantType = TimeInstantType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        }
        if (timeInstant.isSetGmlId()) {
            timeInstantType.setId(timeInstant.getGmlId());
        }
        TimePositionType xbPosType = timeInstantType.addNewTimePosition();

        String timeString = OGCConstants.UNKNOWN;
        if (timeInstant.isSetValue()) {
            // parse db date string and format into GML format
            DateTime date = timeInstant.getValue();
            timeString = DateTimeHelper.formatDateTime2ResponseString(date);
            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime,
            // which needs minutes in
            // timezone offset
            // TODO enable really
        } else if (timeInstant.isSetIndeterminateValue()) {
            timeString = timeInstant.getIndeterminateValue();
        }
        xbPosType.setStringValue(timeString);
        return timeInstantType;
    }

    private XmlObject createPosition(Geometry geom, String foiId) throws OwsExceptionReport {
        if (geom instanceof Point) {
            PointType xbPoint = PointType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbPoint.setId("point_" + foiId);
            createPointFromJtsGeometry((Point) geom, xbPoint);
            return xbPoint;
        } else if (geom instanceof LineString) {
            LineStringType xbLineString =
                    LineStringType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbLineString.setId("lineString_" + foiId);
            createLineStringFromJtsGeometry((LineString) geom, xbLineString);
            return xbLineString;
        } else if (geom instanceof Polygon) {
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
    private void createLineStringFromJtsGeometry(LineString jtsLineString, LineStringType xbLst)
            throws OwsExceptionReport {
        DirectPositionListType xbPosList = xbLst.addNewPosList();
        xbPosList.setSrsName(getSrsName(jtsLineString));
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
        for (int i = 0; i < jtsPolygons.size(); i++) {

            Polygon pol = (Polygon) jtsPolygons.get(i);

            AbstractRingPropertyType xbArpt = xbPolType.addNewExterior();
            AbstractRingType xbArt = xbArpt.addNewRing();

            LinearRingType xbLrt = LinearRingType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());

            // Exterior ring
            LineString ring = pol.getExteriorRing();
            String coords = JTSHelper.getCoordinatesString(ring);
            DirectPositionListType xbPosList = xbLrt.addNewPosList();
            xbPosList.setSrsName(getSrsName(jtsPolygon));
            // switch coordinates
            xbPosList.setStringValue(coords);
            xbArt.set(xbLrt);

            // Rename element name for output
            XmlCursor cursor = xbArpt.newCursor();
            if (cursor.toChild(GMLConstants.QN_ABSTRACT_RING)) {
                cursor.setName(GMLConstants.QN_LINEAR_RING);
            }

            // Interior ring
            int numberOfInteriorRings = pol.getNumInteriorRing();
            for (int ringNumber = 0; ringNumber < numberOfInteriorRings; ringNumber++) {
                xbArpt = xbPolType.addNewInterior();
                xbArt = xbArpt.addNewRing();

                xbLrt = LinearRingType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());

                ring = pol.getInteriorRingN(ringNumber);

                xbPosList = xbLrt.addNewPosList();
                xbPosList.setSrsName(getSrsName(jtsPolygon));
                xbPosList.setStringValue(JTSHelper.getCoordinatesString(ring));
                xbArt.set(xbLrt);

                // Rename element name for output
                cursor = xbArpt.newCursor();
                if (cursor.toChild(GMLConstants.QN_ABSTRACT_RING)) {
                    cursor.setName(GMLConstants.QN_LINEAR_RING);
                }
            }
        }
    }

    private XmlObject createReferenceTypeForCategroyValue(CategoryValue categoryValue) {
        ReferenceType xbRef = ReferenceType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (categoryValue.getValue() != null && !categoryValue.getValue().isEmpty()) {
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

    private XmlObject createReferencType(org.n52.sos.ogc.gml.ReferenceType sosReferenceType) {
        if (sosReferenceType.isSetHref()) {
            ReferenceType referenceType =
                    ReferenceType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            referenceType.setHref(sosReferenceType.getHref());
            if (sosReferenceType.isSetTitle()) {
                referenceType.setTitle(sosReferenceType.getTitle());
            }
            if (sosReferenceType.isSetRole()) {
                referenceType.setRole(sosReferenceType.getRole());
            }
            return referenceType;
        }
        return null;

    }

    private XmlObject createCodeWithAuthorityType(CodeWithAuthority sosCodeWithAuthority) {
        if (sosCodeWithAuthority.isSetValue()) {
            CodeType codeType = CodeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            String value = sosCodeWithAuthority.getValue();
            codeType.setStringValue(value);
            codeType.setCodeSpace(sosCodeWithAuthority.getCodeSpace());
            return codeType;
        }
        return null;
    }

    private XmlObject createCodeType(org.n52.sos.ogc.gml.CodeType sosCodeType) {
        CodeType codeType = CodeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        codeType.setCodeSpace(sosCodeType.getCodeSpace());
        codeType.setStringValue(sosCodeType.getValue());
        return codeType;
    }

    protected XmlObject createMeasureType(QuantityValue quantityValue) {
        MeasureType measureType = MeasureType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (quantityValue.getUnit() != null) {
            measureType.setUom(quantityValue.getUnit());
        } else {
            measureType.setUom("");
        }
        if (quantityValue.getValue() != null) {
            measureType.setDoubleValue(quantityValue.getValue().doubleValue());
        } else {
            measureType.setNil();
        }
        return measureType;
    }

    private XmlObject createFeature(AbstractFeature sosAbstractFeature) throws OwsExceptionReport {
        if (sosAbstractFeature instanceof SamplingFeature) {
            SamplingFeature sampFeat = (SamplingFeature) sosAbstractFeature;
            if (sosAbstractFeature.isSetGmlID()) {
                FeaturePropertyType featureProperty =
                        FeaturePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                featureProperty.setHref("#" + sosAbstractFeature.getGmlId());
                return featureProperty;
            } else {
                if (!sampFeat.isSetGeometry()) {
                    FeaturePropertyType featureProperty =
                            FeaturePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                    featureProperty.setHref(sosAbstractFeature.getIdentifier().getValue());
                    if (sampFeat.isSetNames()) {
                        featureProperty.setTitle(sampFeat.getFirstName().getValue());
                    }
                    return featureProperty;
                }
                StringBuilder builder = new StringBuilder();
                builder.append("sf_");
                builder.append(JavaHelper.generateID(sosAbstractFeature.getIdentifier().getValue()));
                sosAbstractFeature.setGmlId(builder.toString());
                Encoder<XmlObject, SamplingFeature> encoder = CodingHelper.getEncoder(SFConstants.NS_SA, sampFeat);
                if (encoder != null) {
                    return encoder.encode(sampFeat);
                } else {
                    FeaturePropertyType featureProperty =
                            FeaturePropertyType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                    featureProperty.setHref(sampFeat.getIdentifier().getValue());
                    if (sampFeat.isSetNames()) {
                        featureProperty.setTitle(sampFeat.getFirstName().getValue());
                    }
                    return featureProperty;
                }
            }
        } else if (sosAbstractFeature instanceof FeatureCollection) {
            return createFeatureCollection((FeatureCollection) sosAbstractFeature);
        }
        throw new UnsupportedEncoderInputException(this, sosAbstractFeature);
    }

    private XmlObject createFeatureCollection(FeatureCollection sosFeatureCollection) throws OwsExceptionReport {
        Map<String, AbstractFeature> members = sosFeatureCollection.getMembers();
        XmlObject xmlObject = null;
        if (sosFeatureCollection.isSetMembers()) {
            if (members.size() == 1) {
                for (String member : members.keySet()) {
                    if (members.get(member) instanceof SamplingFeature) {
                        return createFeature((SamplingFeature) members.get(member));
                    } else {
                        throw new NoApplicableCodeException().withMessage("No encoder found for featuretype");
                    }
                }
            } else {
                FeatureCollectionDocument2 xbFeatureColllectionDoc =
                        FeatureCollectionDocument2.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
                AbstractFeatureCollectionType xbFeatCol = xbFeatureColllectionDoc.addNewFeatureCollection();
                StringBuilder builder = new StringBuilder();
                builder.append("sfc_");
                builder.append(JavaHelper.generateID(Long.toString(System.currentTimeMillis())));
                xbFeatCol.setId(builder.toString());
                for (String member : members.keySet()) {
                    if (members.get(member) instanceof SamplingFeature) {
                        XmlObject xmlFeature = createFeature((SamplingFeature) members.get(member));
                        xbFeatCol.addNewFeatureMember().set(xmlFeature);
                    } else {
                        throw new NoApplicableCodeException().withMessage("No encoder found for featuretype");
                    }
                }
                xmlObject = xbFeatureColllectionDoc;
            }
        } else {
            FeatureCollectionDocument2 xbFeatColDoc =
                    FeatureCollectionDocument2.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            xbFeatColDoc.addNewFeatureCollection();
            xmlObject = xbFeatColDoc;
        }
        XmlCursor cursor = xmlObject.newCursor();
        boolean isAFC = cursor.toChild(new QName(GMLConstants.NS_GML, GMLConstants.EN_ABSTRACT_FEATURE_COLLECTION));
        if (isAFC) {
            cursor.setName(new QName(GMLConstants.NS_GML, GMLConstants.EN_FEATURE_COLLECTION));
        }
        cursor.dispose();
        return xmlObject;
    }

    private XmlObject createEnvelope(SosEnvelope sosEnvelope) {
        EnvelopeType envelopeType = EnvelopeType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        MinMax<String> minmax = SosHelper.getMinMaxFromEnvelope(sosEnvelope.getEnvelope());
        envelopeType.addNewLowerCorner().setStringValue(minmax.getMinimum());
        envelopeType.addNewUpperCorner().setStringValue(minmax.getMaximum());
        envelopeType.setSrsName(ServiceConfiguration.getInstance().getSrsNamePrefix() + sosEnvelope.getSrid());
        return envelopeType;
    }

    protected String getSrsName(Geometry geom) {
        return ServiceConfiguration.getInstance().getSrsNamePrefix() + geom.getSRID();
    }
}