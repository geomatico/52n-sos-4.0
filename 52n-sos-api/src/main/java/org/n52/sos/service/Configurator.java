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

import static org.n52.sos.service.MiscSettingDefinitions.*;
import static org.n52.sos.service.ServiceSettingDefinitions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.binding.BindingRepository;
import org.n52.sos.cache.ACapabilitiesCacheController;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.convert.ConverterRepository;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IDataSourceInitializator;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.OperationDAORepository;
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
import org.n52.sos.util.ConfiguringSingletonServiceLoader;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.IFactory;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO;
 * configures the logger.
 */
@Configurable
public class Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);
    /**
     * instance attribut, due to the singleton pattern.
     */
    private static Configurator instance = null;
    private static final Lock initLock = new ReentrantLock();
    /**
     * base path for configuration files.
     */
    private String basepath;
    /**
     * character encoding for responses.
     */
    private String characterEncoding;
    /**
     * Map with indicator and name of additional config files for modules.
     */
    private Map<String, String> configFileMap = new HashMap<String, String>(0);
    /**
     * default EPSG code of stored geometries.
     */
    private int defaultEPSG;
    /**
     * default offering identifier prefix, used for auto generation.
     */
    private String defaultOfferingPrefix;
    /**
     * default procedure identifier prefix, used for auto generation.
     */
    private String defaultProcedurePrefix;
    /**
     * date format of gml.
     */
    private String gmlDateFormat;
    /**
     * lease for getResult template in minutes.
     */
    private int lease;
    /**
     * maximum number of GetObservation results.
     */
    private int maxGetObsResults;
    /**
     * minimum size to gzip responses.
     */
    private int minimumGzipSize;
    /**
     * URL of this service.
     */
    private String serviceURL;
    /**
     * boolean, indicates if duplicate observation should be silently ignored
     * during insertion If set to false, duplicate observations trigger an
     * exception.
     */
    private boolean skipDuplicateObservations = false;
    /**
     * directory of sensor descriptions in SensorML format.
     */
    private File sensorDir;
    /**
     * Defines the number of threads available in the thread pool of the cache
     * update executor service.
     */
    private int cacheThreadCount = 5;
    /**
     * Prefix URN for the spatial reference system.
     */
    private String srsNamePrefix;
    /**
     * prefix URN for the spatial reference system.
     */
    private String srsNamePrefixSosV2;
    /**
     * boolean indicates, whether SOS supports quality information in
     * observations.
     */
    private boolean supportsQuality = true;
    /**
     * boolean indicates the order of x and y components of coordinates.
     */
    private List<Range> epsgsWithReversedAxisOrder;
    /**
     * token seperator for result element.
     */
    private String tokenSeperator;
    /**
     * tuple seperator for result element.
     */
    private String tupleSeperator;
    /**
     * decimal separator for result element.
     */
    private String decimalSeparator;
    /**
     * update interval for capabilities cache.
     */
    private long capabiltiesCacheUpdateInterval;
    private Properties connectionProviderProperties;
    /**
     * Implementation of IFeatureQueryHandler.
     */
    private IFeatureQueryHandler featureQueryHandler;
    /**
     * Implementation of IConnectionProvider.
     */
    private IConnectionProvider connectionProvider;
    /**
     * Implementation of
     * <code>IDataSourceInitializator</code>.
     */
    private IDataSourceInitializator dataSourceInitializator;
    /**
     * Capabilities Cache Controller.
     */
    private ACapabilitiesCacheController capabilitiesCacheController;
    /**
     * Implementation of ICacheFeederDAO.
     */
    private ICacheFeederDAO cacheFeederDAO;
    /**
     * Implementation of
     * <code>IProfileHandler</code>.
     */
    private IProfileHandler profileHandler;
    /**
     * Implementation of IAdminServiceOperator.
     */
    private IAdminServiceOperator adminServiceOperator;
    private CodingRepository codingRepository;
    private ServiceOperatorRepository serviceOperatorRepository;
    private OperationDAORepository operationDaoRepository;
    private RequestOperatorRepository requestOperatorRepository;
    private BindingRepository bindingRepository;
    private ConverterRepository converterRepository;
    private AdminRequestOperatorRepository adminRequestOperatorRepository;
    private IFactory<SosServiceIdentification> serviceIdentificationFactory;
    private IFactory<SosServiceProvider> serviceProviderFactory;
    private Tasking tasking;

    /**
     * @return Returns the instance of the SosConfigurator. Null will be
     * returned if the parameterized getInstance method was not invoked
     * before. Usually this will be done in the SOS.
     * <p/>
     * @see Configurator#createInstance(Properties, String)
     */
    public static Configurator getInstance() {
        initLock.lock();
        try {
            return instance;
        } finally {
            initLock.unlock();
        }
    }

    /**
     * @param connectionProviderConfig
     * @param basepath
     * <p/>
     * @return Returns an instance of the SosConfigurator. This method is used
     * to implement the singelton pattern
     *
     * @throws ConfigurationException
     * if the initialization failed
     */
    public static Configurator createInstance(Properties connectionProviderConfig, String basepath)
            throws ConfigurationException {
        if (instance == null) {
            boolean initialize = false;
            initLock.lock();
            try {
                if (instance == null) {
                    try {
                        instance = new Configurator(connectionProviderConfig, basepath);
                        initialize = true;
                    } catch (RuntimeException t) {
                        cleanUpAndThrow(t);
                    } catch (ConfigurationException t) {
                        cleanUpAndThrow(t);
                    }
                }
            } finally {
                initLock.unlock();
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

    /**
     * private constructor due to the singelton pattern.
     *
     * @param configis
     * InputStream of the configfile
     * @param dbconfigis
     * InputStream of the dbconfigfile
     * @param basepath
     * base path for configuration files
     * <p/>
     * @throws OwsExceptionReport
     * if the
     * @throws IOException
     */
    private Configurator(Properties connectionProviderConfig, String basepath) throws ConfigurationException {
        if (basepath == null) {
            String message = "No basepath available!";
            LOGGER.info(message);
            throw new ConfigurationException(message);
        }
        if (connectionProviderConfig == null) {
            String message = "No connection provider configuration available!";
            LOGGER.info(message);
            throw new ConfigurationException(message);
        }

        this.basepath = basepath;
        this.connectionProviderProperties = connectionProviderConfig;
        LOGGER.info("Configurator initialized: [basepath={}]",
                    this.basepath, this.connectionProviderProperties);
    }

    private static void notNullOrEmpty(String name, String val) throws ConfigurationException {
        notNull(name, val);
        if (val.isEmpty()) {
            throw new ConfigurationException(String.format("%s can not be empty!", name));
        }
    }

    private static void notNull(String name, Object val) throws ConfigurationException {
        if (val == null) {
            throw new ConfigurationException(String.format("%s can not be null!", name));
        }
    }

    private static void greaterZero(String name, int i) throws ConfigurationException {
        if (i <= 0) {
            throw new ConfigurationException(String.format("%s can not be smaller or equal zero (was %d)!", name, i));
        }
    }

    /**
     * @return the updateInterval in milli seconds
     */
    public long getUpdateIntervallInMillis() {
        return this.capabiltiesCacheUpdateInterval * 60000;
    }

    @Setting(CAPABILITIES_CACHE_UPDATE_INTERVAL)
    public void setCapabilitiesCacheUpdateInterval(int interval) throws ConfigurationException {
        greaterZero("Cache update interval", interval);
        if (this.capabiltiesCacheUpdateInterval != interval) {
            this.capabiltiesCacheUpdateInterval = interval;
            if (this.capabilitiesCacheController != null) {
                this.capabilitiesCacheController.reschedule();
            }
        }
    }

    /**
     * @return the characterEncoding
     */
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Setting(CHARACTER_ENCODING)
    public void setCharacterEncoding(String encoding) throws ConfigurationException {
        notNullOrEmpty("Character Encoding", encoding);
        this.characterEncoding = encoding;
        XmlOptionsHelper.getInstance(this.characterEncoding, true);
    }

    public int getCacheThreadCount() {
        return cacheThreadCount;
    }

    @Setting(CACHE_THREAD_COUNT)
    public void setCacheThreadCount(int threads) throws ConfigurationException {
        if (threads <= 0) {
            throw new ConfigurationException(String.format("Invalid cache thread count %d", threads));
        }
        this.cacheThreadCount = threads;
    }

    /**
     * @return the configFileMap
     */
    public Map<String, String> getConfigFileMap() {
        return Collections.unmodifiableMap(configFileMap);
    }

    @Setting(CONFIGURATION_FILES)
    public void setConfigurationFiles(String configurationFiles) {
        if (configurationFiles != null && !configurationFiles.isEmpty()) {
            for (String kvp : configurationFiles.split(";")) {
                String[] keyValue = kvp.split(" ");
                this.configFileMap.put(keyValue[0], keyValue[1]);
            }
        } else {
            this.configFileMap.clear();
        }
    }

    /**
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    public String getTokenSeperator() {
        return this.tokenSeperator;
    }

    @Setting(TOKEN_SEPERATOR)
    public void setTokenSeperator(String seperator) throws ConfigurationException {
        notNullOrEmpty("Token seperator", seperator);
        this.tokenSeperator = seperator;
    }

    /**
     * Returns the default tuple seperator for results.
     * <p/>
     * @return the tupleSeperator.
     */
    public String getTupleSeperator() {
        return this.tupleSeperator;
    }

    @Setting(TUPLE_SEPERATOR)
    public void setTupleSeperator(String seperator) throws ConfigurationException {
        notNullOrEmpty("Tuple seperator", seperator);
        this.tupleSeperator = seperator;
    }

    /**
     * Returns the default decimal seperator for results.
     * <p/>
     * @return decimal separator.
     */
    public String getDecimalSeparator() {
        return this.decimalSeparator;
    }

    @Setting(DECIMAL_SEPARATOR)
    public void setDecimalSeperator(String seperator) throws ConfigurationException {
        notNullOrEmpty("Decimal seperator", seperator);
        this.decimalSeparator = seperator;
    }

    /**
     * Returns the minimum size a response has to hvae to be compressed.
     * <p/>
     * @return the minimum threshold
     */
    public int getMinimumGzipSize() {
        return this.minimumGzipSize;
    }

    @Setting(MINIMUM_GZIP_SIZE)
    public void setMinimumGzipSize(int size) {
        this.minimumGzipSize = size;
    }

    /**
     * @return maxGetObsResults
     */
    public int getMaxGetObsResults() {
        return this.maxGetObsResults;
    }

    @Setting(MAX_GET_OBSERVATION_RESULTS)
    public void setMaxGetObservationResults(int maxResults) {
        this.maxGetObsResults = maxResults;
    }

    public String getDefaultOfferingPrefix() {
        return this.defaultOfferingPrefix;
    }

    @Setting(DEFAULT_OFFERING_PREFIX)
    public void setDefaultOfferingPrefix(String prefix) {
        this.defaultOfferingPrefix = prefix;
    }

    public String getDefaultProcedurePrefix() {
        return this.defaultProcedurePrefix;
    }

    @Setting(DEFAULT_PROCEDURE_PREFIX)
    public void setDefaultProcedurePrefix(String prefix) {
        this.defaultProcedurePrefix = prefix;
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     */
    public int getLease() {
        return this.lease;
    }

    @Setting(LEASE)
    public void setLease(int lease) throws ConfigurationException {
        greaterZero("Lease", lease);
        this.lease = lease;
    }

    public int getDefaultEPSG() {
        return this.defaultEPSG;
    }

    @Setting(DEFAULT_EPSG)
    public void setDefaultEpsg(int epsgCode) throws ConfigurationException {
        greaterZero("Default EPSG Code", epsgCode);
        this.defaultEPSG = epsgCode;
    }

    /**
     * @return true if duplicate observations should be skipped during insertion
     */
    public boolean isSkipDuplicateObservations() {
        return this.skipDuplicateObservations;
    }

    @Setting(SKIP_DUPLICATE_OBSERVATIONS)
    public void setSkipDuplicateObservations(boolean skip) {
        this.skipDuplicateObservations = skip;
    }

    /**
     * @return the supportsQuality
     */
    public boolean isSupportsQuality() {
        return this.supportsQuality;
    }

    @Setting(SUPPORTS_QUALITY)
    public void setSupportsQuality(boolean supportsQuality) {
        this.supportsQuality = supportsQuality;
    }

    /**
     * @return Returns the gmlDateFormat.
     */
    public String getGmlDateFormat() {
        return this.gmlDateFormat;
    }

    @Setting(GML_DATE_FORMAT)
    public void setGmlDateFormat(String format) {
        // TODO remove variable?
        this.gmlDateFormat = format;
        DateTimeHelper.setResponseFormat(this.gmlDateFormat);
    }

    /**
     * @return Returns the sensor description directory
     */
    public File getSensorDir() {
        return this.sensorDir;
    }

    @Setting(SENSOR_DIRECTORY)
    public void setSensorDirectory(File sensorDirectory) {
        this.sensorDir = sensorDirectory;
    }

    /**
     * Get service URL.
     *
     * @return the service URL
     */
    public String getServiceURL() {
        return this.serviceURL;
    }

    @Setting(SERVICE_URL)
    public void setServiceURL(URI serviceURL) throws ConfigurationException {
        notNull("Service URL", serviceURL);
        String url = serviceURL.toString();
        if (url.contains("?")) {
            url = url.split("[?]")[0];
        }
        this.serviceURL = url;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    public String getSrsNamePrefix() {
        return this.srsNamePrefix;
    }

    @Setting(SRS_NAME_PREFIX_SOS_V1)
    public void setSrsNamePrefixForSosV1(String prefix) {
        if (!prefix.endsWith(":") && !prefix.isEmpty()) {
            prefix += ":";
        }
        this.srsNamePrefix = prefix;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    public String getSrsNamePrefixSosV2() {
        return this.srsNamePrefixSosV2;
    }

    @Setting(SRS_NAME_PREFIX_SOS_V2)
    public void setSrsNamePrefixForSosV2(String prefix) {
        if (!prefix.endsWith("/") && !prefix.isEmpty()) {
            prefix += "/";
        }
        this.srsNamePrefixSosV2 = prefix;
    }

    /**
     * @param epsgCode
     * <p/>
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

    @Setting(EPSG_CODES_WITH_REVERSED_AXIS_ORDER)
    public void setEpsgCodesWithReversedAxisOrder(String codes) throws ConfigurationException {
        notNullOrEmpty("EPSG Codes to switch coordinates for", codes);
        String[] splitted = codes.split(";");
        this.epsgsWithReversedAxisOrder = new ArrayList<Range>(splitted.length);
        for (String entry : splitted) {
            String[] splittedEntry = entry.split("-");
            Range r = null;
            if (splittedEntry.length == 1) {
                r = new Range(Integer.parseInt(splittedEntry[0]), Integer.parseInt(splittedEntry[0]));
            } else if (splittedEntry.length == 2) {
                r = new Range(Integer.parseInt(splittedEntry[0]), Integer.parseInt(splittedEntry[1]));
            } else {
                throw new ConfigurationException(String.format(
                        "Invalid format of entry in 'switchCoordinatesForEPSG': %s", entry));
            }
            this.epsgsWithReversedAxisOrder.add(r);
        }
    }

    /**
     * Initialize this class. Since this initialization is not done in the
     * constructor, dependent classes can use the SosConfigurator already when
     * called from here.
     */
    private void initialize() throws ConfigurationException {
        LOGGER.info("\n******\n Configurator initialization started\n******\n");
        SettingsManager.getInstance().configure(this);
        initializeConnectionProvider();
        this.codingRepository = new CodingRepository();
        this.serviceIdentificationFactory = new SosServiceIdentificationFactory();
        this.serviceProviderFactory = new SosServiceProviderFactory();
        this.operationDaoRepository = new OperationDAORepository();
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
     * reads the requestListeners from the configFile and returns a
     * RequestOperator containing the requestListeners
     *
     * @return RequestOperators with requestListeners
     * <p/>
     * @throws ConfigurationException if initialization of a RequestListener failed
     */
    private void initializeAdminServiceOperator() throws ConfigurationException {
        this.adminServiceOperator = new ConfiguringSingletonServiceLoader<IAdminServiceOperator>(
                IAdminServiceOperator.class, true).get();
    }

    /**
     * Load implemented cache feeder dao
     *
     * @throws ConfigurationException If no cache feeder dao is implemented
     */
    private void initalizeCacheFeederDAO() throws ConfigurationException {
        this.cacheFeederDAO = new ConfiguringSingletonServiceLoader<ICacheFeederDAO>(ICacheFeederDAO.class, true).get();
    }

    /**
     * intializes the CapabilitiesCache
     *
     * @throws ConfigurationException if initializing the CapabilitiesCache failed
     */
    private void initializeCapabilitiesCacheController() throws ConfigurationException {
        this.capabilitiesCacheController = new ConfiguringSingletonServiceLoader<ACapabilitiesCacheController>(
                ACapabilitiesCacheController.class, true).get();
    }

    /**
     * Load the connection provider implementation
     *
     * @throws ConfigurationException If no connection provider is implemented
     */
    private void initializeConnectionProvider() throws ConfigurationException {
        this.connectionProvider = new ConfiguringSingletonServiceLoader<IConnectionProvider>(IConnectionProvider.class,
                                                                                             true).get();
        this.connectionProvider.initialize(this.connectionProviderProperties);
    }

    private void initializeDataSource() throws ConfigurationException {
        this.dataSourceInitializator = new ConfiguringSingletonServiceLoader<IDataSourceInitializator>(
                IDataSourceInitializator.class, true).get();
        try {
            this.dataSourceInitializator.initializeDataSource();
        } catch (OwsExceptionReport owse) {
            throw new ConfigurationException(owse);
        }
    }

    /**
     * Load implemented feature query handler
     *
     * @throws ConfigurationException If no feature query handler is implemented
     */
    private void initalizeFeatureQueryHandler() throws ConfigurationException {
        this.featureQueryHandler = new ConfiguringSingletonServiceLoader<IFeatureQueryHandler>(
                IFeatureQueryHandler.class, true).get();
    }

    private void initializeProfileHandler() throws ConfigurationException {
        this.profileHandler = new ConfiguringSingletonServiceLoader<IProfileHandler>(IProfileHandler.class, false).get();
        if (this.profileHandler == null) {
            this.profileHandler = new DefaultProfileHandler();
            LOGGER.info("No IProfileHandler implementations is loaded! DefaultHandler is used!");
        }
    }

    /**
     * @return Returns the service identification
     * <p/>
     * @throws OwsExceptionReport
     */
    public SosServiceIdentification getServiceIdentification() throws OwsExceptionReport {
        try {
            return this.serviceIdentificationFactory.get();
        } catch (ConfigurationException e) {
            if (e.getCause() != null && e.getCause() instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e.getCause();
            } else {
                throw Util4Exceptions.createNoApplicableCodeException(e, "Could not generate ServiceIdentification");
            }
        }
    }

    /**
     * @return Returns the service provider
     * <p/>
     * @throws OwsExceptionReport
     */
    public SosServiceProvider getServiceProvider() throws OwsExceptionReport {
        try {
            return this.serviceProviderFactory.get();
        } catch (ConfigurationException e) {
            if (e.getCause() != null && e.getCause() instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e.getCause();
            } else {
                throw Util4Exceptions.createNoApplicableCodeException(e, "Could not generate ServiceProvider");
            }
        }
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

    public OperationDAORepository getOperationDaoRepository() {
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
}
