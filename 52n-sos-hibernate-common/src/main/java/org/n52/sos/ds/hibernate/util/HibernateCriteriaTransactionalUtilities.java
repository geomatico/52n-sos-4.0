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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.HibernateQueryObject;
import org.n52.sos.ds.hibernate.entities.BooleanValue;
import org.n52.sos.ds.hibernate.entities.CategoryValue;
import org.n52.sos.ds.hibernate.entities.CountValue;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.GeometryValue;
import org.n52.sos.ds.hibernate.entities.NumericValue;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.entities.TextValue;
import org.n52.sos.ds.hibernate.entities.Unit;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class HibernateCriteriaTransactionalUtilities {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateCriteriaTransactionalUtilities.class);

    public static synchronized void setDeleteSensorFlag(String identifier, boolean deleteFlag, Session session)
            throws OwsExceptionReport {
        Procedure procedure = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(identifier, session);
        if (procedure != null) {
            procedure.setDeleted(deleteFlag);
            session.saveOrUpdate(procedure);
            session.flush();
        } else {
            String exceptionText = "The requested identifier is not contained in database";
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    public static Procedure getOrInsertProcedure(String identifier, ProcedureDescriptionFormat pdf, Session session) {
        Procedure result = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(identifier, session);
        if (result == null) {
            result = new Procedure();
            result.setProcedureDescriptionFormat(pdf);
            result.setIdentifier(identifier);
            result.setDeleted(false);
            session.save(result);
            session.flush();
        }
        return result;
    }

    public static void insertValidProcedureTime(Procedure proc, String xmlDescription, DateTime validStartTime,
            Session session) {
        ValidProcedureTime vpd = new ValidProcedureTime();
        vpd.setProcedure(proc);
        vpd.setDescriptionXml(xmlDescription);
        vpd.setStartTime(validStartTime.toDate());
        session.save(vpd);
        session.flush();
    }

    public static Offering insertOffering(String offeringIdentifier, String offeringName,
            List<RelatedFeature> relatedFeatures, List<ObservationType> observationTypes, List<FeatureOfInterestType> featureOfInterestTypes, Session session) {
        Offering offering = new Offering();
        offering.setIdentifier(offeringIdentifier);
        if (offeringName != null) {
            offering.setName(offeringName);
        } else {
            offering.setName("Offering for the procedure " + offeringIdentifier);
        }
        if (!relatedFeatures.isEmpty()) {
            offering.setRelatedFeatures(new HashSet<RelatedFeature>(relatedFeatures));
        }
        if (!observationTypes.isEmpty()) {
            offering.setObservationTypes(new HashSet<ObservationType>(observationTypes));
        }
        if (!featureOfInterestTypes.isEmpty()) {
            offering.setFeatureOfInterestTypes(new HashSet<FeatureOfInterestType>(featureOfInterestTypes));
        }
        session.saveOrUpdate(offering);
        session.flush();
        return offering;
    }

    public static List<RelatedFeature> getOrInsertRelatedFeature(SosAbstractFeature feature,
            List<RelatedFeatureRole> roles, Session session) throws OwsExceptionReport {
        // TODO: create featureOfInterest and link to relatedFeature
        List<RelatedFeature> relFeats =
                HibernateCriteriaQueryUtilities.getRelatedFeatures(feature.getIdentifier().getValue(), session);
        if (relFeats == null || (relFeats != null && relFeats.isEmpty())) {
            RelatedFeature relFeat = new RelatedFeature();
            String identifier = feature.getIdentifier().getValue();
            String url = null;
            if (feature instanceof SosSamplingFeature) {
                identifier =
                        Configurator.getInstance().getFeatureQueryHandler()
                                .insertFeature((SosSamplingFeature) feature, session);
                url = ((SosSamplingFeature) feature).getUrl();
            }
            relFeat.setFeatureOfInterest(getOrInsertFeatureOfInterest(identifier, url, session));
            relFeat.setRelatedFeatureRoles(new HashSet<RelatedFeatureRole>(roles));
            session.save(relFeat);
            session.flush();
            relFeats.add(relFeat);
        }
        return relFeats;
    }

    public static List<RelatedFeatureRole> getOrInsertRelatedFeatureRole(String role, Session session) {
        List<RelatedFeatureRole> relFeatRoles = HibernateCriteriaQueryUtilities.getRelatedFeatureRole(role, session);
        if (relFeatRoles == null || (relFeatRoles != null && relFeatRoles.isEmpty())) {
            RelatedFeatureRole relFeatRole = new RelatedFeatureRole();
            relFeatRole.setRelatedFeatureRole(role);
            session.save(relFeatRole);
            session.flush();
            relFeatRoles.add(relFeatRole);
        }
        return relFeatRoles;
    }

    public static List<ObservableProperty> getOrInsertObservableProperty(
            List<SosObservableProperty> observableProperty, Session session) {
        List<String> identifiers = new ArrayList<String>(observableProperty.size());
        for (SosObservableProperty sosObservableProperty : observableProperty) {
            identifiers.add(sosObservableProperty.getIdentifier());
        }
        List<ObservableProperty> obsProps =
                HibernateCriteriaQueryUtilities.getObservableProperties(identifiers, session);
        for (SosObservableProperty sosObsProp : observableProperty) {
            boolean exists = false;
            for (ObservableProperty obsProp : obsProps) {
                if (obsProp.getIdentifier().equals(sosObsProp.getIdentifier())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                ObservableProperty obsProp = new ObservableProperty();
                obsProp.setIdentifier(sosObsProp.getIdentifier());
                obsProp.setDescription(sosObsProp.getDescription());
                session.save(obsProp);
                obsProps.add(obsProp);
            }
        }
        session.flush();
        return obsProps;
    }

    public static Unit getOrInsertUnit(String unit, Session session) {
        Unit result = HibernateCriteriaQueryUtilities.getUnit(unit, session);
        if (result == null) {
            result = new Unit();
            result.setUnit(unit);
            session.save(result);
            session.flush();
        }
        return result;
    }

    public static ObservationConstellation checkOrInsertObservationConstellation(Procedure proc, ObservableProperty obsProp, Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_PROCEDURE, proc));
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_OBSERVABLE_PROPERTY, obsProp));
        ObservationConstellation obsConst = HibernateCriteriaQueryUtilities.getObservationConstellation(queryObject, session);
        if (obsConst == null) {
            obsConst = new ObservationConstellation();
            obsConst.setObservableProperty(obsProp);
            obsConst.setProcedure(proc);
            session.save(obsConst);
            session.flush();
            session.refresh(obsConst);
           
        }
        return obsConst;
    }

    public static ObservationConstellationOfferingObservationType checkOrInsertObservationConstellationOfferingObservationType(ObservationConstellation obsConst,
           Offering offering, Session session) {
        HibernateQueryObject queryObject = new HibernateQueryObject();
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_OBSERVATION_CONSTELLATION, obsConst));
        queryObject.addCriterion(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateConstants.PARAMETER_OFFERING, offering));
        ObservationConstellationOfferingObservationType obsConstOffObsType = HibernateCriteriaQueryUtilities.getObservationConstellationOfferingObservationType(queryObject, session);
        if (obsConstOffObsType == null) {
            obsConstOffObsType = new ObservationConstellationOfferingObservationType();
            obsConstOffObsType.setObservationConstellation(obsConst);
            obsConstOffObsType.setOffering(offering);
            session.save(obsConstOffObsType);
            session.flush();
        }
        return obsConstOffObsType;
    }

    public static void updateValidProcedureTime(ValidProcedureTime validProcedureTime, Session session) {
        session.saveOrUpdate(validProcedureTime);
    }

    public static ObservationConstellationOfferingObservationType updateObservationConstellationOfferingObservationType(ObservationConstellationOfferingObservationType obsConstOffObsType,
            String observationType, Session session) {
        ObservationType obsType = HibernateCriteriaQueryUtilities.getObservationTypeObject(observationType, session);
        obsConstOffObsType.setObservationType(obsType);
        session.saveOrUpdate(obsConstOffObsType);
        return obsConstOffObsType;
    }

    public static FeatureOfInterest getOrInsertFeatureOfInterest(String featureIdentifier, String url, Session session) {
        FeatureOfInterest feature = HibernateCriteriaQueryUtilities.getFeatureOfInterest(featureIdentifier, session);
        if (feature == null) {
            feature = new FeatureOfInterest();
            feature.setIdentifier(featureIdentifier);
            if (url != null && !url.isEmpty()) {
                feature.setUrl(url);
            }
            session.save(feature);
            session.flush();
        } else if (feature.getUrl() != null && !feature.getUrl().isEmpty() && url != null && !url.isEmpty()) {
            if (url != null && !url.isEmpty()) {
                feature.setUrl(url);
                session.saveOrUpdate(feature);
                session.flush();
            }
        }
        return feature;
    }

    public static Set<BooleanValue> getOrInsertBooleanValue(Boolean value, Session session) {
        BooleanValue booleanValue = HibernateCriteriaQueryUtilities.getBooleanValue(value, session);
        if (booleanValue == null) {
            booleanValue = new BooleanValue();
            booleanValue.setValue(value);
            session.save(booleanValue);
            session.flush();
        }
        Set<BooleanValue> values = new HashSet<BooleanValue>(1);
        values.add(booleanValue);
        return values;
    }

    public static Set<CategoryValue> getOrInsertCategoryValue(String value, Session session) {
        CategoryValue categoryValue = HibernateCriteriaQueryUtilities.getCategoryValue(value, session);
        if (categoryValue == null) {
            categoryValue = new CategoryValue();
            categoryValue.setValue(value);
            session.save(categoryValue);
            session.flush();
        }
        Set<CategoryValue> values = new HashSet<CategoryValue>(1);
        values.add(categoryValue);
        return values;
    }

    public static Set<CountValue> getOrInsertCountValue(Integer value, Session session) {
        CountValue countValue = HibernateCriteriaQueryUtilities.getCountValue(value, session);
        if (countValue == null) {
            countValue = new CountValue();
            countValue.setValue(value);
            session.save(countValue);
            session.flush();
        }
        Set<CountValue> values = new HashSet<CountValue>(1);
        values.add(countValue);
        return values;
    }

    public static Set<GeometryValue> getOrInsertGeometryValue(Geometry value, Session session) {
        GeometryValue geomtryValue = HibernateCriteriaQueryUtilities.getGeometryValue(value, session);
        if (geomtryValue == null) {
            geomtryValue = new GeometryValue();
            geomtryValue.setValue(value);
            session.save(geomtryValue);
            session.flush();
        }
        Set<GeometryValue> values = new HashSet<GeometryValue>(1);
        values.add(geomtryValue);
        return values;
    }

    public static Set<NumericValue> getOrInsertQuantityValue(Double value, Session session) {
        NumericValue numericValue = HibernateCriteriaQueryUtilities.getNumericValue(value, session);
        if (numericValue == null) {
            numericValue = new NumericValue();
            numericValue.setValue(value);
            session.save(numericValue);
            session.flush();
        }
        Set<NumericValue> values = new HashSet<NumericValue>(1);
        values.add(numericValue);
        return values;
    }

    public static Set<TextValue> getOrInsertTextValue(String value, Session session) {
        TextValue textValue = HibernateCriteriaQueryUtilities.getTextValue(value, session);
        if (textValue == null) {
            textValue = new TextValue();
            textValue.setValue(value);
            session.save(textValue);
            session.flush();
        }
        Set<TextValue> values = new HashSet<TextValue>(1);
        values.add(textValue);
        return values;
    }

    public static Observation insertObservation(Observation observation, Session session) {
        session.save(observation);
        session.flush();
        return observation;
    }

    public static void insertFeatureOfInterestTypes(Set<String> featureTypes, Session session) {
        for (String featureType : featureTypes) {
            FeatureOfInterestType featureOfInterestType =
                    HibernateCriteriaQueryUtilities.getFeatureOfInterestTypeObject(featureType, session);
            if (featureOfInterestType == null) {
                featureOfInterestType = new FeatureOfInterestType();
                featureOfInterestType.setFeatureOfInterestType(featureType);
                session.save(featureOfInterestType);
            }
        }
        session.flush();
    }

    public static void insertObservationTypes(Set<String> obsTypes, Session session) {
        for (String obsType : obsTypes) {
            ObservationType observationType =
                    HibernateCriteriaQueryUtilities.getObservationTypeObject(obsType, session);
            if (observationType == null) {
                observationType = new ObservationType();
                observationType.setObservationType(obsType);
                session.save(observationType);
            }
        }
        session.flush();
    }

    public static void insertProcedureDescriptionsFormats(Set<String> procDescFormats, Session session) {
        for (String procDescFormat : procDescFormats) {
            ProcedureDescriptionFormat procedureDescriptionFormat =
                    HibernateCriteriaQueryUtilities.getProcedureDescriptionFormatObject(procDescFormat, session);
            if (procedureDescriptionFormat == null) {
                procedureDescriptionFormat = new ProcedureDescriptionFormat();
                procedureDescriptionFormat.setProcedureDescriptionFormat(procDescFormat);
                session.save(procedureDescriptionFormat);
                
            }
        }
        session.flush();
    }

    public static void insertFeatureOfInterestRelationShip(FeatureOfInterest parentFeature,
            FeatureOfInterest childFeature, Session session) {
        parentFeature.getFeatureOfInterestsForChildFeatureId().add(childFeature);
        session.saveOrUpdate(parentFeature);
        session.flush();
        // childFeature.getFeatureOfInterestsForParentFeatureId().add(parentFeature);
        // session.saveOrUpdate(childFeature);
        // session.flush();
    }

    public static void checkOrInsertResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellationOfferingObservationType obsConstOffObsType, FeatureOfInterest featureOfInterest, Session session)
            throws OwsExceptionReport {
        ObservationConstellation observationConstellation = obsConstOffObsType.getObservationConstellation();
        List<ResultTemplate> resultTemplates =
                HibernateCriteriaQueryUtilities.getResultTemplateObject(obsConstOffObsType.getOffering()
                        .getIdentifier(), observationConstellation.getObservableProperty().getIdentifier(), null,
                        session);
        if (!resultTemplateListContainsElements(resultTemplates)) {
            createAndSaveResultTemplate(request, obsConstOffObsType, featureOfInterest, session);
        } else {
            List<String> storedIdentifiers = new ArrayList<String>(0);
            for (ResultTemplate storedResultTemplate : resultTemplates) {
                storedIdentifiers.add(storedResultTemplate.getIdentifier());
                SosResultStructure storedStructure = new SosResultStructure(storedResultTemplate.getResultStructure());
                SosResultStructure newStructure = new SosResultStructure(request.getResultStructure().getXml());
                
                if (!storedStructure.equals(newStructure)) {
                    String exceptionText = String.format(
                                    "The requested resultStructure is different from already inserted result template " +
                                    "for procedure (%s) observedProperty (%s) and offering (%s)!",
                                    observationConstellation.getProcedure().getIdentifier(),
                                    observationConstellation.getObservableProperty().getIdentifier(),
                                    obsConstOffObsType.getOffering().getIdentifier());
                    LOGGER.error(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertResultTemplateParams.proposedTemplate.name(), exceptionText);
                }
                SosResultEncoding storedEncoding = new SosResultEncoding(storedResultTemplate.getResultEncoding());
                SosResultEncoding newEndoding = new SosResultEncoding(request.getResultEncoding().getXml());
                if (!storedEncoding.equals(newEndoding)) {
                    String exceptionText = String.format(
                                    "The requested resultEncoding is different from already inserted result template " +
                                    "for procedure (%s) observedProperty (%s) and offering (%s)!",
                                    observationConstellation.getProcedure().getIdentifier(),
                                    observationConstellation.getObservableProperty().getIdentifier(),
                                    obsConstOffObsType.getOffering().getIdentifier());
                    LOGGER.error(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertResultTemplateParams.proposedTemplate.name(), exceptionText);
                }
            }
            if (request.getIdentifier() != null && !storedIdentifiers.contains(request.getIdentifier())) {
                /* save it only if the identifier is different */
                createAndSaveResultTemplate(request, obsConstOffObsType, featureOfInterest, session);
            }
        }
    }

	private static boolean resultTemplateListContainsElements(List<ResultTemplate> resultTemplates)
	{
		return resultTemplates != null && !resultTemplates.isEmpty();
	}

    private static void createAndSaveResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellationOfferingObservationType obsConstOffObsType, FeatureOfInterest featureOfInterest, Session session) {
        ResultTemplate resultTemplate = new ResultTemplate();
        resultTemplate.setIdentifier(request.getIdentifier());
        resultTemplate.setObservationConstellationOfferingObservationType(obsConstOffObsType);
        resultTemplate.setFeatureOfInterest(featureOfInterest);
        resultTemplate.setResultStructure(request.getResultStructure().getXml());
        resultTemplate.setResultEncoding(request.getResultEncoding().getXml());
        session.save(resultTemplate);
        session.flush();
    }

    public static void insertObservationSingleValue(Set<ObservationConstellationOfferingObservationType> hObsConstOffObsTypes,
            FeatureOfInterest feature,
            SosObservation observation,
            Session session) {
            insertObservationSingleValueWithSetId(hObsConstOffObsTypes, feature, observation, null, session);
    }
    
    private static void insertObservationSingleValueWithSetId(Set<ObservationConstellationOfferingObservationType> observationConstellationOfferingObservationTypes,
            FeatureOfInterest feature,
            SosObservation sosObservation,
            String setId,
            Session session) {
        SosSingleObservationValue<?> value = (SosSingleObservationValue) sosObservation.getValue();
        Observation hObservation = new Observation();
        hObservation.setDeleted(false);
        if (sosObservation.isSetIdentifier()) {
            hObservation.setIdentifier(sosObservation.getIdentifier().getValue());
        }
        if (setId != null && !setId.isEmpty()) {
            hObservation.setSetId(setId);
        }
        Iterator<ObservationConstellationOfferingObservationType> iterator = observationConstellationOfferingObservationTypes.iterator();
        while (iterator.hasNext()) {
            ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType =
                    (ObservationConstellationOfferingObservationType) iterator.next();
            hObservation.setObservationConstellation(observationConstellationOfferingObservationType.getObservationConstellation());
            break;
        }
        hObservation.setFeatureOfInterest(feature);
        HibernateUtilities.addPhenomeonTimeAndResultTimeToObservation(hObservation, sosObservation.getPhenomenonTime(),
                sosObservation.getResultTime());
        HibernateUtilities.addValueToObservation(hObservation, value.getValue(), session);
        if (value.getValue().getUnit() != null) {
            hObservation.setUnit(HibernateCriteriaTransactionalUtilities.getOrInsertUnit(value.getValue().getUnit(),
                    session));
        }
        hObservation.setObservationConstellationOfferingObservationTypes(observationConstellationOfferingObservationTypes);
        HibernateCriteriaTransactionalUtilities.insertObservation(hObservation, session);
    }

    // TODO setID not yet tested - request observations of subset by id is working
    public static void insertObservationMutliValue(Set<ObservationConstellationOfferingObservationType> hObsConstOffObsTypes,
            FeatureOfInterest feature,
            SosObservation containerObservation,
            Session session) throws OwsExceptionReport {
        List<SosObservation> unfoldObservations = HibernateObservationUtilities.unfoldObservation(containerObservation);
        int subObservationIndex = 0;
        String setId = getSetId(containerObservation);
        for (SosObservation sosObservation : unfoldObservations) {
            String idExtension = subObservationIndex+"";
            setIdentifier(containerObservation, sosObservation, setId, idExtension);
            insertObservationSingleValueWithSetId(hObsConstOffObsTypes, feature, sosObservation, setId, session);
            subObservationIndex++;
        }
    }

    private static void setIdentifier(SosObservation containerObservation,
            SosObservation sosObservation,
            String antiSubsettingId,
            String idExtension)
    {
        if (containerObservation.getIdentifier() != null && containerObservation.getIdentifier().isSetValue()) {
            String subObservationIdentifier = String.format("%s-%s", antiSubsettingId, idExtension); 
            CodeWithAuthority subObsIdentifier = new CodeWithAuthority(subObservationIdentifier);
            subObsIdentifier.setCodeSpace(containerObservation.getIdentifier().getCodeSpace());
            sosObservation.setIdentifier(subObsIdentifier);    
        }
    }

    private static String getSetId(SosObservation containerObservation)
    {
        String antiSubsettingId = null;
        if (containerObservation.getIdentifier() != null) {
            antiSubsettingId = containerObservation.getIdentifier().getValue();
        }
        
        if (antiSubsettingId == null || antiSubsettingId.isEmpty()) {
            // if identifier of sweArrayObservation is not set, generate UUID for antisubsetting column
            antiSubsettingId = UUID.randomUUID().toString();
        }
        return antiSubsettingId;
    }

    private HibernateCriteriaTransactionalUtilities() {
    }

}
