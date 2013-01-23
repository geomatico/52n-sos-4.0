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
package org.n52.sos.ds;

import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.AbstractServiceRequest;

/**
 * interface for getting time for a passed GetFeatureOfInterestTime request from
 * the data source
 * 
 */
public interface IGetFeatureOfInterestTimeDAO extends IOperationDAO {

    /**
     * Get the times period of observations for a specific FOI contained in data
     * source.
     * 
     * @param request
     *            GetFeatureOfInterestTime request
     * @return Returns the GetFeatureOfInterestTime response
     * @throws OwsExceptionReport
     *             If an error occurs.
     */
    public TimePeriod getFeatureOfInterestTime(AbstractServiceRequest request) throws OwsExceptionReport;

}
