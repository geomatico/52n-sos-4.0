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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.ds.AbstractInsertResultDAO;
import org.n52.sos.ds.FeatureQueryHandler;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateObservationUtilities;
import org.n52.sos.ds.hibernate.util.ResultHandlingHelper;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.DateTimeParseException;
import org.n52.sos.ogc.gml.CodeWithAuthority;
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
import org.n52.sos.ogc.om.features.samplingFeatures.SosSamplingFeature;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
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
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.DateTimeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertResultDAO extends AbstractInsertResultDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultDAO.class);

    private static final int FLUSH_THRESHOLD = 50;
    
    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();
    
    public InsertResultDAO() {
        super(SosConstants.SOS);
    }
    
    @Override
    public InsertResultResponse insertResult(InsertResultRequest request) throws OwsExceptionReport {
        InsertResultResponse response = new InsertResultResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionHolder.getSession();
            ResultTemplate resultTemplate =
                    HibernateCriteriaQueryUtilities.getResultTemplateObject(request.getTemplateIdentifier(), session);
            transaction = session.beginTransaction();
            SosObservation o = getSingleObservationFromResultValues(response.getVersion(), resultTemplate,
                                                                    request.getResultValues(), session);
            response.setObservation(o);
            List<SosObservation> observations = getSingleObservationsFromObservation(o);
            
            Set<ObservationConstellation> obsConsts = CollectionHelper.asSet(HibernateCriteriaQueryUtilities.getObservationConstellation(resultTemplate.getProcedure(), resultTemplate.getObservableProperty(), resultTemplate.getOffering(), session));
            int insertion = 0, size = observations.size();
            LOGGER.debug("Start saving {} observations.", size);
            for (SosObservation observation : observations) {
                HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(
                        obsConsts, resultTemplate.getFeatureOfInterest(),
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
            throw new NoApplicableCodeException().causedBy(he);
        } finally {
            sessionHolder.returnSession(session);
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
                       getObservation(resultTemplate,
                                      blockValues, resultStructure.getResultStructure(), resultEncoding.getEncoding());
        SosAbstractFeature feature = getSosAbstractFeature(resultTemplate.getFeatureOfInterest(), version, session);
        singleObservation.getObservationConstellation().setFeatureOfInterest(feature);
        return singleObservation;
    }

    protected SosAbstractFeature getSosAbstractFeature(FeatureOfInterest featureOfInterest, String version,
                                                       Session session) throws OwsExceptionReport {
        final FeatureQueryHandler featureQueryHandler = Configurator.getInstance().getFeatureQueryHandler();
        return featureQueryHandler.getFeatureByID(featureOfInterest.getIdentifier(), session, version, -1);
    }
    
    protected List<SosObservation> getSingleObservationsFromObservation(SosObservation observation) throws
            OwsExceptionReport {
        try {
            return HibernateObservationUtilities.unfoldObservation(observation);
        } catch (Exception e) {
            throw new InvalidParameterValueException().at(Sos2Constants.InsertResultParams.resultValues).causedBy(e)
                    .withMessage("The resultValues format does not comply to the resultStructure of the resultTemplate!");
        }
    }

    private SosObservationConstellation getSosObservationConstellation(
            ResultTemplate resultTemplate) {
        SosProcedureDescription procedure = createProcedure(resultTemplate.getProcedure());
        Set<String> offerings = Collections.singleton(resultTemplate.getOffering().getIdentifier());
//        String observationType = resultTemplate.getObservationType().getObservationType();
        AbstractSosPhenomenon observablePropety = new SosObservableProperty(resultTemplate.getObservableProperty().getIdentifier());
        SosAbstractFeature feature = new SosSamplingFeature(new CodeWithAuthority(resultTemplate.getFeatureOfInterest().getIdentifier()));
        return new SosObservationConstellation(procedure, observablePropety, offerings, feature, null);
    }
    
    private SosProcedureDescription createProcedure(Procedure hProcedure) {
        SensorML procedure = new SensorML();
        procedure.setIdentifier(hProcedure.getIdentifier());
        return procedure;
    }

    private SosObservation getObservation(ResultTemplate resultTemplate, String[] blockValues,
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
                SosSweAbstractSimpleType<?> sweAbstractSimpleType = (SosSweAbstractSimpleType) swefield.getElement();
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
        observation.setObservationConstellation(getSosObservationConstellation(resultTemplate));
        observation.setResultType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        observation.setValue(sosValues);
        return observation;
    }

    private SosMultiObservationValues<SosSweDataArray> createObservationValueFrom(String[] blockValues,
            SosSweAbstractDataComponent recordFromResultStructure, SosSweAbstractEncoding encoding,
            int resultTimeIndex, int phenomenonTimeIndex, Map<Integer, SWEConstants.SweSimpleType> types,
                                                                                  Map<Integer, String> units) throws
            OwsExceptionReport {
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
            throw new NoApplicableCodeException().withMessage("Unsupported ResultStructure!");
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
        } catch (DateTimeParseException dte) {
            throw dte.at("phenomenonTime");
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
                System.arraycopy(blockValues, 1, blockValuesWithoutCount, 0, blockValuesWithoutCount.length);
                return blockValuesWithoutCount;
            }
        }
        return null;
    }

    private String[] separateValues(String values, String separator) {
        return values.split(separator);
    }
}
