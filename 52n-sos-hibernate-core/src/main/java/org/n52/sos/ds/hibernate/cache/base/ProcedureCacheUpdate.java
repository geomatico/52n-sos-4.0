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
import static org.n52.sos.ds.hibernate.util.HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION;
import static org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.cache.CacheUpdate;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;

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

    protected Set<String> getObservationIdentifiers(Session session, String procedureIdentifier) {
        Map<String, String> observationConstellationAliases = new HashMap<String, String>();
        HibernateQueryObject ocQuery = new HibernateQueryObject();

        Map<String, String> observationAliases = new HashMap<String, String>();
        HibernateQueryObject oQuery = new HibernateQueryObject();
        String obsConstAlias = addObservationConstallationAliasToMap(observationAliases, null);

        ocQuery.addCriterion(getCriterionForProcedures(observationConstellationAliases, null, procedureIdentifier));
        oQuery.addCriterion(getCriterionForProcedures(observationAliases, obsConstAlias, procedureIdentifier));

        ocQuery.setAliases(observationConstellationAliases);
        List<ObservationConstellation> observationConstellations =
                                       getObservationConstellations(ocQuery, session);

        oQuery.setAliases(observationAliases);

        Set<Observation> allObservations = new HashSet<Observation>(0);
        for (ObservationConstellation observationConstellation : observationConstellations) {
            HibernateQueryObject defaultQueryObject = oQuery.clone();

            String id = getParameterWithPrefix(PARAMETER_OBSERVATION_CONSTELLATION, null);
            defaultQueryObject.addCriterion(getEqualRestriction(id, observationConstellation));

            allObservations.addAll(getObservations(defaultQueryObject, session));
        }

        Set<String> observationIdentifier = new HashSet<String>();
        for (Observation observation : allObservations) {
            if (observation.getIdentifier() != null
                && !observation.getIdentifier().isEmpty()
                && !observationIdentifier.contains(observation.getIdentifier())) {
                observationIdentifier.add(observation.getIdentifier());
            }
            if (observation.getSetId() != null
                && !observation.getSetId().isEmpty()
                && !observationIdentifier.contains(observation.getSetId())) {
                observationIdentifier.add(observation.getSetId());
            }
        }
        return observationIdentifier;
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
                final Set<ObservationConstellation> ocs = p.getObservationConstellations();
                getCache().addProcedure(id);
                getCache().setOfferingsForProcedure(id, getAllOfferingIdentifiersFrom(ocs));
                getCache().setObservablePropertiesForProcedure(id, getObservableProperties(ocs));
                getCache().addParentProcedures(id, getProcedureIdentifiers(p.getProceduresForChildSensorId()));
                getCache().setObservationIdentifiersForProcedure(id, getObservationIdentifiers(getSession(), id));
            }
        }
    }
}
