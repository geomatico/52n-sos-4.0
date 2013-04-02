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
    /*
     * this class contains unnecessary raw types as the OpenJDK
     * 1.6.0 comiler will fail on wrong incompatiple type errors
     */
    public JSONObject encode(Map<SettingDefinitionGroup, Set<SettingDefinition>> grouped) throws JSONException {
        JSONArray sections = new JSONArray();
        List<SettingDefinitionGroup> sortedGroups = new ArrayList<SettingDefinitionGroup>(grouped.keySet());
        Collections.sort(sortedGroups);
        for (SettingDefinitionGroup group : sortedGroups) {
            sections.put(new JSONObject()
                    .put(JSONConstants.TITLE_KEY, group.getTitle())
                    .put(JSONConstants.DESCRIPTION_KEY, group.getDescription())
                    .put(JSONConstants.SETTINGS_KEY, encode(grouped.get(group))));
        }
        return new JSONObject().put(JSONConstants.SECTIONS_KEY, sections);
    }

    public JSONObject encode(Set<SettingDefinition> settings) throws JSONException {
        JSONObject j = new JSONObject();
        List<SettingDefinition> sorted = new ArrayList<SettingDefinition>(settings);
        Collections.sort(sorted);
        for (SettingDefinition def : sorted) {
            j.put(def.getKey(), encode(def));
        }
        return j;
    }

    public JSONObject encode(SettingDefinition def) throws JSONException {
        JSONObject j = new JSONObject()
                .put(JSONConstants.TITLE_KEY, def.getTitle())
                .put(JSONConstants.DESCRIPTION_KEY, def.getDescription())
                .put(JSONConstants.TYPE_KEY, getType(def))
                .put(JSONConstants.REQUIRED_KEY, !def.isOptional())
                .put(JSONConstants.DEFAULT, def.hasDefaultValue() ? encodeValue(def) : null);


        if (def.getType() == SettingType.INTEGER && def instanceof IntegerSettingDefinition) {
            IntegerSettingDefinition iDef = (IntegerSettingDefinition) def;
            if (iDef.hasMinimum()) {
                j.put(JSONConstants.MINIMUM_KEY, iDef.getMinimum());
                j.put(JSONConstants.MINIMUM_EXCLUSIVE_KEY, iDef.isExclusiveMinimum());
            }
            if (iDef.hasMaximum()) {
                j.put(JSONConstants.MAXIMUM_KEY, iDef.getMaximum());
                j.put(JSONConstants.MAXIMUM_EXCLUSIVE_KEY, iDef.isExclusiveMaximum());
            }
        }
        return j;
    }

    private String getType(SettingDefinition def) {
        switch (def.getType()) {
            case INTEGER:
                return JSONConstants.INTEGER_TYPE;
            case NUMERIC:
                return JSONConstants.NUMBER_TYPE;
            case BOOLEAN:
                return JSONConstants.BOOLEAN_TYPE;
            case FILE:
            case STRING:
            case URI:
                return JSONConstants.STRING_TYPE;
            default:
                throw new IllegalArgumentException(String.format("Unknown Type %s", def.getType()));
        }
    }

    private Object encodeValue(SettingDefinition def) {
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
