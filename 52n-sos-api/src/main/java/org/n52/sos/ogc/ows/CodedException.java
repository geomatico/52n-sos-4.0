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

import java.util.Collections;
import java.util.List;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class CodedException extends OwsExceptionReport {
    private final List<CodedException> exceptions = Collections.singletonList(this);
    private final ExceptionCode code;
    private String locator;
    private String message;

    public CodedException(ExceptionCode code) {
        this.code = code;
    }

    public ExceptionCode getCode() {
        return code;
    }

    public String getLocator() {
        return locator;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<CodedException> getExceptions() {
        return exceptions;
    }

    public CodedException at(String locator) {
        this.locator = locator;
        return this;
    }

    public CodedException at(Enum<?> locator) {
        return at(locator.name());
    }

    /**
     * @param message the message format
     * @param args    the optional formatting arguments
     *
     * @return this
     *
     * @see {@link String#format(java.lang.String, java.lang.Object[])}
     */
    public CodedException withMessage(String message, Object... args) {
        if (args != null && args.length > 0) {
            this.message = String.format(message, args);
        } else {
            this.message = message;
        }
        return this;
    }

    public CodedException causedBy(Throwable exception) {
        return (CodedException) initCause(exception);
    }
}
