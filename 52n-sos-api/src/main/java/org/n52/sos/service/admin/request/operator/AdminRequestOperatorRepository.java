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
package org.n52.sos.service.admin.request.operator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.AbstractConfiguringServiceLoaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class AdminRequestOperatorRepository extends AbstractConfiguringServiceLoaderRepository<AdminRequestOperator> {

	private static final Logger log = LoggerFactory.getLogger(AdminRequestOperatorRepository.class);
	private Map<String, AdminRequestOperator> operators = new HashMap<String, AdminRequestOperator>(0);

	public AdminRequestOperatorRepository() throws ConfigurationException {
		super(AdminRequestOperator.class, false);
		load(false);
	}

	public AdminRequestOperator getAdminRequestOperator(String key) {
		return this.operators.get(key);
	}

	public Map<String, AdminRequestOperator> getAdminRequestOperators() {
		return Collections.unmodifiableMap(this.operators);
	}

	@Override
	protected void processConfiguredImplementations(Set<AdminRequestOperator> requestOperators) {
		this.operators.clear();
		for (AdminRequestOperator operator : requestOperators) {
				this.operators.put(operator.getKey(), operator);
		}
		if (this.operators.isEmpty()) {
			StringBuilder exceptionText = new StringBuilder();
			exceptionText.append("No IAdminRequestOperator implementation could be loaded!");
			exceptionText.append(" If the SOS is not used as webapp, this has no effect!");
			exceptionText.append(" Else add a IAdminRequestOperator implementation!");
			log.warn(exceptionText.toString());
		}
	}
}
