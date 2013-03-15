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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class Activatable<T> {
    public static <K, V> Map<K, V> filter(Map<K, Activatable<V>> map) {
        Map<K, V> filtered = new HashMap<K, V>(map.size());
        for (K k : map.keySet()) {
            if (map.get(k) != null && map.get(k).get() != null) {
                filtered.put(k, map.get(k).get());
            }
        }
        return filtered;
    }

    public static <E> Set<E> filter(Set<Activatable<E>> set) {
        Set<E> filtered = new HashSet<E>(set.size());
        for (Activatable<E> a : set) {
            if (a.isActive()) {
                filtered.add(a.get());
            }
        }
        return filtered;
    }
    private T object;
    private boolean active;

    public Activatable(T object) {
        this(object, true);
    }

    public Activatable(T object, boolean active) {
        this.object = object;
        this.active = active;
    }

    /**
     * @return isActive() ? object : null
     */
    public T get() {
        return isActive() ? object : null;
    }

    public boolean isActive() {
        return active;
    }

    public Activatable<T> setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.object != null ? this.object.hashCode() : 0);
        hash = 37 * hash + (this.active ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Activatable<?> other = (Activatable<?>) obj;
        if (this.object != other.object && (this.object == null || !this.object.equals(other.object))) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s[object=%s, active=%s]", getClass().getSimpleName(), this.object, this.active);
    }
}
