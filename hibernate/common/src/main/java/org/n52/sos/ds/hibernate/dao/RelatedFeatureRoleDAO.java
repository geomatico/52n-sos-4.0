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

import java.util.LinkedList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;

/**
 * Hibernate data access class for featureofInterest types
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class RelatedFeatureRoleDAO {

    /**
     * Get related feature role objects for role
     * 
     * @param role
     *            Related feature role
     * @param session
     *            Hibernate session
     * @return Related feature role objects
     */
    @SuppressWarnings("unchecked")
    public List<RelatedFeatureRole> getRelatedFeatureRole(String role, Session session) {
        return session.createCriteria(RelatedFeatureRole.class)
                .add(Restrictions.eq(RelatedFeatureRole.RELATED_FEATURE_ROLE, role)).list();
    }

    /**
     * Insert and get related feature role objects
     * 
     * @param role
     *            Related feature role
     * @param session
     *            Hibernate session
     * @return Related feature objects
     */
    public List<RelatedFeatureRole> getOrInsertRelatedFeatureRole(String role, Session session) {
        List<RelatedFeatureRole> relFeatRoles = new RelatedFeatureRoleDAO().getRelatedFeatureRole(role, session);
        if (relFeatRoles == null) {
            relFeatRoles = new LinkedList<RelatedFeatureRole>();
        }
        if (relFeatRoles.isEmpty()) {
            RelatedFeatureRole relFeatRole = new RelatedFeatureRole();
            relFeatRole.setRelatedFeatureRole(role);
            session.save(relFeatRole);
            session.flush();
            relFeatRoles.add(relFeatRole);
        }
        return relFeatRoles;
    }
}
