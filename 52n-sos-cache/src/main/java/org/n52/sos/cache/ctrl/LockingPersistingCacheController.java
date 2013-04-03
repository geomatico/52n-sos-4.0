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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.cache.WritableCache;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classes that saves the wcc state after each update. Actual functionality is delegated to subclasses.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class LockingPersistingCacheController extends ScheduledContentCacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockingPersistingCacheController.class);
    private static final String CACHE_FILE = "cache.tmp";
    private final ReadWriteLock updateLock = new ReentrantReadWriteLock(true);
    private String cacheFile;
    private WritableContentCache cache;

    public LockingPersistingCacheController() {
        loadOrCreateCache();
    }

    protected WritableContentCache createCacheObject() {
        return new WritableCache();
    }

    @Override
    public ContentCache getCache() {
        this.updateLock.readLock().lock();
        LOGGER.trace("Read locked");
        try {
            return this.cache;
        } finally {
            this.updateLock.readLock().unlock();
            LOGGER.trace("Read unlocked");
        }
    }

    private void setCache(WritableContentCache cache) {
        this.updateLock.writeLock().lock();
        LOGGER.trace("Write locked");
        try {
            this.cache = cache;
        } finally {
            this.updateLock.writeLock().unlock();
            LOGGER.trace("Write unlocked");
        }

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
                this.cache = (WritableContentCache) in.readObject();
            } catch (Throwable t) {
                LOGGER.error(String.format("Error reading cache file '%s'", f.getAbsolutePath()), t);
            } finally {
                IOUtils.closeQuietly(in);
            }
            try {
                f.delete();
            } catch (Throwable t) {
                LOGGER.error(String.format("Error deleting cache file '%s'", f.getAbsolutePath()), t);
            }
        } else {
            LOGGER.debug("No cache temp file found at '{}'", f.getAbsolutePath());
        }
        if (this.cache == null) {
            this.cache = createCacheObject();
        } else {
            setInitialized(true);
        }
    }

    protected void persistCache() {
        File f = getCacheFile();
        if (!f.exists() || f.delete()) {
            ObjectOutputStream out = null;
            if (this.cache != null) {
                LOGGER.debug("Serializing cache to {}", f.getAbsolutePath());
                try {
                    if (f.createNewFile() && f.canWrite()) {
                        out = new ObjectOutputStream(new FileOutputStream(f));
                        out.writeObject(this.cache);
                    } else {
                        LOGGER.error("Can not create writable file {}", f.getAbsolutePath());
                    }
                } catch (Throwable t) {
                    LOGGER.error(String.format("Error serializing cache to '%s'", f.getAbsolutePath()), t);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    /**
     * Acquire the wcc update writeLock.
     *
     * @param what for what is the writeLock acquired (for formatting the logged error)
     *
     * @return if the writeLock was acquired
     *
     * @throws InterruptedException if the thread was interrupted while waiting for the writeLock
     */
    protected boolean writeLock(Object what) throws InterruptedException {
        boolean locked = updateLock.writeLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);
        if (!locked) {
            LOGGER.warn("\n******\n{} not successful because of time out while waiting for update lock. Waited {} "
                        + "ms.\n******\n", what, SosConstants.UPDATE_TIMEOUT);
        } else {
            LOGGER.trace("Write locked for {}", what);
        }
        return locked;
    }

    /**
     * Conditionally unWritelock the wcc update writeLock.
     *
     * @param locked if this thread has the writeLock
     */
    protected void unWritelock(boolean locked) {
        if (locked) {
            LOGGER.trace("Write unlocked");
            updateLock.writeLock().unlock();
        }
    }

    @Override
    public void updateAfterObservationDeletion() throws OwsExceptionReport {
        boolean locked = false;
        try {
            if (locked = writeLock("Observation deletion cache update")) {
                updateAfterObservationDeletion(this.cache);
                persistCache();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
    }

    @Override
    public void updateAfterObservationInsertion(InsertObservationRequest sosRequest) {
        boolean locked = false;
        try {
            if (locked = writeLock("Observation insertion cache update")) {
                updateAfterObservationInsertion(this.cache, sosRequest);
                persistCache();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
    }

    @Override
    public void updateAfterResultInsertion(String templateIdentifier, SosObservation sosObservation) {
        boolean locked = false;
        try {
            if (locked = writeLock("Result insertion cache update")) {
                updateAfterResultInsertion(this.cache, templateIdentifier, sosObservation);
                persistCache();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
    }

    @Override
    public void updateAfterResultTemplateInsertion(InsertResultTemplateRequest sosRequest,
                                                         InsertResultTemplateResponse sosResponse) {
        boolean locked = false;
        try {
            if (locked = writeLock("Result template insertion cache update")) {
                updateAfterResultTemplateInsertion(this.cache, sosRequest, sosResponse);
                persistCache();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
    }

    @Override
    public void updateAfterSensorDeletion(DeleteSensorRequest sosRequest) {
        boolean locked = false;
        try {
            if (locked = writeLock("Sensor deletion cache update")) {
                updateAfterSensorDeletion(this.cache, sosRequest);
                persistCache();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
    }

    @Override
    public void updateAfterSensorInsertion(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse) {
        boolean locked = false;
        try {
            if (locked = writeLock("Sensor insertion cache update")) {
                updateAfterSensorInsertion(this.cache, sosRequest, sosResponse);
                persistCache();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
    }

    @Override
    public boolean updateCacheFromDatasource() throws OwsExceptionReport {
        boolean locked = false;
        try {
            if (locked = writeLock("Datasource cache update")) {
                WritableContentCache wcc = createCacheObject();
                boolean result = updateCacheFromDatasource(wcc);
                setCache(wcc);
                persistCache();
                return result;
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ex);
        } finally {
            unWritelock(locked);
        }
        return false;
    }

    /**
     * @param cache the cache
     *
     * @throws OwsExceptionReport if an error occurs
     * @see #updateAfterObservationDeletion()
     */
    protected abstract void updateAfterObservationDeletion(WritableContentCache cache) throws OwsExceptionReport;

    /**
     * @param cache      the cache
     * @param sosRequest the request
     *
     * @see #updateAfterObservationInsertion(org.n52.sos.request.InsertObservationRequest)
     */
    protected abstract void updateAfterObservationInsertion(WritableContentCache cache,
                                                            InsertObservationRequest sosRequest);

    /**
     * @param cache              the cache
     * @param templateIdentifier the template identifier
     * @param sosObservation     the inserted observation
     *
     * @see #updateAfterResultInsertion(java.lang.String, org.n52.sos.ogc.om.SosObservation)
     */
    protected abstract void updateAfterResultInsertion(WritableContentCache cache,
                                                       String templateIdentifier,
                                                       SosObservation sosObservation);

    /**
     * @param cache       the cache
     * @param sosRequest  the request
     * @param sosResponse the response
     *
     * @see #updateAfterResultTemplateInsertion(org.n52.sos.request.InsertResultTemplateRequest,
     * org.n52.sos.response.InsertResultTemplateResponse)
     */
    protected abstract void updateAfterResultTemplateInsertion(WritableContentCache cache,
                                                               InsertResultTemplateRequest sosRequest,
                                                               InsertResultTemplateResponse sosResponse);

    /**
     * @param cache      the cache
     * @param sosRequest the request
     *
     * @see #updateAfterSensorDeletion(org.n52.sos.request.DeleteSensorRequest)
     */
    protected abstract void updateAfterSensorDeletion(WritableContentCache cache, DeleteSensorRequest sosRequest);

    /**
     * @param cache       the cache
     * @param sosRequest  the request
     * @param sosResponse the response
     *
     * @see #updateAfterSensorInsertion(org.n52.sos.request.InsertSensorRequest,
     * org.n52.sos.response.InsertSensorResponse)
     */
    protected abstract void updateAfterSensorInsertion(WritableContentCache cache, InsertSensorRequest sosRequest,
                                                       InsertSensorResponse sosResponse);

    /**
     * @param cache the cache
     *
     * @return if the cache was updated
     *
     * @throws OwsExceptionReport if an error occurs
     * @see #updateCacheFromDatasource()
     */
    protected abstract boolean updateCacheFromDatasource(WritableContentCache cache) throws OwsExceptionReport;
}
