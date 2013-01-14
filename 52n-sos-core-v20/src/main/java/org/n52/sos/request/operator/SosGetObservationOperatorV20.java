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
import org.n52.sos.ds.IGetObservationDAO;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.encode.IObservationEncoder;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.ConformanceClasses;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swes.SwesExtensions;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.response.GetObservationResponse;
import org.n52.sos.response.ServiceResponse;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.CodingHelper;
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
public class SosGetObservationOperatorV20 extends AbstractV2RequestOperator<IGetObservationDAO, GetObservationRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SosGetObservationOperatorV20.class.getName());
    private static final String OPERATION_NAME = SosConstants.Operations.GetObservation.name();
    private static final Set<String> CONFORMANCE_CLASSES = Collections.singleton(ConformanceClasses.SOS_V2_CORE_PROFILE);

    public SosGetObservationOperatorV20() {
        super(OPERATION_NAME, GetObservationRequest.class);
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.unmodifiableSet(CONFORMANCE_CLASSES);
    }

    @Override
    public ServiceResponse receive(GetObservationRequest sosRequest) throws OwsExceptionReport {
        checkRequestedParameters(sosRequest);
        boolean zipCompression = checkResponseFormat(sosRequest);
        String responseFormat = sosRequest.getResponseFormat();
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
            if (sosRequest.getVersion().equals(Sos2Constants.SERVICEVERSION)
                    && checkForObservationAndMeasurementV20Type(responseFormat)) {
                namespace = Sos2Constants.NS_SOS_20;
            }
            
            GetObservationResponse response = getDao().getObservation(sosRequest);
            if (responseFormat.equals(OMConstants.RESPONSE_FORMAT_OM_2)) {
                if (!isSubsettingExtensionSet(sosRequest.getExtensions())) {
                    response.mergeObservationsWithSameAntiSubsettingIdentifier();
                } else {
                    response.mergeObservationsWithSameX();
                }
            }

            IEncoder<XmlObject, GetObservationResponse> encoder;
            try {
                encoder = CodingHelper.getEncoder(namespace, response);
            }catch (OwsExceptionReport e) {
                String exceptionText = String.format("The value '%s' of the responseFormat parameter is not supported by this server!", responseFormat);
                LOGGER.error(exceptionText, e);
                throw Util4Exceptions.createInvalidParameterValueException(SosConstants.GetObservationParams.responseFormat.name(), exceptionText);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XmlObject encodedObject = encoder.encode(response);
            String contentType = encoder.getContentType();
            encodedObject.save(baos, XmlOptionsHelper.getInstance().getXmlOptions());
            return new ServiceResponse(baos, contentType, zipCompression, true);
        } catch (IOException ioe) {
            String exceptionText = "Error occurs while saving response to output stream!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        }

    }

    private void checkRequestedParameters(GetObservationRequest sosRequest) throws OwsExceptionReport {
        List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
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
            SosHelper.checkFeatureOfInterestIdentifiers(sosRequest.getFeatureIdentifiers(), Configurator.getInstance()
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
            
            if (sosRequest.isSetTemporalFilter()) {
                SosHelper.checkTemporalFilter(sosRequest.getTemporalFilters(),
                        Sos2Constants.GetObservationParams.temporalFilter.name());
//            } else {
//                // TODO check this for pofile
//                List<TemporalFilter> filters = new ArrayList<TemporalFilter>();
//                TemporalFilter filter = new TemporalFilter();
//                filter.setOperator(TimeOperator.TM_Equals);
//                filter.setValueReference("phenomenonTime");
//                TimeInstant timeInstant = new TimeInstant();
//                timeInstant.setIndeterminateValue(SosConstants.FirstLatest.latest.name());
//                filter.setTime(timeInstant);
//                filters.add(filter);
//                sosRequest.setTemporalFilters(filters);
            }
        } catch (OwsExceptionReport owse) {
            exceptions.add(owse);
        }

        Util4Exceptions.mergeAndThrowExceptions(exceptions);
        
        // check if parameters are set, if not throw ResponseExceedsSizeLimit
        // exception
        if (sosRequest.isEmpty()) {
            String exceptionText = "The response exceeds the size limit! Please define some filtering parameters.";
            throw Util4Exceptions.createResponseExceedsSizeLimitException(exceptionText);
        }
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
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            Collection<String> validObservedProperties =
                    Configurator.getInstance().getCapabilitiesCacheController().getObservableProperties();
            for (String obsProp : observedProperties) {
                if (obsProp.isEmpty()) {
                    exceptions.add(Util4Exceptions
                            .createMissingParameterValueException(SosConstants.GetObservationParams.observedProperty
                                    .name()));
                } else {
                    if (!validObservedProperties.contains(obsProp)) {
                        String exceptionText = String.format("The value (%s) of the parameter '%s' is invalid", 
                                obsProp, SosConstants.GetObservationParams.observedProperty.toString());
                        LOGGER.error(exceptionText);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.GetObservationParams.observedProperty.name(), exceptionText));
                    }
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
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String offeringId : offeringIds) {
                if (offeringId == null || (offeringId != null && offeringId.isEmpty())) {
                    exceptions.add(Util4Exceptions
                            .createMissingParameterValueException(SosConstants.GetObservationParams.offering.name()));
                }
                if (offeringId.contains(SosConstants.SEPARATOR_4_OFFERINGS)) {
                    String[] offArray = offeringId.split(SosConstants.SEPARATOR_4_OFFERINGS);
                    if (!offerings.contains(offArray[0])
                            || !Configurator.getInstance().getCapabilitiesCacheController()
                                    .getProcedures4Offering(offArray[0]).contains(offArray[1])) {
                        String exceptionText = String.format("The value (%s) of the parameter '%s' is invalid", 
                                offeringId, SosConstants.GetObservationParams.offering.toString());
                        LOGGER.error(exceptionText);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                SosConstants.GetObservationParams.offering.name(), exceptionText));
                    }

                } else {
                    if (!offerings.contains(offeringId)) {
                        String exceptionText = String.format(
                                "The value (%s) of the parameter '%s' is invalid", offeringId, 
                                SosConstants.GetObservationParams.offering.toString());
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
        IEncoder<XmlObject, SosObservation> encoder = CodingHelper.getEncoder(responseFormat, new SosObservation());
        if (encoder != null && encoder instanceof IObservationEncoder) {
            return ((IObservationEncoder) encoder).isObservationAndMeasurmentV20Type();
        }
        return false;
    }

    private boolean checkResponseFormat(GetObservationRequest request) throws OwsExceptionReport {
        boolean zipCompression = false;
        if (request.getResponseFormat() == null) {
            request.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
        } else if (request.getResponseFormat() != null && request.getResponseFormat().isEmpty()) {
            throw Util4Exceptions
                    .createMissingParameterValueException(SosConstants.GetObservationParams.responseFormat.name());
        } else {
            zipCompression = SosHelper.checkResponseFormatForZipCompression(request.getResponseFormat());
            if (zipCompression) {
                request.setResponseFormat(OMConstants.RESPONSE_FORMAT_OM_2);
            } else {
                Collection<String> supportedResponseFormats =
                        SosHelper.getSupportedResponseFormats(request.getService(), request.getVersion());
                if (!supportedResponseFormats.contains(request.getResponseFormat())) {
                    String exceptionText = String.format("The requested responseFormat (%s) is not supported by this server!",
                            request.getResponseFormat());
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.GetObservationParams.responseFormat.name(), exceptionText);
                }
            }
        }
        return zipCompression;
    }

    private boolean isSubsettingExtensionSet(SwesExtensions extensions) {
        return extensions != null ? extensions.isBooleanExentsionSet(Sos2Constants.Extensions.Subsetting.name()) : false;
    }
}
