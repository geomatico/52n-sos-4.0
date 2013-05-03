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

package org.n52.sos.service.it;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.n52.sos.util.MultiMaps;
import org.n52.sos.util.SetMultiMap;
import org.n52.sos.util.StringHelper;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class RequestBuilder {

    public static RequestBuilder get(String path) {
        return new RequestBuilder("GET", path);
    }

    public static RequestBuilder post(String path) {
        return new RequestBuilder("POST", path);
    }

    public static RequestBuilder put(String path) {
        return new RequestBuilder("PUT", path);
    }

    public static RequestBuilder delete(String path) {
        return new RequestBuilder("DELETE", path);
    }

    public static RequestBuilder options(String path) {
        return new RequestBuilder("OPTIONS", path);
    }

    public static RequestBuilder head(String path) {
        return new RequestBuilder("HEAD", path);
    }
    private ServletContext context = null;
    private SetMultiMap<String, String> headers = MultiMaps.newSetMultiMap();
    private SetMultiMap<String, String> query = MultiMaps.newSetMultiMap();
    private String method = null;
    private String path = null;

    private RequestBuilder(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public RequestBuilder header(String header, String value) {
        headers.add(header, value);
        return this;
    }

    public RequestBuilder contentType(String type) {
        return header("Content-Type", type);
    }

    public RequestBuilder accept(String type) {
        return header("Accept", type);
    }

    public RequestBuilder query(String key, String value) {
        query.add(key, value);
        return this;
    }

    public RequestBuilder query(Enum<?> key, String value) {
        return query(key.name(), value);
    }

    public RequestBuilder query(Enum<?> key, Enum<?> value) {
        return query(key.name(), value.name());
    }

    public RequestBuilder query(String key, Enum<?> value) {
        return query(key, value.name());
    }

    RequestBuilder context(ServletContext context) {
        this.context = context;
        return this;
    }

    public HttpServletRequest build() {
        MockHttpServletRequest req = new MockHttpServletRequest(context);
        req.setMethod(method);
        for (String header : headers.keySet()) {
            for (String value : headers.get(header)) {
                req.addHeader(header, value);
            }
        }

        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for (String key : query.keySet()) {
            Set<String> values = query.get(key);
            req.addParameter(key, values.toArray(new String[values.size()]));
            if (first) {
                queryString.append("?");
                first = false;
            } else {
                queryString.append("&");
            }
            queryString.append(key).append("=").append(StringHelper.join(",", values));
        }
        req.setQueryString(queryString.toString());
        req.setRequestURI((path == null ? "/" : path) + queryString.toString());

        return req;
    }

}
