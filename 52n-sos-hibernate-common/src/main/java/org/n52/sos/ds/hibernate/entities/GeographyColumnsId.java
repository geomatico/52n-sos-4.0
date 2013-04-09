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
package org.n52.sos.ds.hibernate.entities;

import java.io.Serializable;

import org.n52.sos.ds.hibernate.entities.HibernateRelations.GeoColumnsId;

public class GeographyColumnsId implements Serializable, GeoColumnsId {
    public static final String COLUMN = "FGeographyColumn";
    private static final long serialVersionUID = 2973570817118481352L;
    private String FTableCatalog;
    private String FTableSchema;
    private String FTableName;
    private String FGeographyColumn;
    private Integer coordDimension;
    private Integer srid;
    private String type;

    public GeographyColumnsId() {
    }

    public GeographyColumnsId(String FTableCatalog, String FTableSchema, String FTableName, String FGeographyColumn,
                              Integer coordDimension, Integer srid, String type) {
        this.FTableCatalog = FTableCatalog;
        this.FTableSchema = FTableSchema;
        this.FTableName = FTableName;
        this.FGeographyColumn = FGeographyColumn;
        this.coordDimension = coordDimension;
        this.srid = srid;
        this.type = type;
    }

    @Override
    public String getFTableCatalog() {
        return this.FTableCatalog;
    }

    @Override
    public void setFTableCatalog(String FTableCatalog) {
        this.FTableCatalog = FTableCatalog;
    }

    @Override
    public String getFTableSchema() {
        return this.FTableSchema;
    }

    @Override
    public void setFTableSchema(String FTableSchema) {
        this.FTableSchema = FTableSchema;
    }

    @Override
    public String getFTableName() {
        return this.FTableName;
    }

    @Override
    public void setFTableName(String FTableName) {
        this.FTableName = FTableName;
    }

    public String getFGeographyColumn() {
        return this.FGeographyColumn;
    }

    public void setFGeographyColumn(String FGeographyColumn) {
        this.FGeographyColumn = FGeographyColumn;
    }

    @Override
    public Integer getCoordDimension() {
        return this.coordDimension;
    }

    @Override
    public void setCoordDimension(Integer coordDimension) {
        this.coordDimension = coordDimension;
    }

    @Override
    public Integer getSrid() {
        return this.srid;
    }

    @Override
    public void setSrid(Integer srid) {
        this.srid = srid;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.FTableCatalog != null ? this.FTableCatalog.hashCode() : 0);
        hash = 83 * hash + (this.FTableSchema != null ? this.FTableSchema.hashCode() : 0);
        hash = 83 * hash + (this.FTableName != null ? this.FTableName.hashCode() : 0);
        hash = 83 * hash + (this.FGeographyColumn != null ? this.FGeographyColumn.hashCode() : 0);
        hash = 83 * hash + (this.coordDimension != null ? this.coordDimension.hashCode() : 0);
        hash = 83 * hash + (this.srid != null ? this.srid.hashCode() : 0);
        hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
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
        final GeographyColumnsId other = (GeographyColumnsId) obj;
        if ((this.FTableCatalog == null) ? (other.FTableCatalog != null)
            : !this.FTableCatalog.equals(other.FTableCatalog)) {
            return false;
        }
        if ((this.FTableSchema == null) ? (other.FTableSchema != null) : !this.FTableSchema.equals(other.FTableSchema)) {
            return false;
        }
        if ((this.FTableName == null) ? (other.FTableName != null) : !this.FTableName.equals(other.FTableName)) {
            return false;
        }
        if ((this.FGeographyColumn == null) ? (other.FGeographyColumn != null)
            : !this.FGeographyColumn.equals(other.FGeographyColumn)) {
            return false;
        }
        if (this.coordDimension != other.coordDimension && (this.coordDimension == null || !this.coordDimension
                .equals(other.coordDimension))) {
            return false;
        }
        if (this.srid != other.srid && (this.srid == null || !this.srid.equals(other.srid))) {
            return false;
        }
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
