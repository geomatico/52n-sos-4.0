package org.n52.sos.service.operator;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;

public class SosServiceOperatorV20 implements IServiceOperator {
    
    private ServiceOperatorKeyType serviceOperatorKeyType;
    
    public SosServiceOperatorV20() {
        serviceOperatorKeyType = new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
    }
    

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        ServiceResponse response = null;
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
