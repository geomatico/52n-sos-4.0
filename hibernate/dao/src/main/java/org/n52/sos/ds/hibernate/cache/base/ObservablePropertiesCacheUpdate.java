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

import java.util.Set;

import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.cache.AbstractDatasourceCacheUpdate;
import org.n52.sos.ds.hibernate.cache.DatasourceCacheUpdateHelper;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.util.CollectionHelper;

/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ObservablePropertiesCacheUpdate extends AbstractDatasourceCacheUpdate {
    @Override
    public void execute() {
        for (ObservableProperty op : getObservablePropertyObjects(getSession())) {
            final String identifier = op.getIdentifier();
            final Set<ObservationConstellation> ocs = getObservationConstellations(op);
            if (CollectionHelper.isNotEmpty(ocs)) {
                getCache().setOfferingsForObservableProperty(identifier, DatasourceCacheUpdateHelper.getAllOfferingIdentifiersFrom(ocs));
                getCache().setProceduresForObservableProperty(identifier, DatasourceCacheUpdateHelper.getAllProcedureIdentifiersFrom(ocs));
            } else {
                getCache().setOfferingsForObservableProperty(identifier, HibernateCriteriaQueryUtilities.getOfferingIdentifiersForObservableProperty(op.getIdentifier(), getSession()));
                getCache().setProceduresForObservableProperty(identifier, HibernateCriteriaQueryUtilities.getProcedureIdentifiersForObservableProperty(op.getIdentifier(), getSession()));
            }
        }
    }
   
    @SuppressWarnings("unchecked")
    protected Set<ObservationConstellation> getObservationConstellations(ObservableProperty observableProperty) {
        return CollectionHelper.asSet(getSession().createCriteria(ObservationConstellation.class)
                .add(Restrictions.eq(ObservationConstellation.OBSERVABLE_PROPERTY, observableProperty)).list());
    }
}
