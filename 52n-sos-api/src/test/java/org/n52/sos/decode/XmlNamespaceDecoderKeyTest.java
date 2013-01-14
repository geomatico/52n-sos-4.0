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

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class XmlNamespaceDecoderKeyTest {
    private class C1 {}
    private class C2 extends C1 implements I3 {}
    private class C3 extends C2 {}
    private class C4 extends C3 {}
    private class C5 {}
    private interface I1 {}
    private interface I2 extends I1 {}
    private interface I3 extends I2 {}
    private interface I4 {}
    
    @Test
    public void testHashCode() {
        assertEquals(
            new XmlNamespaceDecoderKey("test", C1.class).hashCode(),
            new XmlNamespaceDecoderKey("test", C1.class).hashCode());
        assertEquals(
            new XmlNamespaceDecoderKey(null, C1.class).hashCode(),
            new XmlNamespaceDecoderKey(null, C1.class).hashCode());
        assertEquals(
            new XmlNamespaceDecoderKey("test", null).hashCode(),
            new XmlNamespaceDecoderKey("test", null).hashCode());
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class).hashCode(),
            new XmlNamespaceDecoderKey(null, C1.class).hashCode());
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", null).hashCode(),
            new XmlNamespaceDecoderKey("test", C1.class).hashCode());
        assertNotEquals(
            new XmlNamespaceDecoderKey("test1", C1.class).hashCode(),
            new XmlNamespaceDecoderKey("test", C1.class).hashCode());
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class).hashCode(),
            new XmlNamespaceDecoderKey("test1", C1.class).hashCode());
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class).hashCode(),
            new XmlNamespaceDecoderKey("test", C2.class).hashCode());
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class).hashCode(),
            new XmlNamespaceDecoderKey("test", C2.class).hashCode());
    }
    
    @Test
    public void testEquals() {
        assertEquals(
            new XmlNamespaceDecoderKey("test", C1.class),
            new XmlNamespaceDecoderKey("test", C1.class));
        assertEquals(
            new XmlNamespaceDecoderKey(null, C1.class),
            new XmlNamespaceDecoderKey(null, C1.class));
        assertEquals(
            new XmlNamespaceDecoderKey("test", null),
            new XmlNamespaceDecoderKey("test", null));
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class),
            new XmlNamespaceDecoderKey(null, C1.class));
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", null),
            new XmlNamespaceDecoderKey("test", C1.class));
        assertNotEquals(
            new XmlNamespaceDecoderKey("test1", C1.class),
            new XmlNamespaceDecoderKey("test", C1.class));
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class),
            new XmlNamespaceDecoderKey("test1", C1.class));
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class),
            new XmlNamespaceDecoderKey("test", C2.class));
        assertNotEquals(
            new XmlNamespaceDecoderKey("test", C1.class),
            new XmlNamespaceDecoderKey("test", C2.class));
    }
    

    
    private void test(Class<?> a, Class<?> b, int expected) {
        assertEquals(expected, new XmlNamespaceDecoderKey("test", a)
                .getSimilarity(new XmlNamespaceDecoderKey("test", b)));
    }

    @Test
    public void testSimilartiy() {
        assertEquals(-1, new XmlNamespaceDecoderKey("test", C1.class)
                .getSimilarity(new XmlNamespaceDecoderKey("test1", C1.class)));
        test(C1.class, C2.class, 1);
        test(C1.class, C3.class, 2);
        test(C1.class, C4.class, 3);
        test(C3.class, C4.class, 1);
        test(I1.class, C4.class, 5);
        test(I1.class, I4.class, -1);
        test(C1.class, C5.class, -1);
        test(C1.class, I1.class, -1);
        
        test(C1[].class, C2[].class, 1);
        test(C1[].class, C3[].class, 2);
        test(C1[].class, C4[].class, 3);
        test(C3[].class, C4[].class, 1);
        test(I1[].class, C4[].class, 5);
        test(I1[].class, I4[].class, -1);
        test(C1[].class, C5[].class, -1);
        test(C1[].class, I1[].class, -1);
        
        test(C1[].class, C1.class, -1);
    }
}
