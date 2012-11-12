/**
 * Copyright (C) 2012
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

import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IInsertResultTemplateDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateUtilities;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertResultTemplateDAO implements IInsertResultTemplateDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultTemplateDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertResultTemplate.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public InsertResultTemplateDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        Session session = null;
        if (connection instanceof Session) {
            session = (Session) connection;
        } else {
            String exceptionText = "The parameter connection is not an Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        // get DCP
        DecoderKeyType dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        Map<String, List<String>> dcpMap =
                SosHelper.getDCP(OPERATION_NAME, dkt, Configurator.getInstance().getBindingOperators().values(),
                        Configurator.getInstance().getServiceURL());
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
            // set DCP
            opsMeta.setDcp(dcpMap);
            // TODO set parameter
            return opsMeta;
        }
        return null;
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
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
            session = (Session) connectionProvider.getConnection();
            transaction = session.beginTransaction();
            SosObservationConstellation sosObsConst = request.getObservationConstellation();
            ObservationConstellation obsConst = null;
            for (String offeringID : sosObsConst.getOfferings()) {
                if (request.getResultStructure().getResultStructure() instanceof SosSweDataArray
                        || request.getResultStructure().getResultStructure() instanceof SosSweDataRecord) {
                    sosObsConst.setObservationType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
                } else {
                    String exceptionText = "The requested resultStructure is not supported!";
                    throw Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertResultTemplateParams.observationType.name(), exceptionText);
                }
                obsConst =
                        HibernateUtilities.checkObservationConstellationForObservation(sosObsConst, offeringID,
                                session);
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
            String exceptionText = "!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
        return response;
    }

}
