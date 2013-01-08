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
/***************************************************************
 Copyright (C) 2008
 by 52 North Initiative for Geospatial Open Source Software GmbH

 Contact: Andreas Wytzisk
 52 North Initiative for Geospatial Open Source Software GmbH
 Martin-Luther-King-Weg 24
 48155 Muenster, Germany
 info@52north.org

 This program is free software; you can redistribute and/or modify it under 
 the terms of the GNU General Public License version 2 as published by the 
 Free Software Foundation.

 This program is distributed WITHOUT ANY WARRANTY; even without the implied
 WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program (see gnu-gpl v2.txt). If not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 visit the Free Software Foundation web page, http://www.fsf.org.

 Author: <LIST OF AUTHORS/EDITORS>
 Created: <CREATION DATE>
 Modified: <DATE OF LAST MODIFICATION (optional line)>
 ***************************************************************/

package org.n52.sos.ds.hibernate.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.n52.sos.ds.ISettingsDao;
import org.n52.sos.service.Setting;

/**
 * class that deals with crud operations related to globalsettings table.
 * 
 * @author Shubham Sachdeva
 * 
 */
public class SettingsDao extends AbstractSettingsDao implements ISettingsDao {
	private static final String UPDATE = "UPDATE " + TABLE + " SET " + VALUE + " = ? WHERE " + KEY + " = ?;";
	private static final String INSERT = "INSERT INTO " + TABLE + " VALUES (?, ?);";
	private static final String SELECT_ALL = "SELECT * FROM " + TABLE;

    /**
     * method for updating records of Global Settings table in the SOS DB.
     * 
	 * @param keys 
	 *        keys to retrieve
	 * @return
	 * @throws SQLException  
     * 
     */
	@Override
    public Map<String,String> get(final String... keys) throws SQLException {
		Object conn = null;
		try {
			conn = getConnection();
			if (conn instanceof Connection) {
				return get(keys, (Connection) conn);
			} else if (conn instanceof Session) {
				return ((Session) conn).doReturningWork(new ReturningWork<Map<String, String>>() {
					@Override
					public Map<String, String> execute(Connection cnctn) throws SQLException {
						return get(keys, cnctn);
					}
				});
			} else {
				throw new RuntimeException("Unknown connection type: " + conn.getClass());
			}
		} finally {
			returnConnection(conn);
		}
	}

	private Map<String, String> get(String[] keys, Connection con) throws SQLException {
		final String query;
		if (keys != null && keys.length > 0) {
			StringBuilder sb = new StringBuilder(SELECT_ALL)
					.append(" WHERE ").append(KEY)
					.append(" IN (").append("'").append(keys[0]).append("'");
			for (int i = 1; i < keys.length; ++i) {
				sb.append(",").append("'").append(keys[i]).append("'");
			}
			query = sb.append(");").toString();
		} else {
			query = SELECT_ALL;
		}
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Map<String, String> settings = new HashMap<String, String>(Setting.values().length);
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				settings.put(rs.getString(1), rs.getString(2));
			}
			return settings;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	/**
     * method for updating records of Global Settings table in the SOS DB.
     * 
     * @param settings
	 *        Map of global settings
	 * @throws SQLException  
     * 
     */
	@Override
    public void save(final Map<String, String> settings) throws SQLException {
		Object conn = null;
        try {
			conn = getConnection();
			if (conn instanceof Connection) {
				save(settings, (Connection) conn);
			} else if (conn instanceof Session) {
				Session s = (Session) conn;
				
				Transaction t = null;
				try {
					t = s.beginTransaction();
					s.doWork(new Work() {
						@Override
						public void execute(Connection cnctn) throws SQLException {
							save(settings, cnctn);
						}
					});
					t.commit();
				} catch (HibernateException e) {
					if (t != null) {
						t.rollback();
					}
					throw e;
				}
			} else {
				throw new RuntimeException("Unknown connection type: " + conn.getClass());
			}
		} finally {
			returnConnection(conn);
		}
    }
	
	@Override
    public void save(Map<String, String> settings, Connection conn) throws SQLException {
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtInsert = null;
        try {
            pstmtUpdate = conn.prepareStatement(UPDATE);
            pstmtInsert = conn.prepareStatement(INSERT);
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                pstmtUpdate.setString(1, entry.getValue());
                pstmtUpdate.setString(2, entry.getKey());
                int update = pstmtUpdate.executeUpdate();
                if (update == 0) {
                    pstmtInsert.setString(1, entry.getKey());
                    pstmtInsert.setString(2, entry.getValue());
					pstmtInsert.execute();
                }
            }
        } finally {
            if (pstmtInsert != null) { try { pstmtInsert.close(); } catch (Exception e) {} }
            if (pstmtUpdate != null) { try { pstmtUpdate.close(); } catch (Exception e) {} }
        }
    }
}
