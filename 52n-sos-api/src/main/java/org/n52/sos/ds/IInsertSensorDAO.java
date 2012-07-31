package org.n52.sos.ds;

import org.n52.sos.ogc.sensorML.SosSensorML;
import org.n52.sos.request.SosInsertSensorRequest;

public interface IInsertSensorDAO extends IOperationDAO {

    String insertSensor(SosInsertSensorRequest insertSensorRequest);

}
