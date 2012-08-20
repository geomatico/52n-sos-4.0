package org.n52.sos.ogc.sensorML;

public class AbstractSingleProcess extends AbstractProcess {

    private ProcessMethod method;

    public ProcessMethod getMethod() {
        return method;
    }

    public void setMethod(ProcessMethod method) {
        this.method = method;
    }

}
