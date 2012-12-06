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

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.AbstractRingType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class GmlEncoderv321 implements IEncoder<XmlObject, Object> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GmlEncoderv321.class);

    private List<EncoderKeyType> encoderKeyTypes;

    public GmlEncoderv321() {
        encoderKeyTypes = new ArrayList<EncoderKeyType>();
        encoderKeyTypes.add(new EncoderKeyType(GMLConstants.NS_GML_32));
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
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }

    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        nameSpacePrefixMap.put(GMLConstants.NS_GML_32, GMLConstants.NS_GML_PREFIX);
    }

    @Override
    public String getContentType() {
        return "text/xml";
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
            if (timePeriod.getId() != null && !timePeriod.getId().isEmpty()) {
                timePeriodType.setId(timePeriod.getId());
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
            if (timeInstant.getId() != null && !timeInstant.getId().isEmpty()) {
                timeInstantType.setId(timeInstant.getId());
            }
            TimePositionType xb_posType = timeInstantType.addNewTimePosition();

            // parse db date string and format into GML format
            DateTime date = timeInstant.getValue();
            String timeString = DateTimeHelper.formatDateTime2ResponseString(date);

            // concat minutes for timeZone offset, because gml requires
            // xs:dateTime,
            // which needs minutes in
            // timezone offset
            // TODO enable really
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
        xbPos.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPoint.getSRID());
        String coords;
        if (Configurator.getInstance().switchCoordinatesForEPSG(jtsPoint.getSRID())) {
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
        xbLst.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2()
                + Integer.toString(jtsLineString.getSRID()));
        DirectPositionListType xbPosList = xbLst.addNewPosList();
        xbPosList.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsLineString.getSRID());
        String coords;
        // switch coordinates
        if (Configurator.getInstance().switchCoordinatesForEPSG(jtsLineString.getSRID())) {
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
        for (int i = 0; i < jtsPolygons.size(); i++) {

            Polygon pol = (Polygon) jtsPolygons.get(i);

            AbstractRingPropertyType xbArpt = xbPolType.addNewExterior();
            AbstractRingType xbArt = xbArpt.addNewAbstractRing();

            LinearRingType xbLrt = LinearRingType.Factory.newInstance();

            // Exterior ring
            LineString ring = pol.getExteriorRing();
            String coords;
            DirectPositionListType xbPosList = xbLrt.addNewPosList();
            xbPosList.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPolygon.getSRID());
            // switch coordinates
            if (Configurator.getInstance().switchCoordinatesForEPSG(jtsPolygon.getSRID())) {
                coords = JTSHelper.switchCoordinates4String(ring);
            } else {
                coords = JTSHelper.getCoordinates4String(ring);
            }
            xbPosList.setStringValue(coords);
            xbArt.set(xbLrt);

            // Rename element name for output
            XmlCursor cursor2 = xbArpt.newCursor();
            boolean hasChild2 = cursor2.toChild(GMLConstants.QN_ABSTRACT_RING_32);
            if (hasChild2) {
                cursor2.setName(GMLConstants.QN_LINEAR_RING_32);
            }

            // Interior ring
            int numberOfInteriorRings = pol.getNumInteriorRing();
            for (int ringNumber = 0; ringNumber < numberOfInteriorRings; ringNumber++) {
                xbArpt = xbPolType.addNewInterior();
                xbArt = xbArpt.addNewAbstractRing();

                xbLrt = LinearRingType.Factory.newInstance();

                ring = pol.getInteriorRingN(ringNumber);

                xbPosList = xbLrt.addNewPosList();
                xbPosList.setSrsName(Configurator.getInstance().getSrsNamePrefixSosV2() + jtsPolygon.getSRID());
                // switch coordinates
                if (Configurator.getInstance().switchCoordinatesForEPSG(jtsPolygon.getSRID())) {
                    coords = JTSHelper.switchCoordinates4String(ring);
                } else {
                    coords = JTSHelper.getCoordinates4String(ring);
                }
                xbPosList.setStringValue(coords);
                xbArt.set(xbLrt);

                // Rename element name for output
                cursor2 = xbArpt.newCursor();
                hasChild2 = cursor2.toChild(GMLConstants.QN_ABSTRACT_RING_32);
                if (hasChild2) {
                    cursor2.setName(GMLConstants.QN_LINEAR_RING_32);
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
            CodeWithAuthorityType codeWithAuthority = CodeWithAuthorityType.Factory.newInstance();
            codeWithAuthority.setStringValue(sosCodeWithAuthority.getValue());
            codeWithAuthority.setCodeSpace(sosCodeWithAuthority.getCodeSpace());
            return codeWithAuthority;
        }
        return null;
    }

}
