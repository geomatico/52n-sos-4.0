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

/**
 * @param <T> the type
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class MinMax<T> {

    private T minimum;
    private T maximum;

    public MinMax(T minimum, T maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public MinMax() {
    }

    /**
     * Get the value of minimum
     *
     * @return the value of minimum
     */
    public T getMinimum() {
        return minimum;
    }

    /**
     * Set the value of minimum
     *
     * @param minimum new value of minimum
     * <p/>
     * @return this
     */
    public MinMax<T> setMinimum(T minimum) {
        this.minimum = minimum;
        return this;
    }

    /**
     * Get the value of maximum
     *
     * @return the value of maximum
     */
    public T getMaximum() {
        return maximum;
    }

    /**
     * Set the value of maximum
     *
     * @param maximum new value of maximum
     * <p/>
     * @return this
     */
    public MinMax<T> setMaximum(T maximum) {
        this.maximum = maximum;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == getClass()) {
            final MinMax<T> other = (MinMax<T>) obj;
            return (getMinimum() == null ? other.getMinimum() == null : other.getMinimum().equals(getMinimum()))
                   && (getMaximum() == null ? other.getMaximum() == null : other.getMaximum().equals(getMaximum()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 97;
        int hash = 7;
        hash = prime * hash + (getMinimum() != null ? getMinimum().hashCode() : 0);
        hash = prime * hash + (getMaximum() != null ? getMaximum().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("MinMax[minimum=%s, maximum=%s]", getMinimum(), getMaximum());
    }
    
    
}
