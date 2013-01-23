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
import org.n52.sos.ds.IInsertResultTemplateDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateUtilities;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertResultTemplateDAO extends AbstractHibernateOperationDao implements IInsertResultTemplateDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultTemplateDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertResultTemplate.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session) throws OwsExceptionReport {
        /* nothing to add here */
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
            session = getSession();
            transaction = session.beginTransaction();
            SosObservationConstellation sosObsConst = request.getObservationConstellation();
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
                        HibernateUtilities.checkObservationConstellationForObservation(sosObsConst, offeringID,
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
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The observationType is not supported!");
                    throw Util4Exceptions.createInvalidParameterValueException("observationType",
                            exceptionText.toString());
                }
            }
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            String exceptionText = "Insert result template into database failed!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
        return response;
    }

}
