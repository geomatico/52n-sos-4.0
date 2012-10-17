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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IInsertObservationDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateUtilities;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterDataType;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertObservationDAO implements IInsertObservationDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertObservationDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.InsertObservation.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public InsertObservationDAO() {
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
        DecoderKeyType dkt = null;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        }
        Map<String, List<String>> dcpMap = SosHelper.getDCP(OPERATION_NAME, dkt,
                Configurator.getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL());
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
    
            // set DCP
            opsMeta.setDcp(dcpMap);
            // set offering
            opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.offering.name(),
                    new OWSParameterValuePossibleValues(Configurator.getInstance().getCapabilitiesCacheController()
                            .getOfferings()));
            // set observation
            opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.observation.name(),
                    new OWSParameterValuePossibleValues(new ArrayList<String>(0)));
            opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.observation.name(), new OWSParameterDataType(
                    "http://schemas.opengis.net/om/2.0/observation.xsd#OM_Observation"));
            return opsMeta;
        }
        return null;
    }

    @Override
    public synchronized InsertObservationResponse insertObservation(InsertObservationRequest request)
            throws OwsExceptionReport {
        InsertObservationResponse response = new InsertObservationResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        Session session = null;
        Transaction transaction = null;
        // TODO: check unit and set if available and not defined in DB
        try {
            session = (Session) connectionProvider.getConnection();
            transaction = session.beginTransaction();
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
            for (SosObservation observation : request.getObservation()) {
                SosObservationConstellation sosObsConst = observation.getObservationConstellation();
                ObservationConstellation obsConst = null;
                for (String offeringID : sosObsConst.getOfferings()) {
                    try {
                        obsConst =
                                HibernateUtilities.checkObservationConstellationForObservation(sosObsConst, offeringID, session);
                    } catch (OwsExceptionReport owse) {
                        exceptions.add(owse);
                    }
                    if (obsConst != null) {
                        FeatureOfInterest feature =
                                HibernateUtilities.checkOrInsertFeatureOfInterest(observation.getObservationConstellation()
                                        .getFeatureOfInterest(), session);
                        HibernateUtilities.checkOrInsertFeatureOfInterestRelatedFeatureRelation(feature, obsConst.getOffering(), session);
                        if (observation.getValue() instanceof SosSingleObservationValue) {
                            HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(obsConst, feature, observation, session);
                        } else if (observation.getValue() instanceof SosMultiObservationValues) {
                            HibernateCriteriaTransactionalUtilities.insertObservationMutliValue(obsConst, feature, observation);
                        }
                    }
                }
            }
            // if no observationConstellation is valid, throw exception
            if (exceptions.size() == request.getObservation().size()) {
                Util4Exceptions.mergeAndThrowExceptions(exceptions);
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            String exceptionText = "Error while inserting new observation!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
        // TODO: ... all the DS insertion stuff
        // Requirement 68
        // proc/obsProp/Offering same obsType;

        return response;
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }
}
