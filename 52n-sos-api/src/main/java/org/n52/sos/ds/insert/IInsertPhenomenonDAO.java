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

import org.n52.sos.ogc.om.AbstractSosPhenomenon;
import org.n52.sos.ogc.om.SosCompositePhenomenon;
import org.n52.sos.ogc.om.SosObservableProperty;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Interface for inserting new phenomena and composite phenomena into the SOS
 * DB. Please remember to insert the relationships to offerings and procedures
 * after inserting new phenomena. This should be realized by using the
 * InsertRelationshipDAO. Remember that the relationship between composite
 * phenomenon and phenomena are realized through a foreign key column in the
 * phenomenon table, that references on the compound_phenomenon table. So, if
 * you want to insert phenomena, which are components of a composite phenomenon,
 * either ensure that the composite phenomenon is already contained in the
 * database or just use the methods for inserting phenomena into the database!
 * 
 */
public interface IInsertPhenomenonDAO {

    /**
     * method for inserting a new phenomenon into the SOS DB.
     * 
     * @param phenomenon_id
     *            String id of the phenomenon
     * @param phenomenon_description
     *            String description of the phenomenon
     * @param unit
     *            String unit of the phenomenon (e.g. 'cm' or 'degree')
     * @param applicationLink
     *            String application link of the phenomenon
     * @param valueType
     *            String the type of the values which belong to the new
     *            phenomenon
     * @param compPhenId
     *            String the id of the composite phenomenon, this phenomenon is
     *            part of
     * @throws SQLException
     *             if insertion of a phenomenon failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertPhenomenon(String phenomenon_id, String phenomenon_description, String unit,
            String applicationLink, String valueType, String compPhenId, Connection trCon) throws SQLException,
            OwsExceptionReport;

    /**
     * method for inserting new phenomenon into the SOS DB
     * 
     * @param phenomenon
     *            Phenomenon which should be inserted
     * @throws SQLException
     *             if insertion of a phenomenon failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertPhenomenon(AbstractSosPhenomenon phenomenon, Connection trCon) throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a composite phenomenon into the database
     * 
     * @param compPhenId
     *            id of the composite phenomenon
     * @param compPhenDesc
     *            description of the composite phenomenon
     * @param phenomenonComponents
     *            phenomenon components of the composite phenomenon
     * @throws SQLException
     *             if insertion of a composite phenomenon failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertCompositePhenomenon(String compPhenId, String compPhenDesc,
            List<SosObservableProperty> phenomenonComponents, Connection trCon) throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a composite phenomenon into database
     * 
     * @param compPhenId
     *            id of the composite phenomenon which should be inserted
     * @param compPhenDesc
     *            description of the composite phenomenon which should be
     *            inserted
     * @throws SQLException
     *             if insertion of a composite phenomenon failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertCompositePhenomenon(String compPhenId, String compPhenDesc, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * method for inserting a composite phenomenon into the SOS database
     * 
     * @param compPhen
     *            The composite phenomenon which should be inserted
     * @throws SQLException
     *             if insertion of a composite failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertCompositePhenomenon(SosCompositePhenomenon compPhen, Connection trCon) throws SQLException,
            OwsExceptionReport;

    /**
     * method for inserting composite phenomena into the database
     * 
     * @param compPhens
     *            collection of the composite phenomena, which should be
     *            inserted
     * @throws SQLException
     *             if insertion of a composite phenomenon failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertCompositePhenomena(List<SosCompositePhenomenon> compPhens, Connection trCon)
            throws SQLException, OwsExceptionReport;

    /**
     * method for inserting phenomena into the DB
     * 
     * @param phenomenona
     *            ArrayList wiht Phenomenon which contains the phenomena which
     *            should be inserted
     * @throws SQLException
     *             if insertion of a phenomenon failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertPhenomena(List<SosObservableProperty> phenomenona, Connection trCon) throws SQLException,
            OwsExceptionReport;
}
