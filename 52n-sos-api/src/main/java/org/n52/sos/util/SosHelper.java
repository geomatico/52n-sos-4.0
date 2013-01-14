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
package org.n52.sos.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.joda.time.DateTime;
import org.n52.sos.binding.Binding;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.encode.IObservationEncoder;
import org.n52.sos.ogc.filter.SpatialFilter;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.MinMax;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.n52.sos.ogc.swe.SWEConstants.SwesExceptionCode;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.LinkedList;
import org.n52.sos.decode.DecoderKey;
import org.n52.sos.decode.OperationDecoderKey;

/**
 * Utility class for SOS
 * 
 */
public class SosHelper {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosHelper.class);

    private static final String generatedFoiPrefix = "generated_";

    /**
     * Hide utility constructor
     */
    private SosHelper() {
        super();
    }

    /**
     * Creates a HTTP-Get URL from FOI identifier and service URL for SOS
     * version
     * 
     * @param foiId
     *            FeatureOfInterst identifier
     * @param version
     *            SOS version
     * @param serviceURL
     *            Service URL
     * @return HTTP-Get request for featureOfInterst identifier
     */
    public static String createFoiGetUrl(String foiId, String version, String serviceURL, String urlPattern) {
        StringBuilder url = new StringBuilder();
        // service URL
        url.append(getFoiGetUrl(version, serviceURL, urlPattern));
        // foi-id
        url.append(foiId);
        return url.toString();
    }

    /**
     * Creates a HTTP-Get request for FeatureOfInterst without identifier
     * 
     * @param version
     *            SOS version
     * @param serviceURL
     *            Service URL
     * @return HTTP-Get request for FeatureOfInterest
     */
    public static String getFoiGetUrl(String version, String serviceURL, String urlPattern) {
        StringBuilder url = new StringBuilder();
        // service URL
        url.append(serviceURL);
        // URL pattern for KVP
        url.append(urlPattern);
        // ?
        url.append("?");
        // request
        url.append(RequestParams.request.name()).append("=")
                .append(SosConstants.Operations.GetFeatureOfInterest.name());
        // service
        url.append("&").append(OWSConstants.RequestParams.service.name()).append("=").append(SosConstants.SOS);
        // version
        url.append("&").append(OWSConstants.RequestParams.version.name()).append("=").append(version);
        // FOI identifier
        if (version.equalsIgnoreCase(Sos1Constants.SERVICEVERSION)) {
            url.append("&").append(Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID.name()).append("=");
        } else {
            url.append("&").append(Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name()).append("=");
        }

        return url.toString();
    }

    // /**
    // * Creates a SosAbstractFeature from values
    // *
    // * @param id
    // * FOI identifier
    // * @param desc
    // * FOI description
    // * @param name
    // * FOI name
    // * @param geometry
    // * FOI geometry
    // * @param srid
    // * SRID for FOI
    // * @param featureType
    // * FOI type
    // * @param schemaLink
    // * SchemaLink for type
    // * @return SOS abstract representation of FOI
    // * @throws OwsExceptionReport
    // * If FOI type is not supported or type and geometry are invalid
    // */
    // public static SosAbstractFeature getAbstractFeatureFromValues(String id,
    // String desc, String name, Geometry geometry, int srid,
    // String featureType, String schemaLink) throws OwsExceptionReport {
    //
    // SosAbstractFeature absFeat = null;
    //
    // // add new AbstractFeature to Collection
    // // TODO: implement further AbstractFeatures
    // if (featureType.equalsIgnoreCase(SFConstants.FT_SAMPLINGPOINT)
    // || featureType
    // .equalsIgnoreCase(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_POINT)) {
    // if (geometry instanceof Point) {
    // absFeat = new SosSamplingPoint(id, name, desc,
    // (Point) geometry, featureType, schemaLink);
    // } else {
    // String exceptionText = "The geometry of feature type '"
    // + featureType + "' has to be Point!";
    // LOGGER.error(exceptionText);
    // OwsExceptionReport owse = new OwsExceptionReport(
    // ExceptionLevel.DetailedExceptions);
    // owse.addCodedException(ExceptionCode.NoApplicableCode, null,
    // exceptionText);
    // throw owse;
    // }
    // } else if (featureType.equalsIgnoreCase(SFConstants.FT_SAMPLINGSURFACE)
    // || featureType
    // .equalsIgnoreCase(SFConstants.SAMPLING_FEAT_TYPE_SF_SAMPLING_SURFACE)) {
    // if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
    // absFeat = new SosSamplingSurface(id, name, desc, geometry,
    // featureType, schemaLink);
    // } else {
    // String exceptionText = "The geometry of feature type '"
    // + featureType + "' has to be Polygon!";
    // LOGGER.error(exceptionText);
    // OwsExceptionReport owse = new OwsExceptionReport(
    // ExceptionLevel.DetailedExceptions);
    // owse.addCodedException(ExceptionCode.NoApplicableCode, null,
    // exceptionText);
    // throw owse;
    // }
    // } else {
    // String exceptionText = "The feature type '" + featureType
    // + "' is not supported!";
    // LOGGER.error(exceptionText);
    // OwsExceptionReport owse = new OwsExceptionReport(
    // ExceptionLevel.DetailedExceptions);
    // owse.addCodedException(ExceptionCode.NoApplicableCode, null,
    // exceptionText);
    // throw owse;
    // }
    // return absFeat;
    // }

    /**
     * Parses the SRS name from a request and returns the SRID
     * 
     * @param srsName
     *            Requested SRS name
     * @param srsNamePrefix
     *            SRS name prefix, request version addicted
     * @return SRID
     * @throws OwsExceptionReport
     *             If an error occurs
     */
    public static int parseSrsName(String srsName) throws OwsExceptionReport {
        int srid = -1;
        if (srsName != null && !srsName.isEmpty() && !srsName.equalsIgnoreCase("NOT_SET")) {
            String urnSrsPrefix = Configurator.getInstance().getSrsNamePrefix();
            String urlSrsPrefix = Configurator.getInstance().getSrsNamePrefixSosV2();
            try {
                srid = Integer.valueOf(srsName.replace(urnSrsPrefix, "").replace(urlSrsPrefix, ""));
            } catch (NumberFormatException nfe) {
                StringBuilder builder = new StringBuilder();
                builder.append("Error while parsing srsName parameter!");
                builder.append("Parameter has to match pattern '");
                builder.append(urnSrsPrefix);
                builder.append("' or '");
                builder.append(urlSrsPrefix);
                builder.append("' with appended EPSGcode number");
                LOGGER.error(builder.toString(), nfe);
                OwsExceptionReport owse = new OwsExceptionReport();
                owse.addCodedException(OwsExceptionCode.NoApplicableCode,
                        SosConstants.GetObservationParams.srsName.name(), builder.toString(), nfe);
                throw owse;
            }
        }
        return srid;
    }

    /**
     * Checks the free memory size.
     * 
     * @throws OwsExceptionReport
     *             If no free memory size.
     */
    public static void checkFreeMemory() throws OwsExceptionReport {
        long freeMem;
        // check remaining free memory on heap if too small, throw exception to
        // avoid an OutOfMemoryError
        freeMem = Runtime.getRuntime().freeMemory();
        LOGGER.debug("Remaining Heap Size: " + (freeMem / 1024) + "KB");
        if (Runtime.getRuntime().totalMemory() == Runtime.getRuntime().maxMemory() && freeMem < 256000) { // 256000
            // accords to 256 kB create service exception
            String exceptionText = String.format(
                    "The observation response is to big for the maximal heap size of %d Byte of the virtual machine! Please either refine your getObservation request to reduce the number of observations in the response or ask the administrator of this SOS to increase the maximum heap size of the virtual machine!"
                    , Runtime.getRuntime().maxMemory());
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createResponseExceedsSizeLimitException(exceptionText);
        }
    }

    /**
     * Returns an Envelope that contains the Geometry
     * 
     * @param envelope
     *            Current envelope
     * @param geometry
     *            Geometry to include
     * @return Envelope that includes the Geometry
     */
    public static Envelope checkEnvelope(Envelope envelope, Geometry geometry) {
        Envelope checkedEnvelope = envelope;
        if (checkedEnvelope == null) {
            checkedEnvelope = geometry.getEnvelopeInternal();
        } else if (!checkedEnvelope.contains(geometry.getEnvelopeInternal())) {
            checkedEnvelope.expandToInclude(geometry.getEnvelopeInternal());
        }
        return checkedEnvelope;
    }

    /**
     * Parses the HTTP-Post body with a parameter
     * 
     * @param paramNames
     *            Parameter names
     * @param parameterMap
     *            Parameter map
     * @return Value of the parameter
     * @throws OwsExceptionReport
     *             If the parameter is not supported by this SOS.
     */
    public static String parseHttpPostBodyWithParameter(Enumeration<?> paramNames, Map<?, ?> parameterMap)
            throws OwsExceptionReport {
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if (paramName.equalsIgnoreCase(RequestParams.request.name())) {
                String[] paramValues = (String[]) parameterMap.get(paramName);
                if (paramValues.length == 1) {
                    return paramValues[0];
                } else {
                    String exceptionText = String.format(
                            "The parameter '%s' has more than one value or is empty for HTTP-Post requests by this SOS!", paramName);
                    LOGGER.error(exceptionText);
                    OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
                    throw owse;
                }
            } else {
                String exceptionText = String.format(
                        "The parameter '%s' is not supportted for HTTP-Post requests by this SOS!", paramName);
                LOGGER.error(exceptionText);
                OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
                throw owse;
            }
        }
        // FIXME: valid exception
        OwsExceptionReport se = new OwsExceptionReport();
        throw se;
    }

    /**
     * Checks if a request contains critical characters for SQL insertion, e.g.
     * '\');'
     * 
     * @param requestString
     *            Request as String
     * @throws OwsExceptionReport
     *             If the request contains critical characters
     */
    public static void checkRequestString(String requestString) throws OwsExceptionReport {
        if (requestString.contains("');")) {
            String exceptionText =
                    "Request contains critical character sequence '\');'! If ProstgreSQL database is used, critical code can be excecuted via the request!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport();
            owse.addCodedException(SwesExceptionCode.InvalidRequest, null, exceptionText);
            throw owse;
        }
    }

    /**
     * Checks if the FOI identifier was generated by SOS
     * 
     * @param featureOfInterestIdentifier
     *            FOI identifier from database
     * @param version
     *            SOS version
     * @return True if the FOI identifier was generated
     */
    public static boolean checkFeatureOfInterestIdentifierForSosV2(String featureOfInterestIdentifier, String version) {
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            if (featureOfInterestIdentifier.startsWith(generatedFoiPrefix)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the HTTP DCPs for a operation
     * 
     * @param decoderKey the decoderKey
     * @return Map with DCPs for the SOS operation
     * @throws OwsExceptionReport
     */
    public static Map<String, List<String>> getDCP(OperationDecoderKey decoderKey) throws OwsExceptionReport {
        List<String> httpGetUrls = new LinkedList<String>();
        List<String> httpPostUrls = new LinkedList<String>();
        List<String> httpPutUrls = new LinkedList<String>();
        List<String> httpDeleteUrls = new LinkedList<String>();
        String serviceURL = Configurator.getInstance().getServiceURL();
        try {
            for (Binding binding : Configurator.getInstance().getBindingOperators().values()) {
                // HTTP-Get
                if (binding.checkOperationHttpGetSupported( decoderKey)) {
                    httpGetUrls.add(serviceURL + binding.getUrlPattern() + "?");
                }
                // HTTP-Post
                if (binding.checkOperationHttpPostSupported(decoderKey)) {
                    httpPostUrls.add(serviceURL + binding.getUrlPattern());
                }
                // HTTP-PUT
                if (binding.checkOperationHttpPutSupported(decoderKey)) {
                    httpPutUrls.add(serviceURL + binding.getUrlPattern());
                }
                // HTTP-DELETE
                if (binding.checkOperationHttpDeleteSupported(decoderKey)) {
                    httpDeleteUrls.add(serviceURL + binding.getUrlPattern());
                }

            }
        } catch (Exception e) {
            if (e instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e;
            }
            // FIXME valid exception
            OwsExceptionReport owse = new OwsExceptionReport();
            // owse.addCodedException(invalidparametervalue, locator, message,
            // e);
            throw owse;
        }

        Map<String, List<String>> dcp = new HashMap<String, List<String>>(4);
        if (!httpGetUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_GET, httpGetUrls);
        }
        if (!httpPostUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_POST, httpPostUrls);
        }
        if (!httpPutUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_PUT, httpPutUrls);
        }
        if (!httpDeleteUrls.isEmpty()) {
            dcp.put(SosConstants.HTTP_DELETE, httpDeleteUrls);
        }
        return dcp;
    }

    public static String getUrlPatternForHttpGetMethod(OperationDecoderKey decoderKey) throws OwsExceptionReport {
        try {
            for (Binding binding : Configurator.getInstance().getBindingOperators().values()) {
                if (binding.checkOperationHttpGetSupported(decoderKey)) {
                    return binding.getUrlPattern();
                }
            }
        } catch (Exception e) {
            if (e instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e;
            }
            // FIXME valid exception
            OwsExceptionReport owse = new OwsExceptionReport();
            // owse.addCodedException(invalidparametervalue, locator, message,
            // e);
            throw owse;
        }
        return null;
    }

    /**
     * invert a string map, allowing for duplicate values
     * 
     * @param hierarchy
     *            map to invert
     * @return inverted mapf
     */
    public static Map<String, Collection<String>> invertHierarchy(Map<String, Collection<String>> hierarchy) {
        Map<String, Collection<String>> invertedHierarchy = new HashMap<String, Collection<String>>();
        for (String key : hierarchy.keySet()) {
            for (String value : hierarchy.get(key)) {
                if (invertedHierarchy.get(value) == null) {
                    invertedHierarchy.put(value, new HashSet<String>());
                }
                invertedHierarchy.get(value).add(key);
            }
        }
        return invertedHierarchy;
    }

    /**
     * get collection of hierarchy values for a key
     * 
     * @param hierarchy
     *            map to example
     * @param key
     *            start key
     * @param fullHierarchy 
     *            whether to traverse down the full hierarchy
     * @param includeStartKey
     *            whether to include the passed key in the result collection
     * @return collection of the full hierarchy
     */
    public static Collection<String> getHierarchy(Map<String, Collection<String>> hierarchy, String key,
            boolean fullHierarchy, boolean includeStartKey) {
        Collection<String> hierarchyValues = new HashSet<String>();
        if (includeStartKey) {
            hierarchyValues.add(key);
        }

        Stack<String> keysToCheck = new Stack<String>();
        keysToCheck.push(key);

        while (!keysToCheck.isEmpty()) {
            Collection<String> keyValues = hierarchy.get(keysToCheck.pop());
            if (keyValues != null) {
                for (String value : keyValues) {
                    if (hierarchyValues.add(value) && fullHierarchy) {
                        keysToCheck.push(value);
                    }
                }
            }
        }

        List<String> hvList = new ArrayList<String>(hierarchyValues);
        Collections.sort(hvList);
        return hvList;
    }

    /**
     * creates a HTTP-GET string for DescribeSensor.
     * 
     * @param version the version of the request
     * @param serviceURL the service url
     * @param procedureId
     *            The procedureId for the DescribeSensor request
     * 
     * @param urlPattern the url pattern (e.g. /kvp)
     * @return Get-URL as String
     * @throws UnsupportedEncodingException
     */
    public static String getDescribeSensorUrl(String version, String serviceURL, String procedureId, String urlPattern)
            throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        // service URL
        url.append(serviceURL);
        // URL pattern
        url.append(urlPattern);
        // ?
        url.append("?");
        // request
        url.append(RequestParams.request.name()).append("=").append(SosConstants.Operations.DescribeSensor.name());
        // service
        url.append("&").append(OWSConstants.RequestParams.service.name()).append("=").append(SosConstants.SOS);
        // version
        url.append("&").append(OWSConstants.RequestParams.version.name()).append("=").append(version);
        // procedure
        url.append("&").append(SosConstants.DescribeSensorParams.procedure.name()).append("=").append(procedureId);
        // outputFormat
        if (version.equalsIgnoreCase(Sos1Constants.SERVICEVERSION)) {
            url.append("&").append(Sos1Constants.DescribeSensorParams.outputFormat).append("=")
                    .append(URLEncoder.encode(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE, "UTF-8"));
        } else {
            url.append("&").append(Sos2Constants.DescribeSensorParams.procedureDescriptionFormat).append("=")
                    .append(URLEncoder.encode(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL, "UTF-8"));
        }

        return url.toString();
    }

    /**
     * Checks if the version value is 2.0.0
     * 
     * @param version
     *            requested version
     * @param validVersions valid versions
     * @throws OwsExceptionReport
     *             If version is not 2.0.0.
     */
    public static void checkVersion(String version, Set<String> validVersions) throws OwsExceptionReport {
        if (!validVersions.contains(version)) {
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue, "version", "The requested version '"
                    + version + "' is not supported by this server'!");
            LOGGER.debug("The requested version '" + version + "' is not supported by this server'!");
            throw se;
        }
    }

    /**
     * help method to check the result format parameter. If the application/zip
     * result format is set, true is returned. If not and the value is text/xml;
     * subtype="OM" false is returned. If neither zip nor OM is set, a
     * ServiceException with InvalidParameterValue as its code is thrown.
     * 
     * @param responseFormat
     *            String containing the value of the result format parameter
     * @param version
     * @return boolean true if application/zip is the resultFormat value, false
     *         if its value is text/xml;subtype="OM"
     * @throws OwsExceptionReport
     *             if the parameter value is incorrect
     */
    public static boolean checkResponseFormat(String responseFormat, String version) throws OwsExceptionReport {
        if (OMHelper.checkOMResponseFormat(responseFormat)) {
            return false;
        } else if (responseFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_ZIP)) {
            return true;
        } else {
            String exceptionText =
                    "The value of the parameter '" + SosConstants.GetObservationParams.responseFormat.toString() + "'"
                            + "is invalid. Please check the capabilities for valid values. Delivered value was: "
                            + responseFormat;
            LOGGER.debug(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.InvalidParameterValue,
                    SosConstants.GetObservationParams.responseFormat.toString(), exceptionText);
            throw owse;
        }
    }

    /**
     * checks whether the required service parameter is correct
     * 
     * @param service
     *            service parameter of the request
     * @throws OwsExceptionReport
     *             if service parameter is incorrect
     */
    public static void checkServiceParameter(String service) throws OwsExceptionReport {

        // if service==null, throw exception with missing parameter value code
        if (service == null || service.equalsIgnoreCase("NOT_SET")) {
            OwsExceptionReport se =
                    Util4Exceptions.createMissingParameterValueException(SosConstants.GetCapabilitiesParams.service
                            .name());
            LOGGER.debug("checkServiceParameter", se);
            throw se;
        }
        // if not null, but incorrect, throw also exception
        else if (!service.equals(SosConstants.SOS)) {
            String exceptionText = String.format(
                    "The value of the mandatory parameter '%s' must be '%s'. Delivered value was: %s", 
                    SosConstants.GetCapabilitiesParams.service.toString(),  SosConstants.SOS, service);
            LOGGER.error(exceptionText);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue,
                    SosConstants.GetCapabilitiesParams.service.toString(), exceptionText);
            throw se;
        }
    }

    /**
     * help method to check the result format parameter for application/zip.
     * 
     * @param responseFormat
     *            String containing the value of the responseFormat parameter
     * @return boolean true if application/zip
     */
    public static boolean checkResponseFormatForZipCompression(String responseFormat) {
        return responseFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_ZIP);
    }

    /**
     * checks whether the value of outputFormat parameter is valid
     * 
     * @param procedureDecriptionFormat
     *            the outputFormat parameter which should be checked
     * @param parameterName the parameter name
     * @throws OwsExceptionReport
     *             if the value of the outputFormat parameter is incorrect
     */
    public static void checkProcedureDescriptionFormat(String procedureDecriptionFormat, String parameterName)
            throws OwsExceptionReport {
        if (procedureDecriptionFormat == null || procedureDecriptionFormat.isEmpty()
                || procedureDecriptionFormat.equals(SosConstants.PARAMETER_NOT_SET)) {
            String exceptionText = String.format(
                    "The value of the mandatory parameter '%s' was not found in the request or is incorrect!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
            if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                String exceptionText = String.format(
                        "The value '%s' of the %s parameter is incorrect and has to be '%s' for the requested sensor!", 
                        procedureDecriptionFormat, parameterName, SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE);
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
            } else if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
                String exceptionText = String.format(
                        "The value '%s' of the %s parameter is incorrect and has to be '%s' for the requested sensor!", 
                        procedureDecriptionFormat, parameterName, SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL);
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
            }
        }
    }

    /**
     * checks whether the requested sensor ID is valid
     * 
     * @param procedureID
     *            the sensor ID which should be checked
     * @param validProcedures the valid procedure identifiers
     * @param parameterName the parameter name
     * @throws OwsExceptionReport
     *             if the value of the sensor ID parameter is incorrect
     */
    public static void checkProcedureID(String procedureID, Collection<String> validProcedures, String parameterName)
            throws OwsExceptionReport {
        // null or an empty String
        if (procedureID == null || procedureID.isEmpty()) {
            String exceptionText = String.format(
                    "The value of the mandatory parameter '%s' was not found in the request or is incorrect!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validProcedures.contains(procedureID)) {
            String exceptionText = String.format(
                    "The value of the '%s' parameter is incorrect. Please check the capabilities response document for valid values!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    public static void checkProcedureIDs(Collection<String> procedureIDs, Collection<String> validProcedures,
            String parameterName) throws OwsExceptionReport {
        if (procedureIDs != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String procedureID : procedureIDs) {
                try {
                    checkProcedureID(procedureID, validProcedures, parameterName);
                } catch (OwsExceptionReport owse) {
                    exceptions.add(owse);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }
    
    public static void checkObservationID(String observationID, Collection<String> validObservations, String parameterName) throws OwsExceptionReport {
        if (observationID == null || observationID.isEmpty()) {
            String exceptionText = String.format("The value of the mandatory parameter '%s' was not found in the request or is incorrect!",parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validObservations.contains(observationID)) {
            String exceptionText = String.format("The value of the '%s' parameter is incorrect. Please check the capabilities response document for valid values!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }
    
    public static void checkObservationIDs(Collection<String> observationIDs, Collection<String> validObservations,
            String parameterName) throws OwsExceptionReport {
        if (observationIDs != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String procedureID : observationIDs) {
                try {
                    checkObservationID(procedureID, validObservations, parameterName);
                } catch (OwsExceptionReport owse) {
                    exceptions.add(owse);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    public static void checkFeatureOfInterestIdentifiers(List<String> featuresOfInterest,
            Collection<String> validFeatureOfInterest, String parameterName) throws OwsExceptionReport {
        if (featuresOfInterest != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String featureOfInterest : featuresOfInterest) {
                try {
                    checkFeatureOfInterstIdentifier(featureOfInterest, validFeatureOfInterest, parameterName);
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    public static void checkFeatureOfInterstIdentifier(String featureOfInterest,
            Collection<String> validFeatureOfInterest, String parameterName) throws OwsExceptionReport {
        if (featureOfInterest == null || featureOfInterest.isEmpty()) {
            String exceptionText = String.format(
                    "The value of the parameter '%s' was not found in the request or is incorrect!", parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validFeatureOfInterest.contains(featureOfInterest)) {
            String exceptionText = String.format(
                    "The value '%s' of the parameter '%s' is invalid", featureOfInterest, parameterName);
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    public static void checkObservedProperties(List<String> observedProperties,
            Collection<String> validObservedProperties, String parameterName) throws OwsExceptionReport {
        if (observedProperties != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String observedProperty : observedProperties) {
                try {
                    checkObservedProperty(observedProperty, validObservedProperties, parameterName);
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    public static void checkObservedProperty(String observedProperty, Collection<String> validObservedProperties,
            String parameterName) throws OwsExceptionReport {
        if (observedProperty == null || observedProperty.isEmpty()) {
            String exceptionText =
                    "The value of the parameter '" + parameterName + "' was not found in the request or is not set!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validObservedProperties.contains(observedProperty)) {
            String exceptionText =
                    "The value '" + observedProperty + "' of the parameter '" + parameterName + "' is invalid";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    public static void checkOfferings(Set<String> offerings, Collection<String> validOfferings, String parameterName)
            throws OwsExceptionReport {
        if (offerings != null) {
            List<OwsExceptionReport> exceptions = new LinkedList<OwsExceptionReport>();
            for (String offering : offerings) {
                try {
                    checkObservedProperty(offering, validOfferings, parameterName);
                } catch (OwsExceptionReport e) {
                    exceptions.add(e);
                }
            }
            Util4Exceptions.mergeAndThrowExceptions(exceptions);
        }
    }

    public static void checkOffering(String offering, Collection<String> validOfferings, String parameterName)
            throws OwsExceptionReport {
        if (offering == null || offering.isEmpty()) {
            String exceptionText =
                    "The value of the parameter '" + parameterName + "' was not found in the request or is not set!";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validOfferings.contains(offering)) {
            String exceptionText = "The value '" + offering + "' of the parameter '" + parameterName + "' is invalid";
            LOGGER.debug(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    public static void checkForValidRequestValues(String requestValue, String version) throws OwsExceptionReport {
        boolean validSos1 = false;
        boolean validSos2 = false;
        if (version != null && version.equals(Sos1Constants.SERVICEVERSION)) {
            validSos1 = Sos1Constants.Operations.contains(requestValue);
        } else if (version != null && version.equals(Sos2Constants.SERVICEVERSION)) {
            validSos2 = Sos2Constants.Operations.contains(requestValue);
        }
        if (!validSos1 && !validSos2) {
            String exceptionText = "The requested request value is invalid. Delivered value: " + requestValue;
            throw Util4Exceptions.createInvalidParameterValueException(OWSConstants.RequestParams.request.name(),
                    exceptionText);
        }
    }

    public static void checkSpatialFilters(List<SpatialFilter> spatialFilters, String name) throws OwsExceptionReport {
        // TODO make supported ValueReferences dynamic
        if (spatialFilters != null) {
            for (SpatialFilter spatialFilter : spatialFilters) {
                checkSpatialFilter(spatialFilter, name);
            }
        }

    }

    public static void checkSpatialFilter(SpatialFilter spatialFilter, String name) throws OwsExceptionReport {
        // TODO make supported ValueReferences dynamic
        if (spatialFilter != null) {
            if (spatialFilter.getValueReference() == null
                    || (spatialFilter.getValueReference() != null && spatialFilter.getValueReference().isEmpty())) {
                String exceptionText =
                        "The value of the parameter '" + SosConstants.Filter.ValueReference.name()
                                + "' was not found in the request or is not set!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createMissingParameterValueException(SosConstants.Filter.ValueReference.name());
            } else if (!spatialFilter.getValueReference().equals("sams:shape")
                    && !spatialFilter.getValueReference().equals(
                            "om:featureOfInterest/sams:SF_SpatialSamplingFeature/sams:shape")
                    && !spatialFilter.getValueReference().equals("om:featureOfInterest/*/sams:shape")) {
                String exceptionText =
                        "The value of the parameter '" + SosConstants.Filter.ValueReference.name() + "' is invalid!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(SosConstants.Filter.ValueReference.name(),
                        exceptionText);
            }
        }
    }

    public static void checkTemporalFilter(List<TemporalFilter> temporalFilters, String name)
            throws OwsExceptionReport {
        // TODO make supported ValueReferences dynamic
        if (temporalFilters != null) {
            for (TemporalFilter temporalFilter : temporalFilters) {
                if (temporalFilter.getValueReference() == null
                        || (temporalFilter.getValueReference() != null && temporalFilter.getValueReference().isEmpty())) {
                    String exceptionText =
                            "The value of the parameter '" + SosConstants.Filter.ValueReference.name()
                                    + "' was not found in the request or is not set!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createMissingParameterValueException(SosConstants.Filter.ValueReference
                            .name());
                } else if (!temporalFilter.getValueReference().equals("phenomenonTime")
                        && !temporalFilter.getValueReference().equals("om:phenomenonTime")
                        && !temporalFilter.getValueReference().equals("resultTime")
                        && !temporalFilter.getValueReference().equals("om:resultTime")
                        && !temporalFilter.getValueReference().equals("validTime")
                        && !temporalFilter.getValueReference().equals("om:validTime")) {
                    String exceptionText =
                            "The value of the parameter '" + SosConstants.Filter.ValueReference.name()
                                    + "' was not found in the request or is not set!";
                    LOGGER.debug(exceptionText);
                    throw Util4Exceptions.createInvalidParameterValueException(
                            SosConstants.Filter.ValueReference.name(), exceptionText);
                }
            }
        }
    }

    public static void checkResultTemplate(String resultTemplate, String parameterName) throws OwsExceptionReport {
        if (resultTemplate == null || (resultTemplate != null && resultTemplate.isEmpty())) {
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        } else if (resultTemplate != null
                && !Configurator.getInstance().getCapabilitiesCacheController().getResultTemplates()
                        .contains(resultTemplate)) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("The requested template identifier (");
            exceptionText.append(resultTemplate);
            exceptionText.append(") is not supported by this server!");
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText.toString());
        }
    }

    /**
     * Get valid FOI identifiers for SOS 2.0
     * 
     * @param featureIDs FOI identifiers to test
     * @param version
     *            SOS version
     * @return valid FOI identifiers
     */
    public static Collection<String> getFeatureIDs(Collection<String> featureIDs, String version) {
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            Collection<String> validFeatureIDs = new ArrayList<String>(featureIDs.size());
            for (String featureID : featureIDs) {
                if (checkFeatureOfInterestIdentifierForSosV2(featureID, version)) {
                    validFeatureIDs.add(featureID);
                }
            }
            return validFeatureIDs;
        }
        return featureIDs;
    }

    public static Map<MinMax, String> getMinMaxMapFromEnvelope(Envelope envelope) {
        Map<MinMax, String> map = new EnumMap<MinMax, String>(MinMax.class);
        String minValue, maxValue;
        if (Configurator.getInstance().reversedAxisOrderRequired(Configurator.getInstance().getDefaultEPSG())) {
            minValue = envelope.getMinY() + " " + envelope.getMinX();
            maxValue = envelope.getMaxY() + " " + envelope.getMaxX();
        } else {
            minValue = envelope.getMinX() + " " + envelope.getMinY();
            maxValue = envelope.getMaxX() + " " + envelope.getMaxY();
        }
        map.put(MinMax.MIN, minValue);
        map.put(MinMax.MAX, maxValue);
        return map;
    }

    /**
     * Generates a sensor id from description and current time as long.
     * 
     * @param message
     *            sensor description
     * @return generated sensor id as hex SHA-1.
     */
    public static String generateID(String message) {
        long autoGeneratredID = new DateTime().getMillis();
        String concate = message + Long.toString(autoGeneratredID);
        return bytesToHex(SosConstants.MESSAGE_DIGEST.digest(concate.getBytes()));
    }

    /**
     * Transforms byte to hex representation
     * 
     * @param b
     *            bytes
     * @return hex
     */
    private static String bytesToHex(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int j = 0; j < b.length; j++) {
            buf.append(SosConstants.HEX_DIGITS[(b[j] >> 4) & 0x0f]);
            buf.append(SosConstants.HEX_DIGITS[b[j] & 0x0f]);
        }
        return buf.toString();
    }

    public static SosObservableProperty createSosOberavablePropertyFromSosSMLIo(SosSMLIo output) {
        SosSweAbstractSimpleType<?> ioValue = output.getIoValue();
        String identifier = ioValue.getDefinition();
        String description = ioValue.getDescription();
        String unit = null;
        String valueType = SosConstants.NOT_DEFINED;
        switch (ioValue.getSimpleType()) {
        case Boolean:
            valueType = "swe:Boolean";
            break;
        case Category:
            valueType = "swe:Category";
            break;
        case Count:
            valueType = "swe:Count";
            break;
        case CountRange:
            valueType = "swe:CountRange";
            break;
        case ObservableProperty:
            valueType = "swe:ObservableProperty";
            break;
        case Quantity:
            unit = ((SosSweQuantity) ioValue).getUom();
            valueType = "swe:Quantity";
            break;
        case QuantityRange:
            valueType = "swe:QuantityRange";
            break;
        case Text:
            valueType = "swe:Text";
            break;
        case Time:
            unit = ((SosSweTime) ioValue).getUom();
            valueType = "swe:Time";
            break;
        case TimeRange:
            valueType = "swe:TimeRange";
            break;
        default:
            break;
        }
        if (unit == null || (unit != null && unit.isEmpty())) {
            unit = SosConstants.NOT_DEFINED;
        }
        return new SosObservableProperty(identifier, description, unit, valueType);
    }

    /**
     * @param toNormalize the string to normalize
     * @return a normalized String for use in a file path, i.e. all
     *         [\,/,:,*,?,",<,>,;] characters are replaced by '_'.
     */
    public static String normalize(String toNormalize) {
        // toNormalize = toNormalize.replaceAll("ä", "ae");
        // toNormalize = toNormalize.replaceAll("ö", "oe");
        // toNormalize = toNormalize.replaceAll("ü", "ue");
        // toNormalize = toNormalize.replaceAll("Ä", "AE");
        // toNormalize = toNormalize.replaceAll("Ö", "OE");
        // toNormalize = toNormalize.replaceAll("Ü", "UE");
        // toNormalize = toNormalize.replaceAll("ß", "ss");
        return toNormalize.replaceAll("[\\\\,/,:,\\*,?,\",<,>,;,#,%,=,@]", "_");
    }

    public static Collection<String> getSupportedResponseFormats(String service, String version) {
        Set<String> responseFormats = new HashSet<String>();
        for (IEncoder<?,?> iEncoder : Configurator.getInstance().getCodingRepository().getEncoders()) {
            if (iEncoder instanceof IObservationEncoder) {
                responseFormats.addAll(((IObservationEncoder) iEncoder).getSupportedResponseFormats(service, version));
            }
        }
        return responseFormats;
    }

    public static Object duplicateObject(Object objectToDuplicate) throws OwsExceptionReport {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(objectToDuplicate);
            ByteArrayInputStream byteArrayIntputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayIntputStream);
            Object duplicatedObject = objectInputStream.readObject();
            return duplicatedObject;
        } catch (IOException ioe) {
            String exceptionText = "Error while duplicating object!";
            LOGGER.error(exceptionText, ioe);
            throw Util4Exceptions.createNoApplicableCodeException(ioe, exceptionText);
        } catch (ClassNotFoundException cnfe) {
            String exceptionText = "Error while duplicating object!";
            LOGGER.error(exceptionText, cnfe);
            throw Util4Exceptions.createNoApplicableCodeException(cnfe, exceptionText);
        }
    }

    public static void checkHref(String href, String parameterName) throws OwsExceptionReport {
        if (!href.startsWith("http") && !href.startsWith("urn")) {
            String exceptionText = "The referance (href) has an invalid style!";
            throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
        }
    }

    public static String createCSVFromCodeTypeList(List<CodeType> values) {
        StringBuilder builder = new StringBuilder();
        if (values != null && !values.isEmpty()) {
            for (CodeType value : values) {
                builder.append(value.getValue());
                builder.append(',');
            }
            builder.delete(builder.lastIndexOf(","), builder.length());
        }
        return builder.toString();
    }

    public static List<CodeType> createCodeTypeListFromCSV(String csv) {
        List<CodeType> names = new ArrayList<CodeType>(0);
        if (csv != null && !csv.isEmpty()) {
            for (String name : csv.split(",")) {
                names.add(new CodeType(name));
            }
        }
        return names;
    }

    public static void checkObservationType(String observationType, String parameterName) throws OwsExceptionReport {
        Collection<String> validObservationTypes =
                Configurator.getInstance().getCapabilitiesCacheController().getObservationTypes();
        if (observationType.isEmpty()) {
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        } else {
            if (!validObservationTypes.contains(observationType)) {
                String exceptionText =
                        "The value (" + observationType + ") of the parameter '"
                                + Sos2Constants.InsertSensorParams.observationType.name() + "' is invalid";
                LOGGER.error(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
            }
        }

    }

    public static boolean hasFirstLatestTemporalFilter(List<TemporalFilter> temporalFilters) {
        for (TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() != null && temporalFilter.getTime() instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (ti.isSetIndeterminateValue()) {
                    if (FirstLatest.contains(ti.getIndeterminateValue())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static List<FirstLatest> getFirstLatestTemporalFilter(List<TemporalFilter> temporalFilters) {
        List<FirstLatest> filters = new ArrayList<FirstLatest>(0);
        for (TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() != null && temporalFilter.getTime() instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (ti.isSetIndeterminateValue()) {
                    if (FirstLatest.contains(ti.getIndeterminateValue())) {
                        filters.add(FirstLatest.getEnumForString(ti.getIndeterminateValue()));
                    }
                }
            }
        }
        return filters;
    }

    public static List<TemporalFilter> getNonFirstLatestTemporalFilter(List<TemporalFilter> temporalFilters) {
        List<TemporalFilter> filters = new ArrayList<TemporalFilter>(0);
        for (TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() != null && temporalFilter.getTime() instanceof TimeInstant) {
                TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (!ti.isSetIndeterminateValue()
                        || (ti.isSetIndeterminateValue() && !FirstLatest.contains(ti.getIndeterminateValue()))) {
                    filters.add(temporalFilter);
                }
            }
        }
        return filters;
    }
}