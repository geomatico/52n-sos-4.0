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
package org.n52.sos.config.sqlite;

import java.util.Set;

import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionProvider;
import org.n52.sos.config.settings.BooleanSettingDefinition;
import org.n52.sos.config.settings.FileSettingDefinition;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.config.settings.NumericSettingDefinition;
import org.n52.sos.config.settings.StringSettingDefinition;
import org.n52.sos.config.settings.UriSettingDefinition;
import org.n52.sos.util.CollectionHelper;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SettingDefinitionProviderForTesting implements SettingDefinitionProvider {

    public static final String URI_SETTING = "uri_setting";
    public static final String DOUBLE_SETTING = "double_setting";
    public static final String INTEGER_SETTING = "integer_setting";
    public static final String FILE_SETTING = "file_setting";
    public static final String STRING_SETTING = "string_setting";
    public static final String BOOLEAN_SETTING = "boolean_setting";

    @Override
    public Set<SettingDefinition<?, ?>> getSettingDefinitions() {
        return CollectionHelper.<SettingDefinition<?, ?>>set(
                new BooleanSettingDefinition().setKey(BOOLEAN_SETTING),
                new NumericSettingDefinition().setKey(DOUBLE_SETTING),
                new IntegerSettingDefinition().setKey(INTEGER_SETTING),
                new UriSettingDefinition().setKey(URI_SETTING),
                new FileSettingDefinition().setKey(FILE_SETTING),
                new StringSettingDefinition().setKey(STRING_SETTING));
    }
}
