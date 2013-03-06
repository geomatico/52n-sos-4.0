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

import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.util.Cleanupable;

/**
 * TODO JavaDoc
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface IContentCacheController extends Cleanupable {

    ContentCache getCache();

    void updateAfterObservationDeletion() throws OwsExceptionReport;

    void updateAfterObservationInsertion(InsertObservationRequest sosRequest);

    void updateAfterResultInsertion(String templateIdentifier, SosObservation sosObservation);

    void updateAfterResultTemplateInsertion(InsertResultTemplateRequest sosRequest,
                                            InsertResultTemplateResponse sosResponse);

    void updateAfterSensorDeletion(DeleteSensorRequest sosRequest);

    void updateAfterSensorInsertion(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse);

    /**
     * @return true, if updated successfully
     * @throws OwsExceptionReport
     *             if the query of one of the values described upside failed
     */
    boolean updateCacheFromDatasource() throws OwsExceptionReport;
}
