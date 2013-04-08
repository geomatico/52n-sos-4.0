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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classes that saves the cache state after each apply. Actual functionality is delegated to subclasses.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractContentCacheController extends AbstractSchedulingContentCacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContentCacheController.class);
    private static final String CACHE_FILE = "cache.tmp";
    private static final AtomicInteger COMPLETE_UPDATE_COUNT = new AtomicInteger(0);
    private static final AtomicInteger PARTIAL_UPDATE_COUNT = new AtomicInteger(0);
    private String cacheFile;
    private CompleteUpdate current = null;
    private CompleteUpdate next = null;
    private volatile WritableContentCache cache;
    private Lock lock = new ReentrantLock();

    public AbstractContentCacheController() {
        loadOrCreateCache();
    }

    @Override
    public WritableContentCache getCache() {
        return this.cache;
    }

    protected File getCacheFile() {
        if (this.cacheFile == null) {
            this.cacheFile = new File(Configurator.getInstance().getBasePath(), CACHE_FILE).getAbsolutePath();
        }
        return new File(this.cacheFile);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        persistCache();
    }

    private void loadOrCreateCache() {
        File f = getCacheFile();
        if (f.exists() && f.canRead()) {
            LOGGER.debug("Reading cache from temp file '{}'", f.getAbsolutePath());
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new FileInputStream(f));
                setCache((WritableContentCache) in.readObject());
            } catch (IOException t) {
                LOGGER.error(String.format("Error reading cache file '%s'", f.getAbsolutePath()), t);
            } catch (ClassNotFoundException t) {
                LOGGER.error(String.format("Error reading cache file '%s'", f.getAbsolutePath()), t);
            } finally {
                IOUtils.closeQuietly(in);
            }
            f.delete();
        } else {
            LOGGER.debug("No cache temp file found at '{}'", f.getAbsolutePath());
        }
        if (getCache() == null) {
            setCache(CacheFactory.getInstance().create());
        } else {
            setInitialized(true);
        }
    }

    private void persistCache() {
        File f = getCacheFile();
        if (!f.exists() || f.delete()) {
            ObjectOutputStream out = null;
            if (getCache() != null) {
                LOGGER.debug("Serializing cache to {}", f.getAbsolutePath());
                try {
                    if (f.createNewFile() && f.canWrite()) {
                        out = new ObjectOutputStream(new FileOutputStream(f));
                        out.writeObject(getCache());
                    } else {
                        LOGGER.error("Can not create writable file {}", f.getAbsolutePath());
                    }
                } catch (IOException t) {
                    LOGGER.error(String.format("Error serializing cache to '%s'", f.getAbsolutePath()), t);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    protected void setCache(WritableContentCache wcc) {
        this.cache = wcc;
    }

    public void update(CacheUpdate update) throws OwsExceptionReport {
        if (update != null) {
            if (update.isCompleteUpdate()) {
                executeComplete(new CompleteUpdate(update));
            } else {
                executePartial(new PartialUpdate(update));
            }
        }
    }

    private void runCurrent() throws OwsExceptionReport {
        LOGGER.trace("Starting update {}", this.current);
        this.current.execute();
        LOGGER.trace("Finished update {}", this.current);
        lock();
        try {
            CompleteUpdate u = this.current;
            this.current = null;
            u.signalWaiting();
        } finally {
            unlock();
        }
    }

    private void executePartial(PartialUpdate update) throws OwsExceptionReport {
        update.execute(getCache());
        lock();
        try {
            if (this.current != null) {
                this.current.addUpdate(update);
            }
        } finally {
            unlock();
        }
    }

    private void executeComplete(CompleteUpdate update) throws OwsExceptionReport {
        boolean isCurrent = false;
        boolean isNext = false;
        CompleteUpdate waitFor = null;
        lock();
        try {
            if (current == null || current.isFinished()) {
                current = update;
                isCurrent = true;
            } else if (current.isNotYetStarted()) {
                waitFor = current;
            } else if (next == null || next.isFinished()) {
                next = update;
                waitFor = current;
                isNext = true;
            } else {
                waitFor = next;
            }
        } finally {
            unlock();
        }

        if (isCurrent) {
            runCurrent();
        } else if (isNext) {
            if (waitFor != null) {
                LOGGER.trace("{} waiting for {}", update, waitFor);
                waitFor.waitForCompletion();
                LOGGER.trace("{} stopped waiting for {}", update, waitFor);
            }
            lock();
            try {
                current = next;
                next = null;
            } finally {
                unlock();
            }
            runCurrent();
        } else if (waitFor != null) {
            LOGGER.trace("{} waiting for {}", update, waitFor);
            waitFor.waitForCompletion();
            LOGGER.trace("{} stopped waiting for {}", update, waitFor);
        }
    }

    protected void lock() {
        lock.lock();
    }

    protected void unlock() {
        lock.unlock();
    }

    private enum State {
        WAITING,
        RUNNING,
        APPLYING_UPDATES,
        FINISHED,
        FAILED
    }

    private abstract class Update {
        private final CacheUpdate update;

        Update(CacheUpdate update) {
            this.update = update;
        }

        CacheUpdate getUpdate() {
            return update;
        }
    }

    private class PartialUpdate extends Update {
        private final int nr = PARTIAL_UPDATE_COUNT.getAndIncrement();

        PartialUpdate(CacheUpdate update) {
            super(update);
        }

        synchronized void execute(WritableContentCache cache) throws OwsExceptionReport {
            LOGGER.trace("Starting Update {}", getUpdate());
            getUpdate().reset();
            getUpdate().setCache(cache);
            getUpdate().execute();
            LOGGER.trace("Finished Update {}", getUpdate());
            if (getUpdate().failed()) {
                throw getUpdate().getFailureCause();
            }
        }

        @Override
        public String toString() {
            return String.format("PartialUpdate[#%d]", nr);
        }
    }

    private class CompleteUpdate extends Update {
        private ConcurrentLinkedQueue<PartialUpdate> updates = new ConcurrentLinkedQueue<PartialUpdate>();
        private final Lock lock = new ReentrantLock();
        private final Condition finished = lock.newCondition();
        private State state = State.WAITING;
        private final int nr = COMPLETE_UPDATE_COUNT.getAndIncrement();

        CompleteUpdate(CacheUpdate update) {
            super(update);
        }

        void addUpdate(PartialUpdate update) {
            updates.offer(update);
        }

        State getState() {
            lock();
            try {
                return state;
            } finally {
                unlock();
            }
        }

        boolean isFinished() {
            lock();
            try {
                return getState() == State.FINISHED ||
                       getState() == State.FAILED;
            } finally {
                unlock();
            }
        }

        boolean isNotYetStarted() {
            lock();
            try {
                return getState() == State.WAITING;
            } finally {
                unlock();
            }
        }

        void setState(State state) {
            AbstractContentCacheController.this.lock();
            try {
                lock();
                try {
                    LOGGER.debug("State change: {} -> {}", this.state, state);
                    this.state = state;
                } finally {
                    unlock();
                }
            } finally {
                AbstractContentCacheController.this.unlock();
            }
        }

        void execute() throws OwsExceptionReport {
            setCache(execute(getCache()));
        }

        WritableContentCache execute(WritableContentCache cache) throws OwsExceptionReport {
            if (isFinished()) {
                throw new IllegalStateException("already finished");
            }
            setState(State.RUNNING);
            getUpdate().setCache(cache);
            LOGGER.trace("Starting update {}", getUpdate());
            getUpdate().execute();
            LOGGER.trace("Finished update {}", getUpdate());
            lock();
            try {
                if (getUpdate().failed()) {
                    setState(State.FAILED);
                    throw getUpdate().getFailureCause();
                } else {
                    setState(State.APPLYING_UPDATES);
                    PartialUpdate pu;
                    WritableContentCache cc = getUpdate().getCache();
                    while ((pu = updates.poll()) != null) {
                        pu.execute(cc);
                    }
                    setState(State.FINISHED);
                    return cc;
                }
            } finally {
                unlock();
            }
        }

        void waitForCompletion() throws OwsExceptionReport {
            lock();
            try {
                while (!isFinished()) {
                    try {
                        finished.await();
                    } catch (InterruptedException ex) {
                    }
                }
                if (getState() == State.FAILED) {
                    throw getUpdate().getFailureCause();
                }
            } finally {
                unlock();
            }
        }

        void signalWaiting() {
            lock();
            try {
                finished.signalAll();
            } finally {
                unlock();
            }
        }

        @Override
        public String toString() {
            return String.format("CompleteUpdate[#%d]", nr);
        }

        protected void unlock() {
            lock.unlock();
        }

        protected void lock() {
            lock.lock();
        }
    }
}
