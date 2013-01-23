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

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class EncoderKey {
    
    protected static boolean eq(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
    
    protected static int hash(int initial, int increment, Object... a) {
        int hashCode = initial;
        for (Object o : a) {
            hashCode = increment * hashCode + (o == null ? 0 : o.hashCode());
        }
        return hashCode;
    }
    
    @Override
    public abstract boolean equals(Object obj);
    
    @Override
    public abstract String toString();
    
    @Override
    public abstract int hashCode();
    
    public abstract int getSimilarity(EncoderKey key);
}
