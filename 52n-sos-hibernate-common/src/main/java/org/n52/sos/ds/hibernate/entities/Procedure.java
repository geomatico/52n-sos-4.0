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

// Generated 10.07.2012 15:18:23 by Hibernate Tools 3.4.0.CR1

import java.util.HashSet;
import java.util.Set;

/**
 * Procedure generated by hbm2java
 */
public class Procedure implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long procedureId;

    private ProcedureDescriptionFormat procedureDescriptionFormat;

    private String identifier;

    private boolean deleted;

    private Set<ValidProcedureTime> validProcedureTimes = new HashSet<ValidProcedureTime>(0);

    private Set<FeatureOfInterestType> featureOfInterestTypes = new HashSet<FeatureOfInterestType>(0);

    private Set<ObservationConstellation> observationConstellations = new HashSet<ObservationConstellation>(0);

    private Set<ObservationType> observationTypes = new HashSet<ObservationType>(0);

    private Set<Procedure> proceduresForChildSensorId = new HashSet<Procedure>(0);

//    private Set<ObservationTemplate> observationTemplates = new HashSet<ObservationTemplate>(0);

    private Set<Procedure> proceduresForParentSensorId = new HashSet<Procedure>(0);

    public Procedure() {
    }

//    public Procedure(long procedureId, ProcedureDescriptionFormat procedureDescriptionFormat, String identifier,
//            boolean deleted) {
//        this.procedureId = procedureId;
//        this.procedureDescriptionFormat = procedureDescriptionFormat;
//        this.identifier = identifier;
//        this.deleted = deleted;
//    }
//
//    public Procedure(long procedureId, ProcedureDescriptionFormat procedureDescriptionFormat, String identifier,
//            boolean deleted, Set<ValidProcedureTime> validProcedureTimes, Set<FeatureOfInterestType> featureOfInterestTypes, Set<ObservationConstellation> observationConstellations,
//            Set<ObservationType> observationTypes, Set<Procedure> proceduresForChildSensorId, Set<ObservationTemplate> observationTemplates,
//            Set<Procedure> proceduresForParentSensorId) {
//        this.procedureId = procedureId;
//        this.procedureDescriptionFormat = procedureDescriptionFormat;
//        this.identifier = identifier;
//        this.deleted = deleted;
//        this.validProcedureTimes = validProcedureTimes;
//        this.featureOfInterestTypes = featureOfInterestTypes;
//        this.observationConstellations = observationConstellations;
//        this.observationTypes = observationTypes;
//        this.proceduresForChildSensorId = proceduresForChildSensorId;
//        this.observationTemplates = observationTemplates;
//        this.proceduresForParentSensorId = proceduresForParentSensorId;
//    }

    public long getProcedureId() {
        return this.procedureId;
    }

    public void setProcedureId(long procedureId) {
        this.procedureId = procedureId;
    }

    public ProcedureDescriptionFormat getProcedureDescriptionFormat() {
        return this.procedureDescriptionFormat;
    }

    public void setProcedureDescriptionFormat(ProcedureDescriptionFormat procedureDescriptionFormat) {
        this.procedureDescriptionFormat = procedureDescriptionFormat;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Set<ValidProcedureTime> getValidProcedureTimes() {
        return this.validProcedureTimes;
    }

    public void setValidProcedureTimes(Set<ValidProcedureTime> validProcedureTimes) {
        this.validProcedureTimes = validProcedureTimes;
    }

    public Set<ObservationConstellation> getObservationConstellations() {
        return this.observationConstellations;
    }

    public void setObservationConstellations(Set<ObservationConstellation> observationConstellations) {
        this.observationConstellations = observationConstellations;
    }

    public Set<Procedure> getProceduresForChildSensorId() {
        return this.proceduresForChildSensorId;
    }

    public void setProceduresForChildSensorId(Set<Procedure> proceduresForChildSensorId) {
        this.proceduresForChildSensorId = proceduresForChildSensorId;
    }

//    public Set<ObservationTemplate> getObservationTemplates() {
//        return this.observationTemplates;
//    }
//
//    public void setObservationTemplates(Set<ObservationTemplate> observationTemplates) {
//        this.observationTemplates = observationTemplates;
//    }

    public Set<Procedure> getProceduresForParentSensorId() {
        return this.proceduresForParentSensorId;
    }

    public void setProceduresForParentSensorId(Set<Procedure> proceduresForParentSensorId) {
        this.proceduresForParentSensorId = proceduresForParentSensorId;
    }

}
