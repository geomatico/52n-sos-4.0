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
package org.n52.sos.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import org.n52.sos.binding.Binding;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.decode.DecoderKeyType;
import org.n52.sos.decode.IDecoder;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IDataSourceInitializator;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.IOperationDAO;
import org.n52.sos.ds.ISettingsDao;
import org.n52.sos.encode.EncoderKeyType;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.sos.Range;
import org.n52.sos.request.operator.IRequestOperator;
import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.service.admin.operator.IAdminServiceOperator;
import org.n52.sos.service.admin.request.operator.IAdminRequestOperator;
import org.n52.sos.service.operator.IServiceOperator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.tasking.ASosTasking;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.XmlHelper;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO;
 * configures the logger.
 */
public final class Configurator {

    /** logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);

    /** base path for configuration files */
    private String basepath;

    /** Capabilities Cache Controller */
    private ACapabilitiesCacheController capabilitiesCacheController;

    /**
     * property indicates whether SOS encodes the complete child procedure
     * System within a parent's DescribeSensor response or just the id and link
     */
    private boolean childProceduresEncodedInParentsDescribeSensor = false;

    /**se
     * Implementation of ICacheFeederDAO
     */
    private ICacheFeederDAO cacheFeederDAO;

    /** character encoding for responses */
    private String characterEncoding;

    /**
     * Map with indicator and name of additional config files for modules
     */
    private Map<String, String> configFileMap = new HashMap<String, String>(0);

    /**
     * Implementation of IConnectionProvider
     */
    private IConnectionProvider connectionProvider;

    private IDataSourceInitializator dataSourceInitializator;

    private Map<DecoderKeyType, List<IDecoder>> decoder = new HashMap<DecoderKeyType, List<IDecoder>>(0);

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

    private Map<EncoderKeyType, IEncoder> encoder = new HashMap<EncoderKeyType, IEncoder>(0);

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

    /** Implemented ISosRequestListener */
    private Map<ServiceOperatorKeyType, IServiceOperator> serviceOperators = new HashMap<ServiceOperatorKeyType, IServiceOperator>(0);

    /** directory of sensor descriptions in SensorML format */
    private File sensorDir;

    /** file of service identification information in XML format */
    private File serviceIdentification;

    /** service identification keyword strings */
    private String[] serviceIdentificationKeywords;
	private String serviceIdentificationTitle;
	private String serviceIdentificationAbstract;
	private String serviceIdentificationServiceType;
	private String serviceIdentificationFees;
	private String serviceIdentificationAccessConstraints;

    private ServiceLoader<IAdminServiceOperator> serviceLoaderAdminServiceOperator;
    
    private ServiceLoader<IAdminRequestOperator> serviceLoaderAdminRequesteOperator;

    /** ServiceLoader for ICacheFeederDAO */
    private ServiceLoader<ICacheFeederDAO> serviceLoaderCacheFeederDAO;

    /** ServiceLoader for ACapabilitiesCacheController */
    private ServiceLoader<ACapabilitiesCacheController> serviceLoaderCapabilitiesCacheController;

    /** ServiceLoader for ISosRequestOperator */
    private ServiceLoader<Binding> serviceLoaderBindingOperator;

    private ServiceLoader<IDataSourceInitializator> serviceLoaderDataSourceInitializator;

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

    private ServiceLoader<ASosTasking> serviceLoaderTasking;

    /** file of service provider information in XML format */
    private File serviceProvider;
	
	private String serviceProviderName;
	private String serviceProviderSite;
	private String serviceProviderIndividualName;
	private String serviceProviderPositionName;
	private String serviceProviderPhone;
	private String serviceProviderDeliveryPoint;
	private String serviceProviderCity;
	private String serviceProviderPostalCode;
	private String serviceProviderCountry;
	private String serviceProviderMailAddress;
	private String serviceProviderAdministrativeArea;
	private int minimumGzipSize;

    /** URL of this service */
    private String serviceURL;

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
    private Map<String, IOperationDAO> operationDAOs = new HashMap<String, IOperationDAO>(0);

    /**
     * Implemented ISosRequestOperator
     */
    private Map<String, Binding> bindingOperators = new HashMap<String, Binding>(0);

    private Map<RequestOperatorKeyType, IRequestOperator> requestOperators = new HashMap<RequestOperatorKeyType, IRequestOperator>(0);

    /**
     * Implementation of ASosAdminRequestOperator
     */
    private IAdminServiceOperator adminServiceOperator;
    
    
    private Map<String, IAdminRequestOperator> adminRequestOperators = new HashMap<String, IAdminRequestOperator>(0);

    /**
     * prefix URN for the spatial reference system
     */
    private String srsNamePrefix;

    /**
     * prefix URN for the spatial reference system
     */
    private String srsNamePrefixSosV2;

    /**
     * boolean indicates, whether SOS supports quality information in
     * observations
     */
    private boolean supportsQuality = true;

    /** supported SOS versions */
    private Set<String> supportedVersions = new HashSet<String>(0);
    
    /** supported services */
    private Set<String> supportedServices = new HashSet<String>(0);
    
    /** boolean indicates the order of x and y components of coordinates */
    private List<Range> switchCoordinatesForEPSG;

    private Timer taskingExecutor;

    /** token seperator for result element */
    private String tokenSeperator;

    /** tuple seperator for result element */
    private String tupleSeperator;

