package org.n52.sos.service.operator;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.response.IServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.Util4Exceptions;

public class SosServiceOperatorV20 implements IServiceOperator {
    
    private ServiceOperatorKeyType serviceOperatorKeyType;
    
    public SosServiceOperatorV20() {
        serviceOperatorKeyType = new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
    }
    

    @Override
    public IServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        IServiceResponse response = null;
            IRequestOperator requestOperator = Configurator.getInstance().getRequestOperator(serviceOperatorKeyType, request.getOperationName());
            if (requestOperator != null) {
                response = requestOperator.receiveRequest(request);
            }
        if (response != null) {
            return response;
        }
        throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
    }

    @Override
    public ServiceOperatorKeyType getServiceOperatorKeyType() {
        return serviceOperatorKeyType;
    }

}
