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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.IInsertResultDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateObservationUtilities;
import org.n52.sos.ds.hibernate.util.ResultHandlingHelper;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sensorML.elements.SosSMLIdentifier;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.SosSweDataRecord;
import org.n52.sos.ogc.swe.SosSweField;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SosSweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.request.InsertResultRequest;
import org.n52.sos.response.InsertResultResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertResultDAO extends AbstractHibernateOperationDao implements IInsertResultDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultDAO.class);

    private static final int FLUSH_THRESHOLD = 50;
    
    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertResult.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }
    
    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session) throws OwsExceptionReport {
        opsMeta.addPossibleValuesParameter(Sos2Constants.InsertResultParams.template, getCache().getResultTemplates());
        opsMeta.addAnyParameterValue(Sos2Constants.InsertResultParams.resultValues);
    }
    
    @Override
    public InsertResultResponse insertResult(InsertResultRequest request) throws OwsExceptionReport {
        InsertResultResponse response = new InsertResultResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            ResultTemplate resultTemplate =
                    HibernateCriteriaQueryUtilities.getResultTemplateObject(request.getTemplateIdentifier(), session);
            transaction = session.beginTransaction();
            SosObservation o = getSingleObservationFromResultValues(response.getVersion(), resultTemplate,
                                                                    request.getResultValues(), session);
            response.setObservation(o);
            List<SosObservation> observations = getSingleObservationsFromObservation(o);
            Set<ObservationConstellationOfferingObservationType> obsConstOffObsTypes 
                    = new HashSet<ObservationConstellationOfferingObservationType>(1);
            obsConstOffObsTypes.add(resultTemplate.getObservationConstellationOfferingObservationType());
            int insertion = 0, size = observations.size();
            LOGGER.debug("Start saving {} observations.", size);
            for (SosObservation observation : observations) {
                HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(
                        obsConstOffObsTypes, resultTemplate.getFeatureOfInterest(),
                        observation, session);
                if ((++insertion % FLUSH_THRESHOLD) == 0) {
                    session.flush();
                    session.clear();
                    LOGGER.debug("Saved {}/{} observations.", insertion, size);
                }
            }
            LOGGER.debug("Saved {} observations.", size);
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            // XXX exception text
            String exceptionText = "";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            returnSession(session);
        }
        return response;
    }

    private SosObservation getSingleObservationFromResultValues(String version, ResultTemplate resultTemplate,
                                                                String resultValues, Session session) 
                                                                throws OwsExceptionReport {
        SosResultEncoding resultEncoding = new SosResultEncoding(resultTemplate.getResultEncoding());
        SosResultStructure resultStructure = new SosResultStructure(resultTemplate.getResultStructure());
        String[] blockValues = getBlockValues(resultValues, resultEncoding.getEncoding());
        SosObservation singleObservation =
                       getObservation(resultTemplate.getObservationConstellationOfferingObservationType(),
                                      blockValues, resultStructure.getResultStructure(), resultEncoding.getEncoding());
        SosAbstractFeature feature = getSosAbstractFeature(resultTemplate.getFeatureOfInterest(), version, session);
        singleObservation.getObservationConstellation().setFeatureOfInterest(feature);
        return singleObservation;
    }

    protected SosAbstractFeature getSosAbstractFeature(FeatureOfInterest featureOfInterest, String version,
                                                       Session session) throws OwsExceptionReport {
        final IFeatureQueryHandler featureQueryHandler = Configurator.getInstance().getFeatureQueryHandler();
        return featureQueryHandler.getFeatureByID(featureOfInterest.getIdentifier(), session, version, -1);
    }
    
    protected List<SosObservation> getSingleObservationsFromObservation(SosObservation observation) throws
            OwsExceptionReport {
        try {
            return HibernateObservationUtilities.unfoldObservation(observation);
        } catch (Exception e) {
            String exceptionText = "The resultValues format does not comply to the resultStructure of the resultTemplate!";
            LOGGER.debug(exceptionText, e);
            throw Util4Exceptions.createInvalidParameterValueException(Sos2Constants.InsertResultParams.resultValues
                                                                       .name(), exceptionText);
        }
    }

    private SosObservationConstellation getSosObservationConstellation(
            ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType) {
        ObservationConstellation observationConstellation = observationConstellationOfferingObservationType.getObservationConstellation();
        SosProcedureDescription procedure = createProcedure(observationConstellation.getProcedure());
        Set<String> offerings = Collections.singleton(observationConstellationOfferingObservationType.getOffering().getIdentifier());
        String observationType = observationConstellationOfferingObservationType.getObservationType().getObservationType();
        AbstractSosPhenomenon observablePropety = new SosObservableProperty(observationConstellation.getObservableProperty().getIdentifier());
        /* FIXME where is the feature?! */
        return new SosObservationConstellation(procedure, observablePropety, offerings, null, observationType);
    }
    
    private SosProcedureDescription createProcedure(Procedure hProcedure) {
        SensorML procedure = new SensorML();
        SosSMLIdentifier identifier = new SosSMLIdentifier("uniqueID", "urn:ogc:def:identifier:OGC:uniqueID", hProcedure.getIdentifier());
        List<SosSMLIdentifier> identifiers = new ArrayList<SosSMLIdentifier>(1);
        identifiers.add(identifier);
        procedure.setIdentifications(identifiers);
        return procedure;
    }

    private SosObservation getObservation(ObservationConstellationOfferingObservationType observationConstellationOfferingObservationType, String[] blockValues,
            SosSweAbstractDataComponent resultStructure, SosSweAbstractEncoding encoding) throws OwsExceptionReport {
        int resultTimeIndex = ResultHandlingHelper.hasResultTime(resultStructure);
        int phenomenonTimeIndex = ResultHandlingHelper.hasPhenomenonTime(resultStructure);

        SosSweDataRecord record = setRecordFrom(resultStructure);

        Map<Integer, String> observedProperties = new HashMap<Integer, String>(record.getFields().size() - 1);
        Map<Integer, SWEConstants.SweSimpleType> types =
                new HashMap<Integer, SWEConstants.SweSimpleType>(record.getFields().size() - 1);
        Map<Integer, String> units = new HashMap<Integer, String>(record.getFields().size() - 1);

        int j = 0;
        for (SosSweField swefield : record.getFields()) {
            if (j != resultTimeIndex && j != phenomenonTimeIndex) {
                Integer index = Integer.valueOf(j);
                SosSweAbstractSimpleType sweAbstractSimpleType = (SosSweAbstractSimpleType) swefield.getElement();
                if (sweAbstractSimpleType instanceof SosSweQuantity) {
                    /* TODO units for other SosSweSimpleTypes? */
                    units.put(index, ((SosSweQuantity) sweAbstractSimpleType).getUom());
                }
                types.put(index, sweAbstractSimpleType.getSimpleType());
                observedProperties.put(index, swefield.getElement().getDefinition());
            }
            ++j;
        }

        if (observedProperties.size() > 1) {
            // TODO composite phenomenon
        }

        SosMultiObservationValues<SosSweDataArray> sosValues =
                createObservationValueFrom(blockValues, record, encoding, resultTimeIndex, phenomenonTimeIndex, types,
                        units);

        SosObservation observation = new SosObservation();
        observation.setObservationConstellation(getSosObservationConstellation(observationConstellationOfferingObservationType));
        observation.setResultType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        observation.setValue(sosValues);
        return observation;
    }

    private SosMultiObservationValues<SosSweDataArray> createObservationValueFrom(String[] blockValues,
            SosSweAbstractDataComponent recordFromResultStructure, SosSweAbstractEncoding encoding,
            int resultTimeIndex, int phenomenonTimeIndex, Map<Integer, SWEConstants.SweSimpleType> types,
            Map<Integer, String> units) throws OwsExceptionReport {
        SosSweDataArray dataArray = new SosSweDataArray();
        dataArray.setElementType(recordFromResultStructure);
        dataArray.setEncoding(encoding);

        SweDataArrayValue dataArrayValue = new SweDataArrayValue();
        dataArrayValue.setValue(dataArray);

        for (String block : blockValues) {
            String[] singleValues = getSingleValues(block, encoding);
            if (singleValues != null && singleValues.length > 0) {
                dataArrayValue.addBlock(Arrays.asList(singleValues));
            }
        }
        SosMultiObservationValues<SosSweDataArray> sosValues = new SosMultiObservationValues<SosSweDataArray>();
        sosValues.setValue(dataArrayValue);
        return sosValues;
    }

    private SosSweDataRecord setRecordFrom(SosSweAbstractDataComponent resultStructure) throws OwsExceptionReport {
        SosSweDataRecord record = null;
        if (resultStructure instanceof SosSweDataArray
                && ((SosSweDataArray) resultStructure).getElementType() instanceof SosSweDataRecord) {
            SosSweDataArray array = (SosSweDataArray) resultStructure;
            record = (SosSweDataRecord) array.getElementType();
        } else if (resultStructure instanceof SosSweDataRecord) {
            record = (SosSweDataRecord) resultStructure;
        } else {
            String exceptionText = "Unsupported ResultStructure!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
        return record;
    }

    // TODO move to helper class
    private ITime getPhenomenonTime(String timeString) throws OwsExceptionReport {
        try {
            ITime phenomenonTime;
            if (timeString.contains("/")) {
                String[] times = timeString.split("/");
                DateTime start = DateTimeHelper.parseIsoString2DateTime(times[0].trim());
                DateTime end = DateTimeHelper.parseIsoString2DateTime(times[1].trim());
                phenomenonTime = new TimePeriod(start, end);
            } else {
                DateTime dateTime = DateTimeHelper.parseIsoString2DateTime(timeString.trim());
                phenomenonTime = new TimeInstant(dateTime);
            }
            return phenomenonTime;
        } catch (DateTimeException dte) {
            String exceptionText = "Error while parsing phenomenonTime!";
            LOGGER.error(exceptionText, dte);
            throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
        }
    }

    private String[] getSingleValues(String block, SosSweAbstractEncoding encoding) {
        if (encoding instanceof SosSweTextEncoding) {
            SosSweTextEncoding textEncoding = (SosSweTextEncoding) encoding;
            return separateValues(block, textEncoding.getTokenSeparator());
        }
        return null;
    }

    private String[] getBlockValues(String resultValues, SosSweAbstractEncoding encoding) {
        if (encoding instanceof SosSweTextEncoding) {
            SosSweTextEncoding textEncoding = (SosSweTextEncoding) encoding;
            String[] blockValues = separateValues(resultValues, textEncoding.getBlockSeparator());
            return checkForCountValue(blockValues, textEncoding.getTokenSeparator());
        }
        return null;
    }

    private String[] checkForCountValue(String[] blockValues, String tokenSeparator) {
        if (blockValues != null && blockValues.length > 0) {
            if (blockValues[0].contains(tokenSeparator)) {
                return blockValues;
            } else {
                String[] blockValuesWithoutCount = new String[blockValues.length - 1];
                for (int i = 1; i < blockValues.length; i++) {
                    blockValuesWithoutCount[i - 1] = blockValues[i];
                }
                return blockValuesWithoutCount;
            }
        }
        return null;
    }

    private String[] separateValues(String values, String separator) {
        return values.split(separator);
    }

}
