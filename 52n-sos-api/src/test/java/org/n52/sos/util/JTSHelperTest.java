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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.n52.sos.util.ReverseOf.reverseOf;

import java.util.Random;

import org.junit.Test;
import org.n52.sos.ogc.ows.OwsExceptionReport;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequenceComparator;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class JTSHelperTest extends JTSHelper {

    private static final Random RANDOM = new Random();

    @Test
    public void factoryFromSridShouldSetSrid() {
        GeometryFactory factory = getGeometryFactoryForSRID(4326);
        assertThat(factory, is(notNullValue()));
        Geometry g = factory.createPoint(new Coordinate(1, 2));
        assertThat(g, is(notNullValue()));
        assertThat(g.getSRID(), is(4326));
    }

    @Test
    public void factoryFromGeometryShouldSetSrid() {
        GeometryFactory factory = getGeometryFactoryForSRID(4326);
        assertThat(factory, is(notNullValue()));
        Geometry g = factory.createPoint(new Coordinate(1, 2));
        factory = getGeometryFactory(g);
        assertThat(factory, is(notNullValue()));
        g = factory.createPoint(new Coordinate(1, 2));
        assertThat(g, is(notNullValue()));
        assertThat(g.getSRID(), is(4326));
    }

    @Test
    public void shouldReverseLinearRing() throws OwsExceptionReport {
        testReverse(getGeometryFactoryForSRID(4326).createLinearRing(randomCoordinateRing(10)));
    }

    @Test
    public void shouldReversePoint() throws OwsExceptionReport {
        testReverse(getGeometryFactoryForSRID(4326).createPoint(randomCoordinate()));
    }

    @Test
    public void shouldReverseLineString() throws OwsExceptionReport {
        testReverse(getGeometryFactoryForSRID(4326).createLineString(randomCoordinates(10)));
    }

    @Test
    public void shouldReversePolygon() throws OwsExceptionReport {
        final GeometryFactory factory = getGeometryFactoryForSRID(4326);
        testReverse(factory.createPolygon(
                factory.createLinearRing(randomCoordinateRing(10)),
                new LinearRing[] {
                    factory.createLinearRing(randomCoordinateRing(10)),
                    factory.createLinearRing(randomCoordinateRing(41)),
                    factory.createLinearRing(randomCoordinateRing(13))
                }));
    }

    @Test
    public void shouldReverseMultiPoint() throws OwsExceptionReport {
        final GeometryFactory factory = getGeometryFactoryForSRID(4326);
        testReverse(factory.createMultiPoint(randomCoordinates(20)));
    }

    @Test
    public void shouldReverseMultiLineString() throws OwsExceptionReport {
        final GeometryFactory factory = getGeometryFactoryForSRID(4326);
        testReverse(factory.createMultiLineString(new LineString[] {
                    factory.createLineString(randomCoordinateRing(21)),
                    factory.createLineString(randomCoordinateRing(21)),
                    factory.createLineString(randomCoordinateRing(15)), }));
    }

    @Test
    public void shouldReverseMultiPolygon() throws OwsExceptionReport {
        final GeometryFactory factory = getGeometryFactoryForSRID(4326);
        testReverse(factory.createMultiPolygon(new Polygon[] {
                    factory.createPolygon(
                    factory.createLinearRing(randomCoordinateRing(13)),
                    new LinearRing[] {
                        factory.createLinearRing(randomCoordinateRing(130)),
                        factory.createLinearRing(randomCoordinateRing(4121)),
                        factory.createLinearRing(randomCoordinateRing(12))
                    }),
                    factory.createPolygon(
                    factory.createLinearRing(randomCoordinateRing(8)),
                    new LinearRing[] {
                        factory.createLinearRing(randomCoordinateRing(1101)),
                        factory.createLinearRing(randomCoordinateRing(413)),
                        factory.createLinearRing(randomCoordinateRing(123))
                    }),
                    factory.createPolygon(
                    factory.createLinearRing(randomCoordinateRing(89)),
                    new LinearRing[] {
                        factory.createLinearRing(randomCoordinateRing(112)),
                        factory.createLinearRing(randomCoordinateRing(4)),
                        factory.createLinearRing(randomCoordinateRing(43))
                    })
                }));
    }

    @Test
    public void shouldReverseGeometryCollection() throws OwsExceptionReport {
        final GeometryFactory factory = getGeometryFactoryForSRID(4326);
        testReverse(factory.createGeometryCollection(new Geometry[] {
                    factory.createMultiPolygon(new Polygon[] {
                        factory.createPolygon(
                        factory.createLinearRing(randomCoordinateRing(13)),
                        new LinearRing[] {
                            factory.createLinearRing(randomCoordinateRing(130)),
                            factory.createLinearRing(randomCoordinateRing(4121)),
                            factory.createLinearRing(randomCoordinateRing(12))
                        }),
                        factory.createPolygon(
                        factory.createLinearRing(randomCoordinateRing(8)),
                        new LinearRing[] {
                            factory.createLinearRing(randomCoordinateRing(1101)),
                            factory.createLinearRing(randomCoordinateRing(413)),
                            factory.createLinearRing(randomCoordinateRing(123))
                        }),
                        factory.createPolygon(
                        factory.createLinearRing(randomCoordinateRing(89)),
                        new LinearRing[] {
                            factory.createLinearRing(randomCoordinateRing(112)),
                            factory.createLinearRing(randomCoordinateRing(4)),
                            factory.createLinearRing(randomCoordinateRing(43))
                        })
                    }),
                    factory.createMultiLineString(new LineString[] {
                        factory.createLineString(randomCoordinateRing(21)),
                        factory.createLineString(randomCoordinateRing(21)),
                        factory.createLineString(randomCoordinateRing(15)), }),
                    factory.createPolygon(
                    factory.createLinearRing(randomCoordinateRing(10)),
                    new LinearRing[] {
                        factory.createLinearRing(randomCoordinateRing(10)),
                        factory.createLinearRing(randomCoordinateRing(41)),
                        factory.createLinearRing(randomCoordinateRing(13))
                    }),
                    getGeometryFactoryForSRID(4326).createLineString(randomCoordinates(10)),
                    getGeometryFactoryForSRID(4326).createLineString(randomCoordinates(10))
                }));
    }

    @Test
    public void shouldReverseUnknownGeometry() throws OwsExceptionReport {
        testReverse(new UnknownGeometry(getGeometryFactoryForSRID(4326).createLineString(randomCoordinates(5))));
    }

    protected void testReverse(Geometry geometry) throws OwsExceptionReport {
        Geometry reversed = switchCoordinateAxisOrder(geometry);
        assertThat(reversed, is(instanceOf(geometry.getClass())));
        assertThat(reversed, is(not(sameInstance(geometry))));
        assertThat(reversed, is(notNullValue()));
        assertThat(reversed, is(reverseOf(geometry)));
    }

    public static Coordinate randomCoordinate() {
        return new Coordinate(RANDOM.nextDouble(),
                              RANDOM.nextDouble(),
                              RANDOM.nextDouble());
    }

    public static Coordinate[] randomCoordinates(int size) {
        Coordinate[] coordinates = new Coordinate[size];
        for (int i = 0; i < size; ++i) {
            coordinates[i] = randomCoordinate();
        }
        return coordinates;
    }

    public static Coordinate[] randomCoordinateRing(int size) {
        Coordinate[] coordinates = randomCoordinates(size);
        if (size > 0) {
            coordinates[size - 1] = coordinates[0];
        }
        return coordinates;
    }

    private class UnknownGeometry extends Geometry {

        private static final long serialVersionUID = 1L;
        private Geometry geom;

        UnknownGeometry(Geometry geom) {
            super(geom.getFactory());
            this.geom = geom;
        }

        @Override
        public String getGeometryType() {
            return geom.getGeometryType();
        }

        @Override
        public int getSRID() {
            return geom.getSRID();
        }

        @Override
        public void setSRID(int SRID) {
            geom.setSRID(SRID);
        }

        @Override
        public GeometryFactory getFactory() {
            return geom.getFactory();
        }

        @Override
        public Object getUserData() {
            return geom.getUserData();
        }

        @Override
        public int getNumGeometries() {
            return geom.getNumGeometries();
        }

        @Override
        public Geometry getGeometryN(int n) {
            return geom.getGeometryN(n);
        }

        @Override
        public void setUserData(Object userData) {
            geom.setUserData(userData);
        }

        @Override
        public PrecisionModel getPrecisionModel() {
            return geom.getPrecisionModel();
        }

        @Override
        public Coordinate getCoordinate() {
            return geom.getCoordinate();
        }

        @Override
        public Coordinate[] getCoordinates() {
            return geom.getCoordinates();
        }

        @Override
        public int getNumPoints() {
            return geom.getNumPoints();
        }

        @Override
        public boolean isSimple() {
            return geom.isSimple();
        }

        @Override
        public boolean isValid() {
            return geom.isValid();
        }

        @Override
        public boolean isEmpty() {
            return geom.isEmpty();
        }

        @Override
        public double distance(Geometry g) {
            return geom.distance(g);
        }

        @Override
        public boolean isWithinDistance(Geometry geom, double distance) {
            return this.geom.isWithinDistance(geom, distance);
        }

        @Override
        public boolean isRectangle() {
            return geom.isRectangle();
        }

        @Override
        public double getArea() {
            return geom.getArea();
        }

        @Override
        public double getLength() {
            return geom.getLength();
        }

        @Override
        public Point getCentroid() {
            return geom.getCentroid();
        }

        @Override
        public Point getInteriorPoint() {
            return geom.getInteriorPoint();
        }

        @Override
        public int getDimension() {
            return geom.getDimension();
        }

        @Override
        public Geometry getBoundary() {
            return geom.getBoundary();
        }

        @Override
        public int getBoundaryDimension() {
            return geom.getBoundaryDimension();
        }

        @Override
        public Geometry getEnvelope() {
            return geom.getEnvelope();
        }

        @Override
        public Envelope getEnvelopeInternal() {
            return geom.getEnvelopeInternal();
        }

        @Override
        public void geometryChanged() {
            geom.geometryChanged();
        }

        @Override
        public boolean disjoint(Geometry g) {
            return geom.disjoint(g);
        }

        @Override
        public boolean touches(Geometry g) {
            return geom.touches(g);
        }

        @Override
        public boolean intersects(Geometry g) {
            return geom.intersects(g);
        }

        @Override
        public boolean crosses(Geometry g) {
            return geom.crosses(g);
        }

        @Override
        public boolean within(Geometry g) {
            return geom.within(g);
        }

        @Override
        public boolean contains(Geometry g) {
            return geom.contains(g);
        }

        @Override
        public boolean overlaps(Geometry g) {
            return geom.overlaps(g);
        }

        @Override
        public boolean covers(Geometry g) {
            return geom.covers(g);
        }

        @Override
        public boolean coveredBy(Geometry g) {
            return geom.coveredBy(g);
        }

        @Override
        public boolean relate(Geometry g, String intersectionPattern) {
            return geom.relate(g, intersectionPattern);
        }

        @Override
        public IntersectionMatrix relate(Geometry g) {
            return geom.relate(g);
        }

        @Override
        public boolean equals(Geometry g) {
            return geom.equals(g);
        }

        @Override
        public boolean equalsTopo(Geometry g) {
            return geom.equalsTopo(g);
        }

        @Override
        public boolean equals(Object o) {
            return geom.equals(o);
        }

        @Override
        public int hashCode() {
            return geom.hashCode();
        }

        @Override
        public String toString() {
            return geom.toString();
        }

        @Override
        public String toText() {
            return geom.toText();
        }

        @Override
        public Geometry buffer(double distance) {
            return geom.buffer(distance);
        }

        @Override
        public Geometry buffer(double distance, int quadrantSegments) {
            return geom.buffer(distance, quadrantSegments);
        }

        @Override
        public Geometry buffer(double distance, int quadrantSegments, int endCapStyle) {
            return geom.buffer(distance, quadrantSegments, endCapStyle);
        }

        @Override
        public Geometry convexHull() {
            return geom.convexHull();
        }

        @Override
        public Geometry reverse() {
            return geom.reverse();
        }

        @Override
        public Geometry intersection(Geometry other) {
            return geom.intersection(other);
        }

        @Override
        public Geometry union(Geometry other) {
            return geom.union(other);
        }

        @Override
        public Geometry difference(Geometry other) {
            return geom.difference(other);
        }

        @Override
        public Geometry symDifference(Geometry other) {
            return geom.symDifference(other);
        }

        @Override
        public Geometry union() {
            return geom.union();
        }

        @Override
        public boolean equalsExact(Geometry other, double tolerance) {
            return geom.equalsExact(other, tolerance);
        }

        @Override
        public boolean equalsExact(Geometry other) {
            return geom.equalsExact(other);
        }

        @Override
        public boolean equalsNorm(Geometry g) {
            return geom.equalsNorm(g);
        }

        @Override
        public void apply(CoordinateFilter filter) {
            geom.apply(filter);
        }

        @Override
        public void apply(CoordinateSequenceFilter filter) {
            geom.apply(filter);
        }

        @Override
        public void apply(GeometryFilter filter) {
            geom.apply(filter);
        }

        @Override
        public void apply(GeometryComponentFilter filter) {
            geom.apply(filter);
        }

        @Override
        public Object clone() {
            return new UnknownGeometry((Geometry) geom.clone());
        }

        @Override
        public void normalize() {
            geom.normalize();
        }

        @Override
        public Geometry norm() {
            return new UnknownGeometry(geom.norm());
        }

        @Override
        public int compareTo(Object o) {
            return geom.compareTo(o);
        }

        @Override
        public int compareTo(Object o, CoordinateSequenceComparator comp) {
            return geom.compareTo(o, comp);
        }

        @Override
        protected Envelope computeEnvelopeInternal() {
            return geom.getEnvelopeInternal();
        }

        @Override
        protected int compareToSameClass(Object o) {
            return geom.compareTo(o);
        }

        @Override
        protected int compareToSameClass(Object o, CoordinateSequenceComparator comp) {
            return geom.compareTo(o, comp);
        }
    }
}
