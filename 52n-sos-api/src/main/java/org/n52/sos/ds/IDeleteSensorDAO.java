package org.n52.sos.ds;

import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DeleteSensorRequest;
import org.n52.sos.response.DeleteSensorResponse;

public interface IDeleteSensorDAO extends IOperationDAO {

    DeleteSensorResponse deleteSensor(DeleteSensorRequest request);

}
