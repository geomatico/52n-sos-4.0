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

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.Unit;

/**
 * Hibernate data access class for unit
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class UnitDAO {

    /**
     * Get unit object for unit
     * 
     * @param unit
     *            Unit
     * @param session
     *            Hibernate session
     * @return Unit object
     */
    public Unit getUnit(String unit, Session session) {
        return (Unit) session.createCriteria(Unit.class).add(Restrictions.eq(Unit.UNIT, unit)).uniqueResult();
    }

    /**
     * Insert and get unit object
     * 
     * @param unit
     *            Unit
     * @param session
     *            Hibernate session
     * @return Unit object
     */
    public Unit getOrInsertUnit(String unit, Session session) {
        Unit result = getUnit(unit, session);
        if (result == null) {
            result = new Unit();
            result.setUnit(unit);
            session.save(result);
            session.flush();
            session.refresh(result);
        }
        return result;
    }
}
