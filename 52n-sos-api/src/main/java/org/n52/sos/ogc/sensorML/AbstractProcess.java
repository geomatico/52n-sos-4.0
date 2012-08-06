package org.n52.sos.ogc.sensorML;

import java.util.List;

import org.n52.sos.ogc.sensorML.elements.SosSMLIo;

public class AbstractProcess extends AbstractSensorML {
    
    private List<String> descriptions;
    
    private List<String> names;
    
    private List<SosSMLIo> inputs;
    
    private List<SosSMLIo> outputs;
    
    private List<String> parameters;

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public List<SosSMLIo> getInputs() {
        return inputs;
    }

    public void setInputs(List<SosSMLIo> inputs) {
        this.inputs = inputs;
    }

    public List<SosSMLIo> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<SosSMLIo> outputs) {
        this.outputs = outputs;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

}
