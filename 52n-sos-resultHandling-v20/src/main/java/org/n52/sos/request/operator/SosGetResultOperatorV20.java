package org.n52.sos.request.operator;

import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.IInsertResultDAO;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosGetResultOperatorV20 implements IRequestOperator {
    
    /** the data access object for the DescribeSensor operation */
    private IInsertResultDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = SosConstants.Operations.GetResult.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertResultOperatorV20.class);

    public SosGetResultOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IInsertResultDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("{} initialized successfully!", this.getClass().getSimpleName());
    }


    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        if (hasImplementedDAO()) {
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/resultRetrieval");
        }
        return conformanceClasses;
    }

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasImplementedDAO() {
        if (this.dao != null) {
            return true;
        }
        return false;
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    @Override
    public OWSOperation getOperationMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        return dao.getExtension(connection);
    }

}
