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

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.util.AbstractConfiguringServiceLoaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class ServiceOperatorRepository extends AbstractConfiguringServiceLoaderRepository<ServiceOperator> {
	private static final Logger log = LoggerFactory.getLogger(ServiceOperatorRepository.class);

    /**
     * Implemented ServiceOperator
     */
    private final Map<ServiceOperatorKeyType, ServiceOperator> serviceOperators =
                                                               new HashMap<ServiceOperatorKeyType, ServiceOperator>(0);

	 /** supported SOS versions */
    private final Set<String> supportedVersions = new HashSet<String>(0);

    /** supported services */
    private final Set<String> supportedServices = new HashSet<String>(0);

    /**
     * Load implemented request listener
     *
     * @throws ConfigurationException
     *             If no request listener is implemented
     */
    public ServiceOperatorRepository() throws ConfigurationException {
        super(ServiceOperator.class, true);
		load(false);
    }

    /**
     * Load the implemented request listener and add them to a map with
     * operation name as key
     *
     * @throws ConfigurationException
     *             If no request listener is implemented
     */
    @Override
    protected void processConfiguredImplementations(Set<ServiceOperator> implementations) throws ConfigurationException {
		this.serviceOperators.clear();
		this.supportedServices.clear();
		this.supportedVersions.clear();
        for (ServiceOperator iServiceOperator : implementations) {
			this.serviceOperators.put(iServiceOperator.getServiceOperatorKeyType(), iServiceOperator);
			this.supportedVersions.add(iServiceOperator.getServiceOperatorKeyType().getVersion());
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
        Configurator.getInstance().getRequestOperatorRepository().update();
		super.update();
    }

	  /**
     * @return the implemented request listener
     * @throws OwsExceptionReport
     */
    public Map<ServiceOperatorKeyType, ServiceOperator> getServiceOperators() throws OwsExceptionReport {
        return Collections.unmodifiableMap(serviceOperators);
    }

    public ServiceOperator getServiceOperator(ServiceOperatorKeyType serviceOperatorIdentifier)
            throws OwsExceptionReport {
        return serviceOperators.get(serviceOperatorIdentifier);
    }

	 /**
     * @param service
     * @param version
     * @return the implemented request listener
     * @throws OwsExceptionReport
     */
    public ServiceOperator getServiceOperator(String service, String version) throws OwsExceptionReport {
        return getServiceOperator(new ServiceOperatorKeyType(service, version));
    }

	 /**
     * @return the supportedVersions
     */
    public Set<String> getSupportedVersions() {
        return Collections.unmodifiableSet(this.supportedVersions);
    }

    public boolean isVersionSupported(String version) {
        return this.supportedVersions.contains(version);
    }

    /**
     * @return the supportedVersions
     */
    public Set<String> getSupportedServices() {
        return Collections.unmodifiableSet(this.supportedServices);
    }

    public boolean isServiceSupported(String service) {
        return this.supportedServices.contains(service);
    }


}
