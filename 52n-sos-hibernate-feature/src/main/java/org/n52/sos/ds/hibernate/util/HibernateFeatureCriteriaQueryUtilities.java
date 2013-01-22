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

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.spatial.criterion.SpatialProjections;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for Hibernate Criteria for the feature handling
 * 
 */
public class HibernateFeatureCriteriaQueryUtilities {

    /**
     * Get FeatureOfInterest objects for the defined restrictions
     * 
     * @param aliases
     *            Aliases for query between tables
     * @param criterions
     *            Restriction for the query
     * @param projections
     *            Projections for the query
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest objects
     */
    public static List<FeatureOfInterest> getFeatureOfInterests(HibernateQueryObject queryObject, Session session) {
        return (List<FeatureOfInterest>) HibernateCriteriaQueryUtilities.getObjectList(queryObject,
                session, FeatureOfInterest.class);
    }

    /**
     * Get the extent of FOIs
     * 
     * @param featureIDs
     *            FOIs to get extent from
     * @param session
     *            Hibernate session
     * @return Extent of FOIs
     */
    public static Geometry getEnvelopeForFeatureOfInterestIdentifiers(List<String> featureIDs, Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterest.class);
        criteria.add(Restrictions.in(HibernateConstants.PARAMETER_IDENTIFIER, featureIDs));
        criteria.setProjection(SpatialProjections.extent(HibernateConstants.PARAMETER_GEOMETRY));
        Geometry geometry = (Geometry) criteria.uniqueResult();
        return geometry;
    }

    public static FeatureOfInterest getFeatureOfInterest(String identifier, Geometry geometry, Session session) {
        Criteria criteria = session.createCriteria(FeatureOfInterest.class);
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_IDENTIFIER, identifier));
        disjunction.add(SpatialRestrictions.eq(HibernateConstants.PARAMETER_GEOMETRY, geometry));
        criteria.add(disjunction);
        return (FeatureOfInterest)criteria.uniqueResult();
    }

    public static List<String> getFeatureOfInterestIdentifier(HibernateQueryObject queryObject, Session session) {
        queryObject.addProjection(HibernateCriteriaQueryUtilities.getDistinctProjection(HibernateCriteriaQueryUtilities.getIdentifierParameter(null)));
        return (List<String>) getObjectList(queryObject, session, FeatureOfInterest.class);
    }
    
    
    
    protected static List<?> getObjectList(HibernateQueryObject queryObject, Session session, Class<?> objectClass) {
        Criteria criteria = session.createCriteria(objectClass);
        if (queryObject.isSetAliases()) {
            addAliasesToCriteria(criteria, queryObject.getAliases());
        }
        if (queryObject.isSetCriterions()) {
            Conjunction conjunction = Restrictions.conjunction();
            for (Criterion criterion : queryObject.getCriterions()) {
                conjunction.add(criterion);
            }
            criteria.add(conjunction);
        }
        if (queryObject.isSetProjections()) {
                ProjectionList projectionList = Projections.projectionList();
                for (Projection projection : queryObject.getProjections())
                {
                        projectionList.add(projection);
                }
                criteria.setProjection(projectionList);
        }
        if (queryObject.isSetOrder()) {
            criteria.addOrder(queryObject.getOrder());
        }
        if (queryObject.isSetMaxResults()) {
            criteria.setMaxResults(queryObject.getMaxResult());
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
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
}
