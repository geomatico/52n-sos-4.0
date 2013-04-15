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
package org.n52.sos.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQLHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SQLHelper.class);

    /*
     * TODO find a working library function that can parse and execute a SQL file...
     */
    public static void executeSQLFile(Connection conn, File path) throws SQLException, IOException {
        FileInputStream in = null;
        Statement st = null;
        BufferedReader br = null;
        try {
            /* FIXME DataInputStream!? */
            br = new BufferedReader(new InputStreamReader(new DataInputStream(in = new FileInputStream(path))));
            st = conn.createStatement();
            boolean stringLiteral = false;
            String strLine;
            StringBuilder sql = new StringBuilder();
            LOG.debug("Executing SQL file {}", path);
            while ((strLine = br.readLine()) != null) {
                strLine = strLine.trim();
                if ((strLine.length() > 0) && (!strLine.contains("--"))) {
                    if (strLine.equals("$$")) {
                        stringLiteral = !stringLiteral;
                    }
                    sql.append(" ").append(strLine).append(" ");
                    if (!stringLiteral && strLine.substring(strLine.length() - 1).equals(";")) {
                        st.execute(sql.substring(0, sql.length() - 1));
                        sql = new StringBuilder();
                    }
                }
            }
        } finally {
            close(st);
            close(in);
            close(br);
        }
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException t) {
                LOG.error(String.format("Error closing %s!", closeable.getClass()), t);
            }
        }
    }
    
    public static void close(ResultSet closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (SQLException ex) {
                LOG.error("Error closing ResultSet!", ex);
            }
        }
    }

    public static void close(Statement closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (SQLException ex) {
                LOG.error("Error closing Statement!", ex);
            }
        }
    }

    public static void close(Connection closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (SQLException ex) {
                LOG.error("Error closing Connection!", ex);
            }
        }
    }

    private SQLHelper() {
        // private constructor to enforce static access
    }
}
