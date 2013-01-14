/**
 * Copyright (C) 2012
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
package org.n52.sos.decode;

import static org.junit.Assert.*;
import org.junit.Test;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.Sos2Constants;


/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class XmlOperationDecoderKeyTest {
    
    
    @Test
    public void testHashCode() {
        assertEquals(
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()).hashCode(),
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()).hashCode());
        assertEquals(
            new XmlOperationDecoderKey(null, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()).hashCode(),
            new XmlOperationDecoderKey(null, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()).hashCode());
        assertEquals(
            new XmlOperationDecoderKey(SosConstants.SOS, null, SosConstants.Operations.GetCapabilities.name()).hashCode(),
            new XmlOperationDecoderKey(SosConstants.SOS, null, SosConstants.Operations.GetCapabilities.name()).hashCode());
        assertEquals(
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, null).hashCode(),
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, null).hashCode());
    }
    
     @Test
     public void testEquals() {
         assertEquals(
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()),
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()));
        assertEquals(
            new XmlOperationDecoderKey(null, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()),
            new XmlOperationDecoderKey(null, Sos2Constants.SERVICEVERSION, SosConstants.Operations.GetCapabilities.name()));
        assertEquals(
            new XmlOperationDecoderKey(SosConstants.SOS, null, SosConstants.Operations.GetCapabilities.name()),
            new XmlOperationDecoderKey(SosConstants.SOS, null, SosConstants.Operations.GetCapabilities.name()));
        assertEquals(
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, null),
            new XmlOperationDecoderKey(SosConstants.SOS, Sos2Constants.SERVICEVERSION, null));
     }
}
