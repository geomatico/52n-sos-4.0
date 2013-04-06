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

import java.io.File;
import java.net.URI;

import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.NumericSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;

/**
 * Factory to construct implementation specific {@link SettingValue}s.
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public interface SettingValueFactory {

    /**
     * Constructs a new {@code Boolean} setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<Boolean> newBooleanSettingValue(BooleanSettingDefinition setting, String stringValue);

    /**
     * Constructs a new {@code Integer} setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<Integer> newIntegerSettingValue(IntegerSettingDefinition setting, String stringValue);

    /**
     * Constructs a new {@code String} setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<String> newStringSettingValue(StringSettingDefinition setting, String stringValue);

    /**
     * Constructs a new {@code File} setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<File> newFileSettingValue(FileSettingDefinition setting, String stringValue);

    /**
     * Constructs a new {@code URI} setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<URI> newUriSettingValue(UriSettingDefinition setting, String stringValue);

    /**
     * Constructs a new {@code Double} setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<Double> newNumericSettingValue(NumericSettingDefinition setting, String stringValue);

    /**
     * Constructs a new generic setting value from the supplied definition and string value.
     * <p/>
     * @param setting the setting definition
     * @param stringValue the value as string
     * <p/>
     * @return the implementation specific {@code SettingValue}
     */
    SettingValue<?> newSettingValue(SettingDefinition<?, ?> setting, String stringValue);
}
