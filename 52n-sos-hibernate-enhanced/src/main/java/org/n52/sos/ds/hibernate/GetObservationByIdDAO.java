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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IGetObservationByIdDAO;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateObservationUtilities;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.GetObservationParams;
import org.n52.sos.request.GetObservationByIdRequest;
import org.n52.sos.response.GetObservationByIdResponse;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetObservationByIdDAO extends AbstractHibernateOperationDao implements IGetObservationByIdDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GetObservationByIdDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservationById.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Session session)
            throws OwsExceptionReport {
        Map<String, List<String>> dcpMap = getDCP(new DecoderKeyType(version.equals(Sos1Constants.SERVICEVERSION) 
                                                    ? Sos1Constants.NS_SOS : Sos2Constants.NS_SOS_20));
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
            // set DCP
            opsMeta.setDcp(dcpMap);
            // set identifier
            opsMeta.addParameterValue(
                    Sos2Constants.GetObservationByIdParams.observation.name(),
                    new OWSParameterValuePossibleValues(getCache().getObservationIdentifiers()));
            return opsMeta;
        }
        return null;
    }

    @Override
    public GetObservationByIdResponse getObservationById(GetObservationByIdRequest request) throws OwsExceptionReport {
        if (request instanceof GetObservationByIdRequest) {
            Session session = null;
            try {
                session = getSession();
                if (request.getVersion().equals(Sos1Constants.SERVICEVERSION)) {
                    throw Util4Exceptions.createMissingParameterValueException(GetObservationParams.observedProperty
                            .name());
                } else {
                    List<Observation> observations = queryObservation(request, session);
                    GetObservationByIdResponse response = new GetObservationByIdResponse();
                    response.setService(request.getService());
                    response.setVersion(request.getVersion());
                    response.setResponseFormat(request.getResponseFormat());
                    response.setObservationCollection(HibernateObservationUtilities.createSosObservationsFromObservations(
                            observations, request, session));
                    return response;
                }
            } catch (HibernateException he) {
                String exceptionText = "Error while querying observation data!";
                LOGGER.error(exceptionText, he);
                throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
            } finally {
                returnSession(session);
            }
        } else {
            String exceptionText = "The SOS request is not a SosGetObservationByIdRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    private List<Observation> queryObservation(GetObservationByIdRequest request, Session session) {
        Map<String, String> aliases = new HashMap<String, String>();
        List<Criterion> criterions = new ArrayList<Criterion>();
        List<Projection> projections = new ArrayList<Projection>();
        criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
                HibernateConstants.PARAMETER_IDENTIFIER, request.getObservationIdentifier()));
        criterions.add(HibernateCriteriaQueryUtilities.getDisjunctionCriterionForStringList(
        		HibernateConstants.PARAMETER_ANTI_SUBSETTING, request.getObservationIdentifier()));
        List<Criterion> disjunctedCriterions = new ArrayList<Criterion>(1);
        disjunctedCriterions.add(HibernateCriteriaQueryUtilities.getDisjunctionFor(criterions));
        List<Observation> observations =
                HibernateCriteriaQueryUtilities.getObservations(aliases, disjunctedCriterions, projections, session);
        return observations;
    }
}
