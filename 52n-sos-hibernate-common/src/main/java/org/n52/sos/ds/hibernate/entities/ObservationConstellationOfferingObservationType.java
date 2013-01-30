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
// default package
// Generated 28.01.2013 14:55:02 by Hibernate Tools 4.0.0

import java.util.HashSet;
import java.util.Set;

/**
 * ObservationConstellationOfferingObservationType generated by hbm2java
 */
public class ObservationConstellationOfferingObservationType implements java.io.Serializable {

    private long observationConstellationOfferingObservationTypeId;

    private ObservationType observationType;

    private ObservationConstellation observationConstellation;

    private Offering offering;

//    private Set resultTemplates = new HashSet(0);
//
//    private Set observations = new HashSet(0);

    public ObservationConstellationOfferingObservationType() {
    }

//    public ObservationConstellationOfferingObservationType(long observationConstellationOfferingObservationTypeId,
//            Offering offering) {
//        this.observationConstellationOfferingObservationTypeId = observationConstellationOfferingObservationTypeId;
//        this.offering = offering;
//    }
//
//    public ObservationConstellationOfferingObservationType(long observationConstellationOfferingObservationTypeId,
//            ObservationType observationType, ObservationConstellation observationConstellation, Offering offering,
//            Set resultTemplates, Set observations) {
//        this.observationConstellationOfferingObservationTypeId = observationConstellationOfferingObservationTypeId;
//        this.observationType = observationType;
//        this.observationConstellation = observationConstellation;
//        this.offering = offering;
//        this.resultTemplates = resultTemplates;
//        this.observations = observations;
//    }

    public long getObservationConstellationOfferingObservationTypeId() {
        return this.observationConstellationOfferingObservationTypeId;
    }

    public void setObservationConstellationOfferingObservationTypeId(
            long observationConstellationOfferingObservationTypeId) {
        this.observationConstellationOfferingObservationTypeId = observationConstellationOfferingObservationTypeId;
    }

    public ObservationType getObservationType() {
        return this.observationType;
    }

    public void setObservationType(ObservationType observationType) {
        this.observationType = observationType;
    }

    public ObservationConstellation getObservationConstellation() {
        return this.observationConstellation;
    }

    public void setObservationConstellation(ObservationConstellation observationConstellation) {
        this.observationConstellation = observationConstellation;
    }

    public Offering getOffering() {
        return this.offering;
    }

    public void setOffering(Offering offering) {
        this.offering = offering;
    }

//    public Set getResultTemplates() {
//        return this.resultTemplates;
//    }
//
//    public void setResultTemplates(Set resultTemplates) {
//        this.resultTemplates = resultTemplates;
//    }
//
//    public Set getObservations() {
//        return this.observations;
//    }
//
//    public void setObservations(Set observations) {
//        this.observations = observations;
//    }

}
