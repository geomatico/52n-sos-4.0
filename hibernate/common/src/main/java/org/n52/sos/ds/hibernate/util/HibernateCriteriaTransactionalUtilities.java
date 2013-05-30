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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.sos.ds.hibernate.entities.Codespace;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
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
import org.n52.sos.ds.hibernate.entities.TFeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.TOffering;
import org.n52.sos.ds.hibernate.entities.TProcedure;
import org.n52.sos.ds.hibernate.entities.Unit;
import org.n52.sos.ds.hibernate.entities.ValidProcedureTime;
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.ogc.OGCConstants;
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
import org.n52.sos.util.CollectionHelper;

public class HibernateCriteriaTransactionalUtilities {

    public static Procedure getOrInsertProcedure(String identifier, ProcedureDescriptionFormat pdf,
            Collection<String> parentProcedures, Session session) {
        Procedure result = HibernateCriteriaQueryUtilities.getProcedureForIdentifier(identifier, session);
        if (result == null) {
            TProcedure newResult = new TProcedure();
            newResult.setProcedureDescriptionFormat(pdf);
            newResult.setIdentifier(identifier);
            if (CollectionHelper.isNotEmpty(parentProcedures)) {
                newResult.setParents(CollectionHelper.asSet(HibernateCriteriaQueryUtilities
                        .getProceduresForIdentifiers(parentProcedures, session)));
            }
            result = newResult;
        }
        result.setDeleted(false);
        session.saveOrUpdate(result);
        session.flush();
        session.refresh(result);
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

    public static Offering getAndUpdateOrInsertNewOffering(String offeringIdentifier, String offeringName,
            List<RelatedFeature> relatedFeatures, List<ObservationType> observationTypes,
            List<FeatureOfInterestType> featureOfInterestTypes, Session session) {

        TOffering offering = HibernateCriteriaQueryUtilities.getTOfferingForIdentifier(offeringIdentifier, session);
        if (offering == null) {
            offering = new TOffering();
            offering.setIdentifier(offeringIdentifier);
            if (offeringName != null) {
                offering.setName(offeringName);
            } else {
                offering.setName("Offering for the procedure " + offeringIdentifier);
            }
        }
        if (!relatedFeatures.isEmpty()) {
            offering.setRelatedFeatures(new HashSet<RelatedFeature>(relatedFeatures));
        } else {
            offering.setRelatedFeatures(new HashSet<RelatedFeature>(0));
        }
        if (!observationTypes.isEmpty()) {
            offering.setObservationTypes(new HashSet<ObservationType>(observationTypes));
        } else {
            offering.setObservationTypes(new HashSet<ObservationType>(0));
        }
        if (!featureOfInterestTypes.isEmpty()) {
            offering.setFeatureOfInterestTypes(new HashSet<FeatureOfInterestType>(featureOfInterestTypes));
        } else {
            offering.setFeatureOfInterestTypes(new HashSet<FeatureOfInterestType>(0));
        }
        session.saveOrUpdate(offering);
        session.flush();
        session.refresh(offering);
        return offering;
    }

    public static List<RelatedFeature> getOrInsertRelatedFeature(SosAbstractFeature feature,
            List<RelatedFeatureRole> roles, Session session) throws OwsExceptionReport {
        // TODO: create featureOfInterest and link to relatedFeature
        List<RelatedFeature> relFeats =
                HibernateCriteriaQueryUtilities.getRelatedFeatures(feature.getIdentifier().getValue(), session);
        if (relFeats == null) {
            relFeats = new LinkedList<RelatedFeature>();
        }
        if (relFeats.isEmpty()) {
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
        if (relFeatRoles == null) {
            relFeatRoles = new LinkedList<RelatedFeatureRole>();
        }
        if (relFeatRoles.isEmpty()) {
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
                session.flush();
                session.refresh(obsProp);
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
            session.save(result);
            session.flush();
            session.refresh(result);
        }
        return result;
    }

    public static Codespace getOrInsertCodespace(String codespace, Session session) {
        Codespace result = HibernateCriteriaQueryUtilities.getCodespace(codespace, session);
        if (result == null) {
            result = new Codespace();
            result.setCodespace(codespace);
            session.save(result);
            session.flush();
            session.refresh(result);
        }
        return result;
    }

    public static ObservationConstellation checkOrInsertObservationConstellation(Procedure hProcedure,
            ObservableProperty hObservableProperty, Offering hOffering, boolean hiddenChild, Session session) {
        ObservationConstellation obsConst =
                (ObservationConstellation) session.createCriteria(ObservationConstellation.class)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .add(Restrictions.eq(ObservationConstellation.OFFERING, hOffering))
                        .add(Restrictions.eq(ObservationConstellation.OBSERVABLE_PROPERTY, hObservableProperty))
                        .add(Restrictions.eq(ObservationConstellation.PROCEDURE, hProcedure))
                        .add(Restrictions.eq(ObservationConstellation.HIDDEN_CHILD, hiddenChild)).uniqueResult();
        if (obsConst == null) {
            obsConst = new ObservationConstellation();
            obsConst.setObservableProperty(hObservableProperty);
            obsConst.setProcedure(hProcedure);
            obsConst.setOffering(hOffering);
            obsConst.setDeleted(false);
            obsConst.setHiddenChild(hiddenChild);
            session.save(obsConst);
            session.flush();
            session.refresh(obsConst);
        } else if (obsConst.getDeleted()) {
            obsConst.setDeleted(false);
            session.save(obsConst);
            session.flush();
            session.refresh(obsConst);
        }
        return obsConst;
    }

    public static void updateValidProcedureTime(ValidProcedureTime validProcedureTime, Session session) {
        session.saveOrUpdate(validProcedureTime);
    }

    public static ObservationConstellation updateObservationConstellation(
            ObservationConstellation hObservationConstellation, String observationType, Session session) {
        ObservationType obsType = HibernateCriteriaQueryUtilities.getObservationTypeObject(observationType, session);        
        hObservationConstellation.setObservationType(obsType);
        session.saveOrUpdate(hObservationConstellation);
        
        //update hidden child observation constellations
        Set<String> offerings = new HashSet<String>(Configurator.getInstance().getCache().getOfferingsForProcedure(
                hObservationConstellation.getProcedure().getIdentifier()));
        offerings.remove(hObservationConstellation.getOffering().getIdentifier());
        
        @SuppressWarnings("unchecked")
        List<ObservationConstellation> hiddenChildObsConsts = session.createCriteria(ObservationConstellation.class)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .add(Restrictions.eq(ObservationConstellation.OBSERVABLE_PROPERTY, hObservationConstellation.getObservableProperty()))
                .add(Restrictions.eq(ObservationConstellation.PROCEDURE, hObservationConstellation.getProcedure()))
                .add(Restrictions.eq(ObservationConstellation.HIDDEN_CHILD, true))
                .createCriteria(ObservationConstellation.OFFERING)
                    .add(Restrictions.in(Offering.IDENTIFIER, offerings))
                .list();
        for (ObservationConstellation hiddenChildObsConst : hiddenChildObsConsts) {
            hiddenChildObsConst.setObservationType(obsType);
            session.saveOrUpdate(hiddenChildObsConst);
        }
        
        return hObservationConstellation;
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
            feature.setUrl(url);
            session.saveOrUpdate(feature);
            session.flush();
        }
        return feature;
    }

    public static List<FeatureOfInterestType> getOrInsertFeatureOfInterestTypes(Set<String> featureOfInterestTypes,
            Session session) {
        List<FeatureOfInterestType> featureTypes = new LinkedList<FeatureOfInterestType>();
        for (String featureType : featureOfInterestTypes) {
            featureTypes.add(getOrInsertFeatureOfInterestType(featureType, session));
        }
        return featureTypes;
    }

    public static FeatureOfInterestType getOrInsertFeatureOfInterestType(String featureType, Session session) {
        FeatureOfInterestType featureOfInterestType =
                HibernateCriteriaQueryUtilities.getFeatureOfInterestTypeObject(featureType, session);
        if (featureOfInterestType == null) {
            featureOfInterestType = new FeatureOfInterestType();
            featureOfInterestType.setFeatureOfInterestType(featureType);
            session.save(featureOfInterestType);
            session.flush();
        }
        return featureOfInterestType;
    }

    public static ObservationType getOrInsertObservationType(String obsType, Session session) {
        ObservationType observationType = HibernateCriteriaQueryUtilities.getObservationTypeObject(obsType, session);
        if (observationType == null) {
            observationType = new ObservationType();
            observationType.setObservationType(obsType);
            session.save(observationType);
            session.flush();
        }
        return observationType;
    }

    public static List<ObservationType> getOrInsertObservationTypes(Set<String> observationTypes, Session session) {
        List<ObservationType> obsTypes = new LinkedList<ObservationType>();
        for (String observationType : observationTypes) {
            obsTypes.add(getOrInsertObservationType(observationType, session));
        }
        return obsTypes;
    }

    public static ProcedureDescriptionFormat getOrInsertProcedureDescriptionFormat(String procDescFormat,
            Session session) {
        ProcedureDescriptionFormat procedureDescriptionFormat =
                HibernateCriteriaQueryUtilities.getProcedureDescriptionFormatObject(procDescFormat, session);
        if (procedureDescriptionFormat == null) {
            procedureDescriptionFormat = new ProcedureDescriptionFormat();
            procedureDescriptionFormat.setProcedureDescriptionFormat(procDescFormat);
            session.save(procedureDescriptionFormat);
            session.flush();
        }
        return procedureDescriptionFormat;
    }

    public static void insertFeatureOfInterestRelationShip(TFeatureOfInterest parentFeature,
            FeatureOfInterest childFeature, Session session) {
        parentFeature.getChilds().add(childFeature);
        session.saveOrUpdate(parentFeature);
        session.flush();
    }

    public static void checkOrInsertResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellation hObservationConstellation, FeatureOfInterest featureOfInterest, Session session)
            throws OwsExceptionReport {
        List<ResultTemplate> resultTemplates =
                HibernateCriteriaQueryUtilities.getResultTemplateObject(hObservationConstellation.getOffering()
                        .getIdentifier(), hObservationConstellation.getObservableProperty().getIdentifier(), null,
                        session);
        if (!resultTemplateListContainsElements(resultTemplates)) {
            createAndSaveResultTemplate(request, hObservationConstellation, featureOfInterest, session);
        } else {
            List<String> storedIdentifiers = new ArrayList<String>(0);
            for (ResultTemplate storedResultTemplate : resultTemplates) {
                storedIdentifiers.add(storedResultTemplate.getIdentifier());
                SosResultStructure storedStructure =
                        new SosResultStructure(storedResultTemplate.getResultStructure());
                SosResultStructure newStructure = new SosResultStructure(request.getResultStructure().getXml());

                if (!storedStructure.equals(newStructure)) {
                    throw new InvalidParameterValueException().at(
                            Sos2Constants.InsertResultTemplateParams.proposedTemplate).withMessage(
                            "The requested resultStructure is different from already inserted result template "
                                    + "for procedure (%s) observedProperty (%s) and offering (%s)!",
                            hObservationConstellation.getProcedure().getIdentifier(),
                            hObservationConstellation.getObservableProperty().getIdentifier(),
                            hObservationConstellation.getOffering().getIdentifier());
                }
                SosResultEncoding storedEncoding =
                        new SosResultEncoding(storedResultTemplate.getResultEncoding());
                SosResultEncoding newEndoding = new SosResultEncoding(request.getResultEncoding().getXml());
                if (!storedEncoding.equals(newEndoding)) {
                    throw new InvalidParameterValueException().at(
                            Sos2Constants.InsertResultTemplateParams.proposedTemplate).withMessage(
                            "The requested resultEncoding is different from already inserted result template "
                                    + "for procedure (%s) observedProperty (%s) and offering (%s)!",
                            hObservationConstellation.getProcedure().getIdentifier(),
                            hObservationConstellation.getObservableProperty().getIdentifier(),
                            hObservationConstellation.getOffering().getIdentifier());
                }
            }
            if (request.getIdentifier() != null && !storedIdentifiers.contains(request.getIdentifier())) {
                /* save it only if the identifier is different */
                createAndSaveResultTemplate(request, hObservationConstellation, featureOfInterest, session);
            }
        }
    }

