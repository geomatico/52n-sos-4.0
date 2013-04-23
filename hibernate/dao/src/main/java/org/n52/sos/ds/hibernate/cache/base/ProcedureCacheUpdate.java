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

import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.cache.AbstractDatasourceCacheUpdate;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.util.CollectionHelper;


/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ProcedureCacheUpdate extends AbstractDatasourceCacheUpdate {
    protected Set<String> getObservableProperties(Set<ObservationConstellation> set) {
        Set<String> observableProperties = new HashSet<String>(set.size());
        for (ObservationConstellation observationConstellation : set) {
            observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
        }
        return observableProperties;
    }

    protected Set<String> getProcedureIdentifiers(Set<Procedure> procedures) {
        Set<String> identifiers = new HashSet<String>(procedures.size());
        for (Procedure procedure : procedures) {
            identifiers.add(procedure.getIdentifier());
        }
        return identifiers;
    }

    @SuppressWarnings("unchecked")
    protected Set<String> getObservationIdentifiers(Session session, String procedureIdentifier) {
        return CollectionHelper.asSet(session.createCriteria(Observation.class)
                .setProjection(Projections.distinct(Projections.property(Observation.IDENTIFIER)))
                .add(Restrictions.eq(Observation.DELETED, false))
                .createCriteria(Observation.PROCEDURE)
                .add(Restrictions.eq(Procedure.IDENTIFIER, procedureIdentifier))
                .list());
    }

    @Override
    public void execute() {
        List<Procedure> hProcedures = getProcedureObjects(getSession());
        for (Procedure p : hProcedures) {
            if (!p.isDeleted()) {
                final String id = p.getIdentifier();
                final Set<ObservationConstellation> ocs = getObservationConstellations(p);
                getCache().addProcedure(id);
                getCache().setOfferingsForProcedure(id, getAllOfferingIdentifiersFrom(ocs));
                getCache().setObservablePropertiesForProcedure(id, getObservableProperties(ocs));
                getCache().addParentProcedures(id, getProcedureIdentifiers(p.getProceduresForParentSensorId()));
                getCache().setObservationIdentifiersForProcedure(id, getObservationIdentifiers(getSession(), id));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    protected Set<ObservationConstellation> getObservationConstellations(Procedure procedure) {
        return CollectionHelper.asSet(getSession().createCriteria(ObservationConstellation.class)
                .add(Restrictions.eq(ObservationConstellation.PROCEDURE, procedure)).list());
    }
}
