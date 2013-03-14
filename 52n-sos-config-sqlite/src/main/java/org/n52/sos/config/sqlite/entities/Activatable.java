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
package org.n52.sos.config.sqlite.entities;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.MappedSuperclass;

/**
 * @param <K> the key type
 * @param <T> the type of the extending class
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
@MappedSuperclass
public class Activatable<K extends Serializable, T extends Activatable<K, T>> implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private K key;
    private boolean active;

    public Activatable(K key) {
        this.key = key;
    }

    public boolean isActive() {
        return active;
    }

    @SuppressWarnings("unchecked")
    public T setActive(boolean active) {
        this.active = active;
        return (T) this;
    }

    public K getKey() {
        return key;
    }

    public T setEncodingKey(K encodingKey) {
        this.key = encodingKey;
        return (T) this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (getKey() != null ? getKey().hashCode() : 0);
        hash = 97 * hash + (isActive() ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResponseFormat) {
            ResponseFormat o = (ResponseFormat) obj;
            return (getKey() == null ? o.getKey() == null : getKey().equals(o.getKey())) && isActive() == o.isActive();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[key=%s, active=%b]", getClass().getSimpleName(), getKey(), isActive());
    }
}
