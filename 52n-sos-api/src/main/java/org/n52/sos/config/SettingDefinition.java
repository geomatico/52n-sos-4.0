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
 *
 * Interface for setting definitions that can be used within the Service. Defined settings will be presented in the
 * administrator and installer view.
 * <p/>
 * @see ISettingDefinitionProvider
 * @see SettingDefinitionGroup
 * @see SettingsManager
 * @see org.n52.sos.config.settings.FileSettingDefinition
 * @see org.n52.sos.config.settings.BooleanSettingDefinition
 * @see org.n52.sos.config.settings.IntegerSettingDefinition
 * @see org.n52.sos.config.settings.NumericSettingDefinition
 * @see org.n52.sos.config.settings.StringSettingDefinition
 * @see org.n52.sos.config.settings.UriSettingDefinition
 * <p/>
 * @param <S> The type of the implementing class
 * @param <T> The type of the value
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public interface SettingDefinition<S extends SettingDefinition<S, T>, T> extends Ordered<S> {
    /**
     * @return the unique key of this definition
     */
    String getKey();

    /**
     * @return the title of this definition
     */
    String getTitle();

    /**
     * @return the description of this definition
     */
    String getDescription();

    /**
     * @return wether this setting is optional or required.
     */
    boolean isOptional();

    /**
     * @return the default value (or null if there is none)
     */
    T getDefaultValue();

    /**
     * @return the group of this definition
     */
    SettingDefinitionGroup getGroup();

    /**
     * @return if this definition has a non empty title
     */
    boolean hasTitle();

    /**
     * @return if this definition has a non empty description
     */
    boolean hasDescription();

    /**
     * @return if this definition has a default value
     */
    boolean hasDefaultValue();

    /**
     * @return if this definition has a group
     */
    boolean hasGroup();

    /**
     * Sets the unique identifier of this setting definition, which can be referenced by configurable classes.
     *
     * @param key the <b>unique</b> key
     *
     * @return this (for method chaining)
     */
    S setKey(String key);

    /**
     * Sets the title of this setting definition, which will be presented to the user.
     *
     * @param title the title
     *
     * @return this (for method chaining)
     */
    S setTitle(String title);

    /**
     * Sets the description of this setting definition, which should further describe the purpose of this setting. Can
     * contain XHTML markup.
     *
     * @param description the description
     *
     * @return this (for method chaining)
     */
    S setDescription(String description);

    /**
     * Sets whether this setting is optional or can be null. By default all settings are required.
     *
     * @param optional if this setting is optional
     *
     * @return this (for method chaining)
     */
    S setOptional(boolean optional);

    /**
     * Sets the default value of this setting. All required settings should have a default setting to allow a smoother
     * integration of new settings in old configurations.
     *
     * @param defaultValue the default value
     *
     * @return this (for method chaining)
     */
    S setDefaultValue(T defaultValue);

    /**
     * Sets the group of this definition. If no group is set, the setting will be moved to a default group.
     *
     * @param group the group
     *
     * @return this (for method chaining)
     */
    S setGroup(SettingDefinitionGroup group);

    /**
     * @return the type of the value of this definition
     */
    SettingType getType();
}
