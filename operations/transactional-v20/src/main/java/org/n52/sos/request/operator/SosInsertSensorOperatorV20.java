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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.ds.AbstractInsertSensorDAO;
import org.n52.sos.encode.Encoder;
import org.n52.sos.event.SosEventBus;
import org.n52.sos.event.events.SensorInsertion;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.MissingParameterValueException;
import org.n52.sos.exception.ows.concrete.EncoderResponseUnsupportedException;
import org.n52.sos.exception.ows.concrete.ErrorWhileSavingResponseToOutputStreamException;
import org.n52.sos.exception.ows.concrete.InvalidFeatureOfInterestTypeException;
import org.n52.sos.exception.ows.concrete.MissingFeatureOfInterestTypeException;
import org.n52.sos.exception.ows.concrete.MissingObservedPropertyParameterException;
import org.n52.sos.exception.ows.concrete.NoEncoderForResponseException;
import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.ows.CompositeOwsException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.AbstractProcess;
import org.n52.sos.ogc.sensorML.SensorML;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.MiscSettings;
import org.n52.sos.util.CodingHelper;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.util.JavaHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.n52.sos.wsdl.WSDLConstants;
import org.n52.sos.wsdl.WSDLOperation;

@Configurable
public class SosInsertSensorOperatorV20 extends
        AbstractV2RequestOperator<AbstractInsertSensorDAO, InsertSensorRequest> {

    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertSensor.name();

    private static final Set<String> CONFORMANCE_CLASSES = CollectionHelper.set(
            ConformanceClasses.SOS_V2_INSERTION_CAPABILITIES, ConformanceClasses.SOS_V2_SENSOR_INSERTION);

    private String defaultOfferingPrefix;

    private String defaultProcedurePrefix;

    public SosInsertSensorOperatorV20() {
        super(OPERATION_NAME, InsertSensorRequest.class);
    }

    public String getDefaultOfferingPrefix() {
        return this.defaultOfferingPrefix;
    }

    @Setting(MiscSettings.DEFAULT_OFFERING_PREFIX)
    public void setDefaultOfferingPrefix(String prefix) {
        this.defaultOfferingPrefix = prefix;
    }

    public String getDefaultProcedurePrefix() {
        return this.defaultProcedurePrefix;
    }

    @Setting(MiscSettings.DEFAULT_PROCEDURE_PREFIX)
    public void setDefaultProcedurePrefix(String prefix) {
        this.defaultProcedurePrefix = prefix;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public WSDLOperation getSosOperationDefinition() {
        return WSDLConstants.Operations.INSERT_SENSOR;
    }

    @Override
    public ServiceResponse receive(InsertSensorRequest request) throws OwsExceptionReport {
        checkRequestedParameter(request);
        InsertSensorResponse response = getDao().insertSensor(request);
        SosEventBus.fire(new SensorInsertion(request, response));
        String contentType = SosConstants.CONTENT_TYPE_XML;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Encoder<?, InsertSensorResponse> encoder =
                    Configurator.getInstance().getCodingRepository()
                            .getEncoder(CodingHelper.getEncoderKey(SWEConstants.NS_SWES_20, response));
            if (encoder != null) {
                // TODO valid or validate (?) response object
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

    private void checkRequestedParameter(InsertSensorRequest request) throws OwsExceptionReport {
        CompositeOwsException exceptions = new CompositeOwsException();
        // check parameters with variable content
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
        try {
            checkObservableProperty(request.getObservableProperty());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkProcedureDescriptionFormat(request.getProcedureDescriptionFormat(),
                    Sos2Constants.InsertSensorParams.procedureDescriptionFormat.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        checkAndSetAssignedProcedureID(request);
        checkAndSetAssignedOffering(request);
        try {
            checkProcedureAndOfferingCombination(request);
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
            exceptions.add(new MissingParameterValueException(Sos2Constants.InsertSensorParams.observationType));
            exceptions.add(new MissingParameterValueException(Sos2Constants.InsertSensorParams.featureOfInterestType));
        }
        exceptions.throwIfNotEmpty();
    }

    private void checkObservableProperty(List<String> observableProperty) throws OwsExceptionReport {
        if (observableProperty == null || observableProperty.isEmpty()) {
            throw new MissingObservedPropertyParameterException();
        } else {
            // TODO: check with existing and/or defined in outputs
        }
    }

    private void checkFeatureOfInterestTypes(Set<String> featureOfInterestTypes) throws OwsExceptionReport {
        if (featureOfInterestTypes != null) {
            CompositeOwsException exceptions = new CompositeOwsException();
            Collection<String> validFeatureOfInterestTypes =
                    Configurator.getInstance().getCache().getFeatureOfInterestTypes();
            for (String featureOfInterestType : featureOfInterestTypes) {
                if (featureOfInterestType.isEmpty()) {
                    exceptions.add(new MissingFeatureOfInterestTypeException());
                } else {
                    if (!validFeatureOfInterestTypes.contains(featureOfInterestType)) {
                        exceptions.add(new InvalidFeatureOfInterestTypeException(featureOfInterestType));
                    }
                }
            }
            exceptions.throwIfNotEmpty();
        }
    }

    private void checkObservationTypes(Set<String> observationTypes) throws OwsExceptionReport {
        if (observationTypes != null) {
            CompositeOwsException exceptions = new CompositeOwsException();
            for (String observationType : observationTypes) {
                try {
                    checkObservationType(observationType, Sos2Constants.InsertSensorParams.observationType.name());
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            exceptions.throwIfNotEmpty();
        }
    }

    private void checkAndSetAssignedProcedureID(InsertSensorRequest request) {
        if (request.getProcedureDescription().isSetIdentifier()) {
            request.setAssignedProcedureIdentifier(request.getProcedureDescription().getIdentifier());
        } else {
            request.setAssignedProcedureIdentifier(getDefaultProcedurePrefix()
                    + JavaHelper.generateID(request.getProcedureDescription().toString()));
        }
    }

    private void checkAndSetAssignedOffering(InsertSensorRequest request) {
        // if procedureDescription is SensorML
        List<SosOffering> sosOfferings = null;
        if (request.getProcedureDescription().isSetOffering()) {
            sosOfferings = request.getProcedureDescription().getOfferingIdentifiers();
        } else {
            if (request.getProcedureDescription() instanceof SensorML) {
                SensorML sensorML = (SensorML) request.getProcedureDescription();
                // if SensorML is a wrapper and member size is 1
                if (sensorML.isWrapper() && sensorML.getMembers().size() == 1) {
                    AbstractProcess process = sensorML.getMembers().get(0);
                    if (process.isSetOffering()) {
                        sosOfferings = process.getOfferingIdentifiers();
                    }
                    if (process.isSetParentProcedures()) {
                        getOfferingsForParentProcedures(process.getParentProcedures());
                    }
                }
            }
        }
        if (request.getProcedureDescription().isSetParentProcedures()) {

        }
        // check if offering is valid
        if (sosOfferings == null || sosOfferings.isEmpty()) {
            sosOfferings = new ArrayList<SosOffering>(0);
            sosOfferings.add(new SosOffering(getDefaultOfferingPrefix() + request.getAssignedProcedureIdentifier()));
        }
        request.setAssignedOfferings(sosOfferings);
    }

    private void checkProcedureAndOfferingCombination(InsertSensorRequest request) throws OwsExceptionReport {
        for (SosOffering offering : request.getAssignedOfferings()) {
            if (getCache().getOfferings().contains(offering.getOfferingIdentifier())) {
                throw new InvalidParameterValueException()
                        .at(Sos2Constants.InsertSensorParams.offeringIdentifier)
                        .withMessage(
                                "The offering with the identifier '%s' still exists in this service and it is not allowed to insert more than one procedure to an offering!",
                                offering.getOfferingIdentifier());
            }
        }
    }

    private void getParentProcedures() {
        // checkParentProcedures if exist, else exception
        // insert proc as hidden child to parents
        // insert proc with new offering if set
        // set relation in sensor_system
    }

    private void getChildProcedures() {
        // add parent offerings
        // insert if not exist and proc is encoded, else Exception
        // insert as hidden child
        // set relation in sensor_system
    }

    private void getOfferingsForParentProcedures(Set<String> set) {
        // TODO Auto-generated method stub

    }

}
