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

import static org.hibernate.criterion.Projections.distinct;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.*;
import static org.n52.sos.ds.hibernate.util.HibernateConstants.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.entities.BlobValue;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.Codespace;
import org.n52.sos.ds.hibernate.entities.CompositePhenomenon;
import org.n52.sos.ds.hibernate.entities.CountValue;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.GeometryValue;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
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
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.n52.sos.util.DateTimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for Hiberntate Criteria queries.
 * 
 */
public class HibernateCriteriaQueryUtilities extends DefaultHibernateCriteriaQueryUtilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateCriteriaQueryUtilities.class);

    /**
     * Get min phenomenon time from observations
      * 
     * @param session
     *            Hibernate session
     * @return min time
     */
    public static DateTime getMinPhenomenonTime(Session session) {
        Object min =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.min(PARAMETER_PHENOMENON_TIME_START))
                        .add(getEqualRestriction(DELETED, false)).uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get min result time from observations
     *
     * @param session Hibernate session
     *
     * @return min time
     */
    public static DateTime getMinResultTime(Session session) {
        Object min = session.createCriteria(Observation.class)
                .setProjection(Projections.min(PARAMETER_RESULT_TIME))
                .add(Restrictions.eq(DELETED, false)).uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get max phenomenon from observations
     *
     * @param session Hibernate session
     *
     * @return max time
     */
    public static DateTime getMaxResultTime(Session session) {
        Object max =
               session.createCriteria(Observation.class)
                .setProjection(Projections.max(PARAMETER_RESULT_TIME))
                .add(Restrictions.eq(DELETED, false)).uniqueResult();
        if (max == null) {
            return null;
        } else {
            return new DateTime(max);
        }
    }

    /**
     * @return the global temporal bounding box over all observations, or
     *         <tt>null</tt>
     */
    public static TimePeriod getGlobalTemporalBoundingBox(Session session) {
        if (session != null) {
            Map<String, String> aliases = new HashMap<String, String>();
            String obsConstOffObsTypeAlias =
                    addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
            addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);

            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.min(PARAMETER_PHENOMENON_TIME_START));
            projections.add(Projections.max(PARAMETER_PHENOMENON_TIME_START));
            projections.add(Projections.max(PARAMETER_PHENOMENON_TIME_END));

            Criteria criteria = session.createCriteria(Observation.class);
            addAliasesToCriteria(criteria, aliases);
            criteria.add(getEqualRestriction(DELETED, false));
            criteria.setProjection(projections);

            Object temporalBoundingBox = criteria.uniqueResult();
            if (temporalBoundingBox != null && temporalBoundingBox instanceof Object[]) {
                Object[] record = (Object[]) temporalBoundingBox;
                TimePeriod bBox =
                        createTimePeriod((Timestamp) record[0], (Timestamp) record[1], (Timestamp) record[2]);
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
     * @return a Map containing the temporal bounding box for each offering
     */
    public static Map<String, TimePeriod> getTemporalBoundingBoxesForOfferings(Session session) {
        if (session != null) {
            //
            Map<String, String> aliases = new HashMap<String, String>();
            String obsConstOffObsTypeAlias =
                    addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
            addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);

            ProjectionList projections = Projections.projectionList();
            projections.add(Projections.min(PARAMETER_PHENOMENON_TIME_START));
            projections.add(Projections.max(PARAMETER_PHENOMENON_TIME_START));
            projections.add(Projections.max(PARAMETER_PHENOMENON_TIME_END));
            projections.add(Projections
                    .groupProperty(PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE));

            Criteria criteria = session.createCriteria(Observation.class);
            addAliasesToCriteria(criteria, aliases);
            criteria.add(getEqualRestriction(DELETED, false));
            criteria.setProjection(projections);
            criteria.addOrder(Order
                    .asc(PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE));

            List<?> temporalBoundingBoxes = criteria.list();
            if (!temporalBoundingBoxes.isEmpty()) {
                HashMap<String, TimePeriod> temporalBBoxMap =
                        new HashMap<String, TimePeriod>(temporalBoundingBoxes.size());
                for (Object recordObj : temporalBoundingBoxes) {
                    if (recordObj instanceof Object[]) {
                        Object[] record = (Object[]) recordObj;
                        String key =
                                ((ObservationConstellationOfferingObservationType) record[3]).getOffering()
                                        .getIdentifier();
                        TimePeriod value =
                                createTimePeriod((Timestamp) record[0], (Timestamp) record[1], (Timestamp) record[2]);
                        temporalBBoxMap.put(key, value);
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
     * @param session
     *            Hibernate session
     * @return max time
     */
    public static DateTime getMaxPhenomenonTime(Session session) {
        Object maxStart =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.max(PARAMETER_PHENOMENON_TIME_START))
                        .add(getEqualRestriction(DELETED, false)).uniqueResult();
        Object maxEnd =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.max(PARAMETER_PHENOMENON_TIME_END))
                        .add(getEqualRestriction(DELETED, false)).uniqueResult();
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
        Criteria criteria = session.createCriteria(Observation.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering)).add(
                getEqualRestriction(DELETED, false));
        Object min =
                criteria.setProjection(Projections.min(PARAMETER_PHENOMENON_TIME_START))
                        .uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get min result time from observations for offering
     *
     * @param offering Offering identifier
     * @param session Hibernate session
     *
     * @return min result time for offering
     */
    public static DateTime getMinResultTime4Offering(String offering, Session session) {
        Criteria criteria = session.createCriteria(Observation.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering)).add(
                getEqualRestriction(DELETED, false));
        Object min =
               criteria.setProjection(Projections.min(PARAMETER_RESULT_TIME))
                .uniqueResult();
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
        Criteria criteriaStart = session.createCriteria(Observation.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        addAliasesToCriteria(criteriaStart, aliases);
        criteriaStart.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering)).add(
                getEqualRestriction(DELETED, false));
        Object maxStart =
                criteriaStart.setProjection(Projections.max(HibernateConstants.PARAMETER_PHENOMENON_TIME_START))
                        .uniqueResult();
        Criteria criteriaEnd = session.createCriteria(Observation.class);
        addAliasesToCriteria(criteriaEnd, aliases);
        criteriaEnd.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering)).add(
                getEqualRestriction(HibernateConstants.DELETED, false));
        Object maxEnd =
                criteriaEnd.setProjection(Projections.max(HibernateConstants.PARAMETER_PHENOMENON_TIME_END))
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
     * Get max result time from observations for offering
     *
     * @param offering Offering identifier
     * @param session Hibernate session
     *
     * @return max result time for offering
     */
    public static DateTime getMaxResultTime4Offering(String offering, Session session) {
        Criteria criteriaStart = session.createCriteria(Observation.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        addAliasesToCriteria(criteriaStart, aliases);
        criteriaStart.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering))
                .add(Restrictions.eq(DELETED, false));
        Object maxStart = criteriaStart.setProjection(Projections.max(PARAMETER_RESULT_TIME)).uniqueResult();
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
        Criteria criteria = session.createCriteria(Procedure.class);
        criteria.add(getEqualRestriction(getIdentifierParameter(null), identifier));
        return (Procedure) criteria.uniqueResult();
    }

    /**
     * Get equal expression for requests
     * 
     * @param parameter
     *            parameter name
     * @param value
     *            value
     * @return Equals expression
     */
    public static SimpleExpression getEqualRestriction(String parameter, Object value) {
        return Restrictions.eq(parameter, value);
    }

    /**
     * Get Observation objects for the defined restrictions which are marked as
     * not deleted
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param criterions
     *            Restriction for the query
     * @param projections
     *            Projections for the query
     * @param session
     *            Hibernate session
     * @return Observation objects
     */
    @SuppressWarnings("unchecked")
    public static List<Observation> getObservations(HibernateQueryObject queryObject, Session session) {
        queryObject.addCriterion(getEqualRestriction(HibernateConstants.DELETED, false));
        return (List<Observation>) getObjectList(queryObject, session, Observation.class);
    }

    @SuppressWarnings("unchecked")
    public static List<Observation> getAllObservations(HibernateQueryObject queryObject, Session session) {
        return (List<Observation>) getObjectList(queryObject, session, Observation.class);
    }

    /**
     * Get objects
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param criterions
     *            Restriction for the query
     * @param projections
     *            Projections for the query
     * @param session
     *            Hibernate session
     * @param objectClass
     * @return Result objects
     */
    

    /**
     * Get temporal filter restrictions
     * 
     * @param eventTime
     *            Temporal filters
     * @param criterions
     * @return filter restrictions

     *
     * @throws OwsExceptionReport     *             If the temporal filter is not supported
     */
    public static Criterion getCriterionForTemporalFilters(List<TemporalFilter> eventTime) throws OwsExceptionReport {
        Disjunction disjunction = Restrictions.disjunction();
        for (TemporalFilter temporalFilter : eventTime) {
            disjunction.add(getCriterionForTemporalFilter(temporalFilter));
        }
        return disjunction;
    }

    /**
     * Get restriction for temporal filter
     * 
     * @param temporalFilter
     *            Temporal filer
     * @param criterions
     * @return filter restriction

     *
     * @throws OwsExceptionReport     *             If the temporal filter is not supported
     */
    private static Criterion getCriterionForTemporalFilter(TemporalFilter temporalFilter) throws OwsExceptionReport {
        String valueReference = temporalFilter.getValueReference();
        switch (temporalFilter.getOperator()) {
        // case TM_After:
        // return null;
        // case TM_Before:
        // return null;
        // case TM_Begins:
        // return null;
        // case TM_BegunBy:
        // return null;
        // case TM_Contains:
        // return null;
        case TM_During:
            TimePeriod tp = (TimePeriod) temporalFilter.getTime();
            if (valueReference.contains("phenomenonTime")) {
                Criterion gele =
                        Restrictions.and(Restrictions.ge(HibernateConstants.PARAMETER_PHENOMENON_TIME_START, tp
                                .getStart().toDate()), Restrictions.le(
                                HibernateConstants.PARAMETER_PHENOMENON_TIME_END, tp.getEnd().toDate()));
                Criterion btw =
                        Restrictions.between(HibernateConstants.PARAMETER_PHENOMENON_TIME_START, tp.getStart()
                                .toDate(), tp.getEnd().toDate());
                return Restrictions.or(gele, btw);
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("The requested valueReference for temporal filter is not supported by this server!");
            }
            // case TM_EndedBy:
            // return null;
            // case TM_Ends:
            // return null;
        case TM_Equals:

            TimeInstant ti = (TimeInstant) temporalFilter.getTime();
            if (valueReference.contains("phenomenonTime")) {
                DateTime endTime =
                        DateTimeHelper.setDateTime2EndOfDay4RequestedEndPosition(ti.getValue(),
                                ti.getRequestedTimeLength());
                if (ti.getValue().equals(endTime)) {
                    return getEqualRestriction(HibernateConstants.PARAMETER_PHENOMENON_TIME_START, ti.getValue()
                            .toDate());
                } else {
                    Criterion gele =
                            Restrictions.and(Restrictions.ge(HibernateConstants.PARAMETER_PHENOMENON_TIME_START, ti
                                    .getValue().toDate()), Restrictions.le(
                                    HibernateConstants.PARAMETER_PHENOMENON_TIME_END, endTime.toDate()));
                    Criterion btw =
                            Restrictions.between(HibernateConstants.PARAMETER_PHENOMENON_TIME_START, ti.getValue()
                                    .toDate(), endTime.toDate());
                    return Restrictions.or(gele, btw);
                }
            } else {
                throw new NoApplicableCodeException()
                        .withMessage("The requested valueReference for temporal filter is not supported by this server!");
            }
            // case TM_Meets:
            // return null;
            // case TM_MetBy:
            // return null;
            // case TM_OverlappedBy:
            // return null;
            // case TM_Overlaps:
            // return null;
            default:
                throw new NoApplicableCodeException();
        }
    }

    /**
     * Get spatial filter restrictions
     * 
     * @param propertyName
     *            column name
     * @param resultSpatialFilter
     *            Spatial filter
     * @return filter restriction

     *
     * @throws OwsExceptionReport     *             If the spatial filter is not supported
     */
    public static Criterion getCriterionForSpatialFilter(String propertyName, SpatialOperator operator,
            Geometry geometry)
            throws OwsExceptionReport {
        switch (operator) {
        case BBOX:
            return SpatialRestrictions.within(propertyName, geometry);
            // case Beyond:
            // throw new OwsExceptionReport();
            // case Contains:
            // return SpatialRestrictions.contains(propertyName,
            // resultSpatialFilter.getGeometry());
            // case Crosses:
            // return SpatialRestrictions.crosses(propertyName,
            // resultSpatialFilter.getGeometry());
            // case Disjoint:
            // return SpatialRestrictions.disjoint(propertyName,
            // resultSpatialFilter.getGeometry());
            // case DWithin:
            // return SpatialRestrictions.distanceWithin(propertyName,
            // resultSpatialFilter.getGeometry(), 10);
            // case Equals:
            // return SpatialRestrictions.eq(propertyName,
            // resultSpatialFilter.getGeometry());
            // case Intersects:
            // return SpatialRestrictions.intersects(propertyName,
            // resultSpatialFilter.getGeometry());
            // case Overlaps:
            // return SpatialRestrictions.overlaps(propertyName,
            // resultSpatialFilter.getGeometry());
            // case Touches:
            // return SpatialRestrictions.touches(propertyName,
            // resultSpatialFilter.getGeometry());
            // case Within:
            // return SpatialRestrictions.within(propertyName,
            // resultSpatialFilter.getGeometry());
        default:
                throw new NoApplicableCodeException();
        }
    }

    /**
     * Get restriction for a value list
     * 
     * @param propertyName
     *            column name
     * @param list
     *            restricted values
     * @return filter restriction
     */
    public static Criterion getDisjunctionCriterionForStringList(String propertyName, Collection<String> list) {
        return Restrictions.disjunction().add(Restrictions.in(propertyName, list));
    }

    // OR
    public static Criterion getDisjunctionFor(List<Criterion> criterions) {
        Disjunction disjunction = Restrictions.disjunction();
        for (Criterion criterion : criterions) {
            disjunction.add(criterion);
        }
        return disjunction;
    }

    // AND
    public static Criterion getConjunction(List<Criterion> criterions) {
        Conjunction conjunction = Restrictions.conjunction();
        for (Criterion criterion : criterions) {
            conjunction.add(criterion);
        }
        return conjunction;
    }

    /**
     * Get a distinct projection for the identifier column
     * 
     * @return Distinct projection
     */
    private static Projection getDistinctProjectionForIdentifier() {
        return getDistinctProjection(getIdentifierParameter(null));

    }

    /**
     * Get a distinct projection for the defined column
     * 
     * @param propertyName
     *            Column name for distinct filter
     * @return Distinct projection
     */
    public static Projection getDistinctProjection(String propertyName) {
        return Projections.distinct(Projections.property(propertyName));
    }

    /**
     * Adds an observationConstallation alias to the aliases map
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param prefix
     *            Alias prefix, can be null
     * @return Alias prefix for observationConstallation
     */
    public static String addObservationConstallationAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "oc";
        String parameter = HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addObservationConstallationsAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "ocs";
        String parameter = HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATIONS;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addObservationConstellationOfferingObservationTypeAliasToMap(Map<String, String> aliases,
            String prefix) {
        String alias = "ocoot";
        String parameter = PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPE;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addObservationConstellationOfferingObservationTypesAliasToMap(Map<String, String> aliases,
            String prefix) {
        String alias = "ocoots";
        String parameter = PARAMETER_OBSERVATION_CONSTELLATION_OFFERING_OBSERVATION_TYPES;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    /**
     * Adds an offering alias to the aliases map
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param prefix
     *            Alias prefix, can be null
     * @return Alias prefix for offering
     */
    public static String addOfferingAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "off";
        String parameter = PARAMETER_OFFERING;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    /**
     * Adds an observableProperty alias to the aliases map
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param prefix
     *            Alias prefix, can be null
     * @return Alias prefix for observableProperty
     */
    public static String addObservablePropertyAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "obsProp";
        String parameter = PARAMETER_OBSERVABLE_PROPERTY;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;

    }

    /**
     * Adds a procedure alias to the aliases map
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param prefix
     *            Alias prefix, can be null
     * @return Alias prefix for procedure
     */
    public static String addProcedureAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "proc";
        String parameter = PARAMETER_PROCEDURE;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    private static String addProceduresAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "procs";
        String parameter = PARAMETER_PROCEDURES;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    /**
     * Adds a featureOfInterest alias to the aliases map
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param prefix
     *            Alias prefix, can be null
     * @return Alias prefix for featureOfInterest
     */
    public static String addFeatureOfInterestAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "foi";
        String parameter = PARAMETER_FEATURE_OF_INTEREST;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addFeatureOfInterestTypeAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "foiType";
        String parameter = PARAMETER_FEATURE_OF_INTEREST_TYPE;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addFeatureOfInterestTypesAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "foiTypes";
        String parameter = PARAMETER_FEATURE_OF_INTEREST_TYPES;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addObservationAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "obs";
        String parameter = PARAMETER_OBSERVATIONS;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    private static String addOfferingsAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "offs";
        String parameter = PARAMETER_OFFERINGS;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    private static String addRelatedFeaturesToAliasMap(Map<String, String> aliases, String prefix) {
        String alias = "relFeats";
        String parameter = PARAMETER_RELATED_FEATURES;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    /**
     * Get identifier parameter for a alias prefix
     * 
     * @param prefix
     *            Alias prefix, can be null
     * @return identifier parameter
     */
    public static String getIdentifierParameter(String prefix) {
        return getParameterWithPrefix(PARAMETER_IDENTIFIER, prefix);
    }

    public static String getParameterWithPrefix(String parameter, String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append(prefix);
            builder.append(".");
            builder.append(parameter);
            return builder.toString();
        }

        return parameter;
    }

    /**
     * Get procedure identifiers for FOI
     * 
     * @param session
     *            Hibernate session
     * @param featureOfInterest
     *            FOI object
     * @return Related procedure identifiers
     */
    @SuppressWarnings("unchecked")
    public static List<String> getProceduresForFeatureOfInterest(Session session, FeatureOfInterest featureOfInterest) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstsAlias = addObservationConstallationAliasToMap(aliases, null);
        String procedureAlias = addProcedureAliasToMap(aliases, obsConstsAlias);
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        queryObject.setAliases(aliases);
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(foiAlias),
                featureOfInterest.getIdentifier()));
        queryObject.addProjection(getDistinctProjection(getIdentifierParameter(procedureAlias)));
        return (List<String>) getObjectList(queryObject, session, Observation.class);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getObservationIdentifiers(Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(ne(getIdentifierParameter(null), "null"));
        queryObject.addProjection(getDistinctProjectionForIdentifier());
        return (List<String>) getObjectList(queryObject, session, Observation.class);
    }

    public static ProcedureDescriptionFormat getProcedureDescriptionFormatObject(String procDescFormat, Session session) {
        Criteria criteria = session.createCriteria(ProcedureDescriptionFormat.class);
        criteria.add(eq(PARAMETER_PROCEDURE_DESCRIPTION_FORMAT, procDescFormat));
        return (ProcedureDescriptionFormat) criteria.uniqueResult();
    }

    /**
     * Get FOI identifiers
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param criterions
     *            Restriction for the query
     * @param projections
     *            Projections for the query
     * @param session
     *            Hibernate session
     * @return FOI identifiers
     */
    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestIdentifier(HibernateQueryObject queryObject, Session session) {

        String foiAliases =
                addFeatureOfInterestAliasToMap(queryObject.getAliases(), null);
        queryObject.addProjection(getDistinctProjection(getIdentifierParameter(foiAliases)));
        // queryObject.addProjection(getDistinctProjectionForIdentifier());
        return (List<String>) getObjectList(queryObject, session, Observation.class);
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterest> getFeatureOfInterestObject(Collection<String> featureOfInterestIDs,
            Session session) {
        if (featureOfInterestIDs != null && !featureOfInterestIDs.isEmpty()) {
            Criteria criteria = session.createCriteria(FeatureOfInterest.class);
            criteria.add(in(getIdentifierParameter(null), featureOfInterestIDs));
            return criteria.list();
        }
        return new ArrayList<FeatureOfInterest>(0);
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterest> getFeatureOfInterestObjectsForOffering(String offeringID, Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        queryObject.setAliases(aliases);
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(offeringAlias), offeringID));
        return (List<FeatureOfInterest>) getObjectList(queryObject, session, Observation.class);
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
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypesAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        queryObject.setAliases(aliases);
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(offeringAlias), offeringID));
        queryObject.addCriterion(getEqualRestriction(DELETED, false));
        queryObject.addProjection(distinct(property(getIdentifierParameter(foiAlias))));
        return (List<String>) getObjectList(queryObject, session, Observation.class);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestIdentifiersForObservationConstellation(
            ObservationConstellation observationConstellation, Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        queryObject.setAliases(aliases);
        queryObject.addCriterion(getEqualRestriction(PARAMETER_OBSERVATION_CONSTELLATIONS,
                observationConstellation));
        queryObject.addCriterion(getEqualRestriction(DELETED, false));
        queryObject.addProjection(distinct(property(getIdentifierParameter(foiAlias))));
        return (List<String>) getObjectList(queryObject, session, Observation.class);
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterestType> getFeatureOfInterestTypeObjects(List<String> featureOfInterestType,
                                                                              Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterestType.class);
        criteria.add(in(PARAMETER_FEATURE_OF_INTEREST_TYPE, featureOfInterestType));
        return criteria.list();
    }

    public static FeatureOfInterestType getFeatureOfInterestTypeObject(String featureOfInterestType, Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterestType.class);
        criteria.add(getEqualRestriction(PARAMETER_FEATURE_OF_INTEREST_TYPE, featureOfInterestType));
        return (FeatureOfInterestType) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFeatureOfInterestTypes(Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterestType.class);
        criteria.add(ne(PARAMETER_FEATURE_OF_INTEREST_TYPE, OGCConstants.UNKNOWN));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<FeatureOfInterestType> featureTypes = criteria.list();
        List<String> featureOfInterestTypes = new ArrayList<String>(0);
        if (featureTypes != null) {
            for (FeatureOfInterestType featureOfInterestType : featureTypes) {
                featureOfInterestTypes.add(featureOfInterestType.getFeatureOfInterestType());
            }
        }
        return featureOfInterestTypes;
    }

    public static Collection<String> getFeatureOfInterestTypesForFeatureOfInterest(
            Collection<String> featureOfInterestIDs, Session session) {
        Set<String> featureOfInterestTypes = new HashSet<String>();
        if (featureOfInterestIDs != null && !featureOfInterestIDs.isEmpty()) {
            List<FeatureOfInterest> featureOfInterestObjects =
                    getFeatureOfInterestObject(featureOfInterestIDs, session);
            for (FeatureOfInterest featureOfInterest : featureOfInterestObjects) {
                if (featureOfInterest.getFeatureOfInterestType() != null) {
                    featureOfInterestTypes
                            .add(featureOfInterest.getFeatureOfInterestType().getFeatureOfInterestType());
                }
            }
        }
        return featureOfInterestTypes;
    }

    @SuppressWarnings("unchecked")
    public static List<ObservationType> getObservationTypeObjects(List<String> observationTypes, Session session) {
        Criteria criteria = session.createCriteria(ObservationType.class);
        criteria.add(in(HibernateConstants.PARAMETER_OBSERVATION_TYPE, observationTypes));
        return criteria.list();
    }

    public static ObservationType getObservationTypeObject(String observationType, Session session) {
        Criteria criteria = session.createCriteria(ObservationType.class);
        criteria.add(eq(PARAMETER_OBSERVATION_TYPE, observationType));
        return (ObservationType) criteria.uniqueResult();
    }

    public static ResultType getResultType(String resultType, Session session) {
        Criteria criteria = session.createCriteria(ResultType.class);
        criteria.add(eq(PARAMETER_RESULT_TYPE, resultType));
        return (ResultType) criteria.uniqueResult();
    }

    public static SweType getValueType(String valueType, Session session) {
        Criteria criteria = session.createCriteria(SweType.class);
        criteria.add(eq(PARAMETER_VALUE_TYPE, valueType));
        return (SweType) criteria.uniqueResult();
    }

    public static Unit getUnit(String unit, Session session) {
        Criteria criteria = session.createCriteria(Unit.class);
        criteria.add(eq(PARAMETER_UNIT, unit));
        return (Unit) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeatureRole> getRelatedFeatureRole(String role, Session session) {
        Criteria criteria = session.createCriteria(RelatedFeatureRole.class);
        criteria.add(getEqualRestriction(
                PARAMETER_RELATED_FEATURE_ROLE, role));
        return (List<RelatedFeatureRole>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeature> getRelatedFeatures(String targetIdentifier, Session session) {
        Criteria criteria = session.createCriteria(RelatedFeature.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getEqualRestriction(getIdentifierParameter(foiAlias),
                targetIdentifier));
        return (List<RelatedFeature>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<ObservableProperty> getObservableProperties(List<String> identifiers, Session session) {
        Criteria criteria = session.createCriteria(ObservableProperty.class);
        criteria.add(in(getIdentifierParameter(null), identifiers));
        return (List<ObservableProperty>) criteria.list();
    }

    public static ObservationConstellation getObservationConstallation(Map<String, String> aliases,
            List<Criterion> restrictions, Session session) {
        Criteria criteria = session.createCriteria(ObservationConstellation.class);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getConjunction(restrictions));
        return (ObservationConstellation) criteria.uniqueResult();
    }

    public static ObservationConstellation getObservationConstellation(HibernateQueryObject queryObject,
            Session session) {
        return (ObservationConstellation) getObject(queryObject, session, ObservationConstellation.class);
    }

    public static FeatureOfInterest getFeatureOfInterest(String featureIdentifier, Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterest.class);
        criteria.add(getEqualRestriction(getIdentifierParameter(null), featureIdentifier));
        return (FeatureOfInterest) criteria.uniqueResult();
    }

    @Deprecated
    public static BooleanValue getBooleanValue(Boolean value, Session session) {
        Criteria criteria = session.createCriteria(BooleanValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (BooleanValue) criteria.uniqueResult();
    }

    public static BlobValue getBlobValue(Object value, Session session) {
        Criteria criteria = session.createCriteria(BlobValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (BlobValue) criteria.uniqueResult();
    }

    public static CategoryValue getCategoryValue(String value, Session session) {
        Criteria criteria = session.createCriteria(CategoryValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (CategoryValue) criteria.uniqueResult();
    }

    @Deprecated
    public static CountValue getCountValue(Integer value, Session session) {
        Criteria criteria = session.createCriteria(CountValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (CountValue) criteria.uniqueResult();
    }

    public static GeometryValue getGeometryValue(Geometry value, Session session) {
        Criteria criteria = session.createCriteria(GeometryValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (GeometryValue) criteria.uniqueResult();
    }

    public static NumericValue getNumericValue(BigDecimal value, Session session) {
        Criteria criteria = session.createCriteria(NumericValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (NumericValue) criteria.uniqueResult();
    }

    public static TextValue getTextValue(String value, Session session) {
        Criteria criteria = session.createCriteria(TextValue.class);
        criteria.add(getEqualRestriction(PARAMETER_VALUE, value));
        return (TextValue) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeature> getRelatedFeatureForOffering(String offeringID, Session session) {
        Criteria criteria = session.createCriteria(RelatedFeature.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String offs = addOfferingsAliasToMap(aliases, null);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getEqualRestriction(getIdentifierParameter(offs), offeringID));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<Procedure> getProcedureObjects(Session session) {
        return (List<Procedure>) getObjectList(new HibernateQueryObject(), session, Procedure.class);
    }

    @SuppressWarnings("unchecked")
    public static List<FeatureOfInterest> getFeatureOfInterestObjects(Session session) {
        return (List<FeatureOfInterest>) getObjectList(new HibernateQueryObject(), session, FeatureOfInterest.class);
    }

    @SuppressWarnings("unchecked")
    public static List<RelatedFeature> getRelatedFeatureObjects(Session session) {
        return (List<RelatedFeature>) getObjectList(new HibernateQueryObject(), session, RelatedFeature.class);
    }

    @SuppressWarnings("unchecked")
    public static List<Offering> getOfferingObjects(Session session) {
        return (List<Offering>) getObjectList(new HibernateQueryObject(), session, Offering.class);
    }

    public static Offering getOfferingForIdentifier(String offeringIdentifier, Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(null), offeringIdentifier));
        return (Offering) getObject(queryObject, session, Offering.class);
    }

    @SuppressWarnings("unchecked")
    public static List<ObservableProperty> getObservablePropertyObjects(Session session) {
        return (List<ObservableProperty>) getObjectList(new HibernateQueryObject(), session, ObservableProperty.class);
    }

    @Deprecated
    private static List<?> getDistinctObjects(Session session, Class<?> objectClass) {
        Criteria criteria = session.createCriteria(objectClass);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<CompositePhenomenon> getCompositePhenomenonObjects(Session session) {
        return (List<CompositePhenomenon>) getObjectList(new HibernateQueryObject(), session,
                CompositePhenomenon.class);
    }

    @SuppressWarnings("unchecked")
    public static List<SpatialRefSys> getSpatialRefSysObjects(Session session) {
        return (List<SpatialRefSys>) getObjectList(new HibernateQueryObject(), session, SpatialRefSys.class);
    }

    public static ResultTemplate getResultTemplateObject(String identifier, Session session) {
        Criteria criteria = session.createCriteria(ResultTemplate.class);
        criteria.add(getEqualRestriction(getIdentifierParameter(null), identifier));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return (ResultTemplate) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
	public static List<ResultTemplate> getResultTemplateObjects(Session session) {
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypAlias = addObservationConstellationOfferingObservationTypeAliasToMap(aliases, null);
        
        HibernateQueryObject query = new HibernateQueryObject();
        query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		query.setAliases(aliases);
		query.addCriterion(
				getEqualRestriction(
						getParameterWithPrefix(DELETED, obsConstOffObsTypAlias),
						false));
        
        return (List<ResultTemplate>) getObjectList(query, session, ResultTemplate.class);
    }

    @SuppressWarnings("unchecked")
    public static List<ResultTemplate> getResultTemplateObjectsForObservationConstellation(
            ObservationConstellation observationConstellation, Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>(1);
        String obsConstOffObsTypAlias = addObservationConstellationOfferingObservationTypeAliasToMap(aliases, null);
        queryObject
                .addCriterion(getEqualRestriction(
                        getParameterWithPrefix(PARAMETER_OBSERVATION_CONSTELLATION,
                                obsConstOffObsTypAlias), observationConstellation));
        queryObject.setAliases(aliases);
        return (List<ResultTemplate>) getObjectList(queryObject, session, ResultTemplate.class);
    }

    @SuppressWarnings("unchecked")
    public static ResultTemplate getResultTemplateObject(String offering, String observedProperty, Session session) {
        // Criteria criteria = session.createCriteria(ResultTemplate.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypeAliasToMap(aliases, null);
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, obsConstOffObsTypeAlias);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        String obsPropAlias = addObservablePropertyAliasToMap(aliases, obsConstAlias);
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.setAliases(aliases);
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(offeringAlias), offering));
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(obsPropAlias), observedProperty));
        // criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias),
        // offering));
        // criteria.add(getEqualRestriction(getIdentifierParameter(obsPropAlias),
        // observedProperty));
        // addAliasesToCriteria(criteria, aliases);
        // criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        /* there can be multiple but equal result templates... */
        List<ResultTemplate> templates =
                (List<ResultTemplate>) getObjectList(queryObject, session, ResultTemplate.class);
        return (templates.isEmpty()) ? null : templates.iterator().next();
    }

    @SuppressWarnings("unchecked")
    public static List<ResultTemplate> getResultTemplateObject(String offering, String observedProperty,
                                                               Collection<String> featureOfInterest, Session session) {
        // Criteria criteria = session.createCriteria(ResultTemplate.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstOffObsTypeAlias = addObservationConstellationOfferingObservationTypeAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstOffObsTypeAlias);
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, obsConstOffObsTypeAlias);
        String obsPropAlias = addObservablePropertyAliasToMap(aliases, obsConstAlias);
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(offeringAlias), offering));
        queryObject.addCriterion(getEqualRestriction(getIdentifierParameter(obsPropAlias), observedProperty));
        // criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias),
        // offering));
        // criteria.add(getEqualRestriction(getIdentifierParameter(obsPropAlias),
        // observedProperty));
        if (featureOfInterest != null && !featureOfInterest.isEmpty()) {
            String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
            queryObject.addCriterion(Restrictions.in(getIdentifierParameter(foiAlias), new ArrayList<String>(
                    featureOfInterest)));
            // criteria.add(Restrictions.in(getIdentifierParameter(foiAlias),
            // new ArrayList<String>(featureOfInterest)));
        }
        queryObject.setAliases(aliases);
        // addAliasesToCriteria(criteria, aliases);
        return (List<ResultTemplate>) getObjectList(queryObject, session, ResultTemplate.class);
    }

    @SuppressWarnings("unchecked")
    public static List<ObservationConstellation> getObservationConstellations(HibernateQueryObject queryObject,
                                                                              Session session) {
        return (List<ObservationConstellation>) getObjectList(queryObject, session, ObservationConstellation.class);
    }

    public static Order getOrderForEnum(FirstLatest firstLatest) {
        if (firstLatest.equals(FirstLatest.first)) {
            return Order.asc(PARAMETER_PHENOMENON_TIME_START);
        } else if (firstLatest.equals(FirstLatest.latest)) {
            return Order.desc(PARAMETER_PHENOMENON_TIME_END);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<ObservationConstellationOfferingObservationType> getObservationConstellationOfferingObservationType(
            HibernateQueryObject queryObject, Session session) {
        return (List<ObservationConstellationOfferingObservationType>) getObjectList(queryObject, session,
                ObservationConstellationOfferingObservationType.class);
    }

    public static List<?> getDataAvailabilityValues(HibernateQueryObject queryObject, Session session) {
        return getObjectList(queryObject, session, Observation.class);
    }

    public static Codespace getCodespace(String codespace, Session session) {
        Criteria criteria = session.createCriteria(Codespace.class);
        criteria.add(eq(PARAMETER_CODESPACE, codespace));
        return (Codespace) criteria.uniqueResult();
    }

}
