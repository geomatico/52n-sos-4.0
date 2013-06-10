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
package org.n52.sos.web.admin;

import static org.n52.sos.config.SettingType.BOOLEAN;
import static org.n52.sos.config.SettingType.FILE;
import static org.n52.sos.config.SettingType.INTEGER;
import static org.n52.sos.config.SettingType.NUMERIC;
import static org.n52.sos.config.SettingType.STRING;

import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingValue;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.SettingDefinitionEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.ADMIN_DATABASE_SETTINGS)
public class AdminDatasourceSettingsController extends AbstractDatasourceController {
    private static final Logger LOG = LoggerFactory
            .getLogger(AdminDatasourceSettingsController.class);
    public static final String SETTINGS = "settings";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView view() {
        try {
            return new ModelAndView(ControllerConstants.Views.ADMIN_DATASOURCE_SETTINGS,
                                    SETTINGS, encodeSettings());
          
        } catch (Exception ex) {
            LOG.error("Error reading database settings", ex);
            return new ModelAndView(ControllerConstants.Views.ADMIN_DATASOURCE_SETTINGS,
                                    ControllerConstants.ERROR_MODEL_ATTRIBUTE,
                                    ex.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView save(HttpServletRequest req) throws JSONException {

        // parse them
        Map<String, Object> newSettings =
                parseDatasourceSettings(getDatasource()
                .getChangableSettingDefinitions(), req);
        Properties settings = getSettings();

        // test them
        try {
            getDatasource().validateConnection(settings, newSettings);
            getDatasource().validatePrerequisites(settings, newSettings);
            if (getDatasource().needsSchema()) {
                if (getDatasource().checkIfSchemaExists(settings, newSettings)) {
                    getDatasource().validateSchema(settings, newSettings);
                } else {
                    return error(newSettings, "No schema is present", null);
                }
            }
        } catch (ConfigurationException e) {
            return error(newSettings, null, e);
        }

        // save them
        Properties datasourceProperties = getDatasource()
                .getDatasourceProperties(settings, newSettings);
        getDatabaseSettingsHandler().saveAll(datasourceProperties);


        // reinitialize
        if (Configurator.getInstance() != null) {
            Configurator.getInstance().cleanup();
        }
        
        Configurator.createInstance(getDatabaseSettingsHandler().getAll(),
                                    getBasePath());

        return new ModelAndView(new RedirectView(ControllerConstants.Paths.ADMIN_DATABASE_SETTINGS, true));
    }

    protected Map<String, Object> parseDatasourceSettings(
            Set<SettingDefinition<?, ?>> defs, HttpServletRequest req) {
        Map<String, String> parameters = new HashMap<String, String>(req
                .getParameterMap().size());
        Enumeration<?> e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            parameters.put(key, req.getParameter(key));
        }
        Map<String, Object> parsedSettings =
                new HashMap<String, Object>(parameters.size());
        for (SettingDefinition<?, ?> def : defs) {
            SettingValue<?> newValue = getSettingsManager().getSettingFactory()
                    .newSettingValue(def, parameters.get(def.getKey()));
            parsedSettings.put(def.getKey(), newValue.getValue());
        }
        return parsedSettings;
    }

    private ModelAndView error(Map<String, Object> newSettings,
                               String message, Throwable e) throws JSONException {
        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put(ControllerConstants.ERROR_MODEL_ATTRIBUTE,
                  (message != null) ? message : (e != null)
                                                ? e.getMessage()
                                                : "Could not save settings");
        model.put(SETTINGS, encodeSettings(newSettings));
        LOG.error("Error saving database settings: " + message, e);
        return new ModelAndView(ControllerConstants.Views.ADMIN_DATASOURCE_SETTINGS, model);
    }

    private JSONObject encodeSettings() throws JSONException {
        return encodeSettings(getDatabaseSettingsHandler().getAll());
    }

    private JSONObject encodeSettings(Properties p) throws JSONException {
        SettingDefinitionEncoder enc = new SettingDefinitionEncoder();
        Set<SettingDefinition<?, ?>> defs = getDatasource()
                .getChangableSettingDefinitions();
        for (SettingDefinition<?, ?> def : defs) {
            setDefaultValue(def, p.getProperty(def.getKey(), null));
        }
        JSONObject settings = enc.encode(enc.sortByGroup(defs));
        return new JSONObject().put(SETTINGS, settings);
    }

    private JSONObject encodeSettings(Map<String, Object> p) throws
            JSONException {
        SettingDefinitionEncoder enc = new SettingDefinitionEncoder();
        Set<SettingDefinition<?, ?>> defs = getDatasource()
                .getChangableSettingDefinitions();
        for (SettingDefinition<?, ?> def : defs) {
            setDefaultValue(def, p.get(def.getKey()));
        }
        JSONObject settings = enc.encode(enc.sortByGroup(defs));
        return new JSONObject().put(SETTINGS, settings);
    }

    @SuppressWarnings("unchecked")
    protected void setDefaultValue(SettingDefinition<?, ?> def, String sval) {
        if (sval != null) {
            Object val = getSettingsManager().getSettingFactory()
                    .newSettingValue(def, sval).getValue();
            setDefaultValue(def, val);
        }
    }

    @SuppressWarnings("unchecked")
    protected void setDefaultValue(SettingDefinition<?, ?> def, Object val) {
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
