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
package org.n52.sos.ds.hibernate.admin;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.n52.sos.ds.IAdminUserDao;
import org.n52.sos.ds.hibernate.util.HibernateConstants;
import org.n52.sos.service.AdminUser;

public class AdminUserDao extends SettingsDao implements IAdminUserDao {
	
	@Override
	public void saveAdminUser(AdminUser adminUser) throws SQLException {
		Map<String, String> settings = new HashMap<String, String>(2);
		settings.put(HibernateConstants.ADMIN_USERNAME_KEY, adminUser.getUsername());
		settings.put(HibernateConstants.ADMIN_PASSWORD_KEY, adminUser.getPasswordHash());
		save(settings);
	}
	
	@Override
	public AdminUser getAdminUser() throws SQLException {
		Map<String,String> settings = get(HibernateConstants.ADMIN_USERNAME_KEY,
										  HibernateConstants.ADMIN_PASSWORD_KEY);
		return new AdminUser(settings.get(HibernateConstants.ADMIN_USERNAME_KEY),
							 settings.get(HibernateConstants.ADMIN_PASSWORD_KEY));
	}
	
	@Override
	public void setAdminUserName(String name) throws SQLException {
		save(Collections.singletonMap(HibernateConstants.ADMIN_USERNAME_KEY, name));
		
	}

	@Override
	public void setAdminPassword(String password) throws SQLException {
		save(Collections.singletonMap(HibernateConstants.ADMIN_PASSWORD_KEY, password));
	}
	
}
 