package org.n52.sos.ogc.ows;

import java.util.Map;

import org.n52.sos.ogc.ows.OWSConstants.MinMax;

public class OWSParameterValueRange implements IOWSParameterValue {

    private String minValue;
    
    private String maxValue;

    public OWSParameterValueRange(String minValue, String maxValue) {
        super();
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public OWSParameterValueRange(Map<MinMax, String> minMaxMapFromEnvelope) {
        this.minValue = minMaxMapFromEnvelope.get(OWSConstants.MinMax.MIN);
        this.maxValue = minMaxMapFromEnvelope.get(OWSConstants.MinMax.MAX);
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }
    
    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

}
