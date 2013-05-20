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
package org.n52.sos.ds.hibernate.util;

import java.util.Collections;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.BooleanObservation;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.Codespace;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.Unit;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.util.CollectionHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class HibernateObservationBuilder {
    public static final String CODESPACE = "Codespace";
    public static final String UNIT = "Unit";
    public static final String OFFERING_1 = "Offering1";
    public static final String OFFERING_2 = "Offering2";
    public static final String FEATURE_OF_INTEREST = "FeatureOfInterest";
    public static final String OBSERVABLE_PROPERTY = "ObservableProperty";
    public static final String PROCEDURE_DESCRIPTION_FORMAT = "ProcedureDescriptionFormat";
    public static final String FEATURE_OF_INTEREST_TYPE = "FeatureOfInterestType";
    public static final String OBSERVATION_TYPE = "ObservationType";
    private final Session session;

    public HibernateObservationBuilder(Session session) {
        this.session = session;
    }

    public Observation createObservation(String id, Date phenomenonTimeStart, Date phenomenonTimeEnd, Date resultTime,
                                         Date validTimeStart, Date validTimeEnd) {
        BooleanObservation observation = new BooleanObservation();
        observation.setDeleted(false);
        observation.setFeatureOfInterest(getFeatureOfInterest());
        observation.setIdentifier(id);
        observation.setProcedure(getProcedure());
        observation.setPhenomenonTimeStart(phenomenonTimeStart);
        observation.setPhenomenonTimeEnd(phenomenonTimeEnd);
        observation.setResultTime(resultTime);
        observation.setValidTimeStart(validTimeStart);
        observation.setValidTimeEnd(validTimeEnd);
        observation.setObservableProperty(getObservableProperty());
        observation.setOfferings(CollectionHelper.set(getOffering1(), getOffering2()));
        observation.setUnit(getUnit());
        observation.setCodespace(getCodespace());
        observation.setValue(getBooleanValue());
        session.save(observation);
        session.flush();
        return observation;
    }

    public Observation createObservation(String id, DateTime phenomenonTimeStart, DateTime phenomenonTimeEnd,
                                         DateTime resultTime,
                                         DateTime validTimeStart, DateTime validTimeEnd) {
        return createObservation(id,
                                 phenomenonTimeStart != null ? phenomenonTimeStart.toDate() : null,
                                 phenomenonTimeEnd != null ? phenomenonTimeEnd.toDate() : null,
                                 resultTime != null ? resultTime.toDate() : null,
                                 validTimeStart != null ? validTimeStart.toDate() : null,
                                 validTimeEnd != null ? validTimeEnd.toDate() : null);
    }

    public Observation createObservation(String id, DateTime begin, DateTime end) {
        Date s = begin != null ? begin.toDate() : null;
        Date e = end != null ? end.toDate() : null;
        return createObservation(id, s, e, s, s, e);
    }

    public Observation createObservation(String id, DateTime position) {
        Date s = position != null ? position.toDate() : null;
        return createObservation(id, s, s, s, s, s);
    }

    public Observation createObservation(Enum<?> id, Date phenomenonTimeStart, Date phenomenonTimeEnd, Date resultTime,
                                         Date validTimeStart, Date validTimeEnd) {
        return createObservation(id.name(), phenomenonTimeStart, phenomenonTimeEnd, resultTime, validTimeStart, validTimeEnd);
    }

    public Observation createObservation(Enum<?> id, DateTime phenomenonTimeStart, DateTime phenomenonTimeEnd,
                                         DateTime resultTime,
                                         DateTime validTimeStart, DateTime validTimeEnd) {
        return createObservation(id.name(), phenomenonTimeStart, phenomenonTimeEnd, resultTime, validTimeStart, validTimeEnd);
    }

    public Observation createObservation(Enum<?> id, DateTime begin, DateTime end) {
        return createObservation(id.name(), begin, end);
    }

    public Observation createObservation(Enum<?> id, DateTime time) {
        return createObservation(id.name(), time);
    }

    protected FeatureOfInterest getFeatureOfInterest() {
        FeatureOfInterest featureOfInterest = (FeatureOfInterest) session
                .createCriteria(FeatureOfInterest.class)
                .add(Restrictions.eq(FeatureOfInterest.IDENTIFIER, FEATURE_OF_INTEREST))
                .uniqueResult();
        if (featureOfInterest == null) {
            featureOfInterest = new FeatureOfInterest();
            featureOfInterest.setCodespace(getCodespace());
            featureOfInterest.setDescriptionXml("<xml/>");
            featureOfInterest.setFeatureOfInterestType(getFeatureOfInterestType());
            featureOfInterest.setFeatureOfInterestsForChildFeatureId(null);
            featureOfInterest.setFeatureOfInterestsForParentFeatureId(null);
            featureOfInterest.setIdentifier(FEATURE_OF_INTEREST);
            featureOfInterest.setName(FEATURE_OF_INTEREST);
            session.save(featureOfInterest);
            session.flush();
        }
        return featureOfInterest;
    }

    protected ObservableProperty getObservableProperty() {
        ObservableProperty observableProperty = (ObservableProperty) session
                .createCriteria(ObservableProperty.class)
                .add(Restrictions.eq(ObservableProperty.IDENTIFIER, OBSERVABLE_PROPERTY))
                .uniqueResult();
        if (observableProperty == null) {
            observableProperty = new ObservableProperty();
            observableProperty.setDescription(OBSERVABLE_PROPERTY);
            observableProperty.setIdentifier(OBSERVABLE_PROPERTY);
            session.save(observableProperty);
            session.flush();
        }
        return observableProperty;
    }

    protected Offering getOffering1() {
        Offering offering = (Offering) session
                .createCriteria(Offering.class)
                .add(Restrictions.eq(Offering.IDENTIFIER, OFFERING_1))
                .uniqueResult();
        if (offering == null) {
            offering = new Offering();
            offering.setFeatureOfInterestTypes(Collections.singleton(getFeatureOfInterestType()));
            offering.setIdentifier(OFFERING_1);
            offering.setName(OFFERING_1);
            offering.setObservationTypes(Collections.singleton(getObservationType()));
            offering.setRelatedFeatures(null);
            session.save(offering);
            session.flush();
        }
        return offering;
    }

    protected Offering getOffering2() {
        Offering offering = (Offering) session
                .createCriteria(Offering.class)
                .add(Restrictions.eq(Offering.IDENTIFIER, OFFERING_2))
                .uniqueResult();
        if (offering == null) {
            offering = new Offering();
            offering.setFeatureOfInterestTypes(Collections.singleton(getFeatureOfInterestType()));
            offering.setIdentifier(OFFERING_2);
            offering.setName(OFFERING_2);
            offering.setObservationTypes(Collections.singleton(getObservationType()));
            offering.setRelatedFeatures(null);
            session.save(offering);
            session.flush();
        }
        return offering;
    }

    protected Unit getUnit() {
        Unit unit = (Unit) session
                .createCriteria(Unit.class)
                .add(Restrictions.eq(Unit.UNIT, UNIT))
                .uniqueResult();
        if (unit == null) {
            unit = new Unit();
            unit.setUnit(UNIT);
            session.save(unit);
            session.flush();
        }
        return unit;
    }

    protected Codespace getCodespace() {
        Codespace codespace = (Codespace) session
                .createCriteria(Codespace.class)
                .add(Restrictions.eq(Codespace.CODESPACE, CODESPACE))
                .uniqueResult();
        if (codespace == null) {
            codespace = new Codespace();
            codespace.setCodespace(CODESPACE);
            session.save(codespace);
            session.flush();
        }
        return codespace;
    }

    protected Procedure getProcedure() {
        Procedure procedure = (Procedure) session
                .createCriteria(Procedure.class)
                .add(Restrictions.eq(Procedure.IDENTIFIER, "Procedure"))
                .uniqueResult();
        if (procedure == null) {
            procedure = new Procedure();
            procedure.setDeleted(false);
            procedure.setIdentifier("Procedure");
            procedure.setGeom(null);
            procedure.setProcedureDescriptionFormat(getProcedureDescriptionFormat());
            procedure.setChildProcedures(null);
            procedure.setParentProcedures(null);
            session.save(procedure);
            session.flush();
            procedure.setValidProcedureTimes(Collections.singleton(getValidProcedureTime()));
            session.update(procedure);
            session.flush();
            return procedure;
        }
        return procedure;
    }

    protected ValidProcedureTime getValidProcedureTime() {
        ValidProcedureTime validProcedureTime = (ValidProcedureTime) session
                .createCriteria(ValidProcedureTime.class)
                .add(Restrictions.eq(ValidProcedureTime.PROCEDURE, getProcedure()))
                .uniqueResult();
        if (validProcedureTime == null) {
            validProcedureTime = new ValidProcedureTime();
            validProcedureTime.setDescriptionXml("<xml/>");
            validProcedureTime.setEndTime(null);
            validProcedureTime.setStartTime(new Date());
            validProcedureTime.setProcedure(getProcedure());
            session.save(validProcedureTime);
            session.flush();
        }
        return validProcedureTime;
    }

    protected ProcedureDescriptionFormat getProcedureDescriptionFormat() {
        ProcedureDescriptionFormat procedureDescriptionFormat = (ProcedureDescriptionFormat) session
                .createCriteria(ProcedureDescriptionFormat.class)
                .add(Restrictions.eq(ProcedureDescriptionFormat.PROCEDURE_DESCRIPTION_FORMAT, PROCEDURE_DESCRIPTION_FORMAT))
                .uniqueResult();
        if (procedureDescriptionFormat == null) {
            procedureDescriptionFormat = new ProcedureDescriptionFormat();
            procedureDescriptionFormat.setProcedureDescriptionFormat(PROCEDURE_DESCRIPTION_FORMAT);
            session.save(procedureDescriptionFormat);
            session.flush();
        }
        return procedureDescriptionFormat;
    }

    protected FeatureOfInterestType getFeatureOfInterestType() {
        FeatureOfInterestType featureOfInterestType = (FeatureOfInterestType) session
                .createCriteria(FeatureOfInterestType.class)
                .add(Restrictions.eq(FeatureOfInterestType.FEATURE_OF_INTEREST_TYPE, FEATURE_OF_INTEREST_TYPE))
                .uniqueResult();
        if (featureOfInterestType == null) {
            featureOfInterestType = new FeatureOfInterestType();
            featureOfInterestType.setFeatureOfInterestType(FEATURE_OF_INTEREST_TYPE);
            session.save(featureOfInterestType);
            session.flush();
        }
        return featureOfInterestType;
    }

    protected ObservationType getObservationType() {
        ObservationType observationType = (ObservationType) session
                .createCriteria(ObservationType.class)
                .add(Restrictions.eq(ObservationType.OBSERVATION_TYPE, OBSERVATION_TYPE))
                .uniqueResult();
        if (observationType == null) {
            observationType = new ObservationType();
            observationType.setObservationType(OBSERVATION_TYPE);
            session.save(observationType);
            session.flush();
            session.refresh(observationType);
        }
        return observationType;
    }
    private BooleanValue getBooleanValue() {
        BooleanValue booleanValue = new BooleanValue();
        booleanValue.setValue(true);
        return booleanValue;
    }
}
