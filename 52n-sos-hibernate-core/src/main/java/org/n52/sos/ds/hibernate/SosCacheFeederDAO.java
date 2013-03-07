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
package org.n52.sos.ds.hibernate;

import static org.n52.sos.ds.hibernate.CacheFeederSettingDefinitionProvider.CACHE_THREAD_COUNT;
import static org.n52.sos.util.Util4Exceptions.createNoApplicableCodeException;
import static org.n52.sos.util.Util4Exceptions.mergeAndThrowExceptions;

import java.util.LinkedList;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.cache.InitialCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ObservationDeletionCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ObservationInsertionCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ResultTemplateInsertionCacheUpdate;
import org.n52.sos.ds.hibernate.cache.SensorDeletionCacheUpdate;
import org.n52.sos.ds.hibernate.cache.SensorInsertionCacheUpdate;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface ICacheFeederDAO
 *
 */
@Configurable
public class SosCacheFeederDAO extends HibernateSessionHolder implements ICacheFeederDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosCacheFeederDAO.class);
    
    /**
     * Defines the number of threads available in the thread pool of the cache
     * update executor service.
     */
    private int cacheThreadCount = 5;

    public int getCacheThreadCount() {
        return cacheThreadCount;
    }

    @Setting(CACHE_THREAD_COUNT)
    public void setCacheThreadCount(int threads) throws ConfigurationException {
        Validation.greaterZero("Cache Thread Count", threads);
        this.cacheThreadCount = threads;
    }

    @Override
    public void updateCache(WritableContentCache cache) throws OwsExceptionReport {
        update(cache, new InitialCacheUpdate(getCacheThreadCount()));
    }

    @Override
    @Deprecated
    public void updateAfterSensorInsertion(WritableContentCache cache) throws OwsExceptionReport {
        update(cache, new SensorInsertionCacheUpdate(getCacheThreadCount()));
    }

    @Override
    @Deprecated
    public void updateAfterSensorDeletion(WritableContentCache cache) throws OwsExceptionReport {
        update(cache, new SensorDeletionCacheUpdate(getCacheThreadCount()));
    }

    @Override
    @Deprecated
    public void updateAfterObservationInsertion(WritableContentCache cache) throws OwsExceptionReport {
        update(cache, new ObservationInsertionCacheUpdate(getCacheThreadCount()));
    }

    @Override
    public void updateAfterObservationDeletion(WritableContentCache cache) throws OwsExceptionReport {
        update(cache, new ObservationDeletionCacheUpdate(getCacheThreadCount()));
    }

    @Override
    @Deprecated
    public void updateAfterResultTemplateInsertion(WritableContentCache cache) throws OwsExceptionReport {
        update(cache, new ResultTemplateInsertionCacheUpdate());
    }

    protected void update(WritableContentCache cache, CacheUpdate action) throws OwsExceptionReport {
        if (cache == null) {
            String errorMsg = "CapabilitiesCache object is null";
            IllegalArgumentException e = new IllegalArgumentException(errorMsg);
            LOGGER.debug("Exception thrown:", e);
            LOGGER.error(errorMsg);
            throw createNoApplicableCodeException(e, errorMsg);
        }
        LinkedList<OwsExceptionReport> errors = new LinkedList<OwsExceptionReport>();
        Session session = null;
        try {
            session = getSession();
            action.setCache(cache);
            action.setSession(session);
            action.setErrors(errors);
            action.execute();
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
        mergeAndThrowExceptions(errors);
    }
}
