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

import java.util.Collections;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IUpdateSensorDescriptionDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.UpdateSensorRequest;
import org.n52.sos.response.UpdateSensorResponse;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateSensorDescriptionDAO extends AbstractHibernateOperationDao implements IUpdateSensorDescriptionDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSensorDescriptionDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.UpdateSensorDescription.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }
    
    @Override
    public DecoderKeyType getKeyTypeForDcp(String version) {
        return new DecoderKeyType(SWEConstants.NS_SWES_20);
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session) throws OwsExceptionReport {
        opsMeta.addPossibleValuesParameter(Sos2Constants.UpdateSensorDescriptionParams.procedure, getCache().getProcedures());
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            opsMeta.addPossibleValuesParameter(Sos2Constants.UpdateSensorDescriptionParams.procedureDescriptionFormat,
                    HibernateCriteriaQueryUtilities.getProcedureDescriptionFormatIdentifiers(session));
        }
        opsMeta.addAnyParameterValue(Sos2Constants.UpdateSensorDescriptionParams.description);
    }

    @Override
    public synchronized UpdateSensorResponse updateSensorDescription(UpdateSensorRequest request)
            throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
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
                        HibernateCriteriaQueryUtilities.getProcedureForIdentifier(request.getProcedureIdentifier(),
                                session);
                Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
                for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
                    if (validProcedureTime.getEndTime() == null) {
                        validProcedureTime.setEndTime(currentTime.toDate());
                        HibernateCriteriaTransactionalUtilities.updateValidProcedureTime(validProcedureTime, session);
                    }
                }
                HibernateCriteriaTransactionalUtilities.insertValidProcedureTime(procedure,
                        procedureDescription.getSensorDescriptionXmlString(), currentTime, session);
            }
            session.flush();
            transaction.commit();
            response.setUpdatedProcedure(request.getProcedureIdentifier());
            return response;
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            String exceptionText = "Error while processing data for UpdateSensorDescription document!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
    }

    private ITime getValidTimeForProcedure(SosProcedureDescription procedureDescription) {
        if (procedureDescription instanceof AbstractSensorML) {
            if (((AbstractSensorML) procedureDescription).getValidTime() != null) {
                return ((AbstractSensorML) procedureDescription).getValidTime();
            } else if (procedureDescription instanceof SensorML
                    && ((SensorML) procedureDescription).getMembers().size() == 1) {
                if (((SensorML) procedureDescription).getMembers().get(0).getValidTime() != null) {
                    return ((SensorML) procedureDescription).getMembers().get(0).getValidTime();
                }
            }
        }
        return new TimePeriod(new DateTime(), null);
    }
}
