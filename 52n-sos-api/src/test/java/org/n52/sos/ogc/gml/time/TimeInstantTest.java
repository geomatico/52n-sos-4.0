package org.n52.sos.ogc.gml.time;

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.junit.Test;

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
    public void shoulEqual() {
        DateTime dateTime = new DateTime();
        TimeInstant timeInstant = new TimeInstant(dateTime);
        TimeInstant equalTimeInstant = new TimeInstant(dateTime, "latest");
        assertTrue("TimeInstants are NOT equal",timeInstant.equals(equalTimeInstant));
    }
}
