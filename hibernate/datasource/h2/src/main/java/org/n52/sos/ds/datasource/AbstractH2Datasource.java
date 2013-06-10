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
package org.n52.sos.ds.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Table;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;
import org.n52.sos.config.SettingDefinition;
import org.n52.sos.exception.ConfigurationException;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public abstract class AbstractH2Datasource extends AbstractHibernateDatasource {
    protected static final String H2_DRIVER_CLASS = "org.h2.Driver";
    protected static final String H2_DIALECT_CLASS =
            GeoDBDialect.class.getName();
    protected static final String DEFAULT_USERNAME = "sa";
    protected static final String DEFAULT_PASSWORD = "";

    @Override
    protected Dialect createDialect() {
        return new GeoDBDialect();
    }

    @Override
    public boolean supportsClear() {
        return true;
    }

    @Override
    public Set<SettingDefinition<?, ?>> getChangableSettingDefinitions() {
        return Collections.emptySet();
    }

    @Override
    public void clear(Properties properties) {
        Map<String, Object> settings = parseDatasourceProperties(properties);
        CustomConfiguration config = getConfig(settings);
        Iterator<Table> tables = config.getTableMappings();

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = openConnection(settings);
            stmt = conn.createStatement();
            stmt.execute("set referential_integrity false");
            while (tables.hasNext()) {
                Table table = tables.next();
                if (table.isPhysicalTable()) {
                    stmt.execute("truncate table " + table.getName());
                }
            }
            stmt.execute("set referential_integrity true");
        } catch (SQLException ex) {
            throw new ConfigurationException(ex);
        } finally {
            close(stmt);
            close(conn);
        }
    }
}
