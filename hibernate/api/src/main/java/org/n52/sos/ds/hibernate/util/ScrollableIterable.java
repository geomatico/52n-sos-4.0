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

package org.n52.sos.ds.hibernate.util;

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.hibernate.Criteria;
import org.hibernate.ScrollableResults;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ScrollableIterable<T> implements Iterable<T>, Closeable {
    public static <T> ScrollableIterable<T> fromResults(ScrollableResults sr) {
        return new ScrollableIterable<T>(sr);
    }

    public static <T> ScrollableIterable<T> fromCriteria(Criteria c) {
        return new ScrollableIterable<T>(c.scroll());
    }
    private final ScrollableResults results;
    private Iterator<T> iterator;

    public ScrollableIterable(ScrollableResults results) {
        this.results = results;
    }

    @Override
    public Iterator<T> iterator() {
        if (iterator != null) {
            throw new IllegalStateException("this is a one time iterable");
        }
        return iterator = new ScrollableIterator();
    }

    @Override
    public void close() {
        results.close();
    }

    private class ScrollableIterator implements Iterator<T> {
        private Boolean hasNext;

        @Override
        public boolean hasNext() {
            if (hasNext == null) {
                // only proceed once
                hasNext = results.next();
            }
            return hasNext;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (hasNext) {
                hasNext = null;
                return (T) results.get(0);
            } else {
                throw new NoSuchElementException();
            }
            
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
