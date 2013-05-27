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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.n52.sos.ds.hibernate.HibernateTestCase.assertThat;
import static org.n52.sos.ds.hibernate.util.TemporalRestrictionTest.Identifier.*;

import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.AfterRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.BeforeRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.BeginsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.BegunByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.ContainsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.DuringRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.EndedByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.EndsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.TEqualsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.MeetsRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.MetByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.OverlappedByRestriction;
import org.n52.sos.ds.hibernate.util.TemporalRestriction.OverlapsRestriction;
import org.n52.sos.exception.ows.concrete.UnsupportedTimeException;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class TemporalRestrictionInstantPeriodTest extends TemporalRestrictionTest {
    @Override
    protected TimePeriod createScenario(Session session) throws OwsExceptionReport {
        Transaction transaction = null;
        try {
            DateTime ref = new DateTime(DateTimeZone.UTC).minusDays(1);

            transaction = session.beginTransaction();
            HibernateObservationBuilder b = getBuilder(session);
            b.createObservation(IP_BEFORE_ID, ref.minus(2));
            b.createObservation(IP_BEGINS_ID, ref.minus(1));
            b.createObservation(IP_DURING_ID, ref);
            b.createObservation(IP_ENDS_ID, ref.plus(1));
            b.createObservation(IP_AFTER_ID, ref.plus(2));
            session.flush();
            transaction.commit();
            return new TimePeriod(ref.minus(1), ref.plus(1));
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw he;
        }
    }

    @Test
    public void testAfterPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new AfterRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testAfterResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterResultTime(session, new AfterRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_AFTER_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testBeforePhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new BeforeRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testBeforeResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterResultTime(session, new BeforeRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEFORE_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testEqualsPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new TEqualsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testEqualsResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new TEqualsRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testContainsPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new ContainsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testContainsResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new ContainsRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testDuringPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new DuringRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_DURING_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testDuringResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterResultTime(session, new DuringRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_DURING_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testBeginsPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new BeginsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEGINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testBeginsResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterResultTime(session, new BeginsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_BEGINS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testBegunByPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new BegunByRestriction());
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testBegunByResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new BegunByRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testEndsPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new EndsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_ENDS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testEndsResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterResultTime(session, new EndsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, hasItem(IP_ENDS_ID));
            assertThat(filtered, hasSize(1));
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testEndedByPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new EndedByRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testEndedByResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new EndedByRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testOverlapsPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new OverlapsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testOverlapsResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new OverlapsRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testOverlappedByPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new OverlappedByRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testOverlappedByResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new OverlappedByRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testMeetsPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new MeetsRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testMeetsResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new MeetsRestriction());
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void testMetByPhenomenonTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            Set<Identifier> filtered = filterPhenomenonTime(session, new MetByRestriction());
            assertThat(filtered, is(notNullValue()));
            assertThat(filtered, is(empty()));
        } finally {
            returnSession(session);
        }
    }

    @Test(expected = UnsupportedTimeException.class)
    public void testMetByResultTime() throws OwsExceptionReport {
        Session session = getSession();
        try {
            filterResultTime(session, new MetByRestriction());
        } finally {
            returnSession(session);
        }
    }
}
