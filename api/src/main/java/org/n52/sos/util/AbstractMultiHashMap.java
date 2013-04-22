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
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation that delegates to a {@link HashMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @param <C> the collection type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractMultiHashMap<K, V, C extends Collection<V>> extends AbstractDelegatingMultiMap<K, V, C>
        implements MultiMap<K, V, C>, Map<K, C>, Serializable {
    private static final long serialVersionUID = 5980618435134246476L;
    private final Map<K, C> delegate;

    public AbstractMultiHashMap(Map<? extends K, ? extends C> m) {
        delegate = new HashMap<K, C>(m);
    }

    public AbstractMultiHashMap(int initialCapacity) {
        delegate = new HashMap<K, C>(initialCapacity);
    }

    public AbstractMultiHashMap(int initialCapacity, float loadFactor) {
        delegate = new HashMap<K, C>(initialCapacity, loadFactor);
    }

    public AbstractMultiHashMap() {
        this.delegate = new HashMap<K, C>();
    }

    @Override
    protected Map<K, C> getDelegate() {
        return delegate;
    }
}
