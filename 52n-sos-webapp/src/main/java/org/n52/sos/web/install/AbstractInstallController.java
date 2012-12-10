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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.springframework.stereotype.Controller;

@Controller
public abstract class AbstractInstallController extends AbstractController {

    protected boolean wasSuccessfull(Map<String, Object> settings) {
        Boolean success = (Boolean) settings.get(InstallConstants.SUCCESS);
        if (success != null) {
            settings.remove(InstallConstants.SUCCESS);
            return success.booleanValue();
        }
        return false;
    }

    protected Map<String, Object> getSettings(HttpSession s) {
        s.removeAttribute(ControllerConstants.ERROR_MESSAGE_ATTRIBUTE);
        Map<String, Object> settings = new HashMap<String, Object>();
        Enumeration< ? > e = s.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            settings.put(key, s.getAttribute(key));
        }
        return settings;
    }

    protected Map<String, String> getParameters(HttpServletRequest req) {
        Map<String, String> parameters = new HashMap<String, String>();
        Enumeration< ? > e = req.getParameterNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            parameters.put(key, req.getParameter(key));
        }
        return parameters;
    }

    protected void setSettings(HttpSession session, Map<String, Object> settings) {
        for (Map.Entry<String, Object> e : settings.entrySet()) {
            session.setAttribute(e.getKey(), e.getValue());
        }
    }

    protected boolean hasError(Map<String, Object> settings) {
        return settings.containsKey(ControllerConstants.ERROR_MESSAGE_ATTRIBUTE);
    }

    protected Map<String, Object> error(Map<String, Object> settings, String message) {
        log.error(message);
        settings.put(ControllerConstants.ERROR_MESSAGE_ATTRIBUTE, message);
        settings.put(InstallConstants.SUCCESS, Boolean.FALSE);
        return settings;
    }

    protected Map<String, Object> error(Map<String, Object> settings, String message, Throwable t) {
        log.error(message, t);
        settings.put(ControllerConstants.ERROR_MESSAGE_ATTRIBUTE, message);
        settings.put(InstallConstants.SUCCESS, Boolean.FALSE);
        return settings;
    }

    protected Map<String, Object> success(Map<String, Object> settings) {
        settings.put(InstallConstants.SUCCESS, Boolean.TRUE);
        return settings;
    }
}
