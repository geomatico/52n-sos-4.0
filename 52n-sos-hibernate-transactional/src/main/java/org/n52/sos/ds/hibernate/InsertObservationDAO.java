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
import java.util.UUID;

import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.TextEncodingDocument;
import net.opengis.swe.x20.TextEncodingType;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ds.IInsertObservationDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellation;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateUtilities;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.encode.SweCommonEncoderv20;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InsertObservationDAO extends AbstractHibernateOperationDao implements IInsertObservationDAO {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertObservationDAO.class);

    /**
     * supported SOS operation
     */
    private static final String OPERATION_NAME = SosConstants.Operations.InsertObservation.name();

    @Override
    public String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    public DecoderKeyType getKeyTypeForDcp(String version) {
        return new DecoderKeyType(version.equals(Sos1Constants.SERVICEVERSION) ? Sos1Constants.NS_SOS
                : Sos2Constants.NS_SOS_20);
    }

    @Override
    protected void setOperationsMetadata(OWSOperation opsMeta, String service, String version, Session session)
            throws OwsExceptionReport {
        opsMeta.addPossibleValuesParameter(Sos2Constants.InsertObservationParams.offering, getCache().getOfferings());
        opsMeta.addAnyParameterValue(Sos2Constants.InsertObservationParams.observation);
        opsMeta.addDataTypeParameter(Sos2Constants.InsertObservationParams.observation,
                OMConstants.SCHEMA_LOCATION_OM_2_OM_OBSERVATION);
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
            session = getSession();
            transaction = session.beginTransaction();
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
            for (SosObservation sosObservation : request.getObservation()) {
                SosObservationConstellation sosObsConst = sosObservation.getObservationConstellation();
                ObservationConstellation hObsConst = null;
                for (String offeringID : sosObsConst.getOfferings()) {
                    try {
                        hObsConst =
                                HibernateUtilities.checkObservationConstellationForObservation(sosObsConst,
                                        offeringID, session,
                                        Sos2Constants.InsertObservationParams.observationType.name());
                    } catch (OwsExceptionReport owse) {
                        exceptions.add(owse);
                    }
                    if (hObsConst != null) {
                        FeatureOfInterest hFeature =
                                HibernateUtilities.checkOrInsertFeatureOfInterest(sosObservation
                                        .getObservationConstellation().getFeatureOfInterest(), session);
                        HibernateUtilities.checkOrInsertFeatureOfInterestRelatedFeatureRelation(hFeature,
                                hObsConst.getOffering(), session);
                        if (isSweArrayObservation(hObsConst)) {
                            ResultTemplate resultTemplate = createResultTemplate(sosObservation, hObsConst, hFeature);
                            session.save(resultTemplate);
                            session.flush();
                        }
                        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
                            HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(hObsConst, hFeature,
                                    sosObservation, session);
                        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
                            HibernateCriteriaTransactionalUtilities.insertObservationMutliValue(hObsConst, hFeature,
                                    sosObservation, session);
                        }
                    }
                }
            }
            // if no observationConstellation is valid, throw exception
            if (exceptions.size() == request.getObservation().size()) {
                Util4Exceptions.mergeAndThrowExceptions(exceptions);
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
            returnSession(session);
        }
        // TODO: ... all the DS insertion stuff
        // Requirement 68
        // proc/obsProp/Offering same obsType;

        return response;
    }

    private boolean isSweArrayObservation(ObservationConstellation obsConst) {
        return obsConst.getObservationType().getObservationType()
                .equalsIgnoreCase(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
    }

    private ResultTemplate createResultTemplate(SosObservation observation, ObservationConstellation obsConst,
            FeatureOfInterest feature) throws OwsExceptionReport {
        ResultTemplate resultTemplate = new ResultTemplate();
        // TODO identifier handling: ignoring code space now
        String identifier = null;
        if (observation.getIdentifier() != null && observation.getIdentifier().getValue() != null
                && !observation.getIdentifier().getValue().isEmpty()) {
            identifier = observation.getIdentifier().getValue();
        } else {
            identifier = UUID.randomUUID().toString();
        }
        resultTemplate.setIdentifier(identifier);
        resultTemplate.setObservationConstellation(obsConst);
        resultTemplate.setFeatureOfInterest(feature);
        SosSweDataArray dataArray = ((SweDataArrayValue) observation.getValue().getValue()).getValue();
        SweCommonEncoderv20 sweEncoder = null;
        if (isEncoderRequired(dataArray)) {
            IEncoder encoder = getConfigurator().getEncoder(SWEConstants.NS_SWE_20);
            if (encoder instanceof SweCommonEncoderv20) {
                sweEncoder = (SweCommonEncoderv20) encoder;
            } else {
                String errorMsg =
                        String.format("Could not find encoder for namespace \"%s\".", SWEConstants.NS_SWE_20);
                LOGGER.error(errorMsg);
                throw Util4Exceptions.createNoApplicableCodeException(null, errorMsg);
            }
        }
        if (dataArray.getElementType().getXml() == null) {
            XmlObject encodedXMLObject = sweEncoder.encode(dataArray.getElementType());
            if (encodedXMLObject instanceof DataRecordType) {
                DataRecordDocument xbDataRecord = DataRecordDocument.Factory.newInstance();
                xbDataRecord.setDataRecord((DataRecordType) encodedXMLObject);
                encodedXMLObject = xbDataRecord;
            }
            resultTemplate
                    .setResultStructure(encodedXMLObject.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
        } else {
            resultTemplate.setResultStructure(dataArray.getElementType().getXml());
        }
        if (dataArray.getEncoding().getXml() == null) {
            XmlObject encodedXmlObject = sweEncoder.encode(dataArray.getEncoding());
            if (encodedXmlObject instanceof TextEncodingType) {
                TextEncodingDocument xbTextEncodingDoc = TextEncodingDocument.Factory.newInstance();
                xbTextEncodingDoc.setTextEncoding((TextEncodingType) encodedXmlObject);
                encodedXmlObject = xbTextEncodingDoc;
            }
            resultTemplate.setResultEncoding(encodedXmlObject.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
        } else {
            resultTemplate.setResultEncoding(dataArray.getEncoding().getXml());
        }
        return resultTemplate;
    }

    private boolean isEncoderRequired(SosSweDataArray dataArray) {
        return dataArray.getElementType().getXml() == null || dataArray.getEncoding().getXml() == null;
    }
}
