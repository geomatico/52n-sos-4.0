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

import static org.n52.sos.service.MiscSettings.*;
import static org.n52.sos.service.ServiceSettings.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.binding.BindingRepository;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.cache.ContentCacheController;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.config.annotation.Configurable;
import org.n52.sos.config.annotation.Setting;
import org.n52.sos.convert.ConverterRepository;
import org.n52.sos.ds.ICacheFeederDAO;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.ds.IDataConnectionProvider;
import org.n52.sos.ds.IDataSourceInitializator;
import org.n52.sos.ds.IFeatureConnectionProvider;
import org.n52.sos.ds.IFeatureQueryHandler;
import org.n52.sos.ds.OperationDAORepository;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceIdentificationFactory;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.ows.SosServiceProviderFactory;
import org.n52.sos.request.operator.RequestOperatorRepository;
import org.n52.sos.service.admin.operator.IAdminServiceOperator;
import org.n52.sos.service.admin.request.operator.AdminRequestOperatorRepository;
import org.n52.sos.service.operator.ServiceOperatorRepository;
import org.n52.sos.service.profile.DefaultProfileHandler;
import org.n52.sos.service.profile.IProfile;
import org.n52.sos.service.profile.IProfileHandler;
import org.n52.sos.tasking.Tasking;
import org.n52.sos.util.Cleanupable;
import org.n52.sos.util.ConfiguringSingletonServiceLoader;
import org.n52.sos.util.DateTimeHelper;
import org.n52.sos.util.IFactory;
import org.n52.sos.util.Util4Exceptions;
import org.n52.sos.util.Validation;
import org.n52.sos.util.XmlOptionsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO;
 * configures the logger.
 */
@Configurable
public class Configurator implements Cleanupable {

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
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private Map<String, String> configFileMap = new HashMap<String, String>(0);
    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated private String defaultOfferingPrefix;
    /**
     * date format of gml.
     */
    private String gmlDateFormat;
    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    private int lease;
    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated private int maxGetObsResults;
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
 exception.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated private boolean skipDuplicateObservations = false;
    /**
     * directory of sensor descriptions in SensorML format.
     */
    private String sensorDirectory;
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
     * token separator for result element.
     */
    private String tokenSeparator;
    /**
     * tuple separator for result element.
     */
    private String tupleSeparator;
    /**
     * decimal separator for result element.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated private String decimalSeparator;
    
    private Properties dataConnectionProviderProperties;
    
    private Properties featureConnectionProviderProperties;
    /**
     * Implementation of IFeatureQueryHandler.
     */
    private IFeatureQueryHandler featureQueryHandler;
    
    /**
     * Implementation of IDataConnectionProvider.
     */
    private IConnectionProvider dataConnectionProvider;
    
    /**
     * Implementation of IFeatureConnectionProvider.
     */
    private IConnectionProvider featureConnectionProvider;
    
    /**
     * Implementation of
     * <code>IDataSourceInitializator</code>.
     */
    private IDataSourceInitializator dataSourceInitializator;
    /**
     * Content Cache Controller.
     */
    private ContentCacheController capabilitiesCacheController;
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
        this.dataConnectionProviderProperties = connectionProviderConfig;
        LOGGER.info("Configurator initialized: [basepath={}]",
                    this.basepath, this.dataConnectionProviderProperties);
    }

    /**
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    @Deprecated
    public String getTokenSeperator() {
        return this.tokenSeparator;
    }

    @Deprecated
//    @Setting(MiscSettings.TOKEN_SEPERATOR)
    public void setTokenSeperator(String seperator) throws ConfigurationException {
        Validation.notNullOrEmpty("Token seperator", seperator);
        this.tokenSeparator = seperator;
    }
    
    /**
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    public String getTokenSeparator() {
        return this.tokenSeparator;
    }

    @Setting(MiscSettings.TOKEN_SEPARATOR)
    public void setTokenSeparator(String separator) throws ConfigurationException {
        Validation.notNullOrEmpty("Token separator", separator);
        this.tokenSeparator = separator;
    }

    /**
     * Returns the default tuple seperator for results.
     * <p/>
     * @return the tupleSeperator.
     */
    @Deprecated
    public String getTupleSeperator() {
        return this.tupleSeparator;
    }

    @Deprecated
