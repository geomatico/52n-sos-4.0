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
import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.encode.IObservationEncoder;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.ows.IExtension;
import org.n52.sos.ogc.ows.OWSOperation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.OwsHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * class and forwards requests to the GetObservationDAO; after query of
 * Database, class encodes the ObservationResponse (thru using the IOMEncoder)
 * 
 */
public class SosGetObservationOperatorV20 implements IRequestOperator {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetObservationOperatorV20.class.getName());

    /** the data access object for the GetObservation operation */
    private IGetObservationDAO dao;

    /** Name of the operation the listener implements */
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservation.name();

    private static final String VERSION = Sos2Constants.SERVICEVERSION;

    private static final String SERVICE = SosConstants.SOS;

    private RequestOperatorKeyType requestOperatorKeyType;

    /**
     * Constructor
     * 
     */
    public SosGetObservationOperatorV20() {
        requestOperatorKeyType =
                new RequestOperatorKeyType(new ServiceOperatorKeyType(SERVICE, VERSION), OPERATION_NAME);
        this.dao = (IGetObservationDAO) Configurator.getInstance().getOperationDAOs().get(OPERATION_NAME);
        LOGGER.info("{} initialized successfully!", this.getClass().getSimpleName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#receiveRequest(org.n52.sos.request
     * .AbstractSosRequest)
     */
    @Override
    public ServiceResponse receiveRequest(AbstractServiceRequest request) throws OwsExceptionReport {
        if (request instanceof GetObservationRequest) {

            GetObservationRequest sosRequest = (GetObservationRequest) request;
            checkRequestedParameters(sosRequest);
            boolean zipCompression = checkResponseFormat(sosRequest);
            String responseFormat = sosRequest.getResponseFormat();
            String contentType = SosConstants.CONTENT_TYPE_XML;
            try {
                // check SOS version for response encoding
                String namespace = responseFormat;
                // // O&M 1.0.0
                // if (responseFormat.equals(OMConstants.CONTENT_TYPE_OM)
                // || responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM)) {
                // namespace = responseFormat;
                // contentType = OMConstants.CONTENT_TYPE_OM;
                // }
                // // O&M 2.0 non SOS 2.0
                // else if
                // (!request.getVersion().equals(Sos2Constants.SERVICEVERSION)
                // && (responseFormat.equals(OMConstants.CONTENT_TYPE_OM_2) ||
                // responseFormat
                // .equals(OMConstants.RESPONSE_FORMAT_OM_2))) {
                // namespace.append(responseFormat);
                // contentType = OMConstants.CONTENT_TYPE_OM_2;
                // }
                // O&M 2.0 for SOS 2.0
                // TODO: check if responseFormat is OM-Subtype
                // else
                if (request.getVersion().equals(Sos2Constants.SERVICEVERSION)
                        && checkForObservationAndMeasurementV20Type(responseFormat)) {
                    namespace = Sos2Constants.NS_SOS_20;
                }
                // } else {
                // namespace.append(responseFormat);
                // }
                IEncoder encoder = Configurator.getInstance().getEncoder(namespace.toString());
                if (encoder != null) {
                    GetObservationResponse response = this.dao.getObservation(sosRequest);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Object encodedObject = encoder.encode(response);
                    contentType = encoder.getContentType();
                    if (encodedObject instanceof XmlObject) {
                        ((XmlObject) encodedObject).save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
                        return new ServiceResponse(baos, contentType, zipCompression, true);
                    } else if (encodedObject instanceof ServiceResponse) {
                        return (ServiceResponse) encodedObject;
                    } else {
                        String exceptionText = "The encoder response is not supported!";
                        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
                    }
                } else {
                    String exceptionText =
                            "The value '" + responseFormat
                                    + "' of the responseFormat parameter is not supported by this server!";
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.GetObservationParams.responseFormat.name(), exceptionText);
                }
            } catch (IOException ioe) {
                String exceptionText = "Error occurs while saving response to output stream!";
                LOGGER.error(exceptionText, ioe);
                throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
            }

        } else {
            String exceptionText = "Received request is not a SosGetObservationRequest!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createOperationNotSupportedException(request.getOperationName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sos.listener.ISosRequestListener#hasImplementedDAO()
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sos.listener.ISosRequestListener#getOperationMetadata(java.lang
     * .String, java.lang.Object)
     */
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
    public Set<String> getConformanceClasses() {
        Set<String> conformanceClasses = new HashSet<String>(0);
        if (hasImplementedDAO()) {
            conformanceClasses.add("http://www.opengis.net/spec/SOS/2.0/conf/core");
        }
        return conformanceClasses;
    }

    private void checkRequestedParameters(GetObservationRequest sosRequest) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
        try {
            SosHelper.checkServiceParameter(sosRequest.getService());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            OwsHelper.checkSingleVersionParameter(sosRequest.getVersion(), Configurator.getInstance()
                    .getSupportedVersions());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }

        // check if parameters are set, if not throw ResponseExceedsSizeLimit
        // exception
        checkQueryParametersIfAllEmpty(sosRequest);

        try {
            checkOfferingId(sosRequest.getOfferings());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            checkObservedProperties(sosRequest.getObservedProperties());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkProcedureIDs(sosRequest.getProcedures(), Configurator.getInstance()
                    .getCapabilitiesCacheController().getProcedures(),
                    SosConstants.GetObservationParams.procedure.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkFeatureOfInterest(sosRequest.getFeatureIdentifiers(), Configurator.getInstance()
                    .getCapabilitiesCacheController().getFeatureOfInterest(),
                    SosConstants.GetObservationParams.featureOfInterest.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkSpatialFilter(sosRequest.getSpatialFilter(),
                    SosConstants.GetObservationParams.featureOfInterest.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        try {
            SosHelper.checkTemporalFilter(sosRequest.getEventTimes(),
                    Sos2Constants.GetObservationParams.temporalFilter.name());
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }
        
        
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
    }

    /**
     * checks if mandatory parameter observed property is correct
     * 
     * @param properties
     *            String[] containing the observed properties of the request
     * @param cacheController
     * @param strings
     * @param offering
     *            the requested offeringID
     * @throws OwsExceptionReport
     *             if the parameter does not containing any matching
     *             observedProperty for the requested offering
     */
    private void checkObservedProperties(List<String> observedProperties) throws OwsExceptionReport {
        if (observedProperties != null) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            Collection<String> validObservedProperties =
                    Configurator.getInstance().getCapabilitiesCacheController().getObservableProperties();
            for (String obsProp : observedProperties) {
                if (!validObservedProperties.contains(obsProp)) {
                    String exceptionText =
                            "The value (" + obsProp + ") of the parameter '"
                                    + SosConstants.GetObservationParams.observedProperty.toString() + "' is invalid";
                    LOGGER.error(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.GetObservationParams.observedProperty.name(), exceptionText));
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    /**
     * checks if the passed offeringId is supported
     * 
     * @param strings
     * 
     * @param offeringId
     *            the offeringId to be checked
     * @throws OwsExceptionReport
     *             if the passed offeringId is not supported
     */
    private void checkOfferingId(List<String> offeringIds) throws OwsExceptionReport {
        if (offeringIds != null) {
            Collection<String> offerings = Configurator.getInstance().getCapabilitiesCacheController().getOfferings();
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (String offeringId : offeringIds) {
                if (offeringId.contains(SosConstants.SEPARATOR_4_OFFERINGS)) {
                    String[] offArray = offeringId.split(SosConstants.SEPARATOR_4_OFFERINGS);
                    if (!offerings.contains(offArray[0])
                            || !Configurator.getInstance().getCapabilitiesCacheController()
                                    .getProcedures4Offering(offArray[0]).contains(offArray[1])) {
                        String exceptionText =
                                "The value (" + offeringId + ") of the parameter '"
                                        + SosConstants.GetObservationParams.offering.toString() + "' is invalid";
                        LOGGER.error(exceptionText);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.GetObservationParams.offering.name(), exceptionText));
                    }

                } else {
                    if (!offerings.contains(offeringId)) {
                        String exceptionText =
                                "The value (" + offeringId + ") of the parameter '"
                                        + SosConstants.GetObservationParams.offering.toString() + "' is invalid";
                        LOGGER.error(exceptionText);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.GetObservationParams.offering.name(), exceptionText));
                    }
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    private boolean checkForObservationAndMeasurementV20Type(String responseFormat) throws OwsExceptionReport {
        IEncoder encoder = Configurator.getInstance().getEncoder(responseFormat);
        if (encoder != null && encoder instanceof IObservationEncoder) {
            return ((IObservationEncoder) encoder).isObservationAndMeasurmentV20Type();
        }
        return false;
    }

    private boolean checkResponseFormat(GetObservationRequest request) throws OwsExceptionReport {
        boolean zipCompression = false;
        if (request.getResponseFormat() == null
                || (request.getResponseFormat() != null && request.getResponseFormat().isEmpty())) {
            request.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
        } else {
            zipCompression = SosHelper.checkResponseFormatForZipCompression(request.getResponseFormat());
            if (zipCompression) {
                request.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
            } else {
                Collection<String> supportedResponseFormats =
                        SosHelper.getSupportedResponseFormats(request.getService(), request.getVersion());
                if (!supportedResponseFormats.contains(request.getResponseFormat())) {
                    StringBuilder exceptionText = new StringBuilder();
                    exceptionText.append("The requested responseFormat (");
                    exceptionText.append(request.getResponseFormat());
                    exceptionText.append(") is not supported by this server!");
                    LOGGER.debug(exceptionText.toString());
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.GetObservationParams.responseFormat.name(), exceptionText.toString());
                }
            }
        }
        return zipCompression;
    }

    private void checkQueryParametersIfAllEmpty(GetObservationRequest request) throws OwsExceptionReport {
        if (!request.isSetOffering() && !request.isSetObservableProperty() && !request.isSetProcedure()
                && !request.isSetFeatureOfInterest() && !request.isSetTemporalFilter() && !request.isSetSpatialFilter()) {
            String exceptionText = "The response exceeds the size limit! Please define some filtering parameters.";
            throw Util4Exceptions.createResponseExceedsSizeLimitException(exceptionText);
        }

    }
}
