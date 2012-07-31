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

package org.n52.sos.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Timer;

import javax.servlet.UnavailableException;

import org.n52.sos.binding.IBinding;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.IOperationDAO;
import org.n52.sos.encode.EncoderKeyType;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.encode.ISosRequestEncoder;
import org.n52.sos.ogc.ows.OWSConstants.ExceptionLevel;
import org.n52.sos.ogc.ows.OWSConstants.OwsExceptionCode;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.Range;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.service.admin.operator.IAdminServiceOperator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO;
 * configures the logger.
 * 
 */
public final class Configurator {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);

    /** propertyname of CAPABILITIESCACHECONTROLLER property */
    private static final String CAPABILITIESCACHECONTROLLER = "CAPABILITIESCACHECONTROLLER";

    /** propertyname of CAPABILITIESCACHECONTROLLER property */
    private static final String CAPABILITIESCACHEUPDATEINTERVAL = "CAPABILITIESCACHEUPDATEINTERVAL";

    /** propertyname of character encoding */
    private static final String CHARACTER_ENCODING = "CHARACTERENCODING";

    /** propertyname of childProceduresEncodedInParentsDescribeSensor */
    private static final String CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBESENSOR =
            "CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBESENSOR";

    /** propertyname of CONFIG_FILE_PATH property */
    private static final String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";

    /** propertyname of CONFIGURATION_FILES property */
    private static final String CONFIGURATION_FILES = "CONFIGURATION_FILES";

    /** propertyname for decimal separator */
    private static final String DECIMAL_SEPARATOR = "DECIMALSEPARATOR";

    /** propertyname of DEFAULT_EPSG property */
    public static final String DEFAULT_EPSG = "DEFAULT_EPSG";

    /** propertyname of DEFAULT_OFFERING_PREFIX property */
    private static final String DEFAULT_OFFERING_PREFIX = "DEFAULT_OFFERING_PREFIX";

    /** propertyname of DEFAULT_PROCEDURE_PREFIX property */
    private static final String DEFAULT_PROCEDURE_PREFIX = "DEFAULT_PROCEDURE_PREFIX";

    /** propertyname of DYNAMIC_FOI_LOCATION property */
    private static final String DYNAMIC_FOI_LOCATION = "DYNAMIC_FOI_LOCATION";

    /** propertyname of foi encoded in observations */
    private static final String FOI_ENCODED_IN_OBSERVATION = "FOI_ENCODED_IN_OBSERVATION";

    /** propertyname of foiListedInOfferings */
    private static final String FOI_LISTED_IN_OFFERINGS = "FOI_LISTED_IN_OFFERINGS";

    /** propertyname of logging directory */
    private static final String GML_DATE_FORMAT = "GMLDATEFORMAT";

    /** propertyname of lease for getResulte operation */
    private static final String LEASE = "LEASE";

    /** propertyname of maximum GetObservation results */
    private static final String MAX_GET_OBS_RESULTS = "MAX_GET_OBS_RESULTS";

    /** propertyname of logging directory */
    private static final String NO_DATA_VALUE = "NODATAVALUE";

    /** propertyname of sensor directory */
    private static final String SENSOR_DIR = "SENSORDIR";

    /** propertyname of service identification */
    private static final String SERVICE_IDENTIFICATION_FILE = "SERVICEIDENTIFICATION";

    /** propertyname of service identification keywords */
    private static final String SERVICE_IDENTIFICATION_KEYWORDS = "SERVICEIDENTIFICATIONKEYWORDS";

    /** propertyname of service provider */
    private static final String SERVICE_PROVIDER_FILE = "SERVICEPROVIDER";

    /** propertyname of paramEnumsIncludedInCapabilities */
    private static final String SHOW_FULL_OPERATIONS_METADATA = "SHOW_FULL_OPERATIONS_METADATA";

    /** propertyname of show full OpsMetadata for obs in CapsDoc */
    private static final String SHOW_FULL_OPERATIONS_METADATA_4_OBSERVATIONS =
            "SHOW_FULL_OPERATIONS_METADATA_4_OBSERVATIONS";

    /** propertyname of skipDuplicateObservations */
    private static final String SKIP_DUPLICATE_OBSERVATIONS = "SKIP_DUPLICATE_OBSERVATIONS";

    /** propertyname of SOS_URL property */
    private static final String SOS_URL = "SOS_URL";

    /** propertyname of SPATIAL_OBSERVABLE_PROPERTY property */
    private static final String SPATIAL_OBSERVABLE_PROPERTY = "SPATIAL_OBSERVABLE_PROPERTY";

    /** propertyname of prefix URN for the spatial reference system */
    private static final String SRS_NAME_PREFIX = "SRS_NAME_PREFIX";

    /** propertyname of prefix URN for the spatial reference system */
    private static final String SRS_NAME_PREFIX_SOS_V2 = "SRS_NAME_PREFIX_SOS_V2";

    /** propertyname of SUPPORT_DYNAMIC_LOCATION property */
    private static final String SUPPORT_DYNAMIC_LOCATION = "SUPPORT_DYNAMIC_LOCATION";

    /** propertyname of supportsQuality */
    private static final String SUPPORTSQUALITY = "SUPPORTSQUALITY";

    /** propertyname of supportsQuality */
    private static final String SWITCHCOORDINATESFOREPSG = "SWITCHCOORDINATESFOREPSG";

    /** propertyname of logging directory */
    private static final String TOKEN_SEPERATOR = "TOKENSEPERATOR";

    /** propertyname of logging directory */
    private static final String TUPLE_SEPERATOR = "TUPLESEPERATOR";

    /** base path for configuration files */
    private String basepath;

    /** Capabilities Cache Controller */
    private ACapabilitiesCacheController capsCacheController;

    /**
     * property indicates whether SOS encodes the complete child procedure
     * System within a parent's DescribeSensor response or just the id and link
     */
    private boolean childProceduresEncodedInParentsDescribeSensor = false;

    /**
     * Implementation of ICacheFeederDAO
     */
    private ICacheFeederDAO cacheFeederDAO;

    /** character encoding for responses */
    private String characterEncoding;

    /**
     * Path to the configuration files
     */
    private String configFilePath;

    /**
     * Map with indicator and name of additional config files for modules
     */
    private Map<String, String> configFileMap;

    /**
     * Implementation of IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    private Map<DecoderKeyType, List<IDecoder>> decoder;

    /**
     * default EPSG code of stored geometries
     */
    private int defaultEPSG;

    /**
     * default offering identifier prefix, used for auto generation
     */
    private String defaultOfferingPrefix;

    /**
     * default procedure identifier prefix, used for auto generation
     */
    private String defaultProcedurePrefix;

    /** decimal separator for result element */
    private String decimalSeparator;

    private Map<EncoderKeyType, IEncoder> encoder;

    /**
     * Implementation of IFeatureQueryHandler
     */
    private IFeatureQueryHandler featureQueryHandler;

    /**
     * boolean indicates, whether SOS encodes the complete FOI-instance within
     * the Observation instance or just the FOI id
     */
    private boolean foiEncodedInObservation = true;

    /**
     * boolean, indicates if foi IDs should be included in capabilities
     */
    private boolean foiListedInOfferings = true;

    /** date format of gml */
    private String gmlDateFormat;

    /** instance attribut, due to the singleton pattern */
    private static Configurator instance = null;

    /** lease for getResult template in minutes */
    private int lease;

    /** maximum number of GetObservation results */
    private int maxGetObsResults;

    /** tuple seperator for result element */
    private String noDataValue;

    /** common SOS properties from configFile */
    private Properties props;

    /** encoder for encoding requests */
    private ISosRequestEncoder reqEncoder;

    /** Implemented ISosRequestListener */
    private Map<ServiceOperatorKeyType, IServiceOperator> serviceOperators;

    /** directory of sensor descriptions in SensorML format */
    private File sensorDir;

    /** file of service identification information in XML format */
    private File serviceIdentification;

    /** service identification keyword strings */
    private String[] serviceIdentificationKeywords;

    private ServiceLoader<IAdminServiceOperator> serviceLoaderAdminRequestOperator;

    /** ServiceLoader for ICacheFeederDAO */
    private ServiceLoader<ICacheFeederDAO> serviceLoaderCacheFeederDAO;

    /** ServiceLoader for ISosRequestOperator */
    private ServiceLoader<IBinding> serviceLoaderBindingOperator;

    private ServiceLoader<IDecoder> serviceLoaderDecoder;

    private ServiceLoader<IEncoder> serviceLoaderEncoder;

    /** ServiceLoader for IFeatureQueryHandler */
    private ServiceLoader<IFeatureQueryHandler> serviceLoaderFeatureQueryHandler;

    /** ServiceLoader for ISosOperationDAO */
    private ServiceLoader<IOperationDAO> serviceLoaderOperationDAOs;

    /** ServiceLoader for ISosOperationDAO */
    private ServiceLoader<IRequestOperator> serviceLoaderRequestOperators;

    /** ServiceLoader for ISosRequestListener */
    private ServiceLoader<IServiceOperator> serviceLoaderServiceOperators;

    /** file of service provider information in XML format */
    private File serviceProvider;

    /** URL of this service */
    private String serviceURL;

    /**
     * indicates if the FOI location should be set dynamically from spatial
     * observableProperties
     */
    private boolean setFoiLocationDynamically;

    /**
     * boolean, indicates if possible values for operation parameters should be
     * included in capabilities
     */
    private boolean showFullOperationsMetadata = true;

    /**
     * boolean indicates, whether SOS shows the full OperationsMetadata for
     * observation.
     */
    private boolean showFullOperationsMetadata4Observations = false;

    /**
     * boolean, indicates if duplicate observation should be silently ignored
     * during insertion If set to false, duplicate observations trigger an
     * exception
     */
    private boolean skipDuplicateObservations = false;

    /**
     * Implemented ISosOperationDAO
     */
    private Map<String, IOperationDAO> operationDAOs;

    /**
     * Implemented ISosRequestOperator
     */
    private Map<String, IBinding> bindingOperators;

    private Map<RequestOperatorKeyType, IRequestOperator> requestOperators;

    /**
     * Implementation of ASosAdminRequestOperator
     */
    private IAdminServiceOperator adminRequestOperator;

    /**
     * definition of the observableProperty which holds the dynamic location
     * values
     */
    private String spatialObsProp4DynymicLocation;

    /**
     * prefix URN for the spatial reference system
     */
    private String srsNamePrefix;

    /**
     * prefix URN for the spatial reference system
     */
    private String srsNamePrefixSosV2;

    /**
     * indicates if dynamic locations are supported
     */
    private boolean supportDynamicLocation;

    /**
     * boolean indicates, whether SOS supports quality information in
     * observations
     */
    private boolean supportsQuality = true;

    /** supported SOS versions */
    private Set<String> supportedVersions;

    /** boolean indicates the order of x and y components of coordinates */
    private List<Range> switchCoordinatesForEPSG = new ArrayList<Range>();

    /**
     * Timer for the ACapabilitiesCacheController implementation
     */
    private Timer timer;

    /** token seperator for result element */
    private String tokenSeperator;

    /** tuple seperator for result element */
    private String tupleSeperator;

    /** update interval for capabilities cache */
    private long updateIntervall;

    /**
     * private constructor due to the singelton pattern.
     * 
     * @param configis
     *            InputStream of the configfile
     * @param dbconfigis
     *            InputStream of the dbconfigfile
     * @param basepath
     *            base path for configuration files
     * @throws UnavailableException
     *             if the configFile could not be loaded
     * @throws OwsExceptionReport
     *             if the
     */
    private Configurator(InputStream configis, String basepath) throws OwsExceptionReport, UnavailableException {

        // logFile
        try {

            this.basepath = basepath;

            // creating common SOS properties object from inputStream
            props = loadProperties(configis);
            LOGGER.info("\n******\nConfig File loaded successfully!\n******\n");

        } catch (IOException ioe) {
            LOGGER.error("error while loading config file", ioe);
            throw new UnavailableException(ioe.getMessage());

        }
    }

    /**
     * Initialize this class. Since this initialization is not done in the
     * constructor, dependent classes can use the SosConfigurator already when
     * called from here.
     * 
     * @param soapMessageFactory11
     * @param soapMessageFactory12
     * @param docBuildFactory
     */
    private void initialize() throws OwsExceptionReport {

        supportedVersions = new HashSet<String>();

        String maxGetObsResultsString = props.getProperty(MAX_GET_OBS_RESULTS, "0");
        if (maxGetObsResultsString != null && maxGetObsResultsString.trim().length() > 0) {
            this.maxGetObsResults = Integer.valueOf(maxGetObsResultsString).intValue();
        } else {
            this.maxGetObsResults = 0;
        }

        // creating common SOS properties object from inputStream
        // default lease is 6 hours.
        String leaseString = props.getProperty(LEASE, "600");
        if (leaseString == null || leaseString.equals("")) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No lease is defined in the config file! Please set the lease property on an integer value!");
            LOGGER.error("No lease is defined in the config file! Please set the lease property on an integer value!",
                    se);
            throw se;
        }

        // creating common SOS properties object from inputStream
        // default lease is 6 hours.
        String defaultEPSGstring = props.getProperty(DEFAULT_EPSG, "4326");
        if (defaultEPSGstring == null || defaultEPSGstring.equals("")) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No default EPSG code is defined in the config file! Please set the default EPSG code property on an integer value!");
            LOGGER.error(
                    "No default EPSG code is defined in the config file! Please set the default EPSG code property on an integer value!",
                    se);
            throw se;
        }

        this.defaultEPSG = Integer.valueOf(defaultEPSGstring).intValue();

        String characterEnodingString = props.getProperty(CHARACTER_ENCODING);
        if (characterEnodingString == null || (characterEnodingString != null && characterEnodingString.isEmpty())) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No characterEnoding is defined in the config file!!");
            LOGGER.error("No characterEnoding is defined in the config file!!");
            throw se;
        }
        this.characterEncoding = characterEnodingString;

        String srsNamePrefixString = props.getProperty(SRS_NAME_PREFIX);
        if (srsNamePrefixString == null) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No prefix for the spation reference system is defined in the config file!!");
            LOGGER.error("No SRS prefix is defined in the config file!!");
            throw se;
        } else if (!srsNamePrefixString.endsWith(":") && srsNamePrefixString.length() != 0) {
            srsNamePrefixString += ":";
        }
        this.srsNamePrefix = srsNamePrefixString;

        String srsNamePrefixStringSosV2 = props.getProperty(SRS_NAME_PREFIX_SOS_V2);
        if (srsNamePrefixStringSosV2 == null) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No prefix for the spation reference system is defined in the config file!!");
            LOGGER.error("No SRS prefix is defined in the config file!!");
            throw se;
        } else if (!srsNamePrefixStringSosV2.endsWith("/") && srsNamePrefixStringSosV2.length() != 0) {
            srsNamePrefixStringSosV2 += "/";
        }
        this.srsNamePrefixSosV2 = srsNamePrefixStringSosV2;

        String supportsQualityString = props.getProperty(SUPPORTSQUALITY);
        if (supportsQualityString == null
                || (!supportsQualityString.equalsIgnoreCase("true") && !supportsQualityString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No supportsQuality is defined in the config file or the value :" + supportsQualityString
                            + " is wrong!");
            LOGGER.error("No supportsQuality is defined in the config file or the value '" + supportsQualityString
                    + "' is wrong!", se);
            throw se;
        }
        this.supportsQuality = Boolean.parseBoolean(supportsQualityString);

        // skip duplicate obs
        String skipDuplicateObservationsString = props.getProperty(SKIP_DUPLICATE_OBSERVATIONS);
        if (skipDuplicateObservationsString == null
                || (!skipDuplicateObservationsString.equalsIgnoreCase("true") && !skipDuplicateObservationsString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'skipDuplicateObservations' is defined in the config file or the value '"
                            + skipDuplicateObservationsString + "' is wrong!");
            LOGGER.error("No 'skipDuplicateObservations' is defined in the config file or the value :"
                    + skipDuplicateObservationsString + " is wrong!", se);
            throw se;
        }
        this.skipDuplicateObservations = Boolean.parseBoolean(skipDuplicateObservationsString);

        String switchCoordinatesForEPSGString = props.getProperty(SWITCHCOORDINATESFOREPSG);
        if (switchCoordinatesForEPSGString == null) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            String excMsg =
                    "No switchCoordinatesForEPSG is defined in the config file or the value '" + supportsQualityString
                            + "' is wrong!";
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null, excMsg);
            LOGGER.error(excMsg, se);
            throw se;
        }
        for (String switchCoordinatesForEPSGEntry : switchCoordinatesForEPSGString.split(";")) {
            String[] splittedSwitchCoordinatesForEPSGEntry = switchCoordinatesForEPSGEntry.split("-");
            if (splittedSwitchCoordinatesForEPSGEntry.length == 1) {
                Range r =
                        new Range(Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[0]),
                                Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[0]));
                switchCoordinatesForEPSG.add(r);
            } else if (splittedSwitchCoordinatesForEPSGEntry.length == 2) {
                Range r =
                        new Range(Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[0]),
                                Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[1]));
                switchCoordinatesForEPSG.add(r);
            } else {
                OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
                String excMsg =
                        "Invalid format of entry in 'switchCoordinatesForEPSG': " + switchCoordinatesForEPSGEntry;
                se.addCodedException(OwsExceptionCode.NoApplicableCode, null, excMsg);
                LOGGER.error(excMsg, se);
                throw se;
            }
        }

        // foi encoding
        String foiEncodedInObservationString = props.getProperty(FOI_ENCODED_IN_OBSERVATION);
        if (foiEncodedInObservationString == null
                || (!foiEncodedInObservationString.equalsIgnoreCase("true") && !foiEncodedInObservationString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'foiEncodedInObservation' is defined in the config file or the value '"
                            + supportsQualityString + "' is wrong!");
            LOGGER.error("No 'foiEncodedInObservation' is defined in the config file or the value :"
                    + foiEncodedInObservationString + " is wrong!", se);
            throw se;
        }
        this.foiEncodedInObservation = Boolean.parseBoolean(foiEncodedInObservationString);

        // foi included in offerings
        String foiListedInOfferingsString = props.getProperty(FOI_LISTED_IN_OFFERINGS);
        if (foiListedInOfferingsString == null
                || (!foiListedInOfferingsString.equalsIgnoreCase("true") && !foiListedInOfferingsString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'foiListedInOfferings' is defined in the config file or the value '"
                            + foiListedInOfferingsString + "' is wrong!");
            LOGGER.error("No 'foiListedInOfferings' is defined in the config file or the value :"
                    + foiListedInOfferingsString + " is wrong!", se);
            throw se;
        }
        this.foiListedInOfferings = Boolean.parseBoolean(foiListedInOfferingsString);

        // child procedures encoded in parents DescribeSensor
        String childProceduresEncodedInParentsDescribeSensorString =
                props.getProperty(CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBESENSOR, "false");
        this.childProceduresEncodedInParentsDescribeSensor =
                Boolean.parseBoolean(childProceduresEncodedInParentsDescribeSensorString);

        // full operations metadata
        String showFullOperationsMetadataString = props.getProperty(SHOW_FULL_OPERATIONS_METADATA);
        if (showFullOperationsMetadataString == null
                || (!showFullOperationsMetadataString.equalsIgnoreCase("true") && !showFullOperationsMetadataString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'showFullOperationsMetadata' is defined in the config file or the value '"
                            + showFullOperationsMetadataString + "' is wrong!");
            LOGGER.error("No 'showFullOperationsMetadata' is defined in the config file or the value :"
                    + showFullOperationsMetadataString + " is wrong!", se);
            throw se;
        }
        this.showFullOperationsMetadata = Boolean.parseBoolean(showFullOperationsMetadataString);

        // operations metadata
        String showFullOperationsMetadata4ObservationsString =
                props.getProperty(SHOW_FULL_OPERATIONS_METADATA_4_OBSERVATIONS);
        if (showFullOperationsMetadata4ObservationsString == null
                || (!showFullOperationsMetadata4ObservationsString.equalsIgnoreCase("true") && !showFullOperationsMetadata4ObservationsString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'showFullOperationsMetadata' is defined in the config file or the value '"
                            + showFullOperationsMetadata4ObservationsString + "' is wrong!");
            LOGGER.error("No 'showFullOperationsMetadata' is defined in the config file or the value :"
                    + showFullOperationsMetadata4ObservationsString + " is wrong!", se);
            throw se;
        }
        this.showFullOperationsMetadata4Observations =
                Boolean.parseBoolean(showFullOperationsMetadata4ObservationsString);

        // support dynamic location
        String supportDynamicLocationString = props.getProperty(SUPPORT_DYNAMIC_LOCATION, "false");
        if (supportDynamicLocationString == null
                || (!supportDynamicLocationString.equalsIgnoreCase("true") && !supportDynamicLocationString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'supportDynamicLocation' is defined in the config file or the value '"
                            + supportDynamicLocationString + "' is wrong!");
            LOGGER.error("No 'supportDynamicLocation' is defined in the config file or the value :"
                    + supportDynamicLocationString + " is wrong!", se);
            throw se;
        }
        this.supportDynamicLocation = Boolean.parseBoolean(supportDynamicLocationString);

        // dynamic foi location
        String dynamicFoiLocationString = props.getProperty(DYNAMIC_FOI_LOCATION, "false");
        if (dynamicFoiLocationString == null
                || (!dynamicFoiLocationString.equalsIgnoreCase("true") && !dynamicFoiLocationString
                        .equalsIgnoreCase("false"))) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "No 'dynamicFoiLocation' is defined in the config file or the value '" + dynamicFoiLocationString
                            + "' is wrong!");
            LOGGER.error("No 'dynamicFoiLocation' is defined in the config file or the value :"
                    + dynamicFoiLocationString + " is wrong!", se);
            throw se;
        }
        if (this.supportDynamicLocation == false && dynamicFoiLocationString != null
                && dynamicFoiLocationString.equalsIgnoreCase("true")) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "Support for dynamic location is set to false in the config file! To set dynamic foi location set '"
                            + supportDynamicLocation + "' to true!");
            LOGGER.error(
                    "Support for dynamic location is set to false in the config file! To set dynamic foi location set '"
                            + supportDynamicLocation + "' to true!", se);
            throw se;
        }
        this.setFoiLocationDynamically = Boolean.parseBoolean(dynamicFoiLocationString);

        // obsProp for dynamic location
        String spatialObsProp4DynymicLocationString = props.getProperty(SPATIAL_OBSERVABLE_PROPERTY);
        if (this.supportDynamicLocation == true
                && (spatialObsProp4DynymicLocationString == null || spatialObsProp4DynymicLocationString.isEmpty())) {
            OwsExceptionReport se = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            se.addCodedException(OwsExceptionCode.NoApplicableCode, null,
                    "Dynamic location support is set to true but no observable property is defined!!");
            LOGGER.error("Dynamic location support is set to true but no observable property is defined!!");
            throw se;
        }
        this.spatialObsProp4DynymicLocation = spatialObsProp4DynymicLocationString;

        this.defaultOfferingPrefix = props.getProperty(DEFAULT_OFFERING_PREFIX, "OFFERING_");

        this.defaultProcedurePrefix = props.getProperty(DEFAULT_PROCEDURE_PREFIX, "urn:ogc:object:feature:Sensor:");

        // loading service identification and provider file
        String serviceIdentificationFile = props.getProperty(SERVICE_IDENTIFICATION_FILE);
        this.serviceIdentification = new File(serviceIdentificationFile);
        if (!this.serviceIdentification.exists()) {
            serviceIdentificationFile = this.getBasePath() + props.getProperty(SERVICE_IDENTIFICATION_FILE);
            this.serviceIdentification = new File(serviceIdentificationFile);
        }
        LOGGER.info("\n******\nService Identification File loaded successfully from :" + serviceIdentificationFile
                + " !\n******\n");

        String keywords = props.getProperty(SERVICE_IDENTIFICATION_KEYWORDS);
        if (keywords != null) {
            this.serviceIdentificationKeywords = keywords.split(",");
        } else {
            this.serviceIdentificationKeywords = new String[0];
        }

        String serviceProviderFile = props.getProperty(SERVICE_PROVIDER_FILE);
        this.serviceProvider = new File(serviceProviderFile);
        if (!this.serviceProvider.exists()) {
            serviceProviderFile = this.getBasePath() + props.getProperty(SERVICE_PROVIDER_FILE);
            this.serviceProvider = new File(serviceProviderFile);
        }
        LOGGER.info("\n******\nService Identification File loaded successfully from :" + serviceProviderFile
                + " !\n******\n");

        // loading sensor directory
        this.sensorDir = new File(props.getProperty(SENSOR_DIR));
        if (!this.sensorDir.exists()) {
            this.sensorDir = new File(this.getBasePath() + props.getProperty(SENSOR_DIR));
        }
        LOGGER.info("\n******\nSensor directory file created successfully!\n******\n");

        // get config file path
        this.configFilePath = props.getProperty(CONFIG_FILE_PATH, "/WEB-INF/conf/");

        // get config file names and identifiers
        String configFileMapString = props.getProperty(CONFIGURATION_FILES);
        this.configFileMap = new HashMap<String, String>();
        if (configFileMapString != null && !configFileMapString.isEmpty()) {
            for (String kvp : configFileMapString.split(";")) {
                String[] keyValue = kvp.split(" ");
                this.configFileMap.put(keyValue[0], keyValue[1]);
            }
        }

        // //////////////////////////////////////////////////////////////
        // initialize constants for getResult operation
        this.tokenSeperator = props.getProperty(TOKEN_SEPERATOR);
        this.tupleSeperator = props.getProperty(TUPLE_SEPERATOR);
        this.decimalSeparator = props.getProperty(DECIMAL_SEPARATOR);
        this.gmlDateFormat = props.getProperty(GML_DATE_FORMAT);
        // if format is set
        if (gmlDateFormat != null && !gmlDateFormat.equals("")) {
            DateTimeHelper.setResponseFormat(gmlDateFormat);
        }
        this.noDataValue = props.getProperty(NO_DATA_VALUE);

        setServiceURL(props.getProperty(SOS_URL));

        // //////////////////////////////////////////////////////////////
        // initializing DAOFactory Implementation
        // if not set, throw exception
        LOGGER.info("\n******\n dssos.config file loaded successfully!!\n******\n");

        initializeConnectionProvider();
        initializeOperationDAOs();
        initializeServiceOperators();
        initalizeFeatureQueryHandler();
        initalizeCacheFeederDAO();
        initalizeDecoder();
        initalizeEncoder();
        initializeRequestOperators();

        // initialize CapabilitiesCache
        initializeCapabilitiesCacheController(props);

        initializeBindingOperator();
        initializeAdminRequestOperator();

    }

    /**
     * Eventually cleanup everything created by the constructor
     */
    public void cleanup() {
        if (connectionProvider != null) {
            connectionProvider.cleanup();
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * @return Returns an instance of the SosConfigurator. This method is used
     *         to implement the singelton pattern
     * 
     * @throws OwsExceptionReport
     * 
     *             if no DAOFactory Implementation class is defined in the
     *             ConfigFile or if one or more RequestListeners, defined in the
     *             configFile, could not be loaded
     * 
     * @throws UnavailableException
     *             if the configFile could not be loaded
     * @throws OwsExceptionReport
     * 
     */
    public static synchronized Configurator getInstance(InputStream configis, String basepath)
            throws UnavailableException, OwsExceptionReport {
        if (instance == null) {
            instance = new Configurator(configis, basepath);
            instance.initialize();
        }
        return instance;
    }

    /**
     * @return Returns the instance of the SosConfigurator. Null will be
     *         returned if the parameterized getInstance method was not invoked
     *         before. Usually this will be done in the SOS.
     */
    public static synchronized Configurator getInstance() {
        return instance;
    }

    /**
     * intializes the CapabilitiesCache
     * 
     * @throws OwsExceptionReport
     *             if initializing the CapabilitiesCache failed
     */
    @SuppressWarnings("unchecked")
    private void initializeCapabilitiesCacheController(Properties props) throws OwsExceptionReport {

        // TODO SOSX: entsprechend dem SOSX Flag den CapCacheController
        // initialisieren
        // Umschreiben des CapCache -> muss mehrfach instanzierbar sein
        // CacheController hat Liste mit SOSID und zugehoerigem CapCache
        // Wenn SOSX -> factory.getCapCacheDAO, sonst factory.getConfigDAO
        // rename to intializeCapabilitiesController

        String className = props.getProperty(CAPABILITIESCACHECONTROLLER);
        updateIntervall = Long.parseLong(props.getProperty(CAPABILITIESCACHEUPDATEINTERVAL));

        try {

            if (className == null) {
                LOGGER.error("No CapabilitiesCacheController Implementation is set in the configFile!");
                OwsExceptionReport se = new OwsExceptionReport();
                se.addCodedException(OwsExceptionCode.NoApplicableCode,
                        "SosConfigurator.initializeCapabilitiesCacheController()",
                        "No CapabilitiesCacheController Implementation is set in the configFile!");
                throw se;
            }

            // get Class of the OMEncoder Implementation
            Class controllerClass = Class.forName(className);

            // get Constructor of this class with matching parameter types
            Constructor<ACapabilitiesCacheController> constructor = controllerClass.getConstructor();

            this.capsCacheController = constructor.newInstance();

            LOGGER.info("\n******\n" + className + " loaded successfully!\n******\n");

            // start timertask only if intervall is greater than zero, else
            // don't start
            if (updateIntervall > 0) {
                timer = new Timer();
                timer.scheduleAtFixedRate(this.capsCacheController, getUpdateIntervallInMillis(),
                        getUpdateIntervallInMillis());
                LOGGER.info("\n******\n CapabilitiesCacheController timertask started successfully!\n******\n");
            } else {
                LOGGER.info("\n******\n CapabilitiesCacheController timertask not started!\n******\n");
            }
            try {
                this.capsCacheController.update(false);
            } catch (OwsExceptionReport owse) {
                LOGGER.error("Fatal error: Couldn't initialize capabilities cache!");
                throw owse;
            }

        } catch (ClassNotFoundException cnfe) {
            LOGGER.error("Error while loading CapabilitiesCacheController, required class could not be loaded: "
                    + cnfe.toString());
            throw new OwsExceptionReport(cnfe.getMessage(), cnfe.getCause());
        } catch (SecurityException se) {
            LOGGER.error("Error while loading CapabilitiesCacheController: " + se.toString());
            throw new OwsExceptionReport(se.getMessage(), se.getCause());
        } catch (NoSuchMethodException nsme) {
            LOGGER.error("Error while loading CapabilitiesCacheController, no required constructor available: "
                    + nsme.toString());
            throw new OwsExceptionReport(nsme.getMessage(), nsme.getCause());
        } catch (IllegalArgumentException iae) {
            LOGGER.error("Error while loading CapabilitiesCacheController, parameters for the constructor are illegal: "
                    + iae.toString());
            throw new OwsExceptionReport(iae.getMessage(), iae.getCause());
        } catch (InstantiationException ie) {
            LOGGER.error("The instantiation of a CapabilitiesCacheController failed: " + ie.toString());
            throw new OwsExceptionReport(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iace) {
            LOGGER.error("The instantiation of an CapabilitiesCacheController failed: " + iace.toString());
            throw new OwsExceptionReport(iace.getMessage(), iace.getCause());
        } catch (InvocationTargetException ite) {
            LOGGER.error("the instantiation of an CapabilitiesCacheController failed: " + ite.toString()
                    + ite.getLocalizedMessage() + ite.getCause());
            throw new OwsExceptionReport(ite.getMessage(), ite.getCause());
        }
    }

    /**
     * reads the requestListeners from the configFile and returns a
     * RequestOperator containing the requestListeners
     * 
     * @return RequestOperators with requestListeners
     * @throws OwsExceptionReport
     *             if initialization of a RequestListener failed
     */
    private void initializeAdminRequestOperator() throws OwsExceptionReport {
        serviceLoaderAdminRequestOperator = ServiceLoader.load(IAdminServiceOperator.class);
        Iterator<IAdminServiceOperator> iter = serviceLoaderAdminRequestOperator.iterator();
        try {
            this.adminRequestOperator = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("An IAdminServiceOperator implementation could not be loaded!", sce);
        }
        if (this.adminRequestOperator == null) {
            String exceptionText = "No IAdminServiceOperator implementation is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
        LOGGER.info("\n******\n IAdminServiceOperator loaded successfully!\n******\n");
    }

    /**
     * reads the requestListeners from the configFile and returns a
     * RequestOperator containing the requestListeners
     * 
     * @return RequestOperators with requestListeners
     * @throws OwsExceptionReport
     *             if initialization of a RequestListener failed
     */
    private void initializeBindingOperator() throws OwsExceptionReport {
        bindingOperators = new HashMap<String, IBinding>();
        serviceLoaderBindingOperator = ServiceLoader.load(IBinding.class);
        Iterator<IBinding> iter = serviceLoaderBindingOperator.iterator();
        while (iter.hasNext()) {
            try {
                IBinding iBindingOperator = (IBinding) iter.next();
                if (!(iBindingOperator instanceof IAdminServiceOperator)) {
                    bindingOperators.put(iBindingOperator.getUrlPattern(), iBindingOperator);
                }
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IBinding implementation could not be loaded!", sce);
            }
        }
        if (this.bindingOperators.isEmpty()) {
            String exceptionText = "No IBinding implementation could is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
        LOGGER.info("\n******\n Binding(s) loaded successfully!\n******\n");
    }

    /**
     * Load implemented cache feeder dao
     * 
     * @throws OwsExceptionReport
     *             If no cache feeder dao is implemented
     */
    private void initalizeCacheFeederDAO() throws OwsExceptionReport {
        serviceLoaderCacheFeederDAO = ServiceLoader.load(ICacheFeederDAO.class);
        setCacheFeederDAO();
        LOGGER.info("\n******\n CacheFeederDAO initialized successfully!\n******\n");
    }

    /**
     * Load the connection provider implementation
     * 
     * @throws OwsExceptionReport
     *             If no connection provider is implemented
     */
    private void initializeConnectionProvider() throws OwsExceptionReport {
        Iterator<IConnectionProvider> iter = ServiceLoader.load(IConnectionProvider.class).iterator();
        try {
            this.connectionProvider = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("No IConnectionProvider implementation could be loaded!", sce);
        }
        if (this.connectionProvider == null) {
            String exceptionText = "No IConnectionProvider implementation is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
        LOGGER.info("\n******\n ConnectionProvider loaded successfully!\n******\n");
    }

    private void initalizeDecoder() throws OwsExceptionReport {
        decoder = new HashMap<DecoderKeyType, List<IDecoder>>();
        serviceLoaderDecoder = ServiceLoader.load(IDecoder.class);
        setDecoder();
        LOGGER.info("\n******\n Decoder(s) initialized successfully!\n******\n");
    }

    private void initalizeEncoder() throws OwsExceptionReport {
        encoder = new HashMap<EncoderKeyType, IEncoder>();
        serviceLoaderEncoder = ServiceLoader.load(IEncoder.class);
        setEncoder();
        LOGGER.info("\n******\n Encoder(s) initialized successfully!\n******\n");
    }

    /**
     * Load implemented feature query handler
     * 
     * @throws OwsExceptionReport
     *             If no feature query handler is implemented
     */
    private void initalizeFeatureQueryHandler() throws OwsExceptionReport {
        serviceLoaderFeatureQueryHandler = ServiceLoader.load(IFeatureQueryHandler.class);
        setFeatureQueryHandler();
        LOGGER.info("\n******\n FeatureQueryHandler initialized successfully!\n******\n");
    }

    /**
     * Load implemented operation dao
     * 
     * @throws OwsExceptionReport
     *             If no operation dao is implemented
     */
    private void initializeOperationDAOs() throws OwsExceptionReport {
        operationDAOs = new HashMap<String, IOperationDAO>();
        serviceLoaderOperationDAOs = ServiceLoader.load(IOperationDAO.class);
        setOperationDAOs();
        LOGGER.info("\n******\n OperationDAO(s) initialized successfully!\n******\n");
    }

    /**
     * Load implemented request listener
     * 
     * @throws OwsExceptionReport
     *             If no request listener is implemented
     */
    private void initializeServiceOperators() throws OwsExceptionReport {
        serviceOperators = new HashMap<ServiceOperatorKeyType, IServiceOperator>();
        serviceLoaderServiceOperators = ServiceLoader.load(IServiceOperator.class);
        setServiceOperatorMap();
        LOGGER.info("\n******\n ServiceOperator(s) initialized successfully!\n******\n");
    }

    private void initializeRequestOperators() throws OwsExceptionReport {
        requestOperators = new HashMap<RequestOperatorKeyType, IRequestOperator>();
        serviceLoaderRequestOperators = ServiceLoader.load(IRequestOperator.class);
        setRequestOperatorMap();
        LOGGER.info("\n******\n RequestOperator(s) initialized successfully!\n******\n");
    }

    /**
     * Load the implemented cache feeder dao and add them to a map with
     * operation name as key
     * 
     * @throws OwsExceptionReport
     *             If no cache feeder dao is implemented
     */
    private void setCacheFeederDAO() throws OwsExceptionReport {
        Iterator<ICacheFeederDAO> iter = serviceLoaderCacheFeederDAO.iterator();
        try {
            this.cacheFeederDAO = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("An ICacheFeederDAO implementation could not be loaded!", sce);
        }
        if (this.cacheFeederDAO == null) {
            String exceptionText = "No ICacheFeederDAO implementations is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    private void setDecoder() throws OwsExceptionReport {
        Iterator<IDecoder> iter = serviceLoaderDecoder.iterator();

        while (iter.hasNext()) {
            try {
                IDecoder aDecoder = (IDecoder) iter.next();
                for (DecoderKeyType decoderKeyType : (List<DecoderKeyType>) aDecoder.getDecoderKeyTypes()) {
                    if (decoder.containsKey(decoderKeyType)) {
                        decoder.get(decoderKeyType).add(aDecoder);
                    } else {
                        List<IDecoder> decoderList = new ArrayList<IDecoder>();
                        decoderList.add(aDecoder);
                        decoder.put(decoderKeyType, decoderList);
                    }
                }
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IDecoder implementation could not be loaded!", sce);
            }
        }

        if (this.decoder.isEmpty()) {
            String exceptionText = "No IDecoder implementations is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    private void setEncoder() throws OwsExceptionReport {
        Iterator<IEncoder> iter = serviceLoaderEncoder.iterator();
        while (iter.hasNext()) {
            try {
                IEncoder aEncoder = (IEncoder) iter.next();
                for (EncoderKeyType encoderKeyType : (List<EncoderKeyType>) aEncoder.getEncoderKeyType()) {
                    encoder.put(encoderKeyType, aEncoder);
                }
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IEncoder implementation could not be loaded!", sce);
            }
        }
        if (this.encoder.isEmpty()) {
            String exceptionText = "No IEncoder implementations is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    /**
     * Load the implemented feature query handler and add them to a map with
     * operation name as key
     * 
     * @throws OwsExceptionReport
     *             If no feature query handler is implemented
     */
    private void setFeatureQueryHandler() throws OwsExceptionReport {
        Iterator<IFeatureQueryHandler> iter = serviceLoaderFeatureQueryHandler.iterator();
        try {
            this.featureQueryHandler = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("No IFeatureQueryHandler implementation could be loaded!", sce);
        }
        if (this.featureQueryHandler == null) {
            String exceptionText = "No IFeatureQueryHandler implementations is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    /**
     * Load the implemented operation dao and add them to a map with operation
     * name as key
     * 
     * @throws OwsExceptionReport
     *             If no operation dao is implemented
     */
    private void setOperationDAOs() throws OwsExceptionReport {
        Iterator<IOperationDAO> iter = serviceLoaderOperationDAOs.iterator();
        while (iter.hasNext()) {
            try {
                IOperationDAO aOperationDAO = (IOperationDAO) iter.next();
                operationDAOs.put(aOperationDAO.getOperationName(), aOperationDAO);
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IOperationDAO implementation could not be loaded!", sce);
            }
        }
        if (this.operationDAOs.isEmpty()) {
            String exceptionText = "No IOperationDAO implementations is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    private void setRequestOperatorMap() throws OwsExceptionReport {
        Iterator<IRequestOperator> iter = serviceLoaderRequestOperators.iterator();
        while (iter.hasNext()) {
            try {
                IRequestOperator aRequestOperator = (IRequestOperator) iter.next();
                requestOperators.put(aRequestOperator.getRequestOperatorKeyType(), aRequestOperator);
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IRequestOperator implementation could not be loaded!", sce);
            }
        }
        if (this.encoder.isEmpty()) {
            String exceptionText = "No IRequestOperator implementation is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }

    }

    /**
     * Load the implemented request listener and add them to a map with
     * operation name as key
     * 
     * @throws OwsExceptionReport
     *             If no request listener is implemented
     */
    private void setServiceOperatorMap() throws OwsExceptionReport {
        Iterator<IServiceOperator> iter = serviceLoaderServiceOperators.iterator();
        while (iter.hasNext()) {
            try {
                IServiceOperator iServiceOperator = iter.next();
                serviceOperators.put(iServiceOperator.getServiceOperatorKeyType(), iServiceOperator);
                supportedVersions.add(iServiceOperator.getServiceOperatorKeyType().getVersion());
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IServiceOperator implementation could not be loaded!", sce);
            }
        }
        if (this.serviceOperators.isEmpty()) {
            String exceptionText = "No IServiceOperator implementations is loaded!";
            LOGGER.error(exceptionText);
            OwsExceptionReport owse = new OwsExceptionReport(ExceptionLevel.DetailedExceptions);
            owse.addCodedException(OwsExceptionCode.NoApplicableCode, null, exceptionText);
            throw owse;
        }
    }

    public void updateDecoder() throws OwsExceptionReport {
        serviceLoaderDecoder.reload();
        setDecoder();
        LOGGER.info("\n******\n Decoder(s) re-initialized successfully!\n******\n");
    }

    public void updateEncoder() throws OwsExceptionReport {
        serviceLoaderEncoder.reload();
        setEncoder();
        LOGGER.info("\n******\n Encoder(s) re-initialized successfully!\n******\n");
    }

    /**
     * Update/reload the implemented operation dao
     * 
     * @throws OwsExceptionReport
     *             If no operation dao is implemented
     */
    public void updateOperationDAOs() throws OwsExceptionReport {
        serviceLoaderServiceOperators.reload();
        setOperationDAOs();
        LOGGER.info("\n******\n OperationDAO(s) re-initialized successfully!\n******\n");
    }

    public void updateRequestOperator() throws OwsExceptionReport {
        serviceLoaderRequestOperators.reload();
        setRequestOperatorMap();
        LOGGER.info("\n******\n RequestOperator(s) re-initialized successfully!\n******\n");
    }

    /**
     * Update/reload the implemented request listener
     * 
     * @throws OwsExceptionReport
     *             If no request listener is implemented
     */
    public void updateServiceOperators() throws OwsExceptionReport {
        serviceLoaderServiceOperators.reload();
        setServiceOperatorMap();
        LOGGER.info("\n******\n ServiceOperator(s) re-initialized successfully!\n******\n");
    }

    /**
     * method (re-)loads the configFile
     * 
     * @param is
     *            InputStream containing the configFile
     * @return Returns the configFile property
     * @throws IOException
     */
    public Properties loadProperties(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);

        return properties;
    }

    /**
     * @return Returns the service identification file
     */
    public File getServiceIdentification() {
        return serviceIdentification;
    }

    /**
     * @return Returns the service identification keywords
     */
    public String[] getServiceIdentificationKeywords() {
        return serviceIdentificationKeywords;
    }

    /**
     * @return Returns the service provider file
     */
    public File getServiceProvider() {
        return serviceProvider;
    }

    /**
     * 
     * @return Returns the sensor description directory
     */
    public File getSensorDir() {
        return sensorDir;
    }

    /**
     * @return the supportedVersions
     */
    public Set<String> getSupportedVersions() {
        return supportedVersions;
    }

    public boolean isVersionSupported(String version) {
        return supportedVersions.contains(version);
    }

    /**
     * @return maxGetObsResults
     */
    public int getMaxGetObsResults() {
        return maxGetObsResults;
    }

    /**
     * @param maxGetObsResults
     *            the maxGetObsResults to set
     */
    public void setMaxGetObsResults(int maxGetObsResults) {
        this.maxGetObsResults = maxGetObsResults;
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     */
    public int getLease() {
        return lease;
    }

    /**
     * @param lease
     *            The lease to set.
     */
    public void setLease(int lease) {
        this.lease = lease;
    }

    /**
     * @return Returns the tokenSeperator.
     */
    public String getTokenSeperator() {
        return tokenSeperator;
    }

    /**
     * @return Returns the tupleSeperator.
     */
    public String getTupleSeperator() {
        return tupleSeperator;
    }

    /**
     * Returns decimal separator
     */
    public String getDecimalSeparator() {
        return decimalSeparator;
    }

    /**
     * @return Returns the gmlDateFormat.
     */
    public String getGmlDateFormat() {
        return gmlDateFormat;
    }

    /**
     * @return Returns the noDataValue.
     */
    public String getNoDataValue() {
        return noDataValue;
    }

    /**
     * 
     * @return Returns the encoder for requests
     */
    public ISosRequestEncoder getRequestEncoder() {
        return reqEncoder;
    }

    /**
     * @return the supportsQuality
     */
    public boolean isSupportsQuality() {
        return supportsQuality;
    }

    /**
     * @param supportsQuality
     *            the supportsQuality to set
     */
    public void setSupportsQuality(boolean supportsQuality) {
        this.supportsQuality = supportsQuality;
    }

    /**
     * @param crs
     * @return boolean indicating if coordinates have to be switched
     */
    public boolean switchCoordinatesForEPSG(int crs) {

        boolean switchCoords = false;

        swtch: for (Range r : switchCoordinatesForEPSG) {
            if (r.contains(crs)) {
                switchCoords = true;
                break swtch;
            }
        }
        return switchCoords;
    }

    /**
     * @return boolean indicating the foi encoded in observation
     */
    public boolean isFoiEncodedInObservation() {
        return foiEncodedInObservation;
    }

    /**
     * @return foiIncludedInCapabilities
     */
    public boolean isFoiListedInOfferings() {
        return foiListedInOfferings;
    }

    /**
     * @return boolean indicating if child procedures should be fully encoded in
     *         parents' DescribeSensor resposnes
     */
    public boolean isChildProceduresEncodedInParentsDescribeSensor() {
        return childProceduresEncodedInParentsDescribeSensor;
    }

    /**
     * @return true if duplicate observations should be skipped during insertion
     */
    public boolean isSkipDuplicateObservations() {
        return skipDuplicateObservations;
    }

    /**
     * @return boolean indicating the show full OperationsMetadata for
     *         observations
     */
    public boolean isShowFullOperationsMetadata4Observations() {
        return showFullOperationsMetadata4Observations;
    }

    /**
     * @return showFullOperationsMetadata
     */
    public boolean isShowFullOperationsMetadata() {
        return showFullOperationsMetadata;
    }

    /**
     * @return the characterEncoding
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    public String getSrsNamePrefix() {
        return srsNamePrefix;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    public String getSrsNamePrefixSosV2() {
        return srsNamePrefixSosV2;
    }

    /**
     * @return the base path for configuration files
     */
    public String getBasePath() {
        return basepath;
    }

    /**
     * @return the current capabilitiesCacheController
     */
    public ACapabilitiesCacheController getCapsCacheController() {
        return capsCacheController;
    }

    /**
     * @return the updateInterval in milli seconds
     */
    public long getUpdateIntervallInMillis() {
        return updateIntervall * 60000;
    }

    /**
     * Get service URL.
     * 
     * @return
     */
    public String getServiceURL() {
        return serviceURL;
    }

    /**
     * Set service URL.
     * 
     * @param serviceURL
     */
    public void setServiceURL(String serviceURL) {
        String url = "";
        if (serviceURL.contains("?")) {
            String[] split = serviceURL.split("[?]");
            url = split[0];
        } else {
            url = serviceURL;
        }
        this.serviceURL = url;
    }

    /**
     * @return the supportDynamicLocation
     */
    public boolean isSupportDynamicLocation() {
        return supportDynamicLocation;
    }

    /**
     * @return the setFoiLocationDynamically
     */
    public boolean isSetFoiLocationDynamically() {
        return setFoiLocationDynamically;
    }

    /**
     * @return the spatialObsProp4DynymicLocation
     */
    public String getSpatialObsProp4DynymicLocation() {
        return spatialObsProp4DynymicLocation;
    }

    public String getDefaultOfferingPrefix() {
        return defaultOfferingPrefix;
    }

    public String getDefaultProcedurePrefix() {
        return defaultProcedurePrefix;
    }

    /**
     * @return the configFilePath
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * @return the configFileMap
     */
    public Map<String, String> getConfigFileMap() {
        return configFileMap;
    }

    /**
     * @return the implemented request listener
     * @throws OwsExceptionReport
     */
    public IServiceOperator getServiceOperator(String service, String version) throws OwsExceptionReport {
        return getServiceOperator(new ServiceOperatorKeyType(service, version));
    }

    public IServiceOperator getServiceOperator(ServiceOperatorKeyType serviceOperatorIdentifier)
            throws OwsExceptionReport {
        IServiceOperator serviceOperator = serviceOperators.get(serviceOperatorIdentifier);
        if (serviceOperator != null) {
            return serviceOperator;
        }
        String exceptionText =
                "The service (" + serviceOperatorIdentifier.getService() + ") and/or version ("
                        + serviceOperatorIdentifier.getVersion() + ") is not supported by this server!";
        LOGGER.debug(exceptionText);
        throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
    }

    /**
     * @return the implemented request listener
     * @throws OwsExceptionReport
     */
    public Map<ServiceOperatorKeyType, IServiceOperator> getServiceOperators() throws OwsExceptionReport {
        return serviceOperators;
    }

    /**
     * @return the implemented operation DAOs
     */
    public Map<String, IOperationDAO> getOperationDAOs() {
        return operationDAOs;
    }

    /**
     * @return the implemented cache feeder DAO
     */
    public ICacheFeederDAO getCacheFeederDAO() {
        return cacheFeederDAO;
    }

    /**
     * @return the implemented connection provider
     */
    public IConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * @return the implemented feature query handler
     */
    public IFeatureQueryHandler getFeatureQueryHandler() {
        return featureQueryHandler;
    }

    /**
     * @return the implemented SOS administration request operator
     */
    public IAdminServiceOperator getAdminRequestOperator() {
        return adminRequestOperator;
    }

    public int getDefaultEPSG() {
        return defaultEPSG;
    }

    /**
     * @return the decoder
     * @throws OwsExceptionReport
     */
    public List<IDecoder> getDecoder(String namespace) throws OwsExceptionReport {
        return getDecoder(new DecoderKeyType(namespace));
    }

    /**
     * @return the decoder
     * @throws OwsExceptionReport
     */
    public List<IDecoder> getDecoder(String service, String version) throws OwsExceptionReport {
        return getDecoder(new DecoderKeyType(service, version));
    }
    
    /**
     * @return the decoder
     * @throws OwsExceptionReport
     */
    public List<IDecoder> getDecoder(DecoderKeyType decoderKeyType) throws OwsExceptionReport {
        return decoder.get(decoderKeyType);
    }

    /**
     * @return the encoder
     * @throws OwsExceptionReport
     */
    public IEncoder getEncoder(String namespace) throws OwsExceptionReport {
        return encoder.get(new EncoderKeyType(namespace));
//        if (iEncoder == null) {
//            String exceptionText = "No encoder implementation is available for the namespace '" + namespace + "'!";
//            LOGGER.debug(exceptionText);
//            throw Util4Exceptions.createNoApplicableCodeException(null, exceptionText);
//        }
//        return iEncoder;
    }

    public IBinding getBindingOperator(String urlPattern) {
        return bindingOperators.get(urlPattern);
    }

    public IRequestOperator getRequestOperator(ServiceOperatorKeyType serviceOperatorKeyType, String operationName) {
        return requestOperators.get(new RequestOperatorKeyType(serviceOperatorKeyType, operationName));
    }
    
    public Map<RequestOperatorKeyType, IRequestOperator> getRequestOperator() {
        return requestOperators;
    }

    public Map<String, IBinding> getBindingOperators() {
        return bindingOperators;
    }
}