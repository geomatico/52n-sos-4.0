package org.n52.sos.encode;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;

import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;

public class GmlEncoderTest {
    
    GmlEncoderv321 encoder = new GmlEncoderv321();
    
    @Test (expected=IllegalArgumentException.class)
    public void throwIAEForEncodeNullTest() throws OwsExceptionReport {
        encoder.encode(null);
    }
    
    @Test
    public void isNullForNotSupportedObjectTest() throws OwsExceptionReport {
        assertNull("Encoded object is NOT null", encoder.encode(5));
    }
    
    @Test (expected=IllegalArgumentException.class)
    public void throwsIllegalArgumentExceptionWhenConstructorValueNullTest() throws OwsExceptionReport {
        QuantityValue quantity = new QuantityValue(null);
        encoder.encode(quantity);
    }
    
    @Test
    public void isMeasureTypeValidWithoutUnitTest() throws OwsExceptionReport {
        QuantityValue quantity = new QuantityValue(new BigDecimal(2.2));
        XmlObject encode = encoder.encode(quantity);
        assertTrue("Encoded Object is NOT valid", encode.validate());
    }
    
    @Test
    public void isMeasureTypeValidAllSetTest() throws OwsExceptionReport {
        QuantityValue quantity = new QuantityValue(new BigDecimal(2.2));
        quantity.setUnit("cm");
        XmlObject encode = encoder.encode(quantity);
        assertTrue("Encoded Object is NOT valid", encode.validate());
    }

}
