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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.n52.sos.ds.DeleteDeletedObservationDAO;
import org.n52.sos.exception.ows.concrete.NoImplementationFoundException;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.web.ControllerConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
@RequestMapping(value = ControllerConstants.Paths.ADMIN_DATABASE_DELETE_DELETED_OBSERVATIONS)
public class AdminDeleteDeletedObservationsController extends AbstractAdminController {
    private DeleteDeletedObservationDAO dao;

    private DeleteDeletedObservationDAO getDAO() throws NoImplementationFoundException {
        if (this.dao == null) {
            ServiceLoader<DeleteDeletedObservationDAO> sl = ServiceLoader.load(DeleteDeletedObservationDAO.class);
            Iterator<DeleteDeletedObservationDAO> i = sl.iterator();
            this.dao = i.hasNext() ? i.next() : null;
            if (this.dao == null) {
                throw new NoImplementationFoundException(DeleteDeletedObservationDAO.class);
            }
        }
        return this.dao;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(NoImplementationFoundException.class)
    public String onError(NoImplementationFoundException e) {
        return "The operation is not supported by this SOS";
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete() throws NoImplementationFoundException, OwsExceptionReport {
        getDAO().deleteDeletedObservations();
    }
}
