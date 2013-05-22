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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.sos.ds.hibernate.entities.BlobObservation;
import org.n52.sos.ds.hibernate.entities.BooleanObservation;
import org.n52.sos.ds.hibernate.entities.CategoryObservation;
import org.n52.sos.ds.hibernate.entities.CountObservation;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.GeometryObservation;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.TFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.TextObservation;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.om.values.UnknownValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.Configurator;

public class HibernateUtilities {
    public static ObservationConstellation checkObservationConstellation(SosObservationConstellation soc,
                                                                         String offering, Session session,
                                                                         String parameterName) throws OwsExceptionReport {
        String observableProperty = soc.getObservableProperty().getIdentifier();
        String procedure = soc.getProcedure().getIdentifier();

        Criteria c = session.createCriteria(ObservationConstellation.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        c.createCriteria(ObservationConstellation.OFFERING)
                .add(Restrictions.eq(Offering.IDENTIFIER, offering));


        c.createCriteria(ObservationConstellation.OBSERVABLE_PROPERTY)
                .add(Restrictions.eq(ObservableProperty.IDENTIFIER, observableProperty));


        c.createCriteria(ObservationConstellation.PROCEDURE)
                .add(Restrictions.eq(Procedure.IDENTIFIER, procedure));

        @SuppressWarnings("unchecked")
        List<ObservationConstellation> hocs = c.list();

        if (!hocs.isEmpty()) {
            for (ObservationConstellation hoc : hocs) {
                if (hoc.getObservationType() == null ||
                    (hoc.getObservationType() != null &&
                     (hoc.getObservationType().getObservationType().equals("NOT_DEFINED") ||
                      hoc.getObservationType().getObservationType().isEmpty()))) {
                    return HibernateCriteriaTransactionalUtilities.updateObservationConstellation(hoc, soc
                            .getObservationType(), session);
                } else {
                    if (hoc.getObservationType().getObservationType()
                            .equals(soc.getObservationType())) {
                        return hoc;
                    } else {
                        throw new InvalidParameterValueException().at(parameterName)
                                .withMessage("The requested observationType (%s) is invalid for procedure = %s, observedProperty = %s and offering = %s! The valid observationType is '%s'!",
                                             soc.getObservationType(),
                                             procedure,
                                             observableProperty,
                                             soc.getOfferings(),
                                             hoc.getObservationType().getObservationType());
                    }
                }
            }
        } else {
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested observation constellation (procedure=%s, observedProperty=%s and offering=%s) is invalid!",
                                 procedure,
                                 observableProperty,
                                 soc.getOfferings());
        }
        return null;
    }

    public static FeatureOfInterest checkOrInsertFeatureOfInterest(SosAbstractFeature featureOfInterest, Session session)
            throws OwsExceptionReport {
        if (featureOfInterest instanceof SosSamplingFeature) {
            String featureIdentifier =
                   Configurator.getInstance().getFeatureQueryHandler()
                    .insertFeature((SosSamplingFeature) featureOfInterest, session);
            return HibernateCriteriaTransactionalUtilities.getOrInsertFeatureOfInterest(featureIdentifier,
                                                                                        ((SosSamplingFeature) featureOfInterest)
                    .getUrl(), session);
        } else {
            // TODO: throw exception
            throw new NoApplicableCodeException();
        }
    }

    public static void checkOrInsertFeatureOfInterestRelatedFeatureRelation(FeatureOfInterest featureOfInterest,
                                                                            Offering offering, Session session) {
        List<RelatedFeature> relatedFeatures =
                             HibernateCriteriaQueryUtilities
                .getRelatedFeatureForOffering(offering.getIdentifier(), session);
        if (relatedFeatures != null) {
            for (RelatedFeature relatedFeature : relatedFeatures) {
                HibernateCriteriaTransactionalUtilities.insertFeatureOfInterestRelationShip(
                        (TFeatureOfInterest)relatedFeature.getFeatureOfInterest(), featureOfInterest, session);
            }
        }
    }

    public static void addPhenomeonTimeAndResultTimeToObservation(Observation hObservation, ITime phenomenonTime,
                                                                  TimeInstant resultTime) {
        addPhenomenonTimeToObservation(hObservation, phenomenonTime);
        addResultTimeToObservation(hObservation, resultTime, phenomenonTime);
    }

    public static void addPhenomenonTimeToObservation(Observation hObservation, ITime phenomenonTime) {
        if (phenomenonTime instanceof TimeInstant) {
            TimeInstant time = (TimeInstant) phenomenonTime;
            hObservation.setPhenomenonTimeStart(time.getValue().toDate());
            hObservation.setPhenomenonTimeEnd(time.getValue().toDate());
        } else if (phenomenonTime instanceof TimePeriod) {
            TimePeriod time = (TimePeriod) phenomenonTime;
            hObservation.setPhenomenonTimeStart(time.getStart().toDate());
            hObservation.setPhenomenonTimeEnd(time.getEnd().toDate());
        }
    }

    public static void addResultTimeToObservation(Observation hObservation, TimeInstant resultTime, ITime phenomenonTime) {
        if (resultTime != null) {
            if (resultTime.getValue() != null) {
                hObservation.setResultTime(resultTime.getValue().toDate());
            } else if (resultTime.getIndeterminateValue().contains(Sos2Constants.EN_PHENOMENON_TIME)
                       && phenomenonTime instanceof TimeInstant) {
                hObservation.setResultTime(((TimeInstant) phenomenonTime).getValue().toDate());
            } else {
                // TODO: exception not valid resultTime
            }
        } else {
            if (phenomenonTime instanceof TimeInstant) {
                hObservation.setResultTime(((TimeInstant) phenomenonTime).getValue().toDate());
            } else {
                // TODO exception
            }
        }
    }

    public static void addValidTimeToObservation(Observation hObservation, TimePeriod validTime) {
        if (validTime != null) {
            hObservation.setValidTimeStart(validTime.getStart().toDate());
            hObservation.setValidTimeEnd(validTime.getEnd().toDate());
        }
    }

    public static Observation createObservationFromValue(IValue<?> value, Session session) {
        if (value instanceof BooleanValue) {
            BooleanObservation observation = new BooleanObservation();
            observation.setValue(((BooleanValue) value).getValue());
            return observation;
        } else if (value instanceof UnknownValue) {
            BlobObservation observation = new BlobObservation();
            observation.setValue(((UnknownValue) value).getValue());
            return observation;
        } else if (value instanceof CategoryValue) {
            CategoryObservation observation = new CategoryObservation();
            observation.setValue(((CategoryValue) value).getValue());
            return observation;
        } else if (value instanceof CountValue) {
            CountObservation observation = new CountObservation();
            observation.setValue(((CountValue) value).getValue());
            return observation;
        } else if (value instanceof GeometryValue) {
            GeometryObservation observation = new GeometryObservation();
            observation.setValue(((GeometryValue) value).getValue());
            return observation;
        } else if (value instanceof QuantityValue) {
            NumericObservation observation = new NumericObservation();
            observation.setValue(((QuantityValue) value).getValue());
            return observation;
        } else if (value instanceof TextValue) {
            TextObservation observation = new TextObservation();
            observation.setValue(((TextValue) value).getValue());
            return observation;
        }
        return new Observation();
    }

    private HibernateUtilities() {
    }
}
