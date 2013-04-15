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
package org.n52.sos.ds.hibernate.util;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.n52.sos.ds.hibernate.util.TemporalRestrictionTest.Identifier.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Test;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.HibernateTestCase;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.AfterRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.BeforeRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.BeginsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.BegunByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.ContainsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.DuringRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.EndedByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.EndsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.EqualsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.MeetsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.MetByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.OverlappedByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.OverlapsRestriction;
import org.n52.sos.exception.ows.concrete.UnsupportedTimeException;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class TemporalRestrictionTest extends HibernateTestCase {
    private TimeInstant instantsFilteredByInstantFilter;
    private TimeInstant periodsFilteredByInstantFilter;
    private TimePeriod periodsFilteredByPeriodFilter;
    private TimePeriod instantsFilteredByPeriodFilter;
    private HibernateSessionHolder sessionHolder;

    private HibernateSessionHolder getSessionHolder() {
        if (sessionHolder == null) {
            sessionHolder = new HibernateSessionHolder();
        }
        return sessionHolder;
    }

    @After
    public void cleanup() throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSessionHolder().getSession();
            transaction = session.beginTransaction();
            ScrollableIterable<Observation> i = ScrollableIterable.fromCriteria(session
                    .createCriteria(Observation.class));
            for (Observation o : i) {
                session.delete(o);
            }
            i.close();
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    public HibernateObservationBuilder getBuilder(Session session) throws OwsExceptionReport {
        return new HibernateObservationBuilder(session);
    }

    private void createScenarioForInstantsFilteredByInstant(Session session) throws OwsExceptionReport {
        Transaction transaction = null;
        try {
            DateTime ref = new DateTime(DateTimeZone.UTC).minusDays(1);
            instantsFilteredByInstantFilter = new TimeInstant(ref);
            transaction = session.beginTransaction();
            HibernateObservationBuilder b = getBuilder(session);
            b.createObservation(II_BEFORE_ID, ref.minus(1));
            b.createObservation(II_EQUALS_ID, ref);
            b.createObservation(II_AFTER_ID, ref.plus(1));
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        }
    }

    private void createScenarioForInstantsFilteredByPeriod(Session session) throws OwsExceptionReport {
        Transaction transaction = null;
        try {
            DateTime ref = new DateTime(DateTimeZone.UTC).minusDays(1);
            instantsFilteredByPeriodFilter = new TimePeriod(ref.minus(1), ref.plus(1));
            transaction = session.beginTransaction();
            HibernateObservationBuilder b = getBuilder(session);
            b.createObservation(IP_BEFORE_ID, ref.minus(2));
            b.createObservation(IP_BEGINS_ID, ref.minus(1));
            b.createObservation(IP_DURING_ID, ref);
            b.createObservation(IP_ENDS_ID, ref.plus(1));
            b.createObservation(IP_AFTER_ID, ref.plus(2));
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        }
    }

    private void createScenarioForPeriodsFilteredByInstant(Session session) throws OwsExceptionReport {
        Transaction transaction = null;
        try {
            DateTime ref = new DateTime(DateTimeZone.UTC).minusDays(1);
            periodsFilteredByInstantFilter = new TimeInstant(ref);
            transaction = session.beginTransaction();
            HibernateObservationBuilder b = getBuilder(session);
            b.createObservation(PI_BEGUN_BY_ID, ref, ref.plus(1));
            b.createObservation(PI_ENDED_BY_ID, ref.minus(1), ref);
            b.createObservation(PI_AFTER_ID, ref.plus(1), ref.plus(2));
            b.createObservation(PI_BEFORE_ID, ref.minus(2), ref.minus(1));
            b.createObservation(PI_CONTAINS_ID, ref.minus(1), ref.plus(1));
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        }
    }

    private void createScenarioForPeriodsFilteredByPeriod(Session session) throws OwsExceptionReport {
        Transaction transaction = null;
        try {
            DateTime ref = new DateTime(DateTimeZone.UTC).minusDays(1);
            periodsFilteredByPeriodFilter = new TimePeriod(ref.minus(2), ref.plus(2));
            transaction = session.beginTransaction();
            HibernateObservationBuilder b = getBuilder(session);
            b.createObservation(PP_AFTER_ID, ref.plus(3), ref.plus(4));
            b.createObservation(PP_MET_BY_ID, ref.plus(2), ref.plus(3));
            b.createObservation(PP_OVERLAPPED_BY_ID, ref.plus(1), ref.plus(3));
            b.createObservation(PP_ENDS_ID, ref.plus(1), ref.plus(2));
            b.createObservation(PP_ENDED_BY_ID, ref.minus(3), ref.plus(2));
            b.createObservation(PP_DURING_ID, ref.minus(1), ref.plus(1));
            b.createObservation(PP_EQUALS_ID, ref.minus(2), ref.plus(2));
            b.createObservation(PP_CONTAINS_ID, ref.minus(3), ref.plus(3));
            b.createObservation(PP_BEGUN_BY_ID, ref.minus(2), ref.plus(3));
            b.createObservation(PP_BEGINS_ID, ref.minus(2), ref.minus(1));
            b.createObservation(PP_OVERLAPS_ID, ref.minus(3), ref.minus(1));
            b.createObservation(PP_MEETS_ID, ref.minus(3), ref.minus(2));
            b.createObservation(PP_BEFORE_ID, ref.minus(4), ref.minus(3));
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Identifier> filter(TimePrimitiveFieldDescriptor d, Session session, TemporalRestriction r, ITime time)
            throws OwsExceptionReport {
        List<String> list = session.createCriteria(Observation.class)
                .add(r.get(d, time))
                .setProjection(Projections.distinct(Projections.property(Observation.IDENTIFIER)))
                .list();
        Set<Identifier> s = EnumSet.noneOf(Identifier.class);
        for (String id : list) {
            s.add(valueOf(id));
        }
        return s;
    }

    private Set<Identifier> filterInstantsByInstant(Session session, TemporalRestriction r, boolean resultTime) throws
            OwsExceptionReport {
        createScenarioForInstantsFilteredByInstant(session);
        return filter(resultTime ? TemporalRestrictions.RESULT_TIME_FIELDS
                      : TemporalRestrictions.PHENOMENON_TIME_FIELDS, session, r, instantsFilteredByInstantFilter);
    }

    private Set<Identifier> filterInstantsByPeriod(Session session, TemporalRestriction r, boolean resultTime) throws
            OwsExceptionReport {
        createScenarioForInstantsFilteredByPeriod(session);
        return filter(resultTime ? TemporalRestrictions.RESULT_TIME_FIELDS
                      : TemporalRestrictions.PHENOMENON_TIME_FIELDS, session, r, instantsFilteredByPeriodFilter);
    }

    private Set<Identifier> filterPeriodsByInstant(Session session, TemporalRestriction r, boolean resultTime) throws
            OwsExceptionReport {
        createScenarioForPeriodsFilteredByInstant(session);
        return filter(resultTime ? TemporalRestrictions.RESULT_TIME_FIELDS
                      : TemporalRestrictions.PHENOMENON_TIME_FIELDS, session, r, periodsFilteredByInstantFilter);
    }

    private Set<Identifier> filterPeriodsByPeriod(Session session, TemporalRestriction r, boolean resultTime) throws
            OwsExceptionReport {
        createScenarioForPeriodsFilteredByPeriod(session);
        return filter(resultTime ? TemporalRestrictions.RESULT_TIME_FIELDS
                      : TemporalRestrictions.PHENOMENON_TIME_FIELDS, session, r, periodsFilteredByPeriodFilter);
    }

    @Test
    public void instantsFilteredByAfterInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new AfterRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(II_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test
    public void instantsFilteredByAfterInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new AfterRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(II_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test
    public void instantsFilteredByAfterPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new AfterRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByAfterPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new AfterRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByAfterInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new AfterRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByAfterInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new AfterRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByAfterPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new AfterRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByAfterPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new AfterRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBeforeInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new BeforeRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(II_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBeforeInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new BeforeRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(II_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBeforePeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new BeforeRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBeforePeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new BeforeRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBeforeInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new BeforeRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBeforeInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new BeforeRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItems(PI_BEFORE_ID, PI_ENDED_BY_ID, PI_CONTAINS_ID));
            assertThat(filtered, hasSize(3));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBeforePeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new BeforeRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBeforePeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new BeforeRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItems(PP_BEFORE_ID, PP_MEETS_ID, PP_CONTAINS_ID, PP_ENDED_BY_ID, PP_OVERLAPS_ID));
            assertThat(filtered, hasSize(5));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByEqualsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new EqualsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(II_EQUALS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByEqualsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new EqualsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(II_EQUALS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByEqualsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new EqualsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByEqualsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new EqualsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEqualsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new EqualsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEqualsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new EqualsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_BEGUN_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEqualsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new EqualsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_EQUALS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByEqualsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new EqualsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByContainsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new ContainsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByContainsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new ContainsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByContainsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new ContainsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByContainsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new ContainsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByContainsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new ContainsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_CONTAINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByContainsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new ContainsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByContainsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new ContainsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_CONTAINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByContainsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new ContainsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByDuringInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new DuringRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByDuringInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new DuringRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByDuringPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new DuringRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_DURING_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByDuringPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new DuringRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_DURING_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByDuringInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new DuringRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByDuringInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new DuringRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByDuringPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new DuringRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_DURING_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test
    public void periodsFilteredByDuringPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new DuringRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItems(PP_OVERLAPPED_BY_ID, PP_ENDS_ID, PP_DURING_ID));
            assertThat(filtered, hasSize(3));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByBeginsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new BeginsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByBeginsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new BeginsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBeginsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new BeginsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEGINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test
    public void instantsFilteredByBeginsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new BeginsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEGINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByBeginsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new BeginsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByBeginsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new BeginsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBeginsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new BeginsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_BEGINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBeginsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new BeginsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItems(PP_BEGINS_ID, PP_EQUALS_ID, PP_BEGUN_BY_ID));
            assertThat(filtered, hasSize(3));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBegunByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new BegunByRestriction(), false);
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByBegunByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new BegunByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByBegunByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new BegunByRestriction(), false);
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByBegunByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new BegunByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBegunByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new BegunByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_BEGUN_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByBegunByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new BegunByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByBegunByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new BegunByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_BEGUN_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByBegunByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new BegunByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByEndsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new EndsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByEndsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new EndsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByEndsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new EndsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_ENDS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByEndsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new EndsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_ENDS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByEndsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new EndsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByEndsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new EndsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEndsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new EndsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_ENDS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEndsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new EndsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_MET_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByEndedByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new EndedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByEndedByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new EndedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }


    @Test
    public void instantsFilteredByEndedByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new EndedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByEndedByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new EndedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEndedByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByInstant(session, new EndedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PI_ENDED_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByEndedByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new EndedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByEndedByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new EndedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_ENDED_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByEndedByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new EndedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByOverlapsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new OverlapsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByOverlapsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new OverlapsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByOverlapsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new OverlapsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByOverlapsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new OverlapsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByOverlapsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new OverlapsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByOverlapsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new OverlapsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }


    @Test
    public void periodsFilteredByOverlapsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new OverlapsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_OVERLAPS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByOverlapsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new OverlapsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByOverlappedByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new OverlappedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByOverlappedByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new OverlappedByRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByOverlappedByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new OverlappedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByOverlappedByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new OverlappedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByOverlappedByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new OverlappedByRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByOverlappedByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new OverlappedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByOverlappedByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new OverlappedByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_OVERLAPPED_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByOverlappedByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new OverlappedByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByMeetsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new MeetsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByMeetsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByInstant(session, new MeetsRestriction(), true);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }


    @Test
    public void instantsFilteredByMeetsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new MeetsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByMeetsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new MeetsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByMeetsInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new MeetsRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByMeetsInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new MeetsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByMeetsPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new MeetsRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_MEETS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByMeetsPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new MeetsRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByMetByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new MetByRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }
    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByMetByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByInstant(session, new MetByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void instantsFilteredByMetByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterInstantsByPeriod(session, new MetByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void instantsFilteredByMetByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterInstantsByPeriod(session, new MetByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByMetByInstantPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new MetByRestriction(), false);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByMetByInstantResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByInstant(session, new MetByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test
    public void periodsFilteredByMetByPeriodPhenomenonTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            Set<Identifier> filtered = filterPeriodsByPeriod(session, new MetByRestriction(), false);
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(PP_MET_BY_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void periodsFilteredByMetByPeriodResultTime() throws OwsExceptionReport {
        Session session = getSessionHolder().getSession();
        try {
            filterPeriodsByPeriod(session, new MetByRestriction(), true);
        } finally {
            getSessionHolder().returnSession(session);
        }
    }

    public enum Identifier {
        PP_AFTER_ID,
        PP_MEETS_ID,
        PP_OVERLAPS_ID,
        PP_ENDED_BY_ID,
        PP_CONTAINS_ID,
        PP_EQUALS_ID,
        PP_BEGUN_BY_ID,
        PP_OVERLAPPED_BY_ID,
        PP_MET_BY_ID,
        PP_BEFORE_ID,
        PP_BEGINS_ID,
        PP_ENDS_ID,
        PP_DURING_ID,
        IP_BEFORE_ID,
        IP_BEGINS_ID,
        IP_DURING_ID,
        IP_ENDS_ID,
        IP_AFTER_ID,
        PI_CONTAINS_ID,
        PI_BEFORE_ID,
        PI_AFTER_ID,
        PI_ENDED_BY_ID,
        PI_BEGUN_BY_ID,
        II_AFTER_ID,
        II_EQUALS_ID,
        II_BEFORE_ID;
    }
}
