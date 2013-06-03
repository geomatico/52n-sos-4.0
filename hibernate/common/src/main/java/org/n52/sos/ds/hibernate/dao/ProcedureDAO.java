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
package org.n52.sos.ds.hibernate.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.TProcedure;
import org.n52.sos.util.CollectionHelper;

/**
 * Hibernate data access class for procedure
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class ProcedureDAO {

    /**
     * Get all procedure objects
     * 
     * @param session
     *            Hibernate session
     * @return Procedure objects
     */
    @SuppressWarnings("unchecked")
    public List<Procedure> getProcedureObjects(Session session) {
        return session.createCriteria(Procedure.class).list();
    }

    /**
     * Get Procedure object for procedure identifier
     * 
     * @param identifier
     *            Procedure identifier
     * @param session
     *            Hibernate session
     * @return Procedure object
     */
    public Procedure getProcedureForIdentifier(String identifier, Session session) {
        return (Procedure) session.createCriteria(Procedure.class)
                .add(Restrictions.eq(Procedure.IDENTIFIER, identifier)).uniqueResult();
    }

    /**
     * Get Procedure objects for procedure identifiers
     * 
     * @param identifiers
     *            Procedure identifiers
     * @param session
     *            Hibernate session
     * @return Procedure objects
     */
    @SuppressWarnings("unchecked")
    public List<Procedure> getProceduresForIdentifiers(Collection<String> identifiers, Session session) {
        if (identifiers == null || identifiers.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        return session.createCriteria(Procedure.class).add(Restrictions.in(Procedure.IDENTIFIER, identifiers)).list();
    }

    /**
     * Get procedure identifiers for FOI
     * 
     * @param session
     *            Hibernate session
     * @param feature
     *            FOI object
     * 
     * @return Related procedure identifiers
     */
    @SuppressWarnings("unchecked")
    public List<String> getProceduresForFeatureOfInterest(Session session, FeatureOfInterest feature) {
        return session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                .createCriteria(Observation.FEATURE_OF_INTEREST)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, feature.getIdentifier()))
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER))).list();
    }

    /**
     * Get procedure identifiers for offering identifier
     * 
     * @param offeringIdentifier
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return Procedure identifiers
     */
    @SuppressWarnings("unchecked")
    public List<String> getProcedureIdentifiersForOffering(String offeringIdentifier, Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.PROCEDURE).setProjection(
                Projections.distinct(Projections.property(Procedure.IDENTIFIER)));
        c.createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offeringIdentifier));
        return c.list();
    }

    /**
     * Get procedure identifiers for observable property identifier
     * 
     * @param observablePropertyIdentifier
     *            Observable property identifier
     * @param session
     *            Hibernate session
     * @return Procedure identifiers
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getProcedureIdentifiersForObservableProperty(String observablePropertyIdentifier,
            Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.PROCEDURE).setProjection(
                Projections.distinct(Projections.property(Procedure.IDENTIFIER)));
        c.createCriteria(Observation.OBSERVABLE_PROPERTY).add(
                Restrictions.eq(ObservableProperty.IDENTIFIER, observablePropertyIdentifier));
        return c.list();
    }

    /**
     * Get transactional procedure object for procedure identifier
     * 
     * @param identifier
     *            Procedure identifier
     * @param session
     *            Hibernate session
     * @return Transactional procedure object
     */
    public TProcedure getTProcedureForIdentifier(String identifier, Session session) {
        return (TProcedure) session.createCriteria(TProcedure.class)
                .add(Restrictions.eq(Procedure.IDENTIFIER, identifier)).uniqueResult();
    }

    /**
     * Get min time from observations for procedure
     * 
     * @param procedure
     *            Procedure identifier
     * @param session
     *            Hibernate session
     * @return min time for procedure
     */
    public DateTime getMinDate4Procedure(String procedure, Session session) {
        Criteria criteria =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.min(Observation.PHENOMENON_TIME_START))
                        .add(Restrictions.eq(Observation.DELETED, false)).createCriteria(Observation.PROCEDURE)
                        .add(Restrictions.eq(Procedure.IDENTIFIER, procedure));
        Object min = criteria.uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get max time from observations for procedure
     * 
     * @param procedure
     *            Procedure identifier
     * @param session
     *            Hibernate session
     * @return max time for procedure
     */
    public DateTime getMaxDate4Procedure(String procedure, Session session) {
        Criteria cstart =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.PHENOMENON_TIME_START))
                        .createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedure));

        Object maxStart = cstart.uniqueResult();

        Criteria cend =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.PHENOMENON_TIME_END))
                        .createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedure));

        Object maxEnd = cend.uniqueResult();

        if (maxStart == null && maxEnd == null) {
            return null;
        } else {
            DateTime start = new DateTime(maxStart);
            if (maxEnd != null) {
                DateTime end = new DateTime(maxEnd);
                if (end.isAfter(start)) {
                    return end;
                }
            }
            return start;
        }
    }

    /**
     * Insert and get procedure object
     * 
     * @param identifier
     *            Procedrue identifier
     * @param procedureDecriptionFormat
     *            Procedure description format object
     * @param parentProcedures
     *            Parent procedure identifiers
     * @param session
     *            Hibernate session
     * @return Procedure object
     */
    public Procedure getOrInsertProcedure(String identifier, ProcedureDescriptionFormat procedureDecriptionFormat,
            Collection<String> parentProcedures, Session session) {
        ProcedureDAO procedureDAO = new ProcedureDAO();
        Procedure result = procedureDAO.getProcedureForIdentifier(identifier, session);
        if (result == null) {
            TProcedure newResult = new TProcedure();
            newResult.setProcedureDescriptionFormat(procedureDecriptionFormat);
            newResult.setIdentifier(identifier);
            if (CollectionHelper.isNotEmpty(parentProcedures)) {
                newResult.setParents(CollectionHelper.asSet(procedureDAO.getProceduresForIdentifiers(parentProcedures,
                        session)));
            }
            result = newResult;
        }
        result.setDeleted(false);
        session.saveOrUpdate(result);
        session.flush();
        session.refresh(result);
        return result;
    }

}
