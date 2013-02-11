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
package org.n52.sos.web.json;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.encode.EncoderKey;
import org.n52.sos.encode.IEncoder;
import org.n52.sos.encode.JsonEncoderKey;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;
import org.n52.sos.service.ServiceConstants.SupportedTypeKey;
import org.n52.sos.util.Util4Exceptions;
import org.springframework.http.MediaType;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractJsonEncoder<T> implements IEncoder<JSONObject, T> {

    private final Set<EncoderKey> encoderKeys;
    private final Class<T> clazz;

    protected AbstractJsonEncoder(Class<T> clazz) {
        this.clazz = clazz;
        this.encoderKeys = Collections.<EncoderKey>singleton(new JsonEncoderKey(clazz));
    }

    @Override
    public Set<EncoderKey> getEncoderKeyType() {
        return Collections.unmodifiableSet(encoderKeys);
    }

    @Override
    public Map<SupportedTypeKey, Set<String>> getSupportedTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> getConformanceClasses() {
        return Collections.emptySet();
    }

    @Override
    public String getContentType() {
        return MediaType.APPLICATION_JSON_VALUE;
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {/* only applicable for XML */}

    @Override
    public JSONObject encode(T objectToEncode, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        return encode(objectToEncode);
    }

    @Override
    public JSONObject encode(T objectToEncode) throws OwsExceptionReport {
        try {
            return encodeAsJson(objectToEncode);
        } catch (JSONException e) {
            throw Util4Exceptions.createNoApplicableCodeException(e, String.format("Could not encode %s", clazz));
        }
    }

    protected abstract JSONObject encodeAsJson(T t) throws JSONException;
}
