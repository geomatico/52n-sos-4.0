package org.n52.sos.ds;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.UpdateSensorRequest;
import org.n52.sos.response.UpdateSensorResponse;

public interface IUpdateSensorDescriptionDAO extends IOperationDAO {
    
   public UpdateSensorResponse updateSensorDescription(UpdateSensorRequest updateSensorRequest) throws OwsExceptionReport;

}
