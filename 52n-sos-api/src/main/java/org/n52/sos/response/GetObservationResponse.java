package org.n52.sos.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.sos.ogc.om.SosObservation;

public class GetObservationResponse extends AbstractServiceResponse {

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

    public Collection<SosObservation> mergeObservations(boolean mergeObservationValuesWithSameParameters) {
        Collection<SosObservation> combinedObsCol = new ArrayList<SosObservation>();
        int obsIdCounter = 1;
        for (SosObservation sosObservation : observationCollection) {
            if (combinedObsCol.isEmpty()) {
                sosObservation.setObservationID(Integer.toString(obsIdCounter++));
                combinedObsCol.add(sosObservation);
            } else {
                boolean combined = false;
                for (SosObservation combinedSosObs : combinedObsCol) {
                    if (mergeObservationValuesWithSameParameters) {
                        if (combinedSosObs.getObservationConstellation().equals(
                                sosObservation.getObservationConstellation())) {
                            combinedSosObs.mergeWithObservation(sosObservation, false);
                            combined = true;
                            break;
                        }
                    } else {
                        if (combinedSosObs.getObservationConstellation().equalsExcludingObsProp(
                                sosObservation.getObservationConstellation())) {
                            combinedSosObs.mergeWithObservation(sosObservation, true);
                            combined = true;
                            break;
                        }
                    }
                }
                if (!combined) {
                    combinedObsCol.add(sosObservation);
                }
            }
        }
        return combinedObsCol;
    }

}