//    @Setting(MiscSettings.TUPLE_SEPERATOR)
    public void setTupleSeperator(String seperator) throws ConfigurationException {
        Validation.notNullOrEmpty("Tuple seperator", seperator);
        this.tupleSeparator = seperator;
    }
    
    public String getTupleSeparator() {
        return this.tupleSeparator;
    }

    @Setting(MiscSettings.TUPLE_SEPARATOR)
    public void setTupleSeparator(String separator) throws ConfigurationException {
        Validation.notNullOrEmpty("Tuple separator", separator);
        this.tupleSeparator = separator;
    }

    /**
     * @return the characterEncoding
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Setting(CHARACTER_ENCODING)
    public void setCharacterEncoding(String encoding) throws ConfigurationException {
        Validation.notNullOrEmpty("Character Encoding", encoding);
        this.characterEncoding = encoding;
        XmlOptionsHelper.getInstance(this.characterEncoding, true);
    }

    /**
     * @return the configFileMap
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public Map<String, String> getConfigFileMap() {
        return Collections.unmodifiableMap(configFileMap);
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
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
     * Returns the default decimal seperator for results.
     * <p/>
     * @return decimal separator.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getDecimalSeparator() {
        return this.decimalSeparator;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(DECIMAL_SEPARATOR)
    public void setDecimalSeperator(String seperator) throws ConfigurationException {
        Validation.notNullOrEmpty("Decimal seperator", seperator);
        this.decimalSeparator = seperator;
    }

    /**
     * Returns the minimum size a response has to hvae to be compressed.
     * <p/>
     * @return the minimum threshold
     */
    /*
     * SosServlet
     */
    public int getMinimumGzipSize() {
        return this.minimumGzipSize;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Setting(MINIMUM_GZIP_SIZE)
    public void setMinimumGzipSize(int size) {
        this.minimumGzipSize = size;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public int getMaxGetObsResults() {
        return this.maxGetObsResults;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(MAX_GET_OBSERVATION_RESULTS)
    public void setMaxGetObservationResults(int maxResults) {
        this.maxGetObsResults = maxResults;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getDefaultOfferingPrefix() {
        return this.defaultOfferingPrefix;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(DEFAULT_OFFERING_PREFIX)
    public void setDefaultOfferingPrefix(String prefix) {
        this.defaultOfferingPrefix = prefix;
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public int getLease() {
        return this.lease;
    }
    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(LEASE)
    public void setLease(int lease) throws ConfigurationException {
        Validation.greaterZero("Lease", lease);
        this.lease = lease;
    }

    /**
     * @return true if duplicate observations should be skipped during insertion
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public boolean isSkipDuplicateObservations() {
        return this.skipDuplicateObservations;
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    @Setting(SKIP_DUPLICATE_OBSERVATIONS)
    public void setSkipDuplicateObservations(boolean skip) {
        this.skipDuplicateObservations = skip;
    }

    /**
     * @return the supportsQuality
     */
    //HibernateObservationUtilities
    public boolean isSupportsQuality() {
        return this.supportsQuality;
    }

    @Setting(SUPPORTS_QUALITY)
    public void setSupportsQuality(boolean supportsQuality) {
        this.supportsQuality = supportsQuality;
    }

    /**
     * @return Returns the gmlDateFormat.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
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
    //HibernateProcedureUtilities
    public String getSensorDir() {
        return this.sensorDirectory;
    }

    @Setting(SENSOR_DIRECTORY)
    public void setSensorDirectory(String sensorDirectory) {
        this.sensorDirectory = sensorDirectory;
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
        Validation.notNull("Service URL", serviceURL);
        String url = serviceURL.toString();
        if (url.contains("?")) {
            url = url.split("[?]")[0];
        }
        this.serviceURL = url;
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    /*
     * SosHelper
     * AbstractKvpDecoder
     * GmlEncoderv311
     * ITRequestEncoder
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
    /*
     * SosHelper
     * GmlEncoderv321
     * AbstractKvpDecoder
     * SosEncoderv100
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
     * Initialize this class. Since this initialization is not done in the
     * constructor, dependent classes can use the SosConfigurator already when
     * called from here.
     */
    private void initialize() throws ConfigurationException {
        LOGGER.info("\n******\n Configurator initialization started\n******\n");
        SettingsManager.getInstance().configure(this);
        initializeDataConnectionProvider();
        initializeFeatureConnectionProvider();
        codingRepository = new CodingRepository();
        serviceIdentificationFactory = new SosServiceIdentificationFactory();
        serviceProviderFactory = new SosServiceProviderFactory();
        operationDaoRepository = new OperationDAORepository();
        serviceOperatorRepository = new ServiceOperatorRepository();
        initalizeFeatureQueryHandler();
        initalizeCacheFeederDAO();
        converterRepository = new ConverterRepository();
        requestOperatorRepository = new RequestOperatorRepository();
        bindingRepository = new BindingRepository();
        initializeAdminServiceOperator();
        adminRequestOperatorRepository = new AdminRequestOperatorRepository();
        initializeDataSource();
        initializeContentCacheController();
        tasking = new Tasking();
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
        adminServiceOperator = new ConfiguringSingletonServiceLoader<IAdminServiceOperator>(
                IAdminServiceOperator.class, true).get();
    }

    /**
     * Load implemented cache feeder dao
     *
     * @throws ConfigurationException If no cache feeder dao is implemented
     */
    private void initalizeCacheFeederDAO() throws ConfigurationException {
        cacheFeederDAO = new ConfiguringSingletonServiceLoader<ICacheFeederDAO>(ICacheFeederDAO.class, true).get();
    }

    /**
     * intializes the CapabilitiesCache
     *
     * @throws ConfigurationException if initializing the CapabilitiesCache failed
     */
    private void initializeContentCacheController() throws ConfigurationException {
        capabilitiesCacheController =
        new ConfiguringSingletonServiceLoader<ContentCacheController>(ContentCacheController.class, true).get();
    }

    /**
     * Load the connection provider implementation
     *
     * @throws ConfigurationException If no connection provider is implemented
     */
    private void initializeDataConnectionProvider() throws ConfigurationException {
        dataConnectionProvider = new ConfiguringSingletonServiceLoader<IDataConnectionProvider>(IDataConnectionProvider.class,
                                                                                             true).get();
        dataConnectionProvider.initialize(this.dataConnectionProviderProperties);
    }
    
    /**
     * Load the connection provider implementation
     *
     * @throws ConfigurationException If no connection provider is implemented
     */
    private void initializeFeatureConnectionProvider() throws ConfigurationException {
        featureConnectionProvider = new ConfiguringSingletonServiceLoader<IFeatureConnectionProvider>(IFeatureConnectionProvider.class,
                                                                                             false).get();
        if (featureConnectionProvider != null) {
            featureConnectionProvider.initialize(featureConnectionProviderProperties);
        } else {
            featureConnectionProvider = dataConnectionProvider;
        }
    }

    private void initializeDataSource() throws ConfigurationException {
        dataSourceInitializator = new ConfiguringSingletonServiceLoader<IDataSourceInitializator>(
                IDataSourceInitializator.class, true).get();
        try {
            dataSourceInitializator.initializeDataSource();
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
        featureQueryHandler = new ConfiguringSingletonServiceLoader<IFeatureQueryHandler>(
                IFeatureQueryHandler.class, true).get();
    }

    private void initializeProfileHandler() throws ConfigurationException {
        profileHandler = new ConfiguringSingletonServiceLoader<IProfileHandler>(IProfileHandler.class, false).get();
        if (profileHandler == null) {
            profileHandler = new DefaultProfileHandler();
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
            return serviceIdentificationFactory.get();
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
            return serviceProviderFactory.get();
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
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getBasePath() {
        return basepath;
    }

    /**
     * @return the current capabilitiesCacheController
     */
    public ContentCache getCache() {
        return getCacheController().getCache();
    }

    /**
     * @deprecated use {@link #getCache()}
     */
    @Deprecated
    public ContentCache getCapabilitiesCache() {
        return getCache();
    }

    /**
     * @return the current capabilitiesCacheController
     */
    public ContentCacheController getCacheController() {
        return capabilitiesCacheController;
    }

    /**
     * @deprecated use {@link getCacheController()}
     */
    @Deprecated
    public ContentCacheController getCapabilitiesCacheController() {
        return getCacheController();
    }

    /**
     * @return the implemented cache feeder DAO
     */
    public ICacheFeederDAO getCacheFeederDAO() {
        return cacheFeederDAO;
    }

    /**
     * @return the implemented data connection provider
     */
    public IConnectionProvider getDataConnectionProvider() {
        return dataConnectionProvider;
    }
    
    /**
     * @return the implemented feature connection provider
     */
    public IConnectionProvider getFeatureConnectionProvider() {
        return featureConnectionProvider;
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
        return requestOperatorRepository;
    }

    public CodingRepository getCodingRepository() {
        return codingRepository;
    }

    public OperationDAORepository getOperationDaoRepository() {
        return operationDaoRepository;
    }

    public ServiceOperatorRepository getServiceOperatorRepository() {
        return serviceOperatorRepository;
    }

    public BindingRepository getBindingRepository() {
        return bindingRepository;
    }

    public ConverterRepository getConverterRepository() {
        return converterRepository;
    }

    public AdminRequestOperatorRepository getAdminRequestOperatorRepository() {
        return adminRequestOperatorRepository;
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
        return profileHandler;
    }

    public IProfile getActiveProfile() {
        return getProfileHandler().getActiveProfile();
    }

    /**
     * Eventually cleanup everything created by the constructor
     */
    @Override
    public synchronized void cleanup() {
        if (dataConnectionProvider != null) {
            dataConnectionProvider.cleanup();
            dataConnectionProvider = null;
        }
        if (featureConnectionProvider != null) {
            featureConnectionProvider.cleanup();
            featureConnectionProvider = null;
        }
        if (capabilitiesCacheController != null) {
            capabilitiesCacheController.cleanup();
            capabilitiesCacheController = null;
        }
        if (tasking != null) {
            tasking.cleanup();
            tasking = null;
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
