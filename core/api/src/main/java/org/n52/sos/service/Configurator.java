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

import static org.n52.sos.util.ConfiguringSingletonServiceLoader.loadAndConfigure;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.n52.sos.binding.BindingRepository;
import org.n52.sos.cache.ContentCache;
import org.n52.sos.cache.ContentCacheController;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.convert.ConverterRepository;
import org.n52.sos.ds.CacheFeederDAO;
import org.n52.sos.ds.ConnectionProvider;
import org.n52.sos.ds.DataConnectionProvider;
import org.n52.sos.ds.FeatureQueryHandler;
import org.n52.sos.ds.IFeatureConnectionProvider;
import org.n52.sos.ds.OperationDAORepository;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.exception.ows.NoApplicableCodeException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.ows.SosServiceIdentification;
import org.n52.sos.ogc.ows.SosServiceIdentificationFactory;
import org.n52.sos.ogc.ows.SosServiceProvider;
import org.n52.sos.ogc.ows.SosServiceProviderFactory;
import org.n52.sos.request.operator.RequestOperatorRepository;
import org.n52.sos.service.admin.operator.AdminServiceOperator;
import org.n52.sos.service.admin.request.operator.AdminRequestOperatorRepository;
import org.n52.sos.service.operator.ServiceOperatorRepository;
import org.n52.sos.service.profile.DefaultProfileHandler;
import org.n52.sos.service.profile.ProfileHandler;
import org.n52.sos.tasking.Tasking;
import org.n52.sos.util.Cleanupable;
import org.n52.sos.util.ConfiguringSingletonServiceLoader;
import org.n52.sos.util.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class reads the configFile and builds the RequestOperator and DAO; configures the logger.
 */
