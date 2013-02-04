/**
 * Copyright (C) 2013 by 52 North Initiative for Geospatial Open Source Software
 * GmbH
 *
 * Contact: Andreas Wytzisk 52 North Initiative for Geospatial Open Source
 * Software GmbH Martin-Luther-King-Weg 24 48155 Muenster, Germany
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
package org.n52.sos.ds.hibernate.cache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OfferingCacheUpdate extends CacheUpdate {

    protected static class UpdateThreadFactory implements ThreadFactory {
        private final AtomicInteger i = new AtomicInteger(0);
        private final ThreadGroup tg = new ThreadGroup("cache-update");
        @Override public Thread newThread(Runnable r) {
            return new Thread(tg, r, "cache-update-" + i.getAndIncrement());
        }
    }
    
    @Override
    public void run() {
        List<Offering> hOfferings = HibernateCriteriaQueryUtilities.getOfferingObjects(getSession());
        OfferingCache offeringCache = new OfferingCache(hOfferings);
        // fields required for multithreading
        log.debug("multithreading init");
        ExecutorService executor = Executors.newFixedThreadPool(Configurator.getInstance().getCacheThreadCount(), new UpdateThreadFactory());
        CountDownLatch offeringThreadsRunning = new CountDownLatch(hOfferings.size());
        IConnectionProvider connectionProvider = Configurator.getInstance().getConnectionProvider();
        List<OwsExceptionReport> owsReportsThrownByOfferingThreads = CollectionHelper.synchronizedLinkedList();
        for (Offering offering : hOfferings) {
            if (!containsDeletedProcedure(offering.getObservationConstellationOfferingObservationTypes())) {
                // create runnable for offeringId
                Runnable task = new OfferingCacheUpdateTask(offeringThreadsRunning, connectionProvider, offeringCache, offering, owsReportsThrownByOfferingThreads);
                // put runnable in executor service
                executor.submit(task);
            } else {
                offeringThreadsRunning.countDown();
                log.debug("Offering '{}' contains deleted procedure, latch.countDown().", offering.getIdentifier());
            }
        }
        executor.shutdown(); // <-- will finish all submitted tasks
        // wait for all threads to finish
        try {
            log.debug("Waiting for {} threads to finish", offeringThreadsRunning.getCount());
            offeringThreadsRunning.await();
        } catch (InterruptedException e) {/* nothing to do here */

        }
        log.debug("Finished waiting for other threads");
        if (!owsReportsThrownByOfferingThreads.isEmpty()) {
            getErrors().addAll(owsReportsThrownByOfferingThreads);
            return;
        }
        // save all information in cache
        offeringCache.save(getCache());
    }

    protected boolean containsDeletedProcedure(Set<ObservationConstellationOfferingObservationType> set) {
        for (ObservationConstellationOfferingObservationType obsConstOffObsType : set) {
            return obsConstOffObsType.getObservationConstellation().getProcedure().isDeleted();
        }
        return true;
    }

}