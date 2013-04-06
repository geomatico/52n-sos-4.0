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

import org.n52.sos.util.ClassHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class JsonEncoderKey extends EncoderKey {
    private final Class<?> type;

    public JsonEncoderKey(Class<?> type) {
        this.type = type;
    }
        public Class<?> getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            final JsonEncoderKey o = (JsonEncoderKey) obj;
            return eq(getType(), o.getType());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[type=%s]", getClass().getSimpleName(), getType());
    }


    @Override
    public int getSimilarity(EncoderKey key) {
        if (key instanceof JsonEncoderKey) {
            JsonEncoderKey jsonKey = (JsonEncoderKey) key;
                return ClassHelper.getSimiliarity(getType() != null ?        getType() : Object.class, 
                                           jsonKey.getType() != null ? jsonKey.getType() : Object.class);
        } else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        return hash(5, 37, getType());
    }
}
