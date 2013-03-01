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
package org.hibernate.spatial.dialect.postgis;

import org.hibernate.HibernateException;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.spatial.GeometrySqlTypeDescriptor;
import org.hibernate.spatial.GeometryType;
import org.hibernate.spatial.SpatialAggregate;
import org.hibernate.spatial.SpatialDialect;
import org.hibernate.spatial.SpatialFunction;
import org.hibernate.spatial.SpatialRelation;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * Extends the PostgreSQLDialect by also including information on spatial
 * operators, constructors and processing functions.
 * 
 * @author Karel Maesen
 */
//TODO review super class: PostgreSQL82Dialect instead of PostgreSQLDialect?
public class PostgisDialect52N extends PostgreSQLDialect implements SpatialDialect {
    private static final long serialVersionUID = 1L;

    public PostgisDialect52N() {
        super();
        registerTypesAndFunctions();
    }

    protected final void registerTypesAndFunctions() {

        registerColumnType(java.sql.Types.STRUCT, "geometry");
        // registering OGC functions
        // (spec_simplefeatures_sql_99-04.pdf)

        // section 2.1.1.1
        // Registerfunction calls for registering geometry functions:
        // first argument is the OGC standard functionname, second the name as
        // it occurs in the spatial dialect
        registerFunction("dimension", new StandardSQLFunction("st_dimension", StandardBasicTypes.INTEGER));
        registerFunction("geometrytype", new StandardSQLFunction("st_geometrytype", StandardBasicTypes.STRING));
        registerFunction("srid", new StandardSQLFunction("st_srid", StandardBasicTypes.INTEGER));
        registerFunction("envelope", new StandardSQLFunction("st_envelope", GeometryType.INSTANCE));
        registerFunction("astext", new StandardSQLFunction("st_astext", StandardBasicTypes.STRING));
        registerFunction("asbinary", new StandardSQLFunction("st_asbinary", StandardBasicTypes.BINARY));
        registerFunction("isempty", new StandardSQLFunction("st_isempty", StandardBasicTypes.BOOLEAN));
        registerFunction("issimple", new StandardSQLFunction("st_issimple", StandardBasicTypes.BOOLEAN));
        registerFunction("boundary", new StandardSQLFunction("st_boundary", GeometryType.INSTANCE));

        // Register functions for spatial relation constructs
        registerFunction("overlaps", new StandardSQLFunction("st_overlaps", StandardBasicTypes.BOOLEAN));
        registerFunction("intersects", new StandardSQLFunction("st_intersects", StandardBasicTypes.BOOLEAN));
        registerFunction("equals", new StandardSQLFunction("st_equals", StandardBasicTypes.BOOLEAN));
        registerFunction("contains", new StandardSQLFunction("st_contains", StandardBasicTypes.BOOLEAN));
        registerFunction("crosses", new StandardSQLFunction("st_crosses", StandardBasicTypes.BOOLEAN));
        registerFunction("disjoint", new StandardSQLFunction("st_disjoint", StandardBasicTypes.BOOLEAN));
        registerFunction("touches", new StandardSQLFunction("st_touches", StandardBasicTypes.BOOLEAN));
        registerFunction("within", new StandardSQLFunction("st_within", StandardBasicTypes.BOOLEAN));
        registerFunction("relate", new StandardSQLFunction("st_relate", StandardBasicTypes.BOOLEAN));

        // register the spatial analysis functions
        registerFunction("distance", new StandardSQLFunction("st_distance", StandardBasicTypes.DOUBLE));
        registerFunction("buffer", new StandardSQLFunction("st_buffer", GeometryType.INSTANCE));
        registerFunction("convexhull", new StandardSQLFunction("st_convexhull", GeometryType.INSTANCE));
        registerFunction("difference", new StandardSQLFunction("st_difference", GeometryType.INSTANCE));
        registerFunction("intersection", new StandardSQLFunction("st_intersection", new GeometryType()));
        registerFunction("symdifference", new StandardSQLFunction("st_symdifference", GeometryType.INSTANCE));
        registerFunction("geomunion", new StandardSQLFunction("st_union", GeometryType.INSTANCE));

        // register Spatial Aggregate function
        registerFunction("extent", new StandardSQLFunction("st_extent", GeometryType.INSTANCE));

        // other common functions
        registerFunction("dwithin", new StandardSQLFunction("st_dwithin", StandardBasicTypes.BOOLEAN));
        registerFunction("transform", new StandardSQLFunction("st_transform", GeometryType.INSTANCE));
    }

