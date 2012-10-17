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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IInsertResultDAO;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaQueryUtilities;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ogc.gml.time.ITime;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.IObservationValue;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.values.TextValue;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
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
import org.n52.sos.request.InsertResultRequest;
import org.n52.sos.response.InsertResultResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertResultDAO implements IInsertResultDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertResultDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertResult.name();

    /**
     * Instance of the IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    private final String RESULT_TIME = "http://www.opengis.net/def/property/OGC/0/ResultTime";

    private final String PHENOMENON_TIME = "http://www.opengis.net/def/property/OGC/0/PhenomenonTime";

    /**
     * constructor
     */
    public InsertResultDAO() {
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
        // get DCP
        DecoderKeyType dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        Map<String, List<String>> dcpMap =
                SosHelper.getDCP(OPERATION_NAME, dkt, Configurator.getInstance().getBindingOperators().values(),
                        Configurator.getInstance().getServiceURL());
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
            // set DCP
            opsMeta.setDcp(dcpMap);
            // TODO set parameter
            return opsMeta;
        }
        return null;
    }

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InsertResultResponse insertResult(InsertResultRequest request) throws OwsExceptionReport {
        InsertResultResponse response = new InsertResultResponse();
        response.setService(request.getService());
        response.setVersion(request.getVersion());
        Session session = null;
        Transaction transaction = null;
        try {
            session = (Session) connectionProvider.getConnection();
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
            String exceptionText = "";
            LOGGER.error(exceptionText, he);
            throw Util4Exceptions.createNoApplicableCodeException(he, exceptionText);
        } finally {
            connectionProvider.returnConnection(session);
        }
        return response;
    }

    private List<SosObservation> getSingleObservationsFromResultValues(String resultValues,
            ResultTemplate resultTemplate) throws OwsExceptionReport {
        SosResultEncoding resultEncoding = parseResultEncoding(resultTemplate.getResultEncoding());
        SosResultStructure resultStructure = parseResultStructure(resultTemplate.getResultStructure());
        String[] blockValues = getBlockValues(resultValues, resultEncoding.getEncoding());
        return getSingleObservationFromEachValueBlock(blockValues, resultStructure.getResultStructure(),
                resultEncoding.getEncoding());
    }

    private List<SosObservation> getSingleObservationFromEachValueBlock(String[] blockValues,
            SosSweAbstractDataComponent resultStructure, SosSweAbstractEncoding encoding) throws OwsExceptionReport {
        List<SosObservation> observations = new ArrayList<SosObservation>();
        for (String block : blockValues) {
            String[] singleValues = getSingleValues(block, encoding);
            if (singleValues != null && singleValues.length > 0) {
                SosObservation observation = new SosObservation();
                observation.setResultTime(getResultTime(singleValues, resultStructure));
                // TODO get all values as single values, maybe flag + profile
                // file
                if (true) {
                    observation.setValue(getValuesAsSingleStringValue(singleValues, resultStructure, encoding));
                } else {
                    observation.setValue(getValues(singleValues, resultStructure));
                }
                observations.add(observation);
            }
        }
        return observations;
    }

    private IObservationValue getValuesAsSingleStringValue(String[] singleValues,
            SosSweAbstractDataComponent resultStructure, SosSweAbstractEncoding encoding) throws OwsExceptionReport {
        int resultTimeIndex = hasResultTime(resultStructure);
        int phenomenonTimeIndex = hasPhenomenonTime(resultStructure);
        String tokenSeparator = getTokenSeparator(encoding);
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

    private ITime getPhenomenonTime(String timeString) throws OwsExceptionReport {
        try {
            ITime phenomenonTime = null;
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

    private String getTokenSeparator(SosSweAbstractEncoding encoding) {
        if (encoding instanceof SosSweTextEncoding) {
            return ((SosSweTextEncoding) encoding).getTokenSeparator();
        }
        return null;
    }

    private IObservationValue getValues(String[] singleValues, SosSweAbstractDataComponent resultStructure) {
        // TODO Auto-generated method stub
        return null;
    }

    private int hasPhenomenonTime(SosSweAbstractDataComponent sweDataElement) {
        if (sweDataElement instanceof SosSweDataArray) {
            SosSweDataArray dataArray = (SosSweDataArray) sweDataElement;
            return checkFields(dataArray.getElementType().getFields(), PHENOMENON_TIME);
        } else if (sweDataElement instanceof SosSweDataRecord) {
            SosSweDataRecord dataRecord = (SosSweDataRecord) sweDataElement;
            return checkFields(dataRecord.getFields(), PHENOMENON_TIME);

        }
        return -1;
    }

    private int checkFields(List<SosSweField> fields, String definition) {
        for (int i = 0; i < fields.size(); i++) {
            SosSweAbstractDataComponent element = fields.get(i).getElement();
            if (element instanceof SosSweAbstractSimpleType) {
                SosSweAbstractSimpleType simpleType = (SosSweAbstractSimpleType) element;
                if (simpleType.isSetDefinition() && simpleType.getDefinition().equals(definition)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int hasResultTime(SosSweAbstractDataComponent sweDataElement) {
        if (sweDataElement instanceof SosSweDataArray) {
            SosSweDataArray dataArray = (SosSweDataArray) sweDataElement;
            return checkFields(dataArray.getElementType().getFields(), RESULT_TIME);
        } else if (sweDataElement instanceof SosSweDataRecord) {
            SosSweDataRecord dataRecord = (SosSweDataRecord) sweDataElement;
            return checkFields(dataRecord.getFields(), RESULT_TIME);
        }
        return -1;
    }

    private TimeInstant getResultTime(String[] singleValues, SosSweAbstractDataComponent resultStructure)
            throws OwsExceptionReport {
        try {
            int resultTimeIndex = hasResultTime(resultStructure);
            if (resultTimeIndex != -1) {
                TimeInstant time = new TimeInstant();
                DateTime dateTime = DateTimeHelper.parseIsoString2DateTime(singleValues[resultTimeIndex].trim());
                time.setValue(dateTime);
                return time;
            }
            return null;
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

    private SosResultEncoding parseResultEncoding(String resultEncoding) throws OwsExceptionReport {
        try {
            Object decodedObject = decodeXmlToObject(XmlObject.Factory.parse(resultEncoding));
            if (decodedObject != null && decodedObject instanceof SosSweAbstractEncoding) {
                SosSweAbstractEncoding sosSweEncoding = (SosSweAbstractEncoding) decodedObject;
                SosResultEncoding encoding = new SosResultEncoding();
                encoding.setEncoding(sosSweEncoding);
                return encoding;
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("Error while parsing result encoding!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        } catch (XmlException xmle) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("Error while parsing result encoding!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText.toString());
        }
    }

    private SosResultStructure parseResultStructure(String resultStructure) throws OwsExceptionReport {
        try {
            Object decodedObject = decodeXmlToObject(XmlObject.Factory.parse(resultStructure));
            if (decodedObject != null && decodedObject instanceof SosSweAbstractDataComponent) {
                SosSweAbstractDataComponent sosSweData = (SosSweAbstractDataComponent) decodedObject;
                SosResultStructure sosResultStructure = new SosResultStructure();
                sosResultStructure.setResultStructure(sosSweData);
                return sosResultStructure;
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("Error while parsing result structure!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
            }
        } catch (XmlException xmle) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("Error while parsing result structure!");
            LOGGER.debug(exceptionText.toString());
            throw Util4Exceptions.createNoApplicableCodeException(xmle, exceptionText.toString());
        }

    }

    private Object decodeXmlToObject(XmlObject xmlObject) throws OwsExceptionReport {
        List<IDecoder> decoderList = Configurator.getInstance().getDecoder(XmlHelper.getNamespace(xmlObject));
        if (decoderList != null) {
            for (IDecoder decoder : decoderList) {
                return decoder.decode(xmlObject);
            }
        }
        return null;
    }

}
