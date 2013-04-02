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

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.cache.ctrl.action.InMemoryCacheUpdate;
import org.n52.sos.cache.ctrl.action.ObservationInsertionInMemoryCacheUpdate;
import org.n52.sos.cache.ctrl.action.ResultInsertionInMemoryCacheUpdate;
import org.n52.sos.cache.ctrl.action.ResultTemplateInsertionInMemoryCacheUpdate;
import org.n52.sos.cache.ctrl.action.SensorDeletionInMemoryCacheUpdate;
import org.n52.sos.cache.ctrl.action.SensorInsertionInMemoryCacheUpdate;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 4.0
 */
public class InMemoryCacheController extends CacheFeederDAOCacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCacheController.class);

    /**
     * @see SensorInsertionInMemoryCacheUpdate
     */
    @Override
    protected void updateAfterSensorInsertion(WritableContentCache cache,
                                              InsertSensorRequest sosRequest,
                                              InsertSensorResponse sosResponse) {
        update(cache, new SensorInsertionInMemoryCacheUpdate(sosRequest, sosResponse));
    }

    /**
     * @see ObservationInsertionInMemoryCacheUpdate
     */
    @Override
    protected void updateAfterObservationInsertion(WritableContentCache cache, InsertObservationRequest sosRequest) {
        update(cache, new ObservationInsertionInMemoryCacheUpdate(sosRequest));
    }

    /**
     * @see SensorDeletionInMemoryCacheUpdate
     */
    @Override
    protected void updateAfterSensorDeletion(WritableContentCache cache, DeleteSensorRequest sosRequest) {
        update(cache, new SensorDeletionInMemoryCacheUpdate(sosRequest));
    }

    /**
     * @see ResultTemplateInsertionInMemoryCacheUpdate
     */
    @Override
    protected void updateAfterResultTemplateInsertion(WritableContentCache cache,
                                                      InsertResultTemplateRequest sosRequest,
                                                      InsertResultTemplateResponse sosResponse) {
        update(cache, new ResultTemplateInsertionInMemoryCacheUpdate(sosRequest, sosResponse));
    }

    @Override
    protected void updateAfterResultInsertion(WritableContentCache cache,
                                              String templateIdentifier,
                                              SosObservation sosObservation) {
        update(cache, new ResultInsertionInMemoryCacheUpdate(templateIdentifier, sosObservation));
    }

    /**
     * TODO Eike: test removal of locking mechanisms
     */
    private void update(WritableContentCache cache, InMemoryCacheUpdate cacheUpdate) {
        cacheUpdate.setCache(cache);
        LOGGER.debug("Running {}", cacheUpdate);
        cacheUpdate.execute();
    }
}
