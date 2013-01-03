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

package org.n52.sos.web.admin;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Enumeration;
import java.util.Map;
import java.util.ServiceLoader;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.ds.ISettingsDao;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.service.AdminUser;
import org.n52.sos.service.ConfigurationException;
import org.n52.sos.service.Configurator;
import org.n52.sos.service.Setting;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.ControllerUtils;
import org.n52.sos.web.admin.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminSettingsController extends AbstractController {

    private static final String CURRENT_PASSWORD_PARAMETER = "current_password";

    @Autowired
    private UserService userService;

    private ServiceLoader<ISettingsDao> daoServiceLoader = ServiceLoader.load(ISettingsDao.class);

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public Map<String, String> getData() {
        try {
            ISettingsDao setting = daoServiceLoader.iterator().next();
            return setting.get();
        } catch (Exception ex) {
            /* TODO error handling */
            log.error("Error", ex);
            throw new RuntimeException(ex);
        }
    }

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_SETTINGS, method = RequestMethod.GET)
    public ModelAndView displaySettings() {
        return new ModelAndView(ControllerConstants.Views.ADMIN_SETTINGS, getData());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public String unauthorized(BadCredentialsException ex) {
        return ex.getMessage();
    }

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_SETTINGS_UPDATE, method = RequestMethod.POST)
    public void updateSettings(HttpServletRequest request, HttpServletResponse response) throws BadCredentialsException {
        log.info("Updating Settings");
        try {
            String password = request.getParameter(HibernateConstants.ADMIN_PASSWORD_KEY);
            String username = request.getParameter(HibernateConstants.ADMIN_USERNAME_KEY);
            String currentPassword = request.getParameter(CURRENT_PASSWORD_PARAMETER);
            AdminUser admin = userService.getAdmin();

            if ( (password != null && !password.isEmpty())
                    || (username != null && !username.isEmpty() && !username.equals(admin.getUsername()))) {
                if (currentPassword == null) {
                    throw new BadCredentialsException("You have to submit your current password.");
                }

                userService.authenticate(new UsernamePasswordAuthenticationToken(admin.getUsername(), currentPassword));

                if (password != null && !password.isEmpty()) {
                    if (username != null && !username.isEmpty() && !username.equals(admin.getUsername())) {
                        getUserService().saveAdmin(new AdminUser(username, password));
                    }
                    else {
                        getUserService().setAdminPassword(password);
                    }
                }
                else {
                    getUserService().setAdminUserName(username);
                }

            }

            ISettingsDao dao = daoServiceLoader.iterator().next();
            Map<String, String> currentSettings = dao.get();

            Map<Setting, String> settings = new EnumMap<Setting, String>(Setting.class);

            Enumeration< ? > names = request.getParameterNames();
            while (names.hasMoreElements()) {
                try {
                    String name = (String) names.nextElement();
                    Setting setting = Setting.valueOf(name);
                    String value = setting.parse(request.getParameter(setting.name()));
                    if (setting.isAllowedValue(value)) {
                        String currentValue = currentSettings.get(setting.name());
                        /* only update settings that changed */
                        if (currentValue == null || !currentValue.equals(value)) {
                            settings.put(setting, value);
                        }
                    }
                    else {
                        /* TODO throw 400 */
                    }
                }
                catch (IllegalArgumentException e) {/* do not fail on additional parameters */
                }
            }

            if (log.isDebugEnabled()) {
                for (Map.Entry<Setting, String> e : settings.entrySet()) {
                    log.info("Saving Setting: (\"{}\" => \"{}\")", e.getKey().name(), e.getValue());
                }
            }
            dao.save(ControllerUtils.toStringMap(settings));

            for (Setting setting : settings.keySet()) {
                try {
                    Configurator.getInstance().changeSetting(setting, settings.get(setting));
                }
                catch (ConfigurationException ex) {
                    log.error("Error applying settings", ex);
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
        catch (SQLException e) {
            log.error("Error saving settings", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_SETTINGS_DUMP, method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public @ResponseBody String dump() {
        try {
            ISettingsDao dao = daoServiceLoader.iterator().next();
            Map<String, String> settings = dao.get();
            /* do not export the admin password */
            settings.remove(HibernateConstants.ADMIN_PASSWORD_KEY);
            return new JSONObject(settings).toString(4);
        }
        catch (Exception ex) {
            log.error("Could not load settings", ex);
            throw new RuntimeException(ex);
        }
    }
}