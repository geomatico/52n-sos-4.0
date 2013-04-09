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
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDeletedFlag;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationConstellations;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasValidProcedureTimes;

public class Procedure implements Serializable, HasIdentifier, HasObservationConstellations, HasDeletedFlag,
                                  HasProcedureDescriptionFormat, HasValidProcedureTimes {
    public static final String ID = "procedureId";
    public static final String PROCEDURES_FOR_CHILD_SENSOR_ID = "proceduresForChildSensorId";
    public static final String PROCEDURES_FOR_PARENT_SENSOR_ID = "proceduresForParentSensorId";
    private static final long serialVersionUID = -4982481368011439515L;
    private long procedureId;
    private ProcedureDescriptionFormat procedureDescriptionFormat;
    private String identifier;
    private boolean deleted;
    private Set<ValidProcedureTime> validProcedureTimes = new HashSet<ValidProcedureTime>(0);
    private Set<ObservationConstellation> observationConstellations = new HashSet<ObservationConstellation>(0);
    private Set<Procedure> proceduresForChildSensorId = new HashSet<Procedure>(0);
    private Set<Procedure> proceduresForParentSensorId = new HashSet<Procedure>(0);

    public Procedure() {
    }

    public long getProcedureId() {
        return this.procedureId;
    }

    public void setProcedureId(long procedureId) {
        this.procedureId = procedureId;
    }

    @Override
    public ProcedureDescriptionFormat getProcedureDescriptionFormat() {
        return this.procedureDescriptionFormat;
    }

    @Override
    public void setProcedureDescriptionFormat(ProcedureDescriptionFormat procedureDescriptionFormat) {
        this.procedureDescriptionFormat = procedureDescriptionFormat;
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
    public boolean isDeleted() {
        return this.deleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public Set<ValidProcedureTime> getValidProcedureTimes() {
        return this.validProcedureTimes;
    }

    @Override
    public void setValidProcedureTimes(Set<ValidProcedureTime> validProcedureTimes) {
        this.validProcedureTimes = validProcedureTimes;
    }

    @Override
    public Set<ObservationConstellation> getObservationConstellations() {
        return this.observationConstellations;
    }

    @Override
    public void setObservationConstellations(Set<ObservationConstellation> observationConstellations) {
        this.observationConstellations = observationConstellations;
    }

    public Set<Procedure> getProceduresForChildSensorId() {
        return this.proceduresForChildSensorId;
    }

    public void setProceduresForChildSensorId(Set<Procedure> proceduresForChildSensorId) {
        this.proceduresForChildSensorId = proceduresForChildSensorId;
    }

    public Set<Procedure> getProceduresForParentSensorId() {
        return this.proceduresForParentSensorId;
    }

    public void setProceduresForParentSensorId(Set<Procedure> proceduresForParentSensorId) {
        this.proceduresForParentSensorId = proceduresForParentSensorId;
    }
}
