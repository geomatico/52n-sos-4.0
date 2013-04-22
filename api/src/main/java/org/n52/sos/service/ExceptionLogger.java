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

package org.n52.sos.service;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.event.SosEvent;
import org.n52.sos.event.SosEventListener;
import org.n52.sos.event.events.ExceptionEvent;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single point of exception logging.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 */
public class ExceptionLogger implements SosEventListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionLogger.class);
	public static final Set<Class<? extends SosEvent>> EVENTS = Collections
			.<Class<? extends SosEvent>>singleton(ExceptionEvent.class);

	@Override
	public Set<Class<? extends SosEvent>> getTypes() {
		return EVENTS;
	}

	@Override
	public void handle(final SosEvent event) {
		final ExceptionEvent ee = (ExceptionEvent) event;

		// TODO review logging of exceptions. Stacktrace only on debug level?
		if (ee.getException() instanceof OwsExceptionReport)
		{
			final OwsExceptionReport owse = (OwsExceptionReport) ee.getException();
			if (owse.getStatus().getCode() >= 500)
			{
				LOGGER.error("Exception thrown", owse);
			}
			else if (owse.getStatus().getCode() >= 400)
			{
				LOGGER.warn("Exception thrown", owse);
			}
			else {
				LOGGER.debug("Exception thrown", owse);
			}
		}
		else
		{
			LOGGER.debug("Error processing request", ee.getException());
		}
	}
}
