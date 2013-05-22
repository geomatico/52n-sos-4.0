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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservationType;

public class ObservationType implements Serializable {

    private static final long serialVersionUID = -8338409455186689274L;

    public static final String ID = "observationTypeId";

    public static final String OBSERVATION_TYPE = HasObservationType.OBSERVATION_TYPE;
    
    private long observationTypeId;

    private String observationType;

    public ObservationType() {
    }

    public ObservationType(String observationType) {
        this.observationType = observationType;
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

}
