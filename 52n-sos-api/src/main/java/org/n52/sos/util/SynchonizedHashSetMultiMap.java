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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Implementation based on synchronized {@link HashSet}s and a synchronized {@link HashMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SynchonizedHashSetMultiMap<K, V> extends AbstractSynchronizedMultiHashMap<K, V, Set<V>>
        implements SetMultiMap<K, V> {
    private static final long serialVersionUID = 741828638081663856L;

    public SynchonizedHashSetMultiMap(Map<? extends K, ? extends Set<V>> m) {
        super(m);
    }

    public SynchonizedHashSetMultiMap(int initialCapacity) {
        super(initialCapacity);
    }

    public SynchonizedHashSetMultiMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public SynchonizedHashSetMultiMap() {
        super();
    }

    @Override
    protected Set<V> newCollection() {
        return Collections.synchronizedSet(new HashSet<V>());
    }
}
