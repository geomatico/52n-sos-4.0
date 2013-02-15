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

import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.NumericSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;

/**
 *
 * Interface for setting definitions that can be used within the Service. Defined settings will be presented in the
 * administrator and installer view.
 * <p/>
 * @see ISettingDefinitionProvider
 * @see SettingDefinitionGroup
 * @see SettingsManager
 * @see FileSettingDefinition
 * @see BooleanSettingDefinition
 * @see IntegerSettingDefinition
 * @see NumericSettingDefinition
 * @see StringSettingDefinition
 * @see UriSettingDefinition
 * <p/>
 * @param <S> The type of the implementing class
 * @param <T> The type of the value
 * <p/>
 * @author Christian Autermann <c.autermann@52north.org>
 * @since 4.0
 */
public interface ISettingDefinition<S extends ISettingDefinition<S, T>, T> extends IOrdered<S> {

    public String getKey();

    public String getTitle();

    public String getDescription();

    public boolean isOptional();

    public T getDefaultValue();

    public SettingDefinitionGroup getGroup();

    public boolean hasTitle();

    public boolean hasDescription();

    public boolean hasDefaultValue();

    public boolean hasGroup();

    public S setKey(String key);

    public S setTitle(String title);

    public S setDescription(String description);

    public S setOptional(boolean optional);

    public S setDefaultValue(T defaultValue);

    public S setGroup(SettingDefinitionGroup group);

    public SettingType getType();
}
