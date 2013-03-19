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
package org.n52.sos.binding;

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
public class BindingRepository extends AbstractConfiguringServiceLoaderRepository<Binding> {
	private static final Logger log = LoggerFactory.getLogger(BindingRepository.class);
    private final Map<String, Binding> bindings = new HashMap<String, Binding>(0);

    /**
     * reads the requestListeners from the configFile and returns a
     * RequestOperator containing the requestListeners
     *
     * @throws ConfigurationException
     *             if initialization of a RequestListener failed
     */
    public BindingRepository() throws ConfigurationException {
		super(Binding.class, false);
		load(false);
    }

	@Override
	protected void processConfiguredImplementations(Set<Binding> bindings) throws ConfigurationException {
		this.bindings.clear();
        for (Binding iBindingOperator : bindings) {
			this.bindings.put(iBindingOperator.getUrlPattern(), iBindingOperator);
        }
        if (this.bindings.isEmpty()) {
            StringBuilder exceptionText = new StringBuilder();
            exceptionText.append("No Binding implementation could be loaded!");
            exceptionText.append(" If the SOS is not used as webapp, this has no effect!");
            exceptionText.append(" Else add a Binding implementation!");
            log.warn(exceptionText.toString());
        }
    }

	public Binding getBinding(String urlPattern) {
        return this.bindings.get(urlPattern);
    }

    public Map<String, Binding> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }
}
