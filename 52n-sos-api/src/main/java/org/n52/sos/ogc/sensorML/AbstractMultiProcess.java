package org.n52.sos.ogc.sensorML;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.sensorML.elements.SosSMLComponent;

public class AbstractMultiProcess extends AbstractProcess {

    
    private List<SosSMLComponent> components;
    
    public List<SosSMLComponent> getComponents() {
        return components;
    }

    public void setComponents(List<SosSMLComponent> components) {
        this.components = components;
    }
    
    public void addComponents(List<SosSMLComponent> components) {
        if (this.components == null) {
            this.components = new ArrayList<SosSMLComponent>();
        }
        this.components.addAll(components);
    }
    
    public void addComponents(SosSMLComponent component) {
        if (components == null) {
            components = new ArrayList<SosSMLComponent>();
        }
        components.add(component);
    }
}
