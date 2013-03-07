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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Action;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class CacheUpdate implements Action {
    
    private Session session;
    private WritableContentCache cache;
    private List<OwsExceptionReport> errors;

    public List<OwsExceptionReport> getErrors() {
        return errors;
    }

    public void setErrors(List<OwsExceptionReport> errors) {
        this.errors = errors;
    }
    
    public void setSession(Session session) {
        this.session = session;
    }

    public void setCache(WritableContentCache cache) {
        this.cache = cache;
    }

    protected Session getSession() {
        return session;
    }

    protected WritableContentCache getCache() {
        return cache;
    }

    protected Set<String> getAllOfferingIdentifiersFrom(Set<ObservationConstellation> observationConstellations) {
        Set<String> offerings = new HashSet<String>(observationConstellations.size());
        for (ObservationConstellation oc : observationConstellations) {
            for (ObservationConstellationOfferingObservationType ocoot : oc
                    .getObservationConstellationOfferingObservationTypes()) {
                offerings.add(ocoot.getOffering().getIdentifier());
            }
        }
        return offerings;
    }
    
    protected IFeatureQueryHandler getFeatureQueryHandler() {
        return Configurator.getInstance().getFeatureQueryHandler();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
