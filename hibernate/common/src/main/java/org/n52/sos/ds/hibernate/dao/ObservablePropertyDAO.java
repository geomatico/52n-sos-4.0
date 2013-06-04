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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ogc.om.OmObservableProperty;

/**
 * Hibernate data access class for observable properties
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class ObservablePropertyDAO {

    /**
     * Get observable property objects for observable property identifiers
     * 
     * @param identifiers
     *            Observable property identifiers
     * @param session
     *            Hibernate session
     * @return Observable property objects
     */
    @SuppressWarnings("unchecked")
    public List<ObservableProperty> getObservableProperties(List<String> identifiers, Session session) {
        return session.createCriteria(ObservableProperty.class)
                .add(Restrictions.in(ObservableProperty.IDENTIFIER, identifiers)).list();
    }

    /**
     * Get observable property identifiers for offering identifier
     * 
     * @param offeringIdentifier
     *            Offering identifier
     * @param session
     *            Hibernate session
     * @return Observable property identifiers
     */
    @SuppressWarnings("unchecked")
    public List<String> getObservablePropertyIdentifiersForOffering(String offeringIdentifier, Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.OBSERVABLE_PROPERTY).setProjection(
                Projections.distinct(Projections.property(ObservableProperty.IDENTIFIER)));
        c.createCriteria(Observation.OFFERINGS).add(Restrictions.eq(Offering.IDENTIFIER, offeringIdentifier));
        return c.list();
    }

    /**
     * Get observable property identifiers for procedure identifier
     * 
     * @param procedureIdentifier
     *            Procedure identifier
     * @param session
     *            Hibernate session
     * @return Observable property identifiers
     */
    @SuppressWarnings("unchecked")
    public List<String> getObservablePropertyIdentifiersForProcedure(String procedureIdentifier, Session session) {
        Criteria c = session.createCriteria(Observation.class).add(Restrictions.eq(Observation.DELETED, false));
        c.createCriteria(Observation.OBSERVABLE_PROPERTY).setProjection(
                Projections.distinct(Projections.property(ObservableProperty.IDENTIFIER)));
        c.createCriteria(Observation.PROCEDURE).add(Restrictions.eq(Procedure.IDENTIFIER, procedureIdentifier));
        return c.list();
    }

    /**
     * Get all observable property objects
     * 
     * @param session
     *            Hibernate session
     * @return Observable property objects
     */
    @SuppressWarnings("unchecked")
    public List<ObservableProperty> getObservablePropertyObjects(Session session) {
        return session.createCriteria(ObservableProperty.class).list();
    }

    /**
     * Insert and/or get observable property objects for SOS observable
     * properties
     * 
     * @param observableProperty
     *            SOS observable properties
     * @param session
     *            Hibernate session
     * @return Observable property objects
     */
    public List<ObservableProperty> getOrInsertObservableProperty(List<OmObservableProperty> observableProperty,
            Session session) {
        List<String> identifiers = new ArrayList<String>(observableProperty.size());
        for (OmObservableProperty sosObservableProperty : observableProperty) {
            identifiers.add(sosObservableProperty.getIdentifier());
        }
        List<ObservableProperty> obsProps = new ObservablePropertyDAO().getObservableProperties(identifiers, session);
        for (OmObservableProperty sosObsProp : observableProperty) {
            boolean exists = false;
            for (ObservableProperty obsProp : obsProps) {
                if (obsProp.getIdentifier().equals(sosObsProp.getIdentifier())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ObservableProperty obsProp = new ObservableProperty();
                obsProp.setIdentifier(sosObsProp.getIdentifier());
                obsProp.setDescription(sosObsProp.getDescription());
                session.save(obsProp);
                session.flush();
                session.refresh(obsProp);
                obsProps.add(obsProp);
            }
        }
        return obsProps;
    }

}
