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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.ds.IInsertObservationDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.om.SosObservationConstellation;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.InsertObservationRequest;
import org.n52.sos.response.InsertObservationResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SosInsertObservationOperatorV20 implements IRequestOperator {

    /** the data access object for the DescribeSensor operation */
    private IInsertObservationDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = SosConstants.Operations.InsertObservation.name();

    private RequestOperatorKeyType requestOperatorKeyType;

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosInsertObservationOperatorV20.class);

    public SosInsertObservationOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION),
                        OPERATION_NAME);
        this.dao = (IInsertObservationDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info(String.format("%s initialized successfully!",this.getClass().getName()));
    }

    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof InsertObservationRequest) {
            InsertObservationRequest sosRequest = (InsertObservationRequest) request;
            checkRequestedParameter(sosRequest);
            InsertObservationResponse response = this.dao.insertObservation(sosRequest);
            Configurator.getInstance().getCapabilitiesCacheController().updateAfterObservationInsertion();
            String contentType = SosConstants.CONTENT_TYPE_XML;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                String namespace = Sos2Constants.NS_SOS_20;
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
            String exceptionText = "Received request is not a SosInsertObservationRequest!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
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

    private void checkRequestedParameter(InsertObservationRequest request) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
        try {
            SosHelper.checkServiceParameter(request.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        checkAndAddOfferingToObservationConstallation(request);
        try {
            OwsHelper.checkSingleVersionParameter(request.getVersion(), Configurator.getInstance()
                    .getSupportedVersions());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // offering [1..*]
        try {
            checkOfferings(request.getOfferings());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        // observation [1..*]
        try {
            checkObservations(request.getObservation());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);

    }

    private void checkOfferings(List<String> offerings) throws OwsExceptionReport {
        // TODO: Check requirement for this case in SOS 2.0 specification
        if (offerings == null || (offerings != null && offerings.isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(Sos2Constants.InsertObservationParams.offering
                    .name());
        } else {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (String offering : offerings) {
                if (!Configurator.getInstance().getCapabilitiesCacheController().getOfferings().contains(offering)) {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested offering (");
                    exceptionText.append(offering);
                    exceptionText.append(") is not provided by this server!");
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.offering.name(), exceptionText.toString()));
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    private void checkAndAddOfferingToObservationConstallation(InsertObservationRequest request)
            throws OwsExceptionReport {
        for (String offeringIdentifier : request.getOfferings()) {
            if (Configurator.getInstance().getCapabilitiesCacheController().getOfferings()
                    .contains(offeringIdentifier)) {
                for (SosObservation observation : request.getObservation()) {
                    observation.getObservationConstellation().addOffering(offeringIdentifier);
                }
            } else {
                StringBuilder exceptionText = new StringBuilder();
                exceptionText.append("The requested offering (");
                exceptionText.append(offeringIdentifier);
                exceptionText.append(") is not supported by this server!");
                LOGGER.warn(exceptionText.toString());
            }
        }
    }

    private void checkObservations(List<SosObservation> observations) throws OwsExceptionReport {
        ACapabilitiesCacheController capsController = Configurator.getInstance().getCapabilitiesCacheController();
        if (observations == null || (observations != null && observations.isEmpty())) {
            throw Util4Exceptions
                    .createMissingParameterValueException(Sos2Constants.InsertObservationParams.observation.name());
        } else {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (SosObservation observation : observations) {
                SosObservationConstellation obsConstallation = observation.getObservationConstellation();
                // Requirement 67
                if (!capsController.getObservationTypes().contains(obsConstallation.getObservationType())) {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested observationType (");
                    exceptionText.append(observation.getObservationConstellation().getObservationType());
                    exceptionText.append(") is not supported by this server!");
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            Sos2Constants.InsertObservationParams.observationType.name(), exceptionText.toString()));
                } else {
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

    @Override
    public IExtension getExtension(Object connection) throws OwsExceptionReport {
        return dao.getExtension(connection);
    }

    @Override
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        if (hasImplementedDAO()) {
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/obsInsertion");
        }
        return conformanceClasses;
    }

}
