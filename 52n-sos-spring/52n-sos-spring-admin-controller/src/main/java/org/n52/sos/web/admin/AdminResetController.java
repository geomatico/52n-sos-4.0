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

import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.ControllerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping(ControllerConstants.Paths.ADMIN_RESET)
public class AdminResetController extends AbstractAdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminResetController.class);

    @RequestMapping(method = RequestMethod.GET)
    public String get() {
        return ControllerConstants.Views.ADMIN_RESET;
    }

    @RequestMapping(method = RequestMethod.POST)
    public View post() throws ConfigurationException, ConnectionProviderException {
        log.debug("Resetting Service.");
        if (Configurator.getInstance() != null) {
            log.debug("Resetting configurator.");
            Configurator.getInstance().cleanup();
        }
        getDatabaseSettingsHandler().delete();
        getSettingsManager().deleteAll();
        return new RedirectView(ControllerConstants.Paths.LOGOUT, true);
    }
}
