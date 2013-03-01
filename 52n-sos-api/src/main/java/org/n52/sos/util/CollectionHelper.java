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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CollectionHelper {
    public static <T> Set<T> set(final T... elements) {
        HashSet<T> set = new HashSet<T>(elements.length);
        Collections.addAll(set, elements);
        return Collections.unmodifiableSet(set);
    }

    public static <T> Set<T> set() {
        return new HashSet<T>();
    }

    public static <K, V> Map<K, V> map() {
        return new HashMap<K, V>();
    }

    public static <T> List<T> list() {
        return new LinkedList<T>();
    }

    public static <T> Collection<T> collection() {
        return list();
    }

    public static <T> Collection<T> collection(T... elements) {
        return list(elements);
    }

    public static <T> List<T> list(T... elements) {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public static <T> Set<T> union(final Set<T>... elements) {
        return ((elements.length == 0) ? Collections.<T>emptySet()
                : new HashSet<T>(elements.length * elements[0].size()) {
            private static final long serialVersionUID = 1L;
            {
                for (Set<T> s : elements) {
                    addAll(s);
                }
            }
        });
    }

    public static <T> Set<T> asSet(final Iterable<? extends T> iterable) {
        return (iterable instanceof Collection)
               ? new HashSet<T>((Collection<? extends T>) iterable)
               : new HashSet<T>() {
            private static final long serialVersionUID = 1L;
            {
                for (T t : iterable) {
                    add(t);
                }
            }
        };
    }

    public static <T> Set<T> unmodifiableSet(Set<? extends T> s) {
        return (s == null) ? Collections.<T>emptySet() : Collections.unmodifiableSet(s);
    }

    public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> m) {
        return (m == null) ? Collections.<K, V>emptyMap() : Collections.unmodifiableMap(m);
    }

    public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> c) {
        return (c == null) ? Collections.<T>emptyList() : Collections.unmodifiableCollection(c);
    }

    public static <T> List<T> unmodifiableList(List<? extends T> l) {
        return (l == null) ? Collections.<T>emptyList() : Collections.unmodifiableList(l);
    }

    public static <T> List<T> asList(final T t, final T... ts) {
        ArrayList<T> list = new ArrayList<T>(ts.length + 1);
        list.add(t);
        Collections.addAll(list, ts);
        return list;
    }
    
    public static <T> Set<T> asSet(final T t, final T... ts) {
        Set<T> set = new HashSet<T>(ts.length + 1);
        set.add(t);
        Collections.addAll(set, ts);
        return set;
    }

    public static <T> List<T> conjunctCollections(Collection<T> list1, Collection<T> list2) {
        HashSet<T> s1 = new HashSet<T>(list1);
        s1.retainAll(list2);
        return new ArrayList<T>(s1);
    }

    public static <K, V> Map<K, V> synchronizedInitialSizeMapWithLoadFactor1(final int capacity) {
        return CollectionHelper.synchronizedMap(capacity, 1.0F);
    }

    public static <K, V> Map<K, V> synchronizedMap(int initialCapacity) {
        return Collections.synchronizedMap(new HashMap<K, V>(initialCapacity));
    }

    protected static <K, V> Map<K, V> synchronizedMap(int initialCapacity, float loadFactor) {
        return Collections.synchronizedMap(new HashMap<K, V>(initialCapacity, loadFactor));
    }

    /**
     * Constructs a new synchronized {@code Set} based on a {@link HashSet}.
     *
     * @return a synchronized Set
     */
    public static <T> Set<T> synchronizedSet() {
        return Collections.synchronizedSet(new HashSet<T>());
    }

    /**
     * Constructs a new synchronized {@code Set} based on a {@link HashSet} with the specified {@code initialCapacity}.
     *
     * @param initialCapacity the initial capacity of the set
     *
     * @return a synchronized Set
     */
    public static <T> Set<T> synchronizedSet(int initialCapacity) {
        return Collections.synchronizedSet(new HashSet<T>(initialCapacity));
    }

    /**
     * Constructs a new synchronized {@code List} based on a {@link LinkedList}.
     *
     * @return a synchronized List
     */
    public static <E> List<E> synchronizedList() {
        return Collections.synchronizedList(new LinkedList<E>());
    }

    /**
     * Constructs a new synchronized {@code List} based on a {@link ArrayList} with the specified
     * {@code initialCapacity}.
     *
     * @param initialCapacity the initial capacity of the array list
     *
     * @return a synchronized List
     */
    public static <E> List<E> synchronizedList(int initialCapacity) {
        return Collections.synchronizedList(new ArrayList<E>(initialCapacity));
    }

    private CollectionHelper() {
    }

	/**
	 * @param collectionOfCollection a Collection&lt;Collection&lt;String>>
	 * @return a Collection&lt;String> containing all values of all Collections&lt;String> without any duplicates
	 */
	public static Collection<String> unionOfListOfLists(final Collection<Collection<String>> collectionOfCollection)
	{
		if (collectionOfCollection == null || collectionOfCollection.isEmpty())
		{
			return new ArrayList<String>(0);
		}
		Collection<String> union = new ArrayList<String>();
		for (Collection<String> col : collectionOfCollection)
		{
			if (col != null && !col.isEmpty())
			{
				for (String string : col)
				{
					if (string != null && !string.isEmpty() && !union.contains(string))
					{
						union.add(string);
					}
				}
			}
		}
		return union;
	}
}
