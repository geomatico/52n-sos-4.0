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

import java.util.Properties;

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.util.Cleanupable;

/**
 * Interface for a connection provider that handles the connection to the
 * underlying data source (e.g. database, web service). Implementation can
 * contain a ConnectionPool.
 */
public interface ConnectionProvider extends Cleanupable {

    /**
     * Get a data source connection
     * 
     * @return Connection to the data source
     * @throws ConnectionProviderException  
     */
    Object getConnection() throws ConnectionProviderException;

    /**
     * Return the connection to the provider
     * 
     * @param connection
     *            Connection
     */
    void returnConnection(Object connection);

	/**
	 * Initializes the connection provider.
	 * 
	 * @param properties the properties
	 * 
	 * @throws ConfigurationException if the initialization failed 
	 */
	void initialize(Properties properties) throws ConfigurationException;

}
