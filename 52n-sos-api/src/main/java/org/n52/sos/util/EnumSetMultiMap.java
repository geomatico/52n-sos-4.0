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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@linkplain SetMultiMap} implementation backed with a {@link EnumMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class EnumSetMultiMap<K extends Enum<K>, V> extends AbstractDelegatingMultiMap<K, V, Set<V>> implements
        SetMultiMap<K, V> {
    private static final long serialVersionUID = 1343214593123842785L;
    private final Map<K, Set<V>> delegate;

    public EnumSetMultiMap(Class<K> keyType) {
        this.delegate = new EnumMap<K, Set<V>>(keyType);
    }

    public EnumSetMultiMap(EnumMap<K, ? extends Set<V>> m) {
        this.delegate = new EnumMap<K, Set<V>>(m);
    }

    public EnumSetMultiMap(Map<K, ? extends Set<V>> m) {
        this.delegate = new EnumMap<K, Set<V>>(m);
    }

    @Override
    protected Map<K, Set<V>> getDelegate() {
        return this.delegate;
    }

    @Override
    protected Set<V> newCollection() {
        return new HashSet<V>();
    }
}
