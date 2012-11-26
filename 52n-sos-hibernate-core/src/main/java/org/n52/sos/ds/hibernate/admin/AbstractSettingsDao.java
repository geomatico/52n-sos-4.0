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
package org.n52.sos.ds.hibernate.admin;

import org.n52.sos.ds.IInitializableDao;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.Session;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractSettingsDao implements IInitializableDao {
	protected static final Logger log = LoggerFactory.getLogger(AbstractSettingsDao.class);
	protected static final String TABLE = HibernateConstants.TABLE_NAME_SETTINGS;
	protected static final String KEY = HibernateConstants.PARAMETER_SETTING_KEY;
	protected static final String VALUE = HibernateConstants.PARAMETER_SETTING_VALUE;
	
	private String driver;
	private String connectionString;
	private String user;
	private String pass;
	
	public void initialize(String driver, String connectionString, String user, String pass) {
		this.driver = driver;
		this.connectionString = connectionString;
		this.user = user;
		this.pass = pass;
	}
	
	@Override
	public void initialize(Properties properties) {
		initialize(properties.getProperty(HibernateConstants.DRIVER_PROPERTY), 
				   properties.getProperty(HibernateConstants.CONNECTION_STRING_PROPERTY), 
				   properties.getProperty(HibernateConstants.USER_PROPERTY),
				   properties.getProperty(HibernateConstants.PASS_PROPERTY));
	}
	
	protected Object getConnection() throws SQLException {
		if (Configurator.getInstance() != null) {
			log.debug("Configurator is present. Using ConnectionProvider");
			return Configurator.getInstance().getConnectionProvider().getConnection();
		}
		
		try {
			Class.forName(this.driver);
			return DriverManager.getConnection(this.connectionString, this.user, this.pass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} 
	}
	
	protected void returnConnection(Object o) {
		if (o instanceof Session) {
			Configurator.getInstance().getConnectionProvider().returnConnection(o);
		} else if (o instanceof Connection) {
			try {
				if (o != null) {
					((Connection) o).close();
				}
			} catch (SQLException e) {}
		}
	} 
}
