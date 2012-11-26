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

package org.n52.sos.web.install;

import org.n52.sos.web.ControllerConstants;

public interface InstallConstants {

    /* SQL queries */
    public static final String CAN_CREATE_TABLES = "BEGIN; DROP TABLE IF EXISTS sos_installer_test_table; CREATE TABLE sos_installer_test_table (id integer NOT NULL); DROP TABLE sos_installer_test_table; END;";
    public static final String IS_POSTGIS_INSTALLED = "SELECT version(), postgis_version()";
    public static final String TABLES_ALREADY_EXISTENT = "SELECT observation_id FROM observation LIMIT 0;";
    public static final String CAN_READ_SPATIAL_REF_SYS = "SELECT * FROM spatial_ref_sys LIMIT 0;";

    /* session attributes */
    public static final String ERROR_MESSAGE_ATTRIBUTE = "error";
    public static final String SUCCESS = "success";
    public static final String DBCONFIG_COMPLETE = ControllerConstants.Views.INSTALL_DATABASE + "_complete";
    public static final String OPTIONAL_COMPLETE = ControllerConstants.Views.INSTALL_SETTINGS + "_complete";

    /* request parameters */
    public static final String DRIVER_PARAMETER = "driver";
    public static final String JDBC_PARAMETER = "jdbc_uri";
    public static final String OVERWRITE_TABLES_PARAMETER = "overwrite_tables";
    public static final String CREATE_TEST_DATA_PARAMETER = "create_test_data";
    public static final String CREATE_TABLES_PARAMETER = "create_tables";

}
