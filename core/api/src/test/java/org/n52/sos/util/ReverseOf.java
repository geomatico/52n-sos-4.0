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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ReverseOf extends BaseMatcher<Geometry> {

    private Geometry original;

    public ReverseOf(Geometry original) {
        this.original = original;
    }

    @Override
    public boolean matches(Object item) {
        if (item == null || item.getClass() != original.getClass()) {
            return false;
        }
        Geometry geom = (Geometry) item;
        Coordinate[] orig = original.getCoordinates();
        Coordinate[] switched = geom.getCoordinates();
        if (orig.length != switched.length) {
            return false;
        }
        for (int i = 0; i < orig.length; ++i) {
            if (!isSwitched(orig[i], switched[i])) {
                return false;
            }
        }
        return true;
    }

    protected boolean equal(double a, double b) {
        return Double.isNaN(a) ? Double.isNaN(b) : Double.compare(a, b) == 0;
    }

    protected boolean isSwitched(Coordinate a, Coordinate b) {
        return equal(a.x, b.y) && equal(a.y, b.x) && equal(a.z, b.z);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("reverse of ").appendValue(original);
    }

    @Factory
    public static Matcher<Geometry> reverseOf(Geometry geom) {
        return new ReverseOf(geom);
    }
}
