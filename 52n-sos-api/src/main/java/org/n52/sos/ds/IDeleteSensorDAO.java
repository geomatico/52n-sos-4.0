package org.n52.sos.ds;

import org.n52.sos.request.AbstractServiceRequest;

public interface IDeleteSensorDAO extends IOperationDAO {

    int deleteSensor(AbstractServiceRequest request);

}
