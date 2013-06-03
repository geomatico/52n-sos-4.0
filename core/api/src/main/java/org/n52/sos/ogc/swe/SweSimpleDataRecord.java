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
package org.n52.sos.ogc.swe;

import java.util.ArrayList;
import java.util.List;

public class SweSimpleDataRecord extends SweAbstractDataComponent implements DataRecord {
    /**
     * SimpleDataRecord fields
     */
    private List<SweField> fields;

    @Override
    public List<SweField> getFields() {
        return fields;
    }

    @Override
    public SweSimpleDataRecord setFields(final List<SweField> fields) {
        this.fields = fields;
        return this;
    }

    @Override
    public SweSimpleDataRecord addField(final SweField field) {
        if (fields == null) {
            fields = new ArrayList<SweField>();
        }
        fields.add(field);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 53;
        int hash = 7;
        hash = prime * hash + super.hashCode();
        hash = prime * hash + (getFields() != null ? getFields().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SweDataRecord other = (SweDataRecord) obj;
        if (getFields() != other.getFields() && (getFields() == null || !getFields().equals(other.getFields()))) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public boolean isSetFields() {
        return fields != null && !fields.isEmpty();
    }
}
