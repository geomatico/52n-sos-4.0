/**
 * Copyright (C) 2013 by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk 52 North Initiative for Geospatial Open Source Software GmbH Martin-Luther-King-Weg 24 48155
 * Muenster, Germany info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under the terms of the GNU General Public
 * License version 2 as published by the Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied WARRANTY OF MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program (see gnu-gpl v2.txt). If
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or visit
 * the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.sos.ds.hibernate.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDescription;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservableProperties;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasRequests;

public class CompositePhenomenon implements Serializable, HasIdentifier, HasDescription,
                                            HasObservableProperties, HasRequests {
    public static final String ID = "compositePhenomenonId";
    private static final long serialVersionUID = 8115870640822922168L;
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

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Set<Request> getRequests() {
        return this.requests;
    }

    @Override
    public void setRequests(Set<Request> requests) {
        this.requests = requests;
    }

    @Override
    public Set<ObservableProperty> getObservableProperties() {
        return this.observableProperties;
    }

    @Override
    public void setObservableProperties(Set<ObservableProperty> observableProperties) {
        this.observableProperties = observableProperties;
    }
}
