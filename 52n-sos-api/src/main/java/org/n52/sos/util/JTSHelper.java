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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
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
    public static final CoordinateFilter COORDINATE_SWITCHING_FILTER = new CoordinateFilter() {
        @Override 
        public void filter(Coordinate coord) {
            double tmp = coord.x;
            coord.x = coord.y;
            coord.y = tmp;
        }
    };

    /**
     * Creates a JTS Geometry from an WKT representation. Switches the coordinate order if needed.
     * <p/>
     * @param wkt
     * WKT representation of the geometry
     * @param srid the SRID of the newly created geometry
     * <p/>
     * @return JTS Geometry object
     * <p/>
     * @throws OwsExceptionReport If an error occurs
     */
    public static Geometry createGeometryFromWKT(String wkt, int srid) throws OwsExceptionReport {
        WKTReader wktReader = getWKTReaderForSRID(srid);
        try {
            LOGGER.debug("FOI Geometry: {}", wkt);
            return wktReader.read(wkt);
        } catch (ParseException pe) {
            String exceptionText = "Error while parsing the geometry of featureOfInterest parameter";
            LOGGER.error(exceptionText, pe);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, null, exceptionText, pe);
            throw se;
        }
    }

    public static WKTReader getWKTReaderForSRID(int srid) throws OwsExceptionReport {
        if (srid <= 0) {
            throw Util4Exceptions.createNoApplicableCodeException(null, "SRID may not be <= 0");
        }
        return new WKTReader(getGeometryFactoryForSRID(srid));
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
     */
    public static String getCoordinatesString(Geometry geom) throws OwsExceptionReport {
        StringBuilder builder = new StringBuilder();
        Coordinate[] sourceCoords = geom.getCoordinates();
        if (sourceCoords.length > 0) {
            getCoordinateString(builder, sourceCoords[0]);
            for (int i = 1; i < sourceCoords.length; ++i) {
                getCoordinateString(builder.append(C_BLANK), sourceCoords[i]);
            }
        }
        return builder.toString();
    }

    protected static StringBuilder getCoordinateString(StringBuilder builder, Coordinate coordinate) {
        builder.append(coordinate.x);
        builder.append(C_BLANK);
        builder.append(coordinate.y);
        if (!Double.isNaN(coordinate.z)) {
            builder.append(C_BLANK);
            builder.append(coordinate.z);
        }
        return builder;
    }

    /**
     * Creates a WKT Polygon representation from lower and upper corner values.
     * <p/>
     * @param lowerCorner
     * Lower corner coordinates
     * @param upperCorner
     * Upper corner coordinates
     * <p/>
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

    /**
     * Switches the coordinates of a JTS Geometry.
     * <p/>
     * @param <G> the geometry type
     * @param geometry Geometry to switch coordinates.
     * <p/>
     * @return Geometry with switched coordinates
     * <p/>
     * @throws OwsExceptionReport
     * <p/>
     */
    public static <G extends Geometry> G switchCoordinateAxisOrder(G geometry) throws OwsExceptionReport {
        if (geometry == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        G geom = (G) geometry.clone();
        geom.apply(COORDINATE_SWITCHING_FILTER);
        geom.geometryChanged();
        return geom;
    }

    public static GeometryFactory getGeometryFactory(Geometry geometry) {
        if (geometry.getFactory().getSRID() > 0 || geometry.getSRID() == 0) {
            return geometry.getFactory();
        } else {
            return getGeometryFactoryForSRID(geometry.getSRID());
        }
    }

    public static GeometryFactory getGeometryFactoryForSRID(int srid) {
        return new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
    }

    protected JTSHelper() {
    }
}