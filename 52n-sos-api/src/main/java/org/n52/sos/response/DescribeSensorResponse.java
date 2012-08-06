package org.n52.sos.response;

import org.n52.sos.ogc.sensorML.AbstractSensorML;
import org.n52.sos.ogc.sensorML.SensorML;

public class DescribeSensorResponse extends AbstractServiceResponse {
    
    private String outputFormat;
    
    private AbstractSensorML sensorML;

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public AbstractSensorML getSensorDescription() {
        return sensorML;
    }

    public void setSensorDescription(AbstractSensorML sensorML) {
        this.sensorML = sensorML;
    }

}
