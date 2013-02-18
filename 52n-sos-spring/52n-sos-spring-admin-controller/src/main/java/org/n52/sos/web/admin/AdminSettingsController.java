package org.n52.sos.web.admin;

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


import java.io.File;
import java.net.URI;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.IAdministratorUser;
import org.n52.sos.config.ISettingDefinition;
import org.n52.sos.config.ISettingValue;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class AdminSettingsController extends AbstractController {

    @Autowired
    private UserService userService;
    private SettingsManager sm;

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String unauthorized(BadCredentialsException ex) {
        return ex.getMessage();
    }

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_SETTINGS, method = RequestMethod.GET)
    public ModelAndView displaySettings() {
        return new ModelAndView(ControllerConstants.Views.ADMIN_SETTINGS,
                                ControllerConstants.SETTINGS_MODEL_ATTRIBUTE,
                                getData());
    }

    

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_SETTINGS_UPDATE, method = RequestMethod.POST)
    public void updateSettings(HttpServletRequest request, HttpServletResponse response, Principal user) throws AuthenticationException, ConfigurationException {
        log.info("Updating Settings");
        try {
            updateAdminUser(request, user);
            updateSettings(request);
        } catch (SQLException e) {
            log.error("Error saving settings", e);
            throw new RuntimeException(e.getMessage());
        } catch (ConnectionProviderException e1) {
            log.error("Error saving settings", e1);
            throw new RuntimeException(e1.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_SETTINGS_DUMP, method = RequestMethod.GET,
                    produces = "application/json; charset=UTF-8")
    public String dump() {
        try {
            return new JSONObject(getData()).toString(4);
        } catch (Exception ex) {
            log.error("Could not load settings", ex);
            throw new RuntimeException(ex);
        }
    }

    public Map<String, Object> getData() {
        try {
            return toSimpleMap(getSettingsManager().getSettings());
        } catch (Exception ex) {
            log.error("Error reading settings", ex);
            throw new RuntimeException(ex);
        }
    }

    private Map<String, Object> toSimpleMap(Map<ISettingDefinition<?, ?>, ISettingValue<?>> settings) throws
            ConfigurationException {
        Map<String, Object> simpleMap = new HashMap<String, Object>(settings.size());
        for (Entry<ISettingDefinition<?, ?>, ISettingValue<?>> e : settings.entrySet()) {
            simpleMap.put(e.getKey().getKey(), encodeValue(e.getValue()));
        }
        return simpleMap;
    }

    private Object encodeValue(ISettingValue<?> v) {
        if (v == null || v.getValue() == null) {
            return null;
        }
        switch (v.getType()) {
        case INTEGER:
        case NUMERIC:
        case STRING:
        case BOOLEAN:
            return v.getValue();
        case FILE:
            return ((File) v.getValue()).getPath();
        case URI:
            return ((URI) v.getValue()).toString();
        default:
            throw new IllegalArgumentException(String.format("Type %s is not supported!", v.getType()));
        }
    }

    private void logSettings(Collection<ISettingValue<?>> values) {
        if (log.isDebugEnabled()) {
            for (ISettingValue<?> value : values) {
                log.info("Saving Setting: ('{}'({}) => '{}')", value.getKey(), value.getType(), value.getValue());
            }
        }
    }

    private SettingsManager getSettingsManager() throws ConfigurationException {
        return (sm == null) ? sm = SettingsManager.getInstance() : sm;
    }
    
    private void updateSettings(HttpServletRequest request) throws RuntimeException, SQLException, ConfigurationException, ConnectionProviderException {
        Map<ISettingDefinition<?, ?>, ISettingValue<?>> changedSettings = new HashMap<ISettingDefinition<?, ?>, ISettingValue<?>>();
        for (ISettingDefinition<?, ?> def : getSettingsManager().getSettingDefinitions()) {
            ISettingValue<?> newValue = getSettingsManager().getSettingFactory()
                    .newSettingValue(def, request.getParameter(def.getKey()));
            changedSettings.put(def, newValue);
        }
        logSettings(changedSettings.values());
        for (ISettingValue<?> e : changedSettings.values()) {
            getSettingsManager().changeSetting(e);
        }
    }

    private void updateAdminUser(HttpServletRequest request, Principal user) throws AuthenticationException,
                                                                                    ConfigurationException {
        String password = request.getParameter(ControllerConstants.ADMIN_PASSWORD_REQUEST_PARAMETER);
        String username = request.getParameter(ControllerConstants.ADMIN_USERNAME_REQUEST_PARAMETER);
        String currentPassword = request.getParameter(ControllerConstants.ADMIN_CURRENT_PASSWORD_REQUEST_PARAMETER);
        updateAdminUser(password, username, currentPassword, user.getName());
    }

    private void updateAdminUser(String newPassword, String newUsername, String currentPassword, String currentUsername)
            throws AuthenticationException, ConfigurationException {
        if ((newPassword != null && !newPassword.isEmpty()) 
                || (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(currentUsername))) {
            if (currentPassword == null) {
                throw new BadCredentialsException("You have to submit your current password.");
            }
            IAdministratorUser loggedInAdmin = getUserService().authenticate(currentUsername, currentPassword);
            if (newPassword != null && !newPassword.isEmpty() && !newPassword.equals(currentPassword)) {
                getUserService().setAdminPassword(loggedInAdmin, newPassword);
            }
            if (newUsername != null && !newUsername.isEmpty() && !newUsername.equals(currentUsername)) {
                getUserService().setAdminUserName(loggedInAdmin, newUsername);
            }
        }
    }
}