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

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.ds.hibernate.HibernateTestCase;
import org.n52.sos.ds.hibernate.dao.ObservationDAO;
import org.n52.sos.ds.hibernate.dao.OfferingDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @author CarstenHollmann
 *
 * @since 4.0.0
 */
public class ObservationDAOTest extends HibernateTestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationDAOTest.class);
    
    private final ObservationDAO observationDAO = new ObservationDAO();
    private final OfferingDAO offeringDAO = new OfferingDAO();
    @Before
    public void fillObservations() {
        Session session = getSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            HibernateObservationBuilder b = new HibernateObservationBuilder(session);
            DateTime begin = new DateTime();
            for (int i = 0; i < 50; ++i) {
                b.createObservation(String.valueOf(i), begin.plusHours(i));
            }
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

    @After
    public void clearObservations() throws OwsExceptionReport {
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

    @Test
    public void getGlobalTemporalBoundingBoxWithNullReturnsNull() {
        assertThat("global temporal bounding box", observationDAO.getGlobalTemporalBoundingBox(null), is(nullValue()));
    }

    @Test
    public void getGlobalTemporalBoundingBoxEndBeforeStartOrEqual() throws ConnectionProviderException {
        Session session = getSession();
        try {
            TimePeriod temporalBBox = observationDAO.getGlobalTemporalBoundingBox(session);
            assertThat(temporalBBox, is(notNullValue()));
            assertThat(temporalBBox.getStart(), is(notNullValue()));
            assertThat(temporalBBox.getEnd(), is(notNullValue()));
            timePeriodStartIsBeforeEndOrEqual(temporalBBox);
        } finally {
            returnSession(session);
        }
    }

    private void timePeriodStartIsBeforeEndOrEqual(TimePeriod temporalBBox) {
        boolean startBeforeEndOrEqual = temporalBBox.getStart().isEqual(temporalBBox.getEnd()) ||
                                        temporalBBox.getStart().isBefore(temporalBBox.getEnd());
        assertThat("start is before end or equal", startBeforeEndOrEqual, is(true));
    }

    @Test
    public void getTemporalBoundingBoxForOfferingsWithNullReturnsEmptyList() {
        Map<String, TimePeriod> emptyMap = offeringDAO.getTemporalBoundingBoxesForOfferings(null);
        assertThat("empty map", is(notNullValue()));
        assertThat("map is empty", emptyMap.isEmpty(), is(true));
    }

    @Test
    public void getTemporalBoundingBoxForOfferingsContainsNoNullElements() throws ConnectionProviderException {
        Session session = getSession();
        try {
            Map<String, TimePeriod> tempBBoxMap = offeringDAO
                    .getTemporalBoundingBoxesForOfferings(session);
            assertThat("map is empty", tempBBoxMap.isEmpty(), is(false));
            for (String offeringId : tempBBoxMap.keySet()) {
                assertThat("offering id", offeringId, is(not(nullValue())));
                TimePeriod offeringBBox = tempBBoxMap.get(offeringId);
                assertThat("offering temp bbox", offeringBBox, is(not(nullValue())));
                assertThat("offering temporal bbox start", offeringBBox.getStart(), is(not(nullValue())));
                assertThat("offering temporal bbox start", offeringBBox.getEnd(), is(not(nullValue())));
                timePeriodStartIsBeforeEndOrEqual(offeringBBox);
            }
        } finally {
            returnSession(session);
        }
    }

    @Test
    public void runtimeComparisonGetGlobalTemporalBoundingBoxes() throws ConnectionProviderException {
        long startOldWay, startNewWay, endOldWay, endNewWay;
        Session session = getSession();
        try {
            startOldWay = System.currentTimeMillis();
            observationDAO.getMinPhenomenonTime(session);
            observationDAO.getMaxPhenomenonTime(session);
            endOldWay = System.currentTimeMillis();
            startNewWay = System.currentTimeMillis();
            observationDAO.getGlobalTemporalBoundingBox(session);
            endNewWay = System.currentTimeMillis();
            long oldTime = endOldWay - startOldWay, newTime = endNewWay - startNewWay;
            assertThat(String.format("old way is faster? Old way: %sms\\nNew Way: %sms", oldTime, newTime),
                       newTime < oldTime, is(true));
            LOGGER.debug("old way is faster? Old way: {}ms\\nNew Way: {}ms", oldTime, newTime);
        } finally {
            returnSession(session);
        }
    }
}
