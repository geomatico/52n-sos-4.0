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
package org.n52.sos.ds.hibernate;

import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.ds.AbstractUpdateSensorDescriptionDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
import org.n52.sos.ds.hibernate.dao.ValidProcedureTimeDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.TProcedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.request.UpdateSensorRequest;
import org.n52.sos.response.UpdateSensorResponse;

public class UpdateSensorDescriptionDAO extends AbstractUpdateSensorDescriptionDAO {

    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    
    public UpdateSensorDescriptionDAO() {
        super(SosConstants.SOS);
    }

    @Override
    public synchronized UpdateSensorResponse updateSensorDescription(UpdateSensorRequest request)
            throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionHolder.getSession();
            transaction = session.beginTransaction();
            UpdateSensorResponse response = new UpdateSensorResponse();
            response.setService(request.getService());
            response.setVersion(request.getVersion());
            for (SosProcedureDescription procedureDescription : request.getProcedureDescriptions()) {
                DateTime currentTime = new DateTime();
                // TODO: check for all validTimes of descriptions for this
                // identifier
                // ITime validTime =
                // getValidTimeForProcedure(procedureDescription);
                Procedure procedure =
                        new ProcedureDAO().getProcedureForIdentifier(request.getProcedureIdentifier(),
                                session);
                if (procedure instanceof TProcedure) {
                    Set<ValidProcedureTime> validProcedureTimes = ((TProcedure)procedure).getValidProcedureTimes();
                    ValidProcedureTimeDAO validProcedureTimeDAO = new ValidProcedureTimeDAO();
                    for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
                        if (validProcedureTime.getEndTime() == null) {
                            validProcedureTime.setEndTime(currentTime.toDate());
                            validProcedureTimeDAO.updateValidProcedureTime(validProcedureTime, session);
                        }
                    }
                    validProcedureTimeDAO.insertValidProcedureTime(procedure,
                            procedureDescription.getSensorDescriptionXmlString(), currentTime, session);
                }
            }
            session.flush();
            transaction.commit();
            response.setUpdatedProcedure(request.getProcedureIdentifier());
            return response;
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while processing data for UpdateSensorDescription document!");
        } finally {
            sessionHolder.returnSession(session);
        }
    }

//    private ITime getValidTimeForProcedure(SosProcedureDescription procedureDescription) {
//        if (procedureDescription instanceof AbstractSensorML) {
//            if (((AbstractSensorML) procedureDescription).getValidTime() != null) {
//                return ((AbstractSensorML) procedureDescription).getValidTime();
//            } else if (procedureDescription instanceof SensorML
//                    && ((SensorML) procedureDescription).getMembers().size() == 1) {
//                if (((SensorML) procedureDescription).getMembers().get(0).getValidTime() != null) {
//                    return ((SensorML) procedureDescription).getMembers().get(0).getValidTime();
//                }
//            }
//        }
//        return new TimePeriod(new DateTime(), null);
//    }
}
