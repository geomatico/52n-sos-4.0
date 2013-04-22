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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
public abstract class AbstractProcessingInstallationController extends AbstractInstallController {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(HttpServletRequest req) throws InstallationRedirectError {
        return new ModelAndView(getStep().getView(), toModel(getSettings(checkPrevious(req))));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(HttpServletRequest req, HttpServletResponse resp) throws InstallationSettingsError, InstallationRedirectError {
        HttpSession session = checkPrevious(req);
        InstallationConfiguration c = getSettings(session);
        process(getParameters(req), c);
        setSettings(session, c);
        setComplete(session);
        return redirect(getStep().getNext().getPath());
    }
    
    protected abstract void process(Map<String, String> parameters, InstallationConfiguration c) throws InstallationSettingsError;
    
}
