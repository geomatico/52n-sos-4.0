package org.n52.sos.ogc.om;

import java.util.List;

import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.om.quality.SosQuality;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.IValue;

public class SosSingleObservationValue implements IObservationValue {

    private ITime phenomenonTime;

    private IValue value;

    private List<SosQuality> qualityList;

    public SosSingleObservationValue() {
    }

    public SosSingleObservationValue(IValue value) {
        this.value = value;
    }

    public SosSingleObservationValue(ITime phenomenonTime, IValue value, List<SosQuality> qualityList) {
        this.phenomenonTime = phenomenonTime;
        this.value = value;
        this.qualityList = qualityList;
    }

    @Override
    public ITime getPhenomenonTime() {
        return phenomenonTime;
    }

    public IValue getValue() {
        return value;
    }

    @Override
    public void setValue(IValue value) {
        this.value = value;
    }

    public void setQualityList(List<SosQuality> qualityList) {
        this.qualityList = qualityList;
    }

    public List<SosQuality> getQualityList() {
        return qualityList;
    }

    public void setPhenomenonTime(ITime phenomenonTime) {
        this.phenomenonTime = phenomenonTime;
    }
}
