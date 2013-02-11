package org.n52.sos.web.json.impl;

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


import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.n52.sos.config.settings.IntegerSettingDefinition;
import org.n52.sos.web.json.AbstractSettingDefinitionEncoder;

/**
 * @author Christian Autermann <c.autermann@52north.org>
 */
public class IntegerSettingDefinitionEncoder extends AbstractSettingDefinitionEncoder<Integer, IntegerSettingDefinition> {

    public static final String MAXIMUM_EXCLUSIVE = "maximumExclusive";
    public static final String MINIMUM_EXCLUSIVE = "minimumExclusive";
    public static final String MINIMUM = "minimum";
    public static final String MAXIMUM = "maximum";

    public IntegerSettingDefinitionEncoder() {
        super(IntegerSettingDefinition.class);
    }

    @Override
    protected Object encodeValue(Integer value) {
        return value;
    }

    @Override
    protected String getType(IntegerSettingDefinition t) {
        return INTEGER_TYPE;
    }

    @Override
    protected JSONObject encodeAsJson(IntegerSettingDefinition o) throws JSONException {
        JSONObject j = super.encodeAsJson(o);
        if (o.hasMinimum()) {
            j.put(MINIMUM, o.getMinimum());
            j.put(MINIMUM_EXCLUSIVE, o.isExclusiveMinimum());
        }
        if (o.hasMaximum()) {
            j.put(MAXIMUM, o.getMaximum());
            j.put(MAXIMUM_EXCLUSIVE, o.isExclusiveMaximum());
        }
        return j;
    }
}
