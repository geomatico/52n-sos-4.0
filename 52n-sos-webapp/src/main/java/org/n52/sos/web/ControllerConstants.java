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

package org.n52.sos.web;

public interface ControllerConstants {

    public static interface Views {
        public static final String INDEX = "index";
        public static final String CLIENT = "client";
        public static final String ADMIN_INDEX = "admin/index";
        public static final String ADMIN_DATABASE = "admin/database";
        public static final String ADMIN_LOGIN = "admin/login";
        public static final String ADMIN_RESET = "admin/reset";
        public static final String ADMIN_SETTINGS = "admin/settings";
        public static final String INSTALL_INDEX = "install/index";
        public static final String INSTALL_DATABASE = "install/database";
        public static final String INSTALL_SETTINGS = "install/settings";
        public static final String INSTALL_FINISH = "install/finish";
        public static final String INSTALL_LOAD_SETTINGS = "install/load";
    }

    public static interface Paths {
        public static final String ROOT = "/";
        public static final String INDEX = "/index";
        public static final String CLIENT = "/client";
        public static final String ADMIN_ROOT = "/admin";
        public static final String ADMIN_INDEX = "/admin/index";
        public static final String ADMIN_SETTINGS = "/admin/settings";
        public static final String ADMIN_SETTINGS_DUMP = "/admin/settings.json";
        public static final String ADMIN_SETTINGS_UPDATE = "/admin/settings";
        public static final String ADMIN_DATABASE = "/admin/database";
        public static final String ADMIN_DATABASE_EXECUTE = "/admin/database";
        public static final String ADMIN_RELOAD_CAPABILITIES_CACHE = "/admin/cache/reload";
        public static final String ADMIN_DATABASE_REMOVE_TEST_DATA = "/admin/database/testdata/remove";
        public static final String ADMIN_DATABASE_CREATE_TEST_DATA = "/admin/database/testdata/create";

        public static final String ADMIN_RESET = "/admin/reset";
        public static final String INSTALL_ROOT = "/install";
        public static final String INSTALL_INDEX = "/install/index";
        public static final String INSTALL_DATABASE_CONFIGURATION = "/install/database";
        public static final String INSTALL_SETTINGS = "/install/settings";
        public static final String INSTALL_FINISH = "/install/finish";
        public static final String INSTALL_LOAD_CONFIGURATION = "/install/load";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/j_spring_security_logout";
    }

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /* SQL file paths */
    public static final String INSERT_TEST_DATA_SQL_FILE = "/sql/insert_test_data.sql";
    public static final String REMOVE_TEST_DATA_SQL_FILE = "/sql/remove_test_data.sql";
    public static final String CREATE_DATAMODEL_SQL_FILE = "/sql/script_20_create.sql";
}
