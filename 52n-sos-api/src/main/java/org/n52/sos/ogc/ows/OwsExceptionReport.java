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

import java.util.Iterator;
import java.util.List;

import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.Configurator;

/**
 * Implementation of the ows service exception. The exception codes are defined according the ows common spec. version
 * 1.1.0
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class OwsExceptionReport extends Exception {
    private static final String namespace = OWSConstants.NS_OWS;
    private Integer responseCode;
    private String version;
    
    /**
     * @return Returns the ExceptionTypes of this exception
     */
    public abstract List<? extends CodedException> getExceptions();

    /**
     * Set SOS version
     *
     * @param version the version to set
     *
     * @return this
     */
    public OwsExceptionReport setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get SOS version
     *
     * @return SOS version
     */
    public String getVersion() {
        if (this.version == null) {
            /* FIXME shouldn't this be the other way around? defaulting to the newest version? */
            this.version = Configurator.getInstance().getServiceOperatorRepository()
                    .isVersionSupported(Sos1Constants.SERVICEVERSION)
                           ? Sos1Constants.SERVICEVERSION
                           : Sos2Constants.SERVICEVERSION;
        }
        return this.version;
    }

    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getMessage() {
        StringBuilder faultString = new StringBuilder();
        Iterator<? extends CodedException> i = getExceptions().iterator();
        if (i.hasNext()) {
            faultString.append(i.next().getMessage());
        }
        while (i.hasNext()) {
            faultString.append('\n').append(i.next().getMessage());
        }
        return faultString.toString();
    }

    /**
     * @return the HTTP response code of this {@code OwsExceptionReport} or {@code null} if it is not set
     */
    public Integer getResponseCode() {
        return responseCode;
    }

    /**
     * @return if the HTTP response code for this {@code OwsExceptionReport} is set
     */
    public boolean hasResponseCode() {
        return this.responseCode != null;
    }

    /**
     * Sets the HTTP response code for this {@code OwsExceptionReport}.
     *
     * @param responseCode the code
     *
     * @return this (for method chaining)
     */
    public OwsExceptionReport setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }
}
