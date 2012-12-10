/**
 * Copyright (C) 2012
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Session;
import org.joda.time.DateTime;
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

    public static Procedure getOrInsertProcedure(String identifier, ProcedureDescriptionFormat pdf,
            List<ObservationType> observationTypes, List<FeatureOfInterestType> featureOfInterestTypes, Session session) {
        Procedure result = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(identifier, session);
        if (result == null) {
            result = new Procedure();
            result.setProcedureDescriptionFormat(pdf);
            result.setIdentifier(identifier);
            result.setDeleted(false);
            result.setObservationTypes(new HashSet<ObservationType>(observationTypes));
            result.setFeatureOfInterestTypes(new HashSet<FeatureOfInterestType>(featureOfInterestTypes));
            result.setProcedureId((Long) session.save(result));
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
            List<RelatedFeature> relatedFeatures, List<ObservationType> observationTypes, Session session) {
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
        session.saveOrUpdate(offering);
        session.flush();
        return offering;
    }

    public static List<RelatedFeature> getOrInsertRelatedFeature(SosAbstractFeature feature,
            List<RelatedFeatureRole> roles, Session session) throws OwsExceptionReport {
        // TODO: create featureOfInterest and link to relatedFeature
        List<RelatedFeature> relFeats =
                HibernateCriteriaQueryUtilities.getRelatedFeatures(feature.getIdentifier(), session);
        if (relFeats == null || (relFeats != null && relFeats.isEmpty())) {
            RelatedFeature relFeat = new RelatedFeature();
            String identifier = feature.getIdentifier();
            String url = null;
            if (feature instanceof SosSamplingFeature) {
                identifier =
                        Configurator.getInstance().getFeatureQueryHandler()
                                .insertFeature((SosSamplingFeature) feature, session);
                url = ((SosSamplingFeature) feature).getUrl();
            }
            relFeat.setFeatureOfInterest(getOrInsertFeatureOfInterest(identifier, url, session));
            relFeat.setRelatedFeatureRoles(new HashSet<RelatedFeatureRole>(roles));
            relFeat.setRelatedFeatureId((Long) session.save(relFeat));
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
            relFeatRole.setRelatedFeatureRoleId((Long) session.save(relFeatRole));
            session.flush();
            relFeatRoles.add(relFeatRole);
        }
        return relFeatRoles;
    }

    public static List<ObservableProperty> getOrInsertObservableProperty(
            List<SosObservableProperty> observableProperty, Session session) {
        List<String> identifiers = new ArrayList<String>();
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
                obsProp.setObservablePropertyId((Long) session.save(obsProp));
                session.flush();
                obsProps.add(obsProp);
            }
        }
        return obsProps;
    }

    public static Unit getOrInsertUnit(String unit, Session session) {
        Unit result = HibernateCriteriaQueryUtilities.getUnit(unit, session);
        if (result == null) {
            result = new Unit();
            result.setUnit(unit);
            result.setUnitId((Long) session.save(result));
            session.flush();
        }
        return result;
    }

    public static void checkOrInsertObservationConstellation(Procedure proc, List<ObservableProperty> obsProps,
            Offering offering, Session session) {
        for (ObservableProperty observableProperty : obsProps) {
            ObservationConstellation obsConst = new ObservationConstellation();
            obsConst.setObservableProperty(observableProperty);
            obsConst.setOffering(offering);
            obsConst.setProcedure(proc);
            session.save(obsConst);
            session.flush();
        }
    }

    public static void updateValidProcedureTime(ValidProcedureTime validProcedureTime, Session session) {
        session.saveOrUpdate(validProcedureTime);
    }

    public static ObservationConstellation updateObservationConstellation(ObservationConstellation obsConst,
            String observationType, Session session) {
        ObservationType obsType = HibernateCriteriaQueryUtilities.getObservationTypeObject(observationType, session);
        obsConst.setObservationType(obsType);
        session.saveOrUpdate(obsConst);
        return obsConst;
    }

    public static FeatureOfInterest getOrInsertFeatureOfInterest(String featureIdentifier, String url, Session session) {
        FeatureOfInterest feature = HibernateCriteriaQueryUtilities.getFeatureOfInterest(featureIdentifier, session);
        if (feature == null) {
            feature = new FeatureOfInterest();
            feature.setIdentifier(featureIdentifier);
            if (url != null && !url.isEmpty()) {
                feature.setUrl(url);
            }
            Long id = (Long) session.save(feature);
            session.flush();
            feature.setFeatureOfInterestId(id);
            session.saveOrUpdate(feature);
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
            Long id = (Long) session.save(booleanValue);
            session.flush();
            booleanValue.setBooleanValueId(id);
            session.update(booleanValue);
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
            Long id = (Long) session.save(categoryValue);
            session.flush();
            categoryValue.setCategoryValueId(id);
            session.update(categoryValue);
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
            Long id = (Long) session.save(countValue);
            session.flush();
            countValue.setCountValueId(id);
            session.update(countValue);
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
            Long id = (Long) session.save(geomtryValue);
            session.flush();
            geomtryValue.setGeometryValueId(id);
            session.update(geomtryValue);
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
            Long id = (Long) session.save(numericValue);
            session.flush();
            numericValue.setNumericValueId(id);
            session.update(numericValue);
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
            Long id = (Long) session.save(textValue);
            session.flush();
            textValue.setTextValueId(id);
            session.update(textValue);
            session.flush();
        }
        Set<TextValue> values = new HashSet<TextValue>(1);
        values.add(textValue);
        return values;
    }

    public static Observation insertObservation(Observation observation, Session session) {
        Long id = (Long) session.save(observation);
        session.flush();
        observation.setObservationId(id);
        session.update(observation);
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
                Long id = (Long) session.save(featureOfInterestType);
                session.flush();
                featureOfInterestType.setFeatureOfInterestTypeId(id);
                session.update(featureOfInterestType);
                session.flush();
            }
        }
    }

    public static void insertObservationTypes(Set<String> obsTypes, Session session) {
        for (String obsType : obsTypes) {
            ObservationType observationType =
                    HibernateCriteriaQueryUtilities.getObservationTypeObject(obsType, session);
            if (observationType == null) {
                observationType = new ObservationType();
                observationType.setObservationType(obsType);
                Long id = (Long) session.save(observationType);
                session.flush();
                observationType.setObservationTypeId(id);
                session.update(observationType);
                session.flush();
            }
        }
    }

    public static void insertProcedureDescriptionsFormats(Set<String> procDescFormats, Session session) {
        for (String procDescFormat : procDescFormats) {
            ProcedureDescriptionFormat procedureDescriptionFormat =
                    HibernateCriteriaQueryUtilities.getProcedureDescriptionFormatObject(procDescFormat, session);
            if (procedureDescriptionFormat == null) {
                procedureDescriptionFormat = new ProcedureDescriptionFormat();
                procedureDescriptionFormat.setProcedureDescriptionFormat(procDescFormat);
                Long id = (Long) session.save(procedureDescriptionFormat);
                session.flush();
                procedureDescriptionFormat.setProcedureDescriptionFormatId(id);
                session.update(procedureDescriptionFormat);
                session.flush();
            }
        }
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
            ObservationConstellation observationConstellation, FeatureOfInterest featureOfInterest, Session session)
            throws OwsExceptionReport {
        List<String> features = new ArrayList<String>();
        features.add(featureOfInterest.getIdentifier());
        List<ResultTemplate> resultTemplates =
                HibernateCriteriaQueryUtilities.getResultTemplateObject(observationConstellation.getOffering()
                        .getIdentifier(), observationConstellation.getObservableProperty().getIdentifier(), features,
                        session);
        if (!resultTemplateListContainsElements(resultTemplates)) {
            createAndSaveResultTemplate(request, observationConstellation, featureOfInterest, session);
        } else {
        	// TODO Iterate over result templates and throw exception after checking all?!
            ResultTemplate storedResultTemplate = resultTemplates.get(0);
            if (!storedResultTemplate.getResultStructure().equals(request.getResultStructure().getXml())) {
                String exceptionText = String.format(
                		"The requested resultStructure is different from already inserted result template " +
                		"for procedure (%s) observedProperty (%s) and offering (%s)!",
                		observationConstellation.getProcedure().getIdentifier(),
                		observationConstellation.getObservableProperty().getIdentifier(),
                		observationConstellation.getOffering().getIdentifier());
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(
                        Sos2Constants.InsertResultTemplateParams.resultStructure.name(), exceptionText);
            }
            createAndSaveResultTemplate(request, observationConstellation, featureOfInterest, session);
        }
    }

	private static boolean resultTemplateListContainsElements(List<ResultTemplate> resultTemplates)
	{
		return resultTemplates != null && !resultTemplates.isEmpty();
	}

    private static void createAndSaveResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellation observationConstellation, FeatureOfInterest featureOfInterest, Session session) {
        ResultTemplate resultTemplate = new ResultTemplate();
        resultTemplate.setIdentifier(request.getIdentifier());
        resultTemplate.setObservationConstellation(observationConstellation);
        resultTemplate.setFeatureOfInterest(featureOfInterest);
        resultTemplate.setResultStructure(request.getResultStructure().getXml());
        resultTemplate.setResultEncoding(request.getResultEncoding().getXml());
        session.save(resultTemplate);
        session.flush();
    }

    public static void insertObservationSingleValue(ObservationConstellation obsConst,
            FeatureOfInterest feature,
            SosObservation observation,
            Session session) {
        insertObservationSingleValueWithAntiSubSettingId(obsConst, feature, observation, null, session);
    }
    
    private static void insertObservationSingleValueWithAntiSubSettingId(ObservationConstellation obsConst,
            FeatureOfInterest feature,
            SosObservation observation,
            String antiSubsettingId,
            Session session) {
        SosSingleObservationValue value = (SosSingleObservationValue) observation.getValue();
        Observation hObservation = new Observation();
        hObservation.setDeleted(false);
        if (observation.getIdentifier() != null && !observation.getIdentifier().isSetValue()) {
            hObservation.setIdentifier(observation.getIdentifier().getValue());
        }
        if (antiSubsettingId != null && !antiSubsettingId.isEmpty()) {
            hObservation.setAntiSubsetting(antiSubsettingId);
        }
        hObservation.setObservationConstellation(obsConst);
        hObservation.setFeatureOfInterest(feature);
        HibernateUtilities.addPhenomeonTimeAndResultTimeToObservation(hObservation, observation.getPhenomenonTime(),
                observation.getResultTime());
        HibernateUtilities.addValueToObservation(hObservation, value.getValue(), session);
        if (value.getValue().getUnit() != null) {
            hObservation.setUnit(HibernateCriteriaTransactionalUtilities.getOrInsertUnit(value.getValue().getUnit(),
                    session));
        }
        HibernateCriteriaTransactionalUtilities.insertObservation(hObservation, session);
    }

    // TODO antisubsetting not yet tested - request observations of subset by id is working
    public static void insertObservationMutliValue(ObservationConstellation obsConst,
            FeatureOfInterest feature,
            SosObservation containerObservation,
            Session session) throws OwsExceptionReport {
        List<SosObservation> unfoldObservations = HibernateObservationUtilities.unfoldObservation(containerObservation);
        int subObservationIndex = 0;
        for (SosObservation sosObservation : unfoldObservations) {
            String antiSubsettingId = getAntiSubsettingId(containerObservation);
            setIdentifier(containerObservation, sosObservation, antiSubsettingId, subObservationIndex+"");
            insertObservationSingleValueWithAntiSubSettingId(obsConst, feature, sosObservation, antiSubsettingId, session);
            subObservationIndex++;
        }
    }

    private static void setIdentifier(SosObservation containerObservation,
            SosObservation sosObservation,
            String antiSubsettingId,
            String idExtension)
    {
        if (containerObservation.getIdentifier() != null && !containerObservation.getIdentifier().isSetValue()) {
            String subObservationIdentifier = String.format("%s-%s", antiSubsettingId, idExtension); 
            sosObservation.setIdentifier(new CodeWithAuthority(subObservationIdentifier));    
        }
    }

    private static String getAntiSubsettingId(SosObservation containerObservation)
    {
        String antiSubsettingId = containerObservation.getIdentifier().getValue();
        if (antiSubsettingId == null || antiSubsettingId.isEmpty()) {
            // if identifier of sweArrayObservation is not set, generate UUID for antisubsetting column
            antiSubsettingId = UUID.randomUUID().toString();
        }
        return antiSubsettingId;
    }

}
