package org.n52.sos.ogc.om;

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.quality.SosQuality;

public class SosSingleObservationValue implements IObservationValue<Object> {
    
    private ITime phenomenonTime;
    
    private Object value;
    
    private List<SosQuality> qualityList;

    public SosSingleObservationValue(ITime phenomenonTime, Object value, List<SosQuality> qualityList) {
       this.phenomenonTime = phenomenonTime;
       this.value = value;
       this.qualityList = qualityList;
    }

    @Override
    public ITime getPhenomenonTime() {
        return phenomenonTime;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public void setValues(Object value) {
        this.value = value;
    }
    
    public void setQualityList(List<SosQuality> qualityList) {
        this.qualityList = qualityList;
    }

    public List<SosQuality> getQualityList() {
        return qualityList;
    }
}
