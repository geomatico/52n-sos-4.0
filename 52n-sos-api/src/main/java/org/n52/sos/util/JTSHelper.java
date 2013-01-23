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

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Utility class for the Java Topology Suite.
 */
public class JTSHelper {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JTSHelper.class);

    /**
     * Creates a JTS Geometry from an WKT representation.
     * 
     * @param wktString
     *            WKT representation of the geometry
     * @return JTS Geometry object
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public static Geometry createGeometryFromWKT(String wktString) throws OwsExceptionReport {
        WKTReader wktReader = new WKTReader();
        Geometry geom = null;
        try {
            LOGGER.debug("FOI Geometry: " + wktString);
            geom = wktReader.read(wktString);
        } catch (ParseException pe) {
            String exceptionText = "Error while parsing the geometry of featureOfInterest parameter";
            LOGGER.error(exceptionText, pe);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, null, exceptionText, pe);
            throw se;
        }

        return geom;
    }

    /**
     * Switches the coordinates of a JTS Geometry
     * 
     * @param sourceGeom
     *            Geometry to switch coordinates.
     * @return Geometry with switched coordinatas
     */
    public static Geometry switchCoordinate4Geometry(Geometry sourceGeom) {

        GeometryFactory geomFac = new GeometryFactory();
        Geometry switchedGeom = null;

        if (sourceGeom instanceof MultiPolygon) {
            Polygon[] switchedPolygons = new Polygon[sourceGeom.getNumGeometries()];
            for (int i = 0; i < sourceGeom.getNumGeometries(); i++) {
                switchedPolygons[i] = (Polygon) switchCoordinate4Geometry(sourceGeom.getGeometryN(i));
            }
            switchedGeom = geomFac.createMultiPolygon(switchedPolygons);
        } else {

            // switch coordinates
            Coordinate[] coordArraySource = sourceGeom.getCoordinates();
            List<Coordinate> coordList = new ArrayList<Coordinate>(coordArraySource.length);
            for (Coordinate coordinate : coordArraySource) {
                if (Double.doubleToLongBits(coordinate.z) == Double.doubleToLongBits(Double.NaN)) {
                    coordList.add(new Coordinate(coordinate.y, coordinate.x, coordinate.z));
                }
                // else if(Double.doubleToLongBits(coordinate.z) ==
                // Double.doubleToLongBits(Double.NaN)) {
                // coordList.add(new Coordinate(coordinate.y, coordinate.x,
                // coordinate.z));
                // }
                else {
                    coordList.add(new Coordinate(coordinate.y, coordinate.x));
                }
            }
            Coordinate[] coordArraySwitched = coordList.toArray(coordArraySource);
            CoordinateArraySequence coordSeqArray = new CoordinateArraySequence(coordArraySwitched);

            // create new geometry with switched coordinates.
            if (sourceGeom instanceof Point) {
                Point point = new Point(coordSeqArray, geomFac);
                switchedGeom = point;
            } else if (sourceGeom instanceof LineString) {
                LineString line = new LineString(coordSeqArray, geomFac);
                switchedGeom = line;
            } else if (sourceGeom instanceof Polygon) {
                Polygon polygon = new Polygon(new LinearRing(coordSeqArray, geomFac), null, geomFac);
                switchedGeom = polygon;
            } else if (sourceGeom instanceof MultiPoint) {
                MultiPoint multiPoint = geomFac.createMultiPoint(coordArraySource);
                switchedGeom = multiPoint;
            }
        }
        if (switchedGeom != null) {
            switchedGeom.setSRID(sourceGeom.getSRID());
        }

        return switchedGeom;
    }

    /**
     * Switches the coordinates of a Geometry and returns it as a String.
     * 
     * @param sourceGeom
     *            sourceGeom Geometry to switch coordinates.
     * @return Switched coordinates as a String
     */
    public static String switchCoordinates4String(Geometry sourceGeom) {
        StringBuilder switchedCoords = new StringBuilder();
        Coordinate[] sourceCoords = sourceGeom.getCoordinates();

        for (Coordinate coordinate : sourceCoords) {
            switchedCoords.append(coordinate.y);
            switchedCoords.append(" ");
            switchedCoords.append(coordinate.x);
            if (Double.isNaN(coordinate.z)) {
                switchedCoords.append(" ");
            } else {
                switchedCoords.append(" ");
                switchedCoords.append(coordinate.z).append(" ");
            }
        }

        if (switchedCoords.toString().endsWith(" ")) {
            switchedCoords.delete(switchedCoords.toString().length() - 1, switchedCoords.toString().length());
        }
        return switchedCoords.toString();
    }

    /**
     * Get the coordinates of a Geometry as String
     * 
     * @param sourceGeom
     *            Geometry to get coordinates
     * @return Coordinates as String
     */
    public static String getCoordinates4String(Geometry sourceGeom) {
        StringBuilder stringCoords = new StringBuilder();
        Coordinate[] sourceCoords = sourceGeom.getCoordinates();

        for (Coordinate coordinate : sourceCoords) {
            stringCoords.append(coordinate.x);
            stringCoords.append(" ");
            stringCoords.append(coordinate.y);
            if (Double.isNaN(coordinate.z)) {
                stringCoords.append(" ");
            } else {
                stringCoords.append(" ");
                stringCoords.append(coordinate.z).append(" ");
            }
        }

        if (stringCoords.toString().endsWith(" ")) {
            stringCoords.delete(stringCoords.toString().length() - 1, stringCoords.toString().length());
        }
        return stringCoords.toString();
    }

    /**
     * switches the order of coordinates contained in a string, e.g. from String
     * '3.5 4.4' to '4.4 3.5'
     * 
     * NOTE: ACTUALLY checks, whether dimension is 2D, otherwise throws
     * Exception!!
     * 
     * @param coordsString
     *            contains coordinates, which should be switched
     * @return Returns String contained coordinates in switched order
     * @throws OwsExceptionReport
     */
    public static String switchCoordinatesInString(String coordsString) throws OwsExceptionReport {
        String switchedCoordString = null;
        String[] coordsArray = coordsString.split(" ");
        if (coordsArray.length != 2) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, null,
                    "An error occurred, while switching coordinates. Only a pair with two coordinates are supported!");
            LOGGER.error("Error while  switching coordinates. Only a pair with two coordinates are supported! "
                    + se.getMessage());
            throw se;
        } else {
            switchedCoordString = coordsArray[1] + " " + coordsArray[0];
        }
        return switchedCoordString;
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
        String minX = lowerCorner.split(" ")[0];
        String minY = lowerCorner.split(" ")[1];
        String maxX = upperCorner.split(" ")[0];
        String maxY = upperCorner.split(" ")[1];
        StringBuilder sb = new StringBuilder();
        sb.append(JTSConstants.WKT_POLYGON).append(" ((");
        sb.append(minX).append(" ").append(minY).append(",");
        sb.append(minX).append(" ").append(maxY).append(",");
        sb.append(maxX).append(" ").append(maxY).append(",");
        sb.append(maxX).append(" ").append(minY).append(",");
        sb.append(minX).append(" ").append(minY).append("))");
        return sb.toString();
    }

    private JTSHelper() {
    }
}
