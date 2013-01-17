/**
 * Copyright (C) 2012
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
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
import org.n52.sos.util.AbstractServiceLoaderRepository;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RequestOperatorRepository extends AbstractServiceLoaderRepository<IRequestOperator> {

	private final Map<RequestOperatorKeyType, IRequestOperator> requestOperators =
			new HashMap<RequestOperatorKeyType, IRequestOperator>(0);

	public RequestOperatorRepository() throws ConfigurationException {
		super(IRequestOperator.class, true);
		load(false);
	}

	@Override
	protected void processImplementations(Set<IRequestOperator> requestOperators) throws ConfigurationException {
		this.requestOperators.clear();
		for (IRequestOperator aRequestOperator : requestOperators) {
				this.requestOperators.put(aRequestOperator.getRequestOperatorKeyType(), aRequestOperator);
		}
	}

	@Override
	public void update() throws ConfigurationException {
		Configurator.getInstance().getOperationDaoRepository().update();
		super.update();
	}

	public IRequestOperator getRequestOperator(RequestOperatorKeyType requestOperatorKeyType) {
		return this.requestOperators.get(requestOperatorKeyType);
	}

	public IRequestOperator getRequestOperator(ServiceOperatorKeyType serviceOperatorKeyType, String operationName) {
		return getRequestOperator(new RequestOperatorKeyType(serviceOperatorKeyType, operationName));
	}

	public Map<RequestOperatorKeyType, IRequestOperator> getRequestOperator() {
		return Collections.unmodifiableMap(this.requestOperators);
	}
}