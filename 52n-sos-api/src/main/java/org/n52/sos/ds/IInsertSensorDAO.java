package org.n52.sos.ds;

import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;

public interface IInsertSensorDAO extends IOperationDAO {

    InsertSensorResponse insertSensor(InsertSensorRequest insertSensorRequest);

}
