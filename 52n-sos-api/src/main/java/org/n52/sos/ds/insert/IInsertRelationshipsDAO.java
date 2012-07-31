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

/**
 * Interface for inserting relationships between procedures and phenomena,
 * procedures and features of interest and phenomena and offerings into the SOS
 * DB
 * 
 */
public interface IInsertRelationshipsDAO {

    /**
     * method for inserting a new relationship between procedure and phenomenon
     * into the SOS DB.
     * 
     * ATTENTION: Please make sure that the phenomenon, the phenomenon_id
     * references on, and the procedure, the procedure_id references on, are
     * already contained in the DB!!
     * 
     * @param procedure_id
     *            String the procedure id (id of a sensor or sensor group)
     * @param phenomenon_id
     *            String id of the phenomenon
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcPhenRelationship(String procedure_id, String phenomenon_id, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a new relationship between procedure and
     * feature_of_interest into the SOS DB.
     * 
     * ATTENTION: Please make sure that the feature of interest, the
     * feature_of_interest_id references on, and the procedure, the procedure_id
     * references on, are already contained in the DB!!
     * 
     * @param procedure_id
     *            String the procedure id (id of a sensor or sensor group)
     * @param feature_of_interest_id
     *            String id of the phenomenon
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcFoiRelationship(String procedure_id, String feature_of_interest_id, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * insert the proc phen relationships into the SOS DB
     * 
     * @param procPhenRel
     *            the proc phen relationships which should be inserted
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcPhenRelationships(List<String[]> procPhenRel, Connection trCon) throws SQLException,
            OwsExceptionReport;

    /**
     * insert the proc foi relationships into the SOS DB
     * 
     * @param procFoiRel
     *            the proc foi relationships which should be inserted
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcFoiRelationships(List<String[]> procFoiRel, Connection trCon) throws SQLException,
            OwsExceptionReport;

    /**
     * method for inserting a new relationship between procedure and
     * feature_of_interest into the SOS DB.
     * 
     * ATTENTION: Please make sure that the feature of interest, the
     * feature_of_interest_id references on, and the procedure, the procedure_id
     * references on, are already contained in the DB!!
     * 
     * @param feature_of_interest_id
     *            String id of the phenomenon
     * @param offering_id
     *            String id of the offering
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertFoiOffRelationship(String feature_of_interest_id, String offering_id, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a new relationship between phenomenon and offering
     * into the SOS DB.
     * 
     * ATTENTION: Please make sure that the phenomenon, the phenomenon_id
     * references on, and the offering, the offering_id references on, are
     * already contained in the DB!!
     * 
     * @param phenomenon_id
     *            String id of the phenomenon which is related to the passed
     *            offering (also through its id)
     * @param offering_id
     *            String id of the offering which is related to the passed
     *            phenomenon (also through its id)
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertPhenOffRelationship(String phenomenon_id, String offering_id, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a new relationship between composite phenomenon and
     * offering into the SOS DB.
     * 
     * ATTENTION: Please make sure that the composite phenomenon, the
     * composite_phenomenon_id references on, and the offering, the offering_id
     * references on, are already contained in the DB!!
     * 
     * @param composite_phenomenon_id
     *            String id of the composite phenomenon which is related to the
     *            passed offering (also through its id)
     * @param offering_id
     *            String id of the offering which is related to the passed
     *            phenomenon (also through its id)
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertCompPhenOffRelationship(String composite_phenomenon_id, String offering_id, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a new relationship between procedure and offering
     * into the SOS DB.
     * 
     * ATTENTION: Please make sure that the procedure, the procedure_id
     * references on, and the offering, the offering_id references on, are
     * already contained in the DB!!
     * 
     * @param procedure_id
     *            String the procedure id (id of a sensor or sensor group)
     * @param offering_id
     *            String id of the offering which is related to the passed
     *            phenomenon (also through its id)
     * @param trCon
     *            Connection to the database
     * @throws SQLException
     *             if insertion of a relationship failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertProcOffRelationship(String procedure_id, String offering_id, Connection trCon)
            throws SQLException, OwsExceptionReport;

}
