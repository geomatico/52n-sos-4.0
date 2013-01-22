/**
 * Copyright (C) 2012
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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.n52.sos.service.Setting;
import org.n52.sos.web.ControllerConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.INSTALL_SETTINGS)
public class InstallSettingsController extends AbstractInstallController {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_INDEX, true));
        }
        else if (session.getAttribute(InstallConstants.DBCONFIG_COMPLETE) == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_DATABASE_CONFIGURATION, true));
        }
        return new ModelAndView(ControllerConstants.Views.INSTALL_SETTINGS, getSettings(session));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_INDEX, true));
        }
        else if (session.getAttribute(InstallConstants.DBCONFIG_COMPLETE) == null) {
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_DATABASE_CONFIGURATION, true));
        }

        Map<String, Object> settings = process(getParameters(req), getSettings(session));

        setSettings(session, settings);

        if (wasSuccessfull(settings)) {
            session.setAttribute(InstallConstants.OPTIONAL_COMPLETE, true);
            return new ModelAndView(new RedirectView(ControllerConstants.Paths.INSTALL_FINISH, true));
        }
        else {
            return new ModelAndView(ControllerConstants.Views.INSTALL_SETTINGS, settings);
        }
    }

    private Map<String, Object> process(Map<String, String> parameters, Map<String, Object> settings) {

        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Processing parameters:\n").append("{\n");
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                sb.append("\t").append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
            sb.append("}");
            log.debug(sb.toString());
        }

        for (Setting p : Setting.values()) {
            String value = parameters.get(p.name());
            if (p.isAllowedValue(value)) {
                if (p.type() == Setting.Type.FILE && value != null && !value.isEmpty()) {
                    if (new File(value).exists() || new File(getBasePath() + value).exists()) {
                        settings.put(p.name(), p.parse(value));
                    }
                    else {
                        StringBuilder exceptionText = new StringBuilder();
                        exceptionText.append("Cannot find file '").append(getBasePath()).append(value).append('\'').append(" or ").append('\'').append(value).append('\'').append("'!");
                        return error(settings, exceptionText.toString());
                    }
                }
                else {
                    settings.put(p.name(), p.parse(value));
                }
            }
            else {
                /* TODO include longer name of parameter */
                error(settings, "Could not validate '" + p.name() + "' parameter: " + value);
            }
        }
        if (hasError(settings)) {
            return settings;
        }
        return success(settings);
    }
}
