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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
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
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtilities {
    
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtilities.class);

    public static ObservationConstellation checkObservationConstellationForObservation(
            SosObservationConstellation sosObservationConstellation, String offeringIdentifier, Session session, String parameterName)
            throws OwsExceptionReport, HibernateException {
        // FIXME parameterName should not be part of the parameters
        // check if multiple offerings.
        List<Criterion> criterions = new ArrayList<Criterion>();
        Map<String, String> aliases = new HashMap<String, String>();
        String offAlias = HibernateCriteriaQueryUtilities.addOfferingAliasToMap(aliases, null);
        criterions.add(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(offAlias), offeringIdentifier));
        String obsPropAlias = HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, null);
        criterions.add(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateCriteriaQueryUtilities
                .getIdentifierParameter(obsPropAlias), sosObservationConstellation.getObservableProperty()
                .getIdentifier()));
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, null);
        criterions.add(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias),
                sosObservationConstellation.getProcedure().getProcedureIdentifier()));
        ObservationConstellation obsConst =
                HibernateCriteriaQueryUtilities.getObservationConstallation(aliases, criterions, session);
        if (obsConst != null) {
            if (obsConst.getObservationType() == null
                    || (obsConst.getObservationType() != null && (obsConst.getObservationType().getObservationType()
                            .equals("NOT_DEFINED") || obsConst.getObservationType().getObservationType().isEmpty()))) {
                return HibernateCriteriaTransactionalUtilities.updateObservationConstellation(obsConst,
                        sosObservationConstellation.getObservationType(), session);
            } else {
                if (obsConst.getObservationType().getObservationType()
                        .equals(sosObservationConstellation.getObservationType())) {
                    return obsConst;
                } else {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested observationType (");
                    exceptionText.append(sosObservationConstellation.getObservationType());
                    exceptionText.append(") is invalid for ");
                    exceptionText.append("procedure = ");
                    exceptionText.append(sosObservationConstellation.getProcedure());
                    exceptionText.append(", observedProperty = ");
                    exceptionText.append(sosObservationConstellation.getObservableProperty().getIdentifier());
                    exceptionText.append("and offering = ");
                    exceptionText.append(sosObservationConstellation.getOfferings());
                    exceptionText.append("!");
                    exceptionText.append("The valid observationType is '");
                    exceptionText.append(obsConst.getObservationType().getObservationType());
                    exceptionText.append("'!");
                    LOGGER.debug(exceptionText.toString());
                    throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText.toString());
                }
            }
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested observation constellation (");
            exceptionText.append("procedure=");
            exceptionText.append(sosObservationConstellation.getProcedure());
            exceptionText.append(", observedProperty=");
            exceptionText.append(sosObservationConstellation.getObservableProperty().getIdentifier());
            exceptionText.append(" and offering=");
            exceptionText.append(sosObservationConstellation.getOfferings());
            exceptionText.append(")");
            exceptionText.append(" is invalid!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText.toString());
        }
    }

    public static FeatureOfInterest checkOrInsertFeatureOfInterest(SosAbstractFeature featureOfInterest, Session session)
            throws OwsExceptionReport {
        if (featureOfInterest instanceof SosSamplingFeature) {
            String featureIdentifier =
                    Configurator.getInstance().getFeatureQueryHandler()
                            .insertFeature((SosSamplingFeature) featureOfInterest, session);
            return HibernateCriteriaTransactionalUtilities.getOrInsertFeatureOfInterest(featureIdentifier,
                    ((SosSamplingFeature) featureOfInterest).getUrl(), session);
        } else {
            // TODO: throw exception
            throw new OwsExceptionReport();
        }
    }
    
    public static void checkOrInsertFeatureOfInterestRelatedFeatureRelation(FeatureOfInterest featureOfInterest,
            Offering offering, Session session) {
        List<RelatedFeature> relatedFeatures =
                HibernateCriteriaQueryUtilities.getRelatedFeatureForOffering(offering.getIdentifier(), session);
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
            // TODO: exception missing resultTime
        }
    }

    public static void addValidTimeToObservation(Observation hObservation, TimePeriod validTime) {
        if (validTime != null) {
            hObservation.setValidTimeStart(validTime.getStart().toDate());
            hObservation.setValidTimeEnd(validTime.getEnd().toDate());
        }
    }

    public static void addValueToObservation(Observation hObservation, IValue value, Session session) {
        if (value instanceof BooleanValue) {
            hObservation.setBooleanValues(HibernateCriteriaTransactionalUtilities.getOrInsertBooleanValue(
                    ((BooleanValue) value).getValue(), session));
        } else if (value instanceof CategoryValue) {
            hObservation.setCategoryValues(HibernateCriteriaTransactionalUtilities.getOrInsertCategoryValue(
                    ((CategoryValue) value).getValue(), session));
        } else if (value instanceof CountValue) {
            hObservation.setCountValues(HibernateCriteriaTransactionalUtilities.getOrInsertCountValue(
                    ((CountValue) value).getValue(), session));
        } else if (value instanceof GeometryValue) {
            hObservation.setGeometryValues(HibernateCriteriaTransactionalUtilities.getOrInsertGeometryValue(
                    ((GeometryValue) value).getValue(), session));
        } else if (value instanceof QuantityValue) {
            hObservation.setNumericValues(HibernateCriteriaTransactionalUtilities.getOrInsertQuantityValue(
                    ((QuantityValue) value).getValue(), session));
        } else if (value instanceof TextValue) {
            hObservation.setTextValues(HibernateCriteriaTransactionalUtilities.getOrInsertTextValue(
                    ((TextValue) value).getValue(), session));
        }
    }

}
