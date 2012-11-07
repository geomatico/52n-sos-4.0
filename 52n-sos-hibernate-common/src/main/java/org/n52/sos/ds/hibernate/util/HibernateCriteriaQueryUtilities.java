/**
 * Copyright (C) 2012
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
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.CompositePhenomenon;
import org.n52.sos.ds.hibernate.entities.CountValue;
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
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Util4Exceptions;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for Hiberntate Criteria queries.
 * 
 */
public class HibernateCriteriaQueryUtilities {

    /**
     * Get min time from observations
     * 
     * @param session
     *            Hibernate session
     * @return min time
     */
    public static DateTime getMinObservationTime(Session session) {
        Object min =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.min(HibernateConstants.PARAMETER_PHENOMENON_TIME_START))
						.add(getEqualRestriction(HibernateConstants.DELETED, false))
                        .uniqueResult();
        if (min != null) {
            return new DateTime(min);
        }
        return null;
    }

    /**
     * Get max time from observations
     * 
     * @param session
     *            Hibernate session
     * @return max time
     */
    public static DateTime getMaxObservationTime(Session session) {
        Object maxStart =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.max(HibernateConstants.PARAMETER_PHENOMENON_TIME_START))
						.add(getEqualRestriction(HibernateConstants.DELETED, false))
                        .uniqueResult();
        Object maxEnd =
                session.createCriteria(Observation.class)
                        .setProjection(Projections.max(HibernateConstants.PARAMETER_PHENOMENON_TIME_END))
						.add(getEqualRestriction(HibernateConstants.DELETED, false))
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
        Criteria criteria = session.createCriteria(Observation.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstAlias);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering))
				.add(getEqualRestriction(HibernateConstants.DELETED, false));
        Object min =
                criteria.setProjection(Projections.min(HibernateConstants.PARAMETER_PHENOMENON_TIME_START))
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
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstAlias);
        addAliasesToCriteria(criteriaStart, aliases);
        criteriaStart.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering))
				.add(getEqualRestriction(HibernateConstants.DELETED, false));
        Object maxStart =
                criteriaStart.setProjection(Projections.max(HibernateConstants.PARAMETER_PHENOMENON_TIME_START))
                        .uniqueResult();
        Criteria criteriaEnd = session.createCriteria(Observation.class);
        addAliasesToCriteria(criteriaEnd, aliases);
        criteriaEnd.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering))
				.add(getEqualRestriction(HibernateConstants.DELETED, false));
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
     * Get Observation objects for the defined restrictions which are marked as not deleted
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
    public static List<Observation> getObservations(Map<String, String> aliases, List<Criterion> criterions,
            List<Projection> projections, Session session) {
        criterions.add(getEqualRestriction(HibernateConstants.DELETED, false));
        return (List<Observation>) getObject(aliases, criterions, projections, session, Observation.class);
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
    protected static List<?> getObject(Map<String, String> aliases, List<Criterion> criterions,
            List<Projection> projections, Session session, Class objectClass) {
        Criteria criteria = session.createCriteria(objectClass);
        addAliasesToCriteria(criteria, aliases);
        Conjunction conjunction = Restrictions.conjunction();
        for (Criterion criterion : criterions) {
            conjunction.add(criterion);
        }
        criteria.add(conjunction);
        for (Projection projection : projections) {
            criteria.setProjection(projection);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    /**
     * Get temporal filter restrictions
     * 
     * @param eventTime
     *            Temporal filters
     * @return filter restrictions
     * @throws OwsExceptionReport
     *             If the temporal filter is not supported
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
     * @return filter restriction
     * @throws OwsExceptionReport
     *             If the temporal filter is not supported
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
                throw Util4Exceptions.createNoApplicableCodeException(null,
                        "The requested valueReference for temporal filter is not supported by this server!");
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
                throw Util4Exceptions.createNoApplicableCodeException(null,
                        "The requested valueReference for temporal filter is not supported by this server!");
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
            throw new OwsExceptionReport();
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
     * @throws OwsExceptionReport
     *             If the spatial filter is not supported
     */
    public static Criterion getCriterionForSpatialFilter(String propertyName, SpatialFilter resultSpatialFilter)
            throws OwsExceptionReport {
        switch (resultSpatialFilter.getOperator()) {
        case BBOX:
            return SpatialRestrictions.within(propertyName, resultSpatialFilter.getGeometry());
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
            throw new OwsExceptionReport();
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
    public static Criterion getDisjunctionCriterionForStringList(String propertyName, List<String> list) {
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Restrictions.in(propertyName, list));
        return disjunction;
    }

    public static Criterion getConjunction(List<Criterion> criterions) {
        Conjunction conjunction = Restrictions.conjunction();
        for (Criterion criterion : criterions) {
            conjunction.add(criterion);
        }
        return conjunction;
    }

    /**
     * Add aliases to a Hibernate Criteria
     * 
     * @param criteria
     *            Hibernate Criteria
     * @param aliases
     *            Aliases for query between tables
     */
    public static void addAliasesToCriteria(Criteria criteria, Map<String, String> aliases) {
        for (String aliasKey : aliases.keySet()) {
            criteria.createAlias(aliasKey, aliases.get(aliasKey));
        }
    }

    /**
     * Add a alias to aliases map
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param prefix
     *            Alias prefix
     * @param parameter
     *            Alias column name
     * @param alias
     *            previously defined alias, can be null
     */
    private static void addAliasToMap(Map<String, String> aliases, String prefix, String parameter, String alias) {
        if (prefix != null && !prefix.isEmpty()) {
            aliases.put(prefix + "." + parameter, alias);
        } else {
            aliases.put(parameter, alias);
        }
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
        String parameter = HibernateConstants.PARAMETER_OFFERING;
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
        String parameter = HibernateConstants.PARAMETER_OBSERVABLE_PROPERTY;
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
        String parameter = HibernateConstants.PARAMETER_PROCEDURE;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    private static String addProceduresAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "procs";
        String parameter = HibernateConstants.PARAMETER_PROCEDURES;
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
        String parameter = HibernateConstants.PARAMETER_FEATURE_OF_INTEREST;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addFeatureOfInterestTypeAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "foiType";
        String parameter = HibernateConstants.PARAMETER_FEATURE_OF_INTEREST_TYPE;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addFeatureOfInterestTypesAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "foiTypes";
        String parameter = HibernateConstants.PARAMETER_FEATURE_OF_INTEREST_TYPES;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static String addObservationAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "obs";
        String parameter = HibernateConstants.PARAMETER_OBSERVATIONS;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    private static String addRelatedFeaturesToAliasMap(Map<String, String> aliases, String prefix) {
        String alias = "relFeats";
        String parameter = HibernateConstants.PARAMETER_RELATED_FEATURES;
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
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + "." + HibernateConstants.PARAMETER_IDENTIFIER;
        } else {
            return HibernateConstants.PARAMETER_IDENTIFIER;
        }
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
    public static List<String> getProceduresForFeatureOfInterest(Session session, FeatureOfInterest featureOfInterest) {
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String procedureAlias = addProcedureAliasToMap(aliases, obsConstAlias);
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        List<Projection> projections = new ArrayList<Projection>();
        projections.add(getDistinctProjection(HibernateConstants.PARAMETER_FEATURE_OF_INTEREST));
        projections.add(getDistinctProjection(getIdentifierParameter(procedureAlias)));
        List<Criterion> criterions = new ArrayList<Criterion>();
        criterions.add(getEqualRestriction(getIdentifierParameter(foiAlias), featureOfInterest.getIdentifier()));
        return (List<String>) getObject(aliases, criterions, projections, session, Observation.class);
    }

    public static List<String> getObservationIdentifiers(Session session) {
        List<Projection> projections = new ArrayList<Projection>();
        projections.add(getDistinctProjectionForIdentifier());
        return (List<String>) getObject(new HashMap<String, String>(), new ArrayList<Criterion>(), projections,
                session, Observation.class);
    }

    public static ProcedureDescriptionFormat getProcedureDescriptionFormatObject(String procDescFormat, Session session) {
        Criteria criteria = session.createCriteria(ProcedureDescriptionFormat.class);
        criteria.add(Restrictions.eq(HibernateConstants.PARAMETER_PROCEDURE_DESCRIPTION_FORMAT, procDescFormat));
        return (ProcedureDescriptionFormat) criteria.uniqueResult();
    }

    public static Collection<String> getProcedureDescriptionFormatIdentifiers(Session session) {
        Criteria criteria = session.createCriteria(ProcedureDescriptionFormat.class);
        List<ProcedureDescriptionFormat> procedureDescriptionFormats = criteria.list();
        Set<String> procDescTypes = new HashSet<String>(0);
        if (procedureDescriptionFormats != null) {
            for (ProcedureDescriptionFormat procedureDescriptionFormat : procedureDescriptionFormats) {
                procDescTypes.add(procedureDescriptionFormat.getProcedureDescriptionFormat());
            }
        }
        return procDescTypes;
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
    public static List<String> getFeatureOfInterestIdentifier(Map<String, String> aliases, List<Criterion> criterions,
            List<Projection> projections, Session session) {
        projections.add(getDistinctProjectionForIdentifier());
        return (List<String>) getObject(aliases, criterions, projections, session, FeatureOfInterest.class);
    }

    public static List<FeatureOfInterest> getFeatureOfInterestObject(Collection<String> featureOfInterestIDs,
            Session session) {
        if (featureOfInterestIDs != null && !featureOfInterestIDs.isEmpty()) {
            Criteria criteria = session.createCriteria(FeatureOfInterest.class);
            criteria.add(Restrictions.in(getIdentifierParameter(null), featureOfInterestIDs));
            return criteria.list();
        }
        return new ArrayList<FeatureOfInterest>(0);
    }

    public static List<FeatureOfInterest> getFeatureOfInterestObjectsForOffering(String offeringID, Session session) {
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstAlias);
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        criterions.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offeringID));
        return (List<FeatureOfInterest>) getObject(aliases, criterions, projections, session, Observation.class);
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
    public static List<String> getFeatureOfInterestIdentifiersForOffering(String offeringID, Session session) {
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstAlias);
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        criterions.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offeringID));
		criterions.add(getEqualRestriction(HibernateConstants.DELETED, false));
        projections.add(Projections.distinct(Projections.property(getIdentifierParameter(foiAlias))));
        return (List<String>) getObject(aliases, criterions, projections, session, Observation.class);
    }

    public static List<FeatureOfInterestType> getFeatureOfInterestTypeObjects(List<String> featureOfInterestType,
            Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterestType.class);
        criteria.add(Restrictions.in(HibernateConstants.PARAMETER_FEATURE_OF_INTEREST_TYPE, featureOfInterestType));
        return criteria.list();
    }

    public static FeatureOfInterestType getFeatureOfInterestTypeObject(String featureOfInterestType, Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterestType.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_FEATURE_OF_INTEREST_TYPE, featureOfInterestType));
        return (FeatureOfInterestType) criteria.uniqueResult();
    }

    public static List<String> getFeatureOfInterestTypes(Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterestType.class);
        criteria.add(Restrictions.ne(HibernateConstants.PARAMETER_FEATURE_OF_INTEREST_TYPE, OGCConstants.UNKNOWN));
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

    public static List<ObservationType> getObservationTypeObjects(List<String> observationTypes, Session session) {
        Criteria criteria = session.createCriteria(ObservationType.class);
        criteria.add(Restrictions.in(HibernateConstants.PARAMETER_OBSERVATION_TYPE, observationTypes));
        return criteria.list();
    }

    public static ObservationType getObservationTypeObject(String observationType, Session session) {
        Criteria criteria = session.createCriteria(ObservationType.class);
        criteria.add(Restrictions.eq(HibernateConstants.PARAMETER_OBSERVATION_TYPE, observationType));
        return (ObservationType) criteria.uniqueResult();
    }

    public static Collection<String> getObservationTypes(Session session) {
        Criteria criteria = session.createCriteria(ObservationType.class);
        List<ObservationType> observationTypes = criteria.list();
        List<String> obsTypes = new ArrayList<String>(0);
        if (observationTypes != null) {
            for (ObservationType observationType : observationTypes) {
                obsTypes.add(observationType.getObservationType());
            }
        }
        return obsTypes;
    }

    public static ResultType getResultType(String resultType, Session session) {
        Criteria criteria = session.createCriteria(ResultType.class);
        criteria.add(Restrictions.eq(HibernateConstants.PARAMETER_RESULT_TYPE, resultType));
        return (ResultType) criteria.uniqueResult();
    }

    public static SweType getValueType(String valueType, Session session) {
        Criteria criteria = session.createCriteria(SweType.class);
        criteria.add(Restrictions.eq(HibernateConstants.PARAMETER_VALUE_TYPE, valueType));
        return (SweType) criteria.uniqueResult();
    }

    public static Unit getUnit(String unit, Session session) {
        Criteria criteria = session.createCriteria(Unit.class);
        criteria.add(Restrictions.eq(HibernateConstants.PARAMETER_UNIT, unit));
        return (Unit) criteria.uniqueResult();
    }

    public static List<RelatedFeatureRole> getRelatedFeatureRole(String role, Session session) {
        Criteria criteria = session.createCriteria(RelatedFeatureRole.class);
        criteria.add(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateConstants.PARAMETER_RELATED_FEATURE_ROLE, role));
        return (List<RelatedFeatureRole>) criteria.list();
    }

    public static List<RelatedFeature> getRelatedFeatures(String targetIdentifier, Session session) {
        Criteria criteria = session.createCriteria(RelatedFeature.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(HibernateCriteriaQueryUtilities.getEqualRestriction(getIdentifierParameter(foiAlias),
                targetIdentifier));
        return (List<RelatedFeature>) criteria.list();
    }

    public static List<ObservableProperty> getObservableProperties(List<String> identifiers, Session session) {
        Criteria criteria = session.createCriteria(ObservableProperty.class);
        criteria.add(Restrictions.in(getIdentifierParameter(null), identifiers));
        return (List<ObservableProperty>) criteria.list();
    }

    public static ObservationConstellation getObservationConstallation(Map<String, String> aliases,
            List<Criterion> restrictions, Session session) {
        Criteria criteria = session.createCriteria(ObservationConstellation.class);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getConjunction(restrictions));
        return (ObservationConstellation) criteria.uniqueResult();
    }

    public static FeatureOfInterest getFeatureOfInterest(String featureIdentifier, Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterest.class);
        criteria.add(getEqualRestriction(getIdentifierParameter(null), featureIdentifier));
        return (FeatureOfInterest) criteria.uniqueResult();
    }

    public static BooleanValue getBooleanValue(Boolean value, Session session) {
        Criteria criteria = session.createCriteria(BooleanValue.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_VALUE, value));
        return (BooleanValue) criteria.uniqueResult();
    }

    public static CategoryValue getCategoryValue(String value, Session session) {
        Criteria criteria = session.createCriteria(CategoryValue.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_VALUE, value));
        return (CategoryValue) criteria.uniqueResult();
    }

    public static CountValue getCountValue(Integer value, Session session) {
        Criteria criteria = session.createCriteria(CountValue.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_VALUE, value));
        return (CountValue) criteria.uniqueResult();
    }

    public static GeometryValue getGeometryValue(Geometry value, Session session) {
        Criteria criteria = session.createCriteria(GeometryValue.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_VALUE, value));
        return (GeometryValue) criteria.uniqueResult();
    }

    public static NumericValue getNumericValue(Double value, Session session) {
        Criteria criteria = session.createCriteria(NumericValue.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_VALUE, value));
        return (NumericValue) criteria.uniqueResult();
    }

    public static TextValue getTextValue(String value, Session session) {
        Criteria criteria = session.createCriteria(TextValue.class);
        criteria.add(getEqualRestriction(HibernateConstants.PARAMETER_VALUE, value));
        return (TextValue) criteria.uniqueResult();
    }

    public static List<RelatedFeature> getRelatedFeatureForOffering(String offeringID, Session session) {
        Criteria criteria = session.createCriteria(RelatedFeature.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String offs = addOfferingsAliasToMap(aliases, null);
        addAliasesToCriteria(criteria, aliases);
        criteria.add(getEqualRestriction(getIdentifierParameter(offs), offeringID));
        return criteria.list();
    }

    private static String addOfferingsAliasToMap(Map<String, String> aliases, String prefix) {
        String alias = "offs";
        String parameter = HibernateConstants.PARAMETER_OFFERINGS;
        addAliasToMap(aliases, prefix, parameter, alias);
        return alias;
    }

    public static List<Procedure> getProcedureObjects(Session session) {
        return(List<Procedure>) getDistinctObjects(session, Procedure.class);
    }
    
    public static List<FeatureOfInterest> getFeatureOfInterestObjects(Session session) {
        return (List<FeatureOfInterest>) getDistinctObjects(session, FeatureOfInterest.class);
    }

    public static List<RelatedFeature> getRelatedFeatureObjects(Session session) {
        return (List<RelatedFeature>) getDistinctObjects(session, RelatedFeature.class);
    }

    public static List<Offering> getOfferingObjects(Session session) {
        return (List<Offering>) getDistinctObjects(session, Offering.class);
    }

    public static List<ObservableProperty> getObservablePropertyObjects(Session session) {
        return (List<ObservableProperty>) getDistinctObjects(session, ObservableProperty.class);
    }

    private static List<?> getDistinctObjects(Session session, Class<?> objectClass) {
        Criteria criteria = session.createCriteria(objectClass);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    public static List<CompositePhenomenon> getCompositePhenomenonObjects(Session session) {
        return (List<CompositePhenomenon>) getDistinctObjects(session, CompositePhenomenon.class);
    }

    public static List<SpatialRefSys> getSpatialReySysObjects(Session session) {
        return (List<SpatialRefSys>) getDistinctObjects(session, SpatialRefSys.class);
    }

    public static ResultTemplate getResultTemplateObject(String identifier, Session session) {
        Criteria criteria = session.createCriteria(ResultTemplate.class);
        criteria.add(getEqualRestriction(getIdentifierParameter(null), identifier));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (ResultTemplate) criteria.uniqueResult();
    }

    public static List<ResultTemplate> getResultTemplateObjects(Session session) {
        Criteria criteria = session.createCriteria(ResultTemplate.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (List<ResultTemplate>)criteria.list();
    }

    public static ResultTemplate getResultTemplateObject(String offering, String observedProperty, Session session) {
        Criteria criteria = session.createCriteria(ResultTemplate.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstAlias);
        String obsPropAlias = addObservablePropertyAliasToMap(aliases, obsConstAlias);
        criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering));
        criteria.add(getEqualRestriction(getIdentifierParameter(obsPropAlias), observedProperty));
        addAliasesToCriteria(criteria, aliases);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		/* there can be mutliple but equal result tamplates... */
		List<ResultTemplate> templates = (List<ResultTemplate>) criteria.list();
        return (templates.isEmpty()) ? null : templates.iterator().next();
    }

    public static List<ResultTemplate> getResultTemplateObject(String offering, String observedProperty,
            Collection<String> featureOfInterest, Session session) {
        Criteria criteria = session.createCriteria(ResultTemplate.class);
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = addObservationConstallationAliasToMap(aliases, null);
        String offeringAlias = addOfferingAliasToMap(aliases, obsConstAlias);
        String obsPropAlias = addObservablePropertyAliasToMap(aliases, obsConstAlias);
        criteria.add(getEqualRestriction(getIdentifierParameter(offeringAlias), offering));
        criteria.add(getEqualRestriction(getIdentifierParameter(obsPropAlias), observedProperty));
        if (featureOfInterest != null && !featureOfInterest.isEmpty()) {
            String foiAlias = addFeatureOfInterestAliasToMap(aliases, null);
            criteria.add(Restrictions.in(getIdentifierParameter(foiAlias), new ArrayList<String>(featureOfInterest)));
        }
        addAliasesToCriteria(criteria, aliases);
        return criteria.list();
    }
}
