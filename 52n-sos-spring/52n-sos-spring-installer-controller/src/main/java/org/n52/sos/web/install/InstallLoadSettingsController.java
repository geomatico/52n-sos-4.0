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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_LOAD_CONFIGURATION)
public class InstallLoadSettingsController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(InstallLoadSettingsController.class);

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void post(@RequestBody String config, HttpServletRequest req) throws JSONException, ConfigurationException {
        final HttpSession session = req.getSession();
        InstallationConfiguration c = AbstractInstallController.getSettings(session);
        JSONObject settings = new JSONObject(config);
        Iterator< ?> i = settings.keys();
        while (i.hasNext()) {
            String key = (String) i.next();
            String value = settings.getString(key);
            SettingDefinition<?, ?> def = getSettingsManager().getDefinitionByKey(key);
            if (def == null) {
                log.warn("No definition for setting with key {}", key);
                continue;
            }
            c.setSetting(def, getSettingsManager().getSettingFactory().newSettingValue(def, value));
        }
        AbstractInstallController.setSettings(session, c);
    }

    @ResponseBody
    @ExceptionHandler(ConfigurationException.class)
    public String onConfigurationError(ConfigurationException e) {
        return e.getMessage();
    }
}