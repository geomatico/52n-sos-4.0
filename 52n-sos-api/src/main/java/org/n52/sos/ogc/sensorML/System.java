package org.n52.sos.ogc.sensorML;

import org.n52.sos.ogc.sensorML.elements.SosSMLPosition;

public class System extends AbstractMultiProcess {
    
    private SosSMLPosition position;
    

    
//    private List<ITime> timePositions;
//    
//    private List<String> interfaces;

    public SosSMLPosition getPosition() {
        return position;
    }

    public void setPosition(SosSMLPosition position) {
        this.position = position;
    }


//    public List<ITime> getTimePositions() {
//        return timePositions;
//    }
//
//    public void setTimePositions(List<ITime> timePositions) {
//        this.timePositions = timePositions;
//    }
//
//    public List<String> getInterfaces() {
//        return interfaces;
//    }
//
//    public void setInterfaces(List<String> interfaces) {
//        this.interfaces = interfaces;
//    }

}
