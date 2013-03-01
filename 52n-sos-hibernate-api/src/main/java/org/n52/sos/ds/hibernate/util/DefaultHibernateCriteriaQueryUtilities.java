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

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.HibernateQueryObject;

public class DefaultHibernateCriteriaQueryUtilities {

    public static List<?> getObjectList(HibernateQueryObject queryObject, Session session, Class<?> objectClass) {
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
            for (Projection projection : queryObject.getProjections()) {
                projectionList.add(projection);
            }
            criteria.setProjection(projectionList);
        }
        if (queryObject.isSetOrder()) {
            for (Order order : queryObject.getOrder()) {
                criteria.addOrder(order);
            }
        }
        if (queryObject.isSetMaxResults()) {
            criteria.setMaxResults(queryObject.getMaxResult());
        }
        if (queryObject.isSetResultTransformer()) {
            criteria.setResultTransformer(queryObject.getResultTransformer());
        } else {
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }
        return criteria.list();
    }
    
    protected static Object getObject(HibernateQueryObject queryObject, Session session, Class<?> objectClass) {
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
            for (Projection projection : queryObject.getProjections()) {
                projectionList.add(projection);
            }
            criteria.setProjection(projectionList);
        }
        if (queryObject.isSetOrder()) {
            for (Order order : queryObject.getOrder()) {
                criteria.addOrder(order);
            }
        }
        if (queryObject.isSetMaxResults()) {
            criteria.setMaxResults(queryObject.getMaxResult());
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.uniqueResult();
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
    protected static void addAliasToMap(Map<String, String> aliases, String prefix, String parameter, String alias) {
        if (prefix != null && !prefix.isEmpty()) {
            aliases.put(prefix + "." + parameter, alias);
        } else {
            aliases.put(parameter, alias);
        }
    }
}
