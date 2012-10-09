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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.ds.IInsertSensorDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.InsertSensorRequest;
import org.n52.sos.response.InsertSensorResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosInsertSensorOperatorV20 implements IRequestOperator {

    /** the data access object for the DescribeSensor operation */
    private IInsertSensorDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = Sos2Constants.Operations.InsertSensor.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertSensorOperatorV20.class);

    public SosInsertSensorOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IInsertSensorDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info(String.format("%s initialized successfully!",this.getClass().getName()));
    }

    @Override
    public boolean hasImplementedDAO() {
        if (this.dao != null) {
            return true;
        }
        return false;
    }

    @Override
    public RequestOperatorKeyType getRequestOperatorKeyType() {
        return requestOperatorKeyType;
    }

    @Override
    public OWSOperation getOperationMetadata(String service, String version, Object connection)
            throws OwsExceptionReport {
        return dao.getOperationsMetadata(service, version, connection);
    }
    
    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        return dao.getExtension(connection);
    }

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof InsertSensorRequest) {
            InsertSensorRequest sosRequest = (InsertSensorRequest) request;
            checkRequestedParameter(sosRequest);
    
            InsertSensorResponse response = this.dao.insertSensor(sosRequest);
            // TODO: create update() for after insert sensor
            Configurator.getInstance().getCapabilitiesCacheController().updateAfterSensorInsertion();
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                String namespace = SWEConstants.NS_SWES_20;
                IEncoder encoder = Configurator.getInstance().getEncoder(namespace);
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
        } else {
            String exceptionText = "Received request is not a InsertSensorRequest!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }

    private void checkRequestedParameter(InsertSensorRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
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
        if (request.getMetadata() != null) {
            try {
                checkFeatureOfInterestTypes(request.getMetadata().getFeatureOfInterestTypes());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
            try {
                checkObservationTypes(request.getMetadata().getObservationTypes());
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        } else {
            exceptions.add(Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertSensorParams.featureOfInterestType.name()));
            exceptions.add(Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertSensorParams.observationType.name()));
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

    private void checkObservablePropterty(List<String> observableProperty) throws OwsExceptionReport {
        if (observableProperty == null || (observableProperty != null && observableProperty.isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertSensorParams.observableProperty.name());
        } else {
            // TODO: check with existing and/or defined in outputs
        }
    }

    private void checkFeatureOfInterestTypes(List<String> featureOfInterestTypes) throws OwsExceptionReport {
        if (featureOfInterestTypes == null || (featureOfInterestTypes != null && featureOfInterestTypes.isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertSensorParams.featureOfInterestType.name());
        } else {
            // TODO: check if types are supported here (Caching) or in DAO?
        }
    }

    private void checkObservationTypes(List<String> observationTypes) throws OwsExceptionReport {
        if (observationTypes == null || (observationTypes != null && observationTypes.isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertSensorParams.observationType.name());
        } else {
            // TODO: check if types are supported here (Caching) or in DAO?
        }
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        if (hasImplementedDAO()) {
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/insertionCap");
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/sensorInsertion");
        }
        return conformanceClasses;
    }

}