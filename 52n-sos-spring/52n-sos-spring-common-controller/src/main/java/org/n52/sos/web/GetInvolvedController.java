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
package org.n52.sos.web;

import java.net.URI;

import org.n52.sos.config.SettingValue;
import org.n52.sos.config.SettingsManager;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.config.ConfigurationException;
import org.n52.sos.service.ServiceSettings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
@RequestMapping(ControllerConstants.Paths.GET_INVOLVED)
public class GetInvolvedController extends AbstractController {
    public static final String SERVICE_URL_MODEL_ATTRIBUTE = "serviceUrl";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView view() {
        SettingValue<URI> setting = null;
        try {
            setting = SettingsManager.getInstance().getSetting(ServiceSettings.SERVICE_URL_DEFINITION);
        } catch (ConfigurationException ex) {
            log.error("Could not load service url", ex);
        } catch (ConnectionProviderException ex) {
            log.error("Could not load service url", ex);
        }

        return new ModelAndView(ControllerConstants.Views.GET_INVOLVED, SERVICE_URL_MODEL_ATTRIBUTE,
                                (setting == null) ? "" : setting.getValue() == null ? "" : setting.getValue());
    }
}
