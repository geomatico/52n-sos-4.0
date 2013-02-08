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
package org.n52.sos.cache;

import java.util.Collections;
import java.util.Set;

import org.n52.sos.event.SosEvent;
import org.n52.sos.event.SosEventListener;
import org.n52.sos.event.events.ObservationDeletion;
import org.n52.sos.event.events.ObservationInsertion;
import org.n52.sos.event.events.ResultInsertion;
import org.n52.sos.event.events.ResultTemplateInsertion;
import org.n52.sos.event.events.SensorDeletion;
import org.n52.sos.event.events.SensorInsertion;
import org.n52.sos.event.events.SosContentChangeEvent;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ContentModificationListener implements SosEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentModificationListener.class);
    private static final Set<Class<? extends SosEvent>> TYPES = Collections.<Class<? extends SosEvent>>singleton(SosContentChangeEvent.class);
    
    @Override
    public Set<Class<? extends SosEvent>> getTypes() {
        return Collections.unmodifiableSet(TYPES);
    }

    private ACapabilitiesCacheController getCacheController() {
        return Configurator.getInstance().getCapabilitiesCacheController();
    }

    @Override
    public void handle(SosEvent event) {
        try {
            LOGGER.debug("Updating Cache after content modification: {}", event);
            if (event instanceof SensorInsertion) 
            {
                getCacheController().updateAfterSensorInsertion(
                		((SensorInsertion) event).getRequest(),
                		((SensorInsertion) event).getResponse());
            } 
            else if (event instanceof ObservationInsertion)
            {
                getCacheController().updateAfterObservationInsertion(
                		((ObservationInsertion) event).getRequest());
            }
            else if (event instanceof ObservationDeletion)
            {
            	// this update will always be performed based on database information
                getCacheController().updateAfterObservationDeletion();
            }
            else if (event instanceof ResultTemplateInsertion)
            {
                getCacheController().updateAfterResultTemplateInsertion(
                		((ResultTemplateInsertion) event).getRequest(),
                		((ResultTemplateInsertion) event).getResponse());
            }
            else if (event instanceof SensorDeletion)
            {
                getCacheController().updateAfterSensorDeletion(
                		((SensorDeletion) event).getRequest());
            } 
            else if (event instanceof ResultInsertion)
            {
            	getCacheController().updateAfterResultInsertion( 
            			((ResultInsertion)event).getResponse().getObservation() );
            }
            else {
                LOGGER.warn("Can not handle modification event: {}", event);
            }
            
        } catch (OwsExceptionReport ex) {
            LOGGER.error("Error processing Event", ex);
        }
    }
}
