package org.n52.sos.ogc.sos;

import org.n52.sos.ogc.om.SosOffering;

public abstract class SosProcedureDescription {
    
    public abstract String getProcedureIdentifier();

    public abstract SosOffering getOfferingIdentifier();
}