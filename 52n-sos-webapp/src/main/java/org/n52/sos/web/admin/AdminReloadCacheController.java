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

import javax.servlet.UnavailableException;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.ControllerConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(ControllerConstants.Paths.ADMIN_RELOAD_CAPABILITIES_CACHE)
public class AdminReloadCacheController extends AbstractController {

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reload() throws OwsExceptionReport, UnavailableException {
        log.debug("Reloading Capabilitities Cache");
        if (Configurator.getInstance() != null) {
            Configurator.getInstance().getCapabilitiesCacheController().updateCacheFromDB();   
        } else {
            throw new UnavailableException("configurator is not available");
        }
    }
    
    @ExceptionHandler(OwsExceptionReport.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody String errorWhileRefreshing(OwsExceptionReport e) {
        return e.getMessage();
    }
    
    @ExceptionHandler(UnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public @ResponseBody String noConfigurator(UnavailableException e) {
        return e.getMessage();
    }
}
