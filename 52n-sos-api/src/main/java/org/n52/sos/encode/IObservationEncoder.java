package org.n52.sos.encode;

import org.apache.xmlbeans.XmlObject;

public interface IObservationEncoder<XmlObject, Object> extends IEncoder<XmlObject, Object> {
    
    public boolean isObservationAndMeasurmentV20Type();
    
    public boolean mergeObservationValuesWithSameParameters();

}
