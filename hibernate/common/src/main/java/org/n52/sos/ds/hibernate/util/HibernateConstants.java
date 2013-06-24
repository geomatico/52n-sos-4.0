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
package org.n52.sos.ds.hibernate.util;

public interface HibernateConstants {
    String DIALECT = "hibernate.dialect";
    String DRIVER_CLASS = "hibernate.connection.driver_class";
    String CONNECTION_PROVIDER_CLASS = "hibernate.connection.provider_class";
    String CONNECTION_URL = "hibernate.connection.url";
    String CONNECTION_USERNAME = "hibernate.connection.username";
    String CONNECTION_PASSWORD = "hibernate.connection.password";
    String DEFAULT_CATALOG = "hibernate.default_catalog";
    String DEFAULT_SCHEMA = "hibernate.default_schema";
    String CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";
    String HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
    String CONNECTION_POOL_SIZE = "hibernate.connection.pool_size";
    String CONNECTION_RELEASE_MODE = "hibernate.connection.release_mode";
    String CURRENT_SESSION_CONTEXT = "hibernate.current_session_context_class";
    String CONNECTION_RELEASE_MODE_AFTER_TRANSACTION = "after_transaction";
    String CONNECTION_RELEASE_MODE_AFTER_STATEMENT = "after_statement";
    String CONNECTION_RELEASE_MODE_ON_CLOSE = "on_close";
    String CONNECTION_RELEASE_MODE_AUTO = "auto";
    String THREAD_LOCAL_SESSION_CONTEXT = "thread";
    String HBM2DDL_UPDATE = "update";
    String HBM2DDL_VALIDATE = "validate";
    String HBM2DDL_CREATE = "create";
    String HBM2DDL_CREATE_DROP = "create-drop";
    String C3P0_MIN_SIZE = "hibernate.c3p0.min_size";
    String C3P0_MAX_SIZE = "hibernate.c3p0.max_size";
    String C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";
    String C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
    String C3P0_TIMEOUT = "hibernate.c3p0.timeout";
    String C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements";
    String CONNECTION_AUTO_RECONNECT = "hibernate.connection.autoReconnect";
    String CONNECTION_AUTO_RECONNECT_FOR_POOLS =
            "hibernate.connection.autoReconnectForPools";
    String CONNECTION_TEST_ON_BORROW = "hibernate.connection.testOnBorrow";
    String MAX_FETCH_DEPTH = "hibernate.max_fetch_depth";
}
