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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDescription;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;

public class ObservableProperty implements Serializable, HasIdentifier, HasDescription {
    public static final String ID = "observablePropertyId";
    private static final long serialVersionUID = -7609321954357775125L;
    private long observablePropertyId;
    private String identifier;
    private String description;

    public ObservableProperty() {
    }

    public long getObservablePropertyId() {
        return observablePropertyId;
    }

    public void setObservablePropertyId(final long observablePropertyId) {
        this.observablePropertyId = observablePropertyId;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ObservableProperty setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }
}