    // TODO the getTypeName() override is necessary in the absence of HHH-6074

    /**
     * Get the name of the database type associated with the given
     * {@link java.sql.Types} typecode with the given storage specification
     * parameters. In the case of typecode == 3000, it returns this dialect's
     * spatial type which is <code>GEOMETRY</code>.
     * 
     * @param code
     *            The {@link java.sql.Types} typecode
     * @param length
     *            The datatype length
     * @param precision
     *            The datatype precision
     * @param scale
     *            The datatype scale
     * @return
     * @throws HibernateException
     */
    @Override
    public String getTypeName(int code, long length, int precision, int scale) throws HibernateException {
        if (code == 3000) {
            return "GEOMETRY";
        }
        return super.getTypeName(code, length, precision, scale);
    }

    @Override
    public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
        if (sqlTypeDescriptor instanceof GeometrySqlTypeDescriptor) {
            return PGGeometryTypeDescriptor52N.INSTANCE;
        }
        return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
    }

    @Override
    public String getSpatialRelateSQL(String columnName, int spatialRelation) {
        switch (spatialRelation) {
        case SpatialRelation.WITHIN:
            return " ST_within(" + columnName + ",?)";
        case SpatialRelation.CONTAINS:
            return " ST_contains(" + columnName + ", ?)";
        case SpatialRelation.CROSSES:
            return " ST_crosses(" + columnName + ", ?)";
        case SpatialRelation.OVERLAPS:
            return " ST_overlaps(" + columnName + ", ?)";
        case SpatialRelation.DISJOINT:
            return " ST_disjoint(" + columnName + ", ?)";
        case SpatialRelation.INTERSECTS:
            return " ST_intersects(" + columnName + ", ?)";
        case SpatialRelation.TOUCHES:
            return " ST_touches(" + columnName + ", ?)";
        case SpatialRelation.EQUALS:
            return " ST_equals(" + columnName + ", ?)";
        default:
            throw new IllegalArgumentException("Spatial relation is not known by this dialect");
        }

    }

    @Override
    public String getDWithinSQL(String columnName) {
        return "ST_DWithin(" + columnName + ",?,?)";
    }

    @Override
    public String getHavingSridSQL(String columnName) {
        return "( ST_srid(" + columnName + ") = ?)";
    }

    @Override
    public String getIsEmptySQL(String columnName, boolean isEmpty) {
        String emptyExpr = " ST_IsEmpty(" + columnName + ") ";
        return isEmpty ? emptyExpr : "( NOT " + emptyExpr + ")";
    }

    @Override
    public String getSpatialFilterExpression(String columnName) {
        return "(" + columnName + " && ? ) ";
    }

    @Override
    public String getSpatialAggregateSQL(String columnName, int aggregation) {
        switch (aggregation) {
        case SpatialAggregate.EXTENT:
            StringBuilder stbuf = new StringBuilder();
            stbuf.append("ST_Extent(").append(columnName).append(")");
            return stbuf.toString();
        default:
            throw new IllegalArgumentException("Aggregation of type " + aggregation
                    + " are not supported by this dialect");
        }
    }

    @Override
    public boolean supportsFiltering() {
        return true;
    }

    @Override
    public boolean supports(SpatialFunction function) {
        return (getFunctions().get(function.toString()) != null);
    }
}
