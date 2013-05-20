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
package org.n52.sos.ds.hibernate.cache.base;

import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.getOfferingObjects;

import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.cache.AbstractQueuingDatasourceCacheUpdate;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OfferingCacheUpdate extends AbstractQueuingDatasourceCacheUpdate<Offering> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferingCacheUpdate.class);    
    private static final String THREAD_GROUP_NAME = "offering-cache-update";

    public OfferingCacheUpdate(int threads) {
        super(threads);
    }

    @Override
    protected String getThreadGroupName() {
        return THREAD_GROUP_NAME;
    }

    @Override
    protected List<Offering> getObjectsToQueue() {
        return getOfferingObjects(getSession());
    }    

    @Override    
    protected void queueTask(Offering offering) {
        @SuppressWarnings("unchecked")
        List<ObservationConstellation> observationConstellation = getSession()
                .createCriteria(ObservationConstellation.class)
                .add(Restrictions.eq(ObservationConstellation.DELETED, false))
                .add(Restrictions.eq(ObservationConstellation.OFFERING, offering)).list();
        if (observationConstellation != null && !observationConstellation.isEmpty()) {
            // create runnable for offeringId
            Runnable task =
                     new OfferingCacheUpdateTask(getCountDownLatch(), getSessionFactory(), getCache(), offering, getErrorList());
            // put runnable in executor service
            getExecutor().submit(task);
        } else {
            getCountDownLatch().countDown();
            LOGGER.debug("Offering '{}' contains deleted procedure, latch.countDown().", offering.getIdentifier());
        }
    }
}