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
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.ds.ConnectionProviderException;
import org.n52.sos.encode.ResponseFormatKeyType;
import org.n52.sos.exception.ConfigurationException;
import org.n52.sos.service.CodingRepository;
import org.n52.sos.service.operator.ServiceOperatorKeyType;
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
public class AdminEncodingController extends AbstractAdminController {
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

    @RequestMapping(value = ControllerConstants.Paths.ADMIN_ENCODINGS, method = RequestMethod.GET)
    public String view() throws ConnectionProviderException {
        return ControllerConstants.Views.ADMIN_ENCODINGS;
    }

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_ENCODINGS_JSON_ENDPOINT, method = RequestMethod.GET,
                    produces = ControllerConstants.MEDIA_TYPE_APPLICATION_JSON)
    public String getAll() throws JSONException, ConnectionProviderException {
        return new JSONObject()
                .put(JSONConstants.OBSERVATION_ENCODINGS_KEY, getObservationEncodings())
                .put(JSONConstants.PROCEDURE_ENCODINGS_KEY, getProcedureEncodings())
                .toString();
    }

    @ResponseBody
    @RequestMapping(value = ControllerConstants.Paths.ADMIN_ENCODINGS_JSON_ENDPOINT, method = RequestMethod.POST,
                    consumes = ControllerConstants.MEDIA_TYPE_APPLICATION_JSON)
    public void change(@RequestBody String request) throws JSONException, ConnectionProviderException {
        JSONObject json = new JSONObject(request);

        if (json.has(JSONConstants.RESPONSE_FORMAT_KEY)) {
            ServiceOperatorKeyType sokt = new ServiceOperatorKeyType(json.getString(JSONConstants.SERVICE_KEY),
                                                                     json.getString(JSONConstants.VERSION_KEY));
            ResponseFormatKeyType rfkt = new ResponseFormatKeyType(sokt, json
                    .getString(JSONConstants.RESPONSE_FORMAT_KEY));
            getSettingsManager().setActive(rfkt, json.getBoolean(JSONConstants.ACTIVE_KEY));
        } else if (json.has(JSONConstants.PROCEDURE_DESCRIPTION_FORMAT_KEY)) {
            getSettingsManager().setActive(json.getString(JSONConstants.PROCEDURE_DESCRIPTION_FORMAT_KEY),
                                           json.getBoolean(JSONConstants.ACTIVE_KEY));

        } else {
            throw new JSONException("Invalid JSON");
        }
    }

    protected JSONArray getObservationEncodings() throws ConnectionProviderException, ConfigurationException,
                                                         JSONException {
        JSONArray joes = new JSONArray();
        final Map<ServiceOperatorKeyType, Set<String>> oes = CodingRepository.getInstance()
                .getAllSupportedResponseFormats();
        for (ServiceOperatorKeyType sokt : oes.keySet()) {
            for (String responseFormat : oes.get(sokt)) {
                ResponseFormatKeyType rfkt = new ResponseFormatKeyType(sokt, responseFormat);
                joes.put(new JSONObject()
                        .put(JSONConstants.SERVICE_KEY, rfkt.getService())
                        .put(JSONConstants.VERSION_KEY, rfkt.getVersion())
                        .put(JSONConstants.RESPONSE_FORMAT_KEY, rfkt.getResponseFormat())
                        .put(JSONConstants.ACTIVE_KEY, getSettingsManager().isActive(rfkt)));
            }
        }
        return joes;
    }

    protected JSONArray getProcedureEncodings() throws JSONException, ConnectionProviderException,
                                                       ConfigurationException {
        JSONArray jpes = new JSONArray();
        final Set<String> pes = CodingRepository.getInstance().getAllProcedureDescriptionFormats();
        for (String pe : pes) {
            jpes.put(new JSONObject()
                    .put(JSONConstants.PROCEDURE_DESCRIPTION_FORMAT_KEY, pe)
                    .put(JSONConstants.ACTIVE_KEY, getSettingsManager().isActive(pe)));
        }
        return jpes;
    }
}
