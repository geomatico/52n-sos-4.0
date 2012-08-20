package org.n52.sos.ds.hibernate.entities;

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Observation generated by hbm2java
 */
public class Observation implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long observationId;

    private FeatureOfInterest featureOfInterest;

    private ObservationConstellation observationConstellation;

    private String identifier;

    private Date phenomenonTimeStart;

    private Date phenomenonTimeEnd;

    private Date resultTime;

    private Date validTimeStart;

    private Date validTimeEnd;
    
    private Unit unit;

    private Integer antiSubsetting;

    private Set<SpatialFilteringProfile> spatialFilteringProfiles = new HashSet<SpatialFilteringProfile>(0);

    private Set<Quality> qualities = new HashSet<Quality>(0);

    private Set<CountValue> countValues = new HashSet<CountValue>(0);
    
    private Set<BooleanValue> booleanValues = new HashSet<BooleanValue>(0);

    private Set<GeometryValue> geometryValues = new HashSet<GeometryValue>(0);

    private Set<CategoryValue> categoryValues = new HashSet<CategoryValue>(0);

    private Set<NumericValue> numericValues = new HashSet<NumericValue>(0);

    private Set<TextValue> textValues = new HashSet<TextValue>(0);

    public Observation() {
    }

    public Observation(long observationId, FeatureOfInterest featureOfInterest,
            ObservationConstellation observationConstellation, Date phenomenonTimeStart) {
        this.observationId = observationId;
        this.featureOfInterest = featureOfInterest;
        this.observationConstellation = observationConstellation;
        this.phenomenonTimeStart = phenomenonTimeStart;
    }

    public Observation(long observationId, FeatureOfInterest featureOfInterest,
            ObservationConstellation observationConstellation, String identifier, Date phenomenonTimeStart,
            Date phenomenonTimeEnd, Date resultTime, Date validTimeStart, Date validTimeEnd, Unit unit, Integer antiSubsetting,
            Set<SpatialFilteringProfile> spatialFilteringProfiles, Set<Quality> qualities, Set<CountValue> countValues, Set<BooleanValue> booleanValues,Set<GeometryValue> geometryValues, Set<CategoryValue> categoryValues,
            Set<NumericValue> numericValues, Set<TextValue> textValues) {
        this.observationId = observationId;
        this.featureOfInterest = featureOfInterest;
        this.observationConstellation = observationConstellation;
        this.identifier = identifier;
        this.phenomenonTimeStart = phenomenonTimeStart;
        this.phenomenonTimeEnd = phenomenonTimeEnd;
        this.resultTime = resultTime;
        this.validTimeStart = validTimeStart;
        this.validTimeEnd = validTimeEnd;
        this.unit = unit;
        this.antiSubsetting = antiSubsetting;
        this.spatialFilteringProfiles = spatialFilteringProfiles;
        this.qualities = qualities;
        this.countValues = countValues;
        this.booleanValues = booleanValues;
        this.geometryValues = geometryValues;
        this.categoryValues = categoryValues;
        this.numericValues = numericValues;
        this.textValues = textValues;
    }

    public long getObservationId() {
        return this.observationId;
    }

    public void setObservationId(long observationId) {
        this.observationId = observationId;
    }

    public FeatureOfInterest getFeatureOfInterest() {
        return this.featureOfInterest;
    }

    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    public ObservationConstellation getObservationConstellation() {
        return this.observationConstellation;
    }

    public void setObservationConstellation(ObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Date getPhenomenonTimeStart() {
        return this.phenomenonTimeStart;
    }

    public void setPhenomenonTimeStart(Date phenomenonTimeStart) {
        this.phenomenonTimeStart = phenomenonTimeStart;
    }

    public Date getPhenomenonTimeEnd() {
        return this.phenomenonTimeEnd;
    }

    public void setPhenomenonTimeEnd(Date phenomenonTimeEnd) {
        this.phenomenonTimeEnd = phenomenonTimeEnd;
    }

    public Date getResultTime() {
        return this.resultTime;
    }

    public void setResultTime(Date resultTime) {
        this.resultTime = resultTime;
    }

    public Date getValidTimeStart() {
        return this.validTimeStart;
    }

    public void setValidTimeStart(Date validTimeStart) {
        this.validTimeStart = validTimeStart;
    }

    public Date getValidTimeEnd() {
        return this.validTimeEnd;
    }

    public void setValidTimeEnd(Date validTimeEnd) {
        this.validTimeEnd = validTimeEnd;
    }
    
    public Unit getUnit() {
        return this.unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Integer getAntiSubsetting() {
        return this.antiSubsetting;
    }

    public void setAntiSubsetting(Integer antiSubsetting) {
        this.antiSubsetting = antiSubsetting;
    }

    public Set<SpatialFilteringProfile> getSpatialFilteringProfiles() {
        return this.spatialFilteringProfiles;
    }

    public void setSpatialFilteringProfiles(Set<SpatialFilteringProfile> spatialFilteringProfiles) {
        this.spatialFilteringProfiles = spatialFilteringProfiles;
    }

    public Set<Quality> getQualities() {
        return this.qualities;
    }

    public void setQualities(Set<Quality> qualities) {
        this.qualities = qualities;
    }

    public Set<CountValue> getCountValues() {
        return this.countValues;
    }

    public void setCountValues(Set<CountValue> countValues) {
        this.countValues = countValues;
    }
    
    public Set<BooleanValue> getBooleanValues() {
        return this.booleanValues;
    }

    public void setBooleanValues(Set<BooleanValue> booleanValues) {
        this.booleanValues = booleanValues;
    }

    public Set<GeometryValue> getGeometryValues() {
        return this.geometryValues;
    }

    public void setGeometryValues(Set<GeometryValue> geometryValues) {
        this.geometryValues = geometryValues;
    }

    public Set<CategoryValue> getCategoryValues() {
        return this.categoryValues;
    }

    public void setCategoryValues(Set<CategoryValue> categoryValues) {
        this.categoryValues = categoryValues;
    }

    public Set<NumericValue> getNumericValues() {
        return this.numericValues;
    }

    public void setNumericValues(Set<NumericValue> numericValues) {
        this.numericValues = numericValues;
    }

    public Set<TextValue> getTextValues() {
        return this.textValues;
    }

    public void setTextValues(Set<TextValue> textValues) {
        this.textValues = textValues;
    }

}
