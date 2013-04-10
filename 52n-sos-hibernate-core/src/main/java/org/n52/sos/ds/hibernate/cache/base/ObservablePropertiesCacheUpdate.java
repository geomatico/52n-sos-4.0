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

import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.getObservablePropertyObjects;

import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.util.CollectionHelper;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ObservablePropertiesCacheUpdate extends CacheUpdate {
    protected Set<String> getProcedureIdentifiers(Set<ObservationConstellation> set) {
        Set<String> procedures = new HashSet<String>(set.size());
        for (ObservationConstellation oc : set) {
            procedures.add(oc.getProcedure().getIdentifier());
        }
        return procedures;
    }

    @Override
    public void execute() {
        for (ObservableProperty op : getObservablePropertyObjects(getSession())) {
            final String identifier = op.getIdentifier();
            final Set<ObservationConstellation> ocs = getObservationConstellations(op);
            getCache().setOfferingsForObservableProperty(identifier, getAllOfferingIdentifiersFrom(ocs));
            getCache().setProceduresForObservableProperty(identifier, getProcedureIdentifiers(ocs));
        }
    }
   
    protected Set<ObservationConstellation> getObservationConstellations(ObservableProperty observableProperty) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_OBSERVABLE_PROPERTY, observableProperty));
        return CollectionHelper.asSet(HibernateCriteriaQueryUtilities.getObservationConstellations(queryObject, getSession()));
    }
}
