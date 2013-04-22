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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.ds.AbstractInsertObservationDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.ObservationInsertion;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.EncoderResponseUnsupportedException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.InvalidObservationTypeException;
import org.n52.sos.exception.ows.concrete.InvalidObservationTypeForOfferingException;
import org.n52.sos.exception.ows.concrete.InvalidOfferingParameterException;
import org.n52.sos.exception.ows.concrete.MissingObservationParameterException;
import org.n52.sos.exception.ows.concrete.MissingOfferingParameterException;
import org.n52.sos.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.OMHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLConstants;
import org.n52.sos.wsdl.WSDLOperation;

public class SosInsertObservationOperatorV20 extends AbstractV2RequestOperator<AbstractInsertObservationDAO, InsertObservationRequest> {
    private static final String OPERATION_NAME = SosConstants.Operations.InsertObservation.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_OBSERVATION_INSERTION);

    public SosInsertObservationOperatorV20() {
        super(OPERATION_NAME, InsertObservationRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(InsertObservationRequest request) throws OwsExceptionReport {
        checkRequestedParameter(request);
        InsertObservationResponse response = getDao().insertObservation(request);
        SosEventBus.fire(new ObservationInsertion(request, response));
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Encoder<?, InsertObservationResponse> encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, response));
            if (encoder != null) {
                // TODO valid response object
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

    private void checkRequestedParameter(InsertObservationRequest request) throws OwsExceptionReport {
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
        // offering [1..*]
        try {
            checkAndAddOfferingToObservationConstallation(request);
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // observation [1..*]
        try {
            checkObservations(request.getObservations());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        exceptions.throwIfNotEmpty();
    }

    private void checkAndAddOfferingToObservationConstallation(InsertObservationRequest request)
            throws OwsExceptionReport {
        // TODO: Check requirement for this case in SOS 2.0 specification
        if (request.getOfferings() == null || (request.getOfferings() != null && request.getOfferings().isEmpty())) {
            throw new MissingOfferingParameterException();
        } else {
            CompositeOwsException exceptions = new CompositeOwsException();
            for (String offering : request.getOfferings()) {
                if (offering == null || offering.isEmpty()) {
                    exceptions.add(new MissingOfferingParameterException());
                } else if (!Configurator.getInstance().getCache().getOfferings().contains(offering)) {
                    exceptions.add(new InvalidOfferingParameterException(offering));
                } else {
                    for (SosObservation observation : request.getObservations()) {
                        observation.getObservationConstellation().addOffering(offering);
                    }
                }
            }
            exceptions.throwIfNotEmpty();
        }
    }

    private void checkObservations(List<SosObservation> observations) throws OwsExceptionReport {
        if (observations == null || observations.isEmpty()) {
            throw new MissingObservationParameterException();
        } else {
        	ContentCache cache = Configurator.getInstance().getCache();
            CompositeOwsException exceptions = new CompositeOwsException();
            for (SosObservation observation : observations) {
                SosObservationConstellation obsConstallation = observation.getObservationConstellation();
                checkObservationConstellationParameter(obsConstallation);
                // Requirement 67
                checkOrSetObservationType(observation);
                if (!cache.getObservationTypes().contains(obsConstallation.getObservationType())) {
                    exceptions.add(new InvalidObservationTypeException(obsConstallation.getObservationType()));
                } else if (obsConstallation.isSetOfferings()) {
                    for (String offeringID : obsConstallation.getOfferings()) {
                        Collection<String> allowedObservationTypes =
                                           cache.getAllowedObservationTypesForOffering(offeringID);
                        if (allowedObservationTypes == null || !allowedObservationTypes.contains(obsConstallation
                                .getObservationType())) {
                            exceptions
                                    .add(new InvalidObservationTypeForOfferingException(obsConstallation
                                    .getObservationType(), offeringID));
                        }
                    }
                }
            }
            exceptions.throwIfNotEmpty();
        }
    }

    private void checkObservationConstellationParameter(SosObservationConstellation obsConstallation) throws
            OwsExceptionReport {
        checkProcedureID(obsConstallation.getProcedure().getIdentifier(),
                         Sos2Constants.InsertObservationParams.procedure.name());
        checkObservedProperty(obsConstallation.getObservableProperty().getIdentifier(),
                              Sos2Constants.InsertObservationParams.observedProperty.name());
    }


    private void checkOrSetObservationType(SosObservation sosObservation) throws OwsExceptionReport {
        SosObservationConstellation observationConstellation = sosObservation.getObservationConstellation();
        String obsTypeFromValue = OMHelper.getObservationTypeFromValue(sosObservation.getValue().getValue());
        if (observationConstellation.isSetObservationType()) {
            checkObservationType(observationConstellation.getObservationType(),
                                 Sos2Constants.InsertObservationParams.observationType.name());
            if (obsTypeFromValue != null
                    && !sosObservation.getObservationConstellation().getObservationType().equals(obsTypeFromValue)) {
                throw new NoApplicableCodeException()
                        .withMessage("The requested observation is invalid! The result element does not comply with the defined type (%s)!",
                                     sosObservation.getObservationConstellation().getObservationType());
            }
        } else {
            sosObservation.getObservationConstellation().setObservationType(obsTypeFromValue);
        }
    }
    
    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.INSERT_OBSERVATION;
    }
}
