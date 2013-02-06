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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.AbstractRingPropertyType;
import net.opengis.gml.AbstractRingType;
import net.opengis.gml.CodeType;
import net.opengis.gml.DirectPositionListType;
import net.opengis.gml.DirectPositionType;
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
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.StringHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class GmlEncoderv311 implements IEncoder<XmlObject, Object> {

	private static final Logger LOGGER = LoggerFactory.getLogger(GmlEncoderv311.class);
	
    private static final Set<EncoderKey> ENCODER_KEYS = CodingHelper.encoderKeysForElements(
    		GMLConstants.NS_GML, 
    		org.n52.sos.ogc.gml.time.ITime.class,
            com.vividsolutions.jts.geom.Geometry.class,
            org.n52.sos.ogc.om.values.CategoryValue.class,
            org.n52.sos.ogc.gml.ReferenceType.class,
            org.n52.sos.ogc.om.values.QuantityValue.class,
            org.n52.sos.ogc.gml.CodeWithAuthority.class,
            org.n52.sos.ogc.gml.CodeType.class
            );

    public GmlEncoderv311() {
        LOGGER.debug("Encoder for the following keys initialized successfully: {}!", StringHelper.join(", ", ENCODER_KEYS));
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
    public XmlObject encode(Object element) throws OwsExceptionReport {
        return encode(element, new HashMap<HelperValues, String>());
    }

    @Override
    public XmlObject encode(Object element, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        if (element instanceof ITime) {
            return createTime((ITime) element, additionalValues);
        }
        if (element instanceof Geometry) {
            return createPosition((Geometry) element, additionalValues.get(HelperValues.GMLID));
        }
        if (element instanceof CategoryValue) {
            return createReferenceTypeForCategroyValue((CategoryValue) element);
        }
        if (element instanceof org.n52.sos.ogc.gml.ReferenceType) {
            return createReferencType((org.n52.sos.ogc.gml.ReferenceType) element);
        }
        if (element instanceof CodeWithAuthority) {
            return createCodeWithAuthorityType((CodeWithAuthority) element);
        }
        if (element instanceof QuantityValue) {
            return createMeasureType((QuantityValue)element);
        }
        if (element instanceof org.n52.sos.ogc.gml.CodeType) {
            return createCodeType((org.n52.sos.ogc.gml.CodeType) element);
        }
        return null;
    }

    private XmlObject createTime(ITime time, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
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
     * @throws OwsExceptionReport
     *             if an error occurs.
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
            TimePositionType xbTimePositionBegin = TimePositionType.Factory.newInstance();
            String beginString = DateTimeHelper.formatDateTime2ResponseString(timePeriod.getStart());

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime, which needs minutes in
            // timezone offset
            // TODO enable really
            xbTimePositionBegin.setStringValue(beginString);

            // endPosition
            TimePositionType xbTimePositionEnd = TimePositionType.Factory.newInstance();
            String endString = DateTimeHelper.formatDateTime2ResponseString(timePeriod.getEnd());

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime, which needs minutes in
            // timezone offset
            // TODO enable really
            xbTimePositionEnd.setStringValue(endString);

            timePeriodType.setBeginPosition(xbTimePositionBegin);
            timePeriodType.setEndPosition(xbTimePositionEnd);

            return timePeriodType;
        } catch (DateTimeException dte) {
            String exceptionText = "Error while creating TimePeriod!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        } catch (XmlRuntimeException xre) {
        	String exceptionText = "Error while creating TimePeriod!";
            LOGGER.error(exceptionText, xre);
            throw Util4Exceptions.createNoApplicableCodeException(xre, exceptionText);
        } catch (XmlValueDisconnectedException xvde) {
        	String exceptionText = "Error while creating TimePeriod!";
        	xvde.printStackTrace();
            LOGGER.error(exceptionText, xvde);
            throw Util4Exceptions.createNoApplicableCodeException(xvde, exceptionText);
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
     * @throws OwsExceptionReport
     *             if an error occurs.
     */
    private TimeInstantType createTimeInstantType(TimeInstant timeInstant, TimeInstantType timeInstantType)
            throws OwsExceptionReport {
        try {
            // create time instant
            if (timeInstantType == null) {
                timeInstantType = TimeInstantType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
            }
            if (timeInstant.isSetGmlId()) {
                timeInstantType.setId(timeInstant.getGmlId());
            }
            TimePositionType xb_posType = timeInstantType.addNewTimePosition();

            String timeString = OGCConstants.UNKNOWN;
            if(timeInstant.isSetValue()) {
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
            xb_posType.setStringValue(timeString);
            return timeInstantType;
        } catch (DateTimeException dte) {
            String exceptionText = "Error while creating TimeInstant!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
    }

    private XmlObject createPosition(Geometry geom, String foiId) {

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
        }
        return null;
    }

    /**
     * Creates a XML Point from a SOS Point.
     * 
     * @param jtsPoint
     *            SOS Point
     * @param xbPoint
     *            XML Point
     */
    private void createPointFromJtsGeometry(Point jtsPoint, PointType xbPoint) {
        DirectPositionType xbPos = xbPoint.addNewPos();
        // FIXME find better place for this constant
        // xbPos.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPoint.getSRID());
        final String srsPrefixSosV1 = "urn:ogc:def:crs:EPSG::";
        xbPos.setSrsName(srsPrefixSosV1 + jtsPoint.getSRID());
        String coords;
        if (Configurator.getInstance().reversedAxisOrderRequired(jtsPoint.getSRID())) {
            coords = JTSHelper.switchCoordinates4String(jtsPoint);
        } else {
            coords = JTSHelper.getCoordinates4String(jtsPoint);
        }
        xbPos.setStringValue(coords);
    }

    /**
     * Creates a XML LineString from a SOS LineString.
     * 
     * @param jtsLineString
     *            SOS LineString
     * @param xbLst
     *            XML LinetSring
     */
    private void createLineStringFromJtsGeometry(LineString jtsLineString, LineStringType xbLst) {
    	// FIXME find better place for this constant
        // xbPos.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPoint.getSRID());
        final String srsPrefixSosV1 = "urn:ogc:def:crs:EPSG::";
        xbLst.setSrsName(srsPrefixSosV1
                + Integer.toString(jtsLineString.getSRID()));
        DirectPositionListType xbPosList = xbLst.addNewPosList();
        xbPosList.setSrsName(srsPrefixSosV1 + jtsLineString.getSRID());
        String coords;
        // switch coordinates
        if (Configurator.getInstance().reversedAxisOrderRequired(jtsLineString.getSRID())) {
            coords = JTSHelper.switchCoordinates4String(jtsLineString);
        } else {
            coords = JTSHelper.getCoordinates4String(jtsLineString);
        }
        xbPosList.setStringValue(coords);

    }

    /**
     * Creates a XML Polygon from a SOS Polygon.
     * 
     * @param jtsPolygon
     *            SOS Polygon
     * @param xbPolType
     *            XML Polygon
     */
    private void createPolygonFromJtsGeometry(Polygon jtsPolygon, PolygonType xbPolType) {
        List<?> jtsPolygons = PolygonExtracter.getPolygons(jtsPolygon);
     // FIXME find better place for this constant
        // xbPos.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPoint.getSRID());
        final String srsPrefixSosV1 = "urn:ogc:def:crs:EPSG::";
        for (int i = 0; i < jtsPolygons.size(); i++) {

            Polygon pol = (Polygon) jtsPolygons.get(i);

            AbstractRingPropertyType xbArpt = xbPolType.addNewExterior();
            AbstractRingType xbArt = xbArpt.addNewRing();

            LinearRingType xbLrt = LinearRingType.Factory.newInstance();

            // Exterior ring
            LineString ring = pol.getExteriorRing();
            String coords;
            DirectPositionListType xbPosList = xbLrt.addNewPosList();
            xbPosList.setSrsName(srsPrefixSosV1 + jtsPolygon.getSRID());
            // switch coordinates
            if (Configurator.getInstance().reversedAxisOrderRequired(jtsPolygon.getSRID())) {
                coords = JTSHelper.switchCoordinates4String(ring);
            } else {
                coords = JTSHelper.getCoordinates4String(ring);
            }
            xbPosList.setStringValue(coords);
            xbArt.set(xbLrt);

            // Rename element name for output
            XmlCursor cursor2 = xbArpt.newCursor();
            boolean hasChild2 = cursor2.toChild(GMLConstants.QN_ABSTRACT_RING);
            if (hasChild2) {
                cursor2.setName(GMLConstants.QN_LINEAR_RING);
            }

            // Interior ring
            int numberOfInteriorRings = pol.getNumInteriorRing();
            for (int ringNumber = 0; ringNumber < numberOfInteriorRings; ringNumber++) {
                xbArpt = xbPolType.addNewInterior();
                xbArt = xbArpt.addNewRing();

                xbLrt = LinearRingType.Factory.newInstance();

                ring = pol.getInteriorRingN(ringNumber);

                xbPosList = xbLrt.addNewPosList();
                xbPosList.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPolygon.getSRID());
                // switch coordinates
                if (Configurator.getInstance().reversedAxisOrderRequired(jtsPolygon.getSRID())) {
                    coords = JTSHelper.switchCoordinates4String(ring);
                } else {
                    coords = JTSHelper.getCoordinates4String(ring);
                }
                xbPosList.setStringValue(coords);
                xbArt.set(xbLrt);

                // Rename element name for output
                cursor2 = xbArpt.newCursor();
                hasChild2 = cursor2.toChild(GMLConstants.QN_ABSTRACT_RING);
                if (hasChild2) {
                    cursor2.setName(GMLConstants.QN_LINEAR_RING);
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

    private XmlObject createMeasureType(QuantityValue quantityValue) {
        MeasureType measureType =
                MeasureType.Factory.newInstance(XmlOptionsHelper.getInstance().getXmlOptions());
        if (quantityValue.getUnit() != null) {
            measureType.setUom(quantityValue.getUnit());
        } else {
            measureType.setUom("");
        }
        if (!quantityValue.getValue().equals(Double.NaN)) {
            measureType.setDoubleValue(quantityValue.getValue().doubleValue());
        } else {
            measureType.setNil();
        }
        return measureType;
    }

}