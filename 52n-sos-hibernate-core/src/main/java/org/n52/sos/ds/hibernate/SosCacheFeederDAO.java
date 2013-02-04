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

import java.util.LinkedList;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.cache.CapabilitiesCache;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.cache.CompositePhenomenonCacheUpdate;
import org.n52.sos.ds.hibernate.cache.EventTimeCacheUpdate;
import org.n52.sos.ds.hibernate.cache.FeatureOfInterestCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ObservablePropertiesCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ObservationIdentifiersCacheUpdate;
import org.n52.sos.ds.hibernate.cache.OfferingCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ProcedureCacheUpdate;
import org.n52.sos.ds.hibernate.cache.RelatedFeaturesCacheUpdate;
import org.n52.sos.ds.hibernate.cache.ResultTemplateCacheUpdate;
import org.n52.sos.ds.hibernate.cache.StaticCapabilitiesCacheUpdate;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface ICacheFeederDAO
 *
 */
public class SosCacheFeederDAO extends AbstractHibernateDao implements ICacheFeederDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosCacheFeederDAO.class);

    @Override
    public void updateCache(CapabilitiesCache cache) throws OwsExceptionReport {
        List<CacheUpdate> actions = CollectionHelper.list(
                new OfferingCacheUpdate(),
                new ProcedureCacheUpdate(),
                new ObservablePropertiesCacheUpdate(),
                new FeatureOfInterestCacheUpdate(),
                new RelatedFeaturesCacheUpdate(),
                new CompositePhenomenonCacheUpdate(),
                new StaticCapabilitiesCacheUpdate(),
                new ObservationIdentifiersCacheUpdate(),
                new ResultTemplateCacheUpdate(),
                new EventTimeCacheUpdate());
        update(cache, actions);
    }

    @Override
    public void updateAfterSensorInsertion(CapabilitiesCache cache) throws OwsExceptionReport {
        List<CacheUpdate> actions = CollectionHelper.list(
                new OfferingCacheUpdate(),
                new ProcedureCacheUpdate(),
                new ObservablePropertiesCacheUpdate(),
                new FeatureOfInterestCacheUpdate(),
                new RelatedFeaturesCacheUpdate(),
                new CompositePhenomenonCacheUpdate());
        update(cache, actions);
    }

    @Override
    public void updateAfterSensorDeletion(CapabilitiesCache cache) throws OwsExceptionReport {
        List<CacheUpdate> actions = CollectionHelper.list(
                new OfferingCacheUpdate(),
                new ProcedureCacheUpdate());
        update(cache, actions);
    }

    @Override
    public void updateAfterObservationInsertion(CapabilitiesCache cache) throws OwsExceptionReport {
        List<CacheUpdate> actions = CollectionHelper.list(
                new FeatureOfInterestCacheUpdate(),
                new OfferingCacheUpdate(),
                new EventTimeCacheUpdate());
        update(cache, actions);
    }

    @Override
    public void updateAfterObservationDeletion(CapabilitiesCache cache) throws OwsExceptionReport {
        List<CacheUpdate> actions = CollectionHelper.list(
                new FeatureOfInterestCacheUpdate(),
                new OfferingCacheUpdate(),
                new EventTimeCacheUpdate());
        update(cache, actions);
    }

    @Override
    public void updateAfterResultTemplateInsertion(CapabilitiesCache cache) throws OwsExceptionReport {
        List<CacheUpdate> actions = CollectionHelper.list(
                new OfferingCacheUpdate(),
                new ProcedureCacheUpdate(),
                new ObservablePropertiesCacheUpdate(),
                new FeatureOfInterestCacheUpdate(),
                new RelatedFeaturesCacheUpdate(),
                new CompositePhenomenonCacheUpdate(),
                new ResultTemplateCacheUpdate());
        update(cache, actions);
    }

    protected void update(CapabilitiesCache cache, List<CacheUpdate> actions) throws OwsExceptionReport {
        if (cache == null) {
            String errorMsg = "CapabilitiesCache object is null";
            IllegalArgumentException e = new IllegalArgumentException(errorMsg);
            LOGGER.debug("Exception thrown:", e);
            LOGGER.error(errorMsg);
            throw Util4Exceptions.createNoApplicableCodeException(e, errorMsg);
        }
        LinkedList<OwsExceptionReport> errors = new LinkedList<OwsExceptionReport>();
        Session session = null;
        try {
            session = getSession();
            for (CacheUpdate action : actions) {
                action.setCache(cache);
                action.setSession(session);
                action.setErrors(errors);
                action.update();
            }
        } catch (HibernateException he) {
            String exceptionText = "Error while updating CapabilitiesCache!";
            LOGGER.error(exceptionText, he);
        } finally {
            returnSession(session);
        }
        Util4Exceptions.mergeAndThrowExceptions(errors);
    }
}
