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
package org.n52.sos.util;


import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Utility class for the Java Topology Suite.
 */
public class JTSHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JTSHelper.class);
    public static final char C_BLANK = ' ';
    public static final char COMMA = ',';
    public static final String S_BLANK = " ";

    /**
     * Creates a JTS Geometry from an WKT representation. Switches the coordinate order if needed.
     * <p/>
     * @param wkt
     * WKT representation of the geometry
     * @param srid the SRID of the newly created geometry
     * <p/>
     * @return JTS Geometry object
     * <p/>
     * @throws OwsExceptionReport
     * If an error occurs
     * @see Configurator#reversedAxisOrderRequired(int)
     */
    public static Geometry createGeometryFromWKT(String wkt, int srid) throws OwsExceptionReport {
        WKTReader wktReader = getWKTReaderForSRID(srid);
        try {
            LOGGER.debug("FOI Geometry: {}", wkt);
            Geometry geom = wktReader.read(wkt);
            if (Configurator.getInstance().reversedAxisOrderRequired(srid)) {
                geom = reverseCoordinates(geom);
            }
            return geom;
        } catch (ParseException pe) {
            String exceptionText = "Error while parsing the geometry of featureOfInterest parameter";
            LOGGER.error(exceptionText, pe);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, null, exceptionText, pe);
            throw se;
        }
    }
    
    /**
     * Switches the coordinates of a JTS Geometry
     * 
     * @param geom
     *            Geometry to switch coordinates.
     * @return Geometry with switched coordinatas
     */
    protected static Geometry reverseCoordinates(Geometry geom) {
        GeometryFactory factory = geom.getFactory();

        if (geom instanceof GeometryCollection) {
            Geometry[] reversed = new Geometry[geom.getNumGeometries()];
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                reversed[i] = reverseCoordinates(geom.getGeometryN(i));
            }
            return factory.createGeometryCollection(reversed);
        }

        if (geom instanceof MultiPolygon) {
            Polygon[] reversed = new Polygon[geom.getNumGeometries()];
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                reversed[i] = (Polygon) reverseCoordinates(geom.getGeometryN(i));
            }
            return factory.createMultiPolygon(reversed);
        }

        if (geom instanceof MultiPoint) {
            Point[] reversed = new Point[geom.getNumGeometries()];
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                reversed[i] = (Point) reverseCoordinates(geom.getGeometryN(i));
            }
            return factory.createMultiPoint(reversed);
        }

        if (geom instanceof MultiLineString) {
            LineString[] reversed = new LineString[geom.getNumGeometries()];
            for (int i = 0; i < geom.getNumGeometries(); i++) {
                reversed[i] = (LineString) reverseCoordinates(geom.getGeometryN(i));
            }
            return factory.createMultiLineString(reversed);
        }

        // switch coordinates
        Coordinate[] coordinates = geom.getCoordinates();

        for (int i = 0; i < coordinates.length; ++i) {
            coordinates[i] = new Coordinate(coordinates[i].y, coordinates[i].x, coordinates[i].z);
        }
        
        CoordinateArraySequence reversed = new CoordinateArraySequence(coordinates);

        if (geom instanceof Point) {
            return factory.createPoint(reversed);
        }
        if (geom instanceof LineString) {
            return factory.createLineString(reversed);
        }
        if (geom instanceof Polygon) {
            LinearRing shell = factory.createLinearRing(reversed);
            return factory.createPolygon(shell, null);
        }
       
        //TODO throw exception? log warning?
        return null;
    }

    /**
     * Get the coordinates of a Geometry as String. Switches the coordinate order if needed (SRID is taken from the
     * geometry).
     * <p/>
     * @param geom Geometry to get coordinates
     * <p/>
     * @return Coordinates as String
     * <p/>
     * @throws OwsExceptionReport if the SRID is <= 0
     * @see Configurator#reversedAxisOrderRequired(int)
     */
    public static String getCoordinatesString(Geometry geom) throws OwsExceptionReport {
        return getCoordinatesString(geom, geom.getSRID());
    }

    /**
     * Get the coordinates of a Geometry as String. Switches the coordinate order if needed.
     * <p/>
     * @param geom Geometry to get coordinates
     * @param srid the SRID of the geometry
     * <p/>
     * @return Coordinates as String
     * <p/>
     * @throws OwsExceptionReport if the SRID is <= 0
     * @see Configurator#reversedAxisOrderRequired(int)
     */
    public static String getCoordinatesString(Geometry geom, int srid) throws OwsExceptionReport {
        if (srid <= 0) {
            throw Util4Exceptions.createNoApplicableCodeException(null, "SRID may not be <= 0");
        }
        if (Configurator.getInstance().reversedAxisOrderRequired(srid)) {
            return getSwitchedCoordinatesString(geom);
        } else {
            return getNotSwitchedCoordinatesString(geom);
        }
    }

    /**
     * switches the order of coordinates contained in a string, e.g. from String
     * '3.5 4.4' to '4.4 3.5' or '3.5 4.4 2.1' to '4.4 3.5 2.1'.
     * <p/>
     * @param coordinateString the coordinate, which should be switched
     * <p/>
     * @return Returns String contained coordinates in switched order
     * <p/>
     * @throws OwsExceptionReport
     */
    private static String switchCoordinate(String coordinateString) throws OwsExceptionReport {
        String[] coordsArray = coordinateString.split(S_BLANK);
        if (coordsArray.length == 2) {
            return String.format("%f %f", coordsArray[1], coordsArray[0]);
        } else if (coordsArray.length == 3) {
            return String.format("%f %f %f", coordsArray[1], coordsArray[0], coordsArray[2]);
        } else {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, null,
                    "An error occurred, while switching coordinates. Only a pair with two coordinates are supported!");
            LOGGER.error("Error while  switching coordinates. Only a pair with two coordinates are supported! {}", se.getMessage());
            throw se;
        }
    }

    /**
     * Creates a WKT Polygon representation from lower and upper corner values.
     * 
     * @param lowerCorner
     *            Lower corner coordinates
     * @param upperCorner
     *            Upper corner coordinates
     * @return WKT Polygon
     */
    public static String createWKTPolygonFromEnvelope(String lowerCorner, String upperCorner) {
        final String[] splittedLowerCorner = lowerCorner.split(S_BLANK);
        final String[] splittedUpperCorner = upperCorner.split(S_BLANK);
        final String minX = splittedLowerCorner[0];
        final String minY = splittedLowerCorner[1];
        final String maxX = splittedUpperCorner[0];
        final String maxY = splittedUpperCorner[1];
        StringBuilder sb = new StringBuilder();
        sb.append(JTSConstants.WKT_POLYGON).append(" ((");
        sb.append(minX).append(C_BLANK).append(minY).append(COMMA);
        sb.append(minX).append(C_BLANK).append(maxY).append(COMMA);
        sb.append(maxX).append(C_BLANK).append(maxY).append(COMMA);
        sb.append(maxX).append(C_BLANK).append(minY).append(COMMA);
        sb.append(minX).append(C_BLANK).append(minY).append("))");
        return sb.toString();
    }

    private JTSHelper() {
    }

    protected static String getNotSwitchedCoordinatesString(Geometry geom) {
        StringBuilder builder = new StringBuilder();
        Coordinate[] sourceCoords = geom.getCoordinates();
        if (sourceCoords.length > 0) {
            getNotSwitchedCoordinateString(builder, sourceCoords[0]);
            for (int i = 1; i < sourceCoords.length; ++i) {
                getNotSwitchedCoordinateString(builder.append(C_BLANK), sourceCoords[i]);
            }
        }
        return builder.toString();
    }

    protected static String getSwitchedCoordinatesString(Geometry geom) {
        StringBuilder builder = new StringBuilder();
        Coordinate[] sourceCoords = geom.getCoordinates();
        if (sourceCoords.length > 0) {
            getSwitchedCoordinateString(builder, sourceCoords[0]);
            for (int i = 1; i < sourceCoords.length; ++i) {
                getSwitchedCoordinateString(builder.append(C_BLANK), sourceCoords[i]);
            }
        }
        return builder.toString();
    }

    protected static StringBuilder getSwitchedCoordinateString(StringBuilder builder, Coordinate coordinate) {
        builder.append(coordinate.y);
        builder.append(C_BLANK);
        builder.append(coordinate.x);
        return appendZCoordinate(builder, coordinate);
    }

    protected static StringBuilder getNotSwitchedCoordinateString(StringBuilder builder, Coordinate coordinate) {
        builder.append(coordinate.x);
        builder.append(C_BLANK);
        builder.append(coordinate.y);
        return appendZCoordinate(builder, coordinate);
    }

    protected static StringBuilder appendZCoordinate(StringBuilder builder, Coordinate coordinate) {
        if (!Double.isNaN(coordinate.z)) {
            builder.append(C_BLANK);
            builder.append(coordinate.z);
        }
        return builder;
    }

    protected static WKTReader getWKTReaderForSRID(int srid) throws OwsExceptionReport {
        if (srid <= 0) {
            throw Util4Exceptions.createNoApplicableCodeException(null, "SRID may not be <= 0");
        }
        return new WKTReader(new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid));
    }
    
}
