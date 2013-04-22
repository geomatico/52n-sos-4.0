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

import static org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities.setValidProcedureDescriptionEndTime;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.AbstractDeleteSensorDAO;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.ScrollableIterable;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.response.DeleteSensorResponse;

public class DeleteSensorDAO extends AbstractDeleteSensorDAO {
    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    
    public DeleteSensorDAO() {
        super(SosConstants.SOS);
    }

    @Override
    public synchronized DeleteSensorResponse deleteSensor(DeleteSensorRequest request) throws OwsExceptionReport {
        DeleteSensorResponse response = new DeleteSensorResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionHolder.getSession();
            transaction = session.beginTransaction();
            setDeleteSensorFlag(request.getProcedureIdentifier(), true, session);
            setValidProcedureDescriptionEndTime(request.getProcedureIdentifier(), session);
            transaction.commit();
            response.setDeletedProcedure(request.getProcedureIdentifier());
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while updateing deleted sensor flag data!");
        } finally {
            sessionHolder.returnSession(session);
        }
        return response;
    }

    private void setDeleteSensorFlag(String identifier, boolean deleteFlag, Session session)
            throws OwsExceptionReport {
        Procedure procedure = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(identifier, session);
        if (procedure != null) {
            procedure.setDeleted(deleteFlag);
            session.saveOrUpdate(procedure);
            session.flush();
            setObservationConstellationOfferingObservationTypeAsDeletedForProcedure(identifier, session);
            setObservationsAsDeletedForProcedure(identifier, session);
        } else {
            throw new NoApplicableCodeException().withMessage("The requested identifier is not contained in database");
        }
    }

    private void setObservationConstellationOfferingObservationTypeAsDeletedForProcedure(String procedure,
                                                                                         Session session) {
        @SuppressWarnings("unchecked")
        List<ObservationConstellation> hObservationConstellations = session
                .createCriteria(ObservationConstellation.class)
                .createCriteria(ObservationConstellation.PROCEDURE)
                .add(Restrictions.eq(Procedure.IDENTIFIER, procedure)).list();
        for (ObservationConstellation hObservationConstellation : hObservationConstellations) {
            hObservationConstellation.setDeleted(true);
            session.saveOrUpdate(hObservationConstellation);
            session.flush();
        }
    }

    private void setObservationsAsDeletedForProcedure(String procedure, Session session) {
        ScrollableIterable<Observation> scroll = ScrollableIterable.fromCriteria(session
                .createCriteria(Observation.class)
                .add(Restrictions.eq(Observation.DELETED, false))
                .createCriteria(Observation.PROCEDURE)
                .add(Restrictions.eq(Procedure.IDENTIFIER, procedure)));
        try {
            for (Observation o : scroll) {
                o.setDeleted(true);
                session.update(o);
                session.flush();
            }
        } finally {
            scroll.close();
        }
    }
}
