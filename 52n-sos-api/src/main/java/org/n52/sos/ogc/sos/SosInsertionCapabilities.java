package org.n52.sos.ogc.sos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ogc.ows.IExtension;

public class SosInsertionCapabilities implements IExtension {

    private Set<String> featureOfInterestTypes;

    private Set<String> observationTypes;

    private Set<String> procedureDescriptionFormats;

    private Set<String> supportedEncodings;

    public Set<String> getFeatureOfInterestTypes() {
        return featureOfInterestTypes;
    }

    public Set<String> getObservationTypes() {
        return observationTypes;
    }

    public Set<String> getProcedureDescriptionFormats() {
        return procedureDescriptionFormats;
    }

    public Set<String> getSupportedEncodings() {
        return supportedEncodings;
    }

    public void addFeatureOfInterestTypes(Collection<String> featureOfInterestTypes) {
        if (this.featureOfInterestTypes == null) {
            this.featureOfInterestTypes = new HashSet<String>(0);
        }
        this.featureOfInterestTypes.addAll(featureOfInterestTypes);
    }

    public void addObservationTypes(Collection<String> observationTypes) {
        if (this.observationTypes == null) {
            this.observationTypes = new HashSet<String>(0);
        }
        this.observationTypes.addAll(observationTypes);
    }

    public void addProcedureDescriptionFormats(Collection<String> procedureDescriptionFormats) {
        if (this.procedureDescriptionFormats == null) {
            this.procedureDescriptionFormats = new HashSet<String>(0);
        }
        this.procedureDescriptionFormats.addAll(procedureDescriptionFormats);
    }

    public void addSupportedEncodings(Collection<String> supportedEncodings) {
        if (this.supportedEncodings == null) {
            this.supportedEncodings = new HashSet<String>(0);
        }
        this.supportedEncodings.addAll(supportedEncodings);
    }

    public void addInsertionCapabilities(SosInsertionCapabilities insertionCapabilities) {
        if (insertionCapabilities.getFeatureOfInterestTypes() != null) {
            addFeatureOfInterestTypes(insertionCapabilities.getFeatureOfInterestTypes());
        }
        if (insertionCapabilities.getObservationTypes() != null) {
            addObservationTypes(insertionCapabilities.getObservationTypes());
        }
        if (insertionCapabilities.getProcedureDescriptionFormats() != null) {
            addProcedureDescriptionFormats(insertionCapabilities.getProcedureDescriptionFormats());
        }
        if (insertionCapabilities.getSupportedEncodings() != null) {
            addSupportedEncodings(insertionCapabilities.getSupportedEncodings());
        }

    }

}
