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
package org.n52.sos.cache.ctrl;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.cache.ContentCacheUpdate;
import org.n52.sos.cache.ctrl.action.ObservationInsertionUpdate;
import org.n52.sos.cache.ctrl.action.ResultInsertionUpdate;
import org.n52.sos.cache.ctrl.action.ResultTemplateInsertionUpdate;
import org.n52.sos.cache.ctrl.action.SensorDeletionUpdate;
import org.n52.sos.cache.ctrl.action.SensorInsertionUpdate;
import org.n52.sos.event.SosEvent;
import org.n52.sos.event.SosEventListener;
import org.n52.sos.event.events.ObservationInsertion;
import org.n52.sos.event.events.ResultInsertion;
import org.n52.sos.event.events.ResultTemplateInsertion;
import org.n52.sos.event.events.SensorDeletion;
import org.n52.sos.event.events.SensorInsertion;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0.0
 */
public class DefaultContentModificationListener implements SosEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultContentModificationListener.class);
    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends SosEvent>> TYPES = CollectionHelper.<Class<? extends SosEvent>>set(
            SensorInsertion.class,
            ObservationInsertion.class,
            ResultTemplateInsertion.class,
            SensorDeletion.class,
            ResultInsertion.class);

    @Override
    public Set<Class<? extends SosEvent>> getTypes() {
        return Collections.unmodifiableSet(TYPES);
    }

    @Override
    public void handle(SosEvent event) {
        if (event instanceof SensorInsertion) {
            SensorInsertion e = (SensorInsertion) event;
            handle(new SensorInsertionUpdate(e.getRequest(), e.getResponse()));
        } else if (event instanceof ObservationInsertion) {
            ObservationInsertion e = (ObservationInsertion) event;
            handle(new ObservationInsertionUpdate(e.getRequest()));
        } else if (event instanceof ResultTemplateInsertion) {
            ResultTemplateInsertion e = (ResultTemplateInsertion) event;
            handle(new ResultTemplateInsertionUpdate(e.getRequest(), e.getResponse()));
        } else if (event instanceof SensorDeletion) {
            SensorDeletion e = (SensorDeletion) event;
            handle(new SensorDeletionUpdate(e.getRequest()));
        } else if (event instanceof ResultInsertion) {
            ResultInsertion e = (ResultInsertion) event;
            handle(new ResultInsertionUpdate(e.getRequest().getTemplateIdentifier(),
                                             e.getResponse().getObservation()));
        } else {
            LOGGER.debug("Can not handle modification event: {}", event);
        }
    }

    protected void handle(ContentCacheUpdate update) {
        LOGGER.debug("Updating Cache after content modification: {}", update);
        try {
            Configurator.getInstance().getCacheController().update(update);
        } catch (OwsExceptionReport ex) {
            LOGGER.error("Error processing Event", ex);
        }
    }
}