    /** update interval for capabilities cache */
	private long updateIntervall;
	
	
	private Properties connectionProviderProperties;

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
     * @throws IOException
     */
    private Configurator(Properties config, String basepath) throws ConfigurationException {
		if (basepath == null) {
			String message = "No basepath available!";
			LOGGER.info(message);
			throw new ConfigurationException(message);
		}
		if (config == null) {
			String message = "No connection provider configuration available!";
			LOGGER.info(message);
			throw new ConfigurationException(message);
		}
		
		this.basepath = basepath;
		this.connectionProviderProperties = config;
        LOGGER.info("\n******\nConfig File loaded successfully!\n******\n");
    }
	
	public void changeSetting(Setting setting, String newValue) throws ConfigurationException {
		setSetting(setting, newValue, false);
	}
	
	private void setSetting(Setting setting, String value, boolean initial) throws ConfigurationException {
		/* TODO check what has to be reinitialized when settings change */
		switch(setting) {
			case CAPABILITIES_CACHE_UPDATE_INTERVAL:
				int capCacheUpdateIntervall = parseInteger(setting, value);
				if (this.updateIntervall != capCacheUpdateIntervall) {
					this.updateIntervall  = capCacheUpdateIntervall;
				}
				if (!initial) {
					this.capabilitiesCacheController.reschedule();
				}
				break;
			case CHARACTER_ENCODING:
				if (value == null || value.isEmpty()) {
					String exceptionText = "No characterEnoding is defined in the config file!";
					LOGGER.error(exceptionText);
					throw new ConfigurationException(exceptionText);
				}
				this.characterEncoding = value;
				XmlOptionsHelper.getInstance(this.characterEncoding, true);
				break;
			case CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBE_SENSOR:
				this.childProceduresEncodedInParentsDescribeSensor = parseBoolean(setting, value);
				break;
			case DEFAULT_OFFERING_PREFIX:
				this.defaultOfferingPrefix = parseString(setting, value, true);
				break;
			case DEFAULT_PROCEDURE_PREFIX:
				this.defaultProcedurePrefix = parseString(setting, value, true);
				break;
			case FOI_ENCODED_IN_OBSERVATION:
				this.foiEncodedInObservation = parseBoolean(setting, value);
				break;
			case FOI_LISTED_IN_OFFERINGS:
				this.foiListedInOfferings = parseBoolean(setting, value);
				break;
			case GML_DATE_FORMAT:
				this.gmlDateFormat = parseString(setting, value, true);
				if (this.gmlDateFormat != null && !this.gmlDateFormat.isEmpty()) {
					DateTimeHelper.setResponseFormat(this.gmlDateFormat);
				}
				break;
			case LEASE:
				if (value == null | value.isEmpty()) {
					String exceptionText =
							"No lease is defined in the config file! Please set the lease property on an integer value!";
					LOGGER.error(exceptionText);
					throw new ConfigurationException(exceptionText);
				}
				this.lease = Integer.valueOf(value).intValue();
				break;
			case MAX_GET_OBSERVATION_RESULTS:
				this.maxGetObsResults = parseInteger(setting, value);
				break;
			case NO_DATA_VALUE:
				this.noDataValue = parseString(setting, value, false);
				break;
			case SENSOR_DIRECTORY:
				this.sensorDir = parseFile(setting, value, true);
				break;
			case SHOW_FULL_OPERATIONS_METADATA:
				this.showFullOperationsMetadata = parseBoolean(setting, value);
				break;
			case SHOW_FULL_OPERATIONS_METADATA_FOR_OBSERVATIONS:
				this.showFullOperationsMetadata4Observations = parseBoolean(setting, value);
				break;
			case SKIP_DUPLICATE_OBSERVATIONS:
				this.skipDuplicateObservations = parseBoolean(setting, value);
				break;
			case SOS_URL:
				setServiceURL(parseString(setting, value, false));
				break;
			case SUPPORTS_QUALITY:
				this.supportsQuality = parseBoolean(setting, value);
				break;
			case DEFAULT_EPSG:
				this.defaultEPSG = parseInteger(setting, value);
				break;
			case SRS_NAME_PREFIX_SOS_V1:
				String srsPrefixV1 = parseString(setting, value, true);
				if (!srsPrefixV1.endsWith(":") && srsPrefixV1.length() != 0) {
					srsPrefixV1 += ":";
				}
				this.srsNamePrefix = srsPrefixV1;
				break;
			case SRS_NAME_PREFIX_SOS_V2:
				String srsPrefixV2 = parseString(setting, value, true);
				if (!srsPrefixV2.endsWith("/") && srsPrefixV2.length() != 0) {
					srsPrefixV2 += "/";
				}
				this.srsNamePrefixSosV2 = srsPrefixV2;
				break;
			case SWITCH_COORDINATES_FOR_EPSG_CODES:
				String[] switchCoordinatesForEPSGStrings = parseString(setting, value, true).split(";");
				this.switchCoordinatesForEPSG = new ArrayList<Range>(switchCoordinatesForEPSGStrings.length);
				for (String switchCoordinatesForEPSGEntry : switchCoordinatesForEPSGStrings) {
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
						StringBuilder exceptionText = new StringBuilder();
						exceptionText.append("Invalid format of entry in 'switchCoordinatesForEPSG': ");
						exceptionText.append(switchCoordinatesForEPSGEntry);
						LOGGER.error(exceptionText.toString());
						throw new ConfigurationException(exceptionText.toString());
					}
				}
				break;
			case TOKEN_SEPERATOR:
				this.tokenSeperator = parseString(setting, value, false);
				break;
			case DECIMAL_SEPARATOR:
				this.decimalSeparator = parseString(setting, value, false);
				break;
			case TUPLE_SEPERATOR:
				this.tupleSeperator = parseString(setting, value, false);
				break;
			case SERVICE_PROVIDER_FILE:
				this.serviceProvider = parseFile(setting, value, true);
				break;
			case SERVICE_PROVIDER_NAME:
				this.serviceProviderName = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_SITE:
				this.serviceProviderSite = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_INDIVIDUAL_NAME:
				this.serviceProviderIndividualName = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_POSITION_NAME:
				this.serviceProviderPositionName = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_PHONE:
				this.serviceProviderPhone = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_ADDRESS:
				this.serviceProviderDeliveryPoint = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_CITY:
				this.serviceProviderCity = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_ZIP:
				this.serviceProviderPostalCode = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_STATE:
				this.serviceProviderAdministrativeArea = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_COUNTRY:
				this.serviceProviderCountry = parseString(setting, value, true);
				break;
			case SERVICE_PROVIDER_EMAIL:
				this.serviceProviderMailAddress = parseString(setting, value, true);
				break;
			case SERVICE_IDENTIFICATION_FILE:
				this.serviceIdentification = parseFile(setting, value, true);
				break;
			case SERVICE_IDENTIFICATION_KEYWORDS:
				String keywords = parseString(setting, value, true);
				if (keywords != null) {
					this.serviceIdentificationKeywords = keywords.split(",");
				} else {
					this.serviceIdentificationKeywords = new String[0];
				}
				break;
			case SERVICE_IDENTIFICATION_SERVICE_TYPE:
				this.serviceIdentificationServiceType = parseString(setting, value, true);
				break;
			case SERVICE_IDENTIFICATION_TITLE:
				this.serviceIdentificationTitle = parseString(setting, value, true);
				break;
			case SERVICE_IDENTIFICATION_ABSTRACT:
				this.serviceIdentificationAbstract = parseString(setting, value, true);
				break;
			case SERVICE_IDENTIFICATION_FEES:
				this.serviceIdentificationFees = parseString(setting, value, true);
				break;
			case SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS:
				this.serviceIdentificationAccessConstraints = parseString(setting, value, true);
				break;
			case CONFIGURATION_FILES:
				String configFileMapString = parseString(setting, value, true);
				if (configFileMapString != null && !configFileMapString.isEmpty()) {
					for (String kvp : configFileMapString.split(";")) {
						String[] keyValue = kvp.split(" ");
						this.configFileMap.put(keyValue[0], keyValue[1]);
					}
				}
				break;
			case MINIMUM_GZIP_SIZE:
				this.minimumGzipSize = parseInteger(setting, value);
				break;
			default:
				String message = "Can not decode setting '" + setting.name() + "'!";
				LOGGER.error(message);
				throw new ConfigurationException(message);
		}
	}
	private File parseFile(Setting setting, String value, boolean canBeNull) throws ConfigurationException {
		if (value == null || value.isEmpty()) {
			return null;
		}
		String fileName = parseString(setting, value, canBeNull);
		if (fileName == null) {
			return null;
		}
		File f = new File (fileName);
		if (f.exists()) {
			return f;
		} else {
			f = new File(getBasePath() + fileName);
			if (f.exists()) {
				return f;
			} else {
				StringBuilder exceptionText = new StringBuilder();
				exceptionText.append("Can not find file '(").append(getBasePath()).append(")").append(fileName).append("'!");
				LOGGER.error(exceptionText.toString());
				throw new ConfigurationException(exceptionText.toString());
			}
		}
	}
	
	private int parseInteger(Setting setting, String value) throws ConfigurationException {
		Integer val = null;
		try {
			if (value != null && !value.isEmpty()) {
				val = Integer.valueOf(value);
			}
		} catch(NumberFormatException e) {}
		
		if (val == null) {
			String exceptionText =
					"'" + setting.name() + "' is not properly defined! Please set '" 
					    + setting.name() + "' property to an integer value!";
			LOGGER.error(exceptionText);
			throw new ConfigurationException(exceptionText);
		} else {
			return val.intValue();
		}
	}
	
	private boolean parseBoolean(Setting setting, String value) throws ConfigurationException {
		Boolean val = null;
		if (value != null && !value.isEmpty()) {
			val = value.equalsIgnoreCase("true") ? Boolean.TRUE : 
					value.equalsIgnoreCase("false") ? Boolean.FALSE : null;
		}
		if (val == null) {
			String exceptionText =
					"'" + setting.name() + "' is not properly defined! Please set '" 
					    + setting.name() + "' property to an boolean value!";
			LOGGER.error(exceptionText);
			throw new ConfigurationException(exceptionText);
		} else {
			return val.booleanValue();
		}
	}
	
	private String parseString(Setting setting, String value, boolean canBeEmpty) throws ConfigurationException {
		if (value == null || (value.isEmpty() && !canBeEmpty)) {
			String exceptionText = "String property '" + setting.name() + "' is not defined!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
		}
		return value;
	}
	
	private void validate() throws ConfigurationException {
		/* TODO assert that required fields or xml of service identification are present */
		/* TODO assert that required fields or xml of service provider are present */
	}
	
	public Properties getConnectionProviderProperties() throws ConfigurationException {
		return this.connectionProviderProperties;
	}
	
    /**
     * Initialize this class. Since this initialization is not done in the
     * constructor, dependent classes can use the SosConfigurator already when
     * called from here.
     */
    private void initialize() throws ConfigurationException {
		
		/* do this first as we need access to the database */
		initializeConnectionProvider();
		
		Iterator<ISettingsDao> i = ServiceLoader.load(ISettingsDao.class).iterator();
		if (!i.hasNext()) {
			throw new ConfigurationException("No ISettingsDao implementation is present");
		}
		ISettingsDao settingsDao = i.next();
		
		Map<String, String> settings;
		try {
			settings = settingsDao.get();
		} catch (SQLException ex) {
			throw new ConfigurationException("Can not load settings from database", ex);
		}
		
		/* set settings */
		for (Setting setting : Setting.values()) {
			String value = settings.get(setting.name());
			if (value == null) {
				LOGGER.warn("Setting {} is not present.", setting.name());
			}
			setSetting(setting, value, true);
		}
		
		validate();

        initializeOperationDAOs();
        initializeServiceOperators();
        initalizeFeatureQueryHandler();
        initalizeCacheFeederDAO();
        initalizeDecoder();
        initalizeEncoder();
        initializeRequestOperators();
        initializeBindingOperator();
        initializeAdminServiceOperator();
        initializeAdminRequestOperator();
        initializeDataSource();
        initializeCapabilitiesCacheController();
        // TODO: what?
        initializeTasking();
    }

    /**
     * Eventually cleanup everything created by the constructor
     */
    public synchronized void cleanup() {
        if (connectionProvider != null) {
            connectionProvider.cleanup();
        }
        if (capabilitiesCacheController != null) {
            capabilitiesCacheController.cancel();
            capabilitiesCacheController = null;
        }
        if (taskingExecutor != null) {
            taskingExecutor.cancel();
            taskingExecutor = null;
        }
		instance = null;
    }

    /**
	 * @param config 
	 * @param basepath 
	 * @return Returns an instance of the SosConfigurator. This method is used
     *         to implement the singelton pattern
     * 
	 * @throws ConfigurationException if the initialization failed
     */
    public static synchronized Configurator getInstance(Properties config, String basepath)
            throws ConfigurationException {
        if (instance == null) {
			try {
				instance = new Configurator(config, basepath);
				instance.initialize();
			} catch(RuntimeException t) {
				if (instance != null) {
					instance.cleanup();
					instance = null;
				}
				throw t;
			} catch(ConfigurationException t) {
				if (instance != null) {
					instance.cleanup();
					instance = null;
				}
				throw t;
			}
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
     * reads the requestListeners from the configFile and returns a
     * RequestOperator containing the requestListeners
     * 
     * @return RequestOperators with requestListeners
     * @throws OwsExceptionReport
     *             if initialization of a RequestListener failed
     */
    private void initializeAdminServiceOperator() throws ConfigurationException {
        serviceLoaderAdminServiceOperator = ServiceLoader.load(IAdminServiceOperator.class);
        Iterator<IAdminServiceOperator> iter = serviceLoaderAdminServiceOperator.iterator();
        try {
            this.adminServiceOperator = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("An IAdminServiceOperator implementation could not be loaded!", sce);
        }
        if (this.adminServiceOperator == null) {
            String exceptionText = "No IAdminServiceOperator implementation is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
        LOGGER.info("\n******\n IAdminServiceOperator loaded successfully!\n******\n");
    }
    
    private void initializeAdminRequestOperator() throws ConfigurationException {
        serviceLoaderAdminRequesteOperator = ServiceLoader.load(IAdminRequestOperator.class);
        Iterator<IAdminRequestOperator> iter = serviceLoaderAdminRequesteOperator.iterator();
        while (iter.hasNext()) {
            try {
                IAdminRequestOperator adminRequestOperator = iter.next();
                adminRequestOperators.put(adminRequestOperator.getKey(), adminRequestOperator);
            } catch (ServiceConfigurationError sce) {
                // TODO add more details like which class with qualified name
                // failed to load
                LOGGER.warn(
                        "An IAdminRequestOperator implementation could not be loaded! Exception message: "
                                + sce.getLocalizedMessage(), sce);
            }
        }
        if (this.bindingOperators.isEmpty()) {
            StringBuilder exceptionText = new StringBuilder(); 
            exceptionText.append("No IAdminRequestOperator implementation could be loaded!");
            exceptionText.append(" If the SOS is not used as webapp, this has no effect!");
            exceptionText.append(" Else add a IAdminRequestOperator implementation!");
            LOGGER.warn(exceptionText.toString());
        }
        LOGGER.info("\n******\n IAdminRequestOperator(s) loaded successfully!\n******\n");
    }

    /**
     * reads the requestListeners from the configFile and returns a
     * RequestOperator containing the requestListeners
     * 
     * @return RequestOperators with requestListeners
     * @throws OwsExceptionReport
     *             if initialization of a RequestListener failed
     */
    private void initializeBindingOperator() throws ConfigurationException {
        serviceLoaderBindingOperator = ServiceLoader.load(Binding.class);
        setBindings();
        LOGGER.info("\n******\n Binding(s) loaded successfully!\n******\n");
    }

    /**
     * Load implemented cache feeder dao
     * 
     * @throws OwsExceptionReport
     *             If no cache feeder dao is implemented
     */
    private void initalizeCacheFeederDAO() throws ConfigurationException {
        serviceLoaderCacheFeederDAO = ServiceLoader.load(ICacheFeederDAO.class);
        setCacheFeederDAO();
        LOGGER.info("\n******\n CacheFeederDAO loaded successfully!\n******\n");
    }

    /**
     * intializes the CapabilitiesCache
     * 
     * @throws OwsExceptionReport
     *             if initializing the CapabilitiesCache failed
     */
    private void initializeCapabilitiesCacheController() throws ConfigurationException {
        serviceLoaderCapabilitiesCacheController = ServiceLoader.load(ACapabilitiesCacheController.class);
        Iterator<ACapabilitiesCacheController> iter = serviceLoaderCapabilitiesCacheController.iterator();
        try {
            this.capabilitiesCacheController = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("An ACapabilitiesCacheController implementation could not be loaded!", sce);
        }
        if (this.capabilitiesCacheController == null) {
            String exceptionText = "No ACapabilitiesCacheController implementation is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
        LOGGER.info("\n******\n ACapabilitiesCacheController loaded successfully!\n******\n");
        try {
            this.capabilitiesCacheController.update(false);
        } catch (OwsExceptionReport owse) {
            LOGGER.error("Fatal error: Couldn't initialize capabilities cache!");
            throw new ConfigurationException(owse);
        }
    }
	
    /**
     * Load the connection provider implementation
     * 
     * @throws OwsExceptionReport
     *             If no connection provider is implemented
     */
    private void initializeConnectionProvider() throws ConfigurationException {
        Iterator<IConnectionProvider> iter = ServiceLoader.load(IConnectionProvider.class).iterator();
        try {
            this.connectionProvider = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("No IConnectionProvider implementation could be loaded!", sce);
        }
        if (this.connectionProvider == null) {
            String exceptionText = "No IConnectionProvider implementation is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
		this.connectionProvider.initialize(getConnectionProviderProperties());
        LOGGER.info("\n******\n ConnectionProvider loaded successfully!\n******\n");
    }

    private void initializeDataSource() throws ConfigurationException {
        serviceLoaderDataSourceInitializator = ServiceLoader.load(IDataSourceInitializator.class);
        Iterator<IDataSourceInitializator> iter = serviceLoaderDataSourceInitializator.iterator();
        try {
            this.dataSourceInitializator = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("An IDataSourceInitializator implementation could not be loaded!", sce);
        }
        if (this.dataSourceInitializator == null) {
            String exceptionText = "No IDataSourceInitializator implementation is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
        LOGGER.info("\n******\n IDataSourceInitializator loaded successfully!\n******\n");
        try {
            this.dataSourceInitializator.initializeDataSource();
        } catch (OwsExceptionReport owse) {
            throw new ConfigurationException(owse);
        }
    }

    private void initalizeDecoder() throws ConfigurationException {
        serviceLoaderDecoder = ServiceLoader.load(IDecoder.class);
        setDecoder();
        LOGGER.info("\n******\n Decoder(s) loaded successfully!\n******\n");
    }

    private void initalizeEncoder() throws ConfigurationException {
        serviceLoaderEncoder = ServiceLoader.load(IEncoder.class);
        setEncoder();
        LOGGER.info("\n******\n Encoder(s) loaded successfully!\n******\n");
    }

    /**
     * Load implemented feature query handler
     * 
     * @throws OwsExceptionReport
     *             If no feature query handler is implemented
     */
    private void initalizeFeatureQueryHandler() throws ConfigurationException {
        serviceLoaderFeatureQueryHandler = ServiceLoader.load(IFeatureQueryHandler.class);
        setFeatureQueryHandler();
        LOGGER.info("\n******\n FeatureQueryHandler loaded successfully!\n******\n");
    }

    /**
     * Load implemented operation dao
     * 
     * @throws OwsExceptionReport
     *             If no operation dao is implemented
     */
    private void initializeOperationDAOs() throws ConfigurationException {
        serviceLoaderOperationDAOs = ServiceLoader.load(IOperationDAO.class);
        setOperationDAOs();
        LOGGER.info("\n******\n OperationDAO(s) loaded successfully!\n******\n");
    }

    /**
     * Load implemented request listener
     * 
     * @throws OwsExceptionReport
     *             If no request listener is implemented
     */
    private void initializeServiceOperators() throws ConfigurationException {
        serviceLoaderServiceOperators = ServiceLoader.load(IServiceOperator.class);
        setServiceOperatorMap();
        LOGGER.info("\n******\n ServiceOperator(s) loaded successfully!\n******\n");
    }

    private void initializeRequestOperators() throws ConfigurationException {
        serviceLoaderRequestOperators = ServiceLoader.load(IRequestOperator.class);
        setRequestOperatorMap();
        LOGGER.info("\n******\n RequestOperator(s) loaded successfully!\n******\n");
    }

    private void initializeTasking() {
        serviceLoaderTasking = ServiceLoader.load(ASosTasking.class);
        Iterator<ASosTasking> iterator = serviceLoaderTasking.iterator();
        if (iterator.hasNext()) {
            taskingExecutor = new Timer("TaskingTimer");
            long delayCounter = 0;
            // List<ASosTasking> tasks = new ArrayList<ASosTasking>();
            while (iterator.hasNext()) {
                try {
                    ASosTasking aSosTasking = iterator.next();
                    taskingExecutor.scheduleAtFixedRate(aSosTasking, delayCounter,
                            (aSosTasking.getExecutionIntervall() * 60000));
                    delayCounter += 60000;
                    LOGGER.debug("The task '{}' is started!", aSosTasking.getName());
                } catch (Exception e) {
                    LOGGER.error("Error while starting task", e);
                }
                // tasks.add((ASosTasking) iterator.next());

            }
            // taskingExecutor = new Timer("TaskingTimer");
            // LOGGER.debug("TaskingExecutor initialized with size {}!",
            // tasks.size());
            // long delayCounter = 0;
            // for (ASosTasking aSosTasking : tasks) {
            // taskingExecutor.scheduleAtFixedRate(aSosTasking,
            // (delayCounter+60000),
            // (aSosTasking.getExecutionIntervall()*60000));
            // LOGGER.debug("The task '{}' is started!", aSosTasking.getName());
            // }
            LOGGER.info("\n******\n Task(s) loaded and started successfully!\n******\n");
        }
    }

    /**
     * Load the implemented cache feeder dao and add them to a map with
     * operation name as key
     * 
     * @throws OwsExceptionReport
     *             If no cache feeder dao is implemented
     */
    private void setCacheFeederDAO() throws ConfigurationException {
        Iterator<ICacheFeederDAO> iter = serviceLoaderCacheFeederDAO.iterator();
        try {
            this.cacheFeederDAO = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("An ICacheFeederDAO implementation could not be loaded!", sce);
        }
        if (this.cacheFeederDAO == null) {
            String exceptionText = "No ICacheFeederDAO implementations is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
    }
    
    private void setBindings() throws ConfigurationException {
		for (Binding iBindingOperator : serviceLoaderBindingOperator) {
            try {
                bindingOperators.put(iBindingOperator.getUrlPattern(), iBindingOperator);
            } catch (ServiceConfigurationError sce) {
                // TODO add more details like which class with qualified name
                // failed to load
                LOGGER.warn(
                        "An Binding implementation could not be loaded! Exception message: "
                                + sce.getLocalizedMessage(), sce);
            }
        }
        if (this.bindingOperators.isEmpty()) {
            StringBuilder exceptionText = new StringBuilder(); 
            exceptionText.append("No Binding implementation could be loaded!");
            exceptionText.append(" If the SOS is not used as webapp, this has no effect!");
            exceptionText.append(" Else add a Binding implementation!");
            LOGGER.warn(exceptionText.toString());
        }
    }

    private void setDecoder() throws ConfigurationException {
		for (IDecoder<?, ?> aDecoder : serviceLoaderDecoder) {
            try {
                for (DecoderKeyType decoderKeyType : aDecoder.getDecoderKeyTypes()) {
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
            throw new ConfigurationException(exceptionText);
        }
    }

    private void setEncoder() throws ConfigurationException {
		for (IEncoder<?,?> aEncoder : serviceLoaderEncoder) {
            try {
                for (EncoderKeyType encoderKeyType : aEncoder.getEncoderKeyType()) {
                    encoder.put(encoderKeyType, aEncoder);
                }
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IEncoder implementation could not be loaded!", sce);
            }
        }
        if (this.encoder.isEmpty()) {
            String exceptionText = "No IEncoder implementations is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
        /* reinitialize XmlOptionHelper to get the correct prefixes */
        if (getCharacterEncoding() != null) {
            XmlOptionsHelper.getInstance(getCharacterEncoding(), true);
        }
    }

    /**
     * Load the implemented feature query handler and add them to a map with
     * operation name as key
     * 
     * @throws OwsExceptionReport
     *             If no feature query handler is implemented
     */
    private void setFeatureQueryHandler() throws ConfigurationException {
        Iterator<IFeatureQueryHandler> iter = serviceLoaderFeatureQueryHandler.iterator();
        try {
            this.featureQueryHandler = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("No IFeatureQueryHandler implementation could be loaded!", sce);
        }
        if (this.featureQueryHandler == null) {
            String exceptionText = "No IFeatureQueryHandler implementations is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
    }

    /**
     * Load the implemented operation dao and add them to a map with operation
     * name as key
     * 
     * @throws OwsExceptionReport
     *             If no operation dao is implemented
     */
    private void setOperationDAOs() throws ConfigurationException {
        Iterator<IOperationDAO> iter = serviceLoaderOperationDAOs.iterator();
        while (iter.hasNext()) {
            try {
                IOperationDAO aOperationDAO = iter.next();
                operationDAOs.put(aOperationDAO.getOperationName(), aOperationDAO);
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IOperationDAO implementation could not be loaded!", sce);
            }
        }
        if (this.operationDAOs.isEmpty()) {
            String exceptionText = "No IOperationDAO implementations is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
    }

    private void setRequestOperatorMap() throws ConfigurationException {
		for (IRequestOperator aRequestOperator : serviceLoaderRequestOperators) {
            try {
                requestOperators.put(aRequestOperator.getRequestOperatorKeyType(), aRequestOperator);
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IRequestOperator implementation could not be loaded!", sce);
            }
        }
        if (this.encoder.isEmpty()) {
            String exceptionText = "No IRequestOperator implementation is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }

    }

    /**
     * Load the implemented request listener and add them to a map with
     * operation name as key
     * 
     * @throws OwsExceptionReport
     *             If no request listener is implemented
     */
    private void setServiceOperatorMap() throws ConfigurationException {
		for (IServiceOperator iServiceOperator : serviceLoaderServiceOperators) {
            try {
                serviceOperators.put(iServiceOperator.getServiceOperatorKeyType(), iServiceOperator);
                supportedVersions.add(iServiceOperator.getServiceOperatorKeyType().getVersion());
                supportedServices.add(iServiceOperator.getServiceOperatorKeyType().getService());
            } catch (ServiceConfigurationError sce) {
                LOGGER.warn("An IServiceOperator implementation could not be loaded!", sce);
            }
        }
        if (this.serviceOperators.isEmpty()) {
            String exceptionText = "No IServiceOperator implementations is loaded!";
            LOGGER.error(exceptionText);
            throw new ConfigurationException(exceptionText);
        }
    }

    public void updateBindigs() throws ConfigurationException {
        bindingOperators.clear();
        serviceLoaderBindingOperator.reload();
        setBindings();
        LOGGER.info("\n******\n Binding(s) re-initialized successfully!\n******\n");
    }

    public void updateDecoder() throws ConfigurationException {
        decoder.clear();
        serviceLoaderDecoder.reload();
        setDecoder();
        LOGGER.info("\n******\n Decoder(s) re-initialized successfully!\n******\n");
    }

    public void updateEncoder() throws ConfigurationException {
        encoder.clear();
        serviceLoaderEncoder.reload();
        setEncoder();
        LOGGER.info("\n******\n Encoder(s) re-initialized successfully!\n******\n");
    }

    /**
     * Update/reload the implemented operation dao
     * 
     * @throws ConfigurationException
     *             If no operation dao is implemented
     */
    public void updateOperationDAOs() throws ConfigurationException {
        operationDAOs.clear();
        serviceLoaderOperationDAOs.reload();
        setOperationDAOs();
        LOGGER.info("\n******\n OperationDAO(s) re-initialized successfully!\n******\n");
    }

    public void updateRequestOperator() throws ConfigurationException {
        updateOperationDAOs();
        requestOperators.clear();
        serviceLoaderRequestOperators.reload();
        setRequestOperatorMap();
        LOGGER.info("\n******\n RequestOperator(s) re-initialized successfully!\n******\n");
    }

    /**
     * Update/reload the implemented request listener
     * 
     * @throws ConfigurationException
     *             If no request listener is implemented
     */
    public void updateServiceOperators() throws ConfigurationException {
        updateRequestOperator();
        serviceOperators.clear();
        serviceLoaderServiceOperators.reload();
        setServiceOperatorMap();
        LOGGER.info("\n******\n ServiceOperator(s) re-initialized successfully!\n******\n");
    }

    /**
	 * @return Returns the service identification
	 * @throws OwsExceptionReport  
     */
    public SosServiceIdentification getServiceIdentification() throws OwsExceptionReport {
		SosServiceIdentification sosServiceIdentification = new SosServiceIdentification();
		if (this.serviceIdentification != null) {
			sosServiceIdentification.setServiceIdentification(XmlHelper.loadXmlDocumentFromFile(this.serviceIdentification));
		}
		sosServiceIdentification.setAbstract(this.serviceIdentificationAbstract);
		sosServiceIdentification.setAccessConstraints(this.serviceIdentificationAccessConstraints);
		sosServiceIdentification.setFees(this.serviceIdentificationFees);
		sosServiceIdentification.setServiceType(this.serviceIdentificationServiceType);
		sosServiceIdentification.setTitle(this.serviceIdentificationTitle);
		sosServiceIdentification.setVersions(this.getSupportedVersions());
		sosServiceIdentification.setKeywords(Arrays.asList(this.serviceIdentificationKeywords));
        return sosServiceIdentification;
    }

    /**
	 * @return Returns the service provider
	 * @throws OwsExceptionReport  
     */
    public SosServiceProvider getServiceProvider() throws OwsExceptionReport {
		SosServiceProvider sosServiceProvider = new SosServiceProvider();
		if (this.serviceProvider != null) {
			sosServiceProvider.setServiceProvider(XmlHelper.loadXmlDocumentFromFile(this.serviceProvider));
		}
		sosServiceProvider.setAdministrativeArea(this.serviceProviderAdministrativeArea);
		sosServiceProvider.setCity(this.serviceProviderCity);
		sosServiceProvider.setCountry(this.serviceProviderCountry);
		sosServiceProvider.setDeliveryPoint(this.serviceProviderDeliveryPoint);
		sosServiceProvider.setIndividualName(this.serviceProviderIndividualName);
		sosServiceProvider.setMailAddress(this.serviceProviderMailAddress);
		sosServiceProvider.setName(this.serviceProviderName);
		sosServiceProvider.setPhone(this.serviceProviderPhone);
		sosServiceProvider.setPositionName(this.serviceProviderPositionName);
		sosServiceProvider.setPostalCode(this.serviceProviderPostalCode);
		sosServiceProvider.setSite(this.serviceProviderSite);
        return sosServiceProvider;
    }

    /**
     * @return Returns the sensor description directory
     */
    public File getSensorDir() {
        return sensorDir;
    }

    /**
     * @return the supportedVersions
     */
    public Set<String> getSupportedVersions() {
        return Collections.unmodifiableSet(supportedVersions);
    }

    public boolean isVersionSupported(String version) {
        return supportedVersions.contains(version);
    }
    
    /**
     * @return the supportedVersions
     */
    public Set<String> getSupportedServices() {
        return supportedServices;
    }

    public boolean isServiceSupported(String service) {
        return supportedServices.contains(service);
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
	 * @return the minimum threshold for gzipping responses
	 */
	public int getMinimumGzipSize() {
		return minimumGzipSize;
	}

    /**
     * @return Returns the tupleSeperator.
     */
    public String getTupleSeperator() {
        return tupleSeperator;
    }

    /**
	 * @return Returns decimal separator.
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
    public ACapabilitiesCacheController getCapabilitiesCacheController() {
        return capabilitiesCacheController;
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
        String url;
        if (serviceURL.contains("?")) {
            String[] split = serviceURL.split("[?]");
            url = split[0];
        } else {
            url = serviceURL;
        }
        this.serviceURL = url;
    }

    public String getDefaultOfferingPrefix() {
        return defaultOfferingPrefix;
    }

    public String getDefaultProcedurePrefix() {
        return defaultProcedurePrefix;
    }

    /**
     * @return the configFileMap
     */
    public Map<String, String> getConfigFileMap() {
        return Collections.unmodifiableMap(configFileMap);
    }

    /**
	 * @param service 
	 * @param version 
	 * @return the implemented request listener
     * @throws OwsExceptionReport
     */
    public IServiceOperator getServiceOperator(String service, String version) throws OwsExceptionReport {
        return getServiceOperator(new ServiceOperatorKeyType(service, version));
    }

    public IServiceOperator getServiceOperator(ServiceOperatorKeyType serviceOperatorIdentifier)
            throws OwsExceptionReport {
        return serviceOperators.get(serviceOperatorIdentifier);
        // if (serviceOperator != null) {
        // return serviceOperator;
        // }
        // String exceptionText =
        // "The service (" + serviceOperatorIdentifier.getService() +
        // ") and/or version ("
        // + serviceOperatorIdentifier.getVersion() +
        // ") is not supported by this server!";
        // LOGGER.debug(exceptionText);
        // throw Util4Exceptions.createNoApplicableCodeException(null,
        // exceptionText);
    }

    /**
     * @return the implemented request listener
     * @throws OwsExceptionReport
     */
    public Map<ServiceOperatorKeyType, IServiceOperator> getServiceOperators() throws OwsExceptionReport {
        return Collections.unmodifiableMap(serviceOperators);
    }

    /**
     * @return the implemented operation DAOs
     */
    public Map<String, IOperationDAO> getOperationDAOs() {
        return Collections.unmodifiableMap(operationDAOs);
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
    public IAdminServiceOperator getAdminServiceOperator() {
        return adminServiceOperator;
    }

    public int getDefaultEPSG() {
        return defaultEPSG;
    }

    /**
	 * @param namespace 
	 * @return the decoder
     * @throws OwsExceptionReport
     */
    public List<IDecoder> getDecoder(String namespace) throws OwsExceptionReport {
        return getDecoder(new DecoderKeyType(namespace));
    }

    /**
	 * @param service 
	 * @param version 
	 * @return the decoder
     * @throws OwsExceptionReport
     */
    public List<IDecoder> getDecoder(String service, String version) throws OwsExceptionReport {
        return getDecoder(new DecoderKeyType(service, version));
    }

    /**
	 * @param decoderKeyType 
	 * @return the decoder
     * @throws OwsExceptionReport
     */
    public List<IDecoder> getDecoder(DecoderKeyType decoderKeyType) throws OwsExceptionReport {
        return decoder.get(decoderKeyType);
    }

    /**
	 * @param namespace 
	 * @return the encoder
     * @throws OwsExceptionReport
     */
    public IEncoder getEncoder(String namespace) throws OwsExceptionReport {
        return encoder.get(new EncoderKeyType(namespace));
        // if (iEncoder == null) {
        // String exceptionText =
        // "No encoder implementation is available for the namespace '" +
        // namespace + "'!";
        // LOGGER.debug(exceptionText);
        // throw Util4Exceptions.createNoApplicableCodeException(null,
        // exceptionText);
        // }
        // return iEncoder;
    }

    public Binding getBindingOperator(String urlPattern) {
        return bindingOperators.get(urlPattern);
    }

    public IRequestOperator getRequestOperator(ServiceOperatorKeyType serviceOperatorKeyType, String operationName) {
        return requestOperators.get(new RequestOperatorKeyType(serviceOperatorKeyType, operationName));
    }

    public Map<RequestOperatorKeyType, IRequestOperator> getRequestOperator() {
        return Collections.unmodifiableMap(requestOperators);
    }

    public Map<String, Binding> getBindingOperators() {
        return Collections.unmodifiableMap(bindingOperators);
    }

    public Map<DecoderKeyType, List<IDecoder>> getDecoderMap() {
        return Collections.unmodifiableMap(decoder);
    }

    public Map<EncoderKeyType, IEncoder> getEncoderMap() {
        return Collections.unmodifiableMap(encoder);
    }

    public IAdminRequestOperator getAdminRequestOperator(String key) {
        return adminRequestOperators.get(key);
    }

    public void updateConfiguration() throws ConfigurationException {
        updateBindigs();
        updateOperationDAOs();
        updateDecoder();
        updateEncoder();
        updateServiceOperators();
        updateRequestOperator();
    }
}
