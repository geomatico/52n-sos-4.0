package org.n52.sos.ogc.ows;

import org.apache.xmlbeans.XmlObject;

public class SosServiceProvider {
    
    private XmlObject serviceProvider;

    public XmlObject getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(XmlObject serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

}
