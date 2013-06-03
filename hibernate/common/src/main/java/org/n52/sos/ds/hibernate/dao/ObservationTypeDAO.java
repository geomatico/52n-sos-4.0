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
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.ObservationType;

/**
 * Hibernate data access class for observation types
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class ObservationTypeDAO {

    /**
     * Get observation type objects for observation types
     * 
     * @param observationTypes
     *            Observation types
     * @param session
     *            Hibernate session
     * @return Observation type objects
     */
    @SuppressWarnings("unchecked")
    public List<ObservationType> getObservationTypeObjects(List<String> observationTypes, Session session) {
        return session.createCriteria(ObservationType.class)
                .add(Restrictions.in(ObservationType.OBSERVATION_TYPE, observationTypes)).list();
    }

    /**
     * Get observation type object for observation type
     * 
     * @param observationType
     * @param session
     *            Hibernate session
     * @return Observation type object
     */
    public ObservationType getObservationTypeObject(String observationType, Session session) {
        return (ObservationType) session.createCriteria(ObservationType.class)
                .add(Restrictions.eq(ObservationType.OBSERVATION_TYPE, observationType)).uniqueResult();
    }

    /**
     * Insert or/and get observation type object for observation type
     * 
     * @param observationType
     *            Observation type
     * @param session
     *            Hibernate session
     * @return Observation type object
     */
    public ObservationType getOrInsertObservationType(String observationType, Session session) {
        ObservationType hObservationType = getObservationTypeObject(observationType, session);
        if (hObservationType == null) {
            hObservationType = new ObservationType();
            hObservationType.setObservationType(observationType);
            session.save(hObservationType);
            session.flush();
        }
        return hObservationType;
    }

    /**
     * Insert or/and get observation type objects for observation types
     * 
     * @param observationTypes
     *            Observation types
     * @param session
     *            Hibernate session
     * @return Observation type objects
     */
    public List<ObservationType> getOrInsertObservationTypes(Set<String> observationTypes, Session session) {
        List<ObservationType> obsTypes = new LinkedList<ObservationType>();
        for (String observationType : observationTypes) {
            obsTypes.add(getOrInsertObservationType(observationType, session));
        }
        return obsTypes;
    }
}
