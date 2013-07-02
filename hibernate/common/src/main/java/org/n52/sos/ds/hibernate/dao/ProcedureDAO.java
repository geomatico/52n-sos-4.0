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
import org.joda.time.DateTimeZone;
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
    public List<Procedure> getProcedureObjects(final Session session) {
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
    public Procedure getProcedureForIdentifier(final String identifier, final Session session) {
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
    public List<Procedure> getProceduresForIdentifiers(final Collection<String> identifiers, final Session session) {
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
    public List<String> getProceduresForFeatureOfInterest(final Session session, final FeatureOfInterest feature) {
    	final Criteria c = session.createCriteria(Observation.class);
    	c.add(Restrictions.eq(Observation.DELETED, false));
    	c.createCriteria(Observation.FEATURE_OF_INTEREST).add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, feature.getIdentifier()));
    	c.createCriteria(Observation.PROCEDURE).setProjection(Projections.distinct(Projections.property(Procedure.IDENTIFIER)));
        final List<String> list = c.list();
		return list;
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
    public List<String> getProcedureIdentifiersForOffering(final String offeringIdentifier, final Session session) {
        final Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
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
    public Collection<String> getProcedureIdentifiersForObservableProperty(final String observablePropertyIdentifier,
            final Session session) {
        final Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
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
    public TProcedure getTProcedureForIdentifier(final String identifier, final Session session) {
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
    public DateTime getMinDate4Procedure(final String procedure, final Session session) {
        final Criteria criteria =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.min(Observation.PHENOMENON_TIME_START))
                        .add(Restrictions.eq(Observation.DELETED, false)).createCriteria(Observation.PROCEDURE)
                        .add(Restrictions.eq(Procedure.IDENTIFIER, procedure));
        final Object min = criteria.uniqueResult();
        if (min != null) {
            return new DateTime(min, DateTimeZone.UTC);
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
    public DateTime getMaxDate4Procedure(final String procedure, final Session session) {
        final Criteria cstart =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.PHENOMENON_TIME_START))
                        .createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedure));

        final Object maxStart = cstart.uniqueResult();

        final Criteria cend =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.PHENOMENON_TIME_END))
                        .createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedure));

        final Object maxEnd = cend.uniqueResult();

        if (maxStart == null && maxEnd == null) {
            return null;
        } else {
            final DateTime start = new DateTime(maxStart, DateTimeZone.UTC);
            if (maxEnd != null) {
                final DateTime end = new DateTime(maxEnd, DateTimeZone.UTC);
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
     *            Procedure identifier
     * @param procedureDecriptionFormat
     *            Procedure description format object
     * @param parentProcedures
     *            Parent procedure identifiers
     * @param session
     *            Hibernate session
     * @return Procedure object
     */
    public Procedure getOrInsertProcedure(final String identifier, final ProcedureDescriptionFormat procedureDecriptionFormat,
            final Collection<String> parentProcedures, final Session session) {
        Procedure procedure = getProcedureForIdentifier(identifier, session);
        if (procedure == null) {
            final TProcedure tProcedure = new TProcedure();
            tProcedure.setProcedureDescriptionFormat(procedureDecriptionFormat);
            tProcedure.setIdentifier(identifier);
            if (CollectionHelper.isNotEmpty(parentProcedures)) {
                tProcedure.setParents(CollectionHelper.asSet(getProceduresForIdentifiers(parentProcedures,
                        session)));
            }
            procedure = tProcedure;
        }
        procedure.setDeleted(false);
        session.saveOrUpdate(procedure);
        session.flush();
        session.refresh(procedure);
        return procedure;
    }

}
