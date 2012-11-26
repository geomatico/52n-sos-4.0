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

package org.n52.sos.web;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlUtils {

    private static final Logger log = LoggerFactory.getLogger(SqlUtils.class);

    /*
     * TODO find a working library function that can parse and execute a SQL file...
     */
    public static void executeSQLFile(Connection conn, String path) throws SQLException, IOException {
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
            log.debug("Executing SQL file {}", path);
            while ( (strLine = br.readLine()) != null) {
                strLine = strLine.trim();
                if ( (strLine.length() > 0) && ( !strLine.contains("--"))) {
                    if (strLine.equals("$$")) {
                        stringLiteral = !stringLiteral;
                    }
                    sql.append(" ").append(strLine).append(" ");
                    if ( !stringLiteral && strLine.substring(strLine.length() - 1).equals(";")) {
                        st.execute(sql.substring(0, sql.length() - 1));
                        sql = new StringBuilder();
                    }
                }
            }
        }
        finally {
            if (st != null) {
                try {
                    st.close();
                }
                catch (SQLException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    private SqlUtils() {
        // private constructor to enforce static access
    }
}
