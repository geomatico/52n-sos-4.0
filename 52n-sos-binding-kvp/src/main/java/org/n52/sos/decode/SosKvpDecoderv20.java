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
package org.n52.sos.decode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.n52.sos.ogc.filter.FilterConstants.SpatialOperator;
import org.n52.sos.ogc.filter.FilterConstants.TimeOperator;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.gml.time.TimePeriod;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.request.AbstractServiceRequest;
import org.n52.sos.request.DescribeSensorRequest;
import org.n52.sos.request.GetCapabilitiesRequest;
import org.n52.sos.request.GetFeatureOfInterestRequest;
import org.n52.sos.request.GetObservationRequest;
import org.n52.sos.request.GetResultRequest;
import org.n52.sos.request.GetResultTemplateRequest;
import org.n52.sos.request.SosGetResultRequest;
import org.n52.sos.request.SosGetResultTemplateRequest;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.DateTimeException;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.JTSHelper;
import org.n52.sos.util.KvpHelper;
import org.n52.sos.util.SosHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class SosKvpDecoderv20 implements IKvpDecoder {

    /**
     * logger, used for logging while initialising the constants from
     * configuration file
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosKvpDecoderv20.class);

    private List<DecoderKeyType> decoderKeyTypes;

    public SosKvpDecoderv20() {
        decoderKeyTypes = new ArrayList<DecoderKeyType>();
        DecoderKeyType serviceVersionDKT = new DecoderKeyType(SosConstants.SOS, Sos2Constants.SERVICEVERSION);
        decoderKeyTypes.add(serviceVersionDKT);
        DecoderKeyType serviceDKT = new DecoderKeyType(SosConstants.SOS, null);
        decoderKeyTypes.add(serviceDKT);
        DecoderKeyType namespaceSosDKT = new DecoderKeyType(Sos2Constants.NS_SOS_20);
        decoderKeyTypes.add(namespaceSosDKT);
        DecoderKeyType namespaceSwesDKT = new DecoderKeyType(SWEConstants.NS_SWES_20);
        decoderKeyTypes.add(namespaceSwesDKT);
        StringBuilder logMsgBuilder = new StringBuilder();
        logMsgBuilder.append("Decoder for the following namespaces initialized successfully: ");
        for (DecoderKeyType decoderKeyType : decoderKeyTypes) {
            logMsgBuilder.append(decoderKeyType.toString());
            logMsgBuilder.append(", ");
        }
        logMsgBuilder.delete(logMsgBuilder.lastIndexOf(", "), logMsgBuilder.length());
        logMsgBuilder.append("!");
        LOGGER.info(logMsgBuilder.toString());
    }

    @Override
    public List<DecoderKeyType> getDecoderKeyTypes() {
        return decoderKeyTypes;
    }

    @Override
    public AbstractServiceRequest decode(Map<String, String> element) throws OwsExceptionReport {
        AbstractServiceRequest sosRequest = null;
        String requestParameterValue = null;

        for (String key : element.keySet()) {
            if (key.equalsIgnoreCase(RequestParams.request.name())) {
                requestParameterValue = KvpHelper.checkParameterSingleValue(element.get(key), key);
                break;
            }
        }
        if (requestParameterValue != null) {
            if (requestParameterValue.equalsIgnoreCase(SosConstants.Operations.GetCapabilities.name())) {
                sosRequest = parseGetCapabilities(element);
            } else if (requestParameterValue.equalsIgnoreCase(SosConstants.Operations.DescribeSensor.name())) {
                sosRequest = parseDescribeSensor(element);
            } else if (requestParameterValue.equalsIgnoreCase(SosConstants.Operations.GetObservation.name())) {
                sosRequest = parseGetObservation(element);
            } else if (requestParameterValue.equalsIgnoreCase(SosConstants.Operations.GetFeatureOfInterest.name())) {
                sosRequest = parseGetFeatureOfInterest(element);
            } else if (requestParameterValue.equalsIgnoreCase(Sos2Constants.Operations.GetResultTemplate.name())) {
                sosRequest = parseGetResultTemplate(element);
            } else if (requestParameterValue.equalsIgnoreCase(SosConstants.Operations.GetResult.name())) {
                sosRequest = parseGetResult(element);
            } else {
                throw Util4Exceptions.createOperationNotSupportedException(RequestParams.request.name());
            }

        }
        return sosRequest;
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return new HashMap<SupportedTypeKey, Set<String>>(0);
    }

    /**
     * parses the String representing the getCapabilities request and creates a
     * SosGetCapabilities request
     * 
     * @param element
     *            String with getCapabilities parameters
     * @return Returns SosGetCapabilitiesRequest representing the request
     * @throws OwsExceptionReport
     *             If parsing the String failed
     */
    private GetCapabilitiesRequest parseGetCapabilities(Map<String, String> element) throws OwsExceptionReport {

        GetCapabilitiesRequest request = new GetCapabilitiesRequest();
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory SOS 1.0.0, SOS 2.0 default)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, parameterName);
                }
                // acceptVersions (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.AcceptVersions.name())) {
                    if (!parameterValues.isEmpty()) {
                        request.setAcceptVersions(parameterValues.split(","));
                    } else {
                        OwsExceptionReport owse = new OwsExceptionReport();
                        owse.addCodedException(OwsExceptionCode.InvalidParameterValue,
                                SosConstants.GetCapabilitiesParams.AcceptVersions.name(), "The value of parameter "
                                        + parameterName + " (" + parameterValues + ") is invalid.");
                        throw owse;
                    }
                }
                // acceptFormats (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.AcceptFormats.name())) {
                    request.setAcceptFormats(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }
                // updateSequence (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.updateSequence.name())) {
                    request.setUpdateSequence(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));

                }
                // sections (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetCapabilitiesParams.Sections.name())) {
                    request.setSections(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                } else {
                    String exceptionText =
                            "The parameter '" + parameterName + "' is invalid for the GetCapabilities request!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;

    }

    /**
     * parses the HttpServletRequest representing the describeSensor request and
     * creates a SosDescribeSensor request
     * 
     * @param request
     *            HttpServletRequest, which contains the request parameters
     * @return SosDescribeSensorRequest
     * @throws OwsExceptionReport
     *             if parsing of request fails
     */
    private DescribeSensorRequest parseDescribeSensor(Map<String, String> element) throws OwsExceptionReport {

        DescribeSensorRequest request = new DescribeSensorRequest();
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();

        boolean foundProcedure = false;
        boolean foundProcedureDescriptionFormat = false;
        boolean foundService = false;
        boolean foundVersion = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.service.name()));
                    foundService = true;
                }
                // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.version.name()));
                    foundVersion = true;
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, OWSConstants.RequestParams.request.name());
                }
                // procedure
                else if (parameterName.equalsIgnoreCase(SosConstants.DescribeSensorParams.procedure.name())) {
                    request.setProcedures(KvpHelper.checkParameterSingleValue(parameterValues,
                            SosConstants.DescribeSensorParams.procedure.name()));
                    foundProcedure = true;
                }
                // procedureDescriptionFormat
                else if (parameterName.equalsIgnoreCase(Sos2Constants.DescribeSensorParams.procedureDescriptionFormat
                        .name())) {
                    request.setOutputFormat(KvpHelper.checkParameterSingleValue(parameterValues,
                            Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name()));
                    foundProcedureDescriptionFormat = true;
                }
                // valid time (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.DescribeSensorParams.validTime.name())) {
                    try {
                        request.setTime(parseValidTime(parameterValues,
                                Sos2Constants.DescribeSensorParams.validTime.name()));
                    } catch (DecoderException e) {
                        String exceptionText =
                                "The optional parameter '" + Sos2Constants.DescribeSensorParams.validTime.name()
                                        + "' is not supported by this service!";
                        LOGGER.debug(exceptionText, e);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                Sos2Constants.DescribeSensorParams.validTime.name(), exceptionText));
                    } catch (DateTimeException e) {
                        String exceptionText =
                                "The optional parameter '" + Sos2Constants.DescribeSensorParams.validTime.name()
                                        + "' is not supported by this service!";
                        LOGGER.debug(exceptionText, e);
                        exceptions.add(Util4Exceptions.createInvalidParameterValueException(
                                Sos2Constants.DescribeSensorParams.validTime.name(), exceptionText));
                    }
                } else {
                    String exceptionText =
                            "The optional parameter '" + parameterName + "' is not supported by this service!";
                    LOGGER.debug(exceptionText);
                    exceptions.add(Util4Exceptions.createOptionNotSupportedException(parameterName, exceptionText));
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundProcedure) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue,
                    SosConstants.DescribeSensorParams.procedure.name(),
                    "Your request was invalid! The parameter PROCEDURE must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundProcedureDescriptionFormat) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue,
                    Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name(),
                    "Your request was invalid! The parameter "
                            + Sos2Constants.DescribeSensorParams.procedureDescriptionFormat.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundService) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.service.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.service.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundVersion) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.version.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.version.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;
    }

    /**
     * parses the HttpServletRequest representing the getObservation request and
     * creates a SosGetObservation request
     * 
     * @param request
     *            HttpServletRequest, which contains the request parameters
     * @return SosGetObservationRequest
     * @throws OwsExceptionReport
     *             if parsing of request fails
     */
    private GetObservationRequest parseGetObservation(Map<String, String> element) throws OwsExceptionReport {

        GetObservationRequest request = new GetObservationRequest();
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
        boolean foundService = false;
        boolean foundVersion = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.service.name()));
                    foundService = true;
                }

                // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.version.name()));
                    foundVersion = true;
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, OWSConstants.RequestParams.request.name());
                }

                // offering (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.offering.name())) {
                    request.setOfferings(KvpHelper.checkParameterMultipleValues(parameterValues,
                            SosConstants.GetObservationParams.offering.name()));
                }

                // observedProperty (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.observedProperty.name())) {
                    request.setObservedProperties(KvpHelper.checkParameterMultipleValues(parameterValues,
                            SosConstants.GetObservationParams.observedProperty.name()));
                }

                // procedure (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.procedure.name())) {
                    request.setProcedures(KvpHelper.checkParameterMultipleValues(parameterValues,
                            SosConstants.GetObservationParams.procedure.name()));
                }

                // featureOfInterest (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.featureOfInterest.name())) {
                    request.setFeatureIdentifiers(KvpHelper.checkParameterMultipleValues(parameterValues,
                            SosConstants.GetObservationParams.featureOfInterest.name()));
                }

                // eventTime (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.temporalFilter.name())) {
                    try {
                        request.setEventTimes(parseTemporalFilter(KvpHelper.checkParameterMultipleValues(
                                parameterValues, Sos2Constants.GetObservationParams.temporalFilter.name()),
                                parameterName));
                    } catch (DecoderException e) {
                        OwsExceptionReport owse = new OwsExceptionReport();
                        owse.addCodedException(OwsExceptionCode.InvalidParameterValue,
                                Sos2Constants.GetObservationParams.temporalFilter.name(), "The value of parameter "
                                        + Sos2Constants.GetObservationParams.temporalFilter.name() + " is invalid.");
                        throw owse;
                    } catch (DateTimeException e) {
                        OwsExceptionReport owse = new OwsExceptionReport();
                        owse.addCodedException(OwsExceptionCode.InvalidParameterValue,
                                Sos2Constants.GetObservationParams.temporalFilter.name(), "The value of parameter "
                                        + Sos2Constants.GetObservationParams.temporalFilter.name() + " is invalid.");
                        throw owse;
                    }
                }

                // spatialFilter (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.spatialFilter.name())) {
                    request.setSpatialFilter(parseSpatialFilter(
                            KvpHelper.checkParameterMultipleValues(parameterValues, parameterName), parameterName));
                }

                // responseFormat (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.responseFormat.name())) {
                    request.setResponseFormat(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                }
                // namespaces (conditional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.namespaces.name())) {
                    request.setNamespaces(parseNamespaces(parameterValues));
                } else {
                    String exceptionText =
                            "The parameter '" + parameterName + "' is invalid for the GetObservation request!";
                    LOGGER.debug(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }

            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundService) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.service.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.service.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundVersion) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.version.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.version.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;
    }

    /**
     * parses the HttpServletRequest representing the getFeatureOfInterest
     * request and creates a SosFeatureOfInterest request
     * 
     * @param request
     *            HttpServletRequest, which contains the request parameters
     * @return SosGetFeatureOfInterestRequest
     * @throws OwsExceptionReport
     *             if parsing of request fails
     */
    private GetFeatureOfInterestRequest parseGetFeatureOfInterest(Map<String, String> element)
            throws OwsExceptionReport {

        GetFeatureOfInterestRequest request = new GetFeatureOfInterestRequest();
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();

        boolean foundService = false;
        boolean foundVersion = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.service.name()));
                    foundService = true;
                }

                // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.version.name()));
                    foundVersion = true;
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, OWSConstants.RequestParams.request.name());
                }
                // observedProperty (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetFeatureOfInterestParams.observedProperty
                        .name())) {
                    request.setObservedProperties(KvpHelper.checkParameterMultipleValues(parameterValues,
                            Sos2Constants.GetFeatureOfInterestParams.observedProperty.name()));
                }

                // procedure (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetFeatureOfInterestParams.procedure.name())) {
                    request.setProcedures(KvpHelper.checkParameterMultipleValues(parameterValues,
                            Sos2Constants.GetFeatureOfInterestParams.procedure.name()));
                }

                // featureOfInterest (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetFeatureOfInterestParams.featureOfInterest
                        .name())) {
                    request.setFeatureIdentifiers(KvpHelper.checkParameterMultipleValues(parameterValues,
                            Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name()));
                }

                // spatialFilter (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name())) {
                    List<SpatialFilter> spatialFilters = new ArrayList<SpatialFilter>();
                    spatialFilters.add(parseSpatialFilter(KvpHelper.checkParameterMultipleValues(parameterValues,
                            Sos2Constants.GetFeatureOfInterestParams.spatialFilter.name()), parameterName));
                    request.setSpatialFilters(spatialFilters);
                }
                // namespaces (conditional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.namespaces.name())) {
                    request.setNamespaces(parseNamespaces(parameterValues));
                } else {
                    String exceptionText =
                            "The parameter '" + parameterName + "' is invalid for the GetFeatureOfInterest request!";
                    LOGGER.debug(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }

        }

        if (!foundService) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.service.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.service.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundVersion) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.version.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.version.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;
    }

    private GetResultTemplateRequest parseGetResultTemplate(Map<String, String> element) throws OwsExceptionReport {
        GetResultTemplateRequest request = new GetResultTemplateRequest();
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();

        boolean foundService = false;
        boolean foundVersion = false;
        boolean foundOffering = false;
        boolean foundObservedProperty = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.service.name()));
                    foundService = true;
                }

                // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues,
                            OWSConstants.RequestParams.version.name()));
                    foundVersion = true;
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, OWSConstants.RequestParams.request.name());
                }

                // offering (mandatory)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetResultTemplateParams.offering.name())) {
                    request.setOffering(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundOffering = true;
                }

                // observedProperty (mandatory)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetResultTemplateParams.observedProperty.name())) {
                    request.setObservedProperty(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundObservedProperty = true;
                } else {
                    String exceptionText =
                            "The parameter '" + parameterName + "' is invalid for the GetResultTemplate request!";
                    LOGGER.debug(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundService) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.service.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.service.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundVersion) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.version.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.version.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundOffering) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue,
                    Sos2Constants.GetResultTemplateParams.offering.name(),
                    "Your request was invalid! The mandatory parameter "
                            + Sos2Constants.GetResultTemplateParams.offering.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundObservedProperty) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue,
                    Sos2Constants.GetResultTemplateParams.observedProperty.name(),
                    "Your request was invalid! The mandatory parameter "
                            + Sos2Constants.GetResultTemplateParams.observedProperty.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);

        return request;
    }

    private GetResultRequest parseGetResult(Map<String, String> element) throws OwsExceptionReport {
        GetResultRequest request = new GetResultRequest();
        List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();

        boolean foundService = false;
        boolean foundVersion = false;
        boolean foundOffering = false;
        boolean foundObservedProperty = false;

        for (String parameterName : element.keySet()) {
            String parameterValues = element.get(parameterName);
            try {
                // service (mandatory)
                if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.service.name())) {
                    request.setService(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundService = true;
                }
                // version (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.version.name())) {
                    request.setVersion(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundVersion = true;
                }
                // request (mandatory)
                else if (parameterName.equalsIgnoreCase(OWSConstants.RequestParams.request.name())) {
                    KvpHelper.checkParameterSingleValue(parameterValues, parameterName);
                }
                // offering (mandatory)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetResultTemplateParams.offering.name())) {
                    request.setOffering(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundOffering = true;
                }

                // observedProperty (mandatory)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetResultTemplateParams.observedProperty.name())) {
                    request.setObservedProperty(KvpHelper.checkParameterSingleValue(parameterValues, parameterName));
                    foundObservedProperty = true;
                }
                
                // featureOfInterest (optional)
                else if (parameterName.equalsIgnoreCase(SosConstants.GetObservationParams.featureOfInterest.name())) {
                    request.setFeatureIdentifiers(KvpHelper.checkParameterMultipleValues(parameterValues, parameterName));
                }

                // eventTime (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.temporalFilter.name())) {
                    try {
                        request.setTemporalFilter(parseTemporalFilter(KvpHelper.checkParameterMultipleValues(
                                parameterValues, Sos2Constants.GetObservationParams.temporalFilter.name()),
                                parameterName));
                    } catch (DecoderException e) {
                        throw Util4Exceptions.createInvalidParameterValueException(
                                Sos2Constants.GetObservationParams.temporalFilter.name(),
                                "The parameter value is not valid!");
                    } catch (DateTimeException e) {
                        throw Util4Exceptions.createInvalidParameterValueException(
                                Sos2Constants.GetObservationParams.temporalFilter.name(),
                                "The parameter value is not valid!");
                    }

                }

                // spatialFilter (optional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.spatialFilter.name())) {
                    request.setSpatialFilter(parseSpatialFilter(KvpHelper.checkParameterMultipleValues(
                            parameterValues, Sos2Constants.GetObservationParams.spatialFilter.name()), parameterName));
                }
                // xmlWrapper (default = false) (optional)
                // namespaces (conditional)
                else if (parameterName.equalsIgnoreCase(Sos2Constants.GetObservationParams.namespaces.name())) {
                    request.setNamespaces(parseNamespaces(parameterValues));
                } else {
                    String exceptionText =
                            "The parameter '" + parameterName + "' is invalid for the GetResult request!";
                    LOGGER.debug(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }
            } catch (OwsExceptionReport owse) {
                exceptions.add(owse);
            }
        }

        if (!foundService) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.service.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.service.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundVersion) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue, OWSConstants.RequestParams.version.name(),
                    "Your request was invalid! The mandatory parameter " + OWSConstants.RequestParams.version.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundOffering) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(
                    OwsExceptionCode.MissingParameterValue,
                    Sos2Constants.GetResultParams.offering.name(),
                    "Your request was invalid! The mandatory parameter "
                            + Sos2Constants.GetResultParams.offering.name() + " must be contained in your request!");
            exceptions.add(owse);
        }

        if (!foundObservedProperty) {
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.MissingParameterValue,
                    Sos2Constants.GetResultParams.observedProperty.name(),
                    "Your request was invalid! The mandatory parameter "
                            + Sos2Constants.GetResultParams.observedProperty.name()
                            + " must be contained in your request!");
            exceptions.add(owse);
        }
        Util4Exceptions.mergeAndThrowExceptions(exceptions);
        return request;
    }

    private List<TemporalFilter> parseValidTime(String parameterValue, String parameterName) throws DecoderException,
            DateTimeException {
        List<TemporalFilter> filterList = new ArrayList<TemporalFilter>();
        filterList.add(createTemporalFilterFromValue(parameterValue, null));
        return filterList;
    }

    private SpatialFilter parseSpatialFilter(List<String> parameterValues, String parameterName)
            throws OwsExceptionReport {
        if (!parameterValues.isEmpty()) {
            SpatialFilter spatialFilter = new SpatialFilter();

            boolean hasSrid = false;

            if (parameterValues.get(0).contains(":")) {
                spatialFilter.setValueReference(parameterValues.get(0));
            }

            int srid = 4326;
            if (parameterValues.get(parameterValues.size() - 1).startsWith(
                    Configurator.getInstance().getSrsNamePrefixSosV2())) {
                hasSrid = true;
                srid =
                        SosHelper.parseSrsName(parameterValues.get(parameterValues.size() - 1), Configurator
                                .getInstance().getSrsNamePrefixSosV2());
            } else if (parameterValues.get(parameterValues.size() - 1).startsWith(
                    Configurator.getInstance().getSrsNamePrefix())) {
                hasSrid = true;
                srid =
                        SosHelper.parseSrsName(parameterValues.get(parameterValues.size() - 1), Configurator
                                .getInstance().getSrsNamePrefix());
            }

            List<String> coordinates;
            if (hasSrid) {
                coordinates = parameterValues.subList(1, parameterValues.size() - 1);
            } else {
                coordinates = parameterValues.subList(1, parameterValues.size());
            }

            if (coordinates.size() != 4) {
                throw Util4Exceptions.createInvalidParameterValueException(parameterName,
                        "The parameter value is not valid!");
            }
            String lowerCorner;
            String upperCorner;

            if (Configurator.getInstance().switchCoordinatesForEPSG(srid)) {
                lowerCorner = coordinates.get(1) + " " + coordinates.get(0);
                upperCorner = coordinates.get(3) + " " + coordinates.get(2);
            } else {
                lowerCorner = coordinates.get(0) + " " + coordinates.get(1);
                upperCorner = coordinates.get(2) + " " + coordinates.get(3);
            }
            Geometry geom =
                    JTSHelper.createGeometryFromWKT(JTSHelper.createWKTPolygonFromEnvelope(lowerCorner, upperCorner));
            geom.setSRID(srid);
            spatialFilter.setGeometry(geom);
            spatialFilter.setOperator(SpatialOperator.BBOX);
            return spatialFilter;
        }
        return null;
    }

    private List<TemporalFilter> parseTemporalFilter(List<String> parameterValues, String parameterName)
            throws DecoderException, DateTimeException {
        List<TemporalFilter> filterList = new ArrayList<TemporalFilter>();
        if (parameterValues.size() != 2) {
            throw new DecoderException("The parameter value is not valid!");
            // throw
            // Util4Exceptions.createInvalidParameterValueException(parameterName,
            // "The parameter value is not valid!");
        }
        filterList.add(createTemporalFilterFromValue(parameterValues.get(1), parameterValues.get(0)));
        return filterList;
    }

    private Map<String, String> parseNamespaces(String parameterValues) {
        Map<String, String> namespaces = new HashMap<String, String>();
        List<String> array =
                Arrays.asList(parameterValues.replaceAll("\\),", "").replaceAll("\\)", "").split("xmlns\\("));
        for (String string : array) {
            if (string != null && !string.isEmpty()) {
                String[] s = string.split(",");
                namespaces.put(s[0], s[1]);
            }
        }
        return namespaces;
    }

    private TemporalFilter createTemporalFilterFromValue(String value, String valueReference) throws DecoderException,
            DateTimeException {
        TemporalFilter temporalFilter = new TemporalFilter();
        temporalFilter.setValueReference(valueReference);
        String[] times = value.split("/");

        if (times.length == 1) {
            DateTime instant = DateTimeHelper.parseIsoString2DateTime(times[0]);
            TimeInstant ti = new TimeInstant();
            ti.setValue(instant);
            String valueSplit = null;
            if (times[0].contains("Z")) {
                valueSplit = times[0].substring(0, times[0].indexOf("Z"));
            } else if (times[0].contains("+")) {
                valueSplit = times[0].substring(0, times[0].indexOf("+"));
            }
            if (valueSplit != null) {
                ti.setRequestedTimeLength(valueSplit.length());
            } else {
                ti.setRequestedTimeLength(times[0].length());
            }
            temporalFilter.setOperator(TimeOperator.TM_Equals);
            temporalFilter.setTime(ti);
        } else if (times.length == 2) {
            DateTime start = DateTimeHelper.parseIsoString2DateTime(times[0]);
            // check if end time is a full ISO 8106 string
            String valueSplit = null;
            if (times[1].contains("Z")) {
                valueSplit = times[1].substring(0, times[1].indexOf("Z"));
            } else if (times[1].contains("+")) {
                valueSplit = times[1].substring(0, times[1].indexOf("+"));
            }
            DateTime end = null;
            if (valueSplit != null) {
                end =
                        DateTimeHelper.setDateTime2EndOfDay4RequestedEndPosition(
                                DateTimeHelper.parseIsoString2DateTime(times[1]), valueSplit.length());
            } else {
                end =
                        DateTimeHelper.setDateTime2EndOfDay4RequestedEndPosition(
                                DateTimeHelper.parseIsoString2DateTime(times[1]), times[1].length());
            }
            TimePeriod tp = new TimePeriod();
            tp.setStart(start);
            tp.setEnd(end);
            temporalFilter.setOperator(TimeOperator.TM_During);
            temporalFilter.setTime(tp);

        } else {
            throw new DecoderException(String.format("The paramter value '%s' is invalid!", value));
        }
        return temporalFilter;
    }

    @Override
    public Set<String> getConformanceClasses() {
        return new HashSet<String>(0);
    }

}
