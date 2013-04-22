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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Abstract implementation that delegates to a existing map implementation.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @param <C> the collection type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractDelegatingMultiMap<K, V, C extends Collection<V>> implements MultiMap<K, V, C> {
    @Override
    public int size() {
        return getDelegate().size();
    }

    @Override
    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getDelegate().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getDelegate().containsValue(value);
    }

    @Override
    public C get(Object key) {
        return getDelegate().get(key);
    }

    @Override
    public C put(K key, C value) {
        return getDelegate().put(key, value);
    }

    @Override
    public C add(K key, V value) {
        C c = get(key);
        if (c == null) {
            put(key, c = newCollection());
        }
        c.add(value);
        return c;
    }

    @Override
    public C remove(Object key) {
        return getDelegate().remove(key);
    }

    @Override
    public boolean remove(K k, V v) {
        C c = get(k);
        if (c != null) {
            return c.remove(v);
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends C> m) {
        getDelegate().putAll(m);
    }

    @Override
    public void clear() {
        getDelegate().clear();
    }

    @Override
    public Set<K> keySet() {
        return getDelegate().keySet();
    }

    @Override
    public Collection<C> values() {
        return getDelegate().values();
    }

    @Override
    public C allValues() {
        C c = newCollection();
        for (C v : values()) {
            c.addAll(v);
        }
        return c;
    }

    @Override
    public Set<Entry<K, C>> entrySet() {
        return getDelegate().entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return getDelegate().equals(o);
    }

    @Override
    public int hashCode() {
        return getDelegate().hashCode();
    }

    @Override
    public boolean containsCollectionValue(V v) {
        for (C c : values()) {
            if (c.contains(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(K key, Iterable<V> value) {
        boolean altered = false;
        C c = get(key);
        if (c != null) {
            for (V v : value) {
                if (c.remove(v)) {
                    altered = true;
                }
            }
        }
        return altered;
    }

    @Override
    public boolean hasValues(K key) {
        return containsKey(key) && !get(key).isEmpty();
    }

    @Override
    public C addAll(K key, Collection<? extends V> values) {
        C c = get(key);
        if (c == null) {
            put(key, c = newCollection());
        }
        c.addAll(values);
        return c;
    }

    @Override
    public boolean removeWithKey(K key, V value) {
        boolean altered = remove(key, value);
        if (!hasValues(key)) {
            remove(key);
        }
        return altered;
    }

    @Override
    public boolean removeWithKey(K key, Iterable<V> value) {
        boolean altered = remove(key, value);
        if (!hasValues(key)) {
            remove(key);
        }
        return altered;
    }

    /**
     * @return the delegate to operate on
     */
    protected abstract Map<K, C> getDelegate();

    /**
     * @return a new collection to save values
     */
    protected abstract C newCollection();
}
