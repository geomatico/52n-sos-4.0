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

/**
 * Class that describes a time primitive of an entity. Instants are represented by one field and periods by two.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class TimePrimitiveFieldDescriptor {
    private final String begin;
    private final String end;

    /**
     * Creates a new descriptor for a period.
     *
     * @param begin the begin field
     * @param end   the end field
     */
    public TimePrimitiveFieldDescriptor(String begin, String end) {
        if (begin == null) {
            throw new NullPointerException("start may not be null");
        }
        this.begin = begin;
        this.end = end;
    }

    /**
     * Creates a new descriptor for a time instant.
     *
     * @param position the field name
     */
    public TimePrimitiveFieldDescriptor(String position) {
        this(position, null);
    }

    /**
     * @return the begin position of the period
     */
    public String getBeginPosition() {
        return begin;
    }

    /**
     * @return the end position of the period
     */
    public String getEndPosition() {
        return end;
    }
    /**
     * @return if this descriptor describes a period
     */
    public boolean isPeriod() {
        return getEndPosition() != null;
    }

    /**
     * @return the field name of the instant
     */
    public String getPosition() {
        return getBeginPosition();
    }

    /**
     * @return if this descriptor describes a instant
     */
    public boolean isInstant() {
        return !isPeriod();
    }

}
