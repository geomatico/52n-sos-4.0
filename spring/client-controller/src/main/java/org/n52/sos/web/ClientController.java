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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.n52.sos.request.operator.RequestOperatorKeyType;
import org.n52.sos.service.Configurator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(ControllerConstants.Paths.CLIENT)
public class ClientController extends AbstractController {

    public static final String BINDINGS = "bindings";
    public static final String VERSIONS = "versions";
    public static final String OPERATIONS = "operations";

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get() {
        if (Configurator.getInstance() != null) {
            Map<String, Object> map = new HashMap<String, Object>(2);
            Set<RequestOperatorKeyType> ops = Configurator.getInstance().getRequestOperatorRepository()
                    .getActiveRequestOperatorKeyTypes();
            Set<String> operations = new HashSet<String>(ops.size());
            for (RequestOperatorKeyType op : ops) {
                operations.add(op.getOperationName());
            }
            map.put(OPERATIONS, operations);
            map.put(BINDINGS, Configurator.getInstance().getBindingRepository().getBindings().keySet());
            // TODO add support for different services
            map.put(VERSIONS, Configurator.getInstance().getServiceOperatorRepository().getAllSupportedVersions());
            return new ModelAndView(ControllerConstants.Views.CLIENT, map);

        } else {
            return new ModelAndView(ControllerConstants.Views.CLIENT);
        }


    }
}
