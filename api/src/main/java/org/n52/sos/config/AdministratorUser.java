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
package org.n52.sos.config;

/**
 * Interface for users that are allowed to administer the SOS. Implementations are {@link SettingsManager} specific.
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public interface AdministratorUser {

    /**
     * Get the value of password
     * <p/>
     * @return the value of password
     */
    String getPassword();

    /**
     * Get the value of username
     * <p/>
     * @return the value of username
     */
    String getUsername();

    /**
     * Set the value of password
     * <p/>
     * @param password new value of password
     * <p/>
     * @return this
     */
    AdministratorUser setPassword(String password);

    /**
     * Set the value of username
     * <p/>
     * @param username new value of username
     * <p/>
     * @return this
     */
    AdministratorUser setUsername(String username);
}
