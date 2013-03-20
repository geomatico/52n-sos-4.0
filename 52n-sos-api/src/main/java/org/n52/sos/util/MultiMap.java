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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 *
 * Map that encapsulates access to multiple values per key.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @param <C> the collection type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface MultiMap<K, V, C extends Collection<V>> extends Map<K, C>, Serializable {
    /**
     * Checks if any collection of any key contains {@code v}.
     *
     * @param v the element to check
     *
     * @return if it is contained
     */
    boolean containsCollectionValue(V v);

    /**
     * Adds the specified value to the key. If the collection for the key was {@code null} it will be created.
     *
     * @param key   the key
     * @param value the value
     *
     * @return the collection the value was added to
     */
    C add(K key, V value);

    /**
     * Adds the specified values to the key. If the collection for the key was {@code null} it will be created.
     *
     * @param key    the key
     * @param values the values
     *
     * @return the collection the values were added to
     */
    C addAll(K key, Collection<? extends V> values);

    /**
     * Removes the value of the collection for the specified key (if it exists).
     *
     * @param key   the key
     * @param value the value to remove
     *
     * @return if the map was altered
     */
    boolean remove(K key, V value);

    /**
     * Removes the specified value of the collection for the specified key (if it exists). If the collection for the key
     * is empty after the removal the key is removed from the map.
     *
     * @param key   the key
     * @param value the value
     *
     * @return if the map was altered
     */
    boolean removeWithKey(K key, V value);

    /**
     * Removes the values of the collection for the specified key (if it exists).
     *
     * @param key   the key
     * @param value the values to remove
     *
     * @return if the map was altered
     */
    boolean remove(K key, Iterable<V> value);

    /**
     * Removes the specified values of the collection for the specified key (if it exists). If the collection for the
     * key is empty after the removal the key is removed from the map.
     *
     * @param key   the key
     * @param value the value
     *
     * @return if the map was altered
     */
    boolean removeWithKey(K key, Iterable<V> value);

    /**
     * Checks if the specfied key is contained in this map and if the associated collection is not empty.
     *
     * @param key the key
     *
     * @return if the key has at least one value
     */
    boolean hasValues(K key);

    /**
     * @return the values of all keys
     */
    C allValues();
}
