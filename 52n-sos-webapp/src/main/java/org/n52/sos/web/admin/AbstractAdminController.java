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
package org.n52.sos.web.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.n52.sos.ds.IConnectionProvider;
import org.n52.sos.service.Configurator;
import org.n52.sos.web.AbstractController;
import org.n52.sos.web.SqlUtils;

public abstract class AbstractAdminController extends AbstractController {
       
    protected void executeSqlFile(String path) throws SQLException, FileNotFoundException {
        final File f = new File(getContext().getRealPath(path));
        if (!f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath() + " not found.");
        }
        IConnectionProvider p = Configurator.getInstance().getConnectionProvider();
        Object con = null;
        try {
            con = p.getConnection();
            if (con instanceof Connection) {
                try {
                    SqlUtils.executeSQLFile((Connection) con, f);
                }
                catch (IOException ex) {
                    throw new SQLException(ex);
                }
            }
            else if (con instanceof Session) {
                Session s = (Session) con;
                Transaction t = s.beginTransaction();
                try {
                    s.doWork(new Work() {
                        @Override
                        public void execute(Connection connection) throws SQLException {
                            try {
                                SqlUtils.executeSQLFile(connection, f);
                            }
                            catch (IOException ex) {
                                throw new SQLException(ex);
                            }
                        }
                    });
                    t.commit();
                }
                catch (HibernateException e) {
                    t.rollback();
                }
            }
            else {
                throw new SQLException("Unknown conncetion type: " + con.getClass());
            }
        }
        finally {
            p.returnConnection(con);
        }
    }
}
