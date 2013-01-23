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

import org.n52.sos.exception.IExceptionCode;


public class OwsException {
    
    private IExceptionCode code;
    
    private String locator;
    
    private String[] messages;
    
    private Exception exception;
    

    public OwsException(IExceptionCode code, String locator, String[] messages) {
       this.code = code;
       this.locator = locator;
       this.messages = messages;
    }

    public OwsException(IExceptionCode code, String locator, Exception exception) {
        this.code = code;
        this.locator = locator;
        this.exception = exception;
    }

    public OwsException(IExceptionCode code, String locator, String[] messages, Exception exception) {
        this.code = code;
        this.locator = locator;
        this.messages = messages;
        this.exception = exception;
    }

    public IExceptionCode getCode() {
        return code;
    }

    public String getLocator() {
        return locator;
    }

    public String[] getMessages() {
        return messages;
    }

    public Exception getException() {
        return exception;
    }

}
