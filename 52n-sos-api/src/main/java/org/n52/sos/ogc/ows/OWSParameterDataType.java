package org.n52.sos.ogc.ows;

public class OWSParameterDataType implements IOWSParameterValue {
    
    private String reference;

    public OWSParameterDataType(String reference) {
        super();
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

}
