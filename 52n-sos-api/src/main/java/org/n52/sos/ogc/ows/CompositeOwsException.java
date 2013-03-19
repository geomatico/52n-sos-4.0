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
package org.n52.sos.ogc.ows;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class CompositeOwsException extends OwsExceptionReport {
    private static final long serialVersionUID = 1L;
    private List<CodedException> exceptions = new LinkedList<CodedException>();

    public CompositeOwsException(OwsExceptionReport... exceptions) {
        add(exceptions);
    }

    public CompositeOwsException(Collection<? extends OwsExceptionReport> exceptions) {
        add(exceptions);
    }

    public CompositeOwsException() {
    }

    public CompositeOwsException add(Collection<? extends OwsExceptionReport> exceptions) {
        if (exceptions != null) {
            for (OwsExceptionReport e : exceptions) {
                this.exceptions.addAll(e.getExceptions());
            }
        }
        return this;
    }

    public CompositeOwsException add(OwsExceptionReport... exceptions) {
        return add(Arrays.asList(exceptions));
    }

    @Override
    public List<? extends CodedException> getExceptions() {
        return Collections.unmodifiableList(this.exceptions);
    }

    public void throwIfNotEmpty() throws CompositeOwsException {
        if (hasExceptions()) {
            throw this;
        }
    }

    public int size() {
        return this.exceptions.size();
    }

    public boolean hasExceptions() {
        return !this.exceptions.isEmpty();
    }
}