public class Configurator implements Cleanupable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configurator.class);
    /**
     * instance attribute, due to the singleton pattern.
     */
    private static Configurator instance = null;
    private static final Lock initLock = new ReentrantLock();

    /**
     * @return Returns the instance of the SosConfigurator. <tt>null</tt> will be returned if the parameterized {@link #createInstance(Properties, String)}
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
    public static Configurator createInstance(final Properties connectionProviderConfig, final String basepath)
            throws ConfigurationException {
        if (instance == null) {
            boolean initialize = false;
            initLock.lock();
            try {
                if (instance == null) {
                    try {
                        instance = new Configurator(connectionProviderConfig, basepath);
                        initialize = true;
                    } catch (final RuntimeException t) {
                        cleanUpAndThrow(t);
                    } catch (final ConfigurationException t) {
                        cleanUpAndThrow(t);
                    }
                }
            } finally {
                initLock.unlock();
            }
            if (initialize) {
                try {
                    instance.initialize();
                } catch (final RuntimeException t) {
                    cleanUpAndThrow(t);
                } catch (final ConfigurationException t) {
                    cleanUpAndThrow(t);
                }
            }
        }
        return instance;
    }

    private static void cleanUpAndThrow(final ConfigurationException t) throws ConfigurationException {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
        throw t;
    }

    private static void cleanUpAndThrow(final RuntimeException t) {
        if (instance != null) {
            instance.cleanup();
            instance = null;
        }
        throw t;
    }

    private static void cleanup(final Cleanupable c) {
        if (c != null) {
            c.cleanup();
        }
    }

    protected static <T> T get(final Producer<T> factory) throws OwsExceptionReport {
        try {
            return factory.get();
        } catch (final Exception e) {
            if (e.getCause() != null && e.getCause() instanceof OwsExceptionReport) {
                throw (OwsExceptionReport) e.getCause();
            } else {
                throw new NoApplicableCodeException().withMessage("Could not request object from %s", factory);
            }
        }
    }

    /**
     * base path for configuration files.
     */
    private final String basepath;
    private ServiceConfiguration configuration;
    private final Properties dataConnectionProviderProperties;
    private Properties featureConnectionProviderProperties;
    private FeatureQueryHandler featureQueryHandler;
    private ConnectionProvider dataConnectionProvider;
    private ConnectionProvider featureConnectionProvider;
    private ContentCacheController contentCacheController;
    private CacheFeederDAO cacheFeederDAO;
    private ProfileHandler profileHandler;
    private AdminServiceOperator adminServiceOperator;
    private Producer<SosServiceIdentification> serviceIdentificationFactory;
    private Producer<SosServiceProvider> serviceProviderFactory;
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
    private Configurator(final Properties connectionProviderConfig, final String basepath) throws ConfigurationException {
        if (basepath == null) {
            final String message = "No basepath available!";
            LOGGER.info(message);
            throw new ConfigurationException(message);
        }
        if (connectionProviderConfig == null) {
            final String message = "No connection provider configuration available!";
            LOGGER.info(message);
            throw new ConfigurationException(message);
        }

        this.basepath = basepath;
        dataConnectionProviderProperties = connectionProviderConfig;
        LOGGER.info("Configurator initialized: [basepath={}]",
                    this.basepath, dataConnectionProviderProperties);
    }

   

    /**
     * Initialize this class. Since this initialization is not done in the constructor, dependent classes can use the
     * SosConfigurator already when called from here.
     */
    private void initialize() throws ConfigurationException {
        LOGGER.info("\n******\n Configurator initialization started\n******\n");

        SettingsManager.getInstance().configure(configuration = new ServiceConfiguration());

        initializeConnectionProviders();
        
        serviceIdentificationFactory = new SosServiceIdentificationFactory();
        serviceProviderFactory = new SosServiceProviderFactory();
        OperationDAORepository.getInstance();
        ServiceOperatorRepository.getInstance();
        CodingRepository.getInstance();
        featureQueryHandler = loadAndConfigure(FeatureQueryHandler.class, false);
        cacheFeederDAO = loadAndConfigure(CacheFeederDAO.class, false);
        ConverterRepository.getInstance();
        RequestOperatorRepository.getInstance();
        BindingRepository.getInstance();
        adminServiceOperator = loadAndConfigure(AdminServiceOperator.class, false);
        AdminRequestOperatorRepository.getInstance();
        contentCacheController = loadAndConfigure(ContentCacheController.class, false);
        tasking = new Tasking();
        profileHandler = loadAndConfigure(ProfileHandler.class, false, new DefaultProfileHandler());

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
     */
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
     * @return the current contentCacheController
     */
    public ContentCacheController getCacheController() {
        return contentCacheController;
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
    public FeatureQueryHandler getFeatureQueryHandler() {
        return featureQueryHandler;
    }

    /**
     * @return the implemented SOS administration request operator
     */
    public AdminServiceOperator getAdminServiceOperator() {
        return adminServiceOperator;
    }

    public void updateConfiguration() throws ConfigurationException {
        // TODO update converters
        getBindingRepository().update();
        getOperationDaoRepository().update();
    }

    @Deprecated
    public OperationDAORepository getOperationDaoRepository() {
        return OperationDAORepository.getInstance();
    }

    @Deprecated
    public BindingRepository getBindingRepository() {
        return BindingRepository.getInstance();
    }

    @Deprecated
    public ConverterRepository getConverterRepository() {
        return ConverterRepository.getInstance();
    }

    @Deprecated
    public AdminRequestOperatorRepository getAdminRequestOperatorRepository() {
        return AdminRequestOperatorRepository.getInstance();
    }

    public ProfileHandler getProfileHandler() {
        return profileHandler;
    }

    /**
     * Returns the default token seperator for results.
     * <p/>
     * @return the tokenSeperator.
     */
    public String getTokenSeparator() {
        return getServiceConfiguration().getTokenSeparator();
    }

    public String getTupleSeparator() {
        return getServiceConfiguration().getTupleSeparator();
    }


    /**
     * Returns the minimum size a response has to hvae to be compressed.
     * <p/>
     * @return the minimum threshold
     */
    public int getMinimumGzipSize() {
        return getServiceConfiguration().getMinimumGzipSize();
    }

    public String getDefaultOfferingPrefix() {
        return getServiceConfiguration().getDefaultOfferingPrefix();
    }
    
    public String getDefaultProcedurePrefix() {
        return getServiceConfiguration().getDefaultProcedurePrefix();
    }
    
    public String getDefaultFeaturePrefix() {
        return getServiceConfiguration().getDefaultFeaturePrefix();
    }
    
    public String getDefaultObservablePropertyPrefix() {
        return getServiceConfiguration().getDefaultObservablePropertyPrefix();
    }
    
    public boolean isUseDefaultPrefixes() {
        return getServiceConfiguration().isUseDefaultPrefixes();
    }

    public boolean isEncodeFullChildrenInDescribeSensor() {
    	return getServiceConfiguration().isEncodeFullChildrenInDescribeSensor();
	}

    /**
     * @return the supportsQuality
     */
    //HibernateObservationUtilities
    public boolean isSupportsQuality() {
        return getServiceConfiguration().isSupportsQuality();
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
        return configuration;
    }

    protected void initializeConnectionProviders() throws ConfigurationException {
        dataConnectionProvider = ConfiguringSingletonServiceLoader
                .<ConnectionProvider>loadAndConfigure(DataConnectionProvider.class, true);
        featureConnectionProvider = ConfiguringSingletonServiceLoader
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
