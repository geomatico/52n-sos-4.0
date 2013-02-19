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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public class SosHelperTest extends SosHelper {
    public static final int EPSG4326 = 4326;
    public static final int EPSG31466 = 31466;
    public static final int DEFAULT_EPSG = EPSG4326;
    
    @BeforeClass
    public static void setUp() {
        setConfig(new TestableConfiguration());
    }
    
    @Test
    public void envelopeForEpsg4326() {
        double maxY = 52.15034, maxX = 8.05847;
        double minY = 51.95104, minX = 7.61353;
        Envelope e = new Envelope(new Coordinate(minX, minY), 
                                  new Coordinate(maxX, maxY));
        checkMinMax(getMinMaxFromEnvelope(e, EPSG4326), minY, minX, maxY, maxX);
        checkMinMax(getMinMaxFromEnvelope(e)          , minY, minX, maxY, maxX);
    }

    @Test
    public void envelopeForEpsg31466() {
        double maxX = 3435628, maxY = 5780049;
        double minX = 3404751, minY = 5758364;
        Envelope e = new Envelope(new Coordinate(minX, minY), 
                                  new Coordinate(maxX, maxY));
        checkMinMax(getMinMaxFromEnvelope(e, EPSG31466), minX, minY, maxX, maxY);        
    }

    protected void checkMinMax(MinMax<String> minmax, double minY, double minX, double maxY, double maxX) {
        assertThat(minmax, is(notNullValue()));
        assertThat(minmax.getMinimum(), is(minY + " " + minX));
        assertThat(minmax.getMaximum(), is(maxY + " " + maxX));
    }

    private static class TestableConfiguration extends Configuration {

        @Override
        protected boolean reversedAxisOrderRequired(int srid) {
            if (srid == EPSG4326) {
                return true;
            }
            if (srid == EPSG31466) {
                return false;
            }
            return false;
        }

        @Override
        protected int getDefaultEPSG() {
            return DEFAULT_EPSG;
        }
    }
    
}
