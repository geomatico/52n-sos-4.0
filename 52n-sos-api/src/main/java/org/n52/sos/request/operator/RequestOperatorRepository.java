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
package org.n52.sos.request.operator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.n52.sos.config.ConfigurationException;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.AbstractConfiguringServiceLoaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RequestOperatorRepository extends AbstractConfiguringServiceLoaderRepository<RequestOperator> {
    private static final Logger log = LoggerFactory.getLogger(RequestOperatorRepository.class);
    private final Map<RequestOperatorKeyType, RequestOperator> requestOperators =
                                                                new HashMap<RequestOperatorKeyType, RequestOperator>(0);

    public RequestOperatorRepository() throws ConfigurationException {
        super(RequestOperator.class, true);
        load(false);
    }

    @Override
    protected void processConfiguredImplementations(Set<RequestOperator> requestOperators) throws
            ConfigurationException {
        this.requestOperators.clear();
        SettingsManager sm = SettingsManager.getInstance();
        for (RequestOperator aRequestOperator : requestOperators) {
            try {
                if (sm.isActive(aRequestOperator.getRequestOperatorKeyType())) {
                    log.info("Registered IRequestOperator for {}", aRequestOperator.getRequestOperatorKeyType());
                    this.requestOperators.put(aRequestOperator.getRequestOperatorKeyType(), aRequestOperator);
                } else {
                    log.info("{} is inactive", aRequestOperator.getRequestOperatorKeyType());
                }
            } catch (ConnectionProviderException cpe) {
               throw new ConfigurationException("Error while checking RequestOperators", cpe);
            }
        }
    }

    @Override
    public void update() throws ConfigurationException {
        Configurator.getInstance().getOperationDaoRepository().update();
        super.update();
    }

    public RequestOperator getRequestOperator(RequestOperatorKeyType key) {
        return this.requestOperators.get(key);
    }

    public RequestOperator getRequestOperator(ServiceOperatorKeyType serviceOperatorKeyType, String operationName) {
        return getRequestOperator(new RequestOperatorKeyType(serviceOperatorKeyType, operationName));
    }

    public Map<RequestOperatorKeyType, RequestOperator> getRequestOperator() {
        return Collections.unmodifiableMap(this.requestOperators);
    }

    public Set<RequestOperatorKeyType> getRequestOperatorKeyTypes() {
        return getRequestOperator().keySet();
    }
}
