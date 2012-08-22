/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IInsertObservationDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.Observation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.GeometryValue;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterDataType;
import org.n52.sos.ogc.ows.OWSParameterValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertObservationDAO implements IInsertObservationDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertObservationDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.InsertObservation.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    /**
     * constructor
     */
    public InsertObservationDAO() {
        this.connectionProvider = Configurator.getInstance().getConnectionProvider();
    }

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public OWSOperation getOperationsMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        Session session = null;
        if (connection instanceof Session) {
            session = (Session) connection;
        } else {
            String exceptionText = "The parameter connection is not an Hibernate Session!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }

        OWSOperation opsMeta = new OWSOperation();
        // set operation name
        opsMeta.setOperationName(OPERATION_NAME);

        DecoderKeyType dkt = null;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        }
        opsMeta.setDcp(SosHelper.getDCP(OPERATION_NAME, dkt,
                Configurator.getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL()));
        // set offering
        opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.offering.name(),
                new OWSParameterValue(Configurator.getInstance().getCapabilitiesCacheController().getOfferings()));
        // set observation
        opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.observation.name(), new OWSParameterDataType("http://schemas.opengis.net/om/2.0/observation.xsd#OM_Observation"));
        return opsMeta;
    }

    @Override
    public synchronized InsertObservationResponse insertObservation(InsertObservationRequest request)
            throws OwsExceptionReport {
        InsertObservationResponse response = new InsertObservationResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        Session session = null;
        Transaction transaction = null;
        // TODO: check unit and set if available and not defined in DB
        try {
            session = (Session) connectionProvider.getConnection();
            transaction = session.beginTransaction();
            for (SosObservation observation : request.getObservation()) {
                ObservationConstellation obsConst =
                        checkObservationConstellationForObservation(observation.getObservationConstellation(), session);
                FeatureOfInterest feature =
                        checkOrInsertFeatureOfInterest(observation.getObservationConstellation()
                                .getFeatureOfInterest(), session);
                checkOrInsertFeatureOfInterestRelatedFeatureRelationShip(feature, obsConst.getOffering().getIdentifier(), session);
                if (observation.getValue() instanceof SosSingleObservationValue) {
                    insertObservationSingleValue(obsConst, feature, observation, session);
                } else if (observation.getValue() instanceof SosMultiObservationValues) {
                    insertObservationMutliValue(obsConst, feature, observation);
                }
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            String exceptionText = "Error while inserting new observation!";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
        // TODO: ... all the DS insertion stuff
        // Requirement 68
        // proc/obsProp/Offering same obsType;

        return response;
    }

    private ObservationConstellation checkObservationConstellationForObservation(
            SosObservationConstellation sosObservationConstellation, Session session) throws OwsExceptionReport,
            HibernateException {
        List<Criterion> criterions = new ArrayList<Criterion>();
        Map<String, String> aliases = new HashMap<String, String>();
        String offAlias = HibernateCriteriaQueryUtilities.addOfferingAliasToMap(aliases, null);
        criterions.add(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(offAlias),
                sosObservationConstellation.getOffering()));
        String obsPropAlias = HibernateCriteriaQueryUtilities.addObservablePropertyAliasToMap(aliases, null);
        criterions.add(HibernateCriteriaQueryUtilities.getEqualRestriction(HibernateCriteriaQueryUtilities
                .getIdentifierParameter(obsPropAlias), sosObservationConstellation.getObservableProperty()
                .getIdentifier()));
        String procAlias = HibernateCriteriaQueryUtilities.addProcedureAliasToMap(aliases, null);
        criterions.add(HibernateCriteriaQueryUtilities.getEqualRestriction(
                HibernateCriteriaQueryUtilities.getIdentifierParameter(procAlias),
                sosObservationConstellation.getProcedure()));
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
                    exceptionText.append(") is invalid for procedure, observedProperty and offering!");
                    exceptionText.append("The valid observationType is '");
                    exceptionText.append(obsConst.getObservationType().getObservationType());
                    exceptionText.append("'!");
                    LOGGER.debug(exceptionText.toString());
                    throw Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.observationType.name(), exceptionText.toString());
                }
            }
        } else {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested observation constellation (");
            exceptionText.append("procedure, observedProperty and offering)");
            exceptionText.append(" is invalid!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createInvalidParameterValueException(
                    Sos2Constants.InsertObservationParams.observation.name(), exceptionText.toString());
        }

    }

    private FeatureOfInterest checkOrInsertFeatureOfInterest(SosAbstractFeature featureOfInterest, Session session)
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

    private void checkOrInsertFeatureOfInterestRelatedFeatureRelationShip(FeatureOfInterest featureOfInterest,
            String offeringID, Session session) {
        List<RelatedFeature> relatedFeatures = HibernateCriteriaQueryUtilities.getRelatedFeatureForOffering(offeringID, session);
        if (relatedFeatures != null) {
            for (RelatedFeature relatedFeature : relatedFeatures) {
                HibernateCriteriaTransactionalUtilities.insertFeatureOfInterestRelationShip(relatedFeature.getFeatureOfInterest(), featureOfInterest, session);
            }
        }
        
    }

    private void insertObservationSingleValue(ObservationConstellation obsConst, FeatureOfInterest feature,
            SosObservation observation, Session session) {
        SosSingleObservationValue value = (SosSingleObservationValue) observation.getValue();
        Observation hObservation = new Observation();
        if (observation.getIdentifier() != null && !observation.getIdentifier().isEmpty()) {
            hObservation.setIdentifier(observation.getIdentifier());
        }
        hObservation.setObservationConstellation(obsConst);
        hObservation.setFeatureOfInterest(feature);
        addPhenomeonTimeAndResultTimeToObservation(hObservation, observation.getPhenomenonTime(), observation.getResultTime());
        addValueToObservation(hObservation, value.getValue(), session);
        if (value.getValue().getUnit() != null) {
            hObservation.setUnit(HibernateCriteriaTransactionalUtilities.getOrInsertUnit(value.getValue().getUnit(), session));
        }
        HibernateCriteriaTransactionalUtilities.insertObservation(hObservation, session);
    }

    private void addPhenomeonTimeAndResultTimeToObservation(Observation hObservation, ITime phenomenonTime,
            TimeInstant resultTime) {
        addPhenomeonTimeToObservation(hObservation, phenomenonTime);
        addResultTimeToObservation(hObservation, resultTime, phenomenonTime);
    }

    private void addPhenomeonTimeToObservation(Observation hObservation, ITime phenomenonTime) {
        if (phenomenonTime instanceof TimeInstant) {
            TimeInstant time = (TimeInstant) phenomenonTime;
            hObservation.setPhenomenonTimeStart(time.getValue().toDate());
        } else if (phenomenonTime instanceof TimePeriod) {
            TimePeriod time = (TimePeriod) phenomenonTime;
            hObservation.setPhenomenonTimeStart(time.getStart().toDate());
            hObservation.setPhenomenonTimeEnd(time.getEnd().toDate());
        }
    }

    private void addResultTimeToObservation(Observation hObservation, TimeInstant resultTime, ITime phenomenonTime) {
        if (resultTime != null) {
            if (resultTime.getValue() != null) {
                hObservation.setResultTime(resultTime.getValue().toDate());
            } else if (resultTime.getIndeterminateValue().contains(Sos2Constants.EN_PHENOMENON_TIME) && phenomenonTime instanceof TimeInstant) {
                hObservation.setResultTime(((TimeInstant)phenomenonTime).getValue().toDate());
            } else {
                // TODO: exception not valid resultTime
            }
        } else {
            // TODO: exception missing resultTime 
        }
    }

    private void addValidTimeToObservation(Observation hObservation, TimePeriod validTime) {
        if (validTime != null) {
            hObservation.setValidTimeStart(validTime.getStart().toDate());
            hObservation.setValidTimeEnd(validTime.getEnd().toDate());
        }
    }

    private void addValueToObservation(Observation hObservation, IValue value, Session session) {
        if (value instanceof BooleanValue) {
            hObservation.setBooleanValues(HibernateCriteriaTransactionalUtilities.getOrInsertBooleanValue(((BooleanValue)value).getValue(), session));
        } else if (value instanceof CategoryValue) {
            hObservation.setCategoryValues(HibernateCriteriaTransactionalUtilities.getOrInsertCategoryValue(((CategoryValue)value).getValue(), session));
        } else if (value instanceof CountValue) {
            hObservation.setCountValues(HibernateCriteriaTransactionalUtilities.getOrInsertCountValue(((CountValue)value).getValue(), session));
        } else if (value instanceof GeometryValue) {
            hObservation.setGeometryValues(HibernateCriteriaTransactionalUtilities.getOrInsertGeometryValue(((GeometryValue)value).getValue(), session));
        } else if (value instanceof QuantityValue) {
            hObservation.setNumericValues(HibernateCriteriaTransactionalUtilities.getOrInsertQuantityValue(((QuantityValue)value).getValue(), session));
        } else if (value instanceof TextValue) {
            hObservation.setTextValues(HibernateCriteriaTransactionalUtilities.getOrInsertTextValue(((TextValue)value).getValue(), session));
        }
    }

    private void insertObservationMutliValue(ObservationConstellation obsConst, FeatureOfInterest feature,
            SosObservation observation) {
        // TODO Auto-generated method stub
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }
}
