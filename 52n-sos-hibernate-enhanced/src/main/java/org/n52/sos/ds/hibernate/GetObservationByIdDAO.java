/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetObservationByIdDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateResultUtilities;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservationCollection;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.GetObservationParams;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.SosGetObservationByIdRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetObservationByIdDAO implements IGetObservationByIdDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetObservationByIdDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservationById.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    public GetObservationByIdDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection) throws OwsExceptionReport {
        Session session = null;
        if (connection instanceof Session) {
            session = (Session) connection;
        } else {
            String exceptionText = "The parameter connection is not an Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(SosConstants.Operations.GetObservationById.name());
        // set DCP
        opsMeta.setDcp(SosHelper.getDCP(SosConstants.Operations.GetObservationById.name(), service, version, Configurator
                .getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set identifier
        opsMeta.addParameterValue(Sos2Constants.GetObservationByIdParams.observation.name(),
                HibernateCriteriaQueryUtilities.getObservationIdentifiers(session));
        return opsMeta;
    }

    @Override
    public SosObservationCollection getObservationById(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof SosGetObservationByIdRequest) {
            SosGetObservationByIdRequest sosRequest = (SosGetObservationByIdRequest) request;
            Session session = null;
            try {
                session = (Session) connectionProvider.getConnection();
                if (sosRequest.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                    throw Util4Exceptions.createMissingParameterValueException(GetObservationParams.observedProperty
                            .name());
                } else {
                    List<Observation> observations = queryObservation(sosRequest, session);
                    String responseFormat;
                    if (sosRequest.getResponseFormat() != null && !sosRequest.getResponseFormat().isEmpty()) {
                        responseFormat = sosRequest.getResponseFormat();
                    } else {
                        responseFormat = OMConstants.RESPONSE_FORMAT_OM_2;
                    }
                    return HibernateResultUtilities.createSosObservationCollectionFromObservations(
                            responseFormat, observations, sosRequest.getVersion(), session);
                }
            } catch (HibernateException he) {
                String exceptionText = "Error while querying observation data!";
                LOGGER.error(exceptionText, he);
                throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
            } finally {
                connectionProvider.returnConnection(session);
            }
        } else {
            String exceptionText = "The SOS request is not a SosGetObservationByIdRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    private List<Observation> queryObservation(SosGetObservationByIdRequest request, Session session) {
        Map<String, String> aliases = new HashMap<String, String>();
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateConstants.PARAMETER_IDENTIFIER, request.getObservationIdentifier()));
        List<Observation> observations =
                HibernateCriteriaQueryUtilities.getObservations(aliases, criterions, projections, session);
        return observations;
    }

}
