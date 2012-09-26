/**
 * Copyright (C) 2012
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
 * ObservationType generated by hbm2java
 */
public class ObservationType implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long observationTypeId;

    private String observationType;

    private Set<Procedure> procedures = new HashSet<Procedure>(0);
    
    private Set<Offering> offerings = new HashSet<Offering>(0);

    private Set<ObservationConstellation> observationConstellations = new HashSet<ObservationConstellation>(0);

    public ObservationType() {
    }

    public ObservationType(long observationTypeId, String observationType) {
        this.observationTypeId = observationTypeId;
        this.observationType = observationType;
    }

    public ObservationType(long observationTypeId, String observationType, Set<Procedure> procedures,
            Set<Offering> offerings, Set<ObservationConstellation> observationConstellations) {
        this.observationTypeId = observationTypeId;
        this.observationType = observationType;
        this.procedures = procedures;
        this.offerings = offerings;
        this.observationConstellations = observationConstellations;
    }

    public long getObservationTypeId() {
        return this.observationTypeId;
    }

    public void setObservationTypeId(long observationTypeId) {
        this.observationTypeId = observationTypeId;
    }

    public String getObservationType() {
        return this.observationType;
    }

    public void setObservationType(String observationType) {
        this.observationType = observationType;
    }

    public Set<Procedure> getProcedures() {
        return this.procedures;
    }

    public void setProcedures(Set<Procedure> procedures) {
        this.procedures = procedures;
    }
    
    public Set<Offering> getOfferings() {
        return this.offerings;
    }

    public void setOfferings(Set<Offering> offerings) {
        this.offerings = offerings;
    }

    public Set<ObservationConstellation> getObservationConstellations() {
        return this.observationConstellations;
    }

    public void setObservationConstellations(Set<ObservationConstellation> observationConstellations) {
        this.observationConstellations = observationConstellations;
    }

}
