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
package org.n52.sos.ds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public interface IGeneralQueryDAO extends IInitializableDAO {

    public static class QueryResult {

        private boolean error;
        private String message;
        private List<String> columnNames = new ArrayList<String>(0);
        private List<Row> columns = new LinkedList<Row>();

        public QueryResult(){
            
        }
        public QueryResult(String message, boolean isError){
            this.message = message;
            this.error = isError;
        }
        
        public List<String> getColumnNames() {
            return Collections.unmodifiableList(this.columnNames);
        }

        public void setColumnNames(List<String> columnNames) {
            this.columnNames = new ArrayList<String>(columnNames);
        }

        public List<Row> getRows() {
            return Collections.unmodifiableList(this.columns);
        }

        public void addRow(Row column) {
            this.columns.add(column);
        }

        public boolean isError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class Row {

        private List<String> values = new LinkedList<String>();

        public List<String> getValues() {
            return Collections.unmodifiableList(this.values);
        }

        public void addValue(String value) {
            this.values.add(value);
        }
    }

    /**
     * Method which query the SOS DB
     *
     * @param query normal sql query concerning any table
     *
     * @return query result
     *
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public QueryResult query(String query) throws SQLException, FileNotFoundException, IOException;
}
