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
package org.n52.sos.encode;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.apache.xmlbeans.XmlObject;
import org.junit.Test;
import org.n52.sos.exception.ows.concrete.UnsupportedEncoderInputException;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;

public class GmlEncoderTest {
    
    private GmlEncoderv321 encoder = new GmlEncoderv321();
    
    @Test(expected = OwsExceptionReport.class)
    public void throwIAEForEncodeNullTest() throws OwsExceptionReport {
        encoder.encode(null);
    }
    
    @Test(expected = UnsupportedEncoderInputException.class)
    public void isNullForNotSupportedObjectTest() throws OwsExceptionReport {
        encoder.encode(5);
    }
    
    @Test(expected = OwsExceptionReport.class)
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
