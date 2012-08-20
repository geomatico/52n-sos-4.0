package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IUpdateSensorDescriptionDAO;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.UpdateSensorRequest;
import org.n52.sos.response.UpdateSensorResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateSensorDescriptionDAO implements IUpdateSensorDescriptionDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSensorDescriptionDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.UpdateSensorDescription.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public UpdateSensorDescriptionDAO() {
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

        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(OPERATION_NAME);
        // set DCP
        DecoderKeyType dkt = new DecoderKeyType(SWEConstants.NS_SWES_20);
        opsMeta.setDcp(SosHelper.getDCP(OPERATION_NAME, dkt,
                Configurator.getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set param procedure
        opsMeta.addParameterValue(Sos2Constants.UpdateSensorDescriptionParams.procedure.name(), Configurator
                .getInstance().getCapabilitiesCacheController().getProcedures());
        // set param procedureDescriptionFormat
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            List<String> outputFormatValues = new ArrayList<String>(1);
            outputFormatValues.add(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
            opsMeta.addParameterValue(Sos2Constants.UpdateSensorDescriptionParams.procedureDescriptionFormat.name(),
                    outputFormatValues);
        }
        // set param description
        opsMeta.addParameterValue(Sos2Constants.UpdateSensorDescriptionParams.description.name(),
                new ArrayList<String>(1));
        return opsMeta;
    }

    @Override
    public synchronized UpdateSensorResponse updateSensorDescription(UpdateSensorRequest request) throws OwsExceptionReport {
        Session session = null;
        Transaction transaction = null;
        try {
            session = (Session) connectionProvider.getConnection();
            transaction = session.beginTransaction();
            UpdateSensorResponse response = new UpdateSensorResponse();
            response.setService(request.getService());
            response.setVersion(request.getVersion());
            for (SosProcedureDescription procedureDescription : request.getProcedureDescriptions()) {
                DateTime currentTime = new DateTime();
                // TODO: check for all validTimes of descriptions for this identifier
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
                HibernateCriteriaTransactionalUtilities.insertValidProcedureTime(procedure, request.getProcedureXmlDescription(), currentTime, session);
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
            connectionProvider.returnConnection(session);
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

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

}
