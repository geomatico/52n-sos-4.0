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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.SettingDefinitionGroup;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.service.ConfigurationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
public class SettingDefinitonController extends AbstractController {

    private static final SettingDefinitionGroup DEFAULT_SETTINGS_GROUP = new SettingDefinitionGroup().setTitle("Settings");
    private SettingDefinitionEncoder encoder = new SettingDefinitionEncoder();

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.SETTING_DEFINITIONS, method = RequestMethod.GET,
                    produces = "application/json; charset=UTF-8")
    public String get() throws ConfigurationException, JSONException {
        Set<ISettingDefinition<?, ?>> defs = SettingsManager.getInstance().getSettingDefinitions();
        Map<SettingDefinitionGroup, Set<ISettingDefinition<?, ?>>> grouped = sortByGroup(defs);
        return getEncoder().encode(grouped).toString(4);
    }

    protected Map<SettingDefinitionGroup, Set<ISettingDefinition<?, ?>>> sortByGroup(Set<ISettingDefinition<?, ?>> defs) {
        Map<SettingDefinitionGroup, Set<ISettingDefinition<?, ?>>> map = new HashMap<SettingDefinitionGroup, Set<ISettingDefinition<?, ?>>>();
        for (ISettingDefinition<?, ?> def : defs) {
            SettingDefinitionGroup group = def.hasGroup() ? def.getGroup() : DEFAULT_SETTINGS_GROUP;
            Set<ISettingDefinition<?, ?>> groupDefs = map.get(group);
            if (groupDefs == null) {
                map.put(group, groupDefs = new HashSet<ISettingDefinition<?, ?>>());
            }
            groupDefs.add(def);
        }
        return map;
    }

    protected SettingDefinitionEncoder getEncoder() {
        return encoder;
    }

   
}
