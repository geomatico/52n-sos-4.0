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

import org.n52.sos.service.ConfigurationException;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.MetaDataHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping({ControllerConstants.Paths.ADMIN_INDEX, ControllerConstants.Paths.ADMIN_ROOT})
public class AdminIndexController extends AbstractController {

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get() {
        Map<String, String> model = new HashMap<String, String>(MetaDataHandler.Metadata.values().length);
        try {
            for (MetaDataHandler.Metadata m : MetaDataHandler.Metadata.values()) {
                model.put(m.name(), getMetaDataHandler().get(m));
            }
        } catch (ConfigurationException ex) {
            log.error("Error reading metadata properties", ex);
        }
        return new ModelAndView(ControllerConstants.Views.ADMIN_INDEX, model);
    }
}