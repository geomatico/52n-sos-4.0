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
package org.n52.sos.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.n52.sos.binding.Binding;
import org.n52.sos.decode.OperationDecoderKey;
import org.n52.sos.encode.Encoder;
import org.n52.sos.exception.CodedException;
import org.n52.sos.exception.ows.InvalidParameterValueException;
import org.n52.sos.exception.ows.MissingParameterValueException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.exception.ows.concrete.GenericThrowableWrapperException;
import org.n52.sos.exception.ows.concrete.InvalidProcedureDescriptionFormatException;
import org.n52.sos.exception.ows.concrete.InvalidResponseFormatParameterException;
import org.n52.sos.exception.ows.concrete.MissingProcedureDescriptionFormatException;
import org.n52.sos.exception.sos.ResponseExceedsSizeLimitException;
import org.n52.sos.exception.swes.InvalidRequestException;
import org.n52.sos.ogc.filter.TemporalFilter;
import org.n52.sos.ogc.gml.CodeType;
import org.n52.sos.ogc.gml.time.TimeInstant;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.ows.OWSConstants;
import org.n52.sos.ogc.ows.OWSConstants.RequestParams;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorMLConstants;
import org.n52.sos.ogc.sensorML.elements.SosSMLIo;
import org.n52.sos.ogc.sos.Sos1Constants;
import org.n52.sos.ogc.sos.Sos2Constants;
import org.n52.sos.ogc.sos.SosConstants;
import org.n52.sos.ogc.sos.SosConstants.FirstLatest;
import org.n52.sos.ogc.swe.simpleType.SosSweAbstractSimpleType;
import org.n52.sos.ogc.swe.simpleType.SosSweQuantity;
import org.n52.sos.ogc.swe.simpleType.SosSweTime;
import org.n52.sos.request.operator.AbstractRequestOperator;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Utility class for SOS
 * 
 */
public class SosHelper {
   
    private static Configuration config = new Configuration();
    private static final Logger LOGGER = LoggerFactory.getLogger(SosHelper.class);

    protected static Configuration getConfiguration() {
        return config;
    }

