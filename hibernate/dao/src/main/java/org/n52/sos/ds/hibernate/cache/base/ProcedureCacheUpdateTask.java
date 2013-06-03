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


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.cache.WritableContentCache;
import org.n52.sos.ds.hibernate.ThreadLocalSessionFactory;
import org.n52.sos.ds.hibernate.cache.DatasourceCacheUpdateHelper;
import org.n52.sos.ds.hibernate.dao.ObservablePropertyDAO;
import org.n52.sos.ds.hibernate.dao.OfferingDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.TProcedure;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.RunnableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ProcedureCacheUpdateTask extends RunnableAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcedureCacheUpdateTask.class);
    private CountDownLatch countDownLatch;
    private ThreadLocalSessionFactory sessionFactory;
    private List<OwsExceptionReport> errors;
    private Procedure procedure;
    private WritableContentCache cache;

    ProcedureCacheUpdateTask(
            CountDownLatch countDownLatch, 
            ThreadLocalSessionFactory sessionFactory,
            WritableContentCache offeringCache,
            Procedure procedure,
            List<OwsExceptionReport> error) {
        this.countDownLatch = countDownLatch;
        this.sessionFactory = sessionFactory;
        this.cache = offeringCache;
        this.procedure = procedure;
        this.errors = error;
    }

    protected void getProcedureInformationFromDbAndAddItToCacheMaps(Session session) throws OwsExceptionReport {
        final String id = procedure.getIdentifier();
        final Set<ObservationConstellation> ocs = getObservationConstellations(session, procedure);
        cache.addProcedure(id);
        cache.setOfferingsForProcedure(id, getOfferingIdentifiers(ocs, session));
        cache.setObservablePropertiesForProcedure(id, getObservableProperties(ocs, session));
        if (procedure instanceof TProcedure) {
            cache.addParentProcedures(id, getProcedureIdentifiers(((TProcedure)procedure).getParents()));
        }
        cache.setObservationIdentifiersForProcedure(id, getObservationIdentifiers(session, id));        

        // Temporal Envelope
        ProcedureDAO procedureDAO = new ProcedureDAO();
        cache.setMinPhenomenonTimeForProcedure(id, procedureDAO
                .getMinDate4Procedure(id, session));
        cache.setMaxPhenomenonTimeForProcedure(id, procedureDAO
                .getMaxDate4Procedure(id, session));
    }

    @Override
    public void execute() {
        try {
            getProcedureInformationFromDbAndAddItToCacheMaps(sessionFactory.getSession());
        } catch (OwsExceptionReport owse) {
            errors.add(owse);
        } catch (Exception e) {
            errors.add(new GenericThrowableWrapperException(e)
                    .withMessage("Error while processing procedure cache update task!"));
        } finally {
            LOGGER.debug("ProcedureCacheUpdateTask finished, latch.countDown().");
            countDownLatch.countDown();
        }
    }

    @SuppressWarnings("unchecked")
    protected Set<ObservationConstellation> getObservationConstellations(Session session, Procedure procedure) {
        return CollectionHelper.asSet(session.createCriteria(ObservationConstellation.class)
                .add(Restrictions.eq(ObservationConstellation.PROCEDURE, procedure)).list());
    }
    
    protected Set<String> getObservableProperties(Set<ObservationConstellation> set, Session session) {
        if (CollectionHelper.isNotEmpty(set)) {
            return DatasourceCacheUpdateHelper.getAllObservablePropertyIdentifiersFrom(set);
        } else {
            return CollectionHelper.asSet(new ObservablePropertyDAO().getObservablePropertyIdentifiersForProcedure(procedure.getIdentifier(), session));
        }
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
    
    protected Set<String> getOfferingIdentifiers(Set<ObservationConstellation> observationConstellations, Session session) {
        if (CollectionHelper.isNotEmpty(observationConstellations)) {
            Set<String> offerings = new HashSet<String>(observationConstellations.size());
            for (ObservationConstellation oc : observationConstellations) {
                offerings.add(oc.getOffering().getIdentifier());
            }
            return offerings;
        } else {
            return CollectionHelper.asSet(new OfferingDAO().getOfferingIdentifiersForProcedure(procedure.getIdentifier(), session));
        }
    }
    
}
