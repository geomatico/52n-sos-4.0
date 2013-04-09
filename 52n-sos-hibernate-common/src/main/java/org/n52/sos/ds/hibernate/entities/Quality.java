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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasName;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasSweType;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasUnit;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasValue;

public class Quality implements Serializable, HasUnit, HasSweType, HasName, HasValue<String> {
    public static final String ID = "qualityId";
    private static final long serialVersionUID = 6674112547060874857L;
    private long qualityId;
    private SweType sweType;
    private Unit unit;
    private String name;
    private String value;

    public Quality() {
    }

    public long getQualityId() {
        return this.qualityId;
    }

    public void setQualityId(long qualityId) {
        this.qualityId = qualityId;
    }

    @Override
    public SweType getSweType() {
        return this.sweType;
    }

    @Override
    public void setSweType(SweType sweType) {
        this.sweType = sweType;
    }

    @Override
    public Unit getUnit() {
        return this.unit;
    }

    @Override
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
