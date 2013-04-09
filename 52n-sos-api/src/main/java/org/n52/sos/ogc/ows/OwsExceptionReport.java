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

import org.n52.sos.exception.CodedException;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.HTTPConstants;

/**
 * Implementation of the ows service exception. The exception codes are defined according the ows common spec. version
 * 1.1.0
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public abstract class OwsExceptionReport extends Exception {

	private static final long serialVersionUID = 52L;
	private static final String namespace = OWSConstants.NS_OWS;
	private HTTPConstants.StatusCode status;
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
	public OwsExceptionReport setVersion(final String version) {
		this.version = version;
		return this;
	}

	/**
	 * Get SOS version
	 *
	 * @return SOS version
	 */
	public String getVersion() {
		if (version == null) {
			/* FIXME shouldn't this be the other way around? defaulting to the newest version? */
			version = Configurator.getInstance().getServiceOperatorRepository()
					.isVersionSupported(Sos1Constants.SERVICEVERSION)
					? Sos1Constants.SERVICEVERSION
							: Sos2Constants.SERVICEVERSION;
		}
		return version;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getMessage() {
		final StringBuilder faultString = new StringBuilder();
		final Iterator<? extends CodedException> i = getExceptions().iterator();
		if (i.hasNext()) {
			faultString.append(i.next().getMessage());
		}
		while (i.hasNext()) {
			faultString.append('\n').append(i.next().getMessage());
		}
		return faultString.toString();
	}

	/**
	 * @return the HTTP response code of this {@code OwsExceptionReport} or<br>
	 * 			{@code getExceptions().get(0).getStatus()} if it is not set and 
	 * 			{@code getExceptions().get(0) != this}.
	 */
	public HTTPConstants.StatusCode getStatus() {
		if ((status == null) && (getExceptions().get(0) != this))
		{
			return getExceptions().get(0).getStatus();
		}
		return status;
	}

	/**
	 * @return <tt>true</tt>, if a HTTP response code for this 
	 * 			{@code OwsExceptionReport} or any sub exception is available
	 */
	public boolean hasResponseCode() {
		return getStatus() != null;
	}

	/**
	 * Sets the HTTP response code for this {@code OwsExceptionReport}.
	 *
	 * @param status the code
	 *
	 * @return this (for method chaining)
	 */
	public OwsExceptionReport setStatus(final HTTPConstants.StatusCode responseCode) {
		status = responseCode;
		return this;
	}

}
