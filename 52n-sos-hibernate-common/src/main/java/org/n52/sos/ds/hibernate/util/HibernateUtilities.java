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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.entities.BlobObservation;
import org.n52.sos.ds.hibernate.entities.BlobValue;
import org.n52.sos.ds.hibernate.entities.BooleanObservation;
import org.n52.sos.ds.hibernate.entities.CategoryObservation;
import org.n52.sos.ds.hibernate.entities.CountObservation;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.GeometryObservation;
import org.n52.sos.ds.hibernate.entities.NumericObservation;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.TextObservation;
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
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.Configurator;

public class HibernateUtilities {
    public static ObservationConstellationOfferingObservationType checkObservationConstellationOfferingObservationTypeForObservation(
            SosObservationConstellation sosObservationConstellation, String offeringIdentifier, Session session,
            String parameterName)
            throws OwsExceptionReport, HibernateException {
        // FIXME parameterName should not be part of the parameters
        // check if multiple offerings.
        HibernateQueryObject queryObject = new HibernateQueryObject();
        Map<String, String> aliases = new HashMap<String, String>();
        String obsConstAlias = HibernateCriteriaQueryUtilities.addObservationConstallationAliasToMap(aliases, null);
        String offAlias = HibernateCriteriaQueryUtilities.addOfferingAliasToMap(aliases, null);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(offAlias), offeringIdentifier));
        String obsPropAlias = HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, obsConstAlias);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateCriteriaQueryUtilities
                .getIdentifierParameter(obsPropAlias), sosObservationConstellation.getObservableProperty()
                .getIdentifier()));
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, obsConstAlias);
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias),
                sosObservationConstellation.getProcedure().getProcedureIdentifier()));
        queryObject.setAliases(aliases);
        List<ObservationConstellationOfferingObservationType> obsConstsOffObsTypes = HibernateCriteriaQueryUtilities
                .getObservationConstellationOfferingObservationType(queryObject, session);
        if (obsConstsOffObsTypes != null && !obsConstsOffObsTypes.isEmpty()) {
            for (ObservationConstellationOfferingObservationType obsConstsOffObsType : obsConstsOffObsTypes) {
                if (obsConstsOffObsType.getObservationType() == null
                    || (obsConstsOffObsType.getObservationType() != null && (obsConstsOffObsType.getObservationType()
                        .getObservationType()
                        .equals("NOT_DEFINED") || obsConstsOffObsType.getObservationType().getObservationType()
                        .isEmpty()))) {
                    return HibernateCriteriaTransactionalUtilities
                            .updateObservationConstellationOfferingObservationType(obsConstsOffObsType,
                                                                                   sosObservationConstellation
                            .getObservationType(), session);
                } else {
                    if (obsConstsOffObsType.getObservationType().getObservationType()
                            .equals(sosObservationConstellation.getObservationType())) {
                        return obsConstsOffObsType;
                    } else {
                        throw new InvalidParameterValueException().at(parameterName)
                                .withMessage("The requested observationType (%s) is invalid for procedure = %s, observedProperty = %s and offering = %s! The valid observationType is '%s'!",
                                             sosObservationConstellation.getObservationType(),
                                             sosObservationConstellation.getProcedure(),
                                             sosObservationConstellation.getObservableProperty().getIdentifier(),
                                             sosObservationConstellation.getOfferings(),
                                             obsConstsOffObsType.getObservationType().getObservationType());
                    }
                }
            }
        } else {
            throw new InvalidParameterValueException().at(Sos2Constants.InsertObservationParams.observation)
                    .withMessage("The requested observation constellation (procedure=%s, observedProperty=%s and offering=%s) is invalid!",
                                 sosObservationConstellation.getProcedure(),
                                 sosObservationConstellation.getObservableProperty().getIdentifier(),
                                 sosObservationConstellation.getOfferings());
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
                        relatedFeature.getFeatureOfInterest(), featureOfInterest, session);
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
            org.n52.sos.ds.hibernate.entities.BooleanValue booleanValue =
                                                           new org.n52.sos.ds.hibernate.entities.BooleanValue();
            booleanValue.setValue(((BooleanValue) value).getValue());
            observation.setValue(booleanValue);
            return observation;
        } else if (value instanceof UnknownValue) {
            BlobObservation observation = new BlobObservation();
            observation.setValue(HibernateCriteriaTransactionalUtilities.getOrInsertBlobValue(
                    ((BlobValue) value).getValue(), session));
            return observation;
        } else if (value instanceof CategoryValue) {
            CategoryObservation observation = new CategoryObservation();
            observation.setValue(HibernateCriteriaTransactionalUtilities.getOrInsertCategoryValue(
                    ((CategoryValue) value).getValue(), session));
            return observation;
        } else if (value instanceof CountValue) {
            CountObservation observation = new CountObservation();
            org.n52.sos.ds.hibernate.entities.CountValue countValue = new org.n52.sos.ds.hibernate.entities.CountValue();
            countValue.setValue(((CountValue) value).getValue());
            observation.setValue(countValue);
            return observation;
        } else if (value instanceof GeometryValue) {
            GeometryObservation observation = new GeometryObservation();
            observation.setValue(HibernateCriteriaTransactionalUtilities.getOrInsertGeometryValue(
                    ((GeometryValue) value).getValue(), session));
            return observation;
        } else if (value instanceof QuantityValue) {
            NumericObservation observation = new NumericObservation();
            observation.setValue(HibernateCriteriaTransactionalUtilities.getOrInsertNumericValue(
                    ((QuantityValue) value).getValue(), session));
            return observation;
        } else if (value instanceof TextValue) {
            TextObservation observation = new TextObservation();
            observation.setValue(HibernateCriteriaTransactionalUtilities.getOrInsertTextValue(
                    ((TextValue) value).getValue(), session));
            return observation;
        }
        return new Observation();
    }

    private HibernateUtilities() {
    }
}
