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

import static java.util.Collections.singletonList;
import static org.hibernate.criterion.Restrictions.in;
import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.util.CollectionHelper;


/**
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ProcedureCacheUpdate extends CacheUpdate {
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
        Map<String, String> aliases = new HashMap<String, String>(2);
        HibernateQueryObject queryObject = new HibernateQueryObject();
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, null);
        queryObject.setAliases(aliases);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), procedureIdentifier));
        queryObject.addProjection(Projections.distinct(Projections.property(HibernateConstants.PARAMETER_IDENTIFIER)));
        List<String> objectList = (List<String>)HibernateCriteriaQueryUtilities.getObjectList(queryObject, session, Observation.class);
        return CollectionHelper.asSet(objectList);
    }

    protected Criterion getCriterionForProcedures(Map<String, String> aliasMap, String prefix, String procedure) {
        String procAlias = addProcedureAliasToMap(aliasMap, prefix);
        return in(getIdentifierParameter(procAlias), singletonList(procedure));
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
                getCache().addParentProcedures(id, getProcedureIdentifiers(p.getProceduresForChildSensorId()));
                getCache().setObservationIdentifiersForProcedure(id, getObservationIdentifiers(getSession(), id));
            }
        }
    }
    
    protected Set<ObservationConstellation> getObservationConstellations(Procedure procedure) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_PROCEDURE, procedure));
        return CollectionHelper.asSet(HibernateCriteriaQueryUtilities.getObservationConstellations(queryObject, getSession()));
    }
}
