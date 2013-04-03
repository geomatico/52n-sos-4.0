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
package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.AbstractInsertResultTemplateDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.ResultTemplateInsertion;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.MissingParameterValueException;
import org.n52.sos.exception.ows.concrete.DuplicateIdentifierException;
import org.n52.sos.exception.ows.concrete.EncoderResponseUnsupportedException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.InsertResultTemplateRequest;
import org.n52.sos.response.InsertResultTemplateResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLConstants;
import org.n52.sos.wsdl.WSDLOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosInsertResultTemplateOperatorV20 extends AbstractV2RequestOperator<AbstractInsertResultTemplateDAO, InsertResultTemplateRequest> {

    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertResultTemplate.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_RESULT_INSERTION);
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertResultTemplateOperatorV20.class);

    public SosInsertResultTemplateOperatorV20() {
        super(OPERATION_NAME, InsertResultTemplateRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(InsertResultTemplateRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameter(sosRequest);
        InsertResultTemplateResponse response = getDao().insertResultTemplate(sosRequest);
        SosEventBus.fire(new ResultTemplateInsertion(sosRequest, response));
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Encoder<?, InsertResultTemplateResponse> encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, response));
            if (encoder != null) {
                Object encodedObject = encoder.encode(response);
                if (encodedObject instanceof XmlObject) {
                    ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                    return new ServiceResponse(baos, contentType, false, true);
                } else if (encodedObject instanceof ServiceResponse) {
                    return (ServiceResponse) encodedObject;
                } else {
                    throw new EncoderResponseUnsupportedException();
                }
            } else {
                throw new NoEncoderForResponseException();
            }
        } catch (IOException ioe) {
            throw new ErrorWhileSavingResponseToOutputStreamException(ioe);
        }
    }

    private void checkRequestedParameter(InsertResultTemplateRequest request) throws OwsExceptionReport {
        CompositeOwsException exceptions = new CompositeOwsException();
        try {
            checkServiceParameter(request.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkSingleVersionParameter(request);
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // check offering
        try {
            checkOfferings(request.getObservationTemplate().getOfferings(),
                           Sos2Constants.InsertResultTemplateParams.proposedTemplate.name());
            try {
                checkObservationType(request);
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // check procedure
        try {
            checkProcedureID(request.getObservationTemplate().getProcedure().getIdentifier(),
                             Sos2Constants.InsertResultTemplateParams.proposedTemplate.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // check observedProperty
        try {
            checkObservedProperty(request.getObservationTemplate().getObservableProperty().getIdentifier(),
                                  Sos2Constants.InsertResultTemplateParams.proposedTemplate.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        String identifier = request.getObservationTemplate().getFeatureOfInterest().getIdentifier().getValue();
        if (identifier.isEmpty()) {
            exceptions
                    .add(new MissingParameterValueException(Sos2Constants.InsertResultTemplateParams.proposedTemplate));
        }

        // check identifier
        try {
            checkResultTemplateIdentifier(request.getIdentifier());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        exceptions.throwIfNotEmpty();
        // TODO check parameter as defined in SOS 2.0 spec

        /*
         * check observation template
         *
         * same resultSTructure for procedure, obsProp and Offering
         *
         * empty phenTime, resultTime and result
         *
         * phenTime and resultTime nilReason = 'template'
         *
         * proc, foi, obsProp not empty
         *
         * resultStructure: swe:Time or swe:TimeRange with value
         * "http://www.opengis.net/def/property/OGC/0/PhenomenonTime"
         *
         * If the resultStructure in the ResultTemplate has a swe:Time component
         * with definition property set to the value
         * "http://www.opengis.net/def/property/OGC/0/ResultTime" then the value
         * of this component shall be used by the service to populate the
         * om:resultTime property of the observation template for each new
         * result block the client is going to insert via the InsertResult
         * operation. If no such component is contained in the resultStructure
         * then the service shall use the om:phenomenonTime as value of the
         * om:resultTime (at least the phenomenon time has to be provided in
         * each ResultTemplate). In case the om:phenomenonTime is not a
         * TimeInstant, an InvalidParameterValue exception shall be returned,
         * with locator ‘resultTime’.
         *
         * A client shall encode the om:phenomenonTime as a swe:Time or
         * swe:TimeRange component with definition
         * "http://www.opengis.net/def/property/OGC/0/PhenomenonTime". in the
         * resultStructure that it proposes to the service in the
         * InsertResultTemplate operation request. If any of the observation
         * results that the client intends to send to the service via the
         * InsertResult operation is going to have a resultTime that is
         * different to the phenomenonTime then the resultStructure of the
         * ResultTemplate shall also have a swe:Time component with definition
         * "http://www.opengis.net/def/property/OGC/0/ResultTime".
         *
         * If a result template with differing observationType or (SWE Common
         * encoded) result structure is inserted for the same constellation of
         * procedure, observedProperty and ObservationOffering (for which
         * observations already exist) an exception shall be returned with the
         * ExceptionCode "InvalidParameterValue" and locator value
         * "proposedTemplate".
         */
    }

    private void checkResultTemplateIdentifier(String identifier) throws OwsExceptionReport {
        if (getCache().hasResultTemplate(identifier)) {
            throw new DuplicateIdentifierException("resultTemplate", identifier);
        }

    }

    private void checkObservationType(InsertResultTemplateRequest request) throws OwsExceptionReport {
        SosObservationConstellation observationConstellation = request.getObservationTemplate();
        if (!observationConstellation.isSetObservationType()) {
            observationConstellation.setObservationType(OMConstants.OBS_TYPE_SWE_ARRAY_OBSERVATION);
        }
        // check if observation type is supported
        checkObservationType(observationConstellation.getObservationType(),
                             Sos2Constants.InsertResultTemplateParams.observationType.name());
        Set<String> validObservationTypesForOffering = new HashSet<String>(0);
        for (String offering : observationConstellation.getOfferings()) {
            validObservationTypesForOffering.addAll(Configurator.getInstance().getCache()
                    .getAllowedObservationTypesForOffering(offering));
        }
        // check if observation type is valid for offering
        if (!validObservationTypesForOffering.contains(observationConstellation.getObservationType())) {
            throw new InvalidParameterValueException().at(Sos2Constants.InsertResultTemplateParams.observationType)
                    .withMessage("The requested observation type is not valid for the offering!");
        }
    }

    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.INSERT_RESULT_TEMPLATE;
    }
}
