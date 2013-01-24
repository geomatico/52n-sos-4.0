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
package org.n52.sos.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.binding.BindingRepository;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.convert.ConverterRepository;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IDataSourceInitializator;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.ISettingsDao;
import org.n52.sos.ds.OperationDaoRepository;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceIdentificationFactory;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.ows.SosServiceProviderFactory;
import org.n52.sos.ogc.sos.Range;
import org.n52.sos.request.operator.RequestOperatorRepository;
import org.n52.sos.service.admin.operator.IAdminServiceOperator;
import org.n52.sos.service.admin.request.operator.AdminRequestOperatorRepository;
import org.n52.sos.service.operator.ServiceOperatorRepository;
import org.n52.sos.service.profile.DefaultProfileHandler;
import org.n52.sos.service.profile.IProfile;
import org.n52.sos.service.profile.IProfileHandler;
import org.n52.sos.tasking.Tasking;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.SettingsHelper;
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

    /**
     * se Implementation of ICacheFeederDAO
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

    /**
     * default EPSG code of stored geometries
     */
    private int defaultEPSG;

    /**ad
     * default offering identifier prefix, used for auto generation
     */
    private String defaultOfferingPrefix;

    /**
     * default procedure identifier prefix, used for auto generation
     */
    private String defaultProcedurePrefix;

    /** decimal separator for result element */
    private String decimalSeparator;

    /**
     * Implementation of IFeatureQueryHandler
     */
    private IFeatureQueryHandler featureQueryHandler;

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
    
    private IProfileHandler profileHandler;
    
    private ServiceLoader<IProfileHandler> serviceLoaderProfileHandler;
    
    /** directory of sensor descriptions in SensorML format */
    private File sensorDir;

    private ServiceLoader<IAdminServiceOperator> serviceLoaderAdminServiceOperator;

    /** ServiceLoader for ICacheFeederDAO */
    private ServiceLoader<ICacheFeederDAO> serviceLoaderCacheFeederDAO;

    /** ServiceLoader for ACapabilitiesCacheController */
    private ServiceLoader<ACapabilitiesCacheController> serviceLoaderCapabilitiesCacheController;

    private ServiceLoader<IDataSourceInitializator> serviceLoaderDataSourceInitializator;

    /** ServiceLoader for IFeatureQueryHandler */
    private ServiceLoader<IFeatureQueryHandler> serviceLoaderFeatureQueryHandler;

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
     * Implementation of ASosAdminRequestOperator
     */
    private IAdminServiceOperator adminServiceOperator;

    private CodingRepository codingRepository;
	private ServiceOperatorRepository serviceOperatorRepository;
	private OperationDaoRepository operationDaoRepository;
	private RequestOperatorRepository requestOperatorRepository;
	private BindingRepository bindingRepository;
	private ConverterRepository converterRepository;
	private AdminRequestOperatorRepository adminRequestOperatorRepository;
	private SosServiceIdentificationFactory serviceIdentificationFactory;
    private SosServiceProviderFactory serviceProviderFactory;
	private Tasking tasking;

    /**
	 * Defines the number of threads available in the thread pool of the cache
	 * update executor service.
	 */
    private int cacheThreadCount = 5;


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

    /** boolean indicates the order of x and y components of coordinates */
    private List<Range> epsgsWithReversedAxisOrder;

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
        LOGGER.info("Configurator initialized: [basepath={}]",
                this.basepath, this.connectionProviderProperties);
    }

    public void changeSetting(Setting setting, String newValue) throws ConfigurationException {
        setSetting(setting, newValue, false);
    }

    private void setSetting(Setting setting, String value, boolean initial) throws ConfigurationException {
        /* TODO check what has to be re-initialized when settings change */
        switch (setting) {
        case CAPABILITIES_CACHE_UPDATE_INTERVAL:
            int capCacheUpdateIntervall = SettingsHelper.parseInteger(setting, value);
            if (this.updateIntervall != capCacheUpdateIntervall) {
                this.updateIntervall = capCacheUpdateIntervall;
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
        case CACHE_THREAD_COUNT:
            this.cacheThreadCount = SettingsHelper.parseInteger(setting, value);
            break;
        case CONFIGURATION_FILES:
            String configFileMapString = SettingsHelper.parseString(setting, value, true);
            if (configFileMapString != null && !configFileMapString.isEmpty()) {
                for (String kvp : configFileMapString.split(";")) {
                    String[] keyValue = kvp.split(" ");
                    this.configFileMap.put(keyValue[0], keyValue[1]);
                }
            }
            break;
        case CHILD_PROCEDURES_ENCODED_IN_PARENTS_DESCRIBE_SENSOR:
            this.childProceduresEncodedInParentsDescribeSensor = SettingsHelper.parseBoolean(setting, value);
            break;
        case DEFAULT_OFFERING_PREFIX:
            this.defaultOfferingPrefix = SettingsHelper.parseString(setting, value, true);
            break;
        case DEFAULT_PROCEDURE_PREFIX:
            this.defaultProcedurePrefix = SettingsHelper.parseString(setting, value, true);
            break;
        case FOI_LISTED_IN_OFFERINGS:
            this.foiListedInOfferings = SettingsHelper.parseBoolean(setting, value);
            break;
        case GML_DATE_FORMAT:
            this.gmlDateFormat = SettingsHelper.parseString(setting, value, true);
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
            this.maxGetObsResults = SettingsHelper.parseInteger(setting, value);
            break;
        case NO_DATA_VALUE:
            this.noDataValue = SettingsHelper.parseString(setting, value, false);
            break;
        case SENSOR_DIRECTORY:
            this.sensorDir = SettingsHelper.parseFile(setting, value, true);
            break;
        case SHOW_FULL_OPERATIONS_METADATA:
            this.showFullOperationsMetadata = SettingsHelper.parseBoolean(setting, value);
            break;
        case SHOW_FULL_OPERATIONS_METADATA_FOR_OBSERVATIONS:
            this.showFullOperationsMetadata4Observations = SettingsHelper.parseBoolean(setting, value);
            break;
        case SKIP_DUPLICATE_OBSERVATIONS:
            this.skipDuplicateObservations = SettingsHelper.parseBoolean(setting, value);
            break;
        case SOS_URL:
            setServiceURL(SettingsHelper.parseString(setting, value, false));
            break;
        case SUPPORTS_QUALITY:
            this.supportsQuality = SettingsHelper.parseBoolean(setting, value);
            break;
        case DEFAULT_EPSG:
            this.defaultEPSG = SettingsHelper.parseInteger(setting, value);
            break;
        case SRS_NAME_PREFIX_SOS_V1:
            String srsPrefixV1 = SettingsHelper.parseString(setting, value, true);
            if (!srsPrefixV1.endsWith(":") && srsPrefixV1.length() != 0) {
                srsPrefixV1 += ":";
            }
            this.srsNamePrefix = srsPrefixV1;
            break;
        case SRS_NAME_PREFIX_SOS_V2:
            String srsPrefixV2 = SettingsHelper.parseString(setting, value, true);
            if (!srsPrefixV2.endsWith("/") && srsPrefixV2.length() != 0) {
                srsPrefixV2 += "/";
            }
            this.srsNamePrefixSosV2 = srsPrefixV2;
            break;
        case SWITCH_COORDINATES_FOR_EPSG_CODES:
            String[] switchCoordinatesForEPSGStrings = SettingsHelper.parseString(setting, value, true).split(";");
            this.epsgsWithReversedAxisOrder = new ArrayList<Range>(switchCoordinatesForEPSGStrings.length);
            for (String switchCoordinatesForEPSGEntry : switchCoordinatesForEPSGStrings) {
                String[] splittedSwitchCoordinatesForEPSGEntry = switchCoordinatesForEPSGEntry.split("-");
                if (splittedSwitchCoordinatesForEPSGEntry.length == 1) {
                    Range r =
                            new Range(Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[0]),
                                    Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[0]));
                    epsgsWithReversedAxisOrder.add(r);
                } else if (splittedSwitchCoordinatesForEPSGEntry.length == 2) {
                    Range r =
                            new Range(Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[0]),
                                    Integer.parseInt(splittedSwitchCoordinatesForEPSGEntry[1]));
                    epsgsWithReversedAxisOrder.add(r);
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
            this.tokenSeperator = SettingsHelper.parseString(setting, value, false);
            break;
        case DECIMAL_SEPARATOR:
            this.decimalSeparator = SettingsHelper.parseString(setting, value, false);
            break;
        case TUPLE_SEPERATOR:
            this.tupleSeperator = SettingsHelper.parseString(setting, value, false);
            break;
        case SERVICE_PROVIDER_FILE:
			this.serviceProviderFactory.setFile(SettingsHelper.parseFile(setting, value, true));
            break;
        case SERVICE_PROVIDER_NAME:
            this.serviceProviderFactory.setName(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_SITE:
            this.serviceProviderFactory.setSite(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_INDIVIDUAL_NAME:
            this.serviceProviderFactory.setIndividualName(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_POSITION_NAME:
            this.serviceProviderFactory.setPositionName(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_PHONE:
            this.serviceProviderFactory.setPhone(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_ADDRESS:
            this.serviceProviderFactory.setDeliveryPoint(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_CITY:
            this.serviceProviderFactory.setCity(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_ZIP:
            this.serviceProviderFactory.setPostalCode(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_STATE:
            this.serviceProviderFactory.setAdministrativeArea(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_COUNTRY:
            this.serviceProviderFactory.setCountry(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_PROVIDER_EMAIL:
            this.serviceProviderFactory.setMailAddress(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_FILE:
            this.serviceIdentificationFactory.setFile(SettingsHelper.parseFile(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_KEYWORDS:
            this.serviceIdentificationFactory.setKeywords(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_SERVICE_TYPE:
            this.serviceIdentificationFactory.setServiceType(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_TITLE:
            this.serviceIdentificationFactory.setTitle(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_ABSTRACT:
            this.serviceIdentificationFactory.setAbstract(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_FEES:
            this.serviceIdentificationFactory.setFees(SettingsHelper.parseString(setting, value, true));
            break;
        case SERVICE_IDENTIFICATION_ACCESS_CONSTRAINTS:
            this.serviceIdentificationFactory.setConstraints(SettingsHelper.parseString(setting, value, true));
            break;
        case MINIMUM_GZIP_SIZE:
            this.minimumGzipSize = SettingsHelper.parseInteger(setting, value);
            break;
        default:
            String message = "Can not decode setting '" + setting.name() + "'!";
            LOGGER.error(message);
        }
    }

    /**
     * Initialize this class. Since this initialization is not done in the
     * constructor, dependent classes can use the SosConfigurator already when
     * called from here.
     */
    private void initialize() throws ConfigurationException {

    	LOGGER.info("\n******\n Configurator initialization started\n******\n");

        /* do this first as we need access to the database */
        initializeConnectionProvider();
		this.codingRepository = new CodingRepository();
		this.serviceIdentificationFactory = new SosServiceIdentificationFactory();
		this.serviceProviderFactory = new SosServiceProviderFactory();


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

        this.operationDaoRepository = new OperationDaoRepository();
        this.serviceOperatorRepository = new ServiceOperatorRepository();
        initalizeFeatureQueryHandler();
        initalizeCacheFeederDAO();
        this.converterRepository = new ConverterRepository();
        this.requestOperatorRepository = new RequestOperatorRepository();
		this.bindingRepository = new BindingRepository();
        initializeAdminServiceOperator();
		this.adminRequestOperatorRepository = new AdminRequestOperatorRepository();
        initializeDataSource();
        initializeCapabilitiesCacheController();
        this.tasking = new Tasking();
        initializeProfileHandler();
        LOGGER.info("\n******\n Configurator initialization finished\n******\n");
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
		if (this.tasking != null) {
			this.tasking.cancel();
		}
        instance = null;
    }

    /**
     * @param config
     * @param basepath
     * @return Returns an instance of the SosConfigurator. This method is used
     *         to implement the singelton pattern
     *
     * @throws ConfigurationException
     *             if the initialization failed
     */
    public static Configurator getInstance(Properties config, String basepath)
            throws ConfigurationException {
    	if (instance == null) {
    		boolean initialize = false;
        	INITIALIZE_LOCK.lock();
        	try {
        		if (instance == null) {
            		try {
            			instance = new Configurator(config, basepath);
            			initialize = true;
            		} catch (RuntimeException t) {
                        cleanUpAndThrow(t);
                    } catch (ConfigurationException t) {
                        cleanUpAndThrow(t);
                    }
        		}
        	} finally {
        		INITIALIZE_LOCK.unlock();
        	}
        	if (initialize) {
        		try {
                    instance.initialize();
                } catch (RuntimeException t) {
                    cleanUpAndThrow(t);
                } catch (ConfigurationException t) {
                    cleanUpAndThrow(t);
                }
    		}
    	}
        return instance;
    }

	private static void cleanUpAndThrow(ConfigurationException t) throws ConfigurationException {
		if (instance != null) {
			instance.cleanup();
			instance = null;
		}
		throw t;
	}

	private static void cleanUpAndThrow(RuntimeException t) {
		if (instance != null) {
			instance.cleanup();
			instance = null;
		}
		throw t;
	}

    private static final Lock INITIALIZE_LOCK = new ReentrantLock();

    /**
     * @return Returns the instance of the SosConfigurator. Null will be
     *         returned if the parameterized getInstance method was not invoked
     *         before. Usually this will be done in the SOS.
     */
    public static Configurator getInstance() {
    	INITIALIZE_LOCK.lock();
    	try {
    		return instance;
    	} finally {
    		INITIALIZE_LOCK.unlock();
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
        this.connectionProvider.initialize(this.connectionProviderProperties);
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

    private void initializeProfileHandler() {
        serviceLoaderProfileHandler = ServiceLoader.load(IProfileHandler.class);
        setProfileHandler();
        LOGGER.info("\n******\n ProfileHandler loaded successfully!\n******\n");
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
     * Load the implemented profile handler and add them to a map with
     * operation name as key
     *
     * @throws OwsExceptionReport
     *             If no profile handler is implemented
     */
    private void setProfileHandler() {
        Iterator<IProfileHandler> iter = serviceLoaderProfileHandler.iterator();
        try {
            this.profileHandler = iter.hasNext() ? iter.next() : null;
        } catch (ServiceConfigurationError sce) {
            LOGGER.warn("No IProfileHandler implementation could be loaded!", sce);
        }
        if (this.profileHandler == null) {
            this.profileHandler = new DefaultProfileHandler();
            LOGGER.info("No IProfileHandler implementations is loaded! DefaultHandler is used!");
            
        }
    }

    /**
     * @return Returns the service identification
     * @throws OwsExceptionReport
     */
    public SosServiceIdentification getServiceIdentification() throws OwsExceptionReport {
		return this.serviceIdentificationFactory.get();
    }

    /**
     * @return Returns the service provider
     * @throws OwsExceptionReport
     */
    public SosServiceProvider getServiceProvider() throws OwsExceptionReport {
        return this.serviceProviderFactory.get();
    }

    /**
     * @return Returns the sensor description directory
     */
    public File getSensorDir() {
        return sensorDir;
    }

    /**
     * @return maxGetObsResults
     */
    public int getMaxGetObsResults() {
        return maxGetObsResults;
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     */
    public int getLease() {
        return lease;
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
     * @param epsgCode
     * @return boolean indicating if coordinates have to be switched
     */
    public boolean reversedAxisOrderRequired(int epsgCode) {
        for (Range r : epsgsWithReversedAxisOrder) {
            if (r.contains(epsgCode)) {
                return true;
            }
        }
        return false;
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

    public int getCacheThreadCount() {
        return cacheThreadCount;
    }

    public void updateConfiguration() throws ConfigurationException {
		// TODO update converters
        updateBindings();
		updateOperationDaos();
        updateDecoder();
        updateEncoder();
		updateServiceOperators();
        updateRequestOperator();
    }
	public RequestOperatorRepository getRequestOperatorRepository() {
		return this.requestOperatorRepository;
	}

	public CodingRepository getCodingRepository() {
		return this.codingRepository;
	}

	public OperationDaoRepository getOperationDaoRepository() {
		return this.operationDaoRepository;
	}

	public ServiceOperatorRepository getServiceOperatorRepository() {
		return this.serviceOperatorRepository;
	}

	public BindingRepository getBindingRepository() {
		return this.bindingRepository;
	}

	public ConverterRepository getConverterRepository() {
		return this.converterRepository;
	}

	public AdminRequestOperatorRepository getAdminRequestOperatorRepository() {
		return this.adminRequestOperatorRepository;
	}

	public void updateDecoder() throws ConfigurationException {
		getCodingRepository().updateDecoders();
	}

	public void updateEncoder() throws ConfigurationException {
		getCodingRepository().updateEncoders();
	}

	public void updateOperationDaos() throws ConfigurationException {
		getOperationDaoRepository().update();
	}

	public void updateServiceOperators() throws ConfigurationException {
		getServiceOperatorRepository().update();
	}

	public void updateBindings() throws ConfigurationException {
		getBindingRepository().update();
	}

	public void updateConverter() throws ConfigurationException {
		getConverterRepository().update();
	}

	public void updateRequestOperator() throws ConfigurationException {
		getRequestOperatorRepository().update();
	}

        public IProfileHandler getProfileHandler() {
            return this.profileHandler;
        }
        
        public IProfile getActiveProfile() {
            return getProfileHandler().getActiveProfile();
    
        }

}
