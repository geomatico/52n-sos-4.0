package org.n52.sos.ogc.sensorML;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;

public class AbstractSingleProcess extends AbstractProcess {
    
    private ProcessMethod method;

    public ProcessMethod getMethod() {
        return method;
    }

    public void setMethod(ProcessMethod method) {
        this.method = method;
    }
    
}
