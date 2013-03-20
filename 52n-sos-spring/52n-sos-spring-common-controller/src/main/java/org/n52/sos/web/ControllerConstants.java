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
package org.n52.sos.web;

public interface ControllerConstants {
    String MEDIA_TYPE_APPLICATION_JSON = "application/json; charset=UTF-8";
    interface Views {
        String INDEX = "index";
        String CLIENT = "client";
        String LICENSE = "license";
        String GET_INVOLVED = "get-involved";
        String ADMIN_INDEX = "admin/index";
        String ADMIN_DATABASE = "admin/database";
        String ADMIN_DATABASE_SETTINGS = "admin/database-settings";
        String ADMIN_LOGIN = "admin/login";
        String ADMIN_RESET = "admin/reset";
        String ADMIN_LIBRARY_LIST = "admin/libs";
        String ADMIN_SETTINGS = "admin/settings";
        String ADMIN_OPERATIONS = "admin/operations";
        String ADMIN_ENCODINGS = "admin/encodings";
        String INSTALL_INDEX = "install/index";
        String INSTALL_DATABASE = "install/database";
        String INSTALL_SETTINGS = "install/settings";
        String INSTALL_FINISH = "install/finish";
        String INSTALL_LOAD_SETTINGS = "install/load";
        String ADMIN_LOGGING = "admin/logging";
    }

    interface Paths {
        String ROOT = "/";
        String WSDL = "/wsdl";
        String INDEX = "/index";
        String LICENSE = "/license";
        String CLIENT = "/client";
        String GET_INVOLVED = "/get-involved";
        String SETTING_DEFINITIONS = "/settingDefinitions.json";
        String ADMIN_ROOT = "/admin";
        String ADMIN_INDEX = "/admin/index";
        String ADMIN_SETTINGS = "/admin/settings";
        String ADMIN_SETTINGS_DUMP = "/admin/settings.json";
        String ADMIN_SETTINGS_UPDATE = "/admin/settings";
        String ADMIN_DATABASE = "/admin/database";
        String ADMIN_LIBRARY_LIST = "/admin/libs";
        String ADMIN_OPERATIONS = "/admin/operations";
        String ADMIN_OPERATIONS_JSON_ENDPOINT = "/admin/operations/json";
        String ADMIN_ENCODINGS = "/admin/encodings";
        String ADMIN_ENCODINGS_JSON_ENDPOINT = "/admin/encodings/json";
        String ADMIN_DATABASE_EXECUTE = "/admin/database";
        String ADMIN_DATABASE_SETTINGS = "/admin/database/settings";
        String ADMIN_RELOAD_CAPABILITIES_CACHE = "/admin/cache/reload";
        String ADMIN_DATABASE_REMOVE_TEST_DATA = "/admin/database/testdata/remove";
        String ADMIN_DATABASE_CREATE_TEST_DATA = "/admin/database/testdata/create";
        String ADMIN_DATABASE_CLEAR = "/admin/database/clear";
        String ADMIN_RESET = "/admin/reset";
        String ADMIN_LOGGING = "/admin/logging";
        String ADMIN_LOGGING_FILE_DOWNLOAD = "/admin/logging/file";
        String INSTALL_ROOT = "/install";
        String INSTALL_INDEX = "/install/index";
        String INSTALL_DATABASE = "/install/database";
        String INSTALL_SETTINGS = "/install/settings";
        String INSTALL_FINISH = "/install/finish";
        String INSTALL_LOAD_CONFIGURATION = "/install/load";
        String LOGIN = "/login";
        String LOGOUT = "/j_spring_security_logout";
    }
    String SETTINGS_MODEL_ATTRIBUTE = "settings";
    String DATABASE_SETTINGS_MODEL_ATTRIBUTE = "databaseSettings";
    String ERROR_MODEL_ATTRIBUTE = "error";
    String JDBC_PARAMETER = "jdbc_uri";
    String LIBRARIES_MODEL_ATTRIBUTE = "libs";
    String ROLE_ADMIN = "ROLE_ADMIN";
    /* SQL file paths */
    String INSERT_TEST_DATA_SQL_FILE = "/sql/insert_test_data.sql";
    String REMOVE_TEST_DATA_SQL_FILE = "/sql/remove_test_data.sql";
    String DROP_DATAMODEL_SQL_FILE = "/sql/script_20_drop.sql";
    String CREATE_DATAMODEL_SQL_FILE = "/sql/script_20_create.sql";
    String CLEAR_DATABASE_SQL_FILE = "/sql/clear_database.sql";
    String ADMIN_USERNAME_REQUEST_PARAMETER = "admin_username";
    String ADMIN_PASSWORD_REQUEST_PARAMETER = "admin_password";
    String ADMIN_CURRENT_PASSWORD_REQUEST_PARAMETER = "current_password";
}
