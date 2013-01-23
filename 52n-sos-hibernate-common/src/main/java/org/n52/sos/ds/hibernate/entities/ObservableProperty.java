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
 * ObservableProperty generated by hbm2java
 */
public class ObservableProperty implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long observablePropertyId;

    private String identifier;

    private String description;

//    private Set<Request> requests = new HashSet<Request>(0);
//
//    private Set<CompositePhenomenon> compositePhenomenons = new HashSet<CompositePhenomenon>(0);

    private Set<ObservationConstellation> observationConstellations = new HashSet<ObservationConstellation>(0);

    public ObservableProperty() {
    }

//    public ObservableProperty(long observablePropertyId,String identifier) {
//        this.observablePropertyId = observablePropertyId;
//        this.identifier = identifier;
//    }
//
//    public ObservableProperty(long observablePropertyId, String identifier,
//            String description, Set<Request> requests, Set<CompositePhenomenon> compositePhenomenons, Set<ObservationConstellation> observationConstellations) {
//        this.observablePropertyId = observablePropertyId;
//        this.identifier = identifier;
//        this.description = description;
//        this.requests = requests;
//        this.compositePhenomenons = compositePhenomenons;
//        this.observationConstellations = observationConstellations;
//    }

    public long getObservablePropertyId() {
        return this.observablePropertyId;
    }

    public void setObservablePropertyId(long observablePropertyId) {
        this.observablePropertyId = observablePropertyId;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public Set<Request> getRequests() {
//        return this.requests;
//    }
//
//    public void setRequests(Set<Request> requests) {
//        this.requests = requests;
//    }
//
//    public Set<CompositePhenomenon> getCompositePhenomenons() {
//        return this.compositePhenomenons;
//    }
//
//    public void setCompositePhenomenons(Set<CompositePhenomenon> compositePhenomenons) {
//        this.compositePhenomenons = compositePhenomenons;
//    }

    public Set<ObservationConstellation> getObservationConstellations() {
        return this.observationConstellations;
    }

    public void setObservationConstellations(Set<ObservationConstellation> observationConstellations) {
        this.observationConstellations = observationConstellations;
    }

}
