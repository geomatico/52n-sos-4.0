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
package org.n52.sos.web.install;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.ds.Datasource;
import org.n52.sos.util.CollectionHelper;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.SettingDefinitionEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_DATASOURCE_DIALECTS)
public class InstallDatasourceSettingsController extends AbstractController {
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET,
                    produces = "application/json; charset=UTF-8")
    public String get(HttpSession session) throws JSONException {
        InstallationConfiguration c =
                AbstractInstallController.getSettings(session);
        Map<String, Datasource> datasources = getDatasources();
        return encode(c, datasources).toString(4);
    }

    private JSONObject encode(InstallationConfiguration c,
                              Map<String, Datasource> dialects) throws
            JSONException {
        SettingDefinitionEncoder enc = new SettingDefinitionEncoder();
        JSONObject j = new JSONObject();
        for (String dialect : dialects.keySet()) {
            boolean selected = false;
            if (c.getDatasource() != null &&
                c.getDatasource().getDialectName().equals(dialect)) {
                selected = true;
            }
            Datasource d = dialects.get(dialect);
            Set<SettingDefinition<?, ?>> defs = d.getSettingDefinitions();
            if (selected) {
                for (SettingDefinition<?, ?> def : defs) {
                    setDefaultValue(c, def);
                }
            }
            JSONObject settings = enc.encode(enc.sortByGroup(defs));
            j.put(dialect, new JSONObject()
                    .put("settings", settings)
                    .put("needsSchema", d.needsSchema())
                    .put("supportsTestData", d.supportsTestData())
                    .put("selected", selected));
        }
        return j;
    }

    protected Map<String, Datasource> getDatasources() {
        ServiceLoader<Datasource> load =
                ServiceLoader.load(Datasource.class);
        Map<String, Datasource> dialects = CollectionHelper.map();
        for (Datasource dd : load) {
            dialects.put(dd.getDialectName(), dd);
        }
        return dialects;
    }

    @SuppressWarnings("unchecked")
    protected void setDefaultValue(InstallationConfiguration c,
                                   SettingDefinition<?, ?> def) {
        Object val = c.getDatabaseSetting(def.getKey());
        if (val != null) {
            switch (def.getType()) {
                case BOOLEAN:
                    SettingDefinition<?, Boolean> bsd =
                            (SettingDefinition<?, Boolean>) def;
                    bsd.setDefaultValue((Boolean) val);
                    break;
                case FILE:
                    SettingDefinition<?, File> fsd =
                            (SettingDefinition<?, File>) def;
                    fsd.setDefaultValue((File) val);
                    break;
                case INTEGER:
                    SettingDefinition<?, Integer> isd =
                            (SettingDefinition<?, Integer>) def;
                    isd.setDefaultValue((Integer) val);
                    break;
                case NUMERIC:
                    SettingDefinition<?, Double> dsd =
                            (SettingDefinition<?, Double>) def;
                    dsd.setDefaultValue((Double) val);
                    break;
                case STRING:
                    SettingDefinition<?, String> ssd =
                            (SettingDefinition<?, String>) def;
                    ssd.setDefaultValue((String) val);
                    break;
                case URI:
                    SettingDefinition<?, URI> usd =
                            (SettingDefinition<?, URI>) def;
                    usd.setDefaultValue((URI) val);
                    break;
            }
        }
    }
}
