package org.n52.sos.response;

import org.n52.sos.ogc.ows.SosCapabilities;

public class GetCapabilitiesResponse extends AbstractServiceResponse {
    
    private SosCapabilities capabilities;

    public SosCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(SosCapabilities capabilities) {
        this.capabilities = capabilities;
    }
    
    

}
