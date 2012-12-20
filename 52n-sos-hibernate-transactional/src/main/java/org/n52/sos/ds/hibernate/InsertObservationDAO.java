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
import org.n52.sos.ds.IConnectionProvider;
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
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OWSParameterDataType;
import org.n52.sos.ogc.ows.OWSParameterValuePossibleValues;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SosSweDataArray;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
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
        
        // get DCP
        DecoderKeyType dkt = null;
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            dkt = new DecoderKeyType(Sos1Constants.NS_SOS);
        } else {
            dkt = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        }
        Map<String, List<String>> dcpMap = SosHelper.getDCP(OPERATION_NAME, dkt,
                Configurator.getInstance().getBindingOperators().values(), Configurator.getInstance().getServiceURL());
        if (dcpMap != null && !dcpMap.isEmpty()) {
            OWSOperation opsMeta = new OWSOperation();
            // set operation name
            opsMeta.setOperationName(OPERATION_NAME);
    
            // set DCP
            opsMeta.setDcp(dcpMap);
            // set offering
            opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.offering.name(),
                    new OWSParameterValuePossibleValues(Configurator.getInstance().getCapabilitiesCacheController()
                            .getOfferings()));
            // set observation
            opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.observation.name(),
                    new OWSParameterValuePossibleValues(new ArrayList<String>(0)));
            opsMeta.addParameterValue(Sos2Constants.InsertObservationParams.observation.name(), new OWSParameterDataType(
                    "http://schemas.opengis.net/om/2.0/observation.xsd#OM_Observation"));
            return opsMeta;
        }
        return null;
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
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>(0);
            for (SosObservation sosObservation : request.getObservation()) {
                SosObservationConstellation sosObsConst = sosObservation.getObservationConstellation();
                ObservationConstellation hObsConst = null;
                for (String offeringID : sosObsConst.getOfferings()) {
                    try {
                        hObsConst =
                                HibernateUtilities.checkObservationConstellationForObservation(sosObsConst, offeringID, session, Sos2Constants.InsertObservationParams.observationType.name());
                    } catch (OwsExceptionReport owse) {
                        exceptions.add(owse);
                    }
                    if (hObsConst != null) {
                    	FeatureOfInterest hFeature =
                                HibernateUtilities.checkOrInsertFeatureOfInterest(sosObservation.getObservationConstellation()
                                        .getFeatureOfInterest(), session);
                        HibernateUtilities.checkOrInsertFeatureOfInterestRelatedFeatureRelation(hFeature, hObsConst.getOffering(), session);
                    	if (isSweArrayObservation(hObsConst))
                    	{
                    		ResultTemplate resultTemplate = createResultTemplate(sosObservation, hObsConst, hFeature);
                    		session.save(resultTemplate);
                    		session.flush();
                    	}
                        if (sosObservation.getValue() instanceof SosSingleObservationValue) {
                            HibernateCriteriaTransactionalUtilities.insertObservationSingleValue(hObsConst, hFeature, sosObservation, session);
                        } else if (sosObservation.getValue() instanceof SosMultiObservationValues) {
                            HibernateCriteriaTransactionalUtilities.insertObservationMutliValue(hObsConst, hFeature, sosObservation, session);
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
            connectionProvider.returnConnection(session);
        }
        // TODO: ... all the DS insertion stuff
        // Requirement 68
        // proc/obsProp/Offering same obsType;

        return response;
    }

	private boolean isSweArrayObservation(ObservationConstellation obsConst)
	{
		return obsConst.getObservationType().getObservationType().equalsIgnoreCase(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
	}

	private ResultTemplate createResultTemplate(SosObservation observation,
			ObservationConstellation obsConst,
			FeatureOfInterest feature) throws OwsExceptionReport
	{
		ResultTemplate resultTemplate = new ResultTemplate();
		// TODO identifier handling: ignoring code space now
		String identifier = null;
		if (observation.getIdentifier() != null && observation.getIdentifier().getValue() != null && !observation.getIdentifier().getValue().isEmpty())
		{
			identifier = observation.getIdentifier().getValue();
		}
		else {
			identifier = UUID.randomUUID().toString();
		}
		resultTemplate.setIdentifier(identifier);
		resultTemplate.setObservationConstellation(obsConst);
		resultTemplate.setFeatureOfInterest(feature);
		SosSweDataArray dataArray = ((SweDataArrayValue)observation.getValue().getValue()).getValue();
		SweCommonEncoderv20 sweEncoder = null;
		if (isEncoderRequired(dataArray))
		{
			IEncoder encoder = Configurator.getInstance().getEncoder(SWEConstants.NS_SWE_20);
			if (encoder instanceof SweCommonEncoderv20)
			{
				sweEncoder = (SweCommonEncoderv20) encoder;
			}
			else
			{
				String errorMsg = String.format("Could not find encoder for namespace \"%s\".", SWEConstants.NS_SWE_20);
				LOGGER.error(errorMsg);
				throw Util4Exceptions.createNoApplicableCodeException(null, errorMsg);
			}
		}
		if (dataArray.getElementType().getXml() == null)
		{
			XmlObject encodedXMLObject = sweEncoder.encode(dataArray.getElementType());
			if (encodedXMLObject instanceof DataRecordType)
			{
				DataRecordDocument xbDataRecord = DataRecordDocument.Factory.newInstance();
				xbDataRecord.setDataRecord((DataRecordType) encodedXMLObject);
				encodedXMLObject = xbDataRecord;
			}
			resultTemplate.setResultStructure(encodedXMLObject.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
		}
		else
		{
			resultTemplate.setResultStructure(dataArray.getElementType().getXml());
		}
		if (dataArray.getEncoding().getXml() == null)
		{
			XmlObject encodedXmlObject = sweEncoder.encode(dataArray.getEncoding());
			if (encodedXmlObject instanceof TextEncodingType)
			{
				TextEncodingDocument xbTextEncodingDoc = TextEncodingDocument.Factory.newInstance();
				xbTextEncodingDoc.setTextEncoding((TextEncodingType) encodedXmlObject);
				encodedXmlObject = xbTextEncodingDoc;
			}
			resultTemplate.setResultEncoding(encodedXmlObject.xmlText(XmlOptionsHelper.getInstance().getXmlOptions()));
		}
		else
		{
			resultTemplate.setResultEncoding(dataArray.getEncoding().getXml());
		}
		return resultTemplate;
	}

	private boolean isEncoderRequired(SosSweDataArray dataArray)
	{
		return dataArray.getElementType().getXml() == null || dataArray.getEncoding().getXml() == null;
	}

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }
}
