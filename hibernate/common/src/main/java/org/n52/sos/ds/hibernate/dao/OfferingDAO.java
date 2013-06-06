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

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.TOffering;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate data access class for offering
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class OfferingDAO extends TimeCreator {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OfferingDAO.class);

    /**
     * Get transactional offering object for identifier
     * 
     * @param identifier
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return Transactional offering object
     */
    public TOffering getTOfferingForIdentifier(String identifier, Session session) {
        return (TOffering) session.createCriteria(TOffering.class)
                .add(Restrictions.eq(Offering.IDENTIFIER, identifier)).uniqueResult();
    }

    /**
     * Get all offering objects
     * 
     * @param session
     *            Hibernate session
     * @return Offering objects
     */
    @SuppressWarnings("unchecked")
    public List<Offering> getOfferingObjects(Session session) {
        return session.createCriteria(Offering.class).list();
    }

    /**
     * Get Offering objct for identifier
     * 
     * @param identifier
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return Offering object
     */
    public Offering getOfferingForIdentifier(String identifier, Session session) {
        return (Offering) session.createCriteria(Offering.class).add(Restrictions.eq(Offering.IDENTIFIER, identifier))
                .uniqueResult();
    }

    /**
     * Get offering identifiers for procedure identifier
     * 
     * @param procedureIdentifier
     *            Procedure identifier
     * @param session
     *            Hibernate session
     * @return Offering identifiers
     */
    @SuppressWarnings("unchecked")
    public List<String> getOfferingIdentifiersForProcedure(String procedureIdentifier, Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.OFFERINGS).setProjection(
                Projections.distinct(Projections.property(Offering.IDENTIFIER)));
        c.createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedureIdentifier));
        return c.list();
    }

    /**
     * Get offering identifiers for observable property identifier
     * 
     * @param observablePropertyIdentifier
     *            Observable property identifier
     * @param session
     *            Hibernate session
     * @return Offering identifiers
     */
    @SuppressWarnings("unchecked")
    public Collection<String> getOfferingIdentifiersForObservableProperty(String observablePropertyIdentifier,
            Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.OFFERINGS).setProjection(
                Projections.distinct(Projections.property(Offering.IDENTIFIER)));
        c.createCriteria(Observation.OBSERVABLE_PROPERTY).add(
                Restrictions.eq(ObservableProperty.IDENTIFIER, observablePropertyIdentifier));
        return c.list();
    }

    /**
     * Get min time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session Hibernate session
     * @return min time for offering
     */
    public DateTime getMinDate4Offering(String offering, Session session) {
        Criteria criteria =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.min(Observation.PHENOMENON_TIME_START))
                        .add(Restrictions.eq(Observation.DELETED, false)).createCriteria(Observation.OFFERINGS)
                        .add(Restrictions.eq(Offering.IDENTIFIER, offering));
        Object min = criteria.uniqueResult();
        if (min != null) {
            return new DateTime(min, DateTimeZone.UTC);
        }
        return null;
    }

    /**
     * Get max time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session Hibernate session
     * @return max time for offering
     */
    public DateTime getMaxDate4Offering(String offering, Session session) {
        Criteria cstart =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.PHENOMENON_TIME_START))
                        .createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offering));

        Object maxStart = cstart.uniqueResult();

        Criteria cend =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.PHENOMENON_TIME_END))
                        .createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offering));

        Object maxEnd = cend.uniqueResult();

        if (maxStart == null && maxEnd == null) {
            return null;
        } else {
            DateTime start = new DateTime(maxStart, DateTimeZone.UTC);
            if (maxEnd != null) {
                DateTime end = new DateTime(maxEnd, DateTimeZone.UTC);
                if (end.isAfter(start)) {
                    return end;
                }
            }
            return start;
        }
    }

    /**
     * Get min result time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session Hibernate session
     * 
     * @return min result time for offering
     */
    public DateTime getMinResultTime4Offering(String offering, Session session) {
        Criteria criteria =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.min(Observation.RESULT_TIME));

        criteria.createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offering));

        Object min = criteria.uniqueResult();
        if (min != null) {
            return new DateTime(min, DateTimeZone.UTC);
        }
        return null;
    }

    /**
     * Get max result time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session Hibernate session
     * 
     * @return max result time for offering
     */
    public DateTime getMaxResultTime4Offering(String offering, Session session) {
        Criteria c =
                session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false))
                        .setProjection(Projections.max(Observation.RESULT_TIME));
        c.createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offering));
        Object maxStart = c.uniqueResult();
        if (maxStart == null) {
            return null;
        } else {
            return new DateTime(maxStart, DateTimeZone.UTC);
        }
    }

    /**
     * Get temporal bounding box for each offering
     * 
     * @param session
     *            Hibernate session
     * @return a Map containing the temporal bounding box for each offering
     */
    public Map<String, TimePeriod> getTemporalBoundingBoxesForOfferings(Session session) {
        if (session != null) {
            Criteria criteria =
                    session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
            criteria.createAlias(Observation.OFFERINGS, "off");
            criteria.setProjection(Projections.projectionList()
                    .add(Projections.min(Observation.PHENOMENON_TIME_START))
                    .add(Projections.max(Observation.PHENOMENON_TIME_START))
                    .add(Projections.max(Observation.PHENOMENON_TIME_END))
                    .add(Projections.groupProperty("off." + Offering.IDENTIFIER)));

            List<?> temporalBoundingBoxes = criteria.list();
            if (!temporalBoundingBoxes.isEmpty()) {
                HashMap<String, TimePeriod> temporalBBoxMap =
                        new HashMap<String, TimePeriod>(temporalBoundingBoxes.size());
                for (Object recordObj : temporalBoundingBoxes) {
                    if (recordObj instanceof Object[]) {
                        Object[] record = (Object[]) recordObj;
                        TimePeriod value =
                                createTimePeriod((Timestamp) record[0], (Timestamp) record[1], (Timestamp) record[2]);
                        temporalBBoxMap.put((String) record[3], value);
                    }
                }
                LOGGER.debug(temporalBoundingBoxes.toString());
                return temporalBBoxMap;
            }
        }
        return new HashMap<String, TimePeriod>(0);
    }

    /**
     * Insert or update and get offering
     * 
     * @param offeringIdentifier
     *            Offering identifier
     * @param offeringName
     *            Offering name
     * @param relatedFeatures
     *            Related feature objects
     * @param observationTypes
     *            Allowed observation type objects
     * @param featureOfInterestTypes
     *            Allowed featureOfInterest type objects
     * @param session
     *            Hibernate session
     * @return Offering object
     */
    public Offering getAndUpdateOrInsertNewOffering(String offeringIdentifier, String offeringName,
            List<RelatedFeature> relatedFeatures, List<ObservationType> observationTypes,
            List<FeatureOfInterestType> featureOfInterestTypes, Session session) {

        TOffering offering = new OfferingDAO().getTOfferingForIdentifier(offeringIdentifier, session);
        if (offering == null) {
            offering = new TOffering();
            offering.setIdentifier(offeringIdentifier);
            if (offeringName != null) {
                offering.setName(offeringName);
            } else {
                offering.setName("Offering for the procedure " + offeringIdentifier);
            }
        }
        if (!relatedFeatures.isEmpty()) {
            offering.setRelatedFeatures(new HashSet<RelatedFeature>(relatedFeatures));
        } else {
            offering.setRelatedFeatures(new HashSet<RelatedFeature>(0));
        }
        if (!observationTypes.isEmpty()) {
            offering.setObservationTypes(new HashSet<ObservationType>(observationTypes));
        } else {
            offering.setObservationTypes(new HashSet<ObservationType>(0));
        }
        if (!featureOfInterestTypes.isEmpty()) {
            offering.setFeatureOfInterestTypes(new HashSet<FeatureOfInterestType>(featureOfInterestTypes));
        } else {
            offering.setFeatureOfInterestTypes(new HashSet<FeatureOfInterestType>(0));
        }
        session.saveOrUpdate(offering);
        session.flush();
        session.refresh(offering);
        return offering;
    }
}
