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

import org.apache.commons.io.IOUtils;
import org.n52.sos.cache.WritableCache;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
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
 * Classes that saves the cache state after each update. Actual functionality is delegated to subclasses.
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class PersistingCacheController extends ScheduledContentCacheController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFeederDAOCacheController.class);
    private static final String CACHE_FILE = "cache.tmp";
    private WritableContentCache cache;

    public PersistingCacheController() {
        loadOrCreateCache();
    }

    protected void recreateCache() {
        this.cache = new WritableCache();
    }

    @Override
    public WritableContentCache getCache() {
        return this.cache;
    }

    protected File getCacheFile() {
        return new File(Configurator.getInstance().getBasePath() + File.separator + CACHE_FILE);
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

    protected void persistCache() {
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
                } catch (Throwable t) {
                    LOGGER.error(String.format("Error serializing cache to '%s'", f.getAbsolutePath()), t);
                } finally {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    @Override
    public final void updateAfterObservationDeletion() throws OwsExceptionReport {
        updateAfterObservationDeletion1();
        persistCache();
    }

    

    @Override
    public final void updateAfterObservationInsertion(InsertObservationRequest sosRequest) {
        updateAfterObservationInsertion1(sosRequest);
        persistCache();
    }


    @Override
    public final void updateAfterResultInsertion(String templateIdentifier, SosObservation sosObservation) {
        updateAfterResultInsertion1(templateIdentifier, sosObservation);
        persistCache();
    }



    @Override
    public final void updateAfterResultTemplateInsertion(InsertResultTemplateRequest sosRequest,
                                                         InsertResultTemplateResponse sosResponse) {
        updateAfterResultTemplateInsertion1(sosRequest, sosResponse);
        persistCache();
    }


    @Override
    public final void updateAfterSensorDeletion(DeleteSensorRequest sosRequest) {
        updateAfterSensorDeletion1(sosRequest);
        persistCache();
    }



    @Override
    public final void updateAfterSensorInsertion(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse) {
        updateAfterSensorInsertion1(sosRequest, sosResponse);
        persistCache();
    }


    @Override
    public final boolean updateCacheFromDatasource() throws OwsExceptionReport {
        boolean result = updateCacheFromDatasource1();
        persistCache();
        return result;
    }

    /**
     * @see #updateAfterObservationDeletion()
     */
    protected abstract void updateAfterObservationDeletion1() throws OwsExceptionReport;

    /**
     * @see #updateAfterObservationInsertion(org.n52.sos.request.InsertObservationRequest)
     */
    protected abstract void updateAfterObservationInsertion1(InsertObservationRequest sosRequest);

    /**
     * @see #updateAfterResultInsertion(java.lang.String, org.n52.sos.ogc.om.SosObservation)
     */
    protected abstract void updateAfterResultInsertion1(String templateIdentifier, SosObservation sosObservation);

    /**
     * @see #updateAfterResultTemplateInsertion(org.n52.sos.request.InsertResultTemplateRequest,
     * org.n52.sos.response.InsertResultTemplateResponse)
     */
    protected abstract void updateAfterResultTemplateInsertion1(InsertResultTemplateRequest sosRequest,
                                                                InsertResultTemplateResponse sosResponse);

    /**
     * @see #updateAfterSensorDeletion(org.n52.sos.request.DeleteSensorRequest)
     */
    protected abstract void updateAfterSensorDeletion1(DeleteSensorRequest sosRequest);

    /**
     * @see #updateAfterSensorInsertion(org.n52.sos.request.InsertSensorRequest,
     * org.n52.sos.response.InsertSensorResponse)
     */
    protected abstract void updateAfterSensorInsertion1(InsertSensorRequest sosRequest, InsertSensorResponse sosResponse);

    /**
     * @see #updateCacheFromDatasource()
     */
    protected abstract boolean updateCacheFromDatasource1() throws OwsExceptionReport;
}
