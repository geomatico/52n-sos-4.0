package org.n52.sos.response;

import java.util.List;

import org.n52.sos.ogc.om.SosObservation;

public class GetObservationByIdResponse extends AbstractServiceResponse {

    private String responseFormat;

    private List<SosObservation> observationCollection;
    
    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseFormat) {
        this.responseFormat = responseFormat;
    }

    public List<SosObservation> getObservationCollection() {
        return observationCollection;
    }

    public void setObservationCollection(List<SosObservation> observationCollection) {
       this.observationCollection = observationCollection;
    }

}
