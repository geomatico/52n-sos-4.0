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

import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Interface for implementations of cache feeder DAOs. Used to feed the
 * CapabilitiesCache with data from the data source.
 * 
 */
public interface ICacheFeederDAO {

    public void updateCache(WritableContentCache capabilitiesCache) throws OwsExceptionReport;

    /**
     * @deprecated should be deleted after successful test of InMemoryCacheController 
     */
    public void updateAfterSensorInsertion(WritableContentCache capabilitiesCache) throws OwsExceptionReport;

    /**
     * @deprecated should be deleted after successful test of InMemoryCacheController 
     */
    public void updateAfterObservationInsertion(WritableContentCache capabilitiesCache) throws OwsExceptionReport;

    /**
     * @deprecated should be deleted after successful test of InMemoryCacheController 
     */
    public void updateAfterSensorDeletion(WritableContentCache capabilitiesCache) throws OwsExceptionReport;

    /**
     * @deprecated should be deleted after successful test of InMemoryCacheController 
     */
    public void updateAfterResultTemplateInsertion(WritableContentCache capabilitiesCache) throws
            OwsExceptionReport;

    public void updateAfterObservationDeletion(WritableContentCache capabilitiesCache) throws OwsExceptionReport;
    
}