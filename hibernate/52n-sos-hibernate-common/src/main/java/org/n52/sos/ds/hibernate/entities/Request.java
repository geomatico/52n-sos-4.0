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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCompositePhenomenons;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservableProperties;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationTemplates;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOffering;

public class Request implements Serializable, HasOffering, HasObservableProperties, HasCompositePhenomenons,
                                HasObservationTemplates {
    public static final String ID = "requestId";
    public static final String REQUEST = "request";
    public static final String BEGIN_LEASE = "beginLease";
    public static final String END_LEASE = "endLease";
    private static final long serialVersionUID = 8504087916169001184L;
    private long requestId;
    private Offering offering;
    private String request;
    private Date beginLease;
    private Date endLease;
    private Set<ObservableProperty> observableProperties = new HashSet<ObservableProperty>(0);
    private Set<ObservationTemplate> observationTemplates = new HashSet<ObservationTemplate>(0);
    private Set<CompositePhenomenon> compositePhenomenons = new HashSet<CompositePhenomenon>(0);

    public Request() {
    }

    public Request(long requestId, Offering offering, String request, Date endLease) {
        this.requestId = requestId;
        this.offering = offering;
        this.request = request;
        this.endLease = endLease;
    }

    public Request(long requestId, Offering offering, String request, Date beginLease, Date endLease,
                   Set<ObservableProperty> observableProperties, Set<ObservationTemplate> observationTemplates,
                   Set<CompositePhenomenon> compositePhenomenons) {
        this.requestId = requestId;
        this.offering = offering;
        this.request = request;
        this.beginLease = beginLease;
        this.endLease = endLease;
        this.observableProperties = observableProperties;
        this.observationTemplates = observationTemplates;
        this.compositePhenomenons = compositePhenomenons;
    }

    public long getRequestId() {
        return this.requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    @Override
    public Offering getOffering() {
        return this.offering;
    }

    @Override
    public void setOffering(Offering offering) {
        this.offering = offering;
    }

    public String getRequest() {
        return this.request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Date getBeginLease() {
        return this.beginLease;
    }

    public void setBeginLease(Date beginLease) {
        this.beginLease = beginLease;
    }

    public Date getEndLease() {
        return this.endLease;
    }

    public void setEndLease(Date endLease) {
        this.endLease = endLease;
    }

    @Override
    public Set<ObservableProperty> getObservableProperties() {
        return this.observableProperties;
    }

    @Override
    public void setObservableProperties(Set<ObservableProperty> observableProperties) {
        this.observableProperties = observableProperties;
    }

    @Override
    public Set<ObservationTemplate> getObservationTemplates() {
        return this.observationTemplates;
    }

    @Override
    public void setObservationTemplates(Set<ObservationTemplate> observationTemplates) {
        this.observationTemplates = observationTemplates;
    }

    @Override
    public Set<CompositePhenomenon> getCompositePhenomenons() {
        return this.compositePhenomenons;
    }

    @Override
    public void setCompositePhenomenons(Set<CompositePhenomenon> compositePhenomenons) {
        this.compositePhenomenons = compositePhenomenons;
    }
}
