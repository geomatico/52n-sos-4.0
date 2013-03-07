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
package org.n52.sos.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingDefinitionGroup;
import org.n52.sos.config.SettingType;
import org.n52.sos.config.settings.IntegerSettingDefinition;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class SettingDefinitionEncoder {

    private static final String MAXIMUM_EXCLUSIVE = "maximumExclusive";
    private static final String MINIMUM_EXCLUSIVE = "minimumExclusive";
    private static final String MINIMUM = "minimum";
    private static final String MAXIMUM = "maximum";
    private static final String DESCRIPTION_KEY = "description";
    private static final String TITLE_KEY = "title";
    private static final String REQUIRED_KEY = "required";
    private static final String TYPE_KEY = "type";
    private static final String STRING_TYPE = "string";
    private static final String NUMBER_TYPE = "number";
    private static final String BOOLEAN_TYPE = "boolean";
    private static final String INTEGER_TYPE = "integer";
    private static final String DEFAULT = "default";
    private static final String SECTIONS_KEY = "sections";
    private static final String SETTINGS_KEY = "settings";

    public JSONObject encode(Map<SettingDefinitionGroup, Set<SettingDefinition<?, ?>>> grouped) throws JSONException {
        JSONArray sections = new JSONArray();
        List<SettingDefinitionGroup> sortedGroups = new ArrayList<SettingDefinitionGroup>(grouped.keySet());
        Collections.sort(sortedGroups);
        for (SettingDefinitionGroup group : sortedGroups) {
            sections.put(new JSONObject()
                    .put(TITLE_KEY, group.getTitle())
                    .put(DESCRIPTION_KEY, group.getDescription())
                    .put(SETTINGS_KEY, encode(grouped.get(group))));
        }
        return new JSONObject().put(SECTIONS_KEY, sections);
    }

    public JSONObject encode(Set<SettingDefinition<?, ?>> settings) throws JSONException {
        JSONObject j = new JSONObject();
        List<SettingDefinition<?,?>> sorted = new ArrayList<SettingDefinition<?, ?>>(settings);
        Collections.sort(sorted);
        for (SettingDefinition<?, ?> def : sorted) {
            j.put(def.getKey(), encode(def));
        }
        return j;
    }

    public JSONObject encode(SettingDefinition<?, ?> def) throws JSONException {
        JSONObject j = new JSONObject()
                .put(TITLE_KEY, def.getTitle())
                .put(DESCRIPTION_KEY, def.getDescription())
                .put(TYPE_KEY, getType(def))
                .put(REQUIRED_KEY, !def.isOptional())
                .put(DEFAULT, def.hasDefaultValue() ? encodeValue(def) : null);


        if (def.getType() == SettingType.INTEGER && def instanceof IntegerSettingDefinition) {
            IntegerSettingDefinition iDef = (IntegerSettingDefinition) def;
            if (iDef.hasMinimum()) {
                j.put(MINIMUM, iDef.getMinimum());
                j.put(MINIMUM_EXCLUSIVE, iDef.isExclusiveMinimum());
            }
            if (iDef.hasMaximum()) {
                j.put(MAXIMUM, iDef.getMaximum());
                j.put(MAXIMUM_EXCLUSIVE, iDef.isExclusiveMaximum());
            }
        }
        return j;
    }

    private String getType(SettingDefinition<?, ?> def) {
        switch (def.getType()) {
        case INTEGER:
            return INTEGER_TYPE;
        case NUMERIC:
            return NUMBER_TYPE;
        case BOOLEAN:
            return BOOLEAN_TYPE;
        case FILE:
        case STRING:
        case URI:
            return STRING_TYPE;
        default:
            throw new IllegalArgumentException(String.format("Unknown Type %s", def.getType()));
        }
    }

    private Object encodeValue(SettingDefinition<?, ?> def) {
        switch (def.getType()) {
        case FILE:
        case URI:
            return def.getDefaultValue().toString();
        case BOOLEAN:
        case INTEGER:
        case NUMERIC:
        case STRING:
            return def.getDefaultValue();
        default:
            throw new IllegalArgumentException(String.format("Unknown Type %s", def.getType()));
        }
    }
}
