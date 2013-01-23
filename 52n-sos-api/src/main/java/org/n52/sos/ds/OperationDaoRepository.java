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
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.util.AbstractServiceLoaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class OperationDaoRepository extends AbstractServiceLoaderRepository<IOperationDAO> {

	private static final Logger log = LoggerFactory.getLogger(OperationDaoRepository.class);

	/** Implemented ISosOperationDAO */
    private final Map<String, IOperationDAO> operationDaos = new HashMap<String, IOperationDAO>(0);

    /**
     * Load implemented operation dao
     *
     * @throws ConfigurationException
     *             If no operation dao is implemented
     */
    public OperationDaoRepository() throws ConfigurationException {
		super(IOperationDAO.class, true);
		load(false);
    }

    /**
     * Load the implemented operation dao and add them to a map with operation
     * name as key
     *
     * @throws ConfigurationException
     *             If no operation dao is implemented
     */
	@Override
    protected void processImplementations(Set<IOperationDAO> daos) throws ConfigurationException {
		this.operationDaos.clear();
		for (IOperationDAO dao : daos) {
			this.operationDaos.put(dao.getOperationName(), dao);
		}
    }

    /**
     * @return the implemented operation DAOs
     */
    public Map<String, IOperationDAO> getOperationDAOs() {
        return Collections.unmodifiableMap(this.operationDaos);
    }

	 /**
	  * @param operationName the operation name
	  * @return the implemented operation DAO
     */
    public IOperationDAO getOperationDAO(String operationName) {
        return this.operationDaos.get(operationName);
    }
}
