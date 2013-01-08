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
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IInsertResultDAO;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
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
import org.n52.sos.ogc.om.IObservationValue;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.IValue;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.om.values.TextValue;
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
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertResultDAO extends AbstractHibernateOperationDao implements IInsertResultDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertResult.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }
    
    @Override
    protected DecoderKeyType getKeyTypeForDcp(String version) {
        return new DecoderKeyType(Sos2Constants.NS_SOS_20);
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session) throws OwsExceptionReport {
        /* nothing to add here */
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
            List<SosObservation> observations =
                    getSingleObservationsFromResultValues(request.getResultValues(), resultTemplate);
            for (SosObservation observation : observations) {
                HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(
                        resultTemplate.getObservationConstellation(), resultTemplate.getFeatureOfInterest(),
                        observation, session);
            }
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

    private List<SosObservation> getSingleObservationsFromResultValues(String resultValues,
            ResultTemplate resultTemplate) throws OwsExceptionReport {
        SosResultEncoding resultEncoding = new SosResultEncoding(resultTemplate.getResultEncoding());
        SosResultStructure resultStructure = new SosResultStructure(resultTemplate.getResultStructure());
        String[] blockValues = getBlockValues(resultValues, resultEncoding.getEncoding());
        SosObservation observation =
                getObservation(resultTemplate.getObservationConstellation(), blockValues,
                        resultStructure.getResultStructure(), resultEncoding.getEncoding());
        try {
            return HibernateObservationUtilities.unfoldObservation(observation);
        } catch (Exception e) {
            String exceptionText = "The resultValues format does not comply to the resultStructure of the resultTemplate!";
            LOGGER.debug(exceptionText, e);
            throw Util4Exceptions.createInvalidParameterValueException(Sos2Constants.InsertResult.resultValues.name(),
                    exceptionText);
        }
    }

    private SosObservationConstellation getSosObservationConstellation(ObservationConstellation c) {
        SosProcedureDescription procedure = createProcedure(c.getProcedure());
        Set<String> offerings = Collections.singleton(c.getOffering().getIdentifier());
        String observationType = c.getObservationType().getObservationType();
        AbstractSosPhenomenon observablePropety = new SosObservableProperty(c.getObservableProperty().getIdentifier());
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

    private SosObservation getObservation(ObservationConstellation obsConst, String[] blockValues,
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
        observation.setObservationConstellation(getSosObservationConstellation(obsConst));
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

    private IValue getValue(SWEConstants.SweSimpleType type, String value) throws OwsExceptionReport {
        switch (type) {
        case Boolean:
            return new BooleanValue(Boolean.valueOf(value));
        case Category:
            return new CategoryValue(value);
        case Text:
            return new TextValue(value);
        case Count:
            try {
                return new CountValue(Integer.valueOf(value));
            } catch (NumberFormatException e) {
                String exceptionText = "Error while parsing count value!";
                LOGGER.error(exceptionText, e);
                throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }
        case Quantity:
            try {
                return new QuantityValue(Double.valueOf(value));
            } catch (NumberFormatException e) {
                String exceptionText = "Error while parsing quantity value!";
                LOGGER.error(exceptionText, e);
                throw Util4Exceptions.createNoApplicableCodeException(e, exceptionText);
            }
        case Time:
            try {
                // testing for validity
                value = value.trim();
                DateTimeHelper.parseIsoString2DateTime(value);
                return new TextValue(value);
            } catch (DateTimeException dte) {
                String exceptionText = "Error while parsing time value!";
                LOGGER.error(exceptionText, dte);
                throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
            }
        case TimeRange:
            try {
                value = value.trim();
                String[] times = value.split("/");
                if (times.length != 2) {
                    String exceptionText = "Error while parsing time range value!";
                    LOGGER.error(exceptionText);
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
                DateTimeHelper.parseIsoString2DateTime(times[0].trim());
                DateTimeHelper.parseIsoString2DateTime(times[1].trim());
                return new TextValue(value);
            } catch (DateTimeException dte) {
                String exceptionText = "Error while parsing time range value!";
                LOGGER.error(exceptionText, dte);
                throw Util4Exceptions.createNoApplicableCodeException(dte, exceptionText);
            }
            // TODO case CountRange:
            // TODO case QuantityRange:
            // TODO case ObservableProperty:
        default:
            String exceptionText =
                    new StringBuilder().append("SweSimpleType '").append(type).append("' currently not supported.")
                            .toString();
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
        }
    }

    private IObservationValue getValuesAsSingleStringValue(String[] singleValues,
            SosSweAbstractDataComponent resultStructure, SosSweAbstractEncoding encoding) throws OwsExceptionReport {
        int resultTimeIndex = ResultHandlingHelper.hasResultTime(resultStructure);
        int phenomenonTimeIndex = ResultHandlingHelper.hasPhenomenonTime(resultStructure);
        String tokenSeparator = ResultHandlingHelper.getTokenSeparator(encoding);
        ITime phenomenonTime = null;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < singleValues.length; i++) {
            if (i == phenomenonTimeIndex) {
                phenomenonTime = getPhenomenonTime(singleValues[i]);
            } else if (i != phenomenonTimeIndex && i != resultTimeIndex) {
                builder.append(singleValues[i]);
                if (tokenSeparator != null) {
                    builder.append(tokenSeparator);
                }
            }
        }
        if (tokenSeparator != null && builder.length() > 0) {
            builder.delete(builder.lastIndexOf(tokenSeparator), builder.length());
        }
        TextValue textValue = new TextValue(builder.toString());
        SosSingleObservationValue observation = new SosSingleObservationValue(phenomenonTime, textValue);
        return observation;
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

    private TimeInstant getResultTime(String timeString) throws OwsExceptionReport {
        try {
            TimeInstant time = new TimeInstant();
            DateTime dateTime = DateTimeHelper.parseIsoString2DateTime(timeString.trim());
            time.setValue(dateTime);
            return time;
        } catch (DateTimeException dte) {
            String exceptionText = "Error while parsing resultTime!";
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
