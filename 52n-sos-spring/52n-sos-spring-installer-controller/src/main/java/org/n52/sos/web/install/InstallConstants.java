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

import org.n52.sos.web.ControllerConstants;

public interface InstallConstants {
    public static final String IS_POSTGIS_INSTALLED = "SELECT version(), postgis_version()";
    public static final String GET_VERSION_OF_DATABASE_INSTALLATION = "SELECT value FROM global_settings WHERE key = 'VERSION';";

    /* request parameters */
    public static final String DRIVER_PARAMETER = "driver";
    public static final String CONNECTION_POOL_PARAMETER = "connection_pool";
    public static final String DIALECT_PARAMETER = "dialect";
    public static final String SCHEMA_PARAMETER = "schema";
    public static final String OVERWRITE_TABLES_PARAMETER = "overwrite_tables";
    public static final String CREATE_TEST_DATA_PARAMETER = "create_test_data";
    public static final String CREATE_TABLES_PARAMETER = "create_tables";
    
    public enum Step {
        /* DECLARATION ORDER IS IMPORTANT! */
        WELCOME(ControllerConstants.Paths.INSTALL_INDEX,
                ControllerConstants.Views.INSTALL_INDEX),
        DATABASE(ControllerConstants.Paths.INSTALL_DATABASE,
                 ControllerConstants.Views.INSTALL_DATABASE),
        SETTINGS(ControllerConstants.Paths.INSTALL_SETTINGS,
                 ControllerConstants.Views.INSTALL_SETTINGS),
        FINISH(ControllerConstants.Paths.INSTALL_FINISH,
               ControllerConstants.Views.INSTALL_FINISH);
        private final String path;
        private final String view;
        private final String completionAttribute;
        private Step(String path, String view) {
            this.view = view;
            this.path = path;
            this.completionAttribute = view + "_complete";
        }

        public Step getNext() {
            final Step[] all = values();
            final int me = ordinal();
            return (me < all.length - 1) ? all[me + 1] : null;
        }

        public Step getPrevious() {
            final Step[] all = values();
            final int me = ordinal();
            return (me == 0) ? null : all[me - 1];
        }

        public String getPath() {
            return path;
        }

        public String getView() {
            return view;
        }
        
         public String getCompletionAttribute() {
            return completionAttribute;
        }

    }
}
