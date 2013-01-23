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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.ds.IInsertObservationDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
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
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLOperation;
import org.n52.sos.wsdl.WSDLConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosInsertObservationOperatorV20 extends AbstractV2RequestOperator<IInsertObservationDAO, InsertObservationRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertObservationOperatorV20.class);
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
    public ServiceResponse receive(InsertObservationRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameter(sosRequest);
        InsertObservationResponse response = getDao().insertObservation(sosRequest);
        Configurator.getInstance().getCapabilitiesCacheController().updateAfterObservationInsertion();
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IEncoder<?, InsertObservationResponse> encoder = Configurator.getInstance().getCodingRepository()
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
                    String exceptionText = "The encoder response is not supported!";
                    throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                }
            } else {
                String exceptionText = "Error while getting encoder for response!";
                throw Util4Exceptions.createInvalidParameterValueException("", exceptionText);
            }
        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }
    }

    private void checkRequestedParameter(InsertObservationRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
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
        Util4Exceptions.mergeAndThrowExceptions(exceptions);

    }

    private void checkAndAddOfferingToObservationConstallation(InsertObservationRequest request)
            throws OwsExceptionReport {
        // TODO: Check requirement for this case in SOS 2.0 specification
        if (request.getOfferings() == null || (request.getOfferings() != null && request.getOfferings().isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertObservationParams.offering
                    .name());
        } else {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String offering : request.getOfferings()) {
                if (offering == null || (offering != null && offering.isEmpty())) {
                    throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertObservationParams.offering
                            .name());
                }
                if (!Configurator.getInstance().getCapabilitiesCacheController().getOfferings().contains(offering)) {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested offering (");
                    exceptionText.append(offering);
                    exceptionText.append(") is not supported by this server!");
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.offering.name(), exceptionText.toString()));
                } else {
                    for (SosObservation observation : request.getObservations()) {
                        observation.getObservationConstellation().addOffering(offering);
                    }
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    private void checkObservations(List<SosObservation> observations) throws OwsExceptionReport {
        ACapabilitiesCacheController capsController = Configurator.getInstance().getCapabilitiesCacheController();
        if (observations == null || (observations != null && observations.isEmpty())) {
            throw Util4Exceptions
                    .createMissingParameterValueException(Sos2Constants.InsertObservationParams.observation.name());
        } else {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (SosObservation observation : observations) {
                SosObservationConstellation obsConstallation = observation.getObservationConstellation();
                checkObservationConstellationParameter(obsConstallation);
                // Requirement 67
                checkOrSetObservationType(observation);
                if (!capsController.getObservationTypes().contains(obsConstallation.getObservationType())) {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested observationType (");
                    exceptionText.append(observation.getObservationConstellation().getObservationType());
                    exceptionText.append(") is not supported by this server!");
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.observationType.name(), exceptionText.toString()));
                } else if (obsConstallation.getOfferings() != null) {
                    for (String offeringID : obsConstallation.getOfferings()) {
                        Collection<String> allowedObservationTypes =
                                capsController.getAllowedObservationTypes4Offering(offeringID);
                        if (allowedObservationTypes == null
                                || (allowedObservationTypes != null && !allowedObservationTypes
                                        .contains(obsConstallation.getObservationType()))) {
                            StringBuilder exceptionText = new StringBuilder();
                            exceptionText.append("The requested observationType (");
                            exceptionText.append(obsConstallation.getObservationType());
                            exceptionText.append(") is not allowed for the requested offering (");
                            exceptionText.append(obsConstallation.getOfferings());
                            exceptionText.append(")!");
                            exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                    Sos2Constants.InsertObservationParams.observationType.name(),
                                    exceptionText.toString()));
                        }
                    }
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    private void checkObservationConstellationParameter(SosObservationConstellation obsConstallation) throws OwsExceptionReport {
        ACapabilitiesCacheController capabilitiesCacheController = Configurator.getInstance().getCapabilitiesCacheController();
        checkProcedureID(obsConstallation.getProcedure().getProcedureIdentifier(), capabilitiesCacheController.getProcedures(), Sos2Constants.InsertObservationParams.procedure.name());
        checkObservedProperty(obsConstallation.getObservableProperty().getIdentifier(), capabilitiesCacheController.getObservableProperties(), Sos2Constants.InsertObservationParams.observedProperty.name());
//        String foiIdentifier = obsConstallation.getFeatureOfInterest().getIdentifier().getValue();
//        SosHelper.checkFeatureOfInterstIdentifier(foiIdentifier, capabilitiesCacheController.getFeatureOfInterest(), Sos2Constants.InsertObservationParams.featureOfInterest.name());
    }


    private void checkOrSetObservationType(SosObservation sosObservation) throws OwsExceptionReport {
        SosObservationConstellation observationConstellation = sosObservation.getObservationConstellation();
        String obsTypeFromValue = OMHelper.getObservationTypeFromValue(sosObservation.getValue().getValue());
        if (observationConstellation.getObservationType() != null) {
            SosHelper.checkObservationType(observationConstellation.getObservationType(), Sos2Constants.InsertObservationParams.observationType
                    .name());
            if (obsTypeFromValue != null
                    && !sosObservation.getObservationConstellation().getObservationType().equals(obsTypeFromValue)) {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The requested observation is invalid!");
                exceptionText.append(" The result element does not comply with the defined type (");
                exceptionText.append(sosObservation.getObservationConstellation().getObservationType());
                exceptionText.append(")!");
                LOGGER.debug(exceptionText.toString());
                throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText.toString());
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
