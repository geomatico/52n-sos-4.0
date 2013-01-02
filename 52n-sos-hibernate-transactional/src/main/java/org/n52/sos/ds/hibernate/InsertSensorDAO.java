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
package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IInsertSensorDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
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
import org.n52.sos.ogc.ows.OWSParameterDataType;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosInsertionCapabilities;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosFeatureRelationship;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.util.SosHelper;
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
    public OWSOperation getOperationsMetadata(String service, String version, Session session)
            throws OwsExceptionReport {

        Map<String, List<String>> dcpMap = getDCP(new DecoderKeyType(version.equals(Sos1Constants.SERVICEVERSION) ? 
                                                        Sos1Constants.NS_SOS : SWEConstants.NS_SWES_20));
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            if (version.equals(Sos1Constants.SERVICEVERSION)) {
                // set operation name
                opsMeta.setOperationName(OPERATION_NAME);
                // set DCP
                opsMeta.setDcp(dcpMap);
                // set param sensorDescription
                opsMeta.addParameterValue(Sos1Constants.RegisterSensorParams.SensorDescription.name(),
                        new OWSParameterValuePossibleValues(new ArrayList<String>(1)));
                // set observationTemplate
                opsMeta.addParameterValue(Sos1Constants.RegisterSensorParams.ObservationTemplate.name(),
                        new OWSParameterValuePossibleValues(new ArrayList<String>(1)));
            } else {
                // set operation name
                opsMeta.setOperationName(OPERATION_NAME);
                // set DCP
                opsMeta.setDcp(dcpMap);
                // set param procedureDescription
                opsMeta.addParameterValue(Sos2Constants.InsertSensorParams.procedureDescription.name(),
                        new OWSParameterValuePossibleValues(new ArrayList<String>(1)));
                // set param procedureDescriptionFormat
                opsMeta.addParameterValue(
                        Sos2Constants.InsertSensorParams.procedureDescriptionFormat.name(),
                        new OWSParameterValuePossibleValues(HibernateCriteriaQueryUtilities
                                .getProcedureDescriptionFormatIdentifiers(session)));
                // set param observableProperty
                opsMeta.addParameterValue(Sos2Constants.InsertSensorParams.observableProperty.name(),
                        new OWSParameterValuePossibleValues(new ArrayList<String>(1)));
                // set param metadata
                opsMeta.addParameterValue(Sos2Constants.InsertSensorParams.metadata.name(),
                        new OWSParameterValuePossibleValues(new ArrayList<String>(0)));
                opsMeta.addParameterValue(Sos2Constants.InsertSensorParams.metadata.name(), new OWSParameterDataType(
                        "http://schemas.opengis.net/sos/2.0/sosInsertionCapabilities.xsd#InsertionCapabilities"));
            }
            return opsMeta;
        }
        return null;
    }

    @Override
    public synchronized InsertSensorResponse insertSensor(InsertSensorRequest request) throws OwsExceptionReport {
        InsertSensorResponse response = new InsertSensorResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        String assignedProcedureID = checkOrGetAssignedProcedureID(request);
        SosOffering assignedOffering = checkOrGetAssignedOffering(request, assignedProcedureID);
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
                                procedureDescriptionFormat, observationTypes, featureOfInterestTypes, session);
                // TODO: set correct validTime,
                HibernateCriteriaTransactionalUtilities.insertValidProcedureTime(
                        procedure,
                        getSensorDescriptionFromProcedureDescription(request.getProcedureDescription(),
                                assignedProcedureID), new DateTime(), session);
                List<ObservableProperty> obsProps =
                        getOrInsertNewObservableProperties(request.getObservableProperty(), session);
                Offering offering =
                        insertNewOffering(assignedOffering, request.getRelatatedFeature(), observationTypes, session);
                HibernateCriteriaTransactionalUtilities.checkOrInsertObservationConstellation(procedure, obsProps,
                        offering, session);
                // TODO: parent and child procedures
                response.setAssignedProcedure(assignedProcedureID);
                response.setAssignedOffering(assignedOffering.getOfferingIdentifier());
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

    private String checkOrGetAssignedProcedureID(InsertSensorRequest request) throws OwsExceptionReport {
        // if procedureDescription is SensorML
        if (request.getProcedureDescription() instanceof SensorML) {
            SensorML sensorML = (SensorML) request.getProcedureDescription();
            // if SensorML is not a wrapper
            if (!sensorML.isWrapper()) {
                if (request.getProcedureDescription().getProcedureIdentifier() != null
                        && !request.getProcedureDescription().getProcedureIdentifier().isEmpty()) {
                    return request.getProcedureDescription().getProcedureIdentifier();
                }
            }
            // if SensorML is a wrapper and member size is 1
            else if (sensorML.isWrapper() && sensorML.getMembers().size() == 1) {
                AbstractProcess process = sensorML.getMembers().get(0);
                if (process.getProcedureIdentifier() != null && !process.getProcedureIdentifier().isEmpty()) {
                    return process.getProcedureIdentifier();
                }
            }
            return getConfigurator().getDefaultProcedurePrefix()
                    + SosHelper.generateID(sensorML.getSensorDescriptionXmlString());
        }
        // if procedureDescription not SensorML
        else {
            if (request.getProcedureDescription().getProcedureIdentifier() != null
                    && !request.getProcedureDescription().getProcedureIdentifier().isEmpty()) {
                return request.getProcedureDescription().getProcedureIdentifier();
            } else {
                return getConfigurator().getDefaultProcedurePrefix()
                        + SosHelper.generateID(request.getProcedureDescription().toString());
            }
        }
    }

    private SosOffering checkOrGetAssignedOffering(InsertSensorRequest request, String assignedProcedureID)
            throws OwsExceptionReport {
        // if procedureDescription is SensorML
        SosOffering sosOffering = null;
        if (request.getProcedureDescription() instanceof SensorML) {
            SensorML sensorML = (SensorML) request.getProcedureDescription();
            // if SensorML is not a wrapper
            if (!sensorML.isWrapper()) {
                if (request.getProcedureDescription().getOfferingIdentifier() != null) {
                    sosOffering = request.getProcedureDescription().getOfferingIdentifier();
                }
            }
            // if SensorML is a wrapper and member size is 1
            else if (sensorML.isWrapper() && sensorML.getMembers().size() == 1) {
                AbstractProcess process = sensorML.getMembers().get(0);
                if (process.getProcedureIdentifier() != null && !process.getProcedureIdentifier().isEmpty()) {
                    sosOffering = process.getOfferingIdentifier();
                }
            }
        }
        // if procedureDescription not SensorML
        else {
            if (request.getProcedureDescription().getOfferingIdentifier() != null) {
                sosOffering = request.getProcedureDescription().getOfferingIdentifier();
            }
        }
        // check if offering is valid
        if (sosOffering != null && sosOffering.getOfferingIdentifier() != null
                && !sosOffering.getOfferingIdentifier().isEmpty()) {
            if (!getCache().getKOfferingVProcedures().containsKey(sosOffering.getOfferingIdentifier())) {
                return sosOffering;
            } else {
                String exceptionText = String.format(
                        "The requested offering identifier (%s) is already provided by this server!", sosOffering.getOfferingIdentifier());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
            }
        } else {
            // TODO: configurable postfix for offerings
            return new SosOffering(assignedProcedureID + "/observations", "Offering for sensor " + assignedProcedureID);
        }
    }

    private Offering insertNewOffering(SosOffering assignedOffering, List<SosFeatureRelationship> relatedFeatures,
            List<ObservationType> observationTypes, Session session) throws OwsExceptionReport {
        List<RelatedFeature> hRelatedFeatures = new ArrayList<RelatedFeature>();
        if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
            for (SosFeatureRelationship relatedFeature : relatedFeatures) {
                List<RelatedFeatureRole> relatedFeatureRoles =
                        HibernateCriteriaTransactionalUtilities.getOrInsertRelatedFeatureRole(
                                relatedFeature.getRole(), session);
                hRelatedFeatures.addAll(HibernateCriteriaTransactionalUtilities.getOrInsertRelatedFeature(
                        relatedFeature.getFeature(), relatedFeatureRoles, session));
            }
        }
        return HibernateCriteriaTransactionalUtilities.insertOffering(assignedOffering.getOfferingIdentifier(),
                assignedOffering.getOfferingName(), hRelatedFeatures, observationTypes, session);
    }

    private List<ObservableProperty> getOrInsertNewObservableProperties(List<String> obsProps, Session session) {
        List<SosObservableProperty> observableProperties = new ArrayList<SosObservableProperty>();
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
    public IExtension getExtension(Session session) throws OwsExceptionReport {
        SosInsertionCapabilities insertionCapabilities = new SosInsertionCapabilities();
        try {
            insertionCapabilities.addFeatureOfInterestTypes(HibernateCriteriaQueryUtilities
                    .getFeatureOfInterestTypes(session));
            insertionCapabilities.addObservationTypes(HibernateCriteriaQueryUtilities.getObservationTypes(session));
            insertionCapabilities.addProcedureDescriptionFormats(HibernateCriteriaQueryUtilities
                    .getProcedureDescriptionFormatIdentifiers(session));
        } catch (HibernateException he) {
            String exceptionText = "Error while querying data for InsertionCapabilities!";
            LOGGER.error(exceptionText, he);
            Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        }

        return insertionCapabilities;
    }

}
