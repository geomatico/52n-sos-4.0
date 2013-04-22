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

package org.n52.sos.ds.hibernate;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class HibernateTestCase extends CoreMatchers {
    @BeforeClass
    public static void init() {
        H2Configuration.assertInitialized();
    }

    protected static Session getSession() {
        return H2Configuration.getSession();
    }

    protected static void returnSession(Session session) {
        H2Configuration.returnSession(session);
    }

    public static <T> void assertThat(T actual, Matcher<? super T> matcher) {
        Assert.assertThat(actual, matcher);
    }

    public static <T> void assertThat(String reason, T actual, Matcher<? super T> matcher) {
        Assert.assertThat(reason, actual, matcher);
    }
}
