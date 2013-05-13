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

import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.binding.Binding;
import org.n52.sos.binding.BindingKey;
import org.n52.sos.binding.BindingRepository;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.web.ControllerConstants;
import org.n52.sos.web.JSONConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
@Controller
public class AdminBindingController extends AbstractAdminController {

    @ResponseBody
    @ExceptionHandler(JSONException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String onJSONException(JSONException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ConnectionProviderException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String onConnectionProviderException(ConnectionProviderException e) {
        return e.getMessage();
    }

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_BINDINGS,
                    method = RequestMethod.GET)
    public String view() throws ConnectionProviderException {
        return ControllerConstants.Views.ADMIN_BINDINGS;
    }

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_BINDINGS_JSON_ENDPOINT, method = RequestMethod.GET,
                    produces = ControllerConstants.MEDIA_TYPE_APPLICATION_JSON)
    public String getAll() throws JSONException, ConnectionProviderException {
        return new JSONObject().put(JSONConstants.BINDINGS_KEY, getBindings()).toString();
    }

    protected JSONArray getBindings() throws ConnectionProviderException, JSONException {
        Map<String, Binding> bindings = BindingRepository.getInstance().getAllBindings();
        JSONArray a = new JSONArray();
        for (Binding binding : bindings.values()) {
            String path = binding.getUrlPattern();
            a.put(new JSONObject()
                    .put(JSONConstants.BINDING_KEY, path)
                    .put(JSONConstants.ACTIVE_KEY, getSettingsManager().isActive(new BindingKey(path))));
        }
        return a;
    }

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_BINDINGS_JSON_ENDPOINT, method = RequestMethod.POST,
                    consumes = ControllerConstants.MEDIA_TYPE_APPLICATION_JSON)
    public void change(@RequestBody String request) throws JSONException, ConnectionProviderException {
        JSONObject json = new JSONObject(request);
        if (json.has(JSONConstants.BINDING_KEY)) {
            BindingKey key = new BindingKey(json.getString(JSONConstants.BINDING_KEY));
            getSettingsManager().setActive(key, json.getBoolean(JSONConstants.ACTIVE_KEY));
        } else {
            throw new JSONException("Invalid JSON");
        }
    }
}
