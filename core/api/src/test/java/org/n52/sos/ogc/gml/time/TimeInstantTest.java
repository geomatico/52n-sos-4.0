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
package org.n52.sos.ogc.gml.time;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Test;
import org.n52.sos.ogc.sos.SosConstants.IndeterminateTime;

public class TimeInstantTest {

    @Test
    public void isEmptyForDefaultConstructorTest() {
        assertTrue("new TimeInstant is NOT empty", new TimeInstant().isEmpty());
    }

    @Test
    public void isEmptyForConstructorWithNullTimeTest() {
        assertTrue("new TimeInstant(null) is NOT empty", new TimeInstant(null).isEmpty());
    }

    @Test
    public void isNotEmptyForConstructorWithTimeAndNullIndeterminateValueTest() {
        assertFalse("new TimeInstant(new DateTime(), null) is empty",
                new TimeInstant(new DateTime(), null).isEmpty());
    }

    @Test
    public void isNotEmptyForConstructorWithNullTimeAndIndeterminateValueTest() {
        assertFalse("new TimeInstant(null, \"latest\") is empty", new TimeInstant(null, "latest").isEmpty());
    }
    
    @Test
    public void shouldEqual() {
        DateTime dateTime = new DateTime();
        TimeInstant timeInstant = new TimeInstant(dateTime);
        TimeInstant equalTimeInstant = new TimeInstant(dateTime, "latest");
        assertTrue("TimeInstants are NOT equal",timeInstant.equals(equalTimeInstant));
    }
    
    @Test
    public void testIndeterminateNow() {
        TimeInstant timeInstant = new TimeInstant(null, IndeterminateTime.now.name());
        DateTime beforeAccess = new DateTime();
        DateTime nowValue = timeInstant.resolveValue();
        assertNotNull("TimeInstant now value is null", nowValue);
        assertTrue("TimeInstant now value is too early", nowValue.isAfter(beforeAccess) || nowValue.isEqual(beforeAccess));
        assertTrue("TimeInstant now value is too late", nowValue.isBeforeNow() || nowValue.isEqualNow());
    }
}
