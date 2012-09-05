package org.n52.sos.encode;

import java.util.Set;

import org.apache.xmlbeans.XmlObject;

public interface IObservationEncoder<XmlObject, Object> extends IEncoder<XmlObject, Object> {
    
    public boolean isObservationAndMeasurmentV20Type();
    
    public boolean mergeObservationValuesWithSameParameters();
    
    public boolean isSupported();
    
    public void setSupported(boolean supportted);
    
    // TODO add javadoc
    public Set<String> getSupportedResponseFormats(String service, String version);

}
