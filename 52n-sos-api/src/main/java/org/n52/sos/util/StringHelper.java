/**
 * Copyright (C) 2012
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

import java.util.Arrays;
import java.util.Iterator;

public class StringHelper {

    public static StringBuffer join(CharSequence sep, StringBuffer buff, Iterable<?> src) {
        Iterator<?> it = src.iterator();
        if (it.hasNext()) {
            buff.append(it.next());
        }
        while (it.hasNext()) {
            buff.append(sep).append(it.next());
        }
        return buff;
    }

    public static String join(CharSequence sep, Iterable<?> src) {
        return join(sep, new StringBuffer(), src).toString();
    }

    public static StringBuffer join(CharSequence sep, StringBuffer buff, Object... src) {
        return join(sep, buff, Arrays.asList(src));
    }

    public static String join(CharSequence sep, Object... src) {
        return join(sep, Arrays.asList(src));
    }

    private StringHelper() {
    }
}
