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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.CacheFeederDAO;
import org.n52.sos.ds.hibernate.cache.InitialCacheUpdate;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface CacheFeederDAO
 *
 */
@Configurable
public class SosCacheFeederDAO extends HibernateSessionHolder implements CacheFeederDAO {

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
        if (cache == null) {
            throw new NullPointerException("cache is null");
        }
        CompositeOwsException errors = new CompositeOwsException();
        Session session = null;
        try {
            InitialCacheUpdate update = new InitialCacheUpdate(getCacheThreadCount());
            session = getSession();
            update.setCache(cache);
            update.setErrors(errors);
            update.setSession(session);
            update.execute();
        } catch (HibernateException he) {
            LOGGER.error("Error while updating ContentCache!", he);
        } finally {
            returnSession(session);
        }
        errors.throwIfNotEmpty();
    }
}
