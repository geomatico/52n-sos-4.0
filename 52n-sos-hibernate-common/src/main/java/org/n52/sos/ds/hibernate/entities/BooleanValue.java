package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

/**
 * BooleanValue generated by hbm2java
 */
public class BooleanValue implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private long booleanValueId;

    private boolean value;

    private Set<Observation> observations = new HashSet<Observation>(0);

    public BooleanValue() {
    }

    public BooleanValue(long booleanValueId, boolean value) {
        this.booleanValueId = booleanValueId;
        this.value = value;
    }

    public BooleanValue(long booleanValueId, boolean value, Set<Observation> observations) {
        this.booleanValueId = booleanValueId;
        this.value = value;
        this.observations = observations;
    }

    public long getBooleanValueId() {
        return this.booleanValueId;
    }

    public void setBooleanValueId(long booleanValueId) {
        this.booleanValueId = booleanValueId;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public Set<Observation> getObservations() {
        return this.observations;
    }

    public void setObservations(Set<Observation> observations) {
        this.observations = observations;
    }

}
