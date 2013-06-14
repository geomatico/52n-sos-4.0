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
import org.n52.sos.ds.AbstractInsertSensorDAO;
import org.n52.sos.ds.hibernate.dao.FeatureOfInterestTypeDAO;
import org.n52.sos.ds.hibernate.dao.ObservablePropertyDAO;
import org.n52.sos.ds.hibernate.dao.ObservationConstellationDAO;
import org.n52.sos.ds.hibernate.dao.ObservationTypeDAO;
import org.n52.sos.ds.hibernate.dao.OfferingDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDAO;
import org.n52.sos.ds.hibernate.dao.ProcedureDescriptionFormatDAO;
import org.n52.sos.ds.hibernate.dao.RelatedFeatureDAO;
import org.n52.sos.ds.hibernate.dao.RelatedFeatureRoleDAO;
import org.n52.sos.ds.hibernate.dao.ValidProcedureTimeDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterestType;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.ObservationType;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ProcedureDescriptionFormat;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.RelatedFeatureRole;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.om.OmObservableProperty;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SwesExtension;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosInsertionCapabilities;
import org.n52.sos.ogc.sos.SosOffering;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.swes.SwesFeatureRelationship;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;

public class InsertSensorDAO extends AbstractInsertSensorDAO {

    private final HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    
    public InsertSensorDAO() {
        super(SosConstants.SOS);
    }

    @Override
    public synchronized InsertSensorResponse insertSensor(final InsertSensorRequest request) throws OwsExceptionReport {
        final InsertSensorResponse response = new InsertSensorResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        final String assignedProcedureID = request.getAssignedProcedureIdentifier();
        // we use only the first offering for the response because swes 2.0 specifies only one single element
        final SosOffering firstAssignedOffering = request.getFirstAssignedOffering();
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionHolder.getSession();
            transaction = session.beginTransaction();
            final ProcedureDescriptionFormat procedureDescriptionFormat =
                    new ProcedureDescriptionFormatDAO().getOrInsertProcedureDescriptionFormat(
                            request.getProcedureDescriptionFormat(), session);
            final List<ObservationType> observationTypes =
                    new ObservationTypeDAO().getOrInsertObservationTypes(request.getMetadata()
                            .getObservationTypes(), session);
            final List<FeatureOfInterestType> featureOfInterestTypes =
                    new FeatureOfInterestTypeDAO().getOrInsertFeatureOfInterestTypes(request.getMetadata()
                            .getFeatureOfInterestTypes(), session);
            if (procedureDescriptionFormat != null && observationTypes != null && featureOfInterestTypes != null) {
                final Procedure hProcedure =
                        new ProcedureDAO().getOrInsertProcedure(assignedProcedureID,
                                procedureDescriptionFormat, request.getProcedureDescription().getParentProcedures()
                                ,session);
                // TODO: set correct validTime,
                new ValidProcedureTimeDAO().insertValidProcedureTime(
                        hProcedure,
                        getSensorDescriptionFromProcedureDescription(request.getProcedureDescription(),
                                assignedProcedureID), new DateTime(), session);
                final List<ObservableProperty> hObservableProperties =
                        getOrInsertNewObservableProperties(request.getObservableProperty(), session);
                final ObservationConstellationDAO observationConstellationDAO = new ObservationConstellationDAO();
                for (final SosOffering assignedOffering : request.getAssignedOfferings()) {
                    final Offering hOffering =
                            getAndUpdateOrInsertNewOffering(assignedOffering, request.getRelatedFeatures(), observationTypes,
                                    featureOfInterestTypes, session);
                    for (final ObservableProperty hObservableProperty : hObservableProperties) {
                        observationConstellationDAO.checkOrInsertObservationConstellation(
                                hProcedure, hObservableProperty, hOffering, assignedOffering.isParentOffering(),
                                session);
                    }
                }
                // TODO: parent and child procedures
                response.setAssignedProcedure(assignedProcedureID);
                response.setAssignedOffering(firstAssignedOffering.getOfferingIdentifier());
            } else if (procedureDescriptionFormat == null && observationTypes != null
                    && featureOfInterestTypes != null) {
                throw new InvalidParameterValueException(Sos2Constants.InsertSensorParams.procedureDescriptionFormat, "");
            } else {
                throw new NoApplicableCodeException().withMessage("Error while inserting InsertSensor into database!");
            }
            session.flush();
            transaction.commit();
        } catch (final HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while inserting sensor data into database!");
        } finally {
            sessionHolder.returnSession(session);
        }
        return response;
    }

    private Offering getAndUpdateOrInsertNewOffering(final SosOffering assignedOffering, final List<SwesFeatureRelationship> relatedFeatures,
            final List<ObservationType> observationTypes, final List<FeatureOfInterestType> featureOfInterestTypes, final Session session)
            throws OwsExceptionReport {
        final List<RelatedFeature> hRelatedFeatures = new LinkedList<RelatedFeature>();
        if (relatedFeatures != null && !relatedFeatures.isEmpty()) {
            final RelatedFeatureDAO relatedFeatureDAO = new RelatedFeatureDAO();
            final RelatedFeatureRoleDAO relatedFeatureRoleDAO = new RelatedFeatureRoleDAO();
            for (final SwesFeatureRelationship relatedFeature : relatedFeatures) {
                final List<RelatedFeatureRole> relatedFeatureRoles =
                        relatedFeatureRoleDAO.getOrInsertRelatedFeatureRole(
                                relatedFeature.getRole(), session);
                hRelatedFeatures.addAll(relatedFeatureDAO.getOrInsertRelatedFeature(
                        relatedFeature.getFeature(), relatedFeatureRoles, session));
            }
        }
        return new OfferingDAO().getAndUpdateOrInsertNewOffering(assignedOffering.getOfferingIdentifier(),
                assignedOffering.getOfferingName(), hRelatedFeatures, observationTypes, featureOfInterestTypes,
                session);
    }

    private List<ObservableProperty> getOrInsertNewObservableProperties(final List<String> obsProps, final Session session) {
        final List<OmObservableProperty> observableProperties = new ArrayList<OmObservableProperty>(obsProps.size());
        for (final String observableProperty : obsProps) {
            observableProperties.add(new OmObservableProperty(observableProperty));
        }
        return new ObservablePropertyDAO().getOrInsertObservableProperty(observableProperties, session);
    }

    private String getSensorDescriptionFromProcedureDescription(final SosProcedureDescription procedureDescription,
            final String procedureIdentifier) {
        if (procedureDescription instanceof SensorML) {
            final SensorML sensorML = (SensorML) procedureDescription;
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
    public SwesExtension getExtension() throws OwsExceptionReport {
        final SosInsertionCapabilities insertionCapabilities = new SosInsertionCapabilities();
        insertionCapabilities.addFeatureOfInterestTypes(getCache().getFeatureOfInterestTypes());
        insertionCapabilities.addObservationTypes(getCache().getObservationTypes());
        insertionCapabilities.addProcedureDescriptionFormats(getCache().getProcedureDescriptionFormats());
        return insertionCapabilities;
    }

}
