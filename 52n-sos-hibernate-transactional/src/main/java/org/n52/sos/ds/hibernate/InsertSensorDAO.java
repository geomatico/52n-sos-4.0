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
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.ds.IInsertSensorDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosInsertionCapabilities;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertSensorDAO extends AbstractHibernateOperationDao implements IInsertSensorDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertSensorDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertSensor.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version)
            throws OwsExceptionReport {
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            opsMeta.addAnyParameterValue(Sos1Constants.RegisterSensorParams.SensorDescription);
            opsMeta.addAnyParameterValue(Sos1Constants.RegisterSensorParams.ObservationTemplate);
        } else {
            opsMeta.addAnyParameterValue(Sos2Constants.InsertSensorParams.procedureDescription);
            opsMeta.addPossibleValuesParameter(Sos2Constants.InsertSensorParams.procedureDescriptionFormat,
                    getCacheController().getProcedureDescriptionFormats());
            opsMeta.addAnyParameterValue(Sos2Constants.InsertSensorParams.observableProperty);
            opsMeta.addAnyParameterValue(Sos2Constants.InsertSensorParams.metadata);
            opsMeta.addDataTypeParameter(Sos2Constants.InsertSensorParams.metadata,
                    Sos2Constants.SCHEMA_LOCATION_INSERTION_CAPABILITIES);
        }
    }

    @Override
    public synchronized InsertSensorResponse insertSensor(InsertSensorRequest request) throws OwsExceptionReport {
        InsertSensorResponse response = new InsertSensorResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        String assignedProcedureID = request.getAssignedProcedureIdentifier();
        SosOffering firstAssignedOffering = request.getFirstAssignedOffering();
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            ProcedureDescriptionFormat procedureDescriptionFormat =
                    HibernateCriteriaQueryUtilities.getProcedureDescriptionFormatObject(
                            request.getProcedureDescriptionFormat(), session);
            List<ObservationType> observationTypes =
                    HibernateCriteriaQueryUtilities.getObservationTypeObjects(request.getMetadata()
                            .getObservationTypes(), session);
            List<FeatureOfInterestType> featureOfInterestTypes =
                    HibernateCriteriaQueryUtilities.getFeatureOfInterestTypeObjects(request.getMetadata()
                            .getFeatureOfInterestTypes(), session);
            if (procedureDescriptionFormat != null && observationTypes != null && featureOfInterestTypes != null) {
                Procedure procedure =
                        HibernateCriteriaTransactionalUtilities.getOrInsertProcedure(assignedProcedureID,
                                procedureDescriptionFormat, session);
                // TODO: set correct validTime,
                HibernateCriteriaTransactionalUtilities.insertValidProcedureTime(
                        procedure,
                        getSensorDescriptionFromProcedureDescription(request.getProcedureDescription(),
                                assignedProcedureID), new DateTime(), session);
                List<ObservableProperty> obsProps =
                        getOrInsertNewObservableProperties(request.getObservableProperty(), session);
                for (SosOffering assignedOffering : request.getAssignedOfferings()) {
                    Offering offering =
                            getAndUpdateOrInsertNewOffering(assignedOffering, request.getRelatedFeatures(), observationTypes,
                                    featureOfInterestTypes, session);
                    for (ObservableProperty observableProperty : obsProps) {
                        ObservationConstellation obsConst =
                                HibernateCriteriaTransactionalUtilities.checkOrInsertObservationConstellation(
                                        procedure, observableProperty, session);
                        HibernateCriteriaTransactionalUtilities
                                .checkOrInsertObservationConstellationOfferingObservationType(obsConst, offering,
                                        session);
                    }
                }
                // TODO: parent and child procedures
                response.setAssignedProcedure(assignedProcedureID);
                response.setAssignedOffering(firstAssignedOffering.getOfferingIdentifier());
            } else if (procedureDescriptionFormat == null && observationTypes != null
                    && featureOfInterestTypes != null) {
                // TODO: invalid parameter value procDescFormat
            } else {
                // TODO: exception DB not initialized
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            String exceptionText = "Error while inserting sensor data into database!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
        return response;
    }

    private Offering getAndUpdateOrInsertNewOffering(SosOffering assignedOffering, List<SosFeatureRelationship> relatedFeatures,
            List<ObservationType> observationTypes, List<FeatureOfInterestType> featureOfInterestTypes, Session session)
            throws OwsExceptionReport {
        List<RelatedFeature> hRelatedFeatures = new LinkedList<RelatedFeature>();
        if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
            for (SosFeatureRelationship relatedFeature : relatedFeatures) {
                List<RelatedFeatureRole> relatedFeatureRoles =
                        HibernateCriteriaTransactionalUtilities.getOrInsertRelatedFeatureRole(
                                relatedFeature.getRole(), session);
                hRelatedFeatures.addAll(HibernateCriteriaTransactionalUtilities.getOrInsertRelatedFeature(
                        relatedFeature.getFeature(), relatedFeatureRoles, session));
            }
        }
        return HibernateCriteriaTransactionalUtilities.getAndUpdateOrInsertNewOffering(assignedOffering.getOfferingIdentifier(),
                assignedOffering.getOfferingName(), hRelatedFeatures, observationTypes, featureOfInterestTypes,
                session);
    }

    private List<ObservableProperty> getOrInsertNewObservableProperties(List<String> obsProps, Session session) {
        List<SosObservableProperty> observableProperties = new ArrayList<SosObservableProperty>(obsProps.size());
        for (String observableProperty : obsProps) {
            observableProperties.add(new SosObservableProperty(observableProperty));
        }
        return HibernateCriteriaTransactionalUtilities.getOrInsertObservableProperty(observableProperties, session);
    }

    private String getSensorDescriptionFromProcedureDescription(SosProcedureDescription procedureDescription,
            String procedureIdentifier) {
        if (procedureDescription instanceof SensorML) {
            SensorML sensorML = (SensorML) procedureDescription;
            // if SensorML is not a wrapper
            if (!sensorML.isWrapper()) {
                return sensorML.getSensorDescriptionXmlString();
            }
            // if SensorML is a wrapper and member size is 1
            else if (sensorML.isWrapper() && sensorML.getMembers().size() == 1) {
                return sensorML.getMembers().get(0).getSensorDescriptionXmlString();
            } else {
                // TODO: get sensor description for procedure identifier
                return "";
            }
        }
        // if procedureDescription not SensorML
        else {
            return procedureDescription.getSensorDescriptionXmlString();
        }
    }

    @Override
    public IExtension getExtensionHelper() throws OwsExceptionReport {
        SosInsertionCapabilities insertionCapabilities = new SosInsertionCapabilities();
        insertionCapabilities.addFeatureOfInterestTypes(getCacheController().getFeatureOfInterestTypes());
        insertionCapabilities.addObservationTypes(getCacheController().getObservationTypes());
        insertionCapabilities.addProcedureDescriptionFormats(getCacheController().getProcedureDescriptionFormats());

        return insertionCapabilities;
    }

}
