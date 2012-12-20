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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.AbstractRingType;
import net.opengis.gml.x32.AbstractSurfaceType;
import net.opengis.gml.x32.CodeWithAuthorityType;
import net.opengis.gml.x32.CompositeSurfaceType;
import net.opengis.gml.x32.CoordinatesType;
import net.opengis.gml.x32.DirectPositionListType;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.EnvelopeDocument;
import net.opengis.gml.x32.EnvelopeType;
import net.opengis.gml.x32.LineStringType;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.MeasureType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.PolygonType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.SurfacePropertyType;
import net.opengis.gml.x32.TimeInstantDocument;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodDocument;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;

import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.GMLConstants;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class GmlDecoderv321 implements IDecoder<Object, XmlObject> {

    /**
     * logger, used for logging while initializing the constants from config
     * file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GmlDecoderv321.class);

    private List<DecoderKeyType> decoderKeyTypes;

    private static final String CS = ",";

    private static final String DECIMAL = ".";

    private static final String TS = " ";

    public GmlDecoderv321() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        decoderKeyTypes.add(new DecoderKeyType(GMLConstants.NS_GML_32));
        decoderKeyTypes.add(new DecoderKeyType(MeasureType.type.toString()));
        StringBuilder builder = new StringBuilder();
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            builder.append(decoderKeyType.toString());
            builder.append(", ");
        }
        builder.delete(builder.lastIndexOf(", "), builder.length());
        LOGGER.info("Decoder for the following namespaces initialized successfully: " + builder.toString() + "!");
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }

    @Override
    public Object decode(XmlObject xmlObject) throws OwsExceptionReport {
        if (xmlObject instanceof EnvelopeDocument) {
            return getGeometry4BBOX((EnvelopeDocument) xmlObject);
        } else if (xmlObject instanceof TimeInstantType) {
            return parseTimeInstant((TimeInstantType) xmlObject);
        } else if (xmlObject instanceof TimePeriodType) {
            return parseTimePeriod((TimePeriodType) xmlObject);
        } else if (xmlObject instanceof TimeInstantDocument) {
            return parseTimeInstant(((TimeInstantDocument) xmlObject).getTimeInstant());
        } else if (xmlObject instanceof TimePeriodDocument) {
            return parseTimePeriod(((TimePeriodDocument) xmlObject).getTimePeriod());
        } else if (xmlObject instanceof ReferenceType) {
            return parseReferenceType((ReferenceType) xmlObject);
        } else if (xmlObject instanceof MeasureType) {
            return parseMeasureType((MeasureType) xmlObject);
        } else if (xmlObject instanceof PointType) {
            return parsePointType((PointType) xmlObject);
        } else if (xmlObject instanceof LineStringType) {
            return parseLineStringType((LineStringType) xmlObject);
        } else if (xmlObject instanceof PolygonType) {
            return parsePolygonType((PolygonType) xmlObject);
        } else if (xmlObject instanceof CompositeSurfaceType) {
            return parseCompositeSurfaceType((CompositeSurfaceType) xmlObject);
        } else if (xmlObject instanceof CodeWithAuthorityType) {
            return parseCodeWithAuthorityTye((CodeWithAuthorityType) xmlObject);
        }
        return null;
    }

    /**
     * parses the BBOX element of the featureOfInterest element contained in the
     * GetObservation request and returns a String representing the BOX in
     * Well-Known-Text format
     * 
     * @param xb_bbox
     *            XmlBean representing the BBOX-element in the request
     * @return Returns WKT-String representing the BBOX as Multipoint with two
     *         elements
     * @throws OwsExceptionReport
     *             if parsing the BBOX element failed
     */
    private Geometry getGeometry4BBOX(EnvelopeDocument envelopeDocument) throws OwsExceptionReport {
        EnvelopeType envelopeType = envelopeDocument.getEnvelope();
        int srid =
                SosHelper.parseSrsName(envelopeType.getSrsName());
        String lowerCorner = envelopeType.getLowerCorner().getStringValue();
        String upperCorner = envelopeType.getUpperCorner().getStringValue();
        if (Configurator.getInstance().reversedAxisOrderRequired(srid)) {
            lowerCorner = switchCoordinatesInString(lowerCorner);
            upperCorner = switchCoordinatesInString(upperCorner);
        }
        Geometry geom =
                JTSHelper.createGeometryFromWKT(JTSHelper.createWKTPolygonFromEnvelope(lowerCorner, upperCorner));
        geom.setSRID(srid);
        return geom;
    }

    // /**
    // * Parses a XML time object to a SOS representation.
    // *
    // * @param xbTemporalOpsType
    // * XML time object
    // * @return SOS time object representation.
    // * @throws OwsExceptionReport
    // */
    // private ISosTime parseTime(TemporalOpsType xbTemporalOpsType) throws
    // OwsExceptionReport {
    // ISosTime sosTime = null;
    // XmlCursor timeCursor = xbTemporalOpsType.newCursor();
    // try {
    // if (timeCursor.toChild(GMLConstants.QN_TIME_INSTANT_32)) {
    // sosTime = parseTimeInstantNode(timeCursor.getDomNode());
    // } else if (timeCursor.toChild(GMLConstants.QN_TIME_PERIOD_32)) {
    // sosTime = parseTimePeriodNode(timeCursor.getDomNode());
    // } else {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // "Time",
    // "The requested time type is not supported by this SOS!");
    // LOGGER.error("The requested time type is not supported by this SOS!");
    // throw se;
    // }
    // } catch (XmlException xmle) {
    // OwsExceptionReport se = new OwsExceptionReport();
    // se.addCodedException(OwsExceptionReport.ExceptionCode.InvalidParameterValue,
    // "Time",
    // "Error while parsing time: " + xmle.getMessage());
    // LOGGER.error(se.getMessage() + xmle.getMessage());
    // throw se;
    // }
    // timeCursor.dispose();
    // return sosTime;
    // }

    /**
     * switches the order of coordinates contained in a string, e.g. from String
     * '3.5 4.4' to '4.4 3.5'
     * 
     * NOTE: ACTUALLY checks, whether dimension is 2D, othewise throws
     * Exception!!
     * 
     * @param coordsString
     *            contains coordinates, which should be switched
     * @return Returns String contained coordinates in switched order
     * @throws OwsExceptionReport
     */
    private String switchCoordinatesInString(String coordsString) throws OwsExceptionReport {
        String switchedCoordString = null;
        String[] coordsArray = coordsString.split(" ");
        if (coordsArray.length != 2) {
            String exceptionText =
                    "An error occurred, while switching coordinates. Only a pair with two coordinates are supported!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        } else {
            switchedCoordString = coordsArray[1] + " " + coordsArray[0];
        }
        return switchedCoordString;
    }

    /**
     * parses TimeInstant
     * 
     * @param tp
     *            XmlBean representation of TimeInstant
     * @return Returns a TimeInstant created from the TimeInstantType
     * @throws java.text.ParseException
     * @throws java.text.ParseException
     *             if parsing the datestring into java.util.Date failed
     * @throws OwsExceptionReport
     */
    private TimeInstant parseTimeInstant(TimeInstantType xbTimeIntant) throws OwsExceptionReport {
        try {
            TimeInstant ti = new TimeInstant();
            TimePositionType xbTimePositionType = xbTimeIntant.getTimePosition();
            String timeString = xbTimePositionType.getStringValue();
            if (timeString != null && !timeString.equals("")) {
                ti.setValue(DateTimeHelper.parseIsoString2DateTime(timeString));
                ti.setRequestedTimeLength(timeString.length());
            }
            if (xbTimePositionType.getIndeterminatePosition() != null) {
                ti.setIndeterminateValue(xbTimePositionType.getIndeterminatePosition().toString());
                ti.setRequestedTimeLength(xbTimePositionType.getIndeterminatePosition().toString().length());
            }
            return ti;
        } catch (DateTimeException dte) {
            String exceptionText = "Error while parsing TimeInstant!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
    }

    /**
     * creates SOS representation of time period from XMLBeans representation of
     * time period
     * 
     * @param xb_timePeriod
     *            XMLBeans representation of time period
     * @return Returns SOS representation of time period
     * @throws OwsExceptionReport
     */
    private TimePeriod parseTimePeriod(TimePeriodType xbTimePeriod) throws OwsExceptionReport {
        try {
            // begin position
            TimePositionType xbBeginTPT = xbTimePeriod.getBeginPosition();
            DateTime begin = null;
            if (xbBeginTPT != null) {
                String beginString = xbBeginTPT.getStringValue();
                begin = DateTimeHelper.parseIsoString2DateTime(beginString);
            } else {
                String exceptionText = "gml:TimePeriod! must contain beginPos Element with valid ISO:8601 String!!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

            // end position
            DateTime end = null;
            TimePositionType xbEndTPT = xbTimePeriod.getEndPosition();
            if (xbEndTPT != null) {
                String endString = xbEndTPT.getStringValue();
                end =
                        DateTimeHelper.setDateTime2EndOfDay4RequestedEndPosition(
                                DateTimeHelper.parseIsoString2DateTime(endString), endString.length());
            } else {
                String exceptionText = "gml:TimePeriod! must contain endPos Element with valid ISO:8601 String!!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }

            return new TimePeriod(begin, end);
        } catch (DateTimeException dte) {
            String exceptionText = "Error while parsing TimePeriod!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
    }

    private SosSingleObservationValue parseReferenceType(ReferenceType referenceType) {
        if (referenceType.getHref() != null && !referenceType.getHref().isEmpty()) {
            return new SosSingleObservationValue(new CategoryValue(referenceType.getHref()));
        } else if (referenceType.getTitle() != null && !referenceType.getTitle().isEmpty()) {
            return new SosSingleObservationValue(new CategoryValue(referenceType.getTitle()));
        }
        return new SosSingleObservationValue();
    }

    private SosSingleObservationValue parseMeasureType(MeasureType measureType) {
        QuantityValue quantityValue = new QuantityValue(measureType.getDoubleValue());
        quantityValue.setUnit(measureType.getUom());
        return new SosSingleObservationValue(quantityValue);
    }

    private Object parsePointType(PointType xbPointType) throws OwsExceptionReport {
        Geometry geom = null;
        String geomWKT = null;
        int srid = -1;
        if (xbPointType.getSrsName() != null) {
            srid =
                    SosHelper.parseSrsName(xbPointType.getSrsName());
        }

        if (xbPointType.getPos() != null) {
            DirectPositionType xbPos = xbPointType.getPos();
            if (srid == -1 && xbPos.getSrsName() != null) {
                srid = SosHelper.parseSrsName(xbPos.getSrsName());
            }
            String directPosition = getString4Pos(xbPos);
            geomWKT = "POINT(" + directPosition + ")";
        } else if (xbPointType.getCoordinates() != null) {
            CoordinatesType xbCoords = xbPointType.getCoordinates();
            String directPosition = getString4Coordinates(xbCoords);
            geomWKT = "POINT" + directPosition;
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("For geometry type 'gml:Point' only element ");
            exceptionText.append("'gml:pos' and 'gml:coordinates' are allowed ");
            exceptionText.append("in the feature of interest parameter!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }

        checkSrid(srid);
        if (srid == -1) {
            StringBuilder exceptionText = new StringBuilder("No SrsName ist specified for geometry!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }
        geom = JTSHelper.createGeometryFromWKT(geomWKT);
        geom.setSRID(srid);

        return geom;
    }

    private Object parseLineStringType(LineStringType xbLineStringType) throws OwsExceptionReport {
        Geometry geom = null;
        String geomWKT = null;
        int srid = -1;
        if (xbLineStringType.getSrsName() != null) {
            srid =
                    SosHelper.parseSrsName(xbLineStringType.getSrsName());
        }

        DirectPositionType[] xbPositions = xbLineStringType.getPosArray();

        StringBuffer positions = new StringBuffer();
        if (xbPositions != null && xbPositions.length > 0) {
            if (srid == -1 && xbPositions[0].getSrsName() != null && !(xbPositions[0].getSrsName().equals(""))) {
                srid =
                        SosHelper.parseSrsName(xbPositions[0].getSrsName());
            }
            positions.append(getString4PosArray(xbLineStringType.getPosArray()));
        }
        geomWKT = "LINESTRING" + positions.toString() + "";

        checkSrid(srid);

        geom = JTSHelper.createGeometryFromWKT(geomWKT);
        geom.setSRID(srid);

        return geom;
    }

    private Object parsePolygonType(PolygonType xbPolygonType) throws OwsExceptionReport {
        Geometry geom = null;
        int srid = -1;
        if (xbPolygonType.getSrsName() != null) {
            srid =
                    SosHelper.parseSrsName(xbPolygonType.getSrsName());
        }
        String exteriorCoordString = null;
        StringBuilder geomWKT = new StringBuilder();
        StringBuilder interiorCoordString = new StringBuilder();

        AbstractRingPropertyType xbExterior = xbPolygonType.getExterior();

        if (xbExterior != null) {
            AbstractRingType xbExteriorRing = xbExterior.getAbstractRing();
            if (xbExteriorRing instanceof LinearRingType) {
                LinearRingType xbLinearRing = (LinearRingType) xbExteriorRing;
                exteriorCoordString = getCoordString4LinearRing(xbLinearRing);
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The Polygon must contain the following elements ");
                exceptionText.append("<gml:exterior><gml:LinearRing><gml:posList>!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        }

        AbstractRingPropertyType[] xbInterior = xbPolygonType.getInteriorArray();
        AbstractRingPropertyType xbInteriorRing;
        if (xbInterior != null && xbInterior.length != 0) {
            for (int i = 0; i < xbInterior.length; i++) {
                xbInteriorRing = xbInterior[i];
                if (xbInteriorRing instanceof LinearRingType) {
                    interiorCoordString.append(", " + getCoordString4LinearRing((LinearRingType) xbInteriorRing));
                }
            }
        }

        geomWKT.append("POLYGON(");
        geomWKT.append(exteriorCoordString);
        if (interiorCoordString != null) {
            geomWKT.append(interiorCoordString);
        }
        geomWKT.append(")");

        checkSrid(srid);
        geom = JTSHelper.createGeometryFromWKT(geomWKT.toString());
        geom.setSRID(srid);

        return geom;
    }

    private Geometry parseCompositeSurfaceType(CompositeSurfaceType xbCompositeSurface) throws OwsExceptionReport {
        SurfacePropertyType[] xbCurfaceProperties = xbCompositeSurface.getSurfaceMemberArray();
        int srid = -1;
        ArrayList<Polygon> polygons = new ArrayList<Polygon>(xbCurfaceProperties.length);
        if (xbCompositeSurface.getSrsName() != null) {
            srid =
                    SosHelper.parseSrsName(xbCompositeSurface.getSrsName());
        }
        for (SurfacePropertyType xbSurfaceProperty : xbCurfaceProperties) {
            AbstractSurfaceType xbAbstractSurface = xbSurfaceProperty.getAbstractSurface();
            if (srid == -1 && xbAbstractSurface.getSrsName() != null) {
                srid =
                        SosHelper.parseSrsName(xbAbstractSurface.getSrsName());
            }
            if (xbAbstractSurface instanceof PolygonType) {
                polygons.add((Polygon) parsePolygonType((PolygonType) xbAbstractSurface));
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The FeatureType ");
                exceptionText.append(xbAbstractSurface);
                exceptionText.append(" is not supportted! Only PolygonType");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        }
        if (polygons.isEmpty()) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The FeatureType: ");
            exceptionText.append(xbCompositeSurface);
            exceptionText.append(" does not contain any member!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }
        checkSrid(srid);
        GeometryFactory factory = new GeometryFactory();
        Geometry geom = factory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
        geom.setSRID(srid);
        return geom;
    }

    private CodeWithAuthority parseCodeWithAuthorityTye(CodeWithAuthorityType xbCodeWithAuthority) {
        if (xbCodeWithAuthority.getStringValue() != null && !xbCodeWithAuthority.getStringValue().isEmpty()) {
            CodeWithAuthority sosCodeWithAuthority = new CodeWithAuthority(xbCodeWithAuthority.getStringValue());
            sosCodeWithAuthority.setCodeSpace(xbCodeWithAuthority.getCodeSpace());
            return sosCodeWithAuthority;
        }
        return null;
    }

    /**
     * method parses the passed linearRing(generated thru XmlBEans) and returns
     * a string containing the coordinate values of the passed ring
     * 
     * @param xbLinearRing
     *            linearRing(generated thru XmlBEans)
     * @return Returns a string containing the coordinate values of the passed
     *         ring
     * @throws OwsExceptionReport
     *             if parsing the linear Ring failed
     */
    private String getCoordString4LinearRing(LinearRingType xbLinearRing) throws OwsExceptionReport {

        String result = "";
        DirectPositionListType xbPosList = xbLinearRing.getPosList();
        CoordinatesType xbCoordinates = xbLinearRing.getCoordinates();
        DirectPositionType[] xbPosArray = xbLinearRing.getPosArray();
        if (xbPosList != null && !(xbPosList.getStringValue().equals(""))) {
            result = getString4PosList(xbPosList);
        } else if (xbCoordinates != null && !(xbCoordinates.getStringValue().equals(""))) {
            result = getString4Coordinates(xbCoordinates);
        } else if (xbPosArray != null && xbPosArray.length > 0) {
            result = getString4PosArray(xbPosArray);
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The Polygon must contain the following elements ");
            exceptionText.append("<gml:exterior><gml:LinearRing><gml:posList>, ");
            exceptionText.append("<gml:exterior><gml:LinearRing><gml:coordinates> ");
            exceptionText.append("or <gml:exterior><gml:LinearRing><gml:pos>{<gml:pos>}!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }

        return result;
    }// end getCoordStrig4LinearRing

    /**
     * parses XmlBeans DirectPosition to a String with coordinates for WKT.
     * 
     * @param xbPos
     *            XmlBeans generated DirectPosition.
     * @return Returns String with coordinates for WKT.
     */
    private String getString4Pos(DirectPositionType xbPos) {
        StringBuffer coordinateString = new StringBuffer();

        coordinateString.append(xbPos.getStringValue());

        return coordinateString.toString();
    }

    /**
     * parses XmlBeans DirectPosition[] to a String with coordinates for WKT.
     * 
     * @param xbPosArray
     *            XmlBeans generated DirectPosition[].
     * @return Returns String with coordinates for WKT.
     */
    private String getString4PosArray(DirectPositionType[] xbPosArray) {
        StringBuffer coordinateString = new StringBuffer();
        coordinateString.append("(");
        for (DirectPositionType directPositionType : xbPosArray) {
            coordinateString.append(directPositionType.getStringValue());
            coordinateString.append(", ");
        }
        coordinateString.append(xbPosArray[0].getStringValue());
        coordinateString.append(")");

        return coordinateString.toString();
    }

    /**
     * parses XmlBeans DirectPositionList to a String with coordinates for WKT.
     * 
     * @param xbPosList
     *            XmlBeans generated DirectPositionList.
     * @return Returns String with coordinates for WKT.
     * @throws OwsExceptionReport
     */
    private String getString4PosList(DirectPositionListType xbPosList) throws OwsExceptionReport {
        StringBuffer coordinateString = new StringBuffer("(");
        List<?> values = xbPosList.getListValue();
        if ((values.size() % 2) != 0) {
            String exceptionText = "The Polygons posList must contain pairs of coordinates!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        } else {
            for (int i = 0; i < values.size(); i++) {
                coordinateString.append(values.get(i));
                if ((i % 2) != 0) {
                    coordinateString.append(", ");
                } else {
                    coordinateString.append(" ");
                }
            }
        }
        int length = coordinateString.length();
        coordinateString.delete(length - 2, length);
        coordinateString.append(")");

        return coordinateString.toString();
    }

    /**
     * parses XmlBeans Coordinates to a String with coordinates for WKT.
     * Replaces cs, decimal and ts if different from default.
     * 
     * @param xbCoordinates
     *            XmlBeans generated Coordinates.
     * @return Returns String with coordinates for WKT.
     */
    private String getString4Coordinates(CoordinatesType xbCoordinates) {
        String coordinateString = "";

        coordinateString = "(" + xbCoordinates.getStringValue() + ")";

        // replace cs, decimal and ts if different from default.
        if (!xbCoordinates.getCs().equals(CS)) {
            coordinateString = coordinateString.replace(xbCoordinates.getCs(), CS);
        }
        if (!xbCoordinates.getDecimal().equals(DECIMAL)) {
            coordinateString = coordinateString.replace(xbCoordinates.getDecimal(), DECIMAL);
        }
        if (!xbCoordinates.getTs().equals(TS)) {
            coordinateString = coordinateString.replace(xbCoordinates.getTs(), TS);
        }

        return coordinateString;
    }

    private void checkSrid(int srid) throws OwsExceptionReport {
        if (srid == 0 || srid == -1) {
            StringBuilder exceptionText = new StringBuilder("No SrsName ist specified for geometry!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
        }
    }

}
