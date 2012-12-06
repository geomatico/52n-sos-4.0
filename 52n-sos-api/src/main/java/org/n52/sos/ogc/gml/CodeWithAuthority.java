package org.n52.sos.ogc.gml;

public class CodeWithAuthority {

    private String value;
    
    private String codeSpace = "";

    public CodeWithAuthority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getCodeSpace() {
        return codeSpace;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setCodeSpace(String codeSpace) {
        this.codeSpace = codeSpace;
    }
    
    public boolean isSetValue() {
        return value != null && !value.isEmpty();
    }
    
}