    private static boolean resultTemplateListContainsElements(List<ResultTemplate> resultTemplates) {
        return resultTemplates != null && !resultTemplates.isEmpty();
    }

    private static void createAndSaveResultTemplate(InsertResultTemplateRequest request,
            ObservationConstellation obsConst, FeatureOfInterest featureOfInterest, Session session) {
        ResultTemplate resultTemplate = new ResultTemplate();
        resultTemplate.setIdentifier(request.getIdentifier());
        resultTemplate.setProcedure(obsConst.getProcedure());
        resultTemplate.setObservableProperty(obsConst.getObservableProperty());
        resultTemplate.setOffering(obsConst.getOffering());
        resultTemplate.setFeatureOfInterest(featureOfInterest);
        resultTemplate.setResultStructure(request.getResultStructure().getXml());
        resultTemplate.setResultEncoding(request.getResultEncoding().getXml());
        session.save(resultTemplate);
        session.flush();
    }

    public static void insertObservationSingleValue(Set<ObservationConstellation> observationConstellations,
            FeatureOfInterest feature, SosObservation observation, Session session) throws CodedException {
        insertObservationSingleValueWithSetId(observationConstellations, feature, observation, null, session);
    }

    @SuppressWarnings("rawtypes")
    private static void insertObservationSingleValueWithSetId(Set<ObservationConstellation> observationConstellations,
            FeatureOfInterest feature, SosObservation sosObservation, String setId, Session session) throws CodedException {
        SosSingleObservationValue<?> value = (SosSingleObservationValue) sosObservation.getValue();
        Observation hObservation = HibernateUtilities.createObservationFromValue(value.getValue(), session);
        hObservation.setDeleted(false);
        if (sosObservation.isSetIdentifier()) {
            hObservation.setIdentifier(sosObservation.getIdentifier().getValue());
            if (sosObservation.getIdentifier().isSetCodeSpace()) {
                hObservation.setCodespace(HibernateCriteriaTransactionalUtilities.getOrInsertCodespace(sosObservation
                        .getIdentifier().getCodeSpace(), session));
            }
        }
        if (!hObservation.isSetCodespace()) {
            hObservation.setCodespace(HibernateCriteriaTransactionalUtilities.getOrInsertCodespace(
                    OGCConstants.UNKNOWN, session));
        }

        if (setId != null && !setId.isEmpty()) {
            hObservation.setSetId(setId);
        }
        Iterator<ObservationConstellation> iterator = observationConstellations.iterator();
        boolean firstObsConst = true;
        while (iterator.hasNext()) {
            ObservationConstellation observationConstellation = iterator.next();
            if (firstObsConst) {
                //TODO should subsequent obsConsts be checked for obsProp and procedure agreement with the first?
                hObservation.setObservableProperty(observationConstellation.getObservableProperty());
                hObservation.setProcedure(observationConstellation.getProcedure());
                firstObsConst = false;
            }
            hObservation.getOfferings().add(observationConstellation.getOffering());
        }
        hObservation.setFeatureOfInterest(feature);
        HibernateUtilities.addPhenomeonTimeAndResultTimeToObservation(hObservation,
                sosObservation.getPhenomenonTime(), sosObservation.getResultTime());

        if (value.getValue().getUnit() != null) {
            hObservation.setUnit(HibernateCriteriaTransactionalUtilities.getOrInsertUnit(value.getValue().getUnit(),
                    session));
        }

        // TODO if this observation is a deleted=true, how to set deleted=false
        // instead of insert
        session.saveOrUpdate(hObservation);
        session.flush();
    }

