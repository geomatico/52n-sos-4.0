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
 * CompositePhenomenon generated by hbm2java
 */
public class CompositePhenomenon implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long compositePhenomenonId;

    private String identifier;

    private String description;

    private Set<Request> requests = new HashSet<Request>(0);

    private Set<ObservableProperty> observableProperties = new HashSet<ObservableProperty>(0);

    public CompositePhenomenon() {
    }

    public CompositePhenomenon(long compositePhenomenonId, String identifier) {
        this.compositePhenomenonId = compositePhenomenonId;
        this.identifier = identifier;
    }

    public CompositePhenomenon(long compositePhenomenonId, String identifier, String description, Set<Request> requests,
            Set<ObservableProperty> observableProperties) {
        this.compositePhenomenonId = compositePhenomenonId;
        this.identifier = identifier;
        this.description = description;
        this.requests = requests;
        this.observableProperties = observableProperties;
    }

    public long getCompositePhenomenonId() {
        return this.compositePhenomenonId;
    }

    public void setCompositePhenomenonId(long compositePhenomenonId) {
        this.compositePhenomenonId = compositePhenomenonId;
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

    public Set<Request> getRequests() {
        return this.requests;
    }

    public void setRequests(Set<Request> requests) {
        this.requests = requests;
    }

    public Set<ObservableProperty> getObservableProperties() {
        return this.observableProperties;
    }

    public void setObservableProperties(Set<ObservableProperty> observableProperties) {
        this.observableProperties = observableProperties;
    }

}