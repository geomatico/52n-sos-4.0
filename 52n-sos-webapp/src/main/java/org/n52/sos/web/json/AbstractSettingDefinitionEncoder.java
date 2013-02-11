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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.ISettingDefinition;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractSettingDefinitionEncoder<V, T extends ISettingDefinition<V>> extends AbstractJsonEncoder<T> {

    public static final String DESCRIPTION_KEY = "description";
    public static final String KEY_KEY = "key";
    public static final String TITLE_KEY = "title";
    public static final String REQUIRED_KEY = "required";
    public static final String TYPE_KEY = "type";
    public static final String STRING_TYPE = "string";
    public static final String NUMBER_TYPE = "number";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String INTEGER_TYPE = "integer";
    public static final String DEFAULT = "default";

    public AbstractSettingDefinitionEncoder(Class<T> clazz) {
        super(clazz);
    }

    @Override
    protected JSONObject encodeAsJson(T o) throws JSONException {
        return new JSONObject()
                .put(DESCRIPTION_KEY, o.getDescription())
                .put(KEY_KEY, o.getTitle())
                .put(TITLE_KEY, o.getTitle())
                .put(REQUIRED_KEY, !o.isOptional())
                .put(TYPE_KEY, getType(o))
                .put(DEFAULT, o.hasDefaultValue() ? encodeValue(o.getDefaultValue()) : null);
    }

    protected abstract Object encodeValue(V value);

    protected abstract String getType(T t);
}
