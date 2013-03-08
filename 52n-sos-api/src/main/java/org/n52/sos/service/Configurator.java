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

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.binding.BindingRepository;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.cache.ContentCacheController;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.convert.ConverterRepository;
import org.n52.sos.ds.CacheFeederDAO;
import org.n52.sos.ds.ConnectionProvider;
import org.n52.sos.ds.DataConnectionProvider;
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
import org.n52.sos.util.Producer;
import org.n52.sos.util.Util4Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO; configures the logger.
 */
public class Configurator implements Cleanupable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);
    /**
     * instance attribut, due to the singleton pattern.
     */
    private static Configurator instance = null;
    private static final Lock initLock = new ReentrantLock();

    /**
     * @return Returns the instance of the SosConfigurator. Null will be returned if the parameterized getInstance
     *         method was not invoked before. Usually this will be done in the SOS.
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
     * @return Returns an instance of the SosConfigurator. This method is used to implement the singelton pattern
     *
     * @throws ConfigurationException if the initialization failed
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

    private static void cleanup(Producer<? extends Cleanupable> p) {
        if (p != null) {
            Cleanupable c = p.get();
            if (c != null) {
                c.cleanup();
            }
        }
    }

    private static void cleanup(Cleanupable c) {
        if (c != null) {
            c.cleanup();
        }
    }

    protected static <T> T get(Producer<T> factory) throws OwsExceptionReport {
        try {
            return factory.get();
        } catch (Exception e) {
            if (e instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e;
            } else if (e.getCause() != null && e.getCause() instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e.getCause();
            } else {
                throw Util4Exceptions.createNoApplicableCodeException(e, String
                        .format("Could not request object from %s", factory));
            }
        }
    }

    private static <T> T loadAndConfigure(Class<? extends T> t, boolean required) {
        return new ConfiguringSingletonServiceLoader<T>(t, required).get();

    }

    private static <T> T loadAndConfigure(Class<? extends T> t, boolean required, T defaultImplementation) {
        return new ConfiguringSingletonServiceLoader<T>(t, required, defaultImplementation).get();
    }
    /**
     * base path for configuration files.
     */
    private String basepath;
    private ServiceConfiguration configuration;
    private Properties dataConnectionProviderProperties;
    private Properties featureConnectionProviderProperties;
    private IFeatureQueryHandler featureQueryHandler;
    private ConnectionProvider dataConnectionProvider;
    private ConnectionProvider featureConnectionProvider;
    private ContentCacheController contentCacheController;
    private CacheFeederDAO cacheFeederDAO;
    private IProfileHandler profileHandler;
    private IAdminServiceOperator adminServiceOperator;
    private Producer<SosServiceIdentification> serviceIdentificationFactory;
    private Producer<SosServiceProvider> serviceProviderFactory;
    private CodingRepository codingRepository;
    private ServiceOperatorRepository serviceOperatorRepository;
    private OperationDAORepository operationDaoRepository;
    private RequestOperatorRepository requestOperatorRepository;
    private BindingRepository bindingRepository;
    private ConverterRepository converterRepository;
    private AdminRequestOperatorRepository adminRequestOperatorRepository;
    private Tasking tasking;

    /**
     * private constructor due to the singelton pattern.
     *
     * @param configis   InputStream of the configfile
     * @param dbconfigis InputStream of the dbconfigfile
     * @param basepath   base path for configuration files
     * <p/>
     * @throws OwsExceptionReport if the
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
     * Initialize this class. Since this initialization is not done in the constructor, dependent classes can use the
     * SosConfigurator already when called from here.
     */
    private void initialize() throws ConfigurationException {
        LOGGER.info("\n******\n Configurator initialization started\n******\n");

        SettingsManager.getInstance().configure(configuration = new ServiceConfiguration());

        initializeConnectionProviders();
        codingRepository = new CodingRepository();
        serviceIdentificationFactory = new SosServiceIdentificationFactory();
        serviceProviderFactory = new SosServiceProviderFactory();
        operationDaoRepository = new OperationDAORepository();
        serviceOperatorRepository = new ServiceOperatorRepository();
        featureQueryHandler = loadAndConfigure(IFeatureQueryHandler.class, true);
        cacheFeederDAO = loadAndConfigure(CacheFeederDAO.class, true);
        converterRepository = new ConverterRepository();
        requestOperatorRepository = new RequestOperatorRepository();
        bindingRepository = new BindingRepository();
        adminServiceOperator = loadAndConfigure(IAdminServiceOperator.class, true);
        adminRequestOperatorRepository = new AdminRequestOperatorRepository();
        contentCacheController = loadAndConfigure(ContentCacheController.class, true);
        tasking = new Tasking();
        profileHandler = loadAndConfigure(IProfileHandler.class, false, new DefaultProfileHandler());

        LOGGER.info("\n******\n Configurator initialization finished\n******\n");
    }

    /**
     * @return Returns the service identification
     * <p/>
     * @throws OwsExceptionReport
     */
    public SosServiceIdentification getServiceIdentification() throws OwsExceptionReport {
        return get(serviceIdentificationFactory);
    }

    /**
     * @return Returns the service provider
     * <p/>
     * @throws OwsExceptionReport
     */
    public SosServiceProvider getServiceProvider() throws OwsExceptionReport {
        return get(serviceProviderFactory);
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
     * @return the current contentCacheController
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
     * @return the current contentCacheController
     */
    public ContentCacheController getCacheController() {
        return contentCacheController;
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
    public CacheFeederDAO getCacheFeederDAO() {
        return cacheFeederDAO;
    }

    /**
     * @return the implemented data connection provider
     */
    public ConnectionProvider getDataConnectionProvider() {
        return dataConnectionProvider;
    }

    /**
     * @return the implemented feature connection provider
     */
    public ConnectionProvider getFeatureConnectionProvider() {
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
        getBindingRepository().update();
        getOperationDaoRepository().update();
        getCodingRepository().updateDecoders();
        getCodingRepository().updateEncoders();
        getServiceOperatorRepository().update();
        getRequestOperatorRepository().update();
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

    /**
     * @deprecated use getCodingRepository().updateDecoders();
     */
    @Deprecated
    public void updateDecoder() throws ConfigurationException {
        getCodingRepository().updateDecoders();
    }

    /**
     * @deprecated use getCodingRepository().updateEncoders();
     */
    @Deprecated
    public void updateEncoder() throws ConfigurationException {
        getCodingRepository().updateEncoders();
    }

    /**
     * @deprecated use getOperationDaoRepository().update();
     */
    @Deprecated
    public void updateOperationDaos() throws ConfigurationException {
        getOperationDaoRepository().update();
    }

    /**
     * @deprecated use getServiceOperatorRepository().update();
     */
    @Deprecated
    public void updateServiceOperators() throws ConfigurationException {
        getServiceOperatorRepository().update();
    }

    /**
     * @deprecated use getBindingRepository().update();
     */
    @Deprecated
    public void updateBindings() throws ConfigurationException {
        getBindingRepository().update();
    }

    /**
     * @deprecated use getConverterRepository().update();
     */
    @Deprecated
    public void updateConverter() throws ConfigurationException {
        getConverterRepository().update();
    }

    /**
     * @deprecated use getRequestOperatorRepository().update();
     */
    @Deprecated
    public void updateRequestOperator() throws ConfigurationException {
        getRequestOperatorRepository().update();
    }

    public IProfileHandler getProfileHandler() {
        return profileHandler;
    }

    /**
     * @deprecated use getProfileHandler().getActiveProfile()
     */
    @Deprecated
    public IProfile getActiveProfile() {
        return getProfileHandler().getActiveProfile();
    }

    /**
     * @deprecated use #getTokenSeparator()
     */
    @Deprecated
    public String getTokenSeperator() {
        return getServiceConfiguration().getTokenSeparator();
    }

    /**
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    public String getTokenSeparator() {
        return getServiceConfiguration().getTokenSeparator();
    }

    /**
     * @deprecated use #getTupleSeparator();
     */
    @Deprecated
    public String getTupleSeperator() {
        return getServiceConfiguration().getTupleSeparator();
    }

    public String getTupleSeparator() {
        return getServiceConfiguration().getTupleSeparator();
    }

    /**
     * @return the characterEncoding
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getCharacterEncoding() {
        return getServiceConfiguration().getCharacterEncoding();
    }

    /**
     * @return the configFileMap
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public Map<String, String> getConfigFileMap() {
        return getServiceConfiguration().getConfigFileMap();
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
        return getServiceConfiguration().getDecimalSeparator();
    }

    /**
     * Returns the minimum size a response has to hvae to be compressed.
     * <p/>
     * @return the minimum threshold
     */
    public int getMinimumGzipSize() {
        return getServiceConfiguration().getMinimumGzipSize();
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public int getMaxGetObsResults() {
        return getServiceConfiguration().getMaxGetObsResults();
    }

    /**
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getDefaultOfferingPrefix() {
        return getServiceConfiguration().getDefaultOfferingPrefix();
    }

    /**
     * @return Returns the lease for the getResult template (in minutes).
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public int getLease() {
        return getServiceConfiguration().getLease();
    }

    /**
     * @return true if duplicate observations should be skipped during insertion
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public boolean isSkipDuplicateObservations() {
        return getServiceConfiguration().isSkipDuplicateObservations();
    }

    /**
     * @return the supportsQuality
     */
    //HibernateObservationUtilities
    public boolean isSupportsQuality() {
        return getServiceConfiguration().isSupportsQuality();
    }

    /**
     * @return Returns the gmlDateFormat.
     *
     * @deprecated not used by any code, check for external use or remove
     */
    @Deprecated
    public String getGmlDateFormat() {
        return getServiceConfiguration().getGmlDateFormat();
    }

    /**
     * @return Returns the sensor description directory
     */
    //HibernateProcedureUtilities
    public String getSensorDir() {
        return getServiceConfiguration().getSensorDir();
    }

    /**
     * Get service URL.
     *
     * @return the service URL
     */
    public String getServiceURL() {
        return getServiceConfiguration().getServiceURL();
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    public String getSrsNamePrefix() {
        return getServiceConfiguration().getSrsNamePrefix();
    }

    /**
     * @return prefix URN for the spatial reference system
     */
    public String getSrsNamePrefixSosV2() {
        return getServiceConfiguration().getSrsNamePrefixSosV2();
    }

    public ServiceConfiguration getServiceConfiguration() {
        return this.configuration;
    }

    protected void initializeConnectionProviders() throws ConfigurationException {
        dataConnectionProvider = Configurator
                .<ConnectionProvider>loadAndConfigure(DataConnectionProvider.class, true);
        featureConnectionProvider = Configurator
                .<ConnectionProvider>loadAndConfigure(IFeatureConnectionProvider.class, false);
        dataConnectionProvider.initialize(dataConnectionProviderProperties);
        if (featureConnectionProvider != null) {
            featureConnectionProvider.initialize(featureConnectionProviderProperties != null
                                                         ? featureConnectionProviderProperties
                                                         : dataConnectionProviderProperties);
        } else {
            featureConnectionProvider = dataConnectionProvider;
        }
    }

    /**
     * Eventually cleanup everything created by the constructor
     */
    @Override
    public synchronized void cleanup() {
        cleanup(dataConnectionProvider);
        cleanup(featureConnectionProvider);
        cleanup(contentCacheController);
        cleanup(tasking);
        instance = null;
    }
}
