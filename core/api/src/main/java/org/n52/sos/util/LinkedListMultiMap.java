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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation based on {@link LinkedList}s and a {@link HashMap}.
 *
 * @param <K> the key type
 * @param <V> the value type
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class LinkedListMultiMap<K, V> extends AbstractMultiHashMap<K, V, List<V>> implements ListMultiMap<K, V>, Serializable {
    private static final long serialVersionUID = 5709659568351160402L;

    public LinkedListMultiMap(Map<? extends K, ? extends List<V>> m) {
        super(m);
    }

    public LinkedListMultiMap(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedListMultiMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public LinkedListMultiMap() {
        super();
    }

    @Override
    protected List<V> newCollection() {
        return new LinkedList<V>();
    }
}
