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


import org.n52.sos.cache.ctrl.action.CompleteCacheUpdate;
import org.n52.sos.cache.ctrl.action.ObservationDeletionUpdate;
import org.n52.sos.cache.ctrl.action.ObservationInsertionUpdate;
import org.n52.sos.cache.ctrl.action.ResultInsertionUpdate;
import org.n52.sos.cache.ctrl.action.ResultTemplateInsertionUpdate;
import org.n52.sos.cache.ctrl.action.SensorDeletionUpdate;
import org.n52.sos.cache.ctrl.action.SensorInsertionUpdate;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 *
 * @since 4.0
 */
public class ContentCacheControllerImpl extends AbstractContentCacheController {

    @Override
    public void updateCacheFromDatasource() throws OwsExceptionReport {
        update(new CompleteCacheUpdate());
    }

    @Override
    public void updateAfterObservationDeletion(SosObservation o) throws OwsExceptionReport {
        update(new ObservationDeletionUpdate(o));
    }

    @Override
    public void updateAfterSensorInsertion(InsertSensorRequest req, InsertSensorResponse res) throws OwsExceptionReport {
        update(new SensorInsertionUpdate(req, res));
    }

    @Override
    public void updateAfterObservationInsertion(InsertObservationRequest req) throws OwsExceptionReport {
        update(new ObservationInsertionUpdate(req));
    }

    @Override
    public void updateAfterSensorDeletion(DeleteSensorRequest req) throws OwsExceptionReport {
        update(new SensorDeletionUpdate(req));
    }

    @Override
    public void updateAfterResultTemplateInsertion(InsertResultTemplateRequest req, InsertResultTemplateResponse res)
            throws OwsExceptionReport {
        update(new ResultTemplateInsertionUpdate(req, res));
    }

    @Override
    public void updateAfterResultInsertion(String id, SosObservation o) throws OwsExceptionReport {
        update(new ResultInsertionUpdate(id, o));
    }
}
