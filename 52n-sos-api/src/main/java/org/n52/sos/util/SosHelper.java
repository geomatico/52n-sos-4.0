/***************************************************************
 Copyright (C) 2012
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.n52.sos.binding.IBinding;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.ogc.om.OMConstants;
import org.n52.sos.ogc.om.SosObservation;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.MinMax;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.SosExceptionCode;
import org.n52.sos.ogc.swe.SWEConstants;
import org.n52.sos.ogc.swe.SWEConstants.SwesExceptionCode;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.service.operator.IServiceOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for SOS
 * 
 */
public class SosHelper {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SosHelper.class);

    /**
     * Prefix for decoder methods
     */
    private static final String HTTP_DECODER_METHODE_PREFIX = "parse";

    /**
     * Postfix for decoder methods
     */
    private static final String HTTP_DECODER_METHODE_POSTFIX = "Request";

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
        url.append(serviceURL + "?");
        // URL pattern for KVP
        url.append(urlPattern);
        // ?
        url.append("?");
        // request
        url.append(RequestParams.request.name() + "=" + SosConstants.Operations.GetFeatureOfInterest.name());
        // service
        url.append("&" + OWSConstants.RequestParams.service.name() + "=" + SosConstants.SOS);
        // version
        url.append("&" + OWSConstants.RequestParams.version.name() + "=" + version);
        // FOI identifier
        if (version.equalsIgnoreCase(Sos1Constants.SERVICEVERSION)) {
            url.append("&" + Sos1Constants.GetFeatureOfInterestParams.featureOfInterestID.name() + "=");
        } else {
            url.append("&" + Sos2Constants.GetFeatureOfInterestParams.featureOfInterest.name() + "=");
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
    public static int parseSrsName(String srsName, String srsNamePrefix) throws OwsExceptionReport {
        int srid = -1;
        if (srsName != null && !srsName.equals("") && !srsName.equalsIgnoreCase("NOT_SET")) {
            try {
                srid = Integer.valueOf(srsName.replace(srsNamePrefix, ""));
            } catch (NumberFormatException nfe) {
                String exceptionText =
                        "Error while parsing srsName parameter! Parameter has to match pattern '" + srsNamePrefix
                                + "' with appended EPSGcode number";
                LOGGER.error(exceptionText);
                OwsExceptionReport owse = new OwsExceptionReport(nfe);
                owse.addCodedException(OwsExceptionCode.NoApplicableCode,
                        SosConstants.GetObservationParams.srsName.name(), exceptionText);
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
        LOGGER.debug("Remaining Heap Size: " + freeMem);
        if (Runtime.getRuntime().totalMemory() == Runtime.getRuntime().maxMemory() && freeMem < 256000) { // 256000
            // accords to 256 kB create service exception
            String exceptionText =
                    "The observation response is to big for the maximal heap size = "
                            + Runtime.getRuntime().maxMemory()
                            + " Byte of the virtual machine! "
                            + "Please either refine your getObservation request to reduce the number of observations in the response or ask the administrator of this SOS to increase the maximum heap size of the virtual machine!";
            LOGGER.info(exceptionText);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(SosExceptionCode.ResponseExceedsSizeLimit, null, exceptionText);
            throw se;
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
                    String exceptionText =
                            "The parameter '" + paramName
                                    + "' has more than one value or is empty for HTTP-Post requests by this SOS!";
                    LOGGER.error(exceptionText);
                    OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                    owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
                    throw owse;
                }
            } else {
                String exceptionText =
                        "The parameter '" + paramName + "' is not supportted for HTTP-Post requests by this SOS!";
                LOGGER.error(exceptionText);
                OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
                throw owse;
            }
        }
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
     * Checks if the Decoder methods contain the operation
     * 
     * @param methods
     *            Decoder methods
     * @param operation
     *            SOS operation to check
     * @return True if Decoder method exists.
     */
    public static boolean checkMethodeImplementation4DCP(Method[] methods, String operation) {
        boolean implemented = false;
        String formattedOperation = HTTP_DECODER_METHODE_PREFIX + operation + HTTP_DECODER_METHODE_POSTFIX;
        for (Method method : methods) {
            if (method.getName().equals(formattedOperation)) {
                return true;
            }
        }
        return implemented;
    }

    /**
     * Get the HTTP DCPs for a operation
     * 
     * @param operation
     *            SOS operation
     * @param version
     *            SOS version
     * @param requestOperators
     *            All request operators
     * @param serviceURL
     *            SOS service URL
     * @return Map with DCPs for the SOS operation
     * @throws OwsExceptionReport
     */
    public static Map<String, List<String>> getDCP(String operation, String service, String version,
            Collection<IBinding> bindings, String serviceURL) throws OwsExceptionReport {
        List<String> httpGetUrls = new ArrayList<String>();
        List<String> httpPostUrls = new ArrayList<String>();
        try {
            for (IBinding binding : bindings) {
                // HTTP-Get
                if (binding.checkOperationHttpGetSupported(operation, service, version)) {
                    httpGetUrls.add(serviceURL + binding.getUrlPattern() + "?");
                }
                // HTTP-Post
                if (binding.checkOperationHttpPostSupported(operation, service, version)) {
                    httpPostUrls.add(serviceURL + binding.getUrlPattern());
                }

            }
        } catch (Exception e) {
            if (e instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e;
            }
            OwsExceptionReport owse = new OwsExceptionReport(e);
            throw owse;
        }
        Map<String, List<String>> dcp = new HashMap<String, List<String>>();
        dcp.put(SosConstants.HTTP_GET, httpGetUrls);
        dcp.put(SosConstants.HTTP_POST, httpPostUrls);
        return dcp;
    }

    private static String getKvpNamespaceForVersionAndOperation(String operation, String version) {
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            return Sos1Constants.NS_SOS;
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            return Sos2Constants.NS_SOS_20;
        }
        return "";
    }

    private static String getPostNamespaceForVersionAndOperation(String operation, String version) {
        if (version.equals(Sos1Constants.SERVICEVERSION)) {
            return Sos1Constants.NS_SOS;
        } else if (version.equals(Sos2Constants.SERVICEVERSION)) {
            if (operation.equals(SosConstants.Operations.DescribeSensor.name())
                    || operation.equals(Sos2Constants.Operations.InsertSensor.name())
                    || operation.equals(Sos2Constants.Operations.DeleteSensor.name())) {
                return SWEConstants.NS_SWES_20;
            } else {
                return Sos2Constants.NS_SOS_20;
            }
        }
        return "";
    }

    public static String getUrlPatternForHttpGetMethod(Collection<IBinding> bindings,
            String operationName, String version) throws OwsExceptionReport {
        try {
            for (IBinding binding : bindings) {
                if (binding.checkOperationHttpGetSupported(operationName, version,
                        getKvpNamespaceForVersionAndOperation(operationName, version))) {
                    return binding.getUrlPattern();
                }
            }
        } catch (Exception e) {
            if (e instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e;
            }
            OwsExceptionReport owse = new OwsExceptionReport(e);
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
     * @param procedureId
     *            The procedureId for the DescribeSensor request
     * 
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
        url.append(RequestParams.request.name() + "=" + SosConstants.Operations.DescribeSensor.name());
        // service
        url.append("&" + OWSConstants.RequestParams.service.name() + "=" + SosConstants.SOS);
        // version
        url.append("&" + OWSConstants.RequestParams.version.name() + "=" + version);
        // procedure
        url.append("&" + SosConstants.DescribeSensorParams.procedure.name() + "=" + procedureId);
        // outputFormat
        if (version.equalsIgnoreCase(Sos1Constants.SERVICEVERSION)) {
            url.append("&" + Sos1Constants.DescribeSensorParams.outputFormat + "="
                    + URLEncoder.encode(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE, "UTF-8"));
        } else {
            url.append("&" + Sos2Constants.DescribeSensorParams.procedureDescriptionFormat + "="
                    + URLEncoder.encode(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL, "UTF-8"));
        }

        return url.toString();
    }

    /**
     * Checks if the version value is 2.0.0
     * 
     * @param version
     *            requested version
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
     * Merge SosObservations with the same procedure id.
     * 
     * @param observationMembers
     *            SosObservations list
     * @return merged SosObservations as list
     */
    public static Collection<SosObservation> mergeObservationsForGenericObservation(
            Collection<SosObservation> observationMembers) {
        Collection<SosObservation> combinedObsCol = new ArrayList<SosObservation>();
        for (SosObservation sosObservation : observationMembers) {
            if (combinedObsCol.isEmpty()) {
                combinedObsCol.add(sosObservation);
            } else {
                boolean combined = false;
                for (SosObservation combinedSosObs : combinedObsCol) {
                    if (combinedSosObs.getObservationConstellation().equalsExcludingObsProp(
                            sosObservation.getObservationConstellation())) {
                        combinedSosObs.mergeWithObservation(sosObservation);
                        combined = true;
                        break;
                    }
                }
                if (!combined) {
                    combinedObsCol.add(sosObservation);
                }
            }
        }
        return combinedObsCol;
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
            LOGGER.error("checkServiceParameter", se);
            throw se;
        }
        // if not null, but incorrect, throw also exception
        else if (!service.equals(SosConstants.SOS)) {
            String exceptionText =
                    "The value of the mandatory parameter '" + SosConstants.GetCapabilitiesParams.service.toString()
                            + "' " + "must be '" + SosConstants.SOS + "'. Delivered value was: " + service;
            LOGGER.error(exceptionText);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue,
                    SosConstants.GetCapabilitiesParams.service.toString(), exceptionText);
            throw se;
        }
    }

    /**
     * help method to check the result format parameter. If the application/zip
     * result format is set, true is returned. If not and the value is text/xml;
     * subtype="OM" false is returned. If neither zip nor OM is set, a
     * ServiceException with InvalidParameterValue as its code is thrown.
     * 
     * @param resultFormat
     *            String containing the value of the result format parameter
     * @return boolean true if application/zip is the resultFormat value, false
     *         if its value is text/xml;subtype="OM"
     * @throws OwsExceptionReport
     *             if the parameter value is incorrect
     */
    public static boolean checkResponseFormat(String resultFormat) throws OwsExceptionReport {
        boolean isZipCompr = false;
        if (resultFormat.equalsIgnoreCase(OMConstants.CONTENT_TYPE_OM)) {
            return isZipCompr;
        } else if (resultFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_ZIP)) {
            isZipCompr = true;
            return isZipCompr;
        } else {
            String exceptionText =
                    "The value of the parameter '" + SosConstants.GetObservationParams.responseFormat.toString() + "'"
                            + "must be '" + OMConstants.CONTENT_TYPE_OM + " or " + SosConstants.CONTENT_TYPE_ZIP
                            + "'. Delivered value was: " + resultFormat;
            LOGGER.error(exceptionText);
            OwsExceptionReport se = new OwsExceptionReport();
            se.addCodedException(OwsExceptionCode.InvalidParameterValue,
                    SosConstants.GetObservationParams.responseFormat.toString(), exceptionText);

            throw se;
        }
    }

    /**
     * checks whether the value of outputFormat parameter is valid
     * 
     * @param outputFormat
     *            the outputFormat parameter which should be checked
     * @throws OwsExceptionReport
     *             if the value of the outputFormat parameter is incorrect
     */
    public static void checkProcedureOutputFormat(String outputFormat, String parameterName) throws OwsExceptionReport {
        if (outputFormat == null || outputFormat.isEmpty() || outputFormat == SosConstants.PARAMETER_NOT_SET) {
            String exceptionText =
                    "The value of the mandatory parameter '" + parameterName
                            + "' was not found in the request or is incorrect!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(SosConstants.DescribeSensorParams.procedure
                    .toString());
        }
        if (!outputFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
            if (!outputFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                String exceptionText =
                        "The value '" + outputFormat + "' of the " + parameterName
                                + " parameter is incorrect and has to be '"
                                + SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE + "' for the requested sensor!";
                LOGGER.debug(exceptionText);
                throw Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText);
            } else if (!outputFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
                String exceptionText =
                        "The value '" + outputFormat + "' of the " + parameterName
                                + " parameter is incorrect and has to be '"
                                + SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL + "' for the requested sensor!";
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
     * @throws OwsExceptionReport
     *             if the value of the sensor ID parameter is incorrect
     */
    public static void checkProcedureID(String procedureID, Collection<String> validProcedures, String parameterName)
            throws OwsExceptionReport {
        // null or an empty String
        if (procedureID == null || procedureID.isEmpty()) {
            String exceptionText =
                    "The value of the mandatory parameter '" + parameterName
                            + "' was not found in the request or is incorrect!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createMissingParameterValueException(parameterName);
        }
        if (!validProcedures.contains(procedureID)) {
            String exceptionText =
                    "The value of the '"
                            + parameterName
                            + "' parameter is incorrect. Please check the capabilities response document for valid values!";
            LOGGER.error(exceptionText);
            throw Util4Exceptions.createInvalidParameterValueException(
                    SosConstants.DescribeSensorParams.procedure.name(), exceptionText);
        }
    }

    public static void checkProcedureIDs(Collection<String> procedureIDs, Collection<String> validProcedures,
            String parameterName) throws OwsExceptionReport {
        if (procedureIDs != null) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (String procedureID : procedureIDs) {
                try {
                    checkProcedureID(procedureID, validProcedures, parameterName);
                } catch (OwsExceptionReport owse) {
                    exceptions.add(owse);
                }
            }
            Util4Exceptions.mergeExceptions(exceptions);
        }
    }

    public static void checkFeatureOfInterest(List<String> featuresOfInterest,
            Collection<String> validFeatureOfInterest, String parameterName) throws OwsExceptionReport {
        if (featuresOfInterest != null) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (String featureOfInterest : featuresOfInterest) {
                if (!validFeatureOfInterest.contains(featureOfInterest)) {
                    String exceptionText =
                            "The value '" + featureOfInterest + "' of the parameter '" + parameterName
                                    + "' is invalid";
                    LOGGER.error(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }
            }
            Util4Exceptions.mergeExceptions(exceptions);
        }
    }

    public static void checkObservedProperties(List<String> observedProperties,
            Collection<String> validObservedProperties, String parameterName) throws OwsExceptionReport {
        if (observedProperties != null) {
            List<OwsExceptionReport> exceptions = new ArrayList<OwsExceptionReport>();
            for (String observedProperty : observedProperties) {
                if (!validObservedProperties.contains(observedProperty)) {
                    String exceptionText =
                            "The value '" + observedProperty + "' of the parameter '" + parameterName + "' is invalid";
                    LOGGER.error(exceptionText);
                    exceptions.add(Util4Exceptions.createInvalidParameterValueException(parameterName, exceptionText));
                }
            }
            Util4Exceptions.mergeExceptions(exceptions);
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

    /**
     * Get valid FOI identifiers for SOS 2.0
     * 
     * @param version
     *            SOS version
     * @return valid FOI identifiers
     */
    public static Collection<String> getFeatureIDs(Collection<String> featureIDs, String version) {
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            Collection<String> validFeatureIDs = new ArrayList<String>();
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
        Map<MinMax, String> map = new HashMap<MinMax, String>();
        String minValue = envelope.getMinX() + " " + envelope.getMinY();
        map.put(MinMax.MIN, minValue);
        String maxValue = envelope.getMaxX() + " " + envelope.getMaxY();
        map.put(MinMax.MAX, maxValue);
        return map;
    }
    
}