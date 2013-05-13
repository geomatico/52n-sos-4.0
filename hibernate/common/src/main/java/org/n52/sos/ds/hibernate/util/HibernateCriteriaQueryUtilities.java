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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.BlobValue;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.Codespace;
import org.n52.sos.ds.hibernate.entities.CompositePhenomenon;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.GeometryValue;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.entities.ResultType;
import org.n52.sos.ds.hibernate.entities.SpatialRefSys;
import org.n52.sos.ds.hibernate.entities.SweType;
import org.n52.sos.ds.hibernate.entities.TextValue;
import org.n52.sos.ds.hibernate.entities.Unit;
import org.n52.sos.ogc.OGCConstants;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for Hiberntate Criteria queries.
 * 
 */
public class HibernateCriteriaQueryUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateCriteriaQueryUtilities.class);

    /**
     * Get min phenomenon time from observations
     * 
     * @param session
     *            Hibernate session
     * @return min time
     */
    public static DateTime getMinPhenomenonTime(Session session) {
        Object min = session.createCriteria(Observation.class)
                .setProjection(Projections.min(Observation.PHENOMENON_TIME_START))
                .add(Restrictions.eq(Observation.DELETED, false)).uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get min result time from observations
     * 
     * @param session
     *            Hibernate session
     * 
     * @return min time
     */
    public static DateTime getMinResultTime(Session session) {
        Object min = session.createCriteria(Observation.class)
                .setProjection(Projections.min(Observation.RESULT_TIME))
                .add(Restrictions.eq(Observation.DELETED, false)).uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get max phenomenon from observations
     * 
     * @param session
     *            Hibernate session
     * 
     * @return max time
     */
    public static DateTime getMaxResultTime(Session session) {
        Object max = session.createCriteria(Observation.class)
                .setProjection(Projections.max(Observation.RESULT_TIME))
                .add(Restrictions.eq(Observation.DELETED, false)).uniqueResult();
        if (max == null) {
            return null;
        } else {
            return new DateTime(max);
        }
    }

    /**
     * @param session the session
     *
     * @return the global getEqualRestiction bounding box over all observations, or
     *         <tt>null</tt>
     */
    public static TimePeriod getGlobalTemporalBoundingBox(Session session) {
        if (session != null) {
            Criteria criteria = session.createCriteria(Observation.class);
            criteria.add(Restrictions.eq(Observation.DELETED, false));
            criteria.setProjection(Projections.projectionList()
                    .add(Projections.min(Observation.PHENOMENON_TIME_START))
                    .add(Projections.max(Observation.PHENOMENON_TIME_START))
                    .add(Projections.max(Observation.PHENOMENON_TIME_END)));
            Object temporalBoundingBox = criteria.uniqueResult();
            if (temporalBoundingBox instanceof Object[]) {
                Object[] record = (Object[]) temporalBoundingBox;
                TimePeriod bBox = createTimePeriod((Timestamp) record[0],
                                                   (Timestamp) record[1],
                                                   (Timestamp) record[2]);
                return bBox;
            }
        }
        return null;
    }

    /*
     * Request Temporal Bounding box for each offering:
     * ************************************************* SELECT
     * observation_constellation.offering_id AS offering_id,
     * MIN(observation.phenomenon_time_start) AS min,
     * MAX(observation.phenomenon_time_end) AS max_end ,
     * MAX(observation.phenomenon_time_start) AS max_start FROM observation
     * INNER JOIN observation_constellation ON
     * (observation.observation_constellation_id =
     * observation_constellation.observation_constellation_id) GROUP BY
     * observation_constellation.offering_id ORDER BY
     * observation_constellation.offering_id ASC;
     * 
     * Select max of max_end and max_start for final result
     */
    /**
     * @return a Map containing the bounding box for each offering
     */
    public static Map<String, TimePeriod> getTemporalBoundingBoxesForOfferings(Session session) {
        if (session != null) {
            Criteria criteria = session.createCriteria(Observation.class)
                    .add(Restrictions.eq(Observation.DELETED, false));
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
                        TimePeriod value = createTimePeriod((Timestamp) record[0],
                                                            (Timestamp) record[1],
                                                            (Timestamp) record[2]);
                        temporalBBoxMap.put((String) record[3], value);
                    }
                }
                LOGGER.debug(temporalBoundingBoxes.toString());
                return temporalBBoxMap;
            }
        }
        return new HashMap<String, TimePeriod>(0);
    }

    private static TimePeriod createTimePeriod(Timestamp minStart, Timestamp maxStart, Timestamp maxEnd) {
        DateTime start = new DateTime(minStart);
        DateTime end = new DateTime(maxStart);
        if (maxEnd != null) {
            DateTime endTmp = new DateTime(maxEnd);
            if (endTmp.isAfter(end)) {
                end = endTmp;
            }
        }
        return new TimePeriod(start, end);
    }

    /**
     * Get max phenomenon from observations
     *
     * @param session Hibernate session
     *
     * @return max time
     */
    public static DateTime getMaxPhenomenonTime(Session session) {
        Object maxStart = session.createCriteria(Observation.class)
                .setProjection(Projections.max(Observation.PHENOMENON_TIME_START))
                .add(Restrictions.eq(Observation.DELETED, false))
                .uniqueResult();
        Object maxEnd = session.createCriteria(Observation.class)
                .setProjection(Projections.max(Observation.PHENOMENON_TIME_END))
                .add(Restrictions.eq(Observation.DELETED, false))
                .uniqueResult();
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
     * Get min time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return min time for offering
     */
    public static DateTime getMinDate4Offering(String offering, Session session) {
        Criteria criteria = session.createCriteria(Observation.class)
                .setProjection(Projections.min(Observation.PHENOMENON_TIME_START))
                .add(Restrictions.eq(Observation.DELETED, false))
                .createCriteria(Observation.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));
        Object min = criteria.uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get min result time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session
     * 
     * @return min result time for offering
     */
    public static DateTime getMinResultTime4Offering(String offering, Session session) {
        Criteria criteria = session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .setProjection(Projections.min(Observation.RESULT_TIME));

        criteria.createCriteria(Observation.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));

        Object min = criteria.uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get max time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return max time for offering
     */
    public static DateTime getMaxDate4Offering(String offering, Session session) {
        Criteria cstart = session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .setProjection(Projections.max(Observation.PHENOMENON_TIME_START))
                .createCriteria(Observation.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));

        Object maxStart = cstart.uniqueResult();

        Criteria cend = session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .setProjection(Projections.max(Observation.PHENOMENON_TIME_END))
                .createCriteria(Observation.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));

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
     * Get max result time from observations for offering
     * 
     * @param offering
     *            Offering identifier
     * @param session
     *            Hibernate session
     * 
     * @return max result time for offering
     */
    public static DateTime getMaxResultTime4Offering(String offering, Session session) {
        Criteria c = session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .setProjection(Projections.max(Observation.RESULT_TIME));
        c.createCriteria(Observation.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));
        Object maxStart = c.uniqueResult();
        if (maxStart == null) {
            return null;
        } else {
            return new DateTime(maxStart);
        }
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
    public static Procedure getProcedureForIdentifier(String identifier, Session session) {
        return (Procedure) session.createCriteria(Procedure.class)
                .add(Restrictions.eq(Procedure.IDENTIFIER, identifier))
                .uniqueResult();
    }

    /**
     * Get procedure identifiers for FOI
     *
     * @param session Hibernate session
     * @param feature FOI object
     *
     * @return Related procedure identifiers
     */
    @SuppressWarnings("unchecked")
    public static List<String> getProceduresForFeatureOfInterest(Session session, FeatureOfInterest feature) {
        return session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .createCriteria(Observation.FEATURE_OF_INTEREST)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, feature.getIdentifier()))
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER))).list();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getObservationIdentifiers(Session session) {
        return session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .add(Restrictions.isNotNull(Observation.IDENTIFIER))
                .setProjection(Projections.distinct(Projections.property(Observation.IDENTIFIER))).list();
    }

    public static ProcedureDescriptionFormat getProcedureDescriptionFormatObject(String procDescFormat, Session session) {
        return (ProcedureDescriptionFormat) session.createCriteria(ProcedureDescriptionFormat.class)
                .add(Restrictions.eq(ProcedureDescriptionFormat.PROCEDURE_DESCRIPTION_FORMAT, procDescFormat))
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterest> getFeatureOfInterestObject(Collection<String> featureOfInterestIDs,
            Session session) {
        if (featureOfInterestIDs != null && !featureOfInterestIDs.isEmpty()) {
            return session.createCriteria(FeatureOfInterest.class)
                    .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureOfInterestIDs)).list();
        }
        return Collections.emptyList();
    }

    /**
     * Get FOI identifiers for an offering identifier
     * 
     * @param offeringID
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return FOI identifiers for offering
     */
    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestIdentifiersForOffering(String offeringID, Session session) {
        Criteria c = session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.FEATURE_OF_INTEREST)
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)));
        c.createCriteria(Observation.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offeringID));
        return c.list();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestIdentifiersForObservationConstellation(
            ObservationConstellation oc, Session session) {
        return session.createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .add(Restrictions.eq(Observation.PROCEDURE, oc.getProcedure()))
                .add(Restrictions.eq(Observation.OBSERVABLE_PROPERTY, oc.getObservableProperty()))
                .add(Restrictions.eq(Observation.OFFERINGS, oc.getOffering()))
                .createCriteria(Observation.FEATURE_OF_INTEREST)
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterest.IDENTIFIER)))
                .list();
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterestType> getFeatureOfInterestTypeObjects(List<String> featureOfInterestType,
            Session session) {
        return session.createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.in(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, featureOfInterestType)).list();
    }

    public static FeatureOfInterestType getFeatureOfInterestTypeObject(String featureOfInterestType, Session session) {
        return (FeatureOfInterestType) session.createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.eq(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, featureOfInterestType))
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestTypes(Session session) {
        return session.createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.ne(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, OGCConstants.UNKNOWN))
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE)))
                .list();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestTypesForFeatureOfInterest(
            Collection<String> featureOfInterestIDs, Session session) {
        return session.createCriteria(FeatureOfInterest.class)
                .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureOfInterestIDs))
                .createCriteria(FeatureOfInterest.FEATURE_OF_INTEREST_TYPE)
                .setProjection(Projections.distinct(Projections.property(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE)))
                .list();
    }

    @SuppressWarnings("unchecked")
    public static List<ObservationType> getObservationTypeObjects(List<String> observationTypes, Session session) {
        return session.createCriteria(ObservationType.class)
                .add(Restrictions.in(ObservationType.OBSERVATION_TYPE, observationTypes)).list();
    }

    public static ObservationType getObservationTypeObject(String observationType, Session session) {
        return (ObservationType) session.createCriteria(ObservationType.class)
                .add(Restrictions.eq(ObservationType.OBSERVATION_TYPE, observationType)).uniqueResult();
    }

    public static ResultType getResultType(String resultType, Session session) {
        return (ResultType) session.createCriteria(ResultType.class)
                .add(Restrictions.eq(ResultType.PARAMETER_RESULT_TYPE, resultType)).uniqueResult();
    }

    public static SweType getValueType(String valueType, Session session) {
        return (SweType) session.createCriteria(SweType.class)
                .add(Restrictions.eq(SweType.SWE_TYPE, valueType)).uniqueResult();
    }

    public static Unit getUnit(String unit, Session session) {
        return (Unit) session.createCriteria(Unit.class)
                .add(Restrictions.eq(Unit.UNIT, unit)).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeatureRole> getRelatedFeatureRole(String role, Session session) {
        return session.createCriteria(RelatedFeatureRole.class)
                .add(Restrictions.eq(RelatedFeatureRole.RELATED_FEATURE_ROLE, role)).list();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeature> getRelatedFeatures(String targetIdentifier, Session session) {
        return session.createCriteria(RelatedFeature.class)
                .createCriteria(RelatedFeature.FEATURE_OF_INTEREST)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, targetIdentifier)).list();
    }

    @SuppressWarnings("unchecked")
    public static List<ObservableProperty> getObservableProperties(List<String> identifiers, Session session) {
        return session.createCriteria(ObservableProperty.class)
                .add(Restrictions.in(ObservableProperty.IDENTIFIER, identifiers)).list();
    }

    public static FeatureOfInterest getFeatureOfInterest(String identifier, Session session) {
        return (FeatureOfInterest) session.createCriteria(FeatureOfInterest.class)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, identifier)).uniqueResult();
    }

    public static BlobValue getBlobValue(Object value, Session session) {
        return (BlobValue) session.createCriteria(BlobValue.class)
                .add(Restrictions.eq(BlobValue.VALUE, value)).uniqueResult();
    }

    public static CategoryValue getCategoryValue(String value, Session session) {
        return (CategoryValue) session.createCriteria(CategoryValue.class)
                .add(Restrictions.eq(CategoryValue.VALUE, value)).uniqueResult();
    }

    public static GeometryValue getGeometryValue(Geometry value, Session session) {
        return (GeometryValue) session.createCriteria(GeometryValue.class)
                .add(Restrictions.eq(GeometryValue.VALUE, value)).uniqueResult();
    }

    public static NumericValue getNumericValue(BigDecimal value, Session session) {
        return (NumericValue) session.createCriteria(NumericValue.class)
                .add(Restrictions.eq(NumericValue.VALUE, value)).uniqueResult();
    }

    public static TextValue getTextValue(String value, Session session) {
        return (TextValue) session.createCriteria(TextValue.class)
                .add(Restrictions.eq(TextValue.VALUE, value)).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeature> getRelatedFeatureForOffering(String offering, Session session) {
        return session.createCriteria(RelatedFeature.class)
                .createCriteria(RelatedFeature.OFFERINGS)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering))
                .list();
    }

    @SuppressWarnings("unchecked")
    public static List<Procedure> getProcedureObjects(Session session) {
        return session.createCriteria(Procedure.class).list();
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterest> getFeatureOfInterestObjects(Session session) {
        return session.createCriteria(FeatureOfInterest.class).list();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeature> getRelatedFeatureObjects(Session session) {
        return session.createCriteria(RelatedFeature.class).list();
    }

    @SuppressWarnings("unchecked")
    public static List<Offering> getOfferingObjects(Session session) {
        return session.createCriteria(Offering.class).list();
    }

    public static Offering getOfferingForIdentifier(String identifier, Session session) {
        return (Offering) session.createCriteria(Offering.class)
                .add(Restrictions.eq(Offering.IDENTIFIER, identifier)).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<ObservableProperty> getObservablePropertyObjects(Session session) {
        return session.createCriteria(ObservableProperty.class).list();
    }

    @SuppressWarnings("unchecked")
    public static List<CompositePhenomenon> getCompositePhenomenonObjects(Session session) {
        return session.createCriteria(CompositePhenomenon.class).list();
    }

    @SuppressWarnings("unchecked")
    public static List<SpatialRefSys> getSpatialRefSysObjects(Session session) {
        return session.createCriteria(SpatialRefSys.class).list();
    }

    public static ResultTemplate getResultTemplateObject(String identifier, Session session) {
        return (ResultTemplate) session.createCriteria(ResultTemplate.class)
                .add(Restrictions.eq(ResultTemplate.IDENTIFIER, identifier))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<ResultTemplate> getResultTemplateObjects(Session session) {
        return session.createCriteria(ResultTemplate.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .createCriteria(ResultTemplate.OBSERVATION_CONSTELLATION)
                .add(Restrictions.eq(ObservationConstellation.DELETED, false)).list();
    }

    @SuppressWarnings("unchecked")
    public static List<ResultTemplate> getResultTemplateObjectsForObservationConstellation(
            ObservationConstellation observationConstellation, Session session) {
        return session.createCriteria(ResultTemplate.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq(ResultTemplate.OBSERVATION_CONSTELLATION, observationConstellation))
                .list();
    }

    @SuppressWarnings("unchecked")
    public static ResultTemplate getResultTemplateObject(String offering, String observedProperty, Session session) {
        Criteria rtc = session.createCriteria(ResultTemplate.class).setMaxResults(1);
        Criteria occ = rtc.createCriteria(ResultTemplate.OBSERVATION_CONSTELLATION);
        occ.createCriteria(ObservationConstellation.OFFERING)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));
        occ.createCriteria(ObservationConstellation.OBSERVABLE_PROPERTY)
                .add(Restrictions.eq(ObservableProperty.IDENTIFIER, observedProperty));
        /* there can be multiple but equal result templates... */
        List<ResultTemplate> templates = (List<ResultTemplate>) rtc.list();
        return templates.isEmpty() ? null : templates.iterator().next();
    }

    @SuppressWarnings("unchecked")
    public static List<ResultTemplate> getResultTemplateObject(String offering, String observedProperty,
                                                               Collection<String> featureOfInterest, Session session) {
        Criteria rtc = session.createCriteria(ResultTemplate.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        Criteria occ = rtc.createCriteria(ResultTemplate.OBSERVATION_CONSTELLATION);
        occ.createCriteria(ObservationConstellation.OFFERING)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));
        occ.createCriteria(ObservationConstellation.OBSERVABLE_PROPERTY)
                .add(Restrictions.eq(ObservableProperty.IDENTIFIER, observedProperty));
        if (featureOfInterest != null && !featureOfInterest.isEmpty()) {
            rtc.createCriteria(ResultTemplate.FEATURE_OF_INTEREST)
                    .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureOfInterest));
        }
        return rtc.list();
    }

    public static Order getOrderForEnum(FirstLatest firstLatest) {
        if (firstLatest.equals(FirstLatest.first)) {
            return Order.asc(Observation.PHENOMENON_TIME_START);
        } else if (firstLatest.equals(FirstLatest.latest)) {
            return Order.desc(Observation.PHENOMENON_TIME_END);
        }
        return null;
    }

    public static Codespace getCodespace(String codespace, Session session) {
        return (Codespace) session.createCriteria(Codespace.class)
                .add(Restrictions.eq(Codespace.CODESPACE, codespace)).uniqueResult();
    }

}
