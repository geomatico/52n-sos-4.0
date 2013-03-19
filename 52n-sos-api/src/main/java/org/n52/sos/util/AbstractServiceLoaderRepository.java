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
package org.n52.sos.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

import org.n52.sos.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Abstract class to encapsulate the loading of implementations that are registered with the ServiceLoader interface.
 *
 * @param <T> the type that should be loaded

 *
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public abstract class AbstractServiceLoaderRepository<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractServiceLoaderRepository.class);
	private final ServiceLoader<T> serviceLoader;
	private final Class<T> type;
	private final boolean failIfEmpty;

	protected AbstractServiceLoaderRepository(Class<T> type, boolean failIfEmpty) {
		log.debug("Loading Implementations for {}", type);
		this.type = type;
		this.failIfEmpty = failIfEmpty;
		this.serviceLoader = ServiceLoader.load(this.type);
		log.debug("Implementations for {} loaded succesfull!", this.type);
	}

	public void update() throws ConfigurationException {
		log.debug("Reloading Implementations for {}", this.type);
		load(true);
		log.debug("Implementations for {} reloaded succesfull!", this.type);
	}

	protected final void load(boolean reload) throws ConfigurationException {
		processImplementations(getImplementations(reload));
	}

	private Set<T> getImplementations(boolean reload) throws ConfigurationException {
		if (reload) {
			this.serviceLoader.reload();
		}
		LinkedList<T> implementations = new LinkedList<T>();
		Iterator<T> iter = this.serviceLoader.iterator();
		while (iter.hasNext()) {
			try {
                T t = iter.next();
                log.debug("Found implementation: {}", t);
				implementations.add(t);
			} catch(ServiceConfigurationError e) {
				// TODO add more details like which class with qualified name
                log.warn(String.format("An implementation for %s could not be loaded! Exception message: ", this.type), e);
			}
		}
		if (this.failIfEmpty && implementations.isEmpty()) {
            String exceptionText = String.format("No implementations for %s is found!", this.type);
            log.error(exceptionText);
            throw new ConfigurationException(exceptionText);
		}
        log.debug("Found {} implementations for {}", implementations.size(), this.type);
		return new HashSet<T>(implementations);
	}

	protected abstract void processImplementations(Set<T> implementations) throws ConfigurationException;
}
