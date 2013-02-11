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
package org.n52.sos.web.install;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <c.autermann@52north.org>
 */
public interface ErrorMessages {
    public static final String POST_GIS_IS_NOT_INSTALLED_IN_THE_DATABASE = "PostGIS is not installed in the database.";
    public static final String COULD_INSERT_TEST_DATA = "Could insert test data: %s";
    public static final String NO_DRIVER_SPECIFIED = "no driver specified";
    public static final String NO_JDBC_URL_SPECIFIED = "No JDBC URL specified.";
    public static final String COULD_NOT_WRITE_DATASOURCE_CONFIG = "Could not write datasource config: %s";
    public static final String PASSWORD_IS_INVALID = "Password is invalid.";
    public static final String CANNOT_READ_SPATIAL_REF_SYS_TABLE = "Cannot read 'spatial_ref_sys' table of PostGIS. "
                                                                   + "Please revise your database configuration.";
    public static final String COULD_NOT_LOAD_DIALECT = "Could not load dialect: %s";
    public static final String COULD_NOT_LOAD_CONNECTION_POOL = "Could not load connection pool: %s";
    public static final String COULD_NOT_VALIDATE_PARAMETER = "Could not validate '%s' parameter: %s";
    public static final String CANNOT_INSTANTIATE_CONFIGURATOR = "Cannot instantiate Configurator: %s";
    public static final String INVALID_JDBC_URL_WITH_ERROR_MESSAGE = "Invalid JDBC URL: %s";
    public static final String CANNOT_CREATE_STATEMENT = "Cannot create Statement: %s";
    public static final String COULD_NOT_CONNECT_TO_THE_DATABASE = "Could not connect to the database: %s";
    public static final String COULD_NOT_SAVE_ADMIN_CREDENTIALS = "Could not save admin credentials into the database: %s";
    public static final String INVALID_JDBC_URL = "Invalid JDBC URL.";
    public static final String USERNAME_IS_INVALID = "Username is invalid.";
    public static final String COULD_NOT_LOAD_DRIVER = "Could not load Driver: %s";
    public static final String NO_DIALECT_SPECIFIED = "no dialect specified";
    public static final String TABLES_ALREADY_CREATED_BUT_SHOULD_NOT_OVERWRITE = "Tables already created, but should not overwrite. "
                                                                                 + "Please take a look at the 'Actions' section.";
    public static final String COULD_NOT_INSERT_SETTINGS = "Could not insert settings into the database: %s";
    public static final String NO_CONNECTION_POOL_SPECIFIED = "no connection pool specified";
    public static final String COULD_NOT_CREATE_SOS_TABLES = "Could not create sos tables: %s";
    public static final String CANNOT_FIND_FILE = "Cannot find file '%s%s' or '%s'!";
    public static final String COULD_NOT_CONNECT_TO_DB_SERVER = "Could not connect to DB server: %s";
    public static final String CANNOT_CREATE_TABLES = "Cannot create tables: %s";
    public static final String NO_TABLES_AND_SHOULD_NOT_CREATE = "No tables are present in the database "
                                                                 + "and no tables should be created.";
}
