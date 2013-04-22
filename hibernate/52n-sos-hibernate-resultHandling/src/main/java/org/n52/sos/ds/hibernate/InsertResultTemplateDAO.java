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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.sos.ds.AbstractInsertResultTemplateDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateUtilities;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.InvalidObservationTypeException;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.InsertResultTemplateResponse;

public class InsertResultTemplateDAO extends AbstractInsertResultTemplateDAO {

    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    
    public InsertResultTemplateDAO() {
        super(SosConstants.SOS);
    }

    @Override
    public InsertResultTemplateResponse insertResultTemplate(InsertResultTemplateRequest request)
            throws OwsExceptionReport {
        InsertResultTemplateResponse response = new InsertResultTemplateResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        response.setAcceptedTemplate(request.getIdentifier());
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionHolder.getSession();
            transaction = session.beginTransaction();
            SosObservationConstellation sosObsConst = request.getObservationTemplate();
//            if (request.getResultStructure().getResultStructure() instanceof SosSweDataArray
//                    || request.getResultStructure().getResultStructure() instanceof SosSweDataRecord) {
//                sosObsConst.setObservationType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
//            } else {
//                String exceptionText = "The requested resultStructure is not supported!";
//                throw Util4Exceptions.createInvalidParameterValueException(
//                        Sos2Constants.InsertResultTemplateParams.observationType.name(), exceptionText);
//            }
            ObservationConstellation obsConst = null;
            for (String offeringID : sosObsConst.getOfferings()) {
                obsConst =
                HibernateUtilities.checkObservationConstellation(sosObsConst, offeringID,
                        session, Sos2Constants.InsertResultTemplateParams.proposedTemplate.name());
                if (obsConst != null) {
                    FeatureOfInterest feature =
                            HibernateUtilities.checkOrInsertFeatureOfInterest(sosObsConst.getFeatureOfInterest(),
                                    session);
                    HibernateUtilities.checkOrInsertFeatureOfInterestRelatedFeatureRelation(feature,
                            obsConst.getOffering(), session);
                    HibernateCriteriaTransactionalUtilities.checkOrInsertResultTemplate(request, obsConst, feature, session);
                } else {
                    // TODO make better exception.
                    throw new InvalidObservationTypeException(request.getObservationTemplate().getObservationType());
                }
            }
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Insert result template into database failed!");
        } catch (OwsExceptionReport owse) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw owse;
        } finally {
            sessionHolder.returnSession(session);
        }
        return response;
    }

}
