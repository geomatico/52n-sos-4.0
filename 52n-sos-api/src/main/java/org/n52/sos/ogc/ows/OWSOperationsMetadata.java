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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class OWSOperationsMetadata {

    private SortedSet<OWSOperation> operations;

    private SortedMap<String, List<IOWSParameterValue>> commonValues;

    public SortedSet<OWSOperation> getOperations() {
        return Collections.unmodifiableSortedSet(operations);
    }

    public void setOperations(Collection<OWSOperation> operations) {
        this.operations = operations == null ? null : new TreeSet<OWSOperation>(operations);
    }

    public SortedMap<String, List<IOWSParameterValue>> getCommonValues() {
        return Collections.unmodifiableSortedMap(commonValues);
    }

    public void addOperation(OWSOperation operation) {
        if (operations == null) {
            operations = new TreeSet<OWSOperation>();
        }
        operations.add(operation);
    }

    public void addCommonValue(String parameterName, IOWSParameterValue value) {
        if (commonValues == null) {
            commonValues = new TreeMap<String, List<IOWSParameterValue>>();
        }
        List<IOWSParameterValue> values = commonValues.get(parameterName);
        if (values == null) {
            commonValues.put(parameterName, values = new LinkedList<IOWSParameterValue>());
        }
        values.add(value);
    }
}
