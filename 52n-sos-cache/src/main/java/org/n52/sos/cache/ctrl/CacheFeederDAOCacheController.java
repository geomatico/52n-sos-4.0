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

import org.apache.commons.io.IOUtils;
import org.n52.sos.cache.WritableCache;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ds.CacheFeederDAO;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CacheControllerImpl implements all methods to request all objects and relationships from a standard datasource.
 */
public abstract class CacheFeederDAOCacheController extends ScheduledContentCacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFeederDAOCacheController.class);
    private static final String CACHE_FILE = "cache.tmp";
    private WritableContentCache cache;
    private CacheFeederDAO cacheFeederDAO;

    public CacheFeederDAOCacheController() {
        this.cacheFeederDAO = getCacheDAO();
        loadOrCreateCache();
    }

    protected CacheFeederDAO getCacheDAO() {
        return Configurator.getInstance().getCacheFeederDAO();
    }

    @Override
    public boolean updateCacheFromDatasource() throws OwsExceptionReport {
        LOGGER.info("Capabilities Cache Update started");
        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER.warn("\n******\nCapabilities caches not updated "
                            + "because of time out while waiting for update lock." + "\nWaited "
                            + SosConstants.UPDATE_TIMEOUT + " milliseconds.\n******\n");
                return false;
            }

            while (!isUpdateIsFree()) {
                getUpdateFree().await();
            }
            setUpdateIsFree(false);
            cache = new WritableCache();
            cacheFeederDAO.updateCache(cache);
        } catch (InterruptedException ie) {
            LOGGER.error("Problem while threadsafe capabilities cache update", ie);
            return false;
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
        return true;
    }

    @Override
    public void updateAfterObservationDeletion() throws OwsExceptionReport {
        boolean timeNotElapsed = true;
        try {
            // thread safe updating of the cache map
            timeNotElapsed = getUpdateLock().tryLock(SosConstants.UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

            // has waiting for lock got a time out?
            if (!timeNotElapsed) {
                LOGGER
                        .warn("\n******\nupdate after Observation Deletion not successful because of time out while waiting for update lock."
                              + "\nWaited {} milliseconds.\n******\n", SosConstants.UPDATE_TIMEOUT);
                return;
            }
            while (!isUpdateIsFree()) {
                getUpdateFree().await();
            }
            setUpdateIsFree(false);
            cache = new WritableCache();
            cacheFeederDAO.updateAfterObservationDeletion(cache);
        } catch (InterruptedException e) {
            LOGGER.error("Problem while threadsafe capabilities cache update", e);
        } finally {
            if (timeNotElapsed) {
                getUpdateLock().unlock();
                setUpdateIsFree(true);
            }
        }
    }

    @Override
    public WritableContentCache getCache() {
        return cache;
    }

    protected String getBasePath() {
        return Configurator.getInstance().getBasePath();
    }

    protected File getCacheFile() {
        return new File(getBasePath() + File.separator + CACHE_FILE);
    }

    private void loadOrCreateCache() {
        File f = getCacheFile();
        if (f.exists() && f.canRead()) {
            LOGGER.debug("Reading cache from temp file '{}'", f.getAbsolutePath());
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new FileInputStream(f));
                this.cache = (WritableCache) in.readObject();
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
            this.cache = new WritableCache();
        } else {
            setInitialized(true);
        }
    }

    private void writeCache() {
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

    @Override
    public void cleanup() {
        super.cleanup();
        writeCache();
    }
}
