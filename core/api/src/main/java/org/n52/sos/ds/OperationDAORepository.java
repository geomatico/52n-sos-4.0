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
package org.n52.sos.ds;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.AbstractConfiguringServiceLoaderRepository;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OperationDAORepository extends AbstractConfiguringServiceLoaderRepository<OperationDAO> {
    private static OperationDAORepository instance;

    /**
     * @return Returns a singleton instance of the CodingRepository.
     */
    public static OperationDAORepository getInstance() {
        if (instance == null) {
            instance = new OperationDAORepository();
        }
        return instance;
    }
    /** Implemented ISosOperationDAO */
    private final Map<OperationDAOKeyType, OperationDAO> operationDaos =
            new HashMap<OperationDAOKeyType, OperationDAO>(0);

    /**
     * Load implemented operation dao
     * 
     * @throws ConfigurationException
     *             If no operation dao is implemented
     */
    public OperationDAORepository() throws ConfigurationException {
        super(OperationDAO.class, false);
        load(false);
    }

    /**
     * Load the implemented operation dao and add them to a map with operation
     * name as key.
     * 
     * @throws ConfigurationException
     *             If no operation dao is implemented
     */
    @Override
    protected void processConfiguredImplementations(Set<OperationDAO> daos) throws ConfigurationException {
        this.operationDaos.clear();
        for (OperationDAO dao : daos) {
                operationDaos.put(dao.getOperationDAOKeyType(), dao);
        }
    }

    /**
     * @return the implemented operation DAOs
     */
    public Map<OperationDAOKeyType, OperationDAO> getOperationDAOs() {
        return Collections.unmodifiableMap(this.operationDaos);
    }

    /**
     * @param service
     *            the service name
     * @param operationName
     *            the operation name
     * @return the implemented operation DAO
     */
    public OperationDAO getOperationDAO(String service, String operationName) {
        return this.operationDaos.get(new OperationDAOKeyType(service, operationName));
    }

    /**
     * @param operationDAOIdentifier
     *            the operation DAO identifier
     * @return the implemented operation DAO
     */
    public OperationDAO getOperationDAO(OperationDAOKeyType operationDAOIdentifier) {
        return this.operationDaos.get(operationDAOIdentifier);
    }
}
