/*
 * Copyright (C) 2013 52north.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.hibernate.userType;

import java.net.URI;
import java.net.URISyntaxException;
import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class UriType extends AbstractStringBasedUserType<URI> {

    public UriType() {
        super(URI.class);
    }

    @Override
    protected URI decode(String s) throws HibernateException {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new TypeMismatchException(e);
        }
    }

    @Override
    protected String encode(URI t) throws HibernateException {
        return t.toString();
    }
}
