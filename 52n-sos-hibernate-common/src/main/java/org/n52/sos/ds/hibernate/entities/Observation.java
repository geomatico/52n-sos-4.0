/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.ds.hibernate.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCodespace;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDeletedFlag;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationConstellation;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationConstellationOfferingObservationTypes;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasQualities;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasSpatialFilteringProfiles;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasUnit;

public class Observation implements Serializable, HasIdentifier, HasDeletedFlag, HasFeatureOfInterest,
                                    HasObservationConstellation, HasCodespace, HasUnit, HasSpatialFilteringProfiles,
                                    HasObservationConstellationOfferingObservationTypes, HasQualities {
    public static final String ID = "observationId";
    public static final String PHENOMENON_TIME_START = "phenomenonTimeStart";
    public static final String PHENOMENON_TIME_END = "phenomenonTimeEnd";
    public static final String VALID_TIME_START = "validTimeStart";
    public static final String VALID_TIME_END = "validTimeEnd";
    public static final String RESULT_TIME = "resultTime";
    public static final String SET_ID = "setId";
    private static final long serialVersionUID = 4419764404575493525L;
    private long observationId;
    private Boolean deleted;
    private FeatureOfInterest featureOfInterest;
    private ObservationConstellation observationConstellation;
    private String identifier;
    private Codespace codespace;
    private Date phenomenonTimeStart;
    private Date phenomenonTimeEnd;
    private Date resultTime;
    private Date validTimeStart;
    private Date validTimeEnd;
    private Unit unit;
    private String setId;
    private Set<ObservationConstellationOfferingObservationType> observationConstellationOfferingObservationTypes =
                                                                 new HashSet<ObservationConstellationOfferingObservationType>(0);
    private Set<SpatialFilteringProfile> spatialFilteringProfiles;
    private Set<Quality> qualities = new HashSet<Quality>(0);

    public Observation() {
    }

    public long getObservationId() {
        return this.observationId;
    }

    public void setObservationId(long observationId) {
        this.observationId = observationId;
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return this.featureOfInterest;
    }

    @Override
    public void setFeatureOfInterest(FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public ObservationConstellation getObservationConstellation() {
        return this.observationConstellation;
    }

    @Override
    public void setObservationConstellation(ObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Codespace getCodespace() {
        return this.codespace;
    }

    @Override
    public void setCodespace(Codespace codespace) {
        this.codespace = codespace;
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

    @Override
    public Unit getUnit() {
        return this.unit;
    }

    @Override
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getSetId() {
        return this.setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    @Override
    public Set<ObservationConstellationOfferingObservationType> getObservationConstellationOfferingObservationTypes() {
        return this.observationConstellationOfferingObservationTypes;
    }

    @Override
    public void setObservationConstellationOfferingObservationTypes(
            Set<ObservationConstellationOfferingObservationType> observationConstellationOfferingObservationTypes) {
        this.observationConstellationOfferingObservationTypes = observationConstellationOfferingObservationTypes;
    }

    @Override
    public Set<SpatialFilteringProfile> getSpatialFilteringProfiles() {
        return this.spatialFilteringProfiles;
    }

    @Override
    public void setSpatialFilteringProfiles(Set<SpatialFilteringProfile> spatialFilteringProfiles) {
        this.spatialFilteringProfiles = spatialFilteringProfiles;
    }

    @Override
    public Set<Quality> getQualities() {
        return this.qualities;
    }

    @Override
    public void setQualities(Set<Quality> qualities) {
        this.qualities = qualities;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isSetIdentifier() {
        return getIdentifier() != null && !getIdentifier().isEmpty();
    }

    public boolean isSetCodespace() {
        return getCodespace() != null && getCodespace().isSetCodespace();
    }
}
