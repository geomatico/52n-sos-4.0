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
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface ContentCacheController extends Cleanupable {
    /**
     * @return the content cache
     */
    ContentCache getCache();

    /**
     * Update the cache after a observation was deleted.
     *
     * @param deletedObservation the deleted observation
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateAfterObservationDeletion(SosObservation deletedObservation) throws OwsExceptionReport;

    /**
     * Update the cache after a observation was inserted.
     *
     * @param sosRequest the InsertObservation request
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateAfterObservationInsertion(InsertObservationRequest sosRequest) throws OwsExceptionReport;

    /**
     * Update the cache after a result was inserted.
     *
     * @param templateIdentifier the identifier of the result template
     * @param sosObservation     the observation representing the inserted values
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateAfterResultInsertion(String templateIdentifier, SosObservation sosObservation) throws OwsExceptionReport;

    /**
     * Update the cache after a result template was inserted.
     *
     * @param sosRequest  the request
     * @param sosResponse the response
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateAfterResultTemplateInsertion(InsertResultTemplateRequest sosRequest,
                                            InsertResultTemplateResponse sosResponse) throws OwsExceptionReport;

    /**
     * Update the cache after a sensor was deleted.
     *
     * @param sosRequest the DeleteSensor request
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateAfterSensorDeletion(DeleteSensorRequest sosRequest) throws OwsExceptionReport;

    /**
     * Update the cache after a sensor was inserted.
     *
     * @param sosRequest  the request
     * @param sosResponse the response
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateAfterSensorInsertion(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse) throws
            OwsExceptionReport;

    /**
     * Update the cache from the underlying datasource.
     *
     * @throws OwsExceptionReport if an error occurs
     */
    void updateCacheFromDatasource() throws OwsExceptionReport;
}
