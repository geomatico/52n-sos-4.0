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

import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasCodespace;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasDeletedFlag;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasIdentifier;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasObservableProperty;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasOfferings;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasProcedure;
import org.n52.sos.ds.hibernate.entities.HibernateRelations.HasUnit;

public class Observation implements Serializable, HasIdentifier, HasDeletedFlag, HasObservableProperty, HasProcedure,
        HasFeatureOfInterest, HasOfferings, HasCodespace, HasUnit {

    private static final long serialVersionUID = 2805137008018805520L;

    public static final String ID = "observationId";

    public static final String PHENOMENON_TIME_START = "phenomenonTimeStart";

    public static final String PHENOMENON_TIME_END = "phenomenonTimeEnd";

    public static final String VALID_TIME_START = "validTimeStart";

    public static final String VALID_TIME_END = "validTimeEnd";

    public static final String RESULT_TIME = "resultTime";

    public static final String SET_ID = "setId";

    private long observationId;

    private FeatureOfInterest featureOfInterest;

    private ObservableProperty observableProperty;

    private Procedure procedure;

    private Date phenomenonTimeStart;

    private Date phenomenonTimeEnd;

    private Date resultTime;

    private String identifier;

    private Codespace codespace;

    private boolean deleted;

    private Date validTimeStart;

    private Date validTimeEnd;

    private Unit unit;

    private String setId;

    private Set<Offering> offerings = new HashSet<Offering>(0);

    public Observation() {
    }

    public long getObservationId() {
        return observationId;
    }

    public void setObservationId(final long observationId) {
        this.observationId = observationId;
    }

    @Override
    public FeatureOfInterest getFeatureOfInterest() {
        return featureOfInterest;
    }

    @Override
    public void setFeatureOfInterest(final FeatureOfInterest featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    @Override
    public ObservableProperty getObservableProperty() {
        return observableProperty;
    }

    @Override
    public void setObservableProperty(final ObservableProperty observableProperty) {
        this.observableProperty = observableProperty;
    }

    @Override
    public Procedure getProcedure() {
        return procedure;
    }

    @Override
    public void setProcedure(final Procedure procedure) {
        this.procedure = procedure;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Observation setIdentifier(final String identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    public Codespace getCodespace() {
        return codespace;
    }

    @Override
    public void setCodespace(final Codespace codespace) {
        this.codespace = codespace;
    }

    public Date getPhenomenonTimeStart() {
        return phenomenonTimeStart;
    }

    public void setPhenomenonTimeStart(final Date phenomenonTimeStart) {
        this.phenomenonTimeStart = phenomenonTimeStart;
    }

    public Date getPhenomenonTimeEnd() {
        return phenomenonTimeEnd;
    }

    public void setPhenomenonTimeEnd(final Date phenomenonTimeEnd) {
        this.phenomenonTimeEnd = phenomenonTimeEnd;
    }

    public Date getResultTime() {
        return resultTime;
    }

    public void setResultTime(final Date resultTime) {
        this.resultTime = resultTime;
    }

    public Date getValidTimeStart() {
        return validTimeStart;
    }

    public void setValidTimeStart(final Date validTimeStart) {
        this.validTimeStart = validTimeStart;
    }

    public Date getValidTimeEnd() {
        return validTimeEnd;
    }

    public void setValidTimeEnd(final Date validTimeEnd) {
        this.validTimeEnd = validTimeEnd;
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public void setUnit(final Unit unit) {
        this.unit = unit;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(final String setId) {
        this.setId = setId;
    }

    @Override
    public Set<Offering> getOfferings() {
        return offerings;
    }

    @Override
    public void setOfferings(final Set<Offering> offerings) {
        this.offerings = offerings;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public Observation setDeleted(final boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isSetIdentifier() {
        return getIdentifier() != null && !getIdentifier().isEmpty();
    }

    public boolean isSetCodespace() {
        return getCodespace() != null && getCodespace().isSetCodespace();
    }
}
