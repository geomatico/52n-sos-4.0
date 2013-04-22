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
package org.n52.sos.binding;

/**
 * Class to identifiy a Binding. Used to keep the interfaces stable if the identification of a Binding changes from
 * servlet path to e.g. Content-Type.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class BindingKey {
    private final String servletPath;

    public BindingKey(String servletPath) {
        if (servletPath == null) {
            throw new NullPointerException();
        }
        this.servletPath = servletPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    @Override
    public int hashCode() {
        return 41 * 7 + this.servletPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BindingKey) {
            BindingKey o = (BindingKey) obj;
            return getServletPath() == null ? o.getServletPath() == null
                   : getServletPath().equals(o.getServletPath());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("BindingKey[servletPath=%s]", getServletPath());
    }
}
