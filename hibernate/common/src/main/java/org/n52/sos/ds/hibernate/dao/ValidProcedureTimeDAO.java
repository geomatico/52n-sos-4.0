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

import java.util.Set;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.TProcedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;

/**
 * Hibernate data access class for valid procedure time
 * 
 * @author CarstenHollmann
 * @since 4.0.0
 */
public class ValidProcedureTimeDAO {

    /**
     * Insert valid procedure time for procedrue
     * 
     * @param procedure
     *            Procedure object
     * @param xmlDescription
     *            Procedure XML description
     * @param validStartTime
     *            Valid start time
     * @param session
     *            Hibernate session
     */
    public void insertValidProcedureTime(Procedure procedure, String xmlDescription, DateTime validStartTime,
            Session session) {
        ValidProcedureTime vpd = new ValidProcedureTime();
        vpd.setProcedure(procedure);
        vpd.setDescriptionXml(xmlDescription);
        vpd.setStartTime(validStartTime.toDate());
        session.save(vpd);
        session.flush();
    }

    /**
     * Update valid procedure time object
     * 
     * @param validProcedureTime
     *            Valid procedure time object
     * @param session
     *            Hibernate session
     */
    public void updateValidProcedureTime(ValidProcedureTime validProcedureTime, Session session) {
        session.saveOrUpdate(validProcedureTime);
    }

    /**
     * Set valid end time to valid procedure time object for procedure
     * identifier
     * 
     * @param procedureIdentifier
     *            Procedure identifier
     * @param session
     *            Hibernate session
     */
    public void setValidProcedureDescriptionEndTime(String procedureIdentifier, Session session) {
        TProcedure procedure = new ProcedureDAO().getTProcedureForIdentifier(procedureIdentifier, session);
        Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
        for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
            if (validProcedureTime.getEndTime() == null) {
                validProcedureTime.setEndTime(new DateTime().toDate());
            }
        }
    }
}
