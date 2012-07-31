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

import org.n52.sos.ogc.om.features.SosAbstractFeature;
import org.n52.sos.ogc.ows.OwsExceptionReport;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Interface for inserting new feature of interests into the 52n-SOS-v2-00-00 DB
 * 
 */
public interface IInsertFoiDAO {

    /**
     * method for inserting a new feature of interest into the SOS DB. The
     * geometry which should be inserted has to have the textual form of the
     * geometries the used database system offers.
     * 
     * @param feature_of_interest_id
     *            String the id of this feature of interest
     * @param feature_of_interest_name
     *            String name of this feature of interest
     * @param feature_of_interest_description
     *            String decription of the feature of interest
     * @param geom
     *            geometry of the feature of interest
     * @param featureType
     *            type of the feature of interest (e.g. 'sa:Station')
     * @param schemalink
     *            String containing the schema link
     * @throws SQLException
     *             if insertion of a feature of interest failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertFOI(String feature_of_interest_id, String feature_of_interest_name,
            String feature_of_interest_description, Geometry geomWKT, String featureType, String schemalink,
            Connection trCon) throws SQLException, OwsExceptionReport;

    /**
     * method inserts the feature of interest into the SOS database
     * 
     * @param foi
     *            SosFeatureOfInterest which should be inserted
     * @throws SQLException
     *             if insertion of a feature of interest failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertFOI(SosAbstractFeature foi, Connection trCon) throws SQLException, OwsExceptionReport;

    /**
     * method inserts the multiple feature of interests into the SOS database
     * 
     * @param fois
     *            ArrayList with FeatureOfInterest containing the fois which
     *            should be inserted
     * @throws SQLException
     *             if insertion of a feature of interest failed
     * @throws OwsExceptionReport
     *             if getting the connection to the database from connection
     *             pool failed
     */
    public void insertFOIs(List<SosAbstractFeature> fois, Connection trCon) throws SQLException, OwsExceptionReport;

}
