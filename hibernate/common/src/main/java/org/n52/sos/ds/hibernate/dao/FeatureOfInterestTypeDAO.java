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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ogc.OGCConstants;

/**
 * Hibernate data access class for featureofInterest types
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class FeatureOfInterestTypeDAO {

    /**
     * Get all featureOfInterest types
     * 
     * @param session
     *            Hibernate session
     * @return All featureOfInterest types
     */
    @SuppressWarnings("unchecked")
    public List<String> getFeatureOfInterestTypes(Session session) {
        return session
                .createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.ne(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, OGCConstants.UNKNOWN))
                .setProjection(
                        Projections.distinct(Projections.property(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE)))
                .list();
    }

    /**
     * Get featureOfInterest type object for featureOfInterest type
     * 
     * @param featureOfInterestType
     *            FeatureOfInterest type
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest type object
     */
    public FeatureOfInterestType getFeatureOfInterestTypeObject(String featureOfInterestType, Session session) {
        return (FeatureOfInterestType) session.createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.eq(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, featureOfInterestType))
                .uniqueResult();
    }

    /**
     * Get featureOfInterest type objects for featureOfInterest types
     * 
     * @param featureOfInterestType
     *            FeatureOfInterest types
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest type objects
     */
    @SuppressWarnings("unchecked")
    public List<FeatureOfInterestType> getFeatureOfInterestTypeObjects(List<String> featureOfInterestType,
            Session session) {
        return session.createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.in(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, featureOfInterestType)).list();
    }

    /**
     * Get featureOfInterest type objects for featureOfInterest identifiers
     * 
     * @param featureOfInterestIdentifiers
     *            FeatureOfInterest identifiers
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest type objects
     */
    @SuppressWarnings("unchecked")
    public List<String> getFeatureOfInterestTypesForFeatureOfInterest(Collection<String> featureOfInterestIdentifiers,
            Session session) {
        return session
                .createCriteria(FeatureOfInterest.class)
                .add(Restrictions.in(FeatureOfInterest.IDENTIFIER, featureOfInterestIdentifiers))
                .createCriteria(FeatureOfInterest.FEATURE_OF_INTEREST_TYPE)
                .setProjection(
                        Projections.distinct(Projections.property(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE)))
                .list();
    }

    /**
     * Insert and/or get featureOfInterest type object for featureOfInterest
     * type
     * 
     * @param featureType
     *            FeatureOfInterest type
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest type object
     */
    public FeatureOfInterestType getOrInsertFeatureOfInterestType(String featureType, Session session) {
        FeatureOfInterestType featureOfInterestType =
                new FeatureOfInterestTypeDAO().getFeatureOfInterestTypeObject(featureType, session);
        if (featureOfInterestType == null) {
            featureOfInterestType = new FeatureOfInterestType();
            featureOfInterestType.setFeatureOfInterestType(featureType);
            session.save(featureOfInterestType);
            session.flush();
        }
        return featureOfInterestType;
    }

    /**
     * Insert and/or get featureOfInterest type objects for featureOfInterest
     * types
     * 
     * @param featureOfInterestTypes
     *            FeatureOfInterest types
     * @param session
     *            Hibernate session
     * @return FeatureOfInterest type objects
     */
    public List<FeatureOfInterestType> getOrInsertFeatureOfInterestTypes(Set<String> featureOfInterestTypes,
            Session session) {
        List<FeatureOfInterestType> featureTypes = new LinkedList<FeatureOfInterestType>();
        for (String featureType : featureOfInterestTypes) {
            featureTypes.add(getOrInsertFeatureOfInterestType(featureType, session));
        }
        return featureTypes;
    }

}
