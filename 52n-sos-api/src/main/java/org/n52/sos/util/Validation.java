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

import org.n52.sos.config.ConfigurationException;


/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class Validation {

    public static void notNull(String name, Object val) throws ConfigurationException {
        if (val == null) {
            throw new ConfigurationException(String.format("%s can not be null!", name));
        }
    }

    public static void greaterZero(String name, int i) throws ConfigurationException {
        if (i <= 0) {
            throw new ConfigurationException(String.format("%s can not be smaller or equal zero (was %d)!", name, i));
        }
    }

    public static void greaterEqualZero(String name, int i) throws ConfigurationException {
        if (i < 0) {
            throw new ConfigurationException(String.format("%s can not be smaller than zero (was %d)!", name, i));
        }
    }

    public static void notNullOrEmpty(String name, String val) throws ConfigurationException {
        notNull(name, val);
        if (val.isEmpty()) {
            throw new ConfigurationException(String.format("%s can not be empty!", name));
        }
    }

    private Validation() {
    }
}
