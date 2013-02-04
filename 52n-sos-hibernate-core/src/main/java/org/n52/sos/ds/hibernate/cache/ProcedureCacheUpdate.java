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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
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
        String obsConstAlias = HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(observationAliases, null);

        observationConstellationQueryObject.addCriterion(getCriterionForProcedures(observationConstellationAliases, null, procedureIdentifier));
        observationQueryObject.addCriterion(getCriterionForProcedures(observationAliases, obsConstAlias, procedureIdentifier));

        observationConstellationQueryObject.setAliases(observationConstellationAliases);
        List<ObservationConstellation> observationConstellations = HibernateCriteriaQueryUtilities.getObservationConstellations(observationConstellationQueryObject, session);

        observationQueryObject.setAliases(observationAliases);

        Set<Observation> allObservations = new HashSet<Observation>(0);
        for (ObservationConstellation observationConstellation : observationConstellations) {
            HibernateQueryObject defaultQueryObject = observationQueryObject.clone();

            String id = HibernateCriteriaQueryUtilities.getParameterWithPrefix(HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION, null);
            defaultQueryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(id, observationConstellation));

            allObservations.addAll(HibernateCriteriaQueryUtilities.getObservations(defaultQueryObject, session));
        }

        List<String> observationIdentifier = CollectionHelper.synchronizedLinkedList();
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

    private Criterion getCriterionForProcedures(Map<String, String> aliasMap, String prefix, String procedure) {
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliasMap, prefix);
        return Restrictions.in(HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias), Collections.singletonList(procedure));
    }

    @Override
    public void run() {
        List<Procedure> hProcedures = HibernateCriteriaQueryUtilities.getProcedureObjects(getSession());
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
