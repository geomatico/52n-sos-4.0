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

import java.util.ArrayList;
import java.util.List;

import org.n52.sos.ogc.om.TimeValuePair;

public class TVPValue implements IValue<List<TimeValuePair>> {

    private List<TimeValuePair> values = new ArrayList<TimeValuePair>(0);
    
    private String unit;

    @Override
    public void setValue(List<TimeValuePair> value) {
        this.values = value;
    }

    @Override
    public List<TimeValuePair> getValue() {
        return values;
    }
    
    public void addValue(TimeValuePair value) {
        this.values.add(value);
    }
    
    public void addValues(List<TimeValuePair> values) {
        this.values.addAll(values);
    }

    @Override
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String getUnit() {
        return this.unit;
    }



}
