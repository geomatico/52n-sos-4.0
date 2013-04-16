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

import static org.n52.sos.ds.hibernate.util.TemporalRestrictionTest.Identifier.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.junit.After;
import org.junit.Before;
import org.n52.sos.ds.hibernate.HibernateTestCase;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class TemporalRestrictionTest extends HibernateTestCase {
    private ITime filter;

    @After
    public void cleanup() throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
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
            returnSession(session);
        }
    }

    @Before
    public void createScenario() throws OwsExceptionReport {
        Session session = getSession();
        try {
            this.filter = createScenario(session);
        } finally {
            returnSession(session);
        }
    }

    protected abstract ITime createScenario(Session session) throws OwsExceptionReport;

    protected HibernateObservationBuilder getBuilder(Session session) throws OwsExceptionReport {
        return new HibernateObservationBuilder(session);
    }

    @SuppressWarnings("unchecked")
    private Set<Identifier> filter(TimePrimitiveFieldDescriptor d, TemporalRestriction r, ITime time, Session session)
            throws OwsExceptionReport {
        List<String> list = session.createCriteria(Observation.class).add(r.get(d, time))
                .setProjection(Projections.distinct(Projections.property(Observation.IDENTIFIER))).list();
        Set<Identifier> s = EnumSet.noneOf(Identifier.class);
        for (String id : list) {
            s.add(valueOf(id));
        }
        return s;
    }

    protected Set<Identifier> filterPhenomenonTime(Session session, TemporalRestriction r) throws
            OwsExceptionReport {
        return filter(TemporalRestrictions.PHENOMENON_TIME_FIELDS, r, filter, session);
    }

    protected Set<Identifier> filterResultTime(Session session, TemporalRestriction r) throws
            OwsExceptionReport {
        return filter(TemporalRestrictions.RESULT_TIME_FIELDS, r, filter, session);
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
