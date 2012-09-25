package org.n52.sos.ds.hibernate.entities;

// Generated 19.09.2012 15:09:37 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

/**
 * Offering generated by hbm2java
 */
public class Offering implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private long offeringId;

    private String identifier;

    private String name;

    private Set<ObservationConstellation> observationConstellations = new HashSet<ObservationConstellation>(0);

    private Set<Request> requests = new HashSet<Request>(0);

    private Set<ObservationType> observationTypes = new HashSet<ObservationType>(0);

    private Set<RelatedFeature> relatedFeatures = new HashSet<RelatedFeature>(0);

    public Offering() {
    }

    public Offering(long offeringId, String identifier) {
        this.offeringId = offeringId;
        this.identifier = identifier;
    }

    public Offering(long offeringId, String identifier, String name,
            Set<ObservationConstellation> observationConstellations, Set<Request> requests,
            Set<ObservationType> observationTypes, Set<RelatedFeature> relatedFeatures) {
        this.offeringId = offeringId;
        this.identifier = identifier;
        this.name = name;
        this.observationConstellations = observationConstellations;
        this.requests = requests;
        this.observationTypes = observationTypes;
        this.relatedFeatures = relatedFeatures;
    }

    public long getOfferingId() {
        return this.offeringId;
    }

    public void setOfferingId(long offeringId) {
        this.offeringId = offeringId;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ObservationConstellation> getObservationConstellations() {
        return this.observationConstellations;
    }

    public void setObservationConstellations(Set<ObservationConstellation> observationConstellations) {
        this.observationConstellations = observationConstellations;
    }

    public Set<Request> getRequests() {
        return this.requests;
    }

    public void setRequests(Set<Request> requests) {
        this.requests = requests;
    }

    public Set<ObservationType> getObservationTypes() {
        return this.observationTypes;
    }

    public void setObservationTypes(Set<ObservationType> observationTypes) {
        this.observationTypes = observationTypes;
    }

    public Set<RelatedFeature> getRelatedFeatures() {
        return this.relatedFeatures;
    }

    public void setRelatedFeatures(Set<RelatedFeature> relatedFeatures) {
        this.relatedFeatures = relatedFeatures;
    }

}