    protected static void setConfiguration(final Configuration config) {
        SosHelper.config = config;
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
    public static String createFoiGetUrl(final String foiId, final String version, final String serviceURL, final String urlPattern) {
        final StringBuilder url = new StringBuilder();
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
    public static String getFoiGetUrl(final String version, final String serviceURL, final String urlPattern) {
        final StringBuilder url = new StringBuilder();
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

    public static int parseSrsName(final String srsName) throws OwsExceptionReport {
        int srid = -1;
        if ((srsName != null) && !srsName.isEmpty() && !srsName.equalsIgnoreCase("NOT_SET")) {
            final String urnSrsPrefix = getConfiguration().getSrsNamePrefix();
            final String urlSrsPrefix = getConfiguration().getSrsNamePrefixSosV2();
            try {
                srid = Integer.valueOf(srsName.replace(urnSrsPrefix, "").replace(urlSrsPrefix, ""));
            } catch (final NumberFormatException nfe) {
                throw new NoApplicableCodeException().causedBy(nfe).at(SosConstants.GetObservationParams.srsName)
                        .withMessage("Error while parsing srsName parameter! Parameter has to match "
                                     + "pattern '%s' or '%s' with appended EPSGcode number", urnSrsPrefix, urlSrsPrefix);
            }
        }
        return srid;
    }

    /**
     * Checks the free memory size.
     * 
     * @throws OwsExceptionReport If no free memory size.
     */
    public static void checkFreeMemory() throws OwsExceptionReport {
        long freeMem;
        // check remaining free memory on heap if too small, throw exception to
        // avoid an OutOfMemoryError
        freeMem = Runtime.getRuntime().freeMemory();
        LOGGER.debug("Remaining Heap Size: " + (freeMem / 1024) + "KB");
        if ((Runtime.getRuntime().totalMemory() == Runtime.getRuntime().maxMemory()) && (freeMem < 256000)) { // 256000
            // accords to 256 kB create service exception
            throw new ResponseExceedsSizeLimitException()
                    .withMessage("The observation response is to big for the maximal heap size of %d Byte of the "
                                 + "virtual machine! Please either refine your getObservation request to reduce the "
                                 + "number of observations in the response or ask the administrator of this SOS to "
                                 + "increase the maximum heap size of the virtual machine!",
                                 Runtime.getRuntime().maxMemory());
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
    public static Envelope checkEnvelope(final Envelope envelope, final Geometry geometry) {
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
     *
     * @throws OwsExceptionReport * If the parameter is not supported by this SOS.
     */
    public static String parseHttpPostBodyWithParameter(final Enumeration<?> paramNames, final Map<?, ?> parameterMap) throws
            OwsExceptionReport {
        while (paramNames.hasMoreElements()) {
            final String paramName = (String) paramNames.nextElement();
            if (paramName.equalsIgnoreCase(RequestParams.request.name())) {
                final String[] paramValues = (String[]) parameterMap.get(paramName);
                if (paramValues.length == 1) {
                    return paramValues[0];
                } else {
                    throw new NoApplicableCodeException()
                            .withMessage("The parameter '%s' has more than one value or is empty for HTTP-Post requests by this SOS!",
                                         paramName);
        }
    } else {
        throw new NoApplicableCodeException()
                .withMessage("The parameter '%s' is not supportted for HTTP-Post requests by this SOS!", paramName);
    }
        }
        // FIXME: valid exception
        throw new NoApplicableCodeException();
    }

    /**
     * Checks if a request contains critical characters for SQL insertion, e.g.
     * '\');'
     * 
     * @param requestString
     *            Request as String

     * @deprecated legacy code not needed with hibernate
     * @throws OwsExceptionReport If the request contains critical characters
     */
    @Deprecated
    public static void checkRequestString(final String requestString) throws OwsExceptionReport {
        if (requestString.contains("');")) {
            throw new InvalidRequestException()
                    .withMessage("Request contains critical character sequence '\');'! If ProstgreSQL database is used, critical code can be excecuted via the request!");
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
    public static boolean checkFeatureOfInterestIdentifierForSosV2(final String featureOfInterestIdentifier,
                                                                   final String version) {
        return !(version.equals(Sos2Constants.SERVICEVERSION) &&
                 featureOfInterestIdentifier.startsWith(SosConstants.GENERATED_IDENTIFIER_PREFIX));
    }

    /**
     * @deprecated not used and quite buggy (e.g. REST binding may be selected)
     */
    @Deprecated
    public static String getUrlPatternForHttpGetMethod(final OperationDecoderKey decoderKey) throws OwsExceptionReport {
        try {
            for (final Binding binding : getConfiguration().getBindings()) {
                if (binding.checkOperationHttpGetSupported(decoderKey)) {
                    return binding.getUrlPattern();
                }
            }
        } catch (Exception e) {
            // FIXME valid exception
            throw new NoApplicableCodeException();
        }
        return null;
    }

    /**
     * invert a string map, allowing for duplicate values
     * 
     * @param hierarchy
     *            map to invert

     *
     * @return inverted map
     *
     * @deprecated not used; moved to WritableContentCache (?)
     */
    @Deprecated
    public static <K, V> Map<V, Set<K>> invertHierarchy(final Map<K, Set<V>> hierarchy) {
        final SetMultiMap<V, K> invertedHierarchy = MultiMaps.newSetMultiMap();
        for (final K key : hierarchy.keySet()) {
            for (final V value : hierarchy.get(key)) {
                invertedHierarchy.add(value, key);
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
    // FIXME move to ReadableCache
    public static Set<String> getHierarchy(final Map<String, Set<String>> hierarchy, final String key,
                                           final boolean fullHierarchy,
                                           final boolean includeStartKey) {
        final Set<String> hierarchyValues = new HashSet<String>();
        if (includeStartKey) {
            hierarchyValues.add(key);
        }

        final Stack<String> keysToCheck = new Stack<String>();
        keysToCheck.push(key);

        while (!keysToCheck.isEmpty()) {
            final Collection<String> keyValues = hierarchy.get(keysToCheck.pop());
            if (keyValues != null) {
                for (final String value : keyValues) {
                    if (hierarchyValues.add(value) && fullHierarchy) {
                        keysToCheck.push(value);
                    }
                }
            }
        }

        return hierarchyValues;
    }

    /**
     * get collection of hierarchy values for a set of keys
     *
     * @param hierarchy        map to example
     * @param keys             start key
     * @param fullHierarchy    whether to traverse down the full hierarchy
     * @param includeStartKeys whether to include the passed keys in the result collection
     *
     * @return collection of the full hierarchy
     */
    // FIXME move to ReadableCache
    public static Set<String> getHierarchy(final Map<String, Set<String>> hierarchy, final Set<String> keys, final boolean fullHierarchy,
                                           final boolean includeStartKeys) {
        final Set<String> parents = new HashSet<String>();
        for (final String key : keys) {
            parents.addAll(getHierarchy(hierarchy, key, fullHierarchy, includeStartKeys));
        }
        return parents;
}

    /**
     * creates a HTTP-GET string for DescribeSensor.
     * 
     * @param version
     *            the version of the request
     * @param serviceURL
     *            the service url
     * @param procedureId
     *            The procedureId for the DescribeSensor request
     * 
     * @param urlPattern
     *            the url pattern (e.g. /kvp)
     * @return Get-URL as String
     * @throws UnsupportedEncodingException
     */
    public static String getDescribeSensorUrl(final String version, final String serviceURL, final String procedureId, final String urlPattern)
            throws UnsupportedEncodingException {
        final StringBuilder url = new StringBuilder();
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

     *
     * @throws OwsExceptionReport * if the parameter value is incorrect
     */
    public static boolean checkResponseFormat(final String responseFormat, final String version) throws OwsExceptionReport {
        if (OMHelper.checkOMResponseFormat(responseFormat)) {
            return false;
        } else if (responseFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_ZIP)) {
            return true;
        } else {
            throw new InvalidResponseFormatParameterException(responseFormat);
        }
    }

    /**
     * help method to check the result format parameter for application/zip.
     * 
     * @param responseFormat
     *            String containing the value of the responseFormat parameter
     * @return boolean true if application/zip
     */
    public static boolean checkResponseFormatForZipCompression(final String responseFormat) {
        return responseFormat.equalsIgnoreCase(SosConstants.CONTENT_TYPE_ZIP);
    }

    /**
     * checks whether the value of outputFormat parameter is valid
     * 
     * @param procedureDecriptionFormat
     *            the outputFormat parameter which should be checked
     * @param parameterName
     *            the parameter name

     *
     * @throws OwsExceptionReport if the value of the outputFormat parameter is incorrect
     */
    public static void checkProcedureDescriptionFormat(final String procedureDecriptionFormat, final String parameterName) throws
            OwsExceptionReport {
        if ((procedureDecriptionFormat == null) || procedureDecriptionFormat.isEmpty()
            || procedureDecriptionFormat.equals(SosConstants.PARAMETER_NOT_SET)) {
            throw new MissingProcedureDescriptionFormatException();
        }
        if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
            if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_MIME_TYPE)) {
                throw new InvalidProcedureDescriptionFormatException(procedureDecriptionFormat);
            } else if (!procedureDecriptionFormat.equals(SensorMLConstants.SENSORML_OUTPUT_FORMAT_URL)) {
                throw new InvalidProcedureDescriptionFormatException(procedureDecriptionFormat);
            }
        }
    }

    /**
     * Get valid FOI identifiers for SOS 2.0
     * 
     * @param featureIDs
     *            FOI identifiers to test
     * @param version
     *            SOS version
     * @return valid FOI identifiers
     */
    public static Collection<String> getFeatureIDs(final Collection<String> featureIDs, final String version) {
        if (version.equals(Sos2Constants.SERVICEVERSION)) {
            final Collection<String> validFeatureIDs = new ArrayList<String>(featureIDs.size());
            for (final String featureID : featureIDs) {
                if (checkFeatureOfInterestIdentifierForSosV2(featureID, version)) {
                    validFeatureIDs.add(featureID);
                }
            }
            return validFeatureIDs;
        }
        return featureIDs;
    }

    /**
     * Creates the minimum and maximum values of this envelope in the default EPSG.
     * <p/>
     * @param envelope the envelope
     * <p/>
     * @return the {@code MinMax} describing the envelope
     * <p/>
     */
    public static MinMax<String> getMinMaxFromEnvelope(final Envelope envelope) {
        // TODO for full 3D support add minz to parameter in setStringValue
        return new MinMax<String>()
                .setMaximum(envelope.getMaxX() + " " + envelope.getMaxY())
                .setMinimum(envelope.getMinX() + " " + envelope.getMinY());
    }

    public static SosObservableProperty createSosOberavablePropertyFromSosSMLIo(final SosSMLIo<?> output) {
        final SosSweAbstractSimpleType<?> ioValue = output.getIoValue();
        final String identifier = ioValue.getDefinition();
        final String description = ioValue.getDescription();
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
        if ((unit == null) || unit.isEmpty()) {
            unit = SosConstants.NOT_DEFINED;
        }
        return new SosObservableProperty(identifier, description, unit, valueType);
    }

    /**
     * @deprecated use {@link CodingRepository#getSupportedResponseFormats(java.lang.String, java.lang.String) }
     */
    @Deprecated
public static Collection<String> getSupportedResponseFormats(final String service, final String version) {
        return Configurator.getInstance().getCodingRepository().getSupportedResponseFormats(service, version);
    }
    
    /**
     * @deprecated this method os not used anywhere (2013-04-08_15-35 CEST)
     * FIXME remove before release
     */
    @Deprecated
    public static <T extends Serializable> T duplicateObject(final T objectToDuplicate) throws OwsExceptionReport {
        try {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(objectToDuplicate);
            final ByteArrayInputStream byteArrayIntputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayIntputStream);
            @SuppressWarnings("unchecked")
			final
            T duplicatedObject = (T) objectInputStream.readObject();
            return duplicatedObject;
        } catch (final IOException e) {
            throw new GenericThrowableWrapperException(e).withMessage("Error while duplicating object!");
        } catch (final ClassNotFoundException e) {
            throw new GenericThrowableWrapperException(e).withMessage("Error while duplicating object!");
        }
    }

    public static void checkHref(final String href, final String parameterName) throws OwsExceptionReport {
        if (!href.startsWith("http") && !href.startsWith("urn")) {
            throw new InvalidParameterValueException().at(parameterName)
                    .withMessage("The reference (href) has an invalid style!");
        }
    }

    public static String createCSVFromCodeTypeList(final List<CodeType> values) {
        final StringBuilder builder = new StringBuilder();
        if ((values != null) && !values.isEmpty()) {
            for (final CodeType value : values) {
                builder.append(value.getValue());
                builder.append(',');
            }
            builder.delete(builder.lastIndexOf(","), builder.length());
        }
        return builder.toString();
    }

    public static List<CodeType> createCodeTypeListFromCSV(final String csv) {
        final List<CodeType> names = new ArrayList<CodeType>(0);
        if ((csv != null) && !csv.isEmpty()) {
            for (final String name : csv.split(",")) {
                names.add(new CodeType(name));
            }
        }
        return names;
    }

    /**
     *
     * @deprecated see {@link AbstractRequestOperator#checkObservationType(java.lang.String, java.lang.String)}
     */
    @Deprecated
    public static void checkObservationType(final String observationType, final String parameterName) throws OwsExceptionReport {
        final Collection<String> validObservationTypes = getConfiguration().getObservationTypes();
        if (observationType.isEmpty()) {
            throw new MissingParameterValueException(parameterName);
        } else if (!validObservationTypes.contains(observationType)) {
            throw new InvalidParameterValueException(parameterName, observationType);
        }

    }
    /**
     * @deprecated not used; similar methods moved to GetObservationDAO
     */
    @Deprecated
    public static boolean hasFirstLatestTemporalFilter(final List<TemporalFilter> temporalFilters) {
        for (final TemporalFilter temporalFilter : temporalFilters) {
            if ((temporalFilter.getTime() != null) && (temporalFilter.getTime() instanceof TimeInstant)) {
                final TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (ti.isSetIndeterminateValue() && FirstLatest.contains(ti.getIndeterminateValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @deprecated moved to GetObservationDAO
     */
    @Deprecated
    public static List<FirstLatest> getFirstLatestTemporalFilter(final List<TemporalFilter> temporalFilters) {
        final List<FirstLatest> filters = new ArrayList<FirstLatest>(0);
        for (final TemporalFilter temporalFilter : temporalFilters) {
            if ((temporalFilter.getTime() != null) && (temporalFilter.getTime() instanceof TimeInstant)) {
                final TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                if (ti.isSetIndeterminateValue() && FirstLatest.contains(ti.getIndeterminateValue())) {
                    filters.add(FirstLatest.getEnumForString(ti.getIndeterminateValue()));
                }
            }
        }
        return filters;
    }

    /**
     * @deprecated moved to GetObservationDAO
     */
    @Deprecated
    public static List<TemporalFilter> getNonFirstLatestTemporalFilter(final List<TemporalFilter> temporalFilters) {
        final List<TemporalFilter> filters = new ArrayList<TemporalFilter>(0);
        for (final TemporalFilter temporalFilter : temporalFilters) {
            if (temporalFilter.getTime() != null) {
                if (temporalFilter.getTime() instanceof TimeInstant) {
                    final TimeInstant ti = (TimeInstant) temporalFilter.getTime();
                    if (!ti.isSetIndeterminateValue()
                            || (ti.isSetIndeterminateValue() && !FirstLatest.contains(ti.getIndeterminateValue()))) {
                        filters.add(temporalFilter);
                    }
                } else {
                    filters.add(temporalFilter);
                }
            }
        }
        return filters;
    }

    /**
     * Hide utility constructor
     */
    protected SosHelper() {
    }

    /**
     * Class to encapsulate all calls to the {@link Configurator}. Can be overwritten by tests.
     *
     * @see SosHelper#setConfiguration(org.n52.sos.util.SosHelper.Configuration)
     */
    protected static class Configuration {
        protected Collection<String> getObservationTypes() {
            return Configurator.getInstance().getCache().getObservationTypes();
        }

        protected String getSrsNamePrefix() {
            return Configurator.getInstance().getSrsNamePrefix();
        }

        protected String getSrsNamePrefixSosV2() {
            return Configurator.getInstance().getSrsNamePrefixSosV2();
        }

        protected Set<Encoder<?, ?>> getEncoders() {
            return Configurator.getInstance().getCodingRepository().getEncoders();
        }

        protected Collection<Binding> getBindings() {
            return Configurator.getInstance().getBindingRepository().getBindings().values();
        }
    }

    public static void checkSection(List<String> sections) throws CodedException {
        for (String section : sections) {
            if (section == null || (section != null && section.isEmpty())) {
                throw new MissingParameterValueException(SosConstants.GetCapabilitiesParams.Section.name())
                        .withMessage("The section element is empty!");
            }
        }
    }
}