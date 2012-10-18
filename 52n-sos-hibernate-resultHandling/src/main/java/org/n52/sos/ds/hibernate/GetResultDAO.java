package org.n52.sos.ds.hibernate;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IGetResultDAO;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetResultDAO implements IGetResultDAO {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.GetResult.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

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
    public List<SosObservation> getResult(AbstractServiceRequest request) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

}
