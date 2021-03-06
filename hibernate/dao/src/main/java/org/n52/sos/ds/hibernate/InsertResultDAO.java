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
import org.n52.sos.ds.hibernate.dao.ObservationConstellationDAO;
import org.n52.sos.ds.hibernate.dao.ObservationDAO;
import org.n52.sos.ds.hibernate.dao.ResultTemplateDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateObservationUtilities;
import org.n52.sos.ds.hibernate.util.ResultHandlingHelper;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.DateTimeParseException;
import org.n52.sos.ogc.gml.CodeWithAuthority;
import org.n52.sos.ogc.gml.time.Time;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.om.AbstractPhenomenon;
import org.n52.sos.ogc.om.MultiObservationValues;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.OmObservableProperty;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.ogc.om.OmObservationConstellation;
import org.n52.sos.ogc.om.features.AbstractFeature;
import org.n52.sos.ogc.om.features.samplingFeatures.SamplingFeature;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosProcedureDescription;
import org.n52.sos.ogc.sos.SosResultEncoding;
import org.n52.sos.ogc.sos.SosResultStructure;
import org.n52.sos.ogc.swe.SweAbstractDataComponent;
import org.n52.sos.ogc.swe.SweDataArray;
import org.n52.sos.ogc.swe.SweDataRecord;
import org.n52.sos.ogc.swe.SweField;
import org.n52.sos.ogc.swe.encoding.SweAbstractEncoding;
import org.n52.sos.ogc.swe.encoding.SweTextEncoding;
import org.n52.sos.ogc.swe.simpleType.SweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SweQuantity;
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
                    new ResultTemplateDAO().getResultTemplateObject(request.getTemplateIdentifier(), session);
            transaction = session.beginTransaction();
            OmObservation o =
                    getSingleObservationFromResultValues(response.getVersion(), resultTemplate,
                            request.getResultValues(), session);
            response.setObservation(o);
            List<OmObservation> observations = getSingleObservationsFromObservation(o);

            Set<ObservationConstellation> obsConsts =
                    CollectionHelper
                            .asSet(new ObservationConstellationDAO().getObservationConstellation(
                                    resultTemplate.getProcedure(),
                                    resultTemplate.getObservableProperty(),
                                    Configurator.getInstance().getCache()
                                            .getOfferingsForProcedure(resultTemplate.getProcedure().getIdentifier()),
                                    session));

            int insertion = 0, size = observations.size();
            LOGGER.debug("Start saving {} observations.", size);
            for (OmObservation observation : observations) {
                new ObservationDAO().insertObservationSingleValue(obsConsts, resultTemplate.getFeatureOfInterest(),
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

    private OmObservation getSingleObservationFromResultValues(String version, ResultTemplate resultTemplate,
            String resultValues, Session session) throws OwsExceptionReport {
        SosResultEncoding resultEncoding = new SosResultEncoding(resultTemplate.getResultEncoding());
        SosResultStructure resultStructure = new SosResultStructure(resultTemplate.getResultStructure());
        String[] blockValues = getBlockValues(resultValues, resultEncoding.getEncoding());
        OmObservation singleObservation =
                getObservation(resultTemplate, blockValues, resultStructure.getResultStructure(),
                        resultEncoding.getEncoding(), session);
        AbstractFeature feature = getSosAbstractFeature(resultTemplate.getFeatureOfInterest(), version, session);
        singleObservation.getObservationConstellation().setFeatureOfInterest(feature);
        return singleObservation;
    }

    protected AbstractFeature getSosAbstractFeature(FeatureOfInterest featureOfInterest, String version,
            Session session) throws OwsExceptionReport {
        final FeatureQueryHandler featureQueryHandler = Configurator.getInstance().getFeatureQueryHandler();
        return featureQueryHandler.getFeatureByID(featureOfInterest.getIdentifier(), session, version, -1);
    }

    protected List<OmObservation> getSingleObservationsFromObservation(OmObservation observation)
            throws OwsExceptionReport {
        try {
            return HibernateObservationUtilities.unfoldObservation(observation);
        } catch (Exception e) {
            throw new InvalidParameterValueException()
                    .at(Sos2Constants.InsertResultParams.resultValues)
                    .causedBy(e)
                    .withMessage(
                            "The resultValues format does not comply to the resultStructure of the resultTemplate!");
        }
    }

    private OmObservationConstellation getSosObservationConstellation(ResultTemplate resultTemplate, Session session) {
        ObservationConstellation obsConst =
                HibernateObservationUtilities.getObservationConstellation(resultTemplate.getProcedure(),
                        resultTemplate.getObservableProperty(), Collections.singleton(resultTemplate.getOffering()),
                        session);
        SosProcedureDescription procedure = createProcedure(resultTemplate.getProcedure());
        Set<String> offerings = Collections.singleton(resultTemplate.getOffering().getIdentifier());
        AbstractPhenomenon observablePropety =
                new OmObservableProperty(resultTemplate.getObservableProperty().getIdentifier());
        AbstractFeature feature =
                new SamplingFeature(new CodeWithAuthority(resultTemplate.getFeatureOfInterest().getIdentifier()));
        return new OmObservationConstellation(procedure, observablePropety, offerings, feature, obsConst
                .getObservationType().getObservationType());
    }

    private SosProcedureDescription createProcedure(Procedure hProcedure) {
        SensorML procedure = new SensorML();
        procedure.setIdentifier(hProcedure.getIdentifier());
        return procedure;
    }

    private OmObservation getObservation(ResultTemplate resultTemplate, String[] blockValues,
            SweAbstractDataComponent resultStructure, SweAbstractEncoding encoding, Session session)
            throws OwsExceptionReport {
        int resultTimeIndex = ResultHandlingHelper.hasResultTime(resultStructure);
        int phenomenonTimeIndex = ResultHandlingHelper.hasPhenomenonTime(resultStructure);

        SweDataRecord record = setRecordFrom(resultStructure);

        Map<Integer, String> observedProperties = new HashMap<Integer, String>(record.getFields().size() - 1);
        Map<Integer, String> units = new HashMap<Integer, String>(record.getFields().size() - 1);

        int j = 0;
        for (SweField swefield : record.getFields()) {
            if (j != resultTimeIndex && j != phenomenonTimeIndex) {
                Integer index = Integer.valueOf(j);
                SweAbstractSimpleType<?> sweAbstractSimpleType = (SweAbstractSimpleType) swefield.getElement();
                if (sweAbstractSimpleType instanceof SweQuantity) {
                    /* TODO units for other SosSweSimpleTypes? */
                    units.put(index, ((SweQuantity) sweAbstractSimpleType).getUom());
                }
                observedProperties.put(index, swefield.getElement().getDefinition());
            }
            ++j;
        }

        // TODO support for compositePhenomenon
        // if (observedProperties.size() > 1) {
        // }

        MultiObservationValues<SweDataArray> sosValues =
                createObservationValueFrom(blockValues, record, encoding, resultTimeIndex, phenomenonTimeIndex);

        OmObservation observation = new OmObservation();
        observation.setObservationConstellation(getSosObservationConstellation(resultTemplate, session));
        observation.setResultType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        observation.setValue(sosValues);
        return observation;
    }

    private MultiObservationValues<SweDataArray> createObservationValueFrom(String[] blockValues,
            SweAbstractDataComponent recordFromResultStructure, SweAbstractEncoding encoding, int resultTimeIndex,
            int phenomenonTimeIndex) throws OwsExceptionReport {
        SweDataArray dataArray = new SweDataArray();
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
        MultiObservationValues<SweDataArray> sosValues = new MultiObservationValues<SweDataArray>();
        sosValues.setValue(dataArrayValue);
        return sosValues;
    }

    private SweDataRecord setRecordFrom(SweAbstractDataComponent resultStructure) throws OwsExceptionReport {
        SweDataRecord record = null;
        if (resultStructure instanceof SweDataArray
                && ((SweDataArray) resultStructure).getElementType() instanceof SweDataRecord) {
            SweDataArray array = (SweDataArray) resultStructure;
            record = (SweDataRecord) array.getElementType();
        } else if (resultStructure instanceof SweDataRecord) {
            record = (SweDataRecord) resultStructure;
        } else {
            throw new NoApplicableCodeException().withMessage("Unsupported ResultStructure!");
        }
        return record;
    }

    // TODO move to helper class
    private Time getPhenomenonTime(String timeString) throws OwsExceptionReport {
        try {
            Time phenomenonTime;
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

    private String[] getSingleValues(String block, SweAbstractEncoding encoding) {
        if (encoding instanceof SweTextEncoding) {
            SweTextEncoding textEncoding = (SweTextEncoding) encoding;
            return separateValues(block, textEncoding.getTokenSeparator());
        }
        return null;
    }

    private String[] getBlockValues(String resultValues, SweAbstractEncoding encoding) {
        if (encoding instanceof SweTextEncoding) {
            SweTextEncoding textEncoding = (SweTextEncoding) encoding;
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
