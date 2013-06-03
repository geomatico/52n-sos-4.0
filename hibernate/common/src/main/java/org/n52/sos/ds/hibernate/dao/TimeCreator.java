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
package org.n52.sos.ds.hibernate.dao;

import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.n52.sos.ogc.gml.time.TimePeriod;

/**
 * Abstract class to create a time period object
 * @author CarstenHollmann
 * @since 4.0.0
 */
public abstract class TimeCreator {

    /**
     * Creates a time period object from sources
     * @param minStart Min start timestamp
     * @param maxStart Max start timestamp
     * @param maxEnd Max end timestamp
     * @return Time period object
     */
    public TimePeriod createTimePeriod(Timestamp minStart, Timestamp maxStart, Timestamp maxEnd) {
        DateTime start = new DateTime(minStart);
        DateTime end = new DateTime(maxStart);
        if (maxEnd != null) {
            DateTime endTmp = new DateTime(maxEnd);
            if (endTmp.isAfter(end)) {
                end = endTmp;
            }
        }
        return new TimePeriod(start, end);
    }
}
