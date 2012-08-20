package org.n52.sos.response;

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

    // /**
    // * Merge two observations
    // *
    // * @param sosObservation
    // * observation to add
    // */
    // public void mergeWithObservation(SosObservation sosObservation) {
    // // create compPhen or add obsProp to compPhen
    // if (this.observationConstellation.getObservableProperty() instanceof
    // SosObservableProperty) {
    // List<SosObservableProperty> obsProps = new
    // ArrayList<SosObservableProperty>();
    // obsProps.add((SosObservableProperty)
    // this.observationConstellation.getObservableProperty());
    // obsProps.add((SosObservableProperty)
    // sosObservation.getObservationConstellation().getObservableProperty());
    // SosCompositePhenomenon sosCompPhen =
    // new SosCompositePhenomenon("CompositePhenomenon_" +
    // this.observationConstellation.getProcedure(),
    // null, obsProps);
    // this.observationConstellation.setObservableProperty(sosCompPhen);
    // } else if (this.observationConstellation.getObservableProperty()
    // instanceof SosCompositePhenomenon) {
    // SosCompositePhenomenon sosCompPhen =
    // (SosCompositePhenomenon)
    // this.observationConstellation.getObservableProperty();
    // sosCompPhen.getPhenomenonComponents().add(
    // (SosObservableProperty)
    // sosObservation.getObservationConstellation().getObservableProperty());
    // }
    // // add values
    // for (String phenID : sosObservation.getValues().keySet()) {
    // this.values.put(phenID, sosObservation.getValues().get(phenID));
    //
    // }
    // }

    // /**
    // * Merge SosObservations with the same procedure id.
    // *
    // * @param observationMembers
    // * SosObservations list
    // * @return merged SosObservations as list
    // */
    // public static Collection<SosObservation>
    // mergeObservationsForGenericObservation(
    // Collection<SosObservation> observationMembers) {
    // Collection<SosObservation> combinedObsCol = new
    // ArrayList<SosObservation>();
    // for (SosObservation sosObservation : observationMembers) {
    // if (combinedObsCol.isEmpty()) {
    // combinedObsCol.add(sosObservation);
    // } else {
    // boolean combined = false;
    // for (SosObservation combinedSosObs : combinedObsCol) {
    // if (combinedSosObs.getObservationConstellation().equalsExcludingObsProp(
    // sosObservation.getObservationConstellation())) {
    // combinedSosObs.mergeWithObservation(sosObservation);
    // combined = true;
    // break;
    // }
    // }
    // if (!combined) {
    // combinedObsCol.add(sosObservation);
    // }
    // }
    // }
    // return combinedObsCol;
    // }

}
