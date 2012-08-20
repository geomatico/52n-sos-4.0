package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

/**
 * Quality generated by hbm2java
 */
public class Quality implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long qualityId;

    private SweType sweType;

    private Unit unit;

    private String name;

    private String value;

    private Set<Observation> observations = new HashSet<Observation>(0);

    public Quality() {
    }

    public Quality(long qualityId, SweType sweType, Unit unit, String name, String value) {
        this.qualityId = qualityId;
        this.sweType = sweType;
        this.unit = unit;
        this.name = name;
        this.value = value;
    }

    public Quality(long qualityId, SweType sweType, Unit unit, String name, String value, Set<Observation> observations) {
        this.qualityId = qualityId;
        this.sweType = sweType;
        this.unit = unit;
        this.name = name;
        this.value = value;
        this.observations = observations;
    }

    public long getQualityId() {
        return this.qualityId;
    }

    public void setQualityId(long qualityId) {
        this.qualityId = qualityId;
    }

    public SweType getSweType() {
        return this.sweType;
    }

    public void setSweType(SweType sweType) {
        this.sweType = sweType;
    }

    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<Observation> getObservations() {
        return this.observations;
    }

    public void setObservations(Set<Observation> observations) {
        this.observations = observations;
    }

}