    // TODO setID not yet tested - request observations of subset by id is
    // working
    public static void insertObservationMutliValue(Set<ObservationConstellation> hObsConsts,
            FeatureOfInterest feature, SosObservation containerObservation, Session session) throws OwsExceptionReport {
        List<SosObservation> unfoldObservations =
                HibernateObservationUtilities.unfoldObservation(containerObservation);
        int subObservationIndex = 0;
        String setId = getSetId(containerObservation);
        for (SosObservation sosObservation : unfoldObservations) {
            String idExtension = subObservationIndex + "";
            setIdentifier(containerObservation, sosObservation, setId, idExtension);
            insertObservationSingleValueWithSetId(hObsConsts, feature, sosObservation, setId, session);
            subObservationIndex++;
        }
    }

    private static void setIdentifier(SosObservation containerObservation, SosObservation sosObservation,
            String antiSubsettingId, String idExtension) {
        if (containerObservation.isSetIdentifier()) {
            String subObservationIdentifier = String.format("%s-%s", antiSubsettingId, idExtension);
            CodeWithAuthority subObsIdentifier = new CodeWithAuthority(subObservationIdentifier);
            subObsIdentifier.setCodeSpace(containerObservation.getIdentifier().getCodeSpace());
            sosObservation.setIdentifier(subObsIdentifier);
        }
    }

    private static String getSetId(SosObservation containerObservation) {
        String antiSubsettingId = null;
        if (containerObservation.getIdentifier() != null) {
            antiSubsettingId = containerObservation.getIdentifier().getValue();
        }

        if (antiSubsettingId == null || antiSubsettingId.isEmpty()) {
            // if identifier of sweArrayObservation is not set, generate UUID
            // for antisubsetting column
            antiSubsettingId = UUID.randomUUID().toString();
        }
        return antiSubsettingId;
    }

    public static void setValidProcedureDescriptionEndTime(String procedureIdentifier, Session session) {
        TProcedure procedure =
                HibernateCriteriaQueryUtilities.getTProcedureForIdentifier(procedureIdentifier, session);
        Set<ValidProcedureTime> validProcedureTimes = procedure.getValidProcedureTimes();
        for (ValidProcedureTime validProcedureTime : validProcedureTimes) {
            if (validProcedureTime.getEndTime() == null) {
                validProcedureTime.setEndTime(new DateTime().toDate());
                HibernateCriteriaTransactionalUtilities.updateValidProcedureTime(validProcedureTime, session);
            }
        }
    }

    private HibernateCriteriaTransactionalUtilities() {
    }
}
