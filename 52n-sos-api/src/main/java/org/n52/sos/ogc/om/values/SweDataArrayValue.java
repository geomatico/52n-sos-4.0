/**
 * Copyright (C) 2012
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
package org.n52.sos.ogc.om.values;

import java.util.HashMap;
import java.util.Map;

import org.n52.sos.ogc.gml.time.ITime;

public class SweDataArrayValue implements IValue<Map<ITime, Map<String, IValue>>> {
    
    private Map<ITime, Map<String, IValue>> values;
    
    private String unit;

    @Override
    public void setValue(Map<ITime, Map<String, IValue>> value) {
        this.values = value;
    }

    @Override
    public Map<ITime, Map<String, IValue>> getValue() {
        return values;
    }

    @Override
    public void setUnit(String unit) {
        // do nothing
    }

    @Override
    public String getUnit() {
        return unit;
    }
    
    public void addValue(ITime time, String observedProperty, IValue value) {
        if (values == null) {
            values = new HashMap<ITime, Map<String,IValue>>(0);
        }
		Map<String, IValue> obsPropValue = values.get(time);
		if (obsPropValue == null) {
			values.put(time, obsPropValue = new HashMap<String, IValue>());
		}
        obsPropValue.put(observedProperty, value);
    }
}
