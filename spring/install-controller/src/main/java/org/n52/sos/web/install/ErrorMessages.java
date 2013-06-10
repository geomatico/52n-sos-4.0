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
    String INVALID_DATASOURCE = "The datasource %s is invalid!";
    String POST_GIS_IS_NOT_INSTALLED_IN_THE_DATABASE = "PostGIS is not installed in the database.";
    String COULD_NOT_INSERT_TEST_DATA = "Could not insert test data: %s";
    String NO_DRIVER_SPECIFIED = "no driver specified";
    String NO_SCHEMA_SPECIFIED = "No schema specified";
    String NO_JDBC_URL_SPECIFIED = "No JDBC URL specified.";
    String COULD_NOT_WRITE_DATASOURCE_CONFIG = "Could not write datasource config: %s";
    String PASSWORD_IS_INVALID = "Password is invalid.";
    String COULD_NOT_READ_SPATIAL_REF_SYS_TABLE = "Could not read 'spatial_ref_sys' table of PostGIS. "
                                                               + "Please revise your database configuration.";
    String COULD_NOT_LOAD_DIALECT = "Could not load dialect: %s";
    String COULD_NOT_LOAD_CONNECTION_POOL = "Could not load connection pool: %s";
    String COULD_NOT_VALIDATE_PARAMETER = "Could not validate '%s' parameter: %s";
    String COULD_NOT_INSTANTIATE_CONFIGURATOR = "Could not instantiate Configurator: %s";
    String INVALID_JDBC_URL_WITH_ERROR_MESSAGE = "Invalid JDBC URL: %s";
    String CAN_NOT_CREATE_STATEMENT = "Cannot create Statement: %s";
    String COULD_NOT_CONNECT_TO_THE_DATABASE = "Could not connect to the database: %s";
    String COULD_NOT_SAVE_ADMIN_CREDENTIALS = "Could not save admin credentials into the database: %s";
    String INVALID_JDBC_URL = "Invalid JDBC URL.";
    String USERNAME_IS_INVALID = "Username is invalid.";
    String COULD_NOT_LOAD_DRIVER = "Could not load Driver: %s";
    String NO_DIALECT_SPECIFIED = "no dialect specified";
    String TABLES_ALREADY_CREATED_BUT_SHOULD_NOT_OVERWRITE =
                        "Tables already created, but should not overwrite. "
                        + "Please take a look at the 'Actions' section.";
    String COULD_NOT_INSERT_SETTINGS = "Could not insert settings into the database: %s";
    String NO_CONNECTION_POOL_SPECIFIED = "no connection pool specified";
    String COULD_NOT_CREATE_SOS_TABLES = "Could not create SOS tables: %s";
    String COULD_NOT_DROP_SOS_TABLES = "Could not drop SOS tables: %s";
    String COULD_NOT_FIND_FILE = "Could not find file '%s'!";
    String COULD_NOT_CONNECT_TO_DATABASE_SERVER = "Could not connect to DB server: %s";
    String COULD_NOT_CREATE_TABLES = "Could not create tables: %s";
    String NO_TABLES_AND_SHOULD_NOT_CREATE = "No tables are present in the database "
                                                          + "and no tables should be created.";
    String COULD_NOT_INSTANTIATE_SETTINGS_MANAGER = "Could not instantiate Settings Manager: %s";
    String NO_DEFINITON_FOUND = "No definiton found for setting with key '%s'";
    String COULD_NOT_DELETE_PREVIOUS_SETTINGS = "Could not delete previous settings: %s";
    String COULD_NOT_SET_CATALOG = "Could not set catalog search path";
    String COULD_NOT_CHECK_IF_TABLE_EXISTS = "Could not check if table '%s' exists: %s";
    String COULD_NOT_CHECK_IF_SCHEMA_EXISTS = "Could not check if schema '%s' exists: %s";
    String SCHEMA_DOES_NOT_EXIST = "Schema %s does not exist";
}
