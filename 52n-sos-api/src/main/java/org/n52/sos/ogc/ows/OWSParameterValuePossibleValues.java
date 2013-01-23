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
package org.n52.sos.ogc.ows;

import java.util.Collection;
import java.util.HashSet;

public class OWSParameterValuePossibleValues implements IOWSParameterValue {
    
    private Collection<String> values;

    public OWSParameterValuePossibleValues(Collection<String> values) {
        super();
        this.values = values;
    }

    public OWSParameterValuePossibleValues(String valueString) {
        if (values == null) {
            values = new HashSet<String>();
        }
        values.add(valueString);
    }
    
    public OWSParameterValuePossibleValues(Enum<?> value) {
        this(value.name());
    }

    public Collection<String> getValues() {
        return values;
    }

    public void setValues(Collection<String> values) {
        this.values = values;
    }

}
