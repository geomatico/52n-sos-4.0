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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.ConnectionProvider;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.hibernate.ThreadLocalSessionFactory;
import org.n52.sos.ds.hibernate.cache.AbstractDatasourceCacheUpdate;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
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
public class OfferingCacheUpdate extends AbstractDatasourceCacheUpdate {
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferingCacheUpdate.class);
    private static final String THREAD_GROUP_NAME = "offering-cache-update";
    private final ThreadFactory threadFactory = new GroupedAndNamedThreadFactory(THREAD_GROUP_NAME);
    private final ConnectionProvider connectionProvider = Configurator.getInstance().getDataConnectionProvider();
    private final ThreadLocalSessionFactory sessionFactory = new ThreadLocalSessionFactory(connectionProvider);
    private final ExecutorService executor;
    private List<OwsExceptionReport> errors;
    private CountDownLatch offeringThreadsRunning;

    public OfferingCacheUpdate(int threads) {
        this.executor = Executors.newFixedThreadPool(threads, threadFactory);
    }

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

        LOGGER.debug("multithreading init");

        offeringThreadsRunning = new CountDownLatch(offerings.size());
        errors = CollectionHelper.synchronizedList();

        try {
            queueTasks(offerings);
            waitForTaskCompletion();
            LOGGER.debug("Finished waiting for other threads");
            getErrors().add(errors);
        } finally {
            try {
                getSessionFactory().close();
            } catch (ConnectionProviderException cpe) {
                LOGGER.error("Error while closing SessionFactory", cpe);
            }
        }
    }

    protected void queueTasks(List<Offering> hOfferings) {
        for (Offering offering : hOfferings) {
            queueTask(offering);
        }
    }

    protected void queueTask(Offering offering) {
        @SuppressWarnings("unchecked")
        List<ObservationConstellation> observationConstellation = getSession()
                .createCriteria(ObservationConstellation.class)
                .add(Restrictions.eq(ObservationConstellation.DELETED, false))
                .add(Restrictions.eq(ObservationConstellation.OFFERING, offering)).list();
        if (observationConstellation != null && !observationConstellation.isEmpty()) {
            // create runnable for offeringId
            Runnable task =
                     new OfferingCacheUpdateTask(getCountDownLatch(), getSessionFactory(), getCache(), offering, errors);
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