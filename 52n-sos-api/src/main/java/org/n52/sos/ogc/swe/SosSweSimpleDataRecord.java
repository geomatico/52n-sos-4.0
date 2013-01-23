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

public class SosSweSimpleDataRecord extends SosSweAbstractDataComponent implements AbstractDataRecord {
    /**
     * SimpleDataRecord fields
     */
    private List<SosSweField> fields;

    @Override
    public List<SosSweField> getFields() {
        return fields;
    }

    @Override
    public void setFields(List<SosSweField> fields) {
        this.fields = fields;
    }

    @Override
    public void addField(SosSweField field) {
        if (fields == null) {
            fields = new ArrayList<SosSweField>();
        }
        this.fields.add(field);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + super.hashCode();
        hash = 53 * hash + (this.getFields() != null ? this.getFields().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SosSweDataRecord other = (SosSweDataRecord) obj;
        if (this.getFields() != other.getFields() && (this.getFields() == null || !this.getFields().equals(other.getFields()))) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public boolean isSetFields() {
        return fields != null && !fields.isEmpty();
    }
}
