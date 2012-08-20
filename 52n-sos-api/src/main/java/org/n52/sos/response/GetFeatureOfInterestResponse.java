package org.n52.sos.response;

import org.n52.sos.ogc.om.features.SosAbstractFeature;

public class GetFeatureOfInterestResponse extends AbstractServiceResponse {

    private SosAbstractFeature abstractFeature;

    public SosAbstractFeature getAbstractFeature() {
        return abstractFeature;
    }

    public void setAbstractFeature(SosAbstractFeature abstractFeature) {
        this.abstractFeature = abstractFeature;
    }

}
