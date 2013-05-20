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
package org.n52.sos.ds.hibernate.cache;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.n52.sos.ds.ConnectionProvider;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.hibernate.ThreadLocalSessionFactory;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.GroupedAndNamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 * @author Shane StClair <shane@axiomalaska.com>
 */
public abstract class AbstractQueuingDatasourceCacheUpdate<T> extends AbstractDatasourceCacheUpdate{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractQueuingDatasourceCacheUpdate.class);
    private final ThreadFactory threadFactory = new GroupedAndNamedThreadFactory(getThreadGroupName());
    private final ConnectionProvider connectionProvider = Configurator.getInstance().getDataConnectionProvider();
    private final ThreadLocalSessionFactory sessionFactory = new ThreadLocalSessionFactory(connectionProvider);
    private final ExecutorService executor;
    private List<OwsExceptionReport> errorList;
    private CountDownLatch countDownLatch;
    
    public AbstractQueuingDatasourceCacheUpdate(int threads) {
        this.executor = Executors.newFixedThreadPool(threads, threadFactory);
    }

    protected abstract String getThreadGroupName();
    
    protected CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    protected ExecutorService getExecutor() {
        return executor;
    }

    protected ThreadLocalSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected List<OwsExceptionReport> getErrorList() {
        return this.errorList;
    }
    
    protected abstract List<T> getObjectsToQueue();
    
    @Override
    public void execute() {
        LOGGER.debug("multithreading init");

        List<T> objectsToQueue = getObjectsToQueue();
        countDownLatch = new CountDownLatch(objectsToQueue.size());
        errorList = CollectionHelper.synchronizedList();

        try {
            queueTasks(objectsToQueue);
            waitForTaskCompletion();
            LOGGER.debug("Finished waiting for other threads");
            getErrors().add(errorList);
        } finally {
            try {
                getSessionFactory().close();
            } catch (ConnectionProviderException cpe) {
                LOGGER.error("Error while closing SessionFactory", cpe);
            }
        }
    }

    protected void queueTasks(List<T> objectsToQueue) {
        for (T objectToQueue : objectsToQueue) {
            queueTask(objectToQueue);
        }
    }

    protected abstract void queueTask(T objectToQueue);

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
