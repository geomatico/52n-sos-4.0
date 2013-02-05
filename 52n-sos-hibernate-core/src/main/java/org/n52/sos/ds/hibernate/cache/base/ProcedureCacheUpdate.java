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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

    protected Collection<String> getObservablePropertyIdentifierFromObservationConstellation(Set<ObservationConstellation> set) {
        Set<String> observableProperties = new HashSet<String>(set.size());
        for (ObservationConstellation observationConstellation : set) {
            observableProperties.add(observationConstellation.getObservableProperty().getIdentifier());
        }
        return new ArrayList<String>(observableProperties);
    }

    protected Collection<String> getProcedureIDsFromProcedures(Set<Procedure> proceduresForChildSensorId) {
        List<String> procedureIDs = new ArrayList<String>(proceduresForChildSensorId.size());
        for (Procedure procedure : proceduresForChildSensorId) {
            procedureIDs.add(procedure.getIdentifier());
        }
        return procedureIDs;
    }

    protected Collection<String> getObservationIdentifiersForProcedure(Session session, String procedureIdentifier) {
        Map<String, String> observationConstellationAliases = new HashMap<String, String>();
        HibernateQueryObject observationConstellationQueryObject = new HibernateQueryObject();

        Map<String, String> observationAliases = new HashMap<String, String>();
        HibernateQueryObject observationQueryObject = new HibernateQueryObject();
        String obsConstAlias = addObservationConstallationAliasToMap(observationAliases, null);

        observationConstellationQueryObject.addCriterion(getCriterionForProcedures(observationConstellationAliases, null, procedureIdentifier));
        observationQueryObject.addCriterion(getCriterionForProcedures(observationAliases, obsConstAlias, procedureIdentifier));

        observationConstellationQueryObject.setAliases(observationConstellationAliases);
        List<ObservationConstellation> observationConstellations = getObservationConstellations(observationConstellationQueryObject, session);

        observationQueryObject.setAliases(observationAliases);

        Set<Observation> allObservations = new HashSet<Observation>(0);
        for (ObservationConstellation observationConstellation : observationConstellations) {
            HibernateQueryObject defaultQueryObject = observationQueryObject.clone();

            String id = getParameterWithPrefix(PARAMETER_OBSERVATION_CONSTELLATION, null);
            defaultQueryObject.addCriterion(getEqualRestriction(id, observationConstellation));

            allObservations.addAll(getObservations(defaultQueryObject, session));
        }

        List<String> observationIdentifier = new LinkedList<String>();
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
        Set<String> procedures = new HashSet<String>(hProcedures.size());
        Map<String, Collection<String>> kProcedureVOffering = new HashMap<String, Collection<String>>(hProcedures.size());
        Map<String, Collection<String>> kProcedureVObservableProperties = new HashMap<String, Collection<String>>(hProcedures.size());
        Map<String, Collection<String>> kProcedureVObservationIdentifiers = new HashMap<String, Collection<String>>(hProcedures.size());
        Map<String, Collection<String>> parentProcs = new HashMap<String, Collection<String>>(hProcedures.size());
        for (Procedure hProcedure : hProcedures) {
            if (!hProcedure.isDeleted()) {
                procedures.add(hProcedure.getIdentifier());
                kProcedureVOffering.put(hProcedure.getIdentifier(), getAllOfferingIdentifiersFrom(hProcedure.getObservationConstellations()));
                kProcedureVObservableProperties.put(hProcedure.getIdentifier(), getObservablePropertyIdentifierFromObservationConstellation(hProcedure.getObservationConstellations()));
                parentProcs.put(hProcedure.getIdentifier(), getProcedureIDsFromProcedures(hProcedure.getProceduresForChildSensorId()));
                kProcedureVObservationIdentifiers.put(hProcedure.getIdentifier(), getObservationIdentifiersForProcedure(getSession(), hProcedure.getIdentifier()));
            }
        }
        getCache().setProcedures(procedures);
        getCache().setKProcedureVOfferings(kProcedureVOffering);
        getCache().setProcPhens(kProcedureVObservableProperties);
        getCache().setProcedureHierarchies(parentProcs);
    }
}
