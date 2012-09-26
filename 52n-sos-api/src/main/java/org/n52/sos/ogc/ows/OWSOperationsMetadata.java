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
package org.n52.sos.ogc.ows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OWSOperationsMetadata {

    private Collection<OWSOperation> operations;

    private Map<String, List<IOWSParameterValue>> commonValues;

    public Collection<OWSOperation> getOperations() {
        return operations;
    }

    public void setOperations(Collection<OWSOperation> operations) {
        this.operations = operations;
    }

    public Map<String, List<IOWSParameterValue>> getCommonValues() {
        return commonValues;
    }

    public void setCommonValues(Map<String, List<IOWSParameterValue>> commonValues) {
        this.commonValues = commonValues;
    }

    public void addOperation(OWSOperation operation) {
        if (operations == null) {
            operations = new ArrayList<OWSOperation>();
        }
        operations.add(operation);
    }

    public void addCommonValue(String parameterName, IOWSParameterValue value) {
        if (commonValues == null) {
            commonValues = new HashMap<String, List<IOWSParameterValue>>();
        }
        if (!commonValues.containsKey(parameterName)) {
            List<IOWSParameterValue> list = new ArrayList<IOWSParameterValue>(1);
            list.add(value);
            commonValues.put(parameterName, list);
        } else {
            commonValues.get(parameterName).add(value);
        }
    }
}
