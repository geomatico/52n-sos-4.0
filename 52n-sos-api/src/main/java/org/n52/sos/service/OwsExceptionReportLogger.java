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
import org.n52.sos.event.events.OwsExceptionReportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single point of exception logging.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OwsExceptionReportLogger implements SosEventListener {
    private static final Logger log = LoggerFactory.getLogger(OwsExceptionReportLogger.class);
    public static final Set<Class<? extends SosEvent>> EVENTS = Collections
            .<Class<? extends SosEvent>>singleton(OwsExceptionReportEvent.class);

    @Override
    public Set<Class<? extends SosEvent>> getTypes() {
        return EVENTS;
    }

    @Override
    public void handle(SosEvent event) {
        OwsExceptionReportEvent owsere = (OwsExceptionReportEvent) event;
        log.debug("Error processing request", owsere.getExceptionReport());
    }
}
