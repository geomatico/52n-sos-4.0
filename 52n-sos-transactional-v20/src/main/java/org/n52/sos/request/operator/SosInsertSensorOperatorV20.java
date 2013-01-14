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
package org.n52.sos.request.operator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IInsertSensorDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosInsertSensorOperatorV20 extends AbstractV2RequestOperator<IInsertSensorDAO, InsertSensorRequest>  {
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertSensorOperatorV20.class);
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertSensor.name();
    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(ConformanceClasses.SOS_V2_INSERTION_CAPABILITIES, ConformanceClasses.SOS_V2_SENSOR_INSERTION);
    
    public SosInsertSensorOperatorV20() {
        super(OPERATION_NAME, InsertSensorRequest.class);
    }
    
    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(InsertSensorRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameter(sosRequest);

        InsertSensorResponse response = getDao().insertSensor(sosRequest);
        // TODO: create update() for after insert sensor
        Configurator.getInstance().getCapabilitiesCacheController().updateAfterSensorInsertion();
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IEncoder<?, InsertSensorResponse> encoder = Configurator.getInstance().getCodingRepository()
                    .getEncoder(CodingHelper.getEncoderKey(SWEConstants.NS_SWES_20, response));
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

    private void checkRequestedParameter(InsertSensorRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
        // check parameters with variable content
        try {
            SosHelper.checkServiceParameter(request.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            OwsHelper.checkSingleVersionParameter(request.getVersion(), Configurator.getInstance()
                    .getSupportedVersions());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkObservablePropterty(request.getObservableProperty());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkProcedureDescriptionFormat(request.getProcedureDescriptionFormat(),
                    Sos2Constants.InsertSensorParams.procedureDescriptionFormat.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        if (request.getMetadata() != null) {
            try {
                checkObservationTypes(request.getMetadata().getObservationTypes());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                checkFeatureOfInterestTypes(request.getMetadata().getFeatureOfInterestTypes());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        } else {
            exceptions.add(Util4Exceptions
                    .createMissingParameterValueException(Sos2Constants.InsertSensorParams.observationType.name()));
            exceptions.add(Util4Exceptions
                    .createMissingParameterValueException(Sos2Constants.InsertSensorParams.featureOfInterestType
                            .name()));
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

    private void checkObservablePropterty(List<String> observableProperty) throws OwsExceptionReport {
        if (observableProperty == null || (observableProperty != null && observableProperty.isEmpty())) {
            throw Util4Exceptions
                    .createMissingParameterValueException(Sos2Constants.InsertSensorParams.observableProperty.name());
        } else {
            // TODO: check with existing and/or defined in outputs
        }
    }

    private void checkFeatureOfInterestTypes(List<String> featureOfInterestTypes) throws OwsExceptionReport {
        if (featureOfInterestTypes != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            Collection<String> validFeatureOfInterestTypes =
                    Configurator.getInstance().getCapabilitiesCacheController().getFeatureOfInterestTypes();
            for (String featureOfInterestType : featureOfInterestTypes) {
                if (featureOfInterestType.isEmpty()) {
                    exceptions
                            .add(Util4Exceptions
                                    .createMissingParameterValueException(Sos2Constants.InsertSensorParams.featureOfInterestType
                                            .name()));
                } else {
                    if (!validFeatureOfInterestTypes.contains(featureOfInterestType)) {
                        String exceptionText =
                                "The value (" + featureOfInterestType + ") of the parameter '"
                                        + Sos2Constants.InsertSensorParams.featureOfInterestType.name()
                                        + "' is invalid";
                        LOGGER.error(exceptionText);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                Sos2Constants.InsertSensorParams.featureOfInterestType.name(), exceptionText));
                    }
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    private void checkObservationTypes(List<String> observationTypes) throws OwsExceptionReport {
        if (observationTypes != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String observationType : observationTypes) {
                try {
                    SosHelper.checkObservationType(observationType, Sos2Constants.InsertSensorParams.observationType.name());
                } catch (OwsExceptionReport e) {
                   exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }
}
