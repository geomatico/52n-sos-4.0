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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.opengis.swe.x20.DataRecordDocument;
import net.opengis.swe.x20.DataRecordType;
import net.opengis.swe.x20.TextEncodingDocument;
import net.opengis.swe.x20.TextEncodingType;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.n52.sos.ds.AbstractInsertObservationDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservationConstellationOfferingObservationType;
import org.n52.sos.ds.hibernate.entities.ResultTemplate;
import org.n52.sos.ds.hibernate.util.HibernateCriteriaTransactionalUtilities;
import org.n52.sos.ds.hibernate.util.HibernateUtilities;
import org.n52.sos.encode.Encoder;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.NoEncoderForKeyException;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosMultiObservationValues;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.om.SosSingleObservationValue;
import org.n52.sos.ogc.om.values.SweDataArrayValue;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweAbstractDataComponent;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.ogc.swe.encoding.SosSweAbstractEncoding;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;

public class InsertObservationDAO extends AbstractInsertObservationDAO {
    private HibernateSessionHolder sessionHolder = new HibernateSessionHolder();

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
            session = sessionHolder.getSession();
            transaction = session.beginTransaction();
            CompositeOwsException exceptions = new CompositeOwsException();
            for (SosObservation sosObservation : request.getObservations()) {
                SosObservationConstellation sosObsConst = sosObservation.getObservationConstellation();
                Set<ObservationConstellationOfferingObservationType> hObsConstOffObsTypes =
                                                                     new HashSet<ObservationConstellationOfferingObservationType>(0);
                FeatureOfInterest hFeature = null;
                for (String offeringID : sosObsConst.getOfferings()) {
                    ObservationConstellationOfferingObservationType hObsConstOffObsType = null;
                    try {
                        hObsConstOffObsType =
                        // HibernateUtilities.checkObservationConstellationForObservation(sosObsConst,
                        // offeringID, session,
                        // Sos2Constants.InsertObservationParams.observationType.name());
                        HibernateUtilities.checkObservationConstellationOfferingObservationTypeForObservation(
                                sosObsConst, offeringID, session,
                                Sos2Constants.InsertObservationParams.observationType.name());
                    } catch (OwsExceptionReport owse) {
                        exceptions.add(owse);
                    }
                    if (hObsConstOffObsType != null) {
                        hFeature =
                        HibernateUtilities.checkOrInsertFeatureOfInterest(sosObservation
                                .getObservationConstellation().getFeatureOfInterest(), session);
                        HibernateUtilities.checkOrInsertFeatureOfInterestRelatedFeatureRelation(hFeature,
                                                                                                hObsConstOffObsType
                                .getOffering(), session);
                        if (isSweArrayObservation(hObsConstOffObsType)) {
                            ResultTemplate resultTemplate =
                                           createResultTemplate(sosObservation, hObsConstOffObsType, hFeature);
                            session.save(resultTemplate);
                            session.flush();
                        }
                        hObsConstOffObsTypes.add(hObsConstOffObsType);
                    }
                }

                if (!hObsConstOffObsTypes.isEmpty()) {
                    if (sosObservation.getValue() instanceof SosSingleObservationValue) {
                        HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(hObsConstOffObsTypes,
                                                                                             hFeature, sosObservation, session);
                    } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
                        HibernateCriteriaTransactionalUtilities.insertObservationMutliValue(hObsConstOffObsTypes,
                                                                                            hFeature, sosObservation, session);
                    }
                }
            }
            // if no observationConstellation is valid, throw exception
            if (exceptions.size() == request.getObservations().size()) {
                throw exceptions;
            }
            session.flush();
            transaction.commit();
        } catch (HibernateException he) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new NoApplicableCodeException().causedBy(he)
                    .withMessage("Error while inserting new observation!");
        } finally {
            sessionHolder.returnSession(session);
        }
        // TODO: ... all the DS insertion stuff
        // Requirement 68
        // proc/obsProp/Offering same obsType;

        return response;
    }

    private boolean isSweArrayObservation(ObservationConstellationOfferingObservationType hObsConstOffObsType) {
        return hObsConstOffObsType.getObservationType().getObservationType()
                .equalsIgnoreCase(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
    }

    private ResultTemplate createResultTemplate(SosObservation observation,
                                                ObservationConstellationOfferingObservationType hObsConstOffObsType,
                                                FeatureOfInterest feature)
            throws OwsExceptionReport {
        ResultTemplate resultTemplate = new ResultTemplate();
        // TODO identifier handling: ignoring code space now
        String identifier;
        if (observation.getIdentifier() != null && observation.getIdentifier().getValue() != null
            && !observation.getIdentifier().getValue().isEmpty()) {
            identifier = observation.getIdentifier().getValue();
        } else {
            identifier = UUID.randomUUID().toString();
        }
        resultTemplate.setIdentifier(identifier);
        resultTemplate.setObservationConstellationOfferingObservationType(hObsConstOffObsType);
        resultTemplate.setFeatureOfInterest(feature);
        SosSweDataArray dataArray = ((SweDataArrayValue) observation.getValue().getValue()).getValue();

        if (dataArray.getElementType().getXml() == null) {
            EncoderKey key = CodingHelper.getEncoderKey(SWEConstants.NS_SWE_20, dataArray.getElementType());
            Encoder<XmlObject, SosSweAbstractDataComponent> encoder =
                                                            getConfigurator().getCodingRepository().getEncoder(key);
            if (encoder == null) {
                throw new NoEncoderForKeyException(key);
            }
            XmlObject encodedXMLObject = encoder.encode(dataArray.getElementType());
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
            EncoderKey key = CodingHelper.getEncoderKey(SWEConstants.NS_SWE_20, dataArray.getEncoding());
            Encoder<XmlObject, SosSweAbstractEncoding> encoder =
                                                       getConfigurator().getCodingRepository().getEncoder(key);
            if (encoder == null) {
                throw new NoEncoderForKeyException(key);
            }
            XmlObject encodedXmlObject = encoder.encode(dataArray.getEncoding());
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
}