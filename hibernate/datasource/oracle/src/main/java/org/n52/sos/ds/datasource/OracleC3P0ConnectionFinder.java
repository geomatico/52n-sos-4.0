package org.n52.sos.ds.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

import oracle.jdbc.OracleConnection;

import org.hibernate.spatial.dialect.oracle.ConnectionFinder;
import org.hibernate.spatial.helper.FinderException;

import com.mchange.v2.c3p0.C3P0ProxyConnection;

public class OracleC3P0ConnectionFinder implements ConnectionFinder {
	public static Connection getRawConnection(Connection con) {
		return con;
	}

	public OracleConnection find(Connection con) throws FinderException {
		Connection conn = con;
		if (con instanceof Proxy) {
			try {
				InvocationHandler handler = Proxy.getInvocationHandler(con);
				conn = (Connection) handler.invoke(con, con.getClass()
						.getMethod("getWrappedObject"), null);
			} catch (Throwable e) {
				throw new FinderException(e.getMessage());
			}
		}

		if (conn instanceof OracleConnection) {
			return (OracleConnection) conn;
		}

		if (conn instanceof C3P0ProxyConnection) {
			C3P0ProxyConnection cpCon = (C3P0ProxyConnection) conn;
			Connection unwrappedCon = null;
			try {
				Method rawConnectionMethod = getClass().getMethod(
						"getRawConnection", new Class[] { Connection.class });
				unwrappedCon = (Connection) cpCon.rawConnectionOperation(
						rawConnectionMethod, null,
						new Object[] { C3P0ProxyConnection.RAW_CONNECTION });
			} catch (Throwable ex) {
				throw new FinderException(ex.getMessage());
			}
			if (unwrappedCon != null
					&& unwrappedCon instanceof OracleConnection) {
				return (OracleConnection) unwrappedCon;
			}
		}
		throw new FinderException(
				"Couldn't get Oracle Connection in OracleConnectionFinder");
	}
}