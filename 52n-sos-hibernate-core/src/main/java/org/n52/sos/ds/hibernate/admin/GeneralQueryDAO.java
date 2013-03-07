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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.ReturningWork;
import org.n52.sos.ds.IGeneralQueryDAO;

/**
 * class that deals with crud operations related to SOS DB.
 *
 * @author Shubham Sachdeva
 *
 */
public class GeneralQueryDAO extends AbstractSettingsDAO implements IGeneralQueryDAO {

	/**
	 * Method which query the SOS DB
	 *
	 * @param query normal sql query concerning any table
	 *
	 * @return query result
	 *
	 * @throws SQLException
	 */
	@Override
	public QueryResult query(String query) throws SQLException {
		String q = query.toLowerCase();

		if (q.contains("update ")
				|| q.contains("insert ")
				|| q.contains("delete ")) {
			return update(query);
		} else if (q.contains("alter ")
				|| q.contains("create ")
				|| q.contains("drop ")
				|| q.contains("truncate ")
				|| q.contains("rename ")) {
			return modify(q);
		} else {
			return select(q);
		}
	}

	/**
	 * Execute select query
	 *
	 * @param query
	 *
	 * @return table containing the result of query
	 */
	public QueryResult select(final String query) {
		Object connection = null;
		try {
			connection = getConnection();
			if (connection instanceof Connection) {
				return select(query, (Connection) connection);
			} else if (connection instanceof Session) {
				return ((Session) connection).doReturningWork(new ReturningWork<QueryResult>() {
					@Override
					public QueryResult execute(Connection cnctn) throws SQLException {
						return select(query, cnctn);
					}
				});
			} else {
                return new QueryResult("Unable to execute the query. Cause: Unknown connection object: " + connection.getClass(), true);
			}
		} catch (Exception ex) {
			log.error("Unable to execute the query.", ex);
			return new QueryResult("Unable to execute the query. Cause : " + ex.getMessage(), true);
		} finally {
			returnConnection(connection);
		}
	}
    
	private QueryResult select(String query, Connection conn) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			ResultSetMetaData metaData = rs.getMetaData();
			int columnCount = metaData.getColumnCount();
            QueryResult q = new QueryResult();
            
            LinkedList<String> names = new LinkedList<String>();
            for (int i = 1; i <= columnCount; ++i) {
				names.add(metaData.getColumnLabel(i));
			}
            q.setColumnNames(names);
            
            while (rs.next()) {
                Row c = new Row();
				for (int i = 1; i <= columnCount; ++i) {
					c.addValue(rs.getString(i));
				}
                q.addRow(c);
			}
            return q;
		} catch (Exception ex) {
			log.error("Unable to execute the query.", ex);
            throw new RuntimeException(ex);
			//return new QueryResult("Unable to execute the query. Cause: " + ex.getMessage(), true);
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
	 * Execute update queries like update, insert or delete
	 *
	 * @param query
	 *
	 * @return string mentioning number of affected rows
	 *
	 * @throws SQLException
	 */
	public QueryResult update(final String query) throws SQLException {
		Object connection = null;
		try {
			connection = getConnection();
			if (connection instanceof Connection) {
				return update(query, (Connection) connection);
			} else if (connection instanceof Session) {
				Session s = (Session) connection;
				Transaction t = s.beginTransaction();
				try {
					QueryResult result = s.doReturningWork(new ReturningWork<QueryResult>() {
						@Override
						public QueryResult execute(Connection cnctn) throws SQLException {
							return update(query, cnctn);
						}
					});
					t.commit();
					return result;
				} catch (HibernateException e) {
					t.rollback();
					throw e;
				}
			} else {
				return new QueryResult("Unable to execute the query. Cause: Unknown connection object: " + connection.getClass(), true);
			}
		} catch (Exception ex) {
			log.error("Unable to execute the query.", ex);
			return new QueryResult("Unable to execute the query. Cause: " + ex.getMessage(), true);
		} finally {
			returnConnection(connection);
		}

	}

	private QueryResult update(String query, Connection con) throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			int result = stmt.executeUpdate(query);
            return new QueryResult(result + " rows affected!", false);
		} catch (Exception ex) {
			log.error("Unable to execute the query.", ex);
            return new QueryResult("Unable to execute the query. Cause: " + ex.getMessage(), true);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
				}
			}
		}

	}

	/**
	 * Executes queries that modify db i.e. alter, drop, truncate etc
	 *
	 * @param query
	 *
	 * @return success if query executed successfully, failure otherwise
	 *
	 * @throws SQLException
	 */
	public QueryResult modify(final String query) throws SQLException {
		Object connection = null;
		try {
			connection = getConnection();
			if (connection instanceof Connection) {
				return modify(query, (Connection) connection);
			} else if (connection instanceof Session) {
				Session s = (Session) connection;
				Transaction t = s.beginTransaction();
				try {
					QueryResult result = s.doReturningWork(new ReturningWork<QueryResult>() {
						@Override
						public QueryResult execute(Connection cnctn) throws SQLException {
							return modify(query, cnctn);
						}
					});
					t.commit();
					return result;
				} catch (HibernateException e) {
					t.rollback();
					throw e;
				}
			} else {
                return new QueryResult("Unable to execute the query. Cause: Unknown connection object: " + connection.getClass(), true);
			}
		} catch (Exception ex) {
			log.error("Unable to execute the query.", ex);
			return new QueryResult("Unable to execute the query. Cause: " + ex.getMessage(), true);
		} finally {
			returnConnection(connection);
		}
	}
	
	private QueryResult modify(String query, Connection con) throws SQLException {
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			stmt.execute(query);
            return new QueryResult("Success!", false);
		} catch (Exception ex) {
			log.error("Unable to execute the query.", ex);
            return new QueryResult("Unable to execute the query. Cause: " + ex.getMessage(), true);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
