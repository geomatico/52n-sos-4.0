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
package org.n52.sos.service.operator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.request.operator.RequestOperatorRepository;
import org.n52.sos.util.AbstractConfiguringServiceLoaderRepository;
import org.n52.sos.util.CollectionHelper;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ServiceOperatorRepository extends AbstractConfiguringServiceLoaderRepository<ServiceOperator> {
	private static ServiceOperatorRepository instance;
	
    /**
     * Implemented ServiceOperator
     */
    private final Map<ServiceOperatorKeyType, ServiceOperator> serviceOperators =
            new HashMap<ServiceOperatorKeyType, ServiceOperator>(0);

    /** supported SOS versions */
    private final Map<String, Set<String>> supportedVersions = new HashMap<String, Set<String>>(0);

    /** supported services */
    private final Set<String> supportedServices = new HashSet<String>(0);

    public static ServiceOperatorRepository getInstance(){
    	if (instance == null) {
    		instance = new ServiceOperatorRepository();
    	}
    	return instance;
    }
    
    /**
     * Load implemented request listener
     * 
     * @throws ConfigurationException
     *             If no request listener is implemented
     */
    private ServiceOperatorRepository() throws ConfigurationException {
        super(ServiceOperator.class, false);
        load(false);
    }

    /**
     * Load the implemented request listener and add them to a map with
     * operation name as key
     * 
     * @param implementations
     *            the loaded implementations
     * 
     * @throws ConfigurationException
     *             If no request listener is implemented
     */
    @Override
    protected void processConfiguredImplementations(Set<ServiceOperator> implementations)
            throws ConfigurationException {
        this.serviceOperators.clear();
        this.supportedServices.clear();
        this.supportedVersions.clear();
        for (ServiceOperator iServiceOperator : implementations) {
            this.serviceOperators.put(iServiceOperator.getServiceOperatorKeyType(), iServiceOperator);
            if (this.supportedVersions.containsKey(iServiceOperator.getServiceOperatorKeyType().getService())) {
                this.supportedVersions.get(iServiceOperator.getServiceOperatorKeyType().getService()).add(
                        iServiceOperator.getServiceOperatorKeyType().getVersion());
            } else {
                this.supportedVersions.put(iServiceOperator.getServiceOperatorKeyType().getService(),
                        CollectionHelper.asSet(iServiceOperator.getServiceOperatorKeyType().getVersion()));
            }
            this.supportedServices.add(iServiceOperator.getServiceOperatorKeyType().getService());
        }
    }

    /**
     * Update/reload the implemented request listener
     * 
     * @throws ConfigurationException
     *             If no request listener is implemented
     */
    @Override
    public void update() throws ConfigurationException {
    	RequestOperatorRepository.getInstance().update();
        super.update();
    }

    /**
     * @return the implemented request listener
     */
    public Map<ServiceOperatorKeyType, ServiceOperator> getServiceOperators() {
        return Collections.unmodifiableMap(serviceOperators);
    }

    public Set<ServiceOperatorKeyType> getServiceOperatorKeyTypes() {
        return getServiceOperators().keySet();
    }

    public ServiceOperator getServiceOperator(ServiceOperatorKeyType serviceOperatorIdentifier)
            throws OwsExceptionReport {
        return serviceOperators.get(serviceOperatorIdentifier);
    }

    /**
     * @param service
     * @param version
     * @return the implemented request listener
     * 
     * 
     * @throws OwsExceptionReport
     */
    public ServiceOperator getServiceOperator(String service, String version) throws OwsExceptionReport {
        return getServiceOperator(new ServiceOperatorKeyType(service, version));
    }

    /**
     * @return the supportedVersions
     * 
     * @deprecated use getSupporteVersions(String service)
     */
    public Set<String> getSupportedVersions() {
        return Collections.emptySet();
    }
    
    public Set<String> getAllSupportedVersions() {
        Set<String> set = CollectionHelper.set();
        for (Set<String> versionSet : supportedVersions.values()) {
            set.addAll(versionSet);
        }
        return set;
    }

    /**
     * @return the supportedVersions
     * 
     */
    public Set<String> getSupportedVersions(String service) {
        if (isServiceSupported(service)) {
            return Collections.unmodifiableSet(supportedVersions.get(service));
        }
        return CollectionHelper.set();
    }

    /**
     * @return the supportedVersions
     * 
     * @deprecated use isVersionSupported(String service, String version)
     */
    public boolean isVersionSupported(String version) {
        return getAllSupportedVersions().contains(version);
    }

    /**
     * @return the supportedVersions
     * 
     */
    public boolean isVersionSupported(String service, String version) {
        if (isServiceSupported(service)) {
            return supportedVersions.get(service).contains(version);
        }
        return false;
    }

    /**
     * @return the supportedVersions
     */
    public Set<String> getSupportedServices() {
        return CollectionHelper.set();
    }

    public boolean isServiceSupported(String service) {
        return this.supportedServices.contains(service);
    }

}
