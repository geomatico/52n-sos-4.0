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
 * Holder for generic settings. Implementations are {@link SettingsManager} specific.
 * <p/>
 * @param <T> the value type
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public interface SettingValue<T> {

    /**
     * @return the key of this setting
     */
    String getKey();

    /**
     * @return the value
     */
    T getValue();

    /**
     * @param key the key of this setting
     * <p/>
     * @return <code>this</code>
     */
    SettingValue<T> setKey(String key);

    /**
     * @param value the value of this setting
     * <p/>
     * @return <code>this</code>
     */
    SettingValue<T> setValue(T value);

    /**
     * @return the {@code SettingType} of this value
     */
    SettingType getType();
}
