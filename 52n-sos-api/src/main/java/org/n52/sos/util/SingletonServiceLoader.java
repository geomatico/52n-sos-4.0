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

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.n52.sos.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer that loads a single instance of
 * <code>T</code> with a {@link ServiceLoader}.
 *
 * @param <T> the type to produce
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public class SingletonServiceLoader<T> implements Producer<T> {

    private static final Logger LOG = LoggerFactory.getLogger(SingletonServiceLoader.class);
    private final Class<? extends T> clazz;
    private final boolean failIfNotFound;
    private final ServiceLoader<? extends T> serviceLoader;
    private T implementation;
    private T defaultImplementation;

    public SingletonServiceLoader(Class<? extends T> c, boolean failIfNotFound) {
        this(c, failIfNotFound, null);
    }

    public SingletonServiceLoader(Class<? extends T> c, boolean failIfNotFound, T defaultImplementation) {
        this.clazz = c;
        this.failIfNotFound = failIfNotFound;
        this.serviceLoader = ServiceLoader.load(c);
        this.defaultImplementation = defaultImplementation;
    }

    @Override
    public final T get() throws ConfigurationException {
        if (implementation == null) {
            Iterator<? extends T> iter = serviceLoader.iterator();
            while (iter.hasNext() && implementation == null) {
                try {
                    implementation = iter.next();
                } catch (ServiceConfigurationError sce) {
                    LOG.warn(String.format("Implementation for %s could be loaded!", clazz), sce);
                }
            }
            if (implementation == null && defaultImplementation != null) {
                implementation = defaultImplementation;
            }
            if (implementation == null) {
                String message = String.format("No implementation for %s could be loaded!", clazz);
                if (failIfNotFound) {
                    throw new ConfigurationException(message);
                } else {
                    LOG.warn(message);
                }
            } else {
                processImplementation(implementation);
                LOG.info("Implementation for {} successfully loaded: {}", clazz, implementation);
            }
        }

        return implementation;
    }

    /**
     * Classes extending this class may overwrite the default (empty) implementation.
     * <p/>
     * @param implementation the loaded implementation
     * <p/>
     * @throws ConfigurationException if the processing fails
     */
    protected void processImplementation(T implementation) throws ConfigurationException {
    }
}
