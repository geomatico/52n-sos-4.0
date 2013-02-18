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
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.hibernate.ThreadLocalSessionFactory;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.GroupedAndNamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OfferingCacheUpdate extends CacheUpdate {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OfferingCacheUpdate.class);

    private static final String THREAD_GROUP_NAME = "offering-cache-update";
    private final int threadCount = Configurator.getInstance().getCacheThreadCount();
    private final ThreadFactory threadFactory = new GroupedAndNamedThreadFactory(THREAD_GROUP_NAME);
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount, threadFactory);
    private final IConnectionProvider connectionProvider = Configurator.getInstance().getDataConnectionProvider();
    private final ThreadLocalSessionFactory sessionFactory = new ThreadLocalSessionFactory(connectionProvider);
    private List<OwsExceptionReport> errors;
    private CountDownLatch offeringThreadsRunning;

    protected CountDownLatch getCountDownLatch() {
        return offeringThreadsRunning;
    }

    protected ExecutorService getExecutor() {
        return executor;
    }

    protected ThreadLocalSessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    @Override
    public void execute() {
        List<Offering> offerings = getOfferingObjects(getSession());
        OfferingCache offeringCache = new OfferingCache(offerings);

        LOGGER.debug("multithreading init");
        
        offeringThreadsRunning = new CountDownLatch(offerings.size());
        errors = CollectionHelper.synchronizedLinkedList();

        try {
            queueTasks(offerings, offeringCache);
            waitForTaskCompletion();
            LOGGER.debug("Finished waiting for other threads");
            if (!errors.isEmpty()) {
                getErrors().addAll(errors);
                return;
            }
            // save all information in cache
            offeringCache.save(getCache());
        } finally {
            getSessionFactory().close();
        }
    }

    protected boolean containsDeletedProcedure(Set<ObservationConstellationOfferingObservationType> set) {
        for (ObservationConstellationOfferingObservationType obsConstOffObsType : set) {
            return obsConstOffObsType.getObservationConstellation().getProcedure().isDeleted();
        }
        return true;
    }

    protected void queueTasks(List<Offering> hOfferings, OfferingCache offeringCache) {
        for (Offering offering : hOfferings) {
            queueTask(offering, offeringCache);
        }
    }

    protected void queueTask(Offering offering, OfferingCache offeringCache) {
        if (!containsDeletedProcedure(offering.getObservationConstellationOfferingObservationTypes())) {
            // create runnable for offeringId
            Runnable task = new OfferingCacheUpdateTask(getCountDownLatch(), getSessionFactory(), offeringCache, offering, errors);
            // put runnable in executor service
            getExecutor().submit(task);
        } else {
            getCountDownLatch().countDown();
            LOGGER.debug("Offering '{}' contains deleted procedure, latch.countDown().", offering.getIdentifier());
        }
    }

    protected void waitForTaskCompletion() {
        getExecutor().shutdown(); // <-- will finish all submitted tasks
        // wait for all threads to finish
        try {
            LOGGER.debug("Waiting for {} threads to finish", getCountDownLatch().getCount());
            getCountDownLatch().await();
        } catch (InterruptedException e) {
            /* nothing to do here */
        }
    }
}