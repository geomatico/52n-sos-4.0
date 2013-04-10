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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDeletedFlag;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservableProperty;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationType;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOffering;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedure;

public class ObservationConstellation implements Serializable, HasProcedure, HasObservableProperty, HasOffering, HasObservationType, HasDeletedFlag {
public static final String ID = "observationConstellationId";
    private static final long serialVersionUID = -3890149740562709928L;
    private long observationConstellationId;
    private ObservableProperty observableProperty;
    private Procedure procedure;
    private ObservationType observationType;
    private Offering offering;
    private Boolean deleted;

    public ObservationConstellation() {
    }

    public long getObservationConstellationId() {
        return this.observationConstellationId;
    }

    public void setObservationConstellationId(long observationConstellationId) {
        this.observationConstellationId = observationConstellationId;
    }

    @Override
    public ObservableProperty getObservableProperty() {
        return this.observableProperty;
    }

    @Override
    public void setObservableProperty(ObservableProperty observableProperty) {
        this.observableProperty = observableProperty;
    }

    @Override
    public Procedure getProcedure() {
        return this.procedure;
    }

    @Override
    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }
    
    @Override
    public ObservationType getObservationType() {
        return this.observationType;
    }

    @Override
    public void setObservationType(ObservationType observationType) {
        this.observationType = observationType;
    }
    
    @Override
    public Offering getOffering() {
        return this.offering;
    }

    @Override
    public void setOffering(Offering offering) {
        this.offering = offering;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }
}
