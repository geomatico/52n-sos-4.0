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

import java.util.HashMap;
import java.util.Map;

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.MetaDataHandler;
import org.n52.sos.web.auth.AdministratorUserPrinciple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping({ ControllerConstants.Paths.ADMIN_INDEX, ControllerConstants.Paths.ADMIN_ROOT })
public class AdminIndexController extends AbstractController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminIndexController.class);

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get() {
        boolean warn = false;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AdministratorUserPrinciple) {
            AdministratorUserPrinciple administratorUserPrinciple = (AdministratorUserPrinciple) principal;
            if (administratorUserPrinciple.isDefaultAdmin()) {
                warn = true;
            }
        }
        Map<String, String> metadata = new HashMap<String, String>(MetaDataHandler.Metadata.values().length);
        try {
            for (MetaDataHandler.Metadata m : MetaDataHandler.Metadata.values()) {
                metadata.put(m.name(), getMetaDataHandler().get(m));
            }
        } catch (ConfigurationException ex) {
            LOG.error("Error reading metadata properties", ex);
        }
        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put("metadata", metadata);
        model.put("warning", warn);
        return new ModelAndView(ControllerConstants.Views.ADMIN_INDEX, model);
    }
}