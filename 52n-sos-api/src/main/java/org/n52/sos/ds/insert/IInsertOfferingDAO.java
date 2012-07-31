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

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.n52.sos.ogc.om.SosOffering;
import org.n52.sos.ogc.ows.OwsExceptionReport;

/**
 * Interface for inserting new offerings into the SOS DB
 * 
 */
public interface IInsertOfferingDAO {

    /**
     * method for inserting a new offering.
     * 
     * @param offering_id
     *            String id of the offering
     * @param offering_name
     *            String the name of the offering
     * @param min_time
     *            min_time for observations which belong to the offering; if no
     *            observations are contained yet, insert null!
     * @param max_time
     *            max_time for observations which belong to the offering; if no
     *            observations are contained yet, insert null!
     * @throws SQLException
     *             if insertion of an offering failed
     * @throws IOException
     *             if invoking the refreshMetadata operation of the 52nSOSv2
     *             failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertOffering(String offering_id, String offering_name, String min_time, String max_time,
            Connection trCon) throws SQLException, IOException, OwsExceptionReport;

    /**
     * method for inserting a new offering
     * 
     * @param offering
     *            Offering which should be inserted
     * @throws SQLException
     *             if insertion of an offering failed
     * @throws IOException
     *             if invoking the refreshMetadata operation of the 52nSOSv2
     *             failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertOffering(SosOffering offering, Connection trCon) throws SQLException, IOException,
            OwsExceptionReport;

    /**
     * method for inserting new offerings
     * 
     * @param offerings
     *            ArrayList containing the offerings which should be inserted
     * @throws SQLException
     *             if insertion of an offering failed
     * @throws IOException
     *             if invoking the refreshMetadata operation of the 52nSOSv2
     *             failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertOfferings(List<SosOffering> offerings, Connection trCon) throws SQLException, IOException,
            OwsExceptionReport;

}
