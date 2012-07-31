/***************************************************************
 Copyright (C) 2012
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

package org.n52.sos.ds.insert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sensorML.SensorSystem;

/**
 * Interface for inserting new procedure into the SOS DB. Please remember to
 * insert the relationship to features of interest and phenomena after inserting
 * new procedures. This should be realized through the InsertRelationshipsDAO.
 * 
 */
public interface IInsertProcedureDAO {

    /**
     * method for intserting a new procedure (sensor or sensor group) into the
     * SOS DB.
     * 
     * @param procedure_id
     *            String the procedure id (id of a sensor or sensor group)
     * @param procedure_name
     *            String name of the procedure
     * @param procedure_description
     *            String description of the procedure
     * @param sml_file
     *            complete sensorML description in String representation
     * @throws SQLException
     *             if insertion of a procedure failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcedure(String procedure_id, String[] parent_procedure_ids, String description_url, String description_type, String sml_file,
           Connection trCon) throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a procedure into the SOS database
     * 
     * @param procedure
     *            Procedure which should be inserted
     * @throws SQLException
     *             if insertion of a procedure failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcedure(SensorSystem procedure, Connection trCon) throws SQLException, OwsExceptionReport;

    /**
     * method for inserting multiple procedures into the SOS database
     * 
     * @param procedures
     *            ArrayList with Procedure containing the procedures which
     *            should be inserted
     * @throws SQLException
     *             if insertion of a procedure failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcedures(List<SensorSystem> procedures, Connection trCon) throws SQLException,
            OwsExceptionReport;

}
